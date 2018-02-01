
/**
 * Abstracts a CPU for the use of the MFQ simulation.
 */
public class CPU {

// ---------- Field members
	
	private Job currentJob;
	private int quantumClock;
	private boolean isBusy;

	
// ---------- Public methods
	
	/**
	 * Initializes needed variables for the class.
	 */
	public CPU() {
		quantumClock = 0;
		isBusy = false;
		currentJob = null;
	}
	
	/**
	 * Returns whether or not the CPU is currently processing a job.
	 * @return CPU is busy or not
	 */
	public boolean isBusy() {
		return isBusy;
	}
	
	/**
	 * Returns whether or not the job that is in the CPU is finished
	 * (remaining time of the job is 0).
	 * @return Job is finished or not
	 */
	public boolean jobFinished() {
		if (currentJob != null)
			return currentJob.getTimeRemaining() == 0;
		else
			return false;
	}
	
	/**
	 * Returns whether or not the CPU quantum clock is at 0.
	 * @return CPU quantum clock is finished or not
	 */
	public boolean quantumClockDone() {
		if (currentJob != null)
			return quantumClock == 0;
		else
			return false;
	}
	
	/**
	 * Returns the job that is currently on the CPU.
	 * @return Current job or null if none
	 */
	public Job peekCPU() {
		return currentJob;
	}
	
	/**
	 * Forces the job out of the CPU and returns the job object. Also
	 * increments the job's queue level. Resets busy flag.
	 * @return The job that was preempted
	 */
	public Job preemptCPU() {
		Job temp = currentJob;
		temp.incCurrentQueue();
		currentJob = null;
		isBusy = false;
		quantumClock = 0;
		return temp;
	}
	
	/**
	 * Clears the CPU of the job that resides. This is different from
	 * preemptCPU() in that it will clock the job out of the system and
	 * does not increment the job's queue level. Resets busy flag.
	 * @param clockTime
	 * @return The job that has finished
	 */
	public Job clearCPU(int clockTime) {
		Job temp = currentJob;
		currentJob = null;
		isBusy = false;
		quantumClock = 0;
		temp.setTimeInSystem(clockTime);
		return temp;
	}
	
	/**
	 * Handles decrementing the CPU quantum and the time remaining of the
	 * job that resides in the CPU.
	 */
	public void decClocks() {
		quantumClock--;
		currentJob.decTimeRemaining();
	}
	
	/**
	 * Adds a job into the CPU to be processed. Sets the CPU quantum to the
	 * appropriate amount of time according to the queue the job was in.
	 * Also sets the busy flag of the CPU.
	 * @param job Job to be processed in the CPU
	 */
	public void submitJob(Job job) {
		currentJob = job;
		quantumClock = (int)Math.pow(2,currentJob.getCurrentQueue() + 1);
		isBusy = true;
	}
}
