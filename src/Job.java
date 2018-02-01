
/**
 * Holds information for a job process. The class must be instantiated with
 * proper information and cannot be generic. Information that's passed in
 * to the constructor cannot be changed.
 *
 */
public class Job {
	
// ---------- Field members
	
	private int pid, arrivalTime;
	private int cpuTimeRequired, cpuTimeRemaining, timeInSystem;
	private int currentQueue;
	private int waitingTime, waitTimeStart;
	private int visualID;
	
	
// ---------- Public methods
	
	/**
	 * Constructor for the Job class. The class requires the information (not optional).
	 * @param pid Job process ID
	 * @param arrivalTime Job's arrival time to some time counter
	 * @param cpuTimeRequired Job's time required to completion to some time counter
	 */
	public Job(int pid, int arrivalTime, int cpuTimeRequired) {
		this.pid = pid;
		this.arrivalTime = arrivalTime;
		this.cpuTimeRequired = cpuTimeRequired;
		
		cpuTimeRemaining = cpuTimeRequired;
		currentQueue = 0;
		timeInSystem = 0;
		waitingTime = 0;
		waitTimeStart = 0;
		visualID = 0;
	}
	
	/**
	 * Sets the visual ID of the job.
	 * @param visualID ID of the visual component
	 */
	public void setVID(int visualID) {
		this.visualID = visualID;
	}
	
	/**
	 * Gets the visual ID of the job.
	 * @return ID of the visual component
	 */
	public int getVID() {
		return visualID;
	}
	
	/**
	 * Gets the job's process ID.
	 * @return Job's process ID
	 */
	public int getPID() {
		return pid;
	}
	
	/**
	 * Returns the queue that the job is currently or was in.
	 * @return Queue of job
	 */
	public int getCurrentQueue() {
		return currentQueue;
	}
	
	/**
	 * Returns the time that the job arrives into the system.
	 * @return Arrival time
	 */
	public int getArrivalTime() {
		return arrivalTime;
	}
	
	/**
	 * Returns the amount of time the job has left to process with the CPU.
	 * @return CPU processing time left
	 */
	public int getTimeRemaining() {
		return cpuTimeRemaining;
	}
	
	/**
	 * Returns the amount of time the job requires to finish.
	 * @return Full time required to finish processing
	 */
	public int getTimeRequired() {
		return cpuTimeRequired;
	}
	
	/**
	 * Returns the amount of time the job spent in the system.
	 * @return Time spent in system
	 */
	public int getTimeInSystem() {
		return timeInSystem;
	}
	
	/**
	 * Returns the amount of time the job spent waiting in a queue.
	 * @return Total queue time
	 */
	public int getWaitingTime() {
		return waitingTime;
	}
	
	/**
	 * Sets the amount of time the job spent in the system.
	 * @param input Time spent in system
	 */
	public void setTimeInSystem(int input) {
		timeInSystem = input;
	}
	
	/**
	 * Clocks in the job to measure time spent waiting.
	 * @param timeIn System time when job starts to wait in queue
	 */
	public void clockInQueue(int timeIn) {
		waitTimeStart = timeIn;
	}
	
	/**
	 * Clocks out the job to measure time spent waiting.
	 * @param timeOut System time when job finishes waiting in a queue
	 */
	public void clockOutQueue(int timeOut) {
		waitingTime += timeOut - waitTimeStart;
	}
	
	/**
	 * Decrements the time the job has left towards finishing.
	 */
	public void decTimeRemaining() {
		cpuTimeRemaining--;
	}
	
	/**
	 * Increments the current queue of the job to the next level unless
	 * the maximum queue level has been reached, to which it will simply
	 * stay at that level.
	 */
	public void incCurrentQueue() {
		if (currentQueue < MFQ.MAX_QUEUE_LEVEL - 1)
			currentQueue++;
	}
}
