 package by.parf.checkers.servlet;
 
 import by.parf.checkers.beans.Match;
 import by.parf.checkers.beans.MatchSet;
 import by.parf.checkers.dao.MatchDao;
 import by.parf.checkers.dao.TeamDao;
 import by.parf.checkers.factory.MatchFactory;
 import by.parf.checkers.factory.TeamFactory;
 import by.parf.checkers.service.Constant;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: parf
  * Date: 4.9.13
  * Time: 0.56
  */
 @WebServlet(
         name="addMatchController",
         urlPatterns={"/addMatchController"}
 )
 public class AddMatchController extends AbstractController{
     @Override
     protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         String DATE_PATTERN = "MM/dd/yyyy";
         String TIME_PATTERN = "HH:mm";
         SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
         SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN);
         TeamDao teamDao = TeamFactory.getClassFromFactory();
         MatchDao matchDao = MatchFactory.getClassFromFactory();
 
         String matchName = request.getParameter(Constant.KEY_INPUT_MATCH_NAME);
         Date matchDate = null;
         Date matchTime = null;
         try {
             matchDate = dateFormat.parse(request.getParameter(Constant.KEY_INPUT_MATCH_DATE));
             matchTime = timeFormat.parse(request.getParameter(Constant.KEY_INPUT_MATCH_TIME));
         } catch (ParseException e) {
             e.printStackTrace();
         }
         Long team1Id = Long.valueOf(request.getParameter(Constant.KEY_INPUT_TEAM1_ID));
         Long team2Id = Long.valueOf(request.getParameter(Constant.KEY_INPUT_TEAM2_ID));
 
         long matchId = matchDao.getMaxId() + 1;
         Match match = new Match(matchId);
         match.setTeamFirst(teamDao.getTeamById(team1Id));
         match.setTeamSecond(teamDao.getTeamById(team2Id));
         match.setName(matchName);
         match.setDate(matchDate);
         match.setTime(matchTime);
 
         MatchSet matchSet = matchDao.getMatchSetByDate(match.getDate());
         if (matchSet == null) {
             matchSet = new MatchSet(matchDao.getMaxMatchSetId() + 1, match.getDate());
             matchDao.addMatchSet(matchSet);
         }
         matchDao.addMatch(match);
         matchDao.addMatchToMatchSet(match, matchSet);
 
         response.setContentType("text/html");
         response.getWriter().write("true");
         matchDao.close();
        teamDao.close();
 
     }
 }
