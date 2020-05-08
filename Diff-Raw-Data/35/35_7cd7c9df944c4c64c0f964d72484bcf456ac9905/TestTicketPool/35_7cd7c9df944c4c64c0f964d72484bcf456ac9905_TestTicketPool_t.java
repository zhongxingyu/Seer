 package org.ng12306.tpms.runtime;
 
 import java.util.*;
 
 import org.diting.collections.*;
 
 
 public class TestTicketPool extends ObjectWithSite 
     implements ITicketPool
 {
 
 	public TestTicketPool(Train train) {
 		this._train = train;
 	}
 	
 	private Train _train;
 
 	public Train getTrain() {
 		return this._train;
 	}
 	
 	private Route _route;
 
 	public Route getRoute() {
 		return this._route;
 	}
 
 	
 	private ArrayList<StopRangeGroup> _stopRangeGroups = new ArrayList<StopRangeGroup>();
 
 	private void createPlanTickets() throws Exception
 	{
 		
 		//Here we are creating plan tickets those can be free sold between any two stations. 
 		int maxStopSeq = this._route.getStops().size() - 1;
 		
 		StopRangeGroup fullRangeGroup = new StopRangeGroup();
 		StopRange stopRange = new StopRange();
 		stopRange.start = 0;
 		stopRange.end = maxStopSeq;
 		 
 		fullRangeGroup.setRange(stopRange);
 		this._stopRangeGroups.add(fullRangeGroup);
 		
 		for(OperatingSeat seat : Queries.query(this._train.getCars()).selectMany(new Selector<Car, Iterable<OperatingSeat>>(){
 
 			@Override
 			public Iterable<OperatingSeat> select(Car item) {
 				return item.getSeats();
 			}}))
 		{
 		     PlanTicket pt = new PlanTicket();
 		     pt.setStartStop(0);
 		     pt.setEndStop(maxStopSeq);
 		     pt.setId(UUID.randomUUID());
 		     pt.setOrginalId(pt.getId());
 		     pt.setSeat(seat);
 		     
 		     SalableRange range = pt.getSalableRange();
 		     range.setDepartureStart(0);
 		     range.setDepartureEnd(maxStopSeq);
 		     range.setDestinationStart(0);
 		     range.setDestinationEnd(maxStopSeq);
 		     
 		     fullRangeGroup.getTickets().add(pt);
 		     pt.setGroup(fullRangeGroup);
		    
 		}
 	}
 	
 
 	private void buildTrain() throws Exception
 	{
         IRailwayRepository repo = this.getSite().getRequiredService(IRailwayRepository.class);
 		
 		
 		this._route = Queries.query(this._train.getTrainNumber().getRoutes()).first(new Predicate<Route>(){
 
 			@Override
 			public boolean evaluate(Route r) throws Exception {
 				return  (r.getStartDate().isBefore(_train.getDepartureDate()) || r.getStartDate().isEqual(_train.getDepartureDate()))
 						&& r.getEndDate().isAfter(_train.getDepartureDate());
 			}});
 		
 		
 		int seatSeq = 0;
 		for(int i = 0; i < 18; i ++)
 		{
 			Car car = new Car();
 			car.setId(UUID.randomUUID());
 			car.setTrain(this._train);
 			CarType ct = repo.getCarTypes()[ i % repo.getCarTypes().length];
 			car.setCarTyp(ct);
 			_train.getCars().add(car);
 			
 			for(Seat seat : ct.getSeats())
 			{
 			     OperatingSeat oseat = new OperatingSeat();
 			     oseat.setId(UUID.randomUUID());
 			     oseat.setCar(car);
 			     oseat.setSeatNumber(seat.getNumber());
 			     oseat.setSequence(seatSeq);
 			     oseat.setSeatType(ct.getSeatType());
 			     seatSeq++;
 			     car.getSeats().add(oseat);
 			}
 		}
 	}
 	
 	@Override
 	public void initialize() throws Exception
 	{
 		
 		this.buildTrain();
 		this.createPlanTickets();
 
 	}
 	
 	private int getStationSequence(final String name) throws Exception
 	{
 		return Queries.query(this._route.getStops()).first(new Predicate<RouteStop>(){
 
 			@Override
 			public boolean evaluate(RouteStop stop) throws Exception {
 				return stop.getStation().getName().equals(name);
 			}}).getSequence();
 	}
 	
 
 	@Override
 	public boolean hasTickets(TicketPoolQueryArgs args) throws Exception{
 		
 		Iterable<PlanTicket> planTickets = this.query(args);
 		return Queries.query(planTickets).any();
 		
 		
 	}
 
 	@Override
 	public TicketPoolTicket[] book(final TicketPoolQueryArgs args) throws Exception {
 		
 		
 		PlanTicket[] planTickets = Queries.query(this.query(args)).toArray(new PlanTicket[0]);
 		
 		
 		
 		for(PlanTicket pt : planTickets)
 		{
 			pt.getGroup().getTickets().remove(pt);
 			if(pt.getGroup().getTickets().size() == 0)
 			{
 			    this._stopRangeGroups.remove(pt.getGroup());	
 			}
 			
 			if(args.getDepartureStop() > pt.getStartStop())
 			{
 				PlanTicket pre = new PlanTicket();
 				pre.setId(UUID.randomUUID());
 				pre.setOrginalId(pt.getOrignalId());
 				pre.setStartStop(pt.getStartStop());
 				pre.setEndStop(args.getDepartureStop());
 				pre.setSeat(pt.getSeat());
 				pre.getSalableRange().copyFrom(pt.getSalableRange());
 				this.addPlanTicketToGroup(pre);
 				
 			}
 			
 			
 			if(args.getDestinationStop() < pt.getEndStop())
 			{
 				PlanTicket after = new PlanTicket();
 				after.setId(UUID.randomUUID());
 				after.setOrginalId(pt.getOrignalId());
 				after.setStartStop(args.getDestinationStop());
 				after.setEndStop(pt.getEndStop());
 				after.setSeat(pt.getSeat());
 				after.getSalableRange().copyFrom(pt.getSalableRange());
 				this.addPlanTicketToGroup(after);
 			}
 			
 			
 		}
 		
 		
 		if(this._stopRangeGroups.size() == 0)
 		{
 			this._isSoldOut = true;
 		}
 		
 		
 		TicketPoolTicket[] rs = new TicketPoolTicket[planTickets.length];
 		
 		for(int i = 0; i < planTickets.length; i++)
 		{
 			PlanTicket pt = planTickets[i];
 			rs[i] = new TicketPoolTicket();
 			rs[i].setSeat(pt.getSeat());
 			rs[i].setDepartureStop(args.getDepartureStop());
 			rs[i].setDestinationStop(args.getDestinationStop());
 			rs[i].setPool(this);
 		}
 		
 		
 		return rs;
 	}
 	
 	private void addPlanTicketToGroup(PlanTicket pt)
 	{
 		StopRange range = new StopRange();
 		range.start = pt.getStartStop();
 		range.end = pt.getEndStop();
 		
 		int pos = CollectionUtils.binarySearchBy(this._stopRangeGroups, range, new Selector<StopRangeGroup, StopRange>(){
 
 			@Override
 			public StopRange select(StopRangeGroup item) {
 				return item.getRange();
 			}});
 		
 		
 		StopRangeGroup group;
 		
 		if(pos >= 0)
 		{
 			group = this._stopRangeGroups.get(pos);
 		}
 		else
 		{
 			group = new StopRangeGroup();
 			group.setRange(range);
 			this._stopRangeGroups.add(~pos, group);
 		}
 		
 		pt.setGroup(group);
 		group.getTickets().add(pt);
 	}
 	
 	
 	private Iterable<PlanTicket> query(final TicketPoolQueryArgs args) throws Exception
 	{
 	
 		IPlanTicketFilter[] filters = this.createFilters();
 		
 		Iterable<PlanTicket> rs = Queries.query(this._stopRangeGroups)
 				.where(new Predicate<StopRangeGroup>(){
 
 					@Override
 					public boolean evaluate(StopRangeGroup group)
 							throws Exception {
 						return group.getRange().start <= args.getDepartureStop() && args.getDestinationStop() <= group.getRange().end;
 					}})
 				.selectMany(new Selector<StopRangeGroup, Iterable<PlanTicket>>(){
 
 					@Override
 					public Iterable<PlanTicket> select(StopRangeGroup item) {
 						return item.getTickets();
 					}});
 		
 		
 		for(IPlanTicketFilter filter : filters)
 		{
 			rs = filter.filter(rs, args);
 		}
 		
 		return rs;
 		
 	}
 	
 	
 	private IPlanTicketFilter[] createFilters() throws Exception
 	{
 		
 		ServiceContainer site = new ServiceContainer();
 		site.initializeServices(this.getSite(), new Object[]{this});
 		
 		
 		IPlanTicketFilter[] rs = new IPlanTicketFilter[_filterTypes.length];
 		
 		for(int i = 0; i < _filterTypes.length; i ++)
 		{
 		    rs[i] = (IPlanTicketFilter)_filterTypes[i].newInstance();
 		    if(rs[i] instanceof IObjectWithSite)
 		    {
 		    	((IObjectWithSite)rs[i]).setSite(site);
 		    }
 		}
 		
 		return rs;
 	}
 	
 	
 	@SuppressWarnings("rawtypes")
 	private static Class[] _filterTypes = new Class[] {TicketSalableRangeFilter.class, TicketSeatTypeFilter.class, TicketCountFilter.class};
 
 	@Override
 	public TicketPoolQueryArgs toTicketPoolQueryArgs(TicketQueryArgs args)
 			throws Exception {
 		TicketPoolQueryArgs rs = new TicketPoolQueryArgs();
 		rs.setDepartureStop(this.getStationSequence(args.getDepartureStation()));
 		rs.setDestinationStop(this.getStationSequence(args.getDestinationStation()));
 		
 		if(rs.getDestinationStop() <= rs.getDepartureStop())
 		{
 			throw new IllegalArgumentException("Destination station must be after the departure station.");
 		}
 		
 		rs.setCount(args.getCount());
 		rs.setSeatType(args.getSeatType());
 		
 		
 		
 		
 		return rs;
 	}
 
 
 	@Override
 	public Ticket[] toTicket(TicketPoolTicket[] poolTickets) throws Exception {
 		
 		
 		IRailwayRepository repo = this.getSite().getRequiredService(IRailwayRepository.class);
 		
 		Ticket[] rs = new Ticket[poolTickets.length];
 		
 		for(int i = 0; i < poolTickets.length; i ++)
 		{
 			final TicketPoolTicket pt = poolTickets[i];
 		    Ticket ticket = new Ticket();
 		    ticket.setId(UUID.randomUUID());
 		    ticket.setTrainNumber(this.getTrain().getTrainNumber().getName());
 		    ticket.setSeatNumber(pt.getSeat().getSeatNumber());
 		    ticket.setCar(Integer.toString(pt.getSeat().getCar().getCarNumber()));
 		    String seatTypeName = Queries.query(repo.getSeatTypes())
 		        .where(new Predicate<SeatType>(){
 
 				    @Override
 				    public boolean evaluate(SeatType seatType) throws Exception {
 					    return seatType.getCode() == pt.getSeat().getSeatType();
 				    }})
 				.select(new Selector<SeatType, String>(){
 
 					@Override
 					public String select(SeatType seatType) {
 						return seatType.getName();
 					}})
 				.first();
 		    ticket.setSeatType(seatTypeName);
 		    
 		    ticket.setDepartureStation(this._route.getStops().get(pt.getDepartureStop()).getStation().getName());
 		    ticket.setDestinationStation(this._route.getStops().get(pt.getDestinationStop()).getStation().getName());
 		    ticket.setDepartureDate(this._train.getDepartureDate());
 		    
 		    rs[i] = ticket;
 		   
 		}
 		
 		
 		return rs;
 	}
 
 
 	
 	private Boolean _isSoldOut = false;
 	
 	@Override
 	public Boolean getIsSoldOut() throws Exception {
 		return this._isSoldOut;
 	}
 	
 }
