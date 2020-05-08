 package ee.ut.math.tvt.LaheLabor;
 
 
import java.awt.FlowLayout;
 import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;

 
 public class IntroUI extends JFrame {
 	 IntroUI(){
 	 
 	    this.setSize(300, 300); 
	    this.setLocation(100, 100); 	    
	    
	    JLabel content=new JLabel(new ImageIcon(getClass().getResource("team.jpg")));
 	    content.setLayout(new FlowLayout());
	    add(content);
 
 	    JLabel label1 = new JLabel("Lahe Labor"); 
 	    JLabel label2 = new JLabel("Rene Roost"); 
 	    JLabel label3 = new JLabel("roost.rene@gmail.com"); 
	    JLabel label4 = new JLabel("Team members: Liisbet Jyrlau, Jonatan Raudsepp, Karl Kuusk, Rene Roost"); 
 	    JLabel label5= new JLabel("1.0.0"); 
 
 	    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
 	    content.add(label1);
 	    content.add(label2);
 	    content.add(label3);
 	    content.add(label4);
 	    content.add(label5);
 	    this.setVisible(true);
 	    
 	 }
 }
