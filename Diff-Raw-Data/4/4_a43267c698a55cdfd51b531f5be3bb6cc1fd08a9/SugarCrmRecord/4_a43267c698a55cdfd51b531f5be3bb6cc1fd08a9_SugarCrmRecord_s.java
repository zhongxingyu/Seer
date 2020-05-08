 
  /**
  * Licensed under the EUPL, Version 1.1 or - as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 
 package eu.europeana.uim.sugarcrm;
 
 import eu.europeana.uim.sugarcrm.model.SugarCrmField;
 import eu.europeana.uim.sugarcrm.model.UpdatableField;
 
 
 
 /**
  * Shared interface for the SugarCrmRecord
  * 
  * @author Rene Wiermer (rene.wiermer@kb.nl)
  * @date Aug 12, 2011
  */
 public interface SugarCrmRecord {
 
     /**
      * Updates 
      * 
      * @param field
      */
    public abstract void setItemValue(UpdatableField field);
 
     /**
      * @param field
      * @return the content of the field
      */
     public abstract String getItemValue(SugarCrmField field);
 
 }
