 package kingOfT;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class TokyoPane {
 	private TokyoArea tokyoArea;
 	public JPanel tokyoAreaHolder;
 	public JPanel notTokyoAreaHolder;
 	private JLabel[] tokyoSpaces;
 	private JLabel[] notTokyoSpaces;
 	final ImageIcon icon[] = {
 			new ImageIcon("Pictures/m1.png", "Monster 1"),
 			new ImageIcon("Pictures/m2.png", "Monster 2"),
 			new ImageIcon("Pictures/m3.png", "Monster 3"),
 			new ImageIcon("Pictures/m4.png", "Monster 4"),
 			new ImageIcon("Pictures/m5.png", "Monster 5"),
 			new ImageIcon("Pictures/m6.png", "Monster 6")
 	};
 	final ImageIcon empty = new ImageIcon("Pictures/empty.png","Empty");
 	
 	final Map<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();
 	
 	public void draw() {
 		ArrayList<Monster> tokyoMons = tokyoArea.getMonstersInTokyo();
 		for (int i=0; i<tokyoMons.size(); i++){
 			String name = tokyoMons.get(i).getName();
 			tokyoSpaces[i].setIcon(iconMap.get(name));
 		}
 		for (int i=tokyoMons.size(); i<2; i++){
 			tokyoSpaces[i].setIcon(empty);
 		}
 		ArrayList<Monster> notTokyoMons = tokyoArea.getMonstersNotInTokyo();
 		for (int i=0; i<notTokyoMons.size(); i++){
 			String name = notTokyoMons.get(i).getName();
 			notTokyoSpaces[i].setIcon(iconMap.get(name));
 		}
		for (int i=notTokyoMons.size(); i<6; i++){
 			notTokyoSpaces[i].setIcon(empty);
 		}
 	}
 	public TokyoPane() {
 		tokyoAreaHolder = new JPanel();
 		tokyoSpaces = new JLabel[2];
 		
 		for (int i = 0; i<2; i++) {
 			tokyoSpaces[i] = new JLabel("",empty, JLabel.CENTER);
 			tokyoAreaHolder.add(tokyoSpaces[i]);
 		}
 		notTokyoSpaces = new JLabel[6];
 		notTokyoAreaHolder = new JPanel();
 		for (int i = 0; i<6; i++) {
 			notTokyoSpaces[i] = new JLabel("",empty, JLabel.CENTER);
 			notTokyoAreaHolder.add(notTokyoSpaces[i]);
 		}
 		BoxLayout tokyoBox = new BoxLayout(tokyoAreaHolder,BoxLayout.X_AXIS);
 		tokyoAreaHolder.setLayout(tokyoBox);
 		
 		BoxLayout notTokyoBox = new BoxLayout(notTokyoAreaHolder,BoxLayout.X_AXIS);
 		notTokyoAreaHolder.setLayout(notTokyoBox);
 		iconMap.put("A", icon[0]);
 		iconMap.put("B", icon[1]);
 		iconMap.put("C", icon[2]);
 		iconMap.put("D", icon[3]);
 		iconMap.put("E", icon[4]);
 		iconMap.put("F", icon[5]);
 		}
 
 
 	public void setTokyoArea(TokyoArea tokyoArea) {
 		this.tokyoArea = tokyoArea;
 	}
 	
 	
 }
