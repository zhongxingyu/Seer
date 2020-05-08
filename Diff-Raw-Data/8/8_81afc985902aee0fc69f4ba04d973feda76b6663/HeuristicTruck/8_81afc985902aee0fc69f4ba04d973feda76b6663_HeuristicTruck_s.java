 package rinde.evo4mas.gendreau06;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Sets.newLinkedHashSet;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import rinde.ecj.Heuristic;
 import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
 import rinde.sim.core.TimeLapse;
 import rinde.sim.core.graph.Point;
 import rinde.sim.core.model.pdp.PDPModel;
 import rinde.sim.core.model.pdp.PDPModel.ParcelState;
 import rinde.sim.core.model.pdp.Parcel;
 import rinde.sim.core.model.pdp.Vehicle;
 import rinde.sim.core.model.road.RoadModel;
 import rinde.sim.event.Event;
 import rinde.sim.event.Listener;
 import rinde.sim.problem.common.DefaultParcel;
 import rinde.sim.problem.common.DefaultVehicle;
 import rinde.sim.problem.common.ParcelDTO;
 import rinde.sim.problem.common.VehicleDTO;
 import rinde.sim.util.fsm.StateMachine;
 import rinde.sim.util.fsm.StateMachine.State;
 
 public abstract class HeuristicTruck extends DefaultVehicle implements Listener {
 
 	protected final Heuristic<GendreauContext> program;
 	protected final TimeUntilAvailable<GendreauContext> tua;
 	protected boolean hasChanged = true;
 	protected Parcel currentTarget;
 
 	protected final StateMachine<TruckEvent, HeuristicTruck> stateMachine;
 
 	public enum TruckEvent {
 		CHANGE, END_OF_DAY, READY, ARRIVE, DONE, CONTINUE, YES, NO;
 	}
 
 	protected HeuristicTruck(VehicleDTO pDto, Heuristic<GendreauContext> p, StateMachine<TruckEvent, HeuristicTruck> sm) {
 		super(pDto);
 		program = p;
 		stateMachine = sm;
 		tua = new TimeUntilAvailable<GendreauContext>();
 	}
 
 	static StateMachine<TruckEvent, HeuristicTruck> createStateMachine(State<TruckEvent, HeuristicTruck> earlyTarget,
 			State<TruckEvent, HeuristicTruck> gotoPickup, State<TruckEvent, HeuristicTruck> pickup) {
 
 		final State<TruckEvent, HeuristicTruck> idle = new Idle();
 		final State<TruckEvent, HeuristicTruck> hasTarget = new HasTarget();
 		final State<TruckEvent, HeuristicTruck> isEarly = new IsEarly();
 		final State<TruckEvent, HeuristicTruck> isInCargo = new IsInCargo();
 		final State<TruckEvent, HeuristicTruck> gotoDelivery = new GotoDelivery();
 		final State<TruckEvent, HeuristicTruck> deliver = new Deliver();
 		final State<TruckEvent, HeuristicTruck> goHome = new GoHome();
 
 		return StateMachine.create(idle)/* */
 		.addTransition(idle, TruckEvent.CHANGE, hasTarget) /* */
 		.addTransition(idle, TruckEvent.END_OF_DAY, goHome) /* */
 		.addTransition(hasTarget, TruckEvent.YES, isEarly) /* */
 		.addTransition(hasTarget, TruckEvent.NO, idle) /* */
 		.addTransition(isEarly, TruckEvent.YES, earlyTarget) /* */
 		.addTransition(isEarly, TruckEvent.NO, isInCargo) /* */
 		.addTransition(isInCargo, TruckEvent.YES, gotoDelivery) /* */
 		.addTransition(isInCargo, TruckEvent.NO, gotoPickup) /* */
 		.addTransition(earlyTarget, TruckEvent.CHANGE, hasTarget) /* */
 		.addTransition(earlyTarget, TruckEvent.READY, hasTarget) /* */
 		.addTransition(gotoDelivery, TruckEvent.ARRIVE, deliver) /* */
 		.addTransition(gotoPickup, TruckEvent.ARRIVE, pickup) /* */
 		.addTransition(deliver, TruckEvent.DONE, hasTarget) /* */
 		.addTransition(pickup, TruckEvent.DONE, hasTarget) /* */
 		.addTransition(goHome, TruckEvent.CHANGE, hasTarget) /* */
 		.addTransition(goHome, TruckEvent.ARRIVE, idle) /* */
 		.build();
 	}
 
 	// uses a generic context obj to create a parcel specific context
 	protected GendreauContext createContext(GendreauContext gc, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
 		return new GendreauContext(gc.vehicleDTO, gc.truckPosition, gc.truckContents, ((DefaultParcel) p).dto, gc.time,
 				isInCargo, isAssignedToVehicle, 0, gc.otherVehiclePositions, gc.todoList);
 
 	}
 
 	protected GendreauContext createGenericContext(long time) {
 		final Collection<Parcel> contents = pdpModel.getContents(this);
 		final List<Point> positions = newArrayList();
 		final Set<Vehicle> vehicles = pdpModel.getVehicles();
 		for (final Vehicle v : vehicles) {
 			if (v != this) {
 				positions.add(roadModel.getPosition(v));
 			}
 		}
 		return new GendreauContext(dto, roadModel.getPosition(this), convert(contents), null, time, false, false, -1,
 				positions, new HashSet<Parcel>());
 	}
 
 	protected GendreauContext createFullContext(long time, Parcel p, boolean isInCargo, boolean isAssignedToVehicle) {
 		return createContext(createGenericContext(time), p, isInCargo, isAssignedToVehicle);
 	}
 
 	protected abstract Parcel next(long time);
 
 	protected boolean isTooEarly(Parcel p, Point truckPos, TimeLapse time) {
 		final boolean isPickup = pdpModel.getParcelState(p) != ParcelState.IN_CARGO;
 		final Point loc = isPickup ? ((DefaultParcel) p).dto.pickupLocation : p.getDestination();
 		final long travelTime = (long) ((Point.distance(loc, truckPos) / 30d) * 3600000d);
 		long timeUntilAvailable = (isPickup ? p.getPickupTimeWindow().begin : p.getDeliveryTimeWindow().begin)
 				- time.getStartTime();
 
 		final long remainder = timeUntilAvailable % time.getTimeStep();
 		if (remainder > 0) {
 			timeUntilAvailable += time.getTimeStep() - remainder;
 		}
 		return timeUntilAvailable - travelTime > 0;
 	}
 
 	TimeLapse currentTime;
 	HeuristicTruck truck;
 
 	@Override
 	protected void tickImpl(TimeLapse time) {
 		currentTime = time;
 		truck = this;
 
 		if (hasChanged && stateMachine.isSupported(TruckEvent.CHANGE)) {
 			stateMachine.handle(TruckEvent.CHANGE, this);
 			hasChanged = false;
 		} else {
 			stateMachine.handle(this);
 		}
 		// if (time.hasTimeLeft() && !stateMachine.stateIsOneOf(TruckState.IDLE,
 		// TruckState.EARLY_TARGET)) {
 		// System.err.println(time.getTimeLeft() + " not used");
 		// }
 	}
 
 	@Override
 	protected RoadModel getRoadModel() {
 		return roadModel;
 	}
 
 	protected boolean isEndOfDay(TimeLapse time) {
 		return time.hasTimeLeft()
 				&& time.getTime() > dto.availabilityTimeWindow.end
 						- ((Point.distance(roadModel.getPosition(this), dto.startPosition) / getSpeed()) * 3600000);
 	}
 
 	public static abstract class AbstractTruckState implements State<TruckEvent, HeuristicTruck> {
 
 		public void onEntry(TruckEvent event, HeuristicTruck context) {}
 
 		public void onExit(TruckEvent event, HeuristicTruck context) {}
 
 		public String name() {
 			return this.getClass().getSimpleName();
 		}
 	}
 
 	public static class Idle extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			if (context.isEndOfDay(context.currentTime)
 					&& !context.roadModel.getPosition(context).equals(context.dto.startPosition)) {
 				return TruckEvent.END_OF_DAY;
 			}
 			return null;
 		}
 	}
 
 	public static class HasTarget extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			context.currentTarget = context.next(context.currentTime.getTime());
 			return context.currentTarget != null ? TruckEvent.YES : TruckEvent.NO;
 		}
 	}
 
 	public static class IsEarly extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			return context
 					.isTooEarly(context.currentTarget, context.roadModel.getPosition(context), context.currentTime) ? TruckEvent.YES
 					: TruckEvent.NO;
 		}
 	}
 
 	public static class EarlyTarget extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			if (!context
 					.isTooEarly(context.currentTarget, context.getRoadModel().getPosition(context.truck), context.currentTime)) {
 				return TruckEvent.READY;
 			}
 			return null;
 		}
 	}
 
 	public static class IsInCargo extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			return context.pdpModel.getParcelState(context.currentTarget) == ParcelState.IN_CARGO ? TruckEvent.YES
 					: TruckEvent.NO;
 		}
 	}
 
 	public static class GotoDelivery extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			context.roadModel.moveTo(context.truck, context.currentTarget.getDestination(), context.currentTime);
 			if (context.roadModel.getPosition(context.truck).equals(context.currentTarget.getDestination())) {
 				return TruckEvent.ARRIVE;
 			}
 			return null;
 		}
 	}
 
 	public static class Deliver extends AbstractTruckState {
 		@Override
 		public void onEntry(TruckEvent event, HeuristicTruck context) {
 			context.pdpModel.deliver(context.truck, context.currentTarget, context.currentTime);
 		}
 
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			if (context.currentTime.hasTimeLeft()) {
 				return TruckEvent.DONE;
 			}
 			return null;
 		}
 	}
 
 	public static class GotoPickup extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			context.getRoadModel().moveTo(context.truck, context.currentTarget, context.currentTime);
 			if (context.getRoadModel().equalPosition(context.truck, context.currentTarget)) {
 				return TruckEvent.ARRIVE;
 			}
 			return null;
 		}
 	}
 
 	public static class Pickup extends AbstractTruckState {
 		@Override
 		public void onEntry(TruckEvent event, HeuristicTruck context) {
			context.pdpModel.pickup(context.truck, context.currentTarget, context.currentTime);
 		}
 
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			if (context.currentTime.hasTimeLeft()) {
 				return TruckEvent.DONE;
 			}
 			return null;
 		}
 	}
 
 	public static class GoHome extends AbstractTruckState {
 		public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 			context.roadModel.moveTo(context.truck, context.dto.startPosition, context.currentTime);
 			if (context.roadModel.getPosition(context.truck).equals(context.dto.startPosition)) {
 				return TruckEvent.ARRIVE;
 			}
 			return null;
 		}
 	}
 
 	public Heuristic<GendreauContext> getProgram() {
 		return program;
 	}
 
 	@Override
 	public void initRoadPDP(RoadModel rm, PDPModel pdp) {
 		super.initRoadPDP(rm, pdp);
 		pdpModel.getEventAPI().addListener(this, PDPModel.PDPModelEventType.NEW_PARCEL);
 	}
 
 	static Set<ParcelDTO> convert(Collection<Parcel> parcels) {
 		final Set<ParcelDTO> dtos = newLinkedHashSet();
 		for (final Parcel p : parcels) {
 			dtos.add(((DefaultParcel) p).dto);
 		}
 		return dtos;
 	}
 
 	public void handleEvent(Event e) {
 		hasChanged = true;
 	}
 }
