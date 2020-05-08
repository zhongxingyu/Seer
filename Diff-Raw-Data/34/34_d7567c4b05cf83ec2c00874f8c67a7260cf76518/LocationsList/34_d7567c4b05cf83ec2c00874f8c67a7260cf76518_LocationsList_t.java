 package com.stormdev.minigamez.editor;
 
 import java.awt.PopupMenu;
 import java.awt.dnd.DropTarget;
 import java.util.ArrayList;
 
 import javax.swing.JComboBox;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 
 public class LocationsList extends JComboBox {
 	private static final long serialVersionUID = -6570143230108119046L;
 	private WindowArea window = null;
 	private ArrayList<String> locations = new ArrayList<String>();
	@SuppressWarnings("unchecked")
 	public LocationsList(WindowArea area){
 		this.window = area;
		this.locations = (ArrayList<String>) window.locations.getLocations().clone();
 		draw();
 	}
	@SuppressWarnings("unchecked")
 	public void updateLocs(){
		this.locations = (ArrayList<String>) window.locations.getLocations().clone();
 	}
 	public void draw(){
 		updateLocs();
 		locations.add("worldSpawnPoint");
 		locations.add("arenaCenter");
 		//create
 		this.setModel(new JComboBox(locations.toArray()).getModel());
 	}
 	
 
 }
