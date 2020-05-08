 package models.competition;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.ExpressionList;
 import com.avaje.ebean.Page;
 import controllers.competition.routes;
 import models.dbentities.CompetitionModel;
 import models.dbentities.PupilAnswer;
 import models.dbentities.QuestionSetModel;
 import models.management.Manager;
 import models.management.ModelState;
 import models.user.AuthenticationManager;
 import models.user.User;
 import play.mvc.Call;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Manager for showing a list of contests taken in the past by an authenticated user.
  *
  * @author Kevin Stobbelaar.
  */
 public class CompetitionHistoryManager extends Manager<CompetitionModel> {
 
     private String contestid;
 
     /**
      * Constructor for manager class.
      *
      * @param contestid  contest id
      * @param state      model state
      * @param orderBy    column to be ordered on
      */
     public CompetitionHistoryManager(ModelState state, String orderBy, String contestid) {
         super(CompetitionModel.class, state, orderBy, "name");
         this.contestid = contestid;
         setPageSize(5);
     }
 
     /**
      * Returns the column headers for the objects of type T.
      *
      * @return column headers
      */
     @Override
     public List<String> getColumnHeaders() {
         ArrayList<String> columnHeaders = new ArrayList<String>();
         columnHeaders.add("name");
         columnHeaders.add("type");
         columnHeaders.add("active");
         columnHeaders.add("starttime");
         columnHeaders.add("endtime");
         columnHeaders.add("creator");
         columnHeaders.add("duration");
         return columnHeaders;
     }
 
     /**
      * Returns a page with elements of type T.
      *
      * WARNING: it's better to override this method in your own manager!
      *
      * @param page     page number
      * @return the requested page
      */
     @Override
     public Page<CompetitionModel> page(int page) {
         User user = AuthenticationManager.getInstance().getUser();
         List<PupilAnswer> pupilAnswers = Ebean.find(PupilAnswer.class).where().ieq("indid", user.getID()).findList();
         List<String> competitionIds = new ArrayList<String>();
         for (PupilAnswer pupilAnswer : pupilAnswers){
             QuestionSetModel questionSetModel = pupilAnswer.questionset;
             competitionIds.add(questionSetModel.contest.id);
         }
         return getDataSet()
                 .where()
                 .in("id", competitionIds)
                 .ilike(filterBy, "%" + filter + "%")
                 .orderBy(orderBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 
     /**
      * Returns the route that must be followed to refresh the list.
      *
      * @param page    current page number
      * @param orderBy current order by
      * @param order   current order
      * @param filter  filter on the items
      * @return Call Route that must be followed
      */
     @Override
     public Call getListRoute(int page, String orderBy, String order, String filter) {
         return routes.CompetitionHistoryController.list(page, orderBy, order, filter);
     }
 
     /**
      * Returns the prefix for translation messages.
      *
      * @return name
      */
     @Override
     public String getMessagesPrefix() {
        return "competition.history";
     }
 }
