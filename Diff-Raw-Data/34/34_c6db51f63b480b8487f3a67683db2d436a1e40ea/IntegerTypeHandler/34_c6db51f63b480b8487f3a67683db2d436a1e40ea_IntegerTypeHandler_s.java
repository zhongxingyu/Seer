 /*
  * Last modification information:
 * $Revision: 1.3 $
 * $Date: 2005-01-27 16:45:29 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.util.Properties;
 
 import org.jdom.Element;
 
 /**
  * BooleanTypeHandler
  * Class name and description
  *
  * Date created: Oct 4, 2004
  *
  * @author scott<p>
  *
  */
 public class IntegerTypeHandler extends PrimitiveResourceTypeHandler
 {
 	public IntegerTypeHandler()
 	{
		super("boolean");
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.portfolio.xml.ResourceTypeHandler#handleElement(org.w3c.dom.Element, java.util.Properties)
 	 */
 	public Object handleElement(String value, Properties elementProps)
 		throws HandleElementException
 	{
 		try {
 			return Integer.decode(value);
 		} catch (Throwable e) {
 			throw new HandleElementException("malformed integer");
 		}
 	}
 
 }
