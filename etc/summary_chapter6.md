## 무엇을 적는 것이 좋을까?


### 어떤 질문들이 있어야 할까?

### 어떤 방식으로 접근해갈까?

### 어떤 사람들을 위한 글인가?

### 고려했으면 하는 부분들
- 자바 스트림 API의 특징
    - 선언형 (더 간결하고 가독성이 좋다)
    - 조립할 수 있음 (유연성이 좋아진다)
    - 병렬화 (성능이 좋아진다))
- 컬렉션의 주제는 데이터고 스트림의 주제는 계산이다 (p133, 스트림소개))


### 글의 흐름
- 스트림을 잘 만들었는데?.... 그렇다면 끝?? 
    - 언제 스트림(흐름)이 멈추나요?
    - 중간연산을 통해서 계속 흘려보냈음 // 변환
    - 이제 최종연산으로 소비를 하자! 

- 어떤 방식으로 소비가 가능할까요? (특징은?? 유연성?? 어떻게 유연성을 만족시킬까? 조합...!!)
    - 요소
    - 그룹
    - 분할
- 그렇다면 어떤 형태로 표현할 수 있을까? (Collctor, 즉 행위를 넘겨준다면... 어떻게 행위들을 모아서 보내줄까요? 어떤 행위들이 공통적으로 필요하고 그것을 어떻게 만족시킬까요?)
- 나만의 콜렉터 만들기..!



```java
 public static <T> Collector<T, ?, Optional<T>>
    reducing(BinaryOperator<T> op) {
        class OptionalBox implements Consumer<T> {
            T value = null;
            boolean present = false;

            @Override
            public void accept(T t) {
                if (present) {
                    value = op.apply(value, t);
                }
                else {
                    value = t;
                    present = true;
                }
            }
        }

        return new CollectorImpl<T, OptionalBox, Optional<T>>(
                OptionalBox::new, OptionalBox::accept,
                (a, b) -> { if (b.present) a.accept(b.value); return a; },  // (OptionalBox a, T b)
                a -> Optional.ofNullable(a.value), CH_NOID);
    }
```