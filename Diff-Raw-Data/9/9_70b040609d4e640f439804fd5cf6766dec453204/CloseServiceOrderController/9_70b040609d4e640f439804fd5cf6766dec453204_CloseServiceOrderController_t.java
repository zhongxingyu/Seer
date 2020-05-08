 package com.twobytes.repair.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.twobytes.master.service.ModelService;
 import com.twobytes.master.service.TypeService;
 import com.twobytes.model.Brand;
 import com.twobytes.model.Customer;
 import com.twobytes.model.Employee;
 import com.twobytes.model.GridResponse;
 import com.twobytes.model.IssuePart;
 import com.twobytes.model.Model;
 import com.twobytes.model.ServiceList;
 import com.twobytes.model.ServiceOrder;
 import com.twobytes.model.Type;
 import com.twobytes.repair.form.ServiceOrderDocForm;
 import com.twobytes.repair.form.ServiceOrderForm;
 import com.twobytes.repair.form.ServiceOrderGridData;
 import com.twobytes.repair.form.ServiceOrderSearchForm;
 import com.twobytes.repair.service.ServiceOrderService;
 import com.twobytes.security.form.LoginForm;
 
 @Controller
 public class CloseServiceOrderController {
 
 	@Autowired
 	private ServiceOrderService soService;
 	
 	@Autowired
 	private TypeService typeService;
 	
 	@Autowired
 	private ModelService modelService;
 	
 	@Autowired
 	private MessageSource messages;
 	
 	public void setMessages(MessageSource messages) {
 		this.messages = messages;
 	}
 	
 	private String VIEWNAME_SEARCH = "closeServiceOrder.search";
 	private String VIEWNAME_FORM = "closeServiceOrder.form";
 	
 	private SimpleDateFormat sdfDateTime = new SimpleDateFormat(
 			"dd/MM/yyyy HH:mm", new Locale ( "US" ));
 	
 	@RequestMapping(value = "/closeServiceOrder")
 	public String view(ModelMap model, HttpServletRequest request) {
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		ServiceOrderSearchForm searchForm = new ServiceOrderSearchForm();
 		model.addAttribute("searchForm", searchForm);
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("typeList", typeList);
 		return VIEWNAME_SEARCH;
 	}
 	
 	
 	@RequestMapping(value="/searchCloseServiceOrder")
 	public @ResponseBody GridResponse getData(@RequestParam(value="name", required=false) String name, @RequestParam(value="startDate", required=false) String startDate, @RequestParam(value="endDate", required=false) String endDate, @RequestParam(value="type", required=false) String type, @RequestParam(value="serialNo", required=false) String serialNo, @RequestParam("rows") Integer rows, @RequestParam("page") Integer page, @RequestParam("sidx") String sidx, @RequestParam("sord") String sord){
 		// Because default Tomcat URI encoding is iso-8859-1 so it must encode back to tis620
 		String[] datePart;
 		String searchStartDate = null;
 		String searchEndDate = null;
 		try{
 			if(null != name){
 				name = new String(name.getBytes("iso-8859-1"), "tis620");	
 			}
 			if(null != startDate && !startDate.equals("")){
 				startDate = new String(startDate.getBytes("iso-8859-1"), "tis620");
 				datePart = startDate.split("/");
 				// Change year to Christ year
 //				Integer year = Integer.parseInt(datePart[2]);				
 //				year = year - 543;
 				searchStartDate = datePart[2]+"-"+datePart[1]+"-"+datePart[0];
 			}
 			if (null != endDate && !endDate.equals("")) {
 				endDate = new String(endDate.getBytes("iso-8859-1"), "tis620");
 				datePart = endDate.split("/");
 				searchEndDate = datePart[2] + "-" + datePart[1] + "-"
 						+ datePart[0];
 			}
 			if(null != type){
 				type = new String(type.getBytes("iso-8859-1"), "tis620");	
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}
 		
 		List<ServiceOrder> soList = soService.selectSOForCloseByCriteria(name,
 				searchStartDate, searchEndDate, type, serialNo, rows, page,
 				sidx, sord);
 		GridResponse response = new GridResponse();
 		
 		List<ServiceOrderGridData> rowsList = new ArrayList<ServiceOrderGridData>();
 		
 		Integer total_pages = 0;
 		if(soList.size() > 0){
 			int endData = 0;
 			if(soList.size() < (rows*page)){
 				endData = soList.size();
 			}else{
 				endData = (rows*page);
 			}
 			for(int i=(rows*page - rows); i<endData; i++){
 				ServiceOrder so = soList.get(i);
 				ServiceOrderGridData gridData = new ServiceOrderGridData();
 				gridData.setServiceOrderID(so.getServiceOrderID());
 				gridData.setServiceOrderDate(sdfDateTime.format(so.getServiceOrderDate()));
 				Customer customer = so.getCustomer();
 				gridData.setName(customer.getName());
 				gridData.setTel(customer.getTel());
 				gridData.setMobileTel(customer.getMobileTel());
 				gridData.setType(so.getProduct().getType().getName());
 				gridData.setBrand(so.getProduct().getBrand().getName());
 				gridData.setModel(so.getProduct().getModel().getName());
 				gridData.setSerialNo(so.getProduct().getSerialNo());
 				gridData.setStatus(so.getStatus());
 				rowsList.add(gridData);
 			}
 			total_pages = new Double(Math.ceil(((double)soList.size()/(double)rows))).intValue();
 		}
 		
 		if (page > total_pages) page=total_pages;
 		response.setPage(page.toString());
 		response.setRecords(String.valueOf(soList.size()));
 		response.setTotal(total_pages.toString());
 		response.setRows(rowsList);
 		return response;
 	}
 	
 	@RequestMapping(value = "/closeServiceOrder", params = "do=preCloseServiceOrder")
 	public String preCloseServiceOrder(
 			@RequestParam(value = "serviceOrderID") String serviceOrderID,
 			ModelMap model, HttpServletRequest request) {
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		
 		ServiceOrder so = soService.selectByID(serviceOrderID);
 		ServiceOrderForm form = new ServiceOrderForm();
 		form.setServiceOrderID(so.getServiceOrderID());
 		form.setServiceOrderDate(sdfDateTime.format(so.getServiceOrderDate()));
 		form.setServiceType(so.getServiceType());
 		if(so.getServiceType() == 1){
 			form.setGuaranteeNo(so.getGuaranteeNo());
 		}else if(so.getServiceType() == 4){
 			form.setRefJobID(so.getRefJobID());
 		}
		if(so.getAppointmentDate() != null){
			form.setAppointmentDate(sdfDateTime.format(so.getAppointmentDate()));
		}
 		form.setRefServiceOrder(so.getRefServiceOrder());
 		form.setCustomerType(so.getCustomerType());
 		form.setCustomerID(so.getCustomer().getCustomerID().toString());
 		form.setDeliveryCustomer(so.getDeliveryCustomer());
 		form.setDeliveryEmail(so.getDeliveryEmail());
 		form.setDeliveryMobileTel(so.getDeliveryMobileTel());
 		form.setDeliveryTel(so.getDeliveryTel());
 		form.setProductID(so.getProduct().getProductID());
 		form.setTypeID(so.getProduct().getType().getTypeID());
 		form.setBrandID(so.getProduct().getBrand().getBrandID());
 		form.setModel(so.getProduct().getModel().getName());
 		form.setSerialNo(so.getProduct().getSerialNo());
 		form.setAccessories(so.getAccessories());
 		form.setDesc(so.getDescription());
 		form.setProblem(so.getProblem());
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		form.setEmpOpen(so.getEmpOpen());
 		
 		if(so.getStartFix() != null){
 			form.setStartFix(sdfDateTime.format(so.getStartFix()));
 		}else{
 			form.setStartFix("-");
 		}
 		if(so.getEndFix() != null){
 			form.setEndFix(sdfDateTime.format(so.getEndFix()));
 		}else{
 			form.setEndFix("-");
 		}
 		
 		if(form.getServiceType() == 1){
 			form.setCosting("warranty");
 		}else if(form.getServiceType() == 2 || form.getServiceType() == 3 || form.getServiceType() == 4){
 			form.setCosting("cost");
 		}else if(form.getServiceType() == 5){
 			form.setCosting("free");
 		}
 		
 		form.setIssuePart("noIssuedPart");
 		
 		form.setNetAmount(0.00);
 		
 		model.addAttribute("form", form);
 
 		model.addAttribute("customer", so.getCustomer());
 
 		model.addAttribute(
 				"fullAddr",
 				so.getCustomer().getAddress()
 						+ " "
 						+ this.messages.getMessage("subdistrict_abbr", null,
 								new Locale("th", "TH"))
 						+ " "
 						+ so.getCustomer().getSubdistrict().getName()
 						+ " "
 						+ this.messages.getMessage("district_abbr", null,
 								new Locale("th", "TH"))
 						+ " "
 						+ so.getCustomer().getDistrict().getName()
 						+ " "
 						+ this.messages.getMessage("province_abbr", null,
 								new Locale("th", "TH")) + " "
 						+ so.getCustomer().getProvince().getName());
 
 		model.addAttribute("product", so.getProduct());
 		
 		Type type = typeList.get(0);
 		List<Brand> brandList = new ArrayList<Brand>();
 		List<Model> modelList = new ArrayList<Model>();
 		if (so.getProduct().getType().getBrands().size() > 0) {
 			brandList = so.getProduct().getType().getBrands();
 			form.setBrandID(form.getBrandID());
 			
 			Brand brand = brandList.get(0);
 			modelList = modelService.getModelByTypeAndBrand(type.getTypeID(), brand.getBrandID());
 		} else {
 			Brand blankBrand = new Brand();
 			blankBrand.setBrandID(null);
 			blankBrand.setName("");
 			brandList.add(blankBrand);
 		}
 		
 		model.addAttribute("typeList", typeList);
 		model.addAttribute("brandList", brandList);
 		model.addAttribute("modelList", modelList);
 
 		ServiceOrderDocForm docForm = new ServiceOrderDocForm();
 		model.addAttribute("docForm", docForm);
 		
 		return VIEWNAME_FORM;
 	}
 	
 	@RequestMapping(value = "/closeServiceOrder", params = "do=save")
 	public String doSave(@ModelAttribute("form") ServiceOrderForm form, HttpServletRequest request, ModelMap model){
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		Date now = new Date();
 		Employee user = (Employee)request.getSession().getAttribute("UserLogin");
 		
 		ServiceOrder so = soService.selectByID(form.getServiceOrderID());
 		so.setEndFix(now);
 		
 		so.setCosting(form.getCosting());
 		so.setRealProblem(form.getRealProblem());
 		so.setCause(form.getCause());
 		so.setFixDesc(form.getFixDesc());
 		so.setTotalPrice(form.getNetAmount());
 		so.setRemark(form.getRemark());
 		
 		so.setUpdatedBy(user.getEmployeeID());
 		so.setUpdatedDate(now);
 		so.setStatus(ServiceOrder.FIXED);
 		
 //		Double netAmount = form.getNetAmount();
 //		System.out.println("netAmount = "+netAmount);
 		
 		// Add list of service cost and issue part and sent with service order to method save
 		
 		List<ServiceList> serviceList = new ArrayList<ServiceList>();
 		List<IssuePart> issuePartList = new ArrayList<IssuePart>();
 		
 		if(form.getIssuePartCode_1() != "" && form.getIssuePartName_1() != "" && (form.getIssuePartQty_1() != null && form.getIssuePartQty_1() > 0) && (form.getIssuePartPrice_1() != null && form.getIssuePartPrice_1() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_1().doubleValue() * form.getIssuePartPrice_1();
 //			System.out.println("sumPrice_1 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_1());
 			ip.setName(form.getIssuePartName_1());
 			ip.setQuantity(form.getIssuePartQty_1());
 			ip.setPrice(form.getIssuePartPrice_1());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_2() != "" && form.getIssuePartName_2() != "" && (form.getIssuePartQty_2() != null && form.getIssuePartQty_2() > 0) && (form.getIssuePartPrice_2() != null && form.getIssuePartPrice_2() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_2().doubleValue() * form.getIssuePartPrice_2();
 //			System.out.println("sumPrice_2 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_2());
 			ip.setName(form.getIssuePartName_2());
 			ip.setQuantity(form.getIssuePartQty_2());
 			ip.setPrice(form.getIssuePartPrice_2());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_3() != "" && form.getIssuePartName_3() != "" && (form.getIssuePartQty_3() != null && form.getIssuePartQty_3() > 0) && (form.getIssuePartPrice_3() != null && form.getIssuePartPrice_3() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_3().doubleValue() * form.getIssuePartPrice_3();
 //			System.out.println("sumPrice_3 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_3());
 			ip.setName(form.getIssuePartName_3());
 			ip.setQuantity(form.getIssuePartQty_3());
 			ip.setPrice(form.getIssuePartPrice_3());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_4() != "" && form.getIssuePartName_4() != "" && (form.getIssuePartQty_4() != null && form.getIssuePartQty_4() > 0) && (form.getIssuePartPrice_4() != null && form.getIssuePartPrice_4() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_4().doubleValue() * form.getIssuePartPrice_4();
 //			System.out.println("sumPrice_4 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_4());
 			ip.setName(form.getIssuePartName_4());
 			ip.setQuantity(form.getIssuePartQty_4());
 			ip.setPrice(form.getIssuePartPrice_4());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_5() != "" && form.getIssuePartName_5() != "" && (form.getIssuePartQty_5() != null && form.getIssuePartQty_5() > 0) && (form.getIssuePartPrice_5() != null && form.getIssuePartPrice_5() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_5().doubleValue() * form.getIssuePartPrice_5();
 //			System.out.println("sumPrice_5 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_5());
 			ip.setName(form.getIssuePartName_5());
 			ip.setQuantity(form.getIssuePartQty_5());
 			ip.setPrice(form.getIssuePartPrice_5());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_6() != "" && form.getIssuePartName_6() != "" && (form.getIssuePartQty_6() != null && form.getIssuePartQty_6() > 0) && (form.getIssuePartPrice_6() != null && form.getIssuePartPrice_6() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_6().doubleValue() * form.getIssuePartPrice_6();
 //			System.out.println("sumPrice_6 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_6());
 			ip.setName(form.getIssuePartName_6());
 			ip.setQuantity(form.getIssuePartQty_6());
 			ip.setPrice(form.getIssuePartPrice_6());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_7() != "" && form.getIssuePartName_7() != "" && (form.getIssuePartQty_7() != null && form.getIssuePartQty_7() > 0) && (form.getIssuePartPrice_7() != null && form.getIssuePartPrice_7() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_7().doubleValue() * form.getIssuePartPrice_7();
 //			System.out.println("sumPrice_7 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_7());
 			ip.setName(form.getIssuePartName_7());
 			ip.setQuantity(form.getIssuePartQty_7());
 			ip.setPrice(form.getIssuePartPrice_7());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_8() != "" && form.getIssuePartName_8() != "" && (form.getIssuePartQty_8() != null && form.getIssuePartQty_8() > 0) && (form.getIssuePartPrice_8() != null && form.getIssuePartPrice_8() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_8().doubleValue() * form.getIssuePartPrice_8();
 //			System.out.println("sumPrice_8 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_8());
 			ip.setName(form.getIssuePartName_8());
 			ip.setQuantity(form.getIssuePartQty_8());
 			ip.setPrice(form.getIssuePartPrice_8());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_9() != "" && form.getIssuePartName_9() != "" && (form.getIssuePartQty_9() != null && form.getIssuePartQty_9() > 0) && (form.getIssuePartPrice_9() != null && form.getIssuePartPrice_9() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_9().doubleValue() * form.getIssuePartPrice_9();
 //			System.out.println("sumPrice_9 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_9());
 			ip.setName(form.getIssuePartName_9());
 			ip.setQuantity(form.getIssuePartQty_9());
 			ip.setPrice(form.getIssuePartPrice_9());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_10() != "" && form.getIssuePartName_10() != "" && (form.getIssuePartQty_10() != null && form.getIssuePartQty_10() > 0) && (form.getIssuePartPrice_10() != null && form.getIssuePartPrice_10() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_10().doubleValue() * form.getIssuePartPrice_10();
 //			System.out.println("sumPrice_10 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_10());
 			ip.setName(form.getIssuePartName_10());
 			ip.setQuantity(form.getIssuePartQty_10());
 			ip.setPrice(form.getIssuePartPrice_10());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		if(form.getIssuePartCode_11() != "" && form.getIssuePartName_11() != "" && (form.getIssuePartQty_11() != null && form.getIssuePartQty_11() > 0) && (form.getIssuePartPrice_11() != null && form.getIssuePartPrice_11() > 0)){
 			// Add to table issuePart
 			Double netPrice = form.getIssuePartQty_11().doubleValue() * form.getIssuePartPrice_11();
 //			System.out.println("sumPrice_11 = "+netPrice);
 			IssuePart ip = new IssuePart();
 			ip.setServiceOrder(so);
 			ip.setCode(form.getIssuePartCode_11());
 			ip.setName(form.getIssuePartName_11());
 			ip.setQuantity(form.getIssuePartQty_11());
 			ip.setPrice(form.getIssuePartPrice_11());
 			ip.setNetPrice(netPrice);
 			ip.setCreatedBy(user.getEmployeeID());
 			ip.setCreatedDate(now);
 			ip.setUpdatedBy(user.getEmployeeID());
 			ip.setUpdatedDate(now);
 			issuePartList.add(ip);
 		}
 		
 		if(form.getServiceList_1() != "" && (form.getServicePrice_1() != null && form.getServicePrice_1() > 0)){
 			ServiceList sl = new ServiceList();
 			sl.setServiceOrder(so);
 			sl.setServiceName(form.getServiceList_1());
 			sl.setPrice(form.getServicePrice_1());
 			sl.setCreatedBy(user.getEmployeeID());
 			sl.setCreatedDate(now);
 			sl.setUpdatedBy(user.getEmployeeID());
 			sl.setUpdatedDate(now);
 			serviceList.add(sl);
 		}
 		if(form.getServiceList_2() != "" && (form.getServicePrice_2() != null && form.getServicePrice_2() > 0)){
 			ServiceList sl = new ServiceList();
 			sl.setServiceOrder(so);
 			sl.setServiceName(form.getServiceList_2());
 			sl.setPrice(form.getServicePrice_2());
 			sl.setCreatedBy(user.getEmployeeID());
 			sl.setCreatedDate(now);
 			sl.setUpdatedBy(user.getEmployeeID());
 			sl.setUpdatedDate(now);
 			serviceList.add(sl);
 		}
 		if(form.getServiceList_3() != "" && (form.getServicePrice_3() != null && form.getServicePrice_3() > 0)){
 			ServiceList sl = new ServiceList();
 			sl.setServiceOrder(so);
 			sl.setServiceName(form.getServiceList_3());
 			sl.setPrice(form.getServicePrice_3());
 			sl.setCreatedBy(user.getEmployeeID());
 			sl.setCreatedDate(now);
 			sl.setUpdatedBy(user.getEmployeeID());
 			sl.setUpdatedDate(now);
 			serviceList.add(sl);
 		}
 		if(form.getServiceList_4() != "" && (form.getServicePrice_4() != null && form.getServicePrice_4() > 0)){
 			ServiceList sl = new ServiceList();
 			sl.setServiceOrder(so);
 			sl.setServiceName(form.getServiceList_4());
 			sl.setPrice(form.getServicePrice_4());
 			sl.setCreatedBy(user.getEmployeeID());
 			sl.setCreatedDate(now);
 			sl.setUpdatedBy(user.getEmployeeID());
 			sl.setUpdatedDate(now);
 			serviceList.add(sl);
 		}
 		
 		try {
 			soService.close(so, issuePartList, serviceList);
 		} catch (Exception e) {
 			e.printStackTrace();
 			model.addAttribute("errMsg", e.getMessage());
 		}
 		
 		
 		// Set data for redirect back to form screen
 		form.setServiceOrderDate(sdfDateTime.format(so.getServiceOrderDate()));
 		form.setServiceType(so.getServiceType());
 		if(so.getServiceType() == 1){
 			form.setGuaranteeNo(so.getGuaranteeNo());
 		}else if(so.getServiceType() == 4){
 			form.setRefJobID(so.getRefJobID());
 		}
		if(so.getAppointmentDate() != null){
			form.setAppointmentDate(sdfDateTime.format(so.getAppointmentDate()));
		}
 		form.setRefServiceOrder(so.getRefServiceOrder());
 		form.setCustomerType(so.getCustomerType());
 		form.setCustomerID(so.getCustomer().getCustomerID().toString());
 		form.setDeliveryCustomer(so.getDeliveryCustomer());
 		form.setDeliveryEmail(so.getDeliveryEmail());
 		form.setDeliveryMobileTel(so.getDeliveryMobileTel());
 		form.setDeliveryTel(so.getDeliveryTel());
 		form.setProductID(so.getProduct().getProductID());
 		form.setTypeID(so.getProduct().getType().getTypeID());
 		form.setBrandID(so.getProduct().getBrand().getBrandID());
 		form.setModel(so.getProduct().getModel().getName());
 		form.setSerialNo(so.getProduct().getSerialNo());
 		form.setAccessories(so.getAccessories());
 		form.setDesc(so.getDescription());
 		form.setProblem(so.getProblem());		
 		
 		form.setEmpOpen(so.getEmpOpen());
 		
 		if(so.getStartFix() != null){
 			form.setStartFix(sdfDateTime.format(so.getStartFix()));
 		}else{
 			form.setStartFix("-");
 		}
 		if(so.getEndFix() != null){
 			form.setEndFix(sdfDateTime.format(so.getEndFix()));
 		}else{
 			form.setEndFix("-");
 		}
 		
 		/*if(form.getServiceType() == 1){
 			form.setCosting("warranty");
 		}else if(form.getServiceType() == 2 || form.getServiceType() == 3 || form.getServiceType() == 4){
 			form.setCosting("cost");
 		}else if(form.getServiceType() == 5){
 			form.setCosting("free");
 		}*/
 		
 		model.addAttribute("customer", so.getCustomer());
 		model.addAttribute(
 				"fullAddr",
 				so.getCustomer().getAddress()
 						+ " "
 						+ this.messages.getMessage("subdistrict_abbr", null,
 								new Locale("th", "TH"))
 						+ " "
 						+ so.getCustomer().getSubdistrict().getName()
 						+ " "
 						+ this.messages.getMessage("district_abbr", null,
 								new Locale("th", "TH"))
 						+ " "
 						+ so.getCustomer().getDistrict().getName()
 						+ " "
 						+ this.messages.getMessage("province_abbr", null,
 								new Locale("th", "TH")) + " "
 						+ so.getCustomer().getProvince().getName());
 
 		model.addAttribute("product", so.getProduct());
 
 		
 		
 		// get data for print document
 		ServiceOrderDocForm docForm = setDocPrintForm(so);
 		model.addAttribute("docForm", docForm);
 		model.addAttribute("action", "print");
 		model.addAttribute("form", form);
 		return VIEWNAME_FORM;
 	}
 	
 	private ServiceOrderDocForm setDocPrintForm(ServiceOrder so){
 		ServiceOrderDocForm docForm = new ServiceOrderDocForm();
 		docForm.setServiceOrderID(so.getServiceOrderID());
 		return docForm;
 	}
 }
