internal class WorkingThread(private val servingQueue: WorkingQueue<Task>) : Thread() {

    override fun run() {
        while (active() || servingQueue.queue.isNotEmpty()) {
            var task: Task? = null
            servingQueue.lock.lock()
            try {
                handleWait()
                task = getTask()
            } finally {
                servingQueue.lock.unlock()
                task?.execute?.invoke()
            }
        }
    }

    private fun handleWait() {
        while ((servingQueue.state == ThreadPoolState.PAUSED || servingQueue.queue.isEmpty()) && active()) {
            servingQueue.condition.await()
        }
    }

    private fun getTask(): Task? {
        val task = servingQueue.queue.poll()
        if (task != null) {
            servingQueue.sumLoad -= task.duration
        }
        return task
    }

    private fun active() = servingQueue.state !== ThreadPoolState.SHUTDOWN
}