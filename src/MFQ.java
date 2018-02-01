import java.awt.Dimension;
import java.io.*;
import java.util.Scanner;

import javax.swing.JFrame;

/**
 * Handles the entire multi-level feedback queue system. Contains
 * all the data for the simulation as well as controlling the flow.
 */
public class MFQ {
	
// ---------- Class fields
	
	private PrintWriter pw;
	private Scanner fileInput;
	private ObjectQueue jobQueue;
	private ObjectQueue[] queueLevel;
	private CPU intel;
	private JFrame mainWindow;
	private VisualRepresentor gui;
	
	private int clock;
	
	// Record keeping
	private int totalNumOfJobs, totalJobTime, CPUIdleTime;
	private double avgWaitTime, avgRespTime, throughput, taTime;
	
	// Queues go from 0 to 3 for a total of 4 queues
	public static final int MAX_QUEUE_LEVEL = 4;
	
	// Window properties
	private static final int WIN_W = 970;
	private static final int WIN_H = 600;
	
	// Default window initializers (see initWindow method)
	private Dimension defaultWinSize;
	private String title;
	

// ---------- Public methods
	
	// ---------- Non-GUI methods
	/**
	 * Constructor requires a Scanner and PrintWriter to get the data.
	 * @param fileInput File that is read and parsed for data
	 * @param pw File that is output as a result of program execution
	 * @throws IOException Missing required files to run
	 * @throws InterruptedException Thread sleep interruption
	 */
	public MFQ(Scanner fileInput, PrintWriter pw) throws IOException,
												InterruptedException {
		this.pw = pw;
		this.fileInput = fileInput;
		jobQueue = new ObjectQueue();
		intel = new CPU();
		
		queueLevel = new ObjectQueue[MAX_QUEUE_LEVEL];
		queueLevel[0] = new ObjectQueue();
		queueLevel[1] = new ObjectQueue();
		queueLevel[2] = new ObjectQueue();
		queueLevel[3] = new ObjectQueue();
		
		clock = 0;
		totalNumOfJobs = 0;
		totalJobTime = 0;
		avgRespTime = 0;
		taTime = 0;
		avgWaitTime = 0;
		throughput = 0;
		CPUIdleTime = 0;
		
		try {
			// Creates window for animation
			initWindow();
			runSimulationGUI();
			
		} catch (IOException e) {
			System.out.println(
					"ERROR: One or more images could not be found.");
			System.out.println("Program will run by console only " +
					"and will resume in 5 seconds.\n");
			Thread.sleep(5000);
			// Run by console only (no GUI)
			runSimulation();
		}
	}
	
	/**
	 * Gets the jobs from the given input file and stores them as objects
	 * in a queue.
	 */
	public void getJobs() {
		String delimiter = "[ ]+";
		
		while (fileInput.hasNextLine()) {
			String temp = fileInput.nextLine();
			String[] tokens = temp.split(delimiter);
			
			int tempPID, tempArrivalTime, tempTimeReq;
			tempArrivalTime = Integer.parseInt(tokens[0]);
			tempPID = Integer.parseInt(tokens[1]);
			tempTimeReq = Integer.parseInt(tokens[2]);
			
			jobQueue.insert(new Job(tempPID, tempArrivalTime, tempTimeReq));
		}	
	}
	
	/**
	 * Outputs the header string to the console output and file.
	 */
	public void outputHeader() {
		System.out.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"Event", "Sys Time", "PID", "CPU Time Needed",
				"Total Time in Sys", "LLQ");
		System.out.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"-----", "--------", "---", "---------------",
				"-----------------", "---");
		
		pw.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"Event", "Sys Time", "PID", "CPU Time Needed",
				"Total Time in Sys", "LLQ");
		pw.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"-----", "--------", "---", "---------------",
				"-----------------", "---");
	}
	
	/**
	 * Runs the simulation by console.
	 */
	public void runSimulation() { // Simulation w/o GUI
		getJobs();
		outputHeader();
		while (!jobQueue.isEmpty() || !queueLevel[0].isEmpty() ||
				   !queueLevel[1].isEmpty() || !queueLevel[2].isEmpty() ||
				   !queueLevel[3].isEmpty() || intel.isBusy()) {
			tickSimulation();
		}
		totalRecords();
		outStats();
	}
	
	/**
	 * Reports all the statistics for the program to the console and file.
	 */
	public void outStats() {
		System.out.printf("\nTotal number of jobs -- %d\n", totalNumOfJobs);
		System.out.printf("Total time of all jobs -- %d\n", totalJobTime);
		System.out.printf("Average response time -- %.2f\n", avgRespTime);
		System.out.printf("Average turnaround time -- %.2f\n", taTime);
		System.out.printf("Average waiting time -- %.2f\n", avgWaitTime);
		System.out.printf("Average throughput -- %.2f\n", throughput);
		System.out.printf("Total CPU idle time -- %d\n", CPUIdleTime);
		
		pw.printf("\nTotal number of jobs -- %d\n", totalNumOfJobs);
		pw.printf("Total time of all jobs in system -- %d\n", totalJobTime);
		pw.printf("Average response time -- %.2f\n", avgRespTime);
		pw.printf("Average turnaround time -- %.2f\n", taTime);
		pw.printf("Average waiting time -- %.2f\n", avgWaitTime);
		pw.printf("Average throughput for system -- %.2f\n", throughput);
		pw.printf("Total CPU idle time -- %d\n", CPUIdleTime);
		
		System.out.println("\n----------------------------");
		System.out.println("Check \"csis.txt\" for output.");
	}
	
	// ---------- GUI-based methods
	/**
	 * Runs the simulation by GUI.
	 * @throws InterruptedException Thread sleep interruption
	 */
	public void runSimulationGUI() throws InterruptedException {
		getJobs();
		outputHeader();
		while (!jobQueue.isEmpty() || !queueLevel[0].isEmpty() ||
				   !queueLevel[1].isEmpty() || !queueLevel[2].isEmpty() ||
				   !queueLevel[3].isEmpty() || intel.isBusy()) {
			while(gui.inWaitingState()) Thread.sleep(1);
			// Loop until gui is unpaused
			// Sleep is called so CPU is not running in paused state
			tickSimulationGUI();
			gui.animateGUI();
			Thread.sleep(500 / gui.getSpeedMult());
		}
		totalRecords();
		outStats();
		gui.setFinished();
		gui.animateGUI(); // Final run over for final messages
	}
	
	
// ---------- Private methods
	
	// ---------- Non-GUI methods
	/**
	 * Reports to the console and file when a job arrives with respect to
	 * the current MFQ system clock.
	 * @param job Job that arrived into the system
	 */
	private void outArrivalData(Job job) {
		String sysTime, pid, timeNeeded;
		sysTime = String.valueOf(clock);
		pid = String.valueOf(job.getPID());
		timeNeeded = String.valueOf(job.getTimeRequired());
		
		System.out.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"ARRIVAL", sysTime, pid, timeNeeded, "-", "-");
		pw.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"ARRIVAL", sysTime, pid, timeNeeded, "-", "-");
	}
	
	/**
	 * Reports to the console and file when a job departs with respect to
	 * the current MFQ system clock.
	 * @param job Job that left the system
	 */
	private void recordOutData(Job job) {
		int timeInSys;
		String sysTime, pid, timeInSysStr, llq;
		
		sysTime = String.valueOf(clock);
		pid = String.valueOf(job.getPID());
		timeInSys = job.getTimeInSystem() - job.getArrivalTime();
		timeInSysStr = String.valueOf(timeInSys);
		llq = String.valueOf(job.getCurrentQueue() + 1);
		
		totalJobTime += timeInSys;
		avgWaitTime += job.getWaitingTime();
		totalNumOfJobs++;
		
		System.out.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"DEPARTURE", sysTime, pid, "-", timeInSysStr, llq);
		pw.printf("%9s   %8s   %3s   %15s   %17s   %3s\n",
				"DEPARTURE", sysTime, pid, "-", timeInSysStr, llq);
	}
	
	/**
	 * Handles the main algorithm of the program as a single tick. This is
	 * the non-GUI version of the method.
	 */
	private void tickSimulation() {
		clock++;
		if (!jobQueue.isEmpty()) {
			if (((Job)jobQueue.query()).getArrivalTime() == clock) {
				Job nextJob = (Job)jobQueue.remove();
				queueLevel[nextJob.getCurrentQueue()].insert(nextJob);
				outArrivalData(nextJob);
			}
		}
		if (intel.isBusy()) {
			intel.decClocks();
			if (intel.jobFinished()) {
				Job temp = intel.clearCPU(clock);
				recordOutData(temp);
				submitQueuedJob();
			}
			if (intel.quantumClockDone() || !queueLevel[0].isEmpty()) {
				Job temp = intel.preemptCPU();
				temp.clockInQueue(clock);
				queueLevel[temp.getCurrentQueue()].insert(temp);
				submitQueuedJob();
			}
		} else {
			CPUIdleTime += 1;
			submitQueuedJob();
		}
		if (!queueLevel[0].isEmpty()) avgRespTime++;
	}
	
	/**
	 * Handles submitting a queued job to the MFQ's CPU. This is the non-GUI
	 * version of the method.
	 */
	private void submitQueuedJob() {
		if (!queueLevel[0].isEmpty()) {
			Job temp = (Job)queueLevel[0].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
		
		} else if (!queueLevel[1].isEmpty() && !intel.isBusy()) {
			Job temp = (Job)queueLevel[1].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
		
		} else if (!queueLevel[2].isEmpty() && !intel.isBusy()) {
			Job temp = (Job)queueLevel[2].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
		
		} else if (!queueLevel[3].isEmpty() && !intel.isBusy()) {
			Job temp = (Job)queueLevel[3].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
		}
	}
	
	/**
	 * Totals out records and gets the data ready for the final output
	 * string.
	 */
	private void totalRecords() {
		avgRespTime /= totalNumOfJobs;
		avgWaitTime /= totalNumOfJobs;
		taTime = (double)totalJobTime / (double)totalNumOfJobs;
		throughput = (double)totalNumOfJobs / (double)totalJobTime;
	}
	
	// ---------- GUI-based methods
	/**
	 * Handles creating the window and its default starting values.
	 * @throws IOException Missing required files
	 */
	private void initWindow() throws IOException {
		mainWindow = new JFrame();
		gui = new VisualRepresentor();
		
		defaultWinSize = new Dimension(WIN_W, WIN_H);
		title = "MFQ Lab - Visual Output";
		
		mainWindow.add(gui);
		mainWindow.setTitle(title);
		mainWindow.setSize(defaultWinSize);
		mainWindow.setResizable(false);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setVisible(true);
	}
	
	/**
	 * Handles the main algorithm of the program as a single tick. This is
	 * the GUI version of the method.
	 * @throws InterruptedException Thread sleep interruption
	 */
	private void tickSimulationGUI() throws InterruptedException {
		clock++;
		gui.incSystime();
		gui.animateGUI();
		if (!jobQueue.isEmpty()) {
			if (((Job)jobQueue.query()).getArrivalTime() == clock) {
				Job nextJob = (Job)jobQueue.remove();
				gui.addJobVisual(nextJob.getPID(),nextJob.getTimeRemaining());
				nextJob.setVID(gui.getVID());
				queueLevel[nextJob.getCurrentQueue()].insert(nextJob);
				outArrivalData(nextJob);
				gui.animateGUI();
			}
		}
		if (intel.isBusy()) {
			intel.decClocks();
			gui.decJobVisual(intel.peekCPU().getVID());
			gui.decQuantumTime();
			gui.animateGUI();
			if (intel.jobFinished()) {
				Job temp = intel.clearCPU(clock);
				recordOutData(temp);
				gui.finJobVisual(temp.getVID());
				Thread.sleep(500/gui.getSpeedMult());
				gui.animateGUI();
				submitQueuedJobGUI();
			}
			if (intel.quantumClockDone() || !queueLevel[0].isEmpty()) {
				Job temp = intel.preemptCPU();
				temp.clockInQueue(clock);
				queueLevel[temp.getCurrentQueue()].insert(temp);
				gui.setToQueueJobVisual(temp.getVID());
				Thread.sleep(500/gui.getSpeedMult());
				gui.animateGUI();
				submitQueuedJobGUI();
			}
			
		} else {
			CPUIdleTime += 1;
			submitQueuedJobGUI();
		}
		if (!queueLevel[0].isEmpty()) avgRespTime++;
	}
	
	/**
	 * Handles submitting a queued job to the MFQ's CPU. This is the GUI
	 * version of the method.
	 */
	private void submitQueuedJobGUI() throws InterruptedException {
		if (!queueLevel[0].isEmpty()) {
			Job temp = (Job)queueLevel[0].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
			gui.setToCPUJobVisual(temp.getVID());
			gui.decQueueDepth(1);
		
		} else if (!queueLevel[1].isEmpty() && !intel.isBusy()) {
			Job temp = (Job)queueLevel[1].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
			gui.setToCPUJobVisual(temp.getVID());
			gui.decQueueDepth(2);
		
		} else if (!queueLevel[2].isEmpty() && !intel.isBusy()) {
			Job temp = (Job)queueLevel[2].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
			gui.setToCPUJobVisual(temp.getVID());
			gui.decQueueDepth(3);
		
		} else if (!queueLevel[3].isEmpty() && !intel.isBusy()) {
			Job temp = (Job)queueLevel[3].remove();
			temp.clockOutQueue(clock);
			intel.submitJob(temp);
			gui.setToCPUJobVisual(temp.getVID());
			gui.decQueueDepth(4);
		}
		gui.animateGUI();
	}
}
