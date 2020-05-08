 package vms.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.BoxLayout;
 
 import common.Coord;
 
 public class DataPanel extends JPanel implements MapPanel.Observer {
 	private JPanel centerPanel;
 	private JTextField centerData;
 	private JPanel pointerPanel;
 	private JTextField pointerData;
 	private JPanel zoomPanel;
 	private JTextField zoomData;
 	
 	public DataPanel() {
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		this.setMaximumSize(new Dimension(1000, 100));
 		
 		centerPanel = new JPanel();
 		centerPanel.add(new JLabel("Map center:"));
 		centerData = new JTextField();
 		centerData.setEditable(false);
 //		centerData.setSize(new Dimension(200,20));
 		centerPanel.add(centerData);
 		this.add(centerPanel);
 		
 		pointerPanel = new JPanel();
 		pointerPanel.add(new JLabel("Current location:"));
 		pointerData = new JTextField();
 		pointerData.setEditable(false);
 //		pointerData.setSize(new Dimension(200,20));
 		pointerPanel.add(pointerData);
 		this.add(pointerPanel);
 		
 		zoomPanel = new JPanel();
 		zoomPanel.add(new JLabel("Zooming:"));
 		zoomData = new JTextField();
 		zoomData.setEditable(false);
 //		zoomData.setSize(new Dimension(200,20));
 		zoomPanel.add(zoomData);
 		this.add(zoomPanel);
 //		this.setMinimumSize(new Dimension(200,400));
 	}
 
 	@Override
 	public void update(Coord center, Coord pointer, int range, int maxRange, double width, double height) {
 //		System.out.println(center.x() + ", " + center.y());
 //		centerPanel.remove(centerData);
 		centerData.setText("(" + center.x() + ", " + center.y() + ")");
 //		centerPanel.add(centerData);
 		
 		int xPos, yPos;
 		if (width > height) {
 			xPos = (int)(pointer.x()*2*range*(1+(width-height)/height)/width - range*(width-height)/height - range + center.x());
 			yPos = (int)(pointer.y()*(-2)*range/height + range + center.y());
 //			System.out.println(xPos);
 //			System.out.println(yPos);
 		}
 		else {
 			xPos = (int)(pointer.x()*2*range/width - range + center.x());
 			yPos = (int)(pointer.y()*(-2)*range*(1+(height-width)/width)/height + range*(height-width)/width + range + center.y());
 //			System.out.println(xPos);
 //			System.out.println(yPos);
 		}
 //		System.out.println(xPos + ", " + yPos);
 //		pointerPanel.remove(pointerData);
 		pointerData.setText("(" + xPos + ", " + yPos + ")");
 //		pointerPanel.add(pointerData);
 		
 //		zoomPanel.remove(zoomData);
 		zoomData.setText(100*maxRange/range + "%");
 //		zoomPanel.add(zoomData);
 		
 		this.repaint();
 	}
 
 }
