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
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.co.caprica.vlcj.binding.LibVlc;
 import uk.co.caprica.vlcj.medialist.MediaList;
 import uk.co.caprica.vlcj.player.MediaPlayerFactory;
 import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
 import uk.co.caprica.vlcj.player.list.MediaListPlayer;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 import ca.etsmtl.gti785.model.DashBoardFeed;
 import ca.etsmtl.gti785.model.DashBoardFeed.Settings;
 import ca.etsmtl.gti785.model.DataSource;
 import ca.etsmtl.gti785.model.Media;
 import ca.etsmtl.gti785.model.PlayList;
 import ca.etsmtl.gti785.model.RepertoireDefinition;
 import ca.etsmtl.gti785.model.ServeurState;
 
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
 	private ServeurState serveurState = new ServeurState();
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
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String servInfo = request.getServletPath();
 
 		System.out.println(">Path : " + servInfo);
 
 		response.setContentType("application/json");
 		if (servInfo.equals("/")) {
 
 			final DataSource m = new DataSource("list", "/list");
 			final DataSource v = new DataSource("playlist", "/playlist");
 
 			final ArrayList<DataSource> list = new ArrayList<DataSource>();
 			list.add(m);
 			list.add(v);
 			final DashBoardFeed feed = new DashBoardFeed(list, new Settings());
 
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
 				manageListRequest(response, parameterMap);
 				break;
 			case NEXT:
 				manageNextRequest(response, parameterMap);
 				break;
 			case OPEN:
 				manageOpenRequest(response, parameterMap);
 				break;
 			case ORDER:
 				manageOrderRequest(response, parameterMap);
 				break;
 			case PAUSE:
 				managePauseRequest(response, parameterMap);
 				break;
 			case PLAYLIST:
 				managePlaylistRequest(response, parameterMap);
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
 				manageStateRequest(response, parameterMap);
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
 		// System.out.println("do something in next" + listIdPlay);
 		// if (listIdPlay < playlists.paths.size() - 1) {
 		// listIdPlay++;
 		// mediaPlayer.stop();
 		// if (mediaPlayer.playMedia(playlists.paths.get(listIdPlay))) {
 		// response.setStatus(HttpServletResponse.SC_OK);
 		// Media media = new Media(mediaPlayer.getMediaMeta(),
 		// playlists.paths.get(listIdPlay));
 		// serveurState.setCurrentMedia(media);
 		// response.getWriter().write(
 		// new ObjectMapper().writeValueAsString(serveurState));
 		// }
 		// } else {
 		// response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		// response.sendError(HttpServletResponse.SC_NOT_FOUND);
 		// }
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
 			IOException {
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
 		// if (listIdPlay > 0) {
 		// listIdPlay--;
 		// mediaPlayer.stop();
 		// if (mediaPlayer.playMedia(playlists.paths.get(listIdPlay))) {
 		// response.setStatus(200);
 		// Media media = new Media(mediaPlayer.getMediaMeta(),
 		// playlists.paths.get(listIdPlay));
 		// serveurState.setCurrentMedia(media);
 		// response.getWriter().write(
 		// new ObjectMapper().writeValueAsString(serveurState));
 		//
 		// }
 		// } else {
 		// response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		// response.sendError(HttpServletResponse.SC_NOT_FOUND);
 		//
 		// }
 
 	}
 
 	private void managePlayPlayListRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void managePlaylistRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException {
 		response.getWriter().write(
 				new ObjectMapper().writeValueAsString(playlists));
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
 		playlists.randomise();
 	}
 
 	private void manageClearRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) {
 		playlists.paths.clear();
 		response.setStatus(200);
 	}
 
 	private void manageOpenRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws JsonProcessingException,
 			IOException {
 
 		listIdPlay = 0;
 		File[] array = null;
 		String path = musicHome;
 
 		if (parameterMap.containsKey("path")) {
 
 			path += parameterMap.get("path")[0];
			System.out.println(">>Params : " + path);
 
 			// mediaPlayer.stop();
 			//
 			// playlists.paths.clear();
 			//
 			File file = new File(path);
 			MediaList list = mediaPlayerFactory.newMediaList();
 			if (file.isDirectory()) {
 				array = file.listFiles(musicFilter);
 				for (int i = 0; i < array.length; i++) {
 					// playlists.paths.add(array[i].getAbsolutePath());
 					list.addMedia(array[i].getAbsolutePath());
 				}
 			} else {
 				list.addMedia(file.getAbsolutePath());
 			}
 			mediaPlayer.setMediaList(list);
 			mediaPlayer.play();
 			response.getWriter().write(HttpServletResponse.SC_OK);
 			// mediaPlayer.setPlaySubItems(true);
 			// Media media = new Media(headlessMediaPlayer.getMediaMeta(),
 			// playlists.paths.get(0));
 			//
 			// serveurState = new ServeurState(media, listIdPlay,
 			// headlessMediaPlayer.getVolume(),
 			// headlessMediaPlayer.getPosition());
 			// response.getWriter().write(
 			// new ObjectMapper().writeValueAsString(serveurState));
 			//
 			// try {
 			// if (array.length > 0) {
 			// if (mediaPlayer.playMedia(playlists.paths.get(0))) {
 			// response.setStatus(HttpServletResponse.SC_OK);
 			//
 			// for (int i = 1; i < playlists.paths.size(); i++) {
 			// mediaPlayer.playNextSubItem(playlists.paths.get(i));
 			// }
 			// System.out.println(">>Path asked : " + path);
 			//
 			// } else {
 			// response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			// }
 			// } else {
 			// response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			// response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			// }
 			// } catch (Exception e) {
 			// }
 			//
 			// if (mediaPlayer.playMedia(playlists.paths.get(0))) {
 			// response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			// response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			// }
 		}
 
 		else {
 		}
 
 	}
 
 	private void manageAddRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException {
 
 		if (parameterMap.containsKey("path")) {
 			playlists.paths.add(parameterMap.get("path")[0]);
 			response.setStatus(200);
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(playlists));
 		} else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 	}
 
 	private void manageListRequest(HttpServletResponse response,
 			Map<String, String[]> parameterMap) throws IOException,
 			JsonProcessingException {
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
 			for (int i = 0; i < array.length; i++) {
 				listRepertoire.add(new RepertoireDefinition(path, array[i]
 						.getName()));
 			}
 
 			response.getWriter().write(
 					new ObjectMapper().writeValueAsString(listRepertoire));
 		} else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		}
 		array = null;
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
