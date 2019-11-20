# Chapter 6. 스트림으로 데이터 수집

Stream의 연산은 중간연산자와  최종연산으로 구분 할 수 있다.

* 중간연산자
	* 필터링(`filter`, `distinct`)
	* 슬라이싱(`skip`, `limit`)
	* 요소추출 또는 변환(`map`, `flatMap`)

* 최종연산자
	* 요소 검색(`findFirst`, `findAny`, `allMatch`, `noneMatch`, `anyMatch`)
	* 모든 요소를 반복 조합하려 값을 도출(`reduce`)

## 6.1 컬렉터란 무엇인가? 

### 6.1.1 고급 리듀싱 기능을 수행하는 컬렉터
> 훌륭하게 설계된 함수형 API의 또다른 장점으로 높은 수준의 조합성과 재사용성을 꼽을 수 있다.
- collect로 결과를 수집하는 과정을 간단하면서도 유연한 방식으로 정의
    - 컬렉터로 파라미터화된 리듀싱 연산을 수행하므로
- Collectors 유틸리티 클래스는 자주 사용하는 컬렉터 인스턴스를 손쉽게 생성할 수 있는 정적 팩토리 메서드를 제공 (개꿀띠!)
- 결국엔 내가 원하는 형태로 만들어 줄 컬렉터를 사용함으로써 '내가 원하는 모으는 행위'를 적용
```java
List<Dish> menu = DishStream.collect(Collectors.toList()); // 자주 썼지만... 어떻게 되는 건지는 몰랐던... 그 코드...
```

### 6.1.2 미리 정의된 컬렉터
- Collectors 클래스에서 제공하는 __팩토리 메서드__ 의 기능은 크게 세 가지로 구분할 수 있음
    - 스트림 요소를 하나의 값으로 리듀스하고 요약 (count, maxBy)
    - 요소 그룹화 (groupingBy)
    - 요소 분할 (partitioningBy) // 그룹의 특별한 연산

## 6.2 리듀싱과 요약
> 컬렉터로 스트림의 모든 항목을 하나의 결과로 합칠 수 있다! (그 하나의 결과가 복잡한 녀식이라도..!)
```java
long howManyDishes = menu.stream().collect(Collectors.counting());

Map<Dish.Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType)); // ...!!

//        Map<Dish.Type, List<Dish>> dishesByType = new HashMap<>();
//        for (Dish dish : menu) {
//            List<Dish> dishes = dishesByType.getOrDefault(dish.getType(), new ArrayList<>());
//            dishes.add(dish);
//            dishesByType.put(dish.getType(), dishes);
//        }
//
//        assertThat(dishesByType).isEqualTo(menu.stream().collect(groupingBy(Dish::getType)));
```

### 6.2.1 스트림값에서 최댓값과 최솟값 검색
- Collectors.maxBy, Collectors.minBy
    - 스트림의 요소를 비교하는 데 사용할 Comparator를 인수로 받음..!
    - 어떤 식으로 가능한지는.. 6.5에서!


```java
public static <T> Collector<T, ?, Optional<T>>
maxBy(Comparator<? super T> comparator) { // T 인 애들이 적용될 수 있는 모든 Comparator! (Object에 있는 toString을 모든 Type들이 사용 할 수 있듯이)
    return reducing(BinaryOperator.maxBy(comparator));
}

// 예제
Comparator<Dish> dishCaloriesComparator = Comparator.comparingInt(Dish::getCalories);
Optional<Dish> mostCaloriesDish = menu.stream().collect(maxBy(dishCaloriesComparator));
```

```java 
// <? super T> ....!!
List<Country> countries = Arrays.asList(
                new Country("korea", 1),
                new Country("japan", 2),
                new Country("india", 3)); // i, j, k
                
 // public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
 //  Function<? super T, ? extends U> keyExtractor)
 // 
 // comparing 으로 생각해보면... Function<T가 사용할 수 있는 것들로 구성된 ?, U가 사용할 수 있는 것들을 포함한 ?>
 // 이러면 T 로도 ? 가 하는 일들을 다 할 수 있고 (ex. Object::toString)
 // 외부에서도 U 로 알고 사용해도 ?가 이 처리를 모두 해줄수 있음. (이 예에서는 <? extends U> 대신에 <String extends Object>)
 Comparator<Country> countryNameComparator = Comparator.comparing(Object::toString); // <? super Country> 이기 때문에 Object 를 사용할 수 있음
 Country first =  countries.stream().collect(minBy(countryNameComparator)).get();
 assertThat(first.name).isEqualTo("india");
```
### 6.2.2 요약 연산
- 합계나 평균등을 반환하는 연산
```java
IntSummaryStatistics menuStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
System.out.println(menuStatistics); // IntSummaryStatistics{count=9, sum=4200, min=120, average=466.666667, max=800}
```
### 6.2.3 문자열 연결
```java
String 고기만세 = menu.stream()
                .filter(dish -> dish.getType() == Dish.Type.MEAT)
                .map(Dish::getName)
                .collect(joining(", ", "고기만세..! (", ")"));

System.out.println(고기만세); // 고기만세..! (pork, beef, chicken)
```
### 6.2.4 범용 리듀싱 요약 연산
- 요약 연산, 문자열 연결 등... 모두 범용으로 사용 가능 (그냥.. 프로그래밍 편의성과 가독성을 위해서 이전 것들을(count, maxBy...) 사용하는 것
```java
    // (초기 값, 변환함수, 변환된 항목 두개를 하나로 만드는 함수)
    int totalCalories = menu.stream().collect(reducing(0, Dish::getCalories, (i, j) -> i + j));

    // 매개변수로 한개를 받는 reducing
    // 초기값이 없을 수 있기에 Optional<T> 리턴
    // 변환함수는 자기 자신을 리턴 
    Optional<Dish> mostCalorieDish = menu.stream().collect(reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2);


    public static <T, U>
    Collector<T, ?, U> reducing(U identity,
                                Function<? super T, ? extends U> mapper,
                                BinaryOperator<U> op) {
        return new CollectorImpl<>(
                boxSupplier(identity),
                (a, t) -> { a[0] = op.apply(a[0], mapper.apply(t)); }, // Consumer 인데도 값이 변경되는 이유...! boxSupplier 를 통해서 collection 같은 역할(불변 객체도 상태를 들고 있을 수 있도록)
                (a, b) -> { a[0] = op.apply(a[0], b[0]); return a; },
                a -> a[0], CH_NOID);
    }

    // 이렇게 한 번 감싸기 때문에 
    private static <T> Supplier<T[]> boxSupplier(T identity) {
        return () -> (T[]) new Object[] { identity };
    }

    // reduce
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);
```

- collect vs reduce (http://ideone.com/knwImD 이해를 돕기 위한 테스트 코드)
    - 기능을 구현 할 수 있는데... 무엇이 다를까?
        - collect 메서드는 __도출하려는 결과를 누적하는 컨테이너__ 를 바꾸도록 설계
        - reduce 메서드는 두 값을 하나로 도출하는 __불변형__ 연산

## 6.3 그룹화
> 데이터 집합을 하나 이상의 특성으로 분류해서 그룹화하는 연산
```java
// {FISH=[prawns, salmon], OTHER=[french fries, rice, season fruit, pizza], MEAT=[pork, beef, chicken]}
Map<Dish.Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));
```
### 6.3.1 다수준 그룹화
> 두 인수를 받는 팩토리 메서드 Collectors.groupingBy 를 이용해서 항목을 다수준으로 그룹화할 수 있다. (한 인수를 받으면 downstream == Collectors.toList())
``` public static <T, K, A, D>
    Collector<T, ?, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier,
                                          Collector<? super T, A, D> downstream) {
```

```java
public CaloricLevel Dish::getCaloricLevel() {
    if (calories <= 400) {
        return CaloricLevel.DIET;
    } else if (calories <= 700) {
        return CaloricLevel.NORMAL;
    }
    return CaloricLevel.FAT;
}

// {FISH={NORMAL=[salmon], DIET=[prawns]}, MEAT={NORMAL=[beef], DIET=[chicken], FAT=[pork]}, OTHER={NORMAL=[french fries, pizza], DIET=[rice, season fruit]}}
        System.out.println(menu.stream()
                .collect(groupingBy(Dish::getType,
                        groupingBy(Dish::getCaloricLevel))));
```

### 6.3.2 서브그룹으로 데이터 수집 (각 그룹에서 필요한 값 얻어내기)
```java
// {OTHER=4, FISH=2, MEAT=3}
Map<Dish.Type, Long> typesCount = menu.stream()
        .collect(groupingBy(Dish::getType, counting()));
```

- 컬렉터의 결과를 다른 형식에 적용하기
```java
// collectAndThen 예시
String input = " hi, hello ,   world";
List<String> namesFromInput = Arrays.asList(input.split(","));

// 고칠 수 없는 이름으로 만들기
List<String> namesWithoutSpace = namesFromInput.stream()
        .map(String::trim)
        .collect(collectingAndThen(toList(), Collections::unmodifiableList));  // 왠지 쓸 것 같아서리??... ㅎ

// [hi, hello, world]
System.out.println(namesWithoutSpace);
assertThrows(UnsupportedOperationException.class, () -> namesWithoutSpace.set(0, "new"));
```

- groupingBy + mapping (TreeSet 으로 만들어내는 예제, 정렬된 결과를 원할 경우)
```java
//
// treeSet
Map<Dish.Type, TreeSet<Integer>> typesCalories = menu.stream()
                .collect(groupingBy(Dish::getType, mapping(Dish::getCalories, toCollection(TreeSet::new))));

// {OTHER=[120, 350, 530, 550], FISH=[300, 450], MEAT=[400, 700, 800]}
System.out.println(typesCalories);

```

## 6.4 분할
> 특수한 그룹화 기능 (Map<Boolean, X> 의 형태). 결과적으로 그룹화 맵은 최대 (참 아니면 거짓의 값을 갖는) 두 개의 그룹으로 분류된다.

### 6.4.1 분할의 장점
> 분할 함수가 반환하는 참, 거짓 두 가지 요소의 스트림 리스트를 모두 유지한다는 것이 분할의 장점이다.

- 채식 요리와 채식이 아닌 요리 각각의 그룹에서 가장 칼로리가 높은 요리도 찾을 수 있다.
```java
Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream()
                .collect(partitioningBy(Dish::isVegetarian,
                        collectingAndThen(maxBy(Comparator.comparingInt(Dish::getCalories)), Optional::get)));

// {false=pork, true=pizza}
System.out.println(mostCaloricPartitionedByVegetarian);
```        

### 6.4.2 숫자를 소수와 비소수로 분할하기

Collectors 클래스의 정적 팩토리 메서드
- toList
- toSet
- toCollection
- counting
- summingInt
- averagingInt
- summarizingInt
- joining
- maxBy
- minBy
- reducing
- collectingAndThen
- groupingBy
- partitioningBy

> 조합을 통해서 엄청나게 유연한 행위들을 이뤄냄..! (조합..조합..조합... 결국엔 컬렉터..!)

## 6.5 Collector 인터페이스
> Collector 인터페이스는 리듀싱 연산(즉, 컬렉터)을 어떻게 구현할지 제공하는 메서드 집합으로 구성된다. 
```java
public interface Collector<T, A, R> {
    Supplier<A> supplier();
    BiConsumer<A, T> accumulator();
    BinaryOperator<A> combiner();
    Function<A, R> finisher();
    Set<Characteristics> characteristics();
}
```
- T는 수집될 스트림 항목의 제네릭 형식이다.
- A는 누적자, 즉 수집 과정에서 중간 결과를 누적하는 객체의 형식이다. (상태 변경을 통한 누적)
- R은 수집 연산 결과 객체의 형식(항상 그런 것은 아니지만 대개 컬렉션 형식)이다.

### 6.5.1 Collector 인터페이스의 메서드 살펴보기

#### supplier 메서드: 새로운 결과 컨테이너 만들기
    - 누적을 하기 위한 누적자를 만드는 역할!
#### accumulator 메서드: 결과 컨테이너에 요소 추가하기
    - BiConsumer<A, T> 의 형태
#### finisher 메서드: 최종 변환값을 결과 컨테이너로 적용하기
    - 누적자 객체가 이미 최종 겨로가인 상황도 있음 (이때는 항등함수 반환)
#### cominer 메서드: 두 결과 컨테이너 병합
    - 이를 이용해서 병렬로 수행 (7장의 Spliterator 를 사용)

#### 전반적인 과정(병렬)
1. 분할 할 수 있을 때 까지 분할
2. 각 분할에 대해서 supplier, accumulator 이용해서 해당 누적자에 누적 (각각이 호출되는지 확인하고 싶었는데... 프린트로는 안나오는 듯? 아니면 병렬이 적용이 안되었거나)
3. 누적이 완료된 누적자들을 combiner 를 통해서 합치기
4. 하나로 합쳐진 누적자에 finisher 적용

#### Characteristics 메서드
> 스트림을 병렬로 리듀스할 것인지 그리고 병렬로 리듀스한다면 어떤 최적화를 선택해야 할지 힌트를 제공한다.
- UNORDERED
    - 리뉴싱 결과는 스트림 요소의 방문 순서나 누적 순서에 영향을 받지 않는다.
- CONCURRENT
    - ?... 저도 이해가 안되어요....
- IDENTITY_FINISH
    - finsher 가 항등함수를 리턴할때 (생략하기 위함ㄴ)

### 6.5.2 응용하기 (커스텀 toList())

```java
public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {
    @Override
    public Supplier<List<T>> supplier() {
        System.out.println("supplier called");
        return () -> new ArrayList();
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        System.out.println("accumulator called");
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        System.out.println("combiner called");
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        System.out.println("finisher called");
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        // ...
        return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH, CONCURRENT));
    }
}
```

## 6.6 커스텀 컬렉터를 구현해서 성능 개선하기 (소수 구하기 관련)
- 기존의 소수 구하기는 (candidate 기준으로 검사)
- 이를... 지금까지 모은 primes을 이용해서 최적화하자
- 그러기위한 커스텀 콜렉터

### 6.6.1 소수로만 나누기

### 6.6.2 컬렉터 성능 비교

ㅂ




### 참고
- 스트림 실제 구현... (https://github.com/JetBrains/jdk8u_jdk/blob/master/src/share/classes/java/util/stream/ReferencePipeline.java)
    - 이런 애들로 구성되어 있는 듯 (ch8에서 다뤄줄지도?)