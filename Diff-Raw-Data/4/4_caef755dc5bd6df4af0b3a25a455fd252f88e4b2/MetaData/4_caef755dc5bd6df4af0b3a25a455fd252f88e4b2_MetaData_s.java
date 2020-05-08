 /**
  * SAHARA Scheduling Server
  *
  * MetaData Class. 
  * The metadata attribute identifier in the manifest file.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Herber Yeung
  * @date 4th November 2010
  */
 package au.edu.labshare.schedserver.scormpackager.manifest;
 
 import java.util.HashMap;
 
 import au.edu.labshare.schedserver.scormpackager.lila.Manifest;
 import au.edu.labshare.schedserver.scormpackager.utilities.ScormUtilities;
 
 public class MetaData 
 {
 	public static final int STRING_SCHEMA_MAX_LENGTH = 100;
 	public static final int STRING_SCHEMA_VERSION_MAX_LENGTH = 20;
 	
 	public static final String XMLNS_IMSCP = "http://www.imsproject.org/xsd/imscp_rootv1p1p2";
 	public static final String XMLNS_ADLCP = "http://www.adlnet.org/xsd/adlcp_rootv1p2";
 	public static final String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
 	public static final String XSI_IMSCP_SCHEMALOC = "http://www.imsproject.org/xsd/imscp_rootv1p1p2 imscp_rootv1p1p2.xsd";
 	public static final String XSI_IMSMD_SCHEMALOC = "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1 imsmd_rootv1p2p1.xsd";
 	public static final String XSI_ADLCP_SCHEMALOC = "http://www.adlnet.org/xsd/adlcp_rootv1p2 adlcp_rootv1p2.xsd";
 	
 	HashMap<String, String> schema;
 	String schemaVersion;
 	String identifier; 
 	
 	public MetaData()
 	{
 		schema = new HashMap<String, String>();
 		schema.put("version", Manifest.SCHEMA_VERSION);
		schema.put("xmlns", XSI_ADLCP_SCHEMALOC);
		schema.put("xmlns:adlcp", XMLNS_IMSCP);
 		schema.put("xmlns:xsi", XMLNS_XSI);
 		schema.put("xsi:schemalocation", XSI_IMSCP_SCHEMALOC	
 										+ "        " + XSI_IMSMD_SCHEMALOC  
 										+ "        " + XSI_ADLCP_SCHEMALOC);
 	}
 	
 	public String getSchemaValue(String key)
 	{
 		if(schema.containsKey(key))
 		{
 			return schema.get(key);
 		}
 		else
 			return null;
 	}
 	
 	public void addSchemaAttribute(String key, String value)
 	{
 		if(key != null && value != null)
 		{
 			schema.put(key, value);
 		}
 	}
 	
 	public String getSchemaVersion()
 	{
 		return schemaVersion;
 	}
 	
 	public void setSchemaVersion(String schemaVersion)
 	{
 		if(schemaVersion.length() <= STRING_SCHEMA_VERSION_MAX_LENGTH)
 			this.schemaVersion = schemaVersion;
 	}
 	
 	public String getIdentifer()
 	{
 		return identifier;
 	}
 	
 	public void setIdentifer(String experimentIdentifier)
 	{
 		//Replace any whitespace from name with underscore 
 		if(experimentIdentifier != null)
 		{
 			this.identifier = ScormUtilities.replaceWhiteSpace(experimentIdentifier, null);
 			schema.put("identifier", experimentIdentifier);	//Add this also to the schema
 		}
 	}
 }
