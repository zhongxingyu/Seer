 //Track class will hold a sequence of clips. Tracks will be 
 //played simultaneously with other tracks. Volume will be 
 //adjustable at a track level as well. 
 //
 //Created by: Cam 3/30/13
 //Adapted by: Mike 4/1/13
 
 package com.teamluper.luper;
 
 import java.util.ArrayList;
 
 public class Track {
   // Mike's database field variables
 	private long id;
 	private long ownerUserID;
 	private long parentSequenceID;
 	private boolean isMuted;
 	private boolean isLocked;
 	private String playbackOptions;
 	private boolean isDirty; // dirty = contains unsynced changes
 	
 	// Mike's database access variables
 	private LuperDataSource dataSource;
 	private boolean autoSaveEnabled;
 	
 	// Cam's variables
 	ArrayList <Clip>clips = new ArrayList<Clip>();
 	String[] playBackList;
 	long duration;
 	// volume? an int?
 	
 	// Mike's constructor
 	public Track(LuperDataSource dataSource, boolean autoSaveEnabled) {
 	  this.dataSource = dataSource;
 	  this.autoSaveEnabled = autoSaveEnabled;
 	}
 	
 	// Mike's getters and setters for database abstraction
 	public long getId() { return id; }
 	public void setId(long id) {
 	  this.id = id;
 	}
 	
	//SIZE
	public int size()
	{
		return clips.size();
	}
	
 	public long getOwnerUserID() { return ownerUserID; }
 	public void setOwnerUserID(long ownerUserID) {
     this.ownerUserID = ownerUserID;
 	}
 	public long getParentSequenceID() { return parentSequenceID; }
   public void setParentSequenceID(long parentSequenceID) {
     this.parentSequenceID = parentSequenceID;
   }
 	public boolean isMuted() { return isMuted; }
   public void setMuted(boolean isMuted) {
     this.isMuted = isMuted;
   }
   public boolean isLocked() { return isLocked; }
   public void setLocked(boolean isLocked) {
     this.isLocked = isLocked;
   }
   public String getPlaybackOptions() { return playbackOptions; }
   public void setPlaybackOptions(String playbackOptions) {
     this.playbackOptions = playbackOptions;
   }
   public boolean isDirty() { return isDirty; }
   public void setDirty(boolean isDirty) {
     this.isDirty = isDirty;
   }
 
   public boolean isAutoSaveEnabled() { return autoSaveEnabled; }
   public void setAutoSaveEnabled(boolean autoSaveEnabled) {
     this.autoSaveEnabled = autoSaveEnabled;
   }
   
   //SIZE
   public int size()
   {
     return clips.size();
   }
 
   public ArrayList<Clip> getClips() {
 		return this.clips;
 	}
 	public void createPBList()
 	{
 		for(int i = 0; i < clips.size(); i++)
 		{
 			playBackList[i]=clips.get(i).name;
 		}
 	}
 	//gets the track length by calculating the length of all the clips it contains
 	public long getTrackLength()
 	{
 		long sum = 0;
 		for(Clip c:clips)
 		{
 			sum+=c.duration;
 		}
 		return sum;
 	}
 	
 	//gets the clip at the end of the track
 	public Clip getClip()
 	{
 		return clips.get(clips.size());
 	}
 	
 	//gets the clip at the specified index
 	public Clip getClip(int index)
 	{
 		return clips.get(index);
 	}
 	
 	//adds the clip to back of list
 	public void putClip(Clip clip)
 	{
 		clips.add(clip);
 	}
 	
 	
 	//a method that will allow you to add a clip to the track
 	//TIMES NEED TO BE FIXED
 	public void putClip(int start, Clip clip)
 	{
 //		not sure how to do this, were going to need to find 
 //		out what the start time is then add the clip to the 
 //		array at the appropriate place
 	  
 	  // yeah, i think this is going to be straightforward but only once
 	  // i finish implementing everything else from the database -Mike
 	  
 		int i = 0;
 		for(Clip c : clips)
 		{
 			if(i==start)
 			{
 				clips.add(i, clip);
 			}
 			i++;
 		}
 	}
 //  Removes a clip based on its name
 //  TIMES NEED TO BE FIXED
 	public void removeClip(Clip clip)
 	{
 		for(Clip c : clips)
 		{
 			if(c.name == clip.name)
 			{
 				clips.remove(c);
 			}
 		}
 	}
 	
 //	will be used to select a portion of the track, then duplicate
 //	that part 'howMany' times
 //  TIMES NEED TO BE FIXED - USE MIKE'S DUPLICATE
 	// i'm leaving this one on the back burner for now, for after beta -Mike
 	public void duplicate(int start, int end, int howMany)
 	{
 		ArrayList<Clip> clist = new ArrayList<Clip>();
 		int i = 0;
 		for(Clip c : clips)
 		{
 			if(i == start)
 			{
 				for(int j = start; j<=end; j++)
 				{
 					clist.add(clips.get(j));
 				}
 			}
 			i++;
 		}
 		for(int k = 0; k<howMany; k++)
 		{
 			if(k==0)
 			{
 				clips.addAll(end, clist);
 			}
 			else
 			{
 				clips.addAll(end+clist.size(), clist);
 			}
 		}
 	}
 
 	//getVolume() and setVolume() will need to be added
 
 }
