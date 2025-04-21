import java.util.*

class DefaultThreadPool(queueCount: Int) : ThreadPool {

    private companion object {
        const val THREADS_PER_QUEUE = 2
    }

    private val queueMap: Map<WorkingQueue<Task>, List<WorkingThread>> = initQueues(queueCount)
    private var state = ThreadPoolState.ACTIVE

    @Synchronized
    override fun execute(task: Task) {
        val leastLoadedQueue = queues().minBy { it.sumLoad }
        leastLoadedQueue.pushTask(task)
        leastLoadedQueue.sumLoad += task.duration
    }

    @Synchronized
    override fun shutdown(force: Boolean) {
        state = ThreadPoolState.SHUTDOWN
        if (force) {
            workerThreads().forEach { it.interrupt() }
        }
    }

    @Synchronized
    override fun pause() {
        if (state === ThreadPoolState.ACTIVE) {
            state = ThreadPoolState.PAUSED
        }
    }

    @Synchronized
    override fun resume() {
        if (state === ThreadPoolState.PAUSED) {
            queues().forEach {
                it.condition.signalAll()
            }
        }
    }

    private fun initQueues(queueCount: Int) = (0..<queueCount)
        .map { WorkingQueue<Task>(LinkedList()) }
        .associateWith { queue -> List(THREADS_PER_QUEUE) { WorkingThread(queue).apply { start() } } }

    private fun workerThreads() = queueMap.values.flatten()

    private fun queues() = queueMap.keys
}