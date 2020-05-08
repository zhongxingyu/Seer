 package rogatkin.music_barrel.srv;
 
 import java.io.IOException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import mediautil.gen.MediaFormat;
 
 import org.aldan3.model.DataObject;
 import org.aldan3.model.Log;
 import org.aldan3.model.ServiceProvider;
 
 import com.beegman.webbee.util.AsyncUpdater;
 import com.beegman.webbee.model.UIEvent;
 
 import photoorganizer.formats.MediaFormatFactory;
 import photoorganizer.media.MediaPlayer;
 import photoorganizer.media.MediaPlayer.ProgressListener;
 import photoorganizer.media.MediaPlayer.Status;
 import rogatkin.music_barrel.model.MBModel;
 import rogatkin.music_barrel.model.PlayMode;
 
 // TODO all operations around media player have to be synchronized
 public class PlayerService implements ServiceProvider<PlayerService>, ProgressListener, Runnable {
 	public static final String NAME = "MediaPlayer";
 
 	public static String GET_LIST_Q = "select i.title, i.performer, track, year, l.title album, genre, path, i.id id, l.id list_id from mb_media_item i join mb_play_list_map m on m.item_id=i.id join mb_play_list l on l.id = m.list_id ";
 
 	MBModel appModel;
 	private MediaPlayer mediaPlayer;
 	Thread listPlayer;
 	LinkedBlockingQueue<String> playQueue;
 	PlayMode playMode;
 
 	public PlayerService(MBModel am) {
 		appModel = am;
 		playMode = PlayMode.once; // TODO take from config
 		playQueue = new LinkedBlockingQueue<>(1024);
 		listPlayer = new Thread(this, getPreferredServiceName());
 		listPlayer.setDaemon(true);
 		listPlayer.start();
 	}
 
 	@Override
 	public String getPreferredServiceName() {
 		return NAME;
 	}
 
 	@Override
 	public PlayerService getServiceProvider() {
 		return this;
 	}
 
 	public synchronized PlayerService play(URL media) {
 		// do head call to get content type
 		// resolve player
 		return getServiceProvider();
 	}
 
 	public synchronized PlayerService play(Path media) {
 		if (mediaPlayer != null) {
 			mediaPlayer.close();
 		}
 
 		mediaPlayer = MediaFormatFactory.getPlayer(MediaFormatFactory.createMediaFormat(media.toFile(),
 				appModel.getCharEncoding(), true));
 		//System.err.printf("Player %s for %s%n", mediaPlayer, media);
 		if (mediaPlayer == null)
 			return null;
 		mediaPlayer.setProgressListener(this);
 		mediaPlayer.start();
 		return getServiceProvider();
 	}
 
 	public PlayerService playList(int list_id, long start_item, int mode) {
 		try {
 			Collection<DataObject> list = appModel.getDOService().getObjectsByQuery(
 					GET_LIST_Q + " where l.id=" + list_id, 0, -1);
 			playMode = PlayMode.getMode(mode);
 			boolean add = start_item <= 0;
 			ArrayList<String> shuffle = null;
 			if (PlayMode.shuffle.equals(playMode))
 				shuffle = new ArrayList<>(list.size());
 			for (DataObject dob : list) {
 				if (add == false) {
 					long iid = (long) dob.get("ID");
 					add = start_item == iid;
 				}
 				if (add)
 					if (shuffle != null)
 						shuffle.add("" + dob.get("PATH"));
 					else
 						playQueue.add("" + dob.get("PATH"));
 			}
 			if (shuffle != null) {
 				Random random = new Random();
 				while (shuffle.size() > 0) {
 					//System.err.printf("--Shuffled size %d%n", shuffle.size());
 					playQueue.add(shuffle.remove(random.nextInt(shuffle.size())));
 				}
 			}
 		} catch (Exception e) {
 			Log.l.error(getPreferredServiceName(), e);
 		}
 		return getServiceProvider();
 	}
 
 	public PlayerService playItem(String path, long item_id) {
 		if (path == null || path.isEmpty()) {
 			try {
 				DataObject item = appModel.getDOService().getObjectByQuery(
 						"select path from mb_media_item where id=" + item_id, null);
 				if (item != null)
 					path = "" + item.get("PATH");
 				else
 					return null;
 			} catch (Exception e) {
 				Log.l.error(NAME, e);
 				return null;
 			}
 		}
 		playQueue.add(path);
 		return getServiceProvider();
 	}
 
 	public void removePlay(String path) {
 		if (playQueue.remove(path) == false) {
 			MediaFormat mf = getCurrentMedia();
 			try {
 				if (mf != null && Files.isSameFile(mf.getFile().toPath(), Paths.get(path))) {
 					stop();
 				}
 			} catch (IOException ioe) {
 
 			}
 		}
 	}
 
 	public String[] getPlayQueue() {
 		// TODO size can be changed, so actual result can be inaccurate
 		return playQueue.toArray(new String[playQueue.size()]);
 	}
 
 	public void pause() {
 		if (mediaPlayer != null)
 			mediaPlayer.pause();
 	}
 
 	public void stop() {
 		if (mediaPlayer != null)
 			mediaPlayer.close();
 	}
 
 	public void stopAll() {
 		playQueue.clear();
 		if (mediaPlayer != null)
 			mediaPlayer.close();
 	}
 
 	public void terminate() {
 		stopAll();
 		listPlayer.interrupt();
 	}
 
 	public void resume() {
 		//System.err.printf("Player %s resume %n", mediaPlayer);
 		if (mediaPlayer != null)
 			mediaPlayer.resume();
 	}
 
 	public Status getStatus() {
 		if (mediaPlayer != null)
 			return mediaPlayer.getStatus();
 		return Status.closed;
 	}
 
 	public long getPlaybackPosition() {
 		//if (mediaPlayer != null)
 		//System.err.println("pos Status:"+mediaPlayer.getStatus());
 		if (Status.playing.equals(getStatus()))
 			return mediaPlayer.getPosition();
 		return 0;
 	}
 
 	public MediaFormat getCurrentMedia() {
 		//if (mediaPlayer != null)
 		//System.err.println("curr Status:"+mediaPlayer.getStatus());
		//else
		//	System.err.println("Current player null");
 		if (mediaPlayer != null && !Status.stopped.equals(mediaPlayer.getStatus())
				&& !Status.closed.equals(mediaPlayer.getStatus()) && !Status.inerror.equals(mediaPlayer.getStatus()))
 			return mediaPlayer.getMedia();
 		return null;
 	}
 
 	//////// progress listener interface  ////
 
 	@Override
 	public void finished() {
 		AsyncUpdater updater = (AsyncUpdater) appModel.getService(AsyncUpdater.NAME);
 		UIEvent uie = new UIEvent();
 		uie.eventHandler = "songFinished";
 		uie.parameters = new Object[] { getStatus() };
 		updater.addEvent(appModel.getAppName(), uie);
 		//System.err.printf("event %s dropped for %s%n", uie, mediaPlayer);
 	}
 
 	@Override
 	public void setMaximum(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setValue(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	Path getNext() throws InterruptedException {
 		String p = playQueue.take();
 		switch (playMode) {
 		case repeat:
 			playQueue.add(p);
 		}
 		return Paths.get(p);
 	}
 
 	@Override
 	public void run() {
 		do {
 			try {
 				play(getNext());
 			} catch (InterruptedException ie) {
 				break;
 			} catch (Exception e) {
 				Log.l.error(getPreferredServiceName(), e);
 			}
 			if (mediaPlayer != null)
 				mediaPlayer.waitPlayEnds();
 		} while (true);
 		//System.err.println("Exited===========play thread");
 	}
 }
