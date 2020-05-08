 package com.home.giraffe.base;
 
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
 import com.home.giraffe.R;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 
 public abstract class BaseListFragment<T> extends RoboSherlockListFragment implements LoaderManager.LoaderCallbacks<T>, PullToRefreshAttacher.OnRefreshListener {
 
     private LinearLayout mFooter;
     private PullToRefreshAttacher mPullToRefreshAttacher;
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
 
         mPullToRefreshAttacher = getPullToRefreshAttacher();
         addPullToRefresh();
     }
 
     public boolean isViewDestroyed(){
         return getView() == null;
     }
 
     public int getItemsCount(){
         return getListView().getCount() - getListView().getFooterViewsCount();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         showFooter();
 
         getListView().setFastScrollEnabled(true);
         getListView().setDividerHeight(0);
         getListView().setDivider(null);
 
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Object item = getListAdapter().getItem(i);
                 itemSelected(item);
             }
         });
 
         setRetainInstance(true);
     }
 
     protected abstract void itemSelected(Object item);
 
     public void enableOnScrollUpdates(){
         getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
             @Override
             public void onScrollStateChanged(AbsListView view,
                                              int scrollState) {
             }
 
             @Override
             public void onScroll(AbsListView view, int firstVisibleItem,
                                  int visibleItemCount, int totalItemCount) {
 
                 int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount && totalItemCount > 0) {
                     if (!getActivity().getSupportLoaderManager().hasRunningLoaders()) {
                         showFooter();
                         loadNext();
                     }
                 }
             }
         });
     }
 
     public void disableOnScrollUpdates(){
         getListView().setOnScrollListener(null);
         hideFooter();
     }
 
     public void hideFooter(){
         if(isFooterVisible())
             getListView().removeFooterView(mFooter);
     }
 
     public boolean isFooterVisible() {
         return getListView().getFooterViewsCount() != 0;
     }
 
     public void showFooter() {
         if(getListView().getFooterViewsCount() == 0){
             mFooter = (LinearLayout) getLayoutInflater(null).inflate(R.layout.footer, null);
             getListView().addFooterView(mFooter);
         }
     }
 
     public void update() {
     }
 
     public void loadNext() {
     }
 
     public void addPullToRefresh(){
         mPullToRefreshAttacher.addRefreshableView(getListView(), this);
     }
 
     public void completePullToRefresh(){
         mPullToRefreshAttacher.setRefreshComplete();
     }
 
     @Override
     public void onRefreshStarted(View view) {
         update();
     }
 
     @Override
     public Loader<T> onCreateLoader(int i, Bundle bundle) {
         return null;
     }
 
     @Override
     public void onLoadFinished(Loader<T> inboxLoader, T activities) {
         if(!isViewDestroyed()){
             setListShown(true);
             completePullToRefresh();
             hideFooter();
         }
         if (activities != null) {
             onLoadFinished(activities);
         }
     }
 
     protected abstract void onLoadFinished(T activities);
 
     @Override
     public void onLoaderReset(Loader<T> activitiesLoader) {
     }
 
     public PullToRefreshAttacher getPullToRefreshAttacher() {
         return ((BaseFragmentActivity)getActivity()).getPullToRefreshAttacher();
     }
 }
