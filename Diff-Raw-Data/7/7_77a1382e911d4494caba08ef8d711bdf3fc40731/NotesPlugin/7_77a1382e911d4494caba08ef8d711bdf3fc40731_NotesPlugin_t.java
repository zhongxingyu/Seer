 /* Copyright (c) 2013, Ian Dees
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.openstreetmap.josm.plugins.notes;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.Collection;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.actions.UploadAction;
 import org.openstreetmap.josm.actions.upload.UploadHook;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.gui.MapFrame;
 import org.openstreetmap.josm.gui.MapView;
 import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
 import org.openstreetmap.josm.gui.NavigatableComponent;
 import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
 import org.openstreetmap.josm.gui.layer.Layer;
 import org.openstreetmap.josm.gui.layer.OsmDataLayer;
 import org.openstreetmap.josm.plugins.Plugin;
 import org.openstreetmap.josm.plugins.PluginInformation;
 import org.openstreetmap.josm.plugins.notes.api.DownloadAction;
 import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
 import org.openstreetmap.josm.tools.ImageProvider;
 
 /**
  * Shows notes from OpenStreetMap
  *
  * @author Henrik Niehaus (henrik dot niehaus at gmx dot de)
  */
 public class NotesPlugin extends Plugin implements LayerChangeListener, ZoomChangeListener {
 
     private List<Note> allNotes = new CopyOnWriteArrayList<Note>();
 
     private UploadHook uploadHook;
 
     private NotesDialog dialog;
 
     private NotesLayer layer;
 
     private DownloadAction download = new DownloadAction();
     private Bounds lastBbox;
     private TimerTask task;
     private Timer timer = new Timer();
 
     private ComponentAdapter viewChangeAdapter = new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
             grabNotes();
         }
     };
 
     public NotesPlugin(PluginInformation info) {
         super(info);
         initConfig();
     }
 
     @Override
     public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
         if (newFrame != null) {
             dialog = new NotesDialog(this);
             newFrame.addToggleDialog(dialog);
             if (newFrame.mapView != null) {
                 newFrame.mapView.addComponentListener(viewChangeAdapter);
             }
 
             MapView.addLayerChangeListener(this);
 
             uploadHook = new NotesUploadHook();
             UploadAction.registerUploadHook(uploadHook);
         } else {
             MapView.removeLayerChangeListener(this);
             UploadAction.unregisterUploadHook(uploadHook);
             if (oldFrame.mapView != null) {
                 oldFrame.mapView.removeComponentListener(viewChangeAdapter);
             }
             uploadHook = null;
             dialog = null;
         }
     }
 
     private void initConfig() {
         String debug = Main.pref.get(ConfigKeys.NOTES_API_DISABLED);
         if(debug == null || debug.length() == 0) {
             debug = "false";
             Main.pref.put(ConfigKeys.NOTES_API_DISABLED, debug);
         }
 
         String auto_download = Main.pref.get(ConfigKeys.NOTES_AUTO_DOWNLOAD);
         if(auto_download == null || auto_download.length() == 0) {
             auto_download = "true";
             Main.pref.put(ConfigKeys.NOTES_AUTO_DOWNLOAD, auto_download);
         }
     }
 
     /**
      * Determines the bounds of the current selected layer
      * @return
      */
     protected Bounds bounds(){
         MapView mv = Main.map.mapView;
         return new Bounds(
             mv.getLatLon(0, mv.getHeight()),
             mv.getLatLon(mv.getWidth(), 0));
     }
 
     public void updateData() {
         // disable the dialog
         try {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     if (dialog != null) {
                         dialog.setEnabled(false);
                     }
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // store the current selected node
         Note selectedNote = getDialog().getSelectedNote();
 
         // determine the bounds of the currently visible area
         Bounds bounds = null;
         try {
             bounds = bounds();
         } catch (Exception e) {
             // something went wrong, probably the mapview isn't fully initialized
             System.err.println("Notes: Couldn't determine bounds of currently visible rect. Cancel auto update");
             return;
         }
 
 
         // download data for the new bounds, if the plugin is not in offline mode
         if(!Main.pref.getBoolean(ConfigKeys.NOTES_API_OFFLINE)) {
             try {
                 // download the data
                 download.execute(allNotes, bounds);
 
                 // display the parsed data
                 if(!allNotes.isEmpty() && dialog.isDialogShowing()) {
                     // if the map layer has been closed, while we are requesting the osb db,
                     // we don't have to update the gui, because the user is not interested
                     // in this area anymore
                     if(Main.map != null && Main.map.mapView != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateGui();
                            }
                        });
                     }
                 }
             } catch (Exception e) {
                 if (e instanceof java.net.UnknownHostException) {
                     String message = String.format(tr("Unknown Host: %s - Possibly there is no connection to the Internet.")
                             , e.getMessage());
                     JOptionPane.showMessageDialog(Main.parent, message);
                 } else {
                     JOptionPane.showMessageDialog(Main.parent, e.getMessage());
                 }
                 e.printStackTrace();
             }
         }
 
 
         // restore node selection
         dialog.setSelectedNote(selectedNote);
 
         // enable the dialog
         try {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     dialog.setEnabled(true);
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void updateGui() {
         // update dialog
         dialog.update(allNotes);
 
         // create a new layer if necessary
         updateLayer(allNotes);
 
         // repaint view, so that changes get visible
         Main.map.mapView.repaint();
     }
 
     private synchronized void updateLayer(List<Note> osbData) {
         if(layer == null) {
             layer = new NotesLayer(osbData, "Notes", dialog);
             Main.main.addLayer(layer);
         }
     }
 
     public static ImageIcon loadIcon(String name) {
         return ImageProvider.get(name);
     }
 
     public void activeLayerChange(Layer oldLayer, Layer newLayer) {}
 
     public void layerAdded(Layer newLayer) {
         if(newLayer instanceof OsmDataLayer) {
             // start the auto download loop
             NavigatableComponent.addZoomChangeListener(this);
         }
     }
 
     public void layerRemoved(Layer oldLayer) {
         if(oldLayer == layer) {
             NavigatableComponent.removeZoomChangeListener(this);
             layer = null;
         }
     }
 
     public NotesLayer getLayer() {
         return layer;
     }
 
     public Collection<Note> getDataSet() {
         return allNotes;
     }
 
     public NotesDialog getDialog() {
         return dialog;
     }
 
     @Override
     public void zoomChanged() {
         grabNotes();
     }
 
     private void grabNotes() {
         if (Main.map == null)
             return;
         MapView mv = Main.map.mapView;
         Bounds bbox = mv.getLatLonBounds(mv.getBounds());
 
         // Have the user changed view since last time
         boolean active = isActive();
         if (active && (lastBbox == null || !lastBbox.equals(bbox))) {
             if (task != null) {
                 task.cancel();
             }
 
             // wait 500ms before downloading in case the user is in the middle
             // of a pan/zoom
             task = new DownloadNotesTask();
             timer.schedule(task, 500);
             lastBbox = bbox;
         }
     }
 
     private boolean isActive() {
         return Main.pref.getBoolean(ConfigKeys.NOTES_AUTO_DOWNLOAD)
                 && !Main.pref.getBoolean(ConfigKeys.NOTES_API_OFFLINE)
                 && getDialog() != null
                 && getDialog().isDialogShowing();
     }
 
     private class DownloadNotesTask extends TimerTask {
 
         @Override
         public void run() {
             if (!isActive())
                 return;
 
             updateData();
         }
 
     }
 
 }
