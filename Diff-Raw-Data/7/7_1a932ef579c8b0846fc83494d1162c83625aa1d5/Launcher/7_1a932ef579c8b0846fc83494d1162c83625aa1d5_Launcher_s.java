 package warborn.main;
 
 
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JFrame;
 
 import warborn.SupportClasses.MapData;
 import warborn.controller.*;
 import warborn.map.GothenburgMapView;
 import warborn.map.IMap;
 import warborn.model.Warborn;
 import warborn.view.*;
 
 public class Launcher implements Observer{
 
 	private JFrame frame;
 	private Warborn model;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		
 		Launcher l = new Launcher();
 		l.run();
 	}
 	
 	public void run() {
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public Launcher() {
 		model = new Warborn();
 		init();
 		initialize();
 		//model.addObserver(this);
 		model.nextState();
 		model.nextState();
 		model.nextState();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new MainFrame(model);
 		GothenburgMapView map = new GothenburgMapView(model);
 		new MapController(model, map);
 		model.addObserver(map);
 		frame.add(map);
 	}
 	
 	/**
 	 * Initialize all the views and controllers
 	 */
 	private void init(){
 		MoveView move = new MoveView(/*model*/);
 		new MoveController(model, move);
		BattleView battle = new BattleView(model);
 		new BattleController(model, battle);
 		
 		model.addObserver(move);
 		model.addObserver(battle);
 	}
 
 	
 	public void createGame(int mapIndex){
 		IMap[] mapList = MapData.getMapList(model);
 		new MapController(model, mapList[mapIndex]);
 		model.addObserver(mapList[mapIndex]);
		frame.add(mapList[mapIndex]);
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(model.getState() == 1){
 			model.deleteObserver(this);
 			createGame(model.getMapIndex());
 		}
 	}
 
 }
