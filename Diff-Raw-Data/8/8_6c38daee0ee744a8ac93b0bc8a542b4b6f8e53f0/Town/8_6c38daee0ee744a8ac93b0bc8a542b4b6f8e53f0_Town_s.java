 package game.gameplayStates;
 
 import game.StaticObject;
 import game.interactables.Interactable;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public abstract class Town extends GamePlayState {
 
 	public Town() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void setupObjects(int city, int dream) throws SlickException {
 		// Generate gameobjects that will always appear in both TownNight and TownDay here
 		StaticObject homeBuilding = new StaticObject("home_building", 
 				10*SIZE, 29*SIZE, "assets/town/home_building.png");
 		this.addObject(homeBuilding, false);
 		m_blocked[10][29] = true;
 		m_blocked[11][29] = true;
 		m_blocked[12][29] = true;
 		
 		StaticObject building01 = new StaticObject("building01",
 			14*SIZE, 18*SIZE, "assets/town/building01.png");	
 		this.addObject(building01, false);
 		building01.setRenderPriority(true);
 		StaticObject buildingWalkThrough01A = new StaticObject("buildingWalkThrough01A",
 			14*SIZE, 18*SIZE, "assets/town/building_walkthrough01A.png");
 		this.addObject(buildingWalkThrough01A, false);
 		buildingWalkThrough01A.setRenderPriority(true);
 		StaticObject buildingWalkThrough01B = new StaticObject("buildingWalkThrough01B",
 				17*SIZE, 21*SIZE, "assets/town/building_walkthrough01B.png");
 		this.addObject(buildingWalkThrough01B, false);
 		buildingWalkThrough01B.setRenderPriority(true);
 		
 		StaticObject doormat = new StaticObject("doormat",11*SIZE, 28*SIZE, "assets/gameObjects/doormat.png");
 		this.addObject(doormat, false);
 		
 		//hospital stuff
 		StaticObject hospital = new StaticObject("hospital", 4*SIZE, 17*SIZE, "assets/town/hospital.png");
 		this.addObject(hospital, false);
 		hospital.setRenderPriority(true);
 		StaticObject hospitalTop = new StaticObject("hospitalTop", 4*SIZE+4, 17*SIZE, "assets/town/hospitalTop.png");
 		this.addObject(hospitalTop, false);
 		hospitalTop.setRenderPriority(true);
 		
 		StaticObject hospitalTower = new StaticObject("hospitalTower", 2*SIZE, 15*SIZE, 
 				"assets/town/hospitalTower.png");
 		this.addObject(hospitalTower, false);
 		hospitalTower.setRenderPriority(true);
 		StaticObject hospitalTowerTop = new StaticObject("hospitalTowerTop", 2*SIZE+4, 15*SIZE, 
 				"assets/town/hospitalTowerTop.png");
 		hospitalTowerTop.setRenderPriority(true);
 		this.addObject(hospitalTowerTop, false);
 		
 		
 		StaticObject tree1 = new StaticObject("tree1", 11*SIZE, 18*SIZE, "assets/gameObjects/tree_normal.png");
 		this.addObject(tree1, false);
 		tree1.setRenderPriority(true);
 		m_blocked[11][20] = true;
 		m_blocked[12][20] = true;
 		
 		StaticObject tree2 = new StaticObject("tree2", 19*SIZE, 5*SIZE, "assets/gameObjects/tree_normal.png");
 		this.addObject(tree2, false);
 		tree2.setRenderPriority(true);
 		m_blocked[19][7] = true;
 		m_blocked[20][7] = true;
 		
 		StaticObject sign1 = new StaticObject("sign1", 7*SIZE, 13*SIZE, "assets/gameObjects/sign.png");
 		sign1.setDialogue(new String[] {"\"Horse Stables (formerly the zoo)\""});
 		this.addObject(sign1, true);
 		
 		StaticObject sign2 = new StaticObject("sign2", 6*SIZE, 21*SIZE, "assets/gameObjects/sign.png");
 		sign2.setDialogue(new String[] {"\"Hospital\""});
 		this.addObject(sign2, true);
 		
 		StaticObject fireHydrant = new StaticObject("fireHydrant", 6*SIZE, 23*SIZE, "assets/firehydrant.png");
 		this.addObject(fireHydrant, true);
 		m_blocked[6][23] = true;
 		
 		if (city == 4) {
 			
 		}
 		else if (city == 3) {
 			
 		}
 		else if (city == 2) {
 			
 		}
 		else if (city == 1) {
 			
 		}
 		else if (city == 0) {
 			
 		}
 		
 		if (city < 4) {
 			StaticObject tree_1 = (StaticObject)this.getObject("tree1");
 			tree_1.setSprite(new Image("assets/gameObjects/treebroken.png"));
 			StaticObject tree_2 = (StaticObject)this.getObject("tree2");
 			tree_2.setSprite(new Image("assets/gameObjects/treebroken.png"));
 			tree_1.setRenderPriority(false);
 			tree_2.setRenderPriority(false);
 			
 			this.getObject("fireHydrant").setSprite(new Image("assets/gameObjects/firehydrantbroken.png"));
 			
 			m_blocked[19][7] = false;
 			m_blocked[20][7] = false;
 			m_blocked[11][20] = false;
 			m_blocked[12][20] = false;
 		}
 		
 		// townDay and townNight will call this
 		additionalSetupObjects(city, dream);
 	}
 	
 	public abstract void additionalSetupObjects(int city, int dream) throws SlickException;
 
 	@Override
 	public void setupDialogue(GameContainer container, int city, int dream)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	@Deprecated
 	public void dialogueListener(Interactable i) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
