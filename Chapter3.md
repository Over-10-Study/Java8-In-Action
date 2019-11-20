# java 8  Chapter 3 람다

3.1 람다식이란? <br/>
    함수형 프로그래밍에 적합한 문법적 표현 방식
    람다식은 원래 수학자가 발표한 람다계산식에서 나왔는데, 그것을 프로그래밍에 적용한 것이다.
    
    
람다식의 목적 <br/>
    - 인터페이스가 가지고 있는 추상메서드를 간편하고 즉흥적으로 구현하기 위함 
    
```java
Compatator<Apple> byWeight = new Comparator<Apple>(){
public int compare(Apple a1, Apple a2){
    return a1.getWeight().compareTo(a2.getWeight());
    }
};   
``` 
   이거를 간단하게 바꿀 수 가 있다.
   ```java
   Comparator<Apple> byWeight = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight));
   ```
   
   요런식으로
   
   statement 와 expression의 차이 <br/>
   statement : 이거는 '진술', '서술'이라는 뜻으로 컴파일러가 이해하고 실행할 수 있는 구문을 말한다.<br/>
   expression : 이거는 '수식'이라는 뜻으로 하나이상의 값으로 표현할 수 있는 것을 말한다.
   
   ex)
   ```java
   a = 3;
   if(isvalid()){
      return 3;
   }
   ```
        
   여기서 3은 expression이고 a=3; 이라는 구문은 statement이다.<br />
   3은 하나의 값으로 표현되므로 expression이고, 1+2+3이라는 부분도 뭔가 식이라서 expression같지만 결국 저것들을 다 계산하게 되면 하나의 값이 나오게 된다.<br />
   그러므로 1+2+3도 expression가 되는 것이다.<br />
   arr = [1, 2, 3]이라는 값도 결국 배열이라는 하나의 값이므로 expression가 되는 것이다~<br />
   결국 statement 는 여러값이 모여서 컴파일러가 알아듣게 서술하는 것이므로 statement가 expression을 포함한다고 할 수 있다~<br />
   
   람다의 기본문법 
   
   (parameters) -> expression
   
   (parameters) -> {statements;}
   
   간단하게 말해서 람다의 바디가 값이면!(expression) 괄호를 할 필요가 없고,
   값이 아니면!(statement)이기 때문에 중괄호가 필요한 것이다.
   
 3.2 어디에, 어떻게 람다를 사용할까?<br/>
    람다식은 함수형 인터페이스를 인자로 하는 메서드에 사용하면 된다.
    
 
  3.2.1 함수형 인터페이스<br/>
    함수형 인터페이스는 간단하게 추상메서드가 하나밖에 없는 인터페이스이다.<br/>
    ex) Comparator, Runnable<br/>
    
    
  3.2.2 함수 디스크럽터<br/>
    함수형 인터페이스의 추상 메서드 시그너처는 람다 표현식의 시그너처를 가리킨다. 
    람다 표현식의 시그너처를 서술하는 메서드를 함수 디스크럽터 라고 부른다. <br/>
    ex) Runnable 인터페이스의 유일한 추상 메서드 run은 인수와 반환값이 없으므로 Runnalble은 인수와 반환값이 없는 시그너처인 것이다.<br/>
    boolean test(T t) 가 메서드 시그니처라면 함수 디스크립터로 표현할시 T -> boolean로 표현할 수 있다.
    
  3.3 람다 활용 : 실행 어라운드 패턴 <br/>
    실행 어라운드 패턴(execute around pattern)은 설정(setup)과 정리(cleanup) 과정 사이의 중간 과정만 로직을 전달받는 방식을 말한다.
  try-with-resource 가 그 예 중 하나이다.
  
  
  ```java
  public static void main(String[] args) throws IOException {
          try (FileWriter java7Writer = new FileWriter("sample.txt")) {
              java7Writer.write("java 7 version");
          } catch (IOException ie) {
              // }
          }
      }
  ```
  
   FileWriter는 AutoClosable interface를 상속받고 있다.<br/>
   AutoClosable interface는 close() 함수 하나만 가지고 있으며 자식 클래스에서 close()를 override해 놓으면 try 블럭이 끝나는 순간 자동으로 close()가 진행됨.<br/> 
   만약 직접 만든 클래스가 AutoClosable를 implements하도록 설계해 놓는다면, 직접 만든 클래스도 try-with-resources를 사용할 수 있다.<br/>
   
   
  
   ```java
   public static String processFile() throws IOException{
         try( BufferedReader br = new BufferedReader(new FileReader(“data.txt”))){
             return br.readLine();
         }
     }
   
   --------------------------------------------  
   2단계
   @FunctionalInterface
   public interface BufferedReaderProcessor{
       String process(BufferedReader b) throws IOException;
   }
    
   public static String processFile(BufferedReaderProcessor p) throws IOException{
       ...
   }
   
  --------------------------------------------
  3단계
   public static String processFile(BufferedReaderProcessor p) throws IOException{
        try(BufferedReader br = new BufferedReader(new FileReader(“data.txt”))){
           return p.process(br);
       }
   }
   
   --------------------------------------------
   4단계
   
   String oneLine = processFile ((BufferedReader br) -> br.readline());
   String twoLines = processFile((BufferedReader br) -> br.readLine() + br.readLine());                                               
   ```
   
   1단계 : 일단 processFile 이 동작을 받아서 그 동작을 실행하게끔 하고싶은 것이다.
   자 그러면 일단 processFIle안에 람다를 넣어야 하는 것이다.
   
   2단계 : 람다를 넣었다면 그 람다는 어떤 함수형인터페이스의 유일한 추상메서드를 구현하는 것이기 때문에, 
   그 함수형인터페이스를 내가 정의해야 한다.
   
   3단계 : 그리고 함수형 인터페이스를 1단계의 메소드에서 파라미터로 받게 바꾸고 해당 함수형 인터페이스의 동작을 실행하는 메소드추가한다.
   
   4단계 : 직접 람다식을 넣어서 실행한다! 
   
   
   
   3.4 함수형 인터페이스 사용
   
   3.4.1  Predicate
   Predicate<T>는 test라는 추상메서드를 정의하고 있고, 불린 표현식이 필요한 상황에서 활용할 수 있다.
   시그너처는 제네릭을 받아 불리언을 리턴하는 형식이다! filter 메서드를 정의할 떄 주로 사용할 수 있다.
   
   3.4.2 Consumer 
   Consumer는 마찬가지로 제네릭을 받지만 void 를 반환하고 accept라는 추상메서드를 정의한다.
   forEach를 메서드를 정의할 떄 주로 사용할 수 있다.
   
   3.4.3 Function
   네릭을 받아 재네릭을 반환하는 추상메소드이다. apply라는 추상 메서드를 정의한다.
   주로 map이라는 메서드를 정의할 떄 사용한다.
   
   자 그리고 요쯤에서 T, U 이런게 정확하게 뭔지 몰라서 찾아봤습니다.
      
  E – Element(요소 : 자바 컬렉션 라이브러리에서 많이 사용된다.)
  
  K – Key
  
  N – Number
  
  T – Type
  
  V – Value
  
  S, U, V 등 – 2번째, 3번째, 4번째 타입
  
 박싱과 언박싱<br/>
    - 자바의 형식은 모두 참조형 아니면 기본형인데, 기본형을 참조형으로 바꾸는데 박싱이고, 반대가 언박싱이다.
 변환과정은 비용이 소모되고, 메모리를 탐색하는 과정이 필요하기 떄문에, 
 자바8에서 이러한 과정을 피하기 위해서 특별한 버전의 함수형 인터페이스를 제공한다.<br/>
    - ex) IntPredicate, DoublePredicate 등
    
 
 함수형 인터페이스는 예외를 던지는 동작을 허용하지 않기 떄문에, 람다식에서 확인된 예외로 바꿔서 던져준다.
 
 3.5.1 형식 검사
   메서드의 선언을 확인한다.
   메서드가 기대하는 형식이 함수형인터페이스 이다.
   해당 함수형 인터페이스를 정의하는 추상메서드를 찾는다.
   그 추상메서드의 함수 디스크립터를 본다.
   메서드의 인자로 해당 함수 디스크립터를 따르는 람다식을 전달했는지 최종 확인한다.
   
   
 3.5.2 같은람다, 다른 함수형 인터페이스
   같은 람다지만 그 람다식을 받는 함수형 인터페이스가 다를 수 있기 떄문에, 같은 람다식을 다양하게 활용가능하다.
    
  여기서 잠깐, 다이아몬드 연산자란?
  
  ```java
  List<Integer> integers = new LinkedList<>(strings);
  ```
  다이아몬드 연산자를 사용하면 할당의 오른쪽이 왼쪽과 같은 유형 매개 변수를 가진 진정한 제네릭 인스턴스로 정의 될 수 있다. 
  이러한 매개 변수를 다시 입력하지 않아도된다.<br/>
  한마디로 <>요거를 사용하면은 왼쪽에 있는 context를 따라가게 된다는 의미인거 같다.
  
  3.5.3 형식 추론 <br/>
    람다는 대상형식을 이용해서 람다표현식과 관련된 함수형 인터페이스를 추론하기 때문에,
    람다식의 파라미터부분에 형식을 넣을 필요가 없다.
    
    
  3.5.4 지역 변수 사용 <br/>
    람다식 안에서 지역변수를 사용할 수 있지만, 무조건 final이어야 한다.
    이유 : 멀티스레딩 환경에서 람다식에서 지역변수를 접근하는데, 그 지역변수를 할당하고 있는 스레드가 사라지면 안되기 때문에
    자바에서는 지역변수의 복사본을 제공하는데 그 복사본은 값이 바뀌면 안되므로 final이다!
    
  3.6 메서드 레퍼런스 <br/>
    메서드 레퍼런스는 기존의 메서드 정의를 재활용해서 람다처럼 전달할 수 있다.
    메서드 레퍼런스가 람다보다 가독성이 좋을 때가 있다.
   ```java
   inventory.sort((Apple a1, Apple a2) ->
                      a1.getWeight().compareTo(a2.getWeight()));
                      
  
  inventory.sort(comparing(Apple::getWeight));
   ```
   요런식으로 할 수 있다.
   
   
   3.6.1 요약 <br/>
   메서드 레퍼런스는 하나의 메서드를 참조하는 람다에 적용할 수 있다.
   메서드 레퍼런스의 세 가지 유형
   
   ```java
   (args) -> ClassName.staticMethod(args)
   
   ClassName::staticMethod
   ```
   
   ```java
   (arg0, rest) -> arg0.instanceMethod(rest)
   
   ClassName::instanceMethod
   ```
   
   ```java
   (args) -> expr.instanceMethod(args)
   
   expr::instanceMethod
   ```
   
   3.6.2 생성자 레퍼런스
   이거는 메서드 레퍼런스를 활용해서 기존 생성자를 사용할 때 이다.
   
   ```java
   Supplier<Apple> c1 = Apple::new;
   Apple a1 = c1.get();
   ``` 
   자 여기서 중요한 것은 Apple::new에서 Apple의 생성자를 호출하는 것이 아니다.
   c1.get()에서 진정으로 호출이 되는 것이다.
   
   ```java
   public class Main {
       public static void main(String[] args) {
           BiFunction<Integer, Integer, JM> biFunction = JM::new;
           JM jm  = biFunction.apply(200,500);
       }
   }
   ```
   
   이처럼 인스턴스화 하지않고 생성자에 접근하는 기능을 활용해보자.
   ```java
   public class Main {
       static Map<String, Function<String,Robby>> map = new HashMap<>();
       static{
           map.put("Jm",Jm::new);
           map.put("Matin",Matin::new);
       }
       public static void main(String[] args){
           Robby jm = giveMeRobby("Jm");
           Robby matin = giveMeRobby("Matin");
   
           System.out.println(jm.toString());
           System.out.println(matin.toString());
   
       }
   
       public static Robby giveMeRobby(String name){
           return map.get(name).apply(name);
       }
   }
   ```
   자 여기서 Matin과 Jm은 Robby의 자식클래스이다. 
   그래서 static필드로 메서드 레퍼런스를 이용해서 map에 객체를 생성하는 구문을 넣어주고, 
   giveMeRobby에서는 이름을 받아 각각 Jm, Matin클래스를 생성해서 리턴해준다.
   
  
  3.7 람다, 메서드 레퍼런스 활용
  
  1단계 : 코드 전달
  ```java
  public class AppleComparator implements Comparator<Apple> {
      public int compare(Apple o1, Apple o2) {
          return Integer.compare(o1.getWeight(),o2.getWeight());
      }
  }
  ```
  처음엔 이렇게 직접 Comparator를 구현하는 클래스를 직접 만들어 주었다.
  AppleComparator는 한번만 쓰고 말건데 굳이 클래스로 만드는 것이 번거롭다.
  
  2단계 : 익명 클래스 사용 
  ```java
  inventory.sort(new Comparator<Apple>() {
            @Override
            public int compare(Apple o1, Apple o2) {
                return Integer.compare(o1.getWeight(),o2.getWeight());
            }
        });
  ```
  그래서 sort하는 부분에 이렇게 익명클래스를 이용해보았다.
  하지만 여전히 난잡하다!
  
  3단계 : 람다 표현식 사용
  ```java
  inventory.sort((Apple a1, Apple a2) -> Integer.compare(a1.getWeight(),a2.getWeight()));
  ```
  자 이렇게 람다식으로 바꾸었다 굉장히 깔끔하다. 하지만 이것도 메서드 레퍼런스를 활용해서 바꿀 수가 있다!
  
  ```java
  inventory.sort(Comparator.comparing(Apple::getWeight));
  ```
  이렇게 comparing메서드를 활용해서 구현할 수 있다.
  comparing은 람다식은 파라미터로 받는다.   
  
  
  3.8 람다 표현식을 조합할 수 있는 유용한 메서드
  여러개의 람다 표현식을 조합해서 복잡한 람다식을 만들 수 있다.
  이것은 함수형 인터페이스에서 디폴트메서드를 제공하기 떄문인데, 그거는 이제 누군가가 발표할 것입니다. To be Continue
  ```java
  inventory.sort(Comparator.comparing(Apple::getWeight).reversed());
  ```
  여기서 reversed는 주어진 비교자의 순서를 뒤바꾸는 디폴트 메서드이다.
  
  Comparator 연결 
  ```java
  inventory.sort(Comparator.comparing(Apple::getWeight))
           .reversed()
           .thenComparing(Apple::getCountry));
  ```
  
  Predicate 조합`<br/>
  Preddicate는 negate, and, or세가지 메서드를 제공한다.<br/>
  ex) negate 
  ```java
  public class Main{
      public static void main(String[] args) {
          Predicate<Apple> redApple = Apple::isRedColor;
          Predicate<Apple> notRedApple = redApple.negate();
          List<Apple> apples = Arrays.asList(new Apple(1,true),new Apple(2,false), new Apple(3, true));
          
          System.out.println(apples.stream()
                  .filter(notRedApple)
                  .count());
          
      }
  ```
 
  자 여기서 redApple이라는 Predicate를 선언했는데 이것의 반대인 notRedApple이라는 Predicate는 negate()메서드를 통해서
  만들 수 있다.<br/>
  Arrays.asList를 통해서 빨간사과 2개 빨간사과 아닌 사과 1개를 넣었기 떄문에
  filter메서드의 파라미터로 notRedApple을 넣어줬기 떄문에 결과는 1이된다. 
   
  ```java
  public class Main{
      public static void main(String[] args) {
          Predicate<Apple> redApple = Apple::isRedColor;
          Predicate<Apple> isHeavyApple = Apple::isHeavy;
  
          Predicate<Apple> notRedAndHeavyApple = redApple.and(isHeavyApple);
          List<Apple> apples = Arrays.asList(new Apple(160,true),new Apple(50,false), new Apple(200, true));
  
          System.out.println(apples.stream()
                  .filter(notRedAndHeavyApple)
                  .count());
  
      }
  }
  ``` 
  여기서는 Predicate에서 지원하는 and 디폴트 메서드를 사용했다.
  and 디폴트 메서드는 Predicate를 파라미터로 받아 and연산을 해준다.
  따라서 이 Predicate를 filter의 파라미터로 넘겨주면 무게가 150 보다 크면서 빨간사과를 찾게 되는 것이다.<br/>
  그래서 답은 2
  
  3.8.3 Function 조합 <br/>
    andThen, compose 두가지 디폴트 메서드를 제공한다. <br/>
    andThen은 함수를 먼저 적용한 결과를 다른함수의 입력으로 전달하는 것이다. <br/>
  ```java
  public class Main{
      public static void main(String[] args) {
          Function<Integer, Integer> function1 = a -> a+1;
          Function<Integer, Integer> function2 = a -> a*3;
          Function<Integer, Integer> function3 = function1.andThen(function2);
          System.out.println(function3.apply(2));
  
      }
  }
  ``` 
  
   2에 1을 더하고 3을 곱했으니 답은 9 <br />
    람다 표현식 조합에 유용한 메서드 정리 <br/>
    Comparator : comparing, reversed <br/>
    Predicate : negate, and, or <br/>
    Funtion : andThen, compose <br/>
  
  
  

   
         
    
    
    
   
    
    
    
    
    
  
    
 
   
   
    
   
   
   
                                         
  

  
  
    
  
  
  
  
 

  
     
  
  
  
  
 
   
   
   
   
   
   
   
   
  
   
   
