 /*******************************************************************************
  * Copyright (c) 2006-2011 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.monitoring.cassandra.storage.provider;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import me.prettyprint.cassandra.service.ThriftCfDef;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.ddl.ColumnDefinition;
 import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
 import me.prettyprint.hector.api.ddl.ColumnType;
 import me.prettyprint.hector.api.ddl.ComparatorType;
 import me.prettyprint.hector.api.factory.HFactory;
 
 import org.ebayopensource.turmeric.monitoring.cassandra.storage.dao.MetricIdentifierDAO;
 import org.ebayopensource.turmeric.monitoring.cassandra.storage.dao.MetricsDAO;
 import org.ebayopensource.turmeric.monitoring.cassandra.storage.model.MetricIdentifier;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricIdentifierDAOImpl;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricServiceCallsByTimeDAOImpl;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricTimeSeriesDAOImpl;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricValuesByIpAndDateDAOImpl;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricValuesDAOImpl;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricsServiceConsumerByIpDAOImpl;
 import org.ebayopensource.turmeric.monitoring.provider.dao.impl.MetricsServiceOperationByIpDAOImpl;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.monitoring.MonitoringSystem;
 import org.ebayopensource.turmeric.runtime.common.monitoring.MetricClassifier;
 import org.ebayopensource.turmeric.runtime.common.monitoring.MetricId;
 import org.ebayopensource.turmeric.runtime.common.monitoring.MetricsStorageProvider;
 import org.ebayopensource.turmeric.runtime.common.monitoring.value.MetricValue;
 import org.ebayopensource.turmeric.runtime.common.monitoring.value.MetricValueAggregator;
 import org.ebayopensource.turmeric.utils.cassandra.service.CassandraManager;
 
 /**
  * The Class CassandraMetricsStorageProvider.
  */
 public class CassandraMetricsStorageProvider implements MetricsStorageProvider {
 
 	/** The Constant KEY_SEPARATOR. */
 	public static final String KEY_SEPARATOR = "|";
 
 	/** The snapshot interval. */
 	private int snapshotInterval;
 
 	/** The server side. */
 	private boolean serverSide;
 
 	/** The store service metrics. */
 	private boolean storeServiceMetrics;
 
 	/** The metric id dao. */
 	private MetricIdentifierDAO metricIdDAO;
 
 	/** The metrics dao. */
 	private MetricsDAO metricsDAO;
 
 	private Collection<MetricValueAggregator> previousSnapshot;
 
 	private MetricIdentifierDAOImpl<String> metricIdentifierDAO;
 
 	private MetricTimeSeriesDAOImpl<String> metricTimeSeriesDAO;
 
 	private MetricsServiceConsumerByIpDAOImpl<String, String> metricsServiceConsumerByIpDAO;
 
 	private MetricValuesDAOImpl<String> metricValuesDAO;
 
 	private MetricsServiceOperationByIpDAOImpl<String, String> metricsServiceOperationByIpDAO;
 
 	private MetricServiceCallsByTimeDAOImpl<String, Long> metricServiceCallsByTimeDAO;
 
 	private MetricValuesByIpAndDateDAOImpl<String, Long> metricValuesByIpAndDateDAO;
 
 	/**
 	 * Inits the.
 	 * 
 	 * @param options
 	 *            the options
 	 * @param name
 	 *            the name
 	 * @param collectionLocation
 	 *            the collection location
 	 * @param snapshotInterval
 	 *            the snapshot interval
 	 * @see org.ebayopensource.turmeric.runtime.common.monitoring.MetricsStorageProvider#init(java.util.Map,
 	 *      java.lang.String, java.lang.String, java.lang.Integer)
 	 */
 	@Override
 	public void init(Map<String, String> options, String name,
 			String collectionLocation, Integer snapshotInterval) {
 		System.out.println("collectionLocation = " + collectionLocation);
 		this.serverSide = MonitoringSystem.COLLECTION_LOCATION_SERVER
 				.equals(collectionLocation);
 		this.storeServiceMetrics = Boolean.parseBoolean(options
 				.get("storeServiceMetrics"));
 		this.snapshotInterval = snapshotInterval;
 		String host = options.get("host-address");
 		String s_keyspace = options.get("keyspace-name");
 		String columnFamilyName = "MetricIdentifier";
 		String clusterName = options.get("cluster-name");
 		String embedded = options.get("embedded");
 		if (Boolean.valueOf(embedded)) {
 			CassandraManager.initialize();
 		}
 		this.initDAO(clusterName, host, s_keyspace);
 		metricIdDAO = new MetricIdentifierDAO(clusterName, host, s_keyspace,
 				String.class, MetricIdentifier.class, columnFamilyName);
 		metricsDAO = new MetricsDAO(clusterName, host, s_keyspace);
 		
 
 	}
 
 	/**
 	 * Save metric snapshot.
 	 * 
 	 * @param timeSnapshot
 	 *            the time snapshot
 	 * @param snapshotCollection
 	 *            the snapshot collection
 	 * @throws ServiceException
 	 *             the service exception
 	 * @see org.ebayopensource.turmeric.runtime.common.monitoring.MetricsStorageProvider#saveMetricSnapshot(long,
 	 *      java.util.Collection)
 	 */
 	@Override
 	public void saveMetricSnapshot(long timeSnapshot,
 			Collection<MetricValueAggregator> snapshotCollection)
 			throws ServiceException {
 		try {
 			System.out.println("snapshotCollection null?" + snapshotCollection);
 
 			if (snapshotCollection == null || snapshotCollection.isEmpty()) {
 				System.out.println("snapshotCollection empty");
 				return;
 			}
 			List<MetricValue> metricValuesToSave = new ArrayList<MetricValue>();
 			System.out.println("snapshotCollection element size = "
 					+ snapshotCollection.size());
 			for (MetricValueAggregator metricValueAggregator : snapshotCollection) {
 				System.out.println("metricValueAggregator="
 						+ metricValueAggregator);
 				org.ebayopensource.turmeric.runtime.common.monitoring.MetricId metricId = metricValueAggregator
 						.getMetricId();
 				if (metricId.getOperationName() == null) {
 					// Service-level metric, should we skip it ?
 					if (!storeServiceMetrics) {
 						continue;
 					}
 				}
 
 				metricValueAggregator = resolve(metricValueAggregator);
 
 				MetricIdentifier<String> cmetricIdentifier = null;
 				Collection<MetricClassifier> classifiers = metricValueAggregator
 						.getClassifiers();
 				System.out.println("classifiers.size=" + classifiers.size());
 				for (MetricClassifier metricClassifier : classifiers) {
 					org.ebayopensource.turmeric.runtime.common.monitoring.value.MetricValue metricValue = metricValueAggregator
 							.getValue(metricClassifier);
 					org.ebayopensource.turmeric.runtime.common.monitoring.value.MetricComponentValue[] metricComponentValues = metricValue
 							.getValues();
 					if (valuesAreNonZero(metricComponentValues)) {
 						if (cmetricIdentifier == null) {
 							cmetricIdentifier = findMetricId(getKeyfromMetricId(
 									metricId, serverSide));
 							if (cmetricIdentifier == null) {
 								createMetricId(metricId, metricValueAggregator);
 								cmetricIdentifier = findMetricId(getKeyfromMetricId(
 										metricId, serverSide));
 							}
 						}
 						// now, store the service stats for the
 						// getMetricsMetadata calls
 						metricsDAO.saveServiceOperationByIpCF(getIPAddress(),
 								cmetricIdentifier);
 						// now, store the service stats for the
 						// getMetricsMetadata calls for consumers
 						metricsDAO.saveServiceConsumerByIpCF(getIPAddress(),
 								cmetricIdentifier,
 								metricClassifier.getUseCase());
 						metricValuesToSave.add(metricValue);
 					}
 
 				}
 				metricsDAO.saveMetricValues(getIPAddress(), cmetricIdentifier,
 						timeSnapshot, snapshotInterval, serverSide,
 						metricValuesToSave);
 				metricValuesToSave.clear();
 				cmetricIdentifier = null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			previousSnapshot = snapshotCollection;
 		}
 
 	}
 
 	protected MetricValueAggregator resolve(MetricValueAggregator aggregator) {
 		if (previousSnapshot != null) {
 			MetricId metricId = aggregator.getMetricId();
 			for (MetricValueAggregator previousAggregator : previousSnapshot) {
 				if (metricId.equals(previousAggregator.getMetricId())) {
 					return (MetricValueAggregator) aggregator.diff(
 							previousAggregator, true);
 				}
 			}
 		}
 		return aggregator;
 	}
 
 	/**
 	 * Creates the metric id.
 	 * 
 	 * @param metricId
 	 *            the metric id
 	 * @param metricValueAggregator
 	 *            the metric value aggregator
 	 */
 	private void createMetricId(
 			org.ebayopensource.turmeric.runtime.common.monitoring.MetricId metricId,
 			MetricValueAggregator metricValueAggregator) {
 		MetricIdentifier<String> model = new MetricIdentifier<String>(metricId.getMetricName(),
 				metricId.getAdminName(), metricId.getOperationName(),
 				serverSide);
 		String key = model.getKey();
		metricIdentifierDAO.save(key, model);
		//this.metricIdDAO.save(key, model);
 	}
 
 	/**
 	 * Find metric id.
 	 * 
 	 * @param keyfromMetricId
 	 *            the keyfrom metric id
 	 * @return the metric identifier
 	 */
 	private MetricIdentifier<String> findMetricId(String keyfromMetricId) {
		return this.metricIdentifierDAO.find(keyfromMetricId);
 	}
 
 	/**
 	 * Gets the key from the metric id.
 	 * 
 	 * @param metricId
 	 *            the metric id
 	 * @param serverSide
 	 *            the server side
 	 * @return the keyfrom metric id
 	 */
 	public String getKeyfromMetricId(
 			org.ebayopensource.turmeric.runtime.common.monitoring.MetricId metricId,
 			boolean serverSide) {
 		return metricId.getMetricName() + KEY_SEPARATOR
 				+ metricId.getAdminName() + KEY_SEPARATOR
 				+ metricId.getOperationName() + KEY_SEPARATOR + serverSide;
 	}
 
 	/**
 	 * Checks if is server side.
 	 * 
 	 * @return true, if is server side
 	 */
 	public boolean isServerSide() {
 		return serverSide;
 	}
 
 	/**
 	 * Checks if is store service metrics.
 	 * 
 	 * @return true, if is store service metrics
 	 */
 	public boolean isStoreServiceMetrics() {
 		return storeServiceMetrics;
 	}
 
 	/**
 	 * Gets the snapshot interval.
 	 * 
 	 * @return the snapshot interval
 	 */
 	public int getSnapshotInterval() {
 		return snapshotInterval;
 	}
 
 	/**
 	 * Values are non zero.
 	 * 
 	 * @param metricComponentValues
 	 *            the metric component values
 	 * @return true, if successful
 	 */
 	protected boolean valuesAreNonZero(
 			org.ebayopensource.turmeric.runtime.common.monitoring.value.MetricComponentValue[] metricComponentValues) {
 		for (org.ebayopensource.turmeric.runtime.common.monitoring.value.MetricComponentValue metricComponentValue : metricComponentValues) {
 			Number value = (Number) metricComponentValue.getValue();
 			if (value.longValue() != 0L) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the inet address.
 	 * 
 	 * @return the inet address
 	 * @throws ServiceException
 	 *             the service exception
 	 */
 	public String getIPAddress() throws ServiceException {
 		try {
 			return InetAddress.getLocalHost().getCanonicalHostName();
 		} catch (UnknownHostException x) {
 			throw new ServiceException("Unkonwn host name", x);
 		}
 	}
 
 	private void createCF(final String kspace, final String columnFamilyName,
 			final Cluster cluster, boolean isSuperColumn,
 			final ComparatorType superKeyValidator,
 			final ComparatorType keyValidator,
 			final ComparatorType superComparator,
 			final ComparatorType comparator) {
 
 		if (isSuperColumn) {
 			ThriftCfDef cfDefinition = (ThriftCfDef) HFactory
 					.createColumnFamilyDefinition(kspace, columnFamilyName,
 							superComparator, new ArrayList<ColumnDefinition>());
 			cfDefinition.setColumnType(ColumnType.SUPER);
 			cfDefinition
 					.setKeyValidationClass(superKeyValidator.getClassName());
 			cfDefinition.setSubComparatorType(comparator);
 			cluster.addColumnFamily(cfDefinition);
 		} else {
 			ColumnFamilyDefinition cfDefinition = new ThriftCfDef(kspace,
 					columnFamilyName);
 			cfDefinition.setKeyValidationClass(keyValidator.getClassName());
 			if ("MetricValuesByIpAndDate".equals(columnFamilyName)
 					|| "MetricTimeSeries".equals(columnFamilyName)
 					|| "ServiceCallsByTime".equals(columnFamilyName)) {
 
 				ComparatorType comparator1 = getComparator(Long.class);
 				cfDefinition.setComparatorType(comparator1);
 			} else {
 				cfDefinition.setComparatorType(comparator);
 			}
 
 			cluster.addColumnFamily(cfDefinition);
 		}
 	}
 
 	private ComparatorType getComparator(Class<?> keyTypeClass) {
 		if (keyTypeClass != null && String.class.isAssignableFrom(keyTypeClass)) {
 			return ComparatorType.UTF8TYPE;
 		} else if (keyTypeClass != null
 				&& Integer.class.isAssignableFrom(keyTypeClass)) {
 			return ComparatorType.INTEGERTYPE;
 		} else if (keyTypeClass != null
 				&& Long.class.isAssignableFrom(keyTypeClass)) {
 			return ComparatorType.LONGTYPE;
 		} else if (keyTypeClass != null
 				&& Date.class.isAssignableFrom(keyTypeClass)) {
 			return ComparatorType.TIMEUUIDTYPE;
 		} else {
 			return ComparatorType.BYTESTYPE; // by default
 		}
 
 	}
 
 	private void initDAO(String clusterName, String host, String keyspace) {
 		metricIdentifierDAO = new MetricIdentifierDAOImpl<String>(clusterName,
 				host, keyspace, "MetricIdentifier", String.class);
 		metricTimeSeriesDAO = new MetricTimeSeriesDAOImpl<String>(clusterName,
 				host, keyspace, "MetricTimeSeries", String.class);
 		metricsServiceConsumerByIpDAO = new MetricsServiceConsumerByIpDAOImpl<String, String>(
 				clusterName, host, keyspace, "ServiceConsumerByIp",
 				String.class, String.class);
 		metricValuesDAO = new MetricValuesDAOImpl<String>(clusterName, host,
 				keyspace, "MetricValues", String.class);
 		metricsServiceOperationByIpDAO = new MetricsServiceOperationByIpDAOImpl<String, String>(
 				clusterName, host, keyspace, "ServiceOperationByIp",
 				String.class, String.class);
 		metricServiceCallsByTimeDAO = new MetricServiceCallsByTimeDAOImpl<String, Long>(
 				clusterName, host, keyspace, "ServiceCallsByTime",
 				String.class, Long.class);
 		metricValuesByIpAndDateDAO = new MetricValuesByIpAndDateDAOImpl<String, Long>(
 				clusterName, host, keyspace, "MetricValuesByIpAndDate",
 				String.class, Long.class);
 	}
 
 }
