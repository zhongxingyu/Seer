 package com.arcaner.warlock.rcp.actions;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.swt.widgets.Display;
 
 import com.arcaner.warlock.configuration.Profile;
 import com.arcaner.warlock.network.ISGEConnectionListener;
 import com.arcaner.warlock.network.SGEConnection;
 import com.arcaner.warlock.rcp.plugin.Warlock2Plugin;
 import com.arcaner.warlock.rcp.ui.WarlockSharedImages;
 import com.arcaner.warlock.rcp.ui.network.SWTSGEConnectionListenerAdapter;
 import com.arcaner.warlock.rcp.util.LoginUtil;
 
 public class ProfileConnectAction extends Action implements ISGEConnectionListener {
 	private Profile profile;
 	private IProgressMonitor monitor;
 	private boolean finished;
 	private IStatus status;
 	
 	public ProfileConnectAction (Profile profile) {
 		super(profile.getCharacterName(),
 			WarlockSharedImages.getImageDescriptor(WarlockSharedImages.IMG_CHARACTER));
 		
 		this.profile = profile;
 	}
 	
 	@Override
 	public void run() {
 		finished = false;
 		status = Status.OK_STATUS;
 		
 		Job connectJob = new Job("Logging into profile \"" + profile.getCharacterName() + "\"...") {
 			protected IStatus run(IProgressMonitor monitor) {
 				ProfileConnectAction.this.monitor = monitor;
 				
 				SGEConnection connection = new SGEConnection();
 				connection.addSGEConnectionListener(new SWTSGEConnectionListenerAdapter(ProfileConnectAction.this));
 				monitor.beginTask("Logging into profile \"" + profile.getCharacterName() + "\"...", 5);
 				
				while (!ProfileConnectAction.this.finished) {
					Display.getDefault().syncExec(new Runnable() {
						public void run () { 
							Display.getDefault().readAndDispatch();
						}
					});
				}
 				
 				return status;
 			}
 		};
 		
 		connectJob.schedule();
 	}
 
 	public void loginReady(SGEConnection connection) {
 		monitor.worked(1);
 		
 		if (!monitor.isCanceled()) {
 			connection.login(profile.getAccount().getAccountName(), profile.getAccount().getPassword());
 		} else {
 			status = Status.CANCEL_STATUS;
 			finished = true;
 		}
 	}
 	
 	public void loginFinished(SGEConnection connection, int status) {
 		monitor.worked(1);
 		
 		if (status != SGEConnection.LOGIN_SUCCESS)
 		{
 			LoginUtil.showAuthenticationError(status);
 			this.status = new Status(IStatus.ERROR, Warlock2Plugin.PLUGIN_ID, LoginUtil.getAuthenticationError(status));
 			finished = true;
 		}
 	}
 	
 	public void gamesReady(SGEConnection connection, Map<String, String> games) {
 		monitor.worked(1);
 		
 		if (!monitor.isCanceled())
 		{
 			connection.selectGame(profile.getGameCode());
 		} else {
 			status = Status.CANCEL_STATUS;
 			finished = true;
 		}
 	}
 	
 	public void charactersReady(SGEConnection connection, Map<String, String> characters) {
 		monitor.worked(1);
 		
 		connection.selectCharacter(profile.getCharacterCode());
 	}
 
 	public void readyToPlay(SGEConnection connection, Map<String,String> loginProperties) {
 		monitor.worked(1);
 		monitor.done();
 		
 		if (!monitor.isCanceled())
 		{
 			LoginUtil.connectAndOpenGameView(loginProperties);
 		} else {
 			status = Status.CANCEL_STATUS;
 			finished = true;
 		}
 	}
 
 }
