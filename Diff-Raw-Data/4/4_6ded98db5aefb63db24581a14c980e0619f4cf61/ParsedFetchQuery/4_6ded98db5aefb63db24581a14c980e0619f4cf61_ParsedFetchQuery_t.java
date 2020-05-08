 /**
  *
  * Copyright 2010 Vitalii Tymchyshyn
  * This file is part of EsORM.
  *
  * EsORM is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * EsORM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with EsORM.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.esorm.impl.db;
 
 import org.esorm.EntityConfiguration;
 import org.esorm.ParsedQuery;
 import org.esorm.PreparedQuery;
 import org.esorm.entity.db.ValueExpression;
import org.esorm.impl.QueryCache;
 import org.esorm.parameters.ParameterMapper;
 
 import java.sql.Connection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Vitalii Tymchyshyn
  */
 public class ParsedFetchQuery implements ParsedQuery {
     private final EntityConfiguration configuration;
     private final String query;
     private final Map<ValueExpression, Integer> resultColumns;
     private final List<String> parameterIndexes;
     private final ParameterMapper parameterMapper;
 
     public ParsedFetchQuery(EntityConfiguration configuration, String query, ParameterMapper parameterMapper, Map<ValueExpression, Integer> resultColumns) {
         this(configuration, query, parameterMapper, resultColumns, null);
     }
 
     public ParsedFetchQuery(EntityConfiguration configuration, String query, ParameterMapper parameterMapper, Map<ValueExpression, Integer> resultColumns, List<String> parameterIndexes) {
         this.configuration = configuration;
         this.query = query;
         this.resultColumns = resultColumns;
         this.parameterIndexes = parameterIndexes;
         this.parameterMapper = parameterMapper;
     }
 
     public Type getType() {
         return Type.Fetch;
     }
 
     public EntityConfiguration getResultConfiguration() {
         return configuration;
     }
 
     public <R> PreparedQuery<R> prepare(Connection con) {
        return new PreparedFetchQuery<R>(con, new QueryCache(), configuration, query, parameterMapper, resultColumns, parameterIndexes);
     }
 
     public <R> PreparedQuery<R> prepare(Connection con, Object... params) {
         return this.<R>prepare(con).reset(params);
     }
 
     public <R> PreparedQuery<R> prepare(Connection con, Map<String, Object> params) {
         return this.<R>prepare(con).reset(params);
     }
 }
