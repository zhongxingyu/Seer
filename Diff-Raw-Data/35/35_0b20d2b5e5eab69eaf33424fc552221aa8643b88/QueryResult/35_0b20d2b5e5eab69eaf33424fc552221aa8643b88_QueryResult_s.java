 
 /**********************************************************************
  $Id: QueryResult.java,v 1.19 2003/12/24 01:31:48 vpapad Exp $
 
 
   NIAGARA -- Net Data Management System                                 
                                                                         
   Copyright (c)    Computer Sciences Department, University of          
                        Wisconsin -- Madison                             
   All Rights Reserved.                                                  
                                                                         
   Permission to use, copy, modify and distribute this software and      
   its documentation is hereby granted, provided that both the           
   copyright notice and this permission notice appear in all copies      
   of the software, derivative works or modified versions, and any       
   portions thereof, and that both notices appear in supporting          
   documentation.                                                        
                                                                         
   THE AUTHORS AND THE COMPUTER SCIENCES DEPARTMENT OF THE UNIVERSITY    
   OF WISCONSIN - MADISON ALLOW FREE USE OF THIS SOFTWARE IN ITS "        
   AS IS" CONDITION, AND THEY DISCLAIM ANY LIABILITY OF ANY KIND         
   FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.   
                                                                         
   This software was developed with support by DARPA through             
    Rome Research Laboratory Contract No. F30602-97-2-0247.  
 **********************************************************************/
 
 package niagara.query_engine;
 
 import org.w3c.dom.*;
 
 import niagara.utils.*;
 import niagara.ndom.*;
 import niagara.connection_server.ResultTransmitter;
 
 /**
  * QueryResult is the class at the client that interacts with the
  * output stream of the query
  *
  * @version 1.0
  *
  */
 
 public class QueryResult {
 
     /**
      * This exception is thrown when an attempt is made to get a partial
      * result when a request is already pending
      */
 
     public class AlreadyReturningPartialException extends Exception {
 
     }
 
     /**
      * This is a class for returning result to the user
      */
 
     public class ResultObject {
         public Document result; // the value read from stream
         public boolean isPartial; // is result partial or final
     }
 
     /////////////////////////////////////////////////////////////
     // These are the private data members of the class         //
     /////////////////////////////////////////////////////////////
 
     // The query id assigned by the query engine
     private int queryId;
 
     // The output stream to get results from
     private SourceTupleStream outputStream;
     private PageStream outputPageStream;
 
     // Variable to store whether the output stream is in the process
     // of generating possibly partial results
     private boolean generatingPartialResult;
 
     /**
      * This is the constructor that is initialized with information about
      * the query id and the output stream
      *
      * @param queryId The query id issued by the system
      * @param outputStream The output stream of the query
      */
 
     public QueryResult(int queryId, PageStream outputPageStream) {
         this.queryId = queryId;
         this.outputPageStream = outputPageStream;
         this.outputStream = new SourceTupleStream(outputPageStream);
         this.generatingPartialResult = false;
     }
 
     /**
      * This function returns the query id assigned by the query engine
      *
      * @return query id assigned by the query engine
      */
 
     public int getQueryId() {
         return queryId;
     }
     
     public PageStream getOutputPageStream() {
     	return outputPageStream;
     }
 
     // KT - hack so I don't have to put ResultObject in it's own class,
     // as it is I'm removing about 10,000 extra allocations, so I get
     // one teeny weeny hack
     public ResultObject getNewResultObject() {
         return new ResultObject();
     }
 
     /**
      * This function blocks and waits for the next result from the output
      * stream
      *
      * @exception java.lang.InterruptedException If thread is interrupted during
      *            execution
      *
      * @returns Returns the control flag received from stream
      */
     public int getNextResult(ResultObject resultObject)
         throws java.lang.InterruptedException, ShutdownException {
         // -1 indicates no timeout - infinite timeout
         return internalGetNext(-1, resultObject);
     }
 
     /**
      * This function waits for the next result from the output stream for
      * the specified timeout interval.
      *
      * @param timeout The specified timeout interval
      * @param resultObject An object to be filled in with the result
      *
      */
     public int getNextResult(int timeout, ResultObject resultObject)
         throws ShutdownException, InterruptedException {
         return internalGetNext(timeout, resultObject);
     }
 
     /**
      * This function request partial results to be sent
      *
      * @exception AlreadyReturningPartialException If a partial result
      *            request is pending
      */
 
     public void requestPartialResult()
         throws AlreadyReturningPartialException, ShutdownException {
 
         // If partial results are already invoked, 
         // then raise an exception
         if (generatingPartialResult) {
             throw new AlreadyReturningPartialException();
         }
 
         // Send a request for a partial result		
         System.out.println("QR putting partial request down stream");
         // May return EOS, throw ShutdonwnEx or return NULLFLAG
         // Think I can ignore EOS...famous last words
         int ctrlFlag = outputStream.putCtrlMsg(CtrlFlags.GET_PARTIAL, null);
 
         assert(
             ctrlFlag == CtrlFlags.EOS
                 || ctrlFlag == CtrlFlags.NULLFLAG) : "Unexpected control flag";
 
         // Set the status of generating partial results
         generatingPartialResult = true;
     }
 
     /**
      * This function kills a query
      */
 
     public void kill() {
         try {
             // Attempt to kill the query - best effort - ignore errors
 
             // return from outputStream.putCtrlMsg could be:
             // NULLFLAG, SHUTDOWN, SYNCH_PARTIAL, END_PARTIAL, EOS
             // can ignore all of them
             outputStream.putCtrlMsg(CtrlFlags.SHUTDOWN, "kill query");
         } catch (ShutdownException e) {
             // ignore since we are killing query...
         }
     }
 
     /**
      * send a request for buffer flush down stream - we've been waiting
      * too long for results
      *
      * @returns a result status, similar to what is in resultObject.status
      */
     public int requestBufFlush() throws ShutdownException {
         return outputStream.putCtrlMsg(CtrlFlags.REQUEST_BUF_FLUSH, null);
     }
 
     /**
      * This function blocks and waits for the next result from the output stream.
      *
      * @param timeout Amount of time to sleep on query output stream
      * @param resultObject Object to be filled in with result
      *
      * @return control flag from stream
      *
      * @exception java.lang.InterruptedException If the thread was interrupted
      *            during execution. This happens only without a timeout
      */
 
     private int internalGetNext(int timeout, ResultObject resultObject)
         throws InterruptedException, ShutdownException {
 
         // Get the next element from the query output stream
         resultObject.result = null;
 
         if (timeout < 0) {
             // neg or 0 implies no timeout, we use
             // maxDelay, so we get tuples even from slow streams
             timeout = PageStream.MAX_DELAY;
         }
 
         Tuple tuple = outputStream.getTuple(timeout);
         int ctrlFlag = outputStream.getCtrlFlag();
 
         // Now handle the various types of results
         if (tuple == null) {
             // process the control message
             if (ctrlFlag == CtrlFlags.END_PARTIAL
                 || ctrlFlag == CtrlFlags.SYNCH_PARTIAL) {
                 assert generatingPartialResult : "Unexpected partial result";
                 generatingPartialResult = false;
             }
         } else {
             assert ctrlFlag
                 == CtrlFlags.NULLFLAG : "Unexpected control flag "
                     + CtrlFlags.name[ctrlFlag];
             resultObject.isPartial = tuple.isPartial();
             resultObject.result = extractXMLDocument(tuple);
         }
         return ctrlFlag;
     }
 
     private Document extractXMLDocument(Tuple tupleElement) {
         // First get the last attribute of the tuple
 
         if (ResultTransmitter.OUTPUT_FULL_TUPLE) {
            Document resultDoc = DOMFactory.newDocument();
            Element root = resultDoc.createElement("niagara:tuple");
            resultDoc.appendChild(root);
            for (int i = 0; i < tupleElement.size(); i++) {
                Node tupAttr = tupleElement.getAttribute(i);
                Element elt = tupleAttrToElt(tupAttr, resultDoc);
                root.appendChild(elt);
            }
            return resultDoc;
         }
 
         Node lastAttribute = tupleElement.getAttribute(tupleElement.size() - 1);
         return tupleAttrToDoc(lastAttribute);
     }
 
     private Document tupleAttrToDoc(Node tupAttr) {
 
         if (tupAttr instanceof Document) {
             return (Document) tupAttr;
         } else {
             Document resultDoc = DOMFactory.newDocument();
             Element elt = tupleAttrToElt(tupAttr, resultDoc);
             resultDoc.appendChild(elt);
             return resultDoc;
         }
     }
 
     private Element tupleAttrToElt(Node tupAttr, Document resultDoc) {
 
         if (tupAttr instanceof Element) {
             return (Element) DOMFactory.importNode(resultDoc, tupAttr);
         } else if (tupAttr instanceof Attr) {
             Element newElt =
                 resultDoc.createElement(((Attr) tupAttr).getName());
             Text txt = resultDoc.createTextNode(((Attr) tupAttr).getValue());
             newElt.appendChild(txt);
             return newElt;
         } else if (tupAttr instanceof Text) {
             Element newElt = resultDoc.createElement("Text");
             Text txt = resultDoc.createTextNode(tupAttr.getNodeValue());
             newElt.appendChild(txt);
             return newElt;
         } else if (tupAttr == null) {
             return resultDoc.createElement("niagara:null");
         } else {
             assert false : "KT What did I get?? "
                 + tupAttr.getClass().getName();
             return null;
         }
     }
 
     public String toString() {
         return ("Query Result Object for Query " + queryId);
     }
 }
