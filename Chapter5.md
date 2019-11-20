# chapter_5_스트림의 활용

Stream의 연산은 중간연산자와  최종연산으로 구분 할 수 있다.

* 중간연산자
	* 필터링(`filter`, `distinct`)
	* 슬라이싱(`skip`, `limit`)
	* 요소추출 또는 변환(`map`, `flatMap`)

* 최종연산자
	* 요소 검색(`findFirst`, `findAny`, `allMatch`, `noneMatch`, `anyMatch`)
	* 모든 요소를 반복 조합하려 값을 도출(`reduce`)

## 5.1 필터링과 슬라이싱

### 5.1.1 프레디케이트로 필터링

```java
List<Dish> vegetarianmenu = menu.stream()
								.filter(Dish::isVegetarian)	// 야채만 고르기
								.collect(toList());			// 결과를 List로 반환
```

### 5.1.2 고유 요소 필터링

```java
List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
numbers.stream()
		.filter(i -> i % 2 == 0)		// 짝수 고르기
		.distinct()						// 중복 제거
		.forEach(System.out::println);	// 요소 출력
```

### 5.1.3 스트림 축소

```java
List<Dish> dishes = menu.stream()
						.filter(d -> d.getCalories() > 300)	// 칼로리가 300 초과하는 음식 고르기
						.limit(3)							// 처음 최대 3가지 요리만 반환
						.collect(toList());					// 결과를 List로 반환
```

### 5.1.4 요소 건너뛰기

```java
List<Dish> dishes = menu.stream()
						.filter(d -> d.getCalories() > 300)	// 칼로리가 300 초과하는 음식 고르기
						.skip(2)							// 처음 2가지 요리를 제외한 나머지 반환
						.collect(toList());					// 결과를 List로 반환
```
> n개 이하의 요소를 포함하는 스트림에 `skip(n)`을 요청하면 **빈 스트림** 반환.

## 5.2 매핑

특정 객체에서 특정 데이터를 선택하는 작업은 데이터 처리 과정에서 자주 수행되는 연산이다.
`map`과 `flatMap` 메서드는 특정 데이터를 선택하는 기능을 제공한다.

### 5.2.1 스트림의 각 요소에 함수 적용하기

기존의 값을 '**고친다^modify^**' 라는 개념보다는 '**새로운 버전을 만든다**' 라는 개념에 가깝다.
`변환^transforming^'에 가까운 '**매핑^mapping^**'이라는 단어를 사용한다.

```java
List<String> dishNames = menu.stream()
								.map(Dish::getName)	// 요리에서 이름을 추출한 새로운 stream을 반환
								.collect(toList());	// 결과를 List로 반환
```

### 5.2.2 스트림 평면화

> 생성된 Stream을 하나의 Stream으로 평면화
> Stream의 각 값을 다른 Stream으로 만든 다음에 모든 Stream을 하나의 Stream으로 연결한다.

```java
// ["Hello", "World"] -> ["H", "e", "l", "o", "W", "r", "d"]
words.stream()
		.map(word -> word.split(""))	// Stream<String[]>
		.distinct()						// Stream<String[]>
		.collect(toList());				// List<String[]>

words.stream()
		.map(word -> word.split(""))	// Stream<String[]>
		.map(Arrays::stream)			// Stream<Stream<String>>
		.distinct()						// Stream<Stream<String>>
		.collect(toList());				// List<Stream<String>>

words.stream()
		.map(word -> word.split(""))	// Stream<String[]>
		.flatMap(Arrays::stream)		// Stream<String>
		.distinct()						// Stream<Stream<String>>
		.collect(toList());				// List<Stream<String>>
```

```java
// [1, 2, 3] [3, 4] -> [(1, 3), (1, 4), (2, 3), (2, 4), (3, 3), (3, 4)]

List<Integer> numbers1 = Arrays.asList(1, 2, 3);
List<Integer> numbers2 = Arrays.asList(3, 4);

List<int[]> pairs =
	numbers1.stream()		// Stream<Integer>
			.flatMap(
						i -> numbers2.stream()		// Stream<Integer>
									.map(j -> new int[]{i, j}) // Stream<int[]>
					) // Stream<int[]>
			.collect(toList());
```

## 5.3 검색과 매칭
> **특정 속성이 데이터 집합에 있는지 여부**를 검색하는 데이터 처리도 자주 사용된다.

### 5.3.2 프레디케이트가 모든 요소와 일치하는지 검사

> `anyMatch`, `allMatch`, `noneMatch` 세가지 메서드는 스트림 쇼트기법, 즉 자바의 `&&`, `||`와 같은 연산을 활용한다.

```java
// 모든 요리가 1000칼로리 이하인지 확인
boolean isHealthy = menu.stream()
						.allMatch(d -> d.getCalories() < 1000);

boolean isHealthy = menu.stream()
						.noneMatch(d -> d.getCalories() >= 1000);
```

### 5.3.3 요소 검색

```java
// 현재 Stream에서 임의의 한 요소를 반환한다.
Optional<Dish> dish = menu.stream()
							.filter(Dish::isVegetarian)
							.findAny(); // 아무 요소도 반환하지 않을 수 있다.
```

> Optioanl 이란?
> `Optional<T>` 클래스는 **값의 존재나 부재의 여부를 표현**하는 컨테이너 클래스다.
> 값이 존재하는지 확인하고 값이 없을 때 어떻게 처리할 것인지 강제하는 기능을 제공한다.
>  * `ifPresent()`는 `Optional`이 값을 포함하면 참(true)을 반환하고, 값을 포함하지 않으면 거짓(false)를 반환
>  * `ifPresent(Consumer<T> block)`은 값이 있으면 주어진 block을 실행한다.
>  * `T get()`은 값이 존재하면 값을 반환, 값이 없으면 `NoSuchElementException`을 일으킨다.
>  * `T orElse(T other)` 는 값이 있으면 값을 반환하고, 값이 없으면 기본값을 반환한다.

```java
// 현재 Stream에서 임의의 한 요소를 반환한다.
menu.stream()
	.filter(Dish::isVegetarian)
	.findAny(d -> System.out.println(d.getName())); // 값이 있으면 출력, 없으면 아무일도 일어나지 않는다.
```

### 5.3.4 첫 번째 요소 찾기
```java
List<Integer> someNumbers = Arrays.asList(1, 2, 3, 4, 5);
Optional<Integer> firstSquareDivisibleByThree =
	someNumbers.stream()
				.map(x -> x * x)
				.filter(x -> x % 3 == 0)
				.findFirst(); // 9;
```

> `findFirst`와 `findAny`는 언제 사용하나?
> 병렬 실행에서는 첫 번째 요소를 찾기 어렵다. 
> 따라서 요소의 반환 순서가 상관없다면 병렬 Stream에서는 제약이 적은 `findAny`를 사용한다.

## 5.4 리듀싱
> **리듀싱 연산**(모든 스트림의 요소를 처리해서 값으로 도출)
> 함수형 프로그래밍 언어 용어로는 이 과정이 마치 종이(`Stream`)을 작은 조각이 될때까지 반복해서 접는 것과 비슷하다는 의미로 **폴드^fold^** 라고 부른다.

### 5.4.1 요소의 합

```java
// 방법 1
int sum = 0;	// 변수의 초기값 0
for (int x : numbers) {
	sum += x;	// 리스트의 모든 요소를 조합하는 연산(+)
}

// 방법 2
int sum = numbers.stream().reduce(0, (a, b) -> a + b);
int sum = numbers.stream().reduce(0, Integer::sum);
// 초기값 0, 두 요소를 조합해서 새로운 값을 만드는 BinaryOperator<T>를 사용.
// Stream이 하나의 값으로 줄어들 때까지 람다는 각 요소를 반복해서 조합한다.

// 초기값 없음
Optional<Integer> sum = numbers.stream().reduce((a, b) -> (a + b));
// Stream에 아무 요소가 없는 상황에서 초기값이 없으면 reduce로 합계를 반환할 수 없다.
// 따라서 합계가 없음을 가리킬 수 있도록 Optional 객체로 감싼 결과를 반환한다.
```

### 5.4.2 최대값과 최솟값

```java
// 최대값
Optional<Integer> max = numbers.stream().reduce(Integer::max);

// 최솟값
Optional<Integer> min = numbers.stream().reduce(Integer::min);
```

```java
// 요리의 개수 계산.
int count = menu.stream()
				.map(d -> 1)
				.reduce(0, (a, b) -> a + b);				
// map과 reduce를 연결하는 기법을 맵 리듀스 패턴이라고 한다.
// 쉽게 병렬화 하는 특징 덕분에 구글이 웹 검색에 적용하면서 유명해졌다.
// reduce를 이용하면 내부 반복이 추상화 되면서 내부 구현에서 병렬로 reduce를 실행할 수 있게 된다.(???)

long count = menu.stream().count();
```

> Stream 연산: 상태 없음과 상태 있음
> * `map`, `filter` 등은 입력 Stream에서 각 요소를 받아 0도는 결과를 출력 Stream으로 내보낸다. 즉, 내부 상태를 갖지 않는 연산이다.
> * `reduce`, `sum`, `max` 같은 연산은 결과를 누적할 내부 상태가 필요하다. Stream에서 처리하는 **요소의 수 와 관계없이** 내부상태의 크기는 **한정^bounded^** 되어 있다.
> * `sorted`, `distinct` 같은 연산은 `filter`와 `map`처럼 Stream을 입력으로 받아 다른 Stream을 출력하는 것처럼 보일 수 있다. 하지만 `filter`와 `map`과는 다르다. 어떤 요소를 출력 Stream으로 추가하려면 **모든 요소가 버퍼에 추가되어 있어야 한다.** 연산을 수행하는데 필요한 저장소 크기는 정해져있지 않다. 따라서 데이터 스트림의 크기가 크거나 무한이라면 문제가 생길 수 있다. 즉, 이러한 연산은 **내부 상태를 갖는 연산^statfulOperation^** 으로 간주 할 수 있다.

## 5.5 실전 연습

* 예제 5-1) 2011년에 일어난 모든 트랜잭션을 찾아서 값을 오름차순으로 정렬하시오.
```java
List<Transaction> tr2011 =
	transactions.stream()
				.filter(transaction -> transaction.getYear() == 2011)
				.sorted(comparing(Transaction::getValue))
				.collect(toList());
```

* 예제 5-2) 거래자가 근무하는 모든 도시를 중복 없이 나열하시오.

```java
List<String> cities =
	transactions.stream()
				.filter(transaction -> transaction.getTrader().getCity())
				.distinct()
				.collect(toList());
				
Set<String> cities =
	transactions.stream()
				.filter(transaction -> transaction.getTrader().getCity())
				.collect(toSet());
```

* 예제 5-3) 케임브리지에서 근무하는 모든 거래자를 찾아서 이름순으로 정렬하시오.

```java
List<String> traders=
	transactions.stream()
				.map(Transaction::getTrader)
				.filter(trader -> trader.getCity().equals("Cambridge")
				.distinct()
				.sorted(comparing(Trader::getName))
				.collect(toList());
```

* 예제 5-4) 모든 거래자의 이름을 알파벳 순으로 정렬해서 반환하시오.

```java
String traders=
	transactions.stream()
				.map(transaction -> transaction.getTrader().getName())
				.distinct()
				.sorted()
				.reduce("", (n1, n2) -> n1 + n2);
				
String traders=
	transactions.stream()
				.map(transaction -> transaction.getTrader().getName())
				.distinct()
				.sorted()
				.collect(joining());
```

* 예제 5-5) 밀라노에 거래자가 있는가?

```java
boolean milanBased = 
	transactions.stream()
				.anyMatch(transaction -> transaction.getTrader()
													.getCity()
													.equals("Milan"));
```

* 예제 5-6) 케임브리지에 거주하는 거래자의 모든 트랜잭션값을 출력하시오.

```java
transactions.stream()
	.filter(t -> "Cambridge".equals(t.getTrader().getCity()))
	.map(Transaction::getValue)
	.forEach(System.out::println);
```

* 예제 5-7 전체 트랜잭션 중 최댓값은 얼마인가?

```java
Optional<Integer> highestValue =
	transactions.stream()		
		.map(Transaction::getValue)
		.reduce(Integer::max);
		
Optional<Integer> highestValue =
	transactions.stream()
				.max(comparing(Transaction::getValue));
```

## 5.6 숫자형 스트림

### 5.6.1 기본형 특화 스트림

* 자바 8 에서는 세가지 기본형 특화 스트림을 제공한다.
	* `IntStream`
	* `DoubleStream`
	* `LongStream`

* 각각의 인터페이스는 `sum`, `max` 같이 자주 사용하는 **숫자 관련 리듀싱 연산 수행 메서드** 를 제공한다.
```java
int calories = menu.stream()						// Stream<Dish> 반환
					.mapToInt(Dish::getCalories)	// IntStream 반환
					.sum();
```

* 필요할 때 다시 Object Stream 으로 복원하는 기능도 제공한다.(`boxed`)
```java
IntStream inStream = menu.stream().mapToint(Dish::getCalories);	// Stream -> IntStream
Stream<Integer> stream = intStream.boxed();	// IntStream -> Stream
```

* 특화 Stream은 오직 박싱 과정에서 일어나는 효율성과 관련 있으며 Stream에 추가 기능을 제공하지 않는다.

* 기본값
	* `OptionalInt`
	* `OptionalDouble`
	* `OptionalLong`
	
```java
OptionalInt maxCalories = menu.stream()
								.mapToInt(Dish::getCalories)
								.max();
int max = maxCalories.orElse(1);
```

### 5.6.2 숫자 범위

```java
IntStream evenNumbers = IntStream.rangeClosed(1, 100)			// 1 ~ 100
									.filter(n -> n % 2 == 0);	// 1 ~ 100 범위의 짝수
									
IntStream evenNumbers = IntStream.range(1, 100)					// 1 ~ 99
									.filter(n -> n % 2 == 0);	// 1 ~ 99 범위의 짝수
```

### 5.6.3 숫자 Stream의 활용: 피타고라스 수

```java
Stream<int[]> pythagoreanTriples =
	IntStream.rangeClosed(1, 100).boxed()
				.flatMap(a ->
					IntStream.rangeClosed(a, 100)
								.filter(b -> Math.sqrt(a * a + b * b) % 1 == 0)
								.mapToObj(b ->
									new int[]{a, b, (int) Math.sqrt(a * a + b * b})
						);
// 문제) 제곱근을 두번 계산한다.

Stream<double[]> pythagoreanTriples2 =
	IntStream.rangeClosed(1, 100).boxed()
				.flatMap(a ->
					IntStream.rangeClosed(a, 100)								
								.mapToObj(b ->
									new double[]{a, b, (int) Math.sqrt(a * a + b * b})
								.filter(t -> t[2] % 1 == 0)
						);
```

## 5.7 Stream 만들기

### 5.7.1 값으로 Stream 만들기

```java
Stream<String> stream = stream.of("Java 8", "Lambdas ", "In ", "Action");
steam.map(String::toUpperCase).forEach("System.out::println);

Stream<String> emptyStream = Stream.empty();
```

### 5.7.2 배열로 Stream 만들기

```java
int[] numbers = {2, 3, 5, 7, 11, 13};
int sum = Arrays.stream(numbers).sum(); // 41
```

### 5.7.3 파일로 Stream 만들기
```java
long uniqueWords = 0;

// Stream은 자원을 자동으로 해제할 수 있는 AutoClosable이다.
try(Stream<String> lines = 
		Files.lines(Paths.get("data.txt"), Charset.defaultCharset())) {
	uniqueWords = lines.flatMap(line -> Stream.of(line.split(" "))) // 단어 Stream 생성
						.distinct()
						.count();
} catch (IOException e) {
	// 예외처리
}
```

### 5.7.4 함수로 무한 Stream 만들기

함수를 이용해서 Stream을 만들 수 있는 2개의 정적 메서드가 존재한다.
* `Stream.iterate`
* `Stream.generate`

따라서, 무제한으로 값을 계산할 수 있다!
하지만, 보통 무한한 값을 출력하지 않도록 `limit(n)`함수와 함께 연결해서 사용한다.

```java
Stream.iterate(0, n -> n + 2)
		.limit(10)
		.forEach(sysout.out::println);
/*
0
2
4
...
*/
```

* `iterate`
	* 요청할 때 언바운드 스트림^unboundedStream^ 이라고 표현한다.

```java
Stream.iterate(new int[]{0, 1}, t -> new int[]{t[0], t[0] + t[1]})
		.limit(20)
		.forEach(t -> System.out.println("(" + t[0] + ", " + t[1] +")"));
		// (0, 1), (1, 1), (1, 2), (2, 3), ...

Stream.iterate(new int[]{0, 1}, t -> new int[]{t[0], t[0] + t[1]})
		.limit(20)
		.map(t -> t[0])
		.forEach(System.out::println);	
		// 0 , 1, 1, 2, 3, 5, ...
```

* `generate`
	* `iterate`와 달리 생산된 각 값을 연속적으로 계산하지 않는다.
	* Supplier<T>를 인수로 받아서 새로운 값을 생산한다.

```java
Stream.generate(Math.random)
		.limit(5)
		.forEach(System.out::println);
```

> 가변 상태 객체, 불변 상태 객체
> 병렬로 처리하면서 올바른 결과를 얻으려면 **불변 상태 기법** 을 고수해야한다.
```java
// 람다를 전달(불변 상태 객체)
IntStream ones = IntStream.generate(() -> 1);

// 객체를 전달 -> 상태 필드를 정의할 수 있다.
IntStream twos = IntStrea.generate(new IntSupplier() {
	public int getAsInt() {
		return 2;
	}
});

// 객체에 상태필드 정의(가변상태 객체)
IntSupplier fib = new IntSupplier() {
	private int previous = 0;	// 상태필드 previous
	private int current = 1;	// 상태필드 current
	public int getAsInt() {
		int oldPrevious = this.previous;
		int nextValue = this.previous + this.current;
		this.previous = this.current;
		this.current = nextValue;
		return oldPrevious;
	}
};

IntStream.generate(fib).limit(10).forEach(System.out::println);
```
