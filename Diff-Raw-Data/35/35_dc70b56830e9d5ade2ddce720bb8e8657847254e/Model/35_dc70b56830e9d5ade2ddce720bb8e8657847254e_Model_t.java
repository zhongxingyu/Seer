 package edu.agh.tunev.model.cellular;
 
import java.awt.geom.Point2D;
 import java.util.Vector;
 
 import edu.agh.tunev.model.AbstractModel;
 import edu.agh.tunev.model.cellular.agent.Person;
 import edu.agh.tunev.model.cellular.grid.Board;
 import edu.agh.tunev.model.cellular.grid.Cell;
 import edu.agh.tunev.statistics.KilledStatistics;
 import edu.agh.tunev.statistics.Statistics.AddCallback;
 import edu.agh.tunev.world.Physics;
 import edu.agh.tunev.world.World;
 import edu.agh.tunev.world.World.ProgressCallback;
 
 public final class Model extends AbstractModel<Person> {
 
 	public final static String MODEL_NAME = "wąsowy automat komórkowy";
 
 	public Model(World world) {
 		super(world);
 	}
 
 	// przykładowa dyskretyzacja świata -- czyli rozmiar jednej komórki na
 	// planszy -- oczywiście w metrach -- do zmiany
 	private static final double DX = 0.5;
 	private static final double DY = 0.4;
 
 	// przykładowa dyskretyzacja czasu -- czyli co ile czasu nasze osobniki
 	// podejmują decyzję o skoku? inaczej: co ile rzeczywistego czasu
 	// update'ujemy stan naszego automatu -- oczywiście w sekundach -- do zmiany
 	private static final double DT = 0.5;
 
 	private Board board;
 
 	@Override
 	public void simulate(double duration, Vector<Person> people,
 			ProgressCallback progressCallback, AddCallback addCallback) {
 		// jakie są rzeczywiste wymiary świata?
 		double dimX = world.getXDimension();
 		double dimY = world.getYDimension();
 
 		// jakie są dyskretne wymiary świata? ile komórek w OX i OY?
 		// używa funkcji do tłumaczenia wymiarów z ciągłych na dyskretne z
 		// uwzględnieniem DX i DY. Zobacz poniżej ich definicje.
 		int numX = c2dX(dimX) + 1;
 		int numY = c2dY(dimY) + 1;
 
 		// stwórz automat (planszę komórek) o obliczonych dyskretnych wymiarach;
 		board = new Board(numX, numY);
 
 		// pozaznaczaj osoby na naszej modelowej, wewnętrznej, planszy
 		for (Person p : people) {
 			// w której komórce jest ta osoba?
			int ix = c2dX(p.getPosition().x);
			int iy = c2dY(p.getPosition().y);
 
 			// przesuń ją dokładnie na środek tej komórki...
			p.setPosition(new Point2D.Double(d2cX(ix), d2cY(iy)));
 			// ... i zrób jej tam "zdjęcie" dla interpolatora w chwili t=0[s]
 			interpolator.saveState(p, 0.0);
 
 			// zaznacz w odpowiedniej komórce automatu, że którą osobę
 			Cell c = board.get(ix, iy);
 			c.setPerson(p);
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
 		for (int i = 1; i <= num; i++) {
 			// uaktualnij rzeczywisty czas naszej symulacji
 			t += DT;
 
 			// pościągaj aktualną fizykę do komórek
 			for (int ix = 0; ix < numX; ix++)
 				for (int iy = 0; iy < numY; iy++) {
 					Physics physics = world.getPhysicsAt(t, d2cX(ix), d2cY(iy));
 					board.get(ix, iy).setPhysics(physics);
 				}
 
 			// przejdź do następnego stanu automatu
 			board.update();
 
 			// porób zdjęcia osobom w aktualnym rzeczywistym czasie
 			for (Person p : people)
 				interpolator.saveState(p, t);
 
 			// TODO: uaktualnij wykresy, które mogą być aktualizowane w trakcie
 			// iteracji
 			int currentNumDead = 123; // prawdopodobnie ta dana ustawiana
 										// gdzie indziej ;p~
 			killedStatistics.add(t, currentNumDead);
 
 			// grzeczność: zwiększ ProgressBar w UI
 			progressCallback.update(i, num, (i < num ? "Wciąż liczę..."
 					: "Gotowe!"));
 		}
 
 		// TODO: ew. wypełnij wykresy, które mogą być wypełnione dopiero po
 		// zakończeniu symulacji
 
 		// i tyle ^_^
 	}
 
 	/**
 	 * Discreet to continuous dimensions for OX.
 	 */
 	private static double d2cX(int ix) {
 		// zwróć pozycję w środku komórki
 		return (0.5 + ix) * DX;
 	}
 
 	/**
 	 * Discrete to continuous dimensions for OY.
 	 */
 	private static double d2cY(int iy) {
 		// zwróć pozycję w środku komórki
 		return (0.5 + iy) * DY;
 	}
 
 	/**
 	 * Continuous to discrete dimensions for OX.
 	 */
 	private static int c2dX(double x) {
 		return (int) Math.round(Math.floor(x / DX));
 	}
 
 	/**
 	 * Continuous to discrete dimensions for OY.
 	 */
 	private static int c2dY(double y) {
 		return (int) Math.round(Math.floor(y / DY));
 	}
 
 }
