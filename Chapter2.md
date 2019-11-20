# 동작 파라미터화(Behavior parameterization) 코드 전달하기

## 이 장의 내용
- 변화하는 요구사항에 대응
- 동작 파라미터화
- 익명 클래스
- 람다 표현식 미리보기
- 실전 예제: ```Calculator```, ```Comparator```

소프트웨어 프로그램은 언제나 변화하는 요구사항에 맞춰 변화해야 한다. 예를 들어 한 농부가 재고목록 조사를 쉽게할 수 있도록 돕는 애플리케이션이 있다. 이의 요구사항 변화 모습을 살펴보자.

```
"녹색 사과를 모두 찾고 싶군요."
-> "150그램 이상인 사과를 모두 찾고 싶군요."
-> "150그램 이상이면서 녹색인 사과를 모두 찾았으면 좋겠어요. ㅎㅎ"
```

위처럼 일상생활에서 사용자가 요구하는 사항은 시시때때로 변한다. 이를 효과적으로 대응하기 위해서 **동작 파라미터화** 를 이용한다.

동작 파라미터화란 **아직은 어떻게 실행할 것인지 결정하지 않은 코드 블록** 을 말한다. 동작 파라미터화는 대부분 메서드의 인수로 코드 블록을 전달하므로 이 메서드가 실행될때까지 코드 블록의 실행은 나중으로 미뤄진다. 결과적으로 **코드 블록에 따라 메서드의 동작이 파라미터화된다.**


## 변화하는 요구사항에 대응하기
위에서 살펴본 농부를 위한 프로그램 예제를 살펴보자. 이는 계속해서 요구사항이 변화한다. 이를 코드로 살펴보자.

### 첫 번째 요구 사항 - 1: 녹색 사과 필터링

```java
public static List<Apple> filterGreenApples(List<Apple> inventory) {
  List<Apple> result = new ArrayList<>();     // 사과 누적 리스트
  for (Apple apple : inventory) {
    if ("green".equals(apple.getColor())) {   // 녹색 사과만 선택
      result.add(apple);
    }
  }
  return result
}
```

위 코드는 유연성이 매우 부족한 코드이다. 만약 빨간 사과와 같이 다른 색깔을 원한다면 ```if```문이 계속해서 늘어날 것이다.

### 첫 번째 요구 사항 - 2: 색을 파라미터화
**색을 파라미터화할 수 있도록** 메서드에 추가하면 변화하는 요구사항에 좀 더 유연하게 대응할 수 있다.

```java
public static List<Apple> filterApplesByColor(List<Apple> inventory, String color) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (apple.getColor().equals(color)) {
      reuslt.add(apple);
    }
  }
  return result;
}
```

이와 같이 색을 파라미터화하여 인수로 추가하면 아래와 같이 사용자가 유연하게 사용할 수 있다.

```java
List<Apple> greenApples = filterApplesByColor(inventory, "green");
List<Apple> redApples = filterApplesByColor(inventory, "red");
```

### 두 번째 요구 사항: 150그램 이상 사과
농부는 색 이외에도 무게가 150그램이상인 사과를 원한다. 무게 값을 파라미터화하여 구현해보면 아래와 같다.

```java
public static List<Apple> filterApplesByWeight(List<Apple> inventory, String weight) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (apple.getWeight() > weight) {
      reuslt.add(apple);
    }
  }
  return result;
}
```

이 코드는 좋은 해결책이 될 수 있다. 하지만 색을 필터링하는 것과 중복되는 코드가 너무 많다. 이는 소프트웨어 공학의 **DRY(don't repeat yourself)** 원칙을 어긴다. 만약 색과 무게를 같이 필터링하려면 **전체 구현을 고쳐야 한다.**

### 세 번째 요구 사항: 색과 무게를 필터링
값을 파라미터화해서 색과 무게를 필터링하려면 ```flag``` 라는 ```boolean```값을 통해 구분하여 만들 수 있다. 하지만 이 방법은 매우 좋지 않은 방법이므로 사용하지 말아야 한다!

```java
public static List<Apple> filterApples(List<Apple> inventory, String color
                                            String weight, boolean flag) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if ((flag && apple.getColor().equals(color)) ||  // flag가 true이면 색을 필터링하고,
        (!flag && apple.getWeight() > weight)) {     // flag가 false이면 무게를 필터링한다.
          result.add(apple);
        }
  }
  return result;
}
```

위 코드를 사용자가 사용한다면 아래와 같이 사용할 수 있다.

```java
List<Apple> greeenApples = filterApples(inventory, "green", 0, true);
List<Apple> heavyApples = filterApples(inventory, "", 150, false);
```

위 코드는 매우 안좋은 코드이다. ```true, false```가 무엇을 의미하는지 알 수 없고, **요구사항에 유연하게 대응할 수 없다.** 현재까지 살펴본 방법은 ```String```, ```int```, ```boolean``` 등의 **값으로 파라미터화** 를 하는 것을 살펴봤다. 매우 간단한 요구사항에서는 이 방법이 좋을 수 있으나, 계속해서 요구사항이 바뀐다면 변경하기 매우 어렵게 된다. 이를 효과적으로 요구사항을 반영하기 위해 동작 파라미터화를 사용할 수 있다.


## 동작 파라미터화
이전 예제를 다시 살펴보자. 사과의 어떤 속성에 기초해서 ```boolean```값을 반환하는 방법이 있다. 이를 **predicate(찬반형, 불린을 반환하는 함수)** 라 한다. 이를 선택 조건을 결졍하는 **인터페이스** 로 정의해보자.

```java
public interface ApplePredicate {
  boolean test(Apple apple);
}
```

그리고 아래와 같이 다양한 조건을 대표하는 여러 가지 버전의 ```ApplePredicate```를 정의할 수 있다.

```java
// 150그램 이상의 무거운 사과만 선택
public class AppleHeavyWeightPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return apple.getWeight() > 150;
  }
}
```

```java
// 녹색 사과만 선택
public class AppleGreenColorPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return "green".equals(apple.getColor());
  }
}
```

![image2-1](https://user-images.githubusercontent.com/34755287/61109806-c0648500-a4c0-11e9-8b1e-c15525350011.png)

위 클래스들은 여러 가지의 사과 선택 전략을 캡슐화한 모습이다. 위 조건에 따라 ```filter``` 메서드가 다르게 동작하는데, 이를 [전략 디자인 패턴](https://en.wikipedia.org/wiki/Strategy_pattern)이라고 부른다.

이제 ```filterApples```에서 ```ApplesPredicate``` 객체를 받아 사과의 조건을 검사하도록 메서드를 고쳐야한다. 이를 **동작 파라미터화**, 즉 메서드가 다양한 동작(또는 전략)을 **받아서** 내부적으로 다양한 동작을 **수행할 수 있다.**

### 첫 번째 시도: 추상적 조건으로 필터링
다음은 위에서 살펴본 것처럼 ```filter```메서드에 ```ApplePredicate```를 적용한 모습이다.

```java
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (p.test(apple)) {    // 프레디케이트 객체로 사과 검사 조건을 캡슐화하였다.
      result.add(apple);
    }
  }
}
```

이를 통해 값을 파라미터화한 코드보다 더 유연하고 가독성이 좋은 코드를 완성하였다. 사용자는 필요한 요구사항이 있으면 ```ApplePredicate```를 만들어서 손쉽게 사과를 필터링할 수 있다. 예를 들어 농부가 150그램이 넘는 빨간 사과를 검색하고 싶다면 ```ApplePredicate```를 다음과 같이 만들어서 ```filterApples```에 인수로 넘겨줄 수 있따.

```java
public class AppleRedAndHeavyPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return "red".equals(apple.getColor())
          && apple.getWeight() > 150;
  }
}
```

```java
List<Apple> redAndHeavyApples = fliterApples(inventory, new AppleRedAndHeavyPredicate());
```

동작 파라미터화는 위와 같이 코드 블록을 전달하는 것이다. 실제적으로 사과를 필터링하는 코드는 아래와 같다.

```java
return "red".equals(apple.getColor())
      && apple.getWeight() > 150;
```

이를 ```filterApples```의 인수로 전달하여 **동작** 하도록 만든다. 위 코드를 인수로 전달하기 위해 ```AppleRedAndHeavyPredicate```는 다른 많은 로직과 관계없는 코드가 추가되었다. 이를 해결하는 방법으로 **람다** 가 존재하며, 이는 3장에서 자세히 다룬다.


## 복잡한 과정 간소화
사과를 필터링하기 위해서는 ```ApplePredicate``` 인터페이스를 만들고 이를 구현하는 여러가지 전략 클래스를 만들어야한다. 이는 **상당히 번거러운 작업이다.**

```java
// 150그램 이상의 무거운 사과만 선택
public class AppleHeavyWeightPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return apple.getWeight() > 150;
  }
}

// 녹색 사과만 선택
public class AppleGreenColorPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return "green".equals(apple.getColor());
  }
}

public class FilteringApples {
  public static void main(String...args) {
    List<Apple> inventory = Arrays.asList(new Apple(80, "green"),
                                         new Apple(155, "green"),
                                         new Apple(120, "red"));
    List<Apple> heavyApples = filterApples(inventory, new AppleHeavyWeightPredicate());
    // 결과 리스트는 155그램의 사과 한 개를 포함한다.
    List<Apple> greenApples = filterApples(inventory, new AppleGreenColorPredicate());
    // 결과 리스트는 녹색 사과 두 개를 포함한다.
  }

  public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if (p.test(apple)) {
        result.add(apple);
      }
    }
    return result;
  }
}
```

위 코드를 보면 관련 없는 많은 코드가 추가되었다. 이를 개선하기 위해서 자바는 클래스 선언과 인스턴스화를 동시에 수행할 수 있는 **익명 클래스(anonymous class)** 라는 기법을 제공한다. 익명 클래스는 자바의 지역 클래스(블록 내부에 선언된 클래스)와 비슷하며, 말 그대로 이름이 없는 클래스이다. 이는 클래스 선언과 인스턴스를 동시에 할 수 있으므로 **즉석에서 필요한 구현을 만들 수 있다.**

### 두 번째 시도: 익명 클래스 사용
익명 클래스를 사용하여 ```ApplePredicate```를 구현하는 객체를 만드는 방법으로 필터링 예제를 만들어보자.

```java
List<Apple> redApples = filterApples(inventory, new ApplePredicate() {
  // filterApples 메서드의 동작을 직접 파라미터화했다!
  public boolean test(Apple apple) {
    return "red".equals(apple.getColor());
  }
});
```

하지만 익명 클래스로도 부족한 부분이 있다.
- 아래와 같이 여전히 많은 불필요한 코드가 있다.

```java
List<Apple> redApples = filterApples(inventory, new ApplePredicate() {
  public boolean test(Apple apple) {
    // ...
  }
});
```

- 많은 프로그래머가 익명 클래스의 사용에 익숙하지 않다.

### 세 번째 시도: 람다 표현식 사용
자바 8의 람다 표현식을 이용해서 위 예제 코드를 간단히 구현할 수 있다.

```java
List<Apple> reuslt = filterApples(inventory, (Apple apple) -> "red".equals(apple.getColor()));
```

### 네 번째 시도: 리스트 형식으로 추상화
자바 8에서는 동작뿐아니라 사과와 같은 물건을 추상화하여 여러 물건에 필터링이 작동하도록 할 수 있다. 이는 리스트 형식을 추상화하는 것이다.

```java
// 형식 파라미터 T
public interface Predicate<T> {
  boolean test(T t);
}

public static <T> List<T> filter(List<T> list, Predicate<T> p) {
  List<T> result = new ArrayList<>();
  for (T e : list) {
    if (p.test(e)) {
      result.add(e);
    }
  }
  return result;
}
```

이와 같이 구현하면 사과뿐아니라 바나나, 오렌지, 정수, 문자열 등의 리스트에 필터 메서드를 사용할 수 있다.

```java
List<Apple> redApples = filter(inventory, (Apple apple) -> "red".equals(apple.getColor()));

List<String> evenNumbers = filter(numbers, (Integer i) -> i % 2 == 0);
```

## 실전 예제 - 계산기 프로그램

### 동작 파라미터화

```java
public class Calculator {
    private static Map<String, CalculateStrategy> bucket = new HashMap<>();

    static {
        bucket.put("+", new PlusStrategy());
        bucket.put("-", new MinusStrategy());
        bucket.put("*", new MultiplyStrategy());
        bucket.put("/", new DivideStrategy());
    }

    public static double run(double rightOperator, double leftOperator, String operand) {
        CalculateStrategy calculateStrategy = bucket.get(operand);
        return calculateStrategy.calculate(rightOperator, leftOperator);
    }
}

public interface CalculateStrategy {
    double calculate(double right, double left);
}

public class PlusStrategy implements CalculateStrategy {
    @Override
    public double calculate(double right, double left) {
        return right + left;
    }
}

public class MinusStrategy implements CalculateStrategy {
    @Override
    public double calculate(double right, double left) {
        return right - left;
    }
}

public class MultiplyStrategy implements CalculateStrategy {
    @Override
    public double calculate(double right, double left) {
        return right * left;
    }
}

public class DivideStrategy implements CalculateStrategy {
    @Override
    public double calculate(double right, double left) {
        return right / left;
    }
}
```

### 익명 클래스

```java
public class Calculator {

    private static Map<String, CalculateStrategy> bucket = new HashMap<>();

    static {
        bucket.put("+", new CalculateStrategy() {
            @Override
            public double calculate(double right, double left) {
                return right + left;
            }
        });
        bucket.put("-", new CalculateStrategy() {
            @Override
            public double calculate(double right, double left) {
                return right - left;
            }
        });
        bucket.put("*", new CalculateStrategy(){
            @Override
            public double calculate(double right, double left) {
                return right * left;
            }
        });
        bucket.put("/", new CalculateStrategy() {
            @Override
            public double calculate(double right, double left) {
                return right / left;
            }
        });
    }

    public static double run(double rightOperator, double leftOperator, String operand) {
        CalculateStrategy calculateStrategy = bucket.get(operand);
        return calculateStrategy.calculate(rightOperator, leftOperator);
    }
}

public interface CalculateStrategy {
    double calculate(double right, double left);
}
```

### 람다

```java
public class Calculator {

    private static Map<String, DoubleBinaryOperator> bucket = new HashMap<>();

    static {
        bucket.put("+", Double::sum);
        bucket.put("-", (a, b) -> a - b);
        bucket.put("*", (a, b) -> a * b);
        bucket.put("/", (a, b) -> a / b);
    }

    public static double run(double rightOperator, double leftOperator, String operand) {
        return bucket.get(operand).applyAsDouble(rightOperator, leftOperator);
    }
}
```

### ```Comparator```로 정렬하기
사과 예제에서 농부는 색을 기준으로 사과를 정렬하고 싶다고 한다. 이렇듯 프로그래밍에서 정렬은 매우 흔한 작업이다. 그리고 그 기준 역시 항상 변할 수 있다.

자바 8에서는 ```Collections.sort```뿐 아니라 ```List```에서도 ```sort```메서드를 포함하고 있다. 이는 다음과 같은 ```java.util.Comparator``` 객체를 이용하여 ```sort```의 **동작을 파라미터화할 수 있다.**

```java
// java.util.Comparator
public interface Comparator<T> {
  public int compare(T o1, T o2);
}
```

이 인터페이스를 구현하여 위에서 말한 색을 기준으로 사과를 정렬한다면 다음과 같이 익명 클래스를 사용해서 구현할 수 있다.

```java
inventory.sort(new Comparator<Apple>() {
  public int compare(Apple a1, Apple a2) {
    return a1.getWeight().compareTo(a2.getWeight());
  }
});
```

만약 농부의 요구사항이 바뀌면 새로운 ```Comparator```를 만들어 ```sort```메서드에 전달하면 맞출 수 있다. 그리고 **실제 정렬 세부사항은 추상화되어 있으므로 신경 쓸 필요가 없다.** 이를 람다식으로 표현하면 다음과 같다.

```java
inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));
```
