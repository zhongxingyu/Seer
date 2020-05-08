 package com.fanitoz.hairdresser;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 
 @Path("/ServiceCatalog")
 public class ServiceCatalog {
 
 	private static SessionFactory sessionFactory = null;
 	private static ServiceRegistry serviceRegistry = null;
 
 	public ServiceCatalog() {
 		super();
 
 		if (serviceRegistry == null && sessionFactory == null) {
 			try {
 				Configuration configuration = new Configuration();
 				configuration.configure();
 				serviceRegistry = new ServiceRegistryBuilder().applySettings(
 						configuration.getProperties()).buildServiceRegistry();
 				sessionFactory = configuration
 						.buildSessionFactory(serviceRegistry);
 			} catch (Throwable ex) {
 				System.err.println("Failed to create sessionFactory object."
 						+ ex);
 				throw new ExceptionInInitializerError(ex);
 			}
 		}
 	}
 
 	@GET
 	@Path("/getServices")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Service> getServices() {
 		return getServicesFromDB();
 	}
 
 	@POST
 	@Path("/addService")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Integer addService(Service service) {
 
 		Session session = sessionFactory.openSession();
 		Transaction tx = null;
 		int serviceId = 0;
 
 		try {
 			tx = session.beginTransaction();
 			serviceId = (Integer) session.save(service);
 			tx.commit();
 		} catch (HibernateException e) {
 			if (tx != null)
 				tx.rollback();
 			e.printStackTrace();
 		} finally {
 			session.close();
 		}
 
 		return serviceId;
 	}
 
 	@POST
 	@Path("/dispatcher")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public JSONObject dispatcher(JSONObject jsonObj) {
 
 		String request = null;
 		JSONObject jsonResult = null;
 
 		try {
 			request = jsonObj.getString("request");
 		} catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		if (request != null) {
 			if (request.equals("getServices"))
 				jsonResult = getServicesInJSON();
 		}
 
 		return jsonResult;
 	}
 
 	private JSONObject getServicesInJSON() {
 
 		JSONObject jsonReturn = new JSONObject();
 		JSONArray jsonArray = new JSONArray();
 		JSONObject jsonObject = null;
 		List<Service> services = getServicesFromDB();
 
 		Iterator<Service> i = services.iterator();
 
 		while (i.hasNext()) {
 
 			Service service = i.next();
 
 			try {
 				jsonObject = new JSONObject("{category='"
 						+ service.getCategory() + "', name='"
 						+ service.getName() + "', price='" + service.getPrice()
 						+ "'}");
 				jsonArray.put(jsonObject);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			jsonReturn.put("priceList", jsonArray);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return jsonReturn;
 	}
 
 	private List<Service> getServicesFromDB() {
 
 		List<Service> services = null;
 
 		Session session = sessionFactory.openSession();
 		Transaction tx = null;
 
 		try {
 			tx = session.beginTransaction();
 
 			services = session.createQuery("FROM Service").list();
 
 			tx.commit();
 		} catch (HibernateException e) {
 			if (tx != null)
 				tx.rollback();
 			e.printStackTrace();
 		} finally {
 			session.close();
 		}
 
 		return services;
 	}
 }
