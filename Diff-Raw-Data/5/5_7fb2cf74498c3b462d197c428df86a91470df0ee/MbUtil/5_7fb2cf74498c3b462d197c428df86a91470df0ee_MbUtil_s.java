 /**
  * 
  */
 package org.jcoderz.mp3.intern.util;
 
 import java.text.Collator;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import org.jcoderz.commons.util.Assert;
 import org.jcoderz.mb.MbClient;
 import org.jcoderz.mb.TrackHelper;
 import org.jcoderz.mb.type.Includes;
 import org.jcoderz.mb.type.Medium;
 import org.jcoderz.mb.type.Recording;
 import org.jcoderz.mb.type.Release;
 import org.jcoderz.mb.type.ReleaseGroup;
 import org.jcoderz.mb.type.TrackData;
 import org.jcoderz.mb.type.Type;
 import org.jcoderz.mb.type.Medium.TrackList.Track;
 import org.jcoderz.mp3.intern.MusicBrainzMetadata;
 
 /**
  * Utility class for new musicbrainz interface.
  * 
  * @author Andreas Mandel
  *
  */
 public final class MbUtil
 {
 	private static final String CLASSNAME = Mp3Util.class.getName();
 	private static final Logger LOGGER = Logger.getLogger(CLASSNAME);
 	
     /** No instances for utility class. */
     private MbUtil()
     {
         // No instances.
     }
     
     /**
      * Checks if there are indications that the given release group is 
      * of type soundtrack.
      * Supports the 'New' MB secondary-type-list.
      * @param rg the release group to be examined.
      * @return true for release groups of type story.
      */
     public static boolean isSoundtrack (ReleaseGroup rg)
     {
     	boolean result = false;
     	result = Type.SOUNDTRACK.toString().equalsIgnoreCase(rg.getType());
     	if (!result && rg.getSecondaryTypeList() != null)
     	{
     		final List<String> secondaryType = rg.getSecondaryTypeList().getSecondaryType();
     		for (String type : secondaryType)
     		{
     			if (Type.SOUNDTRACK.toString().equalsIgnoreCase(type))
     			{
     				result = true;
     				break;
     			}
     		}
     	}
     	return result;
     }
 
     /**
      * Checks if there are indications that the given release group is 
      * of type story (either Spokenword, Audiobook, or Interview).
      * Supports the 'New' MB secondary-type-list.
      * @param rg the release group to be examined.
      * @return true for release groups of type story.
      */
     public static boolean isStory (ReleaseGroup rg)
     {
     	boolean result = false;
     	result = isStory(rg.getType());
     	if (!result && rg.getSecondaryTypeList() != null)
     	{
     		final List<String> secondaryType = rg.getSecondaryTypeList().getSecondaryType();
     		for (String type : secondaryType)
     		{
     			if (isStory(type))
     			{
     				result = true;
     				break;
     			}
     		}
     	}
     	return result;
     }
     
     private static boolean isStory(String type)
     {
         return Type.SPOKENWORD.toString().equalsIgnoreCase(type) 
             || Type.AUDIOBOOK.toString().equalsIgnoreCase(type)
             || Type.INTERVIEW.toString().equalsIgnoreCase(type);
     	
     }
     
     /**
      * This method tries to handle the situation where the track id changed within 
      * musicbrainz database. This can happen if releases are merged and happens quite
      * frequently in the ngs schema of musicbrainz, where the same song in different
      * albums appears with the same track id.
      *  
      * @param mbClient the connection to the musicbrainz server used if lookups are needed.
      * @param mbData the metadata of the song we take care for 
      * @param album the album in which the song (mbData) is expected to be 
      * @return the TrackData object containing Release, Medium and Track information uniquely 
      *  identifying the song. 
      */
     public static TrackData getTrackDataWithIdUpdate (
     		MbClient mbClient, MusicBrainzMetadata mbData, Release album)
     {
         final String currentTrackId = mbData.getFileId();
         Assert.notNull(currentTrackId, "mbData.getFileId()");
         
         final Release theAlbum = ensureAlbumContainsMediumInfo(mbClient, album);
         TrackData track = findTrackData(mbClient, theAlbum, mbData);
         if (track.getMedium() == null)
         {   // still not found, search for alternative / updated track id.
         	track = findTrackDataByRecording(mbClient, mbData, theAlbum);
         }
         if (track.getMedium() == null && theAlbum != null)
         {
             Assert.fail("Cold not find track " + currentTrackId + " in Release " + theAlbum.getId());
         }
 
         return track;
     }
 
 	private static TrackData findTrackDataByRecording(MbClient mbClient,
 			MusicBrainzMetadata mbData, final Release theAlbum) 
 	{
 		final String fileId = mbData.getFileId();
 		// Try update, get the recording with the old track id.
 		final Recording recording 
 			= mbClient.getRecording(fileId, Collections.<Includes> emptySet());
 		TrackData track = new TrackData(null, null, null);
 		if (recording != null) {
 			// There might be different titles in the album vs in the recording
 			// :-(
 			// Eg:
 			// http://musicbrainz.org/release/58da2396-81a6-4e85-bab9-e623d42840bd
 			// http://musicbrainz.org/recording/0ca12a54-b5b1-4029-b4eb-4824eb210845
 			// Will You Still Love Me Tomorrow -> Will You Love Me Tomorrow
 
 			try 
 			{
 				mbData.setFileId(recording.getId());
 				track = findTrackData(mbClient, theAlbum, mbData);
 				if (track.getMedium() != null)
 				{
 					LOGGER.info("ID Update " + fileId + " -> " + recording.getId() + " for " + mbData);
 				}
 			} 
 			finally 
 			{
 				mbData.setFileId(fileId);
 			}
 		}
 		return track;
 	}
 
 	private static Release ensureAlbumContainsMediumInfo(MbClient mbClient, Release album) 
 	{
 		final Release theAlbum;
 		if (album.getMediumList() == null || album.getMediumList().getMedium() == null)
         {
             theAlbum = mbClient.getRelease(album.getId());
         }
         else
         {
             theAlbum = album;
         }
 		return theAlbum;
 	}
 
     public static TrackData findTrackData (
     		MbClient mbClient, Release album, MusicBrainzMetadata mbData) 
 
     {
         Medium medium = null;
         Track track = null;
         
         final Release theAlbum = album;
         final String fileId = mbData.getFileId();
 out:
         for (Medium m : theAlbum.getMediumList().getMedium())
         {
         	final int trackPos 
         		= mbData.getTrackNumber() - m.getTrackList().getOffset().intValue();
         	if (trackPos > m.getTrackList().getDefTrack().size())
         	{
         		continue;
         	}
         	final Track t = m.getTrackList().getDefTrack().get(trackPos - 1);
     		if (fileId.equals(t.getRecording().getId()))
             {   // id match wins always!
                 medium = m;
                 track = t;
                 break out;
             }
     		else if (compare(mbData, t))
     		{
             	if (medium == null)
             	{
             		medium = m;
             		track = t;
             	}
             	else
             	{
             		LOGGER.warning("Ambiguous recording can not be matched - will be ignored"
             				+ t.getRecording().getId() + " vs. " + track.getRecording().getId()
             				+ " in album " + theAlbum.getId());
             		medium = null;
             		track = null;
             		break out;
             	}
         	}
         }
         return new TrackData(theAlbum,  medium, track);
     }
 
 	private static boolean compare(MusicBrainzMetadata mbData, Track t) 
 	{
         final Long newLength = TrackHelper.getLength(t);
         return (compare(mbData.getTitle(), TrackHelper.getTitle(t))
         	&& ((newLength == null || 
                    (Math.abs(mbData.getLengthInMilliSeconds() - newLength) > 5000))));
 	}
 
 	private static boolean compare (String a, String b)
     {
        if (a == null)
        {
           a =  "";
        }
        if (b == null)
        {
           b = "";
        }
        a = a.replaceAll("\\(.*\\)", "");
        b = b.replaceAll("\\(.*\\)", "");
        a = a.replaceAll("[^A-Za-z0-9]", "");
        b = b.replaceAll("[^A-Za-z0-9]", "");
        final int len = Math.min(a.length(), b.length()); 
        if (a.length() > 29)
        {
           a = a.substring(0, Math.max(29, len));
        }
        if (b.length() > 29)
        {
           b = b.substring(0, Math.max(29, len));
        }
        final Collator collator = Collator.getInstance(new Locale("en", "US"));
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(a, b) == 0;
     }
 
 }
