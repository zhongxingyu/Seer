 package agent;
 
 import board.Board;
 import board.Board.Barrier;
 import board.Board.Exit;
 import board.Board.NoPhysicsDataException;
 import board.Board.Obstacle;
 import board.Board.Physics;
 import board.Point;
 
 public final class Agent {
 
 	enum Stance {
 		STAND, CROUCH, CRAWL
 	}
 
 	/** Wspolczynnik przeskalowujacy temperature na zagroenie */
 	static final double TEMP_THREAT_COEFF = 0.06;
 
 	/** Szeroko elipsy Agenta w [m]. */
 	public static final double BROADNESS = 0.33;
 
 	/** Ten drugi wymiar (grubo?) elipsy Agenta w [m]. */
 	public static final double THICKNESS = 0.2;
 
 	/**
 	 * Dugo wektora orientacji Agenta w [m]. Nic nie robi, tylko do
 	 * rysowania.
 	 */
 	public static final double ORIENTATION_VECTOR = 1.0;
 
 	/** Kat miedzy promieniami wyznaczajacymi wycinek kola bedacy sasiedztwem */
 	private static final double CIRCLE_SECTOR = 45; // 360/8
 
 	/**
 	 * Wartosc podstawy w f. wykladniczej wykorzystywana do obliczania promienia
 	 * sasiedztwa za pomoca kata
 	 */
 	private static final double BASE_RADIUS_CALC = 1.2;
 
 	/**
 	 * Wartosc podstawy w f. wykladniczej wykorzystywana do obliczania
 	 * wspolczynnika atrakcyjnosci danego kierunku. Wspolczynniki dla kierunkow
 	 * o mniejszych katach, czyli takich, ktore pozwola mniej wiecej zachowac
 	 * kierunek ucieczki, maja odpowiednio wieksza wartosc.
 	 */
 	private static final double BASE_ATTR_CALC = 1.01;
 
 	/**
 	 * Wspczynnik do skalowania funkcji wykadniczej wykorzystywanej do
 	 * obliczania promienia ssiedztwa
 	 */
 	private static final double POW_RADIUS_COEFF = 8;
 
 	/**
 	 * Wspczynnik do skalowania funkcji wykadniczej wykorzystywanej do
 	 * obliczania wspolczynnika atrakcyjnosci.
 	 */
 	private static final double POW_ATTR_COEFF = 1;
 
 	/**
 	 * Minimalna warto wspczynnika zagroenia powodujca zmian kierunku.
 	 * Agent zawsze kieruj si w stron wyjcia, chyba e czynniki rodowiskowe
 	 * mu na to nie pozwalaj.
 	 */
 	private static final double MIN_THREAT_VAL = 60;
 
 	/**
 	 * Odleglosc od wyjscia, dla ktorej agent przestaje zwracac uwage na
 	 * czynniki zewnetrzne i rzuca sie do drzwi/portalu
 	 */
 	private static final double EXIT_RUSH_DIST = 3;
 
 	/** Minimalna temp. przy ktrej agent widzi ogie */
 	private static final double MIN_FLAME_TEMP = 70;
 
 	/** Smiertelna wartosc temp. na wysokosci 1,5m */
 	private static final double LETHAL_TEMP = 80;
 
 	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
 	private static final double LETHAL_CO_CONCN = 30000.0;
 
 	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
 	private static final double LETHAL_HbCO_CONCN = 75.0;
 
 	/** Prdko z jak usuwane s karboksyhemoglobiny z organizmu */
 	private static final double CLEANSING_VELOCITY = 0.08;
 
 	/** Wsp. do obliczania gstoci dymu na podstawie stenia CO */
 	// TODO: bardzo nacigane, ale to jest zbyt zmienne i nie ma danych
 	private static final double CO_SMOKE_COEFF = 6.5;
 
 	/** Wspczynnik funkcji przeksztacajcej odlego na czas reakcji */
 	private static final double REACTION_COEFF = 0.3 * 1000; // wspolczynnik *
 																// [s/ms]
 
 	/** Pozycja Agenta na planszy w rzeczywistych [m]. */
 	Point position;
 
 	/** Aktualnie wybrane wyjcie ewakuacyjne */
 	Exit exit;
 
 	/** Referencja do planszy. */
 	Board board;
 
 	/**
 	 * Orientacja: kt midzy wektorem "wzroku" i osi OX w [deg]. Kiedy wynosi
 	 * 0.0 deg, to Agent "patrzy" jak o OX (jak na geometrii analitycznej).
 	 * Wtedy te sin() i cos() dziaaj ~intuicyjne, tak samo jak analityczne
 	 * wzory. :] -- m.
 	 */
 	double phi;
 
 	/** Flaga informujca o statusie jednostki - zywa lub martwa */
 	private boolean alive;
 
 	/** Flaga mwica o tym, czy Agentowi udao si ju uciec. */
 	boolean exited;
 
 	/** Czas, ktry upynie, nim agent podejmie decyzje o ruchu */
 	private double pre_movement_t;
 
 	/** Aktualne stezenie karboksyhemoglobiny we krwii */
 	private double hbco;
 
 	/** Czas ruchu agenta */
 	double dt;
 
 	/** 'Modul' ruchu agenta */
 	private Motion motion;
 
 	/** Charakterystyka psychiki agenta */
 	private Psyche psyche;
 
 	/**
 	 * Konstruktor agenta. Inicjuje wszystkie pola niezbdne do jego egzystencji
 	 * na planszy. Pozycja jest z gry narzucona z poziomu Board. Orientacja
 	 * zostaje wylosowana.
 	 * 
 	 * @param board
 	 *            referencja do planszy
 	 * @param position
 	 *            referencja to komrki bdcej pierwotn pozycj agenta
 	 */
 	public Agent(Board board, Point position) {
 		this.board = board;
 		this.position = position;
 		motion = new Motion(this);
 		psyche = new Psyche(this);
 
 		phi = Math.random() * 360 - 180;
 
 		alive = true;
 		exited = false;
 		hbco = 0;
 		dt = 0;
 
 		pre_movement_t = (REACTION_COEFF
 				* position.evalDist(board.getNearestFireSrc(position)) + psyche.reaction_t);
 	}
 
 	/**
 	 * Akcje agenta w danej iteracji.
 	 * 
 	 * 1. Sprawdza, czy agent nie powinien zginac w tej turze.
 	 * 
 	 * 2. Wybiera wyjcie.
 	 * 
 	 * 3. Aktualizuje liste checkpointow.
 	 * 
 	 * 4. Aktualizuje stopie poddenerwowania agenta.
 	 * 
 	 * 5. Dostosowuje prdko do warunkw z uwzgldnieniem poddenerwowania.
 	 * 
 	 * 6. Na podstawie danych otrzymanych w poprzednim punkcie podejmuje
 	 * decyzje.
 	 * 
 	 * 7. Wykonuje ruch.
 	 * 
 	 * @param dt
 	 *            Czas w [ms] jaki upyn od ostatniego update()'u. Mona
 	 *            wykorzysta go do policzenia przesunicia w tej iteracji z
 	 *            zadan wartoci prdkoci:
 	 *            {@code dx = dt * v * cos(phi); dy = dt * v * sin(phi);}
 	 * @throws NoPhysicsDataException
 	 */
 	public void update(double _dt) throws NoPhysicsDataException {
 		this.dt = _dt;
 		double curr_temp = getMeanPhysics(0, 360, BROADNESS,
 				Physics.TEMPERATURE);
		double curr_co = getMeanPhysics(0, 360, BROADNESS, Physics.CO);
 		checkIfIWillLive(curr_co, curr_temp);
 
 		if (alive) {
 			double smoke_density = curr_co * CO_SMOKE_COEFF;
 
 			chooseExit();
 			motion.updateCheckpoints();
 			psyche.expAnxiety(TEMP_THREAT_COEFF * curr_temp);
 			motion.adjustVelocity(smoke_density, psyche.anxiety);
 			makeDecision();
 			motion.move();
 		}
 
 		// jak wyszlimy poza plansz, to wyszlimy z tunelu? exited = true
 		// spowoduje zaprzestanie wywietlania agenta i podbicie statystyk
 		// uratowanych w kadym razie :]
 		// TODO: zmienia na true dopiero gdy doszlimy do wyjcia
 		exited = (distToExit(exit) < THICKNESS)
 				|| (position.x < 0 || position.y < 0
 						|| position.x > board.getDimension().x || position.y > board
 						.getDimension().y);
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 
 	/**
 	 * 
 	 * @return obrot wzg OX
 	 */
 	public double getOrientation() {
 		return phi;
 	}
 
 	/**
 	 * Zwraca warto niezalenie od tego, czy jest na planszy, czy nie.
 	 * 
 	 * @return stan zdrowia
 	 */
 	public boolean isAlive() {
 		return alive;
 	}
 
 	/**
 	 * Sprawdza czy agent jest na planszy
 	 * 
 	 * @return
 	 */
 	public boolean isExited() {
 		return exited;
 	}
 
 	/**
 	 * 
 	 * @return czas ktory uplynie przed podjeciem ruchu
 	 */
 	public double getPreMoveTime() {
 		return pre_movement_t;
 	}
 
 	/**
 	 * Zwraca stenie karboksyhemoglobiny we krwi
 	 * 
 	 * @return hbco
 	 */
 	public double getHBCO() {
 		return hbco;
 	}
 
 	/**
 	 * Zwraca aktualna prdko agenta
 	 * 
 	 * @return velocity
 	 */
 	public double getVelocity() {
 		return motion.velocity;
 	}
 
 	/**
 	 * Okresla, czy agent przezyje, sprawdzajac temperature otoczenia i stezenie
 	 * toksyn we krwii
 	 * 
 	 * @param curr_co
 	 *            stenie co w bliskim otoczeniu agenta
 	 * @param curr_temp
 	 *            rednia temp. w bliskim otoczeniu agenta
 	 */
 	private void checkIfIWillLive(double curr_co, double curr_temp) {
 		evaluateHbCO(curr_co);
 
 		if (hbco > LETHAL_HbCO_CONCN || curr_temp > LETHAL_TEMP)
 			alive = false;
 	}
 
 	/**
 	 * Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
 	 * zdolnosci organizmu do usuwania toksyn
 	 */
 	private void evaluateHbCO(double curr_co) {
 		// TODO: Dobrac odpowiednie parametry
 		if (hbco > dt * CLEANSING_VELOCITY)
 			hbco -= dt * CLEANSING_VELOCITY;
 
		hbco += dt * LETHAL_HbCO_CONCN * (curr_co / LETHAL_CO_CONCN);
 	}
 
 	/**
 	 * Podejmuje decyzje, co do kierunku ruchu lub ustala nowy checkpoint.
 	 */
 	private void makeDecision() {
 		phi = calculateNewPhi();
 		double attractivness_ahead = computeThreatComponent(0);
 		Barrier barrier = motion.isStaticCollision(0);
 
 		if (distToExit(exit) > EXIT_RUSH_DIST
 				&& attractivness_ahead > MIN_THREAT_VAL && barrier == null) {
 
 			double attractivness = Double.POSITIVE_INFINITY;
 			for (double angle = -180; angle < 180; angle += CIRCLE_SECTOR) {
 				if (angle == 0)
 					continue;
 
 				double attr_coeff = 1 / computeMagnitudeByAngle(POW_ATTR_COEFF,
 						BASE_ATTR_CALC, angle);
 				double curr_attractivness = attr_coeff
 						* computeThreatComponent(angle);
 
 				if (curr_attractivness < attractivness
 						&& motion.isStaticCollision(angle) == null) {
 
 					attractivness = curr_attractivness;
 					phi += angle;
 				}
 			}
 		}
 
 		if (barrier instanceof Obstacle)
 			motion.addCheckpoint(motion.avoidCollision((Obstacle) barrier));
 	}
 
 	/**
 	 * Metoda obliczajca kt, ktry agent musi obra, by skierowa si do
 	 * wybranego checkpointa. Kt jest wyznaczony przez o X i odcinek czcy
 	 * najblizszy checkpoint z aktualn pozycj agenta. Korzysta z funkcji
 	 * atan2(), ktra w przeciwiestwie do atan() uwzgldnia orientacj na
 	 * paszczynie.
 	 * 
 	 * @return kt zawart w przedziale [-180, 180)
 	 */
 	private double calculateNewPhi() {
 		if (motion.checkpoints.isEmpty()) // TODO: chyba tak ma by, nie byo
 											// tego sprawdzenia i wywalao
 											// ArrayIndexOutOfBoundsException --
 											// m.
 			return phi;
 
 		Point checkpoint = motion.checkpoints
 				.get(motion.checkpoints.size() - 1);
 		double deltaY = checkpoint.y - position.y;
 		double deltaX = checkpoint.x - position.x;
 		double angle = Math.atan2(deltaY, deltaX);
 
 		return Math.toDegrees(angle);
 	}
 
 	/**
 	 * Wybr najbliszego wyjcia do ktrego moliwe jest przejcie
 	 * 
 	 * @throws NoPhysicsDataException
 	 */
 	private void chooseExit() throws NoPhysicsDataException {
 		Exit chosen_exit = null;
 		Exit curr_exit = getNearestExit(-1);
 
 		do {
 			chosen_exit = curr_exit;
 			double dist_exit = distToExit(chosen_exit);
 			curr_exit = getNearestExit(dist_exit);
 		} while (checkForBlockage(chosen_exit) > 0 && curr_exit != null);
 
 		exit = chosen_exit;
 
 	}
 
 	/**
 	 * Bierze pod uwage odlegoci na tylko jednej osi. Szuka najbliszego
 	 * wyjcia w odlegoci nie mniejszej ni dist. Pozwala to na szukanie wyj
 	 * bdcych alternatywami. Dla min_dist mniejszego od 0 szuka po prostu
 	 * najbliszego wyjcia
 	 * 
 	 * @param min_dist
 	 *            zadana minimalna odlego
 	 * @return najblisze wyjcie speniajce warunki
 	 */
 	private Exit getNearestExit(double min_dist) {
 		double shortest_dist = board.getDimension().x + board.getDimension().y;
 		Exit nearest_exit = null;
 
 		for (Exit e : board.getExits()) {
 			double dist = Math.abs(distToExit(e));
 			if (dist < shortest_dist && dist > min_dist) {
 				shortest_dist = dist;
 				nearest_exit = e;
 			}
 		}
 		return nearest_exit;
 	}
 
 	/**
 	 * Algorytm dziaa, poruszajc sie po dwch osiach: X - zawsze, Y - jeli
 	 * znajdzie blokad. Zaczyna od wspolrzdnej Y agenta i porszuamy si po tej
 	 * osi w stron potencjalnego wyjcia. Jeli natrafi na przeszkod, to
 	 * sprawdza, czy caa szeroko tunelu dla tej wartoci Y jest zablokowana.
 	 * Porszuajc si po osi X o szeroko agenta, sprawdza, czy na caym
 	 * odcinku o d. rwnej szerokoci tunelu znajduj si blokady. Jeli
 	 * znajdzie si cho jeden przesmyk - przejcie istnieje -> sprawdzamy
 	 * kolejne punkty na osi Y. Jeli nie istnieje, metoda zwraca wspolrzedna Y
 	 * blokady.
 	 * 
 	 * TODO: W bardziej rzeczywistym modelu agent wybierze kierunek przeciwny do
 	 * rda ognia.
 	 * 
 	 * @param _exit
 	 *            wyjcie, w kierunku ktrego agent chce ucieka
 	 * @return -1 jeli drgoa do wyjcia _exit nie jest zablokowana wspolrzedna
 	 *         y blokady, jesli nie ma przejscia
 	 * @throws NoPhysicsDataException
 	 */
 	// TODO: rework, uwaga na (....XXX__XX...)
 	private double checkForBlockage(Exit _exit) {
 		boolean viable_route = true;
 		double exit_y = _exit.getExitY();
 		double dist = Math.abs(position.y - exit_y);
 		double ds = board.getDataCellDimension().y;
 
 		if (position.y > exit_y)
 			ds = -ds;
 
 		// poruszamy si po osi Y w kierunku wyjcia
 		double y_coord = position.y + ds;
 		while (Math.abs(y_coord - position.y) < dist) {
 			double x_coord = 0 + BROADNESS;
 			double checkpoint_y_temp = 0;
 			try {
 				checkpoint_y_temp = board.getPhysics(
 						new Point(x_coord, y_coord), Physics.TEMPERATURE);
 			} catch (NoPhysicsDataException ex) {
 				// nic sie nie dzieje
 			}
 
 			// poruszamy si po osi X, jeli natrafilimy na blokad
 			if (checkpoint_y_temp > MIN_FLAME_TEMP) {
 				viable_route = false;
 				while (x_coord < board.getDimension().x) {
 					double checkpoint_x_temp = MIN_FLAME_TEMP;
 					Point checkpoint_x = new Point(x_coord, y_coord);
 					try {
 						checkpoint_x_temp = board.getPhysics(checkpoint_x,
 								Physics.TEMPERATURE);
 					} catch (NoPhysicsDataException ex) {
 						// nic sie nie dzieje
 					}
 
 					if (checkpoint_x_temp < MIN_FLAME_TEMP
 							|| motion.isObstacleInPos(checkpoint_x) == null)
 						viable_route = true;
 
 					x_coord += BROADNESS;
 				}
 			}
 			// jeli nie ma przejcia zwracamy wsp. Y blokady
 			if (!viable_route)
 				return y_coord;
 
 			y_coord += ds;
 		}
 		return -1;
 	}
 
 	/**
 	 * Oblicza odleglosc miedzy aktualna pozycja a wyjsciem
 	 * 
 	 * @param _exit
 	 *            wybrane wyjscie
 	 * @return odleglosc
 	 */
 	double distToExit(Exit _exit) {
 		if (_exit == null)
 			return Double.POSITIVE_INFINITY;
 		Point exit_closest_p = _exit.getClosestPoint(position);
 		return position.evalDist(exit_closest_p);
 	}
 
 	/**
 	 * Zwraca redni warto parametru fizycznego na wybranej powierzchni --
 	 * wycinka koa o rodku w rodku danego Agenta.
 	 * 
 	 * Koncept: 1) jedziemy ze staym {@code dalpha} po caym {@code alpha}; 2)
 	 * dla kadego z tych ktw jedziemy ze staym {@code dr} po {@code r}. 3)
 	 * Bierzemy warto parametru w punkcie okrelonym przez {@code dalpha} i
 	 * {@code dr}, dodajemy do sumy, a na kocu 4) zwracamy sum podzielon
 	 * przez liczb wybranych w ten sposb punktw.
 	 * 
 	 * Taki sposb ma 2 zalety: 1) jest ultraprosty, 2) punkty bliej pozycji
 	 * Agenta s gciej rozmieszczone na wycinku, dlatego wiksze znaczenie ma
 	 * temperatura przy nim. ^_^ (Jeszcze kwestia dobrego dobrania
 	 * {@code dalpha} i {@code dr}).
 	 * 
 	 * @param orientation
 	 *            Kt midzy wektorem orientacji Agenta a osi symetrii wycinka
 	 *            koa. Innymi sowy, jak chcemy wycinek po lewej rce danego
 	 *            Agenta, to dajemy tu 90.0 [deg], jak po prawej to -90.0 [deg].
 	 *            (Dlatego, e kty w geometrii analitycznej rosn przeciwnie do
 	 *            ruchu wskazwek zegara!).
 	 * @param alpha
 	 *            Rozstaw "ramion" wycinka koa w [deg]. Jak chcemy np. 1/8
 	 *            koa, to dajemy 45.0 [deg], w miar oczywiste chyba. By moe
 	 *            warto zmieni nazw tego parametru.
 	 * 
 	 *            Nic nie stoi na przeszkodzie, eby wywoa t funkcj z
 	 *            {@code alpha == 0.0} i zdj redni tylko z linii.
 	 * 
 	 *            Mona take przyj {@code alpha == 360.0} i policzy redni
 	 *            z caego otoczenia, np. do wyznaczenia warunkw mierci
 	 *            (zamiast punktowo, tylko na pozycji Agenta). ^_^
 	 * @param r
 	 *            Promie koa, na powierzchni wycinka ktrego obliczamy
 	 *            redni. (Ale konstrukt jzykowy ;b).
 	 * @param what
 	 *            O ktr wielko fizyczn nam chodzi.
 	 * @return
 	 */
 	private double getMeanPhysics(double orientation, double alpha, double r,
 			Physics what) {
 		if (alpha < 0)
 			throw new IllegalArgumentException("alpha < 0");
 		if (r < 0)
 			throw new IllegalArgumentException("r < 0");
 
 		double dalpha = 10; // [deg]
 		double dr = 0.5; // [m]
 
 		double alphaA = phi + orientation - alpha / 2;
 		double alphaB = phi + orientation + alpha / 2;
 		double rA = 0;
 		double rB = r;
 
 		double sum = 0.0;
 		long num = 0;
 
 		alpha = alphaA;
 		// dlatego jest potrzebna konstrukcja do-while, eby to wykonao si
 		// przynajmniej raz (nie jestem pewien czy przy kcie zerowym by
 		// zadziaao z uyciem for-a -- bdy numeryczne: nie mona porwnywa
 		// zmiennoprzecinkowych)
 		do {
 			double sin = Math.sin(Math.toRadians(alpha));
 			double cos = Math.cos(Math.toRadians(alpha));
 			r = rA;
 			do {
 				try {
 					sum += board.getPhysics(new Point(position.x + cos * r,
 							position.y + sin * r), what);
 					num++;
 				} catch (NoPhysicsDataException e) {
 					// nie ma danych tego typu w tym punkcie -- nie uwzglniaj
 					// go do redniej
 				}
 				r += dr;
 			} while (r <= rB);
 			alpha += dalpha;
 		} while (alpha <= alphaB);
 
 		return sum / num;
 	}
 
 	/**
 	 * Oblicza wspolczynnik zagrozenia dla danego kierunku.
 	 * 
 	 * @param angle
 	 *            potencjalnie obrany kierunek
 	 * @return wspolczynnik atrakcyjnosci dla zadanego kierunku, im wyzszy tym
 	 *         GORZEJ
 	 */
 	private double computeThreatComponent(double angle) {
 		double attractivness_comp = 0.0;
 		double r_ahead = computeMagnitudeByAngle(POW_RADIUS_COEFF,
 				BASE_RADIUS_CALC, angle);
 
 		attractivness_comp += getMeanPhysics(angle, CIRCLE_SECTOR, r_ahead,
 				Physics.TEMPERATURE);
 		return attractivness_comp;
 	}
 
 	/**
 	 * Dzieki tej funkcji mozemy latwo otrzymac odpowiednia dlugosc promienia
 	 * sasiedztwa, zaleznie od tego, pod jakim katem jest ono obrocone.
 	 * 
 	 * @param base
 	 *            podstawa potegowania, ma duzy wplyw na zroznicowanie dlugosci
 	 *            promienia, jako ze zmienia sie ona wykladniczo
 	 * @param angle
 	 * @return dlugosc promienia
 	 */
 	private double computeMagnitudeByAngle(double pow_coeff, double base,
 			double angle) {
 		return pow_coeff
 				* Math.pow(base, (180 - Math.abs(angle)) / CIRCLE_SECTOR);
 	}
 }
