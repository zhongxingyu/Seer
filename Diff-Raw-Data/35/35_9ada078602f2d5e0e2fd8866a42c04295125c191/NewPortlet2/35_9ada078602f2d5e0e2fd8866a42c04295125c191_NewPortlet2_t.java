 package com.test;
 
 import java.io.IOException;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletException;
 import javax.portlet.ProcessAction;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.liferay.portal.kernel.servlet.ServletResponseUtil;
 import com.liferay.portal.kernel.util.ParamUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 
 /**
  * Portlet implementation class NewPortlet2
  */
 public class NewPortlet2 extends MVCPortlet {
  
 	public void updateBook(ActionRequest actionRequest,
 			ActionResponse actionResponse)
 			throws IOException, PortletException {
 			String bookTitle = ParamUtil.getString(actionRequest, "bookTitle");
 			String author = ParamUtil.getString(actionRequest, "author");
 			System.out.println("Your inputs ==> " + bookTitle + ", " + author);
 			}
 	
 	@ProcessAction(name = "getCategories")
 	public void getCategories(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 				
 /*		System.out.println("Sending categories");
 		JSONArray list = new JSONArray();
 		JSONObject cat1 = new JSONObject();
 		JSONObject cat2 = new JSONObject();
 		JSONObject cat3 = new JSONObject();
 		cat1.put("id", 10);
 		cat1.put("name", "Antifungals");
 		cat2.put("id", 20);
 		cat2.put("name", "Probiotics");
 		cat3.put("id", 30);
 		cat3.put("name", "Viral vaccines");
 		list.add(cat1);
 		list.add(cat2);
 		list.add(cat3);
 */
 		
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		JSONArray list = DataBaseFunctions.getCategories(DataBaseFunctions.getWebConnection());
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	@ProcessAction(name = "getDrugs")
 	public void getDrugs(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		JSONArray list = DataBaseFunctions.getDrugs(DataBaseFunctions.getWebConnection(),parameters);
 		
 		
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 
 	/**
 	 * 
 	 * @param request
 	 *            Possible parameters:<br>
 	 *            order_id (int),<br>
 	 *            order_start (String/Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
 	 *            order_end (String/Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
 	 *            order_status (String, one of: 'initiated','sent','delivered','canceled'<br>
 	 *            facility_id (int),<br>
 	 *            facility_name (String)
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ProcessAction(name = "getOrderSummary")
 	public void getOrderSummary(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 
 		JSONObject parameters = new JSONObject(request.getParameterMap());
 		JSONArray list = DataBaseFunctions.getOrderSummary(DataBaseFunctions.getWebConnection(),parameters);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}

	/**
	 * 
	 * @deprecated Replaced by {@link #getOrderSummary()}. Add "sent" as order_status to achieve same functionality.
	 */
	@Deprecated
 	@ProcessAction(name = "getSentOrderSummary")
 	public void getSentOrderSummary(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {

		JSONObject parameters = new JSONObject(request.getParameterMap());
		parameters.put("order_status", "sent");
		JSONArray list = DataBaseFunctions.getOrderSummary(DataBaseFunctions.getWebConnection(),parameters);
 		
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	@ProcessAction(name = "getOrderItems")
 	public void getOrderItems(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		System.out.println("order id: " + request.getParameter("id"));
 		JSONArray list = new JSONArray();
 
 		JSONObject obj7=new JSONObject();
 		obj7.put("drugname","Afluria");
 		obj7.put("unitofissue","100 vials");
 		obj7.put("drugform", "vial");
 		obj7.put("current", 10);
 		obj7.put("price", 100);
 		obj7.put("suggested", 11);
 		obj7.put("amount", 200);
 		obj7.put("id", 4);
 		list.add(obj7);
 		
 		JSONObject obj8=new JSONObject();
 		obj8.put("drugname","Gardasil");
 		obj8.put("unitofissue","200 vials");
 		obj8.put("drugform", "vial");
 		obj8.put("current", 30);
 		obj8.put("price", 200);
 		obj8.put("suggested", 1000);
 		obj8.put("amount", 200);
 		obj8.put("id", 5);
 		list.add(obj8);
 		
 		JSONObject obj9=new JSONObject();
 		obj9.put("drugname","Varivax");
 		obj9.put("unitofissue","500 vials");
 		obj9.put("drugform", "vial");
 		obj9.put("current", 30);
 		obj9.put("price", 200);
 		obj9.put("suggested", 1000);
 		obj9.put("amount", 200);
 		obj9.put("id", 6);
 		list.add(obj9);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	@ProcessAction(name = "getSentOrderItems")
 	public void getSentOrderItems(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		JSONArray list = new JSONArray();
 		String idParameter = request.getParameter("id");
 				
 		JSONObject obj7=new JSONObject();
 		obj7.put("drugname","Afluria");
 		obj7.put("unitofissue","100 vials");
 		obj7.put("drugform", "vial");
 		obj7.put("current", 10);
 		obj7.put("price", 100);
 		obj7.put("suggested", 11);
 		obj7.put("amount", 200);
 		obj7.put("drugid", 4);
 		obj7.put("orderid", 40);
 		list.add(obj7);
 		
 		JSONObject obj8=new JSONObject();
 		obj8.put("drugname","Gardasil");
 		obj8.put("unitofissue","200 vials");
 		obj8.put("drugform", "vial");
 		obj8.put("current", 30);
 		obj8.put("price", 200);
 		obj8.put("suggested", 1000);
 		obj8.put("amount", 200);
 		obj7.put("drugid", 5);
 		obj7.put("orderid", 50);
 		list.add(obj8);
 		
 		JSONObject obj9=new JSONObject();
 		obj9.put("drugname","Varivax");
 		obj9.put("unitofissue","500 vials");
 		obj9.put("drugform", "vial");
 		obj9.put("current", 30);
 		obj9.put("price", 200);
 		obj9.put("suggested", 1000);
 		obj9.put("amount", 200);
 		obj7.put("drugid", 6);
 		obj7.put("orderid", 60);
 		list.add(obj9);
 		
 		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
 		httpResponse.setContentType("text/x-json;charset=UTF-8");
 		ServletResponseUtil.write(httpResponse, list.toJSONString());
 		
 	}
 	
 	@ProcessAction(name = "sendOrder")
 	public void sendOrder(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		System.out.println("Created at: " + request.getParameter("created_at"));
 		
 	}
 	
 	@ProcessAction(name = "updateStock")
 	public void updateStock(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		String added = request.getParameter("added");
 		String reduced = request.getParameter("reduced");
 		String drugid = request.getParameter("drugid");
 		
 		System.out.println(drugid);
 		
 		if (added != null && !(added.equals("NaN"))) {
 			System.out.println("Added: " + added);
 		}
 		
 		if (reduced != null && !(reduced.equals("NaN"))) {
 			System.out.println("Reduced: " + reduced);
 		}
 		
 	}
 	
 	@ProcessAction(name = "updateOrder")
 	public void updateOrder(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		String newStatus = request.getParameter("newStatus");
 	}
 	
 	@ProcessAction(name = "addNewDrug")
 	public void addNewDrug(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		String newName = request.getParameter("name");
 		System.out.println(newName);
 	}
 	
 	@ProcessAction(name = "updateDrug")
 	public void updateDrug(ActionRequest request, ActionResponse response)
 			throws PortletException, IOException {
 		
 		String id = request.getParameter("id");
 		System.out.println(id);
 	}
 	
 	/*public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
 		
 		resourceResponse.setContentType("text/javascript");
 		
 		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
 		jsonObject.put("retVal1", "Returing First value from server");
 		
 		try {
 			PrintWriter writer;
 			writer = resourceResponse.getWriter();
 			writer.write(jsonObject.toString());
 			System.out.println(jsonObject.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}*/
 }
