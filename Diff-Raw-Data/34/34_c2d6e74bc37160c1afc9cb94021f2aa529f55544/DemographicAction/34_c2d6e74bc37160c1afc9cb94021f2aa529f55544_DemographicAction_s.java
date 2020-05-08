 package edu.cmu.ebiz.task8.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import edu.cmu.ebiz.task8.bean.PeopleAgeBean;
 import edu.cmu.ebiz.task8.bean.PeopleGenderBean;
 import edu.cmu.ebiz.task8.bean.PeopleIncomeBean;
 import edu.cmu.ebiz.task8.bean.PeopleSegmentBean;
 import edu.cmu.ebiz.task8.formbean.DemographicForm;
 import edu.cmu.ebiz.task8.model.Model;
 import edu.cmu.ebiz.task8.model.ZillowDAO;
 
 public class DemographicAction extends Action {
 	
 	private FormBeanFactory<DemographicForm> formBeanFactory = FormBeanFactory
 			.getInstance(DemographicForm.class);
 	
 	private ZillowDAO zillowDAO;
 	
 	public DemographicAction(Model model) {
 		zillowDAO = model.getZillowDAO();
 	}
 
 	public String getName() { return "demographic.do"; };
 	
 	public String perform(HttpServletRequest request) {
 		List<String> errors = new ArrayList<String>();
 		request.setAttribute("errors", errors);
 		
 		try {
 			DemographicForm form = formBeanFactory.create(request);
 			request.setAttribute("form", form);
 			if (!form.isPresent()) {
 				return "demographic.jsp";
 			}
 			
 			errors.addAll(form.getValidationErrors());
 			if (errors.size() != 0) {
 				return "demographic.jsp";
 			}
 			
 			String state = form.getState();
 			String city = form.getCity();
 			String area = form.getArea();
 			request.setAttribute("state", state);
 			request.setAttribute("city", city);
 			request.setAttribute("area", area);
 			
 			Boolean isValid = zillowDAO.getMsgCode(state, city, area);
 			if (!isValid) {
 				errors.add("Can't get information, please check your input");
 				return "demographic.jsp";
 			}
 			// income info
 			PeopleIncomeBean income = zillowDAO.getIncome(state, city, area);
 			request.setAttribute("income", income);
 			//segment info
 			ArrayList<PeopleSegmentBean> segmentsList = zillowDAO.getSegmentation(state, city, area);
 			PeopleSegmentBean[] segments = new PeopleSegmentBean[segmentsList.size()];
 			for(int i = 0; i<segmentsList.size(); i++) {
 				segments[i] = segmentsList.get(i);
 			}
 			request.setAttribute("segmentList", segments);
 			
 			//gender info
 			PeopleGenderBean gender = zillowDAO.getGender(state, city, area);
 			request.setAttribute("gender", gender);
 			
 			//age info
 			PeopleAgeBean age = zillowDAO.getAgeDecade(state, city, area);
 			request.setAttribute("age", age);
 			
 			String[] latlong = zillowDAO.getLatLong(state, city, area);
 			request.setAttribute("mapurl", "http://maps.googleapis.com/maps/api/staticmap?"
 						+"center=" + latlong[0] + "," + latlong[1]
 						+"&zoom=14&size=450x300&sensor=false");
 			return "demographic.jsp";
 			
 		} catch (FormBeanException e) {
 			errors.add(e.getMessage());
 			return "error.jsp";
 		}
 	}
 }
