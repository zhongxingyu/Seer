 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package jp.co.antenna.DfvJavaCtl;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * DfvObj Class is the object class of XSL Formatter
  * 
  * @author Test User
  */
 public class DfvObj {
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
 	private DfvException lastError;
     
     // Methods
     /**
      * Create the instance of DfvObj, and initialize it.
      * 
      * @throws DfvException
      */
     public DfvObj () throws DfvException {
         // Check EVs and test if SbcCmd.exe exists.
 		String os;
 		try {
 			os = System.getProperty("os.name");
 			if ((os == null) || os.equals(""))
 				throw new Exception();
 		} catch (Exception e) {
 			throw new DfvException(4, 0, "Could not determine OS");
 		}
 		String sbc_home;
 		try {
 			sbc_home = System.getenv("SBC_HOME");
 			if ((sbc_home == null) || sbc_home.equals(""))
 				throw new Exception();
 		} catch (Exception e) {
 			throw new DfvException(4, 1, "Could not find SBC_HOME environment variable");
 		}
 		String separator = System.getProperty("file.separator");
         this.executable = sbc_home + separator;
 		if (os.equals("Linux") || os.equals("SunOS"))
 			this.executable += "bin" + separator + "SBCCmd";
 		else if (os.contains("Windows"))
 			this.executable += "SBCCmd.exe";
 		else
 			throw new DfvException(4, 2, "Unsupported OS.");
         // setup attributes
         this.clear();
     }
     
     /**
      * Cleanup (initialize) Server Based Converter engine.
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
      * Execute formatting. 
      * 
      * @throws jp.co.antenna.DfvJavaCtl.DfvException
      */
     public void execute () throws DfvException {
 		ArrayList<String> cmdArray = new ArrayList<String>();
 		cmdArray.add(this.executable);
 		for (String arg : this.args.keySet()) {
 			cmdArray.add(arg);
 			if (!this.args.get(arg).equals(""))
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
             if (this.logPath == null) {
                 try {
                     InputStream StdErr = process.getErrorStream();
                     errorParser = new ErrorParser(StdErr, this.messageListener);
                     errorParser.start();
                 } catch (Exception e) {}
             }
             exitCode = process.waitFor();
         } catch (Exception e) {}
         if (exitCode != 0) {
            if (errorParser != null && errorParser.LastErrorCode != 0) {
                 this.lastError = new DfvException(errorParser.LastErrorLevel, errorParser.LastErrorCode, errorParser.LastErrorMessage);
 				throw this.lastError;
             } else {
                throw new DfvException(4, 0, "Failed to parse last error. Exit code: " + exitCode);
             }
         }
     }
 
 	public int getErrorCode () throws DfvException {
 		if (this.lastError == null)
 			return 0;
 		else
 			return this.lastError.getErrorCode();
 	}
 
 	public int getErrorLevel () throws DfvException {
 		if (this.lastError == null)
 			return 0;
 		else
 			return this.lastError.getErrorLevel();
 	}
 
 	public String getErrorMessage () throws DfvException {
 		if (this.lastError == null)
 			return null;
 		else
 			return this.lastError.getErrorMessage();
 	}
     
 	public void releaseObject () {
 		this.clear();
 	}
 
     public void releaseObjectEx () throws DfvException {
         releaseObject();
     }
     
     /**
      * Executes the formatting of XSL-FO document specified for src, and outputs it to dst in the output form specified for dst.
      * 
      * @param src   XSL-FO Document
      * @param dst   output stream
      * @param outDevice output device. Please refer to a setPrinterName method about the character string to specify. 
      * @throws jp.co.antenna.DfvJavaCtl.DfvException
      */
     public void render (InputStream src, OutputStream dst, String outDevice) throws DfvException {
         if (this.messageListener != null)
             this.messageListener.onMessage(4, 0, "render() is not implemented yet.");
         throw new DfvException(4, 0, "render() is not implemented yet.");
     }
     
     /**
      * Set the URI of XML document to be formatted.
      * <br>If specified "@STDIN", XML document reads from stdin. The document that is read from stdin is assumed to be FO. 
      * 
      * @param uri URI of XML document
      * @throws jp.co.antenna.DfvJavaCtl.DfvException
      */
     public void setDocumentURI (String uri) throws DfvException {
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
 
 	public void setEndPage(int num) {
 		String opt = "-end";
 		if (this.args.containsKey(opt))
 			this.args.remove(opt);
 		this.args.put(opt, String.valueOf(num));
 	}
 
     public void setErrorLogPath (String path) {
         if (path != null && !path.equals("")) {
             this.logPath = " 2>> " + path;
         } else {
             this.logPath = null;
         }
     }
     
     /**
      * Set the error level to abort formatting process.
      * <br>Server Based Converter will stop formatting when the detected error level is equal to setExitLevel setting or higher.
      * <br>The default value is 2 (Warning). Thus if an error occurred and error level is 2 (Warning) or higher, the 
      * formatting process will be aborted. Please use the value from 1 to 4. When the value of 5 or more is specified, 
      * it is considered to be the value of 4. If a error-level:4 (fatal error) occurs, the formatting process will be 
      * aborted unconditionally. Note: An error is not displayed regardless what value may be specified to be this property. 
      * 
      * @param level Error level to abort
      * @throws jp.co.antenna.DfvJavaCtl.DfvException
      */
     public void setExitLevel (int level) throws DfvException {
         // Set the level...
         String opt = "-extlevel";
         if (this.args.containsKey(opt))
             this.args.remove(opt);
         this.args.put(opt, String.valueOf(level));
     }
 
 	public void setFontAlias (String src, String dst) {
 		String opt = "-fontalias";
 		this.args.put(opt, src + "=" + dst);
 	}
 
 	public void setFormatPageListener (DfvFormatPageListener listener) {
 		// Do nothing
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
 
 	public void setOmitBlankPages(boolean newVal) {
 		String opt = "-omitbp";
 		if (this.args.containsKey(opt))
 			this.args.remove(opt);
 		if (newVal)
 			this.args.put(opt, "");
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
      * @throws jp.co.antenna.DfvJavaCtl.DfvException
      */
     public void setOutputFilePath (String path) throws DfvException {
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
     
 	public void setPdfEmbedAllFonts (boolean embedBool) {
 		String opt = "-peb";
 		if (this.args.containsKey(opt))
 			this.args.remove(opt);
 		if (embedBool) {
 			this.args.put(opt, String.valueOf(S_PDF_EMBALLFONT_ALL));
 		}
 	}
 
     public void setPdfEmbedAllFontsEx (int embedLevel) throws DfvException {
         // fill it in
         String opt = "-peb";
 		if (this.args.containsKey(opt))
 			this.args.remove(opt);
 		this.args.put(opt, String.valueOf(embedLevel));
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
     
 	public void setSvgFormat (String newVal) {
 		String opt = "-svgfmt";
 		if (newVal != null && !newVal.equals("")) {
 			if (this.args.containsKey(opt))
 				this.args.remove(opt);
 			this.args.put(opt, newVal);
 		}
 		else {
 			this.args.remove(opt);
 		}
 	}
 
 	public void setSvgSingleFile (boolean newVal) {
 		String opt = "-svgsingle";
 		if (this.args.containsKey(opt))
 			this.args.remove(opt);
 		if (newVal)
 			this.args.put(opt, "");
 	}
 
 	public void setSvgSinglePageNumber (boolean newVal) {
 		String opt = "-svgspn";
 		if (this.args.containsKey(opt))
 			this.args.remove(opt);
 		if (newVal)
 			this.args.put(opt, "");
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
                 if (line.startsWith("SBCCmd :")) {
                     if (line.contains("Error Level")) {
                         try {
                             int ErrorLevel = Integer.parseInt(line.substring(line.length() - 1, line.length()));
                             line = reader.readLine();
                             int ErrorCode = Integer.parseInt(line.split(" ")[line.split(" ").length - 2]);
                             line = reader.readLine();
                             String ErrorMessage = line.split(" ", 3)[2];
                             line = reader.readLine();
                             if (line.startsWith("SBCCmd :")) {
                                 ErrorMessage += "\n" + line.split(" ", 3)[2];
                             }
                             this.LastErrorLevel = ErrorLevel;
                             this.LastErrorCode = ErrorCode;
                             this.LastErrorMessage = ErrorMessage;
 							if (this.listener != null)
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
 					if (this.listener != null)
 						this.listener.onMessage(ErrorLevel, ErrorCode, ErrorMessage);
 				}
                 line = reader.readLine();
             }
         } catch (Exception e) {}
     }
 }
