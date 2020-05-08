 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2009 Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.exim.impl.xml;
 
 import com.smartitengineering.exim.AssociationConfig.AssociationType;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import javax.xml.transform.sax.TransformerHandler;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  *
  * @author imyousuf
  * @since 0.4
  */
 public class MapElementExporter
 				extends AbstractElementExporter
 				implements ElementExporter {
 
 		private static ElementExporter exporter;
 
 		static {
 				exporter = new MapElementExporter();
 				for (AssociationType type : exporter.getSupportedTypes()) {
 						XMLExporterImpl.addHandler(type, exporter);
 				}
 		}
 
		public ElementExporter getInstance() {
 				return exporter;
 		}
 
 		public MapElementExporter() {
 				Collections.addAll(types, AssociationType.TYPE_MAP);
 		}
 
 		public void exportElement(AssociationType type,
 															Object object,
 															TransformerHandler handler)
 						throws IOException {
 				if (type == null || object == null || handler == null) {
 						throw new IOException("All parameters are required!");
 				}
 				try {
 						AttributesImpl atts = new AttributesImpl();
						handler.startElement(EXIM_COLLECN_NS, EXIMCOLLECTION, type.
 										getSimpleName(), atts);
 						Map map = (Map) object;
 						Set<Map.Entry> objects = map.entrySet();
 						for (Map.Entry entry : objects) {
 								Object key = entry.getKey();
 								Object value = entry.getValue();
 								handler.startElement("", "", "key",
 												atts);
 								AssociationType associationType = AssociationType.
 												getAssociationType(key.getClass());
 								XMLExporterImpl.getElementExporter(associationType).
 												exportElement(associationType, key, handler);
 								handler.endElement("", "", "key");
 								handler.startElement("", "", "value",
 												atts);
 								associationType = AssociationType.getAssociationType(value.
 												getClass());
 								XMLExporterImpl.getElementExporter(associationType).
 												exportElement(associationType, value, handler);
 								handler.endElement("", "", "value");
 						}
						handler.endElement(EXIM_COLLECN_NS, EXIMCOLLECTION, type.
 										getSimpleName());
 				}
 				catch (Exception ex) {
 						throw new IOException(ex);
 				}
 		}
 }
