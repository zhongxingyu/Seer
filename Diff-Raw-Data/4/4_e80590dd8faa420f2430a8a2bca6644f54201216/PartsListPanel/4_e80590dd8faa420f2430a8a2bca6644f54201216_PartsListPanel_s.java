 package manager.panel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import manager.util.ClickablePanel;
 import manager.util.ClickablePanelClickHandler;
 import manager.util.ListPanel;
 import manager.util.WhiteLabel;
 import Utils.Constants;
 import factory.PartType;
 
 public class PartsListPanel extends ListPanel<PartType> {
 	PartsListPanelHandler handler;
 	String buttonText;
 
 	public PartsListPanel(PartsListPanelHandler h) {
 		// by default, button text is set to delete
 		this(h, "delete");
 	}
 
 	public PartsListPanel(PartsListPanelHandler h, String bt) {
 		super();
 		handler = h;
 		itemList = (ArrayList<PartType>) Constants.DEFAULT_PARTTYPES.clone();
 
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setAlignmentX(LEFT_ALIGNMENT);
 		setBorder(Constants.PADDING);
 
 		buttonText = bt;
 
 		parseList();
 	}
 
 	public ArrayList<PartType> getItemList() {
 		return itemList;
 	}
 
 	public void parseList() {
 		panels.clear();
 		removeAll();
 		repaint();
 
 		for (PartType pt : itemList) {
 			ClickablePanel panel = new ClickablePanel(new PanelClickHandler(pt));
 			panel.setSize(350, 50);
 			panel.setBorder(Constants.MEDIUM_PADDING);
 			panel.setAlignmentX(0);
 
 			add(panel);
 
 			JLabel imageLabel = new JLabel(new ImageIcon(pt.getImage()));
 			imageLabel.setMinimumSize(new Dimension(50, 30));
 			imageLabel.setPreferredSize(new Dimension(50, 30));
 			imageLabel.setMaximumSize(new Dimension(50, 30));
 			panel.add(imageLabel);
 
 			WhiteLabel nameLabel = new WhiteLabel("Part: " + pt.getName());
 			nameLabel.setLabelSize(165, 30);
 			panel.add(nameLabel);
 
 			JButton deleteButton = new JButton(buttonText);
 			deleteButton.addActionListener(new PanelButtonClickHandler(pt));
 			panel.add(deleteButton);
 
 			// add padding
 			add(Box.createVerticalStrut(10));
 			panels.put(pt, panel);
 		}
 		validate();
 	}
 
 	public interface PartsListPanelHandler {
 		public void panelClicked(PartType pt);
 
 		public void buttonClicked(PartType pt);
 	}
 
 	private class PanelClickHandler implements ClickablePanelClickHandler {
 		PartType pt;
 
 		public PanelClickHandler(PartType pt) {
 			this.pt = pt;
 		}
 
 		@Override
 		public void mouseClicked() {
 			restoreColors();
 			handler.panelClicked(pt);
 			panels.get(pt).setColor(new Color(5, 151, 255));
 		}
 	}
 
 	private class PanelButtonClickHandler implements ActionListener {
 		PartType pt;
 
 		public PanelButtonClickHandler(PartType pt) {
 			this.pt = pt;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			restoreColors();
 			handler.buttonClicked(pt);
			if (pt != null && panels != null) panels.get(pt).setColor(new Color(150, 6, 6));
 		}
 	}
 }
