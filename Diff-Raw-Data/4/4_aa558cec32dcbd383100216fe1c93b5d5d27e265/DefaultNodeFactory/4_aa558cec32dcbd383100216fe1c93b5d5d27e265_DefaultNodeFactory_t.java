 /***********************************************************************************************************************
  *
  * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 package eu.stratosphere.sopremo.type;
 
 import java.util.IdentityHashMap;
 import java.util.Map;
 
 import eu.stratosphere.util.reflect.ReflectUtil;
 
 public class DefaultNodeFactory implements NodeFactory {
 	private final static DefaultNodeFactory Instance = new DefaultNodeFactory().
 		register(IArrayNode.class, ArrayNode.class).
 		register(IObjectNode.class, ObjectNode.class);
 
 	private final Map<Class<? extends IJsonNode>, Class<? extends IJsonNode>> interfaceImplementations =
 		new IdentityHashMap<Class<? extends IJsonNode>, Class<? extends IJsonNode>>();
 
 	/**
 	 * Returns the instance.
 	 * 
 	 * @return the instance
 	 */
 	public static DefaultNodeFactory getInstance() {
 		return Instance;
 	}
 
 	public <T extends IJsonNode> DefaultNodeFactory register(final Class<T> interfaceType,
 			final Class<? extends T> implementationType) {
 		this.interfaceImplementations.put(interfaceType, implementationType);
 		return this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.cache.NodeFactory#instantiate(java.lang.Class)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends IJsonNode> T instantiate(final Class<T> interfaceType) {
 		final Class<? extends IJsonNode> defaultImplementation = this.interfaceImplementations.get(interfaceType);
 		if (defaultImplementation != null)
 			return (T) ReflectUtil.newInstance(defaultImplementation);
		if (interfaceType == NullNode.class)
			return (T) NullNode.getInstance();
		if (interfaceType == MissingNode.class)
			return (T) MissingNode.getInstance();
 		return ReflectUtil.newInstance(interfaceType);
 	}
 }
