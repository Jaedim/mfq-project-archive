import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

/**
 * A class that was hacked away at to get the MFQ class visualized. It is
 * designed around, hard-coded, and fully dependent on the MFQ class.
 */
public class VisualRepresentor extends JPanel {
	
// ---------- Field members
	
	private static final long serialVersionUID = 1L;
	private static final int TIMER_DELAY = 16; // ~60fps
	private int visualID;
	
	// Required images
	private BufferedImage imgBG, imgCPU;
	private BufferedImage imgStart1, imgStart2, imgStart3;
	private BufferedImage imgSpd11, imgSpd12, imgSpd13;
	private BufferedImage imgSpd21, imgSpd22, imgSpd23;
	private BufferedImage imgSpd31, imgSpd32, imgSpd33;
	private BufferedImage imgSpd41, imgSpd42, imgSpd43;
	private BufferedImage imgSpd51, imgSpd52, imgSpd53;
	private BufferedImage imgJob, imgJobSpace;
	
	// Button information
	private JButton btnStart, btnSpd1, btnSpd2, btnSpd3, btnSpd4, btnSpd5;
	
	// Animation fields
	private ArrayList<JobVisuals> jobList;
	private List<StaticVisuals> elements;
	private List<JobVisuals> jobs;
	private JTextArea output, systemStat;
	private JScrollPane outScroller;
	private String sysStat;
	private boolean waitState, isAnimating;
	private int spdMultiplier, sysTime, cpuQuantum;
	
	
// ---------- Public methods
	
	/**
	 * Handles initiating the GUI data.
	 * @throws IOException Missing required files
	 */
	public VisualRepresentor() throws IOException {
		waitState = true;
		visualID = -1;
		spdMultiplier = 1;
		sysTime = 0;
		cpuQuantum = 0;
		sysStat = "Waiting to start";
		
		initWindow();
		loadImages();
		createBackground();
		createButtons();
	}
	
	/**
	 * Adds a job element to the GUI with its respective data.
	 * @param pid Job's process ID
	 * @param cpuTimeRequired Job's CPU time required
	 */
	public void addJobVisual(int pid, int cpuTimeRequired) {
		JobVisuals temp = new JobVisuals(pid,
										 cpuTimeRequired,
										 spdMultiplier);
		temp.setImage(imgJob);
		jobList.add(temp);
		addJobObject(temp);
		output.append("\nARRIVAL:\t\t" + pid + " - @ - " + sysTime);
		visualID++;
	}
	
	/**
	 * Returns the index of the most recently added GUI job element.
	 * @return <code>visualID</code>
	 */
	public int getVID() {
		return visualID;
	}
	
	/**
	 * Returns the speed multiplier of the GUI. This changes the speed of the
	 * program overall.
	 * @return <code>spdMultiplier</code>
	 */
	public int getSpeedMult() {
		return spdMultiplier;
	}
	
	/**
	 * Returns whether the program is in paused state or not.
	 * @return <code>waitState</code>
	 */
	public boolean inWaitingState() {
		return waitState;
	}
	
	/**
	 * Handles animating the GUI. Moves all GUI job elements to their proper
	 * places. MFQ calculating will pause until the animation is finished.
	 * @throws InterruptedException Thread sleep interruption
	 */
	public void animateGUI() throws InterruptedException {
		boolean isAnimatingState;
		do {
			isAnimatingState = false;
			for (JobVisuals visuals : jobList) {
				isAnimating = visuals.updateJobVisual();
				this.repaint();
				if (isAnimating) isAnimatingState = true;
				while(waitState) Thread.sleep(1);
				// Loop until pause is off
				// Sleep is so CPU isn't running constantly
			}
			setStatState();
			this.repaint();
			Thread.sleep(TIMER_DELAY);
		} while(isAnimatingState);
	}
	
	/**
	 * Decrements the remaining time of the GUI job with the given index
	 * parameter.
	 * @param n Index of the job to be decremented
	 */
	public void decJobVisual(int n) {
		JobVisuals temp = jobs.get(n);
		temp.decTime();
	}
	
	/**
	 * Decrements the GUI CPU quantum.
	 */
	public void decQuantumTime() {
		cpuQuantum--;
	}
	
	/**
	 * Shifts all GUI job elements up in the given GUI queue index.
	 * @param queueLevel Index of the queue to be shifted
	 */
	public void decQueueDepth(int queueLevel) {
		for (JobVisuals visuals : jobList) {
			if (visuals.getQueueLevel() == queueLevel)
				visuals.decQueueDepth();
		}
	}
	
	/**
	 * Sets the given GUI job element to the next GUI queue.
	 * @param n Index of the queue to be moved
	 * @throws InterruptedException Thread sleep interruption
	 */
	public void setToQueueJobVisual(int n) throws InterruptedException {
		JobVisuals temp = jobs.get(n);
		sysStat = "CPU Preemption";
		setStatState();
		temp.moveToQueue();
	}
	
	/**
	 * Sets the GUI output messages to report that the simulation is
	 * completed.
	 */
	public void setFinished() {
		output.append("\n----------------------------------------");
		output.append("\nFinished! Check \"csis.txt\" for output.");
		sysStat = "Simulation Complete";
	}
	
	/**
	 * Sets the given GUI job element to the GUI CPU.
	 * @param n Index of the job to be moved to the GUI CPU
	 */
	public void setToCPUJobVisual(int n) {
		JobVisuals temp = jobs.get(n);
		temp.moveToCPU();
		sysStat = "Working";
		setStatState();
		cpuQuantum = (int)Math.pow(2, temp.getQueueLevel());
	}
	
	/**
	 * Sets the given GUI job element to its finished state so it will clear
	 * out of the animation view.
	 * @param n Index of the GUI job element to be removed
	 */
	public void finJobVisual(int n) {
		JobVisuals temp = jobs.get(n);
		temp.setFinished();
		output.append("\nDEPARTURE:\t\t"+temp.getPID()+" - @ - "+sysTime);
		sysStat = "Job Finished";
		setStatState();
		repaint();
		cpuQuantum = 0;
	}
	
	/**
	 * Increments the GUI system time.
	 */
	public void incSystime() {
		sysTime++;	
	}
	
	
// ---------- Private methods
	
	/**
	 * Adds a new custom visual element to be drawn.
	 * @param in Visual element to be drawn
	 */
	private void addVisualObject(StaticVisuals in) {
		elements.add(in);
	}
	
	/**
	 * Adds a new GUI job element to the animation.
	 * @param in GUI job element to be drawn
	 */
	private void addJobObject(JobVisuals in) {
		jobs.add(in);
	}
	
	/**
	 * Sets the status of the system and outputs it to the output view in
	 * the GUI.
	 */
	private void setStatState() {
		systemStat.setText("System Time:\t         " + sysTime +
				"\n\nSimulation speed:   x" + spdMultiplier +
				"\n\n---------------------------------------------" +
				"\n------------System status-------------" +
				"\n\n               " + sysStat);
	}
	
	/**
	 * Handles the initialization of the GUI window.
	 */
	private void initWindow() {
		this.setLayout(null);
		jobList = new ArrayList<JobVisuals>();
		elements = new ArrayList<StaticVisuals>();
		jobs = new ArrayList<JobVisuals>();
		output = new JTextArea();
		systemStat = new JTextArea();
		outScroller = new JScrollPane();
		
		outScroller.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outScroller.setBounds(580,21,350,140);
		
		output.setBackground(Color.black);
		output.setForeground(Color.green);
		output.setText("Welcome to the" +
				"Multi-Level Feedback Queue Simulation!");
		output.append("\nAll output will be directed here.");
		output.append("\n------------------");
		output.append("\nEVENT:\t\tID    - @ - System time");
		output.append("\n------------------");
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		output.setVisible(true);
		output.setEditable(false);
		output.setCaretPosition(output.getDocument().getLength());
		
		DefaultCaret caret = (DefaultCaret) output.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
		
		systemStat.setBounds(180,20,170,140);
		systemStat.setForeground(Color.green);
		systemStat.setBackground(Color.black);
		
		setStatState();
		
		outScroller.setViewportView(output);
		this.add(outScroller);
		this.add(systemStat);
	}
	
	/**
	 * Handles loading images into the GUI system for drawing.
	 * @throws IOException Missing images
	 */
	private void loadImages() throws IOException {
		imgBG = ImageIO.read(new File("images/background.png"));
		imgStart1 = ImageIO.read(new File("images/start1.png"));
		imgStart2 = ImageIO.read(new File("images/start2.png"));
		imgStart3 = ImageIO.read(new File("images/start3.png"));
		imgSpd11 = ImageIO.read(new File("images/speed11.png"));
		imgSpd12 = ImageIO.read(new File("images/speed12.png"));
		imgSpd13 = ImageIO.read(new File("images/speed13.png"));
		imgSpd21 = ImageIO.read(new File("images/speed21.png"));
		imgSpd22 = ImageIO.read(new File("images/speed22.png"));
		imgSpd23 = ImageIO.read(new File("images/speed23.png"));
		imgSpd31 = ImageIO.read(new File("images/speed31.png"));
		imgSpd32 = ImageIO.read(new File("images/speed32.png"));
		imgSpd33 = ImageIO.read(new File("images/speed33.png"));
		imgSpd41 = ImageIO.read(new File("images/speed41.png"));
		imgSpd42 = ImageIO.read(new File("images/speed42.png"));
		imgSpd43 = ImageIO.read(new File("images/speed43.png"));
		imgSpd51 = ImageIO.read(new File("images/speed51.png"));
		imgSpd52 = ImageIO.read(new File("images/speed52.png"));
		imgSpd53 = ImageIO.read(new File("images/speed53.png"));
		imgJob = ImageIO.read(new File("images/job.png"));
		imgJobSpace = ImageIO.read(new File("images/jobspace.png"));
		imgCPU = ImageIO.read(new File("images/cpu.png"));
	}
	
	/**
	 * Creates custom visual background objects for the GUI.
	 */
	private void createBackground() {
		StaticVisuals bgTile1 = new StaticVisuals();
		StaticVisuals bgTile2 = new StaticVisuals();
		StaticVisuals bgTile3 = new StaticVisuals();
		StaticVisuals bgTile4 = new StaticVisuals();
		
		bgTile1.setImage(imgBG);
		bgTile2.setImage(imgBG);
		bgTile3.setImage(imgBG);
		bgTile4.setImage(imgBG);
		
		bgTile2.setPosition(800, 0);
		bgTile3.setPosition(0, 600);
		bgTile4.setPosition(800, 600);
		
		this.addVisualObject(bgTile1);
		this.addVisualObject(bgTile2);
		this.addVisualObject(bgTile3);
		this.addVisualObject(bgTile4);
	}
	
	/**
	 * Creates the buttons for the GUI to draw as well as their functions.
	 */
	private void createButtons() {
		btnStart = new JButton();
		btnSpd1 = new JButton();
		btnSpd2 = new JButton();
		btnSpd3 = new JButton();
		btnSpd4 = new JButton();
		btnSpd5 = new JButton();
		
		Rectangle btnStartPos = new Rectangle(20, 20,
							imgStart1.getWidth(), imgStart1.getHeight());
		Rectangle btnSpd1Pos = new Rectangle(20, 80,
							imgSpd11.getWidth(), imgSpd11.getHeight());
		Rectangle btnSpd2Pos = new Rectangle(20, 140,
							imgSpd21.getWidth(), imgSpd21.getHeight());
		Rectangle btnSpd3Pos = new Rectangle(20, 200,
							imgSpd31.getWidth(), imgSpd31.getHeight());
		Rectangle btnSpd4Pos = new Rectangle(20, 260,
							imgSpd41.getWidth(), imgSpd41.getHeight());
		Rectangle btnSpd5Pos = new Rectangle(20, 320,
							imgSpd51.getWidth(), imgSpd51.getHeight());
		
		setButton(btnStart, imgStart1, imgStart2, imgStart3, btnStartPos);
		setButton(btnSpd1, imgSpd11, imgSpd12, imgSpd13, btnSpd1Pos);
		setButton(btnSpd2, imgSpd21, imgSpd22, imgSpd23, btnSpd2Pos);
		setButton(btnSpd3, imgSpd31, imgSpd32, imgSpd33, btnSpd3Pos);
		setButton(btnSpd4, imgSpd41, imgSpd42, imgSpd43, btnSpd4Pos);
		setButton(btnSpd5, imgSpd51, imgSpd52, imgSpd53, btnSpd5Pos);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sysStat = "Working";
				waitState = false;
			}
		});
		
		btnSpd1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sysStat = "Paused";
				setStatState();
				waitState = true;
			}
		});
		
		btnSpd2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spdMultiplier = 1;
				for (JobVisuals visuals : jobs) {
					visuals.setSpeedMultiplier(spdMultiplier);
				}
			}
		});
		
		btnSpd3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spdMultiplier = 2;
				for (JobVisuals visuals : jobs) {
					visuals.setSpeedMultiplier(spdMultiplier);
				}
			}
		});
		
		btnSpd4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spdMultiplier = 4;
				for (JobVisuals visuals : jobs) {
					visuals.setSpeedMultiplier(spdMultiplier);
				}
			}
		});
		
		btnSpd5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spdMultiplier = 8;
				for (JobVisuals visuals : jobs) {
					visuals.setSpeedMultiplier(spdMultiplier);
				}
			}
		});
	}
	
	/**
	 * Creates a button with a certain image and properties and adds it
	 * to the custom drawing array.
	 * @param btn Button to be initialized
	 * @param im1 Button's default image
	 * @param im2 Button's mouse-over image
	 * @param im3 Button's clicked image
	 * @param bound Button's position in the GUI
	 */
	private void setButton(JButton btn,
						   BufferedImage im1,
						   BufferedImage im2,
						   BufferedImage im3,
						   Rectangle bound) {
		btn.setIcon(new ImageIcon(im1));
		btn.setRolloverIcon(new ImageIcon(im2));
		btn.setPressedIcon(new ImageIcon(im3));
		btn.setBorder(new EmptyBorder(0, 0, 0, 0));
		btn.setBounds(bound);
		btn.setContentAreaFilled(false);
		this.add(btn);
	}
	
	
// ---------- Protected methods
	
	/**
	 * Handles all of the drawing of custom images as well as text in the
	 * GUI.
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for (StaticVisuals visuals : elements) {
			visuals.drawSprite(g);
		}
		
		g.drawImage(imgJobSpace, 175, 220, null);
		g.drawImage(imgJobSpace, 375, 220, null);
		g.drawImage(imgJobSpace, 575, 220, null);
		g.drawImage(imgJobSpace, 775, 220, null);
		g.drawImage(imgCPU, 390, 20, null);
		g.setColor(Color.blue);
		
		g.setColor(Color.white);
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.drawString("CPU Quantum: " + cpuQuantum, 420, 143);
		g.drawString("Queue 1", 175, 210);
		g.drawString("Queue 2", 375, 210);
		g.drawString("Queue 3", 575, 210);
		g.drawString("Queue 4", 775, 210);
		g.drawRect(579,20,351,141);
		g.drawRect(179,19,171,141);
		
		g.setColor(Color.black);
		for (JobVisuals visuals : jobs) {
			visuals.drawImage(g);
			visuals.drawText(g);
		}
	}
}
