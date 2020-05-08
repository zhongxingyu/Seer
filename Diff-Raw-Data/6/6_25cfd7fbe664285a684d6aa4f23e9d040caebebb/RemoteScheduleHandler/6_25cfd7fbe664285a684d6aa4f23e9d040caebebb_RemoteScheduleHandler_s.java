 /*
  * Copyright 2010 Peter Kuterna
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.peterkuterna.android.apps.devoxxsched.io;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Blocks;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Rooms;
 import net.peterkuterna.android.apps.devoxxsched.provider.ScheduleContract.Sessions;
 import net.peterkuterna.android.apps.devoxxsched.util.Lists;
 import net.peterkuterna.android.apps.devoxxsched.util.Maps;
 import net.peterkuterna.android.apps.devoxxsched.util.ParserUtils;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 
 /**
  * Handle a remote {@link JSONArray} that defines a set of {@link Blocks}
  * entries. Also updates the related {@link Sessions} with {@link Rooms} and
  * {@link Blocks} info.
  */
 public class RemoteScheduleHandler extends JSONHandler {
 	
     private static final String TAG = "ScheduleHandler";
 
     public RemoteScheduleHandler() {
 		super(ScheduleContract.CONTENT_AUTHORITY, false);
 	}
 
 	@Override
 	public ArrayList<ContentProviderOperation> parse(JSONArray schedules, ContentResolver resolver) throws JSONException {
 		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
 		final HashMap<String, ContentProviderOperation> blockBatchMap = Maps.newHashMap();
 		final HashMap<String, ContentProviderOperation> sessionUpdateBatchMap = Maps.newHashMap();
 		
 		Log.d(TAG, "Retrieved " + schedules.length() + " schedule entries.");
 
         for (int i=0; i<schedules.length(); i++) {
             JSONObject schedule = schedules.getJSONObject(i);
             
             final long startTime = ParserUtils.parseDevoxxTime(schedule.getString("fromTime"));
             final long endTime = ParserUtils.parseDevoxxTime(schedule.getString("toTime"));
             
             final String blockId = Blocks.generateBlockId(startTime, endTime);
             
             if (!blockBatchMap.containsKey(blockId)) {
                 final Uri blockUri = Blocks.buildBlockUri(blockId);
                 
                 ContentProviderOperation.Builder builder;
                 if (isRowExisting(Blocks.buildBlockUri(blockId), BlocksQuery.PROJECTION, resolver)) {
                 	builder = ContentProviderOperation.newUpdate(blockUri);
                 } else {
     	            builder = ContentProviderOperation.newInsert(Blocks.CONTENT_URI);
     	            builder.withValue(Blocks.BLOCK_ID, blockId);
                 }
     		    builder.withValue(Blocks.BLOCK_START, startTime);
     		    builder.withValue(Blocks.BLOCK_END, endTime);
 
     		    final String type = schedule.getString("type");
     		    final String code = schedule.getString("code");
     		    final String kind = schedule.getString("kind");
     		    
     		    if (code.startsWith("D10")) {
     		    	builder.withValue(Blocks.BLOCK_TITLE, type.replaceAll("\\ \\(.*\\)", ""));
     		    } else {
     		    	builder.withValue(Blocks.BLOCK_TITLE, schedule.getString("code"));
     		    }
 
     		    builder.withValue(Blocks.BLOCK_TYPE, kind);
     		    blockBatchMap.put(blockId, builder.build());
             }
             
             if (schedule.has("presentationUri")) {
             	final Uri presentationUri = Uri.parse(schedule.getString("presentationUri"));
             	final String sessionId = presentationUri.getLastPathSegment();
             	final Uri sessionUri = Sessions.buildSessionUri(sessionId);
             	
             	if (isRowExisting(sessionUri, SessionsQuery.PROJECTION, resolver)) {
 	            	String roomId = null;
 	                if (schedule.has("room")) {
 	                	final String roomName = schedule.getString("room");
 	                	Cursor cursor = resolver.query(Rooms.buildRoomsWithNameUri(roomName), RoomsQuery.PROJECTION, null, null, null);
 	                	if (cursor.moveToNext()) {
 	                		roomId = cursor.getString(RoomsQuery.ROOM_ID);
 	                	}
 	                	cursor.close();
 	                }
 	            	final ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(sessionUri);
 	            	builder.withValue(Sessions.BLOCK_ID, blockId);
 	            	builder.withValue(Sessions.ROOM_ID, roomId);
 	            	if (schedule.has("note")) {
 	            		final String note = schedule.getString("note");
 	            		if (note != null && note.trim().length() > 0) {
 	            			builder.withValue(Sessions.NOTE, note.trim());
 	            		}
 	            	}
 	            	
 	            	sessionUpdateBatchMap.put(sessionId, builder.build());
             	}
             }
         }
         
         batch.addAll(blockBatchMap.values());
         batch.addAll(sessionUpdateBatchMap.values());
         
         if (schedules.length() > 0) {
 		    for (String lostId : getLostIds(blockBatchMap.keySet(), Blocks.CONTENT_URI, BlocksQuery.PROJECTION, BlocksQuery.BLOCK_ID, resolver)) {
 		    	final Uri lostBlockUri = Blocks.buildBlockUri(lostId);
 		    	batch.add(ContentProviderOperation.newDelete(lostBlockUri).build());
 		    }
 		    for (String lostId : getLostIds(sessionUpdateBatchMap.keySet(), Sessions.CONTENT_URI, SessionsQuery.PROJECTION, SessionsQuery.SESSION_ID, resolver)) {
		    	final Uri lostSessionUri = Sessions.buildSessionUri(lostId);
		    	batch.add(ContentProviderOperation.newDelete(lostSessionUri).build());
 		    }
         }
 
         return batch;
 	}
 
     private interface SessionsQuery {
         String[] PROJECTION = {
         		Sessions.SESSION_ID,
         };
 
         int SESSION_ID = 0;
     }
 
     private interface BlocksQuery {
         String[] PROJECTION = {
                 Blocks.BLOCK_ID,
         };
 
         int BLOCK_ID = 0;
     }
 
    private interface RoomsQuery {
         String[] PROJECTION = {
                 BaseColumns._ID,
                 Rooms.ROOM_ID,
         };
 
         int _ID = 0;
         int ROOM_ID = 1;
     }
     
 }
