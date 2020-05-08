 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Barnabas sapan
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 public class MapOverlayPinNotes extends ItemizedOverlay<OverlayItem> implements Observer {
 	private Context context;
 	private List <Note> notes = new ArrayList<Note>(); 
 	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
 	private MapView mapView;
 
 	/**
 	 * @param context the context
 	 * @param marker the marker to display on the map
 	 */
 	public MapOverlayPinNotes(Context context, Drawable marker, MapView map) {
 		super(boundCenterBottom(marker));
 		this.context = context;
 		this.mapView = map;
 		
 		DatabaseHandler.getInstance(context).addObserver(this);
 				
 		this.refresh();		
 	}
 	
 	@Override
 	protected boolean onTap(int index) {
 		OverlayItem item = overlays.get(index);
 		
 		Intent editNote = new Intent(context, ActivityNote.class);
 		editNote.putExtra(IntentExtra.id.toString(), Integer.parseInt(item.getTitle()));
 		editNote.putExtra(IntentExtra.justId.toString(), true);
 
 		//Start the edit view.
 		context.startActivity(editNote);
 
 		return true;
 	}
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		//Remove shadow from the markers
 		super.draw(canvas, mapView, false);
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		 return overlays.get(i);
 	}
 
 	@Override
 	public int size() {
 		return overlays.size();
 	}
 	
 	/**
 	 * Add an overlay to this Overlay
 	 * @param overlay the overlay to add
 	 */
 	private void addOverlay(OverlayItem overlay) {
 	    overlays.add(overlay);
 	}
 	
 	private void refresh(){
 		this.overlays.clear();
 		this.notes.clear();		
 		this.notes = DatabaseHandler.getInstance(context).getNoteList();
 		
 		//Add all notes with a valid location to the map
 		for(Note note : notes){
 			if(note.getLocation().getLatitude() != 0 && note.getLocation().getLongitude() != 0) {
 				GeoPoint point = new GeoPoint((int)(note.getLocation().getLatitude() * 1E6),
 						(int)(note.getLocation().getLongitude() * 1E6));
 				//As a title add the note id
 				this.addOverlay(new OverlayItem(point, note.getId() + "", null));
 			}
 		}
 		
 		setLastFocusedIndex(-1);
 		this.mapView.invalidate();
 		populate();
 	}
 		
 	public void update(Observable observable, Object data) {
 		if(observable instanceof DatabaseHandler) {
 			if((DatabaseUpdate)data == DatabaseUpdate.UPDATED_LOCATION 
 					|| (DatabaseUpdate)data == DatabaseUpdate.NEW_NOTE 
 					|| (DatabaseUpdate)data == DatabaseUpdate.UPDATED_NOTE 
 					|| (DatabaseUpdate)data == DatabaseUpdate.DELETED_NOTE) {
 				this.refresh();
 			}
 		}
 	}
 }
