 //    Data SQL is a light JDBC wrapper.
 //    Copyright (C) 2014 Adrián Romero Corchado.
 //
 //    This file is part of Data SQL
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.datasql.orm;
 
 import com.adr.datasql.ProcList;
 import com.adr.datasql.Query;
 import com.adr.datasql.Session;
 import com.adr.datasql.data.ParametersMap;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author adrian
  * @param <R>
  */
 public class ListData<R> implements ProcList<R, Map<String, Object>> {
         
     private final Data<R> data;
     
     public ListData(Data<R> data) {        
         this.data = data;
     }
 
     @Override
     public List<R> list(Session s, Map<String, Object> params) throws SQLException {
            
        Field[] fieldsparams;
        if (params == null) {
            fieldsparams = new Field[0];
        } else {
            fieldsparams = data.getDefinition().getFields(params.keySet());
        }
         
         Query<R, Map<String, Object>> querylist = new Query<R, Map<String, Object>>(data.getDefinition().getStatementSelect(fieldsparams))
                 .setResults(data)
                 .setParameters(new ParametersMap(fieldsparams));
         
         return querylist.list(s, params);
     }
 }
