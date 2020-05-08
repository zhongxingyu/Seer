 package sec02.ex03;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JWindow;
 
 public class ClockMainWindow extends JWindow implements MouseListener, MouseMotionListener, Observer{
 	private static final long serialVersionUID = 1L;
 	private static int height = 100;
 	private static int width = 200;
 	
 	private GridBagLayout gbl = new GridBagLayout();
 	private ClockPropertyData data = new ClockPropertyData(); //プロパティデータ
 	private JPopupMenu pop = new ClockMenuPopup(data); //ポップアップメニュー
 	private JPanel digitalTimePanel = new ClockDigitalTimePanel(data);
 	private JPanel analogTimePanel = new ClockAnalogTimePanel(data);
 	
     /*
      * コンストラクタ
      */
     public ClockMainWindow() {
         setLayout(gbl);    
         digitalTimePanel.add(pop);
         analogTimePanel.add(pop);
         addPanel(digitalTimePanel, 0, 0, 1, 1);
         addMouseListener(this);
         addMouseMotionListener(this);
         data.addObserver(this);
         
         setSize(width, height);
         this.setVisible(true);
     }
     
     private void addPanel(JPanel panel, int x, int y, int w, int h) {
 		GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 0.8d;
         gbc.weighty = 0.8d;
         gbc.gridx = x;
         gbc.gridy = y;
         gbc.gridwidth = w;
         gbc.gridheight = h;
         gbc.anchor = GridBagConstraints.NORTH;
         //gbc.insets = new Insets(10, 10, 10, 10);
         gbl.setConstraints(panel, gbc);
         add(panel);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private MouseEvent start;
 	@Override
 	public void mousePressed(MouseEvent e) {
 		start = e;
 		//System.out.println("mousePressed");
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
		int btn = e.getButton();
    	if (btn == MouseEvent.BUTTON3) {
     		//System.out.println("right click");
     		pop.show(this , e.getX() , e.getY());
     	}	
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
		int btn = e.getButton();
    	if (btn == MouseEvent.BUTTON1 || btn == MouseEvent.BUTTON3) {
     		// System.out.println("drag");
     		Point eventLocationOnScreen = e.getLocationOnScreen();
     	    setLocation(eventLocationOnScreen.x - start.getX(),
                     eventLocationOnScreen.y - start.getY());
     	}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub	
 	}
 	
 	@Override
 	public void update(Observable event, Object arg) {
 		if (arg == null)
 			return;
 		
 		String str = (String) arg;
 		if (str.equals("analog")) {
 			getContentPane().removeAll();
 			setSize(400, 400);
 			addPanel(analogTimePanel, 0, 0, 1, 1);
 			analogTimePanel.setVisible(true);
 		} else if (str.equals("digital")) {
 			getContentPane().removeAll();
 			addPanel(digitalTimePanel, 0, 0, 1, 1);
 			digitalTimePanel.setVisible(true);
 		} else {
 			System.out.println("Fatal error!");
 		}
 	}
 
 	public static void main(String args[]) {	
 		ClockMainWindow clock = new ClockMainWindow();
 	}
 }
