 package com.blogpost.starasov.highlightr.model;
 
 import com.blogpost.starasov.highlightr.util.UrlSanitizer;
 import org.springframework.util.Assert;
 
 import javax.persistence.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: starasov
  * Date: 1/5/12
  * Time: 5:35 PM
  */
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"identifier", "type"}))
public class Stream {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @Column(name = "identifier", nullable = false)
     private String identifier;
 
     @Column(name = "type", nullable = false)
     @Enumerated(EnumType.STRING)
     private StreamType type;
 
     @OrderBy("rank")
     @OneToMany(mappedBy = "stream", cascade = CascadeType.MERGE)
     private List<Rank> ranks;
 
     public Stream() {
         this.id = 0L;
         this.ranks = new ArrayList<Rank>();
     }
 
     public Stream(String identifier, StreamType type) {
         this();
         this.identifier = identifier;
         this.type = type;
     }
 
     public static String buildIdentifier(URL url) {
         Assert.notNull(url, "url parameter can't be null.");
         URL sanitized = UrlSanitizer.sanitize(url);
         return sanitized.getHost() + sanitized.getPath();
     }
 
     public static Stream fromUrlAndType(URL url, StreamType type) {
         Assert.notNull(url, "url parameter can't be null.");
         return new Stream(buildIdentifier(url), type);
     }
 
     public Long getId() {
         return id;
     }
 
     public String getIdentifier() {
         return identifier;
     }
 
     public void setIdentifier(String identifier) {
         this.identifier = identifier;
     }
 
     public StreamType getType() {
         return type;
     }
 
     public void setType(StreamType type) {
         this.type = type;
     }
 
     public List<Rank> getRanks() {
         return ranks;
     }
 
     public void setRanks(List<Rank> ranks) {
         this.ranks = ranks;
     }
 
     public Statistics getStatistics() {
         if (ranks.isEmpty()) {
             return Statistics.EMPTY;
         }
 
         int min = ranks.get(0).getRank();
         int max = ranks.get(ranks.size() - 1).getRank();
 
         return new Statistics(Rank.toAverageRank(ranks), min, max);
     }
 
     public boolean hasRank(Rank rank) {
         return ranks.contains(rank);
     }
 
     public void addRank(Rank rank) {
         Assert.notNull(rank, "rank parameter can't be null.");
         ranks.add(rank);
         rank.setStream(this);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Stream stream = (Stream) o;
 
         if (id != null ? !id.equals(stream.id) : stream.id != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : 0;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("Stream");
         sb.append("{id=").append(id);
         sb.append(", identifier='").append(identifier).append('\'');
         sb.append(", type=").append(type);
         sb.append('}');
         return sb.toString();
     }
 }
