 package rinde.evo4mas.gendreau06;
 
 import static com.google.common.collect.Sets.newLinkedHashSet;
 
 import java.util.Collection;
 import java.util.Set;
 
 import rinde.ecj.Heuristic;
 import rinde.evo4mas.common.TruckContext;
 import rinde.sim.core.TimeLapse;
 import rinde.sim.core.graph.Point;
 import rinde.sim.core.model.pdp.PDPModel;
 import rinde.sim.core.model.pdp.PDPModel.ParcelState;
 import rinde.sim.core.model.pdp.Parcel;
 import rinde.sim.core.model.road.RoadModel;
 import rinde.sim.event.Event;
 import rinde.sim.event.Listener;
 import rinde.sim.problem.common.DefaultParcel;
 import rinde.sim.problem.common.DefaultVehicle;
 import rinde.sim.problem.common.ParcelDTO;
 import rinde.sim.problem.common.VehicleDTO;
 import rinde.sim.util.fsm.StateMachine;
 import rinde.sim.util.fsm.StateMachine.State;
 
 public class HeuristicTruck extends DefaultVehicle implements Listener {
 
 	protected final Heuristic<TruckContext> program;
 	protected boolean hasChanged = true;
 	protected Parcel currentTarget;
 
 	protected CoordinationModel coordinationModel;
 
 	protected final StateMachine<TruckEvent, HeuristicTruck> stateMachine;
 
 	enum TruckEvent {
 		CHANGE, END_OF_DAY, READY, ARRIVE, DONE, CONTINUE, YES, NO;
 	}
 
 	public HeuristicTruck(VehicleDTO pDto, Heuristic<TruckContext> p) {
 		super(pDto);
 		program = p;
 
 		stateMachine = StateMachine.create(TruckState.IDLE)/* */
 		.addTransition(TruckState.IDLE, TruckEvent.CHANGE, TruckState.HAS_TARGET) /* */
 		.addTransition(TruckState.IDLE, TruckEvent.END_OF_DAY, TruckState.GO_HOME) /* */
 		.addTransition(TruckState.HAS_TARGET, TruckEvent.YES, TruckState.IS_EARLY) /* */
 		.addTransition(TruckState.HAS_TARGET, TruckEvent.NO, TruckState.IS_END_OF_DAY) /* */
 		.addTransition(TruckState.IS_END_OF_DAY, TruckEvent.YES, TruckState.GO_HOME) /* */
 		.addTransition(TruckState.IS_END_OF_DAY, TruckEvent.NO, TruckState.IDLE) /* */
 		.addTransition(TruckState.IS_EARLY, TruckEvent.YES, TruckState.EARLY_TARGET) /* */
 		.addTransition(TruckState.IS_EARLY, TruckEvent.NO, TruckState.IS_IN_CARGO) /* */
 		.addTransition(TruckState.IS_IN_CARGO, TruckEvent.YES, TruckState.GOTO_DELIVERY) /* */
 		.addTransition(TruckState.IS_IN_CARGO, TruckEvent.NO, TruckState.GOTO_PICKUP) /* */
 		.addTransition(TruckState.EARLY_TARGET, TruckEvent.CHANGE, TruckState.HAS_TARGET) /* */
 		.addTransition(TruckState.EARLY_TARGET, TruckEvent.READY, TruckState.HAS_TARGET) /* */
 		.addTransition(TruckState.GOTO_DELIVERY, TruckEvent.ARRIVE, TruckState.DELIVER) /* */
 		.addTransition(TruckState.GOTO_PICKUP, TruckEvent.ARRIVE, TruckState.PICKUP) /* */
 		.addTransition(TruckState.DELIVER, TruckEvent.DONE, TruckState.HAS_TARGET) /* */
 		.addTransition(TruckState.PICKUP, TruckEvent.DONE, TruckState.HAS_TARGET) /* */
 		.addTransition(TruckState.GO_HOME, TruckEvent.CHANGE, TruckState.HAS_TARGET) /* */
 		.addTransition(TruckState.GO_HOME, TruckEvent.ARRIVE, TruckState.IDLE) /* */
 		.build();
 
 		// System.out.println(stateMachine.toDot());
 
 	}
 
 	protected Parcel next(long time) {
 		return next(program, dto, pdpModel.getParcels(ParcelState.ANNOUNCED, ParcelState.AVAILABLE), pdpModel.getContents(this), coordinationModel
 				.getClaims(), roadModel.getPosition(this), time);
 	}
 
 	protected static Parcel next(Heuristic<TruckContext> program, VehicleDTO dto, Collection<Parcel> todo,
 			Collection<Parcel> contents, Set<Parcel> alreadyClaimed, Point truckPos, long time) {
 		Parcel best = null;
 		double bestValue = Double.POSITIVE_INFINITY;
 
 		boolean isPickup = true;
 
 		final Collection<ParcelDTO> contentDTOs = convert(contents);
 		for (final Parcel p : todo) {
 			// filter out the already claimed parcels
 			if (!alreadyClaimed.contains(p)) {
 				final double v = program.compute(new TruckContext(dto, truckPos, contentDTOs, ((DefaultParcel) p).dto,
 						time, false));
				if (v < bestValue) {
 					best = p;
 					bestValue = v;
 				}
 			}
 		}
 		for (final Parcel p : contents) {
 			final double v = program.compute(new TruckContext(dto, truckPos, contentDTOs, ((DefaultParcel) p).dto,
 					time, true));
			if (v < bestValue) {
 				best = p;
 				bestValue = v;
 				isPickup = false;
 			}
 		}
 		return best;
 	}
 
 	protected boolean isTooEarly(Parcel p, Point truckPos, long time) {
 		final boolean isPickup = pdpModel.getParcelState(currentTarget) == ParcelState.IN_CARGO;
 		final Point loc = isPickup ? ((DefaultParcel) p).dto.pickupLocation : p.getDestination();
 		final long travelTime = (long) ((Point.distance(loc, truckPos) / 30d) * 3600000d);
 		final long timeToBegin = (isPickup ? p.getPickupTimeWindow().begin : p.getDeliveryTimeWindow().begin) - time;
 		return timeToBegin >= 0 && timeToBegin - travelTime > 1000;
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
 		if (time.hasTimeLeft() && !stateMachine.stateIsOneOf(TruckState.IDLE, TruckState.EARLY_TARGET)) {
 			System.err.println(time.getTimeLeft() + " not used");
 		}
 	}
 
 	protected boolean isEndOfDay(TimeLapse time) {
 		return time.hasTimeLeft()
 				&& time.getTime() > dto.availabilityTimeWindow.end
 						- ((Point.distance(roadModel.getPosition(this), dto.startPosition) / getSpeed()) * 3600000);
 	}
 
 	enum TruckState implements State<TruckEvent, HeuristicTruck> {
 		IDLE {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				return null;
 			}
 		},
 		HAS_TARGET {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				context.currentTarget = context.next(context.currentTime.getTime());
 				return context.currentTarget != null ? TruckEvent.YES : TruckEvent.NO;
 			}
 		},
 		IS_END_OF_DAY {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				if (context.isEndOfDay(context.currentTime)) {
 					return TruckEvent.YES;
 				} else {
 					return TruckEvent.NO;
 				}
 			}
 		},
 		IS_EARLY {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				return context
 						.isTooEarly(context.currentTarget, context.roadModel.getPosition(context), context.currentTime
 								.getTime()) ? TruckEvent.YES : TruckEvent.NO;
 			}
 		},
 		IS_IN_CARGO {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				return context.pdpModel.getParcelState(context.currentTarget) == ParcelState.IN_CARGO ? TruckEvent.YES
 						: TruckEvent.NO;
 			}
 		},
 		// DECIDE {
 		// @Override
 		// public TruckEvent handle(TruckEvent event, Truck context) {
 		// context.currentTarget = context.next(context.currentTime.getTime());
 		// if (context.currentTarget == null) {
 		// if (context.isEndOfDay(context.currentTime)) {
 		// return TruckEvent.END_OF_DAY;
 		// } else {
 		// return TruckEvent.NO_TARGET;
 		// }
 		// } else if (context
 		// .isTooEarly(context.currentTarget,
 		// context.roadModel.getPosition(context), context.currentTime
 		// .getTime())) {
 		// return TruckEvent.EARLY;
 		// } else if (context.pdpModel.getParcelState(context.currentTarget) ==
 		// ParcelState.IN_CARGO) {
 		// return TruckEvent.IN_CARGO;
 		// } else {
 		// return TruckEvent.IS_AVAILABLE;
 		// }
 		// }
 		// },
 		EARLY_TARGET {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				if (!context
 						.isTooEarly(context.currentTarget, context.roadModel.getPosition(context.truck), context.currentTime
 								.getTime())) {
 					return TruckEvent.READY;
 				}
 				return null;
 			}
 		},
 		GOTO_DELIVERY {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				context.roadModel.moveTo(context.truck, context.currentTarget.getDestination(), context.currentTime);
 				if (context.roadModel.getPosition(context.truck).equals(context.currentTarget.getDestination())) {
 					return TruckEvent.ARRIVE;
 				}
 				return null;
 			}
 
 		},
 		DELIVER {
 			@Override
 			public void onEntry(TruckEvent event, HeuristicTruck context) {
 				context.pdpModel.deliver(context.truck, context.currentTarget, context.currentTime);
 			}
 
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				if (context.currentTime.hasTimeLeft()) {
 					return TruckEvent.DONE;
 				}
 				return null;
 			}
 		},
 		GOTO_PICKUP {
 			@Override
 			public void onEntry(TruckEvent event, HeuristicTruck context) {
 				context.coordinationModel.claim(context.currentTarget);
 			}
 
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				context.roadModel.moveTo(context.truck, context.currentTarget, context.currentTime);
 				if (context.roadModel.equalPosition(context.truck, context.currentTarget)) {
 					return TruckEvent.ARRIVE;
 				}
 				return null;
 			}
 		},
 		PICKUP {
 			@Override
 			public void onEntry(TruckEvent event, HeuristicTruck context) {
 				context.pdpModel.pickup(context.truck, context.currentTarget, context.currentTime);
 			}
 
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				if (context.currentTime.hasTimeLeft()) {
 					return TruckEvent.DONE;
 				}
 				return null;
 			}
 		},
 		GO_HOME {
 			@Override
 			public TruckEvent handle(TruckEvent event, HeuristicTruck context) {
 				context.roadModel.moveTo(context.truck, context.dto.startPosition, context.currentTime);
 				if (context.roadModel.getPosition(context.truck).equals(context.dto.startPosition)) {
 					return TruckEvent.ARRIVE;
 				}
 				return null;
 			}
 		};
 
 		public abstract TruckEvent handle(TruckEvent event, HeuristicTruck context);
 
 		public void onEntry(TruckEvent event, HeuristicTruck context) {}
 
 		public void onExit(TruckEvent event, HeuristicTruck context) {}
 
 	}
 
 	public void setCoordinationModel(CoordinationModel cm) {
 		coordinationModel = cm;
 	}
 
 	public Heuristic<TruckContext> getProgram() {
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
