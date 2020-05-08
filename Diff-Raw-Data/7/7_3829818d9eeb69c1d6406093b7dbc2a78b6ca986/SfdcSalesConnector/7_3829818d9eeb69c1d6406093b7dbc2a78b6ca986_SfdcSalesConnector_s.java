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
 package com.gooddata.connector;
 
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.exception.InvalidParameterException;
 import com.gooddata.exception.ProcessingException;
 import com.gooddata.exception.SfdcException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.modeling.generator.MaqlGenerator;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.CSVWriter;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 import com.sforce.soap.partner.Field;
 import com.sforce.soap.partner.SoapBindingStub;
 import com.sforce.soap.partner.sobject.SObject;
 import org.apache.axis.message.MessageElement;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.MDC;
 import org.joda.time.DateTime;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * SFDC snapshotting connector
  *
  * @author zd@gooddata.com
  * @version 1.0
  */
 public class SfdcSalesConnector extends SfdcConnector {
 
     private static Logger l = Logger.getLogger(SfdcSalesConnector.class);
 
     public static final String SFDC_ID_NAME = "Id";
     public static final String SFDC_ACCOUNT_REF_NAME = "AccountId";
     public static final String SFDC_USER_REF_NAME = "OwnerId";
 
     private SourceSchema accountSchema;
     private SourceSchema userSchema;
     private SourceSchema oppSchema;
     private SourceSchema snapshotSchema;
 
     private String accountQuery;
     private String userQuery;
     private String oppQuery;
     private String snapshotQuery;
 
 
     /**
         * Creates a new SFDC connector
         */
        protected SfdcSalesConnector() {
            super();
        }
 
       /**
         * Creates a new SFDC connector
         * @return a new instance of the SFDC connector
         */
        public static SfdcSalesConnector createConnector2() {
            return new SfdcSalesConnector();
        }
 
 
     /**
      * Initializes the source and PDM schemas from the config file
      *
      * @param ac the account config file
      * @param uc the user config file
      * @param oc the opportunity config file
      * @param sc the snapshot config file
      * @throws IOException in cas the config file doesn't exists
      */
     protected void initSchema(String ac, String uc, String oc, String sc) throws IOException {
         accountSchema = SourceSchema.createSchema(new File(ac));
         userSchema = SourceSchema.createSchema(new File(uc));
         oppSchema = SourceSchema.createSchema(new File(oc));
         snapshotSchema = SourceSchema.createSchema(new File(sc));
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Map<String, String> extract(SourceSchema schema, String query, String dir, Map<String, String> accountIds,
                                  Map<String, String> userIds, Map<String, String> oppIds) throws IOException {
         l.debug("Extracting SFDC data.");
 
         // Is there an IDENTITY connection point?
         final int identityColumn = schema.getIdentityColumn();
 
         Map<String, String> r = new HashMap<String, String>();
 
         DateTime snapshotDate = new DateTime();
         String snapshotDateText = Constants.DEFAULT_DATE_FMT.print(snapshotDate);
         String snapshotMillis = Long.toString(snapshotDate.getMillis());
 
         File dataFile = new File(dir + System.getProperty("file.separator") + "data.csv");
         l.debug("Extracting SFDC data to file=" + dataFile.getAbsolutePath());
         CSVWriter cw = FileUtil.createUtf8CsvEscapingWriter(dataFile);
         String[] header = this.populateCsvHeaderFromSchema(schema);
 
         // add the extra date headers
         DateColumnsExtender dateExt = new DateColumnsExtender(schema);
         header = dateExt.extendHeader(header);
 
         cw.writeNext(header);
         SoapBindingStub c = connect(getSfdcUsername(), getSfdcPassword(), getSfdcToken());
         List<SObject> result;
         try {
             result = executeQuery(c, query);
         } catch (SfdcException e) {
             l.debug("SFDC query execution failed.", e);
             throw new InternalErrorException("SFDC query execution failed: ", e);
         }
         if (result != null && result.size() > 0) {
             l.debug("Started retrieving SFDC data.");
             SObject firstRow = result.get(0);
             Map<String, Field> fields = describeObject(c, firstRow.getType());
             MessageElement[] frCols = firstRow.get_any();
             String[] colTypes = new String[frCols.length];
             for (int i = 0; i < frCols.length; i++) {
                 String nm = frCols[i].getName();
                 colTypes[i] = getColumnType(fields, nm);
             }
             for (SObject row : result) {
                 MessageElement[] cols = row.get_any();
                 List<String> vals = new ArrayList<String>();
                 StringBuffer digestData = new StringBuffer();
                 String id = "";
                 for (int i = 0; i < cols.length; i++) {
                     String val = cols[i].getValue();
                     String nm = frCols[i].getName();
                     if (nm.equalsIgnoreCase(SFDC_ID_NAME)) {
                         id = val;
                        if(oppIds != null)
                            val = oppIds.get(val);
                     }
                     if (nm.equalsIgnoreCase(SFDC_ACCOUNT_REF_NAME) && accountIds != null)
                         val = accountIds.get(val);
                     if (nm.equalsIgnoreCase(SFDC_USER_REF_NAME) && userIds != null)
                         val = userIds.get(val);
 
                     if (colTypes[i].equals(SourceColumn.LDM_TYPE_DATE)) {
                         if (val != null && val.length() > 0)
                             val = val.substring(0, 10);
                         else
                             val = "";
                     }
                     else if(colTypes[i].equals(SourceColumn.LDM_TYPE_FACT)) {
                         if(val != null && val.length()>0) {
                             try {
                                 double d = Double.parseDouble(val);
                                 val = nf.format(d);
                             }
                             catch (NumberFormatException e) {
                                 val = "";
                             }
                         }
                         else {
                             val = "";
                         }
                     }
                     vals.add(val);
                     digestData.append(val);
                 }
                 // processing final snapshots
                 if(oppIds != null && accountIds != null && userIds != null) {
                     vals.add(0, snapshotMillis);
                     vals.add(0, snapshotDateText);
                 }
                 if(identityColumn >=0) {
                     String digest = DigestUtils.md5Hex(digestData.toString());
                     r.put(id, digest);
                     vals.add(identityColumn, digest);
                 }
                 // add the extra date columns
                 String[] data = dateExt.extendRow(vals.toArray(new String[]{}));
                 cw.writeNext(data);
             }
             l.debug("Retrieved " + result.size() + " SFDC data.");
             cw.flush();
             cw.close();
             l.debug("Extracted SFDC date.");
             return r;
         } else {
             l.debug("The SFDC account query hasn't returned any row.");
             throw new SfdcException("The SFDC account query hasn't returned any row.");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void extract(String dir) throws IOException {
     }
 
     private Map<String,String> transfer(SourceSchema schema, String query,
                           Command c, String pid, boolean waitForFinish, CliParams p, ProcessingContext ctx,
                           Map<String, String> accountIds, Map<String, String> userIds, Map<String, String> oppIds
                         ) throws IOException, InterruptedException {
         Map<String,String> r = null;
         File tmpDir = FileUtil.createTempDir();
         File tmpZipDir = FileUtil.createTempDir();
         String archiveName = tmpDir.getName();
         MDC.put("GdcDataPackageDir",archiveName);
         String archivePath = tmpZipDir.getAbsolutePath() + System.getProperty("file.separator") +
             archiveName + ".zip";
 
         // get information about the data loading package
         String ssn = StringUtil.toIdentifier(schema.getName());
         SLI sli = ctx.getRestApi(p).getSLIById("dataset." + ssn, pid);
         List<Column> sliColumns = ctx.getRestApi(p).getSLIColumns(sli.getUri());
         List<Column> columns = populateColumnsFromSchema(schema);
         if(sliColumns.size() > columns.size())
             throw new InvalidParameterException("The GoodData data loading interface (SLI) expects more columns.");
         String incremental = c.getParam("incremental");
         if(incremental != null && incremental.length() > 0 &&
                 incremental.equalsIgnoreCase("true")) {
             l.debug("Using incremental mode.");
             setIncremental(columns);
         }
 
         // extract the data to the CSV that is going to be transferred to the server
         r = extract(schema, query, tmpDir.getAbsolutePath(), accountIds, userIds, oppIds);
         this.deploy(sli, columns, tmpDir.getAbsolutePath(), archivePath);
         // transfer the data package to the GoodData server
         ctx.getFtpApi(p).transferDir(archivePath);
         // kick the GooDData server to load the data package to the project
         String taskUri = ctx.getRestApi(p).startLoading(pid, archiveName);
         if(waitForFinish) {
             checkLoadingStatus(taskUri, tmpDir.getName(), p, ctx);
         }
         //cleanup
         l.debug("Cleaning the temporary files.");
         FileUtil.recursiveDelete(tmpDir);
         FileUtil.recursiveDelete(tmpZipDir);
         MDC.remove("GdcDataPackageDir");
         return r;
     }
 
     /**
      * {@inheritDoc}
      */
     public void extractAndTransfer(Command c, String pid, Connector cc,  boolean waitForFinish, CliParams p, ProcessingContext ctx)
     	throws IOException, InterruptedException {
         l.debug("Extracting data.");
 
         Map<String,String> accountIds = transfer(getAccountSchema(), getAccountQuery(), c, pid, waitForFinish, p, ctx,
                 null, null, null);
         Map<String,String> userIds = transfer(getUserSchema(), getUserQuery(), c, pid, waitForFinish, p, ctx,
                 null, null, null);
 
         Map<String,String> oppIds = transfer(getOppSchema(), getOppQuery(), c, pid, waitForFinish, p, ctx,
                 null, null, null);
 
         Map<String,String> snapIds = transfer(getSnapshotSchema(), getSnapshotQuery(), c, pid, waitForFinish, p, ctx,
                 accountIds, userIds, oppIds);
 
         l.debug("Data extract finished.");
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command " + c.getCommand());
         try {
             if (c.match("LoadSfdcSales") || c.match("UseSfdcSales")) {
                 loadSfdc(c, cli, ctx);
             } else {
                 l.debug("No match passing the command " + c.getCommand() + " further.");
                 return super.processCommand(c, cli, ctx);
             }
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         l.debug("Processed command " + c.getCommand());
         return true;
     }
 
     public String generateMaqlCreate() {
         StringBuilder sb = new StringBuilder();
     	MaqlGenerator mg = new MaqlGenerator(accountSchema);
         sb.append(mg.generateMaqlCreate());
         mg = new MaqlGenerator(userSchema);
         sb.append(mg.generateMaqlCreate());
         mg = new MaqlGenerator(oppSchema);
         sb.append(mg.generateMaqlCreate());
         mg = new MaqlGenerator(snapshotSchema);
         sb.append(mg.generateMaqlCreate());
         return sb.toString();
     }
 
     /**
      * Loads SFDC data command processor
      *
      * @param c   command
      * @param p   command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      */
     private void loadSfdc(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String usr = c.getParamMandatory("username");
         String psw = c.getParamMandatory("password");
         String t = c.getParam("token");
         String ac = c.getParamMandatory("accountConfigFile");
         File acf = FileUtil.getFile(ac);
         String uc = c.getParamMandatory("userConfigFile");
         File ucf = FileUtil.getFile(uc);
         String oc = c.getParamMandatory("opportunityConfigFile");
         File ocf = FileUtil.getFile(oc);
         String sc = c.getParamMandatory("snapshotConfigFile");
         File scf = FileUtil.getFile(sc);
         initSchema(acf.getAbsolutePath(), ucf.getAbsolutePath(), ocf.getAbsolutePath(), scf.getAbsolutePath());
         setAccountQuery(c.getParamMandatory("accountQuery"));
         setUserQuery(c.getParamMandatory("userQuery"));
         setOppQuery(c.getParamMandatory("opportunityQuery"));
         setSnapshotQuery(c.getParamMandatory("snapshotQuery"));
 
         setSfdcUsername(usr);
         setSfdcPassword(psw);
         setSfdcToken(t);
         // sets the current connector
         ctx.setConnector(this);
         setProjectId(ctx);
         l.info("Snapshotting SFDC Connector successfully loaded.");
     }
 
 
     public SourceSchema getAccountSchema() {
         return accountSchema;
     }
 
     public void setAccountSchema(SourceSchema accountSchema) {
         this.accountSchema = accountSchema;
     }
 
     public SourceSchema getUserSchema() {
         return userSchema;
     }
 
     public void setUserSchema(SourceSchema userSchema) {
         this.userSchema = userSchema;
     }
 
     public SourceSchema getOppSchema() {
         return oppSchema;
     }
 
     public void setOppSchema(SourceSchema oppSchema) {
         this.oppSchema = oppSchema;
     }
 
     public String getAccountQuery() {
         return accountQuery;
     }
 
     public void setAccountQuery(String accountQuery) {
         this.accountQuery = accountQuery;
     }
 
     public String getUserQuery() {
         return userQuery;
     }
 
     public void setUserQuery(String userQuery) {
         this.userQuery = userQuery;
     }
 
     public String getOppQuery() {
         return oppQuery;
     }
 
     public void setOppQuery(String oppQuery) {
         this.oppQuery = oppQuery;
     }
 
     public SourceSchema getSnapshotSchema() {
         return snapshotSchema;
     }
 
     public void setSnapshotSchema(SourceSchema snapshotSchema) {
         this.snapshotSchema = snapshotSchema;
     }
 
     public String getSnapshotQuery() {
         return snapshotQuery;
     }
 
     public void setSnapshotQuery(String snapshotQuery) {
         this.snapshotQuery = snapshotQuery;
     }
 }
