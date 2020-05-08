 /*
  * Copyright 2013 Food and Agriculture Organization of the United Nations (FAO).
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sola.clients.beans.cadastre;
 
 import java.util.List;
 import org.sola.clients.beans.AbstractBindingBean;
 import org.sola.clients.beans.controls.SolaObservableList;
 import org.sola.clients.beans.converters.TypeConverters;
 import org.sola.services.boundary.wsclients.WSManager;
 import org.sola.webservices.transferobjects.search.CadastreObjectSearchParamsTO;
 import org.sola.webservices.transferobjects.search.CadastreObjectSearchResultTO;
 
 /**
  * List of cadastre objects
  */
 public class CadastreObjectSearchResultListBean extends AbstractBindingBean {
 
     public static final String SELECTED_CADASTRE_OBJECT = "selectedCadastreObject";
     SolaObservableList<CadastreObjectSearchResultBean> cadastreObjects;
     CadastreObjectSearchResultBean selectedCadastreObject;
 
     public CadastreObjectSearchResultListBean() {
         super();
         cadastreObjects = new SolaObservableList<CadastreObjectSearchResultBean>();
     }
 
     public SolaObservableList<CadastreObjectSearchResultBean> getCadastreObjects() {
         return cadastreObjects;
     }
 
     public CadastreObjectSearchResultBean getSelectedCadastreObject() {
         return selectedCadastreObject;
     }
 
     public void setSelectedCadastreObject(CadastreObjectSearchResultBean selectedCadastreObject) {
         this.selectedCadastreObject = selectedCadastreObject;
         propertySupport.firePropertyChange(SELECTED_CADASTRE_OBJECT, null, this.selectedCadastreObject);
     }
 
     /**
      * Searches cadastre objects with the given parameters.
      */
     public void search(CadastreObjectSearchParamsBean params) {
         if (params == null) {
             return;
         }
         List< CadastreObjectSearchResultTO> result = WSManager.getInstance().getSearchService()
                 .searchCadastreObjects(TypeConverters.BeanToTrasferObject(params,
                 CadastreObjectSearchParamsTO.class));
      
         TypeConverters.TransferObjectListToBeanList(result, CadastreObjectSearchResultBean.class,
                 (List) getCadastreObjects());
     }
 }
