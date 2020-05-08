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
 package eu.playproject.platform.service.bootstrap;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import eu.playproject.platform.service.bootstrap.api.Subscription;
 
 /**
  * Stores the subscriptions
  * 
  * @author chamerling
  * 
  */
 public class SubscriptionManagerServiceImpl implements
 		eu.playproject.platform.service.bootstrap.api.SubscriptionManager {
 
 	private static Logger logger = Logger
 			.getLogger(SubscriptionManagerServiceImpl.class.getName());
 
 	List<Subscription> subscriptions;
 
 	public SubscriptionManagerServiceImpl() {
 		this.subscriptions = new ArrayList<Subscription>();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.playproject.platform.service.bootstrap.api.SubscriptionManager#
 	 * addSubscription
 	 * (eu.playproject.platform.service.bootstrap.api.Subscription)
 	 */
 	@Override
 	public void addSubscription(Subscription subscription) {
 		if (subscription != null) {
 			subscriptions.add(subscription);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.playproject.platform.service.bootstrap.api.SubscriptionManager#
 	 * getSubscriptions()
 	 */
 	@Override
 	public List<Subscription> getSubscriptions() {
 		return subscriptions;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.playproject.platform.service.bootstrap.api.SubscriptionManager#unsubscribe
 	 * ()
 	 */
 	@Override
	public List<Subscription> unsubscribe() {
 		List<Subscription> result = new ArrayList<Subscription>();
 		for (Subscription subscription : subscriptions) {
 			if (this.unsubscribe(subscription)) {
 				result.add(subscription);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public boolean unsubscribe(Subscription subscription) {
 		logger.info("Unsubscribe from " + subscription);
 		// TODO
 		return false;
 	}
 
 }
