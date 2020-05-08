 package com.dedaulus.cinematty.activities;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.*;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.dedaulus.cinematty.ActivitiesState;
 import com.dedaulus.cinematty.ApplicationSettings;
 import com.dedaulus.cinematty.CinemattyApplication;
 import com.dedaulus.cinematty.R;
 import com.dedaulus.cinematty.activities.adapters.MovieItemWithScheduleAdapter;
 import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
 import com.dedaulus.cinematty.activities.adapters.StoppableAndResumable;
 import com.dedaulus.cinematty.framework.Movie;
 import com.dedaulus.cinematty.framework.SyncStatus;
 import com.dedaulus.cinematty.framework.tools.*;
 
 import java.util.*;
 
 /**
  * User: Dedaulus
  * Date: 12.10.11
  * Time: 10:05
  */
 public class MovieWithScheduleListActivity extends SherlockActivity {
     private CinemattyApplication app;
     private ApplicationSettings settings;
     private ActivitiesState activitiesState;
     private SortableAdapter<Movie> movieListAdapter;
     private ActivityState state;
     private String stateId;
     private int currentDay;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.movie_list);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setTitle(getString(R.string.movies_caption));
 
         app = (CinemattyApplication)getApplication();
         if (app.syncSchedule(this) != SyncStatus.OK) {
             app.restart();
             finish();
             return;
         }
 
         settings = app.getSettings();
         activitiesState = app.getActivitiesState();
 
         stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
         state = activitiesState.getState(stateId);
         if (state == null) throw new RuntimeException("ActivityState error");
 
         View captionView = findViewById(R.id.cinema_panel_in_movie_list);
         ListView list = (ListView)findViewById(R.id.movie_list);
 
         switch (state.activityType) {
         case ActivityState.MOVIE_LIST_W_CINEMA:
             captionView.setVisibility(View.GONE);
             LayoutInflater layoutInflater = LayoutInflater.from(this);
             View view = layoutInflater.inflate(R.layout.cinema_info, null, false);
             setCinemaHeader(view);
 
             list.addHeaderView(view, null, false);
 
             setCurrentDay(settings.getCurrentDay());
             break;
 
         default:
             throw new RuntimeException("ActivityType error");
         }
 
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 onScheduleItemClick(adapterView, view, i, l);
             }
         });
     }
 
     @Override
     protected void onResume() {
         ((StoppableAndResumable) movieListAdapter).onResume();
         if (currentDay != settings.getCurrentDay()) {
             setCurrentDay(settings.getCurrentDay());
         }
         movieListAdapter.sortBy(new MovieComparator(settings.getMovieSortOrder(), settings.getCurrentDay()));
 
         super.onResume();
     }
 
     @Override
     protected void onStop() {
         ((StoppableAndResumable) movieListAdapter).onStop();
         activitiesState.dump();
 
         super.onStop();
     }
 
     @Override
     public void onBackPressed() {
         activitiesState.removeState(stateId);
 
         super.onBackPressed();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
 
         MenuInflater inflater = getSupportMenuInflater();
 
         if (state.cinema.getAddress() != null) {
             inflater.inflate(R.menu.show_map_menu, menu);
         }
 
         if (state.cinema.getPhone() != null) {
             inflater.inflate(R.menu.call_menu, menu);
         }
 
         inflater.inflate(R.menu.select_day_menu, menu);
         switch (currentDay) {
             case Constants.TODAY_SCHEDULE:
                 menu.findItem(R.id.submenu_select_day_today).setChecked(true);
                 break;
 
             case Constants.TOMORROW_SCHEDULE:
                 menu.findItem(R.id.submenu_select_day_tomorrow).setChecked(true);
                 break;
         }
 
         inflater.inflate(R.menu.movie_sort_menu, menu);
         switch (settings.getMovieSortOrder()) {
             case BY_CAPTION:
                 menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
                 break;
             case BY_POPULAR:
                 menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
                 break;
             case BY_RATING:
                 menu.findItem(R.id.submenu_movie_sort_by_rating).setChecked(true);
                 break;
             default:
                 break;
         }
 
         inflater.inflate(R.menu.search_menu, menu);
 
         inflater.inflate(R.menu.preferences_menu, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 app.goHome(this);
                 return true;
 
             case R.id.menu_call:
                 onCinemaPhoneClick(null);
                 return true;
 
             case R.id.menu_show_map:
                 showCinemaOnMap();
                 return true;
 
             case R.id.menu_select_day:
                 return true;
 
             case R.id.submenu_select_day_today:
                 setCurrentDay(Constants.TODAY_SCHEDULE);
                 movieListAdapter.sortBy(new MovieComparator(settings.getMovieSortOrder(), Constants.TODAY_SCHEDULE));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_select_day_tomorrow:
                 setCurrentDay(Constants.TOMORROW_SCHEDULE);
                 movieListAdapter.sortBy(new MovieComparator(settings.getMovieSortOrder(), Constants.TOMORROW_SCHEDULE));
                 item.setChecked(true);
                 return true;
 
             case R.id.menu_movie_sort:
                 return true;
 
             case R.id.submenu_movie_sort_by_caption:
                 movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION, settings.getCurrentDay()));
                 settings.saveMovieSortOrder(MovieSortOrder.BY_CAPTION);
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_movie_sort_by_popular:
                 movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR, settings.getCurrentDay()));
                 settings.saveMovieSortOrder(MovieSortOrder.BY_POPULAR);
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_movie_sort_by_rating:
                 movieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_RATING, settings.getCurrentDay()));
                 settings.saveMovieSortOrder(MovieSortOrder.BY_RATING);
                 item.setChecked(true);
                 return true;
 
             case R.id.menu_search:
                 onSearchRequested();
                 return true;
 
             case R.id.menu_preferences:
                 app.showAbout(this);
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter)adapterView.getAdapter();
         MovieItemWithScheduleAdapter adapter = (MovieItemWithScheduleAdapter)headerAdapter.getWrappedAdapter();
         ListView list = (ListView)view.getParent();
         Movie movie = (Movie)adapter.getItem(i - list.getHeaderViewsCount());
         String cookie = UUID.randomUUID().toString();
 
         ActivityState state = this.state.clone();
         state.movie = movie;
         state.activityType = ActivityState.MOVIE_INFO_W_SCHED;
         activitiesState.setState(cookie, state);
 
         Intent intent = new Intent(this, MovieActivity.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         startActivity(intent);
     }
 
     private void setCurrentDay(int day) {
         settings.setCurrentDay(day);
         currentDay = day;
 
         Collection<Movie> movies = state.cinema.getMovies(settings.getCurrentDay()).values();
         if (movies.isEmpty()) {
             findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
         } else {
             findViewById(R.id.no_schedule).setVisibility(View.GONE);
         }
 
         StoppableAndResumable sar = (StoppableAndResumable) movieListAdapter;
         if (sar != null) sar.onStop();
         movieListAdapter = new MovieItemWithScheduleAdapter(this, new ArrayList<Movie>(movies), state.cinema, settings.getCurrentDay(), app.getImageRetrievers().getMovieSmallImageRetriever());
         sar = (StoppableAndResumable) movieListAdapter;
         sar.onResume();
         ListView list = (ListView)findViewById(R.id.movie_list);
         list.setAdapter(movieListAdapter);
     }
 
     private void setCinemaHeader(View view) {
         setCinemaFavourite(view);
         setCinemaCaption(view);
         setCinemaAddress(view);
         setCinemaPhone(view);
         setCinemaUrl(view);
     }
 
     private void setCinemaFavourite(View view) {
         ImageView favIcon = (ImageView)view.findViewById(R.id.fav_icon_in_cinema_info);
 
         if (state.cinema.getFavourite() > 0) {
             favIcon.setImageResource(R.drawable.ic_list_fav_on);
         } else {
             favIcon.setImageResource(R.drawable.ic_list_fav_off);
         }
     }
 
     private void setCinemaCaption(View view) {
         TextView caption = (TextView)view.findViewById(R.id.cinema_caption);
         caption.setText(state.cinema.getName());
     }
 
     private void setCinemaAddress(View view) {
         View panel = view.findViewById(R.id.cinema_address_panel);
         if (state.cinema.getAddress() != null) {
             TextView address = (TextView)view.findViewById(R.id.cinema_address);
             address.setText(state.cinema.getAddress());
 
             TextView into = (TextView)view.findViewById(R.id.cinema_into);
             if (state.cinema.getInto() != null) {
                 into.setText(state.cinema.getInto());
                 into.setVisibility(View.VISIBLE);
             } else {
                 into.setVisibility(View.GONE);
             }
 
             TextView metro = (TextView)view.findViewById(R.id.cinema_metro);
             if (state.cinema.getMetros().isEmpty()) {
                 metro.setVisibility(View.GONE);
             } else {
                 //metro.setText(getString(R.string.metro_near) + ": " + state.cinema.getMetros());
                 metro.setVisibility(View.VISIBLE);
             }
 
             panel.setVisibility(View.VISIBLE);
         } else {
             panel.setVisibility(View.GONE);
         }
     }
 
     private void setCinemaPhone(View view) {
         View panel = view.findViewById(R.id.cinema_phone_panel);
         if (state.cinema.getPhone() != null) {
             TextView phone = (TextView)view.findViewById(R.id.cinema_phone);
             phone.setText(state.cinema.getPhone());
 
             panel.setVisibility(View.VISIBLE);
         }
         else {
             panel.setVisibility(View.GONE);
         }
     }
 
     private void setCinemaUrl(View view) {
         TextView url = (TextView)view.findViewById(R.id.cinema_url);
         if (state.cinema.getUrl() != null) {
             StringBuilder buf = new StringBuilder(state.cinema.getUrl());
 
             if (state.cinema.getUrl().startsWith("http://")) {
                 buf.delete(0, "http://".length());
             }
 
             int slashPos = buf.indexOf("/");
             if (slashPos != -1) {
                 buf.delete(slashPos, buf.length());
             }
 
             SpannableString str = new SpannableString(buf.toString());
             str.setSpan(new UnderlineSpan(), 0, buf.length(), 0);
 
             url.setText(str);
 
             url.setVisibility(View.VISIBLE);
         } else {
             url.setVisibility(View.GONE);
         }
     }
 
     public void onCinemaAddressClick(View view) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse("geo:0,0?q=Россия, " + app.getCurrentCity().getName() + ", " + state.cinema.getAddress()));
         startActivity(intent);
     }
 
     public void onCinemaPhoneClick(View view) {
         Intent intent = new Intent(Intent.ACTION_DIAL);
         intent.setData(Uri.parse("tel:+7" + state.cinema.getPlainPhone()));
         startActivity(intent);
     }
 
     public void onCinemaUrlClick(View view) {
        if (state.cinema.getUrl() != null) {
             Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(state.cinema.getUrl()));
             startActivity(intent);
         }
     }
 
     public void onCinemaFavIconClick(View view) {
         if (state.cinema.getFavourite() > 0) {
             state.cinema.setFavourite(false);
             ((ImageView)view).setImageResource(R.drawable.ic_list_fav_off);
         } else {
             state.cinema.setFavourite(true);
             ((ImageView)view).setImageResource(R.drawable.ic_list_fav_on);
         }
     }
 
     private void showCinemaOnMap() {
         String cookie = UUID.randomUUID().toString();
         ActivityState state = new ActivityState(ActivityState.CINEMA_ON_MAP, this.state.cinema, null, null, null);
         activitiesState.setState(cookie, state);
 
         Intent intent = new Intent(this, CinemaMapView.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         startActivity(intent);
     }
 }
