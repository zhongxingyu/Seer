 package hg.histo;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 
 public class FramCell extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2707712944901661771L;
 	private String param = "white";
 	
 
     Menu menu=new Menu();
     JPanel optionBox = new JPanel();
     JPanel buttonBar = new JPanel();
     JButton btZoomToFit = new JButton("Zoom To Fit ViewPort");
     
     mxGraph graph = new mxGraph();
 	Object parent = graph.getDefaultParent();
 	mxGraphComponent graphComponent ;
 	AlertView alertview = new AlertView();
 	SearchFile searchFile ;
 	
 	List<Cell> listCells;
 	
 	public FramCell() {
 		
 		super("Frame Cell!");
 		listCells = new ArrayList<Cell>();
 		graphComponent = new mxGraphComponent(graph);
 		graphComponent.setConnectable(false);
 		graphComponent.getViewport().setOpaque(true);
 	
 		
 		
 		optionBox.setLayout(new BorderLayout());
 		buttonBar.setLayout(new FlowLayout());
 		buttonBar.add(btZoomToFit);
 		//btZoomToFit.addActionListener(this);
 		
 		optionBox.add(buttonBar,BorderLayout.CENTER);
 		getContentPane().add(optionBox, BorderLayout.EAST);
 
 		 setJMenuBar(menu.buildMenu());
 	      
 	      menu.getExit().addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					// TODO Auto-generated method stub
 					
 					FramCell.this.setVisible(false);
 				}
 
 			});
 	      menu.getOpen().addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				JFileChooser chooser = new JFileChooser();// cration dun
 
 				chooser.setApproveButtonText("Choose File..."); // intitul du
 				// bouton
 				chooser.showOpenDialog(null); // affiche la boite de dialogue
 				String path = chooser.getSelectedFile().getAbsolutePath();
 				System.out.println("Path selected : " + path);
 				
 				StringTokenizer st = new StringTokenizer(path, "."); 
 				
 				while (st.hasMoreTokens()) { 
 
 				//System.out.println("token:"+st.nextToken()); 
 
 				String path_initial = st.nextToken();
 				System.out.println("path : " + path_initial); 
 
 				String ext=st.nextToken();
 				System.out.println("ext : " + ext);
 
 					/*String path_initial = st.nextToken();
 					System.out.println("token : " + path_initial); 
 
 					String ext = st.nextToken();
 					System.out.println("extention : " + ext); */
 
 					
 
 			    if(ext.equals("csv")){
 			    	System.out.println("Ceci est un bon fichier ");
 			    	System.out.println(ext);
 			    	
 			    	//remplace .cvs to jpg
 			    	String newPath1=ext.replace(ext.charAt(0), 'j');
 			    	String newPath2=newPath1.replace(ext.charAt(1), 'p');
 			    	String newPath3=newPath2.replace(ext.charAt(2), 'g');
 			    	
 			    	// creation de path.jpg
 			    	String path_image = path_initial +"."+ newPath3;
 			    	
 			    	System.out.println("path_image " + path_image);
 			    	
 			    	// aller chercher path.jpg dans le dossier
 			    	
 			    	searchFile = new SearchFile(path_image);
 			    	boolean found = searchFile.searchFileImage(searchFile.name, searchFile.filePath);
 			    	if (found )
 			    		System.out.println("ok found file .jpg ");
 			    	else 
 			    	System.out.println("Ko not found .jpg ");
 			    	
 			    	}
 			    
 			   else if (!ext.equals("csv")){
 			   System.out.println("Ceci n'est pas un bon fichier ");
 			    
 			    JOptionPane.showMessageDialog(graphComponent, "File choosen is not expected",
 					      "avertissement",
 					      JOptionPane.WARNING_MESSAGE);
 			    FramCell.this.setVisible(false);
 			   
 			    
 			   }
 				
 				} 
 									
 			}
 				
 			
 		});
 	     /* menu.getImage().addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				//display or remove image 
 			}
 		});*/
 			
 			
 }
 	public void initFrame(String path){
 		listCells = setListCell(path);
 		graph.getModel().beginUpdate();
 
 		try
 
 		{
 			for (Cell c : listCells) {
 				ColorCell(c);
 				Object v1 = graph
 						.insertVertex(parent, null, c.getClass_name(),
 								c.getInner_x() / 3, c.getInner_y() / 3, 10, 10,
 								"shape=ellipse;per=ellipsePerimeter;fillColor="
 										+ param);
 			}
 		} finally {
 			graph.getModel().endUpdate();
 		}
 
 		graphComponent.setConnectable(false);//edges
 		graphComponent.getViewport().setOpaque(true);//background
 
 
 
 		graphComponent.setBackgroundImage(new ImageIcon(
 				"src/ressources/image0046.jpg"));			
 		
 
 		getContentPane().add(graphComponent);
       
 	}
 	public void changeFrame(String path){
 		listCells.clear();
 		listCells = setListCell(path);
 		
 		graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
 		graph.getModel().beginUpdate();
 		
 		try
 
 		{
 			
 				Object v1 = graph.insertVertex(parent, null,2,6,6, 10,
 						10,"shape=ellipse;per=ellipsePerimeter;fillColor="+param);
 			}
 		
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 		
 			graphComponent.setBackgroundImage(new ImageIcon("src/images/image0046.jpg"));
 		
 		
 		getContentPane().add(graphComponent,BorderLayout.CENTER);
 
 	}
 	public List<Cell> setListCell(String path){
 		
 		File myFile = new File(path);
 		FillCellWithCSV f;
 		try {
 			f = new FillCellWithCSV(myFile);
 			List<Cell> listcells = f.allCells();
 			return listcells;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			setListCell("src/images/image0046.csv");
 		}
 		return null;
 		
 		
 		
 	}
 	public void ColorCell(Cell c) {
 		if (c.getClass_name().equals("Tumor nucleus")) {
 
 			param = "red";
 			// System.out.println(param);
 		} else if (c.getClass_name().equals("Granulocyte nucleus")) {
 			param = "yellow";
 			// System.out.println(param);
 		} else if (c.getClass_name().equals("Lymphocyte Nucleus")) {
 			param = "green";
 			// System.out.println(param);
 		} else if (c.getClass_name().equals("Nucleus DAB+ PRD+")) {
 			param = "black";
 			// System.out.println(param);
 		} else if (c.getClass_name().equals("Nucleus DAB+")) {
 			param = "blue";
 			// System.out.println(param);
 		} else {
 			param = "white";
 			// System.out.println(param);
 		}
 
 	}
 }
