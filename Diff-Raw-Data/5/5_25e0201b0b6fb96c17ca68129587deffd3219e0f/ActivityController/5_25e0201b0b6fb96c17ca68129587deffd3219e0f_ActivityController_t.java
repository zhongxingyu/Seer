 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparatorByLetterCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.ActivityTypeComparator;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 import org.displaytag.tags.TableTagParameters;
 import org.displaytag.util.ParamEncoder;
 import org.displaytag.properties.SortOrderEnum;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 
 @AccessControl(roles = Role.STUDY_ADMIN)
 
 public class ActivityController extends PscAbstractController {
     private ActivityDao activityDao;
     private SourceDao sourceDao;
     private PlannedActivityDao plannedActivityDao;
     private ActivityTypeDao activityTypeDao;
     private final static Integer pageIncrementor =100;
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
 
         String sortOrder = request.getParameter((new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_ORDER)));
         SortOrderEnum sortOrderEnum =  (sortOrder == null || sortOrder.equals("2"))
                 ? SortOrderEnum.DESCENDING : SortOrderEnum.ASCENDING;
         String sortItem = request.getParameter((new ParamEncoder("row").encodeParameterName(TableTagParameters.PARAMETER_SORT)));
 
         if ("POST".equals(request.getMethod())) {
             String sourceId = ServletRequestUtils.getRequiredStringParameter(request, "sourceId");
             Integer index = ServletRequestUtils.getRequiredIntParameter(request, "index");
             if (!sourceId.equals("select")) {
                 model = processRequest(model, sourceId, index, sortOrderEnum, sortItem);
             }
             return new ModelAndView("template/ajax/activityTableUpdate", model);
         } else {
             if (request.getParameterMap().isEmpty()) {
                 model.put("sources", sourceDao.getAll());
             } else {
                 String sourceId = ServletRequestUtils.getRequiredStringParameter(request, "sourceId");
                 model = processRequest(model, sourceId, 0, sortOrderEnum, sortItem);
                 model.put("sourceId", new Integer(sourceId));
                 model.put("sources", sourceDao.getAll());
             }
 
             return new ModelAndView("activity", model);
         }
     }
 
     private Map<String, Object> processRequest( Map<String, Object> model, String sourceId, Integer index, SortOrderEnum sortOrderEnum, String sortItem) throws Exception{
         List<Activity> activities;
 
         if (sourceId.equals("selectAll")) {
             activities = activityDao.getAll();
         } else {
             activities = activityDao.getBySourceId(new Integer(sourceId));
         }
 
         Integer numberOfPages = getNumberOfPagesFromList(activities);
         activities = sortListBasedOnRequest(activities, sortOrderEnum, sortItem);
         Integer indexSecondBorder = index + pageIncrementor;
         //the condition below is for the "previous" event
         if (index < 0) {
             indexSecondBorder = (-1)*indexSecondBorder;
             index= indexSecondBorder - pageIncrementor;
         }
 
        model.put("selectedPage", index);
         //happens only if we have a large amount of activities (> 100 in our case)
         if (activities.size() > pageIncrementor) {
             model.put("index", indexSecondBorder);
             if (indexSecondBorder > activities.size()){
                 //we reached the last page
                 model.put("showNext", false);
                 model.put("activitiesPerSource", activities.subList(index, activities.size()));
             } else {
                 model.put("showNext", true);
                 model.put("activitiesPerSource", activities.subList(index, indexSecondBorder));
             }
 
             if(index >= pageIncrementor ) {
                 model.put("showPrev", true);
             } else {
                 model.put("showPrev", false);
             }
         } else {
             model.put("index", 0);
             model.put("showNext", false);
             model.put("activitiesPerSource", activities);
         }
 
         model.put("activityTypes", activityTypeDao.getAll());
         if (! (sourceId.equals("select") || sourceId.equals("selectAll"))) {
             model.put("displayCreateNewActivity", Boolean.TRUE);
             model.put("showtable", Boolean.TRUE);
         } else {
             model.put("displayCreateNewActivity", Boolean.FALSE);
         }
         model.put("numberOfPages", numberOfPages);
         return model;
     }
 
     List<Activity> sortListBasedOnRequest(List<Activity> activities, SortOrderEnum sortOrderEnum, String sortItem) {
         if (sortItem!= null) {
             if (sortItem.toLowerCase().equals("name")){
                 Collections.sort(activities, new NamedComparatorByLetterCase());
             } else if (sortItem.toLowerCase().equals("type")){
                 Collections.sort(activities, new ActivityTypeComparator());
             }
             if (sortOrderEnum.equals(SortOrderEnum.DESCENDING)){
                 Collections.reverse(activities);
             }
         } else {
             Collections.sort(activities);
         }
 
         return activities;
     }
 
     private Integer getNumberOfPagesFromList(List<Activity> activities) {
         Integer size = activities.size();
         Integer amountOfPages = size/pageIncrementor;
         if (amountOfPages * pageIncrementor < size) {
             amountOfPages++;
         }
         return amountOfPages;
     }
 
 
     //// CONFIGURATION
 
     @Required
     public void setActivityDao(ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     @Required
     public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
         this.plannedActivityDao = plannedActivityDao;
     }
 
     @Required
     public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
         this.activityTypeDao = activityTypeDao;
     }
 }
