 package edu.unsw.cse.comp9323.group1.controllers;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import edu.unsw.cse.comp9323.group1.DAOs.CourseDAO;
 import edu.unsw.cse.comp9323.group1.DAOs.EnrolmentDAO;
 import edu.unsw.cse.comp9323.group1.DAOs.RatingDAO;
 import edu.unsw.cse.comp9323.group1.DAOs.SurveyDAO;
 import edu.unsw.cse.comp9323.group1.models.Course;
 import edu.unsw.cse.comp9323.group1.models.Enrolment;
 import edu.unsw.cse.comp9323.group1.models.StudentModel;
 import edu.unsw.cse.comp9323.group1.models.Survey;
 
 /**
  * 
  * This class is controller class of course detail.
  * 
  * @author group1.comp9323-2013s1
  *
  */
 
 @Controller
 @RequestMapping("/courseDetail")
 public class CourseDetailController {
 	
 	
 	/**
 	 * 
 	 * This method will render detail in course detail page.
 	 * The details are including: course name, rating, enrollment and the available survey.
 	 * 
 	 * @param courseName
 	 * @param studentId
 	 * @param model
 	 * @return
 	 * @throws UnsupportedEncodingException
 	 * @throws URISyntaxException
 	 * @throws HttpException
 	 */
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String getCourseDetail(@RequestParam("courseName") String courseName,@RequestParam("studentId") String studentId, ModelMap model) throws UnsupportedEncodingException, URISyntaxException, HttpException {
 		CourseDAO crsDAO = new CourseDAO();
 		Course course = new Course();
 		course = crsDAO.getCourseByName(courseName);
 		SurveyDAO surveyDAO = new SurveyDAO();
 		List<Survey> allSurveys = new ArrayList<Survey>();
 		allSurveys = surveyDAO.getAllAvailableSurvey();
 		/*
 		 * get spesifically for the current courseID
 		 */
 		List<Survey> listOfSurveys = surveyDAO.getSurveyWithCourseId(courseName);
 		 
 		/*
 		 * filter the result before show it to view
 		 */
 		List<Survey> returnSurveys = new ArrayList<Survey>();
 		Iterator<Survey> listOfSurveysItr = listOfSurveys.iterator();
 		while(listOfSurveysItr.hasNext()){
 			String surveyIdRslt = (String)listOfSurveysItr.next().getId();
 			Iterator<Survey> allSurveysItr = allSurveys.iterator();
 			while(allSurveysItr.hasNext()){
 				Survey surveyTemp = allSurveysItr.next();
 				if(surveyIdRslt.equals(surveyTemp.getId())){
 					returnSurveys.add(surveyTemp);
 				}
 			}
 		}
 	 
 		RatingDAO ratingDAO = new RatingDAO();
 		 
 		double overallRatingCourse = ratingDAO.getOverAllRating(courseName);
 		HashMap <String, Double> userRatingsCourse = new HashMap <String, Double>();
 		userRatingsCourse = ratingDAO.getUserRating(studentId, courseName);
 		
 		model.addAttribute("UniReputationRating", 0);
 		model.addAttribute("UniTeachingRating", 0);
 		model.addAttribute("UniResearchRating", 0);
 		model.addAttribute("UniAdminRating", 0);
 		model.addAttribute("LectureNotesRating", 0);
 		Iterator it = userRatingsCourse.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry pairs = (Map.Entry)it.next();
 			if(pairs.getKey().toString().equalsIgnoreCase("reputation")){
 				model.addAttribute("UniReputationRating", Double.parseDouble(pairs.getValue().toString()));
 				System.out.println(">>>>>>>> reputation sets");
 			}else if(pairs.getKey().toString().equalsIgnoreCase("teaching")){
 		   	 	model.addAttribute("UniTeachingRating", Double.parseDouble(pairs.getValue().toString()));
 		   	}else if(pairs.getKey().toString().equalsIgnoreCase("research")){
 		   		model.addAttribute("UniResearchRating", Double.parseDouble(pairs.getValue().toString()));
 		   	}else if(pairs.getKey().toString().equalsIgnoreCase("admin")){
 		   		model.addAttribute("UniAdminRating", Double.parseDouble(pairs.getValue().toString()));
 		   	}else if(pairs.getKey().toString().equalsIgnoreCase("lecturenotes")){
 		   		model.addAttribute("LectureNotesRating", Double.parseDouble(pairs.getValue().toString()));
 		   	}
 		       
 	       //System.out.println(pairs.getKey().toString() + " = " + pairs.getValue());
 	       //System.out.println(model.get("UniReputationRating"));
 	       System.out.println("ENDDD");
 	       it.remove(); // avoids a ConcurrentModificationException
 		}
 		
 		/*
     	 * getEnrollment
     	 */
     	try {
 	    	EnrolmentDAO enrollmentDAO = new EnrolmentDAO();
 			List<Enrolment> enrolmentList = enrollmentDAO.getEnrollmentBasedOnStudentId(studentId);
 			
 			if (enrolmentList.size() > 0){
 		    	List<Course> coursesList = crsDAO.getAllIDNameCoursesBasedOnMultipleCourseId(enrolmentList);
 		    	int check = 0;
 		    	for (Course c: coursesList) {
 		    		if (c.getName().equals(courseName))
 		    			check = 1;
 		    	}
 		    	model.addAttribute("check_enrollment", check);
 			}
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (HttpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		model.addAttribute("OverallRating", overallRatingCourse);
 		model.addAttribute("allSurveys", returnSurveys);
 		model.addAttribute("course", course);
 		model.addAttribute("studentId", studentId);
 		return "courseDetail";
 	}
 
 }
