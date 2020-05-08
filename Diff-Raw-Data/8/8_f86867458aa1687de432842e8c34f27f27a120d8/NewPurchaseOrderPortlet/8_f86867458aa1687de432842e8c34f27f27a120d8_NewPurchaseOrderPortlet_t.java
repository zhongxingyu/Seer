 package com.purchaseorder;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.portlet.PortletException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 import org.springframework.web.portlet.bind.annotation.ResourceMapping;
 
 import com.javatunes.sb.model.PurchaseOrder;
 import com.javatunes.sb.service.PurchaseOrderLocalServiceUtil;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.json.JSONFactoryUtil;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 
 @Controller(value = "NewPurchaseOrderController")
 @RequestMapping("VIEW")
 public class NewPurchaseOrderPortlet {
 
 	private static Log log = LogFactoryUtil.getLog(NewPurchaseOrderPortlet.class);
 	
 	/**
 	 * default view
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 */
 	@RenderMapping
 	public String handleRenderRequest(RenderRequest request,
 			RenderResponse response, Model model) {
 
 		return "view";
 	}
 	
 	/**
 	 * Creates a empty order for you
 	 * @param request
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ResourceMapping(value = "getNewOrder")
 	public void getNewOrder(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
 		
		String result = "";
 		PurchaseOrder newOrder=null;
 		PurchaseOrder purchaseOrder = PurchaseOrderLocalServiceUtil.createPurchaseOrder(0);
 		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
 		Date orderDate = null;
 		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
 		try {
 			orderDate = (Date)formatter.parse(timeStamp);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
			result = "Excpetion while formating the date!";
 			log.error("Excpetion while formating the date!", e);
 		}
 		purchaseOrder.setStatus(0); //0: Incomplete
 		purchaseOrder.setOrderDate(orderDate);
 		purchaseOrder.setUserId(request.getRemoteUser());
 		try {
 			newOrder=PurchaseOrderLocalServiceUtil.addPurchaseOrder(purchaseOrder);
 		} catch (SystemException e) {
 			// TODO Auto-generated catch block
			result = "Creating the purchaseorder items failed!";
 			log.error("Creating the purchaseorder items failed!", e);
 		}
 			
 		response.setContentType("application/json");
		result=JSONFactoryUtil.looseSerialize(newOrder);
 		response.getWriter().write(result);
 		
 	}
 	
 	/**
 	 * Updating the give purchase order
 	 * @param request
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ResourceMapping(value = "updateOrder")
 	public void updateOrder(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
 		if((request.getParameter("poId")!=null)&&(request.getParameter("status")!=null)){
 			int status=Integer.parseInt(request.getParameter("status"));
 		PurchaseOrder purchaseOrder=PurchaseOrderLocalServiceUtil.createPurchaseOrder(Integer.parseInt(request.getParameter("poId")));
 		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
 		Date orderDate = null;
 		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
 		String result = "";
 		try {
 			orderDate = (Date)formatter.parse(timeStamp);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			result = "Excpetion while formating the date!";
 			log.error("Excpetion while formating the date!", e);
 		}
 		purchaseOrder.setStatus(status);
 		purchaseOrder.setOrderDate(orderDate);
 		purchaseOrder.setUserId(request.getRemoteUser());
 		try {
 			PurchaseOrderLocalServiceUtil.updatePurchaseOrder(purchaseOrder);
 		} catch (SystemException e) {
 			// TODO Auto-generated catch block
 			result = "Updating the purchaseorder items failed!";
 			log.error("updating the purchaseorder items failed!", e);
 			
 		}
 		
 		response.setContentType("application/json");
 		result="{\"outcome\":\"success\"}";
 		response.getWriter().write(result);
 		
 		}
 	}
 	/**
 	 * Deleting the selected purchase order!!
 	 * @param request
 	 * @param response
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	@ResourceMapping(value = "removeThisOrder")
 	public void removeThisOrder(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
 		// TODO
 		String result = "";
 		if(request.getParameter("poId")!=null){
 			try {
 				PurchaseOrderLocalServiceUtil.deletePurchaseOrder(Integer.parseInt(request.getParameter("poId")));
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				result = "Excpetion while parsing the purchase order while deleting the order!";
 				log.error("Excpetion while parsing the purchase order while deleting the order!", e);
 			} catch (PortalException e) {
 				// TODO Auto-generated catch block
 				result = "Portal exception during deleting the purchase order!";
 				log.error("portal exception!! during deleting the purchase order!");
 			} catch (SystemException e) {
 				// TODO Auto-generated catch block
 				result = "Deleting the purchaseorder items failed!";
 				log.error("deleting the purchaseorder items failed!", e);
 				
 			}
 			
 			response.setContentType("application/json");
 			result="{\"outcome\":\"success\"}";
 			response.getWriter().write(result);
 		}
 	}
 }
