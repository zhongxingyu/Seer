 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.server.CustomCallingConvention;
 import org.xins.server.FunctionNotSpecifiedException;
 import org.xins.server.FunctionRequest;
 import org.xins.server.FunctionResult;
 import org.xins.server.InvalidRequestException;
 
 /**
  *
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class CustomTestConvention extends CustomCallingConvention {
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
    
    /**
     * Creates a new instance of CustomTestConvention
     */
    public CustomTestConvention() {
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException, FunctionNotSpecifiedException {
      String[] query = httpRequest.getParameterValues("query");
      if (query.length > 1) {
         throw new InvalidRequestException("Multiple values for input parameter \"query\": \"" + query[0] + "\" and \"" + query[1] + "\".");
      }

       BasicPropertyReader properties = new BasicPropertyReader();
      properties.set("inputText", query[0]);
       properties.set("useDefault", "false");
       return new FunctionRequest("ResultCode", properties, null);
    }
 
    protected void convertResultImpl(FunctionResult xinsResult, 
          HttpServletResponse httpResponse, HttpServletRequest httpRequest) throws IOException {
 
       if (xinsResult.getErrorCode() != null) {
          httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
       } else {
          httpResponse.setStatus(HttpServletResponse.SC_OK);
          PrintWriter out = httpResponse.getWriter();
          out.print("Done.");
          out.close();
       }
    }
    
 }
