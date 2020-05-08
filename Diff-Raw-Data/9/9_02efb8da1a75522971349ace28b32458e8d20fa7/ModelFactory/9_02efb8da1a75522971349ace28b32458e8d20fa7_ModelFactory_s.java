 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud.domain.model;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 
 import de.unioninvestment.eai.portal.portlet.crud.config.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import de.unioninvestment.crud2go.spi.security.CryptorFactory;
 import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config;
 import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPool;
 import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPoolFactory;
 import de.unioninvestment.eai.portal.portlet.crud.domain.form.ResetFormAction;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer.FilterPolicy;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.authentication.Realm;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.CurrentUser;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.UserFactory;
 import de.unioninvestment.eai.portal.portlet.crud.domain.support.QueryOptionListRepository;
 import de.unioninvestment.eai.portal.support.vaadin.database.DatabaseDialect;
 import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
 import de.unioninvestment.eai.portal.support.vaadin.validation.FieldValidatorFactory;
 
 /**
  * Spring-Factory-Klasse für Model-Komponenten.
  * 
  * @author carsten.mjartan
  * 
  */
 @Component
 public class ModelFactory {
 
 	@Autowired
 	private ConnectionPoolFactory connectionPoolFactory;
 
 	@Autowired
 	private UserFactory userFactory;
 
 	@Autowired
 	private ResetFormAction resetFormAction;
 
 	@Autowired
 	private FieldValidatorFactory fieldValidatorFactory;
 
 	@Value("${portlet.crud.table.select.default-width}")
 	private int defaultSelectWidth;
 
 	@Value("${portlet.crud.table.directEdit}")
 	private boolean directEdit;
 
 	@Autowired
 	@Qualifier("prefetchExecutor")
 	private ExecutorService prefetchExecutor;
 
 	@Autowired
 	private CryptorFactory cryptorFactory;
 
 	@Autowired
 	private QueryOptionListRepository queryOptionListRepository;
 
 	@Value("${portlet.crud.optionListCache.cacheDefault}")
 	private boolean useOptionListCacheByDefault;
 
 	@Value("${portlet.crud.databaseBackend.dialect}")
 	private DatabaseDialect dialect;
 
 	/**
 	 * Konstruktor.
 	 */
 	ModelFactory() {
 		// for Spring
 	}
 
 	/**
 	 * Konstruktor mit Parameter.
 	 * 
 	 * @param connectionPoolFactory
 	 *            ConnectionPoolFactory
 	 * @param prefetchExecutor
 	 *            responsible for prefetching option lists
 	 * @param resetFormAction
 	 *            ResetFormAction
 	 * @param fieldValidatorFactory
 	 *            FieldValidatorFactory
 	 * @param defaultSelectWidth
 	 *            Breite der Selectboxen
 	 * @param userFactory
 	 */
 	public ModelFactory(ConnectionPoolFactory connectionPoolFactory,
 			UserFactory userFactory, ExecutorService prefetchExecutor,
 			ResetFormAction resetFormAction,
 			FieldValidatorFactory fieldValidatorFactory,
 			int defaultSelectWidth, boolean separateEditMode) {
 		this.connectionPoolFactory = connectionPoolFactory;
 		this.userFactory = userFactory;
 		this.prefetchExecutor = prefetchExecutor;
 		this.resetFormAction = resetFormAction;
 		this.fieldValidatorFactory = fieldValidatorFactory;
 		this.defaultSelectWidth = defaultSelectWidth;
 		this.directEdit = separateEditMode;
 	}
 
 	/**
 	 * Gibt einen Model-Builder zurück.
 	 * 
 	 * @param eventBus
 	 *            der Event-Bus
 	 * @param config
 	 *            Portletkonfiguration
 	 * @return Model-Builder
 	 */
 	public ModelBuilder getBuilder(EventBus eventBus, Config config, ModelPreferences prefs) {
 		return new ModelBuilder(eventBus, this, userFactory, resetFormAction,
 				fieldValidatorFactory, defaultSelectWidth, config, prefs, directEdit);
 	}
 
 	public Realm getAuthenticationRealm(AuthenticationRealmConfig config) {
 		return new Realm(config, cryptorFactory);
 	}
 
 	/**
 	 * Gibt den Datenbankcontainer zurück.
 	 * 
 	 * @param eventBus
 	 *            der Event-Bus
 	 * @param config
 	 *            die Tabellenkonfiguration
 	 * @param deleteable
 	 *            ob delete-Statements erlaubt sind
 	 * @param updateable
 	 *            ob update-Statements erlaubt sind
 	 * @param insertable
 	 *            ob insert-Statements erlaubt sind
 	 * @param currentUser
 	 *            Aktueller Benutzer
 	 * @param formatPattern
 	 *            Formatierungen nach Spaltennamen.
 	 * @param orderBys
 	 *            Default-Sortierung
 	 * @param filterPolicy
 	 *            Art des Filterhandlings
 	 * @param sizeValidTimeout
 	 *            Cachttimeout für die Anzahl aller selektierten Einträge
 	 * @param pagelength
 	 *            Anzahl der Einträge pro Seite
 	 * @param exportPagelength
 	 *            Anzahl der Einträge pro Seite beim Export
 	 * @return eine neue Instanz des {@link DatabaseTableContainer}
 	 */
 	public DatabaseTableContainer getDatabaseTableContainer(EventBus eventBus,
 			DatabaseTableConfig config, boolean insertable,
 			boolean updateable, boolean deleteable, CurrentUser currentUser,
 			Map<String, String> formatPattern, List<ContainerOrder> orderBys,
 			FilterPolicy filterPolicy, int pagelength, int exportPagelength,
 			int sizeValidTimeout) {
 		ConnectionPool pool = connectionPoolFactory.getPool(config.getDatasource());
 		return new DatabaseTableContainer(eventBus, config,
 				pool, insertable, updateable, deleteable, currentUser,
 				formatPattern, orderBys, filterPolicy, pagelength,
 				exportPagelength, sizeValidTimeout, dialect);
 	}
 
 	/**
 	 * 
 	 * @param eventBus
 	 *            der Event-Bus
 	 * @param config
      *            die Container-Konfiguration
      * @param query
      *            die SQL-Select Abfrage
      * @param insertable
 *            ob insert-Statements erlaubt sind
      * @param updateable
 *            ob update-Statements erlaubt sind
      * @param deleteable
 *            ob delete-Statements erlaubt sind
      * @param primaryKeys
 *            eine Liste mit den Primärschlüssel der Abfrage
      * @param currentUsername
 *            Aktueller Benutzer
      * @param displayPattern
 *            Formatierungen nach Spaltennamen.
      * @param orderBys
 *            Default-Sortierung
      * @param filterPolicy
 *            Art des Filterhandlings
      * @param pagelength
 *            Anzahl der Einträge pro Seite
      * @param exportPagelength
 *            Anzahl der Einträge pro Seite beim Export
      * @param sizeValidTimeout
 *            Cachttimeout für die Anzahl aller selektierten Einträge
      * @param orderByPrimaryKeys
 *            sortiere immer auch nach Primärschlüsseln              @return DatabaseQueryContainer
 	 */
 	public DatabaseQueryContainer getDatabaseQueryContainer(EventBus eventBus,
             DatabaseQueryConfig config, String query, boolean insertable,
             boolean updateable, boolean deleteable, List<String> primaryKeys,
             String currentUsername, Map<String, String> displayPattern,
             List<ContainerOrder> orderBys, FilterPolicy filterPolicy,
             int pagelength, int exportPagelength, Integer sizeValidTimeout,
             boolean orderByPrimaryKeys) {
 		ConnectionPool pool = connectionPoolFactory.getPool(config.getDatasource());
 		return new DatabaseQueryContainer(eventBus, config, query,
 				insertable, updateable, deleteable, primaryKeys, pool,
 				currentUsername, displayPattern, orderBys, filterPolicy,
 				pagelength, exportPagelength, sizeValidTimeout,
 				orderByPrimaryKeys);
 	}
 
 	/**
 	 * @param formatPattern
 	 *            Formatierungen nach Spaltennamen.
 	 * @param defaultOrder
 	 *            Default-Sortierung
 	 * @param filterPolicy
 	 *            Art des Filterhandlings
 	 * @return den Container
 	 */
 	public GenericDataContainer getGenericDataContainer(EventBus eventBus,
 			Map<String, String> formatPattern,
 			List<ContainerOrder> defaultOrder, FilterPolicy filterPolicy) {
 		return new GenericDataContainer(eventBus, formatPattern, defaultOrder,
 				filterPolicy);
 	}
 
 	/**
 	 * @param formatPattern
 	 *            Formatierungen nach Spaltennamen.
 	 * @param defaultOrder
 	 *            Default-Sortierung
 	 * @param filterPolicy
 	 *            Art des Filterhandlings
 	 * @return den Container
 	 */
 	public JMXContainer getJmxContainer(EventBus eventBus,
 			Map<String, String> formatPattern,
 			List<ContainerOrder> defaultOrder, FilterPolicy filterPolicy) {
 		return new JMXContainer(eventBus, formatPattern, defaultOrder,
 				filterPolicy);
 	}
 
 	/**
 	 * @param formatPattern
 	 *            Formatierungen nach Spaltennamen.
 	 * @param defaultOrder
 	 *            Default-Sortierung
 	 * @param filterPolicy
 	 *            Art des Filterhandlings
 	 * @return den Container
 	 */
 	public ReSTContainer getReSTContainer(EventBus eventBus,
 			Map<String, String> formatPattern,
 			List<ContainerOrder> defaultOrder, FilterPolicy filterPolicy) {
 		return new ReSTContainer(eventBus, formatPattern, defaultOrder,
 				filterPolicy);
 	}
 
 	public QueryOptionList getQueryOptionList(EventBus eventBus,
 			SelectConfig config, String datasource) {
 		return new QueryOptionList(config, eventBus, queryOptionListRepository,
 				datasource, prefetchExecutor, useOptionListCacheByDefault);
 	}
 
 	public CompoundSearch getCompoundSearch(CompoundSearchConfig config) {
 		return new CompoundSearch(config);
 	}
 
 }
