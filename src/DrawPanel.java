import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Handles drawing components that use custom images.
 *
 */
public class DrawPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private List<StaticVisuals> elements;
	private List<JobVisuals> jobs;
	
	public DrawPanel() {
		elements = new ArrayList<StaticVisuals>();
		jobs = new ArrayList<JobVisuals>();
	}
	
	public void addVisualObject(StaticVisuals in) {
		elements.add(in);
	}
	
	public void addJobObject(JobVisuals in) {
		jobs.add(in);
	}
	
	public void removeJobObject(int n) {
		jobs.remove(n);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponents(g);
		
		for (StaticVisuals visuals : elements) {
			visuals.drawSprite(g);
		}
		for (JobVisuals visuals : jobs) {
			visuals.drawImage(g);
			visuals.drawText(g);
		}
	}
}
