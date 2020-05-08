 /*
 * $Id: StreamThread.java,v 1.29 2003/08/01 17:28:23 tufte Exp $
  */
 
 package niagara.data_manager;
 
 /** Niagra DataManager
  * StreamThread - an input thread to take a simple stream of XML documents
  * from a file or a socket, for now will just implement the file???
  * Documents in stream are not demarcated, uses a modified xerces
  * parser to parse each document one at a time and put each parsed
  * document into the output stream
  */
 
 import java.io.*;
 import org.xml.sax.*;
 
 import niagara.logical.StreamOp;
 import niagara.ndom.*;
 import niagara.optimizer.colombia.*;
 import niagara.query_engine.TupleSchema;
 import niagara.utils.*;
 import niagara.xmlql_parser.op_tree.*;
 import niagara.firehose.*;
 import niagara.connection_server.NiagraServer;
 
 public class StreamThread extends SourceThread {
     private StreamSpec spec;
     private InputStream inputStream;
     private SinkTupleStream outputStream;
     private niagara.ndom.DOMParser parser;
     private FirehoseClient firehoseClient;
     private BufferedReader bufferedInput = null;
     private MyArray buffer; 
     private CPUTimer cpuTimer;
 
     // Optimization-time attributes
     private Attribute variable;
 
     public void opInitFrom(LogicalOp lop) {
         StreamOp op = (StreamOp) lop;
         spec = op.getSpec();
         variable = op.getVariable();
     }
 
     public void constructTupleSchema(TupleSchema[] inputSchemas) {
         ;
     }
 
     public TupleSchema getTupleSchema() {
         TupleSchema ts = new TupleSchema();
         ts.addMapping(variable);
         return ts;
     }
 
 
     /**
      * Thread run method
      *
      */
     public void run() {
 	if(niagara.connection_server.NiagraServer.RUNNING_NIPROF)
 	    JProf.registerThreadName(this.getName());
 
 	if(niagara.connection_server.NiagraServer.TIME_OPERATORS) {
 	    cpuTimer = new CPUTimer();
 	    cpuTimer.start();
 	}
 
         if (NiagraServer.usingSAXDOM())
             parser = DOMFactory.newParser("saxdom");
         else
             parser = DOMFactory.newParser();
 
 	boolean sourcecreated = false;
 	boolean shutdown = false;
 	String message =  "normal";
 
 	try {
 	    inputStream = createInputStream();
 	   
 	    // firehose scan, we assume stream format
 	    // and must use SAXDOMParser
 	    // stream spec is either firehose spec or filescan spec
 
 	    if(parser instanceof SAXDOMParser && spec.isStream()) {
 		// Use SAXDOMParser - assumes niagara:stream format
 		((SAXDOMParser)(parser)).setOutputStream(outputStream);
 		InputSource inputSource = new InputSource(inputStream);
 		parser.parse(inputSource);
 		/* }else if(parser.supportsStreaming()) {
 		// This must be a modified Xerces parser in streaming mode
 
 		// stream is done when inputStream.read() returns -1
 		InputSource inputSource = new InputSource(inputStream);
 		sourcecreated=true;
 		while(true) {
 		    // IOException is thrown when done - handled below
 		    parser.parse(inputSource);
 		    outputStream.put(parser.getDocument());
 		    }*/
 	    } else if(!spec.isStream()) {
 		// have a regular file and no stream format
 		// means only one document
 		InputSource inputSource = 
 		    new InputSource(inputStream);
 		parser.parse(inputSource);
 		outputStream.put(parser.getDocument());
 	    } else {
 		throw new PEException("KT: Unsupported");
 		// Are not using saxdom, but have a stream document
 		// We assume in this case that there is no niagara:stream
 		// format 
 
 		/* Please save this code in case I want it again someday KT
 		System.out.println("KT: WARNING USING INEFFICIENT READ IN");
 		try {
 		    bufferedInput = 
 			new BufferedReader(new InputStreamReader(inputStream));
 		    buffer = new MyArray(8192);
 
 		    boolean keepgoing = true;
 		    boolean startOfDoc = false;		    
 		    char cbuf[] = new char[5];
 
 		    bufferedInput.read(cbuf, 0, 5);
 		    if(cbuf[0] != '<' || cbuf[1] != '?' || cbuf[2] != 'x' ||
 		       cbuf[3] != 'm' || cbuf[4] != 'l')
 			throw new DMException("Invalidly formed stream");
 		    
 		    int cBufOffset = 0;
 		    int cBufSize = 5;
 
 		    while(true) {
 			buffer.reset();
 			startOfDoc = false;
 			
 			// at this point cbuf should always hold <?xml
 			buffer.appendChars(cbuf, 5);
 			
 			while(!startOfDoc) {
 			    // look for string <?xml - indicates start of NEXT document
 
 			    // fill the remaining portion of cbuf and check
 			    // for start of document
 			    cBufSize = 5;
 			    int ret = bufferedInput.read(cbuf, 
 							 cBufOffset, 
 							 5-cBufOffset);
 			    if(ret == -1)
 				throw new EOSException();
 			    if(ret != 5-cBufOffset)
 				cBufSize = ret; // at end of file
 			    cBufOffset = 0; // reset for next round;
 
 			    if(cbuf[0] == '<' && cbuf[1] == '?' && 
 			       cbuf[2] == 'x' &&
 			       cbuf[3] == 'm' && cbuf[4] == 'l') {
 				// found start of document
 				startOfDoc = true;
 			    } else {
 				buffer.appendChar(cbuf[0]);
 				for(int i = 1; i<cBufSize; i++) {
 				    if(cbuf[i] != '<') {
 					buffer.appendChar(cbuf[i]);
 				    } else {
 					for(int j = 0; j<cBufSize-i; j++)
 					    cbuf[j] = cbuf[j+i];
 					cBufOffset = 5-i;
 				    }
 				}
 			    }
 			}
 			
 			// parse the buffer and put the resulting document
 			// in the output stream
 			parseAndSendBuffer(buffer);
 		    }
 		} catch(EOSException eosE) {
 		    // parse final doc and put in output stream, then return
 		    parseAndSendBuffer(buffer);
 		}
 		*/
 	    }
 		
 	} catch (org.xml.sax.SAXException saxE){
 	    System.err.println("StreamThread::SAX exception parsing document. Message: " 
 			       + saxE.getMessage());
 	    shutdown = true;
 	    message = "SAX Exception " + saxE.getMessage();
 	} catch (java.lang.InterruptedException intE) {
 	    System.err.println("StreamThread::Interruped Exception::run. Message: " 
 			       + intE.getMessage());
 	    shutdown = true;
 	    message = "StreamThread-Interrupted " + intE.getMessage();
 	} catch (java.io.FileNotFoundException fnfE) {
 	    System.err.println("StreamThread::File not found: filename: " +
 			       "Message" + fnfE.getMessage());
 	    shutdown = true;
 	    message = "StreamThread " + fnfE.getMessage();
 	} catch (java.net.UnknownHostException unhE) {
 	    System.err.println("StreamThread::Unknown Host: host: " +
 			       "Message" + unhE.getMessage());
 	    shutdown = true;
 	    message = "StreamThread " + unhE.getMessage();
 	} catch(java.io.IOException ioe) {
 	    if(!sourcecreated) {
 	       System.err.println("StreamThread::IOException. Message: " 
 				  + ioe.getMessage());
 	       shutdown = true;
 	       message = "StreamThread::IOException " + ioe.getMessage();
 	    } else {
 		System.out.println("KT Stream Thread IOException " +
 				   ioe.getMessage());
 		shutdown = false;
 		message = null;
 	    }
 	    // if source was created IOException tends to mean end
 	    // of stream and should be ignored
 	    /*} catch(niagara.data_manager.DMException dmE) {
 	    System.err.println("StreamThread::Stream Exception. Message " +
 			       dmE.getMessage());
 	    */
 	} catch (ShutdownException se) {
 	    System.err.println("StreamThread::ShutdownException. Message " +
 			       se.getMessage());
 	    shutdown = true;
 	    message = se.getMessage();
 	}
 
 	cleanUp(shutdown, message);
 	return;
     }
 
     private void cleanUp(boolean shutdown, String message) {
 
 	try {
 	    closeInputStream();
 	} catch(java.io.IOException ioe) {
 	    /* do nothing */
 	}
 
 	if(niagara.connection_server.NiagraServer.TIME_OPERATORS) {
 	    cpuTimer.stop();
 	    cpuTimer.print(getName() + "(shutdown: " + message + ")");
 	}
 
 	try {
 	    if(!shutdown)
 		outputStream.endOfStream();
 	    else 
 		outputStream.putCtrlMsg(CtrlFlags.SHUTDOWN, message);
 	} catch (java.lang.InterruptedException ie) {
 	    /* do nothing */
 	} catch (ShutdownException se) {
 	    /* do nothing */
 	}
 
 	spec = null;
 	outputStream = null;
 	inputStream = null;
 	parser = null;
 	
 	return;
     }
 
     /**
      * Create an input stream from either a file or a socket
      */
     private InputStream createInputStream() 
 	throws java.io.FileNotFoundException, 
 	       java.net.UnknownHostException,
 	       java.io.IOException {
 	if(spec instanceof FileScanSpec) {
 	    return createFileStream();
 	} else if (spec instanceof FirehoseSpec) {
 	    return createFirehoseStream();
 	} else {
 	    throw new PEException("Invalid Stream Type");
 	}
     }
 
     /** 
      * create an input stream from the file name given
      */
     private java.io.InputStream createFileStream() 
     throws java.io.FileNotFoundException{
     	String fileName = ((FileScanSpec)spec).getFileName();
 	return new FileInputStream(((FileScanSpec)spec).getFileName());
     }
 
     /** 
      * create an input stream from the socket given
      */
     private InputStream createFirehoseStream() 
 	throws java.net.UnknownHostException, java.io.IOException {
 
 	// create firehose client
 	if(firehoseClient == null)
 	    firehoseClient = new FirehoseClient();
 	return firehoseClient.open_stream((FirehoseSpec)spec);
     }
      
     /**
      * Indicate I'm going away and am not going to read any more data 
      * in the stream - to try to close up sockets, etc.
      */
     private void closeInputStream() 
 	throws java.io.IOException {
 	if(spec instanceof FileScanSpec) {
 	    closeFileStream();
 	} else if (spec instanceof FirehoseSpec) {
 	    closeFirehoseStream();
 	} else {
 	    throw new PEException("Invalid Stream Type ");
 	}
 
 	return;
     }
 
     private void closeFileStream() 
 	throws java.io.IOException {
 	if(inputStream != null) {
 	    inputStream.close();
 	}
     }
 
     private void closeFirehoseStream() 
 	throws java.io.IOException {
 	if(firehoseClient != null) {
 	    firehoseClient.close_stream();
 	}
     }
   
 
     private void parseAndSendBuffer(MyArray buffer) 
 	throws org.xml.sax.SAXException, java.lang.InterruptedException,
 	       java.io.IOException, ShutdownException {
 	InputSource inputSource = 
 	    new InputSource(new CharArrayReader(buffer.getBuf(), 0, buffer.getLen()));
 	parser.parse(inputSource);
 	outputStream.put(parser.getDocument());
     }
 
     private class EOSException extends Exception {
 	public EOSException() {
 	    super("End of Stream Exception");
 	}
     }
 
     // simple array 
     class MyArray {
 	char buffer[];
 	int allocSize;
 	int len;
 
 	MyArray() {
 	    this(8192);
 	}
 
 	MyArray(int size) {
 	    buffer = new char[8192];
 	    allocSize = 8192;
 	    len = 0;
 	}
 
 	void appendChar(char c) {
 	    ensureCapacity();
 	    buffer[len] = c;
 	    len++;
 	}
 
 	void appendChars(char[] cbuf, int cLen) {
 	    for(int i = 0; i<cLen; i++)
 		appendChar(cbuf[i]);
 	}
 
 	void ensureCapacity() {
 	    if(len < allocSize) 
 		return;
 	    
 	    int oldAlloc = allocSize;
 	    while(len >= allocSize) {
 		allocSize *=2;
 	    }
 	    char newBuffer[] = new char[allocSize];
 	    System.arraycopy(buffer, 0, newBuffer, 0, oldAlloc);
 	    buffer = newBuffer;
 	    return;
 	}
 
 	char[] getBuf() {
 	    return buffer;
 	}
 
 	void reset() {
 	    len = 0;
 	}
 
 	int getLen() {
 	    return len;
 	}
 
 	public String toString() {
 	    String s = new String(buffer, 0, len);
 	    return s;
 	}
     }
     
     public void plugIn(SinkTupleStream outputStream, DataManager dm) {
         this.outputStream = outputStream;
     }
     
     /**
      * @see niagara.optimizer.colombia.PhysicalOp#FindLocalCost(ICatalog, LogicalProperty, LogicalProperty[])
      */
     public Cost findLocalCost(
         ICatalog catalog,
         LogicalProperty[] InputLogProp) {
         // XXX vpapad: totally bogus flat cost for stream scans
         return new Cost(catalog.getDouble("stream_scan_cost"));
     }
 
     /**
      * @see java.lang.Object#equals(Object)
      */
     public boolean equals(Object o) {
         if (o == null || !(o instanceof StreamThread)) return false;
         if (o.getClass() != getClass()) return o.equals(this);
         // XXX vpapad: Spec.equals is Object.equals
         return spec.equals(((StreamThread) o).spec);
     }
 
     /**
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         // XXX vpapad: spec's hashCode is Object.hashCode()
         return spec.hashCode();
     }
     
     /**
      * @see niagara.optimizer.colombia.Op#copy()
      */
     public Op opCopy() {
         StreamThread st = new StreamThread();
         st.spec = spec;
         st.variable = variable;
         return st;
     }
 
     /**
      * @see niagara.utils.SerializableToXML#dumpAttributesInXML(StringBuffer)
      */
     public void dumpAttributesInXML(StringBuffer sb) {
         sb.append(" var='").append(variable.getName());
         sb.append("'/>");
     }
 
     /**
      * @see niagara.utils.SerializableToXML#dumpChildrenInXML(StringBuffer)
      */
     public void dumpChildrenInXML(StringBuffer sb) {
         ;
     }
 
     public boolean prettyprint() {
         return spec.prettyprint();
     }
 }
 
