 package org.atlasapi.remotesite.music.musicbrainz;
 
import com.google.common.base.Charsets;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
 import it.unimi.dsi.fastutil.longs.Long2LongMap;
 import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.regex.Pattern;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Song;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.joda.time.Duration;
 
 /**
  */
 public class MusicBrainzProcessor {
 
    private static final ByteArrayList BUFFER = new ByteArrayList();
     private static final int NOT_FOUND = -1;
     //
     private static final Pattern TAB_PATTERN = Pattern.compile("\\t");
     private static final String MUSIC_BRAINZ_RECORDING_URI = "http://musicbrainz.org/recording/";
     private static final String MUSIC_BRAINZ_ARTIST_URI = "http://musicbrainz.org/artist/";
     private static final String MUSIC_BRAINZ_CURIE_PREFIX = "mb:";
     private static final String RECORDINGS = "recording";
     private static final String ISRCS = "isrc";
     private static final String TRACKS = "track_name";
     private static final String CREDITS = "artist_credit_name";
     private static final String ARTISTS = "artist";
     private static final String NAMES = "artist_name";
 
     public void process(File dataDir, AdapterLog log, ContentWriter contentWriter, ItemsPeopleWriter peopleWriter) throws Exception {
         BufferedReader recordingsFile = getReader(dataDir, RECORDINGS);
         RandomAccessFile isrcsFile = getRaf(dataDir, ISRCS);
         RandomAccessFile tracksFile = getRaf(dataDir, TRACKS);
         RandomAccessFile creditsFile = getRaf(dataDir, CREDITS);
         RandomAccessFile artistsFile = getRaf(dataDir, ARTISTS);
         RandomAccessFile namesFile = getRaf(dataDir, NAMES);
         Long2LongMap isrcsIndex = buildIndex(isrcsFile, 1);
         Long2LongMap tracksIndex = buildIndex(tracksFile, 0);
         Long2LongMap creditsIndex = buildIndex(creditsFile, 0);
         Long2LongMap artistsIndex = buildIndex(artistsFile, 0);
         Long2LongMap namesIndex = buildIndex(namesFile, 0);
 
         try {
             String currentRecording = recordingsFile.readLine();
             while (currentRecording != null) {
                 try {
                     Iterable<String> currentRecordingData = Splitter.on(TAB_PATTERN).split(currentRecording);
                     String recordUri = MUSIC_BRAINZ_RECORDING_URI + Iterables.get(currentRecordingData, 1);
                     String recordCurie = MUSIC_BRAINZ_CURIE_PREFIX + Iterables.get(currentRecordingData, 1);
 
                     Song song = new Song(recordUri, recordCurie, Publisher.MUSIC_BRAINZ);
                     song.setMediaType(MediaType.AUDIO);
                     song.setSpecialization(Specialization.MUSIC);
 
                     try {
                         Long duration = Long.parseLong(Iterables.get(currentRecordingData, 4));
                         if (duration != null) {
                             song.setDuration(Duration.millis(duration));
                         }
                     } catch (NumberFormatException ex) {
                     }
 
                     String currentIsrc = readData(isrcsFile, isrcsIndex, Iterables.get(currentRecordingData, 0));
                     if (currentIsrc != null) {
                         Iterable<String> currentIsrcData = Splitter.on(TAB_PATTERN).split(currentIsrc);
                         String isrc = Iterables.get(currentIsrcData, 2);
                         song.setIsrc(isrc);
                     }
 
                     String currentTrack = readData(tracksFile, tracksIndex, Iterables.get(currentRecordingData, 2));
                     if (currentTrack != null) {
                         Iterable<String> currentTrackData = Splitter.on(TAB_PATTERN).split(currentTrack);
                         String title = Iterables.get(currentTrackData, 1);
                         song.setTitle(title);
                     }
 
                     String currentCredits = readData(creditsFile, creditsIndex, Iterables.get(currentRecordingData, 3));
                     if (currentCredits != null) {
                         Iterable<String> currentCreditsData = Splitter.on(TAB_PATTERN).split(currentCredits);
                         String currentArtist = readData(artistsFile, artistsIndex, Iterables.get(currentCreditsData, 2));
                         if (currentArtist != null) {
                             Iterable<String> currentArtistData = Splitter.on(TAB_PATTERN).split(currentArtist);
                             String artistUri = MUSIC_BRAINZ_ARTIST_URI + Iterables.get(currentArtistData, 1);
                             String artistCurie = MUSIC_BRAINZ_CURIE_PREFIX + Iterables.get(currentArtistData, 1);
                             String currentName = readData(namesFile, namesIndex, Iterables.get(currentCreditsData, 3));
                             if (currentName != null) {
                                 Iterable<String> currentNameData = Splitter.on(TAB_PATTERN).split(currentName);
                                 String artistName = Iterables.get(currentNameData, 1);
                                 CrewMember artist = new CrewMember(artistUri, artistCurie, Publisher.MUSIC_BRAINZ);
                                 artist.withRole(CrewMember.Role.ARTIST).withName(artistName);
                                 song.setPeople(Lists.newArrayList(artist));
                             }
                         }
                     }
 
                     contentWriter.createOrUpdate(song);
                     if (peopleWriter != null) {
                         peopleWriter.createOrUpdatePeople(song);
                     }
                 } catch (Exception ex) {
                     if (log != null) {
                         log.record(new AdapterLogEntry(AdapterLogEntry.Severity.WARN).withCause(ex).withSource(getClass()).withDescription("Exception when processing Music Brainz."));
                     } else {
                         ex.printStackTrace();
                     }
                 }
                 currentRecording = recordingsFile.readLine();
             }
         } finally {
             recordingsFile.close();
             isrcsFile.close();
             tracksFile.close();
             creditsFile.close();
             artistsFile.close();
             namesFile.close();
         }
     }
 
     private BufferedReader getReader(File dataDir, String fileName) throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(dataDir.getAbsolutePath() + File.separator + fileName));
         return reader;
     }
 
     private RandomAccessFile getRaf(File dataDir, String fileName) throws IOException {
         RandomAccessFile reader = new RandomAccessFile(dataDir.getAbsolutePath() + File.separator + fileName, "r");
         return reader;
     }
 
     private Long2LongMap buildIndex(RandomAccessFile raf, int pos) throws IOException {
         Long2LongMap index = new Long2LongOpenHashMap();
         index.defaultReturnValue(NOT_FOUND);
 
         long pointer = raf.getFilePointer();
         String line = raf.readLine();
         while (line != null) {
             Iterable<String> data = Splitter.on(TAB_PATTERN).split(line);
             String key = Iterables.get(data, pos);
             index.put(Long.parseLong(key), pointer);
             pointer = raf.getFilePointer();
             line = raf.readLine();
         }
         raf.seek(0);
         return index;
     }
 
     private String readData(RandomAccessFile file, Long2LongMap index, String key) throws IOException {
         long pos = index.get(Long.parseLong(key));
         if (pos != NOT_FOUND) {
            byte b = -1;
            boolean eol = false;
            BUFFER.clear();
             file.seek(pos);
            while (!eol) {
                switch (b = file.readByte()) {
                    case -1:
                    case '\n':
                        eol = true;
                        break;
                    case '\r':
                        eol = true;
                        long cur = file.getFilePointer();
                        if ((file.read()) != '\n') {
                            file.seek(cur);
                        }
                        break;
                    default:
                        BUFFER.add(b);
                        break;
                }
            }
            if ((b == -1) && (BUFFER.size() == 0)) {
                return null;
            } else {
                return new String(BUFFER.elements(), 0, BUFFER.size(), Charsets.UTF_8);
            }
         } else {
             return null;
         }
     }
 }
