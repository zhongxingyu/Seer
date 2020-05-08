 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.pivotal;
 
 import com.gooddata.exception.GdcLoginException;
 import com.gooddata.exception.GdcRestApiException;
 import com.gooddata.exception.HttpMethodException;
 import com.gooddata.exception.InvalidParameterException;
 import com.gooddata.util.CSVReader;
 import com.gooddata.util.CSVWriter;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.XPathReader;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthPolicy;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.*;
 import java.net.HttpCookie;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 
 /**
  * GoodData Pivotal API wrapper
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class PivotalApi {
 
     private static Logger l = Logger.getLogger(PivotalApi.class);
 
     /**
      * PT username
      */
     private String userName;
     /**
      * PT password
      */
     private String password;
 
     /**
      * PT project ID (integer)
      */
     private String projectId;
 
     /**
      * List of column headers that will be included in the STORY dataset
      */
     private Set RECORD_STORIES = new HashSet();
 
     /**
      * List of column headers that will be duplicated (both ATTRIBUTE and VALUE) in the STORY dataset
      */
     private Set DUPLICATE_IN_STORIES = new HashSet();
 
     /**
      * List of DATE column headers (we need to convert dates to the ISO format)
      */
     private Set DATE_COLUMNS = new HashSet();
     /**
      * Labels column header
      */
     private String HEADER_LABEL = "Labels";
 
     private static String PIVOTAL_URL = "https://www.pivotaltracker.com";
 
     /**
      * Id column header
      */
     private String HEADER_STORY_ID = "Id";
     /**
      * Shared HTTP client
      */
     private HttpClient client = new HttpClient();
 
     /**
      * The Pivotal API wrapper constructor
      * @param usr - PT username
      * @param psw - PT password
      * @param prjId  - PT project ID (integer)
      */
     public PivotalApi(String usr, String psw, String prjId) {
         this.setUserName(usr);
         this.setPassword(psw);
         this.setProjectId(prjId);
         client.getHostConfiguration().setHost(PIVOTAL_URL);
         // populate the STORY dataset columns
         RECORD_STORIES.addAll(Arrays.asList(new String[] {"Id", "Labels", "Story", "Iteration", "Iteration Start",
                "Iteration End", "Story Type", "Estimate", "Current State", "Created at", "Accepted at", "Deadline",
                 "Requested By", "Owned By", "URL"}));
         DUPLICATE_IN_STORIES.add("Iteration");
 
        DATE_COLUMNS.addAll(Arrays.asList(new String[] {"Iteration Start", "Iteration End", "Created at", "Accepted at",
                 "Deadline"}));
     }
 
 
     /**
      * Get token
      * @throws Exception in case of an IO error
      */
     public void getToken() throws IOException {
         PostMethod m = new PostMethod("/services/tokens/active");
         m.getParams().setCookiePolicy(CookiePolicy.NETSCAPE);
         m.setParameter("username",getUserName());
         m.setParameter("password",getPassword());
         try {
             client.executeMethod(m);
             System.err.println(m.getResponseBodyAsString());
             if (m.getStatusCode() != HttpStatus.SC_OK && m.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                 throw new InvalidParameterException("Invalid PT credentials. HTTP reply code "+m.getStatusCode());
             }
         }
         finally {
             m.releaseConnection();
         }
     }
 
     private String authCookie = "";
 
     /**
      * Sign into the PT
      * @throws Exception in case of an IO error
      */
     public void signin() throws IOException {
         PostMethod m = new PostMethod(PIVOTAL_URL+"/signin");
         m.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
         m.addParameter("credentials[username]",getUserName());
         m.addParameter("credentials[password]",getPassword());
         try {
             client.executeMethod(m);
             if (m.getStatusCode() != HttpStatus.SC_OK && m.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                 throw new InvalidParameterException("Invalid PT credentials. HTTP reply code "+m.getStatusCode());
             }
             Header[] cookies = m.getResponseHeaders("Set-Cookie");
             for(int i=0; i < cookies.length; i++) {
                 String value = cookies[i].getValue();
                 if(i==0)
                     authCookie += value.split(";")[0];
                 else
                     authCookie += "; "+value.split(";")[0];
             }
             Header l = m.getResponseHeader("Location");
             String location = l.getValue();
             GetMethod gm = new GetMethod(location);
             gm.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
             gm.setRequestHeader("Cookie",authCookie);
             client.executeMethod(gm);
             if (gm.getStatusCode() != HttpStatus.SC_OK && gm.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
                 throw new InvalidParameterException("Invalid PT credentials. HTTP reply code "+m.getStatusCode());
             }
         }
         finally {
             m.releaseConnection();
         }
     }
 
     /**
      * Retrieves the PT data in the CSV format
      * @param ptCsv - the filename to store the PT CSV data
      * @throws Exception in case of an IO error
      */
     public void getCsvData(String ptCsv) throws IOException {
         String url = PIVOTAL_URL+"/projects/"+getProjectId()+"/export/";
         PostMethod m = new PostMethod(url);
       
         m.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
         m.setRequestHeader("Cookie",authCookie);
         m.addParameter("options[include_current_backlog_stories]","1");
         m.addParameter("options[include_icebox_stories]","1");
         m.addParameter("options[include_done_stories]","1");
 
         try {
             client.executeMethod(m);
             if (m.getStatusCode() == HttpStatus.SC_OK) {
                 final int BUFLEN = 2048;
                 byte[] buf = new byte[BUFLEN];
                 BufferedInputStream is = new BufferedInputStream(m.getResponseBodyAsStream());
                 BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(ptCsv));
                 int cnt = is.read(buf,0,BUFLEN);
                 while(cnt >0) {
                     os.write(buf, 0, cnt);
                     cnt = is.read(buf,0,BUFLEN);
                 }
                 is.close();
                 os.flush();
                 os.close();
             }
             else {
                 throw new InvalidParameterException("Error retrieving the PT data. HTTP reply code "+m.getStatusCode());
             }
         }
         finally {
             m.releaseConnection();
         }
     }
 
     /**
      * Writes a record to CSV writer
      * @param cw CSV writer
      * @param rec record as a list
      */
     private void writeRecord(CSVWriter cw, List<String> rec) {
         cw.writeNext(rec.toArray(new String[] {}));
     }
 
     private DateTimeFormatter reader = DateTimeFormat.forPattern("MMM dd, yyyy");
     private DateTimeFormatter writer = DateTimeFormat.forPattern("yyyy-MM-dd");
 
     /**
      * Converts the date format (if needed)
      * @param header the CSV column header
      * @param value the value
      * @return the converted date
      */
     private String convertDate(String header, String value) {
         if(DATE_COLUMNS.contains(header)) {
             if(value != null && value.length()>0) {
                 DateTime dt = null;
                 try {
                     dt = reader.parseDateTime(value);
                 } catch (IllegalArgumentException e) {
                     l.debug("Error parsing PT date value '"+value+"'");
                 }
                 return writer.print(dt);
             }
             else {
                 return "";
             }
         }
         else
             return value;
     }
 
     /**
      * Parses the PT CSV file into the STORY, LABEL, and LABEL_TO_STORY CSV files
      * @param csvFile the incoming PT CSV file
      * @param storiesCsv the output STORY CSV file
      * @param labelsCsv the output LABEL CSV file
      * @param labelsToStoriesCsv  the output LABEL_TO_STORY CSV file
      * @throws Exception in case of an IO issue
      */
     public void parse(String csvFile, String storiesCsv, String labelsCsv, String labelsToStoriesCsv, DateTime t) throws IOException {
         String today = writer.print(t);
         CSVReader cr = FileUtil.createUtf8CsvReader(new File(csvFile));
         String[] row = cr.readNext();
         if(row != null && row.length > 0) {
             List<String> headers = Arrays.asList(row);
             List<String> storiesRecord = new ArrayList<String>();
             List<String> labelsRecord = new ArrayList<String>();
             List<String> labelsToStoriesRecord = new ArrayList<String>();
 
             CSVWriter storiesWriter = new CSVWriter(new FileWriter(storiesCsv));
             CSVWriter labelsWriter = new CSVWriter(new FileWriter(labelsCsv));
             CSVWriter labelsToStoriesWriter = new CSVWriter(new FileWriter(labelsToStoriesCsv));
 
             labelsRecord.add("cpId");
             labelsRecord.add("Label Id");
             labelsRecord.add("Label");
             labelsToStoriesRecord.add("cpId");
             labelsToStoriesRecord.add("Story Id");
             labelsToStoriesRecord.add("Label Id");
 
 
             for(String header : headers) {
                 if(RECORD_STORIES.contains(header)) {
                     storiesRecord.add(header);
                     if(DUPLICATE_IN_STORIES.contains(header))
                         storiesRecord.add(header);
                 }
             }
             storiesRecord.add(0, "SnapshotDate");
             storiesRecord.add(0, "cpId");
             writeRecord(storiesWriter, storiesRecord);
             writeRecord(labelsWriter, labelsRecord);
             writeRecord(labelsToStoriesWriter, labelsToStoriesRecord);
 
             Map<String,String> labels = new HashMap<String, String>();
             row = cr.readNext();
             while(row != null && row.length > 1) {
                 storiesRecord.clear();
                 labelsRecord.clear();
                 labelsToStoriesRecord.clear();
                 String storyId = "";
                 String label = "";
                 String key = "";
                 for(int i=0; i < headers.size(); i++) {
                     String header = headers.get(i);
                     if(RECORD_STORIES.contains(header)) {
                         key += row[i] + "|";
                         storiesRecord.add(convertDate(header, row[i]));
                         if(DUPLICATE_IN_STORIES.contains(header))
                             storiesRecord.add(convertDate(header, row[i]));
 
                     }
                     if(HEADER_LABEL.equals(header)) {
                         label = row[i];
                     }
                 }
                 storyId = DigestUtils.md5Hex(key);
                 storiesRecord.add(0, today);
                 storiesRecord.add(0, storyId);
                 String[] lbls = label.split(",");
                 for(String lbl : lbls) {
                     lbl = lbl.trim();
                     if(lbl.length() > 0) {
                         if(labels.containsKey(lbl)) {
                             String lblId = labels.get(lbl);
                             labelsToStoriesRecord.add(storyId);
                             labelsToStoriesRecord.add(lblId);
                             labelsToStoriesRecord.add(0, DigestUtils.md5Hex(storyId + "|" + lblId));
                             writeRecord(labelsToStoriesWriter, labelsToStoriesRecord);
                         }
                         else {
                             String id = DigestUtils.md5Hex(lbl);
                             labels.put(lbl, id);
                             labelsRecord.add(lbl);
                             labelsRecord.add(0, id);
                             labelsToStoriesRecord.add(storyId);
                             labelsToStoriesRecord.add(id);
                             writeRecord(labelsWriter, labelsRecord);
                             labelsToStoriesRecord.add(0, DigestUtils.md5Hex(storyId + "|" + id));
                             writeRecord(labelsToStoriesWriter, labelsToStoriesRecord);
                         }
                     }
                     labelsRecord.clear();
                     labelsToStoriesRecord.clear();                   
                 }
                 writeRecord(storiesWriter, storiesRecord);
                 row = cr.readNext();
             }
             storiesWriter.flush();
             storiesWriter.close();
             labelsWriter.flush();
             labelsWriter.close();
             labelsToStoriesWriter.flush();
             labelsToStoriesWriter.close();
         }
         else {
             throw new InvalidParameterException("The Pivotal extract doesn't contain any row.");
         }
 
 
 
     }
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getProjectId() {
         return projectId;
     }
 
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
 
 }
