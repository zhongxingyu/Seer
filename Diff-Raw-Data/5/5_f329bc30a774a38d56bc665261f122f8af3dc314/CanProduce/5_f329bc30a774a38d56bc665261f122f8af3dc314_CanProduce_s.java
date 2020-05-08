 package vooga.rts.gamedesign.strategy.production;
 
 import vooga.rts.action.InteractiveAction;
 import vooga.rts.commands.Command;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.state.ProducingState;
 import vooga.rts.manager.IndividualResourceManager;
 import vooga.rts.state.GameState;
 import vooga.rts.util.DelayedTask;
import vooga.rts.util.Location3D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class implements ProductionStrategy and is used as an instance in
  * interactives for objects that are able to produce other interactives. The
  * produce method in this class will specify how the interactive will produce
  * other units.
  * 
  * @author Kevin Oh
  * 
  */
 public class CanProduce implements ProductionStrategy {
 
 	private List<InteractiveEntity> myProducables;
 	private ProducingState myProduceState;
 
 	/**
 	 * Creates a new production strategy that represents an entity that can
 	 * produce other entities. It is created with a list of entities that it can
 	 * produce and a rally point (where all the units created by this entity
 	 * will go).
 	 */
 	public CanProduce(InteractiveEntity entity) {
 		myProducables = new ArrayList<InteractiveEntity>();
 		myProduceState = ProducingState.NOT_PRODUCING;
 	}
 
 	/**
 	 * Adds an interactive entity that can be produced to the list of this
 	 * entities producables.
 	 * 
 	 * @param producable
 	 *            is an entity that this production entity can create
 	 */
 	public void addProducable(InteractiveEntity producable) {
 		myProducables.add(producable);
 
 	}
 
 	@Override
 	public void createProductionActions(final InteractiveEntity producer) {
 		for (final InteractiveEntity producable : myProducables) {
 			String commandName = "make " + producable.getInfo().getName();
 			final Map<String, Integer> costMap = producable.getInfo().getCost();
 			producer.addAction(commandName, new InteractiveAction(producer) {
 				@Override
 				public void update(Command command) {
 				}
 
 				@Override
 				public void apply() {
 					IndividualResourceManager playerResources = GameState
 							.getPlayers().getPlayer(producer.getPlayerID())
 							.getResources();
 					if (playerResources.has(costMap)) {
 						playerResources.charge(costMap);
						System.out.println("HAVE "
								+ playerResources.getResources());

 						final InteractiveEntity unit = producable;
 
 						myProduceState = ProducingState.PRODUCING;
 						DelayedTask dt = new DelayedTask(unit.getBuildTime(),
 								new Runnable() {
 									@Override
 									public void run() {
 										InteractiveEntity f = unit.copy();
 										f.setWorldLocation(producer
 												.getWorldLocation());
 										producer.setChanged();
 										producer.notifyObservers(f);
 
 										myProduceState = ProducingState.NOT_PRODUCING;
 										f.move(producer.getRallyPoint());
 
 									}
 								});
 
 						producer.addQueueableTask(dt);
 
 					}
 
 				}
 			});
 			producer.addActionInfo(commandName, producable.getInfo());
 		}
 	}
 
 	@Override
 	public ProducingState getProducingState() {
 		return myProduceState;
 	}
 
 	@Override
 	public List<InteractiveEntity> getProducables() {
 		return myProducables;
 	}
 
 	@Override
 	public void setProducables(List<InteractiveEntity> producables) {
 		myProducables = producables;
 	}
 
 	/**
 	 * Copies the canProduce strategy to the entity
 	 */
 	public void copyStrategy(InteractiveEntity entity) {
 		ProductionStrategy newProduction = new CanProduce(entity);
 		newProduction.setProducables(myProducables);
 		newProduction.createProductionActions(entity);
 		entity.setProductionStrategy(newProduction);
 	}
 }
