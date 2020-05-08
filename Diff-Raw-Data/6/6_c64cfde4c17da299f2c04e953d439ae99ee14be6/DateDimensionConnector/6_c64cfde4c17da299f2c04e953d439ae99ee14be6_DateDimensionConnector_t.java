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
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 import org.apache.log4j.Logger;
 import org.apache.log4j.MDC;
 
 import java.io.*;
 import java.util.List;
 
 /**
  * GoodData Google Analytics Connector
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class DateDimensionConnector extends AbstractConnector implements Connector {
 
     private static Logger l = Logger.getLogger(DateDimensionConnector.class);
 
     //Time dimension context (e.g. created, closed etc.)
     private String name;
 
     // Include time dimension
     private boolean includeTime = false;
 
     private String type = "URN:GOODDATA:DATE";
 
     /**
      * Creates a new Time Dimension Connector
      */
     protected DateDimensionConnector() {
     }
 
     /**
      * Creates a new Time Dimension Connector
      * @return new Time Dimension Connector
      */
     public static DateDimensionConnector createConnector() {
         return new DateDimensionConnector();
     }
 
     private final int BUF_LEN = 2048;
 
     /**
      * {@inheritDoc}
      */
    public void extract(String file, boolean transform) throws IOException {
         l.debug("Extracting time dimension data "+name);
         if(name == null || name.trim().length()<=0)
             name = "";
         InputStream r = DateDimensionConnector.class.getResourceAsStream("/com/gooddata/connector/data.csv");
        FileOutputStream w = new FileOutputStream(file);
         byte[] buf = new byte[BUF_LEN];
         int cnt = r.read(buf,0,BUF_LEN);
         while(cnt > 0) {
             w.write(buf,0,cnt);
             cnt = r.read(buf,0,BUF_LEN);
         }
         w.flush();
         w.close();
         l.debug("Extracted time dimension data "+name);
     }
 
     /**
      * {@inheritDoc}
      */
     public void extractAndTransfer(Command c, String pid, Connector cc,  boolean waitForFinish, CliParams p, ProcessingContext ctx)
     	throws IOException, InterruptedException {
         if(includeTime) {
             l.debug("Extracting data.");
             File tmpDir = FileUtil.createTempDir();
             File tmpZipDir = FileUtil.createTempDir();
             String archiveName = tmpDir.getName();
             MDC.put("GdcDataPackageDir",archiveName);
             String archivePath = tmpZipDir.getAbsolutePath() + System.getProperty("file.separator") +
                 archiveName + ".zip";
 
             // extract the data to the CSV that is going to be transferred to the server
             this.extract(tmpDir.getAbsolutePath());
 
             this.deploy(tmpDir.getAbsolutePath(), archivePath);
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
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void deploy(String dir, String archiveName)
             throws IOException {
         l.debug("Extracting time dimension manifest "+name);
         String fn = dir + System.getProperty("file.separator") +
                 GdcRESTApiWrapper.DLI_MANIFEST_FILENAME;
 
         if(name == null || name.trim().length()<=0)
             name = "";
         String idp = StringUtil.toIdentifier(name);
         String ts = StringUtil.toTitle(name);
         String script = "";
         BufferedReader is = new BufferedReader(new InputStreamReader(
                 DateDimensionConnector.class.getResourceAsStream("/com/gooddata/connector/upload_info.json")));
         String line = is.readLine();
         while (line != null) {
             script += line.replace("%id%", idp) + "\n";
             line = is.readLine();
         }
         FileUtil.writeStringToFile(script, fn);
         l.debug("Manifest file written to file '"+fn+"'. Content: "+script);
         FileUtil.compressDir(dir, archiveName);
         l.debug("Extracted time dimension manifest "+name);
     }
 
     /**
      * {@inheritDoc}
      */
     public String generateMaqlCreate() {
         l.debug("Generating time dimension MAQL with context "+name);
         if(name != null && name.trim().length()>0) {
             String idp = StringUtil.toIdentifier(name);
             String ts = StringUtil.toTitle(name);
             String script = "INCLUDE TEMPLATE \""+getType()+"\" MODIFY (IDENTIFIER \""+idp+"\", TITLE \""+ts+"\");\n\n";
             if(includeTime) {
                 try {
                     BufferedReader is = new BufferedReader(new InputStreamReader(
                             DateDimensionConnector.class.getResourceAsStream("/com/gooddata/connector/TimeDimension.maql")));
                     String line = is.readLine();
                     while (line != null) {
                         script += line.replace("%id%", idp).replace("%name%", ts) + "\n";
                         line = is.readLine();
                     }
                 }
                 catch (IOException e) {
                     throw new InternalErrorException("Can't read the time dimension MAQL,", e);
                 }
             }
             l.debug("Generated time dimension MAQL with context "+name);
             return script;
         }
         else {
             l.debug("Generated time dimension MAQL with no context ");
             return "INCLUDE TEMPLATE \""+getType()+"\"";
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command "+c.getCommand());
         try {
             if(c.match("LoadDateDimension") || c.match("UseDateDimension")) {
                 loadDateDimension(c, cli, ctx);
             }
             else {
                 l.debug("No match passing the command "+c.getCommand()+" further.");
                 return super.processCommand(c, cli, ctx);
             }
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         l.debug("Processed command "+c.getCommand());
         return true;
     }
 
     /**
      * Loads DateDimension data command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      */
     private void loadDateDimension(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String ct = "";
         if(c.checkParam("name"))
             ct = c.getParam( "name");
         this.name = ct;
         if(c.checkParam("includeTime")) {
             String ic = c.getParam( "includeTime");
             includeTime = (ic != null && "true".equalsIgnoreCase(ic));
         }
         if(c.checkParam("type")) {
             type = c.getParam( "type");
         }
 
         // sets the current connector
         ctx.setConnector(this);
         l.info("Time Dimension Connector successfully loaded (name: " + ct + ").");
     }
 
 	/**
 	 * @return the date dimension name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the date dimension name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
 
 }
