 package nz.ac.victoria.ecs.kpsmart.reporting.impl;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import nz.ac.victoria.ecs.kpsmart.InjectOnCall;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.entities.state.MailDelivery;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Price;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Priority;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Route;
 import nz.ac.victoria.ecs.kpsmart.logging.ReadOnlyLog;
 import nz.ac.victoria.ecs.kpsmart.reporting.Report;
 import nz.ac.victoria.ecs.kpsmart.state.ReadOnlyState;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 
 /**
  * A default implementation of a report.
  * 
  * @author hodderdani
  *
  */
 public class DefaultReport implements Report {
 	@Inject
 	private ReadOnlyState state;
 	
 	@Inject 
 	private ReadOnlyLog log;
 	
 	@InjectOnCall
 	public DefaultReport() {}
 	public DefaultReport(ReadOnlyState state, ReadOnlyLog log) {
 		this.state = state;
 		this.log = log;
 	}
 
 	@Override
 	public Collection<AmountOfMail> getAmountsOfMailForAllRoutes() {
 		Collection<Location> startPoints = this.state.getAllLocations();
 		Collection<Location> endPoints = this.state.getAllLocations();
 		Collection<MailDelivery> mailDeliveries = this.state.getAllMailDeliveries();
 		Collection<AmountOfMail> result = new HashSet<AmountOfMail>();
 		
 		for (Location start : startPoints){
 			for (Location end : endPoints) {
 				if (start.equals(end))
 					continue;
 				
 				int count = 0;
 				double weight = 0;
 				double volume = 0;
 				
 				for (MailDelivery m : mailDeliveries) {
 					if (
 							!m.getRoute().get(0).getStartPoint().equals(start) || 
 							!m.getRoute().get(m.getRoute().size()-1).getEndPoint().equals(end))
 						continue;
 					
 					count++;
 					weight += m.getWeight();
 					volume += m.getVolume();
 				}
 				
 				result.add(new AmountOfMail(start, end, count, weight, volume));
 			}
 		}
 		
 		return result;
 	}
 
 	@Override
 	public Collection<RevenueExpediture> getAllRevenueExpenditure() {
 		Collection<Location> allLocations = this.state.getAllLocations();
 		Collection<MailDelivery> mailDeliveries = this.state.getAllMailDeliveries();
 		Collection<RevenueExpediture> result = new HashSet<RevenueExpediture>();
 		
 		for (Location start : allLocations) {
 			for (Location end : allLocations) {
 				if ((start.isInternational() && end.isInternational()) || start.equals(end))
 						continue;
 				
 				for (Priority p : Priority.values()) {
 					double revinue = 0;
 					double expenditure = 0;
 					double totalDeliveryTime = 0;
 					long mailCount = 0;
 					
 					Price price = this.state.getPrice(start, end, p);
 					
 					if (price == null)
 						continue;
 					
 					for (MailDelivery m : mailDeliveries) {
 						if (
 								!m.getPriority().equals(p) ||
 								!m.getRoute().get(0).getStartPoint().equals(start) || 
 								!m.getRoute().get(m.getRoute().size()-1).getEndPoint().equals(end)
 						)
 							continue;
 						
 						revinue += price.getCost(m);
						totalDeliveryTime += m.getShippingDuration()/msinhour;
 						mailCount++;
 						
 						for (Route r : m.getRoute())
 							expenditure += r.getCost(m);
 					}
 					
 					result.add(new RevenueExpediture(start, end, p, revinue, expenditure, totalDeliveryTime/(double)mailCount));
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public int getNumberOfEvents() {
 		return this.log.getAllEvents().size();
 	}
 	
 	@Override
 	public double getTotalExpenditure() {
 		double sum = 0;
 		for (MailDelivery m : this.state.getAllMailDeliveries())
 			sum+= m.getCost();
 		
 		return sum;
 	}
 
 	@Override
 	public double getTotalRevenue() {
 		double sum = 0;
 		for (MailDelivery m : this.state.getAllMailDeliveries())
 			sum += m.getPrice();
 		
 		return sum;
 	}
 	
 	private static final long msinhour = 60*60*1000;
 	
 	public static final class Module extends AbstractModule {
 		@Override
 		protected void configure() {
 			bind(Report.class).to(DefaultReport.class);
 		}
 		
 	}
 
 	@Override
 	public Report getAtEventID(long eventID) {
 		return new DefaultReport(state.getAtEventID(eventID), log.getAtEventID(eventID));
 	}
 }
