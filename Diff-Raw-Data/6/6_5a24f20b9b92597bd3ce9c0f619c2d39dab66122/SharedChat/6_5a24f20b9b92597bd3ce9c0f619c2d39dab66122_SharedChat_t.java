 package org.vaadin.chatbox;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.vaadin.chatbox.client.ChatLine;
 
 public class SharedChat {
 	
 	private ExecutorService pool = Executors.newSingleThreadExecutor();
 	
 	public interface ChatListener {
 		public void lineAdded(ChatLine line);
 	}
 	private CopyOnWriteArrayList<ChatListener> listeners = new CopyOnWriteArrayList<ChatListener>();
 	
 	private ArrayList<ChatLine> lines = new ArrayList<ChatLine>();
 	
 	public void addLine(String message) {
 		addLine(new ChatLine(message));
 	}
 	
	public void addLine(ChatLine line) {
		synchronized (this) {
			lines.add(line);
		}
 		fireLineAdded(line);
 	}
 	
 	public synchronized List<ChatLine> getLinesStartingFrom(int index) {
 		int n = lines.size() - index;
 		if (n <= 0) {
 			return Collections.emptyList();
 		}
 		
 		ArrayList<ChatLine> ret = new ArrayList<ChatLine>(n);
 		for (int i=index; i<lines.size(); ++i) {
 			ret.add(lines.get(i));
 		}
 		return ret;
 	}
 	
 	public void addListener(ChatListener li) {
 		listeners.add(li);
 	}
 	
 	public void removeListener(ChatListener li) {
 		listeners.remove(li);
 	}
 	
 	private void fireLineAdded(final ChatLine line) {
 		pool.execute(new Runnable() {
 			@Override
 			public void run() {
 				for (ChatListener li : listeners) {
 					li.lineAdded(line);
 				}
 			}
 		});
 	}
 }
