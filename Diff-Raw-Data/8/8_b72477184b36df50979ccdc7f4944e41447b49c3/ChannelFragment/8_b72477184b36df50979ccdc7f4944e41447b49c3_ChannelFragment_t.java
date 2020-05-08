 package com.ouchadam.fang.presentation.item;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.FullItem;
 import com.ouchadam.fang.domain.channel.Channel;
 import com.ouchadam.fang.domain.channel.Image;
 import com.ouchadam.fang.persistance.FangProvider;
 import com.ouchadam.fang.persistance.Query;
 import com.ouchadam.fang.persistance.RemoveNewItemCountPersister;
 import com.ouchadam.fang.persistance.database.Tables;
 import com.ouchadam.fang.persistance.database.Uris;
 
 import java.util.List;
 
 import novoda.android.typewriter.cursor.CursorMarshaller;
 
 public class ChannelFragment extends CursorBackedListFragment<Channel> implements OnItemClickListener<Channel>, CursorBackedListFragment.OnLongClickListener<Channel>, ChannelListActionMode.OnChannelListActionMode {
 
     private final ActionBarTitleSetter actionBarTitleSetter;
     private ChannelListActionMode channelListActionMode;
 
     public ChannelFragment() {
         this.actionBarTitleSetter = new ActionBarTitleSetter();
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         actionBarTitleSetter.onAttach(activity);
         channelListActionMode = new ChannelListActionMode(activity, this);
     }
 
     @Override
     protected View getRootLayout(LayoutInflater inflater, ViewGroup container) {
         return inflater.inflate(R.layout.fragment_channel_list, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         setOnItemClickListener(this);
         setOnItemLongClickListener(this);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         actionBarTitleSetter.set("Channels");
     }
 
     @Override
     protected TypedListAdapter<Channel> createAdapter() {
         return new ChannelAdapter(getActivity(), LayoutInflater.from(getActivity()));
     }
 
     @Override
     protected Query getQueryValues() {
         return new Query.Builder()
                 .withUri(FangProvider.getUri(Uris.FULL_CHANNEL))
                 .withProjection(new String[]{Tables.Channel.CHANNEL_TITLE.name(), Tables.Channel.NEW_ITEM_COUNT.name(), Tables.ChannelImage.IMAGE_URL.name()})
                 .build();
     }
 
     @Override
     protected CursorMarshaller<Channel> getMarshaller() {
         return new ChannelSummaryMarshaller();
     }
 
     @Override
     public void onItemClick(TypedListAdapter<Channel> adapter, int position, long itemId) {
        if (!channelListActionMode.isInActionMode()) {
            Channel channel = adapter.getItem(position);
            new RemoveNewItemCountPersister(getActivity().getContentResolver()).persist(channel.getTitle());
            new Navigator(getActivity()).toChannel(channel.getTitle());
        }
     }
 
     @Override
     public boolean onItemLongClick(TypedListAdapter<Channel> adapterView, View view, int position, long itemId) {
         if (!channelListActionMode.isInActionMode()) {
             channelListActionMode.onStart();
             allowChecking();
             setLongPressChecked(position);
             return true;
         }
         return false;
     }
 
     @Override
     public void onDelete() {
         List<Channel> selectedChannels = getAllCheckedPositions();
         if (selectedChannels != null && !selectedChannels.isEmpty()) {
             DownloadDeleter.from(getActivity()).deleteChannels(selectedChannels);
         } else {
             Toast.makeText(getActivity(), "Failed to delete, couldn't find and podcasts", Toast.LENGTH_SHORT).show();
         }
     }
 
     @Override
     public void onActionModeFinish() {
         deselectAll();
         disallowChecking();
     }
 
     private static class ChannelSummaryMarshaller implements CursorMarshaller<Channel> {
         @Override
         public Channel marshall(Cursor cursor) {
             String title = cursor.getString(cursor.getColumnIndexOrThrow(Tables.Channel.CHANNEL_TITLE.name()));
             String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Tables.ChannelImage.IMAGE_URL.name()));
             int newItemCount = cursor.getInt(cursor.getColumnIndexOrThrow(Tables.Channel.NEW_ITEM_COUNT.name()));
 
             return new Channel(title, "", new Image(imageUrl, "", "", 0, 0), "", newItemCount, null, -1);
         }
     }
 }
