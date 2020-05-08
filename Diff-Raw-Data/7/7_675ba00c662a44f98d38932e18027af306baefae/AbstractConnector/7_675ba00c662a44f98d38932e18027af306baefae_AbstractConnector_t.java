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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.gooddata.Constants;
 import com.gooddata.exception.GdcIntegrationErrorException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.transform.Transformer;
 import com.gooddata.util.CSVReader;
 import com.gooddata.util.CSVWriter;
 import org.apache.log4j.Logger;
 import org.apache.log4j.MDC;
 
 import com.gooddata.exception.InvalidParameterException;
 import com.gooddata.exception.ProcessingException;
 import com.gooddata.modeling.generator.MaqlGenerator;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 
 /**
  * GoodData abstract connector implements functionality that can be reused in several connectors.
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public abstract class AbstractConnector implements Connector {
 
     private static Logger l = Logger.getLogger(AbstractConnector.class);
 
     /**
      * The LDM schema of the data source
      */
     protected SourceSchema schema;
 
     /**
      * Project id
      */
     protected String projectId;
 
 
     public static final int DATE_LENGTH_UNRESTRICTED = -1;
 
     /**
      * Default constructor
      */
     protected AbstractConnector() {
     }
 
     /**
      * {@inheritDoc}
      */
     public SourceSchema getSchema() {
         return schema;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSchema(SourceSchema schema) {
         this.schema = schema;
     }
 
 
 
 /**
      * {@inheritDoc}
      */
     public void extract(String dir) throws IOException {
         File dataFile = new File(dir + System.getProperty("file.separator") + "data.csv");
         extract(dataFile.getAbsolutePath(), true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void dump(String file) throws IOException {
         extract(file, true);
     }
 
 
     /**
      * Copies the extracted data and transform them
      * @param cr - reader
      * @param cw - writer
      * @param transform  - perform transformations?
      * @param dateLength - cuts the fate to first dateLength chars
      * @return number of extracted rows
      * @throws IOException
      */
     protected int copyAndTransform(CSVReader cr, CSVWriter cw, boolean transform, int dateLength) throws IOException {
         Transformer t = Transformer.create(schema);
         String[] header = t.getHeader(transform);
         cw.writeNext(header);
         String[] row = cr.readNext();
         int rowCnt = 0;
         while (row != null) {
                 rowCnt++;
                 if(row.length == 1 && row[0].length() == 0) {
                     row = cr.readNext();
                     continue;
                 }
                 if(transform) {
                    try {
                        row = t.transformRow(row, dateLength);
                    }
                    catch (InvalidParameterException e) {
                        throw new InvalidParameterException(e.getMessage()+" Error occured at row "+rowCnt);
                    }
                 }
                 cw.writeNext(row);
                 cw.flush();
                 row = cr.readNext();
         }
         cw.close();
         cr.close();
         return rowCnt;
     }
 
 /**
      * Extract rows
      * @param file name of the target file
      * @param transform perform transformations
      * @throws IOException
      */
      public abstract void extract(String file, final boolean transform) throws IOException;
 
     /**
      * {@inheritDoc}
      */
     public void deploy(SLI sli, List<Column> columns, String dir, String archiveName)
             throws IOException {
         String fn = dir + System.getProperty("file.separator") +
                 GdcRESTApiWrapper.DLI_MANIFEST_FILENAME;
         String cn = sli.getSLIManifest(columns);
         FileUtil.writeStringToFile(cn, fn);
         l.debug("Manifest file written to file '"+fn+"'. Content: "+cn);
         FileUtil.compressDir(dir, archiveName);
     }
 
     /**
      * Initializes the source and PDM schemas from the config file
      * @param configFileName the config file
      * @throws IOException in cas the config file doesn't exists
      */
     protected void initSchema(String configFileName) throws IOException {
         schema = SourceSchema.createSchema(new File(configFileName));
         expandDates(getSchema());
     }
 
     protected void expandDates(SourceSchema s) {
         List<SourceColumn> dates = s.getDates();
         if(dates != null && dates.size()>0) {
             for(SourceColumn d : dates) {
                 String scid = d.getName();
                 SourceColumn dateFact = new SourceColumn(scid + N.DT_SLI_SFX, SourceColumn.LDM_TYPE_FACT, d.getTitle()+" (Date)");
                 String fmt = d.getFormat();
                 if(fmt == null || fmt.length()<=0) {
                     if(d.isDatetime()) {
                         fmt = Constants.DEFAULT_DATETIME_FMT_STRING;
                     }
                     else {
                         fmt = Constants.DEFAULT_DATE_FMT_STRING;
                     }
                 }
                 dateFact.setDateFact(true);
                 dateFact.setDataType("INT");
                 dateFact.setTransformation("GdcDateArithmetics.computeDateFact("+ scid +",\""+fmt+"\")");
                 s.addColumn(dateFact);
                 if(d.isDatetime()) {
                     SourceColumn timeFact = new SourceColumn(scid + N.TM_SLI_SFX, SourceColumn.LDM_TYPE_FACT, d.getTitle()+" (Time)");
                     timeFact.setTimeFact(true);
                     timeFact.setDataType("INT");
                     timeFact.setTransformation("GdcDateArithmetics.computeTimeFact("+ scid + ",\""+fmt+"\")");
                     s.addColumn(timeFact);
                     SourceColumn timeAttr = new SourceColumn(scid +"_"+N.ID, SourceColumn.LDM_TYPE_REFERENCE, d.getTitle()+" (Time)");
                     timeAttr.setTimeFact(true);
                     timeAttr.setSchemaReference(d.getSchemaReference());
                     timeAttr.setTransformation("GdcDateArithmetics.computeTimeAttribute("+scid + ",\""+fmt+"\")");
                     s.addColumn(timeAttr);
                 }
             }
         }
     }
 
     public String generateMaqlCreate() {
     	MaqlGenerator mg = new MaqlGenerator(schema);
     	return mg.generateMaqlCreate();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command "+c.getCommand());
         try {
             if(c.match("GenerateMaql")) {
                 generateMAQL(c, cli, ctx);
             }
             else if(c.match("ExecuteMaql")) {
                 executeMAQL(c, cli, ctx);
             }
             else if(c.match("TransferData") || c.match("TransferAllSnapshots") || c.match("TransferLastSnapshot") ||
                     c.match("TransferSnapshots")) {
                 transferData(c, cli, ctx);
             }
             else if(c.match("Dump")) {
                 dumpData(c, cli, ctx);
             }
             else if (c.match( "GenerateUpdateMaql")) {
                 generateUpdateMaql(c, cli, ctx);
             }
             else {
                 l.debug("No match for command "+c.getCommand());
                 return false;
             }
             l.debug("Command "+c.getCommand()+" processed.");
             return true;
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         catch (InterruptedException e) {
             throw new ProcessingException(e);
         }
     }
 
     /**
      * Generates the MAQL
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void generateMAQL(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         Connector cc = ctx.getConnectorMandatory();
         String maqlFile = c.getParamMandatory("maqlFile");
 
         l.debug("Executing maql generation.");
         String maql = cc.generateMaqlCreate();
         l.debug("Finished maql generation maql:\n"+maql);
                
         FileUtil.writeStringToFile(maql, maqlFile);
         l.info("MAQL script successfully generated into "+maqlFile);
     }
 
     /**
      * Executes MAQL
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void executeMAQL(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         l.debug("Executing MAQL.");
         String pid = ctx.getProjectIdMandatory();
         final String maqlFile = c.getParamMandatory("maqlFile");
         final String ifExistsStr = c.getParam("ifExists");
         final boolean ifExists = (ifExistsStr != null && "true".equalsIgnoreCase(ifExistsStr));
         final File mf = FileUtil.getFile(maqlFile, ifExists);
         if (mf != null) {
 	        final String maql = FileUtil.readStringFromFile(maqlFile);
 	        ctx.getRestApi(p).executeMAQL(pid, maql);
         }
         l.debug("Finished MAQL execution.");
         l.info("MAQL script "+maqlFile+" successfully executed.");
     }
 
     /**
      * Transfers the data to GoodData project
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      * @throws InterruptedException internal problem with making file writable
      */
     protected void transferData(Command c, CliParams p, ProcessingContext ctx) throws IOException, InterruptedException {
         l.debug("Transferring data.");
         Connector cc = ctx.getConnectorMandatory();
         String pid = ctx.getProjectIdMandatory();
         
         boolean waitForFinish = true;
         if(c.checkParam("waitForFinish")) {
             String w = c.getParam( "waitForFinish");
             if(w != null && w.equalsIgnoreCase("false"))
                 waitForFinish = false;
         }
         cc.extractAndTransfer(c, pid, cc, waitForFinish, p, ctx);
         l.debug("Data transfer finished.");
         l.info("Data transfer finished.");
     }
 
     /**
      * Dumps the data to CSV
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      * @throws InterruptedException internal problem with making file writable
      */
     protected void dumpData(Command c, CliParams p, ProcessingContext ctx) throws IOException, InterruptedException {
         l.debug("Dumping data.");
         Connector cc = ctx.getConnectorMandatory();
         String csvFile = c.getParamMandatory("csvFile");
         cc.dump(csvFile);
         l.info("Data dump finished. Data dumped into the file '"+csvFile+"'");
     }
 
     public static List<Column> populateColumnsFromSchema(SourceSchema schema) {
         List<Column> columns = new ArrayList<Column>();
         String ssn = schema.getName();
         for(SourceColumn sc : schema.getColumns()) {
             String scn = sc.getName();
             if(!sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_IGNORE)) {
                 String schemaName = (sc.getSchemaReference() == null) ? ssn : sc.getSchemaReference();
                 Column c = new Column(sc.getName());
                 c.setMode(Column.LM_FULL);
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_ATTRIBUTE) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_CONNECTION_POINT) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_REFERENCE) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE))
                     c.setReferenceKey(1);
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_ATTRIBUTE))
                     c.setPopulates(new String[] {"label." + schemaName + "." + scn});
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_CONNECTION_POINT))
                     c.setPopulates(new String[] {"label." + ssn + "." + scn});
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_REFERENCE)) {
                     if(sc.isTimeFact()) {
                         c.setName(sc.getName());
                         c.setPopulates(new String[] {Constants.DEFAULT_TIME_LABEL+sc.getSchemaReference()});
 
                     }
                     else
                         c.setPopulates(new String[] {"label." + sc.getSchemaReference() +
                             "." + sc.getReference()});
                 }
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_LABEL))
                     c.setPopulates(new String[] {"label." + ssn + "." + sc.getReference() +
                             "." + scn});
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE)) {
                     String fmt = sc.getFormat();
                     if(fmt != null && fmt.length() > 0) {
                         c.setFormat(fmt);
                     }
                     else {
                         if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE))
                             c.setFormat(Constants.DEFAULT_DATE_FMT_STRING);
                         else if(sc.isDatetime())
                             c.setFormat(Constants.DEFAULT_DATETIME_FMT_STRING);
                     }
                     String sr = sc.getSchemaReference();
                     if(sr != null && sr.length() > 0) {
 
                         String r = sc.getReference();
                         if(r != null && r.length() > 0) {
                             // fix for the fiscal date dimension
                             c.setPopulates(new String[] {sr + "." + r +
                                     Constants.DEFAULT_DATE_LABEL_SUFFIX});
                         }
                         else {
                             c.setPopulates(new String[] {sr + "." + Constants.DEFAULT_DATE_LABEL +
                                     Constants.DEFAULT_DATE_LABEL_SUFFIX});
                         }
                     }
                     else {
                         c.setPopulates(new String[] {"label." + ssn + "." + scn});   
                     }
 
                 }
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_FACT)) {
                     if(sc.isDateFact()) {
                         c.setName(sc.getName());
                         c.setPopulates(new String[] {N.DT + "." + ssn + "." + scn.replace(N.DT_SLI_SFX,"")});
                     }
                     else if(sc.isTimeFact()) {
                         c.setName(sc.getName());
                         c.setPopulates(new String[] {N.TM + "." + N.DT + "." + ssn + "." + scn.replace(N.TM_SLI_SFX,"")});
                     }
                     else {
                         c.setPopulates(new String[] {"fact." + ssn + "." + scn});
                     }
                 }
                 columns.add(c);
             }
         }
         return columns;
     }
 
     /**
      * {@inheritDoc}
      */
     public void extractAndTransfer(Command c, String pid, Connector cc,  boolean waitForFinish, CliParams p, ProcessingContext ctx)
     	throws IOException, InterruptedException
     {
         // connector's schema name
         String ssn = cc.getSchema().getName();
         l.debug("Extracting data.");
         File tmpDir = FileUtil.createTempDir();
         File tmpZipDir = FileUtil.createTempDir();
         String archiveName = tmpDir.getName();
         MDC.put("GdcDataPackageDir",archiveName);
         String archivePath = tmpZipDir.getAbsolutePath() + System.getProperty("file.separator") +
             archiveName + ".zip";
 
         // get information about the data loading package      
         SLI sli = ctx.getRestApi(p).getSLIById("dataset." + ssn, pid);
         List<Column> sliColumns = ctx.getRestApi(p).getSLIColumns(sli.getUri());
         List<Column> columns = populateColumnsFromSchema(cc.getSchema());
 
         if(sliColumns.size() > columns.size())
             throw new InvalidParameterException("The GoodData data loading interface (SLI) expects more columns.");
 
 
         String incremental = c.getParam("incremental");
         if(incremental != null && incremental.length() > 0 &&
                 incremental.equalsIgnoreCase("true")) {
             l.debug("Using incremental mode.");
             setIncremental(columns);
         }
 
         // extract the data to the CSV that is going to be transferred to the server
         cc.extract(tmpDir.getAbsolutePath());
         
         cc.deploy(sli, columns, tmpDir.getAbsolutePath(), archivePath);
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
         l.debug("Data extract finished.");
     }
 
     /**
      * Sets the incremental loading status for a part
      * @param cols SLI columns
      */
     protected void setIncremental(List<Column> cols) {
         for(Column col : cols) {
             col.setMode(Column.LM_INCREMENTAL);
         }
     }
 
 
     /**
      * Checks the status of data integration process in the GoodData platform
      * @param taskUri the uri where the task status is determined
      * @param tmpDir temporary dir where the temporary data reside. This directory will be deleted.
      * @param p cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      * @throws InterruptedException internal problem with making file writable
      */
     protected void checkLoadingStatus(String taskUri, String tmpDir, CliParams p, ProcessingContext ctx) throws InterruptedException,IOException {
         l.debug("Checking data transfer status.");
         String status = "";
         while(!status.equalsIgnoreCase("OK") && !status.equalsIgnoreCase("ERROR") && !status.equalsIgnoreCase("WARNING")) {
             status = ctx.getRestApi(p).getLoadingStatus(taskUri);
             l.debug("Loading status = "+status);
             Thread.sleep(500);
         }
         l.debug("Data transfer finished with status "+status);
         if(status.equalsIgnoreCase("OK")) {
             l.info("Data successfully loaded.");
         }
         else if(status.equalsIgnoreCase("WARNING")) {   
             l.info("Data loading succeeded with warnings. Status: "+status);
             Map<String,String> result = ctx.getFtpApi(p).getTransferLogs(tmpDir);
             for(String file : result.keySet()) {
                 if(file.endsWith(".json"))
                     l.info(file+":\n"+result.get(file));
             }
             for(String file : result.keySet()) {
                 if(!file.endsWith(".json"))
                     l.info(file+":\n"+result.get(file));
             }
         }
         else {
             l.info("Data loading failed. Status: "+status);
             Map<String,String> result = ctx.getFtpApi(p).getTransferLogs(tmpDir);
             for(String file : result.keySet()) {
                 if(file.endsWith(".json"))
                     l.info(file+":\n"+result.get(file));
             }
             for(String file : result.keySet()) {
                 if(!file.endsWith(".json"))
                     l.info(file+":\n"+result.get(file));
             }
             throw new GdcIntegrationErrorException("Data successfully transferred but failed to load to the analytical project. " +
                     "This is usually due to issues with data integrity (rows with different number of columns etc.).");
         }
 
     }
 
 
 
     /**
      * Generate the MAQL for new columns 
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      * @throws IOException IO issue
      */
     private void generateUpdateMaql(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         l.debug("Updating MAQL.");
     	//final String configFile = c.getParamMandatory( "configFile");
     	//final SourceSchema schema = SourceSchema.createSchema(new File(configFile));
         final Connector cc = ctx.getConnectorMandatory();
         final SourceSchema schema = cc.getSchema();
 
         if(schema != null) {
 
             final String pid = ctx.getProjectIdMandatory();
             final String maqlFile = c.getParamMandatory( "maqlFile");
             final String dataset = schema.getDatasetName();
 
             final GdcRESTApiWrapper gd = ctx.getRestApi(p);
             final SLI sli = gd.getSLIById(dataset, pid);
 
             final DataSetDiffMaker diffMaker = new DataSetDiffMaker(gd, sli, schema);
             final List<SourceColumn> newColumns = diffMaker.findNewColumns();
             final List<SourceColumn> deletedColumns = diffMaker.findDeletedColumns();
             final MaqlGenerator mg = new MaqlGenerator(schema);
 
             final StringBuilder maql = new StringBuilder();
             if (!deletedColumns.isEmpty()) {
                 mg.setSynchronize(false);
                 maql.append(mg.generateMaqlDrop(deletedColumns, diffMaker.getLocalColumns()));
             }
             if (!newColumns.isEmpty()) {
                 mg.setSynchronize(false);
                 maql.append(mg.generateMaqlAdd(newColumns, diffMaker.getLocalColumns()));
             }
             if (maql.length() > 0) {
                 maql.append(mg.generateMaqlSynchronize());
                 final String mqqlStr = mg.removeDropAndRecreateOfDateFacts(deletedColumns, newColumns, maql.toString());
                 l.debug("Finished maql generation maql:\n" + mqqlStr);
                 FileUtil.writeStringToFile(mqqlStr, maqlFile);
                 l.debug("MAQL update finished.");
                 l.info("MAQL update successfully finished.");
             } else {
                 l.debug("MAQL update successfully finished - no changes detected.");
                 l.info("MAQL update successfully finished - no changes detected.");
             }
         }
         else {
             l.debug("MAQL update ran on a connector with no schema file (e.g. the default GDC Date dimension). This has no effect.");
             l.debug("MAQL update successfully finished - no changes detected.");
             l.info("MAQL update ran on a connector with no schema file (e.g. the default GDC Date dimension). This has no effect.");
             l.info("MAQL update successfully finished - no changes detected.");
         }
     }
 
     public String getProjectId() {
         return projectId;
     }
 
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
    
 
     /**
      * Sets the project id from context
      * @param ctx process context
      * @throws InvalidParameterException if the project id isn't initialized
      */
     protected void setProjectId(ProcessingContext ctx) throws InvalidParameterException {
         String pid = ctx.getProjectIdMandatory();
         if(pid != null && pid.length() > 0)
             setProjectId(pid);
     }
 
     /**
      * Class wrapping local changes to a server-side model
      */
     private static class Changes {
     	private List<SourceColumn> newColumns = new ArrayList<SourceColumn>();
     	private List<SourceColumn> deletedColumns = new ArrayList<SourceColumn>();
     }
 
 }
