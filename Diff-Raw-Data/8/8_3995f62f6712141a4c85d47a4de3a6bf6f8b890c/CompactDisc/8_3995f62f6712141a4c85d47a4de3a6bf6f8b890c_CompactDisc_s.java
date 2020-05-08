 package ro.inf.p2.uebung07;
 
 /**
  * Created with IntelliJ IDEA.
  * User: felix
  * Date: 5/14/13
  * Time: 9:22 PM
  * CompactDisc Class
  */
 public class CompactDisc implements Comparable<CompactDisc> {
 
     private String artist;
     private String title;
 
     private int year;
 
     private String label;
 
 
     public CompactDisc(String artist, String title, int year, String label) {
         this.artist = artist;
         this.title = title;
         this.year = year;
         this.label = label;
     }
 
     public String getArtist() {
         return artist;
     }
 
     public String getTitle() {
         return title;
     }
 
     public int getYear() {
         return year;
     }
 
     public String getLabel() {
         return label;
     }
 
     private String rmArticle(String s) {
        return s.replaceAll("(?i)^der\\s+|^die\\s+|^das\\s+|^ein\\s+|^eine\\s+|^einer\\s+|^eines\\s+|^the\\s+|^a\\s+",
                "");
     }
 
     @Override
     public boolean equals(Object o) {
         String thisArtist, thisTitle, thatArtist, thatTitle;
 
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         CompactDisc that = (CompactDisc) o;
 
         thisArtist = artist.toLowerCase();
         thisTitle = title.toLowerCase();
 
         thatArtist = that.artist.toLowerCase();
         thatTitle = that.title.toLowerCase();
 
         return thisArtist.equals(thatArtist) && thisTitle.equals(thatTitle);
     }
 
     @Override
     public int hashCode() {
         int result = artist.toLowerCase().hashCode();
         result = 31 * result + title.toLowerCase().hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         return "CompactDisc{" +
                 "artist='" + artist + '\'' +
                 ", title='" + title + '\'' +
                 ", year=" + year +
                 ", label='" + label + '\'' +
                 '}';
     }
 
     @Override
     public int compareTo(CompactDisc o) {
         String thisArtist, thatArtist;
 
         if (equals(o)) return 0;
 
        thisArtist = rmArticle(artist).toLowerCase();
        thatArtist = rmArticle(o.getArtist()).toLowerCase();
 
         return thisArtist.compareTo(thatArtist);
     }
 }
