 package simulation.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 
 import panchat.data.User;
 
 import simulation.arrows.MessageArrow;
 import simulation.arrows.MultipleArrow;
 import simulation.arrows.SingleArrow;
 import simulation.view.CellPosition;
 import simulation.view.Position;
 import simulation.view.order.FifoOrderView;
 import simulation.view.order.OrderI;
 
 /**
  * Clase que representa los datos del simulador :
  * 
  * - Lista de flechas
  * 
  * -
  */
 @SuppressWarnings("serial")
 public class SimulationModel extends Observable implements Serializable {
 
 	/*
 	 * Constantes por defecto
 	 */
 	public static final int DEFAULT_NUM_PROCESSES = 4;
 	public static final int DEFAULT_NUM_TICKS = 14;
 
 	public static final boolean ADD_DEBUG = false;
 	public static final boolean REMOVE_DEBUG = false;
 
 	/*
 	 * Atributos
 	 */
 	private int numTicks = DEFAULT_NUM_TICKS;
 
 	// Lista de cortes (usando un bitset, para evitar usar un array de
 	// booleanos. Es dinamico e internamente permite usar operaciones a nivel de
 	// bit).
 	private BitSet cutList = new BitSet();
 
 	// Flechas
 	// Lista de flechas
 	private ArrayList<MultipleArrow> arrowList = new ArrayList<MultipleArrow>();
 
 	// Matriz de flechas, donde CellPosition almacena (Proceso,Tick)
 	private HashMap<CellPosition, MultipleArrow> arrowMatrix = new HashMap<CellPosition, MultipleArrow>();
 
 	// Lista de procesos/usuarios
 	private ArrayList<User> listaProcesos = new ArrayList<User>();
 
 	// capa que se encarga de la ordenacion
 	public OrderI fifo = new FifoOrderView(this);
 
 	/**
 	 * Construimos el objeto de datos de simulacion
 	 */
 	public SimulationModel() {
 		setNumProcesses(DEFAULT_NUM_PROCESSES);
 	}
 
 	/**
 	 * Rutina ayudante para setNumProcesses y setTimeTicks. Busca en las
 	 * flechas, el proceso más lejano desde el cual salga o llegue una flecha, y
 	 * el tick más lejano hasta donde llegue una flecha.
 	 * 
 	 * @return
 	 */
 	private CellPosition lastArrow() {
 
 		CellPosition last = new CellPosition(0, 0);
 
 		for (MultipleArrow arrow : arrowList) {
 			for (CellPosition arrowCellPosition : arrow.getFinalPos()) {
 
 				if (last.tick < arrowCellPosition.tick)
 					last.tick = arrowCellPosition.tick;
 
 				if (last.process < arrowCellPosition.process)
 					last.process = arrowCellPosition.process;
 
 				arrowCellPosition = arrow.getInitialPos();
 				if (last.process < arrowCellPosition.process)
 					last.process = arrowCellPosition.process;
 			}
 		}
 		return last;
 	}
 
 	/*
 	 * Número de procesos
 	 */
 
 	/**
 	 * @return Obtenemos el número de procesos
 	 */
 	public int getNumProcesses() {
 		return listaProcesos.size();
 	}
 
 	/**
 	 * 
 	 * @param pNumProcesses
 	 * 
 	 * @return Establecemos un nuevo número de procesos
 	 */
 	public int setNumProcesses(int pNumProcesses) {
 
 		int numProcesses = pNumProcesses - getNumProcesses();
 
 		// Si hay que añadir nuevos procesos :
 		if (numProcesses > 0) {
 			for (int i = getNumProcesses(); i < pNumProcesses; i++)
 				listaProcesos.add(new User(null));
 
 			// se pide que se recalculen los vectores
 			fifo.recalculateVectors(-1);
 
 			this.setChanged();
 			this.notifyObservers();
 
 		} // Si hay que eliminar nuevos procesos
 		else if (numProcesses < 0) {
 
 			/*
 			 * Para no borrar ningun proceso que tenga una flecha buscamos el
 			 * último proceso con una flecha y comprobamos que no estamos
 			 * intentando eliminarlo.
 			 */
			pNumProcesses = Math.max(pNumProcesses, lastArrow().process);
 
 			for (int i = getNumProcesses(); i > pNumProcesses; i--) {
 				arrowMatrix.remove(i);
				listaProcesos.remove(i-1);
 			}
 			super.setChanged();
 			this.notifyObservers();
 		}
 		return getNumProcesses();
 	}
 
 	/**
 	 * Obtener el número de ticks del canvas
 	 * 
 	 * @return
 	 */
 	public int getTimeTicks() {
 		return this.numTicks;
 	}
 
 	/**
 	 * Establecer el número de ticks
 	 * 
 	 * @param pTimeTicks
 	 * @return
 	 */
 	public int setTimeTicks(int pTimeTicks) {
 
 		if (pTimeTicks > numTicks) {
 			this.numTicks = pTimeTicks;
 
 			super.setChanged();
 			this.notifyObservers();
 
 		} else if (pTimeTicks < numTicks) {
 			this.cutList.clear(pTimeTicks + 1, numTicks);
 			/*
 			 * Para no borrar ningun proceso que tenga una flecha buscamos el
 			 * último tick con una flecha y comprobamos que no estamos
 			 * intentando eliminarlo.
 			 */
 			this.numTicks = Math.max(pTimeTicks, lastArrow().tick + 1);
 			super.setChanged();
 			this.notifyObservers();
 		}
 		return numTicks;
 	}
 
 	/**
 	 * 
 	 * @param tick
 	 * 
 	 * @return Si el tick es un corte o no.
 	 */
 	public boolean isCut(int tick) {
 		return cutList.get(tick);
 	}
 
 	/**
 	 * @return El listado de flechas
 	 */
 	public synchronized List<MultipleArrow> getArrowList() {
 		return arrowList;
 	}
 
 	/**
 	 * 
 	 * @param messageArrow
 	 *            Añadimos esta fecla
 	 */
 	public synchronized void addArrow(MessageArrow messageArrow) {
 
 		// Si la flecha es una MultipleArrow
 		if (messageArrow instanceof MultipleArrow) {
 
 			// FIXME lo ideal sería no tener que añadir una a una cada flecha
 			// :-P
 
 			// Añadir una a una cada SingleArrow
 			MultipleArrow mArrow = (MultipleArrow) messageArrow;
 			for (CellPosition pos : mArrow.getFinalPos())
 				addArrow(mArrow.getArrow(pos));
 
 			super.setChanged();
 			this.notifyObservers();
 		}
 		// Si es una SingleArrow llamar al método con un cast
 		else if (messageArrow instanceof SingleArrow)
 			addArrow((SingleArrow) messageArrow);
 	}
 
 	/**
 	 * 
 	 * @param messageArrow
 	 *            Añadimos esta fecla
 	 */
 	public synchronized void addArrow(SingleArrow messageArrow) {
 		boolean correctness = true;
 
 		CellPosition initialPos = messageArrow.getInitialPos();
 		CellPosition finalPos = messageArrow.getFinalPos();
 
 		MultipleArrow arrow = getMultipleArrow(initialPos);
 
 		// Si no existe el MultipleArrow, lo creamos y añadimos la flecha
 		if (arrow == null) {
 
 			arrow = new MultipleArrow(initialPos, messageArrow);
 
 			// Añadimos la flecha al comienzo y a el final
 			arrowMatrix.put(initialPos, arrow);
 			arrowMatrix.put(finalPos, arrow);
 
 			arrowList.add(arrow);
 
 			if (ADD_DEBUG) {
 				System.out.println();
 				System.out.println("addMultipleArrow:" + arrow);
 				System.out.println("estado flechas:" + arrowList);
 				System.out.println();
 				System.out.println("estado:" + arrowMatrix);
 			}
 			// FIXME
 			// se introduce el correspondiente vector logico
 			// correctness = fifo.addLogicalOrder(messageArrow, false);
 
 		} // Añadimos la flecha
 		else {
 			CellPosition removeArrow = arrow.addArrow(messageArrow);
 
 			// Si al añadir eliminamos una flecha que va al mismo proceso
 			if (removeArrow != null) {
 				arrowMatrix.remove(removeArrow);
 
 				// FIXME
 				// fifo.removeOnlyLogicalOrder(removeArrow);
 			}
 
 			// Añadimos la flecha al final
 			arrowMatrix.put(finalPos, arrow);
 
 			if (ADD_DEBUG) {
 				System.out.println();
 				System.out.println("addArrow:" + messageArrow);
 				System.out.println("estado MultipleArrow:" + arrow);
 				System.out.println("estado flechas:" + arrowList);
 				System.out.println();
 				System.out.println("estado:" + arrowMatrix);
 			}
 
 			// se introduce el correspondiente vector logico
 			// correctness = fifo.addLogicalOrder(messageArrow, true);
 		}
 
 		// FIXME
 		// si no es correcto de acuerdo al orden actual se borra
 		// if (correctness == false) {
 		// deleteArrow(finalPos);
 		// }
 
 		super.setChanged();
 		this.notifyObservers();
 	}
 
 	public synchronized MultipleArrow getMultipleArrow(CellPosition position) {
 		return this.arrowMatrix.get(position);
 	}
 
 	public synchronized MessageArrow getArrow(Position position) {
 
 		if (!(position instanceof CellPosition))
 			return null;
 
 		CellPosition pos = (CellPosition) position;
 
 		MultipleArrow arrow = getMultipleArrow(pos);
 
 		// Si no existe la flecha devolvemos null
 		if (arrow == null)
 			return null;
 
 		// Si la posicion era la flecha inicial devolvemos todo el
 		// MultipleArrow
 		if (arrow.getInitialPos().equals(pos))
 			return arrow;
 
 		// Sino devolvemos la SingleArrow del destino
 		else
 			return arrow.getArrow(pos);
 	}
 
 	/**
 	 * 
 	 * Borramos una flecha de la posicion position. Si la posición es el origen
 	 * de la flecha multiple se borra toda la flecha, en cambio si la posición
 	 * es el destino de alguna de las flechas, se borra de la flecha multiple
 	 * esa flecha.
 	 * 
 	 * @param position
 	 */
 	public synchronized MessageArrow deleteArrow(CellPosition position) {
 		MessageArrow arrow;
 
 		MultipleArrow multipleArrow = arrowMatrix.remove(position);
 
 		if (multipleArrow == null)
 			return null;
 
 		// Si la posicion es la posicion inicial debemos borrar además
 		// las referencias desde los nodos finales.
 		if (multipleArrow.getInitialPos().equals(position)) {
 
 			for (CellPosition pos : multipleArrow.getFinalPos()) {
 				arrowMatrix.remove(pos);
 
 				// se borran los relojes correspondientes
 				// FIXME
 				// System.out.println("por aqui no paso");
 				// fifo.removeLogicalOrder(pos);
 			}
 
 			if (REMOVE_DEBUG) {
 				System.out.println();
 				System.out.println("deleteArrow (inicial):" + position);
 				System.out.println();
 				System.out.println("estado:" + arrowMatrix);
 			}
 
 			arrow = multipleArrow;
 
 			arrowList.remove(multipleArrow);
 		}
 		// Si la posicion es la posicion de destino de una flecha entonces
 		// eliminamos dicha flecha de la MultipleArrow
 		else {
 			arrow = multipleArrow.deleteArrow(position);
 
 			// Si hemos borrado la ultima flecha del grupo, borrar también
 			// el MultiArrow
 			if (multipleArrow.getFinalPos().size() == 0) {
 				arrowMatrix.remove(multipleArrow.getInitialPos());
 				arrowList.remove(multipleArrow);
 			}
 
 			if (REMOVE_DEBUG) {
 				System.out.println();
 				System.out.println("deleteArrow (final):" + position);
 				System.out.println();
 				System.out.println("estado:" + arrowMatrix);
 			}
 
 			// se borran los relojes correspondientes
 			// FIXME
 			// System.out.println("eliminando");
 			// fifo.removeOnlyLogicalOrder(position);
 		}
 		super.setChanged();
 		this.notifyObservers();
 		return arrow;
 	}
 }
