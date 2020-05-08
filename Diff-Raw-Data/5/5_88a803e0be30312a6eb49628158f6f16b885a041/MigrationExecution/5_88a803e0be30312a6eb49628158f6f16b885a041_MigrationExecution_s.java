 package de.jardas.migrator.internal;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.jardas.migrator.DatabaseAdapter;
 import de.jardas.migrator.MigrationException;
 import de.jardas.migrator.event.MigrationEvent;
 import de.jardas.migrator.event.MigrationListener;
 import de.jardas.migrator.event.MigrationStartEvent;
 import de.jardas.migrator.event.MigrationSuccessEvent;
 import de.jardas.migrator.source.MigrationSource;
 
 public class MigrationExecution {
 	private static final Logger LOG = LoggerFactory
 			.getLogger(MigrationExecution.class);
 	private final DatabaseAdapter databaseAdapter;
 	private final List<MigrationSource> migrations;
 	private final List<MigrationListener> listeners;
 	private int totalMigrationCount;
 	private int currentMigrationIndex;
 
 	public MigrationExecution(final DatabaseAdapter databaseAdapter,
 			final List<MigrationSource> migrations,
 			final List<MigrationListener> listeners) {
 		this.databaseAdapter = databaseAdapter;
 		this.migrations = migrations;
 		this.listeners = listeners;
 	}
 
 	public void execute() throws SQLException {
 		final List<MigrationSource> selectedMigrations = selectMigrations(migrations);
 
 		if (selectedMigrations.isEmpty()) {
 			LOG.info("Database is up to date, no migration required.");
 			return;
 		}
 
 		LOG.info("Going to execute {} migrations.", selectedMigrations.size());
 		totalMigrationCount = selectedMigrations.size();
 
 		for (final MigrationSource migration : selectedMigrations) {
 			fireEvent(new MigrationStartEvent(migration.getId(),
 					currentMigrationIndex, totalMigrationCount));
 
 			try {
 				LOG.info("Executing migration '{}' ({}/{})", new Object[] {
						migration.getId(), currentMigrationIndex,
 						totalMigrationCount, });
 				final Date executedAt = new Date();
 				executeMigration(migration);
 				databaseAdapter.registerExecutedMigration(migration.getId(),
 						executedAt);
 			} catch (final Exception e) {
 				final String error = String.format(
 						"Migration '%s' (%d/%d) failed: %s", migration.getId(),
						currentMigrationIndex, totalMigrationCount, e);
 				LOG.error(error, e);
 				throw new MigrationException(error, e);
 			}
 
 			fireEvent(new MigrationSuccessEvent(migration.getId(),
 					currentMigrationIndex, totalMigrationCount));
 			currentMigrationIndex++;
 		}
 	}
 
 	private void executeMigration(final MigrationSource migration)
 			throws SQLException, IOException {
 		final Iterable<String> lines = MigrationHelper.getLines(migration
 				.getInputStream());
 
 		for (final String line : lines) {
 			databaseAdapter.executeStatement(line);
 		}
 	}
 
 	private List<MigrationSource> selectMigrations(
 			final List<MigrationSource> migrations) throws SQLException {
 		final List<MigrationSource> selected = new ArrayList<MigrationSource>();
 
 		for (final MigrationSource migration : migrations) {
 			if (!databaseAdapter.isMigrationApplied(migration.getId())) {
 				selected.add(migration);
 				LOG.debug("Selectig migration '{}' for execution.",
 						migration.getId());
 			} else {
 				LOG.trace("Migration '{}' was already executed.",
 						migration.getId());
 			}
 		}
 
 		return selected;
 	}
 
 	private void fireEvent(final MigrationEvent event) {
 		for (final MigrationListener listener : listeners) {
 			try {
 				listener.onMigrationEvent(event);
 			} catch (final RuntimeException e) {
 				LOG.warn("Error notifying listener " + listener + " about "
 						+ event + ": " + e, e);
 			}
 		}
 	}
 }
