 /*
  * Copyright 2013 Prateek Srivastava (@f2prateek)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.f2prateek.xkcd.ui;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import butterknife.InjectView;
 import com.f2prateek.xkcd.AppConstansts;
 import com.f2prateek.xkcd.R;
 import com.f2prateek.xkcd.XKCDApi;
 import com.f2prateek.xkcd.model.Comic;
 import com.f2prateek.xkcd.ui.base.BaseFragment;
 import com.f2prateek.xkcd.util.Ln;
 import com.squareup.picasso.Picasso;
 import javax.inject.Inject;
 import retrofit.Callback;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 
 /** A fragment to display a single comic. */
 public class ComicViewFragment extends BaseFragment implements Callback<Comic> {
 
   public static final String COMIC_EXTRA_ARG = "com.f2prateek.xkcd.COMIC";
 
   @Inject XKCDApi xkcdApi;
   @InjectView(R.id.comic_image) ImageView comic_image;
   @InjectView(R.id.comic_info_container) View comic_info_container;
   @InjectView(R.id.comic_info_title) TextView comic_info_title;
   @InjectView(R.id.comic_info_date) TextView comic_info_date;
   @InjectView(R.id.comic_info_transcript) TextView comic_info_transcript;
 
   private int comicNumber;
   private Comic comic;
 
   public static ComicViewFragment newInstance(int comicNumber) {
     ComicViewFragment fragment = new ComicViewFragment();
     Bundle args = new Bundle();
     args.putInt(COMIC_EXTRA_ARG, comicNumber);
     fragment.setArguments(args);
     return fragment;
   }
 
   @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     comicNumber = getArguments().getInt(COMIC_EXTRA_ARG);
     xkcdApi.getComic(comicNumber, this);
   }
 
   @Override public void success(Comic comic, Response response) {
     this.comic = comic;
     setHasOptionsMenu(true);
     getActivity().getActionBar().setTitle(comic.getSafe_title());
     Picasso.with(getActivity()).load(comic.getImg()).into(comic_image);
     comic_info_title.setText(comic.getTitle());
     comic_info_date.setText(
         DateUtils.getRelativeTimeSpanString(comic.getTimeInMillis(), System.currentTimeMillis(),
             DateUtils.DAY_IN_MILLIS));
     comic_info_transcript.setText(comic.getTranscript());
   }
 
   @Override public void failure(RetrofitError retrofitError) {
    Ln.e(retrofitError.getCause());
   }
 
   @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     super.onCreateOptionsMenu(menu, inflater);
     inflater.inflate(R.menu.comic_fragment, menu);
   }
 
   @Override public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
       case R.id.action_comic_explanation:
         openUrl(AppConstansts.getExplationUrl(comic.getNum()));
         return true;
       case R.id.action_comic_info:
         showComicInfo();
         return true;
       case R.id.action_comic_link:
         openUrl(AppConstansts.getComicUrl(comic.getNum()));
         return true;
       default:
         return super.onOptionsItemSelected(item);
     }
   }
 
   // Show some information about the current comic
   public void showComicInfo() {
     if (comic_info_container.getVisibility() == View.VISIBLE) {
       comic_info_container.setVisibility(View.GONE);
     } else {
       comic_info_container.setVisibility(View.VISIBLE);
     }
   }
 
   // Opens an activity to view the given url
   public void openUrl(String url) {
     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
     startActivity(intent);
   }
 
   @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
       Bundle savedInstanceState) {
     return inflater.inflate(R.layout.comic_fragment, container, false);
   }
 }
