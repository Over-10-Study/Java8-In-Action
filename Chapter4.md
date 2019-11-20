# Java 8 in action chapter 4

## 자주 쓰는 스트림 연산들의 SIGNATURE:

### ```Stream<T> filter(Predicate<? super T> predicate)```
예제

```java
filter((Integer i) -> i > 100)
```

### ```Stream<T> sorted(Comparator<? super T> comparator) or sorted()```
예제

```java
sorted(comparing(Dish::getCalories))
sorted(comparing(Dish::getCalories).reversed())
```

### ```<R> Stream<R> map(Function<? super T,? extends R> mapper)```
예제
```java
map(integer i -> i+100)
```

### ```Stream<T> distinct()```

### ```<R,A> R	collect(Collector<? super T,A,R> collector) or <R> R	collect(Supplier<R> supplier, BiConsumer<R,? super T> accumulator, BiConsumer<R,R> combiner)```
예제
```java
collect(Collectors.toList());
```

### ```long count()```

 
## 4.1 what is stream?
스트림 == 스트림이란 데이터 처리 연산을 지원하도록 소스에서 추출된 연속된 요소로 정의힐 수 있다  

특징: 데이터를 소비한다, 파이프라이닝(lazy evaluation, short circuit, 그림 4-2 참조), 내부반복(명시적 반복이 없다 for문, while 문)

스트림은 자바 API에 새로 추가딘 기능으로, 스트림을 이용하면 선언형(sql의 query 처럼)으로 컬렉션 데이터를 처리할 수 있다

데이터의 모음을 쉽게 처리하는 자바진영에서 제공하는 기능이다.

~~~java
    public static List<String> getLowCaloricDishesNamesInJava7(List<Dish> dishes){
        List<Dish> lowCaloricDishes = new ArrayList<>();
        for(Dish d: dishes){
            if(d.getCalories() > 400){
                lowCaloricDishes.add(d);
            }
        }
        List<String> lowCaloricDishesName = new ArrayList<>();
        Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
            public int compare(Dish d1, Dish d2){
                return Integer.compare(d1.getCalories(), d2.getCalories());
            }
        });
        for(Dish d: lowCaloricDishes){
            lowCaloricDishesName.add(d.getName());
        }
        return lowCaloricDishesName;
    }

    public static List<String> getLowCaloricDishesNamesInJava8(List<Dish> dishes){
        return dishes.stream()
                .filter(d -> d.getCalories() > 400)
                .sorted(comparing(Dish::getCalories))
                .map(Dish::getName)
                .collect(toList());
    }
~~~
위 코드에서 볼 수 있다시피 stream을 쓴 코드와 안 쓴코드는 확연한 차이가 있다....특히 타인이 코드를 볼 때 로직을 무수한 for loop 로직을 이해야하 한다.

특히 비스트림 코드는 lowCaloricDishes라는 가비지 변수가 사용되면서 복잡도가 올랐다

스트림은 java안에서 다 처리해준다

선언형 프로그래밍과 동작파라미터화를 줄이면 중복코드를 많이 줄일 수 있다. (저칼로리 ==> 고칼로리)

스티림에 있는 filter, sorted, map, collect는 high-level building block으로서 특정 스레딩 모델에 제한되지 않고 자유롭게 사용가능 

스트림의 장점: 선언형(가독성), 조립성(동작파라미터화), 병렬화(성능)

## 4.2 collections vs stream

### 공통점
1. 콜렉션과 스트림 모두 순차적으로 데이터에 접근한다

### 차이점
1. 콜렉션의 주제는 데이터, 스트림의 주제는 계산이다
2. 데이터 계산 시점이 다르다.
<br>2.1 컬렉션은 현재 자료구조가 포함하는 모든 값을 메모리에 저장하는 자료구조이다. 따라서 요소를 추가하거나 지울 때 모든 요소를 저장해야 하며 미리 계산해야 한다 (DVD)
<br>2.2 스트림은 요청할 때만 요소를 계산한다.(lazy evaluation). 따라서 데이터를 추가하거나 지울 수 없다.
3. 스트림은 탐색할 때 소비되기 때문에 단 한번한 탐색할 수 있다.
4. 컬렉션은 외부반복, 스트림은 내부반복
<br> 4.1 내부반복과 외부반복의 차이는 명시적으로 반복하냐의 차이
<br> 4.2 내부적으로 반복하는 스트림은 상대적으로 최적화가 쉽다, library 안에서 도니까!
```java
List<String> names = new ArrayList<>();
for (Dish d: menu) {
    names.add(d.getName());
}

List<String> names = menu.stream().map(Dish::getName).collect(toList());
```

### DVD와 Streaming 이야기
- DVD == 컬렉션 : 적극적 생성 => 모든 갓을 계산 할 때가지 기다린다. DVD는 모든 데이터가 계산되어 들어가 있는 메모리라 볼 수 있지 않을까?
- Streaming Service == 스트림 : 게으른 생성 =>필요할 때만 연산한다. 로딩 뒤로 가기 앞으로 했을 때 로딩!

## 4.3 중간연산 vs 최종 연산
중간연산 : 다른 스트림이 나온다. 파이프라이닝.

최종연산 : 스트림을 닫는다. 스트림이 안나온다.

중간연산의 중요한 특징은 최종 연산을 스트림 파이프라인에 실행하기 전까지는 아무
연산도 수행하지 않는다는 것 == 게으르다 중간 연산을 합친 다음에 합쳐진 중간 연산으로 하번에 처리하기 때문 (예제같이 보기!)

최종연산은 스트림 파이프라인에 결과를 도출한다. 보통 List, Integer, void등 스트림과 다른 타입

## 4.4 스트림의 3요소
- 질의를 수행할 데이터 소스 (컬렉션이나 intStream같은)
- 스트림 파이프라인을 구성할 중간 연산 연결
- 스트림 파이프라인을 실행하고 만들 최종 연산
- 대표적 중간연산 method: filter, map, limit, sorted, distinct
- 대표적 최종연산 method: forEach, count, collect


