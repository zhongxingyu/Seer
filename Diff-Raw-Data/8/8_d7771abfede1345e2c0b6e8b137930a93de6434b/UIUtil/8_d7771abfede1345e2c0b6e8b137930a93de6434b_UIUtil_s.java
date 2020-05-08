 package ui.util;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 import models.User;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import play.Play;
 import play.mvc.Http.Context;
 import play.mvc.Http.Request;
 import scala.Option;
 import services.UserService;
 import util.binders.OptionStringBinder;
 import util.security.SessionUtil;
 import util.user.message.Messages;
 
 import com.google.common.base.Strings;
 
 public final class UIUtil {
 
 	private static final String ENCODING = "UTF-8";
 	private static final String DATE_SORT_FORMAT = "yyyyMMdd|HHmmss|SSS";
 	private static final int GRAVATAR_DEFAULT_SIZE = 25;
 
 	private static final String ROOT_URL = "application.base.url";
 	private static final String GRAVATAR_PICTURE_URL = "gravatar.picture.url";
 	private static final String DATE_FORMAT_PARAMETER = "date.format";
 	private static final String DATE_TIME_FORMAT_PARAMETER = "datetime.format";
 
 	private UIUtil() {
 		throw new AssertionError();
 	}
 
 	public static String formatDate(LocalDate date) {
 		DateTimeFormatter formatter;
 		String formatPattern = Messages.resolve(DATE_FORMAT_PARAMETER);
 		if (Strings.isNullOrEmpty(formatPattern)) {
 			formatter = DateTimeFormat.mediumDate();
 		} else {
 			formatter = DateTimeFormat.forPattern(formatPattern);
 		}
 		return formatter.print(date);
 	}
 
 	public static String formatDateTime(DateTime dateTime) {
 		DateTimeFormatter formatter;
 		String formatPattern = Messages.resolve(DATE_TIME_FORMAT_PARAMETER);
 		if (Strings.isNullOrEmpty(formatPattern)) {
 			formatter = DateTimeFormat.mediumDate();
 		} else {
 			formatter = DateTimeFormat.forPattern(formatPattern);
 		}
 		return formatter.print(dateTime);
 	}
 
 	public static String formatDateTimeSortable(DateTime dateTime) {
 		return DateTimeFormat.forPattern(DATE_SORT_FORMAT).print(dateTime);
 	}
 
 	public static String urlEncode(Request request) {
 		return urlEncode(request.uri());
 	}
 
 	public static Option<String> urlEncode(Option<String> url) {
 		if (url.isEmpty()) {
 			return url;
 		}
 		return Option.apply(urlEncode(url.get()));
 	}
 
 	public static String urlEncode(String url) {
 		try {
 			return URLEncoder.encode(url, ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			return "";
 		}
 	}
 
 	public static String fullUrlDecode(String encodedUrl) {
 		try {
 			return getFullUrl(URLDecoder.decode(encodedUrl, ENCODING));
 		} catch (UnsupportedEncodingException e) {
 			return "";
 		}
 	}
 
 	public static String getFullUrl(String relativeUrl) {
 		String rootUrl = Play.application().configuration().getString(ROOT_URL);
 		if (rootUrl.endsWith("/") && relativeUrl.startsWith("/")) {
 			return rootUrl + relativeUrl.substring(1);
 		}
 		return rootUrl + relativeUrl;
 	}
 
 	public static OptionStringBinder currentPath() {
 		String url = urlEncode(Context.current().request());
 		return OptionStringBinder.create(Option.apply(url));
 	}
 
 	public static boolean isRegisteredUser() {
 		return SessionUtil.isAuthenticated();
 	}
 
 	public static String registeredUserDisplayName() {
 		if (!SessionUtil.isAuthenticated()) {
 			throw new IllegalStateException("No current user");
 		}
 		return SessionUtil.currentUser().get().getDisplay();
 	}
 
 	public static Long registeredUserId() {
 		if (!SessionUtil.isAuthenticated()) {
 			throw new IllegalStateException("No current user");
 		}
 		return SessionUtil.currentUser().get().id;
 	}
 
 	public static boolean isCurrentUser(User user) {
 		if (user == null) {
 			return false;
 		}
 		if (!SessionUtil.isAuthenticated()) {
 			return false;
 		}
 		return SessionUtil.currentUser().get().id.equals(user.id);
 	}
 
 	public static boolean displayGravatar() {
 		if (!SessionUtil.isAuthenticated()) {
 			return false;
 		}
 		if (SessionUtil.currentUser().isEmpty()) {
 			return false;
 		}
 		return !Strings.isNullOrEmpty(SessionUtil.currentUser().get().email);
 	}
 
 	public static String getGravatarUrl(Long userId, int size) {
 		if (userId == null) {
 			return null;
 		}
 		Option<User> optUser = UserService.getUser(userId);
 		return getGravatarUrl(optUser, size);
 	}
 
 	public static String getGravatarUrl() {
 		return getGravatarUrl(SessionUtil.currentUser(), GRAVATAR_DEFAULT_SIZE);
 	}
 
 	public static String getGravatarUrl(Option<User> optUser, int size) {
 		if (optUser.isEmpty()) {
 			return null;
 		}
 
 		User user = optUser.get();
 		String email = Strings.isNullOrEmpty(user.avatarEmail) ? user.email
 				: user.avatarEmail;
		return Strings.isNullOrEmpty(email) ? null : String.format(Play
				.application().configuration().getString(GRAVATAR_PICTURE_URL),
				MD5HexUtil.md5Hex(email), size);
 	}
 }
