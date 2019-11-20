# 11.3 비블록 코드 만들기
이 장부터는 한 요청의 응답을 기다리며 **블록하는 상황을 피해** 최저가격 검색 애플리케이션의 성능을 높일 수 있는 여러 가지 방법에 대해 알아본다.

현재 다음과 같은 상점 리스트가 있다고 가정하자.

```java
List<Shop> shops = Arrays.asList(new Shop("BestPrice"),
        new Shop("LetsSaveBig"),
        new Shop("MyFavoriteShop"),
        new Shop("BuyItAll"));
```

그리고 다음처럼 제품명을 입력하면 상점 이름과 제품가격 문자열 정보를 포함하는 ```List```를 반환하는 메서드를 구현해야 한다.

```java
public List<String> findPrices(String product);
```

4, 5, 6장에서 배운 스트림을 사용하여 순차적으로 정보를 요청하는 코드는 다음과 같이 구현가능하다.

```java
public List<String> findPrices(String product) {
        return shops.stream()                                     // Stream<Shop>
                .map(shop -> String.format("%s price is %.2f",
                        shop.getName(), shop.getPrice(product)))  // Stream<String>
                .collect(toList())                                // List<String>
                ;
    }
```

이 코드의 성능을 다음과 같이 시간을 측정해보자.

```java
long start = System.nanoTime();
System.out.println(findPrices("myPhone27S"));
long duration = (System.nanoTime() - start) / 1_000_000;
System.out.println("Done in " + duration + " msecs");
```

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 4023 msecs
```

4개의 상점에서 가격을 검색하는 동안 각각 아래와 같이 1초의 대기시간이 있으므로 전체 가격 검색 결과는 약 4초가 소요된다.

```java
public double getPrice(String product) {
        return calculatePrice(product);
}

private double calculatePrice(String product) {
        Util.delay();   // 1초 대기
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
}
```

## 병렬 스트림으로 요청 병렬화하기
7장에서 살펴본 것처럼 자바 스트림은 ```parallelStrema()```을 통해 쉽게 순차 계산을 병렬로 처리할 수 있다.

```java
public List<String> findPricesParallel(String product) {
    return shops.parallelStream()
            .map(shop -> String.format("%s price is %.2f",
                    shop.getName(), shop.getPrice(product)))
            .collect(toList())
            ;
}
```

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 1008 msecs
```

그 결과 위처럼 약 1초의 시간이 소요되었고, 순차 계산보다 훨씬 좋은 성능을 쉽게 얻을 수 있다. 이를 더 개선하기 위해서 ```CompletableFutur``` 기능을 활용해보자.

## ```CompletableFuture```로 비동기 호출 구현하기
앞서 팩토리 메서드 supplyAsync로 ```CompletableFuture```를 만들 수 있었다. 이를 통해 위 예제를 구현해보자.

```java
private List<String> findPricesFuture(String product) {
      List<CompletableFuture<String>> priceFutures =
              shops.stream()   // Stream<Shop>
                      // CompletableFuture로 각각의 가격으르 비동기적으로 계산한다.
                      .map(shop -> CompletableFuture.supplyAsync(
                              () -> String.format("%s price is %.2f",
                                      shop.getName(), shop.getPrice(product))))   // Stream<CompletableFuture<String>>
                      .collect(toList())   // List<CompletableFutur<String>>
              ;

      return priceFutures.stream()   // Stream<Completable<String>>
              // 모든 비동기 동작이 끝나길 기다린다.
              .map(CompletableFuture::join)   // Stream<String>
              .collect(toList())   // List<String>
              ;
}
```

변수 ```pirceFutures```에는 각각 **계산 결과가 끝난** 상점의 이름 문자열을 포함하고 있다.

```java
@SuppressWarnings("unchecked")
public T join() {
        Object r;
        if ((r = result) == null)
            r = waitingGet(false);
        return (T) reportJoin(r);
    }
```

메서드 ```join()```은 ```Future``` 인터페이스의 ```get()``` 메서드와 같은 의미를 같지만, 아무 예외를 발생하지 않는다. 위 ```@SuppressWarnings("unchecked")```가 컴파일러에게 설정한 경고를 무시할 수 있도록 하라는 어노테이션이다. 여기서 ```unchecked```는 검증되지 않은 연산자 관련 경고를 억제하라는 의미이다. ```join()```은 아무 예외를 던지지 않기 때문에 ```try/catch```로 감쌀 필요가 없다.

두 ```map``` 연산을 하나의 스트림이 아닌 **2개의 스트림 파이프라인으로** 처리했다는 것이 중요하다. 스트림 연산은 게으른 특성이 있어 하나의 파이프라인으로 연산을 처리하면 **동기적, 순차적으로** 이루어진다.

![image11-4](https://user-images.githubusercontent.com/34755287/61859481-29a5b880-af03-11e9-8445-25a413679d5f.jpg)

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 2006 msecs
```

## 더 확장성이 좋은 해결 방법
### 순차 스트림
순차 스트림 버전의 코드는 정확히 4개의 상점에 하나의 스레드를 할당해서 4개의 작업을 병렬로 수행하였다. 만약 5개의 상점으로 늘어나면 그에 따라 1초가 증가한다.

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08]
Done in 5026 msecs
```

### 병렬 스트림
병렬 스트림 버전에서는 4개의 상점을 검색하기 위해 4개의 모든 스레드(일반적으로 스레드풀은 4개의 스레드를 제공한다.)가 사용되는 상황이므로, 4개를 처리하고, 1개를 또 처리해야하므로 1초가 더 소요된다.

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08]
Done in 2017 msecs
```

### ```CompletableFuture```

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08]
Done in 2010 msecs
```

해당 버전은 병렬 스트림보다 아주 조금 빨라졌다.

### 상점 9개

```
number of thread: 4

[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08, BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 9017 msecs

[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08, BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 3026 msecs

[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08, BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 3017 msecs
```

병렬 스트림과 ```CompletableFuture```버전 모두 내부적으로 ```Runtime.getRuntime().availableProcessors()```가 반환하는 스레드 수를 사용하면서 비슷한 결과가 된다. 하지만 ```CompletableFuture```은 병렬 스트림 버전에 비해 ```Executor```를 지정할 수 있어 다양한 **스레드 옵션을 줄 수 있어 더욱 최적화가 가능하다.**

## 커스텀 ```Executor``` 사용하기
Executor를 사용하여 작업량에 맞는 스레드 개수를 할당해줄 수 있다. 만약 상점이 100개인데, 400개의 스레드를 갖고 있는 것은 낭비일 뿐이다. 그러므로 상점 개수에 맞는 스레드 개수를 설정해주는 것이 좋다. 하지만 스레드가 너무 많으면 서버가 크래시될 수 있으므로 하나의 ```Executor```에는 스레드 개수가 최대 100개로 제한하는 것이 좋다.

```java
private final Executor executor =
        // 상점 수만큼의 스레드를 갖는 풀을 생성한다.(스레드 수의 범위는 0과 100 사이)
        Executors.newFixedThreadPool(Math.min(shops.size(), 100),
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        // 프로그램 종료를 방해하지 않는 데몬 스레드 사용
                        t.setDaemon(true);
                        return t;
                    }
                });
```

```
[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 1018 msecs

[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08]
Done in 1016 msecs

[BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74, ShopEasy price is 176.08, BestPrice price is 123.26, LetsSaveBig price is 169.47, MyFavoriteShop price is 214.13, BuyItAll price is 184.74]
Done in 1039 msecs
```

### ```CompletableFuture``` 병렬화의 장점
지금까지 컬렉션 계산을 병렬화하는 두 가지 방법을 살펴봤다.
- 병렬 스트림으로 변환해서 컬렉션 처리
- 컬렉션을 반복하면서 ```CompletableFuture``` 내부의 연산 실행
CompletableFuture를 사용하면 전체적인 계산이 **블록되지 않도록 스레드풀의 크기를 조절할 수 있다.**


## 비동기 작업 파이프라인 만들기
이전까지 살펴본 것은 ```Future```내부에서 수행하는 작업이 모두 일회성 작업이었다. 지금부터는 **선언형으로 여러 비동기 연산을** ```CompletableFuture```로 파이프라인화하는 방법을 설명한다.

살펴볼 예제는 이전에 살펴보았던 상점에서 하나의 할인 서비스를 사용하기로 했다고 가정한다.

### ```Shop``` 클래스
이전과 다른 점은 ```getPrice()```이고, 아래와 같다.

```java
public String getPrice(String product) {
        double price = calculatePrice(product);
        Discount.Code code = Discount.Code.values()[random.nextInt(Discount.Code.values().length)];
        return String.format("%s:%.2f:%s", name, price, code);
}
```

### ```Discount``` 클래스

```java
public class Discount {

    public enum Code {
        NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

        private final int percentage;

        Code(int percentage) {
            this.percentage = percentage;
        }
    }

    public static String applyDiscount(Quote quote) {
        return quote.getShopName() + " price is "
              // 기존 가격에 할인 코드를 적용한다.
              + Discount.apply(quote.getPrice(), quote.getDiscountCode());
    }
    private static double apply(double price, Code code) {
       // 서비스의 응답 지연을 흉내내는 딜레이
        Util.delay();
        return Util.format(price * (100 - code.percentage) / 100);
    }
}
```

### ```Quote``` 클래스

```java
public class Quote {

    private final String shopName;
    private final double price;
    private final Discount.Code discountCode;

    public Quote(String shopName, double price, Discount.Code discountCode) {
        this.shopName = shopName;
        this.price = price;
        this.discountCode = discountCode;
    }

    // 상점이름, 할인전 가격, 할안된 가격 정보를 담은 Quote 클래스 인스턴스 생성
    public static Quote parse(String s) {
        String[] split = s.split(":");
        String shopName = split[0];
        double price = Double.parseDouble(split[1]);
        Discount.Code discountCode = Discount.Code.valueOf(split[2]);
        return new Quote(shopName, price, discountCode);
    }

    public String getShopName() {
        return shopName;
    }

    public double getPrice() {
        return price;
    }

    public Discount.Code getDiscountCode() {
        return discountCode;
    }
}
```

## 할인 서비스 사용
처음에는 가장 기본적인 순차적과 동기 방식으로 ```findPrices()``` 메서드를 아래와 같이 구현해보았다.

```java
public List<String> findPrices(String product) {
       return shops.stream()   // Stream<Shop>
               .map(shop -> shop.getPrice(product))   // Stream<String>
               .map(Quote::parse)   // Stream<Quote>
               .map(Discount::applyDiscount)   // Stream<String>
               .collect(toList())   // List<String>
               ;
}
```

- ```map(shop -> shop.getPrice(product))```: 각 상점을 요청한 제품의 가격과 할인 코드로 변환
- ```map(Quote::parse)```: 문자열을 파싱해서 ```Quote``` 객체 생성
- ```map(Discount::applyDiscount)```: 원격 ```Discount``` 서비스에 접근해서 최종 할인가격을 계산하고, 가격에 대응하는 상점 이름을 포함하는 문자열을 반환

```
[BestPrice price is 110.93, LetsSaveBig price is 135.58, MyFavoriteShop price is 192.72, BuyItAll price is 184.74, ShopEasy price is 167.28]
Done in 10032 msecs
```

그 결과, 5개의 상점에 가격 정보를 요청하는데 5초, 가격 정보에 할인 코드를 적용하는데 5초가 걸려 대략 10초가 소요되었다.

## 동기 작업과 비동기 작업 조합하기
이제 ```CompletableFuture```에서 제공하는 기능으로 비동기적으로 아래와 같이 재구현하였다.

```java
private final Executor executor =
           // 상점 수만큼의 스레드를 갖는 풀을 생성한다.(스레드 수의 범위는 0과 100 사이)
           Executors.newFixedThreadPool(Math.min(shops.size(), 100),
                   new ThreadFactory() {
                       public Thread newThread(Runnable r) {
                           Thread t = new Thread(r);
                           // 프로그램 종료를 방해하지 않는 데몬 스레드 사용
                           t.setDaemon(true);
                           return t;
                       }
                   });

private List<String> findPrices(String product) {
        List<CompletableFuture<String>> priceFutures =
                shops.stream()   // Stream<Shop>
                        // 각 상점에서 할인전 가격을 비동기적으로 얻는다.
                        .map(shop -> CompletableFuture.supplyAsync(
                                () -> shop.getPrice(product), executor))   // Stream<CompletableFuture<String>>
                        // 상점에서 반환한 문자열을 Quote 객체로 변환한다.
                        .map(future -> future.thenApply(Quote::parse))   // Stream<CompletableFuture<Quote>>
                        // 결과 Future를 다른 비동기 작업과 조합해서 할인 코드를 적용한다.
                        .map(future -> future.thenCompose(quote ->
                                CompletableFuture.supplyAsync(
                                        () -> Discount.applyDiscount(quote), executor)))   // Stream<CompletableFuture<String>>
                        .collect(toList())   // List<CompletableFuture<String>>
                ;

        return priceFutures.stream()   // Stream<CompletableFuture<String>>
                // 스트림의 모든 Future가 종료되길 기다렸다가 각각의 결과를 출력한다.
                .map(CompletableFuture::join)  // Stream<String>
                .collect(toList())   // List<String>
                ;
}
```

![image11-5](https://user-images.githubusercontent.com/34755287/61859494-2d393f80-af03-11e9-9876-8619a5d4d59c.jpg)

### ```thenApply()```
이 메서드는 ```CompletableFuture```가 끝날 때까지 블록하지 않는다는 점에 주의해야한다. 즉, ```CompletableFuture```가 동작을 **완전히 완료한 다음에** ```thenApply```에 전달된 람다식을 적용할 수 있다.

### ```thenCompose```
이 메서드는 두 비동기 연산을 파이프라인으로 만들어 준다. 즉, 첫 번째 연산의 결과를 두 번째 연산으로 전달하는 역할을 한다. 위 예제에서는 아래와 같은 두 비동기 연산을 연결한다.
- 상점에서 가격 정보를 얻어 와서 ```Quote```로 변환하기
- 변환된 ```Quote```를 ```Discount``` 서비스로 전달해서 할인된 최종가격 획득하기
따라서 Future를 사용하는 동안 메인 스레드는 UI 이벤트 처리와 같은 유용한 작업을 수행할 수 있다.

```
[BestPrice price is 110.93, LetsSaveBig price is 135.58, MyFavoriteShop price is 192.72, BuyItAll price is 184.74, ShopEasy price is 167.28]
Done in 2043 msecs
```

위 메서드들에도 ```Async``` 키워드가 붙는 버전이 존재한다. ```Async```로 끝나지 않는 메서드는 이전 작업을 수행한 스레드와 같은 스레드에서 작업을 실행함을 의미하며, ```Async```로 끝나는 메서드는 다음 작업이 다른 스레드에서 실행되도록 스레드 풀에 제출한다.

## 독립 ```CompletablceFuture```와 비독립 ```CompletabeFuture``` 합치기
위에서 살펴본 ```thenCompose``` 메서드와 달리 첫 번째 ```CompletableFuture``` 연산의 완료와 상관없이 두 번째 ```CompletableFuture``` 연산을 실행할 수 있어야 한다. 이 때 ```thenCombine```을 사용한다. ```thenCombine``` 메서드의 특징은 다음과 같다.

- ```BiFunction```을 두 번째 인수로 받는다.
- ```thenCompose```와 같이 ```Async``` 버전이 존재한다. 이 버전은 ```BiFunction```이 정의하는 조합 동작이 스레드 풀로 제출되면서 **별도의 테스크에서 비동기적으로 수행된다.**
