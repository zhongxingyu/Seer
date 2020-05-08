 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Video extends Model{
 
 	public String videoId; 
 	public String fileName; 
 	public long lenght; 
 
 	@OneToMany(cascade=CascadeType.ALL)
 	public List<VideoChunk> chunks = new ArrayList<VideoChunk>();
 
 	@ManyToOne
 	public User addedBy;
 	
 	@OneToMany(cascade=CascadeType.ALL)
 	public List<UserChunks> userChunks = new ArrayList<UserChunks>();
 
 	public Video(String videoId, String fileName, long lenght, List<String> plainChunks, User user) {
 		this.fileName = fileName;
 		this.videoId = videoId;
 		this.lenght = lenght;
 		this.addedBy = user;
 
 		for (int i = 0; i< plainChunks.size(); i++) {
 			chunks.add(new VideoChunk(i, plainChunks.get(i)));
 		}
 	}
 
 	@Override
 	public String toString() {
 		return videoId;
 	}
 
 	public UserChunks getChunksFrom(final User user) {
 		
 		play.Logger.info("Video.getChunksFrom() "+user+" - "+this.userChunks+" - "+this.userChunks.size());
 		
 		for(UserChunks uc : this.userChunks){
 			
 			play.Logger.debug("comparing %s and %s", uc.user.email, user.email);
 			
 			if (uc.user.email.equals(user.email) ) {
 				return uc;
 			}
 		}
 		UserChunks uc = new UserChunks(user);
 		this.userChunks.add(uc);
 		save();
 		return uc;
 
 //		List<UserChunks> uc = (List<UserChunks>) CollectionUtils.collect(this.userChunks, new Transformer(){
 //
 //			@Override
 //			public Object transform(Object arg0) {
 //				UserChunks userChunks = (UserChunks)arg0;
 //				return userChunks.user.email.equals(user.email);
 //			}});
 //
 //		return uc.isEmpty() ? new UserChunks(user) : uc.get(0);
 		
 //		return this.getChunksFrom(user) == null ? new UserChunks(user) : this.getChunksFrom(user);
 //		return this.userChunks.get(user) == null ? new UserChunks(user) : this.userChunks.get(user);
 	}
 
 
 
 	public boolean registerChunks(User user, List<Integer> ucList) {
 
 		boolean someAdded = false;
 
 		UserChunks chunksFrom = this.getChunksFrom(user);
 
 		for(int position : ucList) {
 			if (!chunksFrom.hasChunk(position)) {
 				chunksFrom.chunks.add(new UserChunk(position));
 				someAdded = true;
 			}
 		}
 
 		if(someAdded){
 			save();
 		}
 
 		return someAdded;
 	}
 
 	public synchronized boolean unregisterChunks(User user,
 			List<Integer> chunksToRemove) {
 		
 		boolean removed = false;
 		
 		UserChunks userChunks = this.getChunksFrom(user);
 		
 		for(int chunkPosition : chunksToRemove) {
 			boolean ok = userChunks.removeChunk(chunkPosition);
 			if(ok) {
 				removed = true;
 			}
 		}
 		save();
 		return removed;
 	}
 
 	
 }
 
 
