 package liquibase.eclipse.plugin.controller;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import liquibase.Liquibase;
 import liquibase.eclipse.plugin.model.ChangeSet;
 import liquibase.eclipse.plugin.model.ChangeSetContentProvider;
 import liquibase.eclipse.plugin.model.ChangeSetStatus;
 import liquibase.eclipse.plugin.model.DatabaseConfiguration;
 import liquibase.exception.LiquibaseException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 
 /**
  * Handles the multiple threads started by a liquibase release.
  * 
  * @author afinke
  *
  */
 public class ReleaseJob extends Job {
 
 	private Liquibase liquibase;
 	private DatabaseConfiguration databaseConfiguration;
 	private Button releaseButton;
 	private Shell shell;
 	
 	public ReleaseJob(String name, 
 					  DatabaseConfiguration databaseConfiguration,
 					  Liquibase liquibase, 
 					  Button releaseButton,
 					  Shell shell) {
 		super(name);
 		this.liquibase = liquibase;
 		this.databaseConfiguration = databaseConfiguration;
 		this.releaseButton = releaseButton;
 		this.shell = shell;
 	}
 
 	@Override
 	protected IStatus run(IProgressMonitor progressMonitor) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				// disable release button to avoid multiple releases
 				releaseButton.setEnabled(false);
 			}
 		});
 		ExecutorService executor = Executors.newCachedThreadPool();
 		DatabaseChangelogPoller databaseChangelogPoller = 
 				new DatabaseChangelogPoller(databaseConfiguration);
 		// start polling 'databasechangelog'
 		executor.execute(databaseChangelogPoller);
 		// start updating the database
 		try {
 			liquibase.update(null);
 		} catch (LiquibaseException e) {
 			databaseChangelogPoller.stop();
 			executor.shutdown();
 			List<ChangeSet> changeSets = ChangeSetContentProvider.getInstance().getChangeSets();
 			for (ChangeSet changeSet : changeSets) {
				if(changeSet.getStatus().equals(ChangeSetStatus.RUNNING)) {
 					changeSet.setStatus(ChangeSetStatus.ERROR);
 					break;
 				}
 			}
 			reportError(e.getMessage());
 			return Status.CANCEL_STATUS;
 		}
 		databaseChangelogPoller.stop();
 		executor.shutdown();
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				// enable release button after everything finished
 				releaseButton.setEnabled(true);
 			}
 		});
 		 reportSuccess();
 		return Status.OK_STATUS;
 	}
 	
 	private void reportSuccess() {
 	    Display.getDefault().asyncExec(new Runnable() {
 	      public void run() {
 	        MessageDialog.openInformation(shell, "Success", "Database updates were successfull.");
 	      }
 	    });
 	}
 	
 	private void reportError(final String error) {
 	    Display.getDefault().asyncExec(new Runnable() {
 	      public void run() {
 	        MessageDialog.openError(shell, "Error", error);
 	      }
 	    });
 	}
 	
 }
