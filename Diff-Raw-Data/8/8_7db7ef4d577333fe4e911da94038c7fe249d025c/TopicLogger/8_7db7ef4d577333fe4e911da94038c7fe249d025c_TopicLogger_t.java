 package amp.gel.service;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 import amp.gel.dao.DatastoreWriter;
 import cmf.bus.Envelope;
 import cmf.bus.EnvelopeHeaderConstants;
 import cmf.bus.IEnvelopeBus;
 import cmf.bus.IEnvelopeFilterPredicate;
 import cmf.bus.IRegistration;
 
 public class TopicLogger implements EnvelopeLogger, IRegistration,
 		InitializingBean {
 	private static final Logger logger = LoggerFactory
 			.getLogger(TopicLogger.class);
 
 	/**
 	 * Logs envelopes from this bus
 	 */
 	private IEnvelopeBus envelopeBus;
 
 	/**
 	 * Logs envelopes using this writer
 	 */
 	private DatastoreWriter datastoreWriter;
 
 	/**
 	 * Logs all envelopes with this topic
 	 */
 	private String topic;
 
 	public TopicLogger setEnvelopeBus(IEnvelopeBus envelopeBus) {
 		if (envelopeBus == null)
 			throw new NullPointerException("envelopeBus is null!");
 
 		this.envelopeBus = envelopeBus;
 		return this;
 	}
 
 	public TopicLogger setDatastoreWriter(DatastoreWriter datastoreWriter) {
 		if (datastoreWriter == null)
 			throw new NullPointerException("datastoreWriter is null!");
 
 		this.datastoreWriter = datastoreWriter;
 		return this;
 	}
 
 	public EnvelopeLogger setTopic(String topic) {
 		if (StringUtils.isBlank(topic))
 			throw new IllegalArgumentException("topic is blank!");
 
 		this.topic = topic;
 		return this;
 	}
 
 	public IEnvelopeFilterPredicate getFilterPredicate() {
 		return null;
 	}
 
 	public Map<String, String> getRegistrationInfo() {
 		Map<String, String> info = new HashMap<String, String>();
 		info.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
 		return info;
 	}
 
 	public Object handle(Envelope envelope) {
 		try {
 			datastoreWriter.write(envelope);
 		} catch (Exception e) {
 			logger.error("Unable to write envelope '" + envelope
 					+ "' to datastore!", e);
 		}
 		return true;
 	}
 
 	public Object handleFailed(Envelope envelope, Exception ex)
 			throws Exception {
 		logger.info("Handling failed envelope: " + envelope, ex);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gel.dao.EnvelopeLogger#start()
 	 */
 	public void start() throws Exception {
 		datastoreWriter.initialize();
 		envelopeBus.register(this);
 
        Thread.sleep(1500);
 		logger.info("Started TopicLogger.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gel.dao.EnvelopeLogger#stop()
 	 */
 	public void stop() throws Exception {
 		envelopeBus.unregister(this);
 		datastoreWriter.close();
 
 		logger.info("Stopped TopicLogger.");
 	}
 
 	public void afterPropertiesSet() throws Exception {
 		logger.info("envelopeBus: " + envelopeBus);
 		logger.info("datastoreWriter: " + datastoreWriter);
 		logger.info("topic: " + topic);
 	}
 }
