 package com.iappsam.servlet.pr;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.iappsam.forms.Form;
 import com.iappsam.forms.PR;
 import com.iappsam.forms.PRLine;
 import com.iappsam.managers.DivisionOfficeManager;
 import com.iappsam.managers.ItemManager;
 import com.iappsam.managers.PersonManager;
 import com.iappsam.managers.exceptions.TransactionException;
 import com.iappsam.servlet.form.FormParser;
 import com.iappsam.util.ApplicationContext;
 
 public class PRParser extends FormParser {
 
 	@Override
 	public Form createForm(HttpServletRequest request, ApplicationContext appContext) throws TransactionException {
 
 		ItemManager im = appContext.getItemManager();
 		DivisionOfficeManager dom = appContext.getDivisionOfficeManager();
 		PersonManager pm = appContext.getPersonManager();
 
 		PR pr = new PR();
 
 		String divisionOfficeId = request.getParameter("deptAndSection");
 		if (divisionOfficeId != null)
 			pr.setDivisionOffice(dom.getDivisionOffice(Integer.parseInt(divisionOfficeId)));
 
 		pr.setPrNumber(request.getParameter("prNumber"));
 		pr.setPrDate(request.getParameter("prDate"));
 		pr.setSaiNumber(request.getParameter("saiNumber"));
 		pr.setSaiDate(request.getParameter("saiDate"));
 		pr.setAlobsNumber(request.getParameter("alobsNumber"));
 		pr.setAlobsDate(request.getParameter("alobsDate"));
 		pr.setPurpose(request.getParameter("purpose"));
 
 		String reqId = request.getParameter("requestedBy");
 		if (reqId != null)
 			pr.setRequestedBy(pm.getEmployee(Integer.parseInt(reqId)));
 
 		String aprid = request.getParameter("approvedby");
 		if (aprid != null)
 			pr.setApprovedBy(pm.getEmployee(Integer.parseInt(aprid)));
 
 		String[] quantities = request.getParameterValues("quantity");
 		String[] estUnitCost = request.getParameterValues("estUnitCost");
 		String[] items = request.getParameterValues("items");
 
 		if (items != null)
 			for (int i = 0; i < items.length; i++) {
 				String q = null;
 				String eUC = null;
 				if (quantities != null)
 					q = quantities[i];
 				if (estUnitCost != null)
 					eUC = estUnitCost[i];
 				if (q == null)
 					q = "0";
 				if (eUC == null || eUC == "")
 					eUC = "0";
 				PRLine line = new PRLine();
 
 				line.setQuantity(checkNum(q));
 				line.setItem(im.getItem(Integer.parseInt(items[i])));
 				line.setEstimatedUnitCost(checkNum2(eUC));
 				pr.addLine(line);
 			}
 		return pr;
 	}
 
 	private int checkNum(String value) {
 		int num;
 		try {
 			num = Integer.parseInt(value);
 			if (num < 0)
 				return 0;
 		} catch (Exception e) {
 			return 0;
 		}
 		return num;
 	}
 
	private float checkNum2(String value) {
		float num;
 		try {
			num = Float.parseFloat(value);
 			if (num < 0)
 				return 0;
 		} catch (Exception e) {
 			return 0;
 		}
 		return num;
 	}
 }
