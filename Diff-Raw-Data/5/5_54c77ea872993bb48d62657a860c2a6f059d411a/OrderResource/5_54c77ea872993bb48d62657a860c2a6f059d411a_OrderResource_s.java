 package cs9322.cafe.resources;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.OPTIONS;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import cs9322.cafe.dao.OrdersDao;
 import cs9322.cafe.menu.OrderMenu;
 import cs9322.cafe.model.Order;
 
 
 public class OrderResource {
 	// Allows to insert contextual objects into the class, 
 	// e.g. ServletContext, Request, Response, UriInfo
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 	String id;
 	
 	public OrderResource(UriInfo uriInfo, Request request, String id) {
 		this.uriInfo = uriInfo;
 		this.request = request;
 		this.id = id;
 	}
 	
 	// Produces XML or JSON output for a client 'program'			
 	@GET
 	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 	public Order getOrder() {
 		Order o = OrdersDao.instance.getOrders().get(id);
 		if(o==null)
 			throw new RuntimeException("GET: Order with " + id +  " not found");
 		return o;
 	}
 	
 	// Produces HTML for browser-based client
 	@GET
 	@Produces(MediaType.TEXT_XML)
 	public Order getOrderHTML() {
 		Order o = OrdersDao.instance.getOrders().get(id);
 		if(o==null)
 			throw new RuntimeException("GET: Order with " + id +  " not found");
 		return o;
 	}
 	
 	@PUT
 	@Produces(MediaType.TEXT_HTML)
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public Response putOrder(
 			@FormParam("type") String type,
 			@FormParam("additions") String additions,
 			@FormParam("paidStatus") String paidStatus,
 			@FormParam("baristaStatus") String baristaStatus,
 			@Context HttpServletResponse servletResponse
 			) throws IOException {
 		Response rsp;
 		Order newo = OrdersDao.instance.getOrders().get(id);
 		
 		// check if this order can be updated
 		if((type != null || additions != null) && (!newo.getPaidStatus().equals("1") || !newo.getBaristaStatus().equals("1"))) {
 			rsp = Response.status(403).build();
 			return rsp;
 		}
 		
 		// check if this order can be paid
 		if(type == null && additions == null && baristaStatus == null && paidStatus != null) {
 			if(newo.getPaidStatus().equals("2")){
 				rsp = Response.status(403).build();
 				return rsp;
 			}
 		}
 		
 		//check update barista status
 		if (baristaStatus != null && type == null && additions == null && paidStatus == null) {
 			//check release button
 			if (baristaStatus.equals("3")) {
 				if (!newo.getBaristaStatus().equals("2") || !newo.getPaidStatus().equals("2")) {
 					rsp = Response.status(403).build();
 					return rsp;
 				}
 			}
 			//check prepare button
 			if (baristaStatus.equals("2") && !newo.getBaristaStatus().equals("1")) {
 				rsp = Response.status(403).build();
 				return rsp;
 			}
 		}
 		
 		if(type != null)
 			newo.setType(type);
 		if(additions != null)
 			newo.setAdditions(additions);
 		if(paidStatus != null)
 			newo.setPaidStatus(paidStatus);
 		if(baristaStatus != null)
 			newo.setBaristaStatus(baristaStatus);
 		
 		// update price
 		newo.setCost(String.valueOf(OrderMenu.instance.getPrice(newo.getType()) + OrderMenu.instance.getPrice(newo.getAdditions())));
 		
 		return putAndGetResponse(newo);
 	}
 	
 	@DELETE
 	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 	public Response deleteOrder() {
 		Response rsp;
 		Order delo = OrdersDao.instance.getOrders().get(id);
 		System.out.println(delo.getBaristaStatus());
 		System.out.println(delo.getPaidStatus());
 		if(!delo.getBaristaStatus().equals("1") || !delo.getPaidStatus().equals("1")) {
 			rsp = Response.status(403).build();
 			return rsp;
 		}
 		
 		OrdersDao.instance.getOrders().remove(id);
 		OrdersDao.instance.writeOrders();
 		rsp = Response.status(200).build();
 		
 		return rsp;
 	}
 	
 	@OPTIONS
 	public String getOptions() {
 		Order o = OrdersDao.instance.getOrders().get(id);
 		String paidStatus = o.getPaidStatus();
 		String baristaStatus = o.getBaristaStatus();
		String options = "get/options";
 		if(paidStatus.equals("1") && baristaStatus.equals("1"))
			options = options + "/put/delete";
 		return options;
 	}
 	
 	private Response putAndGetResponse(Order o) {
 		Response res;
 		String result = "{\"additions\":\"" + o.getAdditions() + "\",\"baristaStatus\":\"" 
 					+ o.getBaristaStatus()+ "\",\"cost\":\"" + o.getCost() + "\",\"id\":\""
 					+ o.getId() + "\",\"paidStatus\":\"" + o.getPaidStatus() + "\",\"type\":\""
 					+ o.getType() + "\"}";
 		if(OrdersDao.instance.getOrders().containsKey(o.getId())) {
 			res = Response.ok(result).build();
 		} else {
 			res = Response.ok(result).build();
 		}
 		OrdersDao.instance.getOrders().put(o.getId(), o);
 		OrdersDao.instance.writeOrders();
 		return res;
 	}
 }
