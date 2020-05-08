 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
 import javax.swing.JScrollPane;
 
 public class Content {
 
 	Applet applet;
 	PGraph graph;
 	ElectronicCircuit circuit;
 	VTEquationsInput equationInput;
 	VTLabel questionLabel;
 	
 	public Content(Applet applet) {
 		this.applet = applet;		
 	}
 	
 	public void init() {
 		applet.setSize(600, 500);
 		applet.scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	}
 
 	void postinit() {}
	void next(int i) {}	
	boolean isCorrect(int i, String sel) { return false; }
 	
 	void initNewCircuit() {
 		graph.setSize(520, 200);
 		graph.clear();
 		
 		circuit.clear();
 		circuit.constructOnPGraph_Start(graph, 1, 1);
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.ECapacitor.class, 2, 1).initVarNames("1");
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.ECapacitor.class, 3, 1).initVarNames("2");
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.ECapacitor.class, 4, 1).initVarNames("3");
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.Conn.class, 4, 3);
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.Conn.class, 3, 3);
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.VoltageSource.class, 2, 3).initVarNames("");
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.Conn.class, 1, 3);
 		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.EResistance.class, 1, 1).initVarNames("");
 		circuit.constructOnPGraph_Final(graph);
 		
 		EquationSystem eqSys = new EquationSystem();
 		eqSys.addAuto("Q = C1 * U1");
 		eqSys.addAuto("Q = C2 * U2");
 		eqSys.addAuto("Q = C3 * U3");
 		eqSys.addAuto("Q = C * U");
 		eqSys.addAuto("U = U1 + U2 + U3");
 		equationInput.setEquationSystem(eqSys, "C", Utils.listFromArgs("C1", "C2", "C3"));
 		equationInput.clear();
 	}
 	
 	public void run() {
 		//Utils.debugUtilsParsingOpTree();
 		//EquationSystem.debug();
 		
 		graph = new PGraph(applet, 400, 400);
 		circuit = new ElectronicCircuit();
 		equationInput = new VTEquationsInput("equ", 0, 0, applet.getWidth() - 40);
 		questionLabel = new VTLabel("question", "", 10, 10, applet.getWidth() - 60);
 		initNewCircuit();
 				
 		applet.vtmeta.setExtern(new VisualThing[] {
 				new VTButton("new", "neue Aufgabe", 10, 10, new Runnable() {
 					public void run() {
 						initNewCircuit();
 						applet.revalidateVisualThings();
 						applet.repaint();						
 					}
 				}),
 				new VTImage("graph", 10, 20, applet.getWidth() - 60, 400, graph),
 				questionLabel,
 				equationInput,
 		});
 	}
 	
 }
