 import java.io.File;
 import java.util.Vector;
 
 // remember:
 // master: where the info comes from which images to use
 // slave: where the originals reside, we copy from here
 // target: where the originals from slave should be copied to
 public class AlbumDirectorySearch {
     // find the directory where this album images should be copied to from slave directory
     public File FindSuitableTargetDirectory4Album(Album a){
         return null;
     }
 
     // is there already a targetdirectory and are all images already there?
     public boolean IsAlbumAlreadyInSync(Album a){
         return false;
     }
 
     // walk through the configured directories and search for the directory where the originals reside (slave directory)
    public File FindSlaveDirectory4Album(Album a, Vector<File> configuredParentPathsToSearchIn){
        return null;
    }
 }
