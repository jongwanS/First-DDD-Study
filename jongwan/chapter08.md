# 애그리거트 트랜잭션 관리

### 애그리거트와 트랜잭션
````
＃ 애그리거트의 일관성이 깨지는 케이스

운영자 쓰레드 : 고객 주문에대해 배송 상태로 변경을 진행
고객 쓰레드 : `고객 배송 상태 이전` 이므로 배송지 정보를 변경.

  운영자 쓰레드             고객 쓰레드
      ↓                       ↓
주문 애그리거트 조회       주문 애그리거트 조회
      ↓                       ↓
 `배송 중 상태`로 변경       `배송지` 변경
      ↓                       ↓
     커밋                      ↓
                             커밋                                   
````
- 애그리거트의 일관성이 깨지는 문제가 발생하지 않도록 해야 한다.
- 일관성이 유지하기 위해선 아래와 같은 행위를 진행해야 한다.
  1. 운영자가 배송지 정보를 `조회 및 상태 변경`시, `고객이 애그리거트`를 `수정하지 못하도록` 처리
  2. 운영자가 배송지 조회 후, 고객이 정보를 변경하면, `애그리거트 재조회 후 수정`.
- 이와 같이 애그리거트의 일관성을 유지하기 위해서는 아래와 같은 2가지 방법을 통해 해결할 수 있다.
  - 애그리거트의 정합성 유지는 트랜잭션과 관련이 있고, `DBMS가 지원하는 트랜잭션 처리기법`이 필요하다.
    - `선점 잠금`(Pessimistic Lock)
    - `비선점 잠금`(Optimistic Lock)

### 선점 잠금
- 애그리거트를 먼저 조회한 쓰레드의 일이 종료되기 전까진, 다른 쓰레드가 해당 애그리거트 접근 못하도록 하는 방법이다.
```java

   운영자 쓰레드                    고객 쓰레드
        ↓                              ↓
주문 애그리거트 조회(선점잠금)           ↓
        ↓                      주문 애그리거트 조회(선점 잠금으로 인한 대기)
  배송상태 배송 중 으로 변경             ↓ (블로킹) 
        ↓                              ↓ (블로킹)
      커밋                             ↓ (블로킹)
                                       ↓ (주문애그리거트 조회 성공, 커밋된 최신데이터 조회함) 
                                배송지 변경 시도시 실패(운영자 쓰레드가 배송상태를 배송 중 으로 변경을 진행했으므로)
                                       ↓ 
                                   트랜잭션 해제
````
JPA 에서의 선점잠금
- JPA EntityManager 를 통해, 선점잠금 진행 (`LockModeType.PESSIMISTIC_WRITE`)
````JAVA
Order order = entityManager.find(Order.class
        ,orderNo
        ,LockModeType.PESSIMISTIC_WRITE)
````
- DB 밴더별로 잠금 구현은 다르다.

스프링 데이터 JPA 에서의 선점잠금
````JAVA

public interface MemberRepository extends Repository<Member, MemberId>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") MemberId memberId)
}
````


#### 선점 잠금과 교착 상태
- 선점 잠금 기능 사용시 `잠금 순서`에 따른 `교착 상태(dead lock)가 발생하지 않도록 주의`
````
     쓰레드 1                        쓰레드 2
        ↓                              ↓
A 애그리거트 선점 잠금 획득      B 애그리거트 선점 잠금 획득
        ↓                              ↓
B 애그리거트 선점 잠금 시도      A 애그리거트 선점 잠금 시도
        ↓                              ↓
      블로킹(쓰레드2 이미선점)         블로킹(쓰레드1 이미선점)  
        ↓                              ↓
      대기...                         대기...  
````
- 해결책
  - 잠금 획득을 시도 할때 `최대 대기 시간을 지정`
````JAVA
- JPA
Map<String,Integer> hints = new HashMap<>();
hints.put("javax,persistence.lock.timeout", 2000);
Order order = entityManager.find(Order.class
        ,orderNo
        ,LockModeType.PESSIMISTIC_WRITE, hints)
````
````JAVA
- 스프링 데이터 JPA
public interface MemberRepository extends Repository<Member, MemberId>{
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({
          @QueryHint(name = "javax.persistence.lock.timeout", value = "2000")
  })
  @Query("select m from Member m where m.id = :id")
  Optional<Member> findByIdForUpdate(@Param("id") MemberId memberId)
}
````
※ DB 벤더별로 선점 잠금 방식이 다르므로, 사용 DB에 대해 JPA가 어떤식으로 잠금을 처리하는지 확인이 필요함.

### 비선점 잠금
- 선점 잠금으로 모든 트랜잭션 충돌 문제가 해결되는 것은 아니다.
````
- 운영자가 배송 상태를 변경할때, 배송지 변경여부 체크 안 했다는 가정하에

  운영자                고객
    ↓                    ↓
 주문정보 조회            ↓
    ↓               배송지 변경 요청 (성공)
 배송 상태 변경 요청   
   (성공) 

````
- 비선점 잠금 구현시, 애그리거트 버전으로 구현할 수 있다.
````
   쓰레드1              쓰레드2
     ↓                   ↓
애그리거트 조회(v1)   애그리거트 조회(v1)
     ↓                   ↓
애그리거트 수정           ↓
     ↓                   ↓
트랜잭션 성공(v2)         ↓     
     ↓              트랜잭션 실패(v2가 이미존재하므로)
````
- JPA는 @Version 어노테이션을 통해 비선점 잠금을 구현한다.
  - 해당 버전을 통해 다른 쓰레드가 해당 애그리거트를 변경했는지 확인
  - `비선점 쿼리 실행시 수정된 행이 0개`이면, 다른 쓰레드에 의해 `애그리거트가 변경`되었다고 판단할수 있다. 
  - 어플리케이션에서는 `OptimisitcLockingFailureException`으로 트랜잭션 충돌 처리 할 수 있다.

````JAVA
public class Aggregate{
  ...
  @Version
  private long version;
}
````
````
- JPA @Version을 통한 비선점 잠금

  운영자                       고객
    ↓                           ↓
 주문정보 조회(v1)               ↓
    ↓                   배송지 변경 요청(v1 -> v2 업데이트 성공)
 배송 상태 변경 요청   
(v2 -> v3로 업데이트 시도, 실패) 
````
#### 강제 버전 증가
- 애그리거트 루트 이외에 다른 엔티티가 존재하며, `다른 엔티티의 값만 변경`을 시키면, `애그리거트 관점에서 보면 문제`가 된다. 구성요소가 변경되면 `애그리거트`가 변경된 것이다.
- 따라서 애그리거트 루트 이외에 구성 요소 상태가 변경되면, `루트 애그리거트`의 버전도 함께 증가 해야한다.
````java
@Repository
public class JpaOrderRepository implements OrderRepository{ 
  @PersistenceContext
  private EmtityManager emtityManager;
  
  @Override
  public Order findByIdOptimisticLockMode(OrderNo id){
      return entityManager.find(Order.class
              , id
              , LockMode.OPTIMISTIC_FORCE_INCREMENT);
  }
}
````