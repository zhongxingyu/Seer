 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.ui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.net.URL;
 import java.io.IOException;
 import javax.swing.*;
 import javax.imageio.ImageIO;
 
 import org.nees.rpi.vis.viewer3d.*;
 import org.nees.rpi.vis.loaders.Loader;
 import org.nees.rpi.vis.loaders.CentralLoader;
 import org.nees.rpi.vis.*;
 import org.nees.rpi.vis.model.DVModel;
 import org.nees.rpi.vis.model.DVShape;
 import org.nees.rpi.vis.model.DVGroup;
 
 public class MainFrame extends VisFrame implements Viewer3DDisplayProxy
 {
 	private JPanel main3DPanel;
 
 	private XYChartPanelProxy chartProxy;
 
 	private JPanel sidePanel;
 
 	private SideInfoPanel infoPanel;
 
 	private ShapeInfoPanel shapeInfoBox;
 
 	private JPanel content3DTab;
 
 	private GeoVisTool geoVisTool = null;
 
 	private ModelBar modelBar = null;
 
 	private ArrayList<InteractionPanel> interactionPanels = new ArrayList<InteractionPanel>();
 	private InteractionPanel interactionPanel = null;
 
 	private VisLinkButton closeModelButton;
 
 	/** the model editor frame */
 	private EditorFrame editor = new EditorFrame();
 
 	public MainFrame(GeoVisTool geo, String strTitle)
 	{
 		super("mainwindow");
 
 		geoVisTool = geo;
 		MsgWindow.initInstance(this);
 		// add a listener to kill the application should the
 		// window be closed
 		addWindowListener(new WindowAdapter()
 		{
 			public void windowClosing(WindowEvent e)
 			{
 				geoVisTool.exitRequest();
 			}
 		});
 
 		this.setTitle(strTitle);
 		try
 		{
 			this.setIconImage(ImageIO.read(MainFrame.class.getResource("/images/frame-icon.png")));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 
 		content3DTab = new JPanel();
 		this.setContentPane(content3DTab);
 
 		content3DTab.setLayout(new BorderLayout());
 		main3DPanel = create3DPanel();
 		main3DPanel.setMinimumSize(new Dimension(640, 480));
 		main3DPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#CCCCCC")));
 
 		chartProxy = new XYChartPanelProxy();
 
 		shapeInfoBox = new ShapeInfoPanel();
 
 		sidePanel = new JPanel();
 		//sidePanel.setBackground(new Color(232, 232, 232));
 		sidePanel.setBackground(Color.WHITE);
 		sidePanel.setPreferredSize(new Dimension(240, 600));
 		sidePanel.setLayout(new BorderLayout());
 
 		infoPanel = new SideInfoPanel();
 		content3DTab.add(infoPanel, BorderLayout.WEST);
 
 		this.initPanels();
 	}
 
 	private JPanel create3DPanel()
 	{
 		JPanel panel = new JPanel();
 		panel.setOpaque(true);
 		panel.setSize(new Dimension(640,480));
 		panel.setPreferredSize(new Dimension(640,480));
 		panel.setBackground(Color.BLACK);
 		panel.setLayout(new BorderLayout());
 		return panel;
 	}
 
 	private void initPanels()
 	{
		JPanel panel3DParent = new JPanel();
		panel3DParent.setLayout(new BorderLayout());
		panel3DParent.add(main3DPanel, BorderLayout.CENTER);
		panel3DParent.add(sidePanel, BorderLayout.EAST);
 		modelBar = new ModelBar(geoVisTool, 640,20);
		panel3DParent.add(modelBar, BorderLayout.SOUTH);
 
 		JPanel interPanel = new JPanel();
 		interPanel.setLayout(new BorderLayout());
		interPanel.add(panel3DParent, BorderLayout.CENTER);
 		interPanel.add(chartProxy.getChartPanel(), BorderLayout.SOUTH);
 
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		mainPanel.add(interPanel, BorderLayout.CENTER);
 
 		mainPanel.add(infoPanel, BorderLayout.WEST);
 
 		content3DTab.add(mainPanel);
 	}
 
 	public ModelBar getTrialBar()
 	{
 		return modelBar;
 	}
 	//***************************************************************************
 	// INTERFACE IMPLEMENTATION - Start
 	public JFrame getApplicationFrame()
 	{
 		return this;
 	}
 	public JPanel getMain3DPanel()
 	{
 		return main3DPanel;
 	}
 	public JPanel getOrientation3DPanel()
 	{
 		return null;
 	}
 	public XYChartPanelProxy getXYChartProxy()
 	{
 		return chartProxy;
 	}
 	public ShapeInfoPanel getShapeInfoPanel()
 	{
 		return shapeInfoBox;
 	}
 	// INTERFACE IMPLEMENTATION - End
 	//***************************************************************************
 	//***************************************************************************
 	// FACADE TO COMMON INTERFACE TASKS - Start
 	public void displayShapeInfo(String title, String fields[], String values[])
 	{
 		shapeInfoBox.display(title, fields, values);
 	}
 
 	public void setActiveModel(DVModel model)
 	{
 		int index;
 		geoVisTool.setActiveModel(model);
 		index = geoVisTool.getModelIndex(model);
 		modelBar.setText(model.getName());
 		if (index >= 0)
 			replaceInteractionPanel(index);
 	}
 
 	public void processDataPlot(DVShape dvShape)
 	{
 		chartProxy.processDataObject(dvShape);
 	}
 	// FACADE TO COMMON INTERFACE TASKS - End
 	//***************************************************************************
 
 	private void loadModel(Loader loader)
 	{
 		MsgWindow.getInstance().showMsg("Loading Model ...");
 		DVModel model = loader.triggerLoad(MainFrame.this);
 
 		if (model != null)
 		{
 			processSelectableShapes(model);
 			geoVisTool.loadModel(model);
 
 			InteractionPanel newIPanel = new InteractionPanel(geoVisTool.getController3D(), model, sidePanel.getWidth());
 			interactionPanels.add(newIPanel);
 			setActiveModel(model);
 			if (geoVisTool.getNumModels() > 0)
 				closeModelButton.setEnabled(true);
 
 			infoPanel.updateModelList();
 		}
 
 		if (geoVisTool.getNumModels() == 1)
 			infoPanel.showModelDependentBoxes();
 
 		MsgWindow.getInstance().setVisible(false);
 	}
 
 	/**
 	 * Unloads the active model from the view and swiches the view either
 	 * to the original one it was at when the program first started, or
 	 * to the next open model.
 	 */
 	private void unloadActiveModel()
 	{
 		removeActiveInteractionPanel();
 		geoVisTool.unloadActiveModel();
 
 		//TODO remove only series for the closed model
 		chartProxy.removeAllSeries();
 		modelBar.reset();
 
 		if (geoVisTool.getNumModels() == 0)
 		{
 			closeModelButton.setEnabled(false);
 			infoPanel.hideModelDependentBoxes();
 		}
 		else
 			setActiveModel(geoVisTool.getModels().get(0));
 
 		infoPanel.updateModelList();
 	}
 
 	private void removeActiveInteractionPanel()
 	{
 		sidePanel.remove(interactionPanel);
 		interactionPanels.remove(interactionPanel);
 		interactionPanel = null;
 		sidePanel.revalidate();
 		sidePanel.repaint();
 	}
 
 	private void replaceInteractionPanel(int i)
 	{
 		if (interactionPanel != null)
 			sidePanel.remove(interactionPanel);
 
 		interactionPanel = interactionPanels.get(i);
 		sidePanel.add(
 						interactionPanel,
 						BorderLayout.CENTER);
 		sidePanel.revalidate();
 		sidePanel.repaint();
 	}
 
 	/**
 	 * A hack to make selectable shapes effective as such.
 	 * This should be refactored to be part of the main
 	 * application logic and not part of the UI logic.
 	 */
 	private void processSelectableShapes(DVModel model)
 	{
 		for (int i=0; i<model.getGroupCount(); i++)
 		{
 			DVGroup group = model.getGroup(i);
 			for (int j=0; j<group.size(); j++)
 			{
 				final DVShape dvShape = group.getShape(j);
 				if (dvShape.getShape3D() instanceof SelectableShape3D)
 				{
 					SelectableShape3D sShape3D = (SelectableShape3D) dvShape.getShape3D();
 					if (dvShape.hasMetaData())
 					{
 						Shape3DAction s3DPAction = new Shape3DAction()
 						{
 							public boolean execute(Object bindingObj, SelectableShape3D shape)
 							{
 								displayShapeInfo(
 												dvShape.getName(),
 												dvShape.getMetadataAttributes(),
 												dvShape.getMetadataValues());
 								return true;
 							}
 						};
 						sShape3D.setPassiveAction(s3DPAction);
 					}
 
 					if (dvShape.hasDataSeries())
 					{
 						Shape3DAction s3DAction = new Shape3DAction()
 						{
 							public boolean execute(Object bindingObj, SelectableShape3D shape)
 							{
 								processDataPlot(dvShape);
 								return true;
 							}
 						};
 						sShape3D.setSelectionAction(s3DAction);
 					}
 				}
 			}
 		}
 	}
 
 	class LoadModelThread extends Thread
 	{
 		Loader loader;
 		LoadModelThread(Loader loader)
 		{
 			super();
 			this.loader = loader;
 			this.setName("model-load");
 		}
 		public void run()
 		{
 			loadModel(loader);
 		}
 	}
 
 	class SideInfoPanel extends JPanel
 	{
 		JComponent lastMenuItem;
 		SpringLayout spring;
 		int internalWidth;
 		VisTitleLabel modelListTitle;
 		VisTitleLabel metaTitle;
 		VisTitleLabel otherTitle;
 		Dimension metaTitleDim;
 		Dimension metaBoxDim;
 		ModelListPanel modelListPanel;
 
 		public SideInfoPanel()
 		{
 			super();
 			setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 			spring = new SpringLayout();
 			setLayout(spring);
 			setOpaque(true);
 			setBackground(Color.WHITE);
 			setPreferredSize(new Dimension(200, 400));
 			internalWidth = 190;
 
 			VisTitleLabel menuTitle = new VisTitleLabel("Menu", internalWidth);
 			add(menuTitle);
 			//spring.putConstraint(SpringLayout.NORTH, menuTitle, 100, SpringLayout.NORTH, infoPanel);
 
 			JComponent lastComponent = menuTitle;
 			URL newimageURL = getClass().getResource("/images/new.png");
 			VisLinkButton newLinkButton = new VisLinkButton("New Model", newimageURL);
 			add(newLinkButton);
 			Action newAction = new AbstractAction()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					editor.loadNewModel();
 					editor.setVisible(true);
 				}
 			};
 			newLinkButton.addActionListener(newAction);
 			spring.putConstraint(SpringLayout.NORTH, newLinkButton, 5, SpringLayout.SOUTH, lastComponent);
 
 			lastComponent = newLinkButton;
 
 			Iterator loadersIter = AppSettings.getInstance().getLoaders().iterator();
 			while (loadersIter.hasNext())
 			{
 				final Loader loader = (Loader)loadersIter.next();
 				URL imageURL = getClass().getResource("/images/open.png");
 				VisLinkButton openLinkButton = new VisLinkButton(loader.getDisplayText(), imageURL);
 				add(openLinkButton);
 				Action openAction = new AbstractAction()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						new LoadModelThread(loader).start();
 					}
 				};
 				openLinkButton.addActionListener(openAction);
 				//spring.putConstraint(SpringLayout.WEST, openLinkButton, 0, SpringLayout.WEST, infoPanel);
 				spring.putConstraint(SpringLayout.NORTH, openLinkButton, 5, SpringLayout.SOUTH, lastComponent);
 				lastComponent = openLinkButton;
 			}
 
 			VisLinkButton openFromCentralButton =
 					new VisLinkButton("Open From NEESCentral", getClass().getResource("/images/neescentral-open.png"));
 			add(openFromCentralButton);
 			Action openCentralAction = new AbstractAction()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					new LoadModelThread(new CentralLoader()).start();
 				}
 			};
 			openFromCentralButton.addActionListener(openCentralAction);
 			spring.putConstraint(SpringLayout.NORTH, openFromCentralButton, 5, SpringLayout.SOUTH, lastComponent);
 			lastComponent = openFromCentralButton;
 
 			lastMenuItem = lastComponent;
 
 			URL closeImageURL = getClass().getResource("/images/close-file.png");
 			closeModelButton = new VisLinkButton("Close Model", closeImageURL);
 			closeModelButton.setEnabled(false);
 			add(closeModelButton);
 			spring.putConstraint(SpringLayout.NORTH, closeModelButton, 5, SpringLayout.SOUTH, lastComponent);
 			Action closeModelAction = new AbstractAction()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					unloadActiveModel();
 				}
 			};
 			closeModelButton.addActionListener(closeModelAction);
 
 			lastMenuItem = closeModelButton;
 
 			VisLinkButton tb = new VisLinkButton("Bugga Boo");
 		tb.addActionListener(
 				new AbstractAction()
 				{
 					public void actionPerformed(ActionEvent actionEvent)
 					{
 						MainFrame.this.showMessage("Booo!");
 					}
 				}
 		);
 		//add(tb);
 			//spring.putConstraint(SpringLayout.NORTH, tb, 5, SpringLayout.SOUTH, closeModelButton);
 			//lastMenuItem = tb;
 
 			//TODO Restore the "save as image" functionality
 			/*
 			VisLinkButton saveScreenShot = new VisLinkButton("Save 3D View as Image");
 			add(saveScreenShot);
 			spring.putConstraint(SpringLayout.NORTH, saveScreenShot, 5, SpringLayout.SOUTH, lastComponent);
 			Action saveScreenAction = new AbstractAction()
 			{
 				public void actionPerformed(ActionEvent e)
 				{
 					String lastDir = AppSettings.getInstance().getString("filebrowser.lastdir");
 					JFileChooser fc = new JFileChooser();
 					if (lastDir != null)
 						fc.setCurrentDirectory(new File(lastDir));
 					int returnval = fc.showOpenDialog(MainFrame.this);
 					if (returnval == JFileChooser.APPROVE_OPTION)
 					{
 						AppSettings.getInstance().setProperty(
 										"filebrowser.lastdir", fc.getCurrentDirectory().toString());
 						geoVisTool.getController3D().scheduleTakeSnapshot(fc.getSelectedFile().getAbsolutePath());
 					}
 				}
 			};
 			saveScreenShot.addActionListener(saveScreenAction);
 
 			lastMenuItem = saveScreenShot;
 			*/
 
 
 			modelListTitle = new VisTitleLabel("Open Models", internalWidth);
 			add(modelListTitle);
 			spring.putConstraint(SpringLayout.NORTH, modelListTitle, 20, SpringLayout.SOUTH, lastMenuItem);
 			modelListPanel =
 					new ModelListPanel(geoVisTool)
 					{
 						public void modelSelected(DVModel model)
 						{
 							setActiveModel(model);
 						}
 					};
 			add(modelListPanel);
 			spring.putConstraint(SpringLayout.NORTH, modelListPanel, 5, SpringLayout.SOUTH, modelListTitle);
 			lastMenuItem = modelListPanel;
 
 			metaTitle = new VisTitleLabel("Metadata", internalWidth);
 			add(metaTitle);
 			spring.putConstraint(SpringLayout.NORTH, metaTitle, 20, SpringLayout.SOUTH, lastMenuItem);
 			shapeInfoBox.setPreferredSize(new Dimension(internalWidth, 120));
 			add(shapeInfoBox);
 			spring.putConstraint(SpringLayout.NORTH, shapeInfoBox, 5, SpringLayout.SOUTH, metaTitle);
 
 			AboutPanel aboutPanel = new AboutPanel(internalWidth);
 			add(aboutPanel);
 			spring.putConstraint(SpringLayout.SOUTH, aboutPanel, 0, SpringLayout.SOUTH, this);
 
 			hideModelDependentBoxes();
 		}
 
 		void hideModelDependentBoxes()
 		{
 			modelListTitle.setVisible(false);
 			modelListPanel.setVisible(false);
 			metaTitle.setVisible(false);
 			shapeInfoBox.setVisible(false);
 		}
 
 		void showModelDependentBoxes()
 		{
 			modelListTitle.setVisible(true);
 			modelListPanel.setVisible(true);
 			metaTitle.setVisible(true);
 			shapeInfoBox.setVisible(true);
 			revalidate();
 			repaint();
 		}
 
 		void updateModelList()
 		{
 			modelListPanel.updateModelList();
 			revalidate();
 			repaint();
 		}
 	}
 }
