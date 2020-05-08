 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service.rule;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 public class RuleHelperService {
 
 	private static TangerineUserHelper userHelper;
 	private static ConstituentService constituentService;
 
     public ConstituentService getConstituentService() {
 		return constituentService;
 	}
 
 	public void setConstituentService(ConstituentService constituentService) {
 		this.constituentService = constituentService;
 	}
 
 	public TangerineUserHelper getUserHelper() {
 		return userHelper;
 	}
 
 	public void setUserHelper(TangerineUserHelper userHelper) {
 		this.userHelper = userHelper;
 	}
	
	public static String trimToNull(String str) {
		return StringUtils.trimToNull(str);
	}
 
 	// Returns a Date object with a value of the time at the beginning of the year
     public static Date getBeginningOfYearDate() {
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.YEAR, 0, 0, 0, 0, 0);
         return cal.getTime();
     }
 
     // Returns a Date object as the current date/time
     public static Date getCurrentDate() {
         Calendar cal = Calendar.getInstance();
         return cal.getTime();
     }
 
     // Returns a Date object from a time you specify, e.g. 2 months ago
     public static Date getBeginDate(int number, String timeUnit) {
         Calendar cal = Calendar.getInstance();
         StringBuilder args = new StringBuilder(timeUnit.toUpperCase());
         if (args.toString().equals("MONTHS") || args.toString().equals("YEARS")) {
             args.deleteCharAt(args.length() - 1);
         }
         if (args.toString().equals("MONTH")) {
             cal.add(Calendar.MONTH, -(number));
         }
         if (args.toString().equals("YEAR")) {
             cal.add(Calendar.YEAR, -(number));
         }
         return cal.getTime();
     }
 
     // Returns an integer as the month of the transaction date
     public static int getMonthOfTransaction(Date date) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         return cal.get(Calendar.MONTH);
     }
 
     // Returns a Date object with the value of the time at the beginning of the month
     public static Date getBeginningOfMonthDate(int number) {
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.MONTH, -(number));
         cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getMinimum(Calendar.DAY_OF_MONTH), 0, 0, 0);
         return cal.getTime();
     }
 
     // Returns a Date object with the value of the time at the end of the month
     public static Date getEndOfMonthDate(int number) {
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.MONTH, -(number));
         cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getMaximum(Calendar.DAY_OF_MONTH), 0, 0, 0);
         return cal.getTime();
     }
 
     // Returns a boolean value of whether or not one is a monthly donor
     public static boolean analyzeMonthlyDonor(ArrayList<Object> giftObjects, int numberOfMatches, int numberOfMonths) {
 
         int numMatches = 0;
 
         ArrayList<Gift> gifts = new ArrayList<Gift>();
 
         Gift gift = null;
 
         for (Object g : giftObjects) {
             if (g instanceof Gift) {
                 gift = (Gift) g;
                 gifts.add(gift);
             }
         }
 
         // Cycle through the last x months
         for (int i = 0; i < numberOfMonths; i++) {
             // For each gift
             for (Gift g : gifts) {
                 // If there is a gift in month x, add to number of matches
                 if ((g.getTransactionDate().after(getBeginningOfMonthDate(i))) && (g.getTransactionDate().before(getEndOfMonthDate(i)))) {
                     numMatches += 1;
                     if (numberOfMatches == numMatches) {
                         return true;
                     }
                     break;
                 }
 
             }
 
         }
         return false;
 
     }
 
     // For the logger
     public static Log getLogger() {
         final Log logger = OLLogger.getLog(RuleHelperService.class);
         return logger;
     }
 
     public static Date getBeginDate(int number, String timeUnit, int fiscalYearStartingMonth ) {
     	Calendar cal = Calendar.getInstance();
     	StringBuilder args = new StringBuilder(timeUnit.toUpperCase());
     	if (args.toString().equals("DAYS") || args.toString().equals("WEEKS") || args.toString().equals("MONTHS") || args.toString().equals("YEARS") || args.toString().equals("FISCALYEARS")) {
     		args.deleteCharAt(args.length()-1);
     	}
     	if (args.toString().equals("DAY")) {
     		cal.add(Calendar.DATE, -(number));
     	}
     	else if (args.toString().equals("WEEK")) {
     		cal.add(Calendar.WEEK_OF_YEAR, -(number));
     	}
     	else if (args.toString().equals("MONTH")) {
     		cal.add(Calendar.MONTH, -(number));
     	}
     	else if (args.toString().equals("YEAR")) {
     		cal.add(Calendar.YEAR, -(number));
     	}
     	else if (args.toString().equals("FISCALYEAR")) {
     	    Calendar fiscalYearEnd = Calendar.getInstance();
     	    //subtract 1 from fiscalYearStartingMonth because Jan = 0
     	    fiscalYearEnd.set(Calendar.MONTH, fiscalYearStartingMonth - 1);
     	    fiscalYearEnd.set(Calendar.DAY_OF_MONTH,1);
     	    fiscalYearEnd.set(Calendar.HOUR_OF_DAY, 0);
             fiscalYearEnd.set(Calendar.MILLISECOND, 0);
             fiscalYearEnd.set(Calendar.SECOND, 0);
             fiscalYearEnd.set(Calendar.MINUTE, 0);
 
     	    if (fiscalYearEnd.compareTo(cal) == 1) {
     	        // Fiscal Year end is past today
     	        fiscalYearEnd.add(Calendar.YEAR,-(number));
     	       //System.out.println("Begin Date = " + fiscalYearEnd.getTime() );
     	        return fiscalYearEnd.getTime();
     	    } else {
     	        // Fiscal Year end is before today
     	        fiscalYearEnd.add(Calendar.YEAR,-(number - 1));
     	        // System.out.println("Begin Date = " + fiscalYearEnd.getTime() );
     	        return fiscalYearEnd.getTime();
             }
     	}
 
 
     	return cal.getTime();
     }
 
 
     /**
      * Takes a constituent and returns the number of gifts that constituent has given in the past timeAmount timeUnits
      * (i.e. past 6 MONTHS or 1 YEAR or 2 FISCALYEARS)
      * If the timeAmount and timeUnit are -1/null then it will return the total number of gifts a constituent has given.
      * @param constituent
      * @param timeAmount
      * @param timeUnits
      * @param fiscalYearStartingMonth
      * @return
      *
      */
     public static int numberOfDonationsMadePerTimeFrame(Constituent constituent, int timeAmount, String timeUnit ) {
     	int numberOfMatches = 0;
         int fiscalYearStartingMonth = -1;
     	List<Gift> gifts = constituent.getGifts();
 
         if ( (timeAmount == -1) || (timeUnit == null)){
         	return gifts.size();
         }
 
     	if(timeUnit.equalsIgnoreCase("FISCALYEAR") || timeUnit.equalsIgnoreCase("FISCALYEARS")){
     		fiscalYearStartingMonth = Integer.parseInt(userHelper.getSiteOptionByName("fiscal.year.starting.month"));
     	}
 
         // Cycle through the gifts
         for (Gift g : gifts) {
             // If there is a gift given after the beginning date add it to the number of matches.
             if ((g.getDonationDate().after(getBeginDate(timeAmount, timeUnit, fiscalYearStartingMonth)))) {
             	numberOfMatches++;
             }
         }
         return numberOfMatches;
     }
 
     private static Boolean isDuplicate(Constituent p, String dupDetectCriteria) {
    		Map<String,Object> params = new HashMap<String, Object>();
     	List<Long> ignoreIds = new ArrayList<Long>();
     	long ignoreId = p.getId();
     	ignoreIds.add(ignoreId);
 
 		//System.out.println ("criteria: " + dupDetectCriteria);
 
 		//parse the string and put each into the params map
     	String[] duplicateDetectCriteriaArray = dupDetectCriteria.split(",");
         int paramCount = 0;
 
         for(int i = 0;i < duplicateDetectCriteriaArray.length;i++){
             String[] duplicateDetectCriteria = duplicateDetectCriteriaArray[i].split(":");
             String criteria = duplicateDetectCriteria[0];
             int numCharactersToMatch = Integer.parseInt(duplicateDetectCriteria[1]);
 
 
         //If it is a constituent then match on first and last name if org match on orgname
         if (p.isIndividual()){
            if ( criteria.equalsIgnoreCase("firstName") && p.getFirstName() != null){
            		params.put(criteria, p.getFirstName().substring(0, ((numCharactersToMatch > p.getFirstName().length()) ? p.getFirstName().length() : numCharactersToMatch)));
            		paramCount++;
            }
            if ( criteria.equalsIgnoreCase("lastName") && p.getLastName() != null){
            		params.put(criteria, p.getLastName().substring(0, ((numCharactersToMatch > p.getLastName().length()) ? p.getLastName().length() : numCharactersToMatch)));
            		paramCount++;
            }
         }else if (p.isOrganization()){
            if ( criteria.equalsIgnoreCase("organizationName") && p.getOrganizationName() != null){
            		params.put(criteria, p.getOrganizationName().substring(0, ((numCharactersToMatch > p.getOrganizationName().length()) ? p.getOrganizationName().length() : numCharactersToMatch)));
            		paramCount++;
            }
 
         }
 		/* TODO: Implement match on account number and email address
           if ( criteria.equalsIgnoreCase("accountNumber"))
            		params.put(criteria, p.getAccountNumber().ToString().substring(0,numCharactersToMatch));
 		*/
 			//Primary Email Info
            if ( criteria.equalsIgnoreCase("emailAddress")){
            		params.put("primaryEmail." + criteria, p.getPrimaryEmail().getEmailAddress().substring(0, ((numCharactersToMatch > p.getPrimaryEmail().getEmailAddress().length()) ? p.getPrimaryEmail().getEmailAddress().length() : numCharactersToMatch)));
          		paramCount++;
          	}
 
 			//Primary Address Info
             if ( criteria.equalsIgnoreCase("addressLine1") && p.getPrimaryAddress().getAddressLine1() != null){
            		params.put("primaryAddress." + criteria, p.getPrimaryAddress().getAddressLine1().substring(0, ((numCharactersToMatch > p.getPrimaryAddress().getAddressLine1().length()) ? p.getPrimaryAddress().getAddressLine1().length() : numCharactersToMatch)));
            		paramCount++;
            	}
             if ( criteria.equalsIgnoreCase("stateProvince") && p.getPrimaryAddress().getStateProvince() != null){
            		params.put("primaryAddress." + criteria, p.getPrimaryAddress().getStateProvince().substring(0, ((numCharactersToMatch > p.getPrimaryAddress().getStateProvince().length()) ? p.getPrimaryAddress().getStateProvince().length() : numCharactersToMatch)));
            		paramCount++;
            	}
             if ( criteria.equalsIgnoreCase("city") && p.getPrimaryAddress().getCity() != null){
            		params.put("primaryAddress." + criteria, p.getPrimaryAddress().getCity().substring(0, ((numCharactersToMatch > p.getPrimaryAddress().getCity().length()) ? p.getPrimaryAddress().getCity().length() : numCharactersToMatch)));
            		paramCount++;
            	}
             if ( criteria.equalsIgnoreCase("postalCode") && p.getPrimaryAddress().getPostalCode() != null){
            		params.put("primaryAddress." + criteria, p.getPrimaryAddress().getPostalCode().substring(0, ((numCharactersToMatch > p.getPrimaryAddress().getPostalCode().length()) ? p.getPrimaryAddress().getPostalCode().length() : numCharactersToMatch)));
            		paramCount++;
            	}
 		}
 
 		List<Constituent> constituents = null;
 		if (paramCount > 0){
 			params.put("byPassDuplicateDetection",0);
 			SortInfo sortInfo = new SortInfo();
 			sortInfo.setSort("accountNumber");
 			sortInfo.setStart(0);
 
 	    	constituents = constituentService.searchConstituents(params, true, sortInfo, Locale.US );
 	    }
 
 
 		if (constituents != null && constituents.size() > 1) return true;
     	return false;
 }
 
 
 
 }
