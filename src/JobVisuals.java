import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Handles all of the data regarding GUI job elements. Objects of this
 * class handles animations of itself and must be updated to animate.
 */
public class JobVisuals{
	
// ---------- Field members
	
	private static final int SPEED = 3;
	private Dimension position, newPosition;
	private BufferedImage jobImage, activeImage;
	private String jobStatus;
	private int pid, remainingTime, placement, qLevel, qDepth, speedTime;
	private boolean atNewPos;
	
	// States of animation
	private static final int START 	= 0;
	private static final int Q1 	= 1;
	private static final int Q2 	= 2;
	private static final int Q3 	= 3;
	private static final int Q4 	= 4;
	private static final int CPU 	= 5;
	private static final int DONE	= 10;
	
	private static int q1Count = 0;
	private static int q2Count = 0;
	private static int q3Count = 0;
	private static int q4Count = 0;
	
	
// ---------- Public methods
	
	/**
	 * Constructor requires data that is relevant to the Job class. This is
	 * to set up the animation that relates this to its respective job.
	 * @param pid Job's process ID
	 * @param cpuTimeRequired Time the job requires to finish
	 * @param spdMultiplier The speed of the animation
	 */
	public JobVisuals(int pid, int cpuTimeRequired, int spdMultiplier) {
		position = new Dimension(175,900);
		newPosition = new Dimension(0,0);
		
		this.pid = pid;
		remainingTime = cpuTimeRequired;
		jobStatus = "";
		speedTime = SPEED * spdMultiplier;
		placement = START;
		qLevel = 1;
		qDepth = 0;
		atNewPos = false;
	}
	
	/**
	 * Handles the positioning of the GUI job element. It only moves the
	 * GUI job element closer to its destination per call, so this method
	 * must be repeatedly called to arrive to its proper position.
	 * @return If the GUI job element is at its final position
	 */
	public boolean updateJobVisual() {
		boolean isNotFinished;
		pathSet();
		moveToNewPos();
		isNotFinished = !atNewPos;
		//if (!isNotFinished) 
		setStatus();
		return isNotFinished;
	}
	
	/**
	 * Sets the animation speed multiplier.
	 * @param multiplier Integer multiplier
	 */
	public void setSpeedMultiplier(int multiplier) {
		speedTime = SPEED * multiplier;
	}
	
	/**
	 * Sets the image of the GUI job element for the animatino.
	 * @param jobImage Image that the GUI job element should be
	 */
	public void setImage(BufferedImage jobImage) {
		this.jobImage = jobImage;
		activeImage = jobImage;
	}
	
	/**
	 * Sets the path of animation to the position where the job goes when
	 * it is finished being processed by the CPU.
	 */
	public void setFinished() {
		placement = DONE;
	}
	
	/**
	 * Sets the new position of the GUI job element to where it needs to
	 * animate to. The GUI job element does not move there immediately.
	 * @param x X position of the GUI job element
	 * @param y Y position of the GUI job element
	 */
	public void setNewPosition(int x, int y) {
		newPosition.width = x;
		newPosition.height = y;
	}
	
	/**
	 * Sets the animation path of the GUI job element to the position of
	 * the GUI CPU.
	 */
	public void moveToCPU() {
		placement = CPU;
		qDepth = 0;
		
		switch (qLevel) {
			case 1:  q1Count--; break;
			case 2:  q2Count--; break;
			case 3:  q3Count--; break;
			case 4:  q4Count--; break;
			default: q4Count--;
		}
	}
	
	/**
	 * Sets the animation path of the GUI job element to the position of
	 * the GUI queue of appropriate level.
	 */
	public void moveToQueue() {
		if (qLevel < 4) qLevel++;
		else qLevel = 4;
		
		switch (qLevel) {
			case 1:
				placement = Q1;
				q1Count++;
				qDepth = q1Count;
				break;
			case 2:
				placement = Q2;
				q2Count++;
				qDepth = q2Count;
				break;
			case 3:
				placement = Q3;
				q3Count++;
				qDepth = q3Count;
				break;
			case 4:
				placement = Q4;
				q4Count++;
				qDepth = q4Count;
				break;
			default:
				placement = Q4;
				q4Count++;
				qDepth = q4Count;
		}
	}
	
	/**
	 * Decrements the time of the GUI job element.
	 */
	public void decTime() {
		remainingTime--;
	}
	
	/**
	 * Decrements the depth of the GUI job element within a queue. This
	 * causes the element to shift up in the animation.
	 */
	public void decQueueDepth() {
		qDepth--;
	}
	
	/**
	 * Returns the process ID of this GUI job element.
	 * @return PID
	 */
	public int getPID() {
		return pid;
	}
	
	/**
	 * Gets the remaining time left of the GUI job element.
	 * @return Remaining time integer
	 */
	public int getTimeLeft() {
		return remainingTime;
	}
	
	/**
	 * Returns the current queue that the GUI job element sits in.
	 * @return The queue level of the GUI job element
	 */
	public int getQueueLevel() {
		return qLevel;
	}
	/**
	 * Returns where the new position of the GUI job element is.
	 * @return New position of the GUI job element
	 */
	public Dimension getNewPostion() {
		return newPosition;
	}
	
	/**
	 * Returns the current position of the GUI job element.
	 * @return Position of the GUI job element
	 */
	public Dimension getPosition() {
		return position;
	}
	

// ---------- Private methods
	
	/**
	 * Sets the current position of the GUI job element to the given x and y.
	 * @param x X position
	 * @param y Y position
	 */
	private void setPosition(int x, int y) {
		position.width = x;
		position.height = y;
	}
	
	/**
	 * Moves the GUI job element some determined amount of distance towards
	 * the new position. Must be called multiple times to reach new
	 * position.
	 */
	private void moveToNewPos() {
		double disX, disY, mult;
		int movX, movY;
		
		mult = 0.10;
		disX = (newPosition.width - position.width);
		disY = (newPosition.height - position.height);
		movX = 0;
		movY = 0;
		
		if (disX == 0 && disY == 0) {
			atNewPos = true;
			return;
		} else {
			atNewPos = false;
		}
		
		if (Math.abs(disX) > speedTime)
			movX = (int)((disX * mult) + (Math.abs(disX)/disX * 3));
		else
			position.width = newPosition.width;
		
		if (Math.abs(disY) > speedTime)
			movY = (int)((disY * mult) + (Math.abs(disY)/disY * 3));
		else
			position.height = newPosition.height;
		
		setPosition(position.width + movX, position.height + movY);
	}
	
	/**
	 * Sets the status of the GUI job element to print out.
	 */
	private void setStatus() {
		switch (placement) {
			case START:
				jobStatus = "Starting...";
				break;
			case CPU:
				jobStatus = "Busy...";
				 break;
			case Q1: case Q2: case Q3: case Q4:
				jobStatus = "Waiting...";
				 break;
			case DONE:
				jobStatus = "Finished";
				break;
			default:
				jobStatus = "Unknown state";
		}
	}
	
	/**
	 * Sets the positions of the GUI job element. These coordinates are
	 * hardcoded into the system and must change when the GUI faces
	 * new changes.
	 */
	private void pathSet() {
		switch (placement) {
			case START:
				newPosition = new Dimension(175,
						220 + (qDepth * jobImage.getHeight()));
				break;
			case Q1:
				newPosition = new Dimension(175,
						220 + (qDepth * jobImage.getHeight()));
				break;
			case Q2:
				newPosition = new Dimension(375,
						140 + (qDepth * jobImage.getHeight()));
				break;
			case Q3:
				newPosition = new Dimension(575,
						140 + (qDepth * jobImage.getHeight())); 
				break;
			case Q4:
				newPosition = new Dimension(775,
						140 + (qDepth * jobImage.getHeight()));
				break;
			case CPU:
				newPosition = new Dimension(410,40);
				break;
			case DONE:
				newPosition = new Dimension(410,-100);
				break;
			default:
				newPosition = new Dimension(0,0);
		}
	}

// ---------- Protected methods

	/**
	 * Handles drawing the text for the GUI job element. The text outputs
	 * data regarding the GUI job element.
	 * @param g Buffer that all the drawing happens on
	 */
	protected void drawText(Graphics g) {
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.drawString("Job " + pid,
				position.width + 10, position.height + 15);
		g.drawString("Time left: " + remainingTime,
				position.width + 10, position.height + 35);
		g.drawString("Job status:",
				position.width + 10, position.height + 55);
		g.drawString(jobStatus,
				position.width + 10, position.height + 70);
	}
	
	/**
	 * Draws the given image of the GUI job element.
	 * @param g Buffer that all the drawing happens on
	 */
	protected void drawImage(Graphics g) {
		g.drawImage(activeImage, position.width, position.height, null);
	}
}
