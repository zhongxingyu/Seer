 package nz.ac.victoria.ecs.kpsmart.state.manipulation;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.PersistenceContext;
 
 import nz.ac.victoria.ecs.kpsmart.InjectOnContruct;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Bool;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Carrier;
import nz.ac.victoria.ecs.kpsmart.state.entities.state.CustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.DomesticCustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.MailDelivery;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Priority;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Route;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.StorageEntity;
 import nz.ac.victoria.ecs.kpsmart.util.Filter;
 
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.sql.JoinType;
 
 import com.google.inject.Inject;
 import static nz.ac.victoria.ecs.kpsmart.util.ListUtils.filter;
 
 @InjectOnContruct
 public class HibernateStateManipulator implements StateManipulator {
 	@Inject @PersistenceContext
 	private Session session;
 	
 	protected Session getSession() {
 		return session;
 	}
 
 	@Override
 	public MailDelivery getMailDelivery(final long id) {		
 		return (MailDelivery) getSession().get(MailDelivery.class, id);
 	}
 
 	@Override
 	public void saveMailDelivery(final MailDelivery delivery) {
 		this.getSession().merge(delivery);
 		this.getSession().flush();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<Location> getAllLocations() {
 		return (Collection<Location>) this.getSession().createCriteria(Location.class).list();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Route> getAllRoute() {
 		return (List<Route>) this.getSession().createCriteria(Route.class)
 				.add(Restrictions.eq("disabled", Bool.False))
 				.addOrder(Order.asc("uid.id"))
 				.list();
 	}
 
 	@Override
 	public void saveRoute(Route route) {
 		this.session.merge(route.getUid());
 		this.session.merge(route);
 		this.session.flush();
 	}
 
 	@Override
 	public Carrier getCarrier(long id) {
 //		return (Carrier) this.session.get(Carrier.class, id);
 		return (Carrier) this.getSession().createCriteria(Carrier.class)
 				.add(Restrictions.eq("disabled", Bool.False))
 				.add(Restrictions.eq("id", id))
 				.uniqueResult();
 	}
 
 	@Override
 	public void saveCarrier(StorageEntity carier) {
 		this.getSession().merge(carier);
 		this.getSession().flush();
 	}
 	
 	@Override
 	public void deleteCarrier(StorageEntity carrier) {
 		carrier.setDisabled(true);
 		saveCarrier(carrier);
 	}
 
 //	@SuppressWarnings("unchecked")
 //	@Override
 //	public Collection<Route> getAllRoutesWithPoint(Location location) {
 //		return (Collection<Route>) this.session.createCriteria(Route.class)
 //				.add(Restrictions.ne("disabled", Bool.True))
 //				.add( Restrictions.disjunction()
 //						.add(Restrictions.eq("startPoint", location))
 //						.add(Restrictions.eq("endPoint", location))
 //				)
 //				.list();
 //	}
 
 	@Override
 	public Location getLocationForName(String name) {
 		return (Location) this.getSession().createCriteria(Location.class)
 				.add(Restrictions.eq("name", name))
 				.add(Restrictions.ne("disabled", Bool.True))
 				.uniqueResult();
 	}
 
 	@Override
 	public void saveLocation(Location location) {
 		this.getSession().merge(location);
 		this.getSession().flush();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<Carrier> getAllCarriers() {
 		return (Collection<Carrier>) this.getSession().createCriteria(Carrier.class)
 				.add(Restrictions.eq("disabled", Bool.False))
 				.list();
 	}
 
 	@Override
 	public Route getRouteByID(long id) {
 		return (Route) this.getSession().createCriteria(Route.class)
 				.add(Restrictions.eq("uid.id", id))
 				.add(Restrictions.ne("disabled", Bool.True))
 				.uniqueResult();
 	}
 
 	@Override
 	public void deleteRoute(Route route) {
 		route.setDisabled(true);
 		saveRoute(route);
 	}
 
 	@Override
 	public void save(StorageEntity entity) {
 		this.getSession().save(entity);
 		this.getSession().flush();
 	}
 
 	@Override
 	public void delete(StorageEntity entity) {
//		entity.setDisabled(true);
//		this.getSession().merge(entity);
		this.deleteRelatedEntities(entity);
 		this.getSession().flush();
 	}
	
	@SuppressWarnings("unchecked")
	private void deleteRelatedEntities(StorageEntity entity) {
		entity.setDisabled(true);
		this.session.merge(entity);
		
		if (
			(entity instanceof MailDelivery) ||
			(entity instanceof CustomerPrice) ||
			(entity instanceof Route)
		) 
			return;
		
		if (entity instanceof Carrier)
			for (Route r : (List<Route>) this.session.createCriteria(Route.class)
					.add(Restrictions.eq("primaryKey.carrier", entity))
					.list())
				this.deleteRelatedEntities(r);
		if (entity instanceof Location)
			for (Route r : (List<Route>) this.session.createCriteria(Route.class)
					.add(Restrictions.or(
							Restrictions.eq("primaryKey.startPoint", entity), 
							Restrictions.eq("primaryKey.endPoint", entity)))
					.list())
				this.deleteRelatedEntities(r);
	}
 
 	@Override
 	public DomesticCustomerPrice getDomesticCustomerPrice() {
 		return (DomesticCustomerPrice) this.getSession().createCriteria(DomesticCustomerPrice.class)
 					.uniqueResult();
 	}
 
 	@Override
 	public void save(DomesticCustomerPrice domesticPrice) {
 		this.getSession().createQuery("DELETE FROM "+DomesticCustomerPrice.class.getSimpleName())
 				.executeUpdate();
 		this.getSession().save(domesticPrice);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<Route> getAllRoutesForPriority(final Priority priority) {
 		return filter((List<Route>) this.getSession().createCriteria(Route.class)
 					.add(Restrictions.in("primaryKey.transportMeans", priority.ValidTransportMeans))
 					.add(Restrictions.eq("disabled", Bool.False))
 					.list(),
 				new Filter<Route>() {
 					@Override
 					public boolean filter(Route object) {
 						if (priority.International)
 							return true;
 						
 						return !object.getStartPoint().isInternational() && !object.getEndPoint().isInternational();
 					}
 				});
 		
 //		String statement = "select route from Route as route " + 
 //				"inner join route.primaryKey.startPoint as startPoint " +
 //				"inner join route.primaryKey.endPoint as endPoint " +
 //			"where " +
 ////				"route.primaryKey.transportMeans in :transportmeans and " + 
 //				"startPoint.international = :false and " +
 //				"endPoint.international = :false and " +
 //				"route.disabled = :disabled";
 		
 //		return this.getSession().createQuery(statement)
 ////			.setParameter("transportmeans", priority.ValidTransportMeans)
 //			.setParameter("false", Bool.False)
 //			.setParameter("disabled", Bool.False)
 //			.list();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<Route> getRoutesBetween(Location start, Location end, Priority priority) {
 		return (Collection<Route>) this.getSession().createCriteria(Route.class)
 					.add(Restrictions.eq("primaryKey.startPoint", start))
 					.add(Restrictions.eq("primaryKey.endPoint", end))
 					.add(Restrictions.in("primaryKey.transportMeans", priority.ValidTransportMeans))
 					.add(Restrictions.eq("disabled", Bool.False))
 					.list();
 	}
 
 	@Override
 	public Carrier getCarrier(String name) {
 		return (Carrier) this.session.createCriteria(Carrier.class)
 					.add(Restrictions.eq("name", name))
 					.add(Restrictions.eq("disabled", Bool.False))
 					.uniqueResult();
 	}
 	
 	public static Criterion ifTrue(Criterion ifTrue, Criterion ifFalse, boolean expression) {
 		if (expression)
 			return ifTrue;
 		else
 			return ifFalse;
 	}
 }
