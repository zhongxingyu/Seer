 /**
  * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
  */
 package sim.monitor.subscribers.mbean;
 
 import java.lang.management.ManagementFactory;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectInstance;
 import javax.management.ObjectName;
 
 import sim.monitor.Aggregation;
 import sim.monitor.ContextEntry;
 import sim.monitor.Hit;
 import sim.monitor.Tags;
 import sim.monitor.subscribers.Subscriber;
 import sim.monitor.timing.TimePeriod;
 
 /**
  * The hit values are published to JMX Server through this subscriber. For each
  * container a dynamic MBean is registered.
  *
  * @author val
  *
  */
 public class MBeanSubscriber implements Subscriber {
 
 	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
 			.getLogger(MBeanSubscriber.class);
 
 	private MBeanServer mbServer;
 
 	private Map<ObjectName, Monitoring> mbeans = new HashMap<ObjectName, Monitoring>();
 
 	private Map<String, ContextAttributesTracker> monitorContextAttrTracker = new HashMap<String, ContextAttributesTracker>();
 
 	public MBeanSubscriber() {
 		mbServer = ManagementFactory.getPlatformMBeanServer();
 	}
 
 	private static ObjectName fromHit(String monitorName, String type,
 			String category) {
 		StringBuilder sb = new StringBuilder(Tags.DOMAIN);
 		sb.append(":");
 		sb.append("name=" + toObjectName(monitorName));
 		if (category != null && !"".equals(category)) {
 			sb.append(",category=" + category);
 		}
 		try {
 			return new ObjectName(sb.toString());
 		} catch (MalformedObjectNameException e) {
 			logger.error("Could not createObjectName ", e);
 			return null;
 		} catch (NullPointerException e) {
 			logger.error("Null container received!", e);
 			return null;
 		}
 	}
 
 	private static String toObjectName(String monitorName) {
 		return monitorName;
 	}
 
 	private Monitoring createForHit(Hit hit) {
 		if (hit.getValue() instanceof Long) {
 			return new MonitoringLong();
 		} else {
 			return new MonitoringDouble();
 		}
 	}
 
 	private String typeForHit(Hit hit) {
 		if (hit.getValue() instanceof Long) {
 			return "IntegerValueMonitor";
 		} else if (hit.getValue() instanceof Double) {
 			return "DecimalValueMonitor";
 		} else if (hit.getValue() instanceof BigDecimal) {
 			return "CurrencyValueMonitor";
 		} else if (hit.getValue() instanceof Date) {
 			return "DateTimeValueMonitor";
 		}
 		return "";
 	}
 
 	private int indexOfContextComposite(
 			List<ContextComposite> contextComposites, String contextVaue) {
 		int index = 0;
 		for (ContextComposite cc : contextComposites) {
 			if (cc.getContextValue().equals(contextVaue)) {
 				return index;
 			}
 			index++;
 		}
 		return -1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see sim.monitor.subscribers.Subscriber#update(java.util.Collection,
 	 * sim.monitor.Tags, java.lang.String, java.lang.String)
 	 *
 	 * FIXME maybe we could avoid somehow the synchronized ...
 	 */
 	public synchronized void update(Collection<Hit> hits, Tags tags,
 			String monitorName, String monitorDescription, String name,
 			String description, TimePeriod rateInterval, Aggregation aggregation) {
 		logger.info("updating mbean " + monitorName + " for attribute " + name);
 		for (Hit hit : hits) {
 			logger.info("treat hit " + hit);
 
 			StringBuilder category = new StringBuilder();
 			if (rateInterval != null) {
 				category.append(aggregation.name() + " rate at "
 						+ rateInterval.toReadableString() + " ("
 						+ rateInterval.toString() + aggregation.toShortString()
 						+ ")");
 			} else if (aggregation != null) {
 				category.append(aggregation.name() + " result ("
 						+ aggregation.toShortString() + ")");
 			} else {
 				category.append("Current value");
 			}
 
 			ObjectName objectName = fromHit(monitorName, typeForHit(hit),
 					category.toString());
 			if (objectName == null) {
 				return;
 			}
 			Monitoring monitoring = null;
 			if (!mbeans.containsKey(objectName)) {
 				monitoring = createForHit(hit);
 				monitoring.setRateInterval(rateInterval == null ? -1
 						: rateInterval.getSeconds());
 				monitoring.setAggregation(aggregation == null ? ""
 						: aggregation.toString());
 				monitoring.setType(typeForHit(hit));
 				StringBuilder sb = new StringBuilder();
 				if (tags.getTags().length > 0) {
 					for (int i = 0; i < tags.getTags().length; i++) {
 						if (i > 0) {
 							sb.append(";");
 						}
 						String tag = tags.getTags()[i];
 						sb.append(tag);
 					}
 				}
 				monitoring.setTags(sb.toString());
 				mbeans.put(objectName, monitoring);
 			} else {
 				monitoring = mbeans.get(objectName);
 			}
 
 			if (!monitorContextAttrTracker.containsKey(monitorName)) {
 				monitorContextAttrTracker.put(monitorName,
 						new ContextAttributesTracker());
 			}
 			/*
 			ContextAttributesTracker contextAttrTracker = monitorContextAttrTracker
 					.get(monitorName);
 			AttributeChanges attrChanges = contextAttrTracker.add(hit, name,
 					aggregation);
 			 */
 
 			Set<ObjectInstance> instances = mbServer.queryMBeans(objectName,
 					null);
 			ObjectInstance mb = null;
 
			if (hit.getContext().contains(ContextEntry.UNDEFINED)) {
 				monitoring.setValue(hit.getValue());
 				monitoring.setDescription(monitorDescription);
 			}
 			for (ContextEntry ce : hit.getContext()) {
 				if (ce.equals(ContextEntry.UNDEFINED)) {
 					continue;
 				}
 				if (!monitoring.getContext().containsKey(ce.getKey())) {
 					monitoring.getContext().put(ce.getKey(),
 							new ArrayList<ContextComposite>());
 
 				}
 				List<ContextComposite> contextComposites = monitoring
 						.getContext().get(ce.getKey());
 				int indexOfContextComposite = indexOfContextComposite(
 						contextComposites, ce.getValue().toString());
 				if (indexOfContextComposite == -1) {
 					contextComposites.add(new ContextComposite(ce.getValue()
 							.toString(), hit.getValue().toString()));
 				} else {
 					contextComposites.get(indexOfContextComposite).setValue(
 							hit.getValue().toString());
 				}
 			}
 
 			if (instances.isEmpty()) {
 				try {
 					mb = mbServer.registerMBean(monitoring, objectName);
 				} catch (InstanceAlreadyExistsException e) {
 					logger.error("Instance for objectName " + objectName
 							+ " already exists!", e);
 				} catch (NotCompliantMBeanException e) {
 					logger.error("The mbean for " + objectName
 							+ " is not compliant!", e);
 				} catch (MBeanRegistrationException e) {
 					logger.error(
 							"MBean could not be registered for ObjectName: "
 									+ objectName, e);
 				}
 			}
 
 /*
 			if (attrChanges.hasNewOrRemovedAttributes() && !instances.isEmpty()) {
 				mb = instances.iterator().next();
 				try {
 					logger.info("unregister : " + attrChanges);
 					mbServer.unregisterMBean(mb.getObjectName());
 				} catch (MBeanRegistrationException e) {
 					logger.error(
 							"MBean could not be unregistered for ObjectName: "
 									+ mb.getObjectName(), e);
 				} catch (InstanceNotFoundException e) {
 					logger.error(
 							"Instance for objectName " + mb.getObjectName()
 									+ " could not be found!", e);
 				}
 			}
 
 
 			for (Attribute attr : attrChanges.getAddedAndModifiedAttributes()) {
 				Object value = attr.getValue();
 				monitoringMXBean.getAttributes().put(
 						attr.getName(),
 								new AttributeData(description, value, value
 								.getClass()));
 			}
 			for (String attr : attrChanges.getRemovedAttributes()) {
 				monitoringMXBean.getAttributes().remove(attr);
 			}
 
 			if (attrChanges.hasNewOrRemovedAttributes() || instances.isEmpty()) {
 				try {
 					mb = mbServer.registerMBean(monitoringMXBean, objectName);
 				} catch (InstanceAlreadyExistsException e) {
 					logger.error("Instance for objectName " + objectName
 							+ " already exists!", e);
 				} catch (NotCompliantMBeanException e) {
 					logger.error("The mbean for " + objectName
 							+ " is not compliant!", e);
 				} catch (MBeanRegistrationException e) {
 					logger.error(
 							"MBean could not be registered for ObjectName: "
 									+ objectName, e);
 				}
 			}
 
 */
 			/*
 			List<String> attributes = new ArrayList<String>();
 			for (ContextEntry entry : hit.getContext()) {
 				if (entry.equals(ContextEntry.UNDEFINED)) {
 					attributes.add(name);
 				} else {
 					attributes.add(name + "." + entry.getKey() + "."
 							+ entry.getValue());
 				}
 			}
 
 			for (String attributeName : attributes) {
 				boolean containsAttribute = dynMBean.getAttributes()
 						.containsKey(attributeName);
 
 				Set<ObjectInstance> instances = mbServer.queryMBeans(
 						objectName, null);
 				ObjectInstance mb = null;
 
 				if (!containsAttribute && !instances.isEmpty()) {
 					mb = instances.iterator().next();
 					try {
 						mbServer.unregisterMBean(mb.getObjectName());
 					} catch (MBeanRegistrationException e) {
 						logger.error(
 								"MBean could not be unregistered for ObjectName: "
 										+ mb.getObjectName(), e);
 					} catch (InstanceNotFoundException e) {
 						logger.error(
 								"Instance for objectName " + mb.getObjectName()
 										+ " could not be found!", e);
 					}
 				}
 
 				dynMBean.getAttributes().put(
 						attributeName,
 						new AttributeData(description, hit.getValue()
 								.toString(), hit.getValue().getClass()
 								.getName()));
 
 				if (!containsAttribute || instances.isEmpty()) {
 					try {
 						mb = mbServer.registerMBean(dynMBean, objectName);
 					} catch (InstanceAlreadyExistsException e) {
 						logger.error("Instance for objectName " + objectName
 								+ " already exists!", e);
 					} catch (NotCompliantMBeanException e) {
 						logger.error("The mbean for " + objectName
 								+ " is not compliant!", e);
 					} catch (MBeanRegistrationException e) {
 						logger.error(
 								"MBean could not be registered for ObjectName: "
 										+ objectName, e);
 					}
 				}
 			}
 			*/
 		}
 	}
 
 }
