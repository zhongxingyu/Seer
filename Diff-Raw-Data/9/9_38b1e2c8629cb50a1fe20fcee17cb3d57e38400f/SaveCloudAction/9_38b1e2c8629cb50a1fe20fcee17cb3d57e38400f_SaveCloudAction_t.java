 /*
  File: SaveCloudAction.java
 
  Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
  
  Code written by: Layla Oesper
  Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
  
  This library is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this project.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cytoscape.csplugins.wordcloud;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 
 import cytoscape.Cytoscape;
 import cytoscape.util.CyFileFilter;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.util.FileUtil;
 import cytoscape.view.cytopanels.CytoPanel;
 
 public class SaveCloudAction extends CytoscapeAction
 {
 	//VARIABLES
 	
 	// Extensions for the new file
 	public static String SESSION_EXT = ".png";
 
 	
 	//CONSTRUCTORS
 	
 	/**
 	 * SaveCloudAction constructor.
 	 */
 	public SaveCloudAction()
 	{
 		super("Save Cloud Image");
 	}
 	
 	//METHODS
 	
 	/**
 	 * Method called when a Create Network Cloud action occurs.
 	 * 
 	 * @param ActionEvent - event created when choosing to save the image
 	 * of a cloud.
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		
 		//Open save dialog and get name of file
 		
 		String name; // file name
 
 		// Open Dialog to ask user the file name.
 		try {
 	
 			name = FileUtil.getFile("Save Current Cloud as PNG File", FileUtil.SAVE,
 			                        new CyFileFilter[] {  }).toString();
 		} catch (Exception exp) {
 			// this is because the selection was canceled
 			return;
 		}
 		if (!name.endsWith(SESSION_EXT))
 			name = name + SESSION_EXT;
 		
 		saveFile(name);
 	}
 	
 	
 	/**
 	 * Method that actually creates the file
 	 * @param name
 	 */
 	private void saveFile(String name)
 	{
 		//Retrieve current panel
 		CloudDisplayPanel panel = SemanticSummaryManager.getInstance().getCloudWindow();
 		String cloudName = SemanticSummaryManager.getInstance().getCurCloud().getCloudName();
 		
 		//Get Data panel??
 		CytoPanel cytoPanel = Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
 		Dimension curSize = cytoPanel.getSelectedComponent().getSize();
 		
 		/*
 		//Can I make it work with just the flow panel
 		//Gives exact copy of what is visible (includes scroll bars) - KEEPER
 		JScrollPane scroll = panel.cloudScroll;
 		JFrame frame = new JFrame(cloudName);
 		scroll.setPreferredSize(curSize);
 		frame.getContentPane().add(scroll);
 		scroll.revalidate();
 		frame.pack();
 		frame.setLocation(-100, -100);
 		frame.setVisible(true);
 		*/
 		
 		
 		//Can I make it work with just the flow panel
 		//Gives exact copy of what is visible (includes scroll bars) - KEEPER
 		JScrollPane scroll = panel.cloudScroll;
 		JFrame frame = new JFrame(cloudName);
 		
 		int scrollHeight = scroll.getSize().height;
 		JPanel flowPanel = panel.getTagCloudFlowPanel();
 		int flowHeight = flowPanel.getSize().height;
		flowHeight = flowHeight; // removed addition of 5 to make scroll bar disappear
 		
 		//System.out.println("Scroll: " + scrollHeight);
 		//System.out.println("Flow: " + flowHeight);
 		
 		int width = curSize.width;
 		
 		Dimension fullSize = new Dimension(width, flowHeight);
 		
 		
 		scroll.setPreferredSize(fullSize);
 		frame.getContentPane().add(scroll);
 		scroll.revalidate();
 		frame.pack();
 		frame.setLocation(-100, -100);
 		frame.setVisible(true);
 		
 		/*
 		//Gives exact copy of what is visible (includes scroll bars) - KEEPER
 		JFrame frame = new JFrame(cloudName);
 		panel.setPreferredSize(curSize);
 		frame.getContentPane().add(panel);
 		panel.revalidate();
 		frame.pack();
 		frame.setLocation(-100, -100);
 		frame.setVisible(true);
 		*/
 	
 		
 		
 		//Almost right width, adds scroll bars
 		/*
 		JFrame frame = new JFrame(cloudName);
 		frame.setPreferredSize(curSize);
 		frame.getContentPane().add(panel);
 		panel.revalidate();
 		frame.pack();
 		frame.setLocation(-100, -100);
 		frame.setVisible(true);
 		*/
 		
 		/*
 		panel.revalidate();
 		JPanel flowPanel = panel.getTagCloudFlowPanel();
 		JScrollPane scroll = panel.cloudScroll;
 		flowPanel.revalidate();
 		
 		Dimension size = flowPanel.getSize();
 		Dimension preferredSize = flowPanel.getPreferredSize();
 		JFrame frame = new JFrame(cloudName);
 		Insets frameInset = frame.getInsets();
 		Insets panelInset = panel.getInsets();
 		Insets scrollInset = scroll.getInsets();
 		int width = size.width + frameInset.left + frameInset.right + panelInset.left + panelInset.right + 25 + scrollInset.left + scrollInset.right;
 		int height = size.height + frameInset.top + frameInset.bottom + panelInset.top + panelInset.bottom + 30 + scrollInset.top + scrollInset.bottom;
 		//frame.setPreferredSize(new Dimension(width, height));
 		frame.getContentPane().add(panel);
 		frame.pack();
 		frame.setLocation(-100, -100);
 		frame.setVisible(true);
 		flowPanel.revalidate();
 		panel.revalidate();
 		size = flowPanel.getSize();
 		width = size.width + frameInset.left + frameInset.right + panelInset.left + panelInset.right + 25 + + scrollInset.left + scrollInset.right;
 		height = size.height + frameInset.top + frameInset.bottom + panelInset.top + panelInset.bottom + 30 + + scrollInset.top + scrollInset.bottom;
 		//frame.setPreferredSize(new Dimension(width, height));
 		flowPanel.revalidate();
 		panel.revalidate();
 		frame.pack();
 		*/
 		
 		/*
 		//Testing
 		Dimension size = panel.getPreferredSize();
 		//Dimension newSize = new Dimension(size.width + 40, size.height + 15);
 		//panel.setPreferredSize(newSize);
 		
 		JFrame frame = new JFrame(cloudName);
 		//frame.setSize(newSize);
 		frame.getContentPane().add(panel);
 		frame.pack();
 		frame.setLocation(-100, -100);
 		frame.setVisible(true);
 		
 		panel.revalidate();
 		frame.setPreferredSize(panel.getPreferredSize());
 		panel.revalidate();
 		*/
 		//Dimension imageSize = frame.getPreferredSize();
 		Dimension imageSize = frame.getSize();
 		
 		BufferedImage b = new BufferedImage(imageSize.width, imageSize.height ,BufferedImage.TYPE_INT_RGB); /* change sizes of course */
 		Graphics2D g = b.createGraphics();
 		frame.printAll(g);
 		try{ImageIO.write(b,"png",new File(name));}catch (Exception e) {}
 		
 		frame.dispose();
 		
 		//Trial - add back to regular display
 		panel.add(scroll, BorderLayout.CENTER);
 		
 		
 		new SemanticSummaryPluginAction().loadCloudPanel();
 		
 	}
 
 }
