 package pro.trousev.cleer.android;
 
 import java.util.List;
 
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Messaging;
import pro.trousev.cleer.Playlist;
 
 public interface AndroidMessages {
 	enum TypeOfResult {
 		Compositions, Albums, Genres, Artists, Playlists, Queue, Playlist
 	}
 
 	enum Action {
 		Play, Pause, Stop, Next, Previous, setToQueue, addToQueue, addToPlaylist, createNewList
 	}
 
 	public static class ServiceRequestMessage implements Messaging.Message {
 		public String searchQuery;
 		public TypeOfResult type;
 	}
 
 	public static class ServiceRespondMessage implements Messaging.Message {
 		public List<Item> list;
 		public TypeOfResult typeOfContent;
 	}
 
 	public static class ServiceTaskMessage implements Messaging.Message {
		public Playlist playlist;
 		public Action action;
 		// could we find Playlist with only its' title?
 	}
 }
