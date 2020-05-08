 package no.niths.application.rest;
 
 import java.io.EOFException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.application.rest.exception.DuplicateEntryCollectionException;
 import no.niths.application.rest.exception.HasNotRoleException;
 import no.niths.application.rest.exception.NotInCollectionException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.exception.UnvalidEmailException;
 import no.niths.application.rest.interfaces.GenericRESTController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.services.interfaces.GenericService;
 
 import org.hibernate.NonUniqueObjectException;
 import org.hibernate.QueryParameterException;
 import org.hibernate.TransientObjectException;
 import org.hibernate.exception.ConstraintViolationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.converter.HttpMessageNotReadableException;
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
  * GenericRESTController<your_domain>, then create a class that extends
  * AbstractRESTControllerImpl<your_domain> and implements YourInterface
  * 
  * <pre>
  * {@code
  * @Autowire your service and create a new list in 
  * @package no.niths.application.rest.list and 
  * @Override the two methods:
  * public GenericService<Your_domain> getService() public
  * 		ListAdapter<Your_domain> getList() to return your service and your list
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
 	private static final String ERROR = "Error";
 	private static final String INFO = "Info";
 	
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
 			logger.debug(domain +"");
 			getService().create(domain);
 	}
 
 	/**
 	 * Returns the domain object with the given id
 	 * 
 	 * @param id
 	 *            the id of the domain object
 	 * @return the domain object
 	 * @throws ObjectNotFoundException
 	 *             when object is not found
 	 * 
 	 *             Usage in your own class:
 	 * 
 	 *             <pre>
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
 		T domain = getService().getById(id);
 		ValidationHelper.isObjectNull(domain);
 		logger.debug(domain.toString());
 		return domain;
 	}
 
 	/**
 	 * 
 	 * Returns an array list with all domain objects of the type
 	 * 
 	 * @param domain
 	 *            will search the DB for instances with the same attributes, if
 	 *            null, all will be returned
 	 * @return List of all domain objects
 	 * 
 	 *         Usage in your own class:
 	 * 
 	 *         <pre>
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
 	 * Returns an array list with all exams like getAll(domain), 
 	 * but also supports pagination
 	 * <p>
 	 * @param domain object with attributes to search for
 	 * @param firstResult the first result in the result set
 	 * @param maxResults the number of result to return
 	 */
 	@Override
 	@RequestMapping(value = "paginated/{firstResult}/{maxResults}", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public ArrayList<T> getAll(T domain,@PathVariable int firstResult, @PathVariable int maxResults) {
 		renewList(getService().getAll(domain, firstResult, maxResults));
 		return getList();
 	}
 
 	/**
 	 * Update the domain object
 	 * 
 	 * @param domain
 	 *            the domain
 	 * @throws ObjectNotFoundException
 	 *             when object is not found
 	 * 
 	 *             Usage in your own class:
 	 * 
 	 *             <pre>
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
 		logger.debug("Update");
 		logger.debug(domain + "");
 
 		try {
 			getService().mergeUpdate(domain);
 		} catch (TransientObjectException e) {
 			throw new ObjectNotFoundException(e.getMessage().toString());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Deprecated
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
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
 	 * @param id
 	 *            the if of the domain object to be deleted
 	 * 
 	 *            Usage in your own class:
 	 * 
 	 *            <pre>
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
 	 * <pre>
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
 	 * <pre>
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
 		res.setHeader(ERROR, cve.getMessage().toString());
 	}
 
 	/**
 	 * PUT - Error with header parameters
 	 * 
 	 * Catches constraint violation exceptions Ex: Leader already added to
 	 * committee
 	 */
 	@ExceptionHandler(javax.validation.ConstraintViolationException.class)
 	@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
 	public void constraintViolation2(
 			javax.validation.ConstraintViolationException cve,
 			HttpServletResponse res) {
 		logger.debug("javax.constraint");
 		res.setHeader(ERROR, cve.getMessage().toString());
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
 		res.setHeader(ERROR, e.getMessage().toString());
 	}
 
 	/**
 	 * When server fetches an object and try to insert it into an collection
 	 * where the object already is Example: niths/committees/addEvent/1/5
 	 */
 	@ExceptionHandler(org.hibernate.NonUniqueObjectException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Sorry, it is already a member of the collection")
 	public void notUniqueObjectEx(NonUniqueObjectException e,
 			HttpServletResponse res) {
 		res.setHeader(ERROR, e.getMessage().toString());
 	}
 
 	/**
 	 * Catches illegal arguments Example: When you try to insert a subject into a
 	 * committee
 	 */
 	@ExceptionHandler(java.lang.IllegalArgumentException.class)
 	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
 	public void notValidParams(java.lang.IllegalArgumentException e,
 			HttpServletResponse res) {
 		res.setHeader(ERROR, e.getMessage().toString());
 	}
 
 	/**
 	 * Catches access denied exceptions ExpiredTokenException,
 	 * InvalidTokenException etc...
 	 */
 	@ExceptionHandler(ObjectNotFoundException.class)
 	@ResponseStatus(value = HttpStatus.NO_CONTENT)
 	public void objectNotFound(ObjectNotFoundException e,
 			HttpServletResponse res) {
 		logger.debug("Object not found AbstractRestController");
 		res.setHeader(ERROR, e.getMessage().toString());
 	}
 
 	/**
 	 * Catches invalid email exceptions
 	 */
 	@ExceptionHandler(UnvalidEmailException.class)
 	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
 	public void unvalidEmailException(UnvalidEmailException e,
 			HttpServletResponse res) {
 		res.setHeader(ERROR, e.getMessage().toString());
 	}
 
 	/**
 	 * Catches access denied exceptions ExpiredTokenException,
 	 * InvalidTokenException etc...
 	 */
 	@ExceptionHandler(AccessDeniedException.class)
 	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
 	public void accessDenied(AccessDeniedException e, HttpServletResponse res) {
 		if (e.getMessage() == null) {
 			res.setHeader(ERROR, "Access denied");
 		}
 		logger.debug("Access denied cathed in AbstractRestController");
 	}
 
 	/**
 	 * Catches QueryParameterException, invalid query
 	 */
 	@ExceptionHandler(QueryParameterException.class)
 	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
 	public void invalidSearchParam(QueryParameterException e,
 			HttpServletResponse res) {
 		res.setHeader(ERROR, "Invalid Search param");
 		logger.debug("Invalid search param");
 	}
 
 
 	@ExceptionHandler(HttpMessageNotReadableException.class)
 	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
 	public void httpMessageNotReadableException(
 			HttpMessageNotReadableException e, HttpServletResponse res) {
 
 			res.setHeader(ERROR, "Error with HTTP body");
 
 		logger.debug("Invalid search param");
 	}
 	
 	/**
 	 * 
 	 * @param e
 	 * @param res
 	 */
 	@ExceptionHandler(EOFException.class)
 	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
 	public void endOfFile(EOFException e, HttpServletResponse res) {
 		res.setHeader(ERROR, "Wrong input");
 	}
 	
 	/**
 	 * 
 	 * @param e
 	 * @param res
 	 */
 	@ExceptionHandler(DuplicateEntryCollectionException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT)
 	public void duplicateEntryCollectionException(DuplicateEntryCollectionException e, HttpServletResponse res) {
 		if (e.getMessage() == null) {
			res.setHeader(ERROR,
 					"DuplicateEntry");
 		} else {
			res.setHeader(ERROR, e.getMessage());
 
 		}
 		logger.debug("DuplicateEntry");
 	}
 	
 	/**
 	 * 
 	 * @param e
 	 * @param res
 	 */
 	@ExceptionHandler(HasNotRoleException.class)
 	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
 	public void hasNotRole(HasNotRoleException e, HttpServletResponse res) {
 		if (e.getMessage() == null) {
 			res.setHeader(ERROR,
 					"Does not have role");
 		} else {
 			res.setHeader(ERROR, e.getMessage());
 			
 		}
 	}
 	
 	/**
 	 * 
 	 * @param e
 	 * @param res
 	 */
 	@ExceptionHandler(NotInCollectionException.class)
 	@ResponseStatus(value = HttpStatus.NO_CONTENT)
 	public void notInCollectionException(NotInCollectionException e, HttpServletResponse res) {
 		if (e.getMessage() == null) {
 			res.setHeader(ERROR,
 					"NotInCollectionException");
 		} else {
 			res.setHeader(ERROR, e.getMessage());
 		}
 		logger.debug("NotInCollectionException");
 	}
 	
 	/**
 	 * 
 	 * @param e
 	 * @param res
 	 */
 	@ExceptionHandler(TypeMismatchException.class)
 	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
 	public void typeMismatchException(TypeMismatchException e, HttpServletResponse res) {
 		if (e.getMessage() == null) {
 			res.setHeader(ERROR,
 					"TypeMismatchException");
 		} else {
 			res.setHeader(ERROR, e.getMessage());
 		}
 		logger.debug("TypeMismatchException");
 	}
 	/**
 	 * 
 	 * @param e
 	 * @param res
 	 */
 	@ExceptionHandler(org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException.class)
 	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
 	public void staleEx(org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException e, HttpServletResponse res) {
 	
 		res.setHeader(ERROR, "Something went wrong, are the parameters correct?");
 		
 	}
 }
