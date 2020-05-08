 package pt.inevo.nuxeo.youtube;
 
public interface YouTubeUploaderProgressListener {
     void onStart();
     void onProgress(double progress);
     void onComplete();
     void onError();
 }
