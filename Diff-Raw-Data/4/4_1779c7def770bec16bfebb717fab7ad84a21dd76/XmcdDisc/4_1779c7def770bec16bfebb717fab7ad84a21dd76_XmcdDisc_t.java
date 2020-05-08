 package my.triviagame.xmcd;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.text.WordUtils;
 
 /**
  * Work with a single XMCD file.
  */
 public class XmcdDisc {
 
     /**
      * Initializes an {@link XmcdDisc} from the contents of an xmcd file.
      * Parses the entire file with one pass.
      * 
      * TODO: consider handling text broken down into multiple lines, e.g.
      * DTITLE=Really really really really really really really really really ...
     * DTITLE=really really long artist name that got broken / Crappy Album
     * 
     * TODO: method is too long. Refactor.
      *
      * @param contents all the contents of an xmcd file
      * @param freedbGenre the FreeDB genre (corresponds with the directory name)
      */
     public static XmcdDisc fromXmcdFile(String contents, FreedbGenre freedbGenre)
             throws XmcdFormatException, XmcdMissingInformationException {
         XmcdDisc disc = new XmcdDisc();
         disc.freedbGenre = freedbGenre;
         
         // Validate xmcd signature
         Matcher m = XmcdRegEx.SIGNATURE.matcher(contents);
         if (!m.lookingAt()) {
             throw new XmcdFormatException("Missing xmcd signature");
         }
         
         // Find track offsets
         m.usePattern(XmcdRegEx.TRACK_FRAME_OFFSETS_START);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing track frame offsets");
         }
         List<Integer> trackFrameOffsets = Lists.newArrayList();
         m.usePattern(XmcdRegEx.TRACK_FRAME_OFFSET);
         int regionStart = m.regionStart();
         while (m.find()) {
             if (m.group(1) == null) {
                 // Found list terminator
                 break;
             }
             trackFrameOffsets.add(Integer.parseInt(m.group(1)));
             regionStart = m.end();
         }
         // Reset the region in case we left the previous loop after failing to find the list terminator.
         // This happens in some malformed xmcd files. Finding the terminator fails and the matcher's state is affected
         // such that the region required resetting.
         m.region(regionStart, m.regionEnd());
 
         // Find disc length
         m.usePattern(XmcdRegEx.DISC_LENGTH);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing disc length");
         }
         disc.discLengthSeconds = Short.parseShort(m.group(1));
 
         // Find revision
         m.usePattern(XmcdRegEx.REVISION);
         if (m.find()) {
             disc.revision = Integer.parseInt(m.group(1));
         }
         // If no match found for revision, retain the default value
 
         // Find disc ID
         m.usePattern(XmcdRegEx.DISC_ID);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing FreeDB ID");
         }
         disc.freedbId = (int)Long.parseLong(m.group(1), 16);
 
         // Find disc title
         m.usePattern(XmcdRegEx.DISC_TITLE);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing disc title");
         }
         disc.discTitle = DiscTitle.fromXmcdTitle(m.group(1));
 
         // Find disc year
         m.usePattern(XmcdRegEx.YEAR);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing year");
         }
         if (m.group(1) == null) {
             disc.year = -1;
         } else {
             disc.year = Short.parseShort(m.group(1));
         }
 
         // Find disc genre
         m.usePattern(XmcdRegEx.GENRE);
         if (!m.find()) {
             // Use the FreeDB genre (converted to titlecase, e.g. "Pop")
             disc.genre = WordUtils.capitalize(disc.freedbGenre.name());
         } else {
             disc.genre = m.group(1);
         }
 
         // Find track titles
         m.usePattern(XmcdRegEx.TRACK_TITLE);
         TreeMap<Integer, TrackTitle> trackTitles = Maps.newTreeMap();
         while (m.find()) {
             TrackTitle trackTitle = TrackTitle.fromXmcdTitle(m.group(2), disc.getDiscArtist());
             trackTitles.put(Integer.parseInt(m.group(1)), trackTitle);
         }
         if (trackTitles.isEmpty()) {
             throw new XmcdMissingInformationException("Missing track titles");
         }
         
         // Build tracks out of titles and lengths
         if (trackTitles.size() > trackFrameOffsets.size()) {
             throw new XmcdFormatException(
                     String.format("%d tracks but only %d frame offsets", trackTitles.size(), trackFrameOffsets.size()));
         }
         FrameOffsetsToTrackDurationsSeconds durations =
                 new FrameOffsetsToTrackDurationsSeconds(trackFrameOffsets.iterator(), disc.discLengthSeconds);
         Iterator<TrackTitle> trackTitlesIter = trackTitles.values().iterator();
         while (trackTitlesIter.hasNext()) {
             // Adds tracks by their correct order
             disc.tracks.add(new Track(trackTitlesIter.next(), durations.next()));
         }
 
         return disc;
     }
     
     public XmcdDisc(FreedbGenre freedbGenre, short discLengthSeconds, int revision, int freeDbId, DiscTitle discTitle,
             short year, String genre, List<Track> trackTitles) {
         this.freedbGenre = freedbGenre;
         this.discLengthSeconds = discLengthSeconds;
         this.revision = revision;
         this.freedbId = freeDbId;
         this.discTitle = discTitle;
         this.year = year;
         this.genre = genre;
         this.tracks = trackTitles;
     }
 
     public FreedbGenre getFreedbGenre() {
         return freedbGenre;
     }
 
     public int getDiscLengthSeconds() {
         return discLengthSeconds;
     }
 
     public int getRevision() {
         return revision;
     }
 
     public int getFreeDbId() {
         return freedbId;
     }
 
     public String getDiscTitle() {
         return discTitle.title;
     }
 
     public String getDiscArtist() {
         return discTitle.artist;
     }
 
     /**
      * Returns the disc's year or -1 if no year was specified.
      */
     public int getYear() {
         return year;
     }
 
     public String genre() {
         return genre;
     }
 
     /**
      * Display name for the genre, i.e. "Jazz".
      * Not limited to the 11 FreeDB genres.
      */
     public String getGenre() {
         return genre;
     }
 
     /**
      * Returns a read-only view of the tracks.
      */
     public List<Track> getTracks() {
         return Collections.unmodifiableList(tracks);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof XmcdDisc)) {
             return false;
         }
         XmcdDisc other = (XmcdDisc) obj;
         return (Objects.equal(freedbGenre, other.freedbGenre) &&
                 Objects.equal(discLengthSeconds, other.discLengthSeconds) &&
                 Objects.equal(revision, other.revision) &&
                 Objects.equal(freedbId, other.freedbId) &&
                 Objects.equal(discTitle, other.discTitle) &&
                 Objects.equal(year, other.year) &&
                 Objects.equal(genre, other.genre) &&
                 Objects.equal(tracks, other.tracks));
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(freedbId);
     }
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 
     /**
      * Constructs a new disc with default values.
      * Used while building a disc from xmcd data.
      * Don't return the returned object before setting its fields or it won't make sense to anyone outside this class.
      * Visible for testing.
      */
     XmcdDisc() {
         // Assign default value to revision
         revision = 0;
         // Empty tracks list
         tracks = Lists.newArrayList();
         // Assign invalid values to the rest of the fields
         discLengthSeconds = Short.MIN_VALUE;
         discTitle = null;
         freedbId = 0;
         freedbGenre = null;
         genre = null;
         year = Short.MIN_VALUE;
     }
 
     /**
      * Verifies that a disc is valid.
      * This method is not public because all {@link XmcdDisc}s returned by public methods should always be valid.
      * Visible for testing.
      */
     void validate() throws XmcdMissingInformationException {
         if (discLengthSeconds == Short.MIN_VALUE) {
             throw new XmcdMissingInformationException("Missing disc length");
         }
         if (discTitle == null) {
             throw new XmcdMissingInformationException("Missing disc title");
         }
         if (freedbId == 0) {
             throw new XmcdMissingInformationException("Missing FreeDB ID");
         }
         if (freedbGenre == null) {
             throw new XmcdMissingInformationException("Missing FreeDB genre");
         }
         if (genre == null) {
             throw new XmcdMissingInformationException("Missing genre");
         }
         if (tracks == null || tracks.isEmpty()) {
             throw new XmcdMissingInformationException("Missing track titles");
         }
         if (year == Integer.MIN_VALUE) {
             throw new XmcdMissingInformationException("Missing disc year");
         }
     }
 
     private FreedbGenre freedbGenre;
     private short discLengthSeconds;
     private int revision;
     private int freedbId;
     private DiscTitle discTitle;
     private short year;
     private String genre;
     private List<Track> tracks;
 }
