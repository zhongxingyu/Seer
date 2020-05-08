 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common Development
  * and Distribution License("CDDL") (collectively, the "License").  You
  * may not use this file except in compliance with the License.  You can
  * obtain a copy of the License at
  * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
  * or packager/legal/LICENSE.txt.  See the License for the specific
  * language governing permissions and limitations under the License.
  *
  * When distributing the software, include this License Header Notice in each
  * file and include the License file at packager/legal/LICENSE.txt.
  *
  * GPL Classpath Exception:
  * Oracle designates this particular file as subject to the "Classpath"
  * exception as provided by Oracle in the GPL Version 2 section of the License
  * file that accompanied this code.
  *
  * Modifications:
  * If applicable, add the following below the License Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  * "Portions Copyright [year] [name of copyright owner]"
  *
  * Contributor(s):
  * If you wish your version of this file to be governed by only the CDDL or
  * only the GPL Version 2, indicate your decision by adding "[Contributor]
  * elects to include this software in this distribution under the [CDDL or GPL
  * Version 2] license."  If you don't indicate a single choice of license, a
  * recipient has the option to distribute your version of this file under
  * either the CDDL, the GPL Version 2 or to extend the choice of license to
  * its licensees as provided above.  However, if you add GPL Version 2 code
  * and therefore, elected the GPL Version 2 license, then the option applies
  * only if the new code is made subject to such option by the copyright
  * holder.
  */
 
 package org.icemobile.model;
 
 
 import java.util.List;
 
 
 /**
  * @see javax.faces.model.ListDataModel
  */
 
 public class ListDataModel<E> extends DataModel<E> {
 
 
     // ------------------------------------------------------------ Constructors
 
 
     /**
      * @see javax.faces.model.ListDataModel#ListDataModel()
      */
     public ListDataModel() {
 
         this(null);
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#ListDataModel(List)
      */
     public ListDataModel(List<E> list) {
 
         super();
         setWrappedData(list);
 
     }
 
 
     // ------------------------------------------------------ Instance Variables
 
 
     // The current row index (zero relative)
     private int index = -1;
 
 
     // The list we are wrapping
     private List list;
 
 
     // -------------------------------------------------------------- Properties
 
 
     /**
      * @see javax.faces.model.ListDataModel#isRowAvailable()
      */
     public boolean isRowAvailable() {
 
         if (list == null) {
             return (false);
         } else if ((index >= 0) && (index < list.size())) {
             return (true);
         } else {
             return (false);
         }
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#getRowCount()
      */
     public int getRowCount() {
 
         if (list == null) {
 	    return (-1);
         }
         return (list.size());
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#getRowData()
      */
     public E getRowData() {
 
         if (list == null) {
 	    return (null);
         } else if (!isRowAvailable()) {
             throw new IllegalArgumentException("No row available");
         } else {
             return ((E) list.get(index));
         }
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#getRowIndex()
      */
     public int getRowIndex() {
 
         return (index);
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#setRowIndex(int)
      */
     public void setRowIndex(int rowIndex) {
 
         if (rowIndex < -1) {
             throw new IllegalArgumentException();
         }
         int old = index;
         index = rowIndex;
     	if (list == null) {
     	    return;
     	}
     	DataModelListener [] listeners = getDataModelListeners();
        if ((old != index) && (listeners != null) && (listeners.length > 0)) {
             Object rowData = null;
             if (isRowAvailable()) {
                 rowData = getRowData();
             }
             DataModelEvent event =
                 new DataModelEvent(this, index, rowData);
             int n = listeners.length;
             for (int i = 0; i < n; i++) {
         		if (null != listeners[i]) {
         		    listeners[i].rowSelected(event);
         		}
             }
         }
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#getWrappedData()
      */
     public Object getWrappedData() {
 
         return (this.list);
 
     }
 
 
     /**
      * @see javax.faces.model.ListDataModel#setWrappedData(Object)
      */
     public void setWrappedData(Object data) {
 
         if (data == null) {
             list = null;
             setRowIndex(-1);
         } else {
             list = (List) data;
             index = -1;
             setRowIndex(0);
         }
 
     }
 
 
 
 }
