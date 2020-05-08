 package edu.agh.tunev.model.cellular;
 
 import java.util.Vector;
 
 import edu.agh.tunev.model.AbstractModel;
 import edu.agh.tunev.model.PersonProfile;
 import edu.agh.tunev.model.PersonState.Movement;
 import edu.agh.tunev.model.cellular.agent.NotANeighbourException;
 import edu.agh.tunev.model.cellular.agent.Person;
 import edu.agh.tunev.model.cellular.agent.WrongOrientationException;
 import edu.agh.tunev.model.cellular.grid.Board;
 import edu.agh.tunev.model.cellular.grid.Cell;
 import edu.agh.tunev.statistics.LifeStatistics;
 import edu.agh.tunev.statistics.Statistics.AddCallback;
 import edu.agh.tunev.world.World;
 import edu.agh.tunev.world.World.ProgressCallback;
 
 public final class Model extends AbstractModel {
 
 	public final static String MODEL_NAME = "Social Distances Cellular Automata";
 	private final static double INTERSECTION_TOLERANCE = 0.2;
 
 	public Model(World world) {
 		super(world);
 	}
 
 	public static final double DT = 0.05;
 
 	private Board board;
 	private AllowedConfigs allowedConfigs;
 
 	@Override
 	public void simulate(double duration, Vector<PersonProfile> profiles,
 			ProgressCallback progressCallback, AddCallback addCallback) {
 		// pokaż info o inicjalizacji w ui, bo trwa zanim zacznie iterować i nie
 		// wiadomo ocb :b
 		int num = (int) Math.round(Math.ceil(world.getDuration() / DT));
 		progressCallback.update(0, num, "Initializing...");
 
 		// TODO: pododawaj jakieś wykresy do UI związane z tym modelem
 		//
 		// sidenote: zobacz helpa do interfejsu Statistics: gdy dany wykres
 		// pasuje do wielu modeli (np. liczba zabitych jako f(t)), to dodaj jego
 		// klasę do pakietu tunev.statistics; jeśli pasuje tylko do tego modelu,
 		// to dodaj do pakietu tego modelu
 		LifeStatistics lifeStatistics = new LifeStatistics();
 		addCallback.add(lifeStatistics);
 		// minor fix: przeniosłem wykresy przed tworzenie automatu, żeby już
 		// były dostępne do otwarcia na etapie inicjalizacji
 
 		// stwórz automat (planszę komórek)
 		board = new Board(world);
 		
 
 		// TODO: exception handling
 		try {
 			allowedConfigs = new AllowedConfigs(PersonProfile.WIDTH,
 					PersonProfile.GIRTH, Cell.CELL_SIZE, INTERSECTION_TOLERANCE);
 		} catch (NeighbourIndexException | WrongOrientationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// stwórz sobie swoje reprezentacje ludzi:
 		Vector<Person> people = new Vector<Person>();
 		for (PersonProfile profile : profiles)
 			try {
 				people.add(new Person(profile, board.getCellAt(Cell
 						.c2d(profile.initialPosition)), allowedConfigs));
 			} catch (WrongOrientationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		// TODO: pozaznaczaj przeszkody na planszy
 
 		// TODO: pozaznaczaj wyjścia na planszy
 
 		// kolejne iteracje automatu -- uwaga, żadnego czekania w stylu
 		// Thread.sleep() -- to ma się policzyć *jak najszybciej*! --
 		// wyświetlanie "filmu" z symulacji jest niezależne od obliczania (no,
 		// tyle tylko zależne, że możemy wyświetlać tylko do momentu, który już
 		// się policzył)
 		double t = 0;
 		for (int iteration = 1; iteration <= num; iteration++) {
 			// uaktualnij rzeczywisty czas naszej symulacji
 			t += DT;
 
 			board.update(t);
 
 			// porób zdjęcia osobom w aktualnym rzeczywistym czasie
 			for (Person p : people) {
 				try {
 					try {
 						p.update();
 					} catch (NotANeighbourException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} catch (NeighbourIndexException | WrongOrientationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				interpolator.saveState(p.profile, t, p.getCurrentState());
 				
 			}
 			
 			int alive = 0;
 			int dead = 0;
 			int rescued = 0;
 			for(Person p : people){
 				if(p.isAlive())
 					++alive;
 				else 
 					++dead;
 					
				if(p.getMovement() == Movement.HIDDEN)
 					++rescued;
 			}
 			
 			lifeStatistics.add(t, alive, rescued, dead);
 
 			// grzeczność: zwiększ ProgressBar w UI
 			progressCallback.update(iteration, num,
 					(iteration < num ? "Simulating..." : "Done."));
 		}
 
 		// TODO: ew. wypełnij wykresy, które mogą być wypełnione dopiero po
 		// zakończeniu całej symulacji
 
 		// i tyle ^_^
 	}
 
 }
