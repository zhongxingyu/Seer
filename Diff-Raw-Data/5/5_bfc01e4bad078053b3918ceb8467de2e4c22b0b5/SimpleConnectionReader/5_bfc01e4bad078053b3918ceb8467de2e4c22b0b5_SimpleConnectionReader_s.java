 
 /**********************************************************************
  $Id: SimpleConnectionReader.java,v 1.5 2001/07/17 06:37:48 vpapad Exp $
 
 
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
 
 
 package niagara.client;
 
 import java.net.*;
 import java.io.*;
 
 import java.util.Vector;
 
 import gnu.regexp.*;
 
 class SimpleConnectionReader extends AbstractConnectionReader 
     implements Runnable {
     UIDriverIF ui;
 
     public SimpleConnectionReader(String hostname, int port, 
 				  UIDriverIF ui, DTDCache dtdCache) {
 	super(hostname, port, ui, dtdCache);
 	this.ui = ui;
     }
 
     Vector results = new Vector();
     
     synchronized private void addResult(String line) {
 	    results.addElement(line);
     }
     
     synchronized public String getResults() {
 	String resultStr = "";
 	for (int i=0; i < results.size(); i++) {
 	    resultStr += (String) results.elementAt(i);
 	}
 	results.clear();
 	return resultStr;
     }
 
     /**
      * The run method accumulates result strings
      */
     public void run()   {
 	int local_id = -1, server_id = -1;
 	// Read the connection and throw the callbacks
 	
 	try {
 	    BufferedReader br = new BufferedReader(cReader);
 	    String line;
 	    RE re = new RE("<responseMessage localID\\s*=\\s*\"([0-9]*)\"\\s*serverID\\s*=\\s*\"([0-9]*)\"\\s*responseType\\s*=\\s*\"server_query_id\"");
 	    boolean registered = false;
 	    do {
 		line = br.readLine();
 		if (line.indexOf("<response") == 0  || line.indexOf("</response") == 0) {
 		    if (line.indexOf("\"parse_error\"") != -1) {
 			ui.errorMessage("Syntax error in query!\n");
 		    }
 		    if (!registered && line.indexOf("\"server_query_id\"") != -1) {
 			REMatch m = re.getMatch(line);
 			local_id = Integer.parseInt(m.substituteInto("$1"));
 			server_id = Integer.parseInt(m.substituteInto("$2"));
 			queryRegistry.setServerId(local_id, server_id);
 		    }
 		    if (line.indexOf("\"end_result\">") != -1)
 			ui.notifyFinalResult(local_id);
 		}
 		else {
 		    addResult(line);
                    ((SimpleClient) ui).notifyNew(local_id);
 		}
 		//System.out.println("XXX " + line);
 	    } while (line != null);
 	}
 	catch(Exception e){
 	    System.err.println("An exception in the server connection");
 	    ui.errorMessage("An exception in the server connection");
 	    ui.notifyFinalResult(local_id);
 	}
     }
 }
