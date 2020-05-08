 package com.polopoly.ps.tools.collections.examples;
 
 import static com.polopoly.util.policy.Util.util;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import com.polopoly.cm.ContentId;
 import com.polopoly.cm.client.CMException;
 import com.polopoly.cm.policy.Policy;
 import com.polopoly.ps.tools.collections.ComponentStorage;
 import com.polopoly.ps.tools.collections.component.DefaultComponentStorage;
 import com.polopoly.ps.tools.collections.componentcollection.MapStorage;
 import com.polopoly.ps.tools.collections.componentcollection.MapStorageProvider;
 import com.polopoly.ps.tools.collections.converter.ContentIdConverter;
 import com.polopoly.ps.tools.collections.converter.DateConverter;
 import com.polopoly.ps.tools.collections.converter.StringConverter;
 import com.polopoly.ps.tools.collections.incontent.DefaultQueueInContent;
 import com.polopoly.ps.tools.collections.incontent.EditableQueueInContent;
 import com.polopoly.ps.tools.collections.incontent.QueueInContent;
 import com.polopoly.util.Require;
 import com.polopoly.util.client.PolopolyContext;
 import com.polopoly.util.collection.TransformingIterator;
 import com.polopoly.util.exception.CannotFetchSingletonException;
 import com.polopoly.util.exception.PolicyModificationException;
 import com.polopoly.util.policy.PolicyModification;
 import com.polopoly.util.policy.PolicySingleton;
 import com.polopoly.util.policy.PolicyUtil;
 
 /**
  * This example illustrates how to create a persistent log of events where each
  * event refers to a content object. A queue of maps is used where the different
  * attributes of a log entry are entries in the map. There are two operations:
  * one adding a log entry and one for getting an iterator of log entries back.
  * 
  * Note that the content IDs will be stored as components in this example which
  * is slightly ugly though harmless.
  */
 public class PersistedLog {
 	static final Logger LOGGER = Logger.getLogger(PersistedLog.class.getName());
 
 	private PolicyUtil policy;
 
 	private QueueInContent<MapStorage<String, String>> queue;
 
 	private static final ComponentStorage<String> storage = new DefaultComponentStorage<String>(
 			new StringConverter());
 
 	private static final MapStorageProvider<String, String> provider = new MapStorageProvider<String, String>(
 			storage, new StringConverter());
 
 	private static final String GLOBAL_LOG_EXTERNAL_ID = "globalLog";
 
 	private static final int GLOBAL_LOG_SIZE = 1024;
 
 	private PersistedLog(PolicyUtil policy, int maxSize) {
 		this.policy = Require.require(policy);
 
 		queue = new DefaultQueueInContent<MapStorage<String, String>>(policy,
 				provider, storage, maxSize);
 	}
 
 	public static PersistedLog getGlobalLog(PolopolyContext context)
 			throws CannotFetchSingletonException {
 		Policy policy = new PolicySingleton(context, 17,
 				GLOBAL_LOG_EXTERNAL_ID, "p.DefaultAppConfig").get();
 
 		return new PersistedLog(util(policy), GLOBAL_LOG_SIZE);
 	}
 
 	public void log(final Date date, final ContentId contentId,
 			final String event) throws PolicyModificationException {
 		Require.require(date);
 		Require.require(contentId);
 		Require.require(event);
 
 		queue.modify(new PolicyModification<EditableQueueInContent<MapStorage<String, String>>>() {
 
 			@Override
 			public void modify(
 					EditableQueueInContent<MapStorage<String, String>> newVersion)
 					throws CMException {
 				MapStorage<String, String> newEntry = newVersion.push();
 
 				newEntry.put(DATE, new DateConverter().toString(date));
 				newEntry.put(CONTENT_ID,
 						new ContentIdConverter(policy.getContext())
 								.toString(contentId));
 				newEntry.put(EVENT, event);
 			}
 		});
 	}
 
 	public Iterator<PersistedLogEntry> entries() {
 		return new TransformingIterator<MapStorage<String, String>, PersistedLogEntry>(
 				queue.iterator()) {
 
 			@Override
 			protected PersistedLogEntry transform(
 					final MapStorage<String, String> next) {
 				return new WrappingLogEntry(policy.getContext(), next);
 			}
 		};
 	}
 }
