# Flow 느껴보기
## KotlinConf'23 Agenda "flow 느껴보기"

발표자료 : [https://speakerdeck.com/davidkwon7/flow-neuggyeobogi](https://speakerdeck.com/davidkwon7/flow-neuggyeobogi)

# Coroutine Flow 소개

코루틴의 플로우를 설명하는 많은 키워드들

비동기적으로 처리되는 데이터 스트림 생성 및 처리

앱의 반응성 유지

데이터를 생산 (emit)하는 오퍼레이터와 소비(collect) 하는 오퍼레이터

비동기 코드를 동기적인 코드처럼 작성할 수 있다 데이터를 비동기적으로 처리하고 UI를 업데이트 하는 등의 활용

오퍼레이터를 연결하여 데이터 파이프라인 생성..

중 발표자는 ‘**데이터 스트림 생산 및 소비를 하는 경량화된 비동기 스레드’** 라는것에 초점을 맞추고 설명.

안드로이드 공식 문서에서는 flow를 **‘여러 값을 순차적으로 내보낼 수 있는 유형’** 이라고 소개.
<img width="735" alt="image-20230519-044737" src="https://github.com/H-Zoon/FlowExam/assets/43941511/2057a574-7123-46e8-85d6-b715c56d1060">


다음은 코루틴을 이용해 1~5 의 숫자에서 짝수만 출력하는 코드 예제

```
suspend fun flowTest() {
    flow {
        (1..5).filter {
            isEvenNumber(it)
        }.forEach {
            emit(it)
        }
    }.collect { value ->
        println("flow $value")
    }
}
```

위의 함수는 flow 블록에서 flow를 생성. 숫자 시퀸스에서 **filter** 함수를 이용해 짝수만 필터링. **forEach** 함수를 이용해서 각 숫자 처리, **emit** 함수를 이용해 flow 방출.

다음 코드의 flow를 수집하여 출력한 결과는 다음과 같음.

```
flow 2
flow 4
```

# Flow Operator

## merge

merge operator를 사용하면 2개 이상의 flow 스트림을 하나의 스트림으로 병합 가능.

이때 2개 스트림이 나누어진 상태에서 합쳐지는 것이 아닌, 시간에 흐름에 따라 정렬됨.

```
suspend fun main() {
    val string1: Flow<String> = flowOf("a", "b", "c").onEach {
        delay(100)
    }
    val string2: Flow<String> = flowOf("A", "B", "C").onEach {
        delay(50)
    }
    val together: Flow<String> = merge(string1, string2)
    print(together.toList())
}
```

## combine

combine을 사용함으로써 2가지 이상의 flow 간의 결합이 가능해지기 때문에,

2가지 이상의 데이터를 효과적으로 처리 가능.

```
suspend fun main() {
    val flow1: Flow<Int> = flowOf(1, 2).onEach { delay(10) }
    val flow2: Flow<String> = flowOf("A", "B", "C").onEach { delay(15) }
    flow1.combine(flow2) { i, s -> i.toString() + s }.collect {
        println(it)
    }
}
```

## zip

zip은 combine과 유사한 operator입니다. 차이점은 결합을 하게 되는 경우 발행하는 이벤트의 횟수가 동일하게 맞춰짐.

```
suspend fun main() {
    val flow1: Flow<Int> = flowOf(1, 2).onEach { delay(10) }
    val flow2: Flow<String> = flowOf("A", "B", "C").onEach { delay(15) }
    flow1.zip(flow2) { i, s -> i.toString() + s }.collect {
        println(it)
    }
}
```

## reduce

reduce의 프로퍼티는 accumulator, value 가 존재.

accumulate는 누적된 값을 보여주고, value는 이후에 연산이 진행될 값을 보여줌.

```
suspend fun main() {
    runBlocking {
        flowOf(1, 2, 3)
            .reduce { acc, value -> acc + value }
            .also { println("value $it") }
    }
}
```

## fold

reduce와 유사한 기능을 가지고 있음.

동일하게 accumulate, value를 사용. 차이점은 fold의 경우 fold의 파라미터에 처음 연산 값을 넣어줄 수 있음.

```
suspend fun main() {
    runBlocking {
        flowOf(1, 2, 3)
            .fold(10) { acc, value -> acc + value }
            .also { println("value $it") }
    }
}
```

## scan

fold와 유사하게 결과값을 누적해주는 기능이 있지만, 리스트 형태로 중간 결과값을 모두 가지고 있다는 특징이 있음.

```
suspend fun main() {
    flowOf(1, 2, 3, 4)
        .scan(0) { acc, v -> acc + v }
        .collect { println(it) }
}
```

## buffer

buffer를 사용함으로써 기존의 발행/소비 패턴과 다르게, 데이터 abc 발행 후 데이터 abc 소비하는 형태로 flow를 변경해줄 수 있음.

```
suspend fun previous() {
    flowOf("A", "B", "C")
        .onEach { delay(300) }
        .buffer(3)
        .collect { println("$it") }
}
```

## map / filter

map을 통해 데이터를 변환 가능. filter를 사용하면 조건에 맞는 데이터만 넘겨줄 수 있음.

```
val flowData = flowOf(1, 2, 3, 4, 5)
flowData
    .map { number -> number }
    .filter { it % 2 == 0 }
    .collect { println(it) }
```

## take

take 파라미터 안의 수만큼 소비하며 그 이후에 flow는 cancel.

```
suspend fun main() {
    flowOf(1, 2, 3, 4, 5, 6)
        .take(2)
        .collect { println(it) }
}
```

## drop

drop을 사용하면 작성된 숫자만큼의 값들의 수를 넘길 수 있음.

```
suspend fun main() {
    flowOf(1, 2, 3, 4, 5, 6)
        .drop(2)
        .collect { println(it) }
}
```

## 예제코드의 모든 출력값

```
------FlowMarge------
[A, a, B, C, b, c]
-----FlowCombine-----
1A
2A
2B
2C
--------FlowZip-------
1A
2B
------FlowReduce------
value 6
-------FlowFold-------
value 16
-------FlowScan-------
0
1
3
6
10
------FlowBuffer------
A
B
C
-------FlowData-------
2
4
-------FlowTake-------
1
2
-------FlowDrop-------
3
4
5
6
```

# StateFlow / SharedFlow (kotlinConf recap)

다음 그림은 상황에 적합한 Flow를 사용한 예시.

서버에서는 1:N 구조를 가지므로 수집하는 모든 소비자에게 sharedFlow를 통해 전달 하는 과정을 볼 수 있음.
<img width="477" alt="image-20230519-054009" src="https://github.com/H-Zoon/FlowExam/assets/43941511/58d59e2a-3e6b-4106-a9ca-c34cc7ccf98a">


## SharedFlow

- 수집(collect)하는 모든 소비자에게 값을 보내줄 수 있다.

- 처음 값 구독 시작했을 때는 처음 방출 데이터를 갖지 못한다.
- 같은 값을 연속적으로 방출하게 되는 경우 두 값 모두 collect한다.

## StateFlow

- 현재 상태와 새로운 상태 업데이트를 수집기(collector)에 내보내는 역할을 수행할 수 있다.
- 처음 구독 시작 시, 처음 방출 데이터를 갖는다.
- 같은 값을 연속적으로 방출하게 되는 경우 처음 값만 collect한다.

다음은 flow를 통해 체팅 어플리케이션의 내용을 viewModelScope를 이용하여 방출하는 코드 예제

```
// 메세지 전송
class ChatViewModel : CommonViewModel() {
    fun sendMessage(message: String) {
        viewModelScope.launch {
            chatService.sendEvent(
                MessageEvent(
                    username = username,
                    messageText = message,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
```

sendMessage 함수를 통해 메세지 전달.

viewModelScope.launch를 사용하여 코루틴을 실행하고, chatService를 통해 MessageEvent를 전송.

## Cold Flow / Hot Flow

Cold Flow : collector가 방출된 데이터를 수집할 때마다 새로운 데이터 스트림 생성하므로 collector들은 각각의 개별적인 데이터 스트림에서 데이터 수집.

Hot Flow : collector들이 데이터 스트림을 공유하여 동일한 데이터를 수집하며, 기본적으로 collector가 없어도 데이터 방출합니다. 다수의 구독자가 동일한 스트림을 전달 가능. (다운로드 작업, 동영상 스트림)

```
suspend fun handleSocket(socket: WebSocketServerSession, eventFlow: MutableSharedFlow<ChatEvent>)
= coroutineScope {
    launch {
        eventFlow.collect { event ->
            socket.sendSerialized(event)
        }
    }
}
```

WebSocketServerSession과 MutableSharedFlow를 사용하여 소켓 핸들링과 이벤트 처리를 담당하는 예시. handleSocket 함수는 coroutineScope 내에서 동작하며, eventFlow를 수집(collect)하여 각 이벤트를 소켓에 전송(sendSerialized).

```
// 클라이언트 네트워크 레이어: Flow 이벤트 관찰
fun observeEvents(): Flow<ChatEvent> = flow {
    while (true) {
        emit(socket.receiveDeserialized())
    }
}
```

위 코드는 Flow를 사용하여 클라이언트의 네트워크 레이어에서 이벤트를 관찰하는 예시.

observeEvents 함수는 무한 루프를 통해 계속해서 이벤트를 수신(receiveDeserialized)하고, emit 함수를 사용하여 이벤트를 Flow에 방출.

```
class ChatViewModel {
    private val _messageFlow = MutableStateFlow(listOf<Message>())
    val messageFlow: StateFlow<List<Message>> get() = _messageFlow

    fun connectToChat() {
        viewModelScope.launch {
            chatService.observeEvents().collect { event ->
                when (event) {
                    is MessageEvent -> {
                        _messageFlow.update { list ->
                            list + event.message
                        }
                    }
                    // 다른 이벤트 유형 처리
                }
            }
        }
    }
}
```

위 코드는 ChatViewModel 클래스에서 모든 이벤트의 상태 Flow를 구축하는 예시입니다.

connectToChat 함수는 chatService의 observeEvents를 수집(collect)하여 이벤트를 처리하고, MessageEvent인 경우 _messageFlow를 업데이트하여 새로운 메시지를 추가.

## 코틀린 2.x 에서의 Flow 문법 변화
<img width="509" alt="image-20230519-060018" src="https://github.com/H-Zoon/FlowExam/assets/43941511/6c9804c9-f5d8-4b07-b3f4-3285295537da">


기존에는 내부용 (변경 가능) 객체를 위한 private val 변수, 외부 노출용(변경 불가능) 객체를 위한 public 변수

코틀린 최신버전에서는 보일러플레이트를 제거하기 위해 간은 프로퍼티에 대해 public과 private 타입 제공

# Flow with compose (kotlinConf recap)

다음 그림은 Compose로 구현한 피자 주문 어플리케이션의 예제 코드.

하지만 다음 코드의 문제점과 그 이유는?
<img width="780" alt="image-20230519-060325" src="https://github.com/H-Zoon/FlowExam/assets/43941511/39ebe10d-146a-46f1-a374-6bbcfbbc51a9">


피자 가격이 변동되더라도 UI에 업데이트 안됨.

따라서 위 코드는 다음과 같이 수정되어야 함.

```
@Composable
fun PizzaList(pizzas: List<Pizza>) {
    // 렌더링 로직
}

@Composable
fun LivePizzaList(vm: PizzaViewModel) {
    val prices by vm.pizzas.values.collectAsState()
    PizzaList(prices)
}
```

위와 같이 수정되어야 하는 이유는?

Compose는 value의 변화를 자체적으로 파악하지 못함..

그렇기에 새로운 flow 값의 방출이 일어날 때마다, compose state 형태로 값의 변화를 업데이트를 해주어야 함.

## collectAsStateWithLifeCycle

collectAsState에 LifeCycle이 더해진 compose api. 이를 통해 생명 주기에 맞춰 flow 수집 가능.

collectAsState는 lifeCycle에 종속되지 않는다는 장점이 있습니다. 그렇기에 Android 앱이 백그라운드에 있는 동안 recomposition을 중지하더라도, 계속해서 작업을 진행할 수 있기에, 백그라운드 상태에서도 작업가능.

## 피자 그리기

다음은 컴포즈를 이용하여 피자를 회전시켜 그리는 예제

```
@Composable
fun RotatedPizzaSlice(phase: Int) {
    // 피자 조각 회전 로직
}

@Composable
fun PizzaLoader(numbers: Flow<Int>) {
    val phaseFlow = numbers.map { it % 8 }
    val phase by phaseFlow.collectAsState(initial = 0)
    RotatedPizzaSlice(phase)
}
```

코드 1에서는 numbers Flow를 사용하여 phaseFlow를 생성. 이때 numbers.map { it % 8 }를 통해 numbers Flow의 각 요소를 8로 나눈 나머지 값으로 변환하여 phaseFlow를 생성

```
@Composable
fun RotatedPizzaSlice(phase: Int) {
    // 피자 조각 회전 로직
}

@Composable
fun PizzaLoader(numbers: Flow<Int>) {
    val phaseFlow = remember(numbers) {
        numbers.map { it % 8 }
    }
    val phase by phaseFlow.collectAsState(initial = 0)
    RotatedPizzaSlice(phase)
}
```

코드 2에서는 remember 함수를 사용하여 numbers를 기억.위에서는 remember(numbers) { numbers.map { it % 8 } }를 통해 numbers Flow의 각 요소를 8로 나눈 나머지 값으로 변환한 phaseFlow를 생성하고, numbers Flow가 변경되지 않는 한  phaseFlow를 재사용

코드 1에서 피자 그림이 재대로 그려지지 않는 이유는 phaseFlow를 관찰하고 있는 collectAsState 함수를 사용하여 상태를 관리하지만, phaseFlow가 변경될 때마다 Compose의 재구성이 제대로 이루어지지 않음.

collectAsState 함수는 State 타입을 반환. 하지만 collectAsState 함수는 내부적으로 Flow를 수신하기 때문에 Flow의 각각의 값에 대해 상태 업데이트를 트리거하지 않음.

따라서 코드 1 에서는 phaseFlow가 변경되더라도 Compose는 이를 감지하지 못하고, 변경된 값을 바탕으로 피자 그림을 업데이트하지 않음.

반면에 코드 2에서는 remember 함수를 사용하여 numbers Flow를 관찰. 이는 Compose의 상태 보존 기능을 사용하므로 numbers Flow가 변경될 때마다 phaseFlow가 새로 생성되어 피자 그림이 올바르게 업데이트.
