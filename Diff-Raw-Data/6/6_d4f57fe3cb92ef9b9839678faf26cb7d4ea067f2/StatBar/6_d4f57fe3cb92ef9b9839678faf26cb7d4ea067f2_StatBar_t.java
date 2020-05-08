 package yuuki.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import yuuki.entity.VariableStat;
 
 /**
  * A bar for displaying a stat.
  * 
  * @author TF Nelson
  */
 public class StatBar extends JPanel {
 	
 	private static final long serialVersionUID = 8673785141956435286L;
 
 	/**
 	 * The color of the unfilled portion of all StatBar instances.
 	 */
 	public static final Color BACKGROUND_COLOR = new Color(79, 76, 0);
 
 	/**
 	 * The color of the filled portion of this StatBar.
 	 */
 	private Color barColor;
 	
 	/**
 	 * The total width of this StatBar, including the border.
 	 */
 	private int width;
 	
 	/**
 	 * The total height of this StatBar, including the border.
 	 */
 	private int height;
 	
 	/**
 	 * The VariableStat that this StatBar is showing.
 	 */
 	private VariableStat stat;
 	
 	/**
 	 * The level of the fighter who owns this StatBar's VariableStat.
 	 */
 	private int level;
 	
 	/**
 	 * The currently-displayed value of this StatBar.
 	 */
 	private int value;
 	
 	/**
 	 * Creates a new StatBar with the given dimensions and whose filled portion
 	 * is the given color.
 	 * 
 	 * @param width The total width of the StatBar in pixels.
 	 * @param height The total height of the StatBar in pixels.
 	 * @param barColor The color of the filled portion of the StatBar.
 	 */
 	public StatBar(int width, int height, Color barColor) {
 		this.width = width;
 		this.height = height;
 		this.barColor = barColor;
 		this.stat = null;
 		this.value = 0;
 		this.level = 1;
 	}
 	
 	/**
 	 * Sets this StatBar to be associated with a VariableStat. Subsequent calls
 	 * to update() will use this VaraibleStat to set values.
 	 * 
 	 * @param stat The VariableStat to associate with this StatBar.
 	 */
 	public void setStat(VariableStat stat) {
 		this.stat = stat;
 		this.value = stat.getCurrent();
 	}
 	
 	/**
 	 * Sets the level of the fighter that owns this StatBar's VariableStat.
 	 * 
 	 * @param level The level.
 	 */
 	public void setLevel(int level) {
 		this.level = level;
 	}
 	
 	/**
 	 * Updates this StatBar to show the value of its associated VariableStat.
 	 * If it is a new value, this StatBar is repainted.
 	 */
 	public void update() {
 		if (stat.getCurrent() != this.value) {
 			repaint(1, 1, width - 2, height - 2);
 			this.value = stat.getCurrent();
 			repaint(1, 1, width - 2, height - 2);
 		}
 	}
 	
 	/**
 	 * Checks whether this StatBar is displaying the value of the given
 	 * VariableStat.
 	 * 
 	 * @param stat The stat to check.
 	 * 
 	 * @return Whether the given VariableStat and this StatBar's VariableStat
 	 * reference the same instance.
 	 */
 	public boolean isWatching(Object stat) {
 		return (this.stat == stat);
 	}
 	
 	/**
 	 * Gets the percent of this StatBar that is currently filled.
 	 * 
 	 * @return The percent filled.
 	 */
 	public double getPercent() {
 		return (double) stat.getCurrent() / stat.getMax(level);
 	}
 	
 	/**
 	 * Gets the preferred size of this component.
 	 * 
 	 * @return A Dimension containing the preferred size.
 	 */
 	public Dimension getPreferredSize() {
 		return new Dimension(width, height);
 	}
 	
 	/**
 	 * Paints this component on a graphical context.
 	 * 
 	 * @param g The graphical context to paint this component on.
 	 */
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Color old = g.getColor();
 		paintEdge(g);
 		paintBackground(g);
 		paintProgress(g);
 		g.setColor(old);
 	}
 	
 	/**
 	 * Paints the border of this component on a graphical context.
 	 * 
 	 * @param g The graphical context to paint the border on.
 	 */
 	private void paintEdge(Graphics g) {
 		g.setColor(Color.BLACK);
		// drawRect actually adds one to each dimension
		g.drawRect(0, 0, width - 1, height - 1);
 	}
 	
 	/**
 	 * Paints the unfilled background of this component on a graphical context.
 	 * 
 	 * @param g The graphical context to paint the background on.
 	 */
 	private void paintBackground(Graphics g) {
 		double p = getPercent();
 		int progWidth = (int) Math.round((width - 2) * p);
 		int rectWidth = (width - 2) - progWidth;
 		g.setColor(BACKGROUND_COLOR);
		g.fillRect(progWidth + 1, 1, rectWidth, height - 2);
 	}
 	
 	/**
 	 * Paints the filled foreground of this component on a graphical context.
 	 * 
 	 * @param g The graphical context to paint the foreground on.
 	 */
 	private void paintProgress(Graphics g) {
 		double p = getPercent();
 		int progWidth = (int) Math.round((width - 2) * p);
 		g.setColor(barColor);
 		g.fillRect(1, 1, progWidth, height - 2);
 	}
 	
 }
