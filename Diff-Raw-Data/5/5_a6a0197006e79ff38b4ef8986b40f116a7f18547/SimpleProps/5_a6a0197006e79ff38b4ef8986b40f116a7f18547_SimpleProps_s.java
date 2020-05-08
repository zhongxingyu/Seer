 /**
 Copyright 2008, 2009 Mark Hooijkaas
 
 This file is part of the RelayConnector framework.
 
 The RelayConnector framework is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 The RelayConnector framework is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.kisst.cfg4j;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.kisst.util.StringUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SimpleProps extends PropsBase {
 	private static final Logger logger = LoggerFactory.getLogger(SimpleProps.class);
 	private static final long serialVersionUID = 1L;
 
 	private final SimpleProps parent;
 	private final String name; 
 	private final Map<String, Object> values=new LinkedHashMap<String, Object>();
 	
 	public SimpleProps() { this(null,null); }
 	public SimpleProps(SimpleProps parent, String name) {
 		this.parent=parent;
		this.name=name;
 	}
 	public String getLocalName() { return name; }
 	public String getFullName() {
 		if (parent==null)
 			return name;
 		else { 
 			String prefix=parent.getFullName();
 			if (prefix==null)
 				return name;
 			else
 				return prefix+"."+name;
 		}
 	}
 
 	public Iterable<String> keys() { return values.keySet(); }
 
 	public void put(String key, Object value) {
 		int pos=key.indexOf('.');
 		if (pos<0) {
 			if (value==null) {
 				if (logger.isInfoEnabled())
 					logger.info("removing {}",getFullName()+"."+key);
 				values.remove(key);
 			}
 			else {
 				if (logger.isInfoEnabled())
 					logger.info("put {} = {}",getFullName()+"."+key,value);
 				values.put(key, value);
 			}
 			return;
 		}
 		String keystart=key.substring(0,pos);
 		String keyremainder=key.substring(pos+1);
 		Object o=values.get(keystart);
 		if (o==null) {
 			SimpleProps props=new SimpleProps(this,keystart);
 			values.put(keystart, props);
 			props.put(keyremainder, value);
 		}
 		else if (o instanceof SimpleProps)
 			((SimpleProps)o).put(keyremainder, value);
 		else
 			throw new RuntimeException("key "+getFullName()+"."+key+" already has value "+o+" when adding subkey "+keyremainder);
 	}
 
 	public Object get(String key, Object defaultValue) {
 		logger.debug("getting {}",key);
 		int pos=key.indexOf('.');
 		if (pos<0) {
 			Object result=values.get(key);
 			if (logger.isInfoEnabled())
 				logger.info("returned prop {} with value {}",getFullName()+"."+key,result);
 			if (result==null)
 				return defaultValue;
 			else
 				return result;
 		}
 		String keystart=key.substring(0,pos);
 		String keyremainder=key.substring(pos+1);
 		Object o=values.get(keystart);
 		if (o==null)
 			return defaultValue;
 		else if (o instanceof SimpleProps)
 			return ((SimpleProps)o).get(keyremainder,null);
 		else
 			return defaultValue;
 	}
 
 	public void load(String filename)  { new Parser(filename).fillMap(this);	}
 	public void load(File file)        { new Parser(file).fillMap(this); }
 	public void read(Reader inp)       { new Parser(inp).fillMap(this);}
 	public void read(InputStream inp)  { new Parser(inp).fillMap(this);} 
 
 
 	public String toString() { return toString("");	}
 	public String toString(String indent) {
 		StringBuilder result=new StringBuilder("{\n");
 		for (String key: values.keySet()) {
 			result.append(indent+"\t"+key+": ");
 			Object o=values.get(key);
 			if (o instanceof SimpleProps)
 				result.append(((SimpleProps)o).toString(indent+"\t"));
 			else if (o instanceof String)
 				result.append(StringUtil.doubleQuotedString((String)o)+";");
 			else if (o instanceof File)
 				result.append("@file("+o.toString()+")");
 			else
 				result.append(o.toString());
 			result.append("\n");
 		}
 		result.append(indent+"}");
 		return result.toString();
 	}
 	public String toPropertiesString() {
 		StringBuilder result=new StringBuilder();
 		for (String key: values.keySet()) {
 			Object o=values.get(key);
 			if (o instanceof SimpleProps) {
 				result.append(((SimpleProps)o).toPropertiesString());
 				continue;
 			}
 			result.append(getFullName()+"."+key+"=");
 			if (o instanceof String)
 				result.append(StringUtil.doubleQuotedString((String)o)+"\n");
 			else if (o instanceof File)
 				result.append("@file("+o.toString()+")\n");
 			else
 				result.append(o.toString()+"\n");
 		}
 		return result.toString();
 	}
 
 }
