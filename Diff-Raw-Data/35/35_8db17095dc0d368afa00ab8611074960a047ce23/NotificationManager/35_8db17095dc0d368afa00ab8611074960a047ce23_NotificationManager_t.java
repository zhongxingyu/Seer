 package uk.me.tom_fitzhenry.motionremote.notification;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.joda.time.Minutes;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Optional;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 
 @Singleton
 public class NotificationManager {
 	
 	private final AndroidNotifier androidNotifier;
 	
 	private final List<Incident> pastIncidents = Lists.newArrayList();
 	private Optional<MotionDetectedEvent> currentMotion = Optional.absent();
 	
 	@Inject
 	public NotificationManager(AndroidNotifier androidNotifier) {
 		this.androidNotifier = checkNotNull(androidNotifier);
 	}
 	
 	public void process(MotionDetectedEvent e) {
 		this.currentMotion = Optional.of(e);
 		update();
 	}
 	
 	public void process(MotionStoppedEvent e) {
 		if (currentMotion.isPresent()) {
 			pastIncidents.add(new Incident(currentMotion.get(), e));
 			currentMotion = Optional.absent();
			update();
 		}
 	}
 	
 	private void update() {
 		StringBuilder message = new StringBuilder();
 		
 		if (currentMotion.isPresent()) {
 			message.append("Motion detected!");
 		} else {
 			message.append("Motion stopped");
 		}
 		
 		if (!pastIncidents.isEmpty()) {
			message.append(" (Past: ");
 			
			Collection<String> incidents = Collections2.transform(pastIncidents, new Function<Incident, String>() {
 				public String apply(Incident arg0) {
 					return arg0.toString();
 				}
			});
			
			Joiner
				.on(", ")
				.appendTo(message, incidents);
 			
 			message.append(")");
				
 		}
 		
 		androidNotifier.submit(message.toString());
 		
 	}
 	
 	public void process(CameraDownEvent e) {
 		androidNotifier.submit("Camera down");
 	}
 	
 	public void process(Event e) {
 		e.process(this);
 	}
 	
 	private static class Incident {
 		public final MotionDetectedEvent start;
 		public final MotionStoppedEvent end;
 		
 		public Incident(MotionDetectedEvent s, MotionStoppedEvent e) {
 			this.start = s;
 			this.end = e;
 		}
 		
 		@Override
 		public String toString() {
 			Minutes diff = Minutes.minutesBetween(start.getTimestamp(), end.getTimestamp());
 			return diff.getMinutes() + " mins";
 		}
 		
 	}
 
 }
