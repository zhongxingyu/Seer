 package amp.gel.service;
 
 import java.awt.Toolkit;
 import java.util.Date;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
import amp.bus.rabbit.DeliveryOutcomes;
 import amp.gel.domain.Table;
 import amp.gel.domain.TimeScale;
 import amp.gel.service.event.EventActivityRequestEvent;
 import amp.gel.service.event.EventActivityResponseEvent;
 import amp.gel.service.event.TypeActivityRequestEvent;
 import amp.gel.service.event.TypeActivityResponseEvent;
 import amp.gel.service.event.UserActivityRequestEvent;
 import amp.gel.service.event.UserActivityResponseEvent;
 import cmf.bus.Envelope;
 import cmf.eventing.IEventBus;
 import cmf.eventing.IEventHandler;
 
 public class EventHandlerDemo {
 
 	public static void main(String[] args) throws Exception {
 		ApplicationContext appContext = new ClassPathXmlApplicationContext(
 				"test-cmf-context.xml");
 		IEventBus eventBus = appContext.getBean("eventBus", IEventBus.class);
 
 		/*
 		 * Start event handlers
 		 */
 		new EventActivityResponseEventHandler(eventBus);
 		new TypeActivityResponseEventHandler(eventBus);
 		new UserActivityResponseEventHandler(eventBus);
 
 		Date start = new DateTime().withMonthOfYear(1).toDate();
 		Date stop = new DateTime().toDate();
 		TimeScale timeScale = TimeScale.MONTH;
 
 		EventActivityRequestEvent eventActivityRequestEvent = new EventActivityRequestEvent(
 				start, stop, timeScale);
 		eventBus.publish(eventActivityRequestEvent);
 
 		TypeActivityRequestEvent typeActivityRequestEvent = new TypeActivityRequestEvent(
 				"amp.gel.dao.impl.derby.data.events.search.SetLikesEvent",
 				start, stop, timeScale);
 		eventBus.publish(typeActivityRequestEvent);
 
 		UserActivityRequestEvent userActivityRequestEvent = new UserActivityRequestEvent(
 				"tjefferson", start, stop, timeScale);
 		eventBus.publish(userActivityRequestEvent);
 
 		EventActivityRequestEvent eventActivityRequestEvent2 = new EventActivityRequestEvent(
 				new DateTime().withDayOfMonth(1).toDate(), stop, TimeScale.DAY);
 		eventBus.publish(eventActivityRequestEvent2);
 	}
 
 	static public class EventActivityResponseEventHandler implements
 			IEventHandler<EventActivityResponseEvent> {
 
 		public EventActivityResponseEventHandler(IEventBus eventBus) {
 			try {
 				eventBus.subscribe(this);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		public Class<EventActivityResponseEvent> getEventType() {
 			return EventActivityResponseEvent.class;
 		}
 
 		public Object handleFailed(Envelope envelope, Exception e) {
 			return DeliveryOutcomes.Exception;
 		}
 
 		public Object handle(EventActivityResponseEvent event,
 				Map<String, String> headers) {
 			Toolkit.getDefaultToolkit().beep();
 
 			synchronized (System.out) {
 				System.out.println("\n");
 				System.out.println("Received '"
 						+ event.getClass().getSimpleName() + "'");
 
 				Table eventsByTime = event.getEventsByTime();
 				System.out.println("eventsByTime: " + eventsByTime);
 
 				Table eventsByType = event.getEventsByType();
 				System.out.println("eventsByType: " + eventsByType);
 
 				Table eventsByUser = event.getEventsByUser();
 				System.out.println("eventsByUser: " + eventsByUser);
 			}
 
 			return DeliveryOutcomes.Acknowledge;
 		}
 	}
 
 	static public class TypeActivityResponseEventHandler implements
 			IEventHandler<TypeActivityResponseEvent> {
 
 		public TypeActivityResponseEventHandler(IEventBus eventBus) {
 			try {
 				eventBus.subscribe(this);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		public Class<TypeActivityResponseEvent> getEventType() {
 			return TypeActivityResponseEvent.class;
 		}
 
 		public Object handleFailed(Envelope envelope, Exception e) {
 			return DeliveryOutcomes.Exception;
 		}
 
 		public Object handle(TypeActivityResponseEvent event,
 				Map<String, String> headers) {
 			Toolkit.getDefaultToolkit().beep();
 
 			synchronized (System.out) {
 				System.out.println("\n");
 				System.out.println("Received '"
 						+ event.getClass().getSimpleName() + "'");
 
 				Table eventsByTime = event.getEventsByTime();
 				System.out.println("eventsByTime: " + eventsByTime);
 
 				Table eventsByUser = event.getEventsByUser();
 				System.out.println("eventsByUser: " + eventsByUser);
 			}
 
 			return DeliveryOutcomes.Acknowledge;
 		}
 	}
 
 	static public class UserActivityResponseEventHandler implements
 			IEventHandler<UserActivityResponseEvent> {
 
 		public UserActivityResponseEventHandler(IEventBus eventBus) {
 			try {
 				eventBus.subscribe(this);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		public Class<UserActivityResponseEvent> getEventType() {
 			return UserActivityResponseEvent.class;
 		}
 
 		public Object handleFailed(Envelope envelope, Exception e) {
 			return DeliveryOutcomes.Exception;
 		}
 
 		public Object handle(UserActivityResponseEvent event,
 				Map<String, String> headers) {
 			Toolkit.getDefaultToolkit().beep();
 
 			synchronized (System.out) {
 				System.out.println("\n");
 				System.out.println("Received '"
 						+ event.getClass().getSimpleName() + "'");
 
 				Table eventsByTime = event.getEventsByTime();
 				System.out.println("eventsByTime: " + eventsByTime);
 
 				Table eventsByType = event.getEventsByType();
 				System.out.println("eventsByType: " + eventsByType);
 			}
 
 			return DeliveryOutcomes.Acknowledge;
 		}
 	}
 }
