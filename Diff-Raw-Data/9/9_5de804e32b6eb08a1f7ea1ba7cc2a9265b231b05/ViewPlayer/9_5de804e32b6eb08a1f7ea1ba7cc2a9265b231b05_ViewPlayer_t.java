 package net.sparktank.morrigan.views;
 
 import net.sparktank.morrigan.dialogs.MorriganMsgDlg;
 import net.sparktank.morrigan.model.media.MediaTrack;
 import net.sparktank.morrigan.playback.IPlaybackEngine;
 import net.sparktank.morrigan.playback.ImplException;
 import net.sparktank.morrigan.playback.PlaybackException;
 import net.sparktank.morrigan.var.Const;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.part.ViewPart;
 
 public class ViewPlayer extends ViewPart {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public static final String ID = "net.sparktank.morrigan.views.ViewPlayer";
 	
 	private Label mainLabel;
 	
 	private MediaTrack currentTrack = null; 
 
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	ViewPart methods.
 	
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout ());
 		mainLabel = new Label(parent, SWT.WRAP);
 		
 		updateStatus();
 	}
 	
 	@Override
 	public void setFocus() {}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Playback engine.
 	
 	IPlaybackEngine playbackEngine = null;
 	
 	private IPlaybackEngine getPlaybackEngine () throws ImplException {
 		if (playbackEngine == null) {
 			
 			Class<?> [] classParm = null;
 			Object [] objectParm = null;
 			
 			try {
 				Class<?> cl = Class.forName(Const.PLAYBACK_ENGINE);
 				java.lang.reflect.Constructor<?> co = cl.getConstructor(classParm);
 				playbackEngine = (IPlaybackEngine) co.newInstance(objectParm);
 				
 			} catch (Exception e) {
 				throw new ImplException(e);
 			}
 			
 		}
 		
 		return playbackEngine;
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Playback management.
 	
 	public void loadAndStartPlaying (MediaTrack track) {
 		currentTrack = track;
 		
 		try {
 			getPlaybackEngine().stopPlaying();
 			getPlaybackEngine().unloadFile();
 			
 			getPlaybackEngine().setFile(currentTrack.getFilepath());
 			getPlaybackEngine().setOnfinishHandler(atEndOfTrack);
 			
 			getPlaybackEngine().startPlaying();
 			
 		} catch (PlaybackException e) {
 			currentTrack = null;
 			e.printStackTrace();
 			new MorriganMsgDlg(e).open();
 		}
 		
 		updateStatus();
 	}
 	
 	public void startPlaying () {
 		try {
 			getPlaybackEngine().startPlaying();
 		} catch (PlaybackException e) {
 			new MorriganMsgDlg(e).open();
 		}
 	}
 	
 	public void stopPlaying () {
 		try {
 			getPlaybackEngine().stopPlaying();
 		} catch (PlaybackException e) {
 			new MorriganMsgDlg(e).open();
 		}
 	}
 	
 	private Runnable atEndOfTrack = new Runnable() {
 		@Override
 		public void run() {
			currentTrack = null;
			getSite().getShell().getDisplay().asyncExec(updateStatusRunable);
		}
	};
	
	private Runnable updateStatusRunable = new Runnable() {
		public void run() {
			updateStatus();
 		}
 	};
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 //	Local helper methods.
 	
 	public void updateStatus () {
 		if (currentTrack != null) {
 			mainLabel.setText("Now playing: " + currentTrack.toString());
 			
 		} else {
 			mainLabel.setText("Idle.");
 		}
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
