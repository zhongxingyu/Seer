 /**  
   *  Written by Morgan Allen.
   *  I intend to slap on some kind of open-source license here in a while, but
   *  for now, feel free to poke around for non-commercial purposes.
   */
 
 
 package src.debug ;
 import src.game.actors.* ;
 import src.game.base.* ;
 import src.game.building.* ;
 import src.game.common.* ;
 import src.game.planet.* ;
 import src.game.tactical.* ;
 import src.game.wild.* ;
 import src.graphics.common.* ;
 import src.graphics.widgets.* ;
 import src.user.* ;
 import src.util.* ;
 
 
 
 //
 //  You also need to try scenarios between multiple actors, some of them
 //  hostile, and see how they respond.  Ideally, you don't want actors
 //  willingly running into situations that they then run away from.
 //
 //  More attention on relaxing/recreation.  Add hunting at the redoubt, and
 //  update farming a bit.
 //
 //  Safety patrols and taxation.  Spontaneous missions, and a clearer factoring
 //  out of venues/actors batches in the AI.
 
 //
 /*
 Check to ensure that combat works okay among rival humanoid actors.  Make sure
 mining/farming's up to date.  Try to integrate with hunting.  That may require
 implementing the Ecology class, like you planned.
 
Security patrols.  Diplomatic conversion.  Tax collection and pressfeed.
 
 Simplify the user interface, implement Powers, and add a Main Menu.  That's it.
 
 Walls/Roads and Power/Life Support are the next items, but those might require
 a bigger game.  Maybe *just* power.  Keep it simple.  Condensors for water, and
 from the Vault System.  Share with whole settlement.
 //*/
 
 
 public class DebugBehaviour extends PlayLoop {
   
   
   
   /**  Startup and save/load methods-
     */
   public static void main(String args[]) {
     DebugBehaviour test = new DebugBehaviour() ;
     test.runLoop() ;
   }
   
   
   protected DebugBehaviour() {
     super(true) ;
   }
   
   
   public DebugBehaviour(Session s) throws Exception {
     super(s) ;
   }
   
   
   public void saveState(Session s) throws Exception {
     super.saveState(s) ;
   }
   
   
   
   /**  Setup and updates-
     */
   protected boolean shouldExitLoop() {
     if (KeyInput.wasKeyPressed('r')) {
       resetGame() ;
       return false ;
     }
     if (KeyInput.wasKeyPressed('f')) {
       GameSettings.frozen = ! GameSettings.frozen ;
     }
     if (KeyInput.wasKeyPressed('s')) {
       I.say("SAVING GAME...") ;
       PlayLoop.saveGame("saves/test_session.rep") ;
       return false ;
     }
     if (KeyInput.wasKeyPressed('l')) {
       I.say("LOADING GAME...") ;
       //GameSettings.frozen = true ;
       PlayLoop.loadGame("saves/test_session.rep") ;
       return true ;
     }
     return false ;
   }
   
   
   protected World createWorld() {
     final TerrainGen TG = new TerrainGen(
       32, 0.2f,
       Habitat.MEADOW , 0.7f,
       Habitat.BARRENS, 0.3f
     ) ;
     final World world = new World(TG.generateTerrain()) ;
     TG.setupMinerals(world, 0, 0, 0) ;
     //TG.setupOutcrops(world) ;
     return world ;
   }
   
   
   protected Base createBase(World world) {
     Base base = new Base(world) ;
     return base ;
   }
   
   
   protected HUD createUI(Base base, Rendering rendering) {
     BaseUI UI = new BaseUI(base.world, rendering) ;
     UI.assignBaseSetup(base, new Vec3D(8, 8, 0)) ;
     return UI ;
   }
   
   
   protected void configureScenario(World world, Base base, HUD HUD) {
     
     I.say(" "+(String.class.isAssignableFrom(Object.class))) ;
     //natureScenario(world, base, HUD) ;
     //baseScenario(world, base, HUD) ;
     //missionScenario(world, base, HUD) ;
     socialScenario(world, base, HUD) ;
   }
   
   
   
   /**  Testing out interactions between alien creatures or primitive humanoids.
     */
   private void natureScenario(World world, Base base, HUD UI) {
     GameSettings.noFog = true ;
     
     /*
     Actor prey = new Vareen() ;
     prey.health.setupHealth(0.5f, 1, 0) ;
     prey.enterWorldAt(12, 12, world) ;
     ((BaseUI) UI).selection.pushSelection(prey, true) ;
     //*/
     
     Actor hunter = new Micovore() ;
     hunter.health.setupHealth(0.5f, 1, 0) ;
     hunter.enterWorldAt(6, 6, world) ;
     ((BaseUI) UI).selection.pushSelection(hunter, true) ;
     //hunter.AI.assignBehaviour(new Hunting(hunter, prey, Hunting.TYPE_FEEDS)) ;
     
     ///PlayLoop.setGameSpeed(10.0f) ;
     /*
     for (int n = 16 ; n-- > 0 ;) {
       final Actor prey = Rand.yes() ? new Quud() : new Vareen() ;
       final Tile e = Spacing.pickRandomTile(world.tileAt(16, 16), 8, world) ;
       prey.health.setupHealth(Rand.num(), 1, 0) ;
       prey.enterWorldAt(e.x, e.y, world) ;
     }
     
     final Actor hunter = new Micovore() ;
     hunter.health.setupHealth(Rand.num(), 1, 0) ;
     hunter.enterWorldAt(4, 4, world) ;
     ((BaseUI) UI).selection.pushSelection(hunter, true) ;
     //*/
   }
   
   
   
   /**  These are scenarios associated with upkeep, maintenance and
     *  construction of the settlement-
     */
   private void baseScenario(World world, Base base, HUD UI) {
     GameSettings.hireFree = true ;
     
     final Artificer artificer = new Artificer(base) ;
     artificer.enterWorldAt(8, 8, world) ;
     artificer.structure.setState(VenueStructure.STATE_INTACT, 1.0f) ;
     artificer.onCompletion() ;
     artificer.setAsEstablished(true) ;
     base.intelMap.liftFogAround(artificer, 5) ;
     ((BaseUI) UI).selection.pushSelection(artificer, true) ;
     
     final Actor client = new Human(Vocation.VETERAN, base) ;
     client.gear.incCredits(500) ;
     client.enterWorldAt(4, 4, world) ;
     /*
     final Garrison garrison = new Garrison(base) ;
     garrison.enterWorldAt(2, 6, world) ;
     garrison.setAsEstablished(true) ;
     garrison.structure.setState(VenueStructure.STATE_INSTALL, 0.1f) ;
     //*/
   }
   
   
   
   /**  Testing out directed behaviour like combat, exploration, security or
     *  contact missions.
     */
   private void missionScenario(World world, Base base, HUD UI) {
     
     final Actor actorA = new Human(Vocation.RUNNER, base) ;
     actorA.enterWorldAt(15, 15, world) ;
     final Actor actorB = new Human(Vocation.VETERAN, base) ;
     actorB.enterWorldAt(15, 3, world) ;
     
     final Actor target = new Quud() ;
     target.health.setupHealth(0.5f, 1, 0) ;
     target.enterWorldAt(5, 5, world) ;
     
     final Base otherBase = new Base(world) ;
     world.registerBase(otherBase, true) ;
     base.setRelation(otherBase, -1) ;
     otherBase.setRelation(base, -1) ;
     
     final Venue garrison = new Garrison(otherBase) ;
     garrison.enterWorldAt(8, 8, world) ;
     garrison.structure.setState(VenueStructure.STATE_INTACT, 1) ;
     garrison.setAsEstablished(true) ;
     
     actorA.AI.assignBehaviour(new Combat(actorA, garrison)) ;
     actorB.AI.assignBehaviour(new Combat(actorA, garrison)) ;
     ((BaseUI) UI).selection.pushSelection(actorA, true) ;
     
     /*
     final Mission mission = new ReconMission(base, world.tileAt(20, 20)) ;
     base.addMission(mission) ;
     ((BaseUI) UI).selection.setSelected(mission) ;
     ((BaseUI) UI).camera.zoomNow(mission.subject()) ;
     //*/
     /*
     final Mission mission = new StrikeMission(base, garrison) ;
     mission.setApplicant(assails, true) ;
     base.addMission(mission) ;
     ((BaseUI) UI).selection.setSelected(mission) ;
     //*/
   }
   
   
   /**  Testing out pro-social behaviour like dialogue, recreation and medical
     *  treatment.
     */
   private void socialScenario(World world, Base base, HUD UI) {
     
     //
     //  TODO:  Add a behaviour to ensure that someone tends the desk at the
     //  hospice.  Casual priority, let's say.
     final Actor actor = new Human(Vocation.PHYSICIAN, base) ;
     actor.enterWorldAt(5, 5, world) ;
     
     final Sickbay hospice = new Sickbay(base) ;
     hospice.enterWorldAt(9, 2, world) ;
     hospice.setAsEstablished(true) ;
     hospice.structure.setState(VenueStructure.STATE_INTACT, 1.0f) ;
     hospice.onCompletion() ;
     actor.AI.setEmployer(hospice) ;
     
     final Actor other = new Human(Vocation.VETERAN , base) ;
     ///other.traits.setLevel(ActorConstants.ILLNESS, 2) ;
     other.enterWorldAt(9, 9, world) ;
     ///((BaseUI) UI).selection.pushSelection(other, true) ;
     
     final Garrison garrison = new Garrison(base) ;
     garrison.enterWorldAt(2, 9, world) ;
     garrison.setAsEstablished(true) ;
     garrison.structure.setState(VenueStructure.STATE_INTACT, 1.0f) ;
     garrison.onCompletion() ;
     other.AI.setEmployer(garrison) ;
     
     /*
     final Cantina cantina = new Cantina(base) ;
     cantina.enterWorldAt(9, 9, world) ;
     cantina.setAsEstablished(true) ;
     cantina.structure.setState(VenueStructure.STATE_INTACT, 1.0f) ;
     cantina.onCompletion() ;
     //*/
     
     //*
     final EcologyGen EG = new EcologyGen() ;
     EG.populateFlora(world) ;
     //  Vareen need to flee from the citizens!
     ///EG.populateFauna(world, Species.VAREEN) ;
     //*/
   }
 }
 
 
 
 
 //*/
 
 /*
 final Actor citizen = new Human(Vocation.MILITANT, base) ;
 citizen.enterWorldAt(5, 5, world) ;
 //final Plan explores = new Exploring(citizen, base, world.tileAt(12, 12)) ;
 //citizen.psyche.assignBehaviour(explores) ;
 ((BaseUI) UI).selection.setSelected(citizen) ;
 //*/
 
 
