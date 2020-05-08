 package com.ouchadam.fang.presentation.item;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.channel.Channel;
 import com.ouchadam.fang.domain.channel.Image;
 import com.ouchadam.fang.persistance.FangProvider;
 import com.ouchadam.fang.persistance.Query;
 import com.ouchadam.fang.persistance.database.Tables;
 import com.ouchadam.fang.persistance.database.Uris;
 
 import novoda.android.typewriter.cursor.CursorMarshaller;
 
 public class ChannelFragment extends CursorBackedListFragment<Channel> implements OnItemClickListener<Channel> {
 
     private final ActionBarTitleSetter actionBarTitleSetter;
 
     public ChannelFragment() {
         this.actionBarTitleSetter = new ActionBarTitleSetter();
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         actionBarTitleSetter.onAttach(activity);
     }
 
     @Override
     protected View getRootLayout(LayoutInflater inflater, ViewGroup container) {
         return inflater.inflate(R.layout.fragment_channel_list, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         setOnItemClickListener(this);
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
                .withProjection(new String[] { Tables.Channel.CHANNEL_TITLE.name(), Tables.ChannelImage.URL.name() })
                 .build();
     }
 
     @Override
     protected CursorMarshaller<Channel> getMarshaller() {
         return new ChannelSummaryMarshaller();
     }
 
     @Override
     public void onItemClick(TypedListAdapter<Channel> adapter, int position, long itemId) {
         new Navigator(getActivity()).toChannel(adapter.getItem(position).getTitle());
     }
 
     private static class ChannelSummaryMarshaller implements CursorMarshaller<Channel> {
         @Override
         public Channel marshall(Cursor cursor) {
             String title = cursor.getString(cursor.getColumnIndexOrThrow(Tables.Channel.CHANNEL_TITLE.name()));
            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Tables.ChannelImage.URL.name()));
             return new Channel(title, "", new Image(imageUrl, "", "", 0, 0), "", null);
         }
     }
 }
