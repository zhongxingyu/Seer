 package com.lumpundform.escenario;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.XmlReader;
 import com.badlogic.gdx.utils.XmlReader.Element;
 import com.lumpundform.actores.Heroe;
 import com.lumpundform.actores.Humanoide;
 import com.lumpundform.actores.ObjetoActor;
 import com.lumpundform.actores.Personaje;
 import com.lumpundform.ataques.Ataque;
 import com.lumpundform.ataques.AtaqueEscudo;
 import com.lumpundform.colision.Linea;
 import com.lumpundform.colision.Poligono;
 import com.lumpundform.eventos.Escena;
 import com.lumpundform.eventos.Evento;
 import com.lumpundform.excepciones.ActorNoDefinidoException;
 import com.lumpundform.excepciones.EscenarioSinHeroeException;
 import com.lumpundform.lumpundform.CamaraJuego;
 import com.lumpundform.pociones.PocionBase;
 import com.lumpundform.pociones.PocionMana;
 import com.lumpundform.pociones.PocionVida;
 import com.lumpundform.pociones.Porcentaje;
 import com.lumpundform.utilerias.U;
 
 /**
  * La base para los escenarios, extiende a {@link Stage} y agrega funciones y
  * valores específicos del juego.
  * 
  * @author Sergio Valencia & Luis Gutiérrez
  * 
  */
 public class EscenarioBase extends Stage {
 	private Poligono piso;
 	private Random random;
 	private Porcentaje porcentajePociones;
 	private int contador = 0;
 	private boolean heroeMuerto;
 
 	private Array<Evento> eventos;
 	private Array<Escena> escenas;
 
 	private boolean interfazBloqueada = false;
 
 	/**
 	 * Inicializa un nuevo escenario con los datos dados. Se inicializa un nuevo
 	 * contador aleatorio para las pociones.
 	 * 
 	 * @param width
 	 *            El ancho del escenario.
 	 * @param height
 	 *            El alto del escenario.
 	 * @param stretch
 	 *            Si se debe estirar.
 	 * @param batch
 	 *            El {@link SpriteBatch} con el que se van a dibujar los
 	 *            {@link Actor}es.
 	 */
 	EscenarioBase(float width, float height, boolean stretch, SpriteBatch batch) {
 		super(width, height, stretch, batch);
 		eventos = new Array<Evento>();
 		random = new Random();
 		setPorcentajePociones(new Porcentaje());
 	}
 
 	/**
 	 * Agregar a un actor y además le asigna un id para control interior.
 	 */
 	@Override
 	public void addActor(Actor actor) {
 		/**
 		 * Asigna un ID a todos los {@link ObjetoActor}es agregados al
 		 * escenario.
 		 */
 		if (ObjetoActor.class.isInstance(actor)) {
 			ObjetoActor oa = (ObjetoActor) actor;
 			oa.setId(contador++);
 		}
 		super.addActor(actor);
 	}
 
 	/**
 	 * Dibuja las líneas de colisión del piso del escenario y de todos los
 	 * {@link ObjetoActor}es que se encuentran en el escenario.
 	 */
 	void dibujarLineasColision(CamaraJuego camara) {
 		for (ObjetoActor actor : getActores(ObjetoActor.class)) {
 			U.dibujarLineasColision(actor.getHitbox(), camara);
 		}
 
 		U.dibujarLineasColision(getPiso(), camara);
 	}
 
 	/**
 	 * Revisa si el {@link Heroe} está colisionando con algún otro
 	 * {@link Personaje} en el escenario. Marca al {@link Heroe} como
 	 * colisionando.
 	 */
 	void colisionActores() {
 		try {
 			Heroe heroe = getHeroe();
 			heroe.setColisionActores(false);
 
 			for (Personaje personaje : getActores(Personaje.class)) {
 				if (personaje.getName() != "heroe" && heroe.getHitbox().estaColisionando(personaje.getHitbox())) {
 					heroe.setColisionActores(true);
 					break;
 				}
 			}
 		} catch (EscenarioSinHeroeException e) {
 		}
 	}
 
 	/**
 	 * Revisa si el {@link Heroe} está tocando una {@link PocionBase} para ver
 	 * si la agarra.
 	 */
 	void colisionPociones() {
 		try {
 			Heroe heroe = getHeroe();
 
 			for (PocionBase pocion : getActores(PocionBase.class)) {
 				if (heroe.getHitbox().estaColisionando(pocion.getHitbox())) {
 					if (heroe.agarrarPocion(pocion.getTipo())) {
 						pocion.remove();
 					}
 				}
 			}
 		} catch (EscenarioSinHeroeException e) {
 		}
 	}
 
 	/**
 	 * Revisa le colisión de los {@link Ataque}s del escenario con los
 	 * {@link Personaje}s del mismo para hacer daño. Si un ataque choca con un
 	 * escudo, se destruye sin hacer daño.
 	 */
 	void colisionAtaques() {
 		for (Ataque ataque : getActores(Ataque.class)) {
 			for (Personaje personaje : getActores(Personaje.class)) {
 				if (personaje.isEnemigo() != ataque.getPersonaje().isEnemigo()
 						&& personaje.getHitbox().estaColisionando(ataque.getHitbox())) {
 					if (ataque.isHaceDano()) {
 						personaje.quitarVida(ataque.getDano());
 					}
 					ataque.destruir();
 				}
 			}
 			for (Ataque at : getActores(Ataque.class)) {
 				if (AtaqueEscudo.class.isInstance(at)
 						&& at.getPersonaje().isEnemigo() != ataque.getPersonaje().isEnemigo()
 						&& at.getHitbox().estaColisionando(ataque.getHitbox())) {
 					ataque.destruir();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Detecta la colisión de todos los {@link ObjetoActor}es con el piso.
 	 */
 	void colisionPiso() {
 		Map<String, Boolean> caidaLibre = new HashMap<String, Boolean>();
 
 		for (ObjetoActor actor : getActores(ObjetoActor.class)) {
 			caidaLibre.put(actor.getName(), false);
 
 			// Datos para cuando va hacia derecha o izquierda
 			String puntoColision, puntoColisionTemp;
 			String direccionDiagonalDer, direccionDiagonalIzq, direccionLinea;
 			if (actor.derecha()) {
 				puntoColision = "inf-der";
 				puntoColisionTemp = "inf-izq";
 				direccionDiagonalDer = "abajo";
 				direccionDiagonalIzq = "arriba";
 				direccionLinea = "x";
 			} else {
 				puntoColision = "inf-izq";
 				puntoColisionTemp = "inf-der";
 				direccionDiagonalDer = "arriba";
 				direccionDiagonalIzq = "abajo";
 				direccionLinea = "-x";
 			}
 
 			// Revisa si el actor está en caída libre o si está colisionando
 			// alguna de las esquinas de su hitbox para inicializar variables
 			Vector2 p = null;
 			Float altura = null;
 			String direccionDiagonal = null;
 			if (!getPiso().estaColisionando(actor.getEsquina("inf-izq"))
 					&& !getPiso().estaColisionando(actor.getEsquina("inf-der"))) {
 				caidaLibre.put(actor.getName(), true);
 
 				Vector2 puntoTemp = actor.getEsquina(puntoColisionTemp);
 				p = new Vector2(puntoTemp.x, puntoTemp.y - 25);
 				altura = actor.getY();
 				direccionDiagonal = direccionDiagonalDer;
 			} else if (getPiso().estaColisionando(actor.getEsquina(puntoColision))) {
 				p = actor.getEsquina(puntoColision);
 				altura = actor.getY() + 25;
 				direccionDiagonal = direccionDiagonalIzq;
 			}
 
 			// Saca la línea en la cual se va a posicionar el actor si es
 			// necesario
 			if (p != null) {
 				Linea l = getPiso().linea("arriba", p);
 
 				// Posiciona al actor sobre la línea si la linea tiene una
 				// pendiente menor a 1
 				if (l != null && l.pendiente() != null && l.pendiente() <= 1.0001d && l.yEnX(p) <= altura
 						&& (l.direccionDiagonal() == direccionDiagonal || l.direccionLinea() == direccionLinea)) {
 					caidaLibre.put(actor.getName(), false);
 					actor.setY(l.yEnX(p));
 				}
 			}
 
 			// Cambia el estado colisión del actor, asi como teletransportar del
 			// héroe
 			if (caidaLibre.get(actor.getName())) {
 				actor.setColisionPiso(false);
 				if (actor.isCaer()) {
 					actor.setY(actor.getY() - 5);
 				}
 			} else {
 				actor.setColisionPiso(true);
 				if (actor.getName() == "heroe") {
 					actor.setTeletransportar(false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Limita las posiciones de los {@link ObjetoActor}es del escenario para que
 	 * no se salgan del mismo.
 	 * 
 	 * @param width
 	 *            El ancho del {@link EscenarioBase}.
 	 */
 	void acomodarActores(float width) {
 
 		for (Personaje personaje : getActores(Personaje.class)) {
 			if (personaje.getEsquina("inf-izq").x < 0)
 				personaje.setEsquinaX("inf-izq", 0.0f);
 			if (personaje.getEsquina("inf-der").x > width)
 				personaje.setEsquinaX("inf-izq", (width - personaje.getHitbox().getAncho()));
 
 			// Detecta colisión con paredes
 			Vector2 pc = null;
 			String lineaLateral;
 			Double yPunto = null;
 			if (personaje.derecha()) {
 				pc = personaje.getEsquina("inf-der");
 				lineaLateral = "izquierda";
 				yPunto = Math.floor(pc.y);
 			} else {
 				pc = personaje.getEsquina("inf-izq");
 				lineaLateral = "derecha";
 				yPunto = Math.floor(pc.y) + 10.0f;
 			}
 
 			if (getPiso().estaColisionando(pc) && getPiso().linea("arriba", pc).esHorizontal()
 					&& getPiso().linea(lineaLateral, pc).esVertical()
 					&& yPunto < Math.floor(getPiso().linea("arriba", pc).yEnX(pc))) {
 				Linea linea = getPiso().linea(lineaLateral, pc);
 				if (linea != null) {
 					Float xLinea = null;
 					if (lineaLateral == "izquierda") {
 						xLinea = linea.xEnY(pc) - personaje.getHitbox().getAncho() - 1;
 					} else {
 						xLinea = linea.xEnY(pc) + 1;
 					}
 					personaje.setEsquinaX("inf-izq", xLinea);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Acomoda al {@link Heroe} para que no se salga de la {@link CamaraJuego}.
 	 */
 	void acomodarHeroe() {
 		try {
 			CamaraJuego camara = U.getCamara();
 			float min = camara.getPosicionOrigen().x;
 			float max = camara.getPosicionOrigen().x + camara.viewportWidth;
 			Heroe heroe = getHeroe();
 			if (heroe.getEsquina("inf-izq").x < min)
 				heroe.setEsquinaX("inf-izq", min);
 			if (heroe.getEsquina("inf-der").x > max)
 				heroe.setEsquinaX("inf-izq", (max - heroe.getHitbox().getAncho()));
 		} catch (EscenarioSinHeroeException e) {
 		}
 	}
 
 	/**
 	 * Carga todos los eventos al {@link EscenarioBase} que se encuentran el
 	 * archivo .tmx.
 	 * 
 	 * @param tog
 	 *            El {@link TiledObjectGroup} donde se encuentran todos los
 	 *            eventos.
 	 */
 	void cargarEventos(TiledObjectGroup tog) {
 		for (TiledObject to : tog.objects) {
 			eventos.add(new Evento(U.voltearCoordenadas(to.x, to.y), to, this));
 		}
 	}
 
 	/**
 	 * Revisa todos los eventos del {@link EscenarioBase} para ver si se
 	 * ejecutan.
 	 */
 	void revisarEventos() {
 		try {
 			for (int i = 0; i < eventos.size; i++) {
 				eventos.get(i).revisarEvento(getHeroe());
 			}
 		} catch (EscenarioSinHeroeException e) {
 		}
 	}
 
 	/**
 	 * Carga todas las {@link Escena}s del escenario especificado.
 	 * 
 	 * @param escenario
 	 *            El nombre del escenario del cual cargar las escenas.
 	 */
 	void cargarEscenas(String escenario) {
 		escenas = new Array<Escena>();
 
 		XmlReader xmlF;
 		String xmlS;
 		Element xmlE;
 
 		xmlF = new XmlReader();
 		xmlS = Gdx.files.internal("escenas/escenario_" + escenario + ".xml").readString();
 		xmlE = xmlF.parse(xmlS);
 
 		Array<Element> escena = xmlE.getChildrenByNameRecursively("escena");
 
 		for (Element e : escena) {
 			escenas.add(new Escena(e, e.get("nombre")));
 		}
 	}
 
 	/**
 	 * @param nombre
 	 *            El nombre de la {@link Escena} a buscar.
 	 * @return La {@link Escena} con el nombre dado o <code>null</code> si no se
 	 *         encontró.
 	 */
 	public Escena getEscena(String nombre) {
 		for (Escena escena : escenas) {
 			if (escena.getNombre().equals(nombre))
 				return escena;
 		}
 		return null;
 	}
 
 	/**
 	 * Agrega a un actor sin evento.
 	 * 
 	 * @param tipo
 	 * @param posicion
 	 * @see #agregarActor(String, Vector2, String)
 	 */
 	void agregarActor(String tipo, Vector2 posicion) {
 		agregarActor(tipo, posicion, "");
 	}
 
 	/**
 	 * Agrega un actor al {@link EscenarioBase} en la posición y del tipo
 	 * especificado. También se inicializa el evento al que pertenece.
 	 * 
 	 * @param tipo
 	 *            El tipo de {@link ObjetoActor} a agregar.
 	 * @param posicion
 	 *            La posición en donde agregarlo.
 	 * @param evento
 	 *            El evento al que pertenece el {@link ObjetoActor}. Si no
 	 *            pertenece a ningún evento, pasar <code>""</code>.
 	 */
 	public void agregarActor(String tipo, Vector2 posicion, String evento) {
 		Personaje actor;
 		if (tipo == "heroe") {
 			actor = new Heroe(posicion);
 			setHeroeMuerto(false);
 		} else if (tipo == "humanoide") {
 			actor = new Humanoide("amigo", posicion);
 		} else if (tipo == "enemigo") {
 			actor = new Humanoide("amigo", posicion);
 			actor.setEnemigo(true);
 		} else {
 			throw new ActorNoDefinidoException("El Actor " + tipo + " no esta definido");
 		}
 		actor.setPerteneceAEvento(evento);
 		addActor(actor);
 	}
 
 	/**
 	 * Regresa al {@link Heroe} del escenario.
 	 * 
 	 * @return El héroe.
 	 * @throws EscenarioSinHeroeException
 	 *             Cuando el {@link EscenarioBase} no tiene {@link Heroe}.
 	 */
 	public Heroe getHeroe() throws EscenarioSinHeroeException {
 		List<Heroe> actores = getActores(Heroe.class);
 		if (actores.size() == 0) {
 			throw new EscenarioSinHeroeException("No hay héroe en el escenario.");
 		} else {
 			return actores.get(0);
 		}
 	}
 
 	/**
 	 * Busca a todos los actores dentro del escenario de la clase dada.
 	 * 
 	 * @param <T>
 	 *            La clase a buscar. Tiene que heredar de {@link Actor}.
 	 * @param clase
 	 *            Llamar .class de la clase
 	 * @return Una lista de los actores tipo <T>.
 	 */
 	public <T extends Actor> List<T> getActores(Class<T> clase) {
 		@SuppressWarnings("unchecked")
 		Iterator<T> i = (Iterator<T>) getActors().iterator();
 		List<T> actores = new ArrayList<T>();
 		while (i.hasNext()) {
 			T actor = i.next();
 			if (clase.isInstance(actor)) {
 				actores.add(actor);
 			}
 		}
 		return actores;
 	}
 
 	/**
 	 * Quita todos los ataques que no están dentro de la vista de la
 	 * {@link CamaraJuego}.
 	 */
 	void destruirAtaques() {
 		CamaraJuego camara = U.getCamara();
 		for (Ataque ataque : getActores(Ataque.class)) {
 			if ((ataque.getX() + ataque.getWidth()) < camara.getPosicionOrigen().x
 					|| ataque.getX() > (camara.getPosicionOrigen().x + camara.viewportWidth)) {
 				ataque.quitar();
 			}
 		}
 	}
 
 	/**
 	 * Busca el {@link Evento} del nombre dado.
 	 * 
 	 * @param nombreEvento
 	 *            El nombre del {@link Evento}.
 	 * @return El {@link Evento} encontrado o null si no se encontró.
 	 */
 	public Evento getEvento(String nombreEvento) {
 		for (Evento evento : eventos) {
 			if (evento.getNombre().equals(nombreEvento)) {
 				return evento;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Crea una {@link PocionBase} si cumple las condiciones de probabilidad.
 	 * 
 	 * @param posicion
 	 *            La posición de la poción de ser creada.
 	 */
 	public void crearPocion(Vector2 posicion) {
 		if (random.nextFloat() < getPorcentajePociones().getValor()) {
 			if (random.nextBoolean()) {
 				addActor(new PocionVida(posicion));
 			} else {
 				addActor(new PocionMana(posicion));
 			}
 			getPorcentajePociones().reset();
 		} else {
 			getPorcentajePociones().aumentar();
 		}
 	}
 
 	/**
 	 * Continúa la conversación del evento con nombre <code>nombre</code>.
 	 * 
 	 * @param nombre
 	 *            El nombre del {@link Evento}.
 	 */
 	public void continuarConversacionActual(String nombre) {
 		for (Evento evento : eventos) {
 			if (evento.getNombre().equals(nombre)) {
 				evento.continuarConversacionEnEscena();
 			}
 		}
 	}
 
 	/**
 	 * Hace un toggle del UI. Si está visible, la esconde, si no, la muestra.
 	 */
 	public void toggleUI() {
 		for (Actor actor : getActors()) {
 			if (actor.getClass().getSimpleName().contains("Boton")
 					|| actor.getClass().getSimpleName().contains("Barra")) {
 				if (actor.isVisible()) {
 					SequenceAction sequence = new SequenceAction();
 					sequence.addAction(Actions.fadeOut(0.30f));
 					sequence.addAction(Actions.delay(0.30f));
 					sequence.addAction(Actions.hide());
 					actor.addAction(sequence);
 				} else {
 					SequenceAction sequence = new SequenceAction();
 					sequence.addAction(Actions.show());
 					sequence.addAction(Actions.fadeIn(0.30f));
 					actor.addAction(sequence);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Esconde o muestra la UI.
 	 * 
 	 * @param setVisible
 	 *            Si se va a esconder o a mostrar.
 	 */
 	public void esconderUI(boolean setVisible) {
 		for (Actor actor : getActors()) {
 			if (actor.getClass().getSimpleName().contains("Boton")
 					|| actor.getClass().getSimpleName().contains("Barra")) {
 				if (!setVisible) {
 					SequenceAction sequence = new SequenceAction();
 					sequence.addAction(Actions.fadeOut(0.30f));
 					sequence.addAction(Actions.delay(0.30f));
 					sequence.addAction(Actions.hide());
 					actor.addAction(sequence);
 				} else {
 					SequenceAction sequence = new SequenceAction();
 					sequence.addAction(Actions.show());
 					sequence.addAction(Actions.fadeIn(0.30f));
 					actor.addAction(sequence);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Busca o crea un {@link Actor} en posición 0,0.
 	 * 
 	 * @param nombreActor
 	 * @return
 	 * @see #getActor(String, Vector2)
 	 */
 	@SuppressWarnings("javadoc")
 	public ObjetoActor getActor(String nombreActor) {
 		return getActor(nombreActor, new Vector2(0, 0));
 	}
 
 	/**
 	 * Busca al {@link Actor} del nombre dado. Si no lo encuentra crea uno nuevo
 	 * en la posición especificada y lo regresa.
 	 * 
 	 * @param nombreActor
 	 *            El nombre del {@link Actor}.
 	 * @param posicion
 	 *            La posición donde crearlo de no existir.
 	 * @return El {@link Actor} encontrado o creado.
 	 */
 	public ObjetoActor getActor(String nombreActor, Vector2 posicion) {
 		ObjetoActor personaje = null;
 		for (ObjetoActor personajeTemporal : getActores(ObjetoActor.class)) {
 			if (personajeTemporal.getName().equals(nombreActor)) {
 				personaje = personajeTemporal;
 				break;
 			}
 		}
 		if (personaje == null) {
 			addActor(new Humanoide(nombreActor, posicion));
 			personaje = (Personaje) getActor(nombreActor);
 		}
 		return personaje;
 	}
 
 	public Poligono getPiso() {
 		return piso;
 	}
 
 	public void setPiso(Poligono piso) {
 		this.piso = piso;
 	}
 
 	public Porcentaje getPorcentajePociones() {
 		return porcentajePociones;
 	}
 
 	public void setPorcentajePociones(Porcentaje porcentajePociones) {
 		this.porcentajePociones = porcentajePociones;
 	}
 
 	public Array<Evento> getEventos() {
 		return eventos;
 	}
 
 	public boolean isHeroeMuerto() {
 		return heroeMuerto;
 	}
 
 	public void setHeroeMuerto(boolean heroeMuerto) {
 		this.heroeMuerto = heroeMuerto;
 	}
 
 	public boolean getInterfazBloqueada() {
 		return interfazBloqueada;
 	}
 
 	public void setInterfazBloqueada(boolean bloquear) {
 		this.interfazBloqueada = bloquear;
	}
}
