 /**
  * Copyright (c) 2011 Gunnar Wagenknecht and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  */
 package net.ageto.gyrex.persistence.carbonado.storage.internal;
 
 import java.sql.Connection;
 import java.util.Collection;
 
 import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.Job;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.ageto.gyrex.persistence.carbonado.internal.CarbonadoActivator;
 import net.ageto.gyrex.persistence.carbonado.storage.spi.jdbc.DatabaseSchemaSupport;
 
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.repo.jdbc.JDBCConnectionCapability;
 
 /**
  * Migrates a database schema of a JDBC based Carbonado Repository.
  */
 public class SchemaMigrationJob extends Job {
 
 	private static final class RepositoryRule implements ISchedulingRule {
 		private final String repositoryId;
 
 		public RepositoryRule(final CarbonadoRepositoryImpl repository) {
 			repositoryId = repository.getRepositoryId();
 		}
 
 		@Override
 		public boolean contains(final ISchedulingRule rule) {
 			return this == rule;
 		}
 
 		@Override
 		public boolean isConflicting(final ISchedulingRule rule) {
 			return (rule instanceof RepositoryRule) && repositoryId.equals(((RepositoryRule) rule).repositoryId);
 		}
 	}
 
 	/** STATUS2 */
 	public static final Status NOT_AVAILABLE = new Status(IStatus.INFO, CarbonadoActivator.SYMBOLIC_NAME, "Status information not available.");
 
 	private static final Logger LOG = LoggerFactory.getLogger(SchemaMigrationJob.class);
 
 	private final CarbonadoRepositoryImpl repository;
 	private final boolean migrate;
 
 	private final Collection<RepositoryContentType> contentTypes;
 
 	private IStatus schemaStatus;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param repository
 	 * @param contentTypes
 	 * @param migrate
 	 */
 	public SchemaMigrationJob(final CarbonadoRepositoryImpl repository, final Collection<RepositoryContentType> contentTypes, final boolean migrate) {
 		super(String.format("Verify database schema of '%s'", repository.getDescription()));
 		this.repository = repository;
 		this.contentTypes = contentTypes;
 		this.migrate = migrate;
 		setPriority(LONG);
 		setRule(new RepositoryRule(repository));
 	}
 
 	private IStatus buildStatusInternal(final int severity, final String message) {
 		return new Status(severity, CarbonadoActivator.SYMBOLIC_NAME, message);
 	}
 
 	/**
 	 * the {@link SchemaMigrationJob#schemaStatus} is updated at every touch of
 	 * the repository.
 	 * 
 	 * @return the current schemaStatus or
 	 *         {@link SchemaMigrationJob#NOT_AVAILABLE} if none is set
 	 */
 	public IStatus getSchemaStatus() {
 		final IStatus status = schemaStatus;
 		return null != status ? status : NOT_AVAILABLE;
 	}
 
 	@Override
 	protected IStatus run(final IProgressMonitor progressMonitor) {
 		final SubMonitor monitor = SubMonitor.convert(progressMonitor, String.format("Verifying database schema for '%s'...", repository.getRepositoryId()), 100);
 
 		Repository cRepository;
 		try {
 			cRepository = repository.getOrCreateRepository();
 		} catch (final Exception e) {
 			LOG.error("Failed to open repository {}. {}", new Object[] { repository.getRepositoryId(), ExceptionUtils.getRootCauseMessage(e), e });
 			final String message = String.format("Failed to open repository. Please check server logs. %s", ExceptionUtils.getRootCauseMessage(e));
 			repository.setError(message);
 			return schemaStatus = buildStatusInternal(IStatus.CANCEL, message);
 		}
 
 		// check if JDBC database
 		final JDBCConnectionCapability jdbcConnectionCapability = cRepository.getCapability(JDBCConnectionCapability.class);
 		if (jdbcConnectionCapability == null) {
 			// non JDBC repository means auto-updating schema
 			return schemaStatus = Status.OK_STATUS;
 		}
 
 		// check if transaction in progress
 		if (cRepository.getTransactionIsolationLevel() != null) {
 			LOG.warn("Carbonado repository '{}' (using database {}) is configured with a default transaction level. This is not supported by the current schema migration implementation.", repository.getRepositoryId(), repository.getDescription());
 		}
 
 		// quick check
 		if (contentTypes.isEmpty()) {
 			// don't fail although this should be a programming error
 			LOG.debug("No content types assigned to repository '{}'.", repository.getDescription());
 			repository.setError(null);
 			return schemaStatus = Status.OK_STATUS;
 		}
 
 		// verify schemas
 		try {
 			schemaStatus = verifySchemas(contentTypes, jdbcConnectionCapability, monitor.newChild(50));
 		} catch (final Exception e) {
 			LOG.error("Failed to verify database schema for database {} (repository {}). {}", new Object[] { cRepository.getName(), repository.getRepositoryId(), ExceptionUtils.getRootCauseMessage(e), e });
 			final String message = String.format("Unable to verify database schema. Please check server logs. %s", ExceptionUtils.getRootCauseMessage(e));
 			repository.setError(message);
 			return schemaStatus = buildStatusInternal(IStatus.CANCEL, message);
 		}
 
 		// done
 		if ((null != schemaStatus) && schemaStatus.matches(IStatus.ERROR)) {
 			repository.setError(String.format("Database schema verification failed. Please check database %s.", repository.getDescription()));
			return schemaStatus;
 		} else {
 			repository.setError(null);
			return schemaStatus = Status.OK_STATUS;
 		}
 	}
 
 	private IStatus verifySchema(final RepositoryContentType contentType, final Connection connection, final IProgressMonitor monitor) {
 		final SubMonitor subMonitor = SubMonitor.convert(monitor, String.format("Verifying '%s' for repository '%s'", contentType.getMediaType(), repository.getRepositoryId()), 20);
 
 		final DatabaseSchemaSupport schemaSupport = CarbonadoActivator.getInstance().getSchemaSupportTracker().getSchemaSupport(contentType);
 		if (null == schemaSupport) {
 			// no schema support but JDBC repository means "managed externally" (so we assume "ok")
 			return schemaStatus = Status.OK_STATUS;
 		}
 
 		// check if provisioned first
 		final IStatus provisioningStatus;
 		try {
 			provisioningStatus = schemaSupport.isProvisioned(repository, contentType, connection);
 			if (provisioningStatus.isOK()) {
 				// all good
 				return schemaStatus = provisioningStatus;
 			}
 		} catch (final Exception e) {
 			// this is not good in any case, if we can't verify that a content type
 			// is provisioned, we need to abort and report any error that occurred
 			// only fail if not migrating, otherwise we do make a migration attempt
 			throw new IllegalStateException(String.format("Failed to check provisioning status for content type '%s'. %s", contentType, ExceptionUtils.getRootCauseMessage(e)), e);
 		}
 
 		subMonitor.worked(10);
 
 		// attempt migration
 		if (migrate) {
 			final IStatus result = schemaSupport.provision(repository, contentType, connection, subMonitor.newChild(10));
 			if (result.matches(IStatus.CANCEL | IStatus.ERROR)) {
 				return schemaStatus = result;
 			}
 
 			// all good
 			return schemaStatus = Status.OK_STATUS;
 		}
 
 		return schemaStatus = provisioningStatus;
 	}
 
 	private IStatus verifySchemas(final Collection<RepositoryContentType> contentTypes, final JDBCConnectionCapability jdbcConnectionCapability, final IProgressMonitor monitor) throws Exception {
 		// spin the migration loop
 		Connection connection = null;
 		boolean wasAutoCommit = true; // default to auto-commit
 		try {
 			// get connection
 			connection = jdbcConnectionCapability.getConnection();
 			// remember auto-commit state
 			wasAutoCommit = connection.getAutoCommit();
 
 			// collect result
 			final MultiStatus result = new MultiStatus(CarbonadoActivator.SYMBOLIC_NAME, 0, String.format("Database schema verification result for database %s.", repository.getDescription()), null);
 
 			// verify schemas
 			final SubMonitor subMonitor = SubMonitor.convert(monitor, contentTypes.size());
 			for (final RepositoryContentType contentType : contentTypes) {
 				result.add(verifySchema(contentType, connection, subMonitor.newChild(1)));
 			}
 
 			// commit any pending changes if migration was allowed
 			if (migrate) {
 				connection.commit();
 			} else {
 				connection.rollback();
 			}
 
 			return schemaStatus = result;
 		} finally {
 			if (null != connection) {
 				try {
 					// verify that auto-commit state was not modified
 					if (wasAutoCommit != connection.getAutoCommit()) {
 						// Carbonado uses auto-commit to detect if a transaction
 						// was in progress whan the connection was acquired previously
 						// in this case it does not close the connection, which is fine;
 						// however, if any schema-support implementation removed the auto-commit flag
 						// Carbonado will no longer close the connection because it thinks a
 						// transaction is in progress;
 						// thus we need to reset the auto-commit flag in this case!
 						LOG.debug("Resetting auto-commit flag on connection {} due to modifications during schema migration", connection);
 						connection.setAutoCommit(wasAutoCommit);
 					}
 					jdbcConnectionCapability.yieldConnection(connection);
 				} catch (final Exception e) {
 					throw new IllegalStateException("Unable to properly return a database connection to the pool. This will lead to resource leaks! " + e.getMessage(), e);
 				}
 			}
 		}
 	}
 }
