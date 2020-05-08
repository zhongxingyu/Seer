 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package jp.co.antenna.XfoJavaCtl;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * XfoObj Class is the object class of XSL Formatter
  * 
  * @author Test User
  */
 public class XfoObj {
     // Consts
     public static final int EST_NONE = 0;
     public static final int EST_STDOUT = 1;
     public static final int EST_STDERR = 2;
     
     public static final int S_PDF_EMBALLFONT_PART = 0;
     public static final int S_PDF_EMBALLFONT_ALL = 1;
     public static final int S_PDF_EMBALLFONT_BASE14 = 2;
     
     // Attributes
     private String executable;
     private Runtime r;
     private MessageListener messageListener;
     private String logPath;
     private LinkedHashMap<String, String> args;
 	private XfoException lastError;
     
     // Methods
     /**
      * Create the instance of XfoObj, and initialize it.
      * 
      * @throws XfoException
      */
     public XfoObj () throws XfoException {
         // Check EVs and test if XslCmd.exe exists.
 		String os;
 		try {
 			os = System.getProperty("os.name");
 			if ((os == null) || os.equals(""))
 				throw new Exception();
 		} catch (Exception e) {
 			throw new XfoException(4, 0, "Could not determine OS");
 		}
 		String axf_home;
 		try {
 			axf_home = System.getenv("AXF43_HOME");
 			if ((axf_home == null) || axf_home.equals(""))
 				throw new Exception();
 		} catch (Exception e) {
 			throw new XfoException(4, 1, "Could not find AXF43_HOME environment variable");
 		}
 		String separator = System.getProperty("file.separator");
         this.executable = axf_home + separator;
 		if (os.equals("Linux") || os.equals("SunOS"))
 			this.executable += "bin" + separator + "XSLCmd";
 		else if (os.contains("Windows"))
 			this.executable += "XSLCmd.exe";
 		else
 			throw new XfoException(4, 2, "Unsupported OS.");
         // setup attributes
         this.clear();
     }
     
     /**
      * Cleanup (initialize) XSL Formatter engine.
      */
     public void clear () {
         // reset attributes        
         this.r = Runtime.getRuntime();
         this.logPath = null;
         this.args = new LinkedHashMap<String, String>();
         this.messageListener = null;
 		this.lastError = null;
     }
     
     /**
      * Execute formatting and outputs to a PDF. 
      * 
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void execute () throws XfoException {
 		ArrayList<String> cmdArray = new ArrayList<String>();
 		cmdArray.add(this.executable);
 		for (String arg : this.args.keySet()) {
 			cmdArray.add(arg);
 			cmdArray.add(this.args.get(arg));
 		}
         // Run Formatter with Runtime.exec()
         Process process;
         ErrorParser errorParser = null;
         int exitCode = -1;
         if (this.logPath != null) {
 			cmdArray.add(this.logPath);
         }
         try {
 			String[] s = new String[0];
             process = this.r.exec(cmdArray.toArray(s));
             if ((this.logPath == null) && (this.messageListener != null)) {
                 try {
                     InputStream StdErr = process.getErrorStream();
                     errorParser = new ErrorParser(StdErr, this.messageListener);
                     errorParser.start();
                 } catch (Exception e) {}
             }
             exitCode = process.waitFor();
         } catch (Exception e) {}
         if (exitCode != 0) {
             if (errorParser != null) {
                 this.lastError = new XfoException(errorParser.LastErrorLevel, errorParser.LastErrorCode, errorParser.LastErrorMessage);
 				throw this.lastError;
             } else {
                 throw new XfoException(4, 0, "Failed to parse last error.");
             }
         }
     }
 
 	public int getErrorCode () throws XfoException {
 		if (this.lastError == null)
 			return 0;
 		else
 			return this.lastError.getErrorCode();
 	}
 
 	public int getErrorLevel () throws XfoException {
 		if (this.lastError == null)
 			return 0;
 		else
 			return this.lastError.getErrorLevel();
 	}
 
 	public String getErrorMessage () throws XfoException {
 		if (this.lastError == null)
 			return null;
 		else
 			return this.lastError.getErrorMessage();
 	}
     
 	public void releaseObject () {
 		this.clear();
 	}
 
     public void releaseObjectEx () throws XfoException {
         releaseObject();
     }
     
     /**
      * Executes the formatting of XSL-FO document specified for src, and outputs it to dst in the output form specified for dst.
      * 
      * @param src   XSL-FO Document
      * @param dst   output stream
      * @param outDevice output device. Please refer to a setPrinterName method about the character string to specify. 
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void render (InputStream src, OutputStream dst, String outDevice) throws XfoException {
         if (this.messageListener != null)
             this.messageListener.onMessage(4, 0, "render() is not implemented yet.");
         throw new XfoException(4, 0, "render() is not implemented yet.");
     }
     
     public void setBatchPrint (boolean bat) {
         // Fake it. 
     }
     
     /**
      * Set the URI of XML document to be formatted.
      * <br>If specified "@STDIN", XML document reads from stdin. The document that is read from stdin is assumed to be FO. 
      * 
      * @param uri URI of XML document
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setDocumentURI (String uri) throws XfoException {
         // Set the URI...
         String opt = "-d";
         if (uri != null && !uri.equals("")) {
             if (this.args.containsKey(opt))
                 this.args.remove(opt);
             this.args.put(opt, uri);
         }
         else {
             this.args.remove(opt);
         }
     }
     
     public void setErrorLogPath (String path) {
         if (path != null && !path.equals("")) {
             this.logPath = " 2>> " + path;
         } else {
             this.logPath = null;
         }
     }
     
     public void setErrorStreamType (int type) {
         // Fake it.
     }
     
     /**
      * Set the error level to abort formatting process.
      * <br>XSL Formatter will stop formatting when the detected error level is equal to setExitLevel setting or higher.
      * <br>The default value is 2 (Warning). Thus if an error occurred and error level is 2 (Warning) or higher, the 
      * formatting process will be aborted. Please use the value from 1 to 4. When the value of 5 or more is specified, 
      * it is considered to be the value of 4. If a error-level:4 (fatal error) occurs, the formatting process will be 
      * aborted unconditionally. Note: An error is not displayed regardless what value may be specified to be this property. 
      * 
      * @param level Error level to abort
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setExitLevel (int level) throws XfoException {
         // Set the level...
         String opt = "-extlevel";
         if (this.args.containsKey(opt))
             this.args.remove(opt);
         this.args.put(opt, String.valueOf(level));
     }
     
     /**
      * Set the command-line string for external XSLT processor. For example:
      * <DL><DD>xslt -o %3 %1 %2 %param</DL>
      * %1 to %3 means following:
      * <DL><DD>%1 : XML Document 
      * <DD>%2 : XSL Stylesheet 
      * <DD>%3 : XSLT Output File 
      * <DD>%param : xsl:param</DL>
      * %1 to %3 are used to express only parameter positions. Do not replace them with actual file names.
      * <br>In case you use XSL:param for external XSLT processor, set the name and value here.
      * <br>In Windows version, default MSXML3 will be used. 
      * 
      * @param cmd Command-line string for external XSLT processor
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setExternalXSLT (String cmd) throws XfoException {
         // Fill this in....
     }
     
     /**
      * Register the MessageListener interface to the instance of implemented class.
      * <br>The error that occurred during the formatting process can be received as the event. 
      * 
      * @param listener The instance of implemented class
      */
     public void setMessageListener (MessageListener listener) {
         if (listener != null)
             this.messageListener = listener;
         else
             this.messageListener = null;
     }
     
     public void setMultiVolume (boolean multiVol) {
         String opt = "-multivol";
         if (multiVol) {
             this.args.put(opt, "");
         } else {
             this.args.remove(opt);
         }
     }
     
     /**
      * Specifies the output file path of the formatted result.
      * <br>When the printer is specified as an output format by setPrinterName, a
      * printing result is saved to the specified file by the printer driver.
      * <br>When output format other than a printer is specified, it is saved at the 
      * specified file with the specified output format.
      * <br>When omitted, or when "@STDOUT" is specified, it comes to standard output. 
      * 
      * @param path Path name of output file
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setOutputFilePath (String path) throws XfoException {
         // Set the path...
         String opt = "-o";
         if (path != null && !path.equals("")) {
             if (this.args.containsKey(opt))
                 this.args.remove(opt);
             this.args.put(opt, path);
         }
         else {
             this.args.remove(opt);
         }
     }
     
     public void setPdfEmbedAllFontsEx (int embedLevel) throws XfoException {
         // fill it in
         String opt = "-peb";
         if (embedLevel != -1) {
             this.args.put(opt, String.valueOf(embedLevel));
         } else {
             this.args.remove(opt);
         }
     }
     
     public void setPdfImageCompression (int compressionMethod) {
         // fill it in
     }
     
     public void setPrinterName (String prn) {
         String opt = "-p";
         if (prn != null && !prn.equals("")) {
             if (this.args.containsKey(opt))
                 this.args.remove(opt);
             this.args.put(opt, prn);
         }
         else {
             this.args.remove(opt);
         }
     }
     
     public void setStylesheetURI (String uri) {
         String opt = "-s";
         if (uri != null && !uri.equals("")) {
             if (this.args.containsKey(opt))
                 this.args.remove(opt);
             this.args.put(opt, uri);
         }
         else {
             this.args.remove(opt);
         }
     }
     
     public void setOptionFileURI (String path) {
         String opt = "-i";
         if (path != null && !path.equals("")) {
             if (this.args.containsKey(opt))
                 this.args.remove(opt);
             this.args.put(opt, path);
         }
         else {
             this.args.remove(opt);
         }
     }
     
     public void setXSLTParam (String paramName, String value) {
         // fill it in
     }
 }
 
 class ErrorParser extends Thread {
     private InputStream ErrorStream;
     private MessageListener listener;
     public int LastErrorLevel;
     public int LastErrorCode;
     public String LastErrorMessage;
     
     public ErrorParser (InputStream ErrorStream, MessageListener listener) {
         this.ErrorStream = ErrorStream;
         this.listener = listener;
     }
     
     @Override
     public void run () {
         try {
             // stuff
             BufferedReader reader = new BufferedReader(new InputStreamReader(this.ErrorStream));
             String line = reader.readLine();
             while (line != null) {
                 if (line.startsWith("XSLCmd :")) {
                     if (line.contains("Error Level")) {
                         try {
                             int ErrorLevel = Integer.parseInt(line.substring(line.length() - 1, line.length()));
                             line = reader.readLine();
                             int ErrorCode = Integer.parseInt(line.split(" ")[line.split(" ").length - 2]);
                             line = reader.readLine();
                             String ErrorMessage = line.split(" ", 3)[2];
                             line = reader.readLine();
                             if (line.startsWith("XSLCmd :")) {
                                 ErrorMessage += "\n" + line.split(" ", 3)[2];
                             }
                             this.LastErrorLevel = ErrorLevel;
                             this.LastErrorCode = ErrorCode;
                             this.LastErrorMessage = ErrorMessage;
                             this.listener.onMessage(ErrorLevel, ErrorCode, ErrorMessage);
                         } catch (Exception e) {}
                     }
                 } else if (line.startsWith("Invalid license.")) {
 					int ErrorLevel = 4;
 					int ErrorCode = 24579;
					String ErrorMessage = line 
						+ "\n" + reader.readLine() 
						+ "\n" + reader.readLine();
 					this.LastErrorLevel = ErrorLevel;
 					this.LastErrorCode = ErrorCode;
 					this.LastErrorMessage = ErrorMessage;
 					this.listener.onMessage(ErrorLevel, ErrorCode, ErrorMessage);
 				}
                 line = reader.readLine();
             }
         } catch (Exception e) {}
     }
 }
