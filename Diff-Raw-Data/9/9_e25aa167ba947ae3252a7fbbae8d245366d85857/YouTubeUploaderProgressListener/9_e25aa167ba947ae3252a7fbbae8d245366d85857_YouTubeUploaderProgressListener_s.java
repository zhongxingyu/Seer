 package pt.inevo.nuxeo.youtube;
 
interface YouTubeUploaderProgressListener {
     void onStart();
     void onProgress(double progress);
     void onComplete();
     void onError();
 }
