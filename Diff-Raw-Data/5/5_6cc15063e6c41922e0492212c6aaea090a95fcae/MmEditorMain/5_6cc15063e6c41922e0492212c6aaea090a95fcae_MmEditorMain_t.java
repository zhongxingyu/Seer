 import java.awt.*;
 import java.awt.event.AWTEventListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.print.Book;
 import java.awt.print.PageFormat;
 import java.awt.print.Paper;
 import java.awt.print.PrinterJob;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import mmFileManager.*;
 
 import java.util.*;
 
 import mmFileManager.MmFileSelector;
 
 import com.sun.jna.NativeLibrary;
 
 import mmAccordionMenu.*;
 import mmLanguage.MmLanguage;
 import mmMap.MmMapViewer;
 import mmPrintMarkers.MmPrintMarkers;
 
 public class MmEditorMain extends JFrame implements ActionListener,AWTEventListener{
 
 	/**
 	  */
 	 
 	private static final long serialVersionUID = 1L;
 	public JSplitPane splitPane;
 	public JPanel panelLeft,panelRight; 
 	public JScrollPane scrollPaneLeft,scrollPaneRight;
     public MmAccordionComponent leftAccordion;
     ArrayList<MmAccordionPanel> accordionPanels;
     ArrayList<String> labels = new ArrayList<String>();
     
     public int width;
     
     JPanel accordionPanel;
     
     JFrame frame=null;
     
     int lastFrame,currentFrame;
     
     JMenuBar menuBar;
     
     private MmAccordionMenu accordionMenu;
     
     MmMapViewer map;
     
     String urlLink,fileName;
     
     File dirFile=null,save_file=null;
     
     JFileChooser saveFile;
     
    
     
     JFrame window;
     
     String windowTitle="Minnesmark Editor",fileNameText ="Untitled - ";
     
     public int language=0;
     
     JMenu minnesmark,file,help,preferences,languages;
     
     JMenuItem newTrail,open,save,save_as,print,print_preview,about,exit,swedish,english,editorHelp;
 	
 	public MmEditorMain(Dimension dim)
 	{
 		 menuBar = new JMenuBar();
 		 addMenu();
 		 splitPane = new JSplitPane();
 	     splitPane.setAlignmentX(0);
 	     splitPane.setDividerLocation(410);
 	     accordionPanels = new ArrayList<MmAccordionPanel>();
 	     MmAccordionComponent.width = splitPane.getDividerLocation();
 	     width = splitPane.getDividerLocation();
 	     
 	     
 	     
 	     window = new JFrame();
 	     window.setSize(new Dimension(dim.width,dim.height));
 	     
 	     ImageIcon icon = new ImageIcon();
 	     
 	     
 	     
 	     accordionMenu = new MmAccordionMenu();
 	     
 	     accordionMenu.setMainWindow(window);
 	     
 	     accordionMenu.setFrame();
 	     
 	     addMainLedInterface();
 	     
 	     
 	     window.setJMenuBar(menuBar);
 	     window.setIconImage(new ImageIcon(getClass().getResource("/Icon.png")).getImage());
 	     window.setTitle(fileNameText+windowTitle);
 	     
 	     
 	     
 	    
 	     splitPane.setLeftComponent(scrollPaneLeft);
 	     
 	     splitPane.setAutoscrolls(true);
 	     splitPane.setRightComponent(scrollPaneRight);
 	     
 	  
 	     
 	     	     
 	     splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,new PropertyChangeListener(){
 	    	
 	    	public void propertyChange(PropertyChangeEvent e) {
 	    		
 	    		MmAccordionComponent.width = splitPane.getDividerLocation();
 	    		
 	    		for(int i=0;i<accordionPanels.size();i++)
 	    		{
 	    			accordionPanels.get(i).setWidth(splitPane.getDividerLocation());
 	    			accordionPanels.get(i).adjustLayout();
 	    			leftAccordion.addBar( "Trial Title",accordionPanels.get(i));
 	    		}
 	    		
 	    		/*leftAccordion.addBar( "One", MMAccordionComponent.getDummyPanel( "One",width ) );
 	    		leftAccordion.addBar( "Two", MMAccordionComponent.getDummyPanel( "Two",width ) );
 	    		leftAccordion.addBar( "Three", MMAccordionComponent.getDummyPanel( "Three",width ) );
 	    		leftAccordion.addBar( "Four", MMAccordionComponent.getDummyPanel( "Four",width ) );
 	    		leftAccordion.addBar( "Five", MMAccordionComponent.getDummyPanel( "Five",width ) );*/
 	    		//leftAccordion.setVisibleBar( 2 );
 	    		
 	    		//accordionPanel.setPreferredSize(new Dimension(splitPane.getDividerLocation(),accordionPanel.getHeight()));
 	    	}
 	    });
 	     
 	     
 	     window.add(splitPane);
 	   
 	     window.setVisible(true);
 	     
 	     map.setMainWindow(window);
 	     map.showDialog();
 	     
 	     addMmWindowListener();  
 	     
 	     
 	}
 	
 	
 	public void addMainLedInterface()
 	{
 		panelLeft = new JPanel();
 		//panelLeft.setLayout(new FlowLayout(FlowLayout.LEFT));
 		//panelLeft.setPreferredSize(new Dimension(splitPane.getDividerSize(),600));
 		
 		panelRight = new JPanel();
 		//java.awt.EventQueue.invokeLater(new Runnable() {
 			//public void run() {
 			  map = new MmMapViewer();
 			  map.setFileOpen(false);
 			  map.setLanguage(language);
 			  panelRight.setLayout(new FlowLayout());
 		      panelRight.add(map);
 			//}
 		//});	
 		     
 		      
 		    
 		
 		//panelRight.setPreferredSize(new Dimension(500,600));
 		      
 		//int width = splitPane.getLeftComponent().getPreferredSize().width;  
 		      
 		leftAccordion = new MmAccordionComponent(MmAccordionComponent.width-25,40);
 		
 		
 		
 				
 	    for(int i=0;i<accordionPanels.size();i++)
 	    {
 	    	leftAccordion.addBar( accordionPanels.get(i).barTitle,accordionPanels.get(i));	
 	    }
 		
 	    
 		//leftAccordion.addBar( "Trial Start Events", MmAccordionComponent.getDummyPanel( "Trial Start Events",width ) );
 		//leftAccordion.addBar( "Three", MMAccordionComponent.getDummyPanel( "Three",width ) );
 		//leftAccordion.addBar( "Four", MMAccordionComponent.getDummyPanel( "Four",width ) );
 		//leftAccordion.addBar( "Five", MMAccordionComponent.getDummyPanel( "Five",width ) );
 		
 		//leftAccordion.setVisibleBar( 0 );
 	    
 		//panelLeft.add(leftAccordion);
 	    
 	    JPanel accordionPanel = new JPanel();
 	    accordionPanel.setPreferredSize(new Dimension(375,window.getHeight()+250));
 	    /*accordionPanel.setBackground(new java.awt.Color(45, 183, 255));
 	    accordionPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 	    accordionPanel.setLayout(new javax.swing.BoxLayout(accordionPanel, javax.swing.BoxLayout.LINE_AXIS));*/
 	    
 	    accordionPanel.setBackground(new java.awt.Color(45, 183, 255));
 	    accordionPanel.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, true));
 	    accordionPanel.setLayout(new BoxLayout(accordionPanel,BoxLayout.LINE_AXIS));
         panelLeft.add(accordionPanel);
 	    
         accordionMenu.setMap(map);
         accordionMenu.setLanguage(language);
         
         //first menu
         accordionMenu.addMenu(MmLanguage.language[language][2],"search");
         accordionMenu.addTextFieldItem("search",":"+MmLanguage.language_button[language][0],25);
         //accordionMenu.addGeoFieldItem("search");
         accordionMenu.addButtonItem("search", MmLanguage.language_search[language][0], "ok");
         accordionMenu.addItems("search");
         accordionMenu.buttonActions("search");
         
         
         //second menu
         accordionMenu.addMenu(MmLanguage.language[language][3],"markers");
         ArrayList<JLabel> texts = new ArrayList<JLabel>();
         
         for(int i=0;i<14;i++)
         	texts.add(addLabel(MmLanguage.language_markers[language][i],"patt.marker"+Integer.toString(i+1),i));
         
         
         
         accordionMenu.markerDialog();
 	    accordionMenu.addLabelItems("markers",texts);
 	    accordionMenu.addButtonItem("markers", MmLanguage.language_button[language][1],"ok");
 	    accordionMenu.addButtonItem("markers", MmLanguage.language_button[language][2],"cancel");
 	    accordionMenu.addItems("markers");
 	    accordionMenu.buttonActions("markers");
 	    
        
 	    
 	    //third menu
 	    accordionMenu.addMenu(MmLanguage.language[language][4], "start");
 	    ArrayList<JLabel> texts1 = new ArrayList<JLabel>();
         texts1.add(addStartLabel(MmLanguage.language_startMedia[language][0],"text"));
         texts1.add(addStartLabel(MmLanguage.language_startMedia[language][1],"text"));
         texts1.add(addStartLabel(MmLanguage.language_startMedia[language][1],"text"));
         texts1.add(addStartLabel(MmLanguage.language_startMedia[language][1],"text"));
         texts1.add(addStartLabel("","text"));
         texts1.add(addStartLabel("","text"));
                 
         
 	    accordionMenu.addLabelItems("start",texts1);
 	    accordionMenu.addButtonItem("start", MmLanguage.language_button[0][1],"ok");
 	    accordionMenu.addButtonItem("start", MmLanguage.language_button[0][2],"cancel");
 	    accordionMenu.addItems("start");
 	    //accordionMenu.buttonActions("start");
 	    accordionMenu.startMenuButtonActions();
 	    
 	    
 	    accordionMenu.backgroundPaint("search",new Color(211, 242, 252));
         accordionMenu.backgroundPaint("markers",new Color(211, 242, 252));
         accordionMenu.backgroundPaint("start",new Color(211, 242, 252));
 	    
         
         accordionMenu.setForeground(Color.white);
         
         accordionMenu.setFont(new Font("monospaced", Font.PLAIN, 14));
 	    accordionPanel.add(accordionMenu);
 	    panelLeft.setOpaque(false);
 		scrollPaneLeft = new JScrollPane(panelLeft,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		scrollPaneLeft.setPreferredSize(new Dimension(500,600));
 		
 		
 		
 				
 		scrollPaneRight = new JScrollPane(panelRight,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		scrollPaneRight.setPreferredSize(new Dimension(500,600));
 		
 		
         JScrollBar hscrollBar = scrollPaneRight.getHorizontalScrollBar();
 		
 		hscrollBar.addAdjustmentListener(new AdjustmentListener() {
 
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				// TODO Auto-generated method stub
 				
 				/*System.out.println(" adjust value  "+e.getValue());
 				map.setScrollbarAdjustX(e.getValue());
 				map.drawPoints();*/
 				//map.repaint();
 			}
 			
 		});
 
 		
 		JScrollBar vscrollBar = scrollPaneRight.getVerticalScrollBar();
 		
 		vscrollBar.addAdjustmentListener(new AdjustmentListener() {
 
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 				// TODO Auto-generated method stub
 				
 				//System.out.println(" adjust value  "+e.getValue());
 				/*map.setScrollbarAdjustY(e.getValue());
 				map.drawPoints();*/
 				//map.repaint();
 			}
 			
 		});
 		
 		/*JOptionPane.showMessageDialog(null, menuBar.getX()+"  "+menuBar.getY());
        
 		Point pt = new Point(window.getX(),window.getY());
 		
 		SwingUtilities.convertPointToScreen(pt, window);
 		
 		JOptionPane.showMessageDialog(null, pt+"  "+menuBar.getBounds());*/
         		
 	}
 	
 	public void addMmWindowListener()
 	{
 		
         
 		window.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				// TODO Auto-generated method stub
 				//JOptionPane.showMessageDialog(null, event.getLocationOnScreen());
 				//map.bringTofront();
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent event) {
 				// TODO Auto-generated method stub
 				System.out.println("data "+event.getLocationOnScreen());
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		
 		
 		window.addWindowListener(new WindowListener(){
 
 			@Override
 			public void windowActivated(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void windowClosed(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
                 boolean isSaved = false;
 				
                 if(!map.getStations().isEmpty())
 				{
 					isSaved = map.getChangedState();
 					if(!isSaved)
 					{
 						accordionMenu.setMarkerstSavedState(false);
 						accordionMenu.setStartEventsSaved(false);
 					}
 					
 				}
 				
 				if(accordionMenu.getCurrentAtiveMarkersCount()!=0)
 				{
 					isSaved = accordionMenu.getMarkersSavedState();
 					if(!isSaved)
 				    	accordionMenu.setStartEventsSaved(false);
 				}
 				
 				
 				isSaved = accordionMenu.isStartEventsSaved();
 				
 				if(!isSaved)
 				{
 				
 					Object[] options = {MmLanguage.language_options[language][0],
 							MmLanguage.language_options[language][1],
 							MmLanguage.language_options[language][2]};
 					
 					ImageIcon icon = new ImageIcon(getClass().getResource("/exclamation.png"));
 					
 					int option = JOptionPane.showOptionDialog(null, MmLanguage.language_fileOptions[language][0], MmLanguage.language_fileOptions[language][1], JOptionPane.YES_NO_CANCEL_OPTION,0,icon,options,options[2]);
 					
 				   if(option==JOptionPane.OK_OPTION)
 				   {
 				       SaveFile();
 				    
 				       System.exit(0);
 				   }
 				   
 				   if(option==JOptionPane.NO_OPTION)
 				   {
 						System.exit(0);
 				   }
 				   
 				   if(option==JOptionPane.CANCEL_OPTION)
 				   {
 						window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 				   }
 				   
 				}
 				
 				
 				
 				
 				
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 					
 			
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent event) {
 				// TODO Auto-generated method stub
 				
 				
 				
 			}
 
 			@Override
 			public void windowIconified(WindowEvent event) {
 				// TODO Auto-generated method stub
 							
 				
 			}
 
 			@Override
 			public void windowOpened(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 	    	  
 	     });
 		
 		
 		
 		
 						
 	}
 	
 	@Override
 	public void eventDispatched(AWTEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 	
 	public JLabel addStartLabel(String text,String name)
 	{
 		JLabel lb = new JLabel(text);
 		lb.setName(name);
 		
 		return lb;
 	}
 	
 	public JLabel addLabel(String text,String name,int index)
 	{
 		JLabel lb = new JLabel(text);
 		lb.setName(name);
 		
 		File imgname = new File(System.getProperty("user.dir")+"/globalmarkers/pattern"+Integer.toString(index+1)+".png");
 		
 		Image img;
 		
 		
 		
 		try
 		{
 			img = ImageIO.read(imgname);
 			img = img.getScaledInstance(40, 40, 0);
 			
 			lb.setIcon(new ImageIcon(img));
 			
 		}
 		catch(Exception e) 
 		{
 			JOptionPane.showMessageDialog(null, "Marker Image not Found "+e);
 		}
 		
 		return lb;
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		//System.out.println("get data "+accordionPanels.size()+" "+currentFrame+"  "+lastFrame);
 		
 		ImageIcon imageIcon = new ImageIcon(getClass().getResource("/image_add.png"));
 		
 		for(int i=0;i<accordionPanels.size();i++)
 		{
 			if(e.getSource()==accordionPanels.get(i).getMinusButton())
 			{
 				  //accordionPanels.get(i).mouseEvent.removeComponent();
 				  accordionPanels.get(i).validate();
 				  panelLeft.validate();
 				  leftAccordion.validate();
 			}
 			
 			if(e.getSource()==accordionPanels.get(i).getPlusButton())
 			{
 				System.out.println("frame "+ frame);
 				currentFrame=i;
 				
 				if(frame==null)
 				{	
 				   frame = new JFrame("Minesmark Events");
 				   frame.setPreferredSize(new Dimension(400,400));
 				   frame.setAlwaysOnTop(true);
 				
 				   
 				   for(int j=0;j<2;j++)
 				   {
 					   if(j==0)			
 				 	   {
 						   labels.clear();
 						   //System.out.println("entered "+j);
 						   labels.add("Trial Title");
 						   labels.add("Url");
 						   //accordionPanels.get(i).eventsDialog = new MmAccordionTrialDialog(frame,labels,accordionPanels.get(i));
 						   accordionPanels.get(j).eventsDialog.setFrame(frame);
 						   accordionPanels.get(j).eventsDialog.setLabels(labels);
 						   
 						   if(i==0)
 						   {
 							   for(int x=0;x<accordionPanels.get(i).eventsDialog.componentPanel.getLabels().size();x++)
 							   {
 								  JLabel label = accordionPanels.get(i).eventsDialog.componentPanel.getLabels().get(x);
 								  JCheckBox checkBox = accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(x);
 								  label.setVisible(true);
 								  checkBox.setVisible(true);
 								  
 							   }
 						   }
 						   
 						   
 					   }
 					   
 					   if(j==1)
 					   {
 						   labels.clear();
 						   //System.out.println("entered "+j);
 						   labels.add("BackgroundImageLandscape");
 						   labels.add("BackgroundImagePortrait");
 						   labels.add("BackgroundAudio");
 						   labels.add("BackgroundVideo");
 						   //accordionPanels.get(i).eventsDialog = new MmAccordionTrialDialog(frame,labels,accordionPanels.get(i));
 						   accordionPanels.get(j).eventsDialog.setFrame(frame);
 						   accordionPanels.get(j).eventsDialog.setLabels(labels);
 						   
 						   if(i==1)
 						   {
 							   for(int x=0;x<accordionPanels.get(i).eventsDialog.componentPanel.getLabels().size();x++)
 							   {
 								  JLabel label = accordionPanels.get(i).eventsDialog.componentPanel.getLabels().get(x);
 								  JCheckBox checkBox = accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(x);
 								  label.setVisible(true);
 								  checkBox.setVisible(true);
 								  
 							   }
 						   }
 						   
 						   
 					   }   
 					   
 					   
 				   }
 				   
 				
 				   accordionPanels.get(i).eventsDialog.setOpaque(false);
 				   frame.setContentPane(accordionPanels.get(i).eventsDialog);
 				   frame.pack();
 				   frame.setVisible(true);
 				}
 				else
 				{	
 					if(lastFrame!=currentFrame)
 					{
 						 //System.out.println("enterd else part "+currentFrame+" "+lastFrame);	
 						 for(int j=0;j<accordionPanels.get(lastFrame).eventsDialog.componentPanel.getLabels().size();j++)
 						 {
 							  JLabel label = accordionPanels.get(lastFrame).eventsDialog.componentPanel.getLabels().get(j);
 							  JCheckBox checkBox = accordionPanels.get(lastFrame).eventsDialog.componentPanel.getCheckBoxes().get(j);
 							  //System.out.println("label text "+label.getName());
 							  label.setVisible(false);
 							  checkBox.setVisible(false);
 							  label=null;
 							  checkBox=null;
 						 }
 						 
 						 for(int j=0;j<accordionPanels.get(lastFrame).eventsDialog.componentPanel.getLabels().size();j++)
 						 {
 							  accordionPanels.get(lastFrame).eventsDialog.componentPanel.repaint();
 							  
 						 }
 						 
 						 for(int j=0;j<accordionPanels.get(currentFrame).eventsDialog.componentPanel.getLabels().size();j++)
 						 {
 							  accordionPanels.get(currentFrame).eventsDialog.componentPanel.repaint();
 							  
 						 }
 						 
 						 accordionPanels.get(currentFrame).eventsDialog.setOpaque(false);
 						 frame.setContentPane(accordionPanels.get(currentFrame).eventsDialog);
 						 
 						 
 						 for(int j=0;j<accordionPanels.get(currentFrame).eventsDialog.componentPanel.getLabels().size();j++)
 						 {
 							  //JLabel label = accordionPanels.get(currentFrame).eventsDialog.componentPanel.getLabels().get(j);
 							   //System.out.println("label text "+accordionPanels.get(currentFrame).eventsDialog.componentPanel.getLabels().get(j).getName());
 							   
 							  accordionPanels.get(currentFrame).eventsDialog.componentPanel.getLabels().get(j).setVisible(true);
 							  accordionPanels.get(currentFrame).eventsDialog.componentPanel.getCheckBoxes().get(j).setVisible(true);
 							  //accordionPanels.get(currentFrame).eventsDialog.componentPanel.validate();
 							  accordionPanels.get(currentFrame).eventsDialog.repaint();
 							  accordionPanels.get(currentFrame).eventsDialog.componentPanel.repaint();
 							  //System.out.println("entered data "+ accordionPanels.get(currentFrame).eventsDialog.componentPanel.getLabels().get(j).isVisible());
 							  //accordionPanels.get(currentFrame).eventsDialog.componentPanel.validate();
 							  //accordionPanels.get(currentFrame).eventsDialog.componentPanel.repaint();
 						 }
 				     }   
 				
 					SwingUtilities.updateComponentTreeUI(frame);
 					SwingUtilities.updateComponentTreeUI(this);
 					frame.validate();
 					frame.repaint();
 					frame.setVisible(true);
 					this.validate();
 					this.repaint();
 					
 					System.out.println(" frame count  "+frame.getComponentCount());
 				}	
 			}
 			
 			if(e.getSource()==accordionPanels.get(i).eventsDialog.cancelButton)
 			{
 				frame.dispose();
 				//frame.setVisible(false);
 				lastFrame=i;
 			}
 			
 			if(e.getSource()==accordionPanels.get(i).eventsDialog.okButton)
 			{
 				//for(int x=0;x<accordionPanels.size();x++)
 				{	
 					/*for(int j=0;j<accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().size();j++)
 					{	
 					     System.out.println("check boxs selected "+accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).isSelected());
 					}*/       
 				   
 				   for(int j=0;j<accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().size();j++)
 				   {	
 				       System.out.println(accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).isSelected());	
 					
 				      //JCheckBox checkbox = accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(i);
 				      if(accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).isSelected())
 				      {
 					      //System.out.println(accordionPanels.get(i).rows.get(j).getComponent().getName());
 					      System.out.println(accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).getName());
 					      if(i==0)
 					      {	  
 				             accordionPanels.get(i).addText(accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).getName(), 0, j);
 				             accordionPanels.get(i).addTextField( 1, j);
 				             accordionPanels.get(i).validate();
 				             accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).setSelected(false);
 				             
 					      }
 					      
 					      if(i==1)
 					      {	  
 				             accordionPanels.get(i).addText(accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).getName(), 0, j);
 				             accordionPanels.get(i).addText(imageIcon, 2, j);
 				             accordionPanels.get(i).validate();
 				             accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().get(j).setSelected(false);
 				             
 					      }
 				      }   
 				     
 				   }
 				}  
 				//accordionPanels.get(i).eventsDialog.componentPanel.getCheckBoxes().clear();
 				accordionPanels.get(i).eventsDialog.componentPanel.validate();
 				accordionPanels.get(i).eventsDialog.componentPanel.repaint();
 				frame.dispose();
 				lastFrame=i;
 				//frame.setVisible(false);
 			}
 		}
 	}
 	
 	
 	public void resetStartContent()
 	{
 		
 	    
 	}
 	
 	public void changeText()
 	{
 		
 		accordionMenu.setLanguage(language);
 		accordionMenu.setLanguageText(MmLanguage.language[language][2],"search");
 		accordionMenu.setLanguageText(MmLanguage.language[language][3],"markers");
 		accordionMenu.setLanguageText(MmLanguage.language[language][4],"start");
 		
 		accordionMenu.setSearchText(":"+MmLanguage.language_button[language][0]);
 		
 		ArrayList<String> markerTexts = new ArrayList<String>();
 		
 		for(int i=0;i<18;i++)
 			markerTexts.add(new String(MmLanguage.language_markers[language][i]));
 		
 		accordionMenu.setMarkersText(markerTexts);
 		
 		ArrayList<String> startTexts = new ArrayList<String>();
 		
 		startTexts.add(new String(MmLanguage.language_startMedia[language][0]));
 		startTexts.add(new String(MmLanguage.language_startMedia[language][1]));
 		startTexts.add(new String(MmLanguage.language_startMedia[language][1]));
 		startTexts.add(new String(MmLanguage.language_startMedia[language][1]));
 		
 		accordionMenu.setStartTexts(startTexts);
 		
 		file.setText(MmLanguage.language_menu[language][0]);
 		help.setText(MmLanguage.language_menu[language][1]);
 		
 		newTrail.setText(MmLanguage.language_menu[language][2]);
 		open.setText(MmLanguage.language_menu[language][3]);
 		save.setText(MmLanguage.language_menu[language][4]);
 		save_as.setText(MmLanguage.language_menu[language][5]);
 		print.setText(MmLanguage.language_menu[language][6]);
 		print_preview.setText(MmLanguage.language_menu[language][7]);
 		
 		about.setText(MmLanguage.language_menu[language][10]);
 		//preferences.setText(MmLanguage.language_menu[language][11]);
 		languages.setText(MmLanguage.language_menu[language][12]);
 		exit.setText(MmLanguage.language_menu[language][8]);
 		
 		editorHelp.setText(MmLanguage.language_menu[language][9]);
 		
 	
 		
 		swedish.setText(MmLanguage.language_menu_languages[language][0]);
 		english.setText(MmLanguage.language_menu_languages[language][1]);
 		
 		map.setLanguage(language);
 		map.setMarkerDialogText();
 		
 		
 		
 		
 	}
 	
 	public void addMenu()
 	{
 		
 		minnesmark = new JMenu("Minnesmark");
 		file = new JMenu(MmLanguage.language_menu[language][0]);
 		file.setMnemonic(KeyEvent.VK_F);
 		help = new JMenu(MmLanguage.language_menu[language][1]);
 		help.setMnemonic(KeyEvent.VK_H);
 		
 		
 		
 		menuBar.add(minnesmark);
 		menuBar.add(file);
 		menuBar.add(help);
 		
 		about = new JMenuItem(MmLanguage.language_menu[language][10]);
 		//preferences = new JMenu(MmLanguage.language_menu[language][11]);
 		languages = new JMenu(MmLanguage.language_menu[language][12]);
 		exit = new JMenuItem(MmLanguage.language_menu[language][8],KeyEvent.VK_E);
 		
 		minnesmark.add(about);
 		//minnesmark.add(preferences);
 		minnesmark.add(languages);
 		minnesmark.add(exit);
 		
 		
 		
 		//preferences.add(languages);
 		
 		swedish = new JMenuItem(MmLanguage.language_menu_languages[language][0]);
 		english = new JMenuItem(MmLanguage.language_menu_languages[language][1]);
 		
 		languages.add(swedish);
 		languages.add(english);
 		
 		swedish.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				language = 0;
 				changeText();
 			}
 			
 		});
 		
         english.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				language = 1;
 				changeText();
 				
 			}
 			
 		});
         
         
         exit.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				System.exit(0);
 				
 			}
 			
 		});
 		
 		
 		newTrail = new JMenuItem(MmLanguage.language_menu[language][2],KeyEvent.VK_N);
 		open  = new JMenuItem(MmLanguage.language_menu[language][3],KeyEvent.VK_O);
 		save  = new JMenuItem(MmLanguage.language_menu[language][4],KeyEvent.VK_S);
 		save_as  = new JMenuItem(MmLanguage.language_menu[language][5]);
 		print  = new JMenuItem(MmLanguage.language_menu[language][6],KeyEvent.VK_P);
 		print_preview  = new JMenuItem(MmLanguage.language_menu[language][7]);
 		exit  = new JMenuItem(MmLanguage.language_menu[language][8],KeyEvent.VK_E);
 		
 	
 						
 		file.add(newTrail);
 		file.add(open);
 		file.add(save);
 		file.add(save_as);
 		file.add(print);
 		file.add(print_preview);
 		file.add(exit);
 		
 		/*file.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			
 		});
 		
 		
 		file.addMenuListener(new MenuListener() {
 
 			@Override
 			public void menuCanceled(MenuEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 
 			@Override
 			public void menuDeselected(MenuEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 
 			@Override
 			public void menuSelected(MenuEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 			
 		});
 		
 		help.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			
 		});
 		
 		
 		help.addMenuListener(new MenuListener() {
 
 			@Override
 			public void menuCanceled(MenuEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 
 			@Override
 			public void menuDeselected(MenuEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 
 			@Override
 			public void menuSelected(MenuEvent arg0) {
 				// TODO Auto-generated method stub
 				map.bringTofront();
 			}
 			
 		}); */
 		
 		newTrail.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				/*if(!map.geoPos.isEmpty())
 				{
 					if(map.getSavedState())
 					{	
 						accordionMenu.clearContent();
 						accordionMenu.getStartEvents().imageEvents.clear();
 						accordionMenu.getStartEvents().audioEvents.clear();
 						accordionMenu.getStartEvents().videoEvents.clear();
 						accordionMenu.getStartEvents().messageEvents.clear();
 					    map.resetMapContents();
 					}    
 					else
 					{
 						int option = JOptionPane.showConfirmDialog(null, "kankä spara file", "Spara", JOptionPane.YES_NO_CANCEL_OPTION);
 						if(option==JOptionPane.OK_OPTION)
 						{	
 						    JFileChooser saveFile = new JFileChooser();
 						    int saveOption = saveFile.showSaveDialog(window);
 						    map.hideStationEventWindow();
 						    if(saveOption == JFileChooser.APPROVE_OPTION)
 						    {
 						    	map.showStationEventWindow();
 						       //saveFile.setCurrentDirectory(null);
 						       //JOptionPane.showMessageDialog(window, saveFile.getSelectedFile().getName()+" "+saveFile.getSelectedFile().getPath());			
 						       map.createJSONFile(saveFile.getSelectedFile().toString(),saveFile.getSelectedFile().getPath(),accordionMenu.getGlobalMarkerEvents(),accordionMenu.getStartEvents());
 						       map.setSaved(true);
 						       accordionMenu.clearContent();
 						       resetStartContent();
 							   accordionMenu.getStartEvents().imageEvents.clear();
 							   accordionMenu.getStartEvents().audioEvents.clear();
 							   accordionMenu.getStartEvents().videoEvents.clear();
 							   accordionMenu.getStartEvents().messageEvents.clear();
 						       map.resetMapContents();
 						    }   
 						    
 						    
 						}
 						
 						if(option==JOptionPane.NO_OPTION)
 						{
 							//accordionMenu.getGlobalMarkerEvents().clear();
 							accordionMenu.clearContent();
 							resetStartContent();
 							accordionMenu.getStartEvents().imageEvents.clear();
 							accordionMenu.getStartEvents().audioEvents.clear();
 							accordionMenu.getStartEvents().videoEvents.clear();
 							accordionMenu.getStartEvents().messageEvents.clear();
 						    map.resetMapContents();
 						}
 						
 					}
 				}*/
 				
 			   //JOptionPane.showMessageDialog(null, map.getSavedState());
 				
 				boolean isSaved = false;
 				
 				if(!map.getStations().isEmpty())
 				{
 					isSaved = map.getChangedState();
 					if(!isSaved)
 					{
 						accordionMenu.setMarkerstSavedState(false);
 						accordionMenu.setStartEventsSaved(false);
 					}
 					
 				}
 				
 				if(accordionMenu.getCurrentAtiveMarkersCount()!=0)
 				{
 					isSaved = accordionMenu.getMarkersSavedState();
 					
 					if(!isSaved)
 					{	
 				    	accordionMenu.setStartEventsSaved(false);
 				    	
 					} 	
 				}
 				
 				isSaved = accordionMenu.isStartEventsSaved();
 				
 				
 				
 				if(isSaved)
 				{	
 					if(!accordionMenu.getGlobalMarkerEvents().isEmpty())
 					{
 						for(int i=0;i<accordionMenu.getGlobalMarkerEvents().size();i++)
 						{
 							if(accordionMenu.getGlobalMarkerEvents().get(i).getNumberOfEvents()!=0)
 							    accordionMenu.getGlobalMarkerEvents().get(i).clearContent();
 						}
 					}
 				    accordionMenu.clearContent();   
 				    accordionMenu.getStartEvents().clearContent();
 				    accordionMenu.setSearchText(":"+MmLanguage.language_search[language][0]);
 				    map.resetMapContents();
 				    window.setTitle("Untitled - "+windowTitle);
 				    map.setSaved(false);
 				    accordionMenu.setMarkerstSavedState(false);
 					accordionMenu.setStartEventsSaved(false);
 				}
 				else
 				{	
 					Object[] options = {MmLanguage.language_options[language][0],
 							MmLanguage.language_options[language][1],
 							MmLanguage.language_options[language][2]};
 					
 				    ImageIcon icon = new ImageIcon(getClass().getResource("/exclamation.png"));
 				
 					int option = JOptionPane.showOptionDialog(null, MmLanguage.language_fileOptions[language][0], MmLanguage.language_fileOptions[language][1], JOptionPane.YES_NO_CANCEL_OPTION,0,icon,options,options[2]);
 					
 				    if(option==JOptionPane.OK_OPTION)
 				    {
 				       SaveFile();
 				       accordionMenu.clearContent();
 					   accordionMenu.setSearchText(":"+MmLanguage.language_search[language][0]);
 				       map.resetMapContents();
 				       window.setTitle("Untitled - "+windowTitle);
 				       map.setSaved(false);
 				       dirFile = null;
 				       save_file = null;
 				    }	
 				
 				    if(option==JOptionPane.NO_OPTION)
 				    {  
 				 	   //accordionMenu.getGlobalMarkerEvents().clear();
 					   accordionMenu.clearContent();
 					   resetStartContent();
 					   accordionMenu.setSearchText(":"+MmLanguage.language_search[language][0]);
 				       map.resetMapContents();
 				       window.setTitle("Untitled - "+windowTitle);
 				       map.setSaved(false);
 				       dirFile = null;
 				       save_file = null;
 				       
 				    }
 				}    
 			}
 			
 		});
 		
 		open.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				//map.hideStationEventWindow();
 				
 				/*final VFSJFileChooser vfs1 = new VFSJFileChooser();
 			     
 			     vfs1.setAccessory(new DefaultAccessoriesPanel(vfs1));
 			     vfs1.setFileHidingEnabled(false);
 			     vfs1.setMultiSelectionEnabled(false);
 			     vfs1.setFileSelectionMode(SELECTION_MODE.FILES_AND_DIRECTORIES);
 			     
 			    
 			        
 			     vfs1.addChoosableFileFilter(new MmFileFilter());
 			     
 			     vfs1.setFileFilter(new MmFileFilter());
 
 			     // show the file dialog
 			     RETURN_TYPE answer = vfs1.showOpenDialog(null);*/
 				
 				
                 boolean isSaved = false;
 				
 				if(!map.getStations().isEmpty())
 				{
 					isSaved = map.getChangedState();
 					if(!isSaved)
 					{
 						accordionMenu.setMarkerstSavedState(false);
 						accordionMenu.setStartEventsSaved(false);
 					}
 					
 				}
 				
 				
 				
 				if(accordionMenu.getCurrentAtiveMarkersCount()!=0)
 				{
 					isSaved = accordionMenu.getMarkersSavedState();
 					if(!isSaved)
 				    	accordionMenu.setStartEventsSaved(false);
 				}
 				
 				
 				
 				isSaved = accordionMenu.isStartEventsSaved();
 				
 				
 				
 				if(isSaved && map.isFileOpen())
 				{
 					map.resetMapContents();
 					if(!accordionMenu.getGlobalMarkerEvents().isEmpty())
 					{
 						for(int i=0;i<accordionMenu.getGlobalMarkerEvents().size();i++)
 						{
 							accordionMenu.getGlobalMarkerEvents().get(i).clearContent();
 						}
 					}
 				    accordionMenu.clearContent();   
 				    accordionMenu.getStartEvents().clearContent();
 				    accordionMenu.setSearchText(":"+MmLanguage.language_search[language][0]);
 				    map.setSaved(true);
 				    accordionMenu.setMarkerstSavedState(true);
 				    accordionMenu.setStartEventsSaved(true);
 				    dirFile = null;
 				    save_file = null;
 				}
 				
 				if(!isSaved)
 				{
 					Object[] options = {MmLanguage.language_options[language][0],
 							MmLanguage.language_options[language][1],
 							MmLanguage.language_options[language][2]};
 					
 					ImageIcon icon = new ImageIcon(getClass().getResource("/exclamation.png"));
 					
 					int option = JOptionPane.showOptionDialog(null, MmLanguage.language_fileOptions[language][0], MmLanguage.language_fileOptions[language][1], JOptionPane.YES_NO_CANCEL_OPTION,0,icon,options,options[2]);
 					
 				    if(option==JOptionPane.OK_OPTION)
 				    {
 				       SaveFile();
 				    
 				       map.resetMapContents();
 				       
 				       map.setSaved(true);
 				       accordionMenu.setMarkerstSavedState(true);
 				       accordionMenu.setStartEventsSaved(true);
 				       dirFile = null;
 				       save_file = null;
 				    }
 				    
 				    if(option==JOptionPane.NO_OPTION)
 				    {  
 				 	   //accordionMenu.getGlobalMarkerEvents().clear();
 					   accordionMenu.clearContent();
 					   resetStartContent();
 					   accordionMenu.getStartEvents().clearContent();
 				       map.resetMapContents();
 				       
 				       JFileChooser openFile = new JFileChooser();
 						
 					   openFile.setAcceptAllFileFilterUsed(false);
 						
 						
 						
 					   FileNameExtensionFilter filter = new FileNameExtensionFilter(
 						        "JSON files", "json");
 						
 						openFile.addChoosableFileFilter(filter);
 						openFile.setFileFilter(filter);
 						
 						option = openFile.showOpenDialog(null);
 						
 					    if(option == JFileChooser.APPROVE_OPTION)
 					    {
 					    	
 					    	resetStartContent();
 						    accordionMenu.getStartEvents().clearContent();
 							
 							
 					    	map.resetMapContents();
 						    map.showStationEventWindow();
 					        map.readJSONFileContents(openFile.getSelectedFile().getAbsolutePath(),accordionMenu);
 					        map.setFileOpen(true);
 					        
 					        File file = new File(openFile.getSelectedFile().toString());
 							
 							String fileName1 = file.getName();
 							
 							//JOptionPane.showMessageDialog(null, fileName1);
 							
 							int fileIndex = fileName1.indexOf(".");
 							
 							fileName1 = fileName1.substring(0, fileIndex);
 					        
 							dirFile = new File(openFile.getSelectedFile().getParent());
 							save_file = new File(openFile.getSelectedFile().getAbsolutePath());
 					        
 					        fileNameText = openFile.getSelectedFile().getName() +" - ";
 					        window.setTitle(fileNameText+windowTitle);
 					        map.setSaved(true);
 					        accordionMenu.setMarkerstSavedState(true);
 					        accordionMenu.setStartEventsSaved(true);
 					    }    
 					
 					    if(option == JFileChooser.CANCEL_OPTION)
 					    {
 					    	
 					    	if(!map.isFileOpen())
 					    	{
 					    	
 						        map.showStationEventWindow();
 					    	}    
 					    }
 				    }
 					
 				}
 				else
 				{
 					
 					JFileChooser openFile = new JFileChooser();
 					
 					openFile.setAcceptAllFileFilterUsed(false);
 					
 					
 					
 					FileNameExtensionFilter filter = new FileNameExtensionFilter(
 					        "JSON files", "json");
 					
 					openFile.addChoosableFileFilter(filter);
 					openFile.setFileFilter(filter);
 					
 					int option = openFile.showOpenDialog(null);
 					
 				    if(option == JFileChooser.APPROVE_OPTION)
 				    {
 				    	map.resetMapContents();
 					    map.showStationEventWindow();
 					    accordionMenu.clearContent();
 					    accordionMenu.getStartEvents().clearContent();
 				        map.readJSONFileContents(openFile.getSelectedFile().getAbsolutePath(),accordionMenu);
 				        map.setFileOpen(true);
 				        map.setSaved(true);
 				        accordionMenu.setMarkerstSavedState(true);
 				        accordionMenu.setStartEventsSaved(true);
 				        File file = new File(openFile.getSelectedFile().getAbsolutePath());
 						
 						String fileName1 = file.getName();
 						
 						//JOptionPane.showMessageDialog(null, fileName1);
 						
 						int fileIndex = fileName1.indexOf(".");
 						
 						fileName1 = fileName1.substring(0, fileIndex);
 				        
 				        dirFile = new File(openFile.getSelectedFile().getParent());
 						save_file = new File(openFile.getSelectedFile().getAbsolutePath());
 
 						
 				        
 						fileNameText = openFile.getSelectedFile().getName()+" - ";
 				        window.setTitle(fileNameText+windowTitle);
 				        
 				        
 				    }    
 				
 				    if(option == JFileChooser.CANCEL_OPTION)
 				    {
 				    	
 				    	if(!map.isFileOpen())
 				    	{
 				    	
 					        map.showStationEventWindow();
 				    	}    
 				    }
 				}    
 				
 				
 			}
 			
 		});
 		
 		save.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				SaveFile();
 				
 			}
 			
 		});
 		
 		save_as.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 				map.hideStationEventWindow();
 				
                 saveFile = new JFileChooser();
                 
                 saveFile.setDialogTitle(MmLanguage.language_menu[language][5]);
                 
 				
 				saveFile.setAcceptAllFileFilterUsed(false);
 				
 				FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				        "JSON files", "json");
 				
 				saveFile.addChoosableFileFilter(filter);
 				saveFile.setFileFilter(filter);
 				
 				int option = saveFile.showSaveDialog(null);
 				
 				//JOptionPane.showMessageDialog(null, "file name "+saveFile.getSelectedFile().getName());
 				
 				
 				
 				if(option == JFileChooser.APPROVE_OPTION)
 				{	
 					
 					map.showStationEventWindow();
 					fileName = saveFile.getSelectedFile().toString();
 					
 					
 					if(!fileName.contains(".json"))
 					{	
 						fileName = fileName+".json";
 						
 					}	
 					
 					
 					
 					File file = new File(fileName);
 					
 					String fileName1 = file.getName();
 					
 					//JOptionPane.showMessageDialog(null, fileName1);
 					
 					int fileIndex = fileName1.indexOf(".");
 					
 					fileName1 = fileName1.substring(0, fileIndex);
 					
 					fileNameText = file.getName()+" - ";
 			        window.setTitle(fileNameText+windowTitle);
 					
 					
 					//JOptionPane.showMessageDialog(null, "selected file "+saveFile.getSelectedFile()+"  "+file.getName());
 					
 					
 					dirFile = new File(saveFile.getSelectedFile().getParent()+"/"+fileName1);
 					
 					save_file = new File(saveFile.getSelectedFile()+"/"+file.getName());
 					saveFile.setSelectedFile(save_file);
 					
 					//JOptionPane.showMessageDialog(null, saveFile.getSelectedFile()+"  "+dirFile.isDirectory()+"  "+dirFile.getName()+" "+save_file.exists()+"  "+save_file.getPath());
 					
 					if(dirFile.isDirectory() && save_file.exists())
 					{
 						//File jsonFile = new File(dirFile.getName());
 						int fileOption = JOptionPane.showConfirmDialog(null, MmLanguage.language_fileOptions[language][2],MmLanguage.language_fileOptions[language][1] ,JOptionPane.YES_NO_OPTION);
 						if(fileOption == JOptionPane.YES_OPTION)
 						{
 							map.createJSONFile(save_file.getPath(),(dirFile.getAbsolutePath().toString()),accordionMenu.getGlobalMarkerEvents(),accordionMenu.getStartEvents());
 						    
 						}
 					}
 					else
 					{
 						if(!dirFile.isDirectory())
 						      dirFile.mkdir();
 						map.createJSONFile(save_file.getPath(),(dirFile.getAbsolutePath().toString()),accordionMenu.getGlobalMarkerEvents(),accordionMenu.getStartEvents());
 					    
 					}
 					
 					map.setSaved(true);
 					accordionMenu.setMarkerstSavedState(true);
 				    accordionMenu.setStartEventsSaved(true);
 									   
 				}   
 				
 				
 				
 				if(option == JFileChooser.CANCEL_OPTION)
 				{	
 					
 					map.showStationEventWindow();
 				}
 				
 				
 			}
 			
 		});
 		
 		print.addActionListener(new ActionListener(){
 			
 			public void actionPerformed(ActionEvent event)
 			{
 				
 				//accordionMenu.printMarker();
 				if(map.getStations().isEmpty() && accordionMenu.getGlobalMarkerEvents().isEmpty())
 				{
 					JOptionPane.showMessageDialog(null, MmLanguage.language_printException[language][0]);
 				}
 				else
 					print();
 				
 			}	
 		});
 		
 		print_preview.addActionListener(new ActionListener(){
 			
 			public void actionPerformed(ActionEvent event)
 			{
 				JOptionPane.showMessageDialog(map, map);
 			}
 		});
 		
 		exit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				if(map.getChangedState())
 				{	
 					accordionMenu.clearContent();
 					accordionMenu.getStartEvents().clearContent();
 					System.exit(0);
 				}    
 				else
 				{
 					Object[] options = {MmLanguage.language_options[language][0],
 							MmLanguage.language_options[language][1],
 							MmLanguage.language_options[language][2]};
 					
 					ImageIcon icon = new ImageIcon(getClass().getResource("/exclamation.png"));
 					
 					int option = JOptionPane.showOptionDialog(null, MmLanguage.language_fileOptions[language][0], MmLanguage.language_fileOptions[language][1], JOptionPane.YES_NO_CANCEL_OPTION,0,icon,options,options[2]);
 					if(option==JOptionPane.OK_OPTION)
 					{	
 					    JFileChooser saveFile = new JFileChooser();
 					    int saveOption = saveFile.showSaveDialog(window);
 					    map.hideStationEventWindow();
 					    if(saveOption == JFileChooser.APPROVE_OPTION)
 					    {
 					    	map.bringTofront();
 					       //saveFile.setCurrentDirectory(null);
 					       //JOptionPane.showMessageDialog(window, saveFile.getSelectedFile().getName()+" "+saveFile.getSelectedFile().getPath());			
 					       map.createJSONFile(saveFile.getSelectedFile().toString(),saveFile.getSelectedFile().getPath(),accordionMenu.getGlobalMarkerEvents(),accordionMenu.getStartEvents());
 					       map.setSaved(true);
 					       accordionMenu.clearContent();
 					       resetStartContent();
 						   accordionMenu.getStartEvents().clearContent();
 						   System.exit(0);
 					    }   
 					    
 					    
 					}
 					
 					if(option==JOptionPane.NO_OPTION)
 					{
 						//accordionMenu.getGlobalMarkerEvents().clear();
 						accordionMenu.clearContent();
 						resetStartContent();
 						accordionMenu.getStartEvents().clearContent();
 						
 					    System.exit(0);
 					}
 					
 				}
 				
 			}
 			
 		});
 		
 		
 		editorHelp = new JMenuItem(MmLanguage.language_menu[language][9],KeyEvent.VK_D);
 		
 		help.add(editorHelp);
 		
 		editorHelp.addActionListener(new ActionListener() {
 			
 			
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				//window.setState(JFrame.ICONIFIED);
 				
 				try
 				{
 				   Desktop.getDesktop().browse(new URI("http://www.ida.liu.se/~nagta58/MinnesmarkDemo/Minnesmark.html"));
 				}
 				catch(Exception e)
 				{
 					
 				}
 			}
 		});
 		
 	}
 	
 	public void SaveFile()
 	{
 		map.hideStationEventWindow();
 		
 		String fileName1;
 		
 		
 		
 		if(!map.getSavedState())
 		{		
            saveFile = new JFileChooser();
 		
 		   saveFile.setAcceptAllFileFilterUsed(false);
 		
 		   FileNameExtensionFilter filter = new FileNameExtensionFilter(
 		        "JSON files", "json");
 		
 		   saveFile.addChoosableFileFilter(filter);
 		   saveFile.setFileFilter(filter);
 		
 		   int option = saveFile.showSaveDialog(null);
 		
 		//JOptionPane.showMessageDialog(null, "file name "+saveFile.getSelectedFile().getName());
 		
 		
 		   if(option == JFileChooser.APPROVE_OPTION)
 		   {	
 			 map.showStationEventWindow();
 			 fileName = saveFile.getSelectedFile().toString();
 			 
 			 if(!fileName.contains(".json"))
 			 {	
 				fileName = fileName+".json";
 			 		
 			 }	
 				
 				File file = new File(fileName);
 				
 				fileName1 = file.getName();
 				
 				//JOptionPane.showMessageDialog(null, fileName1);
 				
 				int fileIndex = fileName1.indexOf(".");
 				
 				fileName1 = fileName1.substring(0, fileIndex);
 				
 				
 				//JOptionPane.showMessageDialog(null, "selected file "+saveFile.getSelectedFile()+"  "+file.getName());
 				
 				
 				dirFile = new File(saveFile.getSelectedFile().getParent()+"/"+fileName1);
 				
 				
 				
 				save_file = new File(saveFile.getSelectedFile()+"/"+file.getName());
 				saveFile.setSelectedFile(save_file);
 				
 				
 				fileNameText = file.getName()+" - ";
 		        window.setTitle(fileNameText+windowTitle);
 		   }
 		   
 		   if(option == JFileChooser.CANCEL_OPTION)
 		   {	
 				
 				map.showStationEventWindow();
 				window.setTitle("Untitled - "+windowTitle);
 		   }
 		}  
 						
 			
 			
 			//JOptionPane.showMessageDialog(null, saveFile.getSelectedFile()+"  "+dirFile.isDirectory()+"  "+dirFile.getName()+" "+save_file.exists()+"  "+save_file.getPath());
 			
 			
 			
 			if(dirFile.isDirectory() && save_file.exists() && !map.getSavedState())
 			{
 				//File jsonFile = new File(dirFile.getName());
 				int fileOption = JOptionPane.showConfirmDialog(null, MmLanguage.language_fileOptions[language][2],MmLanguage.language_fileOptions[language][1] ,JOptionPane.YES_NO_OPTION);
 				if(fileOption == JOptionPane.YES_OPTION)
 				{
 					map.createJSONFile(save_file.getPath(),(dirFile.getAbsolutePath().toString()),accordionMenu.getGlobalMarkerEvents(),accordionMenu.getStartEvents());
 				    map.setSaved(true);
 				    accordionMenu.setMarkerstSavedState(true);
 				    accordionMenu.setStartEventsSaved(true);
 				}
 			}
 			else 
 			{
 				if(!dirFile.isDirectory())
 				      dirFile.mkdir();
 				map.createJSONFile(save_file.getPath(),(dirFile.getAbsolutePath().toString()),accordionMenu.getGlobalMarkerEvents(),accordionMenu.getStartEvents());
 			    map.setSaved(true);
 			    accordionMenu.setMarkerstSavedState(true);
 			    accordionMenu.setStartEventsSaved(true);
 			}
 							   
 		}   
 		
 		
 	
 	
 	public void print()
 	{
 		PrinterJob printer = PrinterJob.getPrinterJob();
 	    //printer.setPrintable(this);
 	    boolean ok = printer.printDialog();
 	    	
 	    if(ok)
 	    {
 	    	try
 	    	{
 	    		PageFormat pPageFormat = printer.defaultPage();
 	    		Paper pPaper = pPageFormat.getPaper();
 	    		pPaper.setImageableArea(1.0, 1.0, pPaper.getWidth(), pPaper.getHeight());
 	    		pPageFormat.setPaper(pPaper);
 	    		pPageFormat = printer.pageDialog(pPageFormat);
 	    		Book pBook = new Book();
 	    		if(!map.getStations().isEmpty() || !accordionMenu.getGlobalMarkerEvents().isEmpty())
 				{	
 	    			int index;
 	    				    			
 	    			if(!map.getStations().isEmpty())
 	    			{
 	    				map.isPrintSelected=true;
 	    				pBook.append(map,printer.defaultPage());
 	    			}		
 	    			
	    			if(accordionMenu.getCurrentAtiveMarkersCount()!=0)
 	    			{
 	    				index=accordionMenu.getGlobalMarkerEvents().get(0).getMarkerIndex();
 	    			    pBook.append(new MmPrintMarkers(System.getProperty("user.dir")+"/globalmarkers/pattern"+Integer.toString(index+1)+".png" ,accordionMenu.getGlobalMarkerEvents().get(0),language), printer.defaultPage());
 	    			}    
 	    			
					for(int i=1;i<accordionMenu.getCurrentAtiveMarkersCount();i++)
 					{		
 					   index = accordionMenu.getGlobalMarkerEvents().get(i).getMarkerIndex();
 					   
 					   pBook.append(new MmPrintMarkers(System.getProperty("user.dir")+"/globalmarkers/pattern"+Integer.toString(index+1)+".png" ,accordionMenu.getGlobalMarkerEvents().get(i),language), printer.defaultPage());
 					
 					   //JOptionPane.showMessageDialog(null, "marker index "+index);
 					  			   
 					   //accordionMenu.getMarkers().printMap();
 					}   
 				    
 				}
 	    		//pBook.append(new MmPrintMarkers("/Users/Umapathi/Desktop/brick.gif",null), printer.defaultPage());
 	    		//pBook.append(new MmPrintMarkers("/Users/Umapathi/Desktop/brickNrm.gif",null), pPageFormat);
 	    		//pBook.append(new MmPrintMarkers("/Users/Umapathi/Desktop/dirt_NRM.png",null), pPageFormat);
 	    		//pBook.append(new MmPrintMarkers("/Users/Umapathi/Desktop/childrenAtPond_NRM.png",null), pPageFormat);
 	    		printer.setPageable(pBook);
 	    		printer.print();
 	    			
 	    		}
 	    		catch(Exception e)
 	    		{
 	    			JOptionPane.showMessageDialog(null, e);
 	    		}
 	    	}
 		
 		
 		
 		    
 		
 		//map.printMap();
 		map.isPrintSelected=false;
 		map.drawPoints();
 	
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
         //NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), System.getProperty("user.dir") + File.separator + "lib" + File.separator + "VLC.app" + File.separator + "Contents"+  File.separator + "MacOS"+File.separator + "lib");
         //System.setProperty("jna.library.path", System.getProperty("user.dir") + File.separator + "lib" + File.separator + "VLC" + File.separator + "lib");
         
         
 		
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gd = ge.getDefaultScreenDevice();
         GraphicsConfiguration gc = gd.getDefaultConfiguration();
         Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(gc);
         
         
         
         int sw = gc.getBounds().width - ins.left - ins.right;
         int sh = gc.getBounds().height - ins.top - ins.bottom;
 		
 		//Toolkit toolKit = Toolkit.getDefaultToolkit();
 		
 		new MmEditorMain(new Dimension(sw,sh));
 		
 		
 		
 		 
              
 	}
 
 	
 	/*private static class Listener implements AWTEventListener {
 		
 		MmMapViewer map1;
 		boolean focused = true;
 		static AWTEvent prevEvent;
 	    
 		
         public Listener(MmMapViewer map)
         {
         	map1 = map;
         	focused = true;
         }
         public void eventDispatched(AWTEvent event) {
             
             if(MouseInfo.getPointerInfo().getLocation().y<75 && event.toString().contains("FOCUS_LOST"))
             {
             	 //System.out.print(MouseInfo.getPointerInfo().getLocation() + " | ");
             	 //System.out.print("data  "+event);
             	 map1.bringTofront();
             }
             
             if((MouseInfo.getPointerInfo().getLocation().x<400 && MouseInfo.getPointerInfo().getLocation().y>75) && event.toString().contains("FOCUS_LOST"))
             {
             	 //System.out.print(MouseInfo.getPointerInfo().getLocation() + " | "+"\n");
             	 //JOptionPane.showMessageDialog(null, "Entered");
             	 System.out.print("data1  "+event);
             	 map1.bringTofront();
             }
             
             
             //System.out.println(event.toString());
             //map1.bringTofront();
             //System.out.println("event  " +event.getID());
             
             //int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
             
             //System.out.println("screen height "+screenHeight);
             
             /*if(event.toString().contains("FOCUS_LOST") && (focused) && (MouseInfo.getPointerInfo().getLocation().y>700))
             {
             	//JOptionPane.showMessageDialog(null, "Exit");
             	map1.hideStationEventWindow();
             	focused=false;
             	Listener.prevEvent = event;
             }
             
             /*if((MouseInfo.getPointerInfo().getLocation().y>750))
             {
             	
             	
             	if(prevEvent!=null && prevEvent.toString().contains("FOCUS_GAINED"))
             	{
             		System.out.println("mouse position  "+MouseInfo.getPointerInfo().getLocation());
             		System.out.println("prev event "+prevEvent);
             		map1.showStationEventWindow();
             	}
             	
             	/*if(event.toString().contains("FOCUS_LOST") && (focused) && map1.isStationEventWindowHidden())
             	{
             		//System.out.println("mouse position show "+MouseInfo.getPointerInfo().getLocation());
             		map1.hideStationEventWindow();
             		focused=false;
             		
             	}
             	
             	prevEvent = event;
             	   
             }
             
             if(event.toString().contains("FOCUS_GAINED") && (!focused) && (MouseInfo.getPointerInfo().getLocation().y>700))
             {	
             	map1.showStationEventWindow();
             	//JOptionPane.showMessageDialog(null, "Enter");
             	focused = true;
             	
             	if(prevEvent!=null && prevEvent.toString().contains("FOCUS_GAINED") && (MouseInfo.getPointerInfo().getLocation().y>750))
             	{
             		map1.showStationEventWindow();
             		
             	}
             	
             	Listener.prevEvent = event;
             }	
             	
             
         }
         
         
     }*/
 	
 	
 	
 
 }
 
