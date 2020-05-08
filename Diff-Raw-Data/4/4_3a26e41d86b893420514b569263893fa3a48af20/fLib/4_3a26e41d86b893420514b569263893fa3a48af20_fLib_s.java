 /**
  * Copyright 2001-2005 Iowa State University
  * jportfolio@collaborium.org
  *
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
  
 /**
  * Forecast Library full of needed things for the forecast exercise
  * 
  *
  * @author Daryl Herzmann 26 July 2001
  */
  
 package org.collaborium.portfolio.forecast;
 
 import org.collaborium.portfolio.*;
 import java.sql.*;
 
 public class fLib {
 
  /**
   *  Method that prints out a simple SELECT box 
   *  for the years available
   */
   public static String yearSelect() {
    	StringBuffer sbuf = new StringBuffer();
    
    	sbuf.append("	<SELECT name=\"year\">\n"
 		//+"	<option value=\"2000\">2000\n"
 		+"	<option value=\"2001\">2001\n"
 		+"	<option value=\"2002\">2002\n"
 		+"	<option value=\"2003\">2003\n"
 		+"	<option value=\"2004\">2004\n"
 		+"	<option value=\"2005\">2005\n"
 		+"	<option value=\"2006\">2006\n"
 		+"	<option value=\"2007\">2007\n"
 		+"	<option value=\"2008\">2008\n"
 		+"	<option value=\"2009\">2009\n"
 		+"	<option value=\"2010\">2010\n"
 		+"	<option value=\"2011\">2011\n"
 		+"	<option value=\"2012\">2012\n"
		+"	<option value=\"2013\" SELECTED>2013\n"
 		+"	<option value=\"2014\">2014\n"
 		+"	</SELECT>\n");
    
    	return sbuf.toString();
   } // End of yearSelect()
 
 
  /**
   *  Method that prints out a simple SELECT box 
   *  for the months available
   */
   public static String monthSelect() {
    	StringBuffer sbuf = new StringBuffer();
    
    	sbuf.append("	<SELECT name=\"month\">\n"
 		+"	<option value=\"1\">January\n"
 		+"	<option value=\"2\">Feburary\n"
 		+"	<option value=\"3\">March\n"
 		+"	<option value=\"4\">April\n"
 		+"	<option value=\"5\">May\n"
 		+"	<option value=\"6\">June\n"
 		+"	<option value=\"7\">July\n"
 		+"	<option value=\"8\">August\n"
 		+"	<option value=\"9\">September\n"
 		+"	<option value=\"10\">October\n"
 		+"	<option value=\"11\">November\n"
 		+"	<option value=\"12\">December\n"
 		+"	</SELECT>\n");
    
    	return sbuf.toString();
   } // End of monthSelect()
 
 
 /**
  * Protoype to whichDay()
  */
  public static String whichDay(String portfolio, String callMode, 
    String thisPageURL) throws SQLException {
    return whichDay(portfolio, callMode, thisPageURL, "1970-01-01");
  }
 
  /**
   * Generic method to get the user to select a date
   * @param portfolio which is the Current Portfolio
   * @param callMode value to send back to the CGI server
   * @param thisPageURL value of the page to reference in the form
   * @return HTMLformated string
   */
   public static String whichDay(String portfolio, String callMode, 
     String thisPageURL, String selectedDate) throws SQLException {
 
     StringBuffer sbuf = new StringBuffer();
   
     ResultSet forecastDays = dbInterface.callDB("SELECT * from forecast_days "
       +" WHERE portfolio = '"+ portfolio +"' "
       +" and day <= CURRENT_TIMESTAMP::date ORDER by day ASC ");
 		
     sbuf.append("<FORM METHOD=\"GET\" ACTION=\""+thisPageURL+"\" name=\"f\">\n"
       +"<input type=\"hidden\" name=\"mode\" value=\""+callMode+"\">\n"
       +"<input type=\"hidden\" name=\"portfolio\" value=\""+portfolio+"\">\n"
       +"<table border=0><tr><th>Forecast Date:</th>\n"
       +"<td><SELECT NAME=\"sqlDate\">\n");
     while ( forecastDays.next() ) {
       String thisDate = forecastDays.getString("day");
       sbuf.append("<OPTION VALUE=\""+thisDate+"\" ");
       if (thisDate.equals(selectedDate)) sbuf.append("SELECTED");
       sbuf.append(">"+thisDate+"\n");
     }
     sbuf.append("</SELECT></td><td>\n"
       +"<INPUT TYPE=\"SUBMIT\" VALUE=\"Select Fx Date\">\n"
       +"</td></tr></table>"
       +"</FORM>\n");
   
     return sbuf.toString();
   }
 
 
  /**
   *  Method that prints out a simple SELECT box 
   *  for the months available
   */
   public static String daySelect() {
    	StringBuffer sbuf = new StringBuffer();
    
    	sbuf.append("	<SELECT name=\"day\">\n");
 	for (int i = 1; i < 32; i++)
 		sbuf.append("	<option value="+i+">"+i+"\n");
 
 	sbuf.append("	</SELECT>\n");
    
    	return sbuf.toString();
   } // End of daySelect()
   
  
  /**
   * Method that prints out the results for the last forecast
   * @param portfolio value of the current portfolio
   * @param sqlDate which is the value of the date wanted 
   * @param sortCol self-explainatory
   * @param thisPageURL value of the current pageURL
   */
   public static String forecastResults(String portfolio, String sqlDate, 
     String sortCol, String thisPageURL) 
     throws myException, SQLException {
 	
     StringBuffer sbuf = new StringBuffer();
     sbuf.append("<H3>Previous Forecast Results:</H3>\n");
   
     /** If no date is specified, lets then see if the last answers works**/
     if (sqlDate == null) {
       ResultSet availDates = dbInterface.callDB("SELECT day "
         +"from forecast_answers  WHERE portfolio = '"+ portfolio +"' "
         +" ORDER by day DESC LIMIT 1");
       if ( availDates.next() ) {
 	sqlDate = availDates.getString("day");
       } else {
         throw new myException("No days available to view results for!");
       }
     } 
     sbuf.append( whichDay( portfolio, "l", thisPageURL, sqlDate ) );
 	
     /** We need to get results from the database **/
     forecastDay thisDay = new forecastDay(portfolio, sqlDate);
     thisDay.getValidation();
     thisDay.getClimo();
 	
     if (sortCol == null) {
       sortCol = "total_err";
     }
 	
     sbuf.append( thisDay.catAnswers() );
     sbuf.append("<BR><LI>Table Sorted by: "+ sortCol+"</LI>");
   
     /** Now we have a date, lets get how the kids forecasted **/
     ResultSet forecasts = dbInterface.callDB("SELECT "
      +" getUserName(userid) as realname, * from forecast_grades "
      +" WHERE portfolio = '"+ portfolio +"' "
      +" and day = '"+ sqlDate +"' order by "+sortCol+" ");
 		
     sbuf.append("<P><TABLE>\n"
 		+"<TR>\n"
 		+"	<TH rowspan=\"2\">Forecaster:</TH>\n"
 		+"	<TH colspan=\"5\">Local Site:</TH>\n"
 		+"	<TD rowspan=\"2\"></TD>\n"
 		+"	<TH colspan=\"5\">Floater Site:</TH>\n"
 		+"	<TH colspan=\"2\" rowspan=\"2\">\n"
 		+"      <a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=total_err\">Total:</a></TH>\n"
 		+"</TR>\n"
 		
 		+"<TR>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=local_high\">High:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=local_low\">Low:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=local_prec\">Prec:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=local_snow\">Snow:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=local_err\">Tot:</a></TH>\n"
 
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=float_high\">High:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=float_low\">Low:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=float_prec\">Prec:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=float_snow\">Snow:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=l&sqlDate="+sqlDate+"&sort=float_err\">Tot:</a></TH>\n"
 		+"</TR>\n");
 
 	int i = 0;
 	while ( forecasts.next() ) {
 		if (i % 2 == 0 )
 			sbuf.append("<TR bgcolor=\"#EEEEEE\">\n");
 		else
 			sbuf.append("<TR>\n");
 	
 		sbuf.append("<TD>"+ forecasts.getString("realname") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_high") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_low") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_prec") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_snow") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_err") +"</TD>\n"
 			+"<TD></TD>\n"
 			+"<TD>"+ forecasts.getString("float_high") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_low") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_prec") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_snow") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_err") +"</TD>\n"
 			+"<TD></TD>\n"
 			+"<TD>"+ forecasts.getString("total_err") +"</TD>\n");
 	
 		sbuf.append("</TR>\n");
 		i = i +1;
 	} // End of while()
 	
   
   	sbuf.append("</TABLE>\n");
   
   	return sbuf.toString();
   } // End of lastForecastResults()
   
   
 
   
   /**
   * Method that prints out the results for the last forecast
   * @param portfolio value of the current portfolio
   * @param sqlDate which is the value of the date wanted 
   * @param sortCol self-explainatory
   * @param thisPageURL value of the current pageURL
   */
   public static String cumulativeResults(String portfolio, String sortCol, String thisPageURL) 
   	throws myException, SQLException {
   	StringBuffer sbuf = new StringBuffer();
   
   	if ( sortCol == null )
 		sortCol = "final_tot";
  
   	/** Now we have a date, lets get how the kids forecasted **/
   	ResultSet forecasts = dbInterface.callDB("SELECT getUserName(userid) as realname, *, "
 		+" (p0_total+ p1_total + p2_total + p3_total) AS final_tot from forecast_totals "
 		+" WHERE portfolio = '"+portfolio+"' "
 		+" order by "+sortCol+" ");
 		
 	sbuf.append("<TABLE>\n"
 		+"<TR>\n"
 		+"	<TH rowspan=\"2\">Forecaster:</TH>\n"
 		+"	<TH colspan=\"5\">Local Site:</TH>\n"
 		+"	<TD rowspan=\"2\"></TD>\n"
 		+"	<TH colspan=\"5\">Floater Site:</TH>\n"
 		+"	<TD rowspan=\"2\"></TD>\n"
 		+"	<TH rowspan=\"2\"><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=p0_total\">p0</a></TH>\n"
 		+"	<TH rowspan=\"2\"><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=p1_total\">p1</a></TH>\n"
 		+"	<TH rowspan=\"2\"><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=p2_total\">p2</a></TH>\n"
 		+"	<TH rowspan=\"2\"><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=p3_total\">p3</a></TH>\n"
 		+"	<TH rowspan=\"2\"><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sortfinal_total\">Cum Total</a></TH>\n"
 		+"</TR>\n"
 		
 		+"<TR>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=local_high\">High:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=local_low\">Low:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=local_prec\">Prec:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=local_snow\">Snow:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=local_err\">Tot:</a></TH>\n"
 
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=float_high\">High:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=float_low\">Low:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=float_prec\">Prec:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=float_snow\">Snow:</a></TH>\n"
 		+"<TH><a href=\""+thisPageURL+"?portfolio="+portfolio+"&mode=c&sort=float_err\">Tot:</a></TH>\n"
 		+"</TR>\n");
 
 	int i = 0;
 	while ( forecasts.next() ) {
 		if (i % 2 == 0 )
 			sbuf.append("<TR bgcolor=\"#EEEEEE\">\n");
 		else
 			sbuf.append("<TR>\n");
 	
 		sbuf.append("<TD>"+ forecasts.getString("realname") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_high") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_low") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_prec") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_snow") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("local_err") +"</TD>\n"
 			+"<TD></TD>\n"
 			+"<TD>"+ forecasts.getString("float_high") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_low") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_prec") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_snow") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("float_err") +"</TD>\n"
 			+"<TD></TD>\n"
 			+"<TD>"+ forecasts.getString("p0_total") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("p1_total") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("p2_total") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("p3_total") +"</TD>\n"
 			+"<TD>"+ forecasts.getString("final_tot") +"</TD>\n");
 	
 		sbuf.append("</TR>\n");
 		i = i + 1;
 	} // End of while()
 	
   
   	sbuf.append("</TABLE>\n");
   
   	return sbuf.toString();
   } // End of cumulativeResults()
       
   
   public static String adminRainSelect(String selectName, String selected) {
   	StringBuffer sbuf = new StringBuffer();
   
 	sbuf.append("<SELECT name='"+selectName+"'>\n");
 
 	sbuf.append("<option value='0' ");
 	if ( selected.equalsIgnoreCase("0") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 0 &nbsp; | &nbsp; 0 - Trace\n");
 
 	sbuf.append("<option value='9' ");
 	if ( selected.equalsIgnoreCase("9") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 9 &nbsp; | &nbsp; Trace\n");
 
 	sbuf.append("<option value='1' ");
 	if ( selected.equalsIgnoreCase("1") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 1 &nbsp; | &nbsp; Trace - 0.05\n");
 
 	sbuf.append("<option value='2' ");
 	if ( selected.equalsIgnoreCase("2") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 2 &nbsp; | &nbsp; 0.06 - 0.25\n");
 
 	sbuf.append("<option value='3' ");
 	if ( selected.equalsIgnoreCase("3") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 3 &nbsp; | &nbsp; 0.26 - 0.50\n");
 	
 	sbuf.append("<option value='4' ");
 	if ( selected.equalsIgnoreCase("4") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 4 &nbsp; | &nbsp; 0.51 - 1.00\n");
 	
 	sbuf.append("<option value='5' ");
 	if ( selected.equalsIgnoreCase("5") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 5 &nbsp; | &nbsp; 1.01 +\n");
 
 	sbuf.append("</SELECT>\n");
 
   	return sbuf.toString();
   } // End of rainSelect()
   
     public static String rainSelect(String selectName, String selected) {
   	StringBuffer sbuf = new StringBuffer();
   
 	sbuf.append("<SELECT name='"+selectName+"'>\n");
 
 	sbuf.append("<option value='0' ");
 	if ( selected.equalsIgnoreCase("0") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 0 &nbsp; | &nbsp; 0 - Trace\n");
 
 	sbuf.append("<option value='1' ");
 	if ( selected.equalsIgnoreCase("1") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 1 &nbsp; | &nbsp; Trace - 0.05\n");
 
 	sbuf.append("<option value='2' ");
 	if ( selected.equalsIgnoreCase("2") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 2 &nbsp; | &nbsp; 0.06 - 0.25\n");
 
 	sbuf.append("<option value='3' ");
 	if ( selected.equalsIgnoreCase("3") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 3 &nbsp; | &nbsp; 0.26 - 0.50\n");
 	
 	sbuf.append("<option value='4' ");
 	if ( selected.equalsIgnoreCase("4") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 4 &nbsp; | &nbsp; 0.51 - 1.00\n");
 	
 	sbuf.append("<option value='5' ");
 	if ( selected.equalsIgnoreCase("5") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 5 &nbsp; | &nbsp; 1.01 +\n");
 
 	sbuf.append("</SELECT>\n");
 
   	return sbuf.toString();
   } // End of rainSelect()
   
   public static String adminSnowSelect(String selectName, String selected) {
   	StringBuffer sbuf = new StringBuffer();
 
 	sbuf.append("<SELECT name='"+selectName+"'>\n");
 
 	sbuf.append("<option value='0' ");
 	if ( selected.equalsIgnoreCase("0") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 0 &nbsp; | &nbsp; 0 - Trace\n");
 
 	sbuf.append("<option value='9' ");
 	if ( selected.equalsIgnoreCase("9") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 9 &nbsp; | &nbsp; Trace\n");
 	
 	sbuf.append("<option value='1' ");
 	if ( selected.equalsIgnoreCase("1") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 1 &nbsp; | &nbsp; Trace - 2\"\n");
 
 	sbuf.append("<option value='8' ");
 	if ( selected.equalsIgnoreCase("8") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 8 &nbsp; | &nbsp;  2\"\n");
 	
 	sbuf.append("<option value='2' ");
 	if ( selected.equalsIgnoreCase("2") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 2 &nbsp; | &nbsp; 2\" - 4\"\n");
 
 	sbuf.append("<option value='7' ");
 	if ( selected.equalsIgnoreCase("7") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 7 &nbsp; | &nbsp;  4\"\n");
 
 	sbuf.append("<option value='3' ");
 	if ( selected.equalsIgnoreCase("3") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 3 &nbsp; | &nbsp; 4\"- 8\"\n");
 	
 	sbuf.append("<option value='6' ");
 	if ( selected.equalsIgnoreCase("6") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 6 &nbsp; | &nbsp;  8\"\n");
 	
 	sbuf.append("<option value='4' ");
 	if ( selected.equalsIgnoreCase("4") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 4 &nbsp; | &nbsp; 8\" + \n");
 	
 	sbuf.append("</SELECT>\n");
 
   	return sbuf.toString();
   } // End of snowSelect()
   
   public static String snowSelect(String selectName, String selected) {
   	StringBuffer sbuf = new StringBuffer();
 
 	sbuf.append("<SELECT name='"+selectName+"'>\n");
 
 	sbuf.append("<option value='0' ");
 	if ( selected.equalsIgnoreCase("0") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 0 &nbsp; | &nbsp; 0 - Trace\n");
 
 	sbuf.append("<option value='1' ");
 	if ( selected.equalsIgnoreCase("1") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 1 &nbsp; | &nbsp; Trace - 2\"\n");
 
 	sbuf.append("<option value='2' ");
 	if ( selected.equalsIgnoreCase("2") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 2 &nbsp; | &nbsp; 2\" - 4\"\n");
 
 	sbuf.append("<option value='3' ");
 	if ( selected.equalsIgnoreCase("3") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 3 &nbsp; | &nbsp; 4\"- 8\"\n");
 	
 	sbuf.append("<option value='4' ");
 	if ( selected.equalsIgnoreCase("4") ) sbuf.append("SELECTED");
 	sbuf.append(">CAT 4 &nbsp; | &nbsp; 8\" + \n");
 	
 	sbuf.append("</SELECT>\n");
 
   	return sbuf.toString();
   } // End of snowSelect()
   
   public static String totalForecastErrors( String portfolio ) 
   	throws SQLException {
   	StringBuffer sbuf = new StringBuffer();
   
   	dbInterface.updateDB("DELETE from forecast_totals WHERE portfolio = '"+ portfolio +"' ");
 	
 	/** Total all forecasts first */
 	dbInterface.updateDB("INSERT into forecast_totals ( SELECT userid, portfolio, sum(local_high), "
 		+" sum(local_low), sum(local_prec), sum(local_snow), sum(local_err), sum(float_high), "
 		+" sum(float_low), sum(float_prec), sum(float_snow), sum(float_err) from "
 		+" forecast_grades WHERE portfolio = '"+ portfolio +"' "
 		+" GROUP by userid, portfolio ) ");
   
 	ResultSet students = dbInterface.callDB("SELECT getUserName( username) as realname, "
 		+" username from students "
 		+" WHERE portfolio = '"+ portfolio +"' and nofx = 'n' ");
  
  	while ( students.next() ) {
 		String thisUserID = students.getString("username");
 		dbInterface.updateDB("UPDATE forecast_totals SET "
 		+" p0_total = totalErrorByCase('"+ thisUserID+"', '"+ portfolio +"', 0) , "
 		+" p1_total = totalErrorByCase('"+ thisUserID+"', '"+ portfolio +"', 1) , "
 		+" p2_total = totalErrorByCase('"+ thisUserID+"', '"+ portfolio +"', 2), "
 		+" p3_total = totalErrorByCase('"+ thisUserID+"', '"+ portfolio +"', 3) "
 		+" WHERE userid = '"+ thisUserID +"' and portfolio = '"+ portfolio +"' ");
 	}
 
 	/** Now we need to update Null values, hack! */
 	dbInterface.updateDB("update forecast_totals SET p1_total = 0 WHERE portfolio = '"+ portfolio +"' and p1_total IS NULL");
 	dbInterface.updateDB("update forecast_totals SET p2_total = 0 WHERE portfolio = '"+ portfolio +"' and p2_total IS NULL");
 	dbInterface.updateDB("update forecast_totals SET p3_total = 0 WHERE portfolio = '"+ portfolio +"' and p3_total IS NULL");
 
 	
   	sbuf.append("Done Totalling Forecasts!");
   	return sbuf.toString();
   }
   
  /**
   * gradeForecasts() does the automated grading of the entered forecasts
   * @param portfolio which is the current Portfolio
   * @param sqlDate which is the forecast Date we will be verifying
   * @return String data
   */
   public static String gradeForecasts( String portfolio, String sqlDate ) 
     throws SQLException {
     StringBuffer sbuf = new StringBuffer();
   
     dbInterface.updateDB("DELETE from forecast_grades WHERE "
       +" day = '"+ sqlDate +"' and portfolio = '"+ portfolio +"' ");
   
     ResultSet students = dbInterface.callDB("SELECT "
       +" getUserName( username) as realname, username from students "
       +" WHERE portfolio = '"+ portfolio +"' and nofx = 'n' ");
 
     forecastDay thisDay = new forecastDay(portfolio, sqlDate);
     if (thisDay.getValidation()) {
 	thisDay.getClimo();
 
 	String caseGroup = thisDay.getCaseGroup();
         String local_high = thisDay.getVLocalHighTemp();
         String local_low  = thisDay.getVLocalLowTemp();
         String local_prec = thisDay.getVLocalPrecCat();
         String local_snow = thisDay.getVLocalSnowCat();
         String float_high = thisDay.getVFloaterHighTemp();
         String float_low  = thisDay.getVFloaterLowTemp();
         String float_prec = thisDay.getVFloaterPrecCat();
         String float_snow = thisDay.getVFloaterSnowCat();
 	
         String cl_local_high =  thisDay.getCLocalHighTemp();
         String cl_local_low = 	thisDay.getCLocalLowTemp();
         String cl_local_prec =  thisDay.getCLocalPrecCat();
         String cl_local_snow =  thisDay.getCLocalSnowCat();
         String cl_float_high =  thisDay.getCFloaterHighTemp();
         String cl_float_low = 	thisDay.getCFloaterLowTemp();
         String cl_float_prec =  thisDay.getCFloaterPrecCat();
         String cl_float_snow =  thisDay.getCFloaterSnowCat();
 	
 	String u_local_high = null;
 	String u_local_low = null;
 	String u_local_prec = null;
 	String u_local_snow = null;
 	String u_float_high = null;
 	String u_float_low = null;
 	String u_float_prec = null;
 	String u_float_snow = null;
 	
 	while ( students.next() ) {
 		String thisUserID = students.getString("username");
 		String thisUserName = students.getString("realname");
 		ResultSet userForecast = dbInterface.callDB("SELECT * from forecasts WHERE "
 			+" day = '"+sqlDate+"' and portfolio = '"+ portfolio +"' and "
 			+" userid = '"+ thisUserID +"' ");
 		if ( userForecast.next() ) {
 			u_local_high = userForecast.getString("local_high");
 			u_local_low = userForecast.getString("local_low");
 			u_local_prec = userForecast.getString("local_prec");
 			u_local_snow = userForecast.getString("local_snow");
 			u_float_high = userForecast.getString("float_high");
 			u_float_low = userForecast.getString("float_low");
 			u_float_prec = userForecast.getString("float_prec");
 			u_float_snow = userForecast.getString("float_snow");
 		} else {
 			dbInterface.updateDB("INSERT into forecasts (userid, portfolio, day, local_high, "
 				+" local_low, local_prec, local_snow, float_high, float_low, float_prec, "
 				+" float_snow, type) VALUES ('"+ thisUserID +"', '"+ portfolio +"', "
 				+" '"+ sqlDate +"', '"+ cl_local_high +"', '"+ cl_local_low +"', "
 				+" '"+ cl_local_prec +"', '"+ cl_local_snow +"', '"+ cl_float_high +"' , "
 				+" '"+ cl_float_low +"', '"+ cl_float_prec +"', '"+ cl_float_snow +"', 'c' ) ");
 				
 			u_local_high = cl_local_high;
 			u_local_low =  cl_local_low;
 			u_local_prec = cl_local_prec;
 			u_local_snow = cl_local_snow;
 			u_float_high = cl_float_high;
 			u_float_low =  cl_float_low;
 			u_float_prec = cl_float_prec;
 			u_float_snow = cl_float_snow;
 		}
 	
 		Integer local_high_err = new Integer( gradeTemp( local_high, u_local_high ) );
 		Integer local_low_err = new Integer( gradeTemp( local_low, u_local_low ) );
 		Integer local_prec_err = new Integer( gradePrec( local_prec, u_local_prec ) );
 		Integer local_snow_err = new Integer( gradePrec( local_snow, u_local_snow ) );
 		
 		Integer float_high_err = new Integer( gradeTemp( float_high, u_float_high ) );
 		Integer float_low_err = new Integer( gradeTemp( float_low, u_float_low ) );
 		Integer float_prec_err = new Integer( gradePrec( float_prec, u_float_prec ) );
 		Integer float_snow_err = new Integer( gradePrec( float_snow, u_float_snow ) );
 		
 		Integer local_err = new Integer( local_high_err.intValue() + local_low_err.intValue() 
 			+ local_prec_err.intValue() + local_snow_err.intValue() );
 		Integer float_err = new Integer( float_high_err.intValue() + float_low_err.intValue() 
 			+ float_prec_err.intValue() + float_snow_err.intValue() );
 		Integer total_err = new Integer( local_err.intValue() + float_err.intValue() );
 		
 		dbInterface.updateDB("DELETE from forecast_grades WHERE "
 			+" portfolio = '"+ portfolio +"' and day = '"+ sqlDate +"' "
 			+" and userid = '"+ thisUserID +"' ");
 			
 		dbInterface.updateDB("INSERT into forecast_grades ( userid, portfolio, day, local_high, "
 			+" local_low, local_prec, local_snow, local_err, float_high, float_low, float_prec, "
 			+" float_snow, float_err, total_err, case_group) VALUES ('"+ thisUserID +"', '"+ portfolio +"', "
 			+" '"+ sqlDate +"', '"+ local_high_err.toString() +"', '"+ local_low_err.toString() +"', "
 			+" '"+ local_prec_err.toString() +"', '"+ local_snow_err.toString() +"', "
 			+" "+ local_err.toString() +", '"+ float_high_err.toString() +"' , "
 			+" "+ float_low_err.toString() +", '"+ float_prec_err.toString() +"', "
 			+" "+ float_snow_err.toString() +", "+ float_err.toString() +", "
 			+" "+ total_err.toString() +", "+ caseGroup +" ) ");
 				
 		sbuf.append("<BR>Done grading user: "+ thisUserName +"\n");
 	}
         } else {
           sbuf.append("<P>Verification not done, since none has been entered.");
         }
   	
   	return sbuf.toString();
   } // End of gradeForecasts()
   
   
   public static String gradePrec( String answer, String guess) {
   
   	if ( answer.equals("9") && ( guess.equals("0") || guess.equals("1") ) )
 		return "0";
 	if ( answer.equals("8") && ( guess.equals("1") || guess.equals("2") ) )
 		return "0";
   	if ( answer.equals("7") && ( guess.equals("2") || guess.equals("3") ) )
 		return "0";
   	if ( answer.equals("6") && ( guess.equals("3") || guess.equals("4") ) )
 		return "0";
 		
 	if ( answer.equals( guess ) )
 		return "0";
 		
 	if ( answer.equals("7") && ( guess.equals("0") || guess.equals("1") ) )
 		answer = "2";
 	else if ( answer.equals("6") && ( guess.equals("5") ) )
 		answer = "4";
 	else if ( answer.equals("6") && ( guess.equals("0") || guess.equals("1") || guess.equals("2")) )
 		answer = "3";
 	else if ( answer.equals("7") && ( guess.equals("4") || guess.equals("5") ) )
 		answer = "3";
 	else if ( answer.equals("8") && ( guess.equals("0") ) )
 		answer = "1";
 	else if ( answer.equals("8") && ( guess.equals("3") || guess.equals("4") || guess.equals("5") ) )
 		answer = "2";
 	else if ( answer.equals("9") )
 		answer = "1";
 		
 	Integer answerInt = new java.lang.Integer( answer );
 	Integer guessInt = new java.lang.Integer( guess );
 	
 	return new java.lang.Integer( 4 * java.lang.Math.abs( answerInt.intValue() - guessInt.intValue()  ) ).toString();
 		
   } // End of gradePrec()
   
   public static String gradeTemp( String answer, String guess) {
   	Integer answerInt = new java.lang.Integer( answer );
 	Integer guessInt = new java.lang.Integer( guess );
 	return new java.lang.Integer( java.lang.Math.abs( answerInt.intValue() - guessInt.intValue()  ) ).toString();
 	
   }
   
 } // End of fLib
