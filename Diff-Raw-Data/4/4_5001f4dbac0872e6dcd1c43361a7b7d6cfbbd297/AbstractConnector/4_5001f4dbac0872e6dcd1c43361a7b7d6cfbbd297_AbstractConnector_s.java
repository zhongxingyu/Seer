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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
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
     public abstract void extract(String dir) throws IOException;
     
 
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
 
     protected String[] populateCsvHeaderFromSchema(SourceSchema schema) {
         List<SourceColumn> columns = schema.getColumns();
         String[] header = new String[columns.size()];
         for(int i = 0; i < header.length; i++) {
             header[i] = StringUtil.toIdentifier(columns.get(i).getName());
         }
         return header;
     }
     
 
     protected List<Column> populateColumnsFromSchema(SourceSchema schema) {
         List<Column> columns = new ArrayList<Column>();
         String ssn = StringUtil.toIdentifier(schema.getName());
         for(SourceColumn sc : schema.getColumns()) {
             String scn = StringUtil.toIdentifier(sc.getName());
             if(!sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_IGNORE)) {
                 Column c = new Column(sc.getName());
                 c.setMode(Column.LM_FULL);
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_ATTRIBUTE) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_CONNECTION_POINT) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_REFERENCE) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_DATE))
                     c.setReferenceKey(1);
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_ATTRIBUTE) ||
                    sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_CONNECTION_POINT))
                     c.setPopulates(new String[] {"label." + ssn + "." + scn});
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_REFERENCE))
                     c.setPopulates(new String[] {"label." + StringUtil.toIdentifier(sc.getSchemaReference()) +
                             "." + StringUtil.toIdentifier(sc.getReference())});
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_LABEL))
                     c.setPopulates(new String[] {"label." + ssn + "." + StringUtil.toIdentifier(sc.getReference()) +
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
                         sr = StringUtil.toIdentifier(sr);
                         c.setPopulates(new String[] {sr + "." + Constants.DEFAULT_DATE_LABEL});
                         // add a new column for the date fact
                         Column dfc = new Column(sc.getName() + N.DT_SLI_SFX);
                         dfc.setMode(Column.LM_FULL);
                         dfc.setPopulates(new String[] {N.DT + "." + ssn + "." + scn});
                         columns.add(dfc);
 
 
                         if(sc.isDatetime()) {
                             Column tfc = new Column(sc.getName() + N.TM_SLI_SFX);
                             tfc.setMode(Column.LM_FULL);
                             tfc.setPopulates(new String[] {N.TM + "." + N.DT + "." + ssn + "." + scn});
                             columns.add(tfc);
 
                             Column tid = new Column(N.TM_PFX+StringUtil.toIdentifier(sc.getName())+"_"+N.ID);
                             tid.setMode(Column.LM_FULL);
                             tid.setPopulates(new String[] {Constants.DEFAULT_TIME_LABEL+sr});
                             tid.setReferenceKey(1);
                             columns.add(tid);
                         }
                     }
                     else {
                         c.setPopulates(new String[] {"label." + ssn + "." + scn});   
                     }
 
                 }
                 if(sc.getLdmType().equalsIgnoreCase(SourceColumn.LDM_TYPE_FACT))
                     c.setPopulates(new String[] {"fact." + ssn + "." + scn});
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
         String ssn = StringUtil.toIdentifier(cc.getSchema().getName());
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
 
         /*
         l.info("SLI COLUMNS");
         for(Column co : sliColumns) {
             l.info("name = "+co.getName()+" populates = "+co.getPopulates());
         }
 
         l.info("XML COLUMNS");
         for(Column co : columns) {
             l.info("name = "+co.getName()+" populates = "+co.getPopulates());
         }
         */
 
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
 		if (!newColumns.isEmpty()) {
 			mg.setSynchronize(false);
 			maql.append(mg.generateMaqlAdd(newColumns, diffMaker.sourceColumns));
 		}
 		if (!deletedColumns.isEmpty()) {
 			mg.setSynchronize(false);
 			maql.append(mg.generateMaqlDrop(deletedColumns));
 		}
 
         if (maql.length() > 0) {
         	maql.append(mg.generateMaqlSynchronize());
             l.debug("Finished maql generation maql:\n"+maql.toString());
             FileUtil.writeStringToFile(maql.toString(), maqlFile);
             l.debug("MAQL update finished.");
             l.info("MAQL update successfully finished.");
         } else {
         	l.info("MAQL update successfully finished - no changes detected.");
         }
 
     }
 
 	/**
      * Finds the attributes and facts with no appropriate part or part column
      * TODO: a generic detector of new labels etc could be added too
      * @param schema former source schema
      * @return list of new columns
      */
     private Changes findColumnChanges(List<Column> columns, SourceSchema schema) {
     	Set<String> fileNames = new HashSet<String>();
     	Set<String> factColumns = new HashSet<String>();
     	
     	// TODO look at TestFindColumnChange and move the logic here
     	throw new UnsupportedOperationException("Not implemented yet");
     	/*
     	DLIPart factPart = null;
     	
     	// get fact table's column names
     	for (final DLIPart part : parts) {
     		if (part.getFileName().startsWith(N.FCT_PFX)) {
     			if (factPart == null) {
     				factPart = part;
     				for (Column col : factPart.getColumns()) {
     					factColumns.add(col.getName());
     				}
     			} else {
     				throw new IllegalStateException("Two fact tables detected on the server: "
     						+ factPart.getFileName() + " and " + part.getFileName());
     			}
     		}
     		fileNames.add(part.getFileName());
     	}
 
     	// create set of column names to be used in the search for deleted fields 
     	final Set<String> attributeTablesSet = new HashSet<String>();
     	final Set<String> factColumnSet      = new HashSet<String>();
     	
     	// find columns in the source schema that don't exist on server
     	final List<SourceColumn> newColumns = new ArrayList<SourceColumn>();
     	for (final SourceColumn sc : schema.getColumns()) {
     		if (SourceColumn.LDM_TYPE_ATTRIBUTE.equals(sc.getLdmType()) || SourceColumn.LDM_TYPE_CONNECTION_POINT.equals(sc.getLdmType())) {
     			final String attributeTableName = MaqlGenerator.createAttributeTableName(schema, sc);
         		attributeTablesSet.add(attributeTableName);
     			final String filename = attributeTableName + ".csv";
     			if (!fileNames.contains(filename)) {
     				newColumns.add(sc);
     			}
     		} else if (SourceColumn.LDM_TYPE_FACT.equals(sc.getLdmType())) {
     			final String factColumn = StringUtil.toFactColumnName(sc.getName());
     			factColumnSet.add(factColumn);
     			if (!factColumns.contains(factColumn)) {
     				newColumns.add(sc);
     			}
     		}
     	}
     	
     	// find server-side columns that no more exist in the source schema
     	final List<String> deletedLookups = new ArrayList<String>();
     	final List<String> deletedFactColumns = new ArrayList<String>();
     	for (final String lookupFile : fileNames) {
     		if (!lookupFile.startsWith(N.FCT_PFX)) {
 	    		final String lookup = lookupFile.replaceAll(".csv$", "");
 	    		if (!attributeTablesSet.contains(lookup)) {
 	    			deletedLookups.add(lookup);
 	    		}
     		}
     	}
     	for (final String fact : factColumns) {
     		if (fact.startsWith(N.FCT_PFX) && !factColumnSet.contains(fact)) {
     			deletedFactColumns.add(fact);
     		}
     	}
     	
     	final Changes changes = new Changes();
     	changes.newColumns = newColumns;
     	changes.deletedColumns.addAll(lookups2columns(schema, deletedLookups));
     	changes.deletedColumns.addAll(facts2columns(deletedFactColumns));
     	return changes;
      	*/
     }
     
     private List<SourceColumn> lookups2columns(SourceSchema schema, List<String> lookups) {
     	List<SourceColumn> result = new ArrayList<SourceColumn>();
     	for (String l : lookups) {
     		String prefix = N.LKP_PFX + StringUtil.toIdentifier(schema.getName()) + "_";
     		if (!l.startsWith(prefix)) {
     			throw new IllegalStateException("Lookup table " + l + " does not start with expected prefix " + prefix);
     		}
     		String name = l.replaceAll("^" + prefix, "");
     		result.add(new SourceColumn(name, SourceColumn.LDM_TYPE_ATTRIBUTE, name));
     	}
     	return result;
     }
     
     private List<SourceColumn> facts2columns(List<String> facts) {
     	List<SourceColumn> result = new ArrayList<SourceColumn>();
     	for (String factColumn : facts) {
     		String factName = factColumn.replaceAll("^" + N.FCT_PFX, "");
     		result.add(new SourceColumn(factName, SourceColumn.LDM_TYPE_FACT, factName));
     	}
     	return result;
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
