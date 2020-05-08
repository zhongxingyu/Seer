 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfigTemplate;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagLayout;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import javax.media.j3d.PhysicalBody;
 import javax.media.j3d.PhysicalEnvironment;
 import javax.imageio.ImageIO;
 import javax.media.j3d.ImageComponent;
 import javax.media.j3d.ImageComponent2D;
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.DirectionalLight;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.Material;
 import javax.media.j3d.Node;
 import javax.media.j3d.Screen3D;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.vecmath.AxisAngle4f;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 import javax.media.j3d.GraphicsConfigTemplate3D;
 import com.sun.j3d.utils.geometry.Cone;
 import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
 import com.sun.j3d.utils.geometry.Cylinder;
 import com.sun.j3d.utils.geometry.Primitive;
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickResult;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 import java.awt.image.RenderedImage;
 import javax.media.j3d.ViewPlatform;
 import javax.media.j3d.View;
 
 public class PostProcessor {
 	static RenderedImage renderedImage;
 	static ImageComponent2D imageComponent;
 	static View  v;
 	static Darshan main;
 	static File caseFile;
 	static File dataFile;
 	static Structure molecules[];
 	static Canvas3D visualizer, offScreenCanvas3D;
 	static SimpleUniverse uniVis;
 	static BranchGroup visBranchGroup;
 	static OrbitBehavior orbit;
 	static ViewingPlatform view;
 	static JPanel jpostTool;
 	static Vector<Atom> allAtoms = null;
 	static int noC = 0, type =0, behavior = 0, total =0, moleNumber[] = null, atomToCheck = 0;
 	static double sizeX=0, sizeY=0, sizeZ=0, cutOff=0, sigma=0, epsilon=0, pressure=0, temp =0, maxSigma = 0;
 	static Molecule selectedMol = null;
 	static Molecule chooseMol[] = null;
 	static Vector<Integer> lineNo;
 	static Vector<Integer> saves;
 	static boolean show[];
 	static BranchGroup box;
 	static double scale;
 	static Vector<Vector<Angles>> allAngles;
 	static Vector<Vector<Torsion>> allTorsions;
 	static Vector<BranchGroup> allBranches, bondBranch;
 	static int showingMolecule = 0;
 	static TransformGroup selectingSphere[];
 	static int rdfMole1=9999, rdfMole2=9999, rdfAtom1=9999, rdfAtom2=9999;
 	static boolean start = true;
 	static TransformGroup selectingCone[];
 	static int angleMole1=9999, angleMole2=9999, angleAtom11=9999, angleAtom12=9999, angleAtom21=9999, angleAtom22=9999;
 	static float oldL[];
 	static float pos[][];
 	static float velocities[][];
 	static Vector<PickCanvas> picking;
 	static boolean selectionState = false;
 	static int selectedId =65500, selectedId2 =65500;
 	@SuppressWarnings("static-access")
 	public boolean initializePostProcessor(Darshan ab, File casef){
 		try {
 			main = ab;
 			caseFile = casef;
 			scale = 0.005;			
 			if(casef.exists() ){
 			BufferedReader in = new BufferedReader(new FileReader(casef));
 			int verify = Integer.parseInt(in.readLine());
 			if(verify == 25011989){
 				noC = Integer.parseInt(in.readLine());
 				show = new boolean[noC];
 				for(int i=0; i<noC; i++){
 					show[i]=false;
 				}
 				molecules = new Structure[noC];
 				
 				temp = Double.parseDouble(in.readLine());
 				
 				behavior = 0;
 				type = Integer.parseInt(in.readLine());
 				if(type == 0){
 					sizeX =  Double.parseDouble(in.readLine());
 					sizeY =  Double.parseDouble(in.readLine());
 					sizeZ =  Double.parseDouble(in.readLine());
 					behavior = Integer.parseInt(in.readLine());
 					if(behavior == 0){
 						cutOff = Double.parseDouble(in.readLine());
 					}
 					else if(behavior == 1){
 						cutOff = Double.parseDouble(in.readLine());
 					}
 					else if(behavior == 2){
 						sigma = Double.parseDouble(in.readLine());
 						epsilon  = Double.parseDouble(in.readLine());
 						cutOff = Double.parseDouble(in.readLine());
 					}
 				}
 				else if(type == 1){
 					pressure =  Double.parseDouble(in.readLine());
 					sizeX =  Double.parseDouble(in.readLine());
 					sizeY =  Double.parseDouble(in.readLine());
 					sizeZ =  Double.parseDouble(in.readLine());
 					behavior = Integer.parseInt(in.readLine());
 					if(behavior == 0){
 						cutOff = Double.parseDouble(in.readLine());
 					}
 					else if(behavior == 1){
 						cutOff = Double.parseDouble(in.readLine());
 					}
 					else if(behavior == 3){
 						
 					}
 				}
 				total = Integer.parseInt(in.readLine());
 				total = 0;
 				moleNumber = new int[noC];
 				allAngles = new Vector<Vector<Angles>>();
 				allTorsions = new Vector<Vector<Torsion>>();
 				for(int i=0; i<noC;i++){
 					moleNumber[i] = Integer.parseInt(in.readLine());
 					int atoms = Integer.parseInt(in.readLine());
 					total += moleNumber[i] *atoms;
 					molecules[i] = new Structure();
 					molecules[i].initiate();
 					molecules[i].atomNo = atoms;
 					for(int j=0; j< atoms; j++){
 						StringTokenizer st = new StringTokenizer(in.readLine());
 						if(st.hasMoreTokens()){
 							int rfid = Integer.parseInt(st.nextToken());
 							
 							float mass = Float.parseFloat(st.nextToken());
 							float charge = Float.parseFloat(st.nextToken());
 							float sigma = Float.parseFloat(st.nextToken());
 							float epsilon = Float.parseFloat(st.nextToken());
 							float x = Float.parseFloat(st.nextToken());
 							float y = Float.parseFloat(st.nextToken());
 							float z = Float.parseFloat(st.nextToken());
 							Vector3f position = new Vector3f(x, y, z);
 							
 							molecules[i].atoms.add(rfid);
 							molecules[i].mass.add(mass);
 							molecules[i].charge.add(charge);
 							molecules[i].sigma.add(sigma);
 							molecules[i].epsilon.add(epsilon);
 							molecules[i].pos.add(position);
 						}
 					}
 					int bonds = Integer.parseInt(in.readLine());
 					molecules[i].bondNo = bonds;
 					for(int j=0;j<bonds;j++){
 						StringTokenizer st = new StringTokenizer(in.readLine());
 						if(st.hasMoreTokens()){
 							Bond bond = new Bond();
 							bond.bond(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()), (int)Double.parseDouble(st.nextToken()) );
 							molecules[i].bonds.add(bond);
 						}
 					}
 					int angles  = Integer.parseInt(in.readLine());
 					Vector<Angles> angle = new Vector<Angles>();
 					for(int j=0;j<angles;j++){
 						StringTokenizer st = new StringTokenizer(in.readLine());
 						if(st.hasMoreTokens()){
 							Angles ang = new Angles();
 							ang.newAngle(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
 							angle.add(ang);
 						}
 					}
 					allAngles.add(angle);
 					int torsions  = Integer.parseInt(in.readLine());
 					Vector<Torsion> torsion = new Vector<Torsion>();
 					for(int j=0;j<torsions;j++){
 						StringTokenizer st = new StringTokenizer(in.readLine());
 						if(st.hasMoreTokens()){
 							Torsion tor = new Torsion();
 							tor.addTorsion(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
 							torsion.add(tor);
 						}
 					}			
 					allTorsions.add(torsion);
 				}			
 				velocities = new float[total][3];
 				//System.out.println(in.readLine());
 				in.close();
 				//String filename = in.readLine();
 				//System.out.println(filename);
 				return true;
 				
 			}
 			else{
 				main.jtext.setText("File Does not exists");
 				return false;
 			}
 			}
 		}catch(IOException e){
 			main.jtext.setText("IOException");
 			return false;
 		}
 	return false;
 	}
 	
 	@SuppressWarnings("static-access")
 	public void loadMolecules(){
 		try {
 			allBranches = new Vector<BranchGroup>();
 			bondBranch = new Vector<BranchGroup>();
 			allAtoms = new Vector<Atom>();
 			lineNo = new Vector<Integer>();
 			saves = new Vector<Integer>();
 			int line = 0;
 			String ffFile= "dreiding.abat";
 			BufferedReader  ff = new BufferedReader(new FileReader(ffFile));
 			while(ff.ready()){
 				StringTokenizer sf1 = new StringTokenizer(ff.readLine());
 				main.jtext.setText("Loading data file, this may take some time, please wait");
 				int rf = Integer.parseInt(sf1.nextToken());
 				for(int i=0; i<noC; i++){
 					for(int j=0; j<molecules[i].atomNo; j++){
 						if(sf1.hasMoreTokens())
 						if(molecules[i].atoms.get(j) == rf ){
 							String s = sf1.nextToken();
 							sf1.nextToken();
 							sf1.nextToken();
 							sf1.nextToken();
 							sf1.nextToken();
 							Atom a = new Atom(rf,s, (double)molecules[i].mass.get(j), (double)molecules[i].charge.get(j), (double)molecules[i].sigma.get(j), (double)molecules[i].epsilon.get(j),Double.parseDouble(sf1.nextToken()),Double.parseDouble(sf1.nextToken()),Double.parseDouble(sf1.nextToken()), Integer.parseInt(sf1.nextToken()));
 							allAtoms.add(a);
 							//System.out.println(a.charge);
 							atomToCheck++;
 							break;
 						}
 					}
 				}
 			}
 			if(! allAtoms.isEmpty()){
 				selectedMol = new Molecule();
 				BufferedReader in = new BufferedReader(new FileReader(dataFile));
 				int noOfSaves = 0;
 				in.readLine();
 				line++;
 				noOfSaves = Integer.parseInt(in.readLine());
 				line++;
 				saves.add(noOfSaves);
 				lineNo.add(line);
 				if(type == 1){
 					StringTokenizer st = new StringTokenizer(in.readLine());					
 					sizeX = Double.parseDouble(st.nextToken());
 					sizeY = Double.parseDouble(st.nextToken());
 					sizeZ = Double.parseDouble(st.nextToken());
 				}
 				
 				int summer =0;
 				for(int i=0; i<noC; i++){
 					
 					//System.out.println(""+moleNumber[i]+" "+molecules[i].atomNo+" "+molecules[i].bondNo);
 					for(int l=0; l<moleNumber[i]; l++){
 						
 						for(int j=0; j<molecules[i].atomNo; j++){
 							BranchGroup moleBranch = new BranchGroup();
 							moleBranch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 							moleBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 							moleBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 							moleBranch.setCapability(BranchGroup.ALLOW_DETACH);
 							StringTokenizer st = new StringTokenizer(in.readLine());
 							line++;
 							st.nextToken();
 							//jtext.setText("\n"+rf);
 							int rf = molecules[i].atoms.get(j);
 							Atom a= null;
 							for(int m=0; m<allAtoms.size(); m++){
 								if(rf == allAtoms.get(m).RFID){
 									a = allAtoms.get(m);
 									break;
 								}
 							}
 							
 							a.mass = molecules[i].mass.get(j) * 1.0;
 							a.charge = molecules[i].charge.get(j) * 1.0;
 							a.sigma = molecules[i].sigma.get(j) * 1.0;
 							a.epsilon = molecules[i].epsilon.get(j) * 1.0;
 							put((float)(Double.parseDouble(st.nextToken())), (float)(Double.parseDouble(st.nextToken())), (float)(Double.parseDouble(st.nextToken())),molecules[i].atoms.get(j), moleBranch, a);	
 								
 							allBranches.add(moleBranch);
 							visBranchGroup.addChild(moleBranch);
 							
 						}
 					}
 					
 				}
 				summer = 0;
 				for(int i=0; i<noC; i++){
 					for(int l = 0; l<moleNumber[i]; l++){
 					
 						BranchGroup b = new BranchGroup();
 						b.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 						b.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 						b.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 						b.setCapability(BranchGroup.ALLOW_DETACH);
 						for(int j=0;j<molecules[i].bondNo;j++){
 								int selectedId = molecules[i].bonds.get(j).index[0] +  summer +l * molecules[i].atomNo;
 								int selectedId2 = molecules[i].bonds.get(j).index[1] +  summer +l * molecules[i].atomNo;
 								int tempStrength = molecules[i].bonds.get(j).strength;
 								//System.out.println(""+selectedId+" "+selectedId2);
 								
 								putBond(selectedId, selectedId2, tempStrength, b);
 						}
 						bondBranch.add(b);
 						visBranchGroup.addChild(b);
 					}
 					summer += moleNumber[i] * molecules[i].atomNo;
 					
 				}
 				
 				
 				while(in.ready()){
 					try{
 						String s =in.readLine();
 						line++;
 						noOfSaves=Integer.parseInt(s);
 						saves.add(noOfSaves);
 						lineNo.add(line);
 					}catch(NumberFormatException e){
 						
 					}
 				}
 				main.jtext.setText("Total Saves "+lineNo.size());
 				for(int i=0; i<noC; i++){
 					for(int j=0; j<moleNumber[i]; j++){
 					
 						for(int k=0; k<molecules[i].atomNo; k++){
 							
 						}
 						
 					}
 				}
 				in.close();
 			}			
 		} catch (IOException e) {
 			main.jtext.setText("\nError : Cannot read file");
 		}
 	}
 	public static void putBond(int selectedId, int selectedId2, int tempStrength, BranchGroup moleBranchGroup ){
 		Vector3f pos = selectedMol.vec.get(selectedId);
 		Vector3f pos1 = selectedMol.vec.get(selectedId2);
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
 		moleBranchGroup.addChild(moleTransGroup);
 		Transform3D tempT = new Transform3D();
 		tempT.set(tempAA);
 		tempT.setTranslation(temp);
 			moleTransGroup.setTransform(tempT);
 			int i=tempStrength;
 			selectedMol.addBond(moleTransGroup,selectedId,selectedId2,l,i);
 			//uniVis.addBranchGraph(moleBranchGroup);
 			//visBranchGroup.addChild(moleBranchGroup);
 			moleTransGroup=null;
 			selectedId=9999;
 			selectedId2=9999;
 		
 	}
 	public void put(float x, float y, float z, int RFID, BranchGroup moleBranch, Atom a){
 		TransformGroup moleTransGroup = createBehaviors();
 		moleTransGroup.addChild(  createSphere(RFID));
 		moleBranch.addChild(moleTransGroup);
 		Vector3f temp = new Vector3f(x,y,z);
 		Transform3D tempT = new Transform3D();
 		tempT.setTranslation(temp);
 		moleTransGroup.setTransform(tempT);
 		PickCanvas pickCanvas = new PickCanvas(visualizer, moleBranch);
 	    pickCanvas.setMode(PickCanvas.BOUNDS);
 	    picking.add(pickCanvas);
 		selectedMol.addAtom(a, moleTransGroup,temp, RFID);
 		//uniVis.addBranchGraph(moleBranchGroup);
 	}
 	private static Sphere createSphere(int color) {
 		Appearance app = new Appearance();
 		Color n = new Color();
		n.newColor(color%15);
 		Color3f objColor = new Color3f(n.r, n.g, n.b);
 		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 		app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
 		Sphere sphere = new Sphere( 0.03f, Primitive.GENERATE_NORMALS, app);
 		return sphere;
 	}
 	@SuppressWarnings("static-access")
 	private static Sphere createWiredSphere() {
 		Appearance app = new Appearance();
 		Color n = new Color();
 		n.newColor(1);
 		Color3f objColor = new Color3f(n.r, n.g, n.b);
 		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 		app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
 		javax.media.j3d.PolygonAttributes pa = new javax.media.j3d.PolygonAttributes();
 		pa.setPolygonMode(pa.POLYGON_LINE);
 		app.setPolygonAttributes(pa);
 		Sphere sphere = new Sphere( 0.05f, Primitive.GENERATE_NORMALS, app);
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
 	private static TransformGroup createBehaviors() {
 		TransformGroup objTrans = new TransformGroup();
 		
 		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		return objTrans;
 	}
 	@SuppressWarnings("static-access")
 	public void loadVisualizer(){
 		GraphicsConfiguration confMole = SimpleUniverse.getPreferredConfiguration();
 		visualizer =  new Canvas3D(confMole);
 		picking = new Vector<PickCanvas>();
 		//IMP HERE
 		GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D();
 		gc3D.setSceneAntialiasing( GraphicsConfigTemplate.PREFERRED );
 		GraphicsDevice gd[] = GraphicsEnvironment.
 		getLocalGraphicsEnvironment().
 		getScreenDevices();
 		offScreenCanvas3D = new Canvas3D( gd[0].getBestConfiguration( gc3D ), true );
 		offScreenCanvas3D.setSize(1200, 650);
 		// = 
 		renderedImage =  new BufferedImage( 1200, 650, BufferedImage.TYPE_3BYTE_BGR );
 		imageComponent = new ImageComponent2D(ImageComponent.FORMAT_RGB8, renderedImage);
 		imageComponent.setCapability( ImageComponent2D.ALLOW_IMAGE_READ );
 		offScreenCanvas3D.setOffScreenBuffer( imageComponent );
 		
 		Screen3D sOn = visualizer.getScreen3D();
 	      Screen3D sOff = offScreenCanvas3D.getScreen3D();
 	      Dimension dim = sOn.getSize();
 	      dim.width *= 3;
 	      dim.height *= 3;
 	      sOff.setSize(dim);
 	      sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth()
 	          * 1);
 	      sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight()
 	          *1);
 		
 		
 		uniVis =  new SimpleUniverse(visualizer);
 		uniVis.getViewer().getView().addCanvas3D(offScreenCanvas3D);
 		
 		visBranchGroup = new BranchGroup();
 		addLights(visBranchGroup  );
 		box = createBox(sizeX * 0.1f, sizeY*0.1f, sizeZ * 0.1f);
 		visBranchGroup.addChild(box);
 		
 		visBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		visBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		visBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 		visBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
 		visualizer.addMouseListener(new MouseListener(){
 			public void mouseClicked(MouseEvent arg0) {}
 			public void mouseEntered(MouseEvent arg0) {}
 			public void mouseExited(MouseEvent arg0) {}
 			public void mousePressed(MouseEvent arg0) {
 				if(selectionState){
 					for(int i=0;i<selectedMol.total;i++){
 						picking.get(i).setShapeLocation(arg0);
 						try{
 					    PickResult result =picking.get(i).pickClosest();
 					    if (result != null) {
 					       Shape3D s = (Shape3D)result.getNode(PickResult.SHAPE3D);
 					       if (s != null) {
 					    	   main.jtext.setText("\nSelected "+(i+1));
 					    	   selectedId2 = selectedId;
 					    	   selectedId = i;
 					    	   if(selectedId2!=65500 && selectedId != 65500){
 					    		   Vector3f r = new Vector3f(selectedMol.vec.get(selectedId).x-selectedMol.vec.get(selectedId2).x,
 					    				   selectedMol.vec.get(selectedId).y-selectedMol.vec.get(selectedId2).y,
 					    				   selectedMol.vec.get(selectedId).z-selectedMol.vec.get(selectedId2).z);
 					    		   if(r.x/0.1 >= sizeX*0.5 ){
 										r.x -= sizeX*0.1;
 									}
 									else if(r.x/0.1 < -sizeX*0.5){
 										r.x += sizeX*0.1;
 									}
 									if(r.y/0.1 >= sizeY*0.5){
 										r.y -= sizeY*0.1;
 									}
 									else if(r.y/0.1 < -sizeY*0.5){
 										r.y += sizeY*0.1;
 									}
 									if(r.z/0.1 >= sizeZ*0.5){
 										r.z -= sizeZ*0.1;
 									}
 									else if(r.z/0.1 < -sizeZ*0.5){
 										r.z += sizeZ*0.1;
 									}
 					    		   double dist = r.length()*10;
 					    		   selectedId = 65500;
 					    		   selectedId2 = 65500;
 					    		   main.jtext.setText("Distance = "+dist+" A");
 					    		   String selection =main.jtext.getText();
 								   StringSelection data = new StringSelection(selection);
 								   Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 									clipboard.setContents(data, data);
 					    		   
 					    	   }
 					    	   break;
 					       }
 					    }
 						}catch(IllegalStateException e){
 							
 						}
 					}
 				}
 			}
 			public void mouseReleased(MouseEvent arg0) {}
 		});
 		visualizer.addMouseWheelListener(new MouseWheelListener(){
 
 			public void mouseWheelMoved(MouseWheelEvent arg0) {
 				if(arg0.getWheelRotation()<0){
 					scale*=0.95;
 					
 				}
 				else if(arg0.getWheelRotation()>0){
 					scale/=0.95;
 				}
 			}
 		});
 		
 		visualizer.addMouseMotionListener(new MouseMotionListener(){
 			public void mouseDragged(MouseEvent arg0) {
 			}
 			public void mouseMoved(MouseEvent arg0) {}		
 		});
 		orbit = new OrbitBehavior(visualizer, orbit.REVERSE_TRANSLATE );
 	    orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
 	    uniVis.getViewingPlatform().setViewPlatformBehavior(orbit);
 	    
 		uniVis.addBranchGraph(visBranchGroup);
 	    view = uniVis.getViewingPlatform();
 		view.setNominalViewingTransform();
 		visualizer.addMouseListener(new PopClickListener());
 		
 		
 		
 		orbit.setRotationCenter(new Point3d(sizeX/2*0.1, sizeY/2*0.1, sizeZ/2*0.1));
 	   
 		TransformGroup VpTG = uniVis.getViewingPlatform().getViewPlatformTransform();
 	    float Zcamera = 12; //put the camera 12 meters back
 	    Transform3D Trfcamera = new Transform3D();
 	    Trfcamera.setTranslation(new Vector3f((float)sizeX/2*0.1f, (float)sizeY/2*0.1f, Zcamera));  
 	    VpTG.setTransform( Trfcamera );
 		main.mainPane.add(visualizer, BorderLayout.CENTER);
 		main.mainPane.setBounds(0, 0, (int) (main.width/1.151770658), (int) (main.height/1.181538462));
 		main.setExtendedState(JFrame.MAXIMIZED_BOTH); 
 		
 		
 		
 		//v.addCanvas3D(visualizer);
 		//v.addCanvas3D(offScreenCanvas3D);
 		setToolkit();
 	}
 	public BranchGroup createBox(double d, double e, double f) {
 		BranchGroup box = new BranchGroup();
 		box.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		box.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		box.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 		box.setCapability(BranchGroup.ALLOW_DETACH);
 		
 		LineArray line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, 0));
 		line1.setCoordinate(1, new Point3d(d, 0, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, 0));
 		line1.setCoordinate(1, new Point3d(0, e, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, 0));
 		line1.setCoordinate(1, new Point3d(0, 0, f));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, e, f));
 		line1.setCoordinate(1, new Point3d(0, e, f));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, e, f));
 		line1.setCoordinate(1, new Point3d(d, e, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, e, f));
 		line1.setCoordinate(1, new Point3d(d, 0, f));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, e, 0));
 		line1.setCoordinate(1, new Point3d(d, 0, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, e, 0));
 		line1.setCoordinate(1, new Point3d(0, e, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, 0, f));
 		line1.setCoordinate(1, new Point3d(0, e, f));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(0, e, f));
 		line1.setCoordinate(1, new Point3d(0, e, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, 0, f));
 		line1.setCoordinate(1, new Point3d(d, 0, 0));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, 0, f));
 		line1.setCoordinate(1, new Point3d(0, 0, f));
 		box.addChild(new Shape3D(line1));
 		
 		line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(d, e, f));
 		line1.setCoordinate(1, new Point3d(d, 0, f));
 		box.addChild(new Shape3D(line1));
 		return box;
 	}
 	@SuppressWarnings("static-access")
 	public void setToolkit() {
 		loadMolecules();
 		jpostTool = new JPanel();
 		jpostTool.setBounds((int)(main.width/1.151770658), 0,(int) (main.width/7.588888889), (int)((int)main.height/1.024));
 		JPanel visToolPane = new JPanel(new FlowLayout());
 		visToolPane.setVisible(true);
 		main.jdesk.add(jpostTool);
 		main.frame.add(main.jdesk);
 		final DefaultListModel atomModel = new DefaultListModel();
 		for(int i = 0;i<saves.size();i++){
 			atomModel.addElement(""+saves.get(i));
 		}
 		final JList atomList = new JList(atomModel);
 		atomList.setSelectedIndex(0);
 		atomList.setVisibleRowCount(20);
 		JScrollPane atomScroll = new JScrollPane(atomList);
 		atomList.addListSelectionListener(new ListSelectionListener(){
 			public void valueChanged(ListSelectionEvent arg0) {
 				int b =	atomList.getSelectedIndex();
 				loadPosition(b);
 			}			
 		});
 		atomScroll.setSize(60, 300);
 		jpostTool.setLayout(new BorderLayout());
 		
 		jpostTool.add(atomScroll,BorderLayout.NORTH);
 		
 		JButton exportSys = new JButton("Export All Domain");
 		exportSys.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				exportSys();
 			}		
 		});
 		
 		JButton export = new JButton("Export Domain");
 		export.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame exportDomain = new JFrame("Export Domain");
 				
 				exportDomain.setBounds(400, 150, 300, 250);
 				exportDomain.setVisible(true);
 				exportDomain.setLayout(new BorderLayout());
 				
 				JPanel domainPanel = new JPanel(new GridBagLayout());
 				exportDomain.add(domainPanel, BorderLayout.CENTER);
 				exportDomain.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //				
 				JLabel jselect = new JLabel("Select Domains to Export");
 				final JComboBox ldomain = new JComboBox();
 				for(int i=0; i<noC; i++){
 						ldomain.addItem("Domain"+(i+1));
 					
 				}
 				//ldomain[0].setSelected(true);
 				
 				JPanel butPanel = new JPanel(new FlowLayout());
 				JButton butOk = new JButton("Ok");
 				JButton butCancel = new JButton("Cancel");
 				
 				butOk.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						try{
 							int selected = 99999;
 							boolean go = false;
 							if(true){
 								go = true;
 								selected = ldomain.getSelectedIndex();
 							}
 							//else
 							//	main.jtext.setText("Selected domain is a wall, cannot be exported");
 							if(go && selected!= 99999)
 								go = createExport(selected);
 							if(go)
 							exportDomain.dispose();
 						}catch(NumberFormatException e){
 							main.jtext.setText("Wrong Input"+e.getMessage());
 						}
 					}			
 				});
 				
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						exportDomain.dispose();
 						
 					}			
 				});
 				
 				butPanel.add(butOk);
 				butPanel.add(butCancel);
 				
 				domainPanel.add(jselect,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 
 				domainPanel.add(ldomain,  new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 
 				domainPanel.add(butPanel, new GBC(0,2, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 				
 			}						
 		});
 		
 		JButton calcRdf = new JButton("RDF");
 		calcRdf.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jrdf = new JFrame("Calculate RDF");
 				
 				jrdf.setBounds(400, 150, 300, 250);
 				jrdf.setVisible(true);
 				jrdf.setLayout(new BorderLayout());
 				
 				JPanel rdfpanel = new JPanel(new GridBagLayout());
 				jrdf.add(rdfpanel, BorderLayout.CENTER);
 				jrdf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //				
 				JLabel lcentre = new JLabel("Click to Select Centre Atom");
 				JLabel lother = new JLabel("Click to Select Other Atoms");
 				JLabel lcut = new JLabel("RDF Cutoff");
 				final JButton bcentre = new JButton("..");
 				final JButton bother = new JButton("..");
 				final JTextField tcut= new JTextField(""+cutOff);
 				final JSlider slide = new JSlider();
 				slide.setBorder(BorderFactory.createTitledBorder("% Smoothing"));
 			    slide.setMajorTickSpacing(20);
 			    slide.setMinorTickSpacing(5);
 			    slide.setPaintTicks(true);
 			    slide.setPaintLabels(true);
 			    
 			    
 				bcentre.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						bcentre.setText("..");
 						chooseSingleAtom(bcentre, true);							
 					}
 				});
 				bother.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						bother.setText("..");
 						chooseSingleAtom(bother, false);
 					}
 				});
 				//ldomain[0].setSelected(true);
 				
 				JPanel butPanel = new JPanel(new FlowLayout());
 				JButton butOk = new JButton("Show");
 				JButton butCancel = new JButton("Close");
 				
 				butOk.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						try{
 						if( Float.parseFloat(tcut.getText())>(0.5*sizeX)||Float.parseFloat(tcut.getText())>(0.5*sizeY)||Float.parseFloat(tcut.getText())>(0.5*sizeZ)){
 							main.jtext.setText("Cutoff radius should not be greater than half of system boundary");
 						}
 						else if(rdfMole1 != 9999 && rdfMole2 != 9999 && rdfAtom1 != 9999 && rdfAtom2 != 9999){
 							if(rdfMole1 != rdfMole2)
 								calculateRdf( Float.parseFloat(tcut.getText()), slide.getValue()/20 *1000);
 							else
 								calculateRdfSame( Float.parseFloat(tcut.getText()), slide.getValue()/20 *1000);
 							
 						}
 						}catch(NumberFormatException e){
 							main.jtext.setText("Invalid Input");
 							}
 					}			
 				});
 				
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						jrdf.dispose();
 						
 					}			
 				});
 				
 				butPanel.add(butOk);
 				butPanel.add(butCancel);
 				
 				rdfpanel.add(lcentre,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				rdfpanel.add(bcentre,  new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				rdfpanel.add(lother,  new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				rdfpanel.add(bother,  new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				rdfpanel.add(lcut,  new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				rdfpanel.add(tcut,  new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				rdfpanel.add(slide,  new GBC(0,3,2,2).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				rdfpanel.add(butPanel, new GBC(0,5, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 			}		
 		});
 		JButton calcAng = new JButton("Angles");
 		calcAng.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jangles = new JFrame("Angle Distribution");
 				
 				jangles.setBounds(400, 150, 300, 250);
 				jangles.setVisible(true);
 				jangles.setLayout(new BorderLayout());
 				
 				JPanel anglePanel = new JPanel(new GridBagLayout());
 				jangles.add(anglePanel, BorderLayout.CENTER);
 				jangles.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //				
 				JLabel lcentre = new JLabel("Click to Select Centre Atom");
 				JLabel lother = new JLabel("Click to Select Other Atoms");
 				JLabel innerDia = new JLabel("Inner Diameter");
 				JLabel outerDia = new JLabel("Outer Diameter");
 				
 				final JButton bcentre = new JButton("..");
 				final JButton bother = new JButton("..");
 				final JTextField id = new JTextField(""+0);
 				final JTextField od = new JTextField(""+cutOff);
 				
 				bcentre.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						bcentre.setText("..");
 						chooseVectors(bcentre, true);							
 					}
 				});
 				bother.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						bother.setText("..");
 						chooseVectors(bother, false);
 					}
 				});
 				//ldomain[0].setSelected(true);
 				
 				JPanel butPanel = new JPanel(new FlowLayout());
 				JButton butOk = new JButton("Ok");
 				JButton butCancel = new JButton("Cancel");
 				
 				butOk.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						if(Float.parseFloat(id.getText())>=0 && Float.parseFloat(od.getText())>0
 								&& Float.parseFloat(id.getText())<(0.5*sizeX) && Float.parseFloat(id.getText())<(0.5*sizeY) && Float.parseFloat(id.getText())<(0.5*sizeZ)
 								&& Float.parseFloat(od.getText())<(0.5*sizeX) && Float.parseFloat(od.getText())<(0.5*sizeY) && Float.parseFloat(od.getText())<(0.5*sizeZ)
 								&& Float.parseFloat(id.getText())<Float.parseFloat(od.getText()))
 						if(angleMole1 != 9999 && angleMole2 != 9999 && angleAtom11 != 9999 && angleAtom12 != 9999 && angleAtom21 != 9999 && angleAtom22 != 9999){
 							if(angleMole1 != angleMole2){
 								calculateAngle(Float.parseFloat(id.getText()), Float.parseFloat(od.getText()));
 								main.jtext.setText("Upto here");
 							}
 							else{
 								calculateSameAngle(Float.parseFloat(id.getText()), Float.parseFloat(od.getText()));
 								main.jtext.setText("Upto here + 1");
 							}
 							jangles.dispose();
 						}
 						else{
 							main.jtext.setText("Select The Vectors");
 						}
 						
 					}
 
 								
 				});
 				
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						jangles.dispose();
 						
 					}			
 				});
 				
 				butPanel.add(butOk);
 				butPanel.add(butCancel);
 				
 				anglePanel.add(lcentre,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				anglePanel.add(bcentre,  new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				anglePanel.add(lother,  new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				anglePanel.add(bother,  new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				anglePanel.add(innerDia,  new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				anglePanel.add(id,  new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				anglePanel.add(outerDia,  new GBC(0,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				anglePanel.add(od,  new GBC(1,3,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				anglePanel.add(butPanel, new GBC(0,4, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 			}			
 		});
 		JButton saveImage = new JButton("Save Image");
 		saveImage.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {	
 				offScreenCanvas3D.renderOffScreenBuffer();
 				offScreenCanvas3D.waitForOffScreenRendering();
 				System.out.println( "Rendered to offscreen" );
 
 				  try
 				  {
 					  JFileChooser jfc = new JFileChooser();
 						jfc.setCurrentDirectory(new File(main.workSpace));
 						
 						
 						int f = jfc.showSaveDialog(main.frame);
 						if(f == JFileChooser.APPROVE_OPTION){
 							File file = new File(jfc.getSelectedFile().getAbsolutePath()+".jpeg");
 							ImageIO.write(imageComponent.getRenderedImage(),"jpeg", file);
 						}
 				  
 				  }
 				  catch( Exception e )
 				  {
 				   System.err.println( "Failed to save image: " + e );
 				  }
 				  System.out.println( "Saved image." );
 				 
 			}			
 		});
 		JButton exportPol = new JButton("Export System");
 		exportPol.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame expSys = new JFrame("Export System");
 				
 				expSys.setBounds(400, 150, 300, 250);
 				expSys.setVisible(true);
 				expSys.setLayout(new BorderLayout());
 				
 				JPanel anglePanel = new JPanel(new GridBagLayout());
 				expSys.add(anglePanel, BorderLayout.CENTER);
 				expSys.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //				
 				JLabel lcentre = new JLabel("Choose File Name");
 				
 				final JButton bcentre = new JButton("..");
 				
 								
 				bcentre.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						JFileChooser jfc = new JFileChooser();
 						jfc.setCurrentDirectory(new File(main.workSpace));
 						int f = jfc.showSaveDialog(main.frame);
 						if(f == JFileChooser.APPROVE_OPTION){
 							bcentre.setText(jfc.getSelectedFile().getAbsolutePath()+".pol");
 								
 						}
 					}
 				});
 				
 				//ldomain[0].setSelected(true);
 				
 				JPanel butPanel = new JPanel(new FlowLayout());
 				JButton butOk = new JButton("Ok");
 				JButton butCancel = new JButton("Cancel");
 				
 				butOk.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						File file = new File(bcentre.getText());
 						try {
 							file.createNewFile();
 						} catch (IOException e) {
 							main.jtext.setText("\nError : Cannot create new file");
 						}
 						if(file.exists()){								
 							if(file.canWrite()){
 								try {
 									FileWriter wfile = new FileWriter(file);
 									BufferedWriter out = new BufferedWriter(wfile);
 									
 									FileReader rfile = new FileReader(caseFile);
 									BufferedReader in = new BufferedReader(rfile);
 									//BufferedReader in = new Buffered
 									
 									int verify = Integer.parseInt(in.readLine());
 									if(verify == 25011989){
 										out.write(""+512+"\n");
 										//noC = Integer.parseInt(in.readLine());
 										out.write(sizeX+"\t"+sizeY+"\t"+sizeZ+"\n");
 										out.write(in.readLine()+"\n");
 										
 										in.readLine();										
 										in.readLine();
 										
 										if(type == 0){
 											//sizeX =  Double.parseDouble(in.readLine());
 											in.readLine();
 											//sizeY =  Double.parseDouble(in.readLine());
 											in.readLine();
 											//sizeZ =  Double.parseDouble(in.readLine());
 											in.readLine();
 											//behavior = Integer.parseInt(in.readLine());
 											in.readLine();
 											if(behavior == 0){
 												//cutOff = Double.parseDouble(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 											else if(behavior == 1){
 												//cutOff = Double.parseDouble(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 											else if(behavior == 2){
 												//sigma = Double.parseDouble(in.readLine());
 												//epsilon  = Double.parseDouble(in.readLine());
 												in.readLine();
 												in.readLine();
 												//cutOff = Double.parseDouble(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 										}
 										else if(type == 1){
 											//pressure =  Double.parseDouble(in.readLine());
 											in.readLine();
 											//sizeX =  Double.parseDouble(in.readLine());
 											//sizeY =  Double.parseDouble(in.readLine());
 											//sizeZ =  Double.parseDouble(in.readLine());
 											in.readLine();
 											in.readLine();
 											in.readLine();
 										
 											//behavior = Integer.parseInt(in.readLine());
 											in.readLine();
 											if(behavior == 0){
 												//cutOff = Double.parseDouble(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 											else if(behavior == 1){
 												//cutOff = Double.parseDouble(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 											else if(behavior == 3){
 												
 											}
 										}
 										//total = Integer.parseInt(in.readLine());
 										out.write(in.readLine()+"\n");
 										//moleNumber = new int[noC];
 										//allAngles = new Vector<Vector<Angles>>();
 										//allTorsions = new Vector<Vector<Torsion>>();
 										for(int i=0; i<noC;i++){
 											//moleNumber[i] = Integer.parseInt(in.readLine());
 											out.write(in.readLine()+"\n");
 											int atoms = Integer.parseInt(in.readLine());
 											out.write(atoms+"\n");
 											
 											for(int j=0; j< atoms; j++){
 												out.write(in.readLine()+"\n");
 											}
 											int bonds = Integer.parseInt(in.readLine());
 											out.write(bonds+"\n");
 											molecules[i].bondNo = bonds;
 											for(int j=0;j<bonds;j++){
 												//StringTokenizer st = new StringTokenizer(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 											int angles  = Integer.parseInt(in.readLine());
 											out.write(angles+"\n");
 											for(int j=0;j<angles;j++){
 												//StringTokenizer st = new StringTokenizer(in.readLine());
 												out.write(in.readLine()+"\n");
 											}
 											//allAngles.add(angle);
 											int torsions  = Integer.parseInt(in.readLine());
 											out.write(torsions+"\n");
 											for(int j=0;j<torsions;j++){
 												out.write(in.readLine()+"\n");
 											}			
 										}
 										out.write(""+selectedMol.vec.size()+"\n");
 										for(int i=0; i<selectedMol.vec.size(); i++){
 											out.write(""+(i+1)+"\t"+selectedMol.vec.get(i).x+"\t"+selectedMol.vec.get(i).y+"\t"+selectedMol.vec.get(i).z+"\n");
 										}
 										for(int i=0; i<selectedMol.vec.size(); i++){
 											out.write(""+(i+1)+"\t"+velocities[i][0]+"\t"+velocities[i][1]+"\t"+velocities[i][2]+"\n");
 										}
 										out.write("End");
 									}
 									
 									in.close();
 									out.close();
 								}catch(IOException e){
 									main.jtext.setText("\nError : Could not save");
 								}
 							}
 						}
 						else{
 							main.jtext.setText("\nError : File Write Error");
 						}
 						expSys.dispose();
 					}
 
 								
 				});
 				
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						expSys.dispose();
 						
 					}			
 				});
 				
 				butPanel.add(butOk);
 				butPanel.add(butCancel);
 				
 				anglePanel.add(lcentre,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				anglePanel.add(bcentre,  new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				
 				anglePanel.add(butPanel, new GBC(0,4, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 			
 			}			
 		});
 		
 		JButton calcEnergy = new JButton("Energy");
 		calcEnergy.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				double Energy =0;
 				int summer = 0;
 				double E = 0;
 				boolean go[][] = new boolean[selectedMol.vec.size()][selectedMol.vec.size()];
 				for(int i=0; i<selectedMol.vec.size()-1; i++){
 					for(int j=i+1; j<selectedMol.vec.size(); j++){
 						go[i][j]=true;
 					}
 				}
 				
 				for(int i=0; i<molecules.length; i++){
 					for(int j=0; j<moleNumber[i]; j++){
 						for(int k=0; k<molecules[i].bondNo; k++){
 							//System.out.println(""+(molecules[i].bonds.get(k).index[0]+summer+molecules[i].atomNo * j)+"\t"+(molecules[i].bonds.get(k).index[1]+summer+molecules[i].atomNo * j));
 							Vector3f vec1 = selectedMol.vec.get(molecules[i].bonds.get(k).index[0]+summer+molecules[i].atomNo * j);
 							Vector3f vec2 = selectedMol.vec.get(molecules[i].bonds.get(k).index[1]+summer+molecules[i].atomNo * j);
 							//vec1.x *= 10;
 							//vec1.y *= 10;
 							//vec1.z *= 10;
 							//vec2.x *= 10;
 							//vec2.y *= 10;
 							//vec2.z *= 10;
 							if(molecules[i].bonds.get(k).index[0]+summer+molecules[i].atomNo * j<molecules[i].bonds.get(k).index[1]+summer+molecules[i].atomNo * j)
 								go[molecules[i].bonds.get(k).index[0]+summer+molecules[i].atomNo * j][molecules[i].bonds.get(k).index[1]+summer+molecules[i].atomNo * j] = false;
 							else
 								go[molecules[i].bonds.get(k).index[1]+summer+molecules[i].atomNo * j][molecules[i].bonds.get(k).index[0]+summer+molecules[i].atomNo * j] = false;
 							Vector3f vec4 = new Vector3f(vec1.x-vec2.x, vec1.y-vec2.y, vec1.z-vec2.z);
 							//System.out.println(""+);
 							E =  molecules[i].bonds.get(k).strength *Math.pow((molecules[i].bonds.get(k).length - vec4.length()*10),2)/2;
 							Energy += E*4.184;
 						}
 						for(int k=0; k<allAngles.get(i).size(); k++){
 							//System.out.println(""+(allAngles.get(i).get(k).cAtom+summer+molecules[i].atomNo * j)+"\t"+(allAngles.get(i).get(k).oAtoms[0]+summer+molecules[i].atomNo * j)+"\t"+(allAngles.get(i).get(k).oAtoms[1]+summer+molecules[i].atomNo * j));
 							Vector3f pos1 = selectedMol.vec.get(allAngles.get(i).get(k).oAtoms[0]+summer+molecules[i].atomNo * j);
 							Vector3f pos2 = selectedMol.vec.get(allAngles.get(i).get(k).oAtoms[1]+summer+molecules[i].atomNo * j);
 							Vector3f posc = selectedMol.vec.get(allAngles.get(i).get(k).cAtom+summer+molecules[i].atomNo * j);
 							Vector3f vec1 = new Vector3f(pos1.x-posc.x, pos1.y-posc.y, pos1.z-posc.z);
 							Vector3f vec2 = new Vector3f(pos2.x-posc.x, pos2.y-posc.y, pos2.z-posc.z);
 							double angle = vec1.angle(vec2);
 							E = 100*Math.pow(allAngles.get(i).get(k).angle*Math.PI/180-angle, 2)/2;
 							//System.out.println(""+E);
 							//System.out.println(""+(angle*180/Math.PI));
 							Energy += E*4.184;
 							if(allAngles.get(i).get(k).oAtoms[0]+summer+molecules[i].atomNo * j<allAngles.get(i).get(k).oAtoms[1]+summer+molecules[i].atomNo * j)
 							go[allAngles.get(i).get(k).oAtoms[0]+summer+molecules[i].atomNo * j][allAngles.get(i).get(k).oAtoms[1]+summer+molecules[i].atomNo * j] = false;
 							else
 								go[allAngles.get(i).get(k).oAtoms[1]+summer+molecules[i].atomNo * j][allAngles.get(i).get(k).oAtoms[0]+summer+molecules[i].atomNo * j] = false;
 						}
 						for(int k=0; k<allTorsions.get(i).size(); k++){
 							if(allTorsions.get(i).get(k).forceConst != 40.0){
 								Vector3f v1 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[0]+summer+molecules[i].atomNo * j);
 								Vector3f v2 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[1]+summer+molecules[i].atomNo * j);
 								Vector3f v3 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[2]+summer+molecules[i].atomNo * j);
 								Vector3f v4 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[3]+summer+molecules[i].atomNo * j);
 							/*	if(allTorsions.get(i).get(k).atom[0]+summer+molecules[i].atomNo * j<allTorsions.get(i).get(k).atom[1]+summer+molecules[i].atomNo * j)
 									go[allTorsions.get(i).get(k).atom[0]+summer+molecules[i].atomNo * j][allTorsions.get(i).get(k).atom[1]+summer+molecules[i].atomNo * j] = false;
 									else
 										go[allTorsions.get(i).get(k).atom[0]+summer+molecules[i].atomNo * j][allTorsions.get(i).get(k).atom[1]+summer+molecules[i].atomNo * j] = false;
 								*/
 								Vector3f v12 = new Vector3f(v2.x-v1.x, v2.y-v1.y, v2.z-v1.z);
 						        Vector3f v32 = new Vector3f(v2.x-v3.x, v2.y-v3.y, v2.z-v3.z);
 						        Vector3f cross = new Vector3f();
 						        cross.cross(v12, v32);
 						        cross.normalize();
 						        Vector3f n123 = cross;
 						        
 						       
 						    
 						        Vector3f v23 = new Vector3f(v3.x-v2.x, v3.y-v2.y, v3.z-v2.z);
 						        Vector3f v43 = new Vector3f(v3.x-v4.x, v3.y-v4.y, v3.z-v4.z);
 						        cross = new Vector3f();
 						        cross.cross(v23, v43);
 						        cross.normalize();
 						        Vector3f n234 = cross;
 						        
 						        // sign of the dihedral
 						        cross = new Vector3f();
 						        cross.cross(n123, n234);
 						        double sign = v32.dot(cross);
 						        
 						        if (sign >= 0.0) sign = -1.0;
 						        else             sign = 1.0;
 						        
 						        
 						        double angle = n123.angle(n234)*sign ;
 
 								E = 0.5 * allTorsions.get(i).get(k).forceConst *(1-Math.cos(allTorsions.get(i).get(k).periodicity *(angle-allTorsions.get(i).get(k).angle*Math.PI/180)));
 								Energy += E*4.184;
 								System.out.println("k = "+allTorsions.get(i).get(k).forceConst+"\tn = "+allTorsions.get(i).get(k).periodicity+"\t t = "+allTorsions.get(i).get(k).angle*Math.PI/180);
 							}
 							else{
 								Vector3f v1 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[0]+summer+molecules[i].atomNo * j);
 								Vector3f v2 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[1]+summer+molecules[i].atomNo * j);
 								Vector3f v3 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[2]+summer+molecules[i].atomNo * j);
 								Vector3f v4 = selectedMol.vec.get(allTorsions.get(i).get(k).atom[3]+summer+molecules[i].atomNo * j);
 								
 								
 								Vector3f v12 = new Vector3f(v2.x-v1.x, v2.y-v1.y, v2.z-v1.z);
 						        Vector3f v32 = new Vector3f(v2.x-v3.x, v2.y-v3.y, v2.z-v3.z);
 						        Vector3f cross = new Vector3f();
 						        cross.cross(v12, v32);
 						        cross.normalize();
 						        Vector3f n123 = cross;
 						        
 						       
 						    
 						        Vector3f v23 = new Vector3f(v3.x-v2.x, v3.y-v2.y, v3.z-v2.z);
 						        Vector3f v43 = new Vector3f(v3.x-v4.x, v3.y-v4.y, v3.z-v4.z);
 						        cross = new Vector3f();
 						        cross.cross(v23, v43);
 						        cross.normalize();
 						        Vector3f n234 = cross;
 						        
 						        // sign of the dihedral
 						        cross = new Vector3f();
 						        cross.cross(n123, n234);
 						        double sign = v32.dot(cross);
 						        
 						        if (sign >= 0.0) sign = -1.0;
 						        else             sign = 1.0;
 						        
 						        // and find the angle between the two planes    
 						        //return n123.angleWith(n234) * sign;
 						        
 						        double angle = n123.angle(n234) * sign;
 
 								
 								
 								
 								//System.out.println(""+c1.length()+" "+c1.lengthSquared());
 							//	double cost = -(c1.dot(c2))/(c1.length() *c2.length());
 								
 							//	double angle = Math.acos(cost);
 								
 								//System.out.println(""+allTorsions.get(i).get(k).angle*Math.PI/180 +"\t"+ angle);
 								E = 167.52 *(1+Math.cos(angle));
 								Energy += E;
 							}
 						}
 					}
 					summer+= moleNumber[i] *molecules[i].atomNo;
 				}
 				double sigma = 0;
 				double epsilon = 0;
 				System.out.println(""+sizeX+"\t"+sizeY+"\t"+sizeZ);
 				for(int i=0; i<selectedMol.vec.size()-1; i++){					
 					for(int j= i+1; j<selectedMol.vec.size(); j++){
 						Vector3f vec1 = selectedMol.vec.get(i);
 						Vector3f vec2 = selectedMol.vec.get(j);
 						Vector3f r = new Vector3f((vec1.x-vec2.x), (vec1.y-vec2.y), (vec1.z-vec2.z));
 						if(r.x/0.1 >= sizeX*0.5 ){
 							r.x -= sizeX*0.1;
 						}
 						else if(r.x/0.1 < -sizeX*0.5){
 							r.x += sizeX*0.1;
 						}
 						if(r.y/0.1 >= sizeY*0.5){
 							r.y -= sizeY*0.1;
 						}
 						else if(r.y/0.1 < -sizeY*0.5){
 							r.y += sizeY*0.1;
 						}
 						if(r.z/0.1 >= sizeZ*0.5){
 							r.z -= sizeZ*0.1;
 						}
 						else if(r.z/0.1 < -sizeZ*0.5){
 							r.z += sizeZ*0.1;
 						}
 						double len = r.length()*10;
 						//System.out.println(""+len);
 						if(len<cutOff&&go[i][j]){
 							sigma = 1.781797436 *(selectedMol.atoms.get(i).sigma + selectedMol.atoms.get(j).sigma)/2;
 							
 
 							epsilon = Math.sqrt(selectedMol.atoms.get(i).epsilon * selectedMol.atoms.get(j).epsilon);
 							//System.out.println("Sigma = "+sigma+"\tEpsilon = "+epsilon*4.184+"\tl = "+len);
 							E = 4*epsilon*(Math.pow((sigma/len), 12)- Math.pow((sigma/len), 6));
 							if(E<0){
 							//	System.out.println(""+len);
 							}
 							Energy += 4.184 *E;
 							E = selectedMol.atoms.get(i).charge * selectedMol.atoms.get(j).charge *(1/len)*332.0637;
 							Energy += 4.184 *E;
 							
 						}
 					}
 				}
 				System.out.println(""+Energy);
 				main.jtext.setText("Energy ="+Energy);
 				//System.out.println("Bond Energy = "+bondE);
 				//System.out.println("Angle Energy = "+angleE);
 				//System.out.println("Torsion Energy = "+torsionE);
 				//System.out.println("Inversion Energy = "+inversionE);
 				//System.out.println("LJ Energy = "+ljE);
 				//System.out.println("Q Energy = "+qE);
 			}			
 		});
 		
 		JButton butEn = new JButton("Energy");
 		butEn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				final JFrame jenergy = new JFrame("Energy");
 				
 				jenergy.setBounds(400, 150, 300, 250);
 				jenergy.setVisible(true);
 				jenergy.setLayout(new BorderLayout());
 				
 				JPanel energyPanel = new JPanel(new GridBagLayout());
 				jenergy.add(energyPanel, BorderLayout.CENTER);
 				jenergy.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //				
 
 				JLabel ldom = new JLabel("Select Domain");
 				JLabel len = new JLabel("Select Energy");
 				JLabel lten = new JLabel("Energy");
 				
 				//JCheckBox calForAll = new JCheckBox("For all intervals");
 				//calForAll.setSelected(false);
 				
 				final JComboBox cbdom = new JComboBox();
 				
 				cbdom.addItem("All");
 				for(int i=0; i<molecules.length; i++){
 					cbdom.addItem((i+1)+" Domain");
 				}
 				cbdom.setSelectedIndex(0);
 				
 				final JComboBox cben = new JComboBox();
 				cben.addItem("Total Energy");
 				cben.addItem("Nonbonded Energy");
 				
 				final JTextField tten = new JTextField(""+0);
 				tten.setEditable(false);
 				tten.addMouseListener(new MouseListener(){
 					public void mouseClicked(MouseEvent arg0) {}
 					public void mouseEntered(MouseEvent arg0) {}
 					public void mouseExited(MouseEvent arg0) {}
 					public void mousePressed(MouseEvent arg0) {
 						String selection = tten.getText();
 						   StringSelection data = new StringSelection(selection);
 						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 						clipboard.setContents(data, data);
 						main.jtext.setText("Data copied to clipboard");
 					}
 					public void mouseReleased(MouseEvent arg0) {}
 					
 				});
 				cbdom.addItemListener(new ItemListener(){
 					public void itemStateChanged(ItemEvent arg0) {
 						if(cbdom.getSelectedIndex() == 0){
 							cben.removeAllItems();
 							cben.addItem("Total Energy");
 							cben.addItem("Nonbonded Energy");
 						}
 						else{
 							cben.removeAllItems();
 							cben.addItem("Bond Stretch Energy");
 							cben.addItem("Bond-Angle Bend Energy");
 							cben.addItem("Dihedral Angle Torsion");
 							cben.addItem("Inversion Energy");
 						}
 					}					
 				});
 				
 				//ldomain[0].setSelected(true);
 				
 				JPanel butPanel = new JPanel(new FlowLayout());
 				JButton butOk = new JButton("Calculate");
 				JButton butCancel = new JButton("Done");
 				
 				butOk.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						double energy = 0;
 						if(cbdom.getSelectedIndex()==0){
 							if(cben.getSelectedIndex() == 0){
 								energy = askForEnergy(0, 0);
 							}
 							else if(cben.getSelectedIndex() == 1){
 								energy =askForEnergy(0, 1);
 							}
 						}
 						else{
 							energy =askForEnergy(cbdom.getSelectedIndex(), cben.getSelectedIndex());
 						}
 						tten.setText(""+energy);
 					}
 
 												
 				});
 				
 				butCancel.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent arg0) {
 						jenergy.dispose();						
 					}			
 				});
 				
 				butPanel.add(butOk);
 				butPanel.add(butCancel);
 				
 				energyPanel.add(ldom,  new GBC(0,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				energyPanel.add(cbdom,  new GBC(1,0,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				energyPanel.add(len,  new GBC(0,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				energyPanel.add(cben,  new GBC(1,1,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				energyPanel.add(lten,  new GBC(0,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				energyPanel.add(tten,  new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				//energyPanel.add(calForAll,  new GBC(0,3,2,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				//energyPanel.add(id,  new GBC(1,2,1,1).setFill(GBC.BOTH).setWeight(100, 0).setInsets(5));
 				
 				
 				energyPanel.add(butPanel, new GBC(0,4, 2, 2).setFill(GBC.HORIZONTAL).setWeight(100, 0).setInsets(5) );
 			}			
 		});
 		final JButton select = new JButton("Distance Mode Off");
 		select.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(!selectionState){
 					selectionState = true;
 					select.setText("Distance Mode On");
 				}
 				else{
 					selectionState = false;
 					select.setText("Distance Mode Off");
 				}
 			}			
 		});
 		visToolPane.add(select);
 		visToolPane.add(exportSys);
 		visToolPane.add(export);
 		visToolPane.add(calcRdf);
 		visToolPane.add(calcAng);
 		visToolPane.add(saveImage);
 		visToolPane.add(exportPol);
 		//visToolPane.add(calcEnergy);
 		visToolPane.add(butEn);
 		visToolPane.setSize(166,200);
 		visToolPane.setLayout(new FlowLayout());	
 		jpostTool.add(visToolPane, BorderLayout.CENTER);
 		jpostTool.setVisible(true);
 	}
 	public void chooseVectors(final JButton but, final boolean first){
 		final JFrame jangles = new JFrame("Choose Centre Atom");
 		
 		jangles.setBounds(200, 150, 640, 480);
 		jangles.setVisible(true);
 		jangles.setLayout(new BorderLayout());
 		
 		showingMolecule  = 0;
 		
 		selectingCone = new TransformGroup[molecules.length];
 		
 		JPanel butPanel = new JPanel(new FlowLayout());
 		JButton butOk = new JButton("Ok");
 		JButton butCancel = new JButton("Cancel");
 		
 		butOk.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(first ){
 					if(angleMole1 !=9999 && angleAtom11 != 9999 && angleAtom12 != 9999){
 						jangles.dispose();
 						but.setText("Selected");
 					}
 				}
 				else{
 					if(angleMole2 !=9999 && angleAtom21 != 9999 && angleAtom22 != 9999){
 						jangles.dispose();
 						but.setText("Selected");
 					}
 				}
 				
 			}			
 		});
 		
 		butCancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				jangles.dispose();
 				if(first ){
 					angleMole1 = 9999;
 					angleAtom11 = 9999;
 					angleAtom12 = 9999;
 					but.setText("..");
 				}
 				else{
 					angleMole2 = 9999;
 					angleAtom21 = 9999;
 					angleAtom22 = 9999;
 					but.setText("..");
 				}
 			}			
 		});
 		
 		butPanel.add(butOk);
 		butPanel.add(butCancel);
 		
 		JButton bless = new JButton("<");
 		JButton bmore = new JButton(">");
 		
 		
 		final Canvas3D angleChooser[] =  new Canvas3D[molecules.length];
 		for(int i=0; i<molecules.length; i++){
 			angleChooser[i] = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
 		}
 		SimpleUniverse angleUni[] =  new SimpleUniverse[molecules.length];
 		for(int i=0; i<molecules.length; i++){			
 			angleUni[i] = new SimpleUniverse(angleChooser[i]);
 		}
 		BranchGroup chooseBranchGroup = new BranchGroup();
 		addLights(chooseBranchGroup  );
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
 		
 		final BranchGroup moleculeGroup[] = getMolecules(angleChooser, false);
 		for(int i=0; i<molecules.length; i++){
 			addLights(moleculeGroup[i]  );
 			
 			angleChooser[i].addMouseListener(new MouseListener(){
 				public void mouseClicked(MouseEvent arg0) {	}
 				public void mouseEntered(MouseEvent arg0) {}
 				public void mouseExited(MouseEvent arg0) {}
 				public void mousePressed(MouseEvent arg0) {
 					
 					//boolean yes = false;
 					for(int j=0;j<molecules[showingMolecule].atomNo;j++){
 						chooseMol[showingMolecule].pk.get(j).setShapeLocation(arg0);
 					    PickResult result =chooseMol[showingMolecule].pk.get(j).pickClosest();
 					    if (result != null) {
 					       Shape3D s = (Shape3D)result.getNode(PickResult.SHAPE3D);
 					       if (s != null) {
 					    	   TransformGroup tg =null;
 					    	   Vector3f pos = new Vector3f();
 					    	   Vector3f pos1 = new Vector3f();
 					    	   Boolean go = false;
 					    	   if(first ){
 					    		   angleMole1 = showingMolecule;
 					    		   if(angleAtom12 != angleAtom11)
 					    			   angleAtom12 = angleAtom11;
 					    		   else{
 					    			   angleAtom12 = 9999;
 					    			   angleAtom11 = 9999;
 					    		   }
 					    		   angleAtom11 = j;
 					    		   if(angleAtom12!=9999 &&  angleAtom12 != angleAtom11){
 					    			   tg =  chooseMol[showingMolecule].moleTrans.get(angleAtom11);
 					    			   Transform3D t3d = new Transform3D();
 					    			   tg.getTransform(t3d);						    	   
 					    			   t3d.get(pos);
 					    			   tg = chooseMol[showingMolecule].moleTrans.get(angleAtom12);
 					    			   tg.getTransform(t3d);
 					    			   t3d.get(pos1);
 					    			   go = true;
 					    		   }
 					    	   }
 					    	   else{
 					    		   angleMole2 = showingMolecule;
 					    		   if(angleAtom22 != angleAtom21)
 					    			   angleAtom22 = angleAtom21;
 					    		   else{
 					    			   angleAtom22 = 9999;
 					    			   angleAtom21 = 9999;
 					    		   }
 					    		   angleAtom21 = j;
 					    		   if(angleAtom22!=9999 &&  angleAtom22!=angleAtom21){
 					    			   tg =  chooseMol[showingMolecule].moleTrans.get(angleAtom21);
 					    			   Transform3D t3d = new Transform3D();
 					    			   tg.getTransform(t3d);						    	   
 					    			   t3d.get(pos);
 					    			   tg = chooseMol[showingMolecule].moleTrans.get(angleAtom22);
 					    			   tg.getTransform(t3d);
 					    			   t3d.get(pos1);
 						    		   go = true;
 					    		   }
 					    	   }
 					    	   
 					    	   if(go){ 	   
 					   			
 					    	   		TransformGroup moleTrans = null;
 					   				moleTrans =  selectingCone[showingMolecule];
 					   				Vector3f cross = new Vector3f();
 					   				Vector3f YAXIS = new Vector3f(0, 1, 0);
 					   				Vector3f temp = new Vector3f((pos.x+pos1.x)/2,(pos.y+pos1.y)/2,(pos.z+pos1.z)/2);
 					   				Vector3f temp1 = new Vector3f((pos.x-pos1.x),(pos.y-pos1.y),(pos.z-pos1.z));
 					   				double oldl = oldL[showingMolecule];
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
 					   				moleTrans =  selectingCone[showingMolecule];
 					    		   moleTrans.setTransform(t3d);
 					    		   if(first)
 					    			   angleAtom12 = 9999;
 					    		   else
 					    			   angleAtom22 = 9999;
 					    	   }
 					   		}
 					       //main.jtext.setText(""+angleAtom11+" "+angleAtom12+" "+angleAtom21+" "+angleAtom22);
 					       break;
 					    }
 					}
 				}
 					
 				
 				public void mouseReleased(MouseEvent arg0) {}
 			});		
 		
 			angleChooser[i].addMouseMotionListener(new MouseMotionListener(){
 				public void mouseDragged(MouseEvent arg0) {}
 				public void mouseMoved(MouseEvent arg0) {}		
 			});
 		}
 		for(int i=0; i<molecules.length; i++){
 			angleUni[i].addBranchGraph(moleculeGroup[i]);
 		
 			ViewingPlatform view = angleUni[i].getViewingPlatform();
 			view.setNominalViewingTransform();
 			OrbitBehavior orbit = new OrbitBehavior(angleChooser[i], OrbitBehavior.REVERSE_TRANSLATE );
 		    orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
 		    angleUni[i].getViewingPlatform().setViewPlatformBehavior(orbit);
 
 		    float lx = 0, ly = 0, lz = 0, hx = 0, hy = 0, hz = 0;
 			for(int j=0; j<molecules[i].atomNo; j++){
 				Vector3f pos = molecules[i].pos.get(j);
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
 		    TransformGroup VpTG = angleUni[i].getViewingPlatform().getViewPlatformTransform();
 		    float Zcamera = 1.2f; //put the camera 12 meters back
 		    Transform3D Trfcamera = new Transform3D();
 		    Trfcamera.setTranslation(new Vector3f((float)(hx-lx)*0.015f, (float)(hy-ly)*0.015f, Zcamera));  
 		    VpTG.setTransform( Trfcamera );
 		}
 		bless.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(showingMolecule>0){
 					Transform3D t3d = new Transform3D();
 					Vector3f v3f = new Vector3f(100, 100, 100);
 					t3d.set(v3f);
 					for(int i=0; i<molecules.length; i++){
 						selectingCone[i].setTransform(t3d);
 					}
 					if(first){
 						angleMole1 = 9999;
 						angleAtom11 = 9999;
 						angleAtom12 = 9999;
 					}
 					else{
 						angleMole2 = 9999;
 						angleAtom21 = 9999;
 						angleAtom22 = 9999;
 					}
 					jangles.remove(angleChooser[showingMolecule]);
 					showingMolecule--;
 					jangles.add(angleChooser[showingMolecule], BorderLayout.CENTER);
 					jangles.setVisible(false);
 					jangles.setVisible(true);
 				}
 			}			
 		});
 		bmore.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(showingMolecule<molecules.length-1){
 					Transform3D t3d = new Transform3D();
 					Vector3f v3f = new Vector3f(100, 100, 100);
 					t3d.set(v3f);
 					for(int i=0; i<molecules.length; i++){
 						selectingCone[i].setTransform(t3d);
 					}
 					if(first){
 						angleMole1 = 9999;
 						angleAtom11 = 9999;
 						angleAtom12 = 9999;
 					}
 					else{
 						angleMole2 = 9999;
 						angleAtom21 = 9999;
 						angleAtom22 = 9999;
 					}
 					jangles.remove(angleChooser[showingMolecule]);
 					showingMolecule++;
 					jangles.add(angleChooser[showingMolecule], BorderLayout.CENTER);
 					jangles.setVisible(false);
 					jangles.setVisible(true);
 				}
 			}			
 		});
 		jangles.add(angleChooser[0], BorderLayout.CENTER);
 		jangles.add(bless, BorderLayout.WEST);
 		jangles.add(bmore, BorderLayout.EAST);
 		jangles.add(butPanel, BorderLayout.SOUTH);
 	}
 	@SuppressWarnings("static-access")
 	private boolean exportSys(){
 		JFileChooser jfc = new JFileChooser();
 		jfc.setCurrentDirectory(new File(main.workSpace));
 		int f = jfc.showSaveDialog(main.frame);
 		if(f == JFileChooser.APPROVE_OPTION){
 			File file = new File(jfc.getSelectedFile().getAbsolutePath()+".dom");
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
 					out.write("2");
 					out.write("\n"+1);
 					int summer[] = new int[noC];
 					int sum =0;
 					for(int i=0; i<noC; i++){
 						summer[i] = sum;
 						sum += moleNumber[i]*molecules[i].atomNo;
 					}
 						
 					out.write("\n"+sizeX+"\t"+sizeY+"\t"+sizeZ);
 					
 					for(int selected = 0; selected<noC; selected++){
 						out.write("\n"+moleNumber[selected]);
 						out.write("\n"+molecules[selected].atomNo);
 						
 						for(int i=0;i<molecules[selected].atomNo;i++){
 							Atom a1 = null;
 							for(int j=0; j<allAtoms.size(); j++){
 								if(allAtoms.get(j).RFID ==  molecules[selected].atoms.get(i))
 									a1 = allAtoms.get(j);
 							}
 							out.write("\n"+a1.RFID+" "+a1.name+" "+molecules[selected].mass.get(i)+" "+molecules[selected].charge.get(i)+" "+molecules[selected].sigma.get(i)+" "+molecules[selected].epsilon.get(i)+" "+a1.maxvalency+" "+a1.bondLength+" "+a1.bondAngle+" "+a1.hybridization+" "+(selectedMol.vec.get(summer[selected]+i).x)/0.1+" "+(selectedMol.vec.get(summer[selected]+i).y)/0.1+" "+(selectedMol.vec.get(summer[selected]+i).z)/0.1);
 						}
 						out.write("\n"+molecules[selected].bondNo);
 						for(int i=0;i<molecules[selected].bondNo;i++){
 							Bond b = molecules[selected].bonds.get(i);
 							out.write("\n"+b.index[0]+" "+b.index[1]+" "+" "+b.length+" "+b.strength);
 						}
 						out.write("\n"+allAngles.get(selected).size());
 						for(int i=0; i<allAngles.get(selected).size(); i++){
 							Angles a = allAngles.get(selected).get(i);
 							out.write("\n"+a.cAtom+" "+a.oAtoms[0]+" "+a.oAtoms[1]+" "+a.angle+" "+a.strength);
 						}
 						out.write("\n"+allTorsions.get(selected).size());
 						for(int i=0; i<allTorsions.get(selected).size(); i++){
 							Torsion t = allTorsions.get(selected).get(i);
 							out.write("\n"+t.atom[0]+" "+t.atom[1]+" "+t.atom[2]+" "+t.atom[3]+" "+t.forceConst+" "+t.angle+" "+t.periodicity);
 						}
 					}
 					for(int selected = 0; selected<noC; selected++){
 						out.write("\n"+moleNumber[selected]);
 						for(int i=0; i<moleNumber[selected]; i++){
 							for(int j=0; j<molecules[selected].atomNo; j++){
 								out.write("\n"+j+"\t"+(selectedMol.vec.get(summer[selected]+i*molecules[selected].atomNo+j).x)/0.1+"\t"+(selectedMol.vec.get(summer[selected]+i*molecules[selected].atomNo+j).y)/0.1+"\t"+(selectedMol.vec.get(summer[selected]+i*molecules[selected].atomNo+j).z)/0.1);
 							}
 						}
 					}
 					out.write("\nEnd");
 					out.close();
 					wfile.close();
 					return true;
 				}catch(IOException e){
 					
 					main.jtext.setText("\nError : Could not save");
 					return false;
 				}
 				} 
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 				return false;
 			}
 		}
 		return false;
 	}
 	@SuppressWarnings("static-access")
 	private boolean createExport(int selected) {
 		JFileChooser jfc = new JFileChooser();
 		jfc.setCurrentDirectory(new File(main.workSpace));
 		int f = jfc.showSaveDialog(main.frame);
 		if(f == JFileChooser.APPROVE_OPTION){
 			File file = new File(jfc.getSelectedFile().getAbsolutePath()+".dom");
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
 					out.write("2");
 					out.write("\n"+1);
 					int summer[] = new int[noC];
 					int sum =0;
 					for(int i=0; i<noC; i++){
 						summer[i] = sum;
 						sum += moleNumber[i]*molecules[i].atomNo;
 					}
 					float hx=0, hy=0, hz=0, lx=0, ly=0, lz =0;
 						for(int j=0; j<moleNumber[selected]; j++){			
 							if(j==0){
 								hx = lx = selectedMol.vec.get(summer[selected]+0+j*molecules[selected].atomNo).x;
 								hy = ly = selectedMol.vec.get(summer[selected]+0+j*molecules[selected].atomNo).y;
 								hz = lz = selectedMol.vec.get(summer[selected]+0+j*molecules[selected].atomNo).z;
 							}
 							for(int i=0;i<molecules[selected].atomNo;i++){
 								if(lx>selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).x)
 									lx = selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).x;
 								else if(hx<selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).x)
 									hx = selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).x;
 								if(ly>selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).y)
 									ly = selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).y;
 								else if(hy<selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).y)
 									hy = selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).y;
 								if(lz>selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).z)
 									lz = selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).z;
 								else if(hz<selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).z)
 									hz = selectedMol.vec.get(summer[selected]+i+j*molecules[selected].atomNo).z;
 							}
 						}
 						//System.out.println(""+hx+" "+lx+" "+hy+" "+ly+" "+hz+" "+lz);
 					float mx = hx-lx;
 					float my = hy-ly;
 					float mz = hz-lz;
 					out.write("\n"+mx/0.1+"\t"+my/0.1+"\t"+mz/0.1);
 	
 						out.write("\n"+moleNumber[selected]);
 						out.write("\n"+molecules[selected].atomNo);
 						
 						for(int i=0;i<molecules[selected].atomNo;i++){
 							Atom a1 = null;
 							for(int j=0; j<allAtoms.size(); j++){
 								if(allAtoms.get(j).RFID ==  molecules[selected].atoms.get(i))
 									a1 = allAtoms.get(j);
 							}
 							out.write("\n"+a1.RFID+" "+a1.name+" "+molecules[selected].mass.get(i)+" "+molecules[selected].charge.get(i)+" "+molecules[selected].sigma.get(i)+" "+molecules[selected].epsilon.get(i)+" "+a1.maxvalency+" "+a1.bondLength+" "+a1.bondAngle+" "+a1.hybridization+" "+(selectedMol.vec.get(summer[selected]+i).x-lx)/0.1+" "+(selectedMol.vec.get(summer[selected]+i).y-ly)/0.1+" "+(selectedMol.vec.get(summer[selected]+i).z-lz)/0.1);
 						}
 						out.write("\n"+molecules[selected].bondNo);
 						for(int i=0;i<molecules[selected].bondNo;i++){
 							Bond b = molecules[selected].bonds.get(i);
 							out.write("\n"+b.index[0]+" "+b.index[1]+" "+" "+b.length+" "+b.strength);
 						}
 						out.write("\n"+allAngles.get(selected).size());
 						for(int i=0; i<allAngles.get(selected).size(); i++){
 							Angles a = allAngles.get(selected).get(i);
 							out.write("\n"+a.cAtom+" "+a.oAtoms[0]+" "+a.oAtoms[1]+" "+a.angle+" "+a.strength);
 						}
 						out.write("\n"+allTorsions.get(selected).size());
 						for(int i=0; i<allTorsions.get(selected).size(); i++){
 							Torsion t = allTorsions.get(selected).get(i);
 							out.write("\n"+t.atom[0]+" "+t.atom[1]+" "+t.atom[2]+" "+t.atom[3]+" "+t.forceConst+" "+t.angle+" "+t.periodicity);
 						}
 						
 						out.write("\n"+moleNumber[selected]);
 						for(int i=0; i<moleNumber[selected]; i++){
 							for(int j=0; j<molecules[selected].atomNo; j++){
 								out.write("\n"+j+"\t"+(selectedMol.vec.get(summer[selected]+i*molecules[selected].atomNo+j).x-lx)/0.1+"\t"+(selectedMol.vec.get(summer[selected]+i*molecules[selected].atomNo+j).y-ly)/0.1+"\t"+(selectedMol.vec.get(summer[selected]+i*molecules[selected].atomNo+j).z-lz)/0.1);
 							}
 						}
 					
 					out.write("\nEnd");
 					out.close();
 					wfile.close();
 					return true;
 				}catch(IOException e){
 					
 					main.jtext.setText("\nError : Could not save");
 					return false;
 				}
 				} 
 			}
 			else{
 				main.jtext.setText("\nError : File Write Error");
 				return false;
 			}
 		}
 		return false;
 		
 	}
 	@SuppressWarnings("static-access")
 	public void loadPosition(int b) {
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(dataFile));
 			for(int i=0; i<lineNo.get(b); i++){
 				in.readLine();
 			}
 			
 			if(type == 1&&b==0){
 				
 				String s= in.readLine();
 				StringTokenizer st = new StringTokenizer(s);
 				sizeX = Double.parseDouble(st.nextToken());
 				sizeY = Double.parseDouble(st.nextToken());
 				sizeZ = Double.parseDouble(st.nextToken());
 			}
 			else if(type == 1){
 				in.readLine();
 				String s= in.readLine();
 				StringTokenizer st = new StringTokenizer(s);
 				sizeX = Double.parseDouble(st.nextToken());
 				sizeY = Double.parseDouble(st.nextToken());
 				sizeZ = Double.parseDouble(st.nextToken());
 			}
 			box.detach();
 			box = null;
 			box = createBox(sizeX * 0.1f, sizeY*0.1f, sizeZ * 0.1f);
 			visBranchGroup.addChild(box);
 			orbit.setRotationCenter(new Point3d(sizeX/2*0.1, sizeY/2*0.1, sizeZ/2*0.1));
 			TransformGroup VpTG = uniVis.getViewingPlatform().getViewPlatformTransform();
 
 		    float Zcamera = 12; //put the camera 12 meters back
 		    Transform3D Trfcamera = new Transform3D();
 		    Trfcamera.setTranslation(new Vector3f((float)sizeX/2*0.1f, (float)sizeY/2*0.1f, Zcamera));  
 		    if(start){
 		    	VpTG.setTransform( Trfcamera );
 		    	start = false;
 		    }
 			int summer = 0;
 			for(int i=0; i<noC; i++){
 				
 				for(int l=0; l<moleNumber[i]; l++){
 					for(int j=0; j<molecules[i].atomNo; j++){
 						StringTokenizer st = new StringTokenizer(in.readLine());
 						st.nextToken();
 						float x =(float)Double.parseDouble(st.nextToken());
 						float y =(float)Double.parseDouble(st.nextToken());
 						float z = (float)Double.parseDouble(st.nextToken());
 						float tx = x;
 						float ty = y;
 						float tz = z;
 						if(type == 4){
 						if(x>sizeX*0.1)
 							tx = (float) (x-sizeX*0.1);
 						if(y>sizeY*0.1)
 							ty = (float) (y-sizeY*0.1);
 						if(z>sizeZ*0.1)
 							tz = (float) (z-sizeZ*0.1);
 						if(x<0)
 							tx = (float) (x+sizeX*0.1);
 						if(y<0)
 							ty = (float) (y+sizeY*0.1);
 						if(z<0)
 							tz = (float) (z+sizeZ*0.1);
 						}
 						//if(x!=tx||y!=ty||z!=tz)
 						//System.out.println(""+x+" "+y+" "+z+" "+tx+" "+ty+" "+tz);
 						//if(type == 0)
 						//	placeAtom((float)(x-sizeX/4 + show[i] * 1000), (float)(y-sizeY/4 + show[i] * 1000), (float)(z-sizeZ/4+ show[i] * 1000), summer + l * molecules[i].atomNo + j);
 						//else
 							placeAtom((float)(tx ), (float)(ty), (float)(tz), summer + l * molecules[i].atomNo + j);							
 					}
 				}				
 				summer += moleNumber[i] * molecules[i].atomNo;
 			}
 			for(int i=0; i<total ; i++){
 				
 				StringTokenizer st = new StringTokenizer(in.readLine());
 				st.nextToken();
 				float x =(float)Double.parseDouble(st.nextToken());
 				float y =(float)Double.parseDouble(st.nextToken());
 				float z = (float)Double.parseDouble(st.nextToken());
 				velocities[i][0] = x;
 				velocities[i][1] = y;
 				velocities[i][2] = z;
 				
 			}
 		} catch (FileNotFoundException e) {
 			main.jtext.setText("File not found");
 		} catch (IOException e) {
 			main.jtext.setText("Cannot read file");
 		}
 		
 	}
 	public void placeAtom(float x, float y, float z, int chosen){
 		//System.out.println(""+x+" "+y+" "+z);
 		Transform3D t3d = new Transform3D();
 		t3d.setTranslation(new Vector3f(x,y,z));
 		TransformGroup moleTransGroup=selectedMol.moleTrans.get(chosen);
 		moleTransGroup.setTransform(t3d);
 		selectedMol.vec.remove(chosen);
 		selectedMol.vec.add(chosen, new Vector3f(x,y,z));
 		int yesb =0;
 		Vector<Num> toMove=new Vector<Num>();
 		for(int i=0;i<selectedMol.bondTotal;i++)
 		{
 			int s1=selectedMol.bonds.get(i).index[0];
 			int s2=selectedMol.bonds.get(i).index[1];
 			if(s1==chosen){
 				yesb++;
 				Num m=new Num();
 				m.n =i;
 				toMove.add(m);
 			}
 			else if(s2==chosen){
 				yesb++;
 				Num m=new Num();
 				m.n =i;
 				toMove.add(m);
 			}
 			
 		}
 		if(toMove!=null){
 			for(int i=0;i<yesb;i++){
 				TransformGroup moleTrans = selectedMol.bondTrans.get(toMove.get(i).n);
 				Vector3f pos = new Vector3f(x,y,z);
 				int s1[] = selectedMol.bonds.get(toMove.get(i).n).index;
 				Vector3f pos1 = selectedMol.vec.get(s1[1]);
 				if(s1[1]==chosen){
 					pos1 = selectedMol.vec.get(s1[0]);
 				}
 				Vector3f cross = new Vector3f();
 				Vector3f YAXIS = new Vector3f(0, 1, 0);
 				Vector3f temp = new Vector3f((pos.x+pos1.x)/2,(pos.y+pos1.y)/2,(pos.z+pos1.z)/2);
 				Vector3f away = new Vector3f(1000, 1000, 1000);
 				Vector3f temp1 = new Vector3f((pos.x-pos1.x),(pos.y-pos1.y),(pos.z-pos1.z));
 				double oldl = selectedMol.bonds.get(toMove.get(i).n).length;
 				double l = Math.sqrt(Squares.sqr(pos.x-pos1.x)+Squares.sqr(pos.y-pos1.y)+Squares.sqr(pos.z-pos1.z));
 				temp1.normalize();
 				l=l/oldl;
 				if(l>5)
 					temp.add(away);
 				cross.cross(YAXIS,temp1);
 				AxisAngle4f tempAA = new AxisAngle4f();
 				tempAA.set(cross,(float)Math.acos(YAXIS.dot(temp1)));
 				Transform3D tempT = new Transform3D();
 				tempT.set(tempAA);
 				tempT.setTranslation(temp);
 				tempT.setScale(new Vector3d(1,l,1));
 				moleTrans.setTransform(tempT);
 			}
 		}
 }
 	@SuppressWarnings("static-access")
 	public void clear(){
 		noC = 0; type =0; behavior = 0; total =0; moleNumber = null; atomToCheck = 0;
 		sizeX=0; sizeY=0; sizeZ=0; cutOff=0; sigma=0; epsilon=0; pressure=0; temp =0; maxSigma = 0;
 		molecules = null;
 		allAtoms.removeAllElements();
 		allAtoms = null;
 		lineNo.removeAllElements();
 		lineNo = null;
 		saves.removeAllElements();
 		saves = null;
 		allAngles.removeAllElements();
 		allAngles = null;
 		allTorsions.removeAllElements();
 		allTorsions = null;
 		box.detach();
 		jpostTool.setVisible(false);
 		main.jdesk.remove(jpostTool);
 		visBranchGroup.detach();
 		caseFile = null;
 		dataFile = null;
 		molecules = null;
 		main.mainPane.remove(visualizer);
 		uniVis =null;
 		orbit = null;
 		view = null;
 		visBranchGroup = null;
 		visualizer = null;
 		main.working = false;
 		main.post = null;
 	}
 	@SuppressWarnings("static-access")
 	public void destroy(){
 		noC = 0; type =0; behavior = 0; total =0; moleNumber = null; atomToCheck = 0;
 		sizeX=0; sizeY=0; sizeZ=0; cutOff=0; sigma=0; epsilon=0; pressure=0; temp =0; maxSigma = 0;
 		molecules = null;
 		allAtoms.removeAllElements();
 		allAtoms = null;
 		lineNo.removeAllElements();
 		lineNo = null;
 		saves.removeAllElements();
 		saves = null;
 		allAngles.removeAllElements();
 		allAngles = null;
 		allTorsions.removeAllElements();
 		allTorsions = null;
 		box.detach();
 		jpostTool.setVisible(false);
 		main.jdesk.remove(jpostTool);
 		visBranchGroup.detach();
 		caseFile = null;
 		dataFile = null;
 		molecules = null;
 		main.mainPane.remove(visualizer);
 		uniVis =null;
 		orbit = null;
 		view = null;
 		visBranchGroup = null;
 		visualizer = null;
 		main.working = false;
 		main.post = null;
 		System.exit(0);
 	}
 	private void addLights(BranchGroup visBranchGroup) {
 		/*Color3f color = new Color3f( 1.0f,1.0f,1.0f );
 		Vector3f direction = new Vector3f( 0.0f, -0.0f, -2.0f );
 		DirectionalLight light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		visBranchGroup.addChild( light );
 		direction = new Vector3f( -0.0f, 0.0f, 2.0f );
 		light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		visBranchGroup.addChild( light );
 		direction = new Vector3f( 0.0f, -2.0f, 0.0f );
 		light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		visBranchGroup.addChild( light );*/
 		Color3f color = new Color3f( 1.0f,1.0f,1.0f );
 		Vector3f direction = new Vector3f( 0.0f, -0.0f, -2.0f );
 		DirectionalLight light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		visBranchGroup.addChild( light );
 		direction = new Vector3f( -2.0f, 0.0f, 2.0f );
 		light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		visBranchGroup.addChild( light );
 		direction = new Vector3f( 2.0f, -2.0f, 0.0f );
 		light = new DirectionalLight( color, direction );
 		light.setInfluencingBounds( new BoundingSphere( new Point3d(0.0,0.0,0.0), 200.0 ));
 		visBranchGroup.addChild( light );
 	}
 	@SuppressWarnings("serial")
 	class PopUpDemo extends JPopupMenu {
 	    JMenuItem anItem[];
 	    public PopUpDemo(){
 	        anItem = new JMenuItem[noC];
 	        for(int i=0; i<noC; i++){
 	        	anItem[i] = new JMenuItem("Molecule "+(i+1));
 	        	final int j = i;
 	        	anItem[i].addActionListener(new ActionListener(){
 					@SuppressWarnings("static-access")
 					public void actionPerformed(ActionEvent arg0) {
 						main.jtext.setText(""+show[j]);
 						if(!show[j] ){
 							int summer = 0;
 							for(int u=0; u<j; u++){
 								summer += moleNumber[u]*molecules[u].atomNo;
 							}
 							for(int u=0; u<moleNumber[j]; u++){
 								for(int v=0; v<molecules[j].atomNo; v++)
 								allBranches.get(summer+u*molecules[j].atomNo+v).detach();
 							}
 							summer =0;
 							for(int u=0; u<j; u++){
 								summer += moleNumber[u];
 							}
 							for(int u=0; u<moleNumber[j]; u++)
 								bondBranch.get(summer+u).detach();
 							show[j] = true;
 							/*int summer = 0;
 							for(int m=0; m<j; m++)
 								summer = summer + moleNumber[m] * molecules[m].atomNo;
 							for(int m =0; m<moleNumber[j]; m++){
 								for(int n=0; n<molecules[j].atomNo; n++){
 									Vector3f v3f = selectedMol.vec.get(summer + m * molecules[j].atomNo+ n);
 									placeAtom(v3f.x + 1000, v3f.y+ 1000, v3f.z+ 1000, summer + m * molecules[j].atomNo+ n);
 								}
 							}*/
 						}
 						else {
 							int summer = 0;
 							for(int u=0; u<j; u++){
 								summer += moleNumber[u]*molecules[u].atomNo;
 							}
 							for(int u=0; u<moleNumber[j]; u++){
 								for(int v=0; v<molecules[j].atomNo; v++)
 								visBranchGroup.addChild(allBranches.get(summer+u*molecules[j].atomNo+v));
 							}
 							summer =0;
 							for(int u=0; u<j; u++){
 								summer += moleNumber[u];
 							}
 							for(int u=0; u<moleNumber[j]; u++)
 								visBranchGroup.addChild(bondBranch.get(summer+u));
 							show[j]=false;
 							/*int summer = 0;
 							for(int m=0; m<j; m++)
 								summer = summer + moleNumber[m] * molecules[m].atomNo;
 							for(int m =0; m<moleNumber[j]; m++){
 								for(int n=0; n<molecules[j].atomNo; n++){
 									Vector3f v3f = selectedMol.vec.get(summer + m * molecules[j].atomNo+ n);
 									placeAtom(v3f.x - 1000, v3f.y- 1000, v3f.z- 1000, summer + m * molecules[j].atomNo+ n);
 								}
 							}*/
 						}
 					}	        		
 	        	});
 	        	
 	        	this.add(anItem[i]);
 	        }
 	    }
 	}
 	class PopClickListener extends MouseAdapter {
 	    public void mousePressed(MouseEvent e){
 	        
 	    }
 
 	    public void mouseReleased(MouseEvent e){
 	    	if(e.getButton() == MouseEvent.BUTTON3){
 	        	  doPop(e);
 	        }
 	    }
 
 	    private void doPop(MouseEvent e){
 	        PopUpDemo menu = new PopUpDemo();
 	       	
 	        menu.show(e.getComponent(), e.getX(), e.getY());
 	    }
 	}
 
 	
 	public void chooseSingleAtom(final JButton but, final boolean first ) {
 		final JFrame jrdf = new JFrame("Choose Centre Atom");
 		
 		jrdf.setBounds(200, 150, 640, 480);
 		jrdf.setVisible(true);
 		jrdf.setLayout(new BorderLayout());
 		
 		showingMolecule  = 0;
 		
 		selectingSphere = new TransformGroup[molecules.length];
 		
 		JPanel butPanel = new JPanel(new FlowLayout());
 		JButton butOk = new JButton("Ok");
 		JButton butCancel = new JButton("Cancel");
 		
 		butOk.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(first ){
 					if(rdfMole1 !=9999 && rdfAtom1 != 9999){
 						jrdf.dispose();
 						but.setText("Selected");
 					}
 				}
 				else{
 					if(rdfMole2 !=9999 && rdfAtom2 != 9999){
 						jrdf.dispose();
 						but.setText("Selected");
 					}
 				}
 				
 			}			
 		});
 		
 		butCancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				jrdf.dispose();
 				if(first ){
 					rdfMole1 = 9999;
 					rdfAtom1 = 9999;
 					but.setText("..");
 				}
 				else{
 					rdfMole2 = 9999;
 					rdfAtom2 = 9999;
 					but.setText("..");
 				}
 			}			
 		});
 		
 		butPanel.add(butOk);
 		butPanel.add(butCancel);
 		
 		JButton bless = new JButton("<");
 		JButton bmore = new JButton(">");
 		
 		
 		final Canvas3D rdfChooser[] =  new Canvas3D[molecules.length];
 		for(int i=0; i<molecules.length; i++){
 			rdfChooser[i] = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
 		}
 		SimpleUniverse rdfUni[] =  new SimpleUniverse[molecules.length];
 		for(int i=0; i<molecules.length; i++){			
 			rdfUni[i] = new SimpleUniverse(rdfChooser[i]);
 		}
 		BranchGroup chooseBranchGroup = new BranchGroup();
 		addLights(chooseBranchGroup  );
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);  
 		chooseBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
 		
 		final BranchGroup moleculeGroup[] = getMolecules(rdfChooser, true);
 		for(int i=0; i<molecules.length; i++){
 			addLights(moleculeGroup[i]  );
 			
 			rdfChooser[i].addMouseListener(new MouseListener(){
 				public void mouseClicked(MouseEvent arg0) {	}
 				public void mouseEntered(MouseEvent arg0) {}
 				public void mouseExited(MouseEvent arg0) {}
 				@SuppressWarnings("static-access")
 				public void mousePressed(MouseEvent arg0) {
 					
 					//boolean yes = false;
 					for(int j=0;j<molecules[showingMolecule].atomNo;j++){
 						chooseMol[showingMolecule].pk.get(j).setShapeLocation(arg0);
 					    PickResult result =chooseMol[showingMolecule].pk.get(j).pickClosest();
 					    if (result != null) {
 					       Shape3D s = (Shape3D)result.getNode(PickResult.SHAPE3D);
 					       if (s != null) {
 					    	   if(first ){
 					    		   rdfMole1 = showingMolecule;
 					    		   rdfAtom1 = j;
 					    	   }
 					    	   else{
 					    		   rdfMole2 = showingMolecule;
 					    		   rdfAtom2 = j;
 					    	   }
 					    	   
 					    	   TransformGroup tg = chooseMol[showingMolecule].moleTrans.get(j);
 					    	   Transform3D t3d = new Transform3D();
 					    	   tg.getTransform(t3d);
 					    	   
 					    	   selectingSphere[showingMolecule].setTransform(t3d);
 							   main.jtext.setText(""+j);
 					    	   break;
 					       }
 					    }
 					}
 					
 				}
 				public void mouseReleased(MouseEvent arg0) {}
 			});		
 		
 			rdfChooser[i].addMouseMotionListener(new MouseMotionListener(){
 				public void mouseDragged(MouseEvent arg0) {}
 				public void mouseMoved(MouseEvent arg0) {}		
 			});
 		}
 		for(int i=0; i<molecules.length; i++){
 			rdfUni[i].addBranchGraph(moleculeGroup[i]);
 		
 			ViewingPlatform view = rdfUni[i].getViewingPlatform();
 			view.setNominalViewingTransform();
 			OrbitBehavior orbit = new OrbitBehavior(rdfChooser[i], OrbitBehavior.REVERSE_TRANSLATE );
 		    orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
 		    rdfUni[i].getViewingPlatform().setViewPlatformBehavior(orbit);
 
 		    float lx = 0, ly = 0, lz = 0, hx = 0, hy = 0, hz = 0;
 			for(int j=0; j<molecules[i].atomNo; j++){
 				Vector3f pos = molecules[i].pos.get(j);
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
 		    TransformGroup VpTG = rdfUni[i].getViewingPlatform().getViewPlatformTransform();
 		    float Zcamera = 1.2f; //put the camera 12 meters back
 		    Transform3D Trfcamera = new Transform3D();
 		    Trfcamera.setTranslation(new Vector3f((float)(hx-lx)*0.015f, (float)(hy-ly)*0.015f, Zcamera));  
 		    VpTG.setTransform( Trfcamera );
 		}
 		bless.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(showingMolecule>0){
 					Transform3D t3d = new Transform3D();
 					Vector3f v3f = new Vector3f(100, 100, 100);
 					t3d.set(v3f);
 					for(int i=0; i<molecules.length; i++){
 						selectingSphere[i].setTransform(t3d);
 					}
 					
 					jrdf.remove(rdfChooser[showingMolecule]);
 					showingMolecule--;
 					jrdf.add(rdfChooser[showingMolecule], BorderLayout.CENTER);
 					jrdf.setVisible(false);
 					jrdf.setVisible(true);
 				}
 			}			
 		});
 		bmore.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if(showingMolecule<molecules.length-1){
 					Transform3D t3d = new Transform3D();
 					Vector3f v3f = new Vector3f(100, 100, 100);
 					t3d.set(v3f);
 					for(int i=0; i<molecules.length; i++){
 						selectingSphere[i].setTransform(t3d);
 					}
 					jrdf.remove(rdfChooser[showingMolecule]);
 					showingMolecule++;
 					jrdf.add(rdfChooser[showingMolecule], BorderLayout.CENTER);
 					jrdf.setVisible(false);
 					jrdf.setVisible(true);
 				}
 			}			
 		});
 		jrdf.add(rdfChooser[0], BorderLayout.CENTER);
 		jrdf.add(bless, BorderLayout.WEST);
 		jrdf.add(bmore, BorderLayout.EAST);
 		jrdf.add(butPanel, BorderLayout.SOUTH);
 		//System.out.println(""+molecules.length);
 	}
 	@SuppressWarnings("static-access")
 	public void calculateRdf(float cutOff, int avg){
 		if(avg == 0)
 			avg = 2;
 		final float vec1[][] = new float[moleNumber[rdfMole1]][3];
 		final float vec2[][] = new float[moleNumber[rdfMole2]][3];
 		//System.out.println(vec1.length);
 		//System.out.println(vec2.length);
 		Vector3f temp = null;
 		final int length1 = moleNumber[rdfMole1];
 		final int length2 = moleNumber[rdfMole2];
 		final float dr = 0.0001f;
 		final int bin[] = new int[(int)(cutOff/dr)];
 		
 		
 		int summer1 = 0;
 		for(int i=0; i<rdfMole1; i++)
 			summer1 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[rdfMole1]; i++){
 			temp = selectedMol.vec.get(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1);
 			vec1[i][0] = temp.x;
 			vec1[i][1] = temp.y;
 			vec1[i][2] = temp.z;
 			//System.out.println(""+(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1));
 		}
 		
 		int summer2 = 0;
 		for(int i=0; i<rdfMole2; i++)
 			summer2 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[rdfMole2]; i++){
 			temp = selectedMol.vec.get(summer2 + i*molecules[rdfMole2].atomNo + rdfAtom2);
 			vec2[i][0] = temp.x;
 			vec2[i][1] = temp.y;
 			vec2[i][2] = temp.z;
 			//System.out.println(""+(summer2 + i*molecules[rdfMole2].atomNo + rdfAtom2));
 		}
 		//System.out.println(""+vec1.length +" "+vec2.length);
 		final float size = (float) ( cutOff);
 		final float sizex = (float) (sizeX);
 		final float sizey = (float) (sizeX);
 		final float sizez = (float) (sizeX);
 		for(int i=0; i<length1; i++){
 			for(int j=0; j<length2; j++){
 				float dx = vec1[i][0]-vec2[j][0];
 				float dy = vec1[i][1]-vec2[j][1];
 				float dz = vec1[i][2]-vec2[j][2];
 				if(dx/0.1f >= sizex * 0.5f)
 					dx -= sizex*0.1f;
 				else if(dx/0.1f < -sizex *0.5f)
 					dx += sizex*0.1f;
 				if(dy/0.1f >= sizey * 0.5f)
 					dy -= sizey*0.1f;
 				else if(dy/0.1f < -sizey *0.5f)
 					dy += sizey*0.1f;
 				if(dz/0.1f >= sizez * 0.5f)
 					dz -= sizez*0.1f;
 				else if(dz/0.1f < -sizez *0.5f)
 					dz += sizez*0.1f;
 				float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
 				if((dist/0.1f)<size)
 					bin[(int)(dist/(0.1f*dr))]++;
 			}
 		}
 		/*Kernel rdfKernel = new Kernel(){
 			public void run() {
 				int gid = getGlobalId();
 				if(gid%3 == 0){
 					for(int i=0; i<length2; i++){
 						float dx = (vec1[gid*3]-vec2[i*3]);
 						float dy = (vec1[gid*3+1]-vec2[i*3+1]);
 						float dz = (vec1[gid*3+2]-vec2[i*3+2]);
 						if(dx/0.1f >= sizex * 0.5f)
 							dx -= sizex*0.1f;
 						else if(dx/0.1f < -sizex *0.5f)
 							dx += sizex*0.1f;
 						if(dy/0.1f >= sizey * 0.5f)
 							dy -= sizey*0.1f;
 						else if(dy/0.1f < -sizey *0.5f)
 							dy += sizey*0.1f;
 						if(dz/0.1f >= sizez * 0.5f)
 							dz -= sizez*0.1f;
 						e40lse if(dz/0.1f < -sizez *0.5f)
 							dz += sizez*0.1f;
 						float dist =sqrt(dx * dx + dy * dy + dz * dz);
 						if((dist/0.1f)<size)
 							bin[(int)(dist/(0.1f*dr))]++;
 					}
 				}
 			}			
 		};
 		
 		rdfKernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
 		rdfKernel.execute(Range.create(length1));
 		System.out.println(rdfKernel.getExecutionMode()+" Time = "+rdfKernel.getExecutionTime());
 		*/
 		/*float dist = 0;
 		for(int i=0; i<length1; i++){
 			for(int j=0; j<length2; j++){
 				dist = (float) Math.sqrt(vec1[i*3]*vec2[j*3]+vec1[i*3+1]*vec2[i*3+1]+vec1[i*3+2]*vec2[i*3+2]);
 				System.out.println(""+dist);
 				if(dist/0.1<cutOff)
 					bin[(int)(dist/0.1)]++;
 			}
 		}*/
 		double V = 4 * Math.PI * Math.pow(size, 3)/ 3;
 		File rdfFile = new File(main.workSpace+"/"+caseFile.getName()+"rdf.dat");
 		final float data[] = new float[(int) (size/dr)-avg];
 		float maxAvg = 0;
 		float avgOut=0;
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(rdfFile));
 			for(int i=avg/2; i<(int)(size/dr)-avg/2; i++){
 				float rdf = 0;
 				for(int j=i-avg/2+1; j<i+avg/2; j++){
 					rdf += (float)(bin[j-1] *20* V/ (2 * Math.PI * length1 * length1 * ((j-1/2)*dr)*((j-1/2)*dr) * dr ));
 				}
 					avgOut = rdf/ avg;
 				//	if(rdf!=0)
 					out.write(""+(i+1)*dr+" "+(rdf)/avg+"\n");
 					if(avgOut>maxAvg){
 						maxAvg = avgOut;
 					}
 					data[i-avg/2] = avgOut;
 			}
 			out.close();
 			showGraph(data, maxAvg, size, dr);
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			main.jtext.setText("IO Error");
 		}
 	}
 	@SuppressWarnings({ "static-access" })
 	public void calculateRdfSame(float cutOff, int avg){
 		if(avg == 0)
 			avg = 2;
 		final float vec1[][] = new float[moleNumber[rdfMole1]][3];
 		final float vec2[][] = new float[moleNumber[rdfMole2]][3];
 		//System.out.println(vec1.length);
 		//System.out.println(vec2.length);
 		Vector3f temp = null;
 		final int length1 = moleNumber[rdfMole1];
 		final int length2 = moleNumber[rdfMole2];
 		final float dr = 0.0001f;
 		final int bin[] = new int[(int)(cutOff/dr)];
 		
 		
 		int summer1 = 0;
 		for(int i=0; i<rdfMole1; i++)
 			summer1 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[rdfMole1]; i++){
 			temp = selectedMol.vec.get(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1);
 			vec1[i][0] = temp.x;
 			vec1[i][1] = temp.y;
 			vec1[i][2] = temp.z;
 			//System.out.println(""+(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1));
 		}
 		
 		int summer2 = 0;
 		for(int i=0; i<rdfMole2; i++)
 			summer2 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[rdfMole2]; i++){
 			temp = selectedMol.vec.get(summer2 + i*molecules[rdfMole2].atomNo + rdfAtom2);
 			vec2[i][0] = temp.x;
 			vec2[i][1] = temp.y;
 			vec2[i][2] = temp.z;
 			//System.out.println(""+(summer2 + i*molecules[rdfMole2].atomNo + rdfAtom2));
 		}
 		//System.out.println(""+vec1.length +" "+vec2.length);
 		final float size = (float) ( cutOff);
 		final float sizex = (float) (sizeX);
 		final float sizey = (float) (sizeX);
 		final float sizez = (float) (sizeX);
 		for(int i=0; i<length1; i++){
 			for(int j=i+1; j<length2; j++){
 				float dx = vec1[i][0]-vec2[j][0];
 				float dy = vec1[i][1]-vec2[j][1];
 				float dz = vec1[i][2]-vec2[j][2];
 				if(dx/0.1f >= sizex * 0.5f)
 					dx -= sizex*0.1f;
 				else if(dx/0.1f < -sizex *0.5f)
 					dx += sizex*0.1f;
 				if(dy/0.1f >= sizey * 0.5f)
 					dy -= sizey*0.1f;
 				else if(dy/0.1f < -sizey *0.5f)
 					dy += sizey*0.1f;
 				if(dz/0.1f >= sizez * 0.5f)
 					dz -= sizez*0.1f;
 				else if(dz/0.1f < -sizez *0.5f)
 					dz += sizez*0.1f;
 				float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
 				if((dist/0.1f)<size)
 					bin[(int)(dist/(0.1f*dr))]++;
 			}
 		}
 		/*Kernel rdfKernel = new Kernel(){
 			public void run() {
 				int gid = getGlobalId();
 				if(gid%3 == 0){
 					for(int i=gid+1; i<length2; i++){
 						float dx = (vec1[gid*3]-vec2[i*3]);
 						float dy = (vec1[gid*3+1]-vec2[i*3+1]);
 						float dz = (vec1[gid*3+2]-vec2[i*3+2]);
 						if(dx/0.1f >= sizex * 0.5f)
 							dx -= sizex*0.1f;
 						else if(dx/0.1f < -sizex *0.5f)
 							dx += sizex*0.1f;
 						if(dy/0.1f >= sizey * 0.5f)
 							dy -= sizey*0.1f;
 						else if(dy/0.1f < -sizey *0.5f)
 							dy += sizey*0.1f;
 						if(dz/0.1f >= sizez * 0.5f)
 							dz -= sizez*0.1f;
 						else if(dz/0.1f < -sizez *0.5f)
 							dz += sizez*0.1f;
 						float dist =sqrt(dx * dx + dy * dy + dz * dz);
 						if((dist/0.1f)<size)
 							bin[(int)(dist/(0.1f*dr))]++;
 					}
 				}
 			}			
 		};
 		
 		rdfKernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
 		rdfKernel.execute(Range.create(length1));
 		System.out.println(rdfKernel.getExecutionMode()+" Time = "+rdfKernel.getExecutionTime());
 		*/
 		/*float dist = 0;
 		for(int i=0; i<length1; i++){
 			for(int j=0; j<length2; j++){
 				dist = (float) Math.sqrt(vec1[i*3]*vec2[j*3]+vec1[i*3+1]*vec2[i*3+1]+vec1[i*3+2]*vec2[i*3+2]);
 				System.out.println(""+dist);
 				if(dist/0.1<cutOff)
 					bin[(int)(dist/0.1)]++;
 			}
 		}*/
 		double V = 4 * Math.PI * Math.pow(size, 3)/ 3;
 		File rdfFile = new File(main.workSpace+"/"+caseFile.getName()+"rdf.dat");
 		final float data[] = new float[(int) (size/dr)-avg];
 		float maxAvg = 0;
 		float avgOut=0;
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(rdfFile));
 			for(int i=avg/2; i<(int)(size/dr)-avg/2; i++){
 				float rdf = 0;
 				for(int j=i-avg/2+1; j<i+avg/2; j++){
 					rdf += (float)(bin[j-1] *20* V/ (2 * Math.PI * length1 * length1 * ((j-1/2)*dr)*((j-1/2)*dr) * dr ));
 				}
 					avgOut = rdf/ avg;
 				//	if(rdf!=0)
 					out.write(""+(i+1)*dr+" "+(rdf)/avg+"\n");
 					if(avgOut>maxAvg){
 						maxAvg = avgOut;
 					}
 					data[i-avg/2] = avgOut;
 			}
 			out.close();
 			showGraph(data, maxAvg, size, dr);
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			main.jtext.setText("IO Error");
 		}
 	}
 	
 
 	@SuppressWarnings("static-access")
 	public void showGraph(float[] data, float maxAvg, float size, float dr) {
 		GraphicsConfiguration conf = SimpleUniverse.getPreferredConfiguration();
 		Canvas3D canvasGraph =  new Canvas3D(conf);
 		
 		SimpleUniverse uniGraph =  new SimpleUniverse(canvasGraph);
 		
 		BranchGroup graphBranchGroup = new BranchGroup();
 		addLights( graphBranchGroup );
 		TransformGroup graphTransGroup = new TransformGroup();
 		graphTransGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
 		graphTransGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
 		graphTransGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		graphTransGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		graphTransGroup.addChild(graphTransGroup);
 		
 		
 		orbit = new OrbitBehavior(canvasGraph,orbit.DISABLE_ROTATE+orbit.REVERSE_TRANSLATE);
 	    orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
 	    uniGraph.getViewingPlatform().setViewPlatformBehavior(orbit);
 	    
 	    float PAD = 0.1f;
 	    float w = 6;
 	    float h = 4;
         // Draw ordinate.
         LineArray line1 = new LineArray(2, LineArray.COORDINATES);
 		line1.setCoordinate(0, new Point3d(PAD, PAD, 0));
 		line1.setCoordinate(1, new Point3d(PAD, -h+PAD, 0));
 		graphBranchGroup.addChild(new Shape3D(line1));
 		
         // Draw abcissa.
 		 LineArray line2 = new LineArray(2, LineArray.COORDINATES);
 			line2.setCoordinate(0, new Point3d(PAD, -h+PAD, 0));
 			line2.setCoordinate(1, new Point3d(w-PAD, -h+PAD, 0));
 		graphBranchGroup.addChild(new Shape3D(line2));
 			
         double xInc = (double)(w - 2*PAD)/(data.length);
         double scale = (double)(h - 2*PAD)/maxAvg;
         //System.out.println(""+xInc+" "+scale);
         LineArray mainLine = new LineArray(data.length, LineArray.COORDINATES);
         LineArray otherLine = new LineArray(data.length, LineArray.COORDINATES);
        // mainLine.setCoordinate(0, new Point3d(PAD, -(h - PAD - scale*data[0]), 0));           
         for(int i = 1; i < data.length-1; i++) {
             double x = PAD + i*xInc;
             double y = h - PAD - scale*data[i];
             double x0 = PAD + (i-1)*xInc;
             double y0 = h - PAD - scale*data[i-1];
             mainLine.setCoordinate(i-1, new Point3d(x, -y, 0));            
             otherLine.setCoordinate(i-1, new Point3d(x0, -y0, 0));    
         }
         graphBranchGroup.addChild(new Shape3D(mainLine));
         graphBranchGroup.addChild(new Shape3D(otherLine));
 	    //oldCameraTransform = new Transform3D();
 	    //newCameraTransform = new Transform3D();
 	   
 		uniGraph.addBranchGraph(graphBranchGroup);
 	    view = uniGraph.getViewingPlatform();
 		view.setNominalViewingTransform();
 		 TransformGroup VpTG = uniGraph.getViewingPlatform().getViewPlatformTransform();
 
 		    float Zcamera = 10; //put the camera 12 meters back
 		    Transform3D Trfcamera = new Transform3D();
 		    Trfcamera.setTranslation(new Vector3f(w/2,-h/2, Zcamera));  
 		    VpTG.setTransform( Trfcamera );
 		// uniMole.getViewingPlatform().getViewPlatformTransform().getTransform(oldCameraTransform);
 		JFrame f = new JFrame("Graph");
         f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         JPanel p = new JPanel(new BorderLayout());
         p.add(canvasGraph, BorderLayout.CENTER);   
         f.add(p);
         f.setSize(600,400);
         f.setLocation(200,200);
         f.setVisible(true);
 		//main.jtext.setText("\nMolecule Designer Created");
 	}
 
 	public BranchGroup[] getMolecules(Canvas3D rdfChooser[], boolean type) {
 		BranchGroup moleBranch[] = new BranchGroup[molecules.length];
 		chooseMol = new Molecule[molecules.length];
 		if(!type)
 			oldL = new float[molecules.length];
 		
 		
 		for(int i=0; i<molecules.length; i++){
 			chooseMol[i] = new Molecule();
 			moleBranch[i] = new BranchGroup();
 			Vector3f position = new Vector3f(100, 100, 100);
 			Transform3D t3d = new Transform3D();
 			t3d.set(position);
 			if(type){			
 				selectingSphere[i] = createBehaviors();
 				selectingSphere[i].addChild(createWiredSphere());
 				selectingSphere[i].setTransform(t3d);
 				moleBranch[i].addChild(selectingSphere[i]);
 			}
 			else{
 				selectingCone[i]=createBehaviors();
 				selectingCone[i].addChild(createWiredCone());
 				oldL[i] =1;
 				selectingCone[i].setTransform(t3d);
 				moleBranch[i].addChild(selectingCone[i]);
 			}
 			
 			
 			
 			for(int j=0; j<molecules[i].atomNo; j++){				
 				BranchGroup atomBranch = new BranchGroup();
 				TransformGroup moleTransGroup = createBehaviors();
 				moleTransGroup.addChild(  createSphere(molecules[i].atoms.get(j)));
 				atomBranch.addChild(moleTransGroup);
 				Vector3f temp = new Vector3f(molecules[i].pos.get(j).x*0.1f,molecules[i].pos.get(j).y*0.1f,-molecules[i].pos.get(j).z*0.1f);
 				Transform3D tempT = new Transform3D();
 				tempT.setTranslation(temp);
 				moleTransGroup.setTransform(tempT);		
 				PickCanvas pickCanvas = new PickCanvas(rdfChooser[i], atomBranch);
 			    pickCanvas.setMode(PickCanvas.BOUNDS);
 				chooseMol[i].addAtom( moleTransGroup,pickCanvas,temp,molecules[i].atoms.get(j));
 				moleBranch[i].addChild(atomBranch);
 			}
 			for(int j=0; j< molecules[i].bondNo; j++){
 				BranchGroup bondBranch = new BranchGroup();
 				Vector3f pos = chooseMol[i].vec.get(molecules[i].bonds.get(j).index[0]);
 				Vector3f pos1 = chooseMol[i].vec.get(molecules[i].bonds.get(j).index[1]);
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
 				moleBranch[i].addChild(bondBranch);
 				Transform3D tempT = new Transform3D();
 				tempT.set(tempAA);
 				tempT.setTranslation(temp);
 				moleTransGroup.setTransform(tempT);
 				chooseMol[i].addBond(moleTransGroup,molecules[i].bonds.get(j).index[0],molecules[i].bonds.get(j).index[1],l,1);
 				moleTransGroup=null;
 			}
 		}
 		return moleBranch;
 	}
 
 	@SuppressWarnings("static-access")
 	private Cone createWiredCone() {
 		Appearance app = new Appearance();
 		Color n = new Color();
 		n.newColor(1);
 		Color3f objColor = new Color3f(n.r, n.g, n.b);
 		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
 		app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
 		javax.media.j3d.PolygonAttributes pa = new javax.media.j3d.PolygonAttributes();
 		pa.setPolygonMode(pa.POLYGON_LINE);
 		app.setPolygonAttributes(pa);
 		Cone cone = new Cone( 0.05f, 1, Primitive.GENERATE_NORMALS, app);
 		return cone;
 		
 	}
 	@SuppressWarnings("static-access")
 	public void calculateAngle(float id, float od) {
 		float angles[] = new float[180];
 		Vector3f vec1[] = new Vector3f[moleNumber[angleMole1]];
 		Vector3f vec2[] = new Vector3f[moleNumber[angleMole2]];
 		Vector3f vec1c[] = new Vector3f[moleNumber[angleMole1]];
 		Vector3f vec2c[] = new Vector3f[moleNumber[angleMole2]];
 		
 		Vector3f temp = null;
 		Vector3f temp1 = null;
 		int summer1 = 0;
 		for(int i=0; i<angleMole1; i++)
 			summer1 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[angleMole1]; i++){
 			temp = selectedMol.vec.get(summer1 + i*molecules[angleMole1].atomNo + angleAtom11);
 			temp1 = selectedMol.vec.get(summer1 + i*molecules[angleMole1].atomNo + angleAtom12);
 			vec1[i] = new Vector3f(temp.x-temp1.x, temp.y-temp1.y, temp.z-temp1.z);
 			vec1c[i] = new Vector3f((temp.x+temp1.x)/2, (temp.y+temp1.y)/2, (temp.z+temp1.z)/2);
 			//System.out.println(""+(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1));
 		}
 		
 		int summer2 = 0;
 		for(int i=0; i<angleMole2; i++)
 			summer2 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[angleMole2]; i++){
 			temp = selectedMol.vec.get(summer2 + i*molecules[angleMole2].atomNo + angleAtom21);
 			temp1 = selectedMol.vec.get(summer2 + i*molecules[angleMole2].atomNo + angleAtom22);
 			vec2[i] = new Vector3f(temp.x-temp1.x, temp.y-temp1.y, temp.z-temp1.z);
 			vec2c[i] = new Vector3f((temp.x+temp1.x)/2, (temp.y+temp1.y)/2, (temp.z+temp1.z)/2);
 			//System.out.println(""+(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1));
 		}
 		int maxAng = 0;
 		float dist = 0;
 		for(int i=0; i<moleNumber[angleMole1]; i++){
 			for(int j=0; j<moleNumber[angleMole2]; j++){
 				float angle = (float) (vec1[i].angle(vec2[j])/Math.PI * 180);
 				float dx = vec1c[i].x-vec2[j].x;
 				float dy = vec1[i].y -vec2[j].y;
 				float dz = vec1[i].z -vec2[j].z;
 				if(dx/0.1f >= sizeX * 0.5f)
 					dx -= sizeX*0.1f;
 				else if(dx/0.1f < -sizeX *0.5f)
 					dx += sizeX*0.1f;
 				if(dy/0.1f >= sizeY * 0.5f)
 					dy -= sizeY*0.1f;
 				else if(dy/0.1f < -sizeY *0.5f)
 					dy += sizeY*0.1f;
 				if(dz/0.1f >= sizeZ * 0.5f)
 					dz -= sizeZ*0.1f;
 				else if(dz/0.1f < -sizeZ *0.5f)
 					dz += sizeZ*0.1f;
 				dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
 				if((dist/0.1f)<od && (dist/0.1f)>id){				
 					angles[(int)angle]++;
 					if(maxAng<angles[(int)angle])
 						maxAng = (int) angles[(int)angle];
 				}
 			}
 		}
 		showGraph(angles, maxAng, 180, 1);
 		
 		
 		BufferedWriter out;
 		try {
 			out = new BufferedWriter(new FileWriter(main.workSpace+"/"+caseFile.getName()+"angle.dat"));
 		
 			for(int i=0; i<angles.length; i++){			
 				out.write(""+(i+1)+" "+angles[i]+"\n");
 			}
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	@SuppressWarnings("static-access")
 	public void calculateSameAngle(float id, float od) {
 		float angles[] = new float[180];
 		Vector3f vec1[] = new Vector3f[moleNumber[angleMole1]];
 		Vector3f vec2[] = new Vector3f[moleNumber[angleMole2]];
 		Vector3f vec1c[] = new Vector3f[moleNumber[angleMole1]];
 		Vector3f vec2c[] = new Vector3f[moleNumber[angleMole2]];
 		
 		Vector3f temp = null;
 		Vector3f temp1 = null;
 		int summer1 = 0;
 		for(int i=0; i<angleMole1; i++)
 			summer1 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[angleMole1]; i++){
 			temp = selectedMol.vec.get(summer1 + i*molecules[angleMole1].atomNo + angleAtom11);
 			temp1 = selectedMol.vec.get(summer1 + i*molecules[angleMole1].atomNo + angleAtom12);
 			vec1[i] = new Vector3f(temp.x-temp1.x, temp.y-temp1.y, temp.z-temp1.z);
 			vec1c[i] = new Vector3f((temp.x+temp1.x)/2, (temp.y+temp1.y)/2, (temp.z+temp1.z)/2);
 			//System.out.println(""+(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1));
 		}
 		
 		int summer2 = 0;
 		for(int i=0; i<angleMole2; i++)
 			summer2 += moleNumber[i]*molecules[i].atomNo;
 		for(int i=0; i<moleNumber[angleMole2]; i++){
 			temp = selectedMol.vec.get(summer2 + i*molecules[angleMole2].atomNo + angleAtom21);
 			temp1 = selectedMol.vec.get(summer2 + i*molecules[angleMole2].atomNo + angleAtom22);
 			vec2[i] = new Vector3f(temp.x-temp1.x, temp.y-temp1.y, temp.z-temp1.z);
 			vec2c[i] = new Vector3f((temp.x+temp1.x)/2, (temp.y+temp1.y)/2, (temp.z+temp1.z)/2);
 			//System.out.println(""+(summer1 + i*molecules[rdfMole1].atomNo + rdfAtom1));
 		}
 		int maxAng = 0;
 		float dist = 0;
 		for(int i=0; i<moleNumber[angleMole1]; i++){
 			for(int j=i+1; j<moleNumber[angleMole2]; j++){
 				float angle = (float) (vec1[i].angle(vec2[j])/Math.PI * 180);
 				float dx = vec1c[i].x-vec2[j].x;
 				float dy = vec1[i].y -vec2[j].y;
 				float dz = vec1[i].z -vec2[j].z;
 				if(dx/0.1f >= sizeX * 0.5f)
 					dx -= sizeX*0.1f;
 				else if(dx/0.1f < -sizeX *0.5f)
 					dx += sizeX*0.1f;
 				if(dy/0.1f >= sizeY * 0.5f)
 					dy -= sizeY*0.1f;
 				else if(dy/0.1f < -sizeY *0.5f)
 					dy += sizeY*0.1f;
 				if(dz/0.1f >= sizeZ * 0.5f)
 					dz -= sizeZ*0.1f;
 				else if(dz/0.1f < -sizeZ *0.5f)
 					dz += sizeZ*0.1f;
 				dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
 				if((dist/0.1f)<od && (dist/0.1f)>id){				
 					angles[(int)angle]++;
 					if(maxAng<angles[(int)angle])
 						maxAng = (int) angles[(int)angle];
 				}
 			}
 		}
 		showGraph(angles, maxAng, 180, 1);
 		
 		
 		BufferedWriter out;
 		try {
 			out = new BufferedWriter(new FileWriter(main.workSpace+"/"+caseFile.getName()+"angle.dat"));
 		
 			for(int i=0; i<angles.length; i++){			
 				out.write(""+(i+1)+" "+angles[i]+"\n");
 			}
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public class Structure {
 		public int atomNo = 0;
 		public int bondNo = 0;
 		public Vector<Vector3f> pos;
 		public int copies = 0;
 		public Vector<Integer> atoms;
 		public Vector<Float> mass;
 		public Vector<Float> charge;
 		public Vector<Float> sigma;
 		public Vector<Float> epsilon;
 		public Vector<Bond> bonds;
 		public void initiate(){
 			atoms = new Vector<Integer>();
 			mass = new Vector<Float>();
 			charge = new Vector<Float>();
 			sigma = new Vector<Float>();
 			epsilon = new Vector<Float>();
 			bonds = new Vector<Bond>();
 			pos = new Vector<Vector3f>();
 		}
 	}
 	@SuppressWarnings({ "static-access" })
 	private double askForEnergy(int mol, int enType) {
 		double energy = 0;
 		File arg = null;
 		switch(mol){
 			case 0:
 				arg = new File("Arguments.txt");
 				try {
 					arg.createNewFile();
 					if(arg.exists()){
 						BufferedWriter out = new BufferedWriter(new FileWriter(arg));
 						out.write(""+molecules.length);
 						out.write("\n"+temp);
 							out.write("\n"+sizeX);
 							out.write("\n"+sizeY);
 							out.write("\n"+sizeZ);
 							if(behavior == 0){
 								out.write("\n"+0);
 								out.write("\n"+cutOff);
 							}
 							else if(behavior == 1){
 								out.write("\n"+1);
 								out.write("\n"+cutOff);
 							}
 						for(int i=0; i<molecules.length;i++){
 							out.write("\n"+moleNumber[i]);
 							out.write("\n"+molecules[i].atomNo);
 							for(int j=0; j<molecules[i].atomNo; j++){
 								out.write("\n"+molecules[i].atoms.get(j)+" "+molecules[i].mass.get(j)+" "+molecules[i].charge.get(j)+" "+molecules[i].sigma.get(j)+" "+molecules[i].epsilon.get(j)+" "+molecules[i].pos.get(j).x+" "+molecules[i].pos.get(j).y+" "+molecules[i].pos.get(j).z);
 							}
 							if(enType == 0){
 								out.write("\n"+ molecules[i].bondNo);
 								for(int j=0; j<molecules[i].bondNo; j++){
 									Bond b = molecules[i].bonds.get(j);
 									//Atom a1 = moleculeVec.get(i).atom.get(b.index[0]);
 									//Atom a2 = moleculeVec.get(i).atom.get(b.index[1]);
 									out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 								}
 								out.write("\n"+allAngles.get(i).size());
 								for(int j=0;j<allAngles.get(i).size();j++){
 									out.write("\n"+allAngles.get(i).get(j).cAtom+" "+allAngles.get(i).get(j).oAtoms[0]+" "+allAngles.get(i).get(j).oAtoms[1]+" "+allAngles.get(i).get(j).angle+" "+allAngles.get(i).get(j).strength);
 								}
 								out.write("\n"+allTorsions.get(i).size());
 								for(int j=0;j<allTorsions.get(i).size();j++){
 									out.write("\n"+allTorsions.get(i).get(j).atom[0]+" "+allTorsions.get(i).get(j).atom[1]+" "+allTorsions.get(i).get(j).atom[2]+" "+allTorsions.get(i).get(j).atom[3]+" "+allTorsions.get(i).get(j).forceConst+" "+allTorsions.get(i).get(j).angle+" "+allTorsions.get(i).get(j).periodicity);
 								}
 							}
 							else{
 								out.write("\n"+ molecules[i].bondNo);
 								for(int j=0; j<molecules[i].bondNo; j++){
 									Bond b = molecules[i].bonds.get(j);
 									//Atom a1 = moleculeVec.get(i).atom.get(b.index[0]);
 									//Atom a2 = moleculeVec.get(i).atom.get(b.index[1]);
 									out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+0.0);
 								}
 								out.write("\n"+allAngles.get(i).size());
 								for(int j=0;j<allAngles.get(i).size();j++){
 									out.write("\n"+allAngles.get(i).get(j).cAtom+" "+allAngles.get(i).get(j).oAtoms[0]+" "+allAngles.get(i).get(j).oAtoms[1]+" "+allAngles.get(i).get(j).angle+" "+0.0);
 								}
 								out.write("\n"+0);
 							}
 						}
 						out.close();
 						out = new BufferedWriter(new FileWriter(new File("coordinates.txt")));
 						out.write(""+selectedMol.vec.size());
 						for(int i=0; i<selectedMol.vec.size(); i++){
 							out.write("\n"+(i+1)+"\t"+selectedMol.vec.get(i).x+"\t"+selectedMol.vec.get(i).y+"\t"+selectedMol.vec.get(i).z);
 						}
 						if(enType == 0)
 						for(int i=0; i<selectedMol.vec.size(); i++){
 							out.write("\n"+(i+1)+"\t"+velocities[i][0]+"\t"+velocities[i][1]+"\t"+velocities[i][2]);
 						}
 						else
 							for(int i=0; i<selectedMol.vec.size(); i++){
 								out.write("\n"+(i+1)+"\t"+0+"\t"+0+"\t"+0);
 							}
 						out.close();
 					}					
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 			default:
 				arg = new File("Arguments.txt");
 				try {
 					arg.createNewFile();
 					if(arg.exists()){
 						BufferedWriter out = new BufferedWriter(new FileWriter(arg));
 						out.write(""+1);
 						out.write("\n"+temp);
 							out.write("\n"+sizeX);
 							out.write("\n"+sizeY);
 							out.write("\n"+sizeZ);
 							if(behavior == 0){
 								out.write("\n"+0);
 								out.write("\n"+cutOff);
 							}
 							else if(behavior == 1){
 								out.write("\n"+1);
 								out.write("\n"+cutOff);
 							}
 							out.write("\n"+moleNumber[mol-1]);
 							
 							out.write("\n"+molecules[mol-1].atomNo);
 							for(int j=0; j<molecules[mol-1].atomNo; j++){
 								out.write("\n"+molecules[mol-1].atoms.get(j)+" "+molecules[mol-1].mass.get(j)+" "+0+" "+molecules[mol-1].sigma.get(j)+" "+0+" "+molecules[mol-1].pos.get(j).x+" "+molecules[mol-1].pos.get(j).y+" "+molecules[mol-1].pos.get(j).z);
 							}
 							if(enType == 0){
 								out.write("\n"+ molecules[mol-1].bondNo);
 								for(int j=0; j<molecules[mol-1].bondNo; j++){
 									Bond b = molecules[mol-1].bonds.get(j);
 									//Atom a1 = moleculeVec.get(i).atom.get(b.index[0]);
 									//Atom a2 = moleculeVec.get(i).atom.get(b.index[1]);
 									out.write("\n"+b.index[0]+" "+b.index[1]+" "+b.length+" "+b.strength);
 								}
 							}
 							else{
 								out.write("\n"+0);
 							}
 							if(enType == 1){
 								out.write("\n"+allAngles.get(mol-1).size());
 								for(int j=0;j<allAngles.get(mol-1).size();j++){
 									out.write("\n"+allAngles.get(mol-1).get(j).cAtom+" "+allAngles.get(mol-1).get(j).oAtoms[0]+" "+allAngles.get(mol-1).get(j).oAtoms[1]+" "+allAngles.get(mol-1).get(j).angle+" "+allAngles.get(mol-1).get(j).strength);
 								}
 							}
 							else{
 								out.write("\n"+0);
 							}
 							if(enType == 2){
 								out.write("\n"+allTorsions.get(mol-1).size());
 								for(int j=0;j<allTorsions.get(mol-1).size();j++){
 									double force = allTorsions.get(mol-1).get(j).forceConst;
 									if(allTorsions.get(mol-1).get(j).forceConst == 40.0)
 										force = 0;
 									out.write("\n"+allTorsions.get(mol-1).get(j).atom[0]+" "+allTorsions.get(mol-1).get(j).atom[1]+" "+allTorsions.get(mol-1).get(j).atom[2]+" "+allTorsions.get(mol-1).get(j).atom[3]+" "+force+" "+allTorsions.get(mol-1).get(j).angle+" "+allTorsions.get(mol-1).get(j).periodicity);
 								}
 							}
 							else if(enType == 3){
 								out.write("\n"+allTorsions.get(mol-1).size());
 								for(int j=0;j<allTorsions.get(mol-1).size();j++){
 									double force = allTorsions.get(mol-1).get(j).forceConst;
 									if(allTorsions.get(mol-1).get(j).forceConst != 40.0)
 										force = 0;
 									out.write("\n"+allTorsions.get(mol-1).get(j).atom[0]+" "+allTorsions.get(mol-1).get(j).atom[1]+" "+allTorsions.get(mol-1).get(j).atom[2]+" "+allTorsions.get(mol-1).get(j).atom[3]+" "+force+" "+allTorsions.get(mol-1).get(j).angle+" "+allTorsions.get(mol-1).get(j).periodicity);
 								}
 							}
 							else{
 								out.write("\n"+0);
 							}		
 							int summer = 0;
 							for(int i=0; i<mol-1; i++){
 										summer+= moleNumber[i]*molecules[i].atomNo;
 									
 							}
 							out.close();
 						out = new BufferedWriter(new FileWriter(new File("coordinates.txt")));
 						out.write(""+(moleNumber[mol-1]*molecules[mol-1].atomNo));
 						int number = summer + moleNumber[mol-1]*molecules[mol-1].atomNo;
 						for(int i=summer; i<number; i++){
 							out.write("\n"+(i-summer+1)+"\t"+selectedMol.vec.get(i).x+"\t"+selectedMol.vec.get(i).y+"\t"+selectedMol.vec.get(i).z);
 						}
 						for(int i=summer; i<number; i++){
 							out.write("\n"+(i-summer+1)+"\t"+0+"\t"+0+"\t"+0);
 						}
 						out.close();
 					}					
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;			
 		}
 		ProcessBuilder process = new ProcessBuilder("./energy");
 		process.redirectErrorStream(true);
 		Map<String, String> env = process.environment();
 		Process p = null;
 		env.put("LD_LIBRARY_PATH", "openmm/lib:"+main.ld_library_path);
 		env.put("OPENMM_PLUGIN_DIR", "openmm/lib/plugins");
 //		env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/sanatpc/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 //		env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 //		env.put("OPENMM_PLUGIN_DIR", "/home/sanatpc/Library/OpenMM3.0-Linux64/lib/plugins");
 //		env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/omkar/Library/OpenMM3.0-Linux64/lib:/usr/local/cuda/lib");
 //		env.put("PATH", "/usr/local/cuda/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
 //		env.put("OPENMM_PLUGIN_DIR", "/home/omkar/Library/OpenMM3.0-Linux64/lib/plugins");
 //		env.put("LD_LIBRARY_PATH", "/usr/local/cuda/lib64:/home/sim1/Work/Softwares/OpenMM/lib:/usr/lib64:/lib:/usr/lib:/usr/local/lib");
 //		env.put("PATH", "/home/sim1/Work/Simulations/NewWork/jre1.6.0_30/bin:/usr/local/cuda/bin:$PATH");
 //		env.put("OPENMM_PLUGIN_DIR", "/home/sim1/Work/Softwares/OpenMM/lib/plugins");
 		try {
 			p = ((ProcessBuilder) process).start();
 			InputStream is1 = p.getInputStream();
 			InputStreamReader isr1 = new InputStreamReader(is1);
 			BufferedReader br1 = new BufferedReader(isr1);
 			String line1;
 				while ((line1 = br1.readLine()) != null) {
 					try{
 						energy = Double.parseDouble(line1);
 					}
 					catch(NumberFormatException e){
 						
 					}
 				}
 			p.waitFor();
 		} catch (IOException e) {
 			main.jtext.setText("I/O Error");
 		} catch (InterruptedException e) {
 			main.jtext.setText("Interrupted");
 		}	
 		return energy;
 	}	
 
 	@SuppressWarnings("static-access")
 	public void go() {
 		main.working = true;
 	}
 
 	public void askExit() {
 		destroy();
 	}
 }
