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
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import javax.xml.transform.sax.TransformerHandler;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  * Implements element exporter for all non-map collection types, i.e.
  * {@link List}, {@link Set}, {@link Collection} and array.
  * @author imyousuf
  * @since 0.4
  */
 public class GenericCollectionElementExporter
 				extends AbstractElementExporter
 				implements ElementExporter {
 
 		private static ElementExporter exporter;
 
 		static {
 				exporter = new GenericCollectionElementExporter();
 				for (AssociationType type : exporter.getSupportedTypes()) {
 						XMLExporterImpl.addHandler(type, exporter);
 				}
 		}
 
		public static ElementExporter getInstance() {
 				return exporter;
 		}
 
 		public GenericCollectionElementExporter() {
 				Collections.addAll(types, AssociationType.TYPE_ARRAY,
 								AssociationType.TYPE_COLLECTION, AssociationType.TYPE_LIST,
 								AssociationType.TYPE_SET);
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
						handler.startElement(EXIM_COLLECN_URI, EXIM_COLLECTION_NS, type.
 										getSimpleName(), atts);
 						Collection objects;
 						switch (type) {
 								case TYPE_ARRAY:
 										objects = Arrays.asList((Object[]) object);
 										break;
 								case TYPE_LIST:
 								case TYPE_SET:
 								case TYPE_COLLECTION:
 										objects = (Collection) object;
 										break;
 								default:
 										throw new IOException("Unsupported type - " + type);
 						}
 						for (Object exportObj : objects) {
 								handler.startElement("", "", "item",
 												atts);
 								AssociationType associationType = AssociationType.
 												getAssociationType(exportObj.getClass());
 								XMLExporterImpl.getElementExporter(associationType).
 												exportElement(associationType, exportObj, handler);
 								handler.endElement("", "", "item");
 						}
						handler.endElement(EXIM_COLLECN_URI, EXIM_COLLECTION_NS, type.
 										getSimpleName());
 				}
 				catch (Exception ex) {
 						throw new IOException(ex);
 				}
 		}
 }
