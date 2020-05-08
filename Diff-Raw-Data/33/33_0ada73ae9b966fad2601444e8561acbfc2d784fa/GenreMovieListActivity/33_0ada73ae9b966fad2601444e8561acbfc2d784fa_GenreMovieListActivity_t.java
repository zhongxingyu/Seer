 package com.watchlistapp.movielist;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.watchlistapp.R;
 import com.watchlistapp.fullmoviedescription.FullMovieDescriptionActivity;
 import com.watchlistapp.searchresults.SearchResultsActivity;
 import com.watchlistapp.searchresults.SearchResultsContainer;
 import com.watchlistapp.searchresults.SearchResultsItemAdapter;
 
 /**
  * Created by VEINHORN on 01/01/14.
  */
 public class GenreMovieListActivity extends SearchResultsActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search_results);
 
         getSupportActionBar().setTitle(getIntent().getStringExtra("genreTitle") + " movies");
 
         searchResultsListView = (ListView)findViewById(R.id.search_results_listview);
         searchResultsContainer = new SearchResultsContainer();
         // Get the query that we sent from RecentActivity
         searchResultsItemAdapter = new SearchResultsItemAdapter(GenreMovieListActivity.this, searchResultsContainer, GenreMovieListActivity.this, searchResultsListView);
         searchResultsListView.setAdapter(searchResultsItemAdapter);
 
         searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent intent = new Intent(GenreMovieListActivity.this, FullMovieDescriptionActivity.class);
                 intent.putExtra("movieId", searchResultsContainer.getSearchResultsItemArrayList().get(position).getMovieId());
                intent.putExtra("movieTitle", searchResultsContainer.getSearchResultsItemArrayList().get(position).getTitle());
                 startActivity(intent);
                 overridePendingTransition(R.anim.slide_out, R.anim.slide_in);
             }
         });
 
         String genreId = getIntent().getStringExtra("genreId");
         GenreMoviesLoader genreMoviesLoader = new GenreMoviesLoader(GenreMovieListActivity.this, queryString, searchResultsItemAdapter, searchResultsContainer, GenreMovieListActivity.this, genreId);
         genreMoviesLoader.execute();
     }
 }
