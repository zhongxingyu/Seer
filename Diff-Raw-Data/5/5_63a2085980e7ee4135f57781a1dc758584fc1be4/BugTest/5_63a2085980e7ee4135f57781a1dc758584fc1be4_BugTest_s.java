 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.arco;
 
 import com.sun.grid.arco.model.*;
 import com.sun.grid.arco.xml.XMLQueryResult;
 import com.sun.grid.logging.SGELog;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 import junit.framework.TestCase;
 
 public class BugTest  extends TestCase {
    
    /** Creates a new instance of BugTest */
    public BugTest(String name) {
       super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
       SGELog.init(Logger.global);
    }
    
    /**
     * Tests the Bug 6418074:
     *
     *   ARCo webapplication can not display results with empty value tags
     *
     * @throws java.lang.Exception
     */
    public void test6418074() throws Exception {
       
       ObjectFactory faq = new ObjectFactory();
       
       Result res = faq.createResult();
       
       res.setName( "test bug 6418074" );
       res.setCategory( "bugs" );
       res.setSql("select * from blubber");
       
       Class [] columnTypes = { Integer.class, String.class, Double.class, Date.class };
       
       for( int i = 0; i < columnTypes.length; i++ ) {
          
          ResultColumn column = faq.createResultColumn();
          column.setIndex(i);
          column.setName( "col" + i );
          column.setType( ResultConverter.getColumnType(columnTypes[i]));
          res.getColumn().add(column);
          
       }
       
       int rowCount = 2;
       res.setRowCount(rowCount);
       ResultRow rowObj = null;
       int row = 0;
       int col = 0;
       List valueList = null;
       List rowList = res.getRow();
       for(row = 0; row < rowCount; row++) {
          rowObj = faq.createResultRow();
          rowList.add(rowObj);
          valueList = rowObj.getValue();
          for(col = 0; col < columnTypes.length; col++) {
             valueList.add(ResultConverter.objToStr(null));
          }
       }

       XMLQueryResult xmlQueryResult = new XMLQueryResult(res);
       
       // The xml query result will throw an exception of Bug 6418074 is not fixed
       xmlQueryResult.execute();
    }
    
    
    
 }
