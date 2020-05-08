 package com.watchlistapp.movielist;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.watchlistapp.R;
 import com.watchlistapp.fullmoviedescription.FullMovieDescriptionActivity;
 import com.watchlistapp.searchresults.SearchResultsContainer;
 import com.watchlistapp.searchresults.SearchResultsItemAdapter;
 import com.watchlistapp.themoviedb.LoadMovie;
 
 public class MovieListActivity extends ActionBarActivity {
 
     private ListView searchResultsListView;
     private SearchResultsItemAdapter searchResultsItemAdapter;
     private SearchResultsContainer searchResultsContainer;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_movie_list);
 
         ActionBar actionBar = getSupportActionBar();
         String listTitle = getIntent().getStringExtra("listtitle");
         String actionBarTitle = listTitle + " (" + getIntent().getStringExtra("moviesnumber") + " movies)";
         actionBar.setTitle(actionBarTitle);
 
         searchResultsListView = (ListView)findViewById(R.id.movie_list_listview);
 
         searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent intent = new Intent(MovieListActivity.this, FullMovieDescriptionActivity.class);
                 intent.putExtra("movieId", searchResultsContainer.getSearchResultsItemArrayList().get(position).getMovieId());
                intent.putExtra("movieTitle", searchResultsContainer.getSearchResultsItemArrayList().get(position).getTitle());
                 startActivity(intent);
                 overridePendingTransition(R.anim.slide_out, R.anim.slide_in);
             }
         });
 
         searchResultsContainer = new SearchResultsContainer();
         searchResultsItemAdapter = new SearchResultsItemAdapter(MovieListActivity.this, searchResultsContainer, MovieListActivity.this, searchResultsListView);
         searchResultsListView.setAdapter(searchResultsItemAdapter);
 
         LoadMovie loadMovie = new LoadMovie(MovieListActivity.this, listTitle, searchResultsItemAdapter, searchResultsContainer, MovieListActivity.this);
         loadMovie.execute();
     }
 }
