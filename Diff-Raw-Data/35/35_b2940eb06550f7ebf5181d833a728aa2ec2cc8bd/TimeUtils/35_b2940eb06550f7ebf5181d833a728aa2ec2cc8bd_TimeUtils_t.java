 package com.music.fmv.utils;
 
import com.music.fmv.models.notdbmodels.InternetSong;
import com.music.fmv.models.notdbmodels.PlayAbleSong;

 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * User: Vitalii Lebedynskyi
  * Date: 10/10/13
  * Time: 5:15 PM
  */
 public class TimeUtils {
     public static final SimpleDateFormat MM_SS_DATE_FORMAT = new SimpleDateFormat("mm:ss");
 
    public static CharSequence extractTimeFromSong(PlayAbleSong song) {
        long duration;
        if (song instanceof InternetSong){
            duration = song.getDuration() * 1000;
        }else{
            duration = song.getDuration();
        }
        Date d = new Date(duration);
         return MM_SS_DATE_FORMAT.format(d);
     }
 }
