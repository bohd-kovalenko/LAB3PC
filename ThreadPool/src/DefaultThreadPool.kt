import java.util.*
import kotlin.concurrent.withLock

class DefaultThreadPool(queueCount: Int) : ThreadPool {

    private companion object {
        const val THREADS_PER_QUEUE = 2
    }

    private val queueMap: Map<WorkingQueue<Task>, List<WorkingThread>> = initQueues(queueCount)
    private var state = ThreadPoolState.ACTIVE

    @Synchronized
    override fun execute(task: Task) {
        if (state !== ThreadPoolState.SHUTDOWN) {
            val leastLoadedQueue = queues().minBy { it.sumLoad }
            leastLoadedQueue.pushTask(task)
            println("Task added to queue: ${leastLoadedQueue.id}, current queue load = ${leastLoadedQueue.sumLoad}")
            leastLoadedQueue.sumLoad += task.duration
        }
    }

    @Synchronized
    override fun shutdown(force: Boolean) {
        changeState(ThreadPoolState.SHUTDOWN)
        if (force) {
            workerThreads().forEach { thread ->
                {
                    runCatching { thread.interrupt() }
                }
            }
        }
    }

    @Synchronized
    override fun pause() {
        if (state === ThreadPoolState.ACTIVE) {
            changeState(ThreadPoolState.PAUSED)
        }
    }

    @Synchronized
    override fun resume() {
        if (state === ThreadPoolState.PAUSED) {
            changeState(ThreadPoolState.ACTIVE)
            queues().forEach { queue ->
                queue.lock.withLock {
                    queue.condition.signalAll()
                }
            }
        }
    }

    private fun initQueues(queueCount: Int) = (0..<queueCount)
        .map { queueIndex -> WorkingQueue<Task>(LinkedList()) }
        .associateWith { queue ->
            List(THREADS_PER_QUEUE) { threadIndex ->
                val threadName = "Queue-${queue.id}-Thread-$threadIndex"
                WorkingThread(queue, threadName).apply { start() }
            }
        }

    private fun changeState(newState: ThreadPoolState) {
        state = newState
        queues().forEach { queue ->
            queue.state = newState
        }
    }

    private fun workerThreads() = queueMap.values.flatten()

    fun queues() = queueMap.keys
}
