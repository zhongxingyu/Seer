 
 import java.io.FileNotFoundException;
 import java.lang.IllegalArgumentException;
 
 import edu.umflix.authenticationhandler.AuthenticationHandler;
 import edu.umflix.authenticationhandler.exceptions.InvalidTokenException;
 import edu.umflix.clipstorage.ClipStorage;
 import edu.umflix.exceptions.MovieNotFoundException;
 import edu.umflix.exceptions.RoleNotFoundException;
 import edu.umflix.model.*;
 import edu.umflix.persistence.ActivityDao;
 import edu.umflix.persistence.AdDao;
 import edu.umflix.persistence.MovieDao;
 import edu.umflix.persistence.RoleDao;
 
 import model.exceptions.NoAdsException;
 import model.exceptions.UserNotAllowedException;
 import model.exceptions.ValuesInActivityException;
 import org.junit.Before;
 import org.junit.Test;
 import services.MovieManagerImpl;
 
 import java.util.ArrayList;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class MovieManagerImplTest {
 
     private MovieManagerImpl movieManager;
     private RoleDao roleDao;
     private ActivityDao activityDao;
     private MovieDao movieDao;
     private AuthenticationHandler authenticationHandler;
     private ClipStorage clipStorage;
     private AdDao adDao;
 
     @Before
     public void getMockManager() throws InvalidTokenException, RoleNotFoundException, MovieNotFoundException, FileNotFoundException {
         roleDao = mock(RoleDao.class);
         activityDao = mock(ActivityDao.class);
         movieDao = mock(MovieDao.class);
         authenticationHandler = mock(AuthenticationHandler.class);
         clipStorage = mock(ClipStorage.class);
         adDao = mock(AdDao.class);
 
         //prepare mock roleDao
         Role adminRole = mock(Role.class);
         when(adminRole.getId()).thenReturn((long) 1);
         Role userRole = mock(Role.class);
         when(userRole.getId()).thenReturn((long) 2);
         Role movieProvider = mock(Role.class);
         when(movieProvider.getId()).thenReturn((long) 3);
         Role adProvider = mock(Role.class);
         when(adProvider.getId()).thenReturn((long) 4);
         Role reviewer = mock(Role.class);
         when(reviewer.getId()).thenReturn((long) 5);
         when(roleDao.getRoleById(Role.RoleType.ADMINISTRATOR.getRole())).thenReturn(adminRole);
         when(roleDao.getRoleById(Role.RoleType.USER.getRole())).thenReturn(userRole);
         when(roleDao.getRoleById(Role.RoleType.MOVIE_PROVIDER.getRole())).thenReturn(movieProvider);
         when(roleDao.getRoleById(Role.RoleType.REVIEWER.getRole())).thenReturn(reviewer);
         when(roleDao.getRoleById(Role.RoleType.AD_PROVIDER.getRole())).thenReturn(adProvider);
 
         //prepare mock movieDao
         Movie enabledMovie = mock(Movie.class);
         Movie notEnabledMovie = mock(Movie.class);
         when(enabledMovie.isEnabled()).thenReturn(true);
         when(notEnabledMovie.isEnabled()).thenReturn(false);
         when(enabledMovie.getClips()).thenReturn(new ArrayList<Clip>());
         when(notEnabledMovie.getClips()).thenReturn(new ArrayList<Clip>());
         when(movieDao.getMovieById((long) 1)).thenReturn(enabledMovie);
         when(movieDao.getMovieById((long) 2)).thenReturn(notEnabledMovie);
         when(movieDao.getMovieById((long) 3)).thenThrow(MovieNotFoundException.class);
 
         //prepare mock authenticationHandler
         when(authenticationHandler.validateToken("validTokenAdmin")).thenReturn(true);
         when(authenticationHandler.validateToken("validTokenUser")).thenReturn(true);
         when(authenticationHandler.validateToken("validTokenMovieProvider")).thenReturn(true);
         when(authenticationHandler.validateToken("validTokenAdProvider")).thenReturn(true);
         when(authenticationHandler.validateToken("validTokenReviewer")).thenReturn(true);
         when(authenticationHandler.validateToken("expiredToken")).thenReturn(false);
         when(authenticationHandler.validateToken("invalidToken")).thenThrow(InvalidTokenException.class);
 
         when(authenticationHandler.isUserInRole("validTokenAdmin", mock(Role.class))).thenReturn(false);
         when(authenticationHandler.isUserInRole("validTokenAdmin", roleDao.getRoleById(Role.RoleType.ADMINISTRATOR.getRole()))).thenReturn(true);
         when(authenticationHandler.isUserInRole("validTokenUser", mock(Role.class))).thenReturn(false);
         when(authenticationHandler.isUserInRole("validTokenUser", roleDao.getRoleById(Role.RoleType.USER.getRole()))).thenReturn(true);
         when(authenticationHandler.isUserInRole("validTokenMovieProvider", mock(Role.class))).thenReturn(false);
         when(authenticationHandler.isUserInRole("validTokenMovieProvider", roleDao.getRoleById(Role.RoleType.MOVIE_PROVIDER.getRole()))).thenReturn(true);
         when(authenticationHandler.isUserInRole("validTokenAdProvider", mock(Role.class))).thenReturn(false);
         when(authenticationHandler.isUserInRole("validTokenAdProvider", roleDao.getRoleById(Role.RoleType.AD_PROVIDER.getRole()))).thenReturn(true);
         when(authenticationHandler.isUserInRole("validTokenReviewer", mock(Role.class))).thenReturn(false);
         when(authenticationHandler.isUserInRole("validTokenReviewer", roleDao.getRoleById(Role.RoleType.REVIEWER.getRole()))).thenReturn(true);
 
         User user = mock(User.class);
         User admin = mock(User.class);
         when(user.getEmail()).thenReturn("user@mail.com");
         when(admin.getEmail()).thenReturn("admin@mail.com");
         when(authenticationHandler.getUserOfToken("validTokenUser")).thenReturn(user);
         when(authenticationHandler.getUserOfToken("validTokenAdmin")).thenReturn(admin);
 
         //prepare mock clipStorage
         when(clipStorage.getClipDataByClipId((long) 1)).thenReturn(mock(ClipData.class));
         when(clipStorage.getClipDataByClipId((long) 2)).thenThrow(FileNotFoundException.class);
 
         movieManager = new MovieManagerImpl();
         movieManager.setActivityDao(activityDao);
         movieManager.setAuthenticationHandler(authenticationHandler);
         movieManager.setRoleDao(roleDao);
         movieManager.setMovieDao(movieDao);
         movieManager.setClipStorage(clipStorage);
         movieManager.setAdDao(adDao);
     }
 
     //getMovie tests
 
     @Test
     public void testValidUserToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenUser";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 1)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     @Test
     public void testValidAdminToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenAdmin";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 1)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     @Test
     public void testValidReviewerToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenReviewer";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 1)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     @Test
     public void testValidMovieProviderToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenMovieProvider";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 1)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     @Test
     public void testValidAdProviderToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenAdProvider";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 1)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     @Test(expected = InvalidTokenException.class)
     public void testInvalidUserToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "invalidToken";
         movieManager.getMovie(userToken, (long) 1);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testExpiredUserToken() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "expiredToken";
         movieManager.getMovie(userToken, (long) 1);
     }
 
     @Test(expected = MovieNotFoundException.class)
     public void testNonExistentMovie() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenUser";
         movieManager.getMovie(userToken, (long) 3);
     }
 
     @Test(expected = UserNotAllowedException.class)
     public void testUserNotAllowed() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenUser";
         movieManager.getMovie(userToken, (long) 2);
     }
 
     @Test(expected = UserNotAllowedException.class)
     public void testMovieProviderNotAllowed() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenMovieProvider";
         movieManager.getMovie(userToken, (long) 2);
     }
 
     @Test(expected = UserNotAllowedException.class)
     public void testAdProviderNotAllowed() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenAdProvider";
         movieManager.getMovie(userToken, (long) 2);
     }
 
     @Test
     public void testReviewerAllowed() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenReviewer";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 2)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     @Test
     public void testAdminAllowed() throws InvalidTokenException, IllegalArgumentException, MovieNotFoundException, UserNotAllowedException {
         String userToken = "validTokenAdmin";
         assertArrayEquals(((ArrayList<Clip>) movieManager.getMovie(userToken, (long) 2)).toArray(), new ArrayList<Clip>().toArray());
     }
 
     //getClipData tests
     @Test
     public void testSuccessfulGetClipData() throws FileNotFoundException, InvalidTokenException {
         String userToken = "validTokenUser";
         long clipId = (long) 1;
         movieManager.getClipData(userToken, clipId);
     }
 
     @Test(expected = FileNotFoundException.class)
     public void testUnsuccessfulGetClipData() throws FileNotFoundException, InvalidTokenException {
         String userToken = "validTokenUser";
         long clipId = (long) 2;
         movieManager.getClipData(userToken, clipId);
     }
 
     @Test(expected = InvalidTokenException.class)
     public void testUnsuccessfulGetClipDataBadToken() throws FileNotFoundException, InvalidTokenException {
         String userToken = "invalidToken";
         long clipId = (long) 1;
         movieManager.getClipData(userToken, clipId);
     }
 
     //sendActivity tests
     @Test
     public void testSuccessfulUserSendActivity() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenUser";
         Activity activity = mock(Activity.class);
 
         User user = mock(User.class);
         when(authenticationHandler.authenticate(user)).thenReturn("validTokenUser");
 
         when(user.getEmail()).thenReturn("user@mail.com");
         when(activity.getUser()).thenReturn(user);
        when(activity.getMovieId()).thenReturn((long) 1);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(1)).addActivity(activity);
     }
 
     @Test
     public void testSuccessfulAdminSendActivity() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenAdmin";
         Activity activity = mock(Activity.class);
 
         User admin = mock(User.class);
         when(authenticationHandler.authenticate(admin)).thenReturn("validTokenAdmin");
         when(admin.getEmail()).thenReturn("admin@mail.com");
         when(activity.getUser()).thenReturn(admin);
        when(activity.getMovieId()).thenReturn((long) 2);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(1)).addActivity(activity);
     }
 
     @Test(expected = ValuesInActivityException.class)
     public void testUnsuccessfulSendActivityDifferentUsers() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenUser";
         Activity activity = mock(Activity.class);
 
         User user = mock(User.class);
         when(user.getEmail()).thenReturn("anotherUser@mail.com");
         when(activity.getUser()).thenReturn(user);
        when(activity.getMovieId()).thenReturn((long) 1);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     @Test(expected = ValuesInActivityException.class)
     public void testUnsuccessfulSendActivityMovieNotSet() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenUser";
         Activity activity = mock(Activity.class);
        when(activity.getMovieId()).thenReturn(null);
 
         User user = mock(User.class);
         when(user.getEmail()).thenReturn("user@mail.com");
         when(activity.getUser()).thenReturn(user);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     @Test(expected = ValuesInActivityException.class)
     public void testUnsuccessfulSendActivityUserNotSet() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenUser";
         Activity activity = mock(Activity.class);
 
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     @Test(expected = UserNotAllowedException.class)
     public void testUnsuccessfulSendActivityUserNotAllowed() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenUser";
         Activity activity = mock(Activity.class);
 
         User user = mock(User.class);
         when(user.getEmail()).thenReturn("user@mail.com");
         when(activity.getUser()).thenReturn(user);
        when(activity.getMovieId()).thenReturn((long) 2);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     @Test(expected = InvalidTokenException.class)
     public void testUnsuccessfulSendActivityInvalidToken() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "invalidToken";
         Activity activity = mock(Activity.class);
 
         User user = mock(User.class);
         when(user.getEmail()).thenReturn("user@mail.com");
         when(activity.getUser()).thenReturn(user);
        when(activity.getMovieId()).thenReturn((long) 1);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testUnsuccessfulSendActivityExpiredToken() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "expiredToken";
         Activity activity = mock(Activity.class);
 
         User user = mock(User.class);
         when(user.getEmail()).thenReturn("user@mail.com");
         when(activity.getUser()).thenReturn(user);
        when(activity.getMovieId()).thenReturn((long) 1);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     @Test(expected = ValuesInActivityException.class)
     public void testUnsuccessfulSendActivityNonExistentMovie() throws UserNotAllowedException, InvalidTokenException, ValuesInActivityException {
         String userToken = "validTokenAdmin";
         Activity activity = mock(Activity.class);
 
         User user = mock(User.class);
         when(user.getEmail()).thenReturn("user@mail.com");
         when(activity.getUser()).thenReturn(user);
        when(activity.getMovieId()).thenReturn((long) 3);
         when(activity.getPosition()).thenReturn(1);
         when(activity.getTime()).thenReturn((long) 140);
         movieManager.sendActivity(userToken, activity);
         verify(activityDao, times(0)).addActivity(activity);
     }
 
     //getAd tests
     @Test(expected = NoAdsException.class)
     public void testUnsuccessfulGetAdZeroSizedList() throws NoAdsException, InvalidTokenException {
         String userToken = "validTokenUser";
         long movieId = 1;
         when(adDao.getAllAds()).thenReturn(new ArrayList<Ad>());
         movieManager.getAd(userToken, movieId);
     }
 
     @Test(expected = NoAdsException.class)
     public void testUnsuccessfulGetAdNullList() throws NoAdsException, InvalidTokenException {
         String userToken = "validTokenUser";
         long movieId = 1;
         when(adDao.getAllAds()).thenReturn(null);
         movieManager.getAd(userToken, movieId);
     }
 
     @Test(expected = InvalidTokenException.class)
     public void testUnsuccessfulGetAdBadToken() throws NoAdsException, InvalidTokenException {
         String userToken = "invalidToken";
         long movieId = 1;
         movieManager.getAd(userToken, movieId);
     }
 
     @Test
     public void testSuccessfulGetAd() throws NoAdsException, InvalidTokenException, FileNotFoundException {
         String userToken = "validTokenUser";
         long movieId = 1;
         Ad ad = mock(Ad.class);
         Clip clip = mock(Clip.class);
         when(ad.getClip()).thenReturn(clip);
         when(clip.getId()).thenReturn((long) 1);
         ArrayList<Ad> ads = new ArrayList<Ad>();
         ads.add(ad);
         when(adDao.getAllAds()).thenReturn(ads);
         assertEquals(clipStorage.getClipDataByClipId((long) 1), movieManager.getAd(userToken, movieId));
     }
 
     @Test(expected = NoAdsException.class)
     public void testUnsuccessfulGetAdFileNotFound() throws NoAdsException, InvalidTokenException {
         String userToken = "validTokenUser";
         long movieId = 1;
         Ad ad = mock(Ad.class);
         Clip clip = mock(Clip.class);
         when(ad.getClip()).thenReturn(clip);
         when(clip.getId()).thenReturn((long) 2);
         ArrayList<Ad> ads = new ArrayList<Ad>();
         ads.add(ad);
         when(adDao.getAllAds()).thenReturn(ads);
         movieManager.getAd(userToken, movieId);
     }
 }
