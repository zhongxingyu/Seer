 package com.iappsam.reporting;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.iappsam.forms.PR;
 import com.iappsam.forms.PRLine;
 
 public class PRReport extends AbstractReport {
 
 	private PR form;
 
 	public PRReport(PR form) throws ReportException {
 		super("pr");
 		this.form = form;
 		initPropertyMap();
 		fillReport();
 	}
 
 	@Override
 	protected List<Object[]> getRows() {
 		List<Object[]> rows = new ArrayList<Object[]>();
 
 		for (PRLine line : form.getLines())
 			rows.add(toObjectArray(line));
 
 		return rows;
 	}
 
 	private Object[] toObjectArray(PRLine line) {
 		Object[] objs = new Object[6];
 		objs[0] = line.getQuantity() + "";
 		objs[1] = line.getItem().getUnit().getName();
 		objs[2] = line.getItem().getDescription();
 		objs[3] = line.getItem().getStockNumber();
 		objs[4] = line.getItem().getPrice() + "";
 		objs[5] = line.getCost() + "";
 		return objs;
 	}
 
 	@Override
 	protected String[] getColumnTitles() {
 		return new String[] { "Quantity", "Unit", "Item Description", "Stock No.", "Estimated Unit Cost", "Estimated Cost" };
 	}
 
 	@Override
 	protected void initPropertyMap() {
 		propertyMap.put("PR_NUMBER", form.getPrNumber());
		propertyMap.put("PR_DATE", form.getPrDate() == null ? "" : form.getPrDate() + "");
 		propertyMap.put("SAI_NUMBER", form.getSaiNumber());
		propertyMap.put("SAI_DATE", form.getSaiDate() == null ? "" : form.getSaiDate() + "");
 		propertyMap.put("ALOBS_NUMBER", form.getAlobsNumber());
		propertyMap.put("ALOBS_DATE", form.getAlobsDate() == null ? "" : form.getAlobsDate() + "");
 		propertyMap.put("APPROVED_BY_NAME", form.getApprovedBy().getPerson().getName());
 		propertyMap.put("APPROVED_BY_DESIGNATION", form.getApprovedBy().getDesignation());
 		propertyMap.put("REQUESTED_BY_NAME", form.getRequestedBy().getPerson().getName());
 		propertyMap.put("REQUESTED_BY_DESIGNATION", form.getRequestedBy().getDesignation());
 		propertyMap.put("PURPOSE", form.getPurpose());
 		propertyMap.put("DEPARTMENT", form.getDivisionOffice().getDivisionName());
 		propertyMap.put("SECTION", form.getDivisionOffice().getOfficeName());
 	}
 }
