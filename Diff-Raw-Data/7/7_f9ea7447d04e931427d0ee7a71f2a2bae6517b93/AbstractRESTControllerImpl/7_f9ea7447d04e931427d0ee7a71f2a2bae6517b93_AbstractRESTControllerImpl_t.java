 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.GenericRESTController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.security.Role;
 import no.niths.services.interfaces.GenericService;
 
 import org.hibernate.NonUniqueObjectException;
 import org.hibernate.TransientObjectException;
 import org.hibernate.exception.ConstraintViolationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.HttpStatus;
 import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Abstract class that holds logic for CRUD operations on a given domain type
  * 
  * To add new controllers, create an interface that extends
  * GenericRESTController<Your_Domain> Then create a class that extends
  * AbstractRESTControllerImpl<Your_domaim> implements YourInterface
  * 
  * <pre>
  * {@code
  * @Autowire your service and create a new list in 
  * @package no.niths.application.rest.list and 
  * @Override the two methods:
  * public GenericService<Your_domain> getService() public
  * ListAdapter<Your_domain> getList() to return your service and your list
  * }
  * </pre>
  * 
  * Add extra methods by defining them in the interface and override them in the
  * implementation class Call super.methodName(parameter) to execute CRUD methods
  * 
  * @param <T>
  *            The domain type
  * 
  */
 public abstract class AbstractRESTControllerImpl<T> implements
 		GenericRESTController<T> {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(AbstractRESTControllerImpl.class);
 
 	/**
 	 * Persists the domain
 	 * 
 	 * @param domain
 	 *            the domain to persist
 	 * 
 	 *            <pre>
 	 * {@code
 	 * @PreAuthorize(SecurityConstants.ONLY_ADMIN)//Optional security public
 	 * void create(@RequestBody Your_domain domain){
 	 * 		super.create(domain); 
 	 * }
 	 * </pre>
 	 */
 	@Override
 	@RequestMapping(method = RequestMethod.POST)
 	@ResponseStatus(value = HttpStatus.CREATED, reason = "Created")
 	public void create(@RequestBody T domain) {
 		getService().create(domain);
 	}
 
 	/**
 	 * Returns the domain object with the given id
 	 * 
 	 * @param id
 	 *            the id of the domain object
 	 * @return the domain object
 	 * @throws ObjectNotFoundException when object is not found
 	 * 
 	 *  Usage in your own class:
 	 *	<pre>
 	 * {@code
 	 * @Override
 	 * @PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	 * public You_Domain getById(@PathVariable Long id) {
 	 * 		return super.getById(id);
 	 * }
 	 * </pre>
 	 * 
 	 */
 	@Override
 	@RequestMapping(value = "{id}", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public T getById(@PathVariable Long id) {
 		logger.info("method used");
 		T domain = getService().getById(id);
 		ValidationHelper.isObjectNull(domain);
 		return domain;
 	}
 
 	  /**
      * 
      * Returns an arraylist with all domain objects of the type
      * 
      * @param domain will search the DB for instances with the same attributes,
      * 				if null, all will be returned
      * @return List of all domain objects
      * 
      * Usage in your own class:
 	 *	<pre>
 	 * {@code
 	 * @Override
 	 * @PreAuthorize(SecurityConstants.ONLY_ADMIN) //Optional security
 	 * public ArrayList<Your_Domain> getAll(Your_Domain domain) {
 	 * 		ArrayList<Your_Domain> all = super.getAll(domain);
 	 * return roles;
 	 * }
      */
 	@Override
 	@RequestMapping(method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public ArrayList<T> getAll(T domain) {
 		renewList(getService().getAll(domain));
 		return getList();
 	}
 
 	/**
 	 * Update the domain object
 	 * 
 	 * @param domain the domain
 	 * @throws ObjectNotFoundException when object is not found
 	 * 
 	 *  Usage in your own class:
 	 *	<pre>
 	 * {@code
 	 * @Override
 	 * @PreAuthorize(SecurityConstants.ONLY_ADMIN)//Optional security public
 	 * void update(@RequestBody Your_domain domain){
 	 * 		super.update(domain); 
 	 * }
 	 * </pre>
 	 * 
 	 */
 	@Override
 	@RequestMapping(method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Update OK")
 	public void update(@RequestBody T domain) {
 		logger.info("method used");
 
 		try {
 			getService().update(domain);
 		} catch (TransientObjectException e) {
 			throw new ObjectNotFoundException(e.getMessage().toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Deprecated
 	public void delete(@PathVariable Long id) {
 		if (!getService().delete(id)) {
 			throw new ObjectNotFoundException(
 					"Could not find the object to delete");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void renewList(List<T> list) {
 		getList().clear();
 		getList().addAll(list);
 		getList().setData(getList()); // Used for XML marshaling
 		ValidationHelper.isListEmpty(getList());
 	}
 
 	/**
 	 * Deletes the domain object with the given id
      * 
      * @param id the if of the domain object to be deleted
 	 * 
 	 * Usage in your own class:
 	 *	<pre>
 	 * {@code
 	 * @Override
 	 * @PreAuthorize(SecurityConstants.ONLY_ADMIN)//Optional security public
 	 * void hibernateDelete(@PathVariable long id){
 	 * 		super.hibernateDelete(id); 
 	 * }
 	 * </pre>
      * 
      * 
 	 */
 	@Override
 	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
 	public void hibernateDelete(@PathVariable long id) {
 		try {
 			getService().hibernateDelete(id);
 		} catch (HibernateOptimisticLockingFailureException e) {
 			throw new ObjectNotFoundException("Could not find the object");
 		}
 	}
 
 	/**
 	 * Represents the service
 	 * 
 	 * Must override in own implementation
 	 * 
 	 *	<pre>
 	 * {@code
 	 * @Override
 	 * public GenericService<Your_domain> getService() {
 	 * 		return yourService;
 	 * }
 	 * </pre>
 	 * 
 	 * @return the service of a given type
 	 */
 	public abstract GenericService<T> getService();
 
 	/**
 	 * Adapter for xml presentation of a list
 	 * 
 	 * Must override in own implementation
 	 * 
 	 *	<pre>
 	 * {@code
 	 * @Override
 	 * public ListAdapter<Your_Domain> getList() {
 	 * 		return your_domainList;
 	 * }
 	 * </pre>
 	 * 
 	 * @return Arraylist of a given type
 	 */
 	public abstract ListAdapter<T> getList();
 
 	
 	
 	
 	/**
 	 * EXCEPTIONHANDLING
 	 * 
 	 * Throwing exceptions with custom error header as a response
 	 * 
 	 */
 
 	/**
 	 * PUT - Error with header parameters
 	 * 
 	 * Catches constraint violation exceptions Ex: Leader already added to
 	 * committee
 	 */
 	@ExceptionHandler(ConstraintViolationException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT)
 	public void constraintViolation(ConstraintViolationException cve,
 			HttpServletResponse res) {
 		logger.debug("hibernate.constraintvia");
 
 		res.setHeader("Error", cve.getMessage().toString());
 	}
 
 	/**
 	 * PUT - Error with header parameters
 	 * 
 	 * Catches constraint violation exceptions Ex: Leader already added to
 	 * committee
 	 */
 	@ExceptionHandler(javax.validation.ConstraintViolationException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT)
 	public void constraintViolation2(
 			javax.validation.ConstraintViolationException cve,
 			HttpServletResponse res) {
 		logger.debug("javax.constraint");
 		res.setHeader("Error", cve.getMessage().toString());
 	}
 
 	/**
 	 * POST- Error with header parameters
 	 * 
 	 * Catches constraint violation exceptions Ex: Leader already added to
 	 * committee
 	 */
 	@ExceptionHandler(DataIntegrityViolationException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT)
 	public void dataIntegrity(DataIntegrityViolationException e,
 			HttpServletResponse res) {
 		logger.debug("data");
 		res.setHeader("Error", e.getMessage().toString());
 	}
 
 	/**
 	 * When server fetches an object and try to insert it into an collection
 	 * where the object already is Ex: niths/committees/addEvent/1/5
 	 */
 	@ExceptionHandler(org.hibernate.NonUniqueObjectException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Sorry, it is already a member of the collection")
 	public void notUniqueObjectEx(NonUniqueObjectException e,
 			HttpServletResponse res) {
 		res.setHeader("Error", e.getMessage().toString());
 	}
 
 	/**
 	 * Catches illegal arguments Ex: When you try to insert a subject into a
 	 * committee
 	 */
 	@ExceptionHandler(java.lang.IllegalArgumentException.class)
 	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
 	public void notValidParams(java.lang.IllegalArgumentException e,
 			HttpServletResponse res) {
 		res.setHeader("Error", e.getMessage().toString());
 	}
 
 	/**
 	 * Catches access denied exceptions ExpiredTokenException,
 	 * UnvalidTokenException etc...
 	 */
 	@ExceptionHandler(ObjectNotFoundException.class)
 	@ResponseStatus(value = HttpStatus.NO_CONTENT)
 	public void objectNotFound(ObjectNotFoundException e,
 			HttpServletResponse res) {
 		logger.debug("Object not found AbstractRestController");
 		res.setHeader("Error", e.getMessage().toString());
 	}
 
 	/**
 	 * Catches access denied exceptions ExpiredTokenException,
 	 * UnvalidTokenException etc...
 	 */
 	@ExceptionHandler(AccessDeniedException.class)
 	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public void accessDenied(AccessDeniedException e, HttpServletResponse res) {
		if(e.getMessage() == null){
			res.setHeader("Error", "Access denied");
		}
 		logger.debug("Access denied cathed in AbstractRestController");
 	}
 
 }
