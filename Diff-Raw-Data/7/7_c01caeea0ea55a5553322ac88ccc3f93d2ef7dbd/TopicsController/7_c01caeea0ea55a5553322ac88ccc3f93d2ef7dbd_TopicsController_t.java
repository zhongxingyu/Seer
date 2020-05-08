 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package controllers;
 
 import java.util.List;
 
 import org.ow2.play.governance.api.EventGovernance;
 import org.ow2.play.governance.api.TopicAware;
 import org.ow2.play.governance.api.bean.Topic;
 
 import utils.Locator;
 
 /**
  * @author chamerling
  * 
  */
 public class TopicsController extends PlayController {
 
 	public static void topics() {
 		EventGovernance client = null;
 		try {
 			client = Locator.getEventGovernance(getNode());
 		} catch (Exception e) {
 			handleException(null, e);
 		}
 
 		List<Topic> topics = null;
 		try {
 			topics = client.getTopics();
 		} catch (Exception e) {
 			handleException("Unable to get topics", e);
 		}
 		render(topics);
 	}
 
 	/**
 	 * Get all the DSB topics GET
 	 */
 	public static void dsb() {
 		List<Topic> topics = null;
 		try {
 			TopicAware ta = Locator.getDSBTopicManagement(getNode());
 			topics = ta.get();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		render(topics);
 	}
 
 	/**
 	 * Create a new DSB topic. Does not register it anywhere in registry, just
 	 * add it on the engine.
 	 * 
 	 * POST
 	 * 
 	 * @param name
 	 * @param ns
 	 * @param prefix
 	 */
 	public static void createDSB(String name, String ns, String prefix) {
 		validation.required(name);
 		validation.required(prefix);
 
 		// validation url does not allow IP address...
 		validation.isTrue(ns != null
 				&& (ns.startsWith("http://") || ns.startsWith("https://")));
 
 		validation.required(name);
 		validation.url(ns);
 		validation.required(prefix);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			dsb();
 		}
 
 		try {
 			TopicAware ta = Locator.getDSBTopicManagement(getNode());
 			Topic topic = new Topic();
 			topic.setName(name);
 			topic.setNs(ns);
 			topic.setPrefix(prefix);
 			ta.add(topic);
 			flash.success("Topic has been created on the DSB %s",
 					topic.toString());
 
 		} catch (Exception e) {
 			handleException("Can not create topic", e);
 		}
 		dsb();
 	}
 	
 	public static void deleteDSB(String name, String ns, String prefix) {
 		validation.required(name);
 		validation.required(prefix);
 
 		// validation url does not allow IP address...
 		validation.isTrue(ns != null
 				&& (ns.startsWith("http://") || ns.startsWith("https://")));
 
 		validation.required(name);
 		validation.url(ns);
 		validation.required(prefix);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			dsb();
 		}
 
 		try {
 			TopicAware ta = Locator.getDSBTopicManagement(getNode());
 			Topic topic = new Topic();
 			topic.setName(name);
 			topic.setNs(ns);
 			topic.setPrefix(prefix);
 			ta.delete(topic);
 			flash.success("Topic has been deleted from the DSB %s",
 					topic.toString());
 
 		} catch (Exception e) {
 			handleException("Can not delete topic", e);
 		}
 		dsb();
 	}
 
 	public static void topic(String name, String ns, String prefix) {
 		render(name, ns, prefix);
 	}
 	
 	/**
 	 * Create a subscriber topic in the platform
 	 * 
 	 */
	public static void consumer() {
 		render();
 	}
 	
	public static void createConsumer() {
		consumer();
 	}
 	
 	/**
 	 * Create a producer topic in the platform
 	 * 
 	 */
 	public static void producer() {
 		render();
 	}
 	
 	public static void createProducer() {
 		producer();
 	}
 
 }
