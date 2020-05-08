 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GraphicsConfiguration;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.ScrollPane;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.DirectionalLight;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.Material;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.vecmath.AxisAngle4f;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
 import com.sun.j3d.utils.geometry.Cone;
 import com.sun.j3d.utils.geometry.Cylinder;
 import com.sun.j3d.utils.geometry.Primitive;
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickResult;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 import javax.swing.JCheckBox;
 
 public class SystemBuilder {
 	static JPanel jsys;
 	static JPanel jstkit;
 	static Darshan main;
 	static Vector<SimpleMolecule> simpleMolecule;
 	static File saveSysTo;
 	static int moleCount ;
 	static float Temperature = 298.0f;
 	static float size[];
 	static SystemProperties sysProp;
 	static boolean kaamChalu = false;
 	static int retries = 0;
 	static boolean load = false;
 	static int nos = 0;
 	static int saveInterval = 0;
 	static String caseName = "default";
 	static boolean doIt = false;
 	static Canvas3D canvasSys = null;
 	static SimpleUniverse uniSys = null;
 	static Position mousePos;
 	static int selectedId = 9999;
 	static boolean dragged = false;
 	static OrbitBehavior orbit = null;
 	static ViewingPlatform view = null;
 	static double scale = 0.005;
 	static int listTotal = 1;
 	static BranchGroup sysBranchGroup;
 	static Vector<BranchGroup> regionBranch;
 	static boolean wall[];
 	double majorDim=0;
 	static Vector<SingleMolecule> moleculeVec;
 	static Vector<Vector<Molecule>> actualMolecule;
 	static Vector<Vector<BranchGroup>> actualTransform;
 	static Vector<Vector<Vector<Position>>> allPositions;
 	static TransformGroup sysTransGroup;
 	static Cell cell[][][];
 	static File thisList[];
 	static int tracker = 0;
 	static boolean cgo = false;
 	static Vector<Region> regions;
 	static int pointer = 0;
 	static Vector<Atom> allAtoms;
 	static Crystal crystal;
 	static boolean stop = false;
 	static boolean validate = true;
 	static Vector<Expression> exp;
 	static Vector<Boolean> makeStationary;
 	static Molecule chooseMol;
 	static float oldL;
 	static Structure molecules;
 	static int angleAtom11, angleAtom12;
 	static TransformGroup selectingCone;
 	static JList regionList;
 	static TransformGroup tg;
 	static BranchGroup boxGroup;
 	static float oldX = 0, oldY = 0, oldZ = 0;
 	int sum =0;
 	//static Vector<RegionMolecules> regMol;
 	
 	@SuppressWarnings("static-access")
 	public void initiateSystemBuilder(Darshan ab, SystemProperties sysProperties){
 		main=ab;
 		canvasSys = null;
 		uniSys = null;
 		selectedId = 9999;
 		orbit = null;
 		view = null;
 		mousePos = new Position();
 		sysProp = sysProperties;
 		simpleMolecule = new Vector<SimpleMolecule>();
 		actualMolecule = new Vector<Vector<Molecule>>();
 		actualTransform = new Vector<Vector<BranchGroup>>();
 		allPositions = new Vector<Vector<Vector<Position>>>();
 		makeStationary = new Vector<Boolean>();
 		exp = new Vector<Expression>();
 		allAtoms = new Vector<Atom>();
 		//regMol = new Vector<RegionMolecules>();
 		moleCount = 0;
 		main.jtext.setText("Initializing the system, please wait");
 		moleculeVec =  new Vector<SingleMolecule>();
 		loadAtoms();
 		scanSystem();
 		sysFrame();
 		setSysToolKit();
 		main.working = true;
 	}
 	@SuppressWarnings("static-access")
 	public void loadAtoms(){
 		BufferedReader in1;
 		try {
 			in1 = new BufferedReader(new FileReader("dreiding.abat"));
 			while(in1.ready()){
 				String s = in1.readLine();
 				StringTokenizer st = new StringTokenizer(s);
 				if(st.hasMoreTokens()){
 					int a1= Integer.parseInt(st.nextToken());
 					switch(a1){
 					default:
 						String atomCheck = st.nextToken();
 						Atom a = new Atom(a1,atomCheck,Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()), Integer.parseInt(st.nextToken()));
 						allAtoms.add(a);
 						break;
 					}
 				}
 			}		
 		} catch (FileNotFoundException e) {
 			main.jtext.setText("File not found");
 		} catch (NumberFormatException e) {
 			main.jtext.setText("Invalid Input");
 		} catch (IOException e) {
 			main.jtext.setText("I/O Error");
 		}
 		
 	}
 	private void scanSystem() {
 		int x = (int)(sysProp.xx/sysProp.cutOff);
 		int y = (int)(sysProp.yy/sysProp.cutOff);
 		int z = (int)(sysProp.zz/sysProp.cutOff);
 		cell = new Cell[x+1][y+1][z+1];
 		for(int i=0; i<x+1; i++){
 			for(int j=0; j<y+1; j++){
 				for(int k=0; k<z+1; k++){
 					cell[i][j][k] = new Cell();
 				}
 			}
 		}
 	}
 	public void newScanSystem(){
 		int x = (int)(sysProp.xx/sysProp.cutOff);
 		int y = (int)(sysProp.yy/sysProp.cutOff);
 		int z = (int)(sysProp.zz/sysProp.cutOff);
 		Cell oldCell[][][] = cell;
 		cell = new Cell[x+1][y+1][z+1];
 		for(int i=0; i<x+1; i++){
 			for(int j=0; j<y+1; j++){
 				for(int k=0; k<z+1; k++){
 					cell[i][j][k] = new Cell();
 				}
 			}
 		}
 		x = (int)(oldX/sysProp.cutOff);
 		y = (int)(oldY/sysProp.cutOff);
 		z = (int)(oldZ/sysProp.cutOff);
 		for(int i=0; i<x+1; i++){
 			for(int j=0; j<y+1; j++){
 				for(int k=0; k<z+1; k++){
 					cell[i][j][k] = oldCell[i][j][k];
 				}
 			}
 		}
 	}
 	@SuppressWarnings({ "static-access" })
 	public void loadCaseFile(File f) {
 		File file = new File("optimizeSystem.mole");
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 					BufferedReader in = new BufferedReader(new FileReader(f));
 					if(Integer.parseInt(in.readLine())==25011989){
 						FileWriter wfile = new FileWriter(file.toString());
 						BufferedWriter out = new BufferedWriter(wfile);
 						String s = null;
 						while(in.ready()){
 							s =in.readLine();
 							System.out.println(s);
 						}
 						in.close();
 						out.close();
 						wfile.close();
 					}
 					try{
 						String[] command = {"./createSystem"};
 				        ProcessBuilder probuilder = new ProcessBuilder( command );
 				        Map<String, String> env = probuilder.environment();
 						env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/omkar/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 						env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 						
 				        Process process = probuilder.start();
 						InputStream is = process.getInputStream();
 				        InputStreamReader isr = new InputStreamReader(is);
 				        BufferedReader br = new BufferedReader(isr);
 				        String line;
 				        System.out.printf("Output of running %s is:\n",
 				                Arrays.toString(command));
 				        while ((line = br.readLine()) != null) {
 				        	System.out.println(line);
 				        }
 				        
 				        int exitValue;
 						try {
 							exitValue = process.waitFor();
 							//System.out.println("\n\nExit Value is " + exitValue);
 							BufferedReader in1 = new BufferedReader(new FileReader(new File("coordinates.txt")));
 							nos = Integer.parseInt(in1.readLine());
 							in1.close();
 							if(exitValue == 0){
 								kaamChalu = false;
 								main.jtext.setText("System Load Successful");
 							}
 						} 
 						catch (InterruptedException e){
 							process.destroy();
 							main.jtext.setText("Process terminated, taking too long time");
 						}
 						
 					}catch(IOException e){
 						main.jtext.setText("IO Error");
 					}
 				} catch (IOException e) {
 					main.jtext.setText("IO Error");
 				}
 			}
 			else{
 				main.jtext.setText("File write error");
 			}
 		}
 		
 	}
 	@SuppressWarnings({ "static-access" })
 	public boolean loadDataFile(File f) {
 		main.jtext.setText("Loading data");
 		File file = new File("coordinates.txt");
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 			return false;
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 						BufferedReader in = new BufferedReader(new FileReader(f));
 						FileWriter wfile = new FileWriter(file.toString());
 						BufferedWriter out = new BufferedWriter(wfile);
 						String s = null;
 						while(in.ready()){
 							s =in.readLine();
 							System.out.println(s);
 						}
 						in.close();
 						out.close();
 						wfile.close();
 					
 				} catch (IOException e) {
 					main.jtext.setText("IO Error");
 					return false;
 				}
 			}
 			else{
 				main.jtext.setText("File write error");
 				return false;
 			}
 		}
 		main.jtext.setText("Data Load Successful");
 		return true;
 	}
 	@SuppressWarnings("static-access")
 	public void setSysToolKit(){
 		//main.jtext.setText("\nCreating new toolkit window\n");
 		jstkit = new JPanel(new BorderLayout());
 		jstkit.setBounds((int)(main.width/1.151770658), 0,(int) (main.width/7.588888889), (int)((int)main.height/1.024));
 		main.jdesk.add(jstkit);
 		main.frame.add(main.jdesk);
 		//main.bar.setValue(0);
 		/*kaamChalu = true;
 		main.jtext.setText("Creating system, please wait");
 		Runnable r = new Minimizethis();
 		Thread t = new Thread(r);
 		t.setPriority(Thread.MAX_PRIORITY);
 		t.start();*/
 		final DefaultListModel regionModel = new DefaultListModel();
 		regionList = new JList(regionModel);
 		regionList.setSelectedIndex(2);
 		regionList.setVisibleRowCount(18);
 		JScrollPane regionScroll = new JScrollPane(regionList);
 		regionScroll.setSize(60, 300);
 		
 		jstkit.setLayout(new BorderLayout());
 		jstkit.add(regionScroll,BorderLayout.NORTH);
 		
 		JPanel sysToolPane = new JPanel(new FlowLayout());
 		sysToolPane.setVisible(true);
 		sysToolPane.setSize(166,200);
 		regions = new Vector<Region>();
 		JButton jsetDim = new JButton("Set Size");
 		jsetDim.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jregion = new JFrame();
 				jregion.setTitle("Set Size");
 				jregion.setBounds(400, 150, 340, 200);
 				jregion.setVisible(true);
 				jregion.setLayout(new BorderLayout());
 				jregion.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				
 				JLabel xsize, ysize, zsize;//, rad, length;
 				xsize = new JLabel("Length");
 				ysize = new JLabel("Height");
 				zsize = new JLabel("Width");
 				
 				//rad = new JLabel("Radius");
 				//length = new JLabel("Length");
 
 				final JTextField  txsize, tysize, tzsize;//, trad, tlength;
 				txsize = new JTextField(""+sysProp.xx);
 				tysize = new JTextField(""+sysProp.yy);
 				tzsize = new JTextField(""+sysProp.zz);
 				
 				JButton butNext1 = new JButton("Change");
 				JButton butCancel = new JButton("Cancel");
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						jregion.dispose();
 					}									
 				});
 				
 				butNext1.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						try{
 						float x = Float.parseFloat(txsize.getText());
 						float y = Float.parseFloat(tysize.getText());
 						float z = Float.parseFloat(tzsize.getText());
 						if(sysProp.xx<x && sysProp.yy<y && sysProp.zz <z){
 							oldX = sysProp.xx;
 							oldY = sysProp.yy;
 							oldZ = sysProp.zz;
 							sysProp.xx = x;
 							sysProp.yy = y;
 							sysProp.zz = z;
 							createBox(sysProp.xx*0.1f, sysProp.yy*0.1f, sysProp.zz*0.1f);
 							orbit.setRotationCenter(new Point3d(sysProp.xx/2*0.1, sysProp.yy/2*0.1, sysProp.zz/2*0.1));
 						
 							TransformGroup VpTG = uniSys.getViewingPlatform().getViewPlatformTransform();
 							float Zcamera = 10; //put the camera 12 meters back
 							Transform3D Trfcamera = new Transform3D();
 							Trfcamera.setTranslation(new Vector3f(sysProp.xx/2*0.1f, sysProp.yy/2*0.1f, Zcamera));  
 							VpTG.setTransform( Trfcamera );
 					    
 					    	newScanSystem();
 					    
 					    	jregion.dispose();
 						}
 						else if(regions.size() == 0){
 							oldX = sysProp.xx;
 							oldY = sysProp.yy;
 							oldZ = sysProp.zz;
 							sysProp.xx = x;
 							sysProp.yy = y;
 							sysProp.zz = z;
 							createBox(sysProp.xx*0.1f, sysProp.yy*0.1f, sysProp.zz*0.1f);
 							orbit.setRotationCenter(new Point3d(sysProp.xx/2*0.1, sysProp.yy/2*0.1, sysProp.zz/2*0.1));
 						
 							TransformGroup VpTG = uniSys.getViewingPlatform().getViewPlatformTransform();
 							float Zcamera = 10; //put the camera 12 meters back
 							Transform3D Trfcamera = new Transform3D();
 							Trfcamera.setTranslation(new Vector3f(sysProp.xx/2*0.1f, sysProp.yy/2*0.1f, Zcamera));  
 							VpTG.setTransform( Trfcamera );
 					    
 					    	scanSystem();
 					    
 					    	jregion.dispose();
 						}
 						else{
 							main.jtext.setText("You can shrink the simulation box only if simulation box is empty");
 						}
 						}
 						catch(NumberFormatException e){
 							main.jtext.setText("Invalid Input");
 						}
 					}					
 				});
 				JPanel p1 = new JPanel(new GridBagLayout());
 				JPanel p2 = new JPanel(new FlowLayout());
 				
 				
 				p1.add(xsize, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				p1.add(txsize, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 				
 				p1.add(ysize, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				p1.add(tysize, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				p1.add(zsize, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				p1.add(tzsize, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 				
 				p2.add(butNext1);
 				p2.add(butCancel);
 				
 				jregion.add(p1, BorderLayout.CENTER);
 				jregion.add(p2, BorderLayout.SOUTH);
 			}			
 		});
 		
 		JButton jpolyfill = new JButton("Fill Polymer");
 		jpolyfill.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jregion = new JFrame();
 				jregion.setTitle("Create Region");
 				jregion.setBounds(400, 150, 340, 200);
 				jregion.setVisible(true);
 				jregion.setLayout(new GridBagLayout());
 				jregion.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				
 				final JPanel p1;
 				final JPanel p2;
 				
 				p1 = new JPanel(new GridBagLayout());
 				p2 = new JPanel(new FlowLayout());
 				
 
 				JLabel jcomponents = new JLabel("Number of Monomers");
 				final JTextField jtcomponents = new JTextField("100");
 				
 				//JLabel jshape = new JLabel("Region Shape");
 				//final JComboBox bshape = new JComboBox();
 				//bshape.addItem("Cubic");
 				//bshape.addItem("Spherical");
 				//bshape.addItem("Cuboid");
 				//bshape.addItem("Cylindrical");
 				
 				p1.add(jcomponents, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				p1.add(jtcomponents, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				//p1.add(jshape, new GBC(0,0,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				//p1.add(bshape, new GBC(1,0,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				
 //				p3.add(jlVol);
 				
 				JButton butNext = new JButton("Next");
 				JButton butCancel = new JButton("Cancel");
 				p2.add(butNext);
 				p2.add(butCancel);
 				cgo = false;
 				jregion.setLayout(new BorderLayout());
 			//	jregion.add(p1,BorderLayout.WEST);
 				//jregion.add(p3,BorderLayout.EAST);
 				jregion.add(p1,BorderLayout.CENTER);
 				jregion.add(p2,BorderLayout.SOUTH);
 				
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 							jregion.dispose();
 					}			
 				});
 				butNext.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						try{	
 							try{
 								final int noc = Integer.parseInt(jtcomponents.getText()); 
 					//			int s = bshape.getSelectedIndex();
 								final JPanel p3;
 								final JPanel p4;
 								JLabel lxpos, lypos, lzpos, xsize, ysize, zsize;//, rad, length;
 								lxpos = new JLabel("X Position");
 								lypos = new JLabel("Y Position");
 								lzpos = new JLabel("Z Position");
 								xsize = new JLabel("Length");
 								ysize = new JLabel("Height");
 								zsize = new JLabel("Width");
 								final JCheckBox cex = new JCheckBox("Exclude From Other Regions");
 								
 								//rad = new JLabel("Radius");
 								//length = new JLabel("Length");
 
 								final JTextField txpos;
 								final JTextField typos, tzpos, txsize, tysize, tzsize;//, trad, tlength;
 								txpos = new JTextField(""+sysProp.xx/2);
 								typos = new JTextField(""+sysProp.yy/2);
 								tzpos = new JTextField(""+sysProp.zz/2);
 								
 								JButton butNext1 = new JButton("Create");
 								JButton butCancel = new JButton("Cancel");
 								butCancel.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										jregion.dispose();
 									}									
 								});
 								p3 = new JPanel(new GridBagLayout());
 								p4 = new JPanel(new FlowLayout());
 								
 								p4.add(butNext1);
 								p4.add(butCancel);
 									jregion.remove(p1);
 									jregion.remove(p2);
 									
 										txsize = new JTextField("20");
 										tysize = new JTextField("20");
 										tzsize = new JTextField("20");
 										p3.add(lxpos, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										p3.add(txpos, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										
 										p3.add(lypos, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										p3.add(typos, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										
 										p3.add(lzpos, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										p3.add(tzpos, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										
 										p3.add(xsize, new GBC(0,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										p3.add(txsize, new GBC(1,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 										
 										p3.add(ysize, new GBC(0,4,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										p3.add(tysize, new GBC(1,4,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										
 										p3.add(zsize, new GBC(0,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 										p3.add(tzsize, new GBC(1,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 										
 										p3.add(cex, new GBC(0,6,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 										
 										jregion.setVisible(false);
 										
 										jregion.add(p3,BorderLayout.CENTER);
 										jregion.add(p4,BorderLayout.SOUTH);
 										jregion.setVisible(true);
 										jregion.setSize(340, 300);
 										butNext1.addActionListener(new ActionListener(){
 											public void actionPerformed(ActionEvent arg0) {
 												float x = Float.parseFloat(txpos.getText());
 												float y = Float.parseFloat(typos.getText());
 												float z = Float.parseFloat(tzpos.getText());
 												float xsize = Float.parseFloat(txsize.getText());
 												float ysize = Float.parseFloat(tysize.getText()); 
 												float zsize = Float.parseFloat(tzsize.getText()); 
 												if(x > 0 && x<sysProp.xx && y >0 && y<sysProp.yy
 														&& z>0 && z<sysProp.zz && (-xsize/2+x)>=0
 														&& (-ysize/2+y)>=0 && (-zsize/2+z)>=0 && (xsize/2+x)<= sysProp.xx
 														&& (ysize/2+y)<= sysProp.yy && (zsize/2+z)<= sysProp.zz){
 													cgo = cex.isSelected();
 													jregion.remove(p3);
 													jregion.remove(p4);
 													loadComponents(jregion, noc, x, y, z,xsize, ysize, zsize, true);
 												}
 											}
 
 																															
 										});									
 																
 								
 							}
 							catch(NumberFormatException e){
 								System.out.println("Invalid Input");
 							}
 							
 						}catch(NumberFormatException e){
 							main.jtext.setText("Invalid Input");
 						}
 						
 					}
 				});
 				
 			}			
 		});
 		JButton jregionadd = new JButton("Add Domain");
 		jregionadd.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				
 				/*
 				Random rand = new Random();
 				float x = rand.nextFloat() * sysProp.xx*0.1f - sysProp.xx * 0.05f;
 				float y = rand.nextFloat() * sysProp.yy*0.1f - sysProp.yy * 0.05f;
 				float z = rand.nextFloat() * sysProp.zz*0.1f - sysProp.zz * 0.05f;
 				Region s =new Region(x, y, z, 1.0f, 3.0f, 2);
 				BranchGroup b = s.graph;
 				sysBranchGroup.addChild(b);
 				regionBranch.add(b);*/
 				final JFrame jregion = new JFrame();
 				jregion.setTitle("Create Region");
 				jregion.setBounds(400, 150, 340, 200);
 				jregion.setVisible(true);
 				jregion.setLayout(new BorderLayout());
 				jregion.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			
 				
 
 			//	JLabel jcomponents = new JLabel("Number of Components");
 				//final JTextField jtcomponents = new JTextField("1");
 			//	jtcomponents.disable();
 				
 				//JLabel jshape = new JLabel("Region Shape");
 				//final JComboBox bshape = new JComboBox();
 				//bshape.addItem("Cubic");
 				//bshape.addItem("Spherical");
 				//bshape.addItem("Cuboid");
 				//bshape.addItem("Cylindrical");
 				
 				//p1.add(jcomponents, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				//p1.add(jtcomponents, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				//p1.add(jshape, new GBC(0,0,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				//p1.add(bshape, new GBC(1,0,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				
 //				p3.add(jlVol);
 				
 //				JButton butNext = new JButton("Next");
 	//			JButton butCancel = new JButton("Cancel");
 				//p2.add(butNext);
 				//p2.add(butCancel);
 				cgo = false;
 		//		jregion.setLayout(new BorderLayout());
 			//	jregion.add(p1,BorderLayout.WEST);
 				//jregion.add(p3,BorderLayout.EAST);
 				//jregion.add(p1,BorderLayout.CENTER);
 				//jregion.add(p2,BorderLayout.SOUTH);
 			//	butCancel.addActionListener(new ActionListener(){
 			//		public void actionPerformed(ActionEvent arg0) {
 			//				jregion.dispose();
 			//		}			
 			//	});
 			//	butNext.addActionListener(new ActionListener(){
 			//		public void actionPerformed(ActionEvent arg0) {
 			//			
 			//		}
 					
 			//	});
 				try{	
 					try{
 					//	final int noc = Integer.parseInt(jtcomponents.getText()); 
 			//			int s = bshape.getSelectedIndex();
 						final JPanel p3;
 						final JPanel p4;
 						JLabel lxpos, lypos, lzpos, xsize, ysize, zsize;//, rad, length;
 						lxpos = new JLabel("X Position");
 						lypos = new JLabel("Y Position");
 						lzpos = new JLabel("Z Position");
 						xsize = new JLabel("Length");
 						ysize = new JLabel("Height");
 						zsize = new JLabel("Width");
 						final JCheckBox cex = new JCheckBox("Exclude From Other Regions");
 						
 						//rad = new JLabel("Radius");
 						//length = new JLabel("Length");
 
 						final JTextField txpos;
 						final JTextField typos, tzpos, txsize, tysize, tzsize;//, trad, tlength;
 						txpos = new JTextField(""+sysProp.xx/2);
 						typos = new JTextField(""+sysProp.yy/2);
 						tzpos = new JTextField(""+sysProp.zz/2);
 						
 						JButton butNext1 = new JButton("Create");
 						JButton butCancel = new JButton("Cancel");
 						butCancel.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								jregion.dispose();
 							}									
 						});
 						p3 = new JPanel(new GridBagLayout());
 						p4 = new JPanel(new FlowLayout());
 						
 						p4.add(butNext1);
 						p4.add(butCancel);
 							
 								txsize = new JTextField(""+sysProp.xx);
 								tysize = new JTextField(""+sysProp.yy);
 								tzsize = new JTextField(""+sysProp.zz);
 								p3.add(lxpos, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								p3.add(txpos, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								
 								p3.add(lypos, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								p3.add(typos, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								
 								p3.add(lzpos, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								p3.add(tzpos, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								
 								p3.add(xsize, new GBC(0,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								p3.add(txsize, new GBC(1,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 								
 								p3.add(ysize, new GBC(0,4,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								p3.add(tysize, new GBC(1,4,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								
 								p3.add(zsize, new GBC(0,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								p3.add(tzsize, new GBC(1,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 								
 								p3.add(cex, new GBC(0,6,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 								
 								jregion.setVisible(false);
 								
 								jregion.add(p3,BorderLayout.CENTER);
 								jregion.add(p4,BorderLayout.SOUTH);
 								jregion.setVisible(true);
 								jregion.setSize(340, 300);
 								butNext1.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										try{
 										float x = Float.parseFloat(txpos.getText());
 										float y = Float.parseFloat(typos.getText());
 										float z = Float.parseFloat(tzpos.getText());
 										float xsize = Float.parseFloat(txsize.getText());
 										float ysize = Float.parseFloat(tysize.getText()); 
 										float zsize = Float.parseFloat(tzsize.getText()); 
 										if(x > 0 && x<sysProp.xx && y >0 && y<sysProp.yy
 												&& z>0 && z<sysProp.zz && (-xsize/2+x)>=0
 												&& (-ysize/2+y)>=0 && (-zsize/2+z)>=0 && (xsize/2+x)<= sysProp.xx
 												&& (ysize/2+y)<= sysProp.yy && (zsize/2+z)<= sysProp.zz){
 											cgo = cex.isSelected();
 											jregion.remove(p3);
 											jregion.remove(p4);
 											loadComponents(jregion, 1, x, y, z,xsize, ysize, zsize);
 										}
 										}catch(NumberFormatException e){
 											main.jtext.setText("Invalid Input");
 										}
 									}																					
 								});									
 															
 						
 					}
 					catch(NumberFormatException e){
 						System.out.println("Invalid Input");
 					}
 					
 				}catch(NumberFormatException e){
 					main.jtext.setText("Invalid Input");
 				}
 
 			}
 				
 		});
 		wall = new boolean[6];
 		for(int i=0; i<6 ; i++){
 			wall[i] = false;
 		}
 		JButton jwalladd = new JButton("Add Wall");
 		jwalladd.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame walling = new JFrame();
 				walling.setTitle("Set Properties");
 				walling.setBounds(400, 150, 340, 200);
 				walling.setVisible(true);
 				walling.setLayout(new GridBagLayout());
 				walling.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				
 				final JPanel p1;
 				final JPanel p2;
 				
 				p1 = new JPanel(new GridBagLayout());
 				p2 = new JPanel(new FlowLayout());
 				JLabel jpos = new JLabel("Wall Position");
 				
 				JLabel jcell = new JLabel("Cell Setup");
 				final JButton bcell = new JButton("Incomplete");
 				
 				final JComboBox bpos = new JComboBox();
 				bpos.addItem("X+");
 				bpos.addItem("X-");
 				bpos.addItem("Y+");
 				bpos.addItem("Y-");
 				bpos.addItem("Z+");
 				bpos.addItem("Z-");
 				
 				
 				JLabel jtype = new JLabel("Cell Structure");
 				final JComboBox btype = new JComboBox();
 				btype.addItem("Cubic");
 				btype.addItem("FCC");
 				btype.addItem("BCC");
 				btype.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						crystal = null;
 						bcell.setText("Incomplete");
 					}				
 				});
 				
 				JLabel jthick = new JLabel("Wall Layers");
 				final JTextField jtthick = new JTextField("1");
 				
 				
 				bcell.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg1) {
 						walling.setVisible(false);
 						final JFrame jsetcell = new JFrame();
 						jsetcell.setTitle("Cell Setting");
 						jsetcell.setBounds(400, 150, 340, 200);
 						jsetcell.setVisible(true);
 						jsetcell.setLayout(new BorderLayout());
 						jsetcell.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 						
 						final JComboBox batom1 = new JComboBox();
 						final JComboBox batom2 = new JComboBox();
 						Vector<Atom> copyAtoms = new Vector<Atom>();
 						for(int i=0; i<allAtoms.size(); i++){
 							Atom a1 = Atom.copy(allAtoms.get(i));
 							copyAtoms.add(a1);
 							batom1.addItem(allAtoms.get(i).name);
 							batom2.addItem(allAtoms.get(i).name);
 						}
 						
 						JLabel llength = new JLabel("Cell Length (A)");
 						final JTextField tlength = new JTextField("5.06");
 						
 						final JPanel p3 = new JPanel(new GridBagLayout());
 						final JPanel p4 = new JPanel(new FlowLayout());
 						JLabel latom1 = new JLabel("Atom 1");
 						JLabel latom2 = new JLabel("Atom 2");
 						//System.out.println(""+bpos.getSelectedItem());
 						switch(btype.getSelectedIndex()){
 							case 0:
 								latom1.setText("Corner Atom 1");
 								latom2.setText("Corner Atom 2");
 								batom1.setSelectedItem("Na");
 								batom2.setSelectedItem("Cl");
 								tlength.setText("5.06");
 								break;
 							case 1:
 								latom1.setText("Corner Atom");
 								latom2.setText("Face Centered Atom");
 								batom1.setSelectedItem("Na");
 								batom2.setSelectedItem("Cl");
 								tlength.setText("6");
 								break;
 							case 2:
 								latom1.setText("Corner Atom");
 								latom2.setText("Body Centered Atom");
 								batom1.setSelectedItem("Na");
 								batom2.setSelectedItem("Cl");
 								tlength.setText("6");
 								break;
 							//case 3:
 								
 						}
 						
 						
 						
 						final Vector<Atom> atoms = copyAtoms;
 						JButton jedit1 = new JButton("Edit");
 						jedit1.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								jsetcell.setVisible(false);
 								final JFrame chargeFrame = new JFrame("Edit Atom Data");
 								
 								chargeFrame.setBounds(400, 150, 300, 250);
 								chargeFrame.setVisible(true);
 								chargeFrame.setLayout(new BorderLayout());
 								chargeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 								JPanel chargePanel = new JPanel(new GridBagLayout());
 								chargeFrame.add(chargePanel, BorderLayout.CENTER);
 								//chargeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 								
 								//JLabel valency = new JLabel("Valency");
 								JLabel charge = new JLabel("Charge");
 								JLabel sigma = new JLabel("Sigma");
 								JLabel epsilon = new JLabel("Epsilon");
 							
 								
 								final JTextField textCharge = new JTextField(""+atoms.get(batom1.getSelectedIndex()).charge);
 								final JTextField textSigma = new JTextField(""+atoms.get(batom1.getSelectedIndex()).sigma);
 								final JTextField textEpsilon = new JTextField(""+atoms.get(batom1.getSelectedIndex()).epsilon);
 								//final JTextField textAngle = new JTextField(""+selectedMol.atoms.get(selectedId).bondAngle);
 								
 								JPanel butPanel = new JPanel(new FlowLayout());
 								JButton butOk = new JButton("Ok");
 								JButton butCancel = new JButton("Cancel");
 								
 								butOk.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										try{
 												atoms.get(batom1.getSelectedIndex()).charge = Double.parseDouble(textCharge.getText());
 												atoms.get(batom1.getSelectedIndex()).sigma = Double.parseDouble(textSigma.getText());
 												atoms.get(batom1.getSelectedIndex()).epsilon = Double.parseDouble(textEpsilon.getText());
 													chargeFrame.dispose();
 												jsetcell.setVisible(true);
 										}catch(NumberFormatException e){
 											main.jtext.setText("Wrong Input"+e.getMessage());
 										}
 									}			
 								});
 								
 								butCancel.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										chargeFrame.dispose();
 										jsetcell.setVisible(true);
 									}			
 								});
 								
 								butPanel.add(butOk);
 								butPanel.add(butCancel);
 								
 								chargePanel.add(sigma,  new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textSigma, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(epsilon,  new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textEpsilon, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(charge, new GBC(0,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textCharge, new GBC(1,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(butPanel, new GBC(0,5, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 							}
 						});
 						JButton jedit2 = new JButton("Edit");
 						jedit2.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								jsetcell.setVisible(false);
 								final JFrame chargeFrame = new JFrame("Edit Atom Data");
 								
 								chargeFrame.setBounds(400, 150, 300, 250);
 								chargeFrame.setVisible(true);
 								chargeFrame.setLayout(new BorderLayout());
 								
 								JPanel chargePanel = new JPanel(new GridBagLayout());
 								chargeFrame.add(chargePanel, BorderLayout.CENTER);
 								//chargeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 								chargeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 //								JLabel valency = new JLabel("Valency");
 								JLabel charge = new JLabel("Charge");
 								JLabel sigma = new JLabel("Sigma");
 								JLabel epsilon = new JLabel("Epsilon");
 								JLabel mass = new JLabel("Mass");
 								
 								
 								final JTextField textCharge = new JTextField(""+atoms.get(batom2.getSelectedIndex()).charge);
 								final JTextField textSigma = new JTextField(""+atoms.get(batom2.getSelectedIndex()).sigma);
 								final JTextField textEpsilon = new JTextField(""+atoms.get(batom2.getSelectedIndex()).epsilon);
 								final JTextField textMass = new JTextField(""+atoms.get(batom2.getSelectedIndex()).mass);
 								//final JTextField textAngle = new JTextField(""+selectedMol.atoms.get(selectedId).bondAngle);
 								
 								JPanel butPanel = new JPanel(new FlowLayout());
 								JButton butOk = new JButton("Ok");
 								JButton butCancel = new JButton("Cancel");
 								
 								butOk.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										try{
 												atoms.get(batom1.getSelectedIndex()).charge = Double.parseDouble(textCharge.getText());
 												atoms.get(batom1.getSelectedIndex()).sigma = Double.parseDouble(textSigma.getText());
 												atoms.get(batom1.getSelectedIndex()).epsilon = Double.parseDouble(textEpsilon.getText());
 												atoms.get(batom1.getSelectedIndex()).mass = Double.parseDouble(textMass.getText());
 												chargeFrame.dispose();
 												jsetcell.setVisible(true);
 										}catch(NumberFormatException e){
 											main.jtext.setText("Wrong Input"+e.getMessage());
 										}
 									}			
 								});
 								
 								butCancel.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										chargeFrame.dispose();
 										jsetcell.setVisible(true);
 										
 									}			
 								});
 								
 								butPanel.add(butOk);
 								butPanel.add(butCancel);
 								
 								chargePanel.add(mass,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textMass, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(sigma,  new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textSigma, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(epsilon,  new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textEpsilon, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(charge, new GBC(0,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(textCharge, new GBC(1,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								chargePanel.add(butPanel, new GBC(0,5, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 							}							
 						});
 						
 						p3.add(latom1,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(batom1, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(jedit1,  new GBC(2,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(latom2, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(batom2,  new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(jedit2, new GBC(2,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(llength,  new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(tlength, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						JButton ok = new JButton("Ok");
 						ok.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								crystal = new Crystal(atoms.get(batom1.getSelectedIndex()), atoms.get(batom2.getSelectedIndex()), Float.parseFloat(tlength.getText()), btype.getSelectedIndex());
 								crystal.index[0].mass = 1e12;
 								crystal.index[1].mass = 1e12;
 								bcell.setText("Complete");
 								jsetcell.dispose();
 								walling.setVisible(true);
 							}							
 						});
 						JButton cancel = new JButton("Cancel");
 						cancel.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								jsetcell.dispose();
 								walling.setVisible(true);
 							}							
 						});
 						p4.add(ok);
 						p4.add(cancel);
 						jsetcell.add(p3, BorderLayout.CENTER);
 						jsetcell.add(p4, BorderLayout.SOUTH);
 					}
 				});
 				
 				
 //				
 			
 				//btype.addItem("HCP");
 				
 				p1.add(jthick, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				p1.add(jtthick, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				p1.add(jpos, new GBC(0,1,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				p1.add(bpos, new GBC(1,1,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				
 				p1.add(jtype, new GBC(0,2,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				p1.add(btype, new GBC(1,2,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				
 				p1.add(jcell, new GBC(0,3,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 				p1.add( bcell, new GBC(1,3,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 //				p3.add(jlVol);
 				
 				
 				JButton butCreate = new JButton("Create");
 				JButton butCancel = new JButton("Cancel");
 				p2.add(butCreate);
 				p2.add(butCancel);
 				
 				walling.setLayout(new BorderLayout());
 			//	walling.add(p1,BorderLayout.WEST);
 				//walling.add(p3,BorderLayout.EAST);
 				walling.add(p1,BorderLayout.CENTER);
 				walling.add(p2,BorderLayout.SOUTH);
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 							walling.dispose();
 					}			
 				});
 				butCreate.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						if(crystal != null || !bcell.getText().equals("Incomplete"))
 						try{	
 							
 							float thickness = Float.parseFloat(jtthick.getText()); 
 							int s = bpos.getSelectedIndex();
 							float x=0, y=0, z=0;
 							Region v = null;
 							BranchGroup b = null;
 							switch(s){
 								case 1:
 									if(!(wall[1] || wall[2]|| wall[3]|| wall[4]|| wall[5])){
 										walling.dispose();
 										
 										x = sysProp.xx  ;
 										y = sysProp.yy  ;
 										z = sysProp.zz  ;
 										//System.out.println(""+x+" "+y+" "+z);
 										
 										ListModel lm = regionList.getModel();
 										((DefaultListModel) lm).addElement("Wall "+2);
 										listTotal++;
 										v =new Region((float)(thickness*crystal.length/2) , y/2 , z/2, (float)(thickness*crystal.length) ,y , z, sysProp, true );
 										Crystal c = crystal;
 										replicate(v, c);
 										regions.add(v);
 										Boolean stas = false;
 										makeStationary.add(stas);
 										Expression e = new Expression();
 										exp.add(e);
 										c = null;
 										b = v.graph;
 										sysBranchGroup.addChild(b);
 										regionBranch.add(b);
 										wall[1] = true;
 										
 									}
 									else{
 										main.jtext.setText("Wall Exists"+wall[0]+wall[1]+wall[2]+ wall[3]+ wall[4]+ wall[5]);
 									}
 									break;
 								case 0:
 									if(!(wall[0] || wall[2]|| wall[3]|| wall[4]|| wall[5])){
 										walling.dispose();
 										x = sysProp.xx  ;
 										y = sysProp.yy  ;
 										z = sysProp.zz  ;
 										//System.out.println(""+x+" "+y+" "+z);
 										ListModel lm = regionList.getModel();
 										((DefaultListModel) lm).addElement("Wall "+1);
 										listTotal++;
 										v =new Region(x -(float)(thickness*crystal.length/2) , y/2 , z/2, (float)(thickness*crystal.length) ,y , z, sysProp, true );
 										regions.add(v);
 										Boolean stas = false;
 										makeStationary.add(stas);
 										Expression e = new Expression();
 										exp.add(e);
 										Crystal c = crystal;
 										replicate(v, c);
 										c = null;
 										b = v.graph;
 										sysBranchGroup.addChild(b);
 										regionBranch.add(b);
 										wall[0] = true;
 									}
 									else
 										main.jtext.setText("Wall Exists"+wall[0]+wall[1]+wall[2]+ wall[3]+ wall[4]+ wall[5]);
 									break;
 								case 3:
 									if(!(wall[1] || wall[0]|| wall[3]|| wall[4]|| wall[5])){
 										walling.dispose();
 										x = sysProp.xx  ;
 										y = sysProp.yy  ;
 										z = sysProp.zz  ;
 										//System.out.println(""+x+" "+y+" "+z);
 										ListModel lm = regionList.getModel();
 										((DefaultListModel) lm).addElement("Wall "+4);
 										listTotal++;
 										v =new Region(x/2,  (float)(thickness*crystal.length/2) , z/2, x ,(float)(thickness*crystal.length) , z, sysProp, true );
 										Crystal c = crystal;
 										replicate(v, c);
 										regions.add(v);
 										Boolean stas = false;
 										makeStationary.add(stas);
 										Expression e = new Expression();
 										exp.add(e);
 										c = null;
 										b = v.graph;
 										sysBranchGroup.addChild(b);
 										regionBranch.add(b);
 										wall[3] = true;
 									}else
 										main.jtext.setText("Wall Exists"+wall[0]+wall[1]+wall[2]+ wall[3]+ wall[4]+ wall[5]);
 									break;
 								case 2:
 									if(!(wall[1] || wall[0]|| wall[2]|| wall[4]|| wall[5])){
 										walling.dispose();
 										x = sysProp.xx  ;
 										y = sysProp.yy  ;
 										z = sysProp.zz  ;
 										//System.out.println(""+x+" "+y+" "+z);
 										ListModel lm = regionList.getModel();
 										((DefaultListModel) lm).addElement("Wall "+3);
 										listTotal++;
 										v =new Region(x/2,  y -(float)(thickness*crystal.length/2) , z/2, x ,(float)(thickness*crystal.length) , z, sysProp, true );
 										regions.add(v);
 										Boolean stas = false;
 										makeStationary.add(stas);
 										Expression e = new Expression();
 										exp.add(e);
 										Crystal c = crystal;
 										replicate(v, c);
 										c = null;
 										b = v.graph;
 										sysBranchGroup.addChild(b);
 										regionBranch.add(b);
 										wall[2] = true;
 									}else
 										main.jtext.setText("Wall Exists"+wall[0]+wall[1]+wall[2]+ wall[3]+ wall[4]+ wall[5]);
 									break;
 								case 5:
 									if(!(wall[1] || wall[2]|| wall[3]|| wall[0]|| wall[5])){
 										walling.dispose();
 										x = sysProp.xx  ;
 										y = sysProp.yy  ;
 										z = sysProp.zz  ;
 										//System.out.println(""+x+" "+y+" "+z);
 										ListModel lm = regionList.getModel();
 										((DefaultListModel) lm).addElement("Wall "+6);
 										listTotal++;
 										v =new Region(x/2, y/2, (float)(thickness*crystal.length/2)  , x , y, (float)(thickness*crystal.length), sysProp, true );
 										regions.add(v);
 										Boolean stas = false;
 										makeStationary.add(stas);
 										Expression e = new Expression();
 										exp.add(e);
 										Crystal c = crystal;
 										replicate(v, c);
 										c = null;
 										b = v.graph;
 										sysBranchGroup.addChild(b);
 										regionBranch.add(b);
 										wall[5] = true;
 									}else
 										main.jtext.setText("Wall Exists"+wall[0]+wall[1]+wall[2]+ wall[3]+ wall[4]+ wall[5]);
 									break;
 								case 4:
 									if(!(wall[1] || wall[2]|| wall[3]|| wall[4]|| wall[0])){
 										walling.dispose();
 										x = sysProp.xx  ;
 										y = sysProp.yy  ;
 										z = sysProp.zz  ;
 										//System.out.println(""+x+" "+y+" "+z);
 										ListModel lm = regionList.getModel();
 										((DefaultListModel) lm).addElement("Wall "+5);
 										listTotal++;
 										v =new Region(x/2, y/2,  z -(float)(thickness*crystal.length/2) , x , y, (float)(thickness*crystal.length), sysProp, true );
 										regions.add(v);
 										Boolean stas = false;
 										makeStationary.add(stas);
 										Expression e = new Expression();
 										exp.add(e);
 										Crystal c = crystal;
 										replicate(v, c);
 										c = null;
 										b = v.graph;
 										sysBranchGroup.addChild(b);
 										regionBranch.add(b);
 										wall[4] = true;
 										
 									}else
 										main.jtext.setText("Wall Exists"+wall[0]+wall[1]+wall[2]+ wall[3]+ wall[4]+ wall[5]);
 									break;
 							}
 						}catch(NumberFormatException e){
 							main.jtext.setText("Invalid Input");
 						}
 						
 					}
 					
 					private void replicate(Region v, Crystal c) {
 						Vector<Position> posVec = new Vector<Position>();
 						Vector<Atom> atomVec = new Vector<Atom>();
 						//System.out.println(""+(v.x-v.sizex/2)+" "+(v.x+v.sizex/2)+" "+(v.y-v.sizey/2)+" "+(v.y+v.sizey/2)+" "+(v.z-v.sizez/2)+" "+(v.z+v.sizez/2));
 						
 						switch(c.type){
 							case 0:
 								boolean check = true;
 								for(float i=v.x-v.sizex/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2; k<=(float)((v.z+v.sizez/2)); k+=c.length){
 											if(check){
 													atomVec.add(Atom.copy(c.index[0]));		
 													check = false;
 											}
 											else{
 												atomVec.add(Atom.copy(c.index[1]));
 												check = true;
 											}
 											posVec.add(new Position(i, j, k));
 										}
 										if(check)
 											check = false;
 											else 
 												check = true;
 									}
 									if(check)
 									check = false;
 									else 
 										check = true;
 								}
 								break;
 							case 1:
 								for(float i=v.x-v.sizex/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2; k<=(float)((v.z+v.sizez/2)); k+=c.length){
 												atomVec.add(Atom.copy(c.index[0]));														
 												posVec.add(new Position(i, j, k));
 										}										
 									}
 								}
 								for(float i=v.x-v.sizex/2+c.length/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2+c.length/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2; k<=(float)((v.z+v.sizez/2)+1); k+=c.length){
 												atomVec.add(Atom.copy(c.index[1]));														
 												posVec.add(new Position(i, j, k));
 										}										
 									}
 								}
 								for(float i=v.x-v.sizex/2+c.length/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2+c.length/2; k<=(float)((v.z+v.sizez/2)); k+=c.length){
 												atomVec.add(Atom.copy(c.index[1]));														
 												posVec.add(new Position(i, j, k));
 										}										
 									}
 								}
 								for(float i=v.x-v.sizex/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2+c.length/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2+c.length/2; k<=(float)((v.z+v.sizez/2)); k+=c.length){
 												atomVec.add(Atom.copy(c.index[1]));														
 												posVec.add(new Position(i, j, k));
 										}										
 									}
 								}
 								break;
 							case 2:
 								for(float i=v.x-v.sizex/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2; k<=(float)((v.z+v.sizez/2)); k+=c.length){
 												atomVec.add(Atom.copy(c.index[0]));														
 												posVec.add(new Position(i, j, k));
 										}										
 									}
 								}
 								for(float i=v.x-v.sizex/2+c.length/2; i<=(float)((v.x+v.sizex/2)); i+=c.length){
 									for(float j=v.y-v.sizey/2+c.length/2; j<=(float)((v.y+v.sizey/2)); j+=c.length){
 										for(float k=v.z-v.sizez/2+c.length/2; k<=(float)((v.z+v.sizez/2)); k+=c.length){
 												atomVec.add(Atom.copy(c.index[1]));														
 												posVec.add(new Position(i, j, k));
 										}										
 									}
 								}
 								break;
 						}
 						SingleMolecule s = new SingleMolecule();
 						s.create(atomVec, posVec, new Vector<Bond>(), new Vector<Angles>(), new Vector<Torsion>());
 						moleculeVec.add(s);
 						Vector<BranchGroup> tg1 = new Vector<BranchGroup>();
 						Vector<Molecule> mg1 = new Vector<Molecule>();
 						Vector<Vector<Position>> pos2vec = new Vector<Vector<Position>>();
 						Vector<Position> pos1vec = new Vector<Position>();
 									Molecule m1 = new Molecule();
 									BranchGroup moleBranch = new BranchGroup();
 									moleBranch.setCapability(moleBranch.ALLOW_DETACH);
 										for(int l=0; l<moleculeVec.get(moleculeVec.size()-1).atom.size(); l++){
 											//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 												//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 												boolean work = true;
 												float xx = (float) (moleculeVec.get(tracker).pos.get(l).x ); 
 												float yy = (float) (moleculeVec.get(tracker).pos.get(l).y ); 
 												float zz = (float) (moleculeVec.get(tracker).pos.get(l).z ); 
 												for(int m =0; m<regions.size()-1; m++){
 													if(regions.get(m).exclude)
 													if(xx<regions.get(m).x+regions.get(m).sizex/2 && xx>regions.get(m).x -regions.get(m).sizex/2
 															&& yy<regions.get(m).y+regions.get(m).sizey/2 && yy>regions.get(m).y -regions.get(m).sizey/2
 															&& zz<regions.get(m).z+regions.get(m).sizez/2 && zz>regions.get(m).z -regions.get(m).sizez/2){
 														//System.out.println(""+(regions.get(m).x+regions.get(m).sizex/2)+" "+(regions.get(m).x -regions.get(m).sizex/2));
 														//System.out.println(""+(regions.get(m).y+regions.get(m).sizey/2)+" "+(regions.get(m).y -regions.get(m).sizey/2));
 														//System.out.println(""+(regions.get(m).z+regions.get(m).sizez/2)+" "+(regions.get(m).z -regions.get(m).sizez/2));
 														work = false;
 														break;
 													}
 												}
 											
 											//System.out.println(""+xx/sysProp.cutOff+" "+ yy/sysProp.cutOff +" "+zz/sysProp.cutOff
 											//		+" "+sysProp.xx/ sysProp.cutOff+" "+sysProp.yy/ sysProp.cutOff+" "+sysProp.xx/ sysProp.cutOff);
 											if((int)(xx/sysProp.cutOff) + 1<(int)(sysProp.xx/sysProp.cutOff))
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size();m++){
 												float x = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 												float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											if((int)(xx/sysProp.cutOff) - 1>0)
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 												float x = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											if((int)(yy/sysProp.cutOff) + 1<(int)(sysProp.yy/sysProp.cutOff))
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 												float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											if((int)(yy/sysProp.cutOff) - 1>0)
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 												float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											if((int)(zz/sysProp.cutOff) + 1<(int)(sysProp.zz/sysProp.cutOff))
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.size() && work;m++){
 												float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											if((int)(zz/sysProp.cutOff) - 1>0)
 											for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.size() && work;m++){
 												float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).x - xx;
 												float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).y - yy;
 												float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).z - zz;
 												double res = Math.sqrt(x*x + y*y +z*z);
 												double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).sigma + moleculeVec.get(tracker).atom.get(l).sigma)/(2);
 												if(res<sigma){
 													work =false;
 													break;
 												}
 											}
 											
 											
 											if(!work)
 												continue;
 											float x1 = (float) (moleculeVec.get(moleculeVec.size()-1).pos.get(l).x )*0.1f; 
 											float y1 = (float) (moleculeVec.get(moleculeVec.size()-1).pos.get(l).y )*0.1f; 
 											float z1 = (float) (moleculeVec.get(moleculeVec.size()-1).pos.get(l).z )*0.1f; 
 											pos1vec.add(new Position(moleculeVec.get(moleculeVec.size()-1).pos.get(l).x, moleculeVec.get(moleculeVec.size()-1).pos.get(l).y, moleculeVec.get(moleculeVec.size()-1).pos.get(l).z));
 											
 											cell[(int)(moleculeVec.get(moleculeVec.size()-1).pos.get(l).x/sysProp.cutOff)][(int)(moleculeVec.get(moleculeVec.size()-1).pos.get(l).y/sysProp.cutOff)][(int)(moleculeVec.get(moleculeVec.size()-1).pos.get(l).z /sysProp.cutOff)].addNew(moleculeVec.get(moleculeVec.size()-1).pos.get(l).x, moleculeVec.get(moleculeVec.size()-1).pos.get(l).y, moleculeVec.get(moleculeVec.size()-1).pos.get(l).z, moleculeVec.get(moleculeVec.size()-1).atom.get(l).sigma, moleBranch);
 											TransformGroup moleTrans = createBehaviors();
 											moleTrans.addChild(  createSphere(moleculeVec.get(moleculeVec.size()-1).atom.get(l).RFID));
 											moleBranch.addChild(moleTrans);
 											Vector3f temp = new Vector3f(x1,y1,z1);
 											Transform3D tempT = new Transform3D();
 											tempT.setTranslation(temp);
 											moleTrans.setTransform(tempT);
 											Atom a1 = Atom.copy(moleculeVec.get(moleculeVec.size()-1).atom.get(l));
 											PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 											pickCanvas.setMode(PickCanvas.BOUNDS);
 											m1.addAtom(a1, moleTrans,pickCanvas,temp,moleculeVec.get(moleculeVec.size()-1).atom.get(l).RFID);
 										}
 										//for(int l=0; l<pos1vec.size(); l++){
 										//	cell[(int)(moleculeVec.get(moleculeVec.size()-1).pos.get(l).x/sysProp.cutOff)][(int)(moleculeVec.get(moleculeVec.size()-1).pos.get(l).y/sysProp.cutOff)][(int)(moleculeVec.get(moleculeVec.size()-1).pos.get(l).z /sysProp.cutOff)].addNew(moleculeVec.get(moleculeVec.size()-1).pos.get(l).x, moleculeVec.get(moleculeVec.size()-1).pos.get(l).y, moleculeVec.get(moleculeVec.size()-1).pos.get(l).z, moleculeVec.get(moleculeVec.size()-1).atom.get(l).sigma, moleBranch);
 										//}
 										pos2vec.add(pos1vec);
 										sysTransGroup.addChild(moleBranch);
 										tg1.add(moleBranch);
 										mg1.add(m1);
 									allPositions.add(pos2vec);
 						actualTransform.add(tg1);
 						actualMolecule.add(mg1);
 						tracker++;
 					}
 					
 				});
 			}			
 		});
 		
 		JButton jregiondel = new JButton("Remove");
 		jregiondel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(!regionList.isSelectionEmpty()){
 				String s = (String)regionList.getSelectedValue();
 				StringTokenizer st = new StringTokenizer(s);
 				if(st.nextToken().equals("Wall")){
 					int a = Integer.parseInt(st.nextToken());
 					//System.out.println(""+a);
 					switch(a-1){
 					case 0:
 						wall[0] = false;
 						break;
 					case 1:						
 						wall[1] = false;
 						break;
 					case 2:
 						wall[2] = false;
 						break;
 					case 3:						
 						wall[3] = false;
 						break;
 					case 4:
 						wall[4] = false;
 						break;
 					case 5:
 						wall[5] = false;
 						break;
 					}
 						
 				}
 				delRegion(regionList);
 				}
 			}
 
 			private void delRegion(JList regionList) {
 						//System.out.println("Selected "+regionList.getSelectedIndex());
 						sysBranchGroup.removeChild(regionBranch.get(regionList.getSelectedIndex()));
 						regionBranch.remove(regionList.getSelectedIndex());
 						
 						regions.remove(regionList.getSelectedIndex());
 						makeStationary.remove(regionList.getSelectedIndex());
 						exp.remove(regionList.getSelectedIndex());
 						int x = (int)(sysProp.xx/sysProp.cutOff);
 						int y = (int)(sysProp.yy/sysProp.cutOff);
 						int z = (int)(sysProp.zz/sysProp.cutOff);
 						
 						//for(int v=0; v<regMol.get(regionList.getSelectedIndex()-1).list.size(); v++){
 							//System.out.println(""+regMol.get(regionList.getSelectedIndex()-1).list.get(v));
 						for(int i=0; i<actualTransform.get(regionList.getSelectedIndex()).size(); i++){
 							//System.out.println("hi");
 							
 						for(int j=0; j<x; j++){
 							for(int k=0; k<y; k++){
 								for(int l=0; l<z; l++){
 									
 									for(int m= 0; m<cell[j][k][l].list.size(); m++){
 										if(cell[j][k][l].list.get(m).branch.equals(actualTransform.get(regionList.getSelectedIndex()).get(i))){
 											cell[j][k][l].list.remove(m);
 										}
 									}
 								}
 							}
 						}
 							}
 						
 				//	}
 						
 						//for(int v=0; v<regMol.get(regionList.getSelectedIndex()-1).list.size(); v++){
 						//for(int i=0; i<actualTransform.get(regMol.get(regionList.getSelectedIndex()-1).list.get(v)).size(); i++){
 								//System.out.println("hi");
 						for(int i=0; i<actualTransform.get(regionList.getSelectedIndex()).size(); i++){	
 							//actualTransform.get(regMol.get(regionList.getSelectedIndex()-1).list.get(v)).get(i).detach();
 							actualTransform.get(regionList.getSelectedIndex()).get(i).detach();
 								//sysTransGroup.removeChild(actualTransform.get(regionList.getSelectedIndex()+1).get(i));
 						}
 					//}	
 						/*for(int i=0; i<regMol.size(); i++){
 							for(int j=0; j<regMol.get(i).list.size(); j++){
 								for(int k=0; k<regMol.get(i).list.get(j); k++){
 									for(int l=0; l<regMol.get(regionList.getSelectedIndex()-1).list.size(); l++){
 										for(int m = 0; m<regMol.get(regionList.getSelectedIndex()-1).list.get(l); m++){
 											if(k!=m)
 											if(regMol.get(i).list.get(j)>regMol.get(regionList.getSelectedIndex()-1).list.get(l)){
 												int d =regMol.get(i).list.remove(j);
 												d--;
 												regMol.get(i).list.add(j, d);
 												System.out.println(""+d);
 											}
 										}
 									}
 								}
 							}
 						}*/
 						//for(int v=regMol.get(regionList.getSelectedIndex()-1).list.size()-1; v>0; v--){
 							actualMolecule.remove(regionList.getSelectedIndex());
 							allPositions.remove(regionList.getSelectedIndex());
 							actualTransform.remove(regionList.getSelectedIndex());
 							moleculeVec.remove(regionList.getSelectedIndex());							
 						//}
 						//tracker-=regMol.get(regionList.getSelectedIndex()-1).list.size();
 							tracker--;
 						//regMol.remove(regionList.getSelectedIndex()-1);
 						
 						regionModel.remove(regionList.getSelectedIndex());
 						
 					
 			}
 			
 				//regionList.remove(regionList.getSelectedIndex());
 			
 			
 		});
 		
 		JButton jimport = new JButton("Import Domain");
 		jimport.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jregion = new JFrame("Domain Size");
 				
 				final JPanel p3;
 				final JPanel p4;
 				
 				
 				JLabel lxpos, lypos, lzpos;//, rad, length;
 				lxpos = new JLabel("X Position");
 				lypos = new JLabel("Y Position");
 				lzpos = new JLabel("Z Position");
 				final JCheckBox cex = new JCheckBox("Exclude From Other Regions");
 				
 				//rad = new JLabel("Radius");
 				//length = new JLabel("Length");
 
 				final JTextField txpos;
 				final JTextField typos, tzpos;//, trad, tlength;
 				txpos = new JTextField(""+sysProp.xx/2);
 				typos = new JTextField(""+sysProp.yy/2);
 				tzpos = new JTextField(""+sysProp.zz/2);
 				
 				JButton butNext1 = new JButton("Create");
 				JButton butCancel = new JButton("Cancel");
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						jregion.dispose();
 					}									
 				});
 				p3 = new JPanel(new GridBagLayout());
 				p4 = new JPanel(new FlowLayout());
 				
 				p4.add(butNext1);
 				p4.add(butCancel);
 					
 						p3.add(lxpos, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(txpos, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						p3.add(lypos, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(typos, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						p3.add(lzpos, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(tzpos, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						p3.add(cex, new GBC(0,6,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 						
 						jregion.setVisible(false);
 						
 						jregion.add(p3,BorderLayout.CENTER);
 						jregion.add(p4,BorderLayout.SOUTH);
 						jregion.setVisible(true);
 						jregion.setBounds(400, 150, 340, 280);
 						butNext1.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								float x = Float.parseFloat(txpos.getText());
 								float y = Float.parseFloat(typos.getText());
 								float z = Float.parseFloat(tzpos.getText());
 								if(x > 0 && x<sysProp.xx && y >0 && y<sysProp.yy
 										&& z>0 && z<sysProp.zz ){
 									JFileChooser jfc = new JFileChooser();
 									jfc.setCurrentDirectory(new File(main.workSpace));			
 									int f = jfc.showOpenDialog(main);
 									if(f == JFileChooser.APPROVE_OPTION){
 										File file = jfc.getSelectedFile();					
 										try{					
 											BufferedReader  in = new BufferedReader(new FileReader(file));						
 											String export =in.readLine();
 											if(Integer.parseInt(export)==2){	
 												int noc = Integer.parseInt(in.readLine());
 												boolean works = loadComponents(jregion, noc, x, y, z, in);
 												if(works)	{
 													jregion.dispose();
 												}
 												else{
 													main.jtext.setText("Domain size greater than system size");
 													jregion.dispose();
 												}
 											}
 										} catch (IOException e) {
 											main.jtext.setText("Error : Cannot read file");
 										}catch(NumberFormatException e){
 											main.jtext.setText("Error : Corrupt file");
 										}
 									} 								
 								}
 							}
 
 							private boolean loadComponents(JFrame jregion,
 									int noc, float x, float y, float z,
 									 BufferedReader in) {
 								StringTokenizer st = null;
 								try {
 									st = new StringTokenizer(in.readLine());
 								} catch (IOException e) {
 									main.jtext.setText("I/O Error");
 								}
 								
 								float majorx=0, majory=0, majorz=0;
 								if(st.hasMoreTokens()){									
 									majorx = Float.parseFloat(st.nextToken());
 									//System.out.println(""+majorx);
 									majory = Float.parseFloat(st.nextToken());
 									majorz = Float.parseFloat(st.nextToken());									
 									if(x+majorx/2>sysProp.xx||y+majory/2>sysProp.yy||z+majorz/2>sysProp.zz){
 										
 										return false;
 									}
 								}
 								//System.out.println(""+majorDim);
 								//System.out.println(""+sizex+" "+sizey+" "+sizez);
 								Region r = new Region(x, y, 
 										z, majorx, majory, majorz, sysProp , cgo);
 								
 								majorDim = 0;
 								Vector<Integer> instances = loadDomain(in, noc);	
 								
 								boolean voila = true;
 								voila =	replicate(r,  instances,in);
 								if(!voila){
 									main.jtext.setText("Domain Size greater than System Size");
 									
 									return false;
 								}
 								else{
 								BranchGroup b= r.graph;
 								sysBranchGroup.addChild(b);
 								regionBranch.add(b);
 								ListModel lm = regionList.getModel();
 								((DefaultListModel) lm).addElement("Region "+(pointer));
 								regions.add(r);
 								Boolean stas = false;
 								makeStationary.add(stas);
 								Expression e = new Expression();
 								exp.add(e);
 								//System.out.println("hi");
 								jregion.dispose();
 								return true;
 								}
 								
 							}																
 						});
 			}			
 		});
 		JButton jmol = new JButton("Add Molecule");
 		jmol.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jregion = new JFrame("Molecule Position");
 				
 				final JPanel p3;
 				final JPanel p4;
 				
 				
 				JLabel lxpos, lypos, lzpos;//, rad, length;
 				lxpos = new JLabel("X Position");
 				lypos = new JLabel("Y Position");
 				lzpos = new JLabel("Z Position");
 				
 				final JCheckBox cex = new JCheckBox("Exclude From Other Regions");
 				
 				//rad = new JLabel("Radius");
 				//length = new JLabel("Length");
 
 				final JTextField txpos;
 				final JTextField typos, tzpos;//, trad, tlength;
 				txpos = new JTextField(""+sysProp.xx/2);
 				typos = new JTextField(""+sysProp.yy/2);
 				tzpos = new JTextField(""+sysProp.zz/2);
 				
 				final JCheckBox selVec = new JCheckBox("Select Vector");
 				selVec.setSelected(false);
 				
 				JButton butNext1 = new JButton("Create");
 				JButton butCancel = new JButton("Cancel");
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						jregion.dispose();
 					}									
 				});
 				p3 = new JPanel(new GridBagLayout());
 				p4 = new JPanel(new FlowLayout());
 				
 				p4.add(butNext1);
 				p4.add(butCancel);
 					
 						p3.add(lxpos, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(txpos, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						p3.add(lypos, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(typos, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						p3.add(lzpos, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						p3.add(tzpos, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						//p3.add(selVec, new GBC(0,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 						
 						p3.add(cex, new GBC(0,6,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));	
 						
 						jregion.setVisible(false);
 						
 						jregion.add(p3,BorderLayout.CENTER);
 						jregion.add(p4,BorderLayout.SOUTH);
 						jregion.setVisible(true);
 						jregion.setBounds(400, 150, 340, 280);
 						
 						butNext1.addActionListener(new ActionListener(){
 							public void actionPerformed(ActionEvent arg0) {
 								try{
 								float x = Float.parseFloat(txpos.getText());
 								float y = Float.parseFloat(typos.getText());
 								float z = Float.parseFloat(tzpos.getText());
 								if(x > 0 && x<sysProp.xx && y >0 && y<sysProp.yy
 										&& z>0 && z<sysProp.zz){
 									JFileChooser jfc = new JFileChooser();
 									jfc.setCurrentDirectory(new File(main.workSpace));			
 									int f = jfc.showOpenDialog(main);
 									if(f == JFileChooser.APPROVE_OPTION){
 										File file = jfc.getSelectedFile();					
 										try{					
 											BufferedReader  in = new BufferedReader(new FileReader(file));						
 											
 											String export =in.readLine();
 											if(Integer.parseInt(export)==1){	
 												int a = Integer.parseInt(in.readLine());
 												for(int i=0; i<a; i++)
 													in.readLine();
 												a = Integer.parseInt(in.readLine());
 												for(int i=0; i<a; i++)
 													in.readLine();
 												a = Integer.parseInt(in.readLine());
 												for(int i=0; i<a; i++)
 													in.readLine();
 												a = Integer.parseInt(in.readLine());
 												for(int i=0; i<a; i++)
 													in.readLine();
 												in.readLine();
 												StringTokenizer st =new StringTokenizer(in.readLine());
 												float xsize = Float.parseFloat(st.nextToken());
 												float ysize = Float.parseFloat(st.nextToken());
 												float zsize = Float.parseFloat(st.nextToken());
 												in = new BufferedReader(new FileReader(file));									
 												boolean works = loadComponents(jregion, 1, x, y, z,xsize, ysize, zsize, in);
 												if(works)	{
 													jregion.dispose();
 												}
 											}
 										} catch (IOException e) {
 											main.jtext.setText("Error : Cannot read file");
 											e.printStackTrace();
 										}catch(NumberFormatException e){
 											main.jtext.setText("Error : Corrupt file");
 											e.printStackTrace();
 										}
 									}
 									} 
 								}catch(NumberFormatException e){
 									main.jtext.setText("Invalid Input");
 								}
 							}
 
 							private boolean loadComponents(JFrame jregion,
 									int noc, float x, float y, float z,
 									float xsize, float ysize, float zsize, BufferedReader in) {
 								float sizex = xsize, sizey = ysize, sizez = zsize;
 								
 									if(x+xsize/2>sysProp.xx||y+ysize/2>sysProp.yy||z+zsize/2>sysProp.zz){
 										return false;
 									}
 								
 								
 								//System.out.println(""+majorDim);
 								//System.out.println(""+sizex+" "+sizey+" "+sizez);
 									cgo = cex.isSelected();
 								Region r = new Region(x, y, 
 										z, sizex, sizey, sizez, sysProp , cgo);
 								
 								majorDim = 0;
 								 loadDomain(in, noc);
 								 boolean voila = true;
 								if(!selVec.isSelected()){
 									voila =	replicate(r,in);
 									if(!voila){
 										main.jtext.setText("Cannot Place the Molecule");
 										jregion.dispose();
 										return false;
 									}
 									BranchGroup b= r.graph;
 									sysBranchGroup.addChild(b);
 									regionBranch.add(b);
 									ListModel lm = regionList.getModel();
 									((DefaultListModel) lm).addElement("Region "+(pointer));
 									regions.add(r);
 									Boolean stas = false;
 									makeStationary.add(stas);
 									Expression e = new Expression();
 									exp.add(e);
 									//System.out.println("hi");
 									jregion.dispose();
 									return true;
 								}
 								else{
 									askForVector();
 								}
 								return true;
 								
 							}
 
 							private boolean replicate(Region r,
 									BufferedReader in) {
 								//System.out.println(""+(r.sizex)+" "+(r.sizey)+" "+(r.sizez));
 								
 								//boolean work = true;
 								Vector<BranchGroup> tg1 = new Vector<BranchGroup>();
 								Vector<Molecule> mg1 = new Vector<Molecule>();
 								Vector<Vector<Position>> pos2Vec = new Vector<Vector<Position>>();
 								boolean work = true;
 								for(int j=0; j<moleculeVec.get(tracker).atom.size(); j++){
 									//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 									float xx = (float) (moleculeVec.get(tracker).pos.get(j).x +r.x - r.sizex/ 2); 
 									float yy = (float) (moleculeVec.get(tracker).pos.get(j).y +r.y - r.sizey/ 2); 
 									float zz = (float) (moleculeVec.get(tracker).pos.get(j).z +r.z - r.sizez/ 2); 
 									for(int m =0; m<regions.size()-1; m++){
 										if(regions.get(m).exclude)
 										if(xx<regions.get(m).x+regions.get(m).sizex/2 && xx>regions.get(m).x -regions.get(m).sizex/2
 												&& yy<regions.get(m).y+regions.get(m).sizey/2 && yy>regions.get(m).y -regions.get(m).sizey/2
 												&& zz<regions.get(m).z+regions.get(m).sizez/2 && zz>regions.get(m).z -regions.get(m).sizez/2){
 											//System.out.println(""+(regions.get(m).x+regions.get(m).sizex/2)+" "+(regions.get(m).x -regions.get(m).sizex/2));
 											//System.out.println(""+(regions.get(m).y+regions.get(m).sizey/2)+" "+(regions.get(m).y -regions.get(m).sizey/2));
 											//System.out.println(""+(regions.get(m).z+regions.get(m).sizez/2)+" "+(regions.get(m).z -regions.get(m).sizez/2));
 											work = false;
 											break;
 										}
 									}
 								
 								//System.out.println(""+xx/sysProp.cutOff+" "+ yy/sysProp.cutOff +" "+zz/sysProp.cutOff
 								//		+" "+sysProp.xx/ sysProp.cutOff+" "+sysProp.yy/ sysProp.cutOff+" "+sysProp.xx/ sysProp.cutOff);
 								if((int)(xx/sysProp.cutOff) + 1<(int)(sysProp.xx/sysProp.cutOff))
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size();m++){
 									float x = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								if((int)(xx/sysProp.cutOff) - 1>0)
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 									float x = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								if((int)(yy/sysProp.cutOff) + 1<(int)(sysProp.yy/sysProp.cutOff))
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 									float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								if((int)(yy/sysProp.cutOff) - 1>0)
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 									float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								if((int)(zz/sysProp.cutOff) + 1<(int)(sysProp.zz/sysProp.cutOff))
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.size() && work;m++){
 									float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								if((int)(zz/sysProp.cutOff) - 1>0)
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.size() && work;m++){
 									float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 									float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 									float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 									float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 									double res = Math.sqrt(x*x + y*y +z*z);
 									double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(tracker).atom.get(j).sigma)/(2);
 									if(res<sigma){
 										work =false;
 										break;
 									}
 								}
 								}
 								if(!work)
 									return false;
 										Molecule m = new Molecule();
 										Vector<Position> posVec = new Vector<Position>();
 										BranchGroup moleBranch = new BranchGroup();
 										moleBranch.setCapability(moleBranch.ALLOW_DETACH);
 										
 										for(int j=0; j<moleculeVec.get(tracker).atom.size(); j++){
 											//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 											float x1 = (float) (moleculeVec.get(tracker).pos.get(j).x +r.x - r.sizex/ 2)*0.1f; 
 											float y1 = (float) (moleculeVec.get(tracker).pos.get(j).y +r.y - r.sizey/ 2)*0.1f; 
 											float z1 = (float) (moleculeVec.get(tracker).pos.get(j).z +r.z - r.sizez/ 2)*0.1f; 
 											Position p = new Position(moleculeVec.get(tracker).pos.get(j).x+r.x - r.sizex/ 2, moleculeVec.get(tracker).pos.get(j).y+r.y - r.sizey/ 2, moleculeVec.get(tracker).pos.get(j).z+r.z - r.sizez/ 2);
 											posVec.add(p);
 											cell[(int)(p.x/sysProp.cutOff)][(int)(p.y/sysProp.cutOff)][(int)(p.z/sysProp.cutOff)].addNew(p.x, p.y, p.z, moleculeVec.get(tracker).atom.get(j).sigma, moleBranch);
 											TransformGroup moleTrans = createBehaviors();
 											moleTrans.addChild(  createSphere(moleculeVec.get(tracker).atom.get(j).RFID));
 											moleBranch.addChild(moleTrans);
 											Vector3f temp = new Vector3f(x1,y1,z1);
 											Transform3D tempT = new Transform3D();
 											tempT.setTranslation(temp);
 											moleTrans.setTransform(tempT);
 											Atom a1 = Atom.copy(moleculeVec.get(tracker).atom.get(j));
 											PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 										    pickCanvas.setMode(PickCanvas.BOUNDS);
 											m.addAtom(a1, moleTrans,pickCanvas,temp,moleculeVec.get(tracker).atom.get(j).RFID);
 										}
 										for(int j=0; j<moleculeVec.get(tracker).bond.size(); j++){
 											Vector3f posa = m.vec.get(moleculeVec.get(tracker).bond.get(j).index[0]);
 											Vector3f posb = m.vec.get(moleculeVec.get(tracker).bond.get(j).index[1]);
 											Vector3f cross = new Vector3f();
 											Vector3f YAXIS = new Vector3f(0, 1, 0);
 											Vector3f temp = new Vector3f((posa.x+posb.x)/2,(posa.y+posb.y)/2,(posa.z+posb.z)/2);
 											Vector3f temp1 = new Vector3f((posa.x-posb.x),(posa.y-posb.y),(posa.z-posb.z));
 											
 											double l = (float)Math.sqrt(Squares.sqr(posa.x-posb.x)+Squares.sqr(posa.y-posb.y)+Squares.sqr(posa.z-posb.z));
 											
 											temp1.normalize();
 
 											cross.cross(YAXIS,temp1);
 											
 											AxisAngle4f tempAA = new AxisAngle4f();
 											
 											tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 											TransformGroup moleTrans = createBehaviors();
 											moleTrans.addChild(  createCylinder(l,0));
 											TransformGroup t1 = createBehaviors();
 											t1.addChild(createCylinder(l/2,1));
 											TransformGroup t2 = createBehaviors();
 											t2.addChild(createCylinder(l/6,2));
 											moleBranch.addChild(moleTrans);
 											moleBranch.addChild(t1);
 											moleBranch.addChild(t2);
 											Transform3D tempT = new Transform3D();
 											tempT.set(tempAA);
 											tempT.setTranslation(temp);
 												moleTrans.setTransform(tempT);
 												Transform3D tempT1 = new Transform3D();
 												tempT1.set(tempAA);
 												tempT1.setTranslation(temp);
 												Transform3D tempT2 = new Transform3D();
 												tempT2.set(tempAA);
 												tempT2.setTranslation(temp);
 												float ratio1=1;
 												float ratio2=1;
 												
 												tempT1.setScale(new Vector3d(ratio1,1,ratio1));
 												tempT2.setScale(new Vector3d(ratio2,1,ratio2));
 												t1.setTransform(tempT1);
 												t2.setTransform(tempT2);
 												PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 											    pickCanvas.setMode(PickCanvas.BOUNDS);
 												m.addBond(moleTrans,t1,t2,moleculeVec.get(tracker).bond.get(j).index[0], moleculeVec.get(tracker).bond.get(j).index[0],l,tracker, pickCanvas);
 												//uniMole.addBranchGraph(moleBranch);
 												//moleTransGroup=null;
 											
 										}
 										sysTransGroup.addChild(moleBranch);
 										tg1.add(moleBranch);
 										mg1.add(m);
 										pos2Vec.add(posVec);
 									
 								
 								actualTransform.add(tg1);
 								actualMolecule.add(mg1);
 								allPositions.add(pos2Vec);
 								
 
 								//RegionMolecules rm = new RegionMolecules(list);
 								//regMol.add(rm);									
 								//tracker+= list.size();
 								tracker+=1;
 								pointer++;
 								return true;
 								
 							}																
 						});
 					
 			}			
 		});
 		JButton jforce = new JButton("Add Force");
 		jforce.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(!regionList.isSelectionEmpty()){
 					final int selected = regionList.getSelectedIndex();					
 					
 					final JFrame jforce = new JFrame("Select Force");					
 					
 					JPanel forcepanel = new JPanel(new GridBagLayout());
 					jforce.add(forcepanel, BorderLayout.CENTER);
 					jforce.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //					
 					JLabel lcentre = new JLabel("Force");
 					
 					final JTextField formula = new JTextField("k1*(x)^2");
 					final DefaultListModel constant = new DefaultListModel();
 					final DefaultListModel value = new DefaultListModel();
 					final JList names = new JList(constant);
 					final JList values = new JList(value);
 					JLabel con = new JLabel("Constant");
 					JLabel val = new JLabel("Value");
 					
 					constant.addElement("k1");
 					value.addElement("1");
 					
 					
 					
 				    JButton jadd = new JButton("Add");
 				    JButton jedit = new JButton("Edit");
 				    JButton jdelete = new JButton("Delete");
 					//ldomain[0].setSelected(true);
 					
 				   
 				    
 				    
 				    jadd.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent e) {
 							if(validate){
 							//jforce.setVisible(false);
 								final JDialog addConst = new JDialog(jforce);
 								addConst.setLayout(new GridBagLayout());
 								JLabel constName = new JLabel("Constant Name");
 								JLabel constValue = new JLabel("Constant Value");
 								final JTextField cname = new JTextField("");
 								final JTextField cval =new JTextField("");
 							
 								JPanel butPanel = new JPanel(new FlowLayout());
 								JButton butOk = new JButton("Add");
 								JButton butCancel = new JButton("Cancel");
 							
 								butOk.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										String s = formula.getText();
 										String sub = cname.getText();
 										if(s.contains(sub)){
 											try{
 												boolean ok = true;
 												for(int i=0; i<constant.size(); i++){
 													if(constant.get(i).equals(sub)){
 														ok = false;
 														break;
 													}
 												}
 												if(ok){
 													Float.parseFloat(cval.getText());
 													constant.addElement(sub);
 													value.addElement(cval.getText());
 													addConst.dispose();
 												}
 												else{
 													main.jtext.setText("Constant name already exists");
 												}
 											}catch(NumberFormatException e){
 												main.jtext.setText("Invalid value of constant");
 											}
 										}else{
 											main.jtext.setText("Constant name should be in formula");
 										}
 									}									
 								});
 								
 								butCancel.addActionListener(new ActionListener(){
 									public void actionPerformed(ActionEvent arg0) {
 										addConst.dispose();
 									}									
 								});
 								addConst.add(constName,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								addConst.add(cname,  new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 								
 								addConst.add(constValue, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5) );
 								addConst.add(cval, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5) );
 								
 								butPanel.add(butOk);
 								butPanel.add(butCancel);
 								addConst.add(butPanel, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5) );
 								
 								addConst.setBounds(400, 150, 300, 200);
 								addConst.setVisible(true);
 							}
 						}				    	
 				    });
 				    JPanel butSubPanel = new JPanel(new FlowLayout());
 					JPanel butPanel = new JPanel(new FlowLayout());
 					JButton butOk = new JButton("Ok");
 					JButton butCancel = new JButton("Cancel");
 					
 					butOk.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							String s = formula.getText();
 								s = s.replaceAll("\\(", "");
 								s =s.replaceAll("\\)", "");
 								s =s.replaceAll("sqrt", "");
 								s =s.replaceAll("exp", "");
 								s =s.replaceAll("log", "");
 								s =s.replaceAll("sin", "");
 								s =s.replaceAll("cos", "");
 								s =s.replaceAll("sec", "");
 								s =s.replaceAll("csc", "");
 								s =s.replaceAll("tan", "");
 								s =s.replaceAll("cot", "");
 								s =s.replaceAll("asin", "");
 								s =s.replaceAll("acos", "");
 								s =s.replaceAll("atan", "");
 								s =s.replaceAll("sinh", "");
 								s =s.replaceAll("tanh", "");
 								s =s.replaceAll("erf", "");
 								s =s.replaceAll("erfc", "");
 								s =s.replaceAll("min", "");
 								s =s.replaceAll("max", "");
 								s =s.replaceAll("abs", "");
 								s =s.replaceAll("step", "");
 								s =s.replaceAll("\\+", "");
 								s =s.replaceAll("\\-", "");
 								s =s.replaceAll("\\*", "");
 								s =s.replaceAll("\\/", "");
 								s =s.replaceAll("\\^", "");		
 								s =s.replaceAll("x", "");		
 								s =s.replaceAll("y", "");		
 								s =s.replaceAll("z", "");		
 								s =s.replaceAll(".", "");
 							//	s =s.replaceAll("E", "");
 								for(int i=0; i<constant.size(); i++){
 									String sub = (String) constant.get(i);
 									s = s.replaceAll(sub, "");
 								}
 								if(s.length() ==0){
 									exp.get(selected).formula = formula.getText();	
 									System.out.println(""+formula.getText());
 									String consts[] = new String[constant.size()];
 									for(int i=0; i<constant.size(); i++){
 										consts[i] = (String) constant.get(i);
 									}
 									String vals[] = new String[value.size()];
 									for(int i=0; i<value.size(); i++){
 										vals[i] = (String) value.get(i);
 									}
 									exp.get(selected).constants = consts;
 									exp.get(selected).values = vals;
 									jforce.dispose();
 								}else{
 									try{
 										Integer.parseInt(s);
 										exp.get(selected).formula = formula.getText();	
 										System.out.println(""+formula.getText());
 										String consts[] = new String[constant.size()];
 										for(int i=0; i<constant.size(); i++){
 											consts[i] = (String) constant.get(i);
 										}
 										String vals[] = new String[value.size()];
 										for(int i=0; i<value.size(); i++){
 											vals[i] = (String) value.get(i);
 										}
 										exp.get(selected).constants = consts;
 										exp.get(selected).values = vals;
 										jforce.dispose();
 									}
 									catch(NumberFormatException e){
 										main.jtext.setText(""+s);
 									}
 									
 								}
 						}			
 					});
 					
 					butCancel.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							jforce.dispose();
 							
 						}			
 					});
 					
 					butPanel.add(butOk);
 					butPanel.add(butCancel);
 					
 					JScrollPane firstList = new JScrollPane(names);
 					JScrollPane secondList = new JScrollPane(values);
 					
 					names.addListSelectionListener(new ListSelectionListener(){
 						public void valueChanged(ListSelectionEvent arg0) {
 							values.setSelectedIndex(names.getSelectedIndex());
 						}
 						
 					});
 					
 					values.addListSelectionListener(new ListSelectionListener(){
 						public void valueChanged(ListSelectionEvent e) {
 							names.setSelectedIndex(values.getSelectedIndex());
 						}						
 					});
 					
 					names.setVisibleRowCount(5);
 					values.setVisibleRowCount(5);
 					//firstList.add(names);
 					//secondList.add(values);
 					
 					butSubPanel.add(jadd);
 					butSubPanel.add(jedit);
 					butSubPanel.add(jdelete);
 					
 					forcepanel.add(lcentre,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 					forcepanel.add(formula,  new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 					
 					forcepanel.add(con, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5) );
 					forcepanel.add(val, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5) );
 					
 					forcepanel.add(firstList,  new GBC(0,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 					forcepanel.add(secondList,  new GBC(1,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 					
 					forcepanel.add(butSubPanel,  new GBC(0,6,2,2).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 					
 					forcepanel.add(butPanel, new GBC(0,8, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 					jforce.setBounds(400, 150, 300, 300);
 					jforce.setVisible(true);
 					jforce.setLayout(new BorderLayout());
 				}
 			}			
 		});
 		JButton makeVirtual = new JButton("V-Domain");
 		makeVirtual.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(!regionList.isSelectionEmpty()){
 					boolean b= !makeStationary.get(regionList.getSelectedIndex());
 					if(b)
 						main.jtext.setText("Domain "+(regionList.getSelectedIndex()+1)+" is virtual");
 					else
 						main.jtext.setText("Domain "+(regionList.getSelectedIndex()+1)+" is not virtual");
 					makeStationary.remove(regionList.getSelectedIndex());
 					makeStationary.add(regionList.getSelectedIndex(), b);
 				}
 			}			
 		});
 		sysToolPane.add(jmol);
 		sysToolPane.add(jregionadd);
 		sysToolPane.add(makeVirtual);
 		//sysToolPane.add(jpolyfill);
 		sysToolPane.add(jsetDim);
 		//sysToolPane.add(jwalladd);
 		if(sysProp.type == "NVT")
 			sysToolPane.add(jforce);
 		sysToolPane.add(jimport);
 		//sysToolPane.add(jregionfill);
 		sysToolPane.add(jregiondel);
 		
 		
 		JButton jsim = new JButton("Simulate");
 		jsim.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(!kaamChalu&&allPositions.size()!=0){
 					setSimWnd();
 				}
 			}			
 		});
 		sysToolPane.add(jsim);
 		jstkit.add(sysToolPane, BorderLayout.CENTER);
 		jstkit.setVisible(true);
 	}
 	@SuppressWarnings("static-access")
 	private File fileSelector(JFrame jregion) {
 		JFileChooser fc = new JFileChooser();
 		fc.setCurrentDirectory(new File(main.workSpace));
 		int f = fc.showOpenDialog(jregion);
 		if(f == JFileChooser.APPROVE_OPTION){
 			FileReader read = null;
 			try {
 				double lx=0, ly=0, lz=0, hx=0, hy=0, hz=0;
 				read = new FileReader(fc.getSelectedFile());
 				BufferedReader in = new BufferedReader(read);
 				int isExport = Integer.parseInt(in.readLine());
 				if(isExport == 1){
 					int noAtom =  Integer.parseInt(in.readLine());
 					for(int j=0; j<noAtom ; j++){
 						StringTokenizer st = new StringTokenizer(in.readLine());
 						for(int k=0; k<10; k++)
 							st.nextToken();
 						double tempx = Double.parseDouble(st.nextToken());
 						double tempy = Double.parseDouble(st.nextToken());
 						double tempz = Double.parseDouble(st.nextToken());
 						if(tempx<lx){
 							lx = tempx;	
 						}
 						else if(tempx>hx){
 							hx = tempx;
 						}
 						if(tempy<ly){
 							ly = tempy;														
 						}
 						else if(tempy>hy){
 							hy = tempy;
 						}
 						if(tempz<lz){
 							lz = tempz;														
 						}
 						else if(tempz>hz){
 							hz = tempz;
 						}
 					}
 					if(majorDim < Math.abs(hx-lx))
 					majorDim = Math.abs(hx-lx);
 					if(Math.abs(hy-ly)>majorDim){
 						majorDim = Math.abs(hy-ly);
 					}
 					if(Math.abs(hz-lz)>majorDim){
 						majorDim = Math.abs(hz-lz);
 					}					
 						return fc.getSelectedFile();
 
 					
 				
 					
 				}
 				
 			} catch (FileNotFoundException e) {
 				main.jtext.setText("File Not Found");
 			} catch (NumberFormatException e) {
 				main.jtext.setText("Invalid Input");
 			} catch (IOException e) {
 				main.jtext.setText("IO Error");
 			}
 			
 		}
 		return null;
 	}
 	@SuppressWarnings("deprecation")
 	public void setSimWnd() {
 		
 		final JFrame simFrame = new JFrame("Simulation");
 		simFrame.setBounds(400, 150, 350, 250);
 		simFrame.setVisible(true);
 		
 		simFrame.setLayout(new BorderLayout());
 		JPanel simPanel = new JPanel(new GridBagLayout());
 		simFrame.add(simPanel, BorderLayout.CENTER);
 		simFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		JPanel simButtonPanel = new JPanel(new FlowLayout());
 		JLabel skipTime = new JLabel("Simulation Time up to Equilibrium (ps)");
 		JLabel simTime = new JLabel("Simulation Time after Equilibrium (ps)");
 		JLabel simStep = new JLabel("Step Size       (ps)");
 		JLabel reportInterval = new JLabel("Reporting Intervals");
 		JLabel saveFrequency = new JLabel("Save Frequency");
 		
 		final JTextField equiText = new JTextField("0");
 		equiText.disable();
 		final JTextField timeText = new JTextField("3");
 		final JTextField stepSize = new JTextField("0.001");
 		final JTextField interText = new JTextField("10");
 		final JTextField saveFreqText = new JTextField("1000");
 		JLabel file = new JLabel("Case");
 		final JTextField fileText = new JTextField("default");
 		
 		
 		
 		JButton simOk = new JButton("Ok");
 		JButton simCancel = new JButton("Cancel");
 		
 		simOk.addActionListener(new ActionListener(){
 			@SuppressWarnings({ "static-access" })
 			public void actionPerformed(ActionEvent arg0) {
 				final File checkFile = new File(main.workSpace+"/"+fileText.getText()+".cas");
 				doIt = false;
 				if(checkFile.exists()){
 					final JDialog diaOverwrite = new JDialog();
 					diaOverwrite.setBounds(500,250,300,150);
 					JPanel p1 = new JPanel(new FlowLayout());
 					JPanel p2 = new JPanel(new BorderLayout());
 					p2.setLayout(new GridLayout(2,1));
 					Container contSaveSys = diaOverwrite.getContentPane();
 					contSaveSys.setLayout(new GridLayout(2,1));
 					JLabel lbl1 = new JLabel("     Case file exists, overwrite?");
 					JButton butYes = new JButton("Yes");
 					butYes.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							caseName = fileText.getText();
 							checkFile.delete();
 							File f = new File(main.workSpace+"/"+caseName+".cas");
 							
 							if(f.canWrite())
 								doIt = true;
 							if(!doIt){
 								main.jtext.setText("Cannot create file, please enter another name");
 								diaOverwrite.dispose();
 								simFrame.setVisible(true);
 							}
 							else{
 								diaOverwrite.dispose();
 								simFrame.setVisible(true);
 							}
 							
 						}							
 					});
 					JButton butNo = new JButton("No");
 					butNo.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0){
 							diaOverwrite.dispose();
 							simFrame.setVisible(true);
 						}
 					});
 					p1.add(butYes);
 					p1.add(butNo);
 					p2.add(lbl1, BorderLayout.CENTER);
 					diaOverwrite.add(p2);
 					diaOverwrite.add(p1);
 					diaOverwrite.setVisible(true);
 				}
 				else{
 					caseName = fileText.getText();
 					doIt = true;
 				}
 				boolean go = false;
 				boolean go1 = false;
 				boolean go2 = false;
 				try{
 					double equiTime = Double.parseDouble(equiText.getText());
 					double totTime = Double.parseDouble(timeText.getText());
 					double step = Double.parseDouble(stepSize.getText());
 					int repInterval = Integer.parseInt(interText.getText());
 					int saveFreq = Integer.parseInt(saveFreqText.getText());
 					saveInterval = saveFreq;
 					if(doIt){
 						
 						go1 = writeCaseFile(new File(main.workSpace+"/"+caseName+".cas"));
 						
 						go2 = writePosFile(new File("coordinates.txt"));
 						go = writeParam(equiTime, totTime, step, repInterval, saveFreq, main.workSpace, caseName);
 						//System.out.println(""+go+" "+go1+" "+go2);
 					}
 					else
 						main.jtext.setText("Select Directory");
 				}catch (NumberFormatException num){
 					main.jtext.setText("Wrong Input");
 				}
 				if(go&&!kaamChalu&&go1&&go2){				
 					kaamChalu = true;
 					Runnable r = new Simulatethis();
 					Thread t = new Thread(r);
 					t.setPriority(Thread.MAX_PRIORITY);
 					t.start();
 					simFrame.dispose();
 				}
 				else
 					main.jtext.setText("Please check your input");
 			}
 		});
 		simCancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				simFrame.dispose();
 			}			
 		});
 		
 		simPanel.add(skipTime, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(equiText, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(simTime, new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(timeText, new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(simStep, new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(stepSize, new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(reportInterval, new GBC(0,3,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 		simPanel.add(interText, new GBC(1,3,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 		simPanel.add(saveFrequency, new GBC(0,4,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 		simPanel.add(saveFreqText, new GBC(1,4,1,1).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 		simPanel.add(file, new GBC(0,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		simPanel.add(fileText, new GBC(1,5,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		
 		simButtonPanel.add(simOk);
 		simButtonPanel.add(simCancel);
 		simPanel.add(simButtonPanel, new GBC(0,6, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5));
 		
 	}
 	
 	@SuppressWarnings("static-access")
 	public boolean writePosFile(File file) {
 		main.jtext.setText(file.getName());
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 			return false;
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 					FileWriter wfile = new FileWriter(file.toString());
 					BufferedWriter out = new BufferedWriter(wfile);
 					BufferedReader in = new BufferedReader(new FileReader("optimizeSystem.mole"));
 					int totalNumber = 0;
 					for(int i=0; i<allPositions.size(); i++){
 						for(int j=0; j<allPositions.get(i).size(); j++){
 							totalNumber+= allPositions.get(i).get(j).size();
 						}
 						
 					}
 					out.write(""+totalNumber);
 					totalNumber =1;
 					for(int i=0; i<allPositions.size(); i++){
 						for(int j=0; j<allPositions.get(i).size(); j++){
 							for(int k=0; k<allPositions.get(i).get(j).size(); k++){
 								out.write("\n"+totalNumber++ +"\t"+allPositions.get(i).get(j).get(k).x/10+"\t"+allPositions.get(i).get(j).get(k).y/10+"\t"+allPositions.get(i).get(j).get(k).z/10);
 							}
 						}
 					}
 					for(int i=1; i<totalNumber; i++){
 						out.write("\n"+i+"\t"+"0.000000"+"\t"+"0.000000"+"\t"+"0.000000");
 					}
 					out.write("\nEnd");
 					out.write("\n"+main.workSpace+"/"+caseName+".dat");
 					in.close();
 					out.close();
 
 				} catch (IOException e) {
 					main.jtext.setText("\nError : Unable to save molecule");
 					return false;
 				}
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 				return false;
 			}
 		}
 		return true;
 	}
 	@SuppressWarnings("unused")
 	private Sphere createPointer() {
 			Appearance app = new Appearance();
 			Color3f objColor = new Color3f(1.0f, 1.0f, 1.0f);
 			Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 			app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
 			Sphere sphere = new Sphere( 0.03f, Primitive.GENERATE_NORMALS, app);
 			return sphere;
 	}
 	@SuppressWarnings("static-access")
 	public static boolean writeCaseFile(File file) {
 		main.jtext.setText(file.getName());
 		//System.out.println(""+file.getName());
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 			return false;
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 					FileWriter wfile = new FileWriter(file.toString());
 					BufferedWriter out = new BufferedWriter(wfile);
 					BufferedWriter out1 = new BufferedWriter(new FileWriter(new File("optimizeSystem.mole")));
 					//BufferedReader in = new BufferedReader(new FileReader("optimizeSystem.mole"));
 					out.write("25011989");
 					
 					//String s = null;
 					/*while(in.ready()){
 						s = in.readLine();
 						out.write("\n"+s);
 					}*/
 					out.write("\n"+moleculeVec.size());
 					out.write("\n"+sysProp.temperature);
 					if(sysProp.type == "NVT"){
 						out.write("\n"+0);
 						out.write("\n"+sysProp.xx);
 						out.write("\n"+sysProp.yy);
 						out.write("\n"+sysProp.zz);
 						if(sysProp.behavior == "Periodic"){
 							out.write("\n"+0);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Periodic-Ewald"){
 							out.write("\n"+1);
 							out.write("\n"+sysProp.cutOff);
 						}
 						
 					}
 					else if(sysProp.type == "NPT"){
 						out.write("\n"+1);
 						out.write("\n"+sysProp.pressure);
 						out.write("\n"+sysProp.xx);
 						out.write("\n"+sysProp.yy);
 						out.write("\n"+sysProp.zz);
 						if(sysProp.behavior == "Periodic"){
 							out.write("\n"+0);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Periodic-Ewald"){
 							out.write("\n"+1);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Non-Periodic"){
 							out.write("\n"+3);
 						}
 					}
 					
 					int totalNumber = 0;
 					for(int i=0; i<allPositions.size(); i++){
 						//for(int j=0; j<allPositions.get(i).size();j++)
 							totalNumber += allPositions.get(i).size();
 					}
 					out.write("\n"+totalNumber);
 					for(int i=0; i<moleculeVec.size();i++){
 						out.write("\n"+allPositions.get(i).size());
 						out.write("\n"+moleculeVec.get(i).atom.size());
 						for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 							
 								
 							Atom a1 =moleculeVec.get(i).atom.get(j);
 							if(makeStationary.get(i))
 								a1.mass = 0.0;
 							Position v1 = moleculeVec.get(i).pos.get(j);
 							//System.out.println("\n"+a1.RFID+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x/0.2+" "+v1.y/0.2+" "+v1.z/0.2);
 							out.write("\n"+a1.RFID+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x+" "+v1.y+" "+v1.z);
 						}
 						out.write("\n"+ moleculeVec.get(i).bond.size());
 						for(int j=0;j<moleculeVec.get(i).bond.size();j++){
 							Bond b = moleculeVec.get(i).bond.get(j);
 							//Atom a1 = moleculeVec.get(i).atom.get(b.index[0]);
 							//Atom a2 = moleculeVec.get(i).atom.get(b.index[1]);
 							if(b.strength == 4)
 								out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 							else
 								out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 						}
 						out.write("\n"+moleculeVec.get(i).angle.size());
 						for(int j=0;j<moleculeVec.get(i).angle.size();j++){
 							out.write("\n"+moleculeVec.get(i).angle.get(j).cAtom+" "+moleculeVec.get(i).angle.get(j).oAtoms[0]+" "+moleculeVec.get(i).angle.get(j).oAtoms[1]+" "+moleculeVec.get(i).angle.get(j).angle+" "+moleculeVec.get(i).angle.get(j).strength);
 						}
 						out.write("\n"+moleculeVec.get(i).torsion.size());
 						for(int j=0;j<moleculeVec.get(i).torsion.size();j++){
 							out.write("\n"+moleculeVec.get(i).torsion.get(j).atom[0]+" "+moleculeVec.get(i).torsion.get(j).atom[1]+" "+moleculeVec.get(i).torsion.get(j).atom[2]+" "+moleculeVec.get(i).torsion.get(j).atom[3]+" "+moleculeVec.get(i).torsion.get(j).forceConst+" "+moleculeVec.get(i).torsion.get(j).angle+" "+moleculeVec.get(i).torsion.get(j).periodicity);
 						}
 					}
 					if(moleculeVec.size() == exp.size()){
 					//for(int i=0; i<)
 						int size = 0;
 						for(int i=0; i<exp.size(); i++){
 							if(exp.get(i).formula != null || exp.get(i).constants != null || exp.get(i).values != null){
 							if(exp.get(i).formula.length()!=0 || exp.get(i).constants.length!=0 || exp.get(i).values.length != 0){
 								size++;
 							}
 							}
 						}
 						out.write("\n"+size);
 						for(int i=0; i<exp.size(); i++){
 							if(exp.get(i).formula != null || exp.get(i).constants != null || exp.get(i).values != null){
 							if(exp.get(i).formula.length()!=0 || exp.get(i).constants.length!=0 || exp.get(i).values.length != 0){
 								out.write("\n"+(i));
 								out.write("\n"+exp.get(i).formula);
 								out.write("\n"+exp.get(i).constants.length);
 								for(int j=0; j<exp.get(i).constants.length; j++){
 									out.write("\n"+exp.get(i).constants[j]+" "+exp.get(i).values[j]);
 								}
 							}
 							}
 						}
 					}else{
 						main.jtext.setText("Error in expression");
 					}
 					out.write("\nEnd");
 					out.write("\n"+main.workSpace+"/"+caseName+".dat");
 					//in.close();
 					out.close();
 					
 					
 					//out1.write("25011989");
 					
 					//String s = null;
 					/*while(in.ready()){
 						s = in.readLine();
 						out.write("\n"+s);
 					}*/
 					out1.write(""+moleculeVec.size());
 					out1.write("\n"+sysProp.temperature);
 					if(sysProp.type == "NVT"){
 						out1.write("\n"+0);
 						out1.write("\n"+sysProp.xx);
 						out1.write("\n"+sysProp.yy);
 						out1.write("\n"+sysProp.zz);
 						if(sysProp.behavior == "Periodic"){
 							out1.write("\n"+0);
 							out1.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Periodic-Ewald"){
 							out1.write("\n"+1);
 							out1.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Confined"){
 							out1.write("\n"+2);
 							out1.write("\n"+sysProp.sigma);
 							out1.write("\n"+sysProp.epsilon);
 							out1.write("\n"+sysProp.cutOff);
 						}
 					}
 					else if(sysProp.type == "NPT"){
 						out1.write("\n"+1);
 						out1.write("\n"+sysProp.pressure);
 						out1.write("\n"+sysProp.xx);
 						out1.write("\n"+sysProp.yy);
 						out1.write("\n"+sysProp.zz);
 						if(sysProp.behavior == "Periodic"){
 							out1.write("\n"+0);
 							out1.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Periodic-Ewald"){
 							out1.write("\n"+1);
 							out1.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Non-Periodic"){
 							out1.write("\n"+3);
 						}
 					}
 					
 					totalNumber = 0;
 					for(int i=0; i<allPositions.size(); i++){
 						//for(int j=0; j<allPositions.get(i).size();j++)
 							totalNumber += allPositions.get(i).size();
 					}
 					out1.write("\n"+totalNumber);
 					for(int i=0; i<moleculeVec.size();i++){
 						out1.write("\n"+allPositions.get(i).size());
 						out1.write("\n"+moleculeVec.get(i).atom.size());
 						for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 							Atom a1 =moleculeVec.get(i).atom.get(j);
 							Position v1 = moleculeVec.get(i).pos.get(j);
 							//System.out1.println("\n"+(j+1)+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x/0.2+" "+v1.y/0.2+" "+v1.z/0.2);
 							out1.write("\n"+(j+1)+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x+" "+v1.y+" "+v1.z);
 						}
 						out1.write("\n"+ moleculeVec.get(i).bond.size());
 						for(int j=0;j<moleculeVec.get(i).bond.size();j++){
 							Bond b = moleculeVec.get(i).bond.get(j);
 							//Atom a1 = moleculeVec.get(i).atom.get(b.index[0]);
 							//Atom a2 = moleculeVec.get(i).atom.get(b.index[1]);
 							if(b.strength == 4)
 								out1.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 							else
 								out1.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 						}
 						out1.write("\n"+moleculeVec.get(i).angle.size());
 						for(int j=0;j<moleculeVec.get(i).angle.size();j++){
 							out1.write("\n"+moleculeVec.get(i).angle.get(j).cAtom+" "+moleculeVec.get(i).angle.get(j).oAtoms[0]+" "+moleculeVec.get(i).angle.get(j).oAtoms[1]+" "+moleculeVec.get(i).angle.get(j).angle+" "+moleculeVec.get(i).angle.get(j).strength);
 						}
 						out1.write("\n"+moleculeVec.get(i).torsion.size());
 						for(int j=0;j<moleculeVec.get(i).torsion.size();j++){
 							out1.write("\n"+moleculeVec.get(i).torsion.get(j).atom[0]+" "+moleculeVec.get(i).torsion.get(j).atom[1]+" "+moleculeVec.get(i).torsion.get(j).atom[2]+" "+moleculeVec.get(i).torsion.get(j).atom[3]+" "+moleculeVec.get(i).torsion.get(j).forceConst+" "+moleculeVec.get(i).torsion.get(j).angle+" "+moleculeVec.get(i).torsion.get(j).periodicity);
 						}
 					}
 					if(moleculeVec.size() == exp.size()){
 						//for(int i=0; i<)
 						int size = 0;
 						for(int i=0; i<exp.size(); i++){
 							if(exp.get(i).formula != null || exp.get(i).constants != null || exp.get(i).values != null){
 							if(exp.get(i).formula.length()!=0 || exp.get(i).constants.length!=0 || exp.get(i).values.length != 0){
 								size++;
 							}
 							}
 						}
 						out1.write("\n"+size);
 							for(int i=0; i<exp.size(); i++){
 								if(exp.get(i).formula != null || exp.get(i).constants != null || exp.get(i).values != null)
 								if(exp.get(i).formula.length()!=0 || exp.get(i).constants.length!=0 || exp.get(i).values.length != 0){
 									out1.write("\n"+(i));
 									out1.write("\n"+exp.get(i).formula);
 									out1.write("\n"+exp.get(i).constants.length);
 									for(int j=0; j<exp.get(i).constants.length; j++){
 										out1.write("\n"+exp.get(i).constants[j]+" "+exp.get(i).values[j]);
 									}
 								}
 							}
 						}else{
 							main.jtext.setText("Error in expression");
 						}
 					out1.write("\nEnd");
 					out1.write("\n"+main.workSpace+"/"+caseName+".dat");
 					//in.close();
 					out1.close();
 				} catch (IOException e) {
 					main.jtext.setText("\nError : Unable to save molecule");
 					return false;
 				}
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	@SuppressWarnings("static-access")
 	public boolean writeParam(double equiTime, double totTime, double stepSize, int repInterval, int saveFreq, String path, String name){
 		File file = new File("simulationParameters.mole");
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 					FileWriter wfile = new FileWriter(file.toString());
 					BufferedWriter out = new BufferedWriter(wfile);
 					out.write(""+(equiTime * 1000));
 					out.write("\n"+(totTime*1000));
 					out.write("\n"+(stepSize*1000));
 					out.write("\n"+repInterval);
 					out.write("\n"+saveFreq);
 					out.write("\n"+path);
 					out.write("/"+name);
 					out.write("\nEnd");
 					out.close();
 				} catch (IOException e) {
 					main.jtext.setText("\nError : Unable to save molecule");
 					return false;
 				}
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 				return false;
 			}
 		}
 		return true;
 		
 	}
 	private static void addLights(BranchGroup moleBranchGroup) {
 		Color3f color = new Color3f( 1.0f,1.0f,1.0f );
 		Vector3f direction = new Vector3f( 0.0f, -0.0f, -2.0f );
 		DirectionalLight light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		moleBranchGroup.addChild( light );
 		direction = new Vector3f( -0.0f, 0.0f, 2.0f );
 		light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		moleBranchGroup.addChild( light );
 		direction = new Vector3f( 0.0f, -2.0f, 0.0f );
 		light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		moleBranchGroup.addChild( light );
 		
 
 	}
 	public static void tryToPick(MouseEvent e){
 		
 	}
 	@SuppressWarnings("static-access")
 	public void sysFrame(){
 		jsys = main.mainPane;
 		GraphicsConfiguration confMole = SimpleUniverse.getPreferredConfiguration();
 		canvasSys =  new Canvas3D(confMole);
 		regionBranch = new Vector<BranchGroup>();
 		uniSys =  new SimpleUniverse(canvasSys);
 		
 		sysBranchGroup = new BranchGroup();
 		sysBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		sysBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		sysBranchGroup.setCapability(sysBranchGroup.ALLOW_CHILDREN_EXTEND);  
 		sysBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
 	
 		sysTransGroup = new TransformGroup();
 		sysTransGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
 		sysTransGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
 		sysTransGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		sysTransGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		sysBranchGroup.addChild(sysTransGroup);
 		addLights( sysBranchGroup );
 		
 		createBox(sysProp.xx * 0.1f, sysProp.yy*0.1f, sysProp.zz * 0.1f);
 		
 		
 		canvasSys.addMouseListener(new MouseListener(){
 			public void mouseClicked(MouseEvent arg0) {
 				if(mousePos!= null){
 					mousePos.x=arg0.getX();
 					mousePos.y=arg0.getY();
 					mousePos.z=0;
 				}
 				if(!arg0.isShiftDown()){
 					tryToPick(arg0);
 				}
 			}
 			public void mouseEntered(MouseEvent arg0) {}
 			public void mouseExited(MouseEvent arg0) {}
 			public void mousePressed(MouseEvent arg0) {
 				
 				
 			}
 			public void mouseReleased(MouseEvent arg0) {
 				if(dragged ){
 					selectedId = 9999;
 					dragged = false;
 				}
 			}
 		});
 		canvasSys.addMouseWheelListener(new MouseWheelListener(){
 
 			public void mouseWheelMoved(MouseWheelEvent arg0) {
 				if(arg0.getWheelRotation()<0){
 					scale-=0.00107;
 					
 				}
 				else if(arg0.getWheelRotation()>0){
 					scale+=0.00107;
 				}
 			}
 		});
 		
 		canvasSys.addMouseMotionListener(new MouseMotionListener(){
 			public void mouseDragged(MouseEvent arg0) {
 				
 			/*	if(arg0.isShiftDown()){
 					float y =-(float) (((float)arg0.getY()-325)*0.88*scale*200/600);
 					if(mousePos.y < y){
 						//mousePos.x = (float) (((float)arg0.getX()-613)*2*scale*200/1350); 
 						
 						mousePos.z = mousePos.z + 0.1f ;
 						Vector3f pos = new Vector3f(mousePos.x, mousePos.y, mousePos.z);
 						Transform3D trans = new Transform3D();
 						trans.set(pos);
 						mousePoint.setTransform(trans);
 						
 					}
 					else{
 						//mousePos.x = (float) (((float)arg0.getX()-613)*2*scale*200/1350); 
 						mousePos.z = mousePos.z - 0.1f;
 						Vector3f pos = new Vector3f(mousePos.x, mousePos.y, mousePos.z);
 						Transform3D trans = new Transform3D();
 						trans.set(pos);
 						mousePoint.setTransform(trans);
 						
 					}
 				}*/
 				/*if(!arg0.isShiftDown()){
 					for(int i=0;i<selectedMol.total;i++){
 						selectedMol.pk.get(i).setShapeLocation(arg0);
 					    PickResult result =selectedMol.pk.get(i).pickClosest();
 					    if (result != null) {
 					       Shape3D s = (Shape3D)result.getNode(PickResult.SHAPE3D);
 					       if (s != null) {
 					    	   main.jtext.setText("\nSelected "+selectedMol.atoms.get(i).name);
 					    	   selectedId = i;
 					    	   break;
 					       }
 					    }
 					}
 					if(selectedMol.bondTotal !=0){
 						yesb=0;
 						toMove = new Vector<Num>();
 						for(int i=0;i<selectedMol.bondTotal;i++)
 						{
 							if(selectedMol.bonds.get(i).index[0]==selectedId){
 								yesb++;
 								Num m=new Num();
 								m.n =i;
 								toMove.add(m);
 							}
 							else if(selectedMol.bonds.get(i).index[1]==selectedId){
 								yesb++;
 								Num m=new Num();
 								m.n =i;
 								toMove.add(m);
 							}
 						}
 						if(yesb==0){
 							toMove=null;
 						}
 					}
 					orbit.setRotateEnable(false);
 					float x = (float) (((float)arg0.getX()-600)*2*scale*200/1200); 
 					float y = -(float) (((float)arg0.getY()-304)*0.88*scale*200/608);
 					if(selectedMol.total!=0&&selectedId!=9999){
 								dragAtom(x,y);
 								dragged = true;
 					}
 				}
 				else if(arg0.isShiftDown()){
 					orbit.setRotateEnable(true);
 					
 					mousePos.x=arg0.getX();
 					mousePos.y=arg0.getY();
 				}*/
 			}
 			public void mouseMoved(MouseEvent arg0) {
 			}		
 		});
 		orbit = new OrbitBehavior(canvasSys,orbit.DISABLE_TRANSLATE );
 	    orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
 	    uniSys.getViewingPlatform().setViewPlatformBehavior(orbit);
 	   	uniSys.addBranchGraph(sysBranchGroup);
 	    //view = uniSys.getViewingPlatform();
 	   	orbit.setRotationCenter(new Point3d(sysProp.xx/2*0.1, sysProp.yy/2*0.1, sysProp.zz/2*0.1));
 	    TransformGroup VpTG = uniSys.getViewingPlatform().getViewPlatformTransform();
 
 	    float Zcamera = 10; //put the camera 12 meters back
 	    Transform3D Trfcamera = new Transform3D();
 	    Trfcamera.setTranslation(new Vector3f(sysProp.xx/2*0.1f, sysProp.yy/2*0.1f, Zcamera));  
 	    VpTG.setTransform( Trfcamera );
 		//view.setNominalViewingTransform();
 		main.mainPane.add(canvasSys, BorderLayout.CENTER);
 		main.mainPane.setBounds(0, 0, (int) (main.width/1.151770658), (int) (main.height/1.181538462));
 		main.setExtendedState(JFrame.MAXIMIZED_BOTH); 
 	}
 	
 	public void createBox(float x, float y, float z) {
 		if(boxGroup!=null){
 			boxGroup.detach();
 			boxGroup.removeAllChildren();
 			boxGroup = null;
 		}
 		
 		boxGroup = new BranchGroup();
 		boxGroup.removeAllChildren();
 		boxGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		boxGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		boxGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 		boxGroup.setCapability(BranchGroup.ALLOW_DETACH);
 		
 		LineArray line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, 0));
 		line1.setCoordinate(1, new Point3d(x, 0, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, 0));
 		line1.setCoordinate(1, new Point3d(0, y, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, 0));
 		line1.setCoordinate(1, new Point3d(0, 0, z));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, y, z));
 		line1.setCoordinate(1, new Point3d(0, y, z));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, y, z));
 		line1.setCoordinate(1, new Point3d(x, y, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, y, z));
 		line1.setCoordinate(1, new Point3d(x, 0, z));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, y, 0));
 		line1.setCoordinate(1, new Point3d(x, 0, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, y, 0));
 		line1.setCoordinate(1, new Point3d(0, y, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, z));
 		line1.setCoordinate(1, new Point3d(0, y, z));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, y, z));
 		line1.setCoordinate(1, new Point3d(0, y, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, 0, z));
 		line1.setCoordinate(1, new Point3d(x, 0, 0));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, 0, z));
 		line1.setCoordinate(1, new Point3d(0, 0, z));
 		boxGroup.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(x, y, z));
 		line1.setCoordinate(1, new Point3d(x, 0, z));
 		boxGroup.addChild(new Shape3D(line1));
 		sysBranchGroup.addChild(boxGroup);
 	}
 	/*@SuppressWarnings("static-access")
 	public static void load(File f, int no){
 		try{
 			BufferedReader  in = new BufferedReader(new FileReader(f));
 			in.readLine();
 			
 			int noAtoms = 0;
 			int noBonds = 0;
 			int noAngles = 0;
 			int noTorsions = 0;
 			Vector<Atom> atoms = new Vector<Atom>();
 			Vector<Vector3f> tempVec = new Vector<Vector3f>();
 			Vector<Bond> bonds = new Vector<Bond>();
 			Vector<Angles> angles = new Vector<Angles>();
 			Vector<Torsion> torsions = new Vector<Torsion>();
 			noAtoms = Integer.parseInt(in.readLine());
 			for(int i=0;i<noAtoms;i++){
 				String s = in.readLine();		
 				StringTokenizer st1 = new StringTokenizer(s);
 				Atom a = new Atom(Integer.parseInt(st1.nextToken()),st1.nextToken(),Double.parseDouble(st1.nextToken()),Double.parseDouble(st1.nextToken()),Double.parseDouble(st1.nextToken()),Double.parseDouble(st1.nextToken()),Double.parseDouble(st1.nextToken()),Double.parseDouble(st1.nextToken()),Double.parseDouble(st1.nextToken()), Integer.parseInt(st1.nextToken()));
 				Vector3f temp = new Vector3f(Float.parseFloat(st1.nextToken()),Float.parseFloat(st1.nextToken()),Float.parseFloat(st1.nextToken()));
 				tempVec.add(temp);
 				atoms.add(a);
 			}
 			noBonds = Integer.parseInt(in.readLine());
 			for(int i=0;i<noBonds;i++){
 				String s = in.readLine();
 				StringTokenizer st = new StringTokenizer(s);
 				int a1=Integer.parseInt(st.nextToken());
 				int a2=Integer.parseInt(st.nextToken());
 				double l = Double.parseDouble(st.nextToken());
 				int str = Integer.parseInt(st.nextToken());
 				Bond b = new Bond();
 				b.bond(a1, a2, l, str);
 				bonds.add(b);
 			}
 			noAngles = Integer.parseInt(in.readLine());
 			for(int i=0; i<noAngles; i++){
 				String s = in.readLine();
 				StringTokenizer st = new StringTokenizer(s);
 				int a1 = Integer.parseInt(st.nextToken());
 				int a2 = Integer.parseInt(st.nextToken());
 				int a3 = Integer.parseInt(st.nextToken());
 				double an = Double.parseDouble(st.nextToken());
 				double str = Double.parseDouble(st.nextToken());
 				Angles ang = new Angles();
 				ang.newAngle(a1, a2, a3, an, str);
 				angles.add(ang);
 			}
 			noTorsions = Integer.parseInt(in.readLine());
 			for(int i=0; i<noTorsions; i++){
 				String s = in.readLine();
 				StringTokenizer st = new StringTokenizer(s);
 				int a1 = Integer.parseInt(st.nextToken());
 				int a2 = Integer.parseInt(st.nextToken());
 				int a3 = Integer.parseInt(st.nextToken());
 				int a4 = Integer.parseInt(st.nextToken());
 				double k = Double.parseDouble(st.nextToken());
 				double theta = Double.parseDouble(st.nextToken());
 				double n = Double.parseDouble(st.nextToken());
 				Torsion tor = new Torsion();
 				tor.addTorsion(a1, a2, a3, a4, k, theta, n);
 				torsions.add(tor);
 			}
 				//SimpleMolecule molecule = new SimpleMolecule();
 				//molecule.initiateMolecule(atoms, noAtoms, tempVec, bonds, noBonds);
 				//molecule.setAngles(angles, noAngles);
 				//molecule.setTorsions(torsions, noTorsions);
 			
 		} catch (IOException e) {main.jtext.setText("\nError : Cannot read file");}
 	}*/
 	
 	/*
 	private class InterruptScheduler extends TimerTask
 	{
 		Thread target = null;
 
 		public InterruptScheduler(Thread target){
 			this.target = target;
 		}
 		@Override
 		public void run(){
 			target.interrupt();
 		}
 	}*/
 	/*public static void globalMinimize(){
 		File file = new File("optimizeSystem.mole");
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 					FileWriter wfile = new FileWriter(file.toString());
 					BufferedWriter out = new BufferedWriter(wfile);
 					out.write(""+sysProp.noOfComponents);
 					out.write("\n"+Temperature);
 					out.write("\n"+sysProp.maxSigma);
 					if(sysProp.type == "NVT"){
 						out.write("\n"+0);
 						out.write("\n"+sysProp.xx);
 						out.write("\n"+sysProp.yy);
 						out.write("\n"+sysProp.zz);
 						if(sysProp.behavior == "Periodic"){
 							out.write("\n"+0);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Periodic-Ewald"){
 							out.write("\n"+1);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Confined"){
 							out.write("\n"+2);
 							out.write("\n"+sysProp.sigma);
 							out.write("\n"+sysProp.epsilon);
 							out.write("\n"+sysProp.cutOff);
 						}
 					}
 					else if(sysProp.type == "NPT"){
 						out.write("\n"+1);
 						out.write("\n"+sysProp.pressure);
 						out.write("\n"+sysProp.xx);
 						out.write("\n"+sysProp.yy);
 						out.write("\n"+sysProp.zz);
 						if(sysProp.behavior == "Periodic"){
 							out.write("\n"+0);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Periodic-Ewald"){
 							out.write("\n"+1);
 							out.write("\n"+sysProp.cutOff);
 						}
 						else if(sysProp.behavior == "Non-Periodic"){
 							out.write("\n"+3);
 						}
 					}
 					out.write("\n"+sysProp.totalNumber);
 					for(int i=0; i<sysProp.noOfComponents;i++){
 						out.write("\n"+sysProp.moleNumbers[i]);
 						out.write("\n"+simpleMolecule[i].atomLength);
 						for(int j=0; j<simpleMolecule[i].atomLength; j++){
 							Atom a1 =simpleMolecule[i].atoms.get(j);
 							Vector3f v1 = simpleMolecule[i].pos.get(j);
 							out.write("\n"+a1.RFID+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x+" "+v1.y+" "+v1.z);
 						}
 						out.write("\n"+simpleMolecule[i].bondLength);
 						for(int j=0;j<simpleMolecule[i].bondLength;j++){
 							Bond b = simpleMolecule[i].bonds.get(j);
 							Atom a1 = simpleMolecule[i].atoms.get(b.index[0]);
 							Atom a2 = simpleMolecule[i].atoms.get(b.index[1]);
 							if(b.strength == 4)
 								out.write("\n"+b.index[0]+" "+b.index[1]+" "+(a1.bondLength+a2.bondLength)+" "+1.5*700);
 							else
 								out.write("\n"+b.index[0]+" "+b.index[1]+" "+(a1.bondLength+a2.bondLength)+" "+b.strength*700);
 						}
 						out.write("\n"+simpleMolecule[i].angleLength);
 						for(int j=0;j<simpleMolecule[i].angleLength;j++){
 							out.write("\n"+simpleMolecule[i].angles.get(j).cAtom+" "+simpleMolecule[i].angles.get(j).oAtoms[0]+" "+simpleMolecule[i].angles.get(j).oAtoms[1]+" "+simpleMolecule[i].angles.get(j).angle+" "+simpleMolecule[i].angles.get(j).strength);
 						}
 						out.write("\n"+simpleMolecule[i].torsionLength);
 						for(int j=0;j<simpleMolecule[i].torsionLength;j++){
 							out.write("\n"+simpleMolecule[i].torsions.get(j).atom[0]+" "+simpleMolecule[i].torsions.get(j).atom[1]+" "+simpleMolecule[i].torsions.get(j).atom[2]+" "+simpleMolecule[i].torsions.get(j).atom[3]+" "+simpleMolecule[i].torsions.get(j).forceConst+" "+simpleMolecule[i].torsions.get(j).angle+" "+simpleMolecule[i].torsions.get(j).periodicity);
 						}
 					}
 					out.write("\nEnd\n");
 					out.close();
 					try{
 						//ProcessBuilder process = new ProcessBuilder("./minimize");
 						//Map env = process.environment();
 						//Process p = null;
 						//env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/omkar/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 						//env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 						
 						//p = ((ProcessBuilder) process).start();
 						
 						
 						String[] command = {"./createSystem"};
 				        ProcessBuilder probuilder = new ProcessBuilder( command );
 				        Map env = probuilder.environment();
 						env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/omkar/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 						env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 						env.put("OPENMM_PLUGIN_DIR", "/home/omkar/Library/OpenMM3.0-Linux64/lib/plugins");
 //						env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/sanatpc/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 //						env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 //						env.put("OPENMM_PLUGIN_DIR", "/home/sanatpc/Library/OpenMM3.0-Linux64/lib/plugins");
 //						env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/sim/Documents/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 //						env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 //						env.put("OPENMM_PLUGIN_DIR", "/home/sim/Documents/OpenMM3.0-Linux64/lib/plugins");
 						
 				        Process process = probuilder.start();
 						InputStream is = process.getInputStream();
 				        InputStreamReader isr = new InputStreamReader(is);
 				        BufferedReader br = new BufferedReader(isr);
 				        String line;
 				        System.out.printf("Output of running %s is:\n",
 				                Arrays.toString(command));
 				        while ((line = br.readLine()) != null) {
 				        	System.out.println(line);
 				        }
 				        
 				        int exitValue;
 						try {
 							exitValue = process.waitFor();
 							System.out.println("\n\nExit Value is " + exitValue);
 							BufferedReader in1 = new BufferedReader(new FileReader(new File("coordinates.txt")));
 							nos = Integer.parseInt(in1.readLine());
 							System.out.println("\n"+nos);
 							in1.close();
 							if(exitValue == 0){
 								kaamChalu = false;
 								main.jtext.setText("System Created Successfully");
 								getFileName();
 							}
 							else {
 								main.jtext.setText("System Creation Failure, retrying "+retries);
 								retries++;
 								if(retries > 100){
 									main.jtext.setText("Please reset the system with more size or less number of molecules");
 									return;
 								}
 								globalMinimize();								
 							}
 						} 
 						catch (InterruptedException e){
 							process.destroy();
 							main.jtext.setText("Process terminated, taking too long time");
 						}
 					}catch(IOException e){
 						e.printStackTrace();
 					}
 				} catch (IOException e) {
 					main.jtext.setText("\nError : Unable to save molecule");
 				}
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 			}
 		}
 	}*/
 	
 
 	@SuppressWarnings("static-access")
 	public static void clear(){
 		tracker = 0;
 		moleCount = 0;
 		sysProp = null;
 		jstkit.setVisible(false);
 		main.jdesk.remove(jstkit);
 		jstkit = null;
 		jsys=null;
 		
 
 		for(int i=simpleMolecule.size()-1; i>=0; i--){
 			simpleMolecule.remove(i);
 		}
 		simpleMolecule = null;
 		for(int i=actualMolecule.size()-1; i>=0; i--){
 			actualMolecule.remove(i);
 		}
 		actualMolecule = null;
 		for(int i= 0; i<actualTransform.size(); i++){
 			for(int k=0; k<actualTransform.get(i).size(); k++){
 				actualTransform.get(i).get(k).detach();
 			}
 		}
 		actualTransform = null;
 		allPositions = null;
 		allAtoms = null;
 		uniSys.cleanup();
 		uniSys = null;
 		main.mainPane.remove(canvasSys);
 		main.sBuilder = null;
 		
 		moleculeVec.removeAllElements();
 		moleculeVec = null;
 		main.working = false;
 		moleculeVec = null;
 	}
 	@SuppressWarnings("static-access")
 	private static void destroy(){
 		tracker = 0;
 		moleCount = 0;
 		sysProp = null;
 		jstkit.setVisible(false);
 		main.jdesk.remove(jstkit);	
 		canvasSys = null;
 		uniSys = null;
 		selectedId = 9999;
 		mousePos = null;
 		jstkit = null;
 		jsys=null;
 		main=null;
 		main.sBuilder = null;		
 		main.working = false;
 		main = null;
 		System.exit(0);
 	}
 	@SuppressWarnings({ })
 	public static void simulate(){
 		File f = new File("checkIt.txt");
 		boolean doIt = false;
 		if(f.exists()){
 			doIt = f.delete();
 		}
 		else{
 			doIt = true;
 		}
 		if(doIt){
 		String[] command1 = {"./simulate"};
 		ProcessBuilder probuilder1 = new ProcessBuilder( command1 );
 		Map<String, String> env1 = probuilder1.environment();
 		probuilder1.redirectErrorStream(true);
 		env1.put("LD_LIBRARY_PATH", "openmm/lib:"+Darshan.ld_library_path);
 		env1.put("OPENMM_PLUGIN_DIR", "openmm/lib/plugins");
 //		env1.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/sanatpc/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 //		env1.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 //		env1.put("OPENMM_PLUGIN_DIR", "/home/sanatpc/Library/OpenMM3.0-Linux64/lib/plugins");
 //		env1.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/omkar/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 //		env1.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 //		env1.put("OPENMM_PLUGIN_DIR", "/home/omkar/Library/OpenMM3.0-Linux64/lib/plugins");
 //		env1.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/sim1/Work/Softwares/OpenMM/lib:/usr/lib64:/lib:/usr/lib:/usr/local/lib");
 //		env1.put("PATH", "/home/sim1/Work/Simulations/NewWork/jre1.6.0_30/bin:/usr/local/cuda/bin:$PATH");
 //		env1.put("OPENMM_PLUGIN_DIR", "/home/sim1/Work/Softwares/OpenMM/lib/plugins");
 		Process process1 = null;
 		try {
 			process1 = probuilder1.start();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		if(process1 != null){
 			InputStream is1 = process1.getInputStream();
 			InputStreamReader isr1 = new InputStreamReader(is1);
 			BufferedReader br1 = new BufferedReader(isr1);
 			String line1;
 			//System.out.println("Output of running "+Arrays.toString(command1)+"is:\n");
 			try {
 				while ((line1 = br1.readLine()) != null) {
 					System.out.println(line1);
 				}
 				//int exitValue =
 				process1.waitFor();
 				/*if( false){
 					final JDialog diaNewPost = new JDialog();
 					diaNewPost.setBounds(500,250,300,150);
 					diaNewPost.setLayout(new BorderLayout());
 					JPanel p1 = new JPanel(new FlowLayout());
 					JPanel p2 = new JPanel();
 					p2.setLayout(new GridLayout(2,1));
 					Container contSaveSys = diaNewPost.getContentPane();
 					contSaveSys.setLayout(new GridLayout(2,1));
 					diaNewPost.setTitle("Simulation Complete...");
 					JLabel lbl2 = new JLabel("                 Go To Post-Processor?");
 					JButton butYes = new JButton("OK");
 					JButton butNo = new JButton("Cancel");
 					butYes.addActionListener(new ActionListener(){
 						@SuppressWarnings("static-access")
 						public void actionPerformed(ActionEvent arg0) {
 							PostProcessor p1 = new PostProcessor();
 							if(p1.initializePostProcessor(main, new File(main.workSpace + "/" +caseName +".cas"))){
 								p1.dataFile = new File(main.workSpace+ "/" + caseName +".dat");
 								diaNewPost.dispose();
 								jstkit.setVisible(false);
 								main.jdesk.remove(jstkit);
 								main.post = p1;
 								main.post.loadVisualizer();
 								clear();								
 							}				
 						}							
 					});
 					butNo.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							diaNewPost.dispose();
 						}						
 					});
 					p1.add(butYes);
 					p1.add(butNo);
 					p2.add(lbl2);
 					diaNewPost.add(p2, BorderLayout.CENTER);
 					diaNewPost.add(p1, BorderLayout.SOUTH);
 					diaNewPost.show();
 				}*/
 				kaamChalu = false;
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		}
 	}
 	private static Sphere createSphere(int color) {
 		Appearance app = new Appearance();
 		Color n = new Color();
		n.newColor(color-1);
 		Color3f objColor = new Color3f(n.r, n.g, n.b);
 		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 		app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
 		Sphere sphere = new Sphere( 0.03f, Primitive.GENERATE_NORMALS, app);
 		return sphere;
 	}
 	private static Cylinder createCylinder(double length, int i) {
 		
 		Appearance app = new Appearance();
 			Color3f whiteColor = new Color3f(1.0f, 1.0f, 1.0f);
 			Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 			Color3f redColor = new Color3f(0.5f, 0.5f, 0.5f);
 			if(i==0){
 				app.setMaterial(new Material(whiteColor, black, whiteColor, whiteColor, 80.0f));
 				Cylinder cylinder = new Cylinder( 0.01f,(float)length, app);
 				return cylinder;
 			}
 			else if(i==1){
 				app.setMaterial(new Material(redColor, black, redColor, redColor, 80.0f));
 				Cylinder cylinder = new Cylinder( 0.006f,(float)length, app);
 				return cylinder;
 			}
 			else {
 				app.setMaterial(new Material(whiteColor, black, whiteColor, whiteColor, 80.0f));
 				Cylinder cylinder = new Cylinder( 0.0065f,(float)length, app);
 				return cylinder;
 			}
 		
 	}
 	@SuppressWarnings({ "deprecation" })
 	public  void askSysSave(final String state){
 		if(state == "Exit"){
 			final JDialog diaSaveSys = new JDialog();
 			diaSaveSys.setBounds(500,250,300,135);
 			JPanel p1,p2;
 			p1= new JPanel();
 			p2= new JPanel();
 			
 			Container contSaveSys = diaSaveSys.getContentPane();
 			contSaveSys.setLayout(new GridLayout(2,1));
 			JLabel lblSave = new JLabel("Save Changes to System ?");
 			JButton butYes = new JButton("Yes"); 
 			butYes.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					diaSaveSys.dispose();
 					saveSys();
 					destroy();
 				}
 			});
 			JButton butNo = new JButton("No");
 			butNo.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					diaSaveSys.dispose();
 					destroy();
 				}
 			});
 			p1.add(lblSave);
 			p2.add(butYes);
 			p2.add(butNo);
 			diaSaveSys.add(p1);
 			diaSaveSys.add(p2);
 			diaSaveSys.show(true);
 		}
 		else if(state == "Save"){
 			saveSys();
 			//main.jtext.setText("Save option not ready");
 		}
 		else if(state == "Clear"){
 			final JDialog diaSaveSys = new JDialog();
 			diaSaveSys.setBounds(500,250,300,135);
 			JPanel p1,p2;
 			p1= new JPanel();
 			p2= new JPanel();
 			
 			Container contSaveSys = diaSaveSys.getContentPane();
 			contSaveSys.setLayout(new GridLayout(2,1));
 			JLabel lblSave = new JLabel("Save Changes to System ?");
 			JButton butYes = new JButton("Yes"); 
 			butYes.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					diaSaveSys.dispose();
 					saveSys();
 					clear();
 				}
 			});
 			JButton butNo = new JButton("No");
 			butNo.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0) {
 					diaSaveSys.dispose();
 					clear();
 				}
 			});
 			p1.add(lblSave);
 			p2.add(butYes);
 			p2.add(butNo);
 			diaSaveSys.add(p1);
 			diaSaveSys.add(p2);
 			diaSaveSys.show(true);
 		}
 	}
 	
 	@SuppressWarnings("static-access")
 	private static void saveSys(){
 		JFileChooser jfc = new JFileChooser();
 		jfc.setCurrentDirectory(new File(main.workSpace));
 		int f = jfc.showSaveDialog(main.frame);
 		if(f == JFileChooser.APPROVE_OPTION){
 			File file = jfc.getSelectedFile();
 		try {
 			file = new File(file.getAbsolutePath()+".sys");
 			file.createNewFile();
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot create new file");
 		}
 		if(file.exists()){
 			if(file.canWrite()){
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					main.jtext.setText("\nError : Cannot create new file");
 					return ;
 				}
 				if(file.exists()){
 					if(file.canWrite()){
 						try {
 							FileWriter wfile = new FileWriter(file.toString());
 							BufferedWriter out = new BufferedWriter(wfile);
 							//BufferedReader in = new BufferedReader(new FileReader("optimizeSystem.mole"));
 							out.write("28041986");							
 							//String s = null;
 							/*while(in.ready()){
 								s = in.readLine();
 								out.write("\n"+s);
 							}*/
 							out.write("\n"+moleculeVec.size());
 							out.write("\n"+sysProp.temperature);
 							if(sysProp.type == "NVT"){
 								out.write("\n"+0);
 								out.write("\n"+sysProp.xx);
 								out.write("\n"+sysProp.yy);
 								out.write("\n"+sysProp.zz);
 								if(sysProp.behavior == "Periodic"){
 									out.write("\n"+0);
 									out.write("\n"+sysProp.cutOff);
 								}
 								else if(sysProp.behavior == "Periodic-Ewald"){
 									out.write("\n"+1);
 									out.write("\n"+sysProp.cutOff);
 								}
 								
 							}
 							else if(sysProp.type == "NPT"){
 								out.write("\n"+1);
 								out.write("\n"+sysProp.pressure);
 								out.write("\n"+sysProp.xx);
 								out.write("\n"+sysProp.yy);
 								out.write("\n"+sysProp.zz);
 								if(sysProp.behavior == "Periodic"){
 									out.write("\n"+0);
 									out.write("\n"+sysProp.cutOff);
 								}
 								else if(sysProp.behavior == "Periodic-Ewald"){
 									out.write("\n"+1);
 									out.write("\n"+sysProp.cutOff);
 								}
 								else if(sysProp.behavior == "Non-Periodic"){
 									out.write("\n"+3);
 								}
 							}
 							
 							int totalNumber = 0;
 							for(int i=0; i<allPositions.size(); i++){
 								//for(int j=0; j<allPositions.get(i).size();j++)
 									totalNumber += allPositions.get(i).size();
 							}
 							out.write("\n"+totalNumber);
 							for(int i=0; i<moleculeVec.size();i++){
 								out.write("\n"+regions.get(i).exclude);
 								out.write("\n"+regions.get(i).sizex+"\n"+regions.get(i).sizey+"\n"+regions.get(i).sizez);
 								out.write("\n"+regions.get(i).x+"\n"+regions.get(i).y+"\n"+regions.get(i).z);
 								out.write("\n"+allPositions.get(i).size());
 								out.write("\n"+moleculeVec.get(i).atom.size());
 								for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 									
 										
 									Atom a1 =moleculeVec.get(i).atom.get(j);
 									if(makeStationary.get(i))
 										a1.mass = 0.0;
 									Position v1 = moleculeVec.get(i).pos.get(j);
 									//System.out.println("\n"+a1.RFID+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x/0.2+" "+v1.y/0.2+" "+v1.z/0.2);
 									out.write("\n"+a1.RFID+" "+a1.mass+" "+a1.charge+" "+a1.sigma+" "+a1.epsilon+" "+v1.x+" "+v1.y+" "+v1.z);
 								}
 								out.write("\n"+ moleculeVec.get(i).bond.size());
 								for(int j=0;j<moleculeVec.get(i).bond.size();j++){
 									Bond b = moleculeVec.get(i).bond.get(j);
 									//Atom a1 = moleculeVec.get(i).atom.get(b.index[0]);
 									//Atom a2 = moleculeVec.get(i).atom.get(b.index[1]);
 									if(b.strength == 4)
 										out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 									else
 										out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 								}
 								out.write("\n"+moleculeVec.get(i).angle.size());
 								for(int j=0;j<moleculeVec.get(i).angle.size();j++){
 									out.write("\n"+moleculeVec.get(i).angle.get(j).cAtom+" "+moleculeVec.get(i).angle.get(j).oAtoms[0]+" "+moleculeVec.get(i).angle.get(j).oAtoms[1]+" "+moleculeVec.get(i).angle.get(j).angle+" "+moleculeVec.get(i).angle.get(j).strength);
 								}
 								out.write("\n"+moleculeVec.get(i).torsion.size());
 								for(int j=0;j<moleculeVec.get(i).torsion.size();j++){
 									out.write("\n"+moleculeVec.get(i).torsion.get(j).atom[0]+" "+moleculeVec.get(i).torsion.get(j).atom[1]+" "+moleculeVec.get(i).torsion.get(j).atom[2]+" "+moleculeVec.get(i).torsion.get(j).atom[3]+" "+moleculeVec.get(i).torsion.get(j).forceConst+" "+moleculeVec.get(i).torsion.get(j).angle+" "+moleculeVec.get(i).torsion.get(j).periodicity);
 								}
 							}
 							if(moleculeVec.size() == exp.size()){
 							//for(int i=0; i<)
 								int size = 0;
 								for(int i=0; i<exp.size(); i++){
 									if(exp.get(i).formula != null || exp.get(i).constants != null || exp.get(i).values != null){
 									if(exp.get(i).formula.length()!=0 || exp.get(i).constants.length!=0 || exp.get(i).values.length != 0){
 										size++;
 									}
 									}
 								}
 								out.write("\n"+size);
 								for(int i=0; i<exp.size(); i++){
 									if(exp.get(i).formula != null || exp.get(i).constants != null || exp.get(i).values != null){
 									if(exp.get(i).formula.length()!=0 || exp.get(i).constants.length!=0 || exp.get(i).values.length != 0){
 										out.write("\n"+(i));
 										out.write("\n"+exp.get(i).formula);
 										out.write("\n"+exp.get(i).constants.length);
 										for(int j=0; j<exp.get(i).constants.length; j++){
 											out.write("\n"+exp.get(i).constants[j]+" "+exp.get(i).values[j]);
 										}
 									}
 									}
 								}
 								 totalNumber = 0;
 									for(int i=0; i<allPositions.size(); i++){
 										for(int j=0; j<allPositions.get(i).size(); j++){
 											totalNumber+= allPositions.get(i).get(j).size();
 										}
 										
 									}
 									int v = 1;
 								out.write("\n"+totalNumber);
 								for(int i=0; i<allPositions.size(); i++){
 									for(int j=0; j<allPositions.get(i).size(); j++){
 										for(int k=0; k<allPositions.get(i).get(j).size(); k++){
 											out.write("\n"+v++ +"\t"+allPositions.get(i).get(j).get(k).x/10+"\t"+allPositions.get(i).get(j).get(k).y/10+"\t"+allPositions.get(i).get(j).get(k).z/10);
 										}
 									}
 								}								
 								out.write("\nEnd");
 
 							}else{
 								main.jtext.setText("Error in expression");
 							}
 							//in.close();
 							out.close();
 						}catch(IOException e){
 							main.jtext.setText("Save failed");
 						}
 					}
 				}
 						
 				} 
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 			}
 		}
 	}
 	class Simulatethis implements Runnable{
 		public void run() {
 			simulate();			
 		}		
 	}
 	class Minimizethis implements Runnable{
 		public void run(){
 			//globalMinimize();
 		}
 	}
 	private static TransformGroup createBehaviors() {
 		TransformGroup objTrans = new TransformGroup();
 		
 		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		return objTrans;
 	}
 	@SuppressWarnings("static-access")
 	public void checkImportingFile(){
 		double tempMaxSigma = 0;
 		File file = null;
 		JFileChooser jfc = new JFileChooser();
 		jfc.setCurrentDirectory(new File(main.workSpace));
 		int f = jfc.showOpenDialog(main);
 		if(f == JFileChooser.APPROVE_OPTION){
 			file = jfc.getSelectedFile();
 		}
 		int v=0;							
 		try {
 			if(file!=null){
 				BufferedReader in = new BufferedReader(new FileReader(file));
 				try {
 					String export = "0";
 					export = in.readLine();
 					v=Integer.parseInt(export);
 					if(v == 1){
 						export = in.readLine();
 						int noAtoms = Integer.parseInt(export);
 						for(int b =0; b<noAtoms; b++){
 							export = in.readLine();
 							StringTokenizer st1 = new StringTokenizer(export);
 							st1.nextToken();
 							st1.nextToken();
 							st1.nextToken();
 							String no = st1.nextToken();
 							no = st1.nextToken();
 							double sigma = Double.parseDouble(no);
 							if(sigma > tempMaxSigma){
 								tempMaxSigma = sigma;
 							}
 						}
 					}
 					try {
 						in.close();
 					} catch (IOException e) {
 						main.jtext.setText("Error 103: Invalid export file");
 					}
 				} catch (IOException e) {
 				main.jtext.setText("Error 102: Invalid export file");
 				}
 			}
 			
 			
 		} catch (FileNotFoundException e1) {
 			main.jtext.setText("Error 101: Invalid export file");
 		}
 		
 		
 	}
 	class SingleMolecule{
 		Vector<Atom> atom;
 		Vector<Position> pos;
 		Vector<Bond> bond;
 		Vector<Angles> angle;
 		Vector<Torsion> torsion;
 		private void create(Vector<Atom> a, Vector<Position> p, Vector<Bond> b, Vector<Angles> ang, Vector<Torsion> t){
 			atom = a;
 			pos = p;
 			bond = b;
 			angle = ang;
 			torsion = t;
 		}
 	}
 	class RegionMolecules{
 		Vector<Integer> list;
 		RegionMolecules(Vector<Integer> l){
 			list = l;
 		}
 	}
 	class Crystal{
 		Atom index[];
 		float length;
 		int type;
 		Crystal(Atom a1, Atom a2, float l, int t){
 			index = new Atom[2];
 			index[0] = a1;
 			index[1] = a2;
 			length = l;
 			type = t;
 		}
 		
 	}
 	class SimpleMolecule {
 		public Vector<Atom> atoms;
 		public int atomLength=0;
 		public Vector<Vector3f> pos;
 		public Vector<Bond> bonds;
 		public int bondLength=0;
 		public Vector<Angles> angles;
 		public int angleLength=0;
 		public Vector<Torsion> torsions;
 		public int torsionLength=0;
 		public Vector<Torsion> inversions;
 		public int inversionLength=0;
 		public void initiateMolecule(Vector<Atom> atom, int al, Vector<Vector3f> p, Vector<Bond> bond, int bl){
 			atoms = atom;
 			atomLength = al;
 			pos = p;
 			bonds = bond;
 			bondLength = bl;
 		}
 		public void setAngles(Vector<Angles> angle, int al){
 			angles = angle;
 			angleLength = al;
 		}
 		public void setTorsions( Vector<Torsion> torsion, int tl){
 			torsions = torsion;
 			torsionLength = tl;
 		}
 		public void setInversions(Vector<Torsion> inversion, int il){
 			inversions = inversion;
 			inversionLength = il;
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	private Vector<Integer> loadDomain(BufferedReader read, int noc) {
 		Vector<Integer> instances = new Vector<Integer>();
 		try {
 		for(int i=0; i<noc; i++){			
 				int instance = Integer.parseInt(read.readLine());
 				instances.add(instance);
 				int atoms = Integer.parseInt(read.readLine());
 				Vector<Atom> atomVec = new Vector<Atom>();
 				Vector<Position> posVec = new Vector<Position>();
 				for(int j=0; j<atoms; j++){
 					StringTokenizer st = new StringTokenizer(read.readLine());
 					Atom a = new Atom(Integer.parseInt(st.nextToken()), st.nextToken(), Double.parseDouble(st.nextToken()), 
 							Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),
 							Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()), Integer.parseInt(st.nextToken()));
 					Position posAtom = new Position();
 					posAtom.x = Float.parseFloat(st.nextToken());
 					posAtom.y = Float.parseFloat(st.nextToken());
 					posAtom.z = Float.parseFloat(st.nextToken());
 					atomVec.add(a);
 					posVec.add(posAtom);
 				}
 				int bonds = Integer.parseInt(read.readLine());
 				Vector<Bond> bond = new Vector<Bond>();
 				for(int j=0; j<bonds; j++){
 					StringTokenizer st = new StringTokenizer(read.readLine());
 					Bond b= new Bond();
 					b.bond(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Float.parseFloat(st.nextToken()), Integer.parseInt(st.nextToken()));
 					bond.add(b);
 				}
 				int angles = Integer.parseInt(read.readLine());
 				Vector<Angles> angle = new Vector<Angles>();
 				for(int j=0; j<angles; j++){
 					StringTokenizer st = new StringTokenizer(read.readLine());
 					Angles a = new Angles();
 					a.newAngle(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
 							Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()));
 					angle.add(a);
 				}
 				int torsions = Integer.parseInt(read.readLine());
 				Vector<Torsion> torsion = new Vector<Torsion>();
 				for(int j=0; j<torsions; j++){
 					StringTokenizer st = new StringTokenizer(read.readLine());
 					Torsion t = new Torsion();
 					t.addTorsion(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
 							Integer.parseInt(st.nextToken()), Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()));
 					torsion.add(t);
 				}
 				//System.out.println(""+atoms+" "+bonds+" "+angles+" "+torsions);
 				SingleMolecule s = new SingleMolecule();
 				s.create(atomVec, posVec, bond, angle, torsion);				
 				moleculeVec.add(s);
 			} 
 		}
 		catch (FileNotFoundException e) {
 			main.jtext.setText("File not found");
 		}
 		catch (IOException e) {
 			main.jtext.setText("I/o error");
 		}	 
 		return instances;
 	}
 	@SuppressWarnings("static-access")
 	public boolean replicate(Region r, Vector<Integer> instances, BufferedReader read) {
 		//System.out.println(""+(r.sizex)+" "+(r.sizey)+" "+(r.sizez));
 		
 		//boolean work = true;
 		int tries = 0;
 		int tempInstance[] =  new int[instances.size()];
 		for(int i=0; i<instances.size(); i++)
 			tempInstance[i] = 0;
 		tries = 0;
 		try{
 			for(int i=tracker; i<instances.size()+tracker ; i++){
 				
 				Vector<BranchGroup> tg1 = new Vector<BranchGroup>();
 				Vector<Molecule> mg1 = new Vector<Molecule>();
 				Vector<Vector<Position>> pos2Vec = new Vector<Vector<Position>>();
 				int i1 = Integer.parseInt(read.readLine());
 				if(i1!=instances.get(i-tracker))
 					return false;
 				Vector<Vector<Position>> poses = new Vector<Vector<Position>>();
 				for(int v=0; v<instances.get(i-tracker); v++){					
 					Vector<Position> pos = new Vector<Position>();
 					for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 						StringTokenizer st = new StringTokenizer(read.readLine());
 						st.nextToken();
 						Position p = new Position(Float.parseFloat(st.nextToken()),Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()));
 						if(p.x>r.sizex||p.y>r.sizey||p.z>r.sizez){
 							
 							return false;
 						}						
 						pos.add(p);
 					}
 					poses.add(pos);
 				}
 				for(int k=0; k<instances.get(i-tracker); k++){	
 						
 						tries = 0;
 						Molecule m = new Molecule();
 						Vector<Position> posVec = new Vector<Position>();
 						BranchGroup moleBranch = new BranchGroup();
 						moleBranch.setCapability(moleBranch.ALLOW_DETACH);
 						
 						for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 							//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 							float x1 = (float) (poses.get(k).get(j).x +r.x-r.sizex/2)*0.1f; 
 							float y1 = (float) (poses.get(k).get(j).y +r.y-r.sizey/2)*0.1f; 
 							float z1 = (float) (poses.get(k).get(j).z +r.z-r.sizez/2)*0.1f; 
 							Position p = new Position(poses.get(k).get(j).x+r.x-r.sizex/2, poses.get(k).get(j).y+r.y-r.sizey/2, poses.get(k).get(j).z+r.z-r.sizez/2);
 							posVec.add(p);
 							cell[(int)(p.x/sysProp.cutOff)][(int)(p.y/sysProp.cutOff)][(int)(p.z/sysProp.cutOff)].addNew(p.x, p.y, p.z, moleculeVec.get(i).atom.get(j).sigma, moleBranch);
 							TransformGroup moleTrans = createBehaviors();
 							moleTrans.addChild(  createSphere(moleculeVec.get(i).atom.get(j).RFID));
 							moleBranch.addChild(moleTrans);
 							Vector3f temp = new Vector3f(x1,y1,z1);
 							Transform3D tempT = new Transform3D();
 							tempT.setTranslation(temp);
 							moleTrans.setTransform(tempT);
 							Atom a1 = Atom.copy(moleculeVec.get(i).atom.get(j));
 							PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 						    pickCanvas.setMode(PickCanvas.BOUNDS);
 							m.addAtom(a1, moleTrans,pickCanvas,temp,moleculeVec.get(i).atom.get(j).RFID);
 						}
 						for(int j=0; j<moleculeVec.get(i).bond.size(); j++){
 							Vector3f posa = m.vec.get(moleculeVec.get(i).bond.get(j).index[0]);
 							Vector3f posb = m.vec.get(moleculeVec.get(i).bond.get(j).index[1]);
 							Vector3f cross = new Vector3f();
 							Vector3f YAXIS = new Vector3f(0, 1, 0);
 							Vector3f temp = new Vector3f((posa.x+posb.x)/2,(posa.y+posb.y)/2,(posa.z+posb.z)/2);
 							Vector3f temp1 = new Vector3f((posa.x-posb.x),(posa.y-posb.y),(posa.z-posb.z));
 							
 							double l = (float)Math.sqrt(Squares.sqr(posa.x-posb.x)+Squares.sqr(posa.y-posb.y)+Squares.sqr(posa.z-posb.z));
 							
 							temp1.normalize();
 
 							cross.cross(YAXIS,temp1);
 							
 							AxisAngle4f tempAA = new AxisAngle4f();
 							
 							tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 							TransformGroup moleTrans = createBehaviors();
 							moleTrans.addChild(  createCylinder(l,0));
 							
 							moleBranch.addChild(moleTrans);
 							
 							Transform3D tempT = new Transform3D();
 							tempT.set(tempAA);
 							tempT.setTranslation(temp);
 								moleTrans.setTransform(tempT);
 								
 								
 								
 								PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 							    pickCanvas.setMode(PickCanvas.BOUNDS);
 								m.addBond(moleTrans,moleculeVec.get(i).bond.get(j).index[0], moleculeVec.get(i).bond.get(j).index[0],l,i);
 								//uniMole.addBranchGraph(moleBranch);
 								//moleTransGroup=null;
 							
 						}
 						sysTransGroup.addChild(moleBranch);
 						tg1.add(moleBranch);
 						mg1.add(m);
 						pos2Vec.add(posVec);
 					
 					if(tries>100000){
 						main.jtext.setText("Broken on "+(k+1));
 						break;
 					}
 				}
 				actualTransform.add(tg1);
 				actualMolecule.add(mg1);
 				allPositions.add(pos2Vec);
 				
 			}
 			//RegionMolecules rm = new RegionMolecules(list);
 			//regMol.add(rm);									
 			//tracker+= list.size();
 			tracker+=1;
 			pointer++;
 			return true;
 		}catch(IOException e){
 			main.jtext.setText("I/O Error");
 		}
 		return false;
 		}
 	
 	public void askForVector(){
 		final JFrame askVector = new JFrame("Select Vector");
 		askVector.setBounds(200, 150, 640, 480);
 		askVector.setVisible(true);
 		askVector.setLayout(new BorderLayout());
 		
 		selectingCone = new TransformGroup();
 		JPanel butPanel = new JPanel(new FlowLayout());
 		
 		final JComboBox boxDir = new JComboBox();
 		boxDir.addItem("X+");
 		boxDir.addItem("X-");
 		boxDir.addItem("Y+");
 		boxDir.addItem("Y-");
 		boxDir.addItem("Z+");
 		boxDir.addItem("Z-");
 		
 		JLabel ldir = new JLabel("Direction");
 		
 		JButton butOk = new JButton("Ok");
 		JButton butCancel = new JButton("Cancel");
 		
 		butOk.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(angleAtom11 !=9999 && angleAtom12 != 9999){
 					//Vector3f p1 = molecules.pos.get(angleAtom11);
 					//Vector3f p2 = molecules.pos.get(angleAtom12);
 					
 					//Vector3f vector = new Vector3f(p1.x-p2.x, p1.y-p2.y, p1.z-p2.z);
 					
 					
 					
 				}
 			}			
 		});
 		
 		butCancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				askVector.dispose();
 			}			
 		});
 		
 		butPanel.add(ldir);
 		butPanel.add(boxDir);
 		butPanel.add(butOk);
 		butPanel.add(butCancel);
 		
 		loadPosToMol();
 		
 		Canvas3D angleChooser =  new Canvas3D(SimpleUniverse.getPreferredConfiguration());
 		SimpleUniverse angleUni = new SimpleUniverse(angleChooser);
 		
 		tg = new TransformGroup();
 		tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
 		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
 		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		BranchGroup moleculeGroup = new BranchGroup();
 			getMolecules(angleChooser);
 		addLights(moleculeGroup);
 		moleculeGroup.addChild(tg);
 
 		angleChooser.addMouseListener(new MouseListener(){
 			public void mouseClicked(MouseEvent arg0) {
 				for(int j=0;j<molecules.atomNo;j++){
 					chooseMol.pk.get(j).setShapeLocation(arg0);
 				    PickResult result =chooseMol.pk.get(j).pickClosest();
 				    if (result != null) {
 				       Shape3D s = (Shape3D)result.getNode(PickResult.SHAPE3D);
 				       if (s != null) {
 				    	   TransformGroup tg =null;
 				    	   Vector3f pos = new Vector3f();
 				    	   Vector3f pos1 = new Vector3f();
 				    	   Boolean go = false;
 				    	   
 				    		   if(angleAtom12 != angleAtom11)
 				    			   angleAtom12 = angleAtom11;
 				    		   else{
 				    			   angleAtom12 = 9999;
 				    			   angleAtom11 = 9999;
 				    		   }
 				    		   angleAtom11 = j;
 				    		   if(angleAtom12!=9999 &&  angleAtom12 != angleAtom11){
 				    			   tg =  chooseMol.moleTrans.get(angleAtom11);
 				    			   Transform3D t3d = new Transform3D();
 				    			   tg.getTransform(t3d);						    	   
 				    			   t3d.get(pos);
 				    			   tg = chooseMol.moleTrans.get(angleAtom12);
 				    			   tg.getTransform(t3d);
 				    			   t3d.get(pos1);
 				    			   go = true;
 				    		   }
 				    	  
 				    	   
 				    	   if(go){ 	   
 				   			
 				    	   		TransformGroup moleTrans = null;
 				   				moleTrans =  selectingCone;
 				   				Vector3f cross = new Vector3f();
 				   				Vector3f YAXIS = new Vector3f(0, 1, 0);
 				   				Vector3f temp = new Vector3f((pos.x+pos1.x)/2,(pos.y+pos1.y)/2,(pos.z+pos1.z)/2);
 				   				Vector3f temp1 = new Vector3f((pos.x-pos1.x),(pos.y-pos1.y),(pos.z-pos1.z));
 				   				double oldl = oldL;
 				   				double l = Math.sqrt(Squares.sqr(pos.x-pos1.x)+Squares.sqr(pos.y-pos1.y)+Squares.sqr(pos.z-pos1.z));
 				   				temp1.normalize();
 				   				l=l/oldl;
 				   				cross.cross(YAXIS,temp1);
 				   						
 				   				AxisAngle4f tempAA = new AxisAngle4f();
 				   						
 				   				tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 				   				Transform3D tempT = new Transform3D();
 				   				tempT.set(tempAA);
 				   				tempT.setTranslation(temp);
 				   				tempT.setScale(new Vector3d(1,l,1));	Transform3D tempT1 = new Transform3D();
 				   				tempT1.set(tempAA);
 				   				tempT1.setTranslation(temp);
 				   				Transform3D tempT2 = new Transform3D();
 				   				tempT2.set(tempAA);
 				   				tempT2.setTranslation(temp);
 				   				tempT.setScale(new Vector3d(1,l,1));
 				   				moleTrans.setTransform(tempT);
 				   			}
 				    	   else{
 				    		   Vector3f v3f = new Vector3f(100, 100, 100);
 				    		   Transform3D t3d = new Transform3D();
 				    		   t3d.set(v3f);
 				    		   TransformGroup moleTrans = null;
 				   				moleTrans =  selectingCone;
 				    		   moleTrans.setTransform(t3d);
 				    			   angleAtom12 = 9999;
 				    		  
 				    	   }
 				   		}
 				       //main.jtext.setText(""+angleAtom11+" "+angleAtom12+" "+angleAtom21+" "+angleAtom22);
 				       break;
 				    }
 				}
 			}
 			public void mouseEntered(MouseEvent arg0) {}
 			public void mouseExited(MouseEvent arg0) {}
 			public void mousePressed(MouseEvent arg0) {}
 			public void mouseReleased(MouseEvent arg0) {}			
 		});
 		
 		//angleChooser.add
 		angleUni.addBranchGraph(moleculeGroup);
 		
 		ViewingPlatform view = angleUni.getViewingPlatform();
 		view.setNominalViewingTransform();
 		OrbitBehavior orbit = new OrbitBehavior(angleChooser, OrbitBehavior.REVERSE_TRANSLATE );
 	    orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
 	    angleUni.getViewingPlatform().setViewPlatformBehavior(orbit);
 
 	    float lx = 0, ly = 0, lz = 0, hx = 0, hy = 0, hz = 0;
 		for(int j=0; j<molecules.atomNo; j++){
 			Vector3f pos = molecules.pos.get(j);
 			if(pos.x< lx)
 				lx = pos.x;
 			if(pos.y< ly)
 				ly = pos.y;
 			if(pos.z< lz)
 				lz = pos.z;
 			if(pos.x> hx)
 				hx = pos.x;
 			if(pos.y> hy)
 				hy = pos.y;
 			if(pos.z> hz)
 				hz = pos.z;
 		}			
 		 
 	    orbit.setRotationCenter(new Point3d((hx-lx)*0.015f, (float)(hy-ly)*0.015f, 0));
 	    TransformGroup VpTG = angleUni.getViewingPlatform().getViewPlatformTransform();
 	    float Zcamera = 1.2f; //put the camera 12 meters back
 	    Transform3D Trfcamera = new Transform3D();
 	    Trfcamera.setTranslation(new Vector3f((float)(hx-lx)*0.015f, (float)(hy-ly)*0.015f, Zcamera));  
 	    VpTG.setTransform( Trfcamera );
 	    
 	    
 	    askVector.add(angleChooser, BorderLayout.CENTER);
 	    askVector.add(butPanel, BorderLayout.SOUTH);
 	}
 	private void loadPosToMol() {
 		molecules = new Structure();
 		molecules.initiate();
 		molecules.atomNo = moleculeVec.get(moleculeVec.size()-1).atom.size();
 		for(int i=0; i<molecules.atomNo; i++){
 			molecules.atoms.add( moleculeVec.get(moleculeVec.size()-1).atom.get(i).RFID);
 			molecules.mass.add( moleculeVec.get(moleculeVec.size()-1).atom.get(i).mass);
 			molecules.charge.add(moleculeVec.get(moleculeVec.size()-1).atom.get(i).charge);
 			molecules.sigma.add(moleculeVec.get(moleculeVec.size()-1).atom.get(i).sigma);
 			molecules.epsilon.add(moleculeVec.get(moleculeVec.size()-1).atom.get(i).epsilon);
 			Vector3f position = new Vector3f(moleculeVec.get(moleculeVec.size()-1).pos.get(i).x, moleculeVec.get(moleculeVec.size()-1).pos.get(i).y, moleculeVec.get(moleculeVec.size()-1).pos.get(i).z);
 			molecules.pos.add(position);
 		}
 		molecules.bondNo = moleculeVec.get(moleculeVec.size()-1).bond.size();
 		for(int i=0; i<molecules.bondNo; i++){
 			molecules.bonds.add(moleculeVec.get(moleculeVec.size()-1).bond.get(i));
 		}
 	}
 	private void getMolecules(Canvas3D angleChooser) {
 		chooseMol = new Molecule();
 		
 			Vector3f position = new Vector3f(100, 100, 100);
 			Transform3D t3d = new Transform3D();
 			t3d.set(position);
 			
 				selectingCone =createBehaviors();
 				selectingCone.addChild(createWiredCone());
 				oldL =1;
 				selectingCone.setTransform(t3d);
 				tg.addChild(selectingCone);
 			
 			
 			
 			for(int j=0; j<molecules.atomNo; j++){				
 				BranchGroup atomBranch = new BranchGroup();
 				TransformGroup moleTransGroup = createBehaviors();
 				moleTransGroup.addChild(  createSphere(molecules.atoms.get(j)));
 				atomBranch.addChild(moleTransGroup);
 				Vector3f temp = new Vector3f(molecules.pos.get(j).x*0.1f,molecules.pos.get(j).y*0.1f,-molecules.pos.get(j).z*0.1f);
 				Transform3D tempT = new Transform3D();
 				tempT.setTranslation(temp);
 				moleTransGroup.setTransform(tempT);		
 				PickCanvas pickCanvas = new PickCanvas(angleChooser, atomBranch);
 			    pickCanvas.setMode(PickCanvas.BOUNDS);
 				chooseMol.addAtom( moleTransGroup,pickCanvas,temp,molecules.atoms.get(j));
 				tg.addChild(atomBranch);
 			}
 			for(int j=0; j< molecules.bondNo; j++){
 				BranchGroup bondBranch = new BranchGroup();
 				Vector3f pos = chooseMol.vec.get(molecules.bonds.get(j).index[0]);
 				Vector3f pos1 = chooseMol.vec.get(molecules.bonds.get(j).index[1]);
 				Vector3f cross = new Vector3f();
 				Vector3f YAXIS = new Vector3f(0, 1, 0);
 				Vector3f temp = new Vector3f((pos.x+pos1.x)/2,(pos.y+pos1.y)/2,(pos.z+pos1.z)/2);
 				Vector3f temp1 = new Vector3f((pos.x-pos1.x),(pos.y-pos1.y),(pos.z-pos1.z));
 				
 				double l = (float)Math.sqrt(Squares.sqr(pos.x-pos1.x)+Squares.sqr(pos.y-pos1.y)+Squares.sqr(pos.z-pos1.z));
 				temp1.normalize();
 				cross.cross(YAXIS,temp1);
 				AxisAngle4f tempAA = new AxisAngle4f();
 				tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 				
 				TransformGroup moleTransGroup = createBehaviors();
 				moleTransGroup.addChild(  createCylinder(l,0));
 				bondBranch.addChild(moleTransGroup);
 				tg.addChild(bondBranch);
 				Transform3D tempT = new Transform3D();
 				tempT.set(tempAA);
 				tempT.setTranslation(temp);
 				moleTransGroup.setTransform(tempT);
 				chooseMol.addBond(moleTransGroup,molecules.bonds.get(j).index[0],molecules.bonds.get(j).index[1],l,1);
 				moleTransGroup=null;
 			}
 	}
 	public static class Cell {
 		static Vector<Dhruv> list;
 		Cell(){
 			list = new Vector<Dhruv>();
 		}
 		public void addNew(float xx, float yy, float zz, double si, BranchGroup moleBranch){
 			Dhruv d = new Dhruv();
 			d.x = xx;
 			d.y = yy;
 			d.z = zz;
 			d.sigma = si;
 			list.add(d);
 			d.branch = moleBranch;
 		}
 		class Dhruv{
 			float x=0, y=0, z=0;
 			double sigma = 0;
 			BranchGroup branch = null;
 		}
 	}
 	private Cone createWiredCone() {
 		Appearance app = new Appearance();
 		Color n = new Color();
 		n.newColor(1);
 		Color3f objColor = new Color3f(n.r, n.g, n.b);
 		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 		app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
 		javax.media.j3d.PolygonAttributes pa = new javax.media.j3d.PolygonAttributes();
 		pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
 		app.setPolygonAttributes(pa);
 		Cone cone = new Cone( 0.05f, 1, Primitive.GENERATE_NORMALS, app);
 		return cone;
 		
 	}
 	public class Expression{
 		String formula;
 		String constants[];
 		String values[];
 		public void addExpression(String form, String consts[], String vals[]){
 			formula = form;
 			constants = consts;
 			values = vals;
 		}
 		
 	}
 	public class Structure {
 		public int atomNo = 0;
 		public int bondNo = 0;
 		public Vector<Vector3f> pos;
 		public int copies = 0;
 		public Vector<Integer> atoms;
 		public Vector<Double> mass;
 		public Vector<Double> charge;
 		public Vector<Double> sigma;
 		public Vector<Double> epsilon;
 		public Vector<Bond> bonds;
 		public void initiate(){
 			atoms = new Vector<Integer>();
 			mass = new Vector<Double>();
 			charge = new Vector<Double>();
 			sigma = new Vector<Double>();
 			epsilon = new Vector<Double>();
 			bonds = new Vector<Bond>();
 			pos = new Vector<Vector3f>();
 		}
 	}
 	private void loadComponents(JFrame jregion, int noc, float x, float y, float z, float xsize, float ysize, float zsize, boolean now) {
 		Region r = new Region(x, y, z, xsize, ysize, zsize, sysProp , cgo);
 		BranchGroup b= r.graph;
 		sysBranchGroup.addChild(b);
 		regionBranch.add(b);
 		ListModel lm = regionList.getModel();
 		((DefaultListModel) lm).addElement("Region "+(pointer+1));
 		regions.add(r);
 		Boolean stas = false;
 		makeStationary.add(stas);
 		Expression e = new Expression();
 		exp.add(e);
 		majorDim = 0;
 		 loadMolecules(noc);
 		
 		replicate(r);
 		//System.out.println("hi");
 		jregion.dispose();
 		thisList = null;
 	}	
 	
 	private void loadMolecules(int nom) {
 		
 				int atoms = nom;
 				Vector<Atom> atomVec = new Vector<Atom>();
 				Vector<Position> posVec = new Vector<Position>();
 				for(int j=0; j<atoms; j++){
 					Atom a;
 					if(j==0||j==atoms-1){
 						a = new Atom(1, "C_33", 15.0344, 0.0, 2.0900, 0.3050, 1.0,
 								0.77, 109.471, 3);
 					}
 					else{
 						a = new Atom(1, "C_32", 14.0265, 0.0, 2.0400, 0.2150, 2.0,
 								0.77, 109.471, 3);
 					}
 
 					Position posAtom = new Position();
 					posAtom.x = 0;
 					posAtom.y = 0;
 					posAtom.z = 0;
 					atomVec.add(a);
 					posVec.add(posAtom);
 				}
 				int bonds = nom-1;
 				Vector<Bond> bond = new Vector<Bond>();
 				for(int j=0; j<bonds; j++){
 					Bond b= new Bond();
 					b.bond(j, j+1, 1.53, 700);
 					bond.add(b);
 				}
 				//int angles = nom-2;
 				int angles = 0;
 				Vector<Angles> angle = new Vector<Angles>();
 				for(int j=0; j<angles; j++){
 					Angles a = new Angles();
 					a.newAngle(j, j+1, j+2, 109.471, 100.0);
 					angle.add(a);
 				}
 				//int torsions = nom-3;
 				int torsions = 0;
 				Vector<Torsion> torsion = new Vector<Torsion>();
 				for(int j=0; j<torsions; j++){
 					Torsion t = new Torsion();
 					t.addTorsion(j, j+1, j+2, j+3, 1.0, 180.0, 3.0);
 					torsion.add(t);
 				}
 				//System.out.println(""+atoms+" "+bonds+" "+angles+" "+torsions);
 				SingleMolecule s = new SingleMolecule();
 				s.create(atomVec, posVec, bond, angle, torsion);
 				boolean doIt = true;
 				/*for(int j=0;j<i; j++){
 					if(s==moleculeVec.get(j)){
 						doIt = false;
 						indices.add(j);
 					}
 				}*/
 				if(doIt){
 					moleculeVec.add(s);					
 				}										
 	
 		
 		
 	}
 	@SuppressWarnings("static-access")
 	private boolean replicate(Region r) {
 		//System.out.println(""+(r.sizex)+" "+(r.sizey)+" "+(r.sizez));
 		stop = false;
 		//boolean work = true;
 		Vector<Integer> instances = new Vector<Integer>();
 		int tries = 0;
 		int tempInstance[] =  new int[instances.size()];
 		for(int i=0; i<instances.size(); i++)
 			tempInstance[i] = 0;
 		
 		//System.out.println(""+tracker);
 			for(int i=tracker; i<1+tracker ; i++){
 				int k=0;
 				Vector<BranchGroup> tg1 = new Vector<BranchGroup>();
 				Vector<Molecule> mg1 = new Vector<Molecule>();
 				Vector<Vector<Position>> pos2Vec = new Vector<Vector<Position>>();
 				while(true){
 					boolean go = true;
 					boolean work = true;
 					Random ran = new Random();
 					float tempx = (float) (ran.nextFloat() * r.sizex +r.x - r.sizex/ 2);
 					float tempy = (float) (ran.nextFloat() * r.sizey +r.y - r.sizey/ 2);
 					float tempz = (float) (ran.nextFloat() * r.sizez +r.z - r.sizez/ 2);
 					//System.out.println(""+moleculeVec.get(i).atom.size());
 					
 					Position pos[] = new Position[moleculeVec.get(i).atom.size()];
 					for(int j=0; j<moleculeVec.get(i).atom.size()&&go; j++){
 						float xx=0, yy=0, zz=0;						
 						xx = ((tempx + moleculeVec.get(i).pos.get(j).x));
 						yy = ((tempy + moleculeVec.get(i).pos.get(j).y));
 						zz = ((tempz + moleculeVec.get(i).pos.get(j).z));
 						
 						//System.out.println(""+xx+" "+yy+" "+zz);
 						if(xx<r.x+r.sizex/2 && yy<r.y+r.sizey/2 && zz<r.z+r.sizez/2 && xx>r.x-r.sizex/2 && yy>r.y-r.sizey/2 && zz>r.z-r.sizez/2){
 							
 							
 								for(int m =0; m<regions.size()-1; m++){
 									if(regions.get(m).exclude)
 									if(xx<regions.get(m).x+regions.get(m).sizex/2 && xx>regions.get(m).x -regions.get(m).sizex/2
 											&& yy<regions.get(m).y+regions.get(m).sizey/2 && yy>regions.get(m).y -regions.get(m).sizey/2
 											&& zz<regions.get(m).z+regions.get(m).sizez/2 && zz>regions.get(m).z -regions.get(m).sizez/2){
 										//System.out.println(""+(regions.get(m).x+regions.get(m).sizex/2)+" "+(regions.get(m).x -regions.get(m).sizex/2));
 										//System.out.println(""+(regions.get(m).y+regions.get(m).sizey/2)+" "+(regions.get(m).y -regions.get(m).sizey/2));
 										//System.out.println(""+(regions.get(m).z+regions.get(m).sizez/2)+" "+(regions.get(m).z -regions.get(m).sizez/2));
 										work = false;
 										break;
 									}
 								}
 							
 							//System.out.println(""+xx/sysProp.cutOff+" "+ yy/sysProp.cutOff +" "+zz/sysProp.cutOff
 							//		+" "+sysProp.xx/ sysProp.cutOff+" "+sysProp.yy/ sysProp.cutOff+" "+sysProp.xx/ sysProp.cutOff);
 							if((int)(xx/sysProp.cutOff) + 1<(int)(sysProp.xx/sysProp.cutOff))
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size();m++){
 								float x = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							if((int)(xx/sysProp.cutOff) - 1>0)
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 								float x = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							if((int)(yy/sysProp.cutOff) + 1<(int)(sysProp.yy/sysProp.cutOff))
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 								float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							if((int)(yy/sysProp.cutOff) - 1>0)
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 								float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							if((int)(zz/sysProp.cutOff) + 1<(int)(sysProp.zz/sysProp.cutOff))
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.size() && work;m++){
 								float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							if((int)(zz/sysProp.cutOff) - 1>0)
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.size() && work;m++){
 								float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 								float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 								float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 								float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 								double res = Math.sqrt(x*x + y*y +z*z);
 								double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 								if(res<sigma){
 									work =false;
 									break;
 								}
 							}
 							
 							if(work){		
 								pos[j] = new Position();
 								pos[j].x = xx;
 								pos[j].y = yy;
 								pos[j].z = zz;
 								//System.out.println(""+xx+" "+yy+" "+zz);
 								//System.out.println(""+j);
 								int going = 0;
 								while(going<1000){
 								Random randi = new Random();
 								int bang =randi.nextInt();
 								if(bang<0)
 									bang = - bang;
 								float tx = 0, ty = 0, tz = 0;
 								switch(bang%6){
 									case 0:
 										tx = 1.5f;
 										ty = 0f;
 										tz = 0f;
 										break;
 									case 1:
 										tx = 0f;
 										ty = 1.5f;
 										tz = 0f;
 										break;
 									case 2:
 										tx = 0f;
 										ty = 0f;
 										tz = 1.5f;
 										break;
 									case 4:
 										tx = -1.5f;
 										ty = 0f;
 										tz = 0f;
 										break;
 									case 5:
 										tx = 0f;
 										ty = -1.5f;
 										tz = 0f;
 										break;
 									case 6:
 										tx = 0f;
 										ty = 0f;
 										tz = -1.5f;
 										break;
 									case 7:
 										tx = 1.5f;
 										ty = 1.5f;
 										tz = 0f;
 										break;
 									case 8:
 										tx = 1.5f;
 										ty = 0f;
 										tz = 1.5f;
 										break;
 									case 9:
 										tx = 0f;
 										ty = 1.5f;
 										tz = 1.5f;
 										break;
 									case 10:
 										tx = -1.5f;
 										ty = -1.5f;
 										tz = 0f;
 										break;
 									case 11:
 										tx = -1.5f;
 										ty = 0f;
 										tz = -1.5f;
 										break;
 									case 12:
 										tx = 0f;
 										ty = -1.5f;
 										tz = -1.5f;
 										break;
 								}
 								boolean done = true;
 								//System.out.println(""+j);
 								for(int m=0; m<j; m++){
 									//System.out.println(""+m+" "+moleculeVec.get(i).pos.get(m).x +" "+ tx +" "+ moleculeVec.get(i).pos.get(m).y +" "+ ty +" "+ moleculeVec.get(i).pos.get(m).z +" "+ tz);
 									if(moleculeVec.get(i).pos.get(m).x == moleculeVec.get(i).pos.get(j).x+tx && moleculeVec.get(i).pos.get(m).y == moleculeVec.get(i).pos.get(j).y+ty && moleculeVec.get(i).pos.get(m).z == moleculeVec.get(i).pos.get(j).z+tz){
 										going++;
 										done = false;
 										break;
 									}
 								}
 								if(done){
 									if(j<moleculeVec.get(tracker).atom.size()-1){
 									moleculeVec.get(i).pos.get(j+1).x = moleculeVec.get(i).pos.get(j).x+tx;
 									moleculeVec.get(i).pos.get(j+1).y = moleculeVec.get(i).pos.get(j).y+ty;
 									moleculeVec.get(i).pos.get(j+1).z = moleculeVec.get(i).pos.get(j).z+tz;
 									}
 									break;
 								}
 								//System.out.println(""+j+" "+tx+" "+ty+" "+tz);
 								}								
 								
 							}
 							else{
 								tries ++;
 								go = false;
 								pos = null;
 								j--;
 								continue;
 							}
 						}
 						else{
 							tries ++;
 							go = false;
 							pos = null;
 							j--;
 							continue;
 						}
 						
 					}
 					if(go&&pos!=null){
 						k++;
 						Molecule m = new Molecule();
 						Vector<Position> posVec = new Vector<Position>();
 						BranchGroup moleBranch = new BranchGroup();
 						moleBranch.setCapability(moleBranch.ALLOW_DETACH);
 						for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 							//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 							float x1 = (float) (pos[j].x )*0.1f; 
 							float y1 = (float) (pos[j].y )*0.1f; 
 							float z1 = (float) (pos[j].z )*0.1f; 
 							//System.out.println(""+pos[j].x+" "+r.sizex/2+" "+r.x);
 							Position p = new Position(pos[j].x, pos[j].y, pos[j].z);
 							posVec.add(p);
 							cell[(int)(pos[j].x/sysProp.cutOff)][(int)(pos[j].y/sysProp.cutOff)][(int)(pos[j].z/sysProp.cutOff)].addNew(pos[j].x, pos[j].y, pos[j].z, moleculeVec.get(i).atom.get(j).sigma, moleBranch);
 							TransformGroup moleTrans = createBehaviors();
 							moleTrans.addChild(  createSphere(moleculeVec.get(i).atom.get(j).RFID));
 							moleBranch.addChild(moleTrans);
 							Vector3f temp = new Vector3f(x1,y1,z1);
 							Transform3D tempT = new Transform3D();
 							tempT.setTranslation(temp);
 							moleTrans.setTransform(tempT);
 							Atom a1 = Atom.copy(moleculeVec.get(i).atom.get(j));
 							PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 						    pickCanvas.setMode(PickCanvas.BOUNDS);
 							m.addAtom(a1, moleTrans,pickCanvas,temp,moleculeVec.get(i).atom.get(j).RFID);
 							
 						}
 						for(int j=0; j<moleculeVec.get(i).bond.size(); j++){
 							Vector3f posa = m.vec.get(moleculeVec.get(i).bond.get(j).index[0]);
 							Vector3f posb = m.vec.get(moleculeVec.get(i).bond.get(j).index[1]);
 							Vector3f cross = new Vector3f();
 							Vector3f YAXIS = new Vector3f(0, 1, 0);
 							Vector3f temp = new Vector3f((posa.x+posb.x)/2,(posa.y+posb.y)/2,(posa.z+posb.z)/2);
 							Vector3f temp1 = new Vector3f((posa.x-posb.x),(posa.y-posb.y),(posa.z-posb.z));
 							
 							double l = (float)Math.sqrt(Squares.sqr(posa.x-posb.x)+Squares.sqr(posa.y-posb.y)+Squares.sqr(posa.z-posb.z));
 							
 							temp1.normalize();
 
 							cross.cross(YAXIS,temp1);
 							
 							AxisAngle4f tempAA = new AxisAngle4f();
 							
 							tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 							TransformGroup moleTrans = createBehaviors();
 							moleTrans.addChild(  createCylinder(l,0));
 							
 							moleBranch.addChild(moleTrans);
 							Transform3D tempT = new Transform3D();
 							tempT.set(tempAA);
 							tempT.setTranslation(temp);
 								moleTrans.setTransform(tempT);
 							
 								PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 							    pickCanvas.setMode(PickCanvas.BOUNDS);
 								m.addBond(moleTrans,moleculeVec.get(i).bond.get(j).index[0], moleculeVec.get(i).bond.get(j).index[0],l,i);
 								//uniMole.addBranchGraph(moleBranch);
 								//moleTransGroup=null;
 							
 						}
 						sysTransGroup.addChild(moleBranch);
 						tg1.add(moleBranch);
 						mg1.add(m);
 						pos2Vec.add(posVec);
 					}
 					if(tries>10000){
 						System.out.println(""+tries);
 						main.jtext.setText("Broken on "+(k+1));
 						break;
 					}
 				}
 				actualTransform.add(tg1);
 				actualMolecule.add(mg1);
 				allPositions.add(pos2Vec);
 				tries = 0;
 			}
 			//RegionMolecules rm = new RegionMolecules(list);
 			//regMol.add(rm);									
 			//tracker+= list.size();
 			tracker+=1;
 			pointer++;
 			return true;
 		}
 	private void loadComponents(final JFrame jregion, final int noc,final float x ,final float y ,final float z, final float xsize,final float ysize,final float zsize) {
 		final JPanel p1, p2;
 		p1 = new JPanel(new GridBagLayout());
 		p2 = new JPanel(new FlowLayout());
 		jregion.setTitle("Select Composition");
 		JLabel jselcomp = new JLabel("Select Components");
 		JLabel jselnumber = new JLabel("Select Number");
 		
 		
 		final JButton bcomp[] = new JButton[noc];
 		final JTextField tnum[] = new JTextField[noc];
 		
 		p1.add(jselcomp, new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		p1.add(jselnumber, new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		thisList = new File[noc];
 		for(int i=0; i<noc; i++){
 			final int v = i;
 			bcomp[i] = new JButton("...");
 		
 			bcomp[i].addActionListener(new ActionListener(){							
 				public void actionPerformed(ActionEvent arg0) {
 					File f = null;
 					f = fileSelector(jregion);	
 					if(f!= null){
 						
 						//System.out.println(""+majorDim);
 						System.out.println(f.getAbsolutePath());
 						bcomp[v].setText(f.getAbsolutePath());
 						thisList[v] = f;
 						
 					}
 				}
 			});
 			tnum[i] = new JTextField("10");
 			p1.add(bcomp[i], new GBC(0,i+1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 			p1.add(tnum[i], new GBC(1,i+1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 		}
 		
 		JButton bok = new JButton("Ok");
 		bok.addActionListener(new ActionListener(){
 			@SuppressWarnings("static-access")
 			public void actionPerformed(ActionEvent arg0) {
 				try{
 				for(int i=0; i<noc; i++){
 					if(bcomp[i].getText() == "..."){
 						main.jtext.setText("Select all components");
 						return;
 					}				
 					int a = Integer.parseInt(tnum[i].getText());
 					if(a<0){
 						main.jtext.setText("Invalid instances");
 						return;
 					}
 				}
 				}catch(NumberFormatException e){
 					main.jtext.setText("Incorrect input");
 				}
 				//for(int i=)
 				float sizex = xsize, sizey = ysize, sizez = zsize;
 				if(xsize<majorDim)
 					sizex = (float) majorDim;
 				if(ysize<majorDim)
 					sizey = (float) majorDim;
 				if(zsize<majorDim)
 					sizez = (float) majorDim;
 				//System.out.println(""+majorDim);
 				//System.out.println(""+sizex+" "+sizey+" "+sizez);
 				Region r = new Region(x, y, 
 						z, sizex, sizey, sizez, sysProp , cgo);
 				BranchGroup b= r.graph;
 				sysBranchGroup.addChild(b);
 				regionBranch.add(b);
 				ListModel lm = regionList.getModel();
 				((DefaultListModel) lm).addElement("Region "+(pointer+1));
 				regions.add(r);
 				Boolean stas = false;
 				makeStationary.add(stas);
 				Expression e = new Expression();
 				exp.add(e);
 				majorDim = 0;
 				Vector<Integer> indices = loadMolecules();
 				Vector<Integer> instances = getInstances();
 				
 				replicate(r, indices, instances);
 				//System.out.println("hi");
 				jregion.dispose();
 				thisList = null;
 			}
 			private Vector<Integer> getInstances() {
 				Vector<Integer> numbers= new Vector<Integer>();
 				for(int i=0; i<noc; i++){
 					int n = Integer.parseInt(tnum[i].getText());
 					numbers.add(n);
 				}
 				return numbers;
 			}
 			
 			@SuppressWarnings("static-access")
 			private Vector<Integer> loadMolecules() {
 				Vector<Integer> instances = new Vector<Integer>();
 				for(int i=0; i<noc; i++){
 					try {
 						BufferedReader read = new BufferedReader(new FileReader(thisList[i]));
 						read.readLine();
 						int atoms = Integer.parseInt(read.readLine());
 						Vector<Atom> atomVec = new Vector<Atom>();
 						Vector<Position> posVec = new Vector<Position>();
 						for(int j=0; j<atoms; j++){
 							StringTokenizer st = new StringTokenizer(read.readLine());
 							Atom a = new Atom(Integer.parseInt(st.nextToken()), st.nextToken(), Double.parseDouble(st.nextToken()), 
 									Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()),
 									Double.parseDouble(st.nextToken()),Double.parseDouble(st.nextToken()), Integer.parseInt(st.nextToken()));
 							Position posAtom = new Position();
 							posAtom.x = Float.parseFloat(st.nextToken());
 							posAtom.y = Float.parseFloat(st.nextToken());
 							posAtom.z = Float.parseFloat(st.nextToken());
 							atomVec.add(a);
 							posVec.add(posAtom);
 						}
 						int bonds = Integer.parseInt(read.readLine());
 						Vector<Bond> bond = new Vector<Bond>();
 						for(int j=0; j<bonds; j++){
 							StringTokenizer st = new StringTokenizer(read.readLine());
 							Bond b= new Bond();
 							b.bond(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Float.parseFloat(st.nextToken()), Integer.parseInt(st.nextToken()));
 							bond.add(b);
 						}
 						int angles = Integer.parseInt(read.readLine());
 						Vector<Angles> angle = new Vector<Angles>();
 						for(int j=0; j<angles; j++){
 							StringTokenizer st = new StringTokenizer(read.readLine());
 							Angles a = new Angles();
 							a.newAngle(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
 									Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()));
 							angle.add(a);
 						}
 						int torsions = Integer.parseInt(read.readLine());
 						Vector<Torsion> torsion = new Vector<Torsion>();
 						for(int j=0; j<torsions; j++){
 							StringTokenizer st = new StringTokenizer(read.readLine());
 							Torsion t = new Torsion();
 							t.addTorsion(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
 									Integer.parseInt(st.nextToken()), Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()));
 							torsion.add(t);
 						}
 						read.close();
 						//System.out.println(""+atoms+" "+bonds+" "+angles+" "+torsions);
 						SingleMolecule s = new SingleMolecule();
 						s.create(atomVec, posVec, bond, angle, torsion);
 						boolean doIt = true;
 						/*for(int j=0;j<i; j++){
 							if(s==moleculeVec.get(j)){
 								doIt = false;
 								indices.add(j);
 							}
 						}*/
 						if(doIt){
 							moleculeVec.add(s);
 							instances.add(i);
 						}										
 					} catch (FileNotFoundException e) {
 						main.jtext.setText("File not found");
 					} catch (IOException e) {
 						main.jtext.setText("I/o error");
 					}
 				}
 				return instances;
 			}
 			@SuppressWarnings("static-access")
 			private boolean replicate(Region r, Vector<Integer> indices, Vector<Integer> instances) {
 				//System.out.println(""+(r.sizex)+" "+(r.sizey)+" "+(r.sizez));
 				stop = false;
 				//boolean work = true;
 				int tries = 0;
 				int tempInstance[] =  new int[instances.size()];
 				for(int i=0; i<instances.size(); i++)
 					tempInstance[i] = 0;
 				//System.out.println(""+tracker);
 					for(int i=tracker; i<noc+tracker ; i++){
 						Vector<BranchGroup> tg1 = new Vector<BranchGroup>();
 						Vector<Molecule> mg1 = new Vector<Molecule>();
 						Vector<Vector<Position>> pos2Vec = new Vector<Vector<Position>>();
 						int ding = 0;
 						for(int k=0; k<instances.get(i-tracker); ){	
 							boolean go = true;
 							boolean work = true;
 							Random ran = new Random();
 							ding++;
 							float tempx = (float) (ran.nextFloat() * r.sizex +r.x - r.sizex/ 2);
 							float tempy = (float) (ran.nextFloat() * r.sizey +r.y - r.sizey/ 2);
 							float tempz = (float) (ran.nextFloat() * r.sizez +r.z - r.sizez/ 2);
 							//System.out.println(""+moleculeVec.get(i).atom.size());
 							Position pos[] = new Position[moleculeVec.get(i).atom.size()];
 							for(int j=0; j<moleculeVec.get(i).atom.size()&&go; j++){
 								float xx=0, yy=0, zz=0;
 								switch(ding%12){
 								case 0:
 									xx = ((tempx + moleculeVec.get(i).pos.get(j).x));
 									yy = ((tempy + moleculeVec.get(i).pos.get(j).y));
 									zz = ((tempz + moleculeVec.get(i).pos.get(j).z));
 									break;
 								case 1:
 									xx = ((tempx + moleculeVec.get(i).pos.get(j).x));
 									yy = ((tempy + moleculeVec.get(i).pos.get(j).z));
 									zz = ((tempz + moleculeVec.get(i).pos.get(j).y));
 									break;
 								case 2:
 									xx = ((tempx + moleculeVec.get(i).pos.get(j).z));
 									yy = ((tempy + moleculeVec.get(i).pos.get(j).y));
 									zz = ((tempz + moleculeVec.get(i).pos.get(j).x));
 									break;
 								case 3:
 									xx = ((tempx + moleculeVec.get(i).pos.get(j).y));
 									yy = ((tempy + moleculeVec.get(i).pos.get(j).x));
 									zz = ((tempz + moleculeVec.get(i).pos.get(j).z));
 									break;
 								case 4:
 									xx = ((tempx + moleculeVec.get(i).pos.get(j).y));
 									yy = ((tempy + moleculeVec.get(i).pos.get(j).z));
 									zz = ((tempz + moleculeVec.get(i).pos.get(j).x));
 									break;
 								case 5:
 									xx = ((tempx + moleculeVec.get(i).pos.get(j).z));
 									yy = ((tempy + moleculeVec.get(i).pos.get(j).x));
 									zz = ((tempz + moleculeVec.get(i).pos.get(j).y));
 									break;
 								case 6:
 									xx = ((tempx - moleculeVec.get(i).pos.get(j).x));
 									yy = ((tempy - moleculeVec.get(i).pos.get(j).y));
 									zz = ((tempz - moleculeVec.get(i).pos.get(j).z));
 									break;
 								case 7:
 									xx = ((tempx - moleculeVec.get(i).pos.get(j).x));
 									yy = ((tempy - moleculeVec.get(i).pos.get(j).z));
 									zz = ((tempz - moleculeVec.get(i).pos.get(j).y));
 									break;
 								case 8:
 									xx = ((tempx - moleculeVec.get(i).pos.get(j).z));
 									yy = ((tempy - moleculeVec.get(i).pos.get(j).y));
 									zz = ((tempz - moleculeVec.get(i).pos.get(j).x));
 									break;
 								case 9:
 									xx = ((tempx - moleculeVec.get(i).pos.get(j).y));
 									yy = ((tempy - moleculeVec.get(i).pos.get(j).x));
 									zz = ((tempz - moleculeVec.get(i).pos.get(j).z));
 									break;
 								case 10:
 									xx = ((tempx - moleculeVec.get(i).pos.get(j).y));
 									yy = ((tempy - moleculeVec.get(i).pos.get(j).z));
 									zz = ((tempz - moleculeVec.get(i).pos.get(j).x));
 									break;
 								case 11:
 									xx = ((tempx - moleculeVec.get(i).pos.get(j).z));
 									yy = ((tempy - moleculeVec.get(i).pos.get(j).x));
 									zz = ((tempz - moleculeVec.get(i).pos.get(j).y));
 									break;
 								}
 								//System.out.println(""+xx+" "+yy+" "+zz);
 								if(xx<x+xsize/2 && yy<y+ysize/2 && zz<z+zsize/2 && xx>x-xsize/2 && yy>y-ysize/2 && zz>z-zsize/2){
 									
 										for(int m =0; m<regions.size()-1; m++){
 											if(regions.get(m).exclude)
 											if(xx<regions.get(m).x+regions.get(m).sizex/2 && xx>regions.get(m).x -regions.get(m).sizex/2
 													&& yy<regions.get(m).y+regions.get(m).sizey/2 && yy>regions.get(m).y -regions.get(m).sizey/2
 													&& zz<regions.get(m).z+regions.get(m).sizez/2 && zz>regions.get(m).z -regions.get(m).sizez/2){
 												//System.out.println(""+(regions.get(m).x+regions.get(m).sizex/2)+" "+(regions.get(m).x -regions.get(m).sizex/2));
 												//System.out.println(""+(regions.get(m).y+regions.get(m).sizey/2)+" "+(regions.get(m).y -regions.get(m).sizey/2));
 												//System.out.println(""+(regions.get(m).z+regions.get(m).sizez/2)+" "+(regions.get(m).z -regions.get(m).sizez/2));
 												work = false;
 												break;
 											}
 										}
 									
 									//System.out.println(""+xx/sysProp.cutOff+" "+ yy/sysProp.cutOff +" "+zz/sysProp.cutOff
 									//		+" "+sysProp.xx/ sysProp.cutOff+" "+sysProp.yy/ sysProp.cutOff+" "+sysProp.xx/ sysProp.cutOff);
 									if((int)(xx/sysProp.cutOff) + 1<(int)(sysProp.xx/sysProp.cutOff))
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size();m++){
 										float x = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)+1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									if((int)(xx/sysProp.cutOff) - 1>0)
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 										float x = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)-1][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									if((int)(yy/sysProp.cutOff) + 1<(int)(sysProp.yy/sysProp.cutOff))
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 										float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)+1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									if((int)(yy/sysProp.cutOff) - 1>0)
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 										float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)-1][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									if((int)(zz/sysProp.cutOff) + 1<(int)(sysProp.zz/sysProp.cutOff))
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.size() && work;m++){
 										float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)+1].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									if((int)(zz/sysProp.cutOff) - 1>0)
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.size() && work;m++){
 										float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)-1].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									for(int m=0; m<cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.size() && work;m++){
 										float x = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).x - xx;
 										float y = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).y - yy;
 										float z = cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).z - zz;
 										double res = Math.sqrt(x*x + y*y +z*z);
 										double sigma = (cell[(int)(xx/sysProp.cutOff)][(int)(yy/sysProp.cutOff)][(int)(zz/sysProp.cutOff)].list.get(m).sigma + moleculeVec.get(i).atom.get(j).sigma)/(2);
 										if(res<sigma){
 											work =false;
 											break;
 										}
 									}
 									if(work){		
 										//System.out.println(""+j);
 										
 										pos[j] = new Position();
 										pos[j].x = xx;
 										pos[j].y = yy;
 										pos[j].z = zz;
 										//System.out.println(""+j+" "+xx+" "+yy+" "+zz+" "+pos[j].x +" "+pos[j].y+" "+pos[j].z);
 									}
 									else{
 										tries ++;
 										go = false;
 										pos = null;
 										break;
 									}
 								}
 								else{
 									tries ++;
 									go = false;
 									pos = null;
 									break;
 								}
 								
 							}
 							if(go&&pos!=null){
 								k++;
 								Molecule m = new Molecule();
 								Vector<Position> posVec = new Vector<Position>();
 								BranchGroup moleBranch = new BranchGroup();
 								moleBranch.setCapability(moleBranch.ALLOW_DETACH);
 								for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 									//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 									float x1 = (float) (pos[j].x )*0.1f; 
 									float y1 = (float) (pos[j].y )*0.1f; 
 									float z1 = (float) (pos[j].z )*0.1f; 
 									//System.out.println(""+pos[j].x+" "+r.sizex/2+" "+r.x);
 									Position p = new Position(pos[j].x, pos[j].y, pos[j].z);
 									posVec.add(p);
 									cell[(int)(pos[j].x/sysProp.cutOff)][(int)(pos[j].y/sysProp.cutOff)][(int)(pos[j].z/sysProp.cutOff)].addNew(pos[j].x, pos[j].y, pos[j].z, moleculeVec.get(i).atom.get(j).sigma, moleBranch);
 									TransformGroup moleTrans = createBehaviors();
 									moleTrans.addChild(  createSphere(moleculeVec.get(i).atom.get(j).RFID));
 									moleBranch.addChild(moleTrans);
 									Vector3f temp = new Vector3f(x1,y1,z1);
 									Transform3D tempT = new Transform3D();
 									tempT.setTranslation(temp);
 									moleTrans.setTransform(tempT);
 									Atom a1 = Atom.copy(moleculeVec.get(i).atom.get(j));
 									PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 								    pickCanvas.setMode(PickCanvas.BOUNDS);
 									m.addAtom(a1, moleTrans,pickCanvas,temp,moleculeVec.get(i).atom.get(j).RFID);
 									
 								}
 								for(int j=0; j<moleculeVec.get(i).bond.size(); j++){
 									Vector3f posa = m.vec.get(moleculeVec.get(i).bond.get(j).index[0]);
 									Vector3f posb = m.vec.get(moleculeVec.get(i).bond.get(j).index[1]);
 									Vector3f cross = new Vector3f();
 									Vector3f YAXIS = new Vector3f(0, 1, 0);
 									Vector3f temp = new Vector3f((posa.x+posb.x)/2,(posa.y+posb.y)/2,(posa.z+posb.z)/2);
 									Vector3f temp1 = new Vector3f((posa.x-posb.x),(posa.y-posb.y),(posa.z-posb.z));
 									
 									double l = (float)Math.sqrt(Squares.sqr(posa.x-posb.x)+Squares.sqr(posa.y-posb.y)+Squares.sqr(posa.z-posb.z));
 									
 									temp1.normalize();
 
 									cross.cross(YAXIS,temp1);
 									
 									AxisAngle4f tempAA = new AxisAngle4f();
 									
 									tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 									TransformGroup moleTrans = createBehaviors();
 									moleTrans.addChild(  createCylinder(l,0));
 									TransformGroup t1 = createBehaviors();
 									t1.addChild(createCylinder(l/2,1));
 									TransformGroup t2 = createBehaviors();
 									t2.addChild(createCylinder(l/6,2));
 									moleBranch.addChild(moleTrans);
 									moleBranch.addChild(t1);
 									moleBranch.addChild(t2);
 									Transform3D tempT = new Transform3D();
 									tempT.set(tempAA);
 									tempT.setTranslation(temp);
 										moleTrans.setTransform(tempT);
 										Transform3D tempT1 = new Transform3D();
 										tempT1.set(tempAA);
 										tempT1.setTranslation(temp);
 										Transform3D tempT2 = new Transform3D();
 										tempT2.set(tempAA);
 										tempT2.setTranslation(temp);
 										float ratio1=1;
 										float ratio2=1;
 										
 										tempT1.setScale(new Vector3d(ratio1,1,ratio1));
 										tempT2.setScale(new Vector3d(ratio2,1,ratio2));
 										t1.setTransform(tempT1);
 										t2.setTransform(tempT2);
 										PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 									    pickCanvas.setMode(PickCanvas.BOUNDS);
 										m.addBond(moleTrans,t1,t2,moleculeVec.get(i).bond.get(j).index[0], moleculeVec.get(i).bond.get(j).index[0],l,i, pickCanvas);
 										//uniMole.addBranchGraph(moleBranch);
 										//moleTransGroup=null;
 									
 								}
 								sysTransGroup.addChild(moleBranch);
 								tg1.add(moleBranch);
 								mg1.add(m);
 								pos2Vec.add(posVec);
 							}
 							if(tries>10000||stop){
 								main.jtext.setText("Broken on "+(k+1));
 								break;
 							}
 						}
 						actualTransform.add(tg1);
 						actualMolecule.add(mg1);
 						allPositions.add(pos2Vec);
 						tries = 0;
 					}
 					//RegionMolecules rm = new RegionMolecules(list);
 					//regMol.add(rm);									
 					//tracker+= list.size();
 					tracker+=1;
 					pointer++;
 					return true;
 				}
 		});
 		JButton bcancel = new JButton("Cancel");
 		bcancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				jregion.dispose();
 			}							
 		});
 		
 		JButton bstop = new JButton("Stop");
 		bstop.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				stop = true;
 				jregion.dispose();
 				thisList = null;
 			}
 			
 		});
 		p2.add(bok);
 		p2.add(bcancel);
 		//jregion.setSize(340, 60 + (noc)*80);
 		jregion.setVisible(false);
 		
 		jregion.add(p1, BorderLayout.CENTER);
 		jregion.add(p2, BorderLayout.SOUTH);
 		jregion.setVisible(true);
 	}	
 	@SuppressWarnings("static-access")
 	public void loadSystem(BufferedReader in, int noC){
 		try {
 			Integer.parseInt(in.readLine());
 			Atom atoms[][] = new Atom[noC][];
 			Bond bonds[][] = new Bond[noC][];
 			Angles angle[][] = new Angles[noC][];
 			Torsion torsion[][] = new Torsion[noC][];
 			Position poses[][] = new Position[noC][];
 			
 			int instances[] = new int[noC];
 			float size[][] = new float[noC][3];
 			float center[][] = new float[noC][3];
 			boolean cgo[] = new boolean[noC];
 			
 			for(int i=0; i<noC; i++){
 				cgo[i] = Boolean.parseBoolean(in.readLine());
 				size[i][0] = Float.parseFloat(in.readLine());
 				size[i][1] = Float.parseFloat(in.readLine());
 				size[i][2] = Float.parseFloat(in.readLine());
 				center[i][0] = Float.parseFloat(in.readLine());
 				center[i][1] = Float.parseFloat(in.readLine());
 				center[i][2] = Float.parseFloat(in.readLine());
 				instances[i] = Integer.parseInt(in.readLine());
 				int number = Integer.parseInt(in.readLine());
 				atoms[i] = new Atom[number];
 				
 				StringTokenizer st = null;
 				Atom a = null;
 				Bond b = null;
 				Angles an = null;
 				Torsion t = null;
 				
 				poses[i] = new Position[number * instances[i]];
 				for(int j=0; j<number; j++){
 					st = new StringTokenizer(in.readLine());
 					int rf = Integer.parseInt(st.nextToken());
 					a = Atom.copy(allAtoms.get(rf-1));
 					a.mass = Double.parseDouble(st.nextToken());
 					a.charge = Double.parseDouble(st.nextToken());
 					a.sigma = Double.parseDouble(st.nextToken());  // need to change here
 					a.epsilon = Double.parseDouble(st.nextToken());
 					
 					Position p = new Position();
 					p.x = Float.parseFloat(st.nextToken());
 					p.y = Float.parseFloat(st.nextToken());
 					p.z = Float.parseFloat(st.nextToken());
 					poses[i][j] = p;
 					
 					atoms[i][j] = a;
 				}
 				number = Integer.parseInt(in.readLine());
 				bonds[i] = new Bond[number];
 				for(int j=0; j<number; j++){
 					st = new StringTokenizer(in.readLine());
 					b = new Bond();
 					b.bond(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()),
 							Double.parseDouble(st.nextToken()), Integer.parseInt(st.nextToken()));
 					bonds[i][j] = b;
 				}
 				number = Integer.parseInt(in.readLine());
 				angle[i] = new Angles[number];
 				for(int j=0; j<number; j++){
 					st = new StringTokenizer(in.readLine());
 					an = new Angles();
 					an.cAtom = Integer.parseInt(st.nextToken());
 					an.oAtoms[0] = Integer.parseInt(st.nextToken());
 					an.oAtoms[1] = Integer.parseInt(st.nextToken());
 					an.angle = Double.parseDouble(st.nextToken());
 					an.strength = Double.parseDouble(st.nextToken());
 					angle[i][j] = an;
 				}
 				number = Integer.parseInt(in.readLine());
 				torsion[i] = new Torsion[number];
 				for(int j=0; j<number; j++){
 					st = new StringTokenizer(in.readLine());
 					t = new Torsion();
 					t.atom[0] = Integer.parseInt(st.nextToken());
 					t.atom[1] = Integer.parseInt(st.nextToken());
 					t.atom[2] = Integer.parseInt(st.nextToken());
 					t.atom[3] = Integer.parseInt(st.nextToken());
 					t.forceConst = Double.parseDouble(st.nextToken());
 					t.angle = Double.parseDouble(st.nextToken());
 					t.periodicity = Double.parseDouble(st.nextToken());
 					torsion[i][j] = t;
 				}
 			}
 			int number = Integer.parseInt(in.readLine());
 			Expression express[] = new Expression[noC];
 			for(int i=0; i<noC; i++){
 				express[i] = new Expression();
 			}
 			
 			for(int j=0; j<number; j++){
 				int expNumber = Integer.parseInt(in.readLine());
 				Expression ex = new Expression();
 				ex.formula = in.readLine();
 				int length = Integer.parseInt(in.readLine());
 				StringTokenizer st = null;
 				for(int k=0; k<length; k++){
 					st = new StringTokenizer(in.readLine());
 					ex.constants[k] = st.nextToken();
 					ex.values[k] = st.nextToken();
 				}
 				express[expNumber] = ex;
 			}
 			number = Integer.parseInt(in.readLine());
 			float pos[][] = new float[number][3];
 			StringTokenizer st = null;
 			
 			for(int i=0; i<number; i++){
 				st = new StringTokenizer(in.readLine());
 				st.nextToken();
 				pos[i][0] = Float.parseFloat(st.nextToken());
 				pos[i][1] = Float.parseFloat(st.nextToken());
 				pos[i][2] = Float.parseFloat(st.nextToken());
 			}
 			for(int i=0; i<noC; i++){
 					exp.add(express[i]);
 			}
 			for(int i=0; i<noC; i++){
 				SingleMolecule e= new SingleMolecule();
 				e.atom = new Vector<Atom>();
 				e.bond = new Vector<Bond>();
 				e.angle = new Vector<Angles>();
 				e.torsion = new Vector<Torsion>();
 				e.pos = new Vector<Position>();
 				for(int j=0; j< atoms[i].length; j++){
 					e.atom.add(atoms[i][j]);
 					e.pos.add(poses[i][j]);
 				}
 				for(int j=0; j<bonds[i].length; j++){
 					e.bond.add(bonds[i][j]);
 				}
 				for(int j=0; j<angle[i].length; j++){
 					e.angle.add(angle[i][j]);
 				}
 				for(int j=0; j<torsion[i].length; j++){
 					e.torsion.add(torsion[i][j]);
 				}
 				moleculeVec.add(e);
 			}
 			for(int i=0; i<noC; i++){
 				Region r = new Region(center[i][0], center[i][1], center[i][2], size[i][0], size[i][1],
 						size[i][2], sysProp, cgo[i]);
 				BranchGroup b = r.graph;
 				sysBranchGroup.addChild(b);
 				regionBranch.add(b);
 				ListModel lm = regionList.getModel();
 				((DefaultListModel) lm).addElement("Region "+(pointer+1));
 				regions.add(r);
 				Boolean stas = false;
 				makeStationary.add(stas);
 				replicatePos(r,  instances[i], pos);
 			}
 			for(int i=0; i<noC; i++){
 				for(int k=0; k<instances[i]; k++)
 				for(int j=0; j<moleculeVec.get(i).atom.size(); j++){
 				}
 			}
 		} catch (NumberFormatException e) {
 			main.jtext.setText("Invalid Input");
 		} catch (IOException e) {
 			main.jtext.setText("I/O Error");
 		}
 		
 		
 
 	}
 	private void replicatePos(Region r, int instances, float[][] pos2) {
 		stop = false;
 		//boolean work = true;
 		
 		
 		//System.out.println(""+tracker);
 				Vector<BranchGroup> tg1 = new Vector<BranchGroup>();
 				Vector<Molecule> mg1 = new Vector<Molecule>();
 				Vector<Vector<Position>> pos2Vec = new Vector<Vector<Position>>();
 					//boolean work = true;
 					//System.out.println(""+moleculeVec.get(i).atom.size());
 					for(int i=0; i<instances; i++){
 					Position pos[] = new Position[moleculeVec.get(tracker).atom.size()];
 					
 					for(int j=0; j<moleculeVec.get(tracker).atom.size(); j++){
 						//System.out.println(i*moleculeVec.get(tracker).atom.size()+j);
 						float xx=0, yy=0, zz=0;						
 						xx = (pos2[sum +i*moleculeVec.get(tracker).atom.size()+j][0]*10);
 						yy = (pos2[sum +i*moleculeVec.get(tracker).atom.size()+j][1]*10);
 						zz = (pos2[sum +i*moleculeVec.get(tracker).atom.size()+j][2]*10);
 						//sum++;
 						//System.out.println(""+xx+" "+yy+" "+zz);
 	
 								pos[j] = new Position();
 								pos[j].x = xx;
 								pos[j].y = yy;
 								pos[j].z = zz;
 								//System.out.println(""+xx+" "+yy+" "+zz);
 								//System.out.println(""+j);
 								
 								//System.out.println(""+j+" "+tx+" "+ty+" "+tz);
 								
 								
 						
 						
 					}
 					if(pos!=null){
 						Molecule m = new Molecule();
 						Vector<Position> posVec = new Vector<Position>();
 						BranchGroup moleBranch = new BranchGroup();
 						moleBranch.setCapability(BranchGroup.ALLOW_DETACH);
 						for(int j=0; j<moleculeVec.get(tracker).atom.size(); j++){
 							//System.out.println(""+j+" "+pos[j].x*0.1 +" "+pos[j].y*0.1+" "+pos[j].z*0.1);
 							float x1 = (float) (pos[j].x )*0.1f; 
 							float y1 = (float) (pos[j].y )*0.1f; 
 							float z1 = (float) (pos[j].z )*0.1f; 
 							//System.out.println(""+pos[j].x+" "+r.sizex/2+" "+r.x);
 							Position p = new Position(pos[j].x, pos[j].y, pos[j].z);
 							posVec.add(p);
 							cell[(int)(pos[j].x/sysProp.cutOff)][(int)(pos[j].y/sysProp.cutOff)][(int)(pos[j].z/sysProp.cutOff)].addNew(pos[j].x, pos[j].y, pos[j].z, moleculeVec.get(tracker).atom.get(j).sigma, moleBranch);
 							TransformGroup moleTrans = createBehaviors();
 							moleTrans.addChild(  createSphere(moleculeVec.get(tracker).atom.get(j).RFID));
 							System.out.println(moleculeVec.get(tracker).atom.get(j).RFID);
 							moleBranch.addChild(moleTrans);
 							Vector3f temp = new Vector3f(x1,y1,z1);
 							Transform3D tempT = new Transform3D();
 							tempT.setTranslation(temp);
 							moleTrans.setTransform(tempT);
 							Atom a1 = Atom.copy(moleculeVec.get(tracker).atom.get(j));
 							PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 						    pickCanvas.setMode(PickCanvas.BOUNDS);
 							m.addAtom(a1, moleTrans,pickCanvas,temp,moleculeVec.get(tracker).atom.get(j).RFID);
 							
 						}
 						for(int j=0; j<moleculeVec.get(tracker).bond.size(); j++){
 							Vector3f posa = m.vec.get(moleculeVec.get(tracker).bond.get(j).index[0]);
 							Vector3f posb = m.vec.get(moleculeVec.get(tracker).bond.get(j).index[1]);
 						//	System.out.println("\n"+posa.x+"\t"+posa.y+"\t"+posa.z+"\t"+posb.x+"\t"+posb.y+"\t"+posb.z);
 							Vector3f cross = new Vector3f();
 							Vector3f YAXIS = new Vector3f(0, 1, 0);
 							Vector3f temp = new Vector3f((posa.x+posb.x)/2,(posa.y+posb.y)/2,(posa.z+posb.z)/2);
 							Vector3f temp1 = new Vector3f((posa.x-posb.x),(posa.y-posb.y),(posa.z-posb.z));
 							
 							double l = (float)Math.sqrt(Squares.sqr(posa.x-posb.x)+Squares.sqr(posa.y-posb.y)+Squares.sqr(posa.z-posb.z));
 							
 							temp1.normalize();
 
 							cross.cross(YAXIS,temp1);
 							
 							AxisAngle4f tempAA = new AxisAngle4f();
 							
 							tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 							TransformGroup moleTrans = createBehaviors();
 							moleTrans.addChild(  createCylinder(l,0));
 							
 							moleBranch.addChild(moleTrans);
 							Transform3D tempT = new Transform3D();
 							tempT.set(tempAA);
 							tempT.setTranslation(temp);
 							moleTrans.setTransform(tempT);
 								
 							PickCanvas pickCanvas = new PickCanvas(canvasSys, moleBranch);
 							pickCanvas.setMode(PickCanvas.BOUNDS);
 							m.addBond(moleTrans,moleculeVec.get(tracker).bond.get(j).index[0], moleculeVec.get(tracker).bond.get(j).index[0],l,tracker);
 						}
 						sysTransGroup.addChild(moleBranch);
 						tg1.add(moleBranch);
 						mg1.add(m);
 						pos2Vec.add(posVec);
 					}
 					}
 					actualTransform.add(tg1);
 					actualMolecule.add(mg1);
 					allPositions.add(pos2Vec);
 					sum += instances * moleculeVec.get(tracker).atom.size();
 					tracker++;
 					pointer++;
 					
 				
 				
 			
 			//RegionMolecules rm = new RegionMolecules(list);
 			//regMol.add(rm);									
 			//tracker+= list.size();
 			
 	}
 }
