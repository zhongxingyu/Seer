 package dacus.core.subscriber.impl;
 
import deus.core.publisher.PublisherStub;
 import deus.core.subscriber.Subscriber;
 import deus.core.subscriber.impl.AbstractSubscriberStub;
 import deus.core.subscriber.impl.SubscriberImpl;
 import deus.model.pub.SubscriberMetadata;
 import deus.model.user.id.UserId;
 import deus.model.user.id.UserIdType;
 
 public class LocalSubscriberStub<Id extends UserId> extends AbstractSubscriberStub<Id> {
 
 	private Subscriber<Id> subscriber;
 
 
 	public LocalSubscriberStub(SubscriberMetadata<Id> subscriberMetadata) {
 		super(subscriberMetadata);
		assert(subscriberMetadata.getUserId().getType().equals(UserIdType.local));
 		subscriber = new SubscriberImpl<Id>(subscriberMetadata);
 	}
 
 
 	@Override
	public void update(PublisherStub<Id> publisher, Object change) {
		subscriber.update(publisher, change);
 	}
 
 
 }
