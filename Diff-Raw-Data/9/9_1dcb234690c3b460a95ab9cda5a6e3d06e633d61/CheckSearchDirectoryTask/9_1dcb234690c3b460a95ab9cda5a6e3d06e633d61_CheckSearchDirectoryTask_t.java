 /*
  *  Copyright (C) 2011 wusel
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.wusel.partyplayer.tasks;
 
 import de.wusel.partyplayer.cli.FileCallback;
 import de.wusel.partyplayer.cli.FileSearcher;
 import de.wusel.partyplayer.cli.TagReader;
 import de.wusel.partyplayer.cli.TrackInfo;
 import de.wusel.partyplayer.library.Library;
 import de.wusel.partyplayer.settings.Settings;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 
 /**
  *
  * @author wusel
  */
 public class CheckSearchDirectoryTask extends Task<Void, TrackInfo> {
 
     private static final Logger log = Logger.getLogger(CheckSearchDirectoryTask.class);
     private final Library library;
     private final Settings settings;
     private int maxFiles = 0;
     private int currentFiles = 0;
 
     public CheckSearchDirectoryTask(Application application, Library library, Settings settings) {
         super(application);
         this.library = library;
         this.settings = settings;
     }
 
     @Override
     protected Void doInBackground() throws Exception {
         final ExecutorService executorService = Executors.newFixedThreadPool(4);
 
         FileSearcher fileSearcher = new FileSearcher(settings);
         fileSearcher.search(new FileCallback() {
 
             @Override
             public void fileFound(final File file) {
 
                 if (!library.containsSong(file.getAbsolutePath())) {
 
                     maxFiles++;
                     message("available", currentFiles, maxFiles);
                     setProgress(currentFiles, 0, maxFiles);
                     executorService.submit(new Runnable() {
 
                         @Override
                         public void run() {
                             try {
                                 publish(TagReader.read(file));
                             } catch (IOException ex) {
                                 log.error("error while parsing trackinfo", ex);
                             }
                         }
                     });
                 }
             }
         });
         executorService.shutdown();
         executorService.awaitTermination(1000, TimeUnit.DAYS);
        setProgress(0, 0, 100);
        message("finished", library.getSongCount());
         return null;
     }
 
     @Override
     protected void process(List<TrackInfo> values) {
         for (TrackInfo trackInfo : values) {
             currentFiles++;
             if (trackInfo != null) {
                 library.addTrackInfo(trackInfo);
                 setProgress(currentFiles, 0, maxFiles);
                 message("available", currentFiles, maxFiles);
             }
         }
     }
 
 }
