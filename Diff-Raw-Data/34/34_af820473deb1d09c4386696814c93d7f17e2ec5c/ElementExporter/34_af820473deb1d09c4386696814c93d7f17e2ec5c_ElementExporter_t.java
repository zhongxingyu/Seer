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
 import java.util.Collection;
 import javax.xml.transform.sax.TransformerHandler;
 
 /**
  * An interface for different types of association exporter implementation. All
  * its implementation should be designed in singleton thread safe way.
  * @author imyousuf
  * @since 0.4
  */
 public interface ElementExporter {
 
		public static final String EXIM_COLLECTION_NS = "exim-collection";
		public static final String EXIM_COLLECN_URI =
 															 "http://www.smartitengineering.com/smart-dao/exim/collections";
		public static final String EXIM_BASIC_TYPES_NS = "exim-basic-types";
		public static final String EXIM_BASIC_TYPES_URI =
															 "http://www.smartitengineering.com/smart-dao/exim/basic-types";
 
 		/**
 		 * This operation is resposnsible for exporting XML elements of type it
 		 * supports. It will use other type exporters for exporting and will only be
 		 * responsible for the types it supports.
 		 * @param type Type to export
 		 * @param object Object to export
 		 * @param handler Handler to use to create elements and attributes
 		 * @throws IOException If any error while exporting
 		 */
 		public void exportElement(final AssociationType type,
 															final Object object,
 															final TransformerHandler handler)
 						throws IOException;
 
 		public Collection<AssociationType> getSupportedTypes();
 }
