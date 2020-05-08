 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.code.nanorm;
 
 import java.lang.reflect.Type;
 
 import com.google.code.nanorm.internal.type.TypeHandler;
 
 /**
  * Factory for creating type handlers. Type handlers are responsible for mapping
  * data from {@link java.sql.ResultSet} to Java objects and from Java objects to
 * {@PreparedStatement} parameters.
  * 
  * TODO: Support for JDBC types!
  * 
  * @see TypeHandler
  * @author Ivan Dubrov
  * @version 1.0 31.05.2008
  */
 public interface TypeHandlerFactory {
 
 	/**
 	 * Get type handler for given type.
 	 * 
 	 * @param type type
 	 * @return type handler.
 	 */
 	TypeHandler<?> getTypeHandler(Type type);
 
 	/**
 	 * Get type handler for unknown types.
 	 * 
 	 * @return type handler for unknown types
 	 */
 	TypeHandler<Object> getUnknownTypeHandler();
 
 	/**
 	 * Register new type handler.
 	 * 
 	 * @param type type
 	 * @param handler type handler
 	 */
 	void register(Type type, TypeHandler<?> handler);
 }
