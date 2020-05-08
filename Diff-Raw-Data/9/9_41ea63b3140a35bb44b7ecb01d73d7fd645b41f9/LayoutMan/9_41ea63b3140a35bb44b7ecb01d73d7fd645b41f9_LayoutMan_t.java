 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.LayoutManager;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 public class LayoutMan implements LayoutManager{
 
 	@Override
 	public void layoutContainer(Container parent) {
 		System.out.println("on rentre dans layoutContainer");
 		Window w = (Window) parent.getParent().getParent().getParent() ;
 		int wX = w.getWidth() ;
 		int wY = w.getHeight() ;
 		float ratio = wX/(float)wY ;
 
 		System.out.println("le ratio dans layoutman: " + ratio) ;
 		System.out.println("win layoutMan l h: " + wX + " " + wY) ;
 		
 
 		if (16/(float)9 - 0.005 < ratio && ratio < 16/(float)9 + 0.005) {
 			System.out.println("cool") ;
 			int margin = 10 ;
 			Component[] c = parent.getComponents();
 
 			c[0].setBounds(margin, margin, wX/6, wY/11) ;  // label1
 			c[1].setBounds(wX - (margin + 30 + 2*wX/8), margin, wX/8, wY/11) ;         // button1
 			c[2].setBounds(wX - (margin + wX/8), margin, wX/8, wY/11) ;              // button2
 
 			c[3].setBounds(margin, c[0].getY() + c[0].getHeight() + 5, wX - 2*margin - 35, wY/11) ;   // text
			c[4].setBounds(wX - (margin + 35), c[3].getY(), 35, c[3].getHeight()) ;     // button3
 			
 		
 			c[5].setBounds(margin + (int)(wX/3.69), c[3].getY() + c[3].getHeight() + 20 , (int)(wX/2.4), (int)(wY/3.86)) ;   // list
 
 			int l = (wX - 2*margin - 3*20)/4 ;
 			int y = c[5].getY() + c[5].getHeight() + 20 ;
 			c[6].setBounds(margin, y, l, (int)(wY/5.4)) ;     // button4
 			c[7].setBounds(margin + l + 20 , y, l, (int)(wY/5.4)) ;   // button5
 			c[8].setBounds(margin + 2*l + 40, y, l, (int)(wY/5.4)) ;     // button6
 			c[9].setBounds(margin + 3*l + 60, y, l, (int)(wY/5.4)) ;   // button7
 			c[10].setBounds(margin, c[6].getY() + c[6].getHeight() + 10, wX - 2*margin, wY/11) ;  // label2
 
 			ImageIcon twitter = new ImageIcon("icon/twitter.png");
 			Image itwitter = twitter.getImage().getScaledInstance(c[0].getWidth()/2,c[0].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JLabel)c[0]).setIcon(new ImageIcon(itwitter));
 			
 			ImageIcon logout = new ImageIcon("icon/logout.png");
 			Image ilogout = logout.getImage().getScaledInstance(c[1].getWidth()/2,c[1].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[1]).setIcon(new ImageIcon(ilogout));
 			
 			ImageIcon rotate = new ImageIcon("icon/rotate.png");
 			Image irotate = rotate.getImage().getScaledInstance(c[2].getWidth()/2,c[2].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[2]).setIcon(new ImageIcon(irotate));
 			
 			ImageIcon search = new ImageIcon("icon/search.png");
 			Image isearch = search.getImage().getScaledInstance(c[4].getWidth()/2,c[4].getHeight()/2,java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[4]).setIcon(new ImageIcon(isearch));
 			
 			ImageIcon contact = new ImageIcon("icon/contact.jpg");
 			Image icontact = contact.getImage().getScaledInstance(c[6].getWidth()/2,c[6].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[6]).setIcon(new ImageIcon(icontact));
 			
 			ImageIcon horloge = new ImageIcon("icon/horloge.png");
 			Image ihorloge = horloge.getImage().getScaledInstance(c[7].getWidth()/2,c[7].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[7]).setIcon(new ImageIcon(ihorloge));
 			
 			ImageIcon phone = new ImageIcon("icon/phone.png");
 			Image iphone = phone.getImage().getScaledInstance(c[8].getWidth()/2,c[8].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[8]).setIcon(new ImageIcon(iphone));
 			
 			ImageIcon profile = new ImageIcon("icon/profile.png");
 			Image iprofile = profile.getImage().getScaledInstance(c[9].getWidth()/2,c[9].getHeight(),java.awt.Image.SCALE_SMOOTH);
 			((JButton)c[9]).setIcon(new ImageIcon(iprofile));
 						
 		}
 		else if (9/(float)16 - 0.005 < ratio && ratio < 9/(float)16 +0.005) {
 			System.out.println("format 9/16 bien reconnu") ;
 			
 		}
 			System.out.println("on sort de layoutContainer") ;
 			System.out.println("") ;
 	}
 
 	@Override
 	public Dimension minimumLayoutSize(Container parent) {
 		return new Dimension(480,270);
 	}
 
 	@Override
 	public Dimension preferredLayoutSize(Container parent) {
 		return new Dimension(480, 270);
 	}
 
 	@Override
 	public void removeLayoutComponent(Component comp) {}
 
 	@Override
 	public void addLayoutComponent(String name, Component comp) {}
 }
