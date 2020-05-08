 /*
  *  $Id: PWAction.java 2013/09/28 1:12:41 masamitsu $
  *
  *  ===============================================================================
  *
  *   Copyright (C) 2013  Masamitsu Oikawa  <oicawa@gmail.com>
  *   
  *   Permission is hereby granted, free of charge, to any person obtaining a copy
  *   of this software and associated documentation files (the "Software"), to deal
  *   in the Software without restriction, including without limitation the rights
  *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *   copies of the Software, and to permit persons to whom the Software is
  *   furnished to do so, subject to the following conditions:
  *   
  *   The above copyright notice and this permission notice shall be included in
  *   all copies or substantial portions of the Software.
  *   
  *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *   THE SOFTWARE.
  *
  *  ===============================================================================
  */
 
 package pw.core.action;
 
 import pw.core.PWField;
 import pw.core.PWSession;
import pw.core.PWField.KeyType;
 import pw.core.accesser.PWQuery;
 import pw.core.item.PWItem;
 
 /**
  * @author masamitsu
  *
  */
 public abstract class PWAction {
 	
 	protected PWSession session;
 	
 	public PWAction() {
 	}
 	
 	/**
 	 * @param arguments
 	 */
 	protected abstract void parseArguments(String[] arguments);
 
 	public abstract Object run(Object... objects);
 	
 	public void setSession(PWSession session)
 	{
 		this.session = session;
 	}
 	
 	public void setParameters(String[] arguments) {
 		parseArguments(arguments);
 	}
 	
 	public static String getWhereQueryByKeys(Class<? extends PWItem> itemType, PWField.KeyType keyType) {
     	StringBuffer buffer = new StringBuffer();
     	for (PWField field : PWItem.getFields(itemType, keyType)) {
  			buffer.append(PWQuery.AND);
  			buffer.append(field.getName());
  			buffer.append(" = ?");
     	}
     	String wheresQuery = buffer.substring(PWQuery.AND.length());
     	return wheresQuery;
 	}
 	
 	public static String getWhereQuery(Class<? extends PWItem> itemType, PWItem item) {
     	StringBuffer buffer = new StringBuffer();
     	for (PWField field : PWItem.getFields(itemType)) {
     		Object value = field.getValue(item);
     		if (value == null) {
     			continue;
     		}
  			buffer.append(PWQuery.AND);
  			buffer.append(field.getName());
  			buffer.append(" = ?");
     	}
     	String wheresQuery = buffer.substring(PWQuery.AND.length());
     	return wheresQuery;
 	}
 }
