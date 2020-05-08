 package com.dedaulus.cinematty;
 
 import android.app.Application;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import com.dedaulus.cinematty.activities.StartupActivity;
 import com.dedaulus.cinematty.framework.*;
 import com.dedaulus.cinematty.framework.tools.*;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.*;
 import java.util.*;
 
 /*
  * User: Dedaulus
  * Date: 13.03.11
  * Time: 21:41
  */
 public class CinemattyApplication extends Application {
     private UniqueSortedList<Cinema> mCinemas;
     private UniqueSortedList<Movie> mMovies;
     private UniqueSortedList<MovieActor> mActors;
     private UniqueSortedList<MovieGenre> mGenres;
     private List<MoviePoster> mPosters;
 
     private int mCurrentDay = Constants.TODAY_SCHEDULE;
 
     private City mCurrentCity = null;
 
     private boolean mLocationListenStarted = false;
     private List<LocationClient> mLocationClients;
     private final LocationListener mLocationListener = new LocationListener() {
         public void onLocationChanged(Location location) {
             updateCurrentLocation(location);
         }
 
         public void onStatusChanged(String s, int i, Bundle bundle) {}
 
         public void onProviderEnabled(String s) {}
 
         public void onProviderDisabled(String s) {}
     };
 
     private Location mCurrentLocation;
 
    private HashMap<String, ActivityState> mState;
 
     private PictureRetriever mPictureRetriever = null;
     private static final String LOCAL_PICTURES_FOLDER = "pictures";
 
     private static final String FAV_CINEMAS_FILE = "cinematty_fav_cinemas";
     private static final String FAV_ACTORS_FILE = "cinematty_fav_actors";
     private static final String PREFERENCES_FILE = "cinematty_preferences";
     private static final String PREF_CINEMA_SORT_ORDER = "cinema_sort_order";
     private static final String PREF_MOVIE_SORT_ORDER = "movie_sort_order";
     private static final String PREF_CURRENT_CITY = "current_city";
     private static final String DUMP_FILE = "dump_state.xml";
 
     private boolean mIsDataActual = false;
 
     {
         mCinemas = new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>());
         mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
         mActors = new UniqueSortedList<MovieActor>(new DefaultComparator<MovieActor>());
         mGenres = new UniqueSortedList<MovieGenre>(new DefaultComparator<MovieGenre>());
         mPosters = new ArrayList<MoviePoster>();
         mLocationClients = new ArrayList<LocationClient>();
     }
 
     public boolean isDataActual() {
         return mIsDataActual;
     }
 
     public void dumpData() {
         StringBuilder xml = new StringBuilder();
         Calendar date = Calendar.getInstance();
         xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><data date=\"");
 
         String year = Integer.toString(date.get(Calendar.YEAR));
         String month = Integer.toString(date.get(Calendar.MONTH));
         String day = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
         String hour = Integer.toString(date.get(Calendar.HOUR_OF_DAY));
         String min = Integer.toString(date.get(Calendar.MINUTE));
         String sec = Integer.toString(date.get(Calendar.SECOND));
         xml.append(year).append(".").append(month).append(".").append(day).append(".").append(hour).append(".").append(min).append(".").append(sec);
         xml.append("\">");
 
         for (String cookie : mState.keySet()) {
             xml.append("<state cookie=\"").append(cookie).append("\"");
             ActivityState state = mState.get(cookie);
             String cinema = state.cinema != null ? state.cinema.getCaption() : null;
             String movie = state.movie != null ? state.movie.getCaption() : null;
             String actor = state.actor != null ? state.actor.getActor() : null;
             String genre = state.genre != null ? state.genre.getGenre() : null;
 
             xml.append(" type=\"").append(state.activityType).append("\"");
             if (cinema != null) xml.append(" cinema=\"").append(cinema).append("\"");
             if (movie != null) xml.append(" movie=\"").append(movie).append("\"");
             if (actor != null) xml.append(" actor=\"").append(actor).append("\"");
             if (genre != null) xml.append(" genre=\"").append(genre).append("\"");
             xml.append(" />");
         }
         xml.append("</data>");
 
         try {
             Writer output = new BufferedWriter(new FileWriter(new File(getCacheDir(), DUMP_FILE)));
             output.write(xml.toString());
             output.close();
         } catch (IOException e){}
     }
 
     private boolean restoreData() {
         StateReceiver receiver = new StateReceiver(this, DUMP_FILE);
         mState = receiver.getState();
         return mState != null;
     }
 
     public void restart() {
         if (mState != null) mState.clear();
         Intent intent = new Intent(this, StartupActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(intent);
     }
 
     public boolean retrieveData(boolean useLocalOnly) throws IOException, ParserConfigurationException, SAXException {
         ScheduleReceiver receiver = new ScheduleReceiver(this, getCurrentCity().getFileName());
         StringBuffer pictureFolder = new StringBuffer();
         if (!receiver.getSchedule(mCinemas, mMovies, mActors, mGenres, pictureFolder, mPosters, useLocalOnly)) return false;
 
         String remotePictureFolder = getString(R.string.settings_url) + "/" + pictureFolder.toString();
         if (mPictureRetriever == null) {
             mPictureRetriever = new HttpPictureRetriever(this, remotePictureFolder, LOCAL_PICTURES_FOLDER);
         } else {
             mPictureRetriever.setRemotePictureFolder(remotePictureFolder);
         }
 
         Map<String, ?> favs = getFavouriteCinemas();
         for (String caption : favs.keySet()) {
             int cinemaId = mCinemas.indexOf(new Cinema(caption));
             if (cinemaId != -1) {
                 mCinemas.get(cinemaId).setFavourite((Long)favs.get(caption));
             }
         }
 
         favs = getFavouriteActors();
         for (String caption : favs.keySet()) {
             int actorId = mActors.indexOf(new MovieActor(caption));
             if (actorId != -1) {
                 mActors.get(actorId).setFavourite((Long)favs.get(caption));
             }
         }
 
         if (useLocalOnly) {
             mIsDataActual = restoreData();
         } else {
            mState = new HashMap<String, ActivityState>();
             mIsDataActual = true;
         }
 
         return mIsDataActual;
     }
 
     public int getCurrentDay() {
         return mCurrentDay;
     }
 
     public void setCurrentDay(int day) {
         mCurrentDay = day;
     }
 
     public UniqueSortedList<Cinema> getCinemas() {
         return mCinemas;
     }
 
     public UniqueSortedList<Movie> getMovies() {
         return mMovies;
     }
 
     public UniqueSortedList<MovieActor> getActors() {
         return mActors;
     }
 
     public UniqueSortedList<MovieGenre> getGenres() {
         return mGenres;
     }
 
     public List<MoviePoster> getPosters() {
         return mPosters;
     }
 
     public PictureRetriever getPictureRetriever() {
         return mPictureRetriever;
     }
 
     public ActivityState getState(String cookie) {
         return mState.get(cookie);
     }
 
     public void setState(String cookie, ActivityState state) {
         mState.put(cookie, state);
     }
 
     public void removeState(String cookie) {
         mState.remove(cookie);
     }
 
     public void saveFavouriteActors() {
         SharedPreferences preferences = getSharedPreferences(FAV_ACTORS_FILE, MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         for (MovieActor actor : mActors) {
             if (actor.getFavourite() > 0) {
                 editor.putLong(actor.getActor(), actor.getFavourite());
             } else {
                 editor.remove(actor.getActor());
             }
         }
 
         editor.commit();
     }
 
     private Map<String, ?> getFavouriteActors() {
         SharedPreferences preferences = getSharedPreferences(FAV_ACTORS_FILE, MODE_PRIVATE);
         return preferences.getAll();
     }
 
     public void saveFavouriteCinemas() {
         SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + mCurrentCity.getId(), MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         for (Cinema cinema : mCinemas) {
             if (cinema.getFavourite() > 0) {
                 editor.putLong(cinema.getCaption(), cinema.getFavourite());
             } else {
                 editor.remove(cinema.getCaption());
             }
         }
 
         editor.commit();
     }
 
     private Map<String, ?> getFavouriteCinemas() {
         SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + mCurrentCity.getId(), MODE_PRIVATE);
         return preferences.getAll();
     }
 
     public void saveCinemaSortOrder(CinemaSortOrder sortOrder) {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(PREF_CINEMA_SORT_ORDER, sortOrder.ordinal());
 
         editor.commit();
     }
 
     public CinemaSortOrder getCinemaSortOrder() {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         int order = preferences.getInt(PREF_CINEMA_SORT_ORDER, CinemaSortOrder.BY_CAPTION.ordinal());
 
         for (CinemaSortOrder sortOrder : CinemaSortOrder.values()) {
             if (sortOrder.ordinal() == order) return sortOrder;
         }
 
         return CinemaSortOrder.BY_CAPTION;
     }
 
     public void saveMovieSortOrder(MovieSortOrder sortOrder) {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(PREF_MOVIE_SORT_ORDER, sortOrder.ordinal());
 
         editor.commit();
     }
 
     public MovieSortOrder getMovieSortOrder() {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         int order = preferences.getInt(PREF_MOVIE_SORT_ORDER, CinemaSortOrder.BY_CAPTION.ordinal());
 
         for (MovieSortOrder sortOrder : MovieSortOrder.values()) {
             if (sortOrder.ordinal() == order) return sortOrder;
         }
 
         return MovieSortOrder.BY_CAPTION;
     }
 
     public void saveCurrentCityId(int id) {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(PREF_CURRENT_CITY, id);
 
         editor.commit();
     }
 
     public int getCurrentCityId() {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         return preferences.getInt(PREF_CURRENT_CITY, -1);
     }
 
     public City getCurrentCity() {
         if (mCurrentCity != null) return mCurrentCity;
         else {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             try {
                 SAXParser parser = factory.newSAXParser();
                 CityHandler handler = new CityHandler();
                 parser.parse(getResources().openRawResource(R.raw.cities), handler);
                 int id = getCurrentCityId();
                 List<City> cities = handler.getCityList();
                 for (City city : cities) {
                     if (city.getId() == id) {
                         mCurrentCity = city;
                         return mCurrentCity;
                     }
                 }
                 return null;
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     public void setCurrentCity(City city) {
         mCurrentCity = city;
     }
 
     public Location getCurrentLocation() {
         return mCurrentLocation;
     }
 
     public void updateCurrentLocation(Location location) {
         mCurrentLocation = LocationHelper.selectBetterLocation(location, mCurrentLocation);
 
         for (LocationClient client : mLocationClients) {
             client.onLocationChanged(location);
         }
     }
 
     public void addLocationClient(LocationClient client) {
         if (!mLocationClients.contains(client)) {
             mLocationClients.add(client);
         }
     }
 
     public void removeLocationClient(LocationClient client) {
         mLocationClients.remove(client);
     }
 
     public void startListenLocation() {
         if (mLocationListenStarted) return;
 
         LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
 
         Criteria criteria = new Criteria();
 
         criteria.setAccuracy(Criteria.ACCURACY_COARSE);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setCostAllowed(true);
         criteria.setPowerRequirement(Criteria.POWER_LOW);
         String coarseProvider = locationManager.getBestProvider(criteria, true);
 
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         String fineProvider = locationManager.getBestProvider(criteria, true);
 
         Location coarseLocation = null;
         if (coarseProvider != null) {
             coarseLocation = locationManager.getLastKnownLocation(coarseProvider);
             locationManager.requestLocationUpdates(coarseProvider, LocationHelper.COARSE_TIME_LISTEN_TIMEOUT, LocationHelper.COARSE_LISTEN_DISTANCE, mLocationListener);
         }
 
         Location fineLocation = null;
         if (fineProvider != null) {
             fineLocation = locationManager.getLastKnownLocation(fineProvider);
             locationManager.requestLocationUpdates(fineProvider, LocationHelper.FINE_TIME_LISTEN_TIMEOUT, LocationHelper.FINE_LISTEN_DISTANCE, mLocationListener);
         }
 
         if (coarseLocation == null && fineLocation == null) {
             mCurrentLocation = null;
         } else if (coarseLocation != null && fineLocation == null) {
             mCurrentLocation = coarseLocation;
         } else if (fineLocation != null && coarseLocation == null) {
             mCurrentLocation = fineLocation;
         } else {
             mCurrentLocation = LocationHelper.selectBetterLocation(coarseLocation, fineLocation);
         }
 
         mLocationListenStarted = true;
     }
 
     public void stopListenLocation() {
         LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
         locationManager.removeUpdates(mLocationListener);
         locationManager.removeUpdates(mLocationListener);
 
         mLocationListenStarted = false;
     }
 }
