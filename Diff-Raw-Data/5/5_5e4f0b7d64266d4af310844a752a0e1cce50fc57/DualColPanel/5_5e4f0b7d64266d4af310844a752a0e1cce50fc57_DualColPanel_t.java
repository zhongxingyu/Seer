 import java.awt.*;
 
 /********************************************************************************
** This panel derived class has two vertical child panels, a left and a right.
 */
 
 public class DualColPanel extends Panel
 {
 	/********************************************************************************
 	** Constructor.
 	*/
 
 	public DualColPanel()
 	{
 		super(new BorderLayout(5, 5));
 
 		// Add the two child panels.
 		add(BorderLayout.WEST,   m_pnlLeft);
 		add(BorderLayout.CENTER, m_pnlRight);
 	}
 
 	/********************************************************************************
	** Add a componet to each of the left and right child panels.
 	*/
 
 	public void add(Component cmpLeft, Component cmpRight)
 	{
 		m_pnlLeft.add (cmpLeft);
 		m_pnlRight.add(cmpRight);
 	}
 
 	/********************************************************************************
 	** Constants.
 	*/
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	// Child panels.
 	private Panel	m_pnlLeft  = new Panel(new GridLayout(0, 1, 5, 5));
 	private Panel	m_pnlRight = new Panel(new GridLayout(0, 1, 5, 5));
 }
