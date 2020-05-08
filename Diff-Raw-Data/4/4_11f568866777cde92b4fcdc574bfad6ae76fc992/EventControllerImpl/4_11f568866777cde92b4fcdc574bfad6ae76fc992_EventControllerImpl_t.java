 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.exception.DuplicateEntryCollectionException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.helper.TimeDTO;
 import no.niths.application.rest.interfaces.EventController;
 import no.niths.application.rest.lists.EventList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Event;
 import no.niths.domain.location.Location;
 import no.niths.services.interfaces.EventsService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.LocationService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Controller for events
  */
 @Controller
 @RequestMapping(AppConstants.EVENTS)
 public class EventControllerImpl extends AbstractRESTControllerImpl<Event>
 		implements EventController {
 
 	@Autowired
 	private EventsService service;
 
 	@Autowired
 	private LocationService locService;
 	
 	private static final Logger logger = LoggerFactory
 			.getLogger(EventControllerImpl.class);
 
 	private EventList eventList = new EventList();
 	
 	
 	@Override
 	public Event getById(@PathVariable Long id) {
 		Event e = super.getById(id);
 		if(e.getLocation() != null){
 			e.getLocation().setEvents(null);
 			e.getLocation().setFeeds(null);
 		}
 		
 		logger.debug(e.getLocation()+"");
 		return e;
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ArrayList<Event> getAll(Event domain) {
 		eventList = (EventList) super.getAll(domain);
 		clearRelations();
 		return eventList;
 	}
 	
 	@Override
 	public ArrayList<Event> getAll(Event domain, @PathVariable int firstResult, @PathVariable int maxResults) {
 		eventList = (EventList) super.getAll(domain, firstResult, maxResults);
 		clearRelations();
 		return eventList;
 	}
 	
 	private void clearRelations(){
 		for (Event e : eventList) {
 			e.setLocation(null);
 		}
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
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@RequestMapping(value = "add/location/{eventId}/{locId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Location Added")
 	public void addLocation(@PathVariable Long eventId,@PathVariable Long locId) {
 		Event event = service.getById(eventId);
 		ValidationHelper.isObjectNull(event, "Event not exist");
 		
 		if (event.getLocation() != null && event.getLocation().getId() == locId) {
 			logger.debug("location exist");
 			throw new DuplicateEntryCollectionException("Location exist");
 		}
 		
 		Location location = locService.getById(locId);
 		ValidationHelper.isObjectNull(location, "Location does not exist");
 		
 		event.setLocation(location);
 		service.update(event);
 		logger.debug("Location added to event");
 		
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@RequestMapping(value = "remove/location/{eventId}/{locId}", method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Location removed")
 	public void removeLocation(@PathVariable Long eventId,@PathVariable Long locId) {
 		Event event = service.getById(eventId);
 		ValidationHelper.isObjectNull(event, "Event not exist");
 		
 		boolean isRemoved = false;
 		if (event.getLocation() != null && event.getLocation().getId() == locId) {
 			isRemoved = true;
 			event.setLocation(null);
 		}
 
 		if (isRemoved) {
 			service.update(event);
 		} else {
 			logger.debug("Event not Found");
 			throw new ObjectNotFoundException("Event not Found");
 		}
 	}
 	
 	@Override
 	@RequestMapping(value = "dates", method = RequestMethod.GET)
 	@ResponseBody
 	public List<Event> getEventsBetweenDates(TimeDTO timeDTO) {
 		logger.debug(timeDTO +"");
 		ValidationHelper.isObjectNull(timeDTO.getStartTime());
 		
 		if(timeDTO.getEndTime() != null){
 			renewList(service.getEventsBetweenDates(timeDTO.getStartTimeCal(), timeDTO.getEndTimeCal()));
 		}else{
 			renewList(service.getEventsBetweenDates(timeDTO.getStartTimeCal(), null));
 		}
 		
 		for (Event e : eventList) {
 			e.setLocation(null);
 		}
 		return eventList;
 	}
 }
