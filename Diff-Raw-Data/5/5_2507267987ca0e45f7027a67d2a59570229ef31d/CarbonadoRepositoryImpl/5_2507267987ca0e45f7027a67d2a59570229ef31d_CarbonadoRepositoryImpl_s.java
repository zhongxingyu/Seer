 /*******************************************************************************
  * Copyright (c) 2010 AGETO and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  *******************************************************************************/
 package net.ageto.gyrex.persistence.carbonado.storage.internal;
 
 import java.util.Collection;
 import java.util.Properties;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.sql.DataSource;
 
 import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;
 import org.eclipse.gyrex.persistence.storage.content.RepositoryContentTypeSupport;
 import org.eclipse.gyrex.persistence.storage.exceptions.ResourceFailureException;
 import org.eclipse.gyrex.persistence.storage.lookup.DefaultRepositoryLookupStrategy;
 import org.eclipse.gyrex.persistence.storage.lookup.RepositoryContentTypeAssignments;
 import org.eclipse.gyrex.persistence.storage.provider.RepositoryProvider;
 import org.eclipse.gyrex.persistence.storage.settings.IRepositoryPreferences;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 
 import org.osgi.service.jdbc.DataSourceFactory;
 
 import org.apache.commons.lang.text.StrBuilder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.ageto.gyrex.persistence.carbonado.internal.CarbonadoActivator;
 import net.ageto.gyrex.persistence.carbonado.internal.CarbonadoDebug;
 import net.ageto.gyrex.persistence.carbonado.storage.CarbonadoRepository;
 import net.ageto.gyrex.persistence.carbonado.storage.ICarbonadoRepositoryConstants;
 import net.ageto.gyrex.persistence.carbonado.storage.internal.jdbc.JdbcHelper;
 import net.ageto.gyrex.persistence.carbonado.storage.internal.jdbc.TracingDataSource;
 import net.ageto.gyrex.persistence.jdbc.pool.IPoolDataSourceFactoryConstants;
 
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.repo.jdbc.JDBCRepositoryBuilder;
 
 public class CarbonadoRepositoryImpl extends CarbonadoRepository {
 
 	private static final Logger LOG = LoggerFactory.getLogger(CarbonadoRepositoryImpl.class);
 
 	private final CarbonadoRepositoryContentTypeSupport carbonadoRepositoryContentTypeSupport;
 	private final AtomicReference<SchemaMigrationJob> schemaMigrationJobRef = new AtomicReference<SchemaMigrationJob>();
 
 	private volatile IStatus error;
 
 	private final IRepositoryPreferences repositoryPreferences;
 
 	public CarbonadoRepositoryImpl(final String repositoryId, final RepositoryProvider repositoryProvider, final IRepositoryPreferences repositoryPreferences) {
 		super(repositoryId, repositoryProvider, new CarbonadoRepositoryMetrics(createMetricsId(repositoryProvider, repositoryId), repositoryId));
 		this.repositoryPreferences = repositoryPreferences;
 		carbonadoRepositoryContentTypeSupport = new CarbonadoRepositoryContentTypeSupport(this);
 	}
 
 	private DataSource createDataSource() throws Exception {
 		// check if we use a shared pool
 		final String poolId = repositoryPreferences.get(JDBC_POOL_ID, null);
 		if (null != poolId) {
 			if (CarbonadoDebug.debug) {
 				LOG.debug("Using connection pool {}...", poolId);
 			}
 			final DataSourceFactory dataSourceFactory = CarbonadoActivator.getInstance().getPoolDataSourceFactory();
 			final Properties dataSourceProperties = new Properties();
 			dataSourceProperties.put(IPoolDataSourceFactoryConstants.POOL_ID, poolId);
 			dataSourceProperties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, getRepositoryId());
 			dataSourceProperties.put(DataSourceFactory.JDBC_DESCRIPTION, String.format("DataSource for repository %s using database %s", getRepositoryId(), String.valueOf(repositoryPreferences.get(JDBC_DATABASE_NAME, null))));
 			dataSourceProperties.put(DataSourceFactory.JDBC_DATABASE_NAME, repositoryPreferences.get(JDBC_DATABASE_NAME, null));
 			return dataSourceFactory.createDataSource(dataSourceProperties);
 		}
 
 		// otherwise, fallback to old implementation
 		return CarbonadoActivator.getInstance().getDataSourceSupport().createDataSource(getRepositoryId(), repositoryPreferences);
 	}
 
 	@Override
 	protected Repository createRepository() throws Exception {
 		// migrate old settings
 		final String databaseName = repositoryPreferences.get("db_name", null);
 		if (null != databaseName) {
 			if (null == repositoryPreferences.get(JDBC_DATABASE_NAME, null)) {
 				repositoryPreferences.put(JDBC_DATABASE_NAME, databaseName, false);
 			}
 			repositoryPreferences.remove("db_name");
 			repositoryPreferences.flush();
 		}
 
 		// create the data source first
 		final DataSource dataSource = createDataSource();
 		try {
 			final JDBCRepositoryBuilder builder = new JDBCRepositoryBuilder();
 			builder.setDataSource(TracingDataSource.wrap(dataSource));
 			builder.setName(generateRepoName(getRepositoryId(), repositoryPreferences));
 			builder.setAutoVersioningEnabled(true, null);
 			builder.setDataSourceLogging(CarbonadoDebug.dataSourceLogging);
 			builder.setCatalog(repositoryPreferences.get(ICarbonadoRepositoryConstants.JDBC_DATABASE_NAME, null)); // MySQL database name
 			return builder.build();
 		} catch (final Exception e) {
 			// close data source on error (COLUMBUS-1177)
 			JdbcHelper.closeQuietly(dataSource);
 			throw e;
 		} catch (final Error e) {
 			// close data source on error (COLUMBUS-1177)
 			JdbcHelper.closeQuietly(dataSource);
 			throw e;
 		}
 	}
 
 	private String generateRepoName(final String repositoryId, final IRepositoryPreferences preferences) {
 		final StrBuilder name = new StrBuilder();
 		name.append(repositoryId);
 		name.append(" (db ");
 		name.append(preferences.get(ICarbonadoRepositoryConstants.JDBC_DATABASE_NAME, "<unknown>"));
 		final String poolId = preferences.get(ICarbonadoRepositoryConstants.JDBC_POOL_ID, null);
 		if (null != poolId) {
 			name.append(", pool ").append(poolId);
 		} else {
 			name.append(", host ");
 			name.append(preferences.get(CarbonadoRepository.HOSTNAME, "<unknown>"));
 			name.append(", user ");
 			name.append(preferences.get(CarbonadoRepository.USERNAME, "<unknown>"));
 		}
 		name.append(")");
 		return name.toString();
 	}
 
 	@Override
 	public RepositoryContentTypeSupport getContentTypeSupport() {
 		return carbonadoRepositoryContentTypeSupport;
 	}
 
 	@Override
 	protected Repository getOrCreateRepository() throws ResourceFailureException {
		// overriden for package-visibility
 		return super.getOrCreateRepository();
 	}
 
 	@Override
 	protected IStatus getStatus() {
 		// check for internal error
 		final IStatus errorStatus = error;
 		if (null != errorStatus)
 			return errorStatus;
 
 		// check for ongoing schema migration
 		if (null != schemaMigrationJobRef.get())
 			return new Status(IStatus.CANCEL, CarbonadoActivator.SYMBOLIC_NAME, String.format("Schema verifivation in progress for repository '%s'.", getRepositoryId()));
 
 		// ok
 		return Status.OK_STATUS;
 	}
 
 	public void setError(final String message) {
 		error = null != message ? new Status(IStatus.ERROR, CarbonadoActivator.SYMBOLIC_NAME, message) : null;
 	}
 
 	/**
 	 * Schedules a schema check with the repository.
 	 * 
 	 * @param migrate
 	 *            <code>true</code> if a schema migration should be performed
 	 *            automatically, <code>false</code> otherwise
 	 * @param waitForFinish
 	 *            <code>true</code> if the current thread should wait for the
 	 *            schema verification to finish
 	 */
 	public IStatus verifySchema(final boolean migrate, final boolean waitForFinish) {
 		// get all assigned content types
 		final Collection<RepositoryContentType> contentTypes;
 		try {
 			final RepositoryContentTypeAssignments assignments = DefaultRepositoryLookupStrategy.getDefault().getContentTypeAssignments(getRepositoryId());
 			contentTypes = assignments.getContentTypes(true);
		} catch (final IllegalStateException e) {
 			// fail if case of unresolved content types
 			throw new ResourceFailureException(e.getMessage());
 		}
 
 		// quick check
 		if (contentTypes.isEmpty())
 			// nothing to do
 			return Status.OK_STATUS;
 
 		final SchemaMigrationJob migrationJob = new SchemaMigrationJob(this, contentTypes, migrate);
 		if (schemaMigrationJobRef.compareAndSet(null, migrationJob)) {
 			final CountDownLatch waitSignal = new CountDownLatch(1);
 			migrationJob.addJobChangeListener(new JobChangeAdapter() {
 				@Override
 				public void done(final IJobChangeEvent event) {
 					// reset reference when done
 					schemaMigrationJobRef.compareAndSet(migrationJob, null);
 					waitSignal.countDown();
 				}
 			});
 			migrationJob.schedule();
 
 			if (!waitForFinish)
 				return Job.ASYNC_FINISH;
 
 			try {
 				LOG.debug("Waiting for database '{}' to finish verifying schema.", getDescription());
 				if (!waitSignal.await(3, TimeUnit.MINUTES))
 					throw new ResourceFailureException(String.format("Timout waiting for database to verify schema. Please check database '%s'. ", getDescription()));
 				return migrationJob.getSchemaStatus();
 			} catch (final InterruptedException e) {
 				Thread.currentThread().interrupt();
 				return Status.CANCEL_STATUS;
 			}
 		}
 
 		return Status.CANCEL_STATUS;
 	}
 }
