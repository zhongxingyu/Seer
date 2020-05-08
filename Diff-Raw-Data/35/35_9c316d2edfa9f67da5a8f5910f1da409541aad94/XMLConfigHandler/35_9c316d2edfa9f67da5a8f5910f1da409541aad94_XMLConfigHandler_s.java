 /*
  *    
    Copyright 2011 Andreas Hohnholt
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    */
 package net.smart_json_database;
 
import net.smart_json_databsase.tools.Logger;
import net.smart_json_databsase.tools.Util;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /*
  * 
  * Parser for the EntityConfiguration.XML
  * 
  * Author Andreas Hohnholt
  * 
  */
 public class XMLConfigHandler extends DefaultHandler{
 
 	Logger LOG = Logger.CreateLogger("XMLHandler");
 	
 	private String dbName = "";
 	private int dbVersion = -1;
 	private String upgradeClass = "";
 	
 
 	protected String getDbName() {
 		return dbName;
 	}
 
 
 
 	protected int getDbVersion() {
 		return dbVersion;
 	}
 
 
 
 	protected String getUpgradeClass() {
 		return upgradeClass;
 	}
 
 
 
 	@Override
 	public void startDocument() throws SAXException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 		// TODO Auto-generated method stub
 		
 
 		if("Database".equals(localName))
 		{
 			String value = attributes.getValue("name");
 			
 			if(!Util.IsNullOrEmpty(value))
 			{
 				dbName = value;
 			}
 			
 			value = attributes.getValue("version");
 			
 			if(!Util.IsNullOrEmpty(value))
 			{
 				dbVersion = Integer.parseInt(value);
 			}
 			
 			value = attributes.getValue("upgradeClass");
 			
 			if(!Util.IsNullOrEmpty(value))
 			{
 				upgradeClass = value;
 			}
 		}
 	}
 	
 	
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		// TODO Auto-generated method stub
 		super.endElement(uri, localName, qName);
 	}
 	
 	@Override
 	public void endDocument() throws SAXException {
 		// TODO Auto-generated method stub
 		super.endDocument();
 	}
 }	
