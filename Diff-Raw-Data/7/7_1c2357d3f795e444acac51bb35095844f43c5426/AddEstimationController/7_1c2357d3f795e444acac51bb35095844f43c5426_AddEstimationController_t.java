 package by.parf.checkers.servlet;
 
 import by.parf.checkers.beans.Evaluation;
 import by.parf.checkers.beans.Match;
 import by.parf.checkers.beans.MatchSet;
 import by.parf.checkers.beans.User;
 import by.parf.checkers.dao.EvaluationDao;
 import by.parf.checkers.dao.MatchDao;
 import by.parf.checkers.factory.EvaluationFactory;
 import by.parf.checkers.factory.MatchFactory;
 import by.parf.checkers.service.Constant;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: parf
  * Date: 21.9.13
  * Time: 17.48
  */
 @WebServlet(
         name="addEstimationController",
         urlPatterns = {"/addEstimationController"}
 )
 public class AddEstimationController extends AbstractController {
     @Override
     protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         int GOALS_FIRST_TEAM = 0;
         int GOALS_SECOND_TEAM = 1;
 
         HttpSession session = request.getSession();
         User user = (User) session.getAttribute(Constant.KEY_USER);
         String EstimationList =  request.getParameter(Constant.KEY_INPUT_ESTIMATION);
         long matchSetId = Long.valueOf(request.getParameter(Constant.KEY_INPUT_MATCH_SET));
 
 
         EvaluationDao evaluationDao = EvaluationFactory.getClassFromFactory();
         MatchDao matchDao = MatchFactory.getClassFromFactory();
         MatchSet matchSet = matchDao.getMatchSetById(matchSetId);
 
 
         List<Match> matchList = matchSet.getMatches();
         Evaluation tmpEvaluation;
         String[] evaluationList = EstimationList.split(",");
         int evlLength = evaluationList.length;
 
         for (int i = 0; i < evlLength; i++) {
             tmpEvaluation = new Evaluation(evaluationDao.getMaxId() + 1);
             tmpEvaluation.setTeamFirstGoals(Integer.valueOf((evaluationList[i].split(":")[GOALS_FIRST_TEAM]).trim()));
             tmpEvaluation.setTeamSecondGoals(Integer.valueOf((evaluationList[i].split(":")[GOALS_SECOND_TEAM]).trim()));
             tmpEvaluation.setMatch(matchList.get(i));
             tmpEvaluation.setUser(user);
             evaluationDao.addEvaluation(tmpEvaluation);
         }
 
         response.setContentType("text/html");
         response.getWriter().write("true");
         matchDao.close();
        evaluationDao.close();
     }
 }
