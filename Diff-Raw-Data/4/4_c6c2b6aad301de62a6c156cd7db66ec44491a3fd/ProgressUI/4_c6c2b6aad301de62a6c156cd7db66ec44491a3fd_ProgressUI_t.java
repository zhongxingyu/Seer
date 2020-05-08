 package cytoscape.process.ui;
 
 import cytoscape.Cytoscape;
 import cytoscape.process.Stoppable;
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 
 /**
  * This class is a utility for providing a popup dialog which displays progress
  * on a lengthy process.  Historically, Cytoscape tasks such as loading a
  * graph from a file or laying out a large graph were done in the AWT event
  * handling thread.  Such tasks take on the order of minutes sometimes; because
  * these tasks were computed by the AWT event handling thread, the Cytoscape
  * desktop would become unresponsive while these tasks were executing.
  * <code>ProgressUI</code> was designed as a framework to ease the transition
  * of computing lengthy tasks in theads other than the AWT event handling
  * thread; see next paragraph.<p>
  * Tasks which were initially forked as new threads to prevent the
  * unresponsive Cytoscape desktop problem touched parts of code which
  * invoked Swing and/or Piccolo libraries.  Therefore, it was necessary to
  * limit, as much as programatically possible, actions on the desktop which
  * would cause concurrent execution of similar code (Swing and Piccolo are
  * only single-thread safe).  To do this, modal dialogs were created which
  * could not be closed by the user, and which would block all user input
  * until the task in question had finished.
  **/
 public final class ProgressUI
 {
 
   private static final Object[] s_contrl = new Object[1];
 
   // No constructor for this class.
   private ProgressUI() {}
 
   /**
    * Creates a modal progress dialog.  (But does not show it, yet.)
    * A progress dialog is global and
    * there should only be one background process running at a time which
    * corresponds to this dialog - the reasoning behind this is that
    * we want to stay as close as possible to a single-threaded model.<p>
    * A plain vanilla progress dialog has no stop button and has a generic
    * progress animation which knows nothing about percent completed of the
    * process.  The two options for a progress dialog are a stop button (which
    * can be made to appear by passing a non-<code>nulL</code>
    * <code>stop</code> parameter to this method) and a progress animation with
    * a percent completed (the percent completed animation is triggered by
    * using the returned <code>ProgressUIControl</code> object).<p>
    * This method <i>MUST</i> be called from the AWT queue handling thread.<p>
    * A progress UI can only be created once all previous progress UIs have been
    * disposed of.  This method will throw an <code>IllegalStateException</code>
    * if previous progress UI has not been disposed of at the time this method
    * is called.
    *
    * @param title desired title of the dialog window; may not be
    *   <code>null</code>.
    * @param message brief message that will appear to the user;
    *   may not be <code>null</code>.
    * @param stop hook to allow a stop button to stop a process; if
    *   <code>null</code>, no stop button will appear in the dialog.
    * @return hook for controlling this UI.
    * @exception IllegalStateException if this is called while another
    *   progress dialog is currently open.
    * @exception IllegalThreadStateException if this is called from a thread
   *   that is not the AWT event handling thread
   *   (<nobr><code>java.awt.EventQueue.isDispatchThread()</code></nobr>).
    **/
   public static ProgressUIControl startProgress(String title,
                                                 String message,
                                                 final Stoppable stop)
   {
     if (!EventQueue.isDispatchThread())
       throw new IllegalThreadStateException
         ("startProgress() required to be called from AWT dispatch thread");
     if (title == null) throw new NullPointerException("title is null");
     if (message == null) throw new NullPointerException("message is null");
     Frame frame = Cytoscape.getDesktop();
     JDialog busyDialog = new JDialog(frame, title, true);
     busyDialog.setResizable(false);
     busyDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
     JPanel panel = new JPanel(new BorderLayout());
     panel.setBorder(new EmptyBorder(20, 20, 20, 20));
     panel.add(new JLabel(message), BorderLayout.CENTER);
     final JProgressBar progress = new JProgressBar(0, 100);
     progress.setIndeterminate(true);
     progress.setStringPainted(true);
     progress.setString("");
     panel.add(progress, BorderLayout.SOUTH);
     busyDialog.getContentPane().add(panel, BorderLayout.CENTER);
     final ProgressUIControl returnThis = new ProgressUIControl
       (s_contrl, busyDialog, new PercentCompletedHook() {
           public void setPercentCompleted(final int percent) {
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                   progress.setIndeterminate(false);
                   progress.setString(null);
                   progress.setValue(percent); } } ); } },
        frame);
     if (stop != null)
     {
       JButton button = new JButton("Stop");
       button.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
             try { stop.stop(); }
             finally { returnThis.dispose(); } } } );
       JPanel panel2 = new JPanel(new FlowLayout());
       panel2.setBorder(new EmptyBorder(0, 20, 20, 20));
       panel2.add(button);
       busyDialog.getContentPane().add(panel2, BorderLayout.SOUTH);
     }
     else
     {
       busyDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
     }
     synchronized (s_contrl) {
       if (s_contrl[0] == null) s_contrl[0] = new Object();
       else throw new IllegalStateException
              ("another progress dialog is currently being shown"); }
     return returnThis;
   }
 
 }
