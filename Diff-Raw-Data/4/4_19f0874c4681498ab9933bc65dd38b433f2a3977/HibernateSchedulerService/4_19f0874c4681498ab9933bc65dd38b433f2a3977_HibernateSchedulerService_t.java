 /**
  * 
  */
 package se.mrpeachum.scheduler.service;
 
 import java.net.URISyntaxException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.http.client.utils.URIBuilder;
 import org.codehaus.jackson.node.ObjectNode;
 import org.codehaus.jackson.node.TextNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.oauth2.client.OAuth2RestTemplate;
 import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.client.RestOperations;
 
 import se.mrpeachum.scheduler.controllers.oauth.OAuth2Handler;
 import se.mrpeachum.scheduler.dao.EmployeeDao;
 import se.mrpeachum.scheduler.dao.PositionDao;
 import se.mrpeachum.scheduler.dao.ShiftDao;
 import se.mrpeachum.scheduler.dao.UserDao;
 import se.mrpeachum.scheduler.entities.Employee;
 import se.mrpeachum.scheduler.entities.Position;
 import se.mrpeachum.scheduler.entities.Shift;
 import se.mrpeachum.scheduler.entities.ShiftDto;
 import se.mrpeachum.scheduler.entities.User;
 import se.mrpeachum.scheduler.exception.RedirectException;
 
 /**
  * @author eolsson
  * 
  */
 @Service
 @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
 public class HibernateSchedulerService implements SchedulerService {
 
 	@Autowired
 	private RestOperations googleRestTemplate;
 
 	@Autowired
 	private UserDao userDao;
 
 	@Autowired
 	private PositionDao positionDao;
 
 	@Autowired
 	private EmployeeDao employeeDao;
 
 	@Autowired
 	private ShiftDao shiftDao;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateSchedulerService.class);
 
 	private static final int ONE_DAY_MS = 86_400_000;
 
 	private static final DateFormat YEAR_WEEK_FORMAT = new SimpleDateFormat("YYYYww");
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * se.mrpeachum.scheduler.service.SchedulerService#fetchOrSaveUser(javax
 	 * .servlet.http.HttpSession)
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public User fetchOrSaveUser(HttpSession session) {
 		ObjectNode res = null;
 		try {
 			String code = (String) session.getAttribute(OAuth2Handler.AUTH_CODE);
 			if (code != null) {
 				((OAuth2RestTemplate) googleRestTemplate).getOAuth2ClientContext().getAccessTokenRequest()
 						.setAuthorizationCode(code);
 			}
 			res = googleRestTemplate.getForObject("https://www.googleapis.com/oauth2/v1/userinfo", ObjectNode.class);
 		} catch (UserRedirectRequiredException redirectException) {
 			try {
 				URIBuilder builder = new URIBuilder(redirectException.getRedirectUri());
 				for (Map.Entry<String, String> entry : redirectException.getRequestParams().entrySet()) {
 					builder.addParameter(entry.getKey(), entry.getValue());
 				}
 				throw new RedirectException("redirect:" + builder.build());
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 			}
 		} catch (Exception e) {
 			session.invalidate();
 			throw new RedirectException("redirect:");
 		}
 		TextNode name = (TextNode) res.get("name");
 		TextNode email = (TextNode) res.get("email");
 		TextNode id = (TextNode) res.get("id");
 
 		User user = userDao.findByAccountId(id.asText());
 
 		if (user == null) {
 			user = new User();
 			user.setAccountId(id.asText());
 			user.setName(name.asText());
 			user.setEmail(email.asText());
 			userDao.save(user);
 			userDao.flush();
 		}
 
 		return user;
 	}
 
 	@Override
 	@Transactional
 	public List<Position> getPositions(User user) {
 		return positionDao.getPositionsForUser(user);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void mergePositions(User user, List<Position> newPositions) {
 		LOGGER.debug("New positions class: {}, toString: {}", newPositions.getClass(), newPositions);
 		List<Position> existingPositions = positionDao.getPositionsForUser(user);
 		List<Position> positionsToRemove = new ArrayList<>();
 		// see if any existing are now missing (aka deleted)
 		for (Position pos : existingPositions) {
 			if (!newPositions.contains(pos)) {
 				positionsToRemove.add(pos);
 			}
 		}
 		// delete the removed positions
 		for (Position pos : positionsToRemove) {
 			shiftDao.deleteForPositionId(pos);
 			positionDao.delete(pos);
 		}
 		for (Position newPosition : newPositions) {
 			if (existingPositions.contains(newPosition)) {
 				for (Position existingPos : existingPositions) {
 					if (existingPos.getId() == newPosition.getId()) {
 						existingPos.setName(newPosition.getName());
 						existingPos.setColor(newPosition.getColor());
 						positionDao.save(existingPos);
 						break;
 					}
 				}
 			} else {
 				newPosition.setUser(user);
 				positionDao.save(newPosition);
 			}
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void mergeEmployees(User user, List<Employee> newEmployees) {
 		List<Employee> existingEmployees = employeeDao.getEmployeesForUser(user);
 		List<Employee> remove = new ArrayList<>();
 
 		for (Employee emp : existingEmployees) {
 			if (!newEmployees.contains(emp)) {
 				remove.add(emp);
 			}
 		}
 
 		for (Employee emp : remove) {
 			shiftDao.deleteForEmployeeId(emp);
 			employeeDao.delete(emp);
 		}
 
 		int i = 0;
 		for (Employee emp : newEmployees) {
 			if (existingEmployees.contains(emp)) {
 				for (Employee existing : existingEmployees) {
 					if (existing.equals(emp)) {
 						existing.setOrder(i++);
 						employeeDao.save(existing);
 					}
 				}
 			} else {
 				emp.setUser(user);
 				emp.setOrder(i++);
 				employeeDao.save(emp);
 			}
 		}
 	}
 
 	@Override
 	@Transactional
 	public List<Employee> getEmployees(User user) {
 		List<Employee> employees = employeeDao.getEmployeesForUser(user);
 		Collections.sort(employees);
 		return employees;
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void saveShift(ShiftDto dto, User user) {
 		Employee emp = employeeDao.findById(dto.getEmployee());
 		Position pos = positionDao.findByNameAndUser(dto.getPosition(), user);
 		Calendar startDate = Calendar.getInstance(Locale.US);
 		startDate.setTime(dto.getStartDate());
 		Calendar endDate = Calendar.getInstance(Locale.US);
 		endDate.setTime(dto.getEndDate());
 
 		for (int i = 1; i <= 7; i++) {
 			if (dto.shouldCopyToDayOfWeek(i)) {
 				LOGGER.debug("Adding shift to day #{}", i);
 				addShiftForDay(startDate.get(Calendar.HOUR), startDate.get(Calendar.MINUTE),
 						endDate.get(Calendar.HOUR), endDate.get(Calendar.MINUTE), dto.getDay(), user, i, emp, pos);
 			}
 		}
 		employeeDao.save(emp);
 	}
 
 	private void addShiftForDay(final Integer startHour, final Integer startMinute, final Integer endHour,
 			final Integer endMinute, final long firstDayOfWeek, final User user, final int dayOfWeek,
 			final Employee emp, final Position pos) {
 		Shift shift = new Shift();
 
 		shift.setStartHour((startHour == 0 ? 12 : startHour));
 		shift.setStartMinute(startMinute);
 		shift.setEndHour((endHour == 0 ? 12 : endHour));
 		shift.setEndMinute(endMinute);
 
 		shift.setPosition(pos);
 
 		Date day = new Date(firstDayOfWeek + ((dayOfWeek - 1) * ONE_DAY_MS));
 		shift.setDay(day);
 		shift.setUser(user);
 		shift.setEmployee(emp);
 
 		shiftDao.save(shift);
 
 		LOGGER.debug("Added to: {}", day);
 
 		emp.getShifts().add(shift);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void deleteShift(Long id, User user) {
 		Shift shift = shiftDao.findById(id);
 		if (!shift.getUser().equals(user)) {
 			throw new IllegalStateException("Cannot delete a shift that user doesn't own");
 		}
 		Employee emp = shift.getEmployee();
 		for (Iterator<Shift> iter = emp.getShifts().iterator(); iter.hasNext();) {
 			if (iter.next().equals(shift)) {
 				iter.remove();
 			}
 		}
 		// remove the link to this shift from the employee
 		employeeDao.save(emp);
 
 		// then remove the shift
 		shiftDao.delete(shift);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void copyShifts(Long employeeId, Date fromWeek, Date toWeek, User user) throws Exception {
 		Employee emp = employeeDao.findById(employeeId);
 		if (!emp.getUser().equals(user)) {
 			throw new IllegalStateException("Cannot copy shifts that user doesn't own");
 		}
 		LOGGER.debug("Copying shifts from week {} to week {} for employee {}", new Object[]{fromWeek, toWeek, employeeId});
 
		for (int i = 1; i <= 7; i++) {
			Date dayCheck = new Date(fromWeek.getTime() + ((i - 1) * ONE_DAY_MS));
 			List<Shift> shifts = emp.getShiftsForDayMillis(dayCheck.getTime());
 			LOGGER.debug("Day {} ({}): {} shifts to copy", new Object[]{i, dayCheck, shifts.size()});
 			for (Shift s : shifts) {
 				this.addShiftForDay(s.getStartHour(), s.getStartMinute(), s.getEndHour(), s.getEndMinute(),
 						toWeek.getTime(), user, i, emp, s.getPosition());
 			}
 		}
 		employeeDao.save(emp);
 	}
 
 }
