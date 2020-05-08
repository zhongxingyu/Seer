 package com.github.signed.mp3;
 
 import com.google.common.collect.Lists;
 
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class Mp3Album {
 
     public static Mp3Album For(Path path){
         return new Mp3Album(path);
     }
 
     private Path path;
 
     public Mp3Album(Path path) {
         this.path = path;
     }
 
     public void forEachMp3File(Callback<Context> callback){
        System.out.println("process album at '"+path+"'");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path, "*.mp3")) {
            List<Path> allPath = Lists.newArrayList(directoryStream);
             Collections.sort(allPath, new Comparator<Path>() {
                 @Override
                 public int compare(Path o1, Path o2) {
                     return o1.getFileName().compareTo(o2.getFileName());
                 }
             });
 
             final int totalNumberOfTracks = allPath.size();
             int currentTrackNumber = 1;
 
             for (Path path : allPath) {
                System.out.println(path);
                 Mp3 currentMp3 = Mp3.From(path);
                 Context context = new Context(totalNumberOfTracks, currentTrackNumber, path, currentMp3);
                 callback.call(context);
                 ++currentTrackNumber;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
     public static class Context{
         public final int totalNumberOfTracks;
         public final int trackNumber;
         public final Path currentTracksPath;
         public final Mp3 currentTrack;
 
         public Context(int totalNumberOfTracks, int trackNumber, Path currentTracksPath, Mp3 currentTrack) {
             this.totalNumberOfTracks = totalNumberOfTracks;
             this.trackNumber = trackNumber;
             this.currentTracksPath = currentTracksPath;
             this.currentTrack = currentTrack;
         }
     }
 }
