 package org.alt60m.ministry.servlet;
 
 import java.util.*;
 import java.text.*;
 import org.apache.log4j.*;
 import org.alt60m.servlet.*;
 import org.alt60m.ministry.model.dbio.*;
 
 class QrySessionInfo {
 	public QrySessionInfo() {
 		queryFields = new Hashtable();
 		showFields = new Vector();
 	}
 
 	public Map queryFields;
 	public List showFields;
 	public List results;
 };
 
 public class HRQueryController extends Controller {
 	String VIEWS_FILE;
 	final String DEFAULT_ACTION = "showStaffQuery";
 	private final String[] _abbrevStrategy = new String[] {"Catalytic","Staffed","ESM","WSN","Operations","HR","Fund Dev","National Director's Office"};
 	private final String[] _expandStrategy = new String[] {"Catalytic","Staffed Campus","ESM","WSN","Operations","Human Resources","Fund Development","National Director's Office"};
 	private final String[] _abbrevRegion = new String[] {"NE","MA","MS","SE","GL","UM","GP","RR","NW","SW","NC"};
 	private final String[] _expandRegion = new String[] {"Northeast","Mid-Atlantic","MidSouth","Southeast","Great Lakes","Upper Midwest","Great Plains Int'l","Red River","Greater Northwest","Pacific Southwest","National Campus Office"};
 	
 	public void init() {
 		try {
 			VIEWS_FILE = getServletContext().getRealPath("/WEB-INF/HRQueryViews.xml");
 			setViewsFile(VIEWS_FILE);
 			setDefaultAction(DEFAULT_ACTION);
 			log.info("init() completed.  Ready for action.");
 		} catch (Exception e) {
 			log.fatal("init() failed", e);
 		}
 	}
 	
 	/**
 	 * @param ctx ActionContext object
 	 */
 	public void showStaffQuery(ActionContext ctx) {
 		try {
 			ActionResults results = new ActionResults("showStaffQuery");
 			results.addMap("region", keyPairArrayToMap(_abbrevRegion, _expandRegion));
 			results.addMap("strategy", keyPairArrayToMap(_abbrevStrategy, _expandStrategy));
 			results.addMap("isNewStaff", keyPairArrayToMap(new String[] { "true", "false" }, new String[] { "Yes", "No" }));
 			results.addMap("maritalStatus", keyPairArrayToMap(
 					new String[] { "M", "S", "U", "W" },
 					new String[] { "Married", "Single", "???", "Widowed" }));
 			results.addMap("position", getDistinctFieldValues("position"));
 			results.addMap("countryStatus", getDistinctFieldValues("countryStatus"));
 			results.addMap("jobStatus", getDistinctFieldValues("jobStatus"));
 			results.addMap("position", getDistinctFieldValues("position"));
 			results.addMap("country", getDistinctFieldValues("primaryEmpLocCountry"));
 			results.addMap("jobTitle", getDistinctFieldValues("jobTitle"));
 			ctx.setReturnValue(results);
 			ctx.goToView("showStaffQuery");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showStaffQuery().", e);
 		}
 	}
 
 	private Map getDistinctFieldValues(String field) {
 		Map results = new TreeMap();
 		try {
 			StaffInfo si = new StaffInfo();
 			String[] values = si.getDistinctFieldValues(field);
 
 			for (int i = 0; i < values.length; i++) {
				if ((values[i]!=null)&&!(values[i].equals(""))){
				results.put(values[i], values[i]);
				}
				
 			}
 		} catch (Exception e) {
 			log.error(e, e);
 		}
 		return results;
 	}
 
 	private Vector getAccountNumbers(String qry) {
 		Vector results = new Vector();
 		try {
 			StaffInfo si = new StaffInfo();
 			String[] values = si.getAccountNumbers(qry);
 			for (int i = 0; i < values.length; i++) {
 				results.add(values[i]);
 			}
 		} catch (Exception e) {
 			log.error(e, e);
 		}
 		return results;
 	}
 
 	/**
 	 * @param ctx ActionContext object
 	 */
 	public void showFieldsSelect(ActionContext ctx) {
 		try {
 			ActionResults results = new ActionResults("showFieldsSelect");
 
 			// Create a session key
 			String sessionKey = "Qry_" + new Date().getTime();
 
 			// Create a object to store query info
 			QrySessionInfo sessionInfo = new QrySessionInfo();
 			ctx.setSessionValue(sessionKey, sessionInfo);
 
 			results.putValue("queryID", sessionKey);
 
 			// Get which fields they asked to be included
 			String[] queryFields = ctx.getInputStringArray("queryFields");
 
 			sessionInfo.queryFields.clear();
 
 			// If they didn't provide any query fields, don't complain...
 			if (queryFields != null) {
 				for (int cnt = 0; cnt < queryFields.length; cnt++) {
 					sessionInfo.queryFields.put(
 						queryFields[cnt],
 						ctx.getInputString(queryFields[cnt], true));
 				}
 			}
 			ctx.setReturnValue(results);
 			ctx.goToView("showFieldsSelect");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showFieldsSelect().", e);
 		}
 	}
 
 	/**
 	 * @param ctx ActionContext object
 	 */
 	public void executeQuery(ActionContext ctx) {
 		try {
 			ActionResults results = new ActionResults("executeQuery");
 			String sessionKey = ctx.getInputString("queryID", true);
 			String[] showFields = ctx.getInputStringArray("showFields");
 
 			results.putValue("queryID", sessionKey);
 			QrySessionInfo info =
 				(QrySessionInfo) ctx.getSessionValue(sessionKey);
 
 			// Store the selected fields as a vector
 
 			info.showFields.clear();
 			// If they selected any
 			if (showFields != null) {
 				for (int cnt = 0; cnt < showFields.length; cnt++)
 					info.showFields.add(showFields[cnt]);
 			} else { // didn't select any
 				// Show their name at minimum 
 				info.showFields.add("name");
 			}
 
 			// Always just return the primary key
 			String qry = new String("");
 
 			// If they selected some criteria
 			if (info.queryFields.size() > 0)
 				qry = makeConditionalClause(info.queryFields);
 
 			// Always just order by name (tbd -- ability to select order criteria)
 			log(qry);
 
 //			StaffInfo si = new StaffInfo();
 
 			// Return list of primary keys
 			Vector staffIds = getAccountNumbers(qry);
 
 			info.results = staffIds;
 
 			// Return to view subset of staff info
 			fillResultsWithQuerySubset(ctx, info, results);
 			ctx.setReturnValue(results);
 			ctx.goToView("showQueryResults");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform executeQuery().", e);
 		}
 	}
 
 	private Map getSelectedFieldsFromResults(Staff staffObj, List fieldsToShow) {
 		Map hashedStaff = new TreeMap();
 		for (int cnt = 0; cnt < fieldsToShow.size(); cnt++) {
 			String fieldName = (String) fieldsToShow.get(cnt);
 			if ("name".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put(
 					"name",
 					staffObj.getLastName() + ", " + staffObj.getFirstName());
 			}
 			if ("accountNo".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("accountNo", staffObj.getAccountNo());
 			}
 			if ("email".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("email", staffObj.getEmail());
 			}
 			if ("homePhone".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("homePhone", staffObj.getHomePhone());
 			}
 			if ("workPhone".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("workPhone", staffObj.getWorkPhone());
 			}
 			if ("otherPhone".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("otherPhone", staffObj.getOtherPhone());
 			}
 			if ("region".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("region", staffObj.getRegion());
 			}
 			if ("birthDate".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("birthDate", staffObj.getBirthDate());
 			}
 			if ("accountCode".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("accountCode", staffObj.getAccountCode());
 			}
 			if ("countryCode".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("countryCode", staffObj.getCountryCode());
 			}
 			if ("countryStatus".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("countryStatus", staffObj.getCountryStatus());
 			}
 			if ("deptId".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("deptId", staffObj.getDeptId());
 			}
 			if ("deptName".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("deptName", staffObj.getDeptName());
 			}
 			if ("employmentType".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("employmentType", staffObj.getEmploymentType());
 			}
 			if ("gender".equalsIgnoreCase(fieldName))
 				hashedStaff.put(
 					"Gender",
 					staffObj.getIsMale() ? "Male" : "Female");
 
 			if ("hireDates".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("hireDate", staffObj.getHireDate());
 				hashedStaff.put("rehireDate", staffObj.getRehireDate());
 				hashedStaff.put("rehireDate", staffObj.getReportingDate());
 
 			}
 
 			if ("primaryAddress".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put(
 					"primaryAddress",
 					addressToString(staffObj.getPrimaryAddress()));
 			}
 
 			if ("secondaryAddress".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put(
 					"secondaryAddress",
 					addressToString(staffObj.getSecondaryAddress()));
 			}
 
 			if ("isEmailSecure".equalsIgnoreCase(fieldName))
 				hashedStaff.put(
 					"IsEmailSecure",
 					staffObj.getIsEmailSecure() ? "True" : "False");
 
 			if ("isNewStaff".equalsIgnoreCase(fieldName))
 				hashedStaff.put(
 					"IsNewStaff",
 					staffObj.getIsNewStaff() ? "True" : "False");
 
 			if ("jobCode".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("jobCode", staffObj.getJobCode());
 			}
 			if ("jobStatus".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("jobStatus", staffObj.getJobStatus());
 			}
 			if ("jobTitle".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("jobTitle", staffObj.getJobTitle());
 			}
 			if ("joiningNS".equalsIgnoreCase(fieldName))
 				hashedStaff.put(
 					"JoiningNS",
 					staffObj.getJoiningNS() ? "True" : "False");
 
 			//			if("loaDates".equalsIgnoreCase(fieldName))			{ hashedStaff.put("LoaDates", staffObj.getLoaDates()); }
 			if ("maritalStatus".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("maritalStatus", staffObj.getMaritalStatus());
 			}
 			if ("marriageDate".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("marriageDate", staffObj.getMarriageDate());
 			}
 			if ("ministry".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("ministry", staffObj.getMinistry());
 			}
 			if ("mobilePhone".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("mobilePhone", staffObj.getMobilePhone());
 			}
 			if ("newStaffTrainingDate".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put(
 					"newStaffTrainingDate",
 					staffObj.getNewStaffTrainingDate());
 			}
 			if ("position".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("position", staffObj.getPosition());
 			}
 			if ("reportingDate".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("reportingDate", staffObj.getReportingDate());
 			}
 			if ("resignationDate".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put(
 					"resignationDate",
 					staffObj.getResignationDate());
 			}
 			//if("position".equalsIgnoreCase(fieldName))				{ hashedStaff.put("position", staffObj.getPosition()); }
 			if ("serviceDate".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("serviceDate", staffObj.getServiceDate());
 			}
 			//			if("spouseInfo".equalsIgnoreCase(fieldName))			{ hashedStaff.put("SpouseInfo", staffObj.getSpouseInfo()); }
 			if ("strategy".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("strategy", staffObj.getStrategy());
 			}
 			if ("url".equalsIgnoreCase(fieldName)) {
 				hashedStaff.put("url", staffObj.getUrl());
 			}
 		}
 		return hashedStaff;
 	}
 
 	private String addressToString(OldAddress address) {
 		StringBuffer strAddress = new StringBuffer();
 		if (address == null)
 			return "";
 
 		strAddress.append(address.getAddress1());
 
 		if (address.getAddress2().trim().length() > 0)
 			strAddress.append(", " + address.getAddress2());
 
 		if (address.getAddress3().trim().length() > 0)
 			strAddress.append(", " + address.getAddress3());
 
 		if (address.getAddress4().trim().length() > 0)
 			strAddress.append(", " + address.getAddress4());
 
 		strAddress.append(", " + address.getCity());
 		strAddress.append(", " + address.getState());
 		strAddress.append(", " + address.getZip());
 		strAddress.append(", " + address.getCountry());
 
 		return strAddress.toString();
 	}
 
 	private String makeConditionalClause(Map queryFields)
 		throws java.text.ParseException {
 		StringBuffer clause = new StringBuffer();
 		String value;
 
 		// Build search clause
 		if ((value = (String) queryFields.get("lastName")) != null)
 			addCondition(
 				clause,
 				"staff.lastName like '" + value.toUpperCase() + "%'");
 		if ((value = (String) queryFields.get("region")) != null)
 			addCondition(
 				clause,
 				"staff.region like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("isNewStaff")) != null)
 			addCondition(
 				clause,
 				"staff.isNewStaff = '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("newStaffTrainingDate"))
 			!= null) {
 			SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy,M,d");
 			Date nstd =
 				parseSimpleDate(
 					(String) queryFields.get("newStaffTrainingDate"));
 			addCondition(
 				clause,
 				"staff.newStaffTrainingDate = Date("
 					+ dtFmt.format(nstd)
 					+ ")");
 		}
 		if ((value = (String) queryFields.get("maritalStatus")) != null)
 			addCondition(
 				clause,
 				"staff.maritalStatus like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("role")) != null)
 			addCondition(
 				clause,
 				"staff.position like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("countryStatus")) != null)
 			addCondition(
 				clause,
 				"staff.countryStatus like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("jobStatus")) != null)
 			addCondition(
 				clause,
 				"staff.jobStatus like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("strategy")) != null)
 			addCondition(
 				clause,
 				"staff.strategy like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("position")) != null)
 			addCondition(
 				clause,
 				"staff.position like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("countryCode")) != null)
 			addCondition(
 				clause,
 				"staff.countryCode like '" + value.toUpperCase() + "'");
 		if ((value = (String) queryFields.get("jobTitle")) != null)
 			addCondition(
 				clause,
 				"staff.jobTitle like '" + value.toUpperCase() + "'");
 
 		return clause.toString();
 	}
 
 	/**
 	 * @param ctx ActionContext object
 	 */
 	public void showQueryResults(ActionContext ctx) {
 		try {
 			ActionResults results = new ActionResults("showQueryResults");
 			String sessionKey = ctx.getInputString("queryID", true);
 			results.putValue("queryID", sessionKey);
 			QrySessionInfo info =
 				(QrySessionInfo) ctx.getSessionValue(sessionKey);
 			fillResultsWithQuerySubset(ctx, info, results);
 			ctx.setReturnValue(results);
 			ctx.goToView("showQueryResults");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showQueryResults().", e);
 		}
 	}
 
 	private void fillResultsWithQuerySubset(
 		ActionContext ctx,
 		QrySessionInfo info,
 		ActionResults results)
 		throws Exception {
 		// Get start and end
 		int start = 0;
 		int end = 0;
 		int showRecs = 0;
 		int totalRecs = info.results.size();
 
 		try {
 			start = Integer.parseInt(ctx.getInputString("from"));
 		} catch (NumberFormatException nfe) {
 			start = 0;
 		}
 
 		try {
 			showRecs = Integer.parseInt(ctx.getInputString("showRecs"));
 		} catch (NumberFormatException nfe) {
 			showRecs = 10;
 		}
 
 		if ((start < 0) || (start >= totalRecs))
 			start = 0;
 		if ((start + showRecs) > totalRecs)
 			end = totalRecs - 1;
 		else
 			end = (start + showRecs) - 1;
 
 //		StaffInfo si = new StaffInfo();
 		Collection hashedResults = new Vector();
 		List subList = info.results.subList(start, end + 1);
 		for (int cnt = 0; cnt < subList.size(); cnt++) {
 			String staffID = (String) subList.get(cnt);
 			Staff staffObj = new Staff(staffID);
 			hashedResults.add(
 				getSelectedFieldsFromResults(staffObj, info.showFields));
 		}
 		results.addCollection("fields", info.showFields);
 		log(
 			"start="
 				+ start
 				+ ", end="
 				+ end
 				+ ", showRecs="
 				+ showRecs
 				+ ", totalRecs="
 				+ totalRecs);
 		results.addCollection("queryResults", hashedResults);
 		results.putValue("start", Integer.toString(start));
 		results.putValue("end", Integer.toString(end));
 		results.putValue("showRecs", Integer.toString(showRecs));
 		results.putValue("total", Integer.toString(totalRecs));
 	}
 
 	private void addCondition(StringBuffer qry, String newCondition) {
 		if (qry.length() == 0)
 			qry.append(newCondition);
 		else
 			qry.append(" AND " + newCondition);
 
 	}
 
 	public static void main(String[] args) throws Exception {
 
 	}
 
 	private Map keyPairArrayToMap(String[] keyArr, String[] valueArr) {
 		Map mappings = new TreeMap();
 		for (int cnt = 0; cnt < keyArr.length; cnt++) {
 			mappings.put(keyArr[cnt], valueArr[cnt]);
 		}
 		return mappings;
 	}
 
 	private java.sql.Date parseSimpleDate(String date)
 		throws java.text.ParseException {
 		StringTokenizer tokens = new StringTokenizer(date, "/");
 		if (tokens.countTokens() != 3)
 			throw new ParseException("Unparsable: " + date, 0);
 
 		int month = Integer.parseInt(tokens.nextToken()) - 1;
 		int day = Integer.parseInt(tokens.nextToken());
 		int year = Integer.parseInt(tokens.nextToken());
 
 		Calendar c = Calendar.getInstance();
 		c.set(year, month, day);
 		return new java.sql.Date(c.getTime().getTime());
 	}
 }
