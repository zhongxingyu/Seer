 package com.music.fmv.fragments;
 
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.*;
 import com.music.fmv.R;
 import com.music.fmv.adapters.SearchBandAdapter;
 import com.music.fmv.core.BaseFragment;
 import com.music.fmv.models.BandInfoModel;
 import com.music.fmv.models.SearchBandModel;
 import com.music.fmv.tasks.GetBandTask;
 import com.music.fmv.tasks.SearchBandTask;
 import com.music.fmv.utils.ViewUtils;
 import com.music.fmv.views.LoadDialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: vitaliylebedinskiy
  * Date: 8/5/13
  * Time: 12:30 PM
  */
 public class SearchArtistFragment extends BaseFragment {
     private Integer futureArtistPage = 1;
     private int artistsPageAvailable = 0;
     private String lastRequest;
 
     private ListView artistsListView;
     private ArrayList<SearchBandModel> artistsList = new ArrayList<SearchBandModel>(21);
     private boolean artistTaskRunned;
     private SearchBandAdapter artistsAdapter;
     private View rotateFooter;
 
     private boolean getUserTaskRunned;
     private TextView emptyView;
 
     @Override
     protected void createView(Bundle savedInstanceState) {
         mainView = inflateView(R.layout.search_artist_fragment);
         emptyView = (TextView) mainView.findViewById(R.id.empty_view);
         artistsListView = (ListView) mainView.findViewById(R.id.artists_list);
         artistsListView.addHeaderView(createSearchHeader(searchListener));
         artistsListView.setHeaderDividersEnabled(false);
         artistsListView.setOnScrollListener(scrollListener);
         artistsAdapter = new SearchBandAdapter(artistsList, baseActivity);
         artistsListView.setAdapter(artistsAdapter);
         artistsListView.setOnItemClickListener(artistClickListener);
         //Initialization of footer view(ProgressBar)
         rotateFooter = inflateView(R.layout.rotate_footer);
     }
 
     private void processSearch(String s) {
         searchBand(s, null);
     }
 
     private void searchBand(String query, final Integer page){
         if (TextUtils.isEmpty(query) || artistTaskRunned) return;
         query = query.trim();
 
         SearchBandTask task = new SearchBandTask(query, page, baseActivity){
             public LoadDialog dialog;
 
             @Override
             protected void onPreExecute() {
                 //If page == null it means that it is new request, else its updating of list (adding new page)
                 artistTaskRunned = true;
                 if (page == null) {
                     dialog = new LoadDialog(baseActivity, this);
                     dialog.show();
                 }
             }
 
             @Override
             protected void onPostExecute(List<SearchBandModel> result) {
                 if(dialog != null) dialog.dismiss();
                 dialog = null;
                 artistTaskRunned = false;
                 artistsPageAvailable = 0;

                if (isCancelled()) return;

                 //empty result
                 if (result != null && result.size() ==0){
                     Toast.makeText(baseActivity, getString(R.string.empty_result), Toast.LENGTH_SHORT).show();
                     clearArtists();
                     return;
                 }
 
                 //it means that error was occurred during search
                 if (isError || result == null){
                     Toast.makeText(baseActivity, getString(R.string.request_error), Toast.LENGTH_SHORT).show();
                     clearArtists();
                     return;
                 }
 
                 //normal answer. update list
                 artistsPageAvailable = SearchBandModel.AVAILABLE_PAGES;
                 updateBandList(result, page == null);
             }
 
             @Override
             public void canceledByUser() {
                artistTaskRunned= false;
                cancel(true);
             }
         };
 
         if (runTask(task)){
             lastRequest = query;
         }
     }
 
     private void getNextBandPage(){
         if (artistsPageAvailable > 0 ) {
             searchBand(lastRequest, futureArtistPage);
         }
     }
 
     private void updateBandList(List<SearchBandModel> searchBandModels,boolean isClear){
         if(isClear) {
             artistsList.clear();
             futureArtistPage = 1;
         }
         artistsList.addAll(searchBandModels);
         artistsAdapter.notifyDataSetChanged();
         futureArtistPage += 1;
 
         if (artistsPageAvailable <= 0){
             artistsListView.removeFooterView(rotateFooter);
         }else if (artistsPageAvailable > 0 && artistsListView.getFooterViewsCount() == 0){
             artistsListView.addFooterView(rotateFooter);
         }
     }
 
     private void clearArtists() {
         artistsList.clear();
         artistsListView.removeFooterView(rotateFooter);
         SearchBandModel.AVAILABLE_PAGES= 0;
         artistsAdapter.notifyDataSetChanged();
     }
 
     //Scroll listeners for lists, call method when last item in list is visible
     private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
         @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
 
         @Override
         public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
             final int lastItem = firstVisibleItem + visibleItemCount;
             if(lastItem == totalItemCount) {
                 getNextBandPage();
             }
         }
     };
 
     //Called when user click search on the soft keyboard
     private TextView.OnEditorActionListener searchListener = new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             ViewUtils.hideSoftKeyboard(baseActivity);
             if (actionId == EditorInfo.IME_ACTION_SEARCH){
                 String text = v.getText().toString() ;
                 processSearch(text);
                 return true;
             }
             return false;
         }
     };
 
     private AdapterView.OnItemClickListener artistClickListener = new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             if (getUserTaskRunned) return;
 
             SearchBandModel m = artistsList.get(position - artistsListView.getHeaderViewsCount());
             GetBandTask task = new GetBandTask(baseActivity) {
                 public LoadDialog dialog;
 
                 @Override
                 protected void onPreExecute() {
                     if (dialog == null){
                         dialog = new LoadDialog(baseActivity, this);
                     }
                     dialog.show();
                 }
 
                 @Override
                 protected void onPostExecute(BandInfoModel bandInfoModel) {
                    if (isCancelled()) return;

                     getUserTaskRunned = false;
                     if (dialog != null) dialog.dismiss();
                 }
 
                 @Override
                 public void canceledByUser() {
                     getUserTaskRunned = false;
                    cancel(true);
                 }
             };
 
             if (runTask(task)){
                 getUserTaskRunned = true;
             }
         }
     };
 }
