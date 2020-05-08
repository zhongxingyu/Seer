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
 
 package com.google.code.nanorm.config;
 
 import java.lang.reflect.Type;
 
import javax.sql.DataSource;

 import com.google.code.nanorm.NanormFactory;
import com.google.code.nanorm.SessionManagement;
 import com.google.code.nanorm.TypeHandlerFactory;
 import com.google.code.nanorm.internal.FactoryImpl;
 import com.google.code.nanorm.internal.config.InternalConfiguration;
 import com.google.code.nanorm.internal.introspect.IntrospectionFactory;
 import com.google.code.nanorm.internal.introspect.asm.ASMIntrospectionFactory;
 import com.google.code.nanorm.internal.type.TypeHandler;
 import com.google.code.nanorm.internal.type.TypeHandlerFactoryImpl;
 
 /**
  * Public configuration class for nanorm factory creation.
  * 
  * @author Ivan Dubrov
  * @version 1.0 19.06.2008
  */
 public class NanormConfiguration {
 
 	private final TypeHandlerFactory typeHandlerFactory;
 
 	private final IntrospectionFactory introspectionFactory;
 
 	private final InternalConfiguration config;
 	
 	private SessionConfig sessionConfig;
 
 	/**
 	 * Constructor.
 	 */
 	public NanormConfiguration() {
 		typeHandlerFactory = new TypeHandlerFactoryImpl();
 		// introspectionFactory = new ReflectIntrospectionFactory();
 
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		introspectionFactory = new ASMIntrospectionFactory(cl);
 
 		config = new InternalConfiguration(typeHandlerFactory,
 				introspectionFactory);
 	}
 
 	/**
 	 * Register type handler.
 	 * @param type type for handler
 	 * @param handler type handler
 	 */
 	public void registerTypeHandler(Type type, TypeHandler<?> handler) {
 		typeHandlerFactory.register(type, handler);
 	}
 
 	/**
 	 * Set session manager.
 	 */
 	public void setSessionConfig(SessionConfig sessionConfig) {
 		this.sessionConfig = sessionConfig;
 	}
 
 	
 	/**
 	 * Build factory.
 	 * @return factory.
 	 */
 	public NanormFactory buildFactory() {
 		return new FactoryImpl(config, sessionConfig);
 	}
 }
