 package de.zigapeda.flowspring.gui;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.Painter;
 import javax.swing.UIManager;
 
 import de.zigapeda.flowspring.Main;
 
 public class Balancebar extends JPanel implements MouseListener, MouseMotionListener {
 	private static final long serialVersionUID = -1418651840977914483L;
 	private JProgressBar balancebar;
 	private JLabel balance;
 	
 	public Balancebar() {
 		super();
 		this.setLayout(null);
 		this.balancebar = new BalanceProgressBar();
 		this.balance = new JLabel("balance");
 		this.balancebar.setBounds(0, 14, 100, 14);
 		this.balance.setBounds(5, 0, 95, 14);
 		this.balancebar.setMaximum(100);
 		this.balancebar.addMouseListener(this);
 		this.balancebar.addMouseMotionListener(this);
 		this.balancebar.setValue(50);
 		this.add(this.balancebar);
 		this.add(this.balance);
 	}
	
	public int getValue() {
		return this.balancebar.getValue();
	}
 
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	public void mousePressed(MouseEvent e) {
 		this.balancebar.setValue(e.getX());
 		Main.getWindow().getPlayercontroller().setPan(this.balancebar.getValue());
 		this.balancebar.repaint();
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 
 	public void mouseExited(MouseEvent e) {
 		
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		this.balancebar.setValue(e.getX());
 		Main.getWindow().getPlayercontroller().setPan(this.balancebar.getValue());
 		this.balancebar.repaint();
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		
 	}
 }
 
 class BalanceProgressBar extends JProgressBar {
 	private static final long serialVersionUID = 7161447457324882101L;
 	
 	public int getValue() {
 		if(super.getValue() < 45 || super.getValue() > 55) {
 			return super.getValue();
 		}
 		return 50;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void paint(Graphics g) {
 		Painter<JProgressBar> p = (Painter<JProgressBar>) UIManager.get("ProgressBar[Enabled].backgroundPainter");
 		p.paint((Graphics2D) g, this, 100, 14); 
         p = (Painter<JProgressBar>) UIManager.get("ProgressBar[Enabled+Finished].foregroundPainter");
         int v = this.getValue();
         if(v < 45) {
         	v = 45 - v;
             p.paint((Graphics2D) g.create(45 - v, 0, 10 + v, 14), this, 10 + v, 14);
         } else if(v > 55) {
         	v = v - 55;
             p.paint((Graphics2D) g.create(45, 0, 10 + v, 14), this, 10 + v, 14);
         } else {
             p.paint((Graphics2D) g.create(45, 0, 10, 14), this, 10, 14);
         }
     }
 }
