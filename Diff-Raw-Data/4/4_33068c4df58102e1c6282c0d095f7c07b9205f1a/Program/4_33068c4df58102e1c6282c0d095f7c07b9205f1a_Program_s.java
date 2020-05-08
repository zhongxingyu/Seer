 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 /********************************************************************************
 ** This class is used to store and manage the program commands.
 */
 
 public class Program
 	implements Runnable
 {
 	/********************************************************************************
 	** Clear the program.
 	*/
 
 	public void clear()
 	{
 		m_vCmds.removeAll();
 		m_vStack.removeAllElements();
 
 		notifyListeners(CLEARED);
 	}
 
 	/********************************************************************************
 	** Add the command to the program.
 	*/
 
 	public void add(Cmd oCmd)
 	{
 		// Not inside a repeat cmd?
 		if (m_vStack.size() == 0)
 		{
 			m_vCmds.add(oCmd);
 
 			// Start a new cmd block.
 			if (oCmd instanceof RepeatCmd)
 				m_vStack.push(oCmd);
 		}
 		// Inside a repeat cmd.
 		else // (m_vStack.size() > 0)
 		{
 			RepeatCmd oRepeatCmd = (RepeatCmd) m_vStack.lastElement();
 
 			// Add to the repeat cmd.
 			oRepeatCmd.add(oCmd);
 
 			// Start a new cmd block.
 			if (oCmd instanceof RepeatCmd)
 				m_vStack.push(oCmd);
 
 			// Pop repeat cmd off the stack.
			if (oCmd instanceof EndRepeatCmd)
 				m_vStack.pop();
 		}
 	}
 
 	/********************************************************************************
 	** Start the thread to execute the program.
 	*/
 
 	public void execute(Component oFrame, Turtle oTurtle, int nInterval)
 	{
 		oTurtle.reset(oTurtle.isVisible());
 
 		m_oThread   = new Thread(this, "Execute");
 		m_oTurtle   = oTurtle;
 		m_nInterval = nInterval;
 		m_oFrame    = oFrame;
 
 		m_oThread.start();
 	}
 
 	/********************************************************************************
 	** Execute the program using a different context.
 	*/
 
 	public void execute(Component oFrame, ExecContext oContext)
 	{
 		notifyListeners(STARTED);
 
 		try
 		{
 			Turtle oTurtle = oContext.getTurtle();
 
 			oTurtle.reset(oTurtle.isVisible());
 
 			m_vCmds.execute(oContext);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 
 			MsgBox.alert(oFrame, "JLogo", e.toString());
 		}
 
 		notifyListeners(STOPPED);
 	}
 
 	/********************************************************************************
 	** Gets if the program is currently executing.
 	*/
 
 	public boolean isExecuting()
 	{
 		return (m_oThread != null);
 	}
 
 	/********************************************************************************
 	** Execute the program on a separate thread.
 	*/
 
 	public void run()
 	{
 		notifyListeners(STARTED);
 
 		try
 		{
 			ExecContext oContext = new ExecContext(m_oTurtle, m_oThread, m_nInterval);
 
 			oContext.pause();
 			m_vCmds.execute(oContext);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 
 			MsgBox.alert(m_oFrame, "JLogo", e.toString());
 		}
 
 		notifyListeners(STOPPED);
 
 		m_oThread = null;
 		m_oTurtle = null;
 	}
 
 	/********************************************************************************
 	** Convert the program to source code.
 	*/
 
 	public SourceLines getSource()
 	{
 		SourceLines oLines = new SourceLines();
 
 		m_vCmds.getSource(oLines);
 
 		return oLines;
 	}
 
 	/********************************************************************************
 	** Event handler methods.
 	*/
 
 	public void addActionListener(ActionListener l)
 	{
 		m_oListeners = AWTEventMulticaster.add(m_oListeners, l);
 	}
 
 	public void removeActionListener(ActionListener l)
 	{
 		m_oListeners = AWTEventMulticaster.remove(m_oListeners, l);
 	}
 
 	/********************************************************************************
 	** Notify event listeners of the event.
 	*/
 
 	public void notifyListeners(String strEvent)
 	{
 		if (m_oListeners != null)
 			m_oListeners.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, strEvent));
 	}
 
 	/********************************************************************************
 	** Constants.
 	*/
 
 	// Event types.
 	public static final String EDITED  = "EDITED";
 	public static final String STARTED = "STARTED";
 	public static final String STOPPED = "STOPPED";
 	public static final String CLEARED = "CLEARED";
 
 	// Program speeds.
 	public static final int SPEED_FACTOR = 150;
 
 	public static final int FAST    = (0 * SPEED_FACTOR);
 	public static final int MEDIUM  = (1 * SPEED_FACTOR);
 	public static final int SLOW    = (2 * SPEED_FACTOR);
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	// The command list.
 	private CmdBlock	m_vCmds  = new CmdBlock();
 	private Stack		m_vStack = new Stack();
 
 	// The execute thread.
 	private Thread		m_oThread;
 	private Turtle		m_oTurtle;
 	private int			m_nInterval;
 	private Component	m_oFrame;
 
 	// Event listeners.
 	private ActionListener m_oListeners;
 }
