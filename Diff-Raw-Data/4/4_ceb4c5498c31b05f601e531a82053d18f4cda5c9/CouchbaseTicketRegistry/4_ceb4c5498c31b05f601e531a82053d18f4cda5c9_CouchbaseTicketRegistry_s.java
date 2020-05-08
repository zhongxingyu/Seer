 package se.kth.infosys.login.couchbase;
 
 /*
    Copyright (C) 2013 KTH, Kungliga tekniska hogskolan, http://www.kth.se
 
    In part derived from work with the following copyright:
 
    Copyright 2010, JA-SIG, Inc., http://www.jasig.org/
 
    This file is part of cas-server-integration-couchbase.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 import org.jasig.cas.monitor.TicketRegistryState;
 import org.jasig.cas.ticket.ServiceTicket;
 import org.jasig.cas.ticket.Ticket;
 import org.jasig.cas.ticket.TicketGrantingTicket;
 import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
 
 import com.couchbase.client.protocol.views.Query;
 import com.couchbase.client.protocol.views.View;
 import com.couchbase.client.protocol.views.ViewDesign;
 import com.couchbase.client.protocol.views.ViewResponse;
 import com.couchbase.client.protocol.views.ViewRow;
 
 /**
  * A Ticket Registry storage backend which uses the memcached protocol. 
  * CouchBase is a multi host NoSQL database with a memcached interface 
  * to persistent storage which also is quite usable as a replicated
  * tickage storage engine for multiple front end CAS servers.
  */
 public final class CouchbaseTicketRegistry extends AbstractDistributedTicketRegistry implements TicketRegistryState {
 	/* Couchbase client factory */
 	@NotNull
 	private CouchbaseClientFactory couchbase;
 
 	@Min(0)
 	private int tgtTimeout;
 
 	@Min(0)
 	private int stTimeout;
 
 
 	/**
 	 * Default constructor
 	 */
 	public CouchbaseTicketRegistry() {}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void updateTicket(final Ticket ticket) {
 		log.debug("Updating ticket {}", ticket);
 		try {
 			if (!couchbase.getClient().replace(ticket.getId(), getTimeout(ticket), ticket).get()) {
 				log.error("Failed updating {}", ticket);
 			}
 		} catch (final InterruptedException e) {
 			log.warn("Interrupted while waiting for response to async replace operation for ticket {}. " +
 					"Cannot determine whether update was successful.", ticket);
 		} catch (final Exception e) {
 			log.error("Failed updating {}", ticket, e);
 		}
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addTicket(final Ticket ticket) {
 		log.debug("Adding ticket {}", ticket);
 		try {
 			if (!couchbase.getClient().add(ticket.getId(), getTimeout(ticket), ticket).get()) {
 				log.error("Failed adding {}", ticket);
 			}
 		} catch (final InterruptedException e) {
 			log.warn("Interrupted while waiting for response to async add operation for ticket {}. " +
 					"Cannot determine whether add was successful.", ticket);
 		} catch (final Exception e) {
 			log.error("Failed adding {}", ticket, e);
 		}
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean deleteTicket(final String ticketId) {
 		log.debug("Deleting ticket {}", ticketId);
 		try {
 			return couchbase.getClient().delete(ticketId).get();
 		} catch (final Exception e) {
 			log.error("Failed deleting {}", ticketId, e);
 		}
 		return false;
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Ticket getTicket(final String ticketId) {
 		try {
 			final Ticket t = (Ticket) couchbase.getClient().get(ticketId);
 			if (t != null) {
 				return getProxiedTicketInstance(t);
 			}
 		} catch (final Exception e) {
 			log.error("Failed fetching {} ", ticketId, e);
 		}
 		return null;
 	}
 
 
 	/**
 	 * Starts the couchbase client.
 	 */
 	public void initialize() {
 		couchbase.ensureIndexes(UTIL_DOCUMENT, ALL_VIEWS);
 		couchbase.initialize();
 	}
 
 
 	/**
 	 * Stops the couchbase client.
 	 */
 	public void destroy() throws Exception {
 		couchbase.shutdown();
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean needsCallback() {
 		return true;
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Collection<Ticket> getTickets() {
 		List<Ticket> tickets = new ArrayList<Ticket>();
 		View allKeys = couchbase.getClient().getView(UTIL_DOCUMENT, ALL_TICKETS_VIEW.getName());
 		Query query = new Query();
 		query.setIncludeDocs(true);
 		ViewResponse response = couchbase.getClient().query(allKeys, query);
 		Iterator<ViewRow> iterator = response.iterator();
 
 		while (iterator.hasNext()) {
 			Ticket ticket = getTicket(iterator.next().getId());
			tickets.add(ticket);
 		}
 
 		return Collections.unmodifiableCollection(tickets);
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int sessionCount() {
 		View tgtCountView = couchbase.getClient().getView(UTIL_DOCUMENT, TGT_COUNT_VIEW.getName());
 		return getCountFromView(tgtCountView);
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int serviceTicketCount() {
 		View stCountView = couchbase.getClient().getView(UTIL_DOCUMENT, ST_COUNT_VIEW.getName());
 		return getCountFromView(stCountView);
 	}
 
 
 	private int getCountFromView(View view) {
 		Query query = new Query();
 		query.setIncludeDocs(false);
 		ViewResponse response = (ViewResponse) couchbase.getClient().query(view, query);
 		Iterator<ViewRow> iterator = response.iterator();
 		if (iterator.hasNext()) {
 			ViewRow res = response.iterator().next();
 			return Integer.valueOf(res.getValue());
 		} else {
 			return 0;
 		}
 	}
 
 
 	/**
 	 * Sets the time after which a ticket granting ticket will be
 	 * purged from the registry.
 	 * 
 	 * @param tgtTimeout Ticket granting ticket timeout in seconds.
 	 */
 	public void setTgtTimeout(final int tgtTimeout) {
 		this.tgtTimeout = tgtTimeout;
 	}
 
 
 	/**
 	 * Sets the time after which a session ticket will be purged
 	 * from the registry.
 	 * 
 	 * @param stTimeout Session ticket timeout in seconds.
 	 */
 	public void setStTimeout(final int stTimeout) {
 		this.stTimeout = stTimeout;
 	}
 
 
 	private int getTimeout(final Ticket t) {
 		if (t instanceof TicketGrantingTicket) {
 			return tgtTimeout;
 		} else if (t instanceof ServiceTicket) {
 			return stTimeout;
 		}
 		throw new IllegalArgumentException("Invalid ticket type");
 	}
 
 
 	/**
 	 * @param Couchbase client factory to use.
 	 */
 	public void setCouchbase(final CouchbaseClientFactory couchbase) {
 		this.couchbase = couchbase;
 	}
 
 
 	/*
 	 * Views, or indexes, in the database. 
 	 */
 	private static final ViewDesign ALL_TICKETS_VIEW = new ViewDesign(
 			"all_tickets", 
 			"function(d,m) {emit(m.id);}");
 	private static final ViewDesign ST_COUNT_VIEW = new ViewDesign(
 			"st_count",
 			"function (d,m) {if (m.id.lastIndexOf(\"ST-\") === 0) {emit(m.id);}}",
 			"_count");
 	private static final ViewDesign TGT_COUNT_VIEW = new ViewDesign(
 			"tgt_count",
 			"function (d,m) {if (m.id.lastIndexOf(\"TGT-\") === 0) {emit(m.id);}}",
 			"_count");
 	private static final List<ViewDesign> ALL_VIEWS = Arrays.asList(new ViewDesign[] {
 			ALL_TICKETS_VIEW,
 			ST_COUNT_VIEW,
 			TGT_COUNT_VIEW
 	});
 	private static final String UTIL_DOCUMENT = "utils";
 }
