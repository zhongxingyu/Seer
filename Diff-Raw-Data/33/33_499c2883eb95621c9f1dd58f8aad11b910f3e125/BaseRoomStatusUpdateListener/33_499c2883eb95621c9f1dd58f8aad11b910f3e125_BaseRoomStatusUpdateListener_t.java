 package com.cauchymop.goblob.ui;
 
 import android.util.Log;
 
 import com.google.android.gms.games.multiplayer.realtime.Room;
 import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
 
 import java.util.List;
 
 /**
 * A minimalist {@link RoomStatusUpdateListener} which just logs all the events.
 */
 class BaseRoomStatusUpdateListener implements RoomStatusUpdateListener {
 
   private static final String TAG = BaseRoomStatusUpdateListener.class.getName();
 
   @Override
   public void onRoomConnecting(Room room) {
     Log.d(TAG, "onRoomConnecting(" + room + ")");
   }
 
   @Override
   public void onRoomAutoMatching(Room room) {
     Log.d(TAG, "onRoomAutoMatching(" + room + ")");
   }
 
   @Override
   public void onPeerInvitedToRoom(Room room, List<String> strings) {
     Log.d(TAG, "onPeerInvitedToRoom(" + room + ", " + strings + ")");
   }
 
   @Override
   public void onPeerDeclined(Room room, List<String> strings) {
     Log.d(TAG, "onPeerDeclined(" + room + ", " + strings + ")");
   }
 
   @Override
   public void onPeerJoined(Room room, List<String> strings) {
     Log.d(TAG, "onPeerJoined(" + room + ", " + strings + ")");
   }
 
   @Override
   public void onPeerLeft(Room room, List<String> strings) {
     Log.d(TAG, "onPeerLeft(" + room + ", " + strings + ")");
   }
 
   @Override
   public void onConnectedToRoom(Room room) {
     Log.d(TAG, "onConnectedToRoom(" + room + ")");
   }
 
   @Override
   public void onDisconnectedFromRoom(Room room) {
     Log.d(TAG, "onDisconnectedFromRoom(" + room + ")");
   }
 
   @Override
   public void onPeersConnected(Room room, List<String> strings) {
     Log.d(TAG, "onPeersConnected(" + room + ", " + strings + ")");
   }
 
   @Override
   public void onPeersDisconnected(Room room, List<String> strings) {
     Log.d(TAG, "onPeersDisconnected(" + room + ", " + strings + ")");
   }

  @Override
  public void onP2PConnected(String s) {
    Log.d(TAG, "onP2PConnected(" + s + ")");
  }

  @Override
  public void onP2PDisconnected(String s) {
    Log.d(TAG, "onP2PDisconnected(" + s + ")");
  }
 }
