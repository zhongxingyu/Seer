 package com.iappsam.servlet.po;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.iappsam.forms.Form;
 import com.iappsam.forms.PO;
 import com.iappsam.managers.DivisionOfficeManager;
 import com.iappsam.managers.ItemManager;
 import com.iappsam.managers.POManager;
 import com.iappsam.managers.PersonManager;
 import com.iappsam.managers.SupplierManager;
 import com.iappsam.managers.exceptions.TransactionException;
 import com.iappsam.servlet.form.FormParser;
 import com.iappsam.util.ApplicationContext;
 
 public class POParser extends FormParser {
 
 	@Override
 	public Form createForm(HttpServletRequest req, ApplicationContext appContext) throws TransactionException {
 		SupplierManager sm = appContext.getSupplierManager();
 		POManager pom = appContext.getPOManager();
 		DivisionOfficeManager dom = appContext.getDivisionOfficeManager();
 		PersonManager pm = appContext.getPersonManager();
 		ItemManager im = appContext.getItemManager();
 
 		String supplierParam = req.getParameter("supplier");
		int mopId = Integer.parseInt(req.getParameter("mop"));
		int doId = Integer.parseInt(req.getParameter("dom"));
		long amount = Long.parseLong(req.getParameter("amount"));
		int accountantId = Integer.parseInt(req.getParameter("accountant"));
		int deanId = Integer.parseInt(req.getParameter("dean"));
 
 		PO po = new PO();
 		po.setPoNumber(req.getParameter("po-number"));
 
 		if (supplierParam != null)
 			po.setSupplier(sm.getSupplier(Integer.parseInt(supplierParam)));
 
		po.setModeOfProcurement(pom.getModeOfProcurement(mopId));
		po.setDivisionOffice(dom.getDivisionOffice(doId));
 		po.setDateOfDelivery(req.getParameter("date-of-delivery"));
 		po.setPaymentTerm(req.getParameter("payment-term"));
 		po.setDeliveryTerm(req.getParameter("delivery-term"));
 		po.setTotalAmountInWords(req.getParameter("total-amount-in-words"));
 		po.setOrNumber(req.getParameter("or-number"));
		po.setAmount(amount);
		po.setAccountant(pm.getEmployee(accountantId));
 		po.setAccountantDate(req.getParameter("accountant-date"));
		po.setDean(pm.getEmployee(deanId));
 		po.setDeanDate(req.getParameter("dean-date"));
 
 		String[] items = req.getParameterValues("items");
 
 		if (items != null)
 			for (String item : items) {
 				try {
 					po.addLine(im.getItem(Integer.parseInt(item)));
 				} catch (NumberFormatException e) {
 				}
 			}
 
 		return po;
 	}
 }
