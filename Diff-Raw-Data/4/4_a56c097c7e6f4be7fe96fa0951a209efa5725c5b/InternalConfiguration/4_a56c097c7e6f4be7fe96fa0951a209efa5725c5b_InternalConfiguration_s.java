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
 package com.google.code.nanorm.internal.config;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.nanorm.DataSink;
 import com.google.code.nanorm.SQLSource;
 import com.google.code.nanorm.TypeHandlerFactory;
 import com.google.code.nanorm.annotations.Call;
 import com.google.code.nanorm.annotations.Insert;
 import com.google.code.nanorm.annotations.Property;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.ResultMapList;
 import com.google.code.nanorm.annotations.ResultMapRef;
 import com.google.code.nanorm.annotations.Scalar;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.annotations.SelectKey;
 import com.google.code.nanorm.annotations.Source;
 import com.google.code.nanorm.annotations.Update;
 import com.google.code.nanorm.exceptions.ConfigurationException;
 import com.google.code.nanorm.internal.DynamicFragment;
 import com.google.code.nanorm.internal.Fragment;
 import com.google.code.nanorm.internal.TextFragment;
 import com.google.code.nanorm.internal.introspect.IntrospectionFactory;
 import com.google.code.nanorm.internal.introspect.Setter;
 import com.google.code.nanorm.internal.introspect.TypeOracle;
 import com.google.code.nanorm.internal.mapping.result.DefaultRowMapper;
 import com.google.code.nanorm.internal.mapping.result.RowMapper;
 import com.google.code.nanorm.internal.mapping.result.ScalarRowMapper;
 import com.google.code.nanorm.internal.util.Messages;
 import com.google.code.nanorm.internal.util.ToStringBuilder;
 
 /**
  * TODO: Thread safety?
  * 
  * TODO: Validate: no maps with same id
  * 
  * TODO: Validate: one data sink and query method is void
  * 
  * @author Ivan Dubrov
  * @version 1.0 29.05.2008
  */
 @SuppressWarnings("deprecation")
 public class InternalConfiguration {
 
 	private final static Logger LOGGER = LoggerFactory.getLogger(InternalConfiguration.class);
 
 	private final Set<Class<?>> mapped;
 
 	/**
 	 * Result map id is [package].[class]#id
 	 */
 	private final Map<String, ResultMapConfig> resultMapsConfig;
 
 	/**
 	 * Statement id is [package].[class]#method
 	 */
 	private final Map<StatementKey, StatementConfig> statementsConfig;
 
 	private final List<Runnable> postConfigureList;
 
 	private final TypeHandlerFactory typeHandlerFactory;
 
 	private final IntrospectionFactory introspectionFactory;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param typeHandlerFactory type handler factory
 	 * @param introspectionFactory introspection factory
 	 */
 	public InternalConfiguration(TypeHandlerFactory typeHandlerFactory,
 			IntrospectionFactory introspectionFactory) {
 		resultMapsConfig = new HashMap<String, ResultMapConfig>();
 		statementsConfig = new HashMap<StatementKey, StatementConfig>();
 		mapped = new HashSet<Class<?>>();
 		postConfigureList = new ArrayList<Runnable>();
 
 		// TODO: Should be configurable
 		this.typeHandlerFactory = typeHandlerFactory;
 		this.introspectionFactory = introspectionFactory;
 	}
 
 	/**
 	 * Get statement configuration for method.
 	 * 
 	 * @param method method
 	 * @return statement configuration for method
 	 */
 	private StatementConfig getStatementConfig(StatementKey key) {
 
 		StatementConfig statementConfig = statementsConfig.get(key);
 
 		if (statementConfig == null) {
 			throw new ConfigurationException(Messages.missingConfiguration(key.getMapper(), key
 					.getName()));
 		}
 		return statementConfig;
 	}
 
 	/**
 	 * Get statement configuration for method.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method to get configuration for
 	 * @return statement configuration for method
 	 */
 	public StatementConfig getStatementConfig(Class<?> mapper, Method method) {
 		Type[] resolved = TypeOracle.resolveMethodArguments(method, mapper);
 		StatementKey key = new StatementKey(mapper, method.getName(), resolved);
		return getStatementConfig(key);
 	}
 
 	/**
 	 * Configure mapper.
 	 * 
 	 * TODO: Elaborate Javadoc!
 	 * 
 	 * @param mapper mapper interface
 	 * @throws ConfigurationException configuration is invalid
 	 */
 	public void configure(Class<?> mapper) throws ConfigurationException {
 		synchronized (this) {
 			if (mapped.add(mapper)) {
 				// Configure super mappers first
 				for (Class<?> superMapper : mapper.getInterfaces()) {
 					configure(superMapper);
 				}
 
 				LOGGER.info("Configuring mapper interface " + mapper.getName());
 
 				// Configure mapper itself
 				processResultMaps(mapper);
 				for (Method method : mapper.getMethods()) {
 					processMethod(mapper, method);
 				}
 
 				postProcess();
 			}
 		}
 	}
 
 	/**
 	 * Post process the configuration, resolving the references of subselects.
 	 */
 	private void postProcess() throws ConfigurationException {
 		for (Runnable runnable : postConfigureList) {
 			runnable.run();
 		}
 		postConfigureList.clear();
 	}
 
 	/**
 	 * Process given method and generate statement configuration from it.
 	 * 
 	 * <p>
 	 * FIXME: Is that ok to just strip generics info (by calling
 	 * <code>method.getParameterType()</code>)? We probably should propagate
 	 * type info from the top mapper class when going down to the interfaces in
 	 * configure method. So, currently, the Nanorm annotations could be applied
 	 * only to methods without unknown generic types. That means you cannot do
 	 * something like this:
 	 * 
 	 * <pre>
 	 * interface Super&lt;T&gt; {
 	 * 	&#064;Select(&quot;SELECT FROM SOME WHERE ID = id&quot;)
 	 * 	Some selectById(T id);
 	 * }
 	 * 
 	 * interface Concrete extends Super&lt;Integer&gt; {
 	 * 	Some selectById(Integer id);
 	 * }
 	 * </pre>
 	 * 
 	 * </p>
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method to gather configuration from
 	 */
 	private void processMethod(Class<?> mapper, Method method) throws ConfigurationException {
 		assert (method != null);
 		assert (mapper != null);
 
 		StatementKey key = new StatementKey(mapper, method.getName(), method.getParameterTypes());
 
 		// Each method should be configured only once
 		assert (!statementsConfig.containsKey(key));
 
 		final StatementConfig stConfig = new StatementConfig(key);
 		final ResultMapConfig mapConfig = createResultMapConfig(method);
 
 		Select select = method.getAnnotation(Select.class);
 		Update update = method.getAnnotation(Update.class);
 		Insert insert = method.getAnnotation(Insert.class);
 		Call call = method.getAnnotation(Call.class);
 		Source source = method.getAnnotation(Source.class);
 
 		Validation.validateExclusive(mapper, method, select, update, insert, call, source);
 
 		String sql = null;
 		Class<? extends SQLSource> sqlSource = null;
 
 		QueryKind kind = null;
 
 		if (select != null) {
 			sql = select.value();
 			sqlSource = select.sqlSource();
 			kind = QueryKind.SELECT;
 		} else if (call != null) {
 			sql = call.value();
 			sqlSource = call.sqlSource();
 			kind = QueryKind.CALL;
 		} else if (insert != null) {
 			sql = insert.value();
 			sqlSource = insert.sqlSource();
 			kind = QueryKind.INSERT;
 		} else if (update != null) {
 			sql = update.value();
 			sqlSource = update.sqlSource();
 			kind = QueryKind.UPDATE;
 		} else if (source != null) {
 			// Deprecated case.
 			kind = QueryKind.SELECT;
 			sqlSource = source.value();
 		} else {
 			// Skip method
 			LOGGER.info("Skipping method '{}' in mapper '{}' (no configuration).",
 					method.getName(), mapper.getName());
 			return;
 		}
 		stConfig.setKind(kind);
 
 		// TODO: Check we have exactly one of these!
 		if (sqlSource != SQLSource.class) {
 			Fragment builder = new DynamicFragment(sqlSource, introspectionFactory);
 			stConfig.setStatementBuilder(builder);
 		} else if (sql != null) {
 			// TODO: Batch case!
 			Fragment builder = new TextFragment(sql, method.getGenericParameterTypes(),
 					introspectionFactory);
 			stConfig.setStatementBuilder(builder);
 		}
 		// TODO: Check sql is not empty!
 
 		// Configure select key statement
 		SelectKey selectKey = method.getAnnotation(SelectKey.class);
 		if (selectKey != null) {
 			Validation.validateSelectKey(selectKey, mapper, method);
 
 			StatementKey selectKeyKey = new StatementKey(mapper, method.getName() + ":key", method
 					.getGenericParameterTypes());
 
 			StatementConfig selectKeySt = new StatementConfig(selectKeyKey);
 			if (selectKey.value().length() > 0) {
 				selectKeySt.setStatementBuilder(new TextFragment(selectKey.value(), method
 						.getGenericParameterTypes(), introspectionFactory));
 			}
 			selectKeySt.setParameterTypes(method.getGenericParameterTypes());
 
 			// Key type is method return value
 			Type keyType = method.getGenericReturnType();
 
 			// If return type is void, the key type is deduced from the property
 			// type
 			// Validation already checked that in this case property is not
 			// empty
 			if (keyType == void.class) {
 				String prop = selectKey.property();
 				keyType = introspectionFactory.getParameterType(method.getGenericParameterTypes(),
 						prop);
 			}
 			selectKeySt.setResultType(keyType);
 			selectKeySt.setRowMapper(new ScalarRowMapper(keyType, typeHandlerFactory));
 
 			if (selectKey.property().length() > 0) {
 				Setter keySetter = introspectionFactory.buildParameterSetter(method
 						.getGenericParameterTypes(), selectKey.property());
 				stConfig.setKeySetter(keySetter);
 			}
 
 			stConfig.setSelectKeyType(selectKey.type());
 			stConfig.setSelectKey(selectKeySt);
 		}
 
 		// For update we always use method return value, but for select we try
 		// to find DataSink if return type is void
 		// TODO: Should this work for generated keys as well?
 		Type returnType = null;
 		if (kind == QueryKind.SELECT && method.getReturnType() == void.class) {
 			int pos = searchResultCallback(method);
 			if (pos == -1) {
 				throw new ConfigurationException(Messages.invalidReturnType(method));
 			}
 
 			stConfig.setCallbackIndex(pos);
 
 			// First parameter of DataSink interface
 			ParameterizedType pt = (ParameterizedType) method.getGenericParameterTypes()[pos];
 			returnType = pt.getActualTypeArguments()[0];
 
 		} else {
 			returnType = method.getGenericReturnType();
 		}
 		stConfig.setResultType(returnType);
 
 		// Create row mapper if return type is not void
 		if (returnType != void.class) {
 			// Create row mapper during post-configuration step, after the
 			// subselects are configured
 			postConfigureList.add(new Runnable() {
 				public void run() {
 					// At this time, all subselect properties for this result
 					// map should be post-configured already
 					stConfig.setRowMapper(createRowMapper(stConfig.getResultType(), mapConfig));
 				}
 			});
 		}
 
 		stConfig.setParameterTypes(method.getGenericParameterTypes());
 
 		// Put it two times: one time with parameter types, for regular
 		// processing
 		statementsConfig.put(key, stConfig);
 
 		// Second time without parameter types (for subselect references,
 		// where parameter types are not known and therefore the matching
 		// query is searched by mapper class and query name only)
 		StatementKey key2 = new StatementKey(key.getMapper(), key.getName(), null);
 		statementsConfig.put(key2, stConfig);
 	}
 
 	// TODO: Move to separate helper class
 	private int searchResultCallback(Method method) {
 		// Try to find DataSink
 		Type[] types = method.getGenericParameterTypes();
 		for (int i = 0; i < types.length; ++i) {
 			if (types[i] instanceof ParameterizedType) {
 				ParameterizedType pt = (ParameterizedType) types[i];
 				if (pt.getRawType() == DataSink.class) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * 
 	 * @param type result type to map result set row to
 	 * @param config result map configuration
 	 * @return
 	 */
 	private RowMapper createRowMapper(Type type, ResultMapConfig config) {
 		assert (config != null);
 
 		// For explicitly marked by @Scalar, we use scalar row mapper
 		if (config.isScalar()) {
 			if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
 				return new ScalarRowMapper(((Class<?>) type).getComponentType(), typeHandlerFactory);
 			}
 			return new ScalarRowMapper(type, typeHandlerFactory);
 		}
 
 		// For primitive types and String we never use usual row mapper, only
 		// scalar mapping
 		if (type instanceof Class<?>) {
 			Class<?> clazz = (Class<?>) type;
 			if (clazz.isPrimitive() || type == String.class) {
 				return new ScalarRowMapper(type, typeHandlerFactory);
 			}
 			if (clazz.isArray()
 					&& (clazz.getComponentType().isPrimitive() || clazz.getComponentType() == String.class)) {
 				return new ScalarRowMapper(clazz.getComponentType(), typeHandlerFactory);
 			}
 		}
 
 		return new DefaultRowMapper(type, config, introspectionFactory, typeHandlerFactory);
 	}
 
 	/**
 	 * Process result maps defined for given class.
 	 * 
 	 * @param clazz class to gather result maps from
 	 */
 	private void processResultMaps(Class<?> clazz) throws ConfigurationException {
 		ResultMap resultMap = clazz.getAnnotation(ResultMap.class);
 
 		// Search for @ResultMap
 		if (resultMap != null) {
 			ResultMapConfig cfg = createResultMapConfig(clazz, null, resultMap);
 
 			resultMapsConfig.put(cfg.getId(), cfg);
 		}
 
 		// Search for @ResultMapList
 		ResultMapList list = clazz.getAnnotation(ResultMapList.class);
 		if (list != null) {
 			for (ResultMap resultMap2 : list.value()) {
 				ResultMapConfig cfg = createResultMapConfig(clazz, null, resultMap2);
 
 				resultMapsConfig.put(cfg.getId(), cfg);
 			}
 		}
 	}
 
 	/**
 	 * Get result map config for given method.
 	 * 
 	 * @param method method
 	 * @return
 	 */
 	private ResultMapConfig createResultMapConfig(Method method) throws ConfigurationException {
 		Class<?> mapper = method.getDeclaringClass();
 
 		ResultMap resultMap = method.getAnnotation(ResultMap.class);
 		ResultMapRef ref = method.getAnnotation(ResultMapRef.class);
 		Scalar scalar = method.getAnnotation(Scalar.class);
 
 		Validation.validateExclusive(mapper, method, resultMap, ref, scalar);
 
 		if (resultMap != null) {
 			// Use result map
 			return createResultMapConfig(mapper, method, resultMap);
 		} else if (scalar != null) {
 			// Use scalar mapping
 			ResultMapConfig config = new ResultMapConfig(mapper.getName() + "#(scalar)");
 			config.setScalar(true);
 			return config;
 		} else if (ref != null) {
 			// Resolve reference
 			Class<?> refMapper = ref.declaringClass();
 			if (ref.declaringClass() == Object.class) {
 				refMapper = mapper;
 			}
 			ResultMapConfig resultMapConfig = findResultMap(refMapper, ref.value());
 			if (resultMapConfig == null) {
 				throw new ConfigurationException(Messages.resultMapNotFound(mapper, method, ref));
 			}
 			return resultMapConfig;
 		} else {
 			// Search for default map or automap
 
 			// Search for default map for the mapper (with empty id)
 			// FIXME: Remove this feature?
 			ResultMapConfig resultMapConfig = findResultMap(mapper, "");
 			if (resultMapConfig == null) {
 				// Return automap if no map is defined
 				resultMapConfig = createAutoResultMapConfig(mapper);
 			}
 			return resultMapConfig;
 		}
 	}
 
 	/**
 	 * Create {@link ResultMapConfig} instance from {@link RowMapper}
 	 * annotation.
 	 * 
 	 * @param clazz
 	 * @param resultMap
 	 * @return
 	 */
 	private ResultMapConfig createResultMapConfig(Class<?> mapper, Method method,
 			ResultMap resultMap) throws ConfigurationException {
 		assert (mapper != null);
 		assert (resultMap != null);
 
 		List<PropertyMappingConfig> mappings = new ArrayList<PropertyMappingConfig>();
 
 		// Set of all properties, for groupBy list validation
 		Set<String> propnames = null;
 		if (resultMap.groupBy().length > 0) {
 			propnames = new HashSet<String>();
 		}
 		for (Property mapping : resultMap.mappings()) {
 			PropertyMappingConfig propMapping = createPropertyMappingConfig(mapper, method,
 					resultMap, mapping);
 			mappings.add(propMapping);
 
 			// Collect property names for groupBy validation
 			if (propnames != null) {
 				propnames.add(propMapping.getProperty());
 			}
 		}
 		Validation.validateGroupBy(mapper, method, resultMap, propnames);
 
 		ResultMapConfig config = new ResultMapConfig(mapper.getName() + "#" + resultMap.id());
 		config.setMappings(mappings.toArray(new PropertyMappingConfig[mappings.size()]));
 		config.setAuto(resultMap.auto());
 		config.setGroupBy(resultMap.groupBy());
 		return config;
 	}
 
 	/**
 	 * Create automatic result mapping config.
 	 * 
 	 * @param mapper mapper
 	 * @return result map config
 	 * @throws ConfigurationException configuration is invalid
 	 */
 	private ResultMapConfig createAutoResultMapConfig(Class<?> mapper)
 			throws ConfigurationException {
 		assert (mapper != null);
 
 		ResultMapConfig config = new ResultMapConfig(mapper.getName() + "#(auto)");
 		config.setMappings(new PropertyMappingConfig[0]);
 		config.setAuto(true);
 		return config;
 	}
 
 	/**
 	 * Create configuration bean for property mapping.
 	 * 
 	 * @param mapper mapper interface
 	 * @param method method being mapped
 	 * @param resultMap result map
 	 * @param mapping mapping configuration annotation
 	 * @return mapping configuration
 	 * @throws ConfigurationException configuration is invalid
 	 */
 	private PropertyMappingConfig createPropertyMappingConfig(Class<?> mapper, Method method,
 			ResultMap resultMap, Property mapping) throws ConfigurationException {
 		assert (mapper != null);
 		assert (resultMap != null);
 		assert (mapping != null);
 
 		PropertyMappingConfig propMapping = new PropertyMappingConfig();
 
 		Validation.validatePropertyMapping(mapping, mapper, resultMap);
 
 		propMapping.setProperty(mapping.value());
 		propMapping.setColumn(mapping.column());
 		propMapping.setColumnIndex(mapping.columnIndex());
 		if (propMapping.getColumnIndex() == 0
 				&& (propMapping.getColumn() == null || "".equals(propMapping.getColumn()))) {
 			propMapping.setColumn(propMapping.getProperty());
 		}
 
 		// We have a nested mapping
 		if (!"".equals(mapping.nestedMap().value())) {
 			Class<?> clazz = mapping.nestedMap().declaringClass();
 			if (clazz == Object.class) {
 				clazz = mapper;
 			}
 			ResultMapConfig nestedMapConfig = findResultMap(clazz, mapping.nestedMap().value());
 
 			if (nestedMapConfig == null) {
 				throw new ConfigurationException(Messages.nestedMapNotFound(mapper, method,
 						resultMap, mapping));
 			}
 
 			propMapping.setNestedMapConfig(nestedMapConfig);
 		}
 
 		// We have subselect mapping
 		if (!"".equals(mapping.subselect())) {
 			// TODO: Map it?
 			Class<?> subselectMapper = mapping.subselectMapper() != Object.class ? mapping
 					.subselectMapper() : mapper;
 
 			// null parameters mean that parameters are not known.
 			// in that case, any matching method will be used
 			// FIXME: Derive parameters from the property type! But we need bean
 			// class for this...
 			StatementKey subselectKey = new StatementKey(subselectMapper, mapping.subselect(), null);
 
 			// Subselect properties requires post-configuration (the referenced
 			// query could be not yet configured, so wait until all queries are
 			// processed)
 			final SubselectConfig subselectInfo = new SubselectConfig(subselectKey, propMapping,
 					mapper, resultMap);
 
 			postConfigureList.add(new Runnable() {
 				public void run() {
 					subselectPostConfigure(subselectInfo);
 				}
 			});
 		}
 		return propMapping;
 	}
 
 	/**
 	 * Post configuration step for the subselects. Searches for the referenced
 	 * statement.
 	 * 
 	 * @param subselectInfo
 	 */
 	private void subselectPostConfigure(SubselectConfig subselectInfo) {
 		assert (subselectInfo != null);
 
 		StatementKey key = subselectInfo.getSubselectKey();
 		StatementConfig stConfig = statementsConfig.get(key);
 
 		if (stConfig == null) {
 			throw new ConfigurationException(Messages.subselectNotFound(subselectInfo));
 		}
 
 		if (stConfig.getParameterTypes().length != 1) {
 			throw new ConfigurationException(Messages.subselectParameterCount(subselectInfo));
 		}
 		subselectInfo.getPropertyMapping().setSubselect(stConfig);
 	}
 
 	/**
 	 * Find result map config with given reference id.
 	 * 
 	 * FIXME: Made public for custom project, to get information about the
 	 * mapping between property&lt;-&gt;column.
 	 * 
 	 * @param clazz declaring class
 	 * @param refId reference id
 	 * @return result map config
 	 * @throws ConfigurationException configuration is invalid
 	 */
 	public ResultMapConfig findResultMap(Class<?> clazz, String refId)
 			throws ConfigurationException {
 		String key = clazz.getName() + "#" + refId;
 
 		if (!mapped.contains(clazz)) {
 			throw new ConfigurationException(Messages.notMapped(clazz, refId));
 		}
 
 		ResultMapConfig resultMapConfig = resultMapsConfig.get(key);
 
 		// Search in superinterfaces
 		if (resultMapConfig == null) {
 			for (Class<?> superMapper : clazz.getInterfaces()) {
 				resultMapConfig = findResultMap(superMapper, refId);
 				if (resultMapConfig != null) {
 					break;
 				}
 			}
 		}
 		return resultMapConfig;
 	}
 
 	/** @return Returns the typeHandlerFactory. */
 	public TypeHandlerFactory getTypeHandlerFactory() {
 		return typeHandlerFactory;
 	}
 
 	/** @return Returns the introspectionFactory. */
 	public IntrospectionFactory getIntrospectionFactory() {
 		return introspectionFactory;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("resultMapsConfig", resultMapsConfig).append(
 				"statementsConfig", statementsConfig).append("typeHandlerFactory",
 				typeHandlerFactory).append("introspectionFactory", introspectionFactory).toString();
 	}
 }
