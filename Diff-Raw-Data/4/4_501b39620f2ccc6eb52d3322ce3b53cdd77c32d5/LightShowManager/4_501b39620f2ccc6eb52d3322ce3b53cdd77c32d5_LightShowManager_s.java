 package com.partyrock.show;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import com.partyrock.LightMaster;
 import com.partyrock.anim.Animation;
 import com.partyrock.anim.ElementAnimation;
 import com.partyrock.element.ElementController;
 import com.partyrock.gui.select.Selection;
 import com.partyrock.music.MP3;
 import com.partyrock.settings.PersistentSettings;
 
 /**
  * Manages everything associated with the show specifically (such as the list of animations and MP3 file)
  * 
  * @author Matthew
  * 
  */
 @SuppressWarnings("unused")
 public class LightShowManager {
     private PersistentSettings showFile;
     private MP3 music;
     private LightMaster master;
     private ConcurrentSkipListMap<Integer, List<Animation>> animations;
     private HashMap<ElementController, Set<Animation>> animationsByElement;
     private double nextStartTime;
     private boolean isPlaying;
     private boolean isPaused;
 
     public LightShowManager(LightMaster master) {
         this.master = master;
 
         animations = new ConcurrentSkipListMap<Integer, List<Animation>>();
         animationsByElement = new HashMap<ElementController, Set<Animation>>();
     }
 
     /**
      * Adds an animation at the given time
      * 
      * @param animation The animation to add
      */
     public void addAnimation(Animation animation) {
         // Insert into the animations list by start time
         int startTime = animation.getStartTime();
         if (!animations.containsKey(startTime)) {
             animations.put(startTime, Collections.synchronizedList(new ArrayList<Animation>()));
         }
 
         animations.get(startTime).add(animation);
 
         // Also insert by element because it's way easier to render that way
         if (animation instanceof ElementAnimation) {
             ElementAnimation e = (ElementAnimation) animation;
 
             for (ElementController controller : e.getElements()) {
                 if (!animationsByElement.containsKey(controller)) {
                     animationsByElement.put(controller, Collections.synchronizedSet(new TreeSet<Animation>()));
                 }
 
                 animationsByElement.get(controller).add(animation);
             }
         }
     }
 
     public List<Animation> getAnimationsForTime(int key) {
         return animations.get(key);
     }
 
     public boolean hasAnimationsAtTime(int key) {
         return animations.containsKey(key);
     }
 
     public void loadShow(File f) {
         // TODO Auto-generated method stub
 
     }
 
     /**
      * Loads a new file as the music
      * 
      * @param f
      */
     public void loadMusic(File f) {
         music = new MP3(f);
     }
 
     /**
      * Returns the MP3 file representing the music
      * 
      * @return
      */
     public MP3 getMusic() {
         return music;
     }
 
     /**
      * Call to start playing the show, which will use the given selection if possible
      */
     public void play() {
         Selection selection;
         if ((selection = master.getWindowManager().getMain().getSelection()) == null) {
             startPlay();
             playShow(nextStartTime, -1 / 1000.0);
         } else {
             playSelection(selection);
             playShow(selection.start, selection.duration);
         }
     }
 
     /**
      * Will play a given selection from the music
      * 
      * @param selection The selection to play
      */
     public void playSelection(Selection selection) {
         nextStartTime = selection.start;
         startPlay();
     }
 
     /**
      * Actually starts playing the music
      */
     private void startPlay() {
         isPlaying = true;
         isPaused = false;
         playMusic();
     }
 
     /**
      * Pauses the show
      */
     public void pause() {
         isPlaying = false;
         isPaused = true;
         pauseMusic();
     }
 
     /**
      * Plays the music
      */
     private void playMusic() {
         if (music == null) {
             return;
         }
 
         if (music.getStartTime() != nextStartTime || music.getCurrentTime() != nextStartTime) {
             makeNewMusic();
             if (!music.isPlaying()) {
                 music.play(nextStartTime);
             }
         } else {
             music.play(nextStartTime);
         }
     }
 
     /**
      * Pauses the music
      */
     private void pauseMusic() {
         if (music == null) {
             return;
         }
         music.pause();
     }
 
     private void stopMusic() {
         if (music == null) {
             return;
         }
 
         music.stop();
     }
 
     public void playShow(double startTime, double duration) {
         LightShow show = new LightShow(this, (int) (startTime * 1000), (int) (duration * 1000));
         show.start();
     }
 
     /**
      * Returns the duration of the music, or -1 if no music is loaded
      */
     public double getMusicDuration() {
         if (music == null) {
             return -1;
         }
 
         return music.getDuration();
     }
 
     /**
      * Returns the current position we're at in the music, or -1 if no music is loaded
      * 
      * @return
      */
     public double getCurrentTime() {
         if (music == null) {
             return -1;
         }
 
         double currentTime = music.getCurrentTime();
 
         // Returns the next start time so that even if music isn't playing, the line still renders
         if (currentTime == -1) {
             return nextStartTime;
         }
 
         return currentTime;
     }
 
     /**
      * Change the current time to something new
      * 
      * @param time The time to change to
      */
     public void setCurrentTime(double time) {
         if (time < 0) {
             time = 0;
         }
 
         nextStartTime = time;
 
         makeNewMusic();
     }
 
     /**
      * Makes a new MP3 file (so, for example, changing the position)
      */
     private void makeNewMusic() {
         File f = music.getFile();
         boolean playing = music.isPlaying();
         music.stop();
         music = new MP3(f);
         if (playing) {
             music.play(nextStartTime);
         }
     }
 
     public void toggle() {
         if (!isPlaying()) {
             play();
         } else {
             pause();
         }
     }
 
     public boolean isPlaying() {
         return isPlaying;
     }
 
     public boolean isPaused() {
         return isPaused;
     }
 
     public void shutdown() {
         stopMusic();
     }
 
     public Set<Animation> getAnimationsForElement(ElementController element) {
         return animationsByElement.get(element);
     }
 }
