 package edu.agh.tunev.model.cellular;
 
 import java.util.Vector;
 
 import edu.agh.tunev.model.AbstractModel;
 import edu.agh.tunev.model.PersonProfile;
 import edu.agh.tunev.model.cellular.agent.Person;
 import edu.agh.tunev.model.cellular.agent.WrongOrientationException;
 import edu.agh.tunev.model.cellular.grid.Board;
 import edu.agh.tunev.model.cellular.grid.Cell;
 import edu.agh.tunev.statistics.KilledStatistics;
 import edu.agh.tunev.statistics.Statistics.AddCallback;
 import edu.agh.tunev.world.World;
 import edu.agh.tunev.world.World.ProgressCallback;
 
 public final class Model extends AbstractModel {
 
 	public final static String MODEL_NAME = "Social Distances Cellular Automata";
 	private final static double INTERSECTION_TOLERANCE = 0.1;
 
 	public Model(World world) {
 		super(world);
 	}
 
 	private static final double DT = 0.5;
 
 	private Board board;
 	private AllowedConfigs allowedConfigs;
 
 	@Override
 	public void simulate(double duration, Vector<PersonProfile> profiles,
 			ProgressCallback progressCallback, AddCallback addCallback) {
 
 		// stwórz automat (planszę komórek)
 		board = new Board(world);
 		
 		//TODO: exception handling
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
 		
 
 		// TODO: pododawaj jakieś wykresy do UI związane z tym modelem
 		//
 		// sidenote: zobacz helpa do interfejsu Statistics: gdy dany wykres
 		// pasuje do wielu modeli (np. liczba zabitych jako f(t)), to dodaj jego
 		// klasę do pakietu tunev.statistics; jeśli pasuje tylko do tego modelu,
 		// to dodaj do pakietu tego modelu
 		KilledStatistics killedStatistics = new KilledStatistics();
 		addCallback.add(killedStatistics);
 
 		// TODO: pozaznaczaj przeszkody na planszy
 
 		// TODO: pozaznaczaj wyjścia na planszy
 
 		// kolejne iteracje automatu -- uwaga, żadnego czekania w stylu
 		// Thread.sleep() -- to ma się policzyć *jak najszybciej*! --
 		// wyświetlanie "filmu" z symulacji jest niezależne od obliczania (no,
 		// tyle tylko zależne, że możemy wyświetlać tylko do momentu, który już
 		// się policzył)
		int num = (int) Math.round(Math.ceil(world.getDuration() / DT));
 		double t = 0;
 		for (int iteration = 1; iteration <= num; iteration++) {
 			// uaktualnij rzeczywisty czas naszej symulacji
 			t += DT;
 
 			board.update(t);
 			
 
 			// porób zdjęcia osobom w aktualnym rzeczywistym czasie
 			for (Person p : people){
 				try {
 					p.update();
 				} catch (NeighbourIndexException | WrongOrientationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				interpolator.saveState(p.profile, t, p.getCurrentState());
 				// <michał> wyświetlanie
 				// "edu.agh.tunev.model.PersonState@194cd40" niczego nie daje, a
 				// spowalnia, wykomentowałem :)
 				//
 				// System.out.println(p.getCurrentState());
 			}
 
 			// TODO: uaktualnij wykresy, które mogą być aktualizowane w trakcie
 			// symulowania
 			int currentNumDead = 123; // prawdopodobnie ta dana ustawiana
 										// gdzie indziej ;p~
 			killedStatistics.add(t, currentNumDead);
 
 			// grzeczność: zwiększ ProgressBar w UI
 			progressCallback.update(iteration, num,
					(iteration < num ? "Still simulating..." : "Ready!"));
 		}
 
 		// TODO: ew. wypełnij wykresy, które mogą być wypełnione dopiero po
 		// zakończeniu całej symulacji
 
 		// i tyle ^_^
 	}
 
 }
