 package ru.tulupov.nsuconnect.fragment;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AbsListView;
 import android.widget.ListView;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.adapter.MessageAdapter;
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.database.loader.MessageLoader;
 import ru.tulupov.nsuconnect.helper.SettingsHelper;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.Message;
 import ru.tulupov.nsuconnect.model.Settings;
 import ru.tulupov.nsuconnect.util.adapter.AdapterLoaderCallback;
 
 
 public class MessagesFragment extends Fragment {
     private static final int UPDATE_LIST_LOADER_ID = 0;
     private static final String TAG = MessagesFragment.class.getSimpleName();
     private static final long UPDATE_TIMEOUT = 3000;
     private MessageAdapter adapter;
     private ListView list;
     private View footer;
     private boolean listAtTheEnd;
     private boolean firstTime = true;
     private BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             update();
 
         }
     };
     private BroadcastReceiver updateTypingStatusReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             boolean isTyping = intent.getBooleanExtra(DatabaseConstants.EXTRA_IS_TYPING, false);
 
             updateTypingStatus(isTyping);
 
         }
     };
 
     private void updateTypingStatus(boolean isTyping) {
         final View container = footer.findViewById(R.id.container);
         if (isTyping) {
            if (container.getVisibility() != View.VISIBLE) {
                 Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
                 container.setVisibility(View.VISIBLE);
                 container.startAnimation(animation);
             }
         } else {
            if (container.getVisibility() != View.GONE) {
                 Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                 animation.setAnimationListener(new Animation.AnimationListener() {
                     @Override
                     public void onAnimationStart(Animation animation) {
 
                     }
 
                     @Override
                     public void onAnimationEnd(Animation animation) {
                         container.setVisibility(View.GONE);
                     }
 
                     @Override
                     public void onAnimationRepeat(Animation animation) {
 
                     }
                 });
                 container.startAnimation(animation);
             }
         }
     }
 
     private Chat chat;
 
     public static MessagesFragment newInstance(final Context context) {
         return (MessagesFragment) Fragment.instantiate(context, MessagesFragment.class.getName());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_messsages, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
 
         try {
             chat = HelperFactory.getHelper().getChatDao().getLast();
 
         } catch (SQLException e) {
             Log.e(TAG, "cannot create chat entity", e);
         }
         footer = View.inflate(getActivity(), R.layout.footer_messages, null);
 
         list = (ListView) view.findViewById(R.id.list);
         list.addFooterView(footer);
         adapter = new MessageAdapter();
         list.setAdapter(adapter);
         list.setOnScrollListener(new AbsListView.OnScrollListener() {
             @Override
             public void onScrollStateChanged(AbsListView absListView, int i) {
 
             }
 
             @Override
             public void onScroll(AbsListView lw, final int firstVisibleItem,
                                  final int visibleItemCount, final int totalItemCount) {
                 final int lastItem = firstVisibleItem + visibleItemCount;
                 listAtTheEnd = (lastItem == totalItemCount);
             }
         });
 
     }
 
 
     private void update() {
         LoaderManager loaderManager = getLoaderManager();
         Loader loader = loaderManager.getLoader(UPDATE_LIST_LOADER_ID);
         if (loader == null) {
             loaderManager.initLoader(UPDATE_LIST_LOADER_ID, null, new AdapterLoaderCallback<Message>(adapter) {
                 @Override
                 public Loader<List<Message>> onCreateLoader(int i, Bundle bundle) {
                     return new MessageLoader(getActivity(), chat);
                 }
 
                 @Override
                 public void onLoadFinished(Loader<List<Message>> loader, List<Message> data) {
                     super.onLoadFinished(loader, data);
 
                     if (listAtTheEnd && !firstTime) {
 
                         list.setSelection(adapter.getCount() - 1);
                     }
 
                     if (firstTime) {
                         firstTime = false;
                         list.postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 list.setSelection(adapter.getCount() - 1);
                             }
                         }, 1000);
                     }
 
 
                 }
             });
         }
         loader = loaderManager.getLoader(UPDATE_LIST_LOADER_ID);
         loader.forceLoad();
     }
 
 
     @Override
     public void onResume() {
         super.onResume();
         update();
         IntentFilter updateListFilter = new IntentFilter(DatabaseConstants.ACTION_UPDATE_MESSAGE_LIST);
         getActivity().registerReceiver(updateListReceiver, updateListFilter);
 
         IntentFilter updateTypingStatusFilter = new IntentFilter(DatabaseConstants.ACTION_UPDATE_TYPING_STATUS);
         getActivity().registerReceiver(updateTypingStatusReceiver, updateTypingStatusFilter);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         getActivity().unregisterReceiver(updateListReceiver);
         getActivity().unregisterReceiver(updateTypingStatusReceiver);
     }
 
 
 }
