# Chapter 8 리펙토링, 테스팅, 디버깅

* 이장에서는 람다 표현식을 이용해서 기존 코드를 어떻게 리팩토링 해야 하는지 살펴본다!


## 8.1 가독성과 유연성을 개선하는 리팩토링

### 8.1.1 코드 가독성 개선
* 코드 가독성을 개선한다는 것은 우리가 구현한 코드를 다른 사람이 쉽게 이해하고 유지보수할 수 있게 만드는 것을 의미한다!
* 코드 가독서을 높이려면 코드의 문서화를 잘하고, 표준 코딩 규칙을 준수하는 등(컨벤션)의 노력을 해야된다.
* 여기서는 람다, 메서드 레퍼런스, 스트림을 활용해서 코드 가독성을 개선할 수 있는 3가지 리펙토링 방법
1. 익명 클래스를 람다 표현식으로 리팩토링
2. 람다 표현식을 메서드 레퍼런스로 리팩토링
3. 명령형 데이터 처리를 스트림으로 리팩토링

### 8.1.2 익명 클래스를 람다 표현식으로 리팩토링
* 3장에서 했듯이 하나의 추상메서드를 구현하는 익명 클래스는 람다 표현식으로 리팩토링 할 수 있다.
* 리팩토링 하는이유 : 예제 확인

```java
public static void main(String[] args) {  
    Runnable runnable = new Runnable() {  
        @Override  
  public void run() {  
            System.out.println("Hello");  
  }  
    };  
       
      
 Runnable runnable1 = () -> System.out.println("Hello");  
}

```
* 딱 봐도 뒤에 있는게 훨씬 가독성이 좋고 난잡하지 않다.
* #### 람다 표현식와 익명 클래스의 차이
1. 익명클래스는 컴파일시점에서 ‘클래스명$1.class’ 처럼 $기호를 이용하여 순서대로 번호가 부여되는데 람다표현식이 작성된 클래스를 컴파일하면 이를 확인할 수가 없다.

2. 가장 큰 차이는 this키워드가 의미하는 바이다.  익명클래스에서 this는 익명클래스 자신을 가리킵니다.  반면, 람다표현식에서 this 는 람다표현을 감싸고 있는 클래스를 지칭하게 된다.

3. 또한 람다표현식과 익명클래스의 다른 차이는 컴파일하는 방식의 차이가 있다. 자바 컴파일러는 람다 표현식을 클래스 내 private 메소드로 컴파일한다.  
이때,  자바7에서 명세가 추가 된 invokeDynamic 을 사용하여 변환한다.  invokeDynamic 명세를 이용하여 람다표현식은 성능 면에서 익명클래스보다 유리하게 동작한다고 한다. 이유는 해당 메서드가 호출되기전까지 초기화를 진행하지 않기 때문이다. 이를 통해 결국 메모리를 다소 절약하는 효과를 가져오게 한다.

![enter image description here](https://lh3.googleusercontent.com/0GpJXnpZJzW8GRmswZ4J7TKxxilJu1GbwR4opUBi9NaNcpy7sW8quw9pcT-bR_SY6ASc-yReyhA)

```java
int a = 10;  
Runnable runnable = new Runnable() {  
    @Override  
  public void run() {  
        int a =2;  
  System.out.println(a);  
  }  
};  

Runnable runnable1 = () -> {  
    int a = 2;  //여기서 a가 이미 scope에 정의되어 있다고 나온다!
  System.out.println(a);  
}
```
* 콘텍스트 오버로딩에 의한 모호함
```java
public interface Task {  
    public void execute();  
}

public static void main(String[] args) {  
	doSomething( () -> System.out.println("danger")); //이렇게 하면 Runnable을 파라미터로 하는지 Task를 파라미터로 하는지 알 수가 없다.  
 //왜냐하면 Runnable과 Task의 시그니처가 같기 때문.}  
  
	doSomething(new Task() {  
    @Override  
    public void execute() {  
        System.out.println("No danger"); // 이렇게 하면 당연히 된다. Task라는걸 명시해줫기 떄문  
  }  
});
public static void doSomething(Runnable r) {r.run();}  
public static void doSomething(Task a) {a.execute();}
```

근데 이런거를 인텔리 j에서 알아서 해준답니다....

### 8.1.3 람다 표현식을 메서드 레퍼런스로 리팩토링

```java
Map<CaloricLevel, List<Dish>> dishesByCaloricLevel =   
        menu.stream.collect(groupingBy(dish -> {  
            if (dish.getCalories() <= 400) return CaloricLevel.DIET;  
			else if(dish.getCalories() <= 700) return CaloricLevel.NORMAL;  
			else return CaloricLevel.FAT;  
  }));

Map<CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(groupingBy(Dish::getCaloricLevel));
```
 groupingBy안에 있는 메서드를 Dish의 클래스내에 getCaloricLevel이라는 메서드로 선언하고 사용하면 보다 가독성있게 리팩토링 할 수 있다.

### 8.1.4 명령형 데이터 처리를 스트림으로 리팩토링하기

* 기존에 우리가 사용하던 명령형 코드를 스트림으로 바꾸면 가독성도 좋아지고 병렬성도 좋아지는 두마리 토끼를 잡을 수 있다.
```java
List<String> dishname = new ArrayList<>();  
for(Dish dish : menu) {  
    if(dish.getCalories() > 300){  
        dishnames.add(dish.getName());  
  }  
}  
  
menu.parallelStream  
        .filter(d -> d.getCalories() > 300)  
        .map(Dish::getName)  
        .collerc(toList());
```


### 8.1.5 코드 유연성 개선

* 조건부 연기실행과 실행 어라운드 패턴으로 람다 표현식 리팩토링을 살펴본다. 

#### 1.  조건부 연기실행 
```java
if(logger.isLoggable(Log.FINER)){  
        logger.finer("Problem: " + generateDiagnostic());  
  }  
```
* 위의 코드는  isLoggable이라는 메서드에 의해 클라이언트 코드로 노출이 된다.
* 메시지를 로깅할 떄마다 logger 객체의 상태를 계속 확인한다.

```java
logger.log(Leve.FINER, "Problem : " + generateDiagnostic());  
}
```
* 이렇게하면 불필요한 if문을 제거할 수 있고 logger의 상태를 노출하지 않아도 된다. 
* 하지만 이렇게 해도 항상 로깅 메시지를 평가하게 된다.

 ```java
 logger.log(Level.FINER, () -> "Problem: "+ generateDiagnostic());
 ```
*  이렇게하면 logger의 수준이 적절하게 설정되어 있을 떄만 인수로 넘겨진 람다를 내부적으로 실행한다고 한다!

#### 2. 실행 어라운드
```java
String oneLine = processFile((BufferedReader b) -> b.readLine());   // 한줄만 읽음
System.out.println(oneLine);  
  
String twoLines = processFile((BufferedReader b) -> b.readLine() + b.readLine()); //두줄 읽음  
System.out.println(twoLines);

public static String processFile(BufferedReaderProcessor p) throws IOException {  
   try(BufferedReader br = new BufferedReader(new FileReader("lambdasinaction/chap3/data.txt"))){  
      return p.process(br);  
  }
  ```
* 매번 같은 준비, 종료 과정을 반복적으로 수행하는 코드가 있으면 람다로 변환할 수 있다.

### 람다로 디자인 패턴 리팩토링


#### 1.  전략 패턴

* 전략 패턴은 세 부분으로 구성 된다.
1. 알고리즘을 나타내는 인터페이스
2. 알고리즘을 구현하는 구현체
3. 전략 객체를 사용하는 클라이언트

![enter image description here](https://img1.daumcdn.net/thumb/R720x0.q80/?scode=mtistory2&fname=http://cfile8.uf.tistory.com/image/2458DE3C52DFCAF121A3FD)

1. 인터페이스 선언
```java
interface ValidationStrategy {  
    public boolean execute(String s);  
}
```
2. 인터페이스 구현하는 구현체
```java
static private class IsAllLowerCase implements ValidationStrategy {  
    public boolean execute(String s){  
        return s.matches("[a-z]+");  
  }  
}  
static private class IsNumeric implements ValidationStrategy {  
    public boolean execute(String s){  
        return s.matches("\\d+");  
  }  
}
```

3. 전략 객체를 사용하는 클라이언트
```java
static private class Validator{  
    private final ValidationStrategy strategy;  
 public Validator(ValidationStrategy v){  
        this.strategy = v;  
  }  
    public boolean validate(String s){  
        return strategy.execute(s); }  
}
```
* 자 이렇게 준비가 끝난 상태에서 이것을 사용할 떄에 지금 2번에 있는 인터페이스 구현체의 내용을 직접 입력하는 것 보다 람다식으로 전달한다면 2번의 인터페이스 구현체를 굳이 선언하지 않아도 된다는 장점이 생긴다!

```java
// old school  
Validator v1 = new Validator(new IsNumeric());  
System.out.println(v1.validate("aaaa"));  
Validator v2 = new Validator(new IsAllLowerCase ());  
System.out.println(v2.validate("bbbb"));  
  
  
// with lambdas  
Validator v3 = new Validator((String s) -> s.matches("\\d+"));  
System.out.println(v3.validate("aaaa"));  
Validator v4 = new Validator((String s) -> s.matches("[a-z]+"));  
System.out.println(v4.validate("bbbb"));
```
* 앞의  old school이 깔끔해 보인다고는 하지만 저렇게 사용하기 위해선 IsNumberic 과 IsAllLowerCase 클래스를 생성하고 구현하는 메소드를 작성해줘야 한다는 단점이 있는데 이것을 제거할 수 있다.
* 그러므로 람다 표현식으로 전략 디자인 패턴을 대신할 수 있다!

#### 2. 템플릿 메서드 패턴

* 템플릿 메서드 패턴은 여러 클래스에서 공통으로 사용하는 메서드를 상위 클래스에서 정의하고, 하위 클래스마다 다르게 구현해야 하는 세부적인 사항을 하위 클래스에서 구현하는 패턴을 말한다.
![enter image description here](https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https://t1.daumcdn.net/cfile/tistory/25527B4652DE84050B)


```java
abstract class OnlineBanking {  
    public void processCustomer(int id){  
        Customer c = Database.getCustomerWithId(id);  
  makeCustomerHappy(c);  
  }  
    abstract void makeCustomerHappy(Customer c);  
  
  
  // dummy Customer class  
  static private class Customer {}  
    // dummy Datbase class  
  static private class Database{  
        static Customer getCustomerWithId(int id){ return new Customer();}  
    }  
}
```
* 자 위의 예제에서 processCustomer은 id를 이용해서 makeCustomerHappy동작을 실행한다.
* 위의 부분에서 makeCustomerHappy에 대한 동작을 람다식을 통해 해결할 수 있다.

```java
public static void main(String[] args) {  
    new OnlineBankingLambda().processCustomer(1337, (Customer c) -> System.out.println("Hello!"));  
}  
  
public void processCustomer(int id, Consumer<Customer> makeCustomerHappy){  
    Customer c = Database.getCustomerWithId(id);  
  makeCustomerHappy.accept(c);  
}  
  
// dummy Customer class  
static private class Customer {}  
// dummy Database class  
static private class Database{  
    static Customer getCustomerWithId(int id){ return new Customer();}  
}
```

* 바뀐것은 ProcessCustomer이라는 메서드를 Consumer<Customer>로 바꿔서 람다식으로 전달받을 수 있게 끔 만들어서 템플릿 메서드 패턴에서 발생하는 자잘한 코드를 제거할 수 있다.

#### 3. 옵저버 패턴

* 옵저버패턴은 어떤 이벤트가 발생했을 때 한 객체가 다른객체 리스트에 자동으로 알림을 보내야 하는 상황에서 사용한다.
* 예를들어 버튼을 누르면 옵저버에 알림이 전달되고 정해진 동작이 수행된다.
![enter image description here](https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https://t1.daumcdn.net/cfile/tistory/245ECB3652EC118619)
1. 일단 옵저버를 그룹화 할 인터페이스가 필요하다.
```java
interface Observer{  
    void inform(String tweet);  
}
```

2. 그리고 이 옵저버를 구현하는 여러 옵저버를 정의한다.
```java
static private class NYTimes implements Observer{  
    @Override  
  public void inform(String tweet) {  
        if(tweet != null && tweet.contains("money")){  
            System.out.println("Breaking news in NY!" + tweet);  
  }  
    }  
}  
  
static private class Guardian implements Observer{  
    @Override  
  public void inform(String tweet) {  
        if(tweet != null && tweet.contains("queen")){  
            System.out.println("Yet another news in London... " + tweet);  
  }  
    }  
}  
  
static private class LeMonde implements Observer{  
    @Override  
  public void inform(String tweet) {  
        if(tweet != null && tweet.contains("wine")){  
            System.out.println("Today cheese, wine and news! " + tweet);  
  }  
    }  
}
```

3. 그리고 주제의 인터페이스를 정의한다.
```java
interface Subject{  
    void registerObserver(Observer o);  
 void notifyObservers(String tweet);  
}
```
4. 그리고 주제 인터페이스를 구현하는 구현체를 만든다.
Feed는 옵저버를 등록하고 트윗의 옵저버에 이를 알리는 역할이다.
```java
static private class Feed implements Subject{  
    private final List<Observer> observers = new ArrayList<>();  
 public void registerObserver(Observer o) {  
        this.observers.add(o);  
  }  
    public void notifyObservers(String tweet) {  
        observers.forEach(o -> o.inform(tweet));  
  }  
}
```
5. 실행
```java
Feed f = new Feed();  
f.registerObserver(new NYTimes());  
f.registerObserver(new Guardian());  
f.registerObserver(new LeMonde());  
f.notifyObservers("The queen said her favourite book is Java 8 in Action!");
```
* NYTimes, Guadian, LeMonde에서 모두 같은 inform이라는 메서드를 정의하고있다.
* ... 슬슬 감이 오지 않는가? 하나의 인터페이스의 추상메서드를 구현하는 클래스가 여럿 존재한다 이것을 람다식으로 받아서 처리하면 되지 않겟는가? 


```java
feedLambda.registerObserver((String tweet) -> {  
    if(tweet != null && tweet.contains("money")){  
        System.out.println("Breaking news in NY! " + tweet); }  
});  
feedLambda.registerObserver((String tweet) -> {  
    if(tweet != null && tweet.contains("queen")){  
        System.out.println("Yet another news in London... " + tweet); }  
});  
```
#### 8.2.4 의무 체인 패턴

* 의무 체인 패턴도 비슷하다. 어떠한 작업을 처리하고 그뒤에 다른 객체로 결과를 전달하고, 다른 객체도 해야할 작업을 처리한 다음에 또 다른 객체에 전달한다.

```java
static private abstract class ProcessingObject<T> {  
    protected ProcessingObject<T> successor;  
  
 public void setSuccessor(ProcessingObject<T> successor) {  
        this.successor = successor;  
  }  
  
    public T handle(T input) {  
        T r = handleWork(input);  
		 if (successor != null) {  
            return successor.handle(r);  
  }  
        return r;  
  }  
  
    abstract protected T handleWork(T input);  
}  
  
static private class HeaderTextProcessing  
        extends ProcessingObject<String> {  
    public String handleWork(String text) {  
        return "From Raoul, Mario and Alan: " + text;  
  }  
}  
  
static private class SpellCheckerProcessing  
        extends ProcessingObject<String> {  
    public String handleWork(String text) {  
        return text.replaceAll("labda", "lambda");  
  }  
}
```
ProcessingObject 에서 setSuccessor로 작업을 할 것을 넣어주고
handle을 이용해서 작업을 수행한다.
그리고 handleWork를 오버라이드 해서 실행한다.

역시 handleWork를 오버라이드 하기위해 클래스를 2개를 선언하는데 이것을 람다식으로 바꾸는 것이다.

```java
//람다식 전
ProcessingObject<String> p1 = new HeaderTextProcessing();  
ProcessingObject<String> p2 = new SpellCheckerProcessing();  
p1.setSuccessor(p2);  
String result1 = p1.handle("Aren't labdas really sexy?!!");  
System.out.println(result1);

//람다식 후
UnaryOperator<String> headerProcessing =  
        (String text) -> "From Raoul, Mario and Alan: " + text;  
UnaryOperator<String> spellCheckerProcessing =  
        (String text) -> text.replaceAll("labda", "lambda");  
Function<String, String> pipeline = headerProcessing.andThen(spellCheckerProcessing);  
String result2 = pipeline.apply("Aren't labdas really sexy?!!");  
System.out.println(result2);
```
UnartOperator와  Function을 활용해서 ProcessingObject를 구현하는 클래스를 만들지 않고도 실행할 수 있다.

#### 5. 팩토리 패턴

* 팩토리 패턴은 인스턴스화 로직을 클라이언트에 노출하지 않고 객체를 만들떄 사용한다.

```java
//람다식 전
public static Product createProduct(String name){  
    switch(name){  
        case "loan": return new Loan();  
        case "stock": return new Stock();  
        case "bond": return new Bond();  
        default: throw new RuntimeException("No such product " + name);  
  }  
}

//람다식 후
final static private Map<String, Supplier<Product>> map = new HashMap<>();  
static {  
    map.put("loan", Loan::new);  
    map.put("stock", Stock::new);  
    map.put("bond", Bond::new);  
}

public static Product createProductLambda(String name){  
    Supplier<Product> p = map.get(name);  
    if(p != null) return p.get();  
    throw new RuntimeException("No such product " + name);  
}
```
* 람다식의 메서드 레퍼런스를 활용해 예쁘게 구현할 수 있다. 이로써 생성자를 외부로 노출하지 않을 수 있다.


* 디자인 패턴을 5개나 살펴봤지만 맥락은 비슷한 것 같다. 인터페이스나 추상 클래스가 있고, 그 인터페이스나 추상클래스를 구현하는 클래스를 만드는 과정에서 코드가 많아질 수 있는 부분을 람다식을 통해서 해결하는 것이다. 하지만 람다식을 무조건 쓰는 것 보다 상황에 맞게 쓰는 것이 좋을 것이지만 람다식을 사용해서 리팩토링 할 수 있는 코드는 심심치 않게 존재할 것 같다는 생각이 든다.

####  8.3.2 람다를 사용하는 메서드의 동작에 집중해라

* 람다식 자체는 동작을 하나의 조각으로 캡슐화 하는 것이다.
* 그러면 세부구현을 포함하는 람다표현식을 공개하는 것보다는 람다표현식을 사용하는 메서드 전체를 테스트 하는 것이 옳다!

#### 8.4 디버깅

* 문제가 발생한 코드를 디버깅할 떄 개발자는 두가지를 확인해야 한다.

1. 스택트레이스
2. 로깅

* 프로그램 실행이 멈추면 어디서 멈추었는지 확힌해야 하는데 바로 스택 프레임에서 이 정보를 얻을 수 있다. 프로그램이 메서드를 호출할 떄마다 호출 위치, 호출할 떄의 인수값, 호출된 메서드의 지역번수 등을 포함한 호출정보가 생성되며 스택 프레임에 저장된다.
* 따라서 프로그램이 멈추면 어떻게 멈췄는지 프레임별로 보여주는 스택 트레이스를 얻을 수 있다. 즉 문제가 발생한 지점의 메서드 호출 리스트를 얻을 수 있다.





