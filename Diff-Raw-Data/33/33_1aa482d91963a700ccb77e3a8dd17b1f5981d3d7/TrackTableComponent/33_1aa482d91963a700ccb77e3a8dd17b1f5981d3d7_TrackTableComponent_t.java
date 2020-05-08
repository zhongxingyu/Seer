 /*
  * Copyright (c) 2011, dan.clark@nekocode.org
  *
  * Licensed under FreeBSD license.  See README for details.
  */
 
 package org.nekocode.nowplaying.components.modes.tagsdnd;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.divxdede.swing.busy.BusyModel;
 import org.divxdede.swing.busy.JBusyComponent;
 import org.divxdede.swing.busy.ui.BasicBusyLayerUI;
 import org.divxdede.swing.busy.ui.BusyLayerUI;
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 import org.nekocode.nowplaying.MediaPlayer;
 import org.nekocode.nowplaying.components.icons.SpinningDialBusyIcon;
 import org.nekocode.nowplaying.internals.NamedThreadFactory;
 import org.nekocode.nowplaying.objects.Track;
 import org.nekocode.nowplaying.objects.UnknownTrack;
 import org.nekocode.nowplaying.tags.TagModel;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static java.lang.String.format;
 
 /**
  * A table of tracks.  This component already has quite a bit of customized behavior, reducing
  * coding of components that want to inherit that behavior.
  * <ul>
  * <li>Supports drag and drop of tiles onto the component.</li>
  * <li>Able to lock the component in a busy state using @{link #setBusy(boolean)}</li>
  * <li>Automatically resizes the columns to match the input length.</li>
  * <li>Functions to access the tracks contained in the table.</li>
  * <li>Table is contained in a JScrollPane</li>
  * </ul>
  */
 public class TrackTableComponent extends JPanel {
     private static final Logger log = LogManager.getLogger(TrackTableComponent.class);
 
     private final ExecutorService workerThread = Executors.newSingleThreadExecutor(
             new NamedThreadFactory("TrackTable-AddFiles", false));
     private final MediaPlayer mediaPlayer;
     private final TrackTableModel dataModel;
     private final BusyModel busyModel;
 
     private final JScrollPane scrollpane;
     private final Border defaultBorder;
     private final Border highlightBorder;
     private final TagModel tagModel;
     private final TrackTable trackTable;
     private final Collection<ChangeListener> changeListeners;
 
     /**
      * Constructor.
      *
      * @param mediaPlayer nowplaying... media play
      * @param tagModel tag model
      */
     public TrackTableComponent(MediaPlayer mediaPlayer, TagModel tagModel) {
         super(new BorderLayout());
 
         this.mediaPlayer = mediaPlayer;
         this.tagModel = tagModel;
         changeListeners = new HashSet<>();
         dataModel = new TrackTableModel(tagModel);
         tagModel.addTagChangeListener(dataModel);
         trackTable = new TrackTable(dataModel);
 
         scrollpane = new JScrollPane(trackTable);
         scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
         defaultBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
         highlightBorder = BorderFactory.createLineBorder(Color.blue, 2, false);
         setBorder(false);
 
         BusyLayerUI busyLayerUI = new BasicBusyLayerUI(0, 0.85f, Color.WHITE);
         JBusyComponent<JComponent> busyComponent = new JBusyComponent<>(scrollpane, busyLayerUI);
 
         // use the BusyModel to control the busy state of this component
         busyModel = busyComponent.getBusyModel();
         busyModel.setCancellable(false);
         busyModel.setDeterminate(false);
         BasicBusyLayerUI ui = new BasicBusyLayerUI();
         ui.setBusyIcon(new SpinningDialBusyIcon(64, 64));
         busyComponent.setBusyLayerUI(ui);
 
         this.add(busyComponent, BorderLayout.CENTER);
         // the drop target needs to be created, but doesn't need to be attached to anything
         //noinspection UnusedAssignment
         DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new FileDropListener());
 
         // create an action that will delete the selected rows if the delete key is pressed
         Action deleteRow = new AbstractAction() {
               /* (non-Javadoc)
                * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                */
               @Override
               public void actionPerformed(ActionEvent e) {
                   trackTable.deleteSelectedRows();
               }};
         trackTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                 KeyStroke.getKeyStroke("DELETE"), "deleteRow");
         trackTable.getActionMap().put("deleteRow", deleteRow);
 
     }
 
     /**
      * Reset the table - delete all rows and resize columns back to their default widths.
      */
     public void clear() {
         dataModel.clear();
         trackTable.shrinkColumns();
         fireChangeEvent(new TrackTableChangeEvent(TrackTableChangeEvent.ChangeType.CLEAR, null, this));
     }
 
     /**
      * Change the border of this component to indicate that a drag-and-drop operation is in progress.
      *
      * @param dragInProgress is dnd operation beginning?  (false means it is ending)
      */
     private void setBorder(boolean dragInProgress) {
         if (dragInProgress) {
             scrollpane.setBorder(highlightBorder);
         } else {
             scrollpane.setBorder(defaultBorder);
         }
     }
 
     /**
      * Return all tracks stored in this component.
      *
      * @return unsorted list of tracks
      */
     public List<Track> getTracks() {
         return dataModel.getTracks();
     }
 
     /**
      * Add a track to this component.
      *
      * @param track track to add.  duplicates allowed
      */
     public void addTrack(Track track) {
         dataModel.addTrack(track);
         fireChangeEvent(new TrackTableChangeEvent(TrackTableChangeEvent.ChangeType.ADD, track, this));
     }
 
     /**
      * Set this component as busy or not.  Not interactions will be possible while this component
      * is set to busy.
      *
      * @param locked busy state
      */
     public void setBusy(boolean locked) {
         busyModel.setBusy(locked);
     }
 
     /**
      * Free resources held by this object.
      */
     public void dispose() {
         tagModel.removeTagChangeListener(dataModel);
     }
 
     /**
      * Registers a ChangeListener to receive TrackTableChangeEvents.
      *
      * @param l change listener
      */
     public void addChangeListener(ChangeListener l) {
         changeListeners.add(l);
     }
 
     /**
      * Unregisters a ChangeListener so it no longer receives TrackTableChangeEvents.
      *
      * @param l change listener
      */
     public void removeChangeListener(ChangeListener l) {
         changeListeners.remove(l);
     }
 
     /**
      * Notify all registered listeners that a change has occured.
      *
      * @param e change event
      */
     public void fireChangeEvent(TrackTableChangeEvent e) {
         for (ChangeListener l : changeListeners) {
             l.stateChanged(e);
         }
     }
 
     public void shutdown() {
         workerThread.shutdown();
     }
 
     /**
      * Handles dragging and dropping into this window.
      */
     private class FileDropListener implements DropTargetListener {
 
         /**
          * Approves lists of files.  Changes border to indicate drag is in progress.
          *
          * @param dtde drag event
          */
         @Override
         public void dragEnter(DropTargetDragEvent dtde) {
             if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                 setBorder(true);
                 dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
             } else {
                 setBorder(false);
                 dtde.rejectDrag();
             }
         }
 
         /**
          * Changes border to indicate no drag is in progress.
          *
          * @param dte drop event
          */
         @Override
         public void dragExit(DropTargetEvent dte) {
             setBorder(false);
         }
 
         @Override
         public void drop(DropTargetDropEvent dtde) {
             setBorder(false);
             if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                 dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                 try {
                     @SuppressWarnings("unchecked")
                     List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                     workerThread.execute(new AddFiles(files));
                 } catch (UnsupportedFlavorException | IOException e) {
                     log.error("error processing file drop", e);
                 }
             } else {
                 dtde.rejectDrop();
             }
         }
 
         @Override
         public void dragOver(DropTargetDragEvent dtde) {
         }
 
         @Override
         public void dropActionChanged(DropTargetDragEvent dtde) {
             log.debug("dropActionChanged: " + dtde.toString());
         }
     }
 
     /**
      * Adds files collected via drag-and-drop to the component.
      */
     private class AddFiles implements Runnable {
         private final List<File> files;
 
         public AddFiles(List<File> files) {
             this.files = new ArrayList<>(files);
         }
 
         public void run() {
             setBusy(true);
 
             // dig into directories recursively
             for (int i = 0; i < files.size(); i++) {
                 if (files.get(i).isDirectory()) {
                     log.debug("Adding subdirectories of " + files.get(i));
                     files.addAll(Arrays.asList(files.get(i).listFiles()));
                     files.remove(i);
                     i--; // to account for the fact that we removed the directory
                 }
             }
 
            Map<String, Collection<Track>> foundTracks = new HashMap<>();

             for (final File file : files) {
                 try {
                     log.debug("Loading file: " + file.getAbsolutePath());
 
                     AudioFile f = AudioFileIO.read(file);
                     Tag tag = f.getTag();
 
                     final String title = tag.getFirst(FieldKey.TITLE);
                     final String artist = tag.getFirst(FieldKey.ARTIST);
                     final String album = tag.getFirst(FieldKey.ALBUM);
                     String key = format("{title=\"%s\", artist=\"%s\", album=\"%s\"}", title, artist, album);
 
                    if (!foundTracks.containsKey(key)) {
                        Collection<Track> tracks = mediaPlayer.findTracks(title, artist, album);
                        // take advantage of MonkeyTunes bug (it turns "AND" queries into "OR" queries)
                        for (Track track : tracks) {
                            key = format("{title=\"%s\", artist=\"%s\", album=\"%s\"}",
                                    track.getTitle(), track.getArtist(), track.getAlbum());
                            foundTracks.computeIfAbsent(key, k -> new ArrayList<>());
                            foundTracks.get(key).add(track);
                        }
                    }
                    Collection<Track> tracks = foundTracks.get(key);

                     if (tracks.isEmpty()) {
                         log.info("Unable to find match for " + key);
                         addTrack(new UnknownTrack() {
                             @Override
                             public String getTitle() {
                                 return title;
                             }
 
                             @Override
                             public String getAlbum() {
                                 return artist;
                             }
 
                             @Override
                             public String getArtist() {
                                 return album;
                             }
                         });
                     } else {
                         log.info("Successfully found match for " + key);
                         if (tracks.size() != 1) {
                             log.warn("Search resulted in multiple tracks for " + key);
                         }
                         tracks.forEach(TrackTableComponent.this::addTrack);
                     }
                 } catch (Exception e) {
                     log.debug("Couldn't load track " + file.getAbsolutePath(), e);
                     // probably not a big deal - jaudiotagger didn't recognize this file as having any tags
                     addTrack(new UnknownTrack() {
                         @Override
                         public String getTitle() {
                             return file.getAbsolutePath();
                         }
                     });
                 }
             }
             setBusy(false);
         }
     }
 
     /**
      * A Listener interface for TrackTableComponent that is notified of track additions and removals.
      */
     public static class TrackTableChangeEvent extends ChangeEvent {
         private final ChangeType type;
         private final Track track;
 
         public static enum ChangeType { ADD, REMOVE, CLEAR }
 
         /**
          * Constructs a ChangeEvent object.
          *
          * @param type type of track change (add, remove or clear all)
          * @param track the track affected (null if type is CLEAR)
          * @param source the Object that is the source of the event
          *               (typically <code>this</code>)
          */
         public TrackTableChangeEvent(ChangeType type, Track track, TrackTableComponent source) {
             super(source);
             this.type = type;
             this.track = track;
         }
 
         public ChangeType getType() {
             return type;
         }
 
         public Track getTrack() {
             return track;
         }
     }
 
 }
