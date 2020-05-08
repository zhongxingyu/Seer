 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr.photos;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import com.aetrion.flickr.Parameter;
 import com.aetrion.flickr.util.StringUtilities;
 
 /**
  * @author Anthony Eden
  */
 public class SearchParameters {
 
     private String userId;
     private String[] tags;
     private String tagMode;
     private String text;
     private Date minUploadDate;
     private Date maxUploadDate;
     private Date minTakenDate;
     private Date maxTakenDate;
     private Date interestingnessDate;
     private String license;
     private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
 	private boolean extrasLicense = false;
 	private boolean extrasDateUpload = false;
 	private boolean extrasDateTaken = false;
 	private boolean extrasOwnerName = false;
 	private boolean extrasIconServer = false;
 	private boolean extrasOriginalFormat = false;
 
 	/** order argument */
 	public static int DATE_POSTED_DESC = 0;
 	/** order argument */
 	public static int DATE_POSTED_ASC = 1;
 	/** order argument */
 	public static int DATE_TAKEN_DESC = 2;
 	/** order argument */
 	public static int DATE_TAKEN_ASC = 3;
 	/** order argument */
 	public static int INTERESTINGNESS_DESC = 4;
 	/** order argument */
 	public static int INTERESTINGNESS_ASC = 5;
 	/** order argument */
 	public static int RELEVANCE = 6;
 	private int sort = 0;
 
     public SearchParameters() {
 
     }
 
     public String getUserId() {
         return userId;
     }
 
     public void setUserId(String userId) {
         this.userId = userId;
     }
 
     public String[] getTags() {
         return tags;
     }
 
     public void setTags(String[] tags) {
         this.tags = tags;
     }
 
     public String getTagMode() {
         return tagMode;
     }
 
     public void setTagMode(String tagMode) {
         this.tagMode = tagMode;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public Date getMinUploadDate() {
         return minUploadDate;
     }
 
     public void setMinUploadDate(Date minUploadDate) {
         this.minUploadDate = minUploadDate;
     }
 
     public Date getMaxUploadDate() {
         return maxUploadDate;
     }
 
     public void setMaxUploadDate(Date maxUploadDate) {
         this.maxUploadDate = maxUploadDate;
     }
 
     public Date getMinTakenDate() {
         return minTakenDate;
     }
 
     public void setMinTakenDate(Date minTakenDate) {
         this.minTakenDate = minTakenDate;
     }
 
     public Date getMaxTakenDate() {
         return maxTakenDate;
     }
 
     public void setMaxTakenDate(Date maxTakenDate) {
         this.maxTakenDate = maxTakenDate;
     }
 
     public String getLicense() {
         return license;
     }
 
     public void setLicense(String license) {
         this.license = license;
     }
 
     public Date getInterestingnessDate() {
         return interestingnessDate;
     }
     
     /**
      * Set the date, for which interesting Photos to request.
      * 
      * @param intrestingnessDate
      */
     public void setInterestingnessDate(Date intrestingnessDate) {
         this.interestingnessDate = intrestingnessDate;
     }
 	
     /**
      * Setting all toogles to get extra-fields in Photos-search.<br>
      * The default is false.
      * 
      * @param toggle to include or exclude all extra fields.
      */
     public void setExtras(boolean toggle) {
         setExtrasLicense(toggle);
         setExtrasDateUpload(toggle);
         setExtrasDateTaken(toggle);
         setExtrasOwnerName(toggle);
         setExtrasIconServer(toggle);
         setExtrasOriginalFormat(toggle);
     }
 	
     public void setExtrasLicense(boolean toggle) {
         this.extrasLicense = toggle;
     }
     public void setExtrasDateUpload(boolean toggle) {
         this.extrasDateUpload = toggle;
     }
     public void setExtrasDateTaken(boolean toggle) {
         this.extrasDateTaken = toggle;
     }
     public void setExtrasOwnerName(boolean toggle) {
         this.extrasOwnerName = toggle;
     }
     public void setExtrasIconServer(boolean toggle) {
         this.extrasIconServer = toggle;
     }
     public void setExtrasOriginalFormat(boolean toggle) {
         this.extrasOriginalFormat = toggle;
     }
 	
     public int getSort() {
         return sort;
     }
 	
     /**
      * Set the sort-order to one of the following constants 
      * DATE_POSTED_ASC, DATE_TAKEN_DESC, DATE_TAKEN_ASC, 
      * INTERESTINGNESS_DESC, INTERESTINGNESS_ASC, RELEVANCE<br>
      * The default is DATE_POSTED_DESC
      * 
      * @param order constant
      */
     public void setSort(int sort) {
         this.sort = sort;
     }
 	
     public Collection getAsParameters() {
         List parameters = new ArrayList();
 
         String userId = getUserId();
         if (userId != null) {
             parameters.add(new Parameter("user_id", userId));
         }
 
         String[] tags = getTags();
         if (tags != null) {
             parameters.add(new Parameter("tags", StringUtilities.join(tags, ",")));
         }
 
         String tagMode = getTagMode();
         if (tagMode != null) {
             parameters.add(new Parameter("tag_mode", tagMode));
         }
 
         String text = getText();
         if (text != null) {
             parameters.add(new Parameter("text", text));
         }
 
         Date minUploadDate = getMinUploadDate();
         if (minUploadDate != null) {
             parameters.add(new Parameter("min_upload_date", new Long(minUploadDate.getTime())));
         }
 
         Date maxUploadDate = getMaxUploadDate();
         if (maxUploadDate != null) {
             parameters.add(new Parameter("max_upload_date", new Long(maxUploadDate.getTime())));
         }
 
        Date minTakenDate = getMinUploadDate();
         if (minTakenDate != null) {
             parameters.add(new Parameter("min_taken_date", new Long(minTakenDate.getTime())));
         }
 
        Date maxTakenDate = getMinUploadDate();
         if (maxTakenDate != null) {
             parameters.add(new Parameter("max_taken_date", new Long(maxTakenDate.getTime())));
         }
 
         String license = getLicense();
         if (license != null) {
             parameters.add(new Parameter("license", license));
         }
         
         Date intrestingnessDate = getInterestingnessDate();
         if (intrestingnessDate != null) {
             parameters.add(new Parameter("date", DF.format(intrestingnessDate)));
         }
         
         if(extrasLicense || extrasDateUpload ||
            extrasDateTaken || extrasOwnerName ||
            extrasIconServer || extrasOriginalFormat) {
             Vector argsList = new Vector();
             if(extrasLicense) argsList.add("license");
             if(extrasDateUpload) argsList.add("date_upload");
             if(extrasDateTaken) argsList.add("date_taken");
             if(extrasOwnerName) argsList.add("owner_name");
             if(extrasIconServer) argsList.add("icon_server");
             if(extrasOriginalFormat) argsList.add("original_format");
             parameters.add(new Parameter("extras", StringUtilities.join(argsList,",")));
         }
     	
         if(sort != DATE_POSTED_DESC) {
             String sortArg = null;
             if(sort == DATE_POSTED_ASC) sortArg = "date-posted-asc";
             if(sort == DATE_TAKEN_DESC) sortArg = "date-taken-desc";
             if(sort == DATE_TAKEN_ASC) sortArg = "date-taken-asc";
             if(sort == INTERESTINGNESS_DESC) sortArg = "interestingness-desc";
             if(sort == INTERESTINGNESS_ASC) sortArg = "interestingness-asc";
             if(sort == RELEVANCE) sortArg = "relevance";
             if(sortArg != null) parameters.add(new Parameter("sort", sortArg));
         }
 		
         return parameters;
     }
 }
