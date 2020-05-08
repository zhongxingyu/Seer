 package ca.etsmtl.gti785;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.management.ObjectInstance;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.audio.exceptions.CannotReadException;
 import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
 import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
 import org.jaudiotagger.tag.Tag;
 import org.jaudiotagger.tag.TagException;
 
 import uk.co.caprica.vlcj.binding.LibVlc;
 import uk.co.caprica.vlcj.medialist.MediaList;
 import uk.co.caprica.vlcj.medialist.MediaListItem;
 import uk.co.caprica.vlcj.player.MediaPlayerFactory;
 import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
 import uk.co.caprica.vlcj.player.list.MediaListPlayer;
 import uk.co.caprica.vlcj.player.list.MediaListPlayerMode;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 import ca.etsmtl.gti785.model.DashBoardFeed;
 import ca.etsmtl.gti785.model.DataSource;
 import ca.etsmtl.gti785.model.ListReponse;
 import ca.etsmtl.gti785.model.Media;
 import ca.etsmtl.gti785.model.PlayList;
 import ca.etsmtl.gti785.model.RepertoireDefinition;
 import ca.etsmtl.gti785.model.ServerState;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 
 /**
  * Servlet implementation class ServletManETS
  */
 public class ServletManETS extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static String hostAddress;
 	private static String userHome;
 	private static String musicHome;
 	private static final String extensions[] = new String[] { "mp3", "flac",
 			"mp4" };
 
 	private static final FileFilter fileFilter = new FileFilter() {
 
 		public boolean accept(File file) {
 			if (file.isDirectory()) {
 				return true;
 			} else {
 				String path = file.getAbsolutePath().toLowerCase();
 				for (int i = 0, n = extensions.length; i < n; i++) {
 					String extension = extensions[i];
 					if ((path.endsWith(extension) && (path.charAt(path.length()
 							- extension.length() - 1)) == '.')) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 	};
 
 	private static final FileFilter musicFilter = new FileFilter() {
 
 		public boolean accept(File file) {
 			String path = file.getAbsolutePath().toLowerCase();
 			System.out.println("FileFilter path =" + path);
 			for (int i = 0, n = extensions.length; i < n; i++) {
 				String extension = extensions[i];
 				System.out.println("FileFilter Extension=" + extension);
 				if ((path.endsWith(extension) && (path.charAt(path.length()
 						- extension.length() - 1)) == '.')) {
 					return true;
 				}
 			}
 			return false;
 		}
 	};
 
 	// EXECUTED AT START, ONLY ONE TIME
 	static {
 		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(),
 				"C:\\Program Files\\VideoLAN\\VLC");
 		System.out.println("C:\\Program Files\\VideoLAN\\VLC");
 
 		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
 		System.out.println("Native Lib loaded");
 
 		userHome = System.getProperty("user.home");
 		musicHome = checkMusicHomeExist();
 
 		System.out.println("============================");
 		System.out.println("Server System info :");
 		System.out.println(System.getProperty("os.name"));
 		System.out.println(System.getProperty("os.arch"));
 		System.out.println(System.getProperty("os.version"));
 		System.out.println("User Home folder : " + userHome);
 		System.out.println("============================");
 		InetAddress IP = null;
 		try {
 			IP = InetAddress.getLocalHost();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 
 		hostAddress = "http://" + IP.getHostAddress();
 		hostAddress += ":8080/";
 		System.out.println("Adress of my system is : " + hostAddress);
 		System.out.println("============================");
 	}
 
 	// private MediaManager mediaManager;
 	private PlayList playlists = new PlayList();
 	private int listIdPlay = 0;
 	private ServerState serveurState = new ServerState();
 	private MediaListPlayer mediaPlayer;
 	private HeadlessMediaPlayer headlessMediaPlayer;
 	private MediaPlayerFactory mediaPlayerFactory;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ServletManETS() {
 		super();
 
 		mediaPlayerFactory = new MediaPlayerFactory();
 		headlessMediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
 
 		mediaPlayer = mediaPlayerFactory.newMediaListPlayer();
 
 		mediaPlayer.setMediaPlayer(headlessMediaPlayer);
 		mediaPlayer.setMode(MediaListPlayerMode.DEFAULT);
 
 	}
 
 	private static String checkMusicHomeExist() {
 		return new File(userHome + "\\Music").exists() ? userHome + "\\Music"
 				: "false";
 	}
 
 	// ////////////////////////////////////////////////////
 	//
 	// HTTP MANAGEMENT
 	//
 	// ////////////////////////////////////////////////////
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws JsonProcessingException,
 			IOException {
 
 		String servInfo = request.getServletPath();
 
 		System.out.println(">Path : " + servInfo);
 
 		response.setContentType("application/json");
 		if (servInfo.equals("/")) {
 
 			final DataSource m = new DataSource("list", "/list");
 			final DataSource v = new DataSource("playlist", "/playlist");
 
 			final ArrayList<DataSource> list = new ArrayList<DataSource>();
 			list.add(m);
 			list.add(v);
 			final DashBoardFeed feed = new DashBoardFeed(list);
 
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(feed));
 
 		} else {
 
 			final PrintWriter out = response.getWriter();
 
 			final Map<String, String[]> parameterMap = request
 					.getParameterMap();
 
 			final RestRequest resourceValues = new RestRequest(servInfo);
 			System.out.println(">REST : " + resourceValues.c.toString());
 			switch (resourceValues.c) {
 			case ADD:
 				try {
 					manageAddRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case CLEAR:
 				try {
 					manageClearRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case LIST:
 				try {
 					manageListRequest(response, parameterMap);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case NEXT:
 				try {
 					manageNextRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case OPEN:
 				try {
 					manageOpenRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case ORDER:
 				try {
 					manageOrderRequest(response, parameterMap);
 				} catch (Exception e) {
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 					e.printStackTrace();
 				}
 				break;
 			case PAUSE:
 				managePauseRequest(response, parameterMap);
 				break;
 			case PLAYLIST:
 				try {
 					managePlaylistRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case PLAYPLAYLIST:
 				try {
 					managePlayPlayListRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case PREVIOUS:
 				try {
 					managePreviousRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case REMOVE:
 				try {
 					manageRemoveRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case SEEK:
 				manageSeekRequest(response, parameterMap);
 				break;
 			case STATE:
 				try {
 					manageStateRequest(response, parameterMap);
 				} catch (Exception e) {
 					e.printStackTrace();
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				break;
 			case STOP:
 				manageStopRequest(response, parameterMap);
 				break;
 			case VOLUME:
 				manageVolumeRequest(response, parameterMap);
 				break;
 			case NONE:
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				break;
 			default:
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 				break;
 			}
 			out.close();
 			out.flush();
 		}
 	}
 
 	private void manageNextRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
 
 		
 		if(listIdPlay<mediaPlayer.getMediaList().size()-1){
 			listIdPlay++;
 			mediaPlayer.playNext();
 			MediaList mediaList = mediaPlayer.getMediaList();
 			Media media = createMedia(mediaList.items().get(listIdPlay));
 			serveurState = new ServerState(media, listIdPlay,
 					headlessMediaPlayer.getVolume(),
 					headlessMediaPlayer.getPosition());
 
 			serveurState.setPlaylist(getPlayListDef(mediaList));
 			response.setStatus(HttpServletResponse.SC_OK);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		}else{
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND );
 			response.sendError(HttpServletResponse.SC_NOT_FOUND );
 		}
 	}
 
 	private void manageVolumeRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException {
 
 		int volume;
 		if (parameterMap.containsKey("value")) {
 			volume = Integer.parseInt(parameterMap.get("value")[0]);
 			System.out.println("volume = " + volume);
 			if (volume >= 0 && volume < 201) {
 				headlessMediaPlayer.setVolume(volume);
 				response.setStatus(HttpServletResponse.SC_OK);
 				serveurState.setVolume(volume);
 				serveurState.setCurrentPosition(headlessMediaPlayer
 						.getPosition());
 				response.getWriter().write(
 						new ObjectMapper().writeValueAsString(serveurState));
 
 			} else {
 				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			}
 		} else {
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 
 	}
 
 	private void manageStopRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException {
 		if (headlessMediaPlayer.canPause()) {
 			mediaPlayer.stop();
 			response.setStatus(HttpServletResponse.SC_OK);
 			serveurState.setCurrentMedia(null);
 			serveurState.setCurrentPosition(0);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		} else {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	private void manageStateRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException, CannotReadException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 		
 		serveurState.setCurrentPosition(headlessMediaPlayer.getPosition()
 				* headlessMediaPlayer.getLength());
 		serveurState.setVolume(headlessMediaPlayer.getVolume());
 
 		final MediaList mediaList = mediaPlayer.getMediaList();
 		if (mediaList != null) {
 			serveurState.setCurrentID(listIdPlay);
 			final Map<Integer, Media> list = new TreeMap<Integer, Media>();
 
 			int i = 0;
 			for (MediaListItem m : mediaList.items()) {
 				list.put(i++, createMedia(m));
 			}
 			serveurState.setPlaylist(list);
 			Media media = createMedia(mediaList.items().get(listIdPlay));
 			serveurState.setCurrentMedia(media);
 		} else {
 		
 		}
 
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.getWriter().write(
 				new ObjectMapper().writeValueAsString(serveurState));
 	}
 
 	// Time position in second
 	private void manageSeekRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException {
 		if (headlessMediaPlayer.isPlaying()) {
 			if (parameterMap.containsKey("value")) {
 				int positionTime = Integer
 						.parseInt(parameterMap.get("value")[0]);
 				long duration = headlessMediaPlayer.getLength();
 				float positionSeek = (float) ((positionTime * 1.0) / (duration / 1000));
 
 				if(positionSeek<1){
 					headlessMediaPlayer.setPosition(positionSeek);
 					response.setStatus(200);
 					serveurState.setCurrentPosition(positionSeek);
 					response.getWriter().write(new ObjectMapper().writeValueAsString(serveurState));
 				}else{
 					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 				}
 		
 			} else {
 				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			}
 		}else{
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	private void manageRemoveRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException,
 			CannotReadException, TagException, ReadOnlyFileException,
 			InvalidAudioFrameException {
 	
 		if(mediaPlayer.getMediaList()!=null){
 			if(parameterMap.containsKey("id")){
 				int id  = Integer.parseInt(parameterMap.get("id")[0]);
 				if( id>=0 && id<mediaPlayer.getMediaList().size()){
 					mediaPlayer.getMediaList().removeMedia(id);
 					response.setStatus(HttpServletResponse.SC_OK);
 					
 					response.getWriter().write(new ObjectMapper().writeValueAsString(getPlayListDef(mediaPlayer.getMediaList())));
 				}			
 			}else{
 				// TODO return playListDenition
 			}
 		}else{
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 		
 	}
 
 	private void managePreviousRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
 
 		if(listIdPlay>0){
 			System.out.println("previous listIdPlay="+listIdPlay);
 			listIdPlay--;
 			mediaPlayer.playPrevious();
 	
 			MediaList mediaList = mediaPlayer.getMediaList();
 			Media media = createMedia(mediaList.items().get(listIdPlay));
 			serveurState = new ServerState(media, listIdPlay,
 					headlessMediaPlayer.getVolume(),
 					headlessMediaPlayer.getPosition());
 			serveurState.setPlaylist(getPlayListDef(mediaList));
 			response.setStatus(HttpServletResponse.SC_OK);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		}else{
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND );
 			response.sendError(HttpServletResponse.SC_NOT_FOUND );
 		}
 		
 		
 	}
 
 	private void managePlayPlayListRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
 		int mediaToPlay=-1;
 		
 		mediaPlayer.stop();
 		MediaList mediaList = mediaPlayer.getMediaList();
 		if(mediaList!=null){
 			
 			if (parameterMap.containsKey("id")) {
 				mediaToPlay = Integer.parseInt(parameterMap.get("id")[0]);
 			}
 			if(mediaToPlay!=-1){
 				if(mediaToPlay<mediaList.size()){
 					listIdPlay = mediaToPlay;
 					mediaPlayer.playItem(listIdPlay);
 				}else{
 					response.setStatus(HttpServletResponse.SC_NOT_FOUND );
 					response.sendError(HttpServletResponse.SC_NOT_FOUND );
 				}
 			}else{
 				listIdPlay = 0;
 				mediaPlayer.playItem(listIdPlay);
 			}
 			final Map<Integer, Media> list = new TreeMap<Integer, Media>();
 			int i = 0;
 			for (MediaListItem m : mediaList.items()) {
 				list.put(i++, createMedia(m));
 			}
 
 			response.setStatus(HttpServletResponse.SC_OK);
 						
 			Media media = createMedia(mediaList.items().get(listIdPlay));
 			serveurState = new ServerState(media, listIdPlay,
 					headlessMediaPlayer.getVolume(),
 					headlessMediaPlayer.getPosition());
 			serveurState.setPlaylist(list);
 
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 			
 		}else{
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND );
 			response.sendError(HttpServletResponse.SC_NOT_FOUND );
 		}
 		
 		
 		
 		
 	}
 
 	private void managePlaylistRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException, CannotReadException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 
 		final MediaList mediaList = mediaPlayer.getMediaList();
 		if (mediaList != null) {
 
 			final Map<Integer, Media> list = new TreeMap<Integer, Media>();
 
 			int i = 0;
 			for (MediaListItem m : mediaList.items()) {
 				list.put(i++, createMedia(m));
 			}
 
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(list));
 		} else {
 			response.getWriter().write(
 					new ObjectMapper()
 							.writeValueAsString(new TreeMap<Integer, Media>()));
 		}
 	}
 
 	private void managePauseRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException {
 		if (headlessMediaPlayer.canPause()) {
 			mediaPlayer.pause();
 			response.setStatus(HttpServletResponse.SC_OK);
 			if (mediaPlayer.isPlaying()) {
				serveurState.setPause(false);
			} else {
 				serveurState.setPause(true);
 			}
 			serveurState.setCurrentPosition(headlessMediaPlayer.getPosition());
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		} else {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 
 		}
 
 	}
 
 	private void manageOrderRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException, CannotReadException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 
 		if (parameterMap.containsKey("id")) {
 			if (parameterMap.containsKey("move")) {
 				int id = Integer.parseInt(parameterMap.get("id")[0]);
 				String move = parameterMap.get("move")[0];
 				MediaList mediaList = mediaPlayer.getMediaList();
 				List<MediaListItem> listMedia = new ArrayList<MediaListItem>();
 				MediaListItem tempo;
 				for (MediaListItem m : mediaList.items()) {
 					listMedia.add(m);
 				}
 				if (move.equals("up") || move.equals("UP")) {
 					System.out.println("Order");
 					if (id - 1 > 0) {
 						tempo = listMedia.get(id - 1);
 						listMedia.set(id - 1, listMedia.get(id));
 						listMedia.set(id, tempo);
 					} else {
 						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 						response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 					}
 				} else if (move.equals("down") || move.equals("DOWN")) {
 					if (id + 1 > listMedia.size() - 1) {
 						tempo = listMedia.get(id + 1);
 						listMedia.set(id + 1, listMedia.get(id));
 						listMedia.set(id, tempo);
 					} else {
 						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 						response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 					}
 				} else {
 					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 					return;
 				}
 
 				mediaList.clear();
 				for (MediaListItem mediaTri : listMedia) {
 					mediaList.addMedia(mediaTri.mrl());
 				}
 				mediaPlayer.setMediaList(mediaList);
 				response.setStatus(200);
 				response.getWriter().write(
 						new ObjectMapper()
 								.writeValueAsString(getPlayListDef(mediaPlayer
 										.getMediaList())));
 			}
 		} else {
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 
 	private void manageClearRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException, CannotReadException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 
 		mediaPlayer.getMediaList().clear();
 		response.setStatus(200);
 		response.getWriter().write(
 				new ObjectMapper()
 						.writeValueAsString(getPlayListDef(mediaPlayer
 								.getMediaList())));
 
 	}
 
 	private void manageOpenRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException, CannotReadException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 
 		listIdPlay = 0;
 		File[] array = null;
 		String path = musicHome;
 
 		if (parameterMap.containsKey("path")) {
 
 			path += parameterMap.get("path")[0];
 			System.out.println(">>Params : " + path);
 
 			File file = new File(path);
 			MediaList list = mediaPlayerFactory.newMediaList();
 			if (file.isDirectory()) {
 				array = file.listFiles(musicFilter);
 				for (int i = 0; i < array.length; i++) {
 					list.addMedia(array[i].getAbsolutePath());
 				}
 			} else {
 				list.addMedia(file.getAbsolutePath());
 			}
 
 			mediaPlayer.stop();
 			mediaPlayer.setMediaList(list);
 			mediaPlayer.play();
 
 			Media media = createMedia(list.items().get(0));
 
 			final Map<Integer, Media> listToServer = new TreeMap<Integer, Media>();
 			int i = 0;
 			for (MediaListItem m : mediaPlayer.getMediaList().items()) {
 				listToServer.put(i++, createMedia(m));
 			}
 
 			serveurState = new ServerState(media, listIdPlay,
 					headlessMediaPlayer.getVolume(),
 					headlessMediaPlayer.getPosition());
 
 			serveurState.setPlaylist(listToServer);
 			response.setStatus(HttpServletResponse.SC_OK);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		}
 
 		else {
 			// code 400
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 
 	}
 
 	private Media createMedia(MediaListItem item) throws CannotReadException,
 			IOException, TagException, ReadOnlyFileException,
 			InvalidAudioFrameException {
 		
 		String realPath = item.mrl().substring(8).replaceAll("%20", " ");
 		realPath = realPath.replaceAll("%27", "'");
 		realPath = realPath.replaceAll("%28", "(");
 		realPath = realPath.replaceAll("%29", ")");
 		realPath = realPath.replaceAll("%E8", "");
 		realPath = realPath.replaceAll("%EF", "");
 		realPath = realPath.replaceAll("%26", "&");
 		realPath = realPath.replaceAll("%5B", "[");
 		realPath = realPath.replaceAll("%5D", "]");
 
 		AudioFile f = AudioFileIO.read(new File(realPath));
 		Tag tag = f.getTag();
 
 		Media media = new Media(tag, realPath, f.getAudioHeader()
 				.getTrackLength());
 		return media;
 	}
 
 	private void manageAddRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException,
 			CannotReadException, TagException, ReadOnlyFileException,
 			InvalidAudioFrameException {
 
 		if (parameterMap.containsKey("path")) {
 
 			String path = musicHome + parameterMap.get("path")[0];
 			mediaPlayer.getMediaList().addMedia(path);
 
 			response.setStatus(200);
 			response.getWriter().write(
 					new ObjectMapper()
 							.writeValueAsString(getPlayListDef(mediaPlayer
 									.getMediaList())));
 		} else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 
 	private void manageListRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException,
 			JsonProcessingException, CannotReadException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 		File[] array;
 		String path = musicHome;
 		// add path param if key=rep is present
 		if (parameterMap.containsKey("rep")) {
 
 			path += parameterMap.get("rep")[0];
 			System.out.println(">>Path asked : " + path);
 		}
 		// list all file from directory with file filtering
 		array = new File(path).listFiles(fileFilter);
 		if (array != null) {
 			ListReponse r =createListReponse(array,path );
 
 			response.setStatus(200);
 			response.getWriter()
 					.write(new ObjectMapper().writeValueAsString(r));
 		} else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 		array = null;
 	}
 
 	private Media createMedia(File file) throws CannotReadException,
 			IOException, TagException, ReadOnlyFileException,
 			InvalidAudioFrameException {
 
 		AudioFile f = AudioFileIO.read(file);
 		Tag tag = f.getTag();
 
 		Media media = new Media(tag, new File(musicHome).toPath()
 				.relativize(file.toPath()).toString(), f.getAudioHeader()
 				.getTrackLength());
 
 		return media;
 	}
 	
 	private ListReponse createListReponse(File[] array, String path) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{
 		List<RepertoireDefinition> listRepertoire = new ArrayList<RepertoireDefinition>();
 		List<Media> listMedia = new ArrayList<Media>();
 		for (int i = 0; i < array.length; i++) {
 
 			if (array[i].isDirectory()) {
 
 				listRepertoire.add(new RepertoireDefinition(path
 						.substring(musicHome.length())
 						+ "\\"
 						+ array[i].getName(), array[i].getName()));
 			} else {
 				listMedia.add(createMedia(array[i]));
 			}
 		}
 		ListReponse r = new ListReponse(listRepertoire, listMedia);
 		return r;
 	}
 
 	private Map<Integer, Media> getPlayListDef(MediaList mediaList)
 			throws CannotReadException, IOException, TagException,
 			ReadOnlyFileException, InvalidAudioFrameException {
 		final Map<Integer, Media> list = new TreeMap<Integer, Media>();
 		int i = 0;
 		for (MediaListItem m : mediaList.items()) {
 			list.put(i++, createMedia(m));
 		}
 		return list;
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		PrintWriter out = response.getWriter();
 		out.println("[1,2,3,4,5,6,7,8,9,10]");
 		out.flush();
 	}
 
 	@Override
 	public void destroy() {
 		super.destroy();
 		mediaPlayerFactory.release();
 		headlessMediaPlayer.release();
 		mediaPlayer.release();
 	}
 }
