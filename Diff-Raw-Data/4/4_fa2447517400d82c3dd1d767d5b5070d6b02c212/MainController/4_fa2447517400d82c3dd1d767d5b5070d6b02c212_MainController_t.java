 package org.akceptor.ijournal.web;
 
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 
 import org.akceptor.ijournal.domain.Group;
 import org.akceptor.ijournal.domain.Subject;
 import org.akceptor.ijournal.service.GroupService;
 import org.akceptor.ijournal.service.SubjectService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value = "/hello")
 public class MainController {
 @Autowired 
 GroupService groupService;
 @Autowired 
 SubjectService subjectService;
 private static Group currentGroup;
 private static Subject currentSubject;
 
 		/**
 	 * Method provides data for inbox.jsp. Most of requests mapped to this
 	 * controller are redirected to this method
 	 */
 	@RequestMapping(method = RequestMethod.POST)
 	//
 	public ModelAndView showSelectedOptipons(HttpServletRequest request) {
 		
 		String selectedGroup ="none";
 		String selectedSubject ="none";
 		//Group and subject selected. Show them
 		if (request!=null){
 			//Map<String, String[]> parameters = request.getParameterMap();
 			selectedGroup = request.getParameter("Group");
 			selectedSubject = request.getParameter("Subject");
 		}
 		
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("hello");
 		
 		
 		if (selectedGroup!=null){
 			currentGroup=groupService.getGroupByID(Integer.parseInt(selectedGroup));
 			currentSubject=subjectService.getSubjectByID(Integer.parseInt(selectedSubject));
 		}
 		try {
 				mav.addObject("selectedGroup", currentGroup);
 				mav.addObject("selectedSubject", currentSubject);
 				mav.addObject("groupMembers", groupService.getGroupMembersByID(currentGroup.getId()));
 				mav.addObject("subjectDates", subjectService.getSubjectDatesByID(currentSubject.getId()));		
		} catch (Exception e) {
				return mav;
		};
 		//mav.addObject(attributeName, attributeValue);
 //		} else
 //		{
 //			mav.addObject("selectedGroup", currentGroup);
 //			mav.addObject("selectedSubject", subjectService.getSubjectByID(currentGroup.getId()));
 //			mav.addObject("groupMembers", groupService.getGroupMembersByID(1));
 //			mav.addObject("subjectDates", subjectService.getSubjectDatesByID(1));
 //		};
 		return mav;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	//First Login - select group and subject
 	public ModelAndView selectGroupAndSubject() {
 		//Adding groups combobox
 		ArrayList groups = new ArrayList();//creating list
 		int gcount=1;//counter for "value" option
 		//Adding subjects combobox
 		ArrayList subjects = new ArrayList();//creating list
 		int scount=1;//counter for "value" option
 		
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("hello");
 		mav.addObject("gcount",gcount);
 		mav.addObject("groups",groupService.getGroups());
 		mav.addObject("scount",scount);
 		mav.addObject("subjects",subjectService.getSubjects());
 		return mav;
 	}
 
 	
 
 }
