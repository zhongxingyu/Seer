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
 		hostAddress += ":8080/ManETS_Server/#";
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
 
 			switch (resourceValues.c) {
 			case ADD:
 				manageAddRequest(response, parameterMap);
 				break;
 			case CLEAR:
 				manageClearRequest(response, parameterMap);
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
 				manageNextRequest(response, parameterMap);
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
 				manageOrderRequest(response, parameterMap);
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
 				managePlayPlayListRequest(response, parameterMap);
 				break;
 			case PREVIOUS:
 				managePreviousRequest(response, parameterMap);
 				break;
 			case REMOVE:
 				manageRemoveRequest(response, parameterMap);
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
 			Map<String, String[]> parameterMap) throws IOException {
 
 		mediaPlayer.playNext();
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
 
 			final Map<Integer, Media> list = new TreeMap<Integer, Media>();
 
 			int i = 0;
 			for (MediaListItem m : mediaList.items()) {
 				list.put(i++, createMedia(m));
 			}
 			serveurState.setPlaylist(list);
 		} else {
 
 		}
 
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.getWriter().write(
 				new ObjectMapper().writeValueAsString(serveurState));
 	}
 
 	private void manageSeekRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException {
 		if (parameterMap.containsKey("value")) {
 			int seek = Integer.parseInt(parameterMap.get("value")[0]);
 			headlessMediaPlayer.setPosition(seek);
 			response.setStatus(200);
 
 			// TODO fixe that shite
 		} else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 
 	private void manageRemoveRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) {
 		// TODO Auto-generated method stub
 		mediaPlayer.getMediaList().removeMedia(0);
 	}
 
 	private void managePreviousRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException {
 		mediaPlayer.playPrevious();
 	}
 
 	private void managePlayPlayListRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) {
 		if (parameterMap.containsKey("id")) {
 			String string = parameterMap.get("id")[0];
 
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
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		} else {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 
 		}
 
 	}
 
 	private void manageOrderRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) {
 		mediaPlayer.getMediaList().setStandardMediaOptions("--random");
 	}
 
 	private void manageClearRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) {
 
 		mediaPlayer.getMediaList().clear();
 		response.setStatus(200);
 
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
 
 			serveurState = new ServerState(media, listIdPlay,
 					headlessMediaPlayer.getVolume(),
 					headlessMediaPlayer.getPosition());
 
 			response.setStatus(HttpServletResponse.SC_OK);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(serveurState));
 		}
 
 		else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 
 	}
 
 	private Media createMedia(MediaListItem item) throws CannotReadException,
 			IOException, TagException, ReadOnlyFileException,
 			InvalidAudioFrameException {
 
 		final String realPath = item.mrl().substring(8).replaceAll("%20", " ");
		
		AudioFile f = AudioFileIO.read(new File(realPath));
 		Tag tag = f.getTag();
 
 		Media media = new Media(tag, realPath, f.getAudioHeader()
 				.getTrackLength());
 		return media;
 	}
 
 	private void manageAddRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException {
 
 		if (parameterMap.containsKey("path")) {
 
 			mediaPlayer.getMediaList().addMedia(
 					musicHome + parameterMap.get("path")[0]);
 
 			response.setStatus(200);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(playlists));
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
 			System.out.println(">>Path asked : " + musicHome);
 		}
 		// list all file from directory with file filtering
 		array = new File(path).listFiles(fileFilter);
 		if (array != null) {
 
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
