import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking<Unit>  {
    println("Hello Flow World!")

    launch {
        flowMarge()
        flowCombine()
        flowZip()
        flowReduce()
        flowFold()
        flowScan()
        flowBuffer()
        flowData()
        flowTake()
        flowDrop()
    }
}

suspend fun flowMarge() {
    println("------FlowMarge------")
    val string1: Flow<String> = flowOf("a", "b", "c").onEach {
        delay(100)
    }
    val string2: Flow<String> = flowOf("A", "B", "C").onEach {
        delay(50)
    }
    val together: Flow<String> = merge(string1, string2)
    println(together.toList())
}

suspend fun flowCombine() {
    println("-----FlowCombine-----")
    val flow1: Flow<Int> = flowOf(1, 2).onEach { delay(10) }
    val flow2: Flow<String> = flowOf("A", "B", "C").onEach { delay(15) }
    flow1.combine(flow2) { f1, f2 -> f1.toString() + f2 }.collect {
        println(it)
    }
}

suspend fun flowZip() {
    println("--------FlowZip-------")
    val flow1: Flow<Int> = flowOf(1, 2).onEach { delay(10) }
    val flow2: Flow<String> = flowOf("A", "B", "C").onEach { delay(15) }
    flow1.zip(flow2) { f1, f2 -> f1.toString() + f2 }.collect {
        println(it)
    }
}

suspend fun flowReduce() {
    println("------FlowReduce------")
    runBlocking {
        flowOf(1, 2, 3)
            .reduce { acc, value -> acc + value }
            .also { println("value $it") }
    }
}

suspend fun flowFold() {
    println("-------FlowFold-------")
    runBlocking {
        flowOf(1, 2, 3)
            .fold(10) { acc, value -> acc + value }
            .also { println("value $it") }
    }
}

suspend fun flowScan() {
    println("-------FlowScan-------")
    flowOf(1, 2, 3, 4)
        .scan(0) { acc, v -> acc + v }
        .collect { println(it) }
}

suspend fun flowBuffer() {
    println("------FlowBuffer------")
    flowOf("A", "B", "C")
        .onEach { delay(300) }
        .buffer(3)
        .collect { println(it) }
}

suspend fun flowData() {
    println("-------FlowData-------")
    val flowData = flowOf(1, 2, 3, 4, 5)
    flowData
        .map { number -> number }
        .filter { it % 2 == 0 }
        .collect { println(it) }
}

suspend fun flowTake() {
    println("-------FlowTake-------")
    flowOf(1, 2, 3, 4, 5, 6)
        .take(2)
        .collect { println(it) }
}

suspend fun flowDrop() {
    println("-------FlowDrop-------")
    flowOf(1, 2, 3, 4, 5, 6)
        .drop(2)
        .collect { println(it) }
}