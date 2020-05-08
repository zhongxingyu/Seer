 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.interfaces.EventController;
 import no.niths.application.rest.lists.EventList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.domain.Event;
 import no.niths.services.interfaces.EventsService;
 import no.niths.services.interfaces.GenericService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Controller for events
  */
 @Controller
 @RequestMapping(AppConstants.EVENTS)
 public class EventControllerImpl extends AbstractRESTControllerImpl<Event>
 		implements EventController {
 
 	@Autowired
 	private EventsService service;
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(EventControllerImpl.class);
 
 	private EventList eventList = new EventList();
 	
	
	@Override
	public Event getById(@PathVariable Long id) {
		Event e = super.getById(id);
		e.getLocation().setEvents(null);
		e.getLocation().setFeeds(null);
		return e;
	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ArrayList<Event> getAll(Event domain) {
 		super.getAll(domain);
 
 		
 		for (Event e : eventList) {
 			e.setLocation(null);
 		}
 		
 		return eventList;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void create(@RequestBody Event domain) {
 		super.create(domain);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void update(@RequestBody Event domain) {
 		super.update(domain);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public GenericService<Event> getService() {
 		return service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ListAdapter<Event> getList() {
 		return eventList;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@RequestMapping(value = { "tag={tag}" }, method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public List<Event> getEventsByTag(@PathVariable String tag) {
 		
 		renewList(service.getEventsByTag(tag));
 		for (Event e : eventList) {
 			e.setLocation(null);
 		}
 		
 		return eventList;
 	}
 }
