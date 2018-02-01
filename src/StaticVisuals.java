import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

/**
 * Handles custom GUI elements that are simply drawn and do not interact
 * with other GUI elements.
 */
public class StaticVisuals extends JComponent {

// ---------- Field members
	
	private static final long serialVersionUID = 1L;
	private Image activeImage;
	private Dimension position;
	
	
// ---------- Public methods
	
	/**
	 * Constructor sets up class field members.
	 */
	public StaticVisuals() {
		activeImage = null;
		position = new Dimension(0,0);
	}
	
	/**
	 * Sets the image of the GUI element.
	 * @param im Image that the GUI element will draw
	 */
	public void setImage(Image im) {
		activeImage = im;
	}
	
	/**
	 * Sets the position to which the image will be drawn at.
	 * @param x X position
	 * @param y Y position
	 */
	public void setPosition(int x, int y) {
		position.height = y;
		position.width = x;
	}
	
	/**
	 * Sets the position relative to current position to which the image
	 * will be drawn at.
	 * @param x Relative X position
	 * @param y Relative Y position
	 */
	public void setPositionRel(int x, int y) {
		position.height += y;
		position.width += x;
	}
	
	/**
	 * Returns the position of the GUI element. Returns the position as
	 * a Dimension object.
	 * @return Position of GUI object
	 */
	public Dimension getPosition() {
		return position;
	}
	
	
// ---------- Protected methods
	
	/**
	 * Handles drawing the GUI element's image in the GUI.
	 * @param g Drawing buffer
	 */
	protected void drawSprite(Graphics g) {
		g.drawImage(activeImage, position.width, position.height, null);
	}
}
