#java chapter 10: Optional
~~~java
//as is
public String getCarInsuranceName(Person person) {
    if (person != null) {
        Car car = person.getCar();
        if (Car != null) {
            Insurance insurance = car.getInsurance();
            if (insurance != null) {
                return insurance.getName();
            }
        }
    }
}
~~~
~~~java
//to be
public String getCarInsuranceName(Optional<Person> person) {
    return person.flatMap(Person::getCat)
                 .flatMap(Car::getInsurance)
                 .map(Insurance::getName)
                 .orElse("Unknown")
}
~~~

## 1. opttional vs null
- null은 개발자에게 있어서 가장 많이 버그를 유발하는 존재!
- null 때문에 발생하는 문제:
1. 에러의 근원: NullPointerException
2. 코드를 어지럽힌다: 위에 예시 참고
3. 아무 의미가 없다: null은 아무 의미도 표현하지 않는다. 반면 optional은 optional를 명시적으로 사용함으로서 값이 있을 수고 있고 없을 수도 있다는 것을 말해준다. 또한 값이 있어햐 할 곳에 없다면 알고리즘에 대한 체크 가능성을 열어준다.
4. 자바 철학에 위배: 자바는 pointer를 쓰지 않는다

- 따라서 자바는 하스켈의 선택형값의 개념을 빌려 Optional<T>제공을 한다
- Optional를 쉽게 말하면 유용한 기능을 많이 제공하는 객체와 null를 담는 "그릇이다" -by JM

## 2. Optional 사용법
- Optional.empty(): 빈 Optional 반환
- Optional.of(car): Car객체가 든 Optional 반환
- Optional.ofNullable(car): car객체가 있으면 객체 아니면 null 반환
~~~java
//다음은 맞는 코드일까요?
Optional<Person> personOpt = Optional.of(person);
Optional<String> name = personOpt.map(Person::getCar)//Optional<Optional>
                                 .map(Car::getInsurance)
                                 .map(Insurance::getName)
                                 
//우리의 flatMap!
~~~
~~~java
public String getCarInsuranceName(Optional<Person> person) {
    return person.flatMap(Person::getCar)
                 .flatMap(Car::getInsurance)
                 .map(Insurance::getName)
                 .orElse("Unknonw");
}
// 주목포인트 하나 : 반환 type
// 주목포인트 둘 : map
~~~
- Optional값 받아오기:
1. get()
2. orElse(T Other)
3. orElseGet(Supplier)
4. orElseThrow(exceptionSupplier)
5. ifPresent(Consumer)

- 위 모든 함수들은 Optional 값이 있다면 안에 들어있는 값을 반환한다

- p.326쪽 같이 보기

~~~java
somethingOpt.filter(something -> "something".equals(somthing.getSomething())).ifPresent(something -> System.out.println("ok"))
~~~

- 애매한 값은 ofNullAble로 감싸라! (Optional<Object> value = Optional.ofNullable(map.get("key))).

~~~java
public int readDuration(Properties props, String name) {
    return Optional.ofNullable(props.getProperty(name))
                   .flatMap(OptionalUtility::stringToInt)
                   .filter(i->i>0)
                   .orElse(0)
}
~~~

- Optional의 특징 중간,최종 연산이 없다....거의 모두다 Optional를 반환 (orElse있을 때에는 Optional안에 있는 값 반환)
- 기본현 특화 Optional 사용하지마라: 성능도 나아지지 않고, map, flatMap, filter기능 없음