 
 /**********************************************************************
  $Id: attr.java,v 1.4 2003/02/20 05:43:15 vpapad Exp $
 
 
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
 
 
 /**
  * This class is used to store attribute name and its value.
  *
  */
 package niagara.xmlql_parser.syntax_tree;
 
 import niagara.utils.CUtil;
 
 
 public class attr {
     
     private String name;	// name of the attribute
     private data value;	        // its value
 
     /**
      * Constructor
      *
      * @param name of the attribute
      * @param value of the attribute ( identifier, variable or schema attribute)
      */
 
     public attr(String s, data d) {
 	name = s;
 	value = d;
     }
 
     /**
      * @return the name of the attribute
      */
 
     public String getName() {
 	return name;
     }
 
     /**
      * @return value of the attribute
      */
 
     public data getValue() {
 	return value;
     }
 
     /**
      * if the value is a variable, then replace it with a schema attribute
      * representing the position of the corresponding schema unit in the
      * schema
      *
      * @param variable table that maps the variable to schema attribute
      */
 
     public void replaceVar(varTbl vt) {
 	int type = value.getType();
 	schemaAttribute sa;
 
 	if(type == dataType.VAR) {
 		String var = (String)value.getValue();
 		sa = vt.lookUp(var);
		// XXX vpapad: Ugh.. try to search for the variable
		// without the leading dollar sign
		if (sa == null && var.charAt(0) == '$') {
		    sa = vt.lookUp(var.substring(1));
		}
 		value = new data(dataType.ATTR,sa);
 	}
     }
 
     /**
      * prints this class on the standard output
      *
      * @param number of tabs before each line
      */
 
     public void dump(int i) {
 	CUtil.genTab(i);
 	System.out.println("ATTR");
 	CUtil.genTab(i);
 	System.out.println(name);
 	value.dump(i);
     }
 
 };
