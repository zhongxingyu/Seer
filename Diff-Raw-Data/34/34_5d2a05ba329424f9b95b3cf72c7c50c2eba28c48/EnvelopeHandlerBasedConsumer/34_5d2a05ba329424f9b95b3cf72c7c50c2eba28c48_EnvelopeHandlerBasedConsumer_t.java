 package pegasus.eventbus.rabbitmq;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventResult;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.DefaultConsumer;
 import com.rabbitmq.client.ShutdownSignalException;
 import com.rabbitmq.client.AMQP.BasicProperties;
 
 class EnvelopeHandlerBasedConsumer extends DefaultConsumer {
 
 	private final Logger    		LOG;
 	private final String 			queueName; 
 	private final EnvelopeHandler 	consumer;
 	
 	public EnvelopeHandlerBasedConsumer(Channel channel, String queueName, EnvelopeHandler handler) {
 		super(channel);
 		this.queueName = queueName;
 		this.consumer = handler;
 	
         //TODO: PEGA-727 Need to add tests to assert that this logger name is always valid (i.e. queue names with . and any other illegal chars are correctly mangled.)
 		LOG = LoggerFactory.getLogger(String.format("%s$>%s", this.getClass().getCanonicalName(), queueName.replace('.', '_')));
 	}
 	
 	@Override
 	public void handleConsumeOk(String consumerTag) {
 		LOG.debug("ConsumeOk received for ConsumerTag [{}].", consumerTag);
 		super.handleConsumeOk(consumerTag);
 	}
 
 	@Override
 	public void handleCancel(String consumerTag) throws IOException {
 		LOG.debug("Subscription Cancel received for ConsumerTag [{}].", consumerTag);
 		super.handleCancel(consumerTag);
 	}
 	
 	@Override
 	public void handleCancelOk(String consumerTag) {
 		LOG.debug("Subscription CancelOk received for ConsumerTag [{}].", consumerTag);
 		super.handleCancelOk(consumerTag);
 	}
 	
 	@Override
 	public void handleRecoverOk(String consumerTag) {
 		LOG.debug("Subscription RecoverOk received for ConsumerTag [{}].", consumerTag);
 		super.handleRecoverOk(consumerTag);
 	}
 
 	@Override
 	public void handleShutdownSignal(String consumerTag,
 			ShutdownSignalException sig) {
 		LOG.debug("Subscription ShutdownSignal received for ConsumerTag [{}].", consumerTag);
 		super.handleShutdownSignal(consumerTag, sig);
 	}
 	
 	@Override
 	public void handleDelivery(String consumerTag,
 			com.rabbitmq.client.Envelope amqpEnvelope,
 			BasicProperties properties, 
 			byte[] body)
 			throws IOException {
 		
 		try {
 			super.handleDelivery(consumerTag, amqpEnvelope, properties, body);
 			
 			Channel channel = this.getChannel();
 			
 			LOG.trace("Handling delivery for ConsumerTag [{}].", consumerTag);
 			
 			long deliveryTag  = amqpEnvelope.getDeliveryTag();
 			
 			LOG.trace("DeliveryTag is [{}] for message on ConsumerTag [{}]", deliveryTag, consumerTag);
 			
 			Envelope envelope = RabbitMessageBus.createEnvelope(properties, body);
 			
 			LOG.trace("Envelope create for DeliveryTag [{}].", deliveryTag);
 			
 			EventResult result;
 			try {
 				
 				LOG.trace("Handling envelope for DeliveryTag [{}].", deliveryTag);
 				
 				//TODO: PEGA-726 what if handler incorrectly returns null? I think we should assume Failed to be safe.
 				result = consumer.handleEnvelope(envelope);
 				
			} catch (Throwable e) {
 				
 				result = EventResult.Failed;
 				
 				String id;
 				
 				try {
 					
 					id = envelope.getId().toString();
 					
				} catch (Throwable ee) {
 					
 					id = "<message id not available>";
 				}
 				
 				LOG.error("Envelope handler of type " + consumer.getClass().getCanonicalName() + " on queue " + queueName + " threw exception of type " + e.getClass().getCanonicalName()
 						+ " handling message " + id + ", DeliveryTag: " + deliveryTag, e);
 				
 			}
 			
 			LOG.trace("Determining how to handle EventResult [{}]", result);
 			
 			switch (result) {
 			case Handled:
 				
 				LOG.trace("Accepting DeliveryTag [{}]", deliveryTag);
 				
 				channel.basicAck(deliveryTag, false);
 				
 				break;
 			case Failed:
 				
 				LOG.trace("Rejecting DeliveryTag [{}]", deliveryTag);
 				
 				channel.basicReject(deliveryTag, false);
 				
 				break;
 			case Retry:
 				
 				LOG.trace("Retrying DeliveryTag [{}]", deliveryTag);
 				
 				channel.basicReject(deliveryTag, true);
 				
 				break;
 			}
		} catch (Throwable e) {

 			LOG.error("handleDelivery failed on queue " + queueName + ".", e);
 		}
 	}
 	
 }
