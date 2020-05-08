 package org.atlasapi.media.entity;
 
 import org.atlasapi.content.rdf.annotations.RdfClass;
 import org.atlasapi.media.vocabulary.PO;
 import org.joda.time.Duration;
 
 @RdfClass(namespace = PO.NS)
 public class Song extends Item {
 
 	private String isrc;
     private Long duration;
 	
 	public Song(String uri, String curie, Publisher publisher) {
 		super(uri, curie, publisher);
         setMediaType(MediaType.AUDIO);
         setSpecialization(Specialization.MUSIC);
 	}
 	
 	public void setIsrc(String isrc) {
 		this.isrc = isrc;
 	}
 
     public String getIsrc() {
         return isrc;
     }
 
     public void setDuration(Duration duration) {
        this.duration = duration.getStandardSeconds();
     }
 
     public Duration getDuration() {
        return Duration.standardSeconds(duration);
     }
 	
 	protected Song() {}
 	
 	@Override
 	public Song copy() {
 	    Song song = new Song();
 	    Item.copyTo(this, song);
 	    song.isrc = isrc;
         song.duration = duration;
 	    return song;
 	}
 }
