import java.time.Duration
import java.time.Instant

internal class WorkingThread(private val servingQueue: WorkingQueue<Task>, name: String) : Thread(name) {
    private var tasksHandledCount = 0
    private val averageAwaitTimeMs: MutableList<Long> = mutableListOf()

    override fun run() {
        while (active() || servingQueue.queue.isNotEmpty()) {
            var task: Task? = null
            servingQueue.lock.lock()
            try {
                handleWait()
                task = getTask()
            } finally {
                servingQueue.lock.unlock()
                tasksHandledCount++
                task?.execute?.invoke()
            }
        }
        val sumAwaitTimeMs = averageAwaitTimeMs.sum()
        println("Thread $name handled $tasksHandledCount tasks, average await time: ${Duration.ofMillis(sumAwaitTimeMs / tasksHandledCount)}")
    }

    private fun handleWait() {
        try {
            while (active() && (servingQueue.state == ThreadPoolState.PAUSED || servingQueue.queue.isEmpty())) {
                val startTime = Instant.now()
                servingQueue.condition.await()
                averageAwaitTimeMs.add(Duration.between(startTime, Instant.now()).toMillis())
            }
        } catch (_: InterruptedException) {
            currentThread().interrupt()
            return
        }
    }

    private fun getTask(): Task? {
        val task = servingQueue.queue.poll()
        if (task != null) servingQueue.sumLoad -= task.duration
        if (servingQueue.sumLoad < Duration.ZERO) servingQueue.sumLoad = Duration.ZERO
        return task
    }

    private fun active() = servingQueue.state !== ThreadPoolState.SHUTDOWN
}
