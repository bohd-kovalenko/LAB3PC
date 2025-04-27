import java.time.Duration
import java.util.Random
import kotlin.concurrent.thread

fun main() {
    println("Running ThreadPool tests...")
    val taskFactory = TaskFactory()
    var sumDuration = Duration.ZERO
    val threadPool = DefaultThreadPool(3)
    val random = Random()
    val testDurationMillis = 60_000L

    val thread = thread {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < testDurationMillis) {
            val task = taskFactory.createTask()
            sumDuration += task.duration
            threadPool.execute(task)

            val delayMillis = random.nextInt(3000)
            Thread.sleep(delayMillis.toLong())
        }
    }

    thread.join()
    threadPool.shutdown()

    Runtime.getRuntime().addShutdownHook(Thread {
        val sumAverageTime = taskFactory.averageTimeToComplete.sum()
        if (taskFactory.averageTimeToComplete.isNotEmpty()) {
            println("Average time from creation to completion: ${Duration.ofMillis(sumAverageTime / taskFactory.averageTimeToComplete.size)}")
        } else {
            println("No tasks completed")
        }

        threadPool.queues().forEach {
            if (it.averageLoad.isNotEmpty()) {
                val sumTasksCount = it.averageLoad.sum()
                println("Queue ${it.id} average load: ${sumTasksCount / it.averageLoad.size}")
            } else {
                println("Queue ${it.id} had no load")
            }
        }
    })
}
