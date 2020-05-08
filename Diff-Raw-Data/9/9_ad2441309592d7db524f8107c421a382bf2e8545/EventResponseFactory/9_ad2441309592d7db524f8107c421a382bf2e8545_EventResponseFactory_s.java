 package org.backgitup;
 
 import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
 
 import java.nio.file.Path;
 import java.nio.file.WatchEvent;
 
 class EventResponseFactory {
 
 	private final Path fTarget;
 
 	public EventResponseFactory(Path target) {
 		this.fTarget = target;
 		
 	}
 	
	public EventResponse create(WatchEvent.Kind<Path> eventKind) {
 		EventResponse ret = null;
 		if (eventKind == ENTRY_CREATE) {
 			ret = new CreateEventResponse(fTarget);
 		} else if (eventKind == ENTRY_DELETE) {
 			ret = new DeleteEventResponse(fTarget);
 		} else if (eventKind == ENTRY_MODIFY) {
 			ret = new ModifyEventResponse(fTarget);
 		}
 		return ret;
 	}
 	
 }
