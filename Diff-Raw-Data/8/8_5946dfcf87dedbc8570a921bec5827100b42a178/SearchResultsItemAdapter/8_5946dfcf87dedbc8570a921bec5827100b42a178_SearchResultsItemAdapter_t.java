 package com.watchlistapp.searchresults;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.makeramen.RoundedImageView;
 import com.watchlistapp.R;
 import com.watchlistapp.addmovietolistdialog.AddMovieToListDialogFragment;
 import com.watchlistapp.authorization.LoggedInUser;
 import com.watchlistapp.authorization.LoggedInUserContainer;
 import com.watchlistapp.database.WatchListDatabaseHandler;
import com.watchlistapp.themoviedb.NewPosterLoader;
 import com.watchlistapp.ratingbar.ColoredRatingBar;
 import com.watchlistapp.utils.DateUtil;
 
 /**
  * Created by VEINHORN on 02/12/13.
  */
 public class SearchResultsItemAdapter extends BaseAdapter {
 
     private static class ViewHolder {
         public RoundedImageView poster;
         public TextView title;
         public TextView releaseDate;
         public TextView rating;
         public TextView votes;
         public ColoredRatingBar votesRatingBar;
         public ImageButton amazonButton;
         public ImageButton addToListButton;
     }
 
     private SearchResultsContainer searchResultsContainer;
     private Context context;
     private LayoutInflater layoutInflater;
     private Activity activity;
     private ListView listView;
 
     public SearchResultsItemAdapter() {
 
     }
 
     public SearchResultsItemAdapter(Context context, SearchResultsContainer searchMovieContainer, Activity activity, ListView listView) {
         this.context = context;
         this.searchResultsContainer = searchMovieContainer;
         layoutInflater = LayoutInflater.from(context);
         this.activity = activity;
         this.listView = listView;
     }
 
     @Override
     public int getCount() {
         return searchResultsContainer.getSearchResultsItemArrayList().size();
     }
 
     @Override
     public Object getItem(int position) {
         return searchResultsContainer.getSearchResultsItemArrayList().get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder viewHolder;
 
         if(convertView == null) {
             convertView = layoutInflater.inflate(R.layout.search_results_item, null);
             viewHolder = new ViewHolder();
             viewHolder.poster = (RoundedImageView)convertView.findViewById(R.id.search_results_poster);
             viewHolder.title = (TextView)convertView.findViewById(R.id.search_results_title);
             viewHolder.rating = (TextView)convertView.findViewById(R.id.search_results_rating);
             viewHolder.releaseDate = (TextView)convertView.findViewById(R.id.search_results_release_date);
             viewHolder.votes = (TextView)convertView.findViewById(R.id.search_results_votes);
             viewHolder.votesRatingBar = (ColoredRatingBar)convertView.findViewById(R.id.search_results_rating_bar);
 
             viewHolder.amazonButton = (ImageButton)convertView.findViewById(R.id.search_results_activity_amazon_button);
             viewHolder.addToListButton = (ImageButton)convertView.findViewById(R.id.search_results_activity_add_to_list_button);
             //viewHolder.amazonButton.setOnClickListener(amazonButtonListener);
             viewHolder.addToListButton.setOnClickListener(addToListButtonListener);
             convertView.setTag(viewHolder);
         } else {
             viewHolder = (ViewHolder)convertView.getTag();
         }
 
         String title = searchResultsContainer.getSearchResultsItemArrayList().get(position).getTitle();
         String rating = searchResultsContainer.getSearchResultsItemArrayList().get(position).getRating() + "/" + "10";
         String votes = searchResultsContainer.getSearchResultsItemArrayList().get(position).getVotes() + " votes";
         String posterUrl = searchResultsContainer.getSearchResultsItemArrayList().get(position).getPosterLink();
         // themoviedb has 10digits rating so here we convert it to 5digits
         Float ratingBarVote = Float.valueOf(searchResultsContainer.getSearchResultsItemArrayList().get(position).getRating()) / (float)2;
         String releaseDate = searchResultsContainer.getSearchResultsItemArrayList().get(position).getReleaseDate();
         // Sometimes on themoviedb date may be empty
         if(releaseDate.equals("")) {
             releaseDate = "Release date: Unknown";
         } else {
             releaseDate = "Release date: " + DateUtil.convertDate(releaseDate);
         }
         // Convert from one time format to another
 
         viewHolder.title.setText(title);
         viewHolder.rating.setText(rating);
         viewHolder.releaseDate.setText(releaseDate);
         viewHolder.votes.setText(votes);
         viewHolder.votesRatingBar.setRating(ratingBarVote);
         NewPosterLoader newPosterLoader = new NewPosterLoader(context, viewHolder.poster, posterUrl, NewPosterLoader.BIG);
         newPosterLoader.loadPoster();
         return convertView;
     }
 
     /*
     // Amazon button action listener
     private View.OnClickListener amazonButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(final View view) {
             Intent intent = new Intent(context, AmazonListActivity.class);
             //final int position =
             //intent.putExtra("movietitle", );
             context.startActivity(intent);
         }
     };*/
 
     // Add to button listener
     private View.OnClickListener addToListButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(final View view) {
 
             FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
             // Create and show the dialog
             DialogFragment newFragment = AddMovieToListDialogFragment.newInstance();
 
             //
             WatchListDatabaseHandler watchListDatabaseHandler = new WatchListDatabaseHandler(context);
             LoggedInUserContainer loggedInUserContainer = watchListDatabaseHandler.getAllUsers();
             LoggedInUser loggedInUser = loggedInUserContainer.searchLastLoggedInUser();
 
             // Here we put arguments to dialog that will be shown
             // We put movieId and userId
             Bundle bundle = new Bundle();
             bundle.putString("movieId", searchResultsContainer.getSearchResultsItemArrayList().get(listView.getPositionForView(view)).getMovieId());
             bundle.putString("userId", loggedInUser.getServerId());
             bundle.putString("userPassword", loggedInUser.getPassword());
             bundle.putString("userEmail", loggedInUser.getEmail());
             newFragment.setArguments(bundle);
             newFragment.show(ft, "dialog");
         }
     };
 }
