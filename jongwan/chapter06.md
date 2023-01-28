# 응용 서비스와 표현 영역

### 표현 영역과 응용 영역
- 표현 영역
  - 사용자의 요청을 해석하며, 응용 계층에게 기능을 위임한다.
  - 사용자의 요청(파라미터) 를 응용 서비스에 맞게 객체를 재 생성 한뒤, 서비스의 메서드를 호출한다.
- 응용 영역 : 사용자 요청에 대한 응답값을 리턴한다.
````JAVA
public class MemberController {
    public User join(String reqEmail, String reqPassword){
        String email = reqEmail;
        String password = reqPassword;
        //응용 계층에 전달할 객체 새로 생성
        JoinRequest joinRequest = new JoinRequest(email,password);
        //응용 계층 실행
        JoinService joinService = new JoinService();
        joinService.createUser(joinRequest);
        return new User();
    }
}
````
### 응용 서비스의 역할
- 사용자의 요청을 처리하기 위해 리포지터리에서 도메인 객체를 가져온다.
- `도메인 영역`과 `표현 영역`을 연결해주는 창구 역할을 한다.
- `도메인 객체 간의 흐름을 제어`한다.
- `트랜잭션 처리`를 담당한다.
- 응용 서비스가 복잡하면, 도메인 로직을 구현하고 있을 가능성이 있으므로 의심해보자.

#### 도메인 로직 넣지 않기
````JAVA
# AS-IS - bad
- 응용계층에 도메인 로직이 들어감
- 수정시, 도메인 로직을 파악하기 위해 여러 영역을 분석 해야 한다.
- 새로운 기능을 추가시, 코드 변경을 어렵게 만든다.
- 새로운 기능을 추가하기 위해, 새로운 응용계층을 생성하는 경우가 생길 수도 있다.
public class ChangePsswordService {
  public void changePassword(String userId, String oldPw, String newPw) throws Exception {
    User dbDataUser = new User();//db데이터 조회
    checkUserExists(dbDataUser);
    dbDataUser.changePassword(oldPw, newPw);
    
    //응용 계층에 도메인 로직이 들어간다.
    if(!matchPassword(oldPw)) throw new Exception();
    dbDataUser.setPassword(newPw);
  }
}

# TO-BE - good
- 비밀번호 변경을 도메인에서 처리한다.
- 코드의 응집도가 높아진다.
public class ChangePsswordService {
  public void changePassword(String userId, String oldPw, String newPw) throws Exception {
      User dbDataUser = new User();//db데이터 조회
      checkUserExists(dbDataUser);
      dbDataUser.changePassword(oldPw, newPw);
      //... save
  }
}
````
### 응용 서비스의 구현
#### 응용 서비스의 크기
- 응용 계층의 역할이 많아지면, 코드를 점점 얽히게 만들어 코드 품질을 낮춘다.
- 구분되는 기능별로 응용 계층을 나누는 것이 좋다.(`응용 계층에 기능별로 2~3개 기능`)
````JAVA
# AS-IS
- UserService 역할이 방대 해질 수 있다.
public class UserService {
    private UserRepository userRepository;
    private Notifier notifier;
    
    public void changePassword(){};
    public void initializePassword(){};
    public void findExistUser(){};
    public void sendNotify(){};
    public void findBadUser(){};
}

# TO-BE
- 기능별로 쪼개면, 코드 품질을 좋게 유지할 수 있다.
public class UserService {
  private UserRepository userRepository;
  private Notifier notifier;
  public void findExistUser(){};
  public void findBadUser(){};
}
public class UserPasswordService{
  public void changePassword(){};
  public void initializePassword(){};
}
public class notifierService{
  public void sendNotify(){};   
}
````
#### 응용 서비스의 인터페이스와 클래스
- 인터페이스가 필요한 상황은 구현클래스가 여러 개인 경우이므로, 인터페이스가 명확하게 필요하기 전까지는
  무조건 작성할 필요는 없다.
````JAVA
- 무조건 impl을 만드는 것은 잘못되었고, 꼭 필요할 경우에만 생성하자.
public interface ChangePasswordService{
    public void changePassword();
}
public class ChangePasswordServiceImpl implements ChangePasswordService{
  @Override
  public void changePassword();
}
````
#### 메서드 파라미터와 값 리턴
- 응용 서비스에 요청할 요청 파라미터가 두 개 이상 존재하면 데이터 전달을 위한 별도 클래스를 사용하는 것이 편리하다.
- 응용 서비스는 표현 영역에서 필요한 데이터만 리턴하는것이 응집도를 높이는 방법이다.
  - 애그리거트 자체를 리턴하면 `표현계층(controller)에서도 애그리거트가 제공하는 메서드를 호출`할 수 있다.
#### 표현 영역에 의존하지 않기
````JAVA
- 응용 계층이 표현 계층을 의존하게 된다.(HttpServletRequest)
- 유지 보수하기 힘들어진다.(request 데이터가 어떤 데이터를 포함하는지 구분하기 어렵다.)
public class MemberController{
    @PostMapping
    public String submit(HttpServletRequest request){
        changePasswordService(request);
        return 'ok';
    }
}
````
#### 트랜잭션 처리
- 프레임워크가 제공하는 트랜잭션 기능을 적극 사용하자.(관리가 편함)

### 표현 영역
- 사용자가 보는 화면을 제공 및 제어한다.
- 사용자의 요청을 응용계층에 전달하고 결과를 사용자에게 전달한다.
- 사용자의 세션을 관리한다.(웹에서 쿠키, 서버 세션, 권한 등)

### 값 검증
- `표현 영역`과 `응용 서비스` 두 곳에서 모두 수행한다.
- `표현 영역` : 필수 값, 값의 형식, 범위 등을 검증
- `응용 서비스` : 데이터 존재 유무와 같은 논리적 오류 검증
````JAVA
# 응용 서비스내 검증 version1
- 이와같이 응용 서비스에서 각 값이 유효한지 확인할 목적으로 익셉션 사용시, 사용자에게 불편함 제공
 > 첫 번째 값이 올바르지 않아 익셉션 발생시키면, 나머지 항목이 올바른지는 알 수 없음.
- 컨트롤러 소스가 난잡해진다.

public class JoinService{
  public void join(JoinRequest joinRequest){
    checkEmpty(joinRequest.getId(), "id");
    checkEmpty(joinRequest.getName(), "name");
    checkEmpty(joinRequest.getPassword(), "password");
    checkDuplicateId(joinRequest.getId());
  }

  private void checkDuplicateId(String id) {
    int duplicateIdCount = 1;//동일한 아이디가 존재
    if(duplicateIdCount > 0){
      throw new DuplicateIdException("동일한 아이디가 존재합니다.");
    }
  }

  private void checkEmpty(String value, String propertyName){
    if(value == null || value.isEmpty()){
      throw new EmptyPropertyException(propertyName);
    }
  }    
}


- 서비스 계층에서 전달하는 exception을 전부 catch 해야함.
public class JoinController{

  public Map<String,String> join(JoinRequest joinRequest){
    Map<String,String> result = new HashMap<>();
    try {
      joinService.join(joinRequest);
    }catch (EmptyPropertyException emptyEx){    //값이 null or empty
      result.put("error",emptyEx.getMessage());
    }catch (DuplicateIdException duplEx){       //중복된 id 존재
      result.put("error",duplEx.getMessage());
    }catch (Exception e){                       //그외 에러
      result.put("error",e.getMessage());
    }
    return result;
  }
}
````
````JAVA
# 응용 서비스내 검증 version2
- form으로 넘어오는 데이터 전체를 체크하여, 오입력된 필드정보를 전달한다.
- 표현게층에서는 exception을 하나만 받아 처리할 수 있게된다.

public class JoinService{
  public void join(JoinRequest joinRequest){
    List<ValidationError> errors = new ArrayList<>();
    if(joinRequest.getId() == null)
      errors.add(ValidationError.of("아이디 없음","empty"));
    if(joinRequest.getPassword() == null)
      errors.add(ValidationError.of("비밀번호 없음","empty"));
    if(joinRequest.getName() == null)
      errors.add(ValidationError.of("이메일 없음","empty"));
    if(isDuplicateIdExist(joinRequest.getId()))
      errors.add(ValidationError.of("동일한id 존재", "duplidexist"));

    if(!errors.isEmpty())
      throw new ValidationErrorException(errors);
  }
}

- service 계층에서 하나의 익셉션만 전달하므로 표현계층 난잡한 소스를 줄일 수 있다.
public class JoinController {
  public Map<String, Object> joinUsingValidator(JoinRequest joinRequest) {
    Map<String, Object> result = new HashMap<>();
    try {
      joinService.join(joinRequest);
    } catch (ValidationErrorException ve) { //service 게층에서 하나의 익셉션만 던짐
      result.put("error", ve.getErrors());
    } catch (Exception e) {
      result.put("error", e.getMessage());
    }
    return result;
  }
}
````

````JAVA
# 표현영역에서 검증1

public class JoinController {
  private JoinService joinService;

  public String joinUsingPresentationValidator(JoinRequest joinRequest) {
    List<String> erros = new ArrayList<>();
    if (joinRequest == null) erros.add("request값 없음");
    if (joinRequest.getId() == null) erros.add("아이디 없음");
    if (!erros.isEmpty()) return erros.toString();
    ....
    try {
      joinService.join(joinRequest);
    } catch (Exception e) {
      ...
    }
    return "view";
  }
}

# 표현영역에서 검증2
-spring 프레임워크에서 제공하는 Validator 인터페이스를 구현.
public class JoinController {
  private final JoinRequestValidator joinRequestValidator;
  private final JoinService joinService;
    
  public String join(JoinRequest joinRequest, Errors errors){
    joinRequestValidator.vaidate(joinRequest,errors);
    try{
      joinService.join(joinRequest);
    }catch (Exception e){
    }
    return "view";
  }
}
````
### 권한 검사
- `권한 검사`는 표현영역,응용서비스, 도메인에서 수행될 수 있다.
- `표현영역`
  - 사용자 인증정보 생성/인증여부 검사(제어하기 좋은 위치는 `서블릿 필터`이다.)
  - ex) 로그인유저만 특정 페이지에 접근
- `응용 서비스`
  - 스프링 시큐리티에서 제공하는 권한검사 기능도 사용할 수 있다.
    - @PreAuthrize
  - ex) 어드민 페이지 
- `도메인`
  - 보안 프레임워크를 확장해서 개별 도메인 객체 수준의 권한 검사 기능도 넣을 수 있다.
    - 높은 프레임워크 이해도가 필요하다.
  - ex) 자기자신이 작성한 게시글만 삭제할 수 있는 경우

### 조회 전용 기능과 응용 서비스
- 서비스에서 수행하는 내용이 단순 조회 일때 `응용계층`이 아니라 `표현 영역`에서 바로 접근해도 괜찮다.
````JAVA
public class Controller{
    private orderViewDao orderViewDao;
    
    public String list(Model model){
        int orderId = 2022123;
        model.addAttribute("orders", orderViewDao.selectByOrderer(orderId));
        return "order/list";
    }
}
````
