import java.time.Duration

fun main() {
    val threadPool = DefaultThreadPool(3)
    val taskFactory = TaskFactory()
    var sumDur = Duration.ZERO
    for (i in 1..100) {
        val task = taskFactory.createTask()
        sumDur += task.duration
        threadPool.execute(task)
        println("Pushed into a queue: ${task.duration}")
    }
    println(sumDur)
}
