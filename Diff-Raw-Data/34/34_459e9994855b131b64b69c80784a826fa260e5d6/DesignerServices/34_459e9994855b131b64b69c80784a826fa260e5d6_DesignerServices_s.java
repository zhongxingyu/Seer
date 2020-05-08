 /*
  * Copyright 2009 www.scribble.org
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.scribble.protocol.designer;
 
 import org.scribble.protocol.export.ProtocolExportManager;
 import org.scribble.protocol.monitor.ProtocolMonitor;
import org.scribble.protocol.parser.ProtocolParser;
 import org.scribble.protocol.parser.ProtocolParserManager;
 import org.scribble.protocol.projection.ProtocolProjector;
 import org.scribble.protocol.validation.ProtocolValidationManager;
 
 /**
  * This class provides a manager for accessing services used
  * by the designer.
  *
  */
 public class DesignerServices {
 	
     public static final String PROTOCOL_FILE_EXTENSION = "spr";
 
 	private static ProtocolValidationManager m_validationManager=null;
 	private static ProtocolParserManager m_parserManager=null;
 	private static ProtocolProjector m_protocolProjector=null;
 	private static ProtocolMonitor m_protocolMonitor=null;
 	private static ProtocolExportManager m_protocolExportManager=null;
 
 	public static ProtocolValidationManager getValidationManager() {
 		return(m_validationManager);
 	}
 	
 	public static void setValidationManager(ProtocolValidationManager vm) {
 		m_validationManager = vm;
 	}
 	
 	public static ProtocolParserManager getParserManager() {
 		return(m_parserManager);
 	}
 	
 	public static void setParserManager(ProtocolParserManager pm) {
 		m_parserManager = pm;
 	}
 	
 	public static ProtocolMonitor getProtocolMonitor() {
 		return(m_protocolMonitor);
 	}
 	
 	public static void setProtocolMonitor(ProtocolMonitor parser) {
 		m_protocolMonitor = parser;
 	}
 	
 	public static ProtocolProjector getProtocolProjector() {
 		return(m_protocolProjector);
 	}
 	
 	public static void setProtocolProjector(ProtocolProjector projector) {
 		m_protocolProjector = projector;
 	}	
 	
 	public static ProtocolExportManager getProtocolExportManager() {
 		return(m_protocolExportManager);
 	}
 	
 	public static void setProtocolExportManager(ProtocolExportManager pem) {
 		m_protocolExportManager = pem;
 	}	
 }
