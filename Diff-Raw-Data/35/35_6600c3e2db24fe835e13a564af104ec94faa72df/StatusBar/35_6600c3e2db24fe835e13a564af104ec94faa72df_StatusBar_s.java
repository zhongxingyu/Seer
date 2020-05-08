 package mensonge.userinterface;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import mensonge.core.tools.ActionMessageObserver;
 import mensonge.core.tools.CacheObserver;
 import mensonge.core.tools.DataBaseObserver;
 import mensonge.core.tools.Utils;
 
 public class StatusBar extends JPanel implements ActionListener, ActionMessageObserver, DataBaseObserver, CacheObserver
 {
 	private static final long serialVersionUID = 8573623540967463794L;
 	private static final int STATUS_BAR_HEIGHT = 16;
 	/**
 	 * En millisecondes
 	 */
 	private static final int TIMER_DELAY = 10000;
 	private Timer timer;
 	private GraphicalUserInterface gui;
 	private JLabel status;
 	private JLabel dbSize;
 	private JLabel cacheSize;
 
 	public StatusBar(GraphicalUserInterface graphicalUserInterface)
 	{
 		super();
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		this.status = new JLabel();
 		this.add(status);
 
 		this.cacheSize = new JLabel();
		this.dbSize = new JLabel("Taille de la base de données : " + Utils.humanReadableByteCount(Utils.getDBSize(), false));
 		this.add(Box.createHorizontalGlue());
 		this.add(cacheSize);
 		this.add(Box.createHorizontalStrut(10));
 		this.add(dbSize);
		
 		this.gui = graphicalUserInterface;
 		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
 		setPreferredSize(new Dimension(this.getWidth(), STATUS_BAR_HEIGHT));
 		timer = new Timer(TIMER_DELAY, this);
 	}
 
 	public void setMessage(String message)
 	{
 		this.status.setText(" " + message);
 		repaint();
 		this.timer.stop();
 	}
 
 	public void done()
 	{
 		this.timer.restart();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0)
 	{
 		this.status.setText("");
 		this.timer.stop();
 	}
 
 	@Override
 	public void onInProgressAction(String message)
 	{
 		this.setMessage(message);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	}
 
 	@Override
 	public void onCompletedAction(String message)
 	{
 		this.setMessage(message);
 		this.done();
		this.setCursor(Cursor.getDefaultCursor());
		this.getParent().setCursor(Cursor.getDefaultCursor());
		gui.setCursor(Cursor.getDefaultCursor());
 	}
 
 	@Override
 	public void onUpdateCache(long newCacheSize)
 	{
 		cacheSize.setText("Taille du cache : " + Utils.humanReadableByteCount(newCacheSize, false));
 	}
 
 	@Override
 	public void onUpdateDataBase()
 	{
 		dbSize.setText("Taille de la base de données : " + Utils.humanReadableByteCount(Utils.getDBSize(), false));
 	}

 }
