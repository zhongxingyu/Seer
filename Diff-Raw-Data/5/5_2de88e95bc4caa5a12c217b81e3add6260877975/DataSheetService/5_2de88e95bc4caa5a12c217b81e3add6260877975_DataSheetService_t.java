 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package gc.carbon.data;
 
 import com.jellymold.sheet.Sheet;
 import com.jellymold.utils.cache.CacheHelper;
 import com.jellymold.utils.ThreadBeanHolder;
 import gc.carbon.domain.data.DataCategory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import java.io.Serializable;
 
 @Service
 public class DataSheetService implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private DataSheetFactory dataSheetFactory;
 
     private CacheHelper cacheHelper = CacheHelper.getInstance();
 
     public DataSheetService() {
         super();
     }
 
     public Sheet getSheet(DataBrowser browser) {
         ThreadBeanHolder.set("dataCategoryForFactory", browser.getDataCategory());
        return (Sheet) cacheHelper.getCacheable(dataSheetFactory);
     }
 
     public Sheet getSheet(DataCategory dataCategory) {
         ThreadBeanHolder.set("dataCategoryForFactory", dataCategory);
         return (Sheet) cacheHelper.getCacheable(dataSheetFactory);
     }
 
     public void removeSheet(DataCategory dataCategory) {
         cacheHelper.remove("DataSheets", "DataSheet_" + dataCategory.getUid());
     }
 
     public DataSheetFactory getDataSheetFactory() {
         return dataSheetFactory;
     }
 }
