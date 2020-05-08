 package simulation.order_dinamic.arrows;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import simulation.arrows.MessageArrow;
 import simulation.arrows.MultipleArrow;
 import simulation.arrows.SingleArrow;
 import simulation.model.SimulationArrowModel;
 import simulation.view.CellPosition;
 
 public class TotalArrow extends MultipleArrow implements Serializable, Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	/*
 	 * Flechas para cada una de las fases
 	 */
 	public class SendArrow extends SingleArrow {
 		private static final long serialVersionUID = 1L;
 
 		public SendArrow(CellPosition initialPos, CellPosition finalPos) {
 			super(initialPos, finalPos);
 		}
 	}
 
 	public class ProposalArrow extends SingleArrow {
 		private static final long serialVersionUID = 1L;
 
 		public ProposalArrow(CellPosition initialPos, CellPosition finalPos) {
 			super(initialPos, finalPos);
 		}
 	}
 
 	public class FinalArrow extends SingleArrow {
 		private static final long serialVersionUID = 1L;
 
 		public FinalArrow(CellPosition initialPos, CellPosition finalPos) {
 			super(initialPos, finalPos);
 		}
 	}
 
 	/*
 	 * Atributos
 	 */
 	private SimulationArrowModel simulationModel;
 
 	/*
 	 * Posición central, cuando todos los procesos han respondido con sus
 	 * propuestas.
 	 */
 	private CellPosition proposalPosition = new CellPosition(-1, -1);
 
 	/*
 	 * Listas de cada fase : envio, propuesta, confirmación.
 	 */
 	private List<SendArrow> sendArrows = new LinkedList<SendArrow>();
 
 	private List<ProposalArrow> proposalArrows = new LinkedList<ProposalArrow>();
 
 	private List<FinalArrow> finalArrows = new LinkedList<FinalArrow>();
 
 	private List<CellPosition> proposalPositions = new LinkedList<CellPosition>();
 
 	private CellPosition movingCell;
 
 	public TotalArrow(CellPosition initialPos,
 			SimulationArrowModel simulationModel) {
 
		super(initialPos);
 
 		// Añadimos la posición inicial a la lista de posiciones.
		this.positionList.add(initialPos);
 
 		this.simulationModel = simulationModel;
 
 		// Las flechas totales observan el SimulationModel, ya que si la
 		// aumentamos el número de procesos debemos recalcular la flecha.
 		simulationModel.addObserver(this);
 
 		recalculate();
 	}
 
 	private TotalArrow(CellPosition initialPos) {
 
 		super(initialPos);
 
 		// Añadimos la posición inicial a la lista de posiciones.
 		this.positionList.add(initialPos);
 	}
 
 	/**
 	 * Recalcular
 	 */
 	public void recalculate() {
 
 		// Creamos las flechas de envio, buscando flechas libres desde la
 		// posición actual en cada proceso.
 		CellPosition iter = initialPos.clone();
 
 		// Guardaremos el tick máximo de la última flecha de envio.
 		proposalPosition = initialPos.clone();
 
 		int size = simulationModel.getNumProcesses();
 
 		// Creamos una flecha de envio a todos.
 		for (int i = 0; i < size; i++) {
 
 			// Para cada proceso != del proceso de origen
 			if (i != initialPos.process) {
 
 				SingleArrow arrow = getArrow(sendArrows, i);
 				CellPosition finalPos;
 
 				// Comprobamos si existe una flecha de envio (1º fase)
 				if (arrow == null) {
 
 					// Obtenemos una posición libre en el proceso i
 					iter.process = i;
 					finalPos = simulationModel.freeCell(iter);
 
 					// Creamos la flecha de envio y la añadimos a la lista de :
 					// - De flechas (MultipleArrow)
 					// - De posiciones (MultipleArrow)
 					// - De flechas de envio
 					SendArrow arrow2 = new SendArrow(initialPos, finalPos);
 					this.arrowList.add(arrow2);
 					this.positionList.add(finalPos);
 					this.sendArrows.add(arrow2);
 
 					// Creamos las flechas de respuesta a las de envio
 					CellPosition resp = new CellPosition(initialPos.process,
 							finalPos.tick);
 					resp = simulationModel.freeCell(resp);
 
 					while (this.positionList.contains(resp)) {
 						resp = simulationModel.freeCell(resp);
 					}
 
 					ProposalArrow arrow3 = new ProposalArrow(finalPos, resp);
 					this.arrowList.add(arrow3);
 					this.positionList.add(resp);
 					this.proposalArrows.add(arrow3);
 					this.proposalPositions.add(resp);
 
 					finalPos = resp;
 
 				} else {
 
 					SingleArrow arrow2 = getArrow(proposalArrows, i);
 
 					if (!arrow2.isValid(simulationModel)
 							|| listContains(proposalPositions, arrow2
 									.getFinalPos())) {
 
 						// Creamos las flechas de respuesta a las de envio
 						CellPosition resp = new CellPosition(
 								initialPos.process, arrow2.getFinalPos().tick);
 						resp = simulationModel.freeCell(resp);
 
 						while (listContains(proposalPositions, resp)) {
 							resp = simulationModel.freeCell(resp);
 						}
 
 						arrow2.getFinalPos().set(resp);
 					}
 					finalPos = arrow2.getFinalPos();
 				}
 
 				// Guardamos el tick máximo
 				if (finalPos.tick > proposalPosition.tick)
 					proposalPosition = finalPos;
 			}
 		}
 
 		iter.tick = proposalPosition.tick;
 
 		// Creamos las flechas finales.
 		for (int i = 0; i < size; i++) {
 
 			// Para cada proceso != del proceso de origen
 			if (i != initialPos.process) {
 
 				SingleArrow arrow = getArrow(finalArrows, i);
 
 				CellPosition finalPos;
 
 				if (arrow == null) {
 
 					iter.process = i;
 					finalPos = simulationModel.freeCell(iter);
 
 					FinalArrow arrow2 = new FinalArrow(proposalPosition,
 							finalPos);
 
 					this.arrowList.add(arrow2);
 					this.positionList.add(finalPos);
 					this.finalArrows.add(arrow2);
 
 				} else {
 
 					SingleArrow arrow2 = getArrow(finalArrows, i);
 
 					arrow2.setInitialPos(proposalPosition);
 
 					if (!arrow2.isValid(simulationModel)) {
 
 						// Creamos las flechas de respuesta a las de envio
 						CellPosition resp = new CellPosition(i,
 								proposalPosition.tick);
 						resp = simulationModel.freeCell(resp);
 
 						arrow2.getFinalPos().set(resp);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Verificamos si se encuentra en un lugar válido y/o libre :
 	 * 
 	 * <ul>
 	 * <li>Una flecha no puede tener flechas que vayan hacia atrás.</li>
 	 * <li>Una flecha no puede apuntar a una celda ya ocupada.</li>
 	 * </ul>
 	 * 
 	 * @param messageArrow
 	 * 
 	 * @return Si es valida la flecha
 	 */
 	public boolean isValid(SimulationArrowModel simulationModel) {
 
 		if (!super.isValid(simulationModel))
 			return false;
 
 		if (moveCell != null && simulationModel.getArrow(moveCell) != null)
 			return false;
 
 		return true;
 	}
 
 	@Override
 	public boolean deleteArrow(CellPosition position) {
 		// No se puede borrar una flecha interior de una flecha Total
 		return true;
 	}
 
 	@Override
 	public void addArrow(MessageArrow arrow) {
 		// No se puede añadir una flecha a una flecha Total
 	}
 
 	@Override
 	public boolean add2Simulation(SimulationArrowModel simulationModel) {
 
 		this.movingCell = null;
 
 		simulationModel.addArrow(this);
 
 		return true;
 	}
 
 	@Override
 	public MultipleArrow clone() {
 		TotalArrow clone = new TotalArrow(this.initialPos);
 
 		clone.simulationModel = this.simulationModel;
 
 		for (SingleArrow arrow : this.arrowList)
 			clone.arrowList.add(arrow.clone());
 
 		for (CellPosition pos : this.positionList)
 			clone.positionList.add(pos.clone());
 
 		for (SendArrow arrow : this.sendArrows)
 			clone.sendArrows.add(arrow);
 
 		for (ProposalArrow arrow : this.proposalArrows)
 			clone.proposalArrows.add(arrow);
 
 		clone.proposalPosition = this.proposalPosition.clone();
 
 		for (FinalArrow arrow : this.finalArrows)
 			clone.finalArrows.add(arrow);
 
 		return clone;
 	}
 
 	@Override
 	public CellPosition move(CellPosition newPosition) {
 
 		CellPosition result = super.move(newPosition);
 
 		if (movingCell == null) {
 			movingCell = result.clone();
 		} else {
 			result.process = movingCell.process;
 			recalculate();
 		}
 		return result;
 	}
 
 	@Override
 	public Collection<CellPosition> getPositions() {
 
 		// Evitamos devolver posiciones repetidas
 		ArrayList<CellPosition> list = new ArrayList<CellPosition>();
 
 		for (CellPosition pos : this.positionList)
 			if (list.indexOf(pos) == -1)
 				list.add(pos);
 
		return list;
 	}
 
 	/**
 	 * Si cambian el número de procesos, debemos añadir nuevas flechas.
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		recalculate();
 	}
 
 	/*
 	 * Funciones ayudantes
 	 */
 	/**
 	 * @param lista
 	 * 
 	 * @param process
 	 * 
 	 * @return Obtenemos la flecha correspondiente a un proceso de una lista de
 	 *         flechas.
 	 */
 	private static SingleArrow getArrow(List<? extends SingleArrow> lista,
 			int process) {
 		for (SingleArrow arrow : lista) {
 			if (arrow instanceof SendArrow || arrow instanceof FinalArrow) {
 				if (arrow.getFinalPos().process == process)
 					return arrow;
 			} else if (arrow.getInitialPos().process == process)
 				return arrow;
 		}
 		return null;
 	}
 
 	private static boolean listContains(Collection<CellPosition> collection,
 			CellPosition cell) {
 		for (CellPosition pos : collection) {
 			if (pos.equals(cell) && pos != cell)
 				return true;
 		}
 		return false;
 	}
 }
