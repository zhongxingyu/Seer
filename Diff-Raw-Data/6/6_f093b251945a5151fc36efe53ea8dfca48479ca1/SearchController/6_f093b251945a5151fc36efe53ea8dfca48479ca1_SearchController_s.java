 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 import java.util.Collection;
 import java.util.ArrayList;
 
 
 public class SearchController extends Controller {
 	public static String QUERY_FIELD = "QUERY";
 	public static String START_FIELD = "TIME_FROM";
 	public static String END_FIELD = "TIME_UNTIL";
 	public static String RANKING_FIELD = "RANKING";
 
 	public Collection<Record> results;
 	public SearchQuery query;
 
 	public SearchController(ServletContext context,
 			HttpServletRequest request, HttpServletResponse response,
 			HttpSession session) {
 		super(context, request, response, session);
 		results = new ArrayList<Record>();
 	}
 
 	public boolean doSearch() {
 		query = new SearchQuery(request.getParameter(QUERY_FIELD),
 			parseDateOrGetNull(request.getParameter(START_FIELD)),
 			parseDateOrGetNull(request.getParameter(END_FIELD)));
 
 		String rankBy = request.getParameter(RANKING_FIELD);
 
 		if (rankBy != null && rankBy.equals("DATE_ASC")) {
 			query.setRankingPolicy(SearchQuery.RANK_BY_DATE_ASC);
 		} else if (rankBy != null && rankBy.equals("DATE_DES")) {
 			query.setRankingPolicy(SearchQuery.RANK_BY_DATE_DES);
 		} else {
 			query.setRankingPolicy(SearchQuery.RANK_BY_SCORE);
 		}
 
 		query.addClause(createSecurityConstraint());
		results = query.executeSearch(getDatabaseConnection());
 		return true;
 	}
 
 	public Date parseDateOrGetNull(String date) {
 		try {
 			return parseDate(date);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public SearchQuery.WhereClause createSecurityConstraint() {
 		switch (user.getType()) {
 			case User.PATIENT_T:
 				return new ColumnEqualsClause("patient_name",
 					user.getUserName());
 			case User.DOCTOR_T:
 				return new ColumnEqualsClause("doctor_name",
 					user.getUserName());
 			case User.RADIOLOGIST_T:
 				return new ColumnEqualsClause("radiologist_name",
 					user.getUserName());
 			// admin
 			default:
 				return null;
 		}
 	}
 
 	public static class ColumnEqualsClause implements SearchQuery.WhereClause {
 		String column;
 		String value;
 
 		ColumnEqualsClause(String column, String value) {
 			this.column = column;
 			this.value = value;
 		}
 
 		@Override
 		public String getClause() {
 			return " AND " + column + " = ? ";
 		}
 
 		@Override
 		public int addData(PreparedStatement s, int position)
 			throws SQLException {
 			s.setString(position, value);
 			return 1;
 		}
 	}
 }
