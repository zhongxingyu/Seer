 package my.triviagame.xmcd;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.text.WordUtils;
 
 /**
  * Work with a single XMCD file.
  */
 public class XmcdDisc {
 
     /**
      * Initializes an {@link XmcdDisc} from the contents of an xmcd file.
      *
      * TODO(shayba): optimize to parse contents with one pass.
      *
      * @param contents all the contents of an xmcd file
      * @param freedbGenre the FreeDB genre (corresponds with the directory name)
      */
     public static XmcdDisc fromXmcdFile(String contents, FreedbGenre freedbGenre)
             throws XmcdFormatException, XmcdMissingInformationException {
         XmcdDisc disc = new XmcdDisc();
         Matcher m;
         
         // Validate xmcd signature
         m = XmcdRegEx.SIGNATURE.matcher(contents);
        if (!m.matches()) {
             throw new XmcdFormatException("Missing xmcd signature");
         }
 
         disc.freedbGenre = freedbGenre;
 
         m = XmcdRegEx.DISC_LENGTH.matcher(contents);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing disc length");
         }
         disc.discLengthSeconds = Short.parseShort(m.group(1));
 
         m = XmcdRegEx.REVISION.matcher(contents);
         if (m.find()) {
             disc.revision = Byte.parseByte(m.group(1));
         }
         // If no match found for revision, retain the default value
 
         m = XmcdRegEx.DISC_ID.matcher(contents);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing FreeDB ID");
         }
         disc.freedbId = Integer.parseInt(m.group(1), 16);
 
         m = XmcdRegEx.DISC_TITLE.matcher(contents);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing disc title");
         }
         disc.discTitle = DiscTitle.fromXmcdTitle(m.group(1));
 
         m = XmcdRegEx.YEAR.matcher(contents);
         if (!m.find()) {
             throw new XmcdMissingInformationException("Missing year");
         }
         disc.year = Short.parseShort(m.group(1));
 
         m = XmcdRegEx.GENRE.matcher(contents);
         if (!m.find()) {
             // Use the FreeDB genre (converted to titlecase, e.g. "Pop")
             disc.genre = WordUtils.capitalize(disc.freedbGenre.name());
         } else {
             disc.genre = m.group(1);
         }
 
         m = XmcdRegEx.TRACK_TITLE.matcher(contents);
         while (m.find()) {
             TrackTitle trackTitle = TrackTitle.fromXmcdTitle(m.group(2), disc.getDiscArtist());
             disc.tracks.add(Integer.parseInt(m.group(1)), trackTitle);
         }
         if (disc.tracks.isEmpty()) {
             throw new XmcdMissingInformationException("Missing track titles");
         }
 
         return disc;
     }
 
     public XmcdDisc(FreedbGenre freedbGenre, short discLengthSeconds, byte revision, int freeDbId, DiscTitle discTitle,
             short year, String genre, List<TrackTitle> trackTitles) {
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
 
     public byte getRevision() {
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
     public List<TrackTitle> getTracks() {
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
     private byte revision;
     private int freedbId;
     private DiscTitle discTitle;
     private short year;
     private String genre;
     private List<TrackTitle> tracks;
 }
