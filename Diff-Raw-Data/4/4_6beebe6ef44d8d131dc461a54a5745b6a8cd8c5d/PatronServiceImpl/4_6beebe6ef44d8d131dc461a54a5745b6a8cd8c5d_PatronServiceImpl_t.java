 package com.twistlet.falcon.model.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.twistlet.falcon.controller.bean.User;
 import com.twistlet.falcon.model.entity.FalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.FalconLocation;
 import com.twistlet.falcon.model.entity.FalconPatron;
 import com.twistlet.falcon.model.entity.FalconRole;
 import com.twistlet.falcon.model.entity.FalconService;
 import com.twistlet.falcon.model.entity.FalconStaff;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.FalconUserRole;
 import com.twistlet.falcon.model.repository.FalconAppointmentPatronRepository;
 import com.twistlet.falcon.model.repository.FalconPatronRepository;
 import com.twistlet.falcon.model.repository.FalconUserRepository;
 import com.twistlet.falcon.model.repository.FalconUserRoleRepository;
 
 @Service
 public class PatronServiceImpl implements PatronService {
 	
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	private final FalconPatronRepository falconPatronRepository;
 	
 	private final FalconUserRepository falconUserRepository;
 	
 	private final FalconUserRoleRepository falconUserRoleRepository;
 	
 	private final FalconAppointmentPatronRepository falconAppointmentPatronRepository;
 	
 	private final PasswordEncoder passwordEncoder;
 	
 	
 	@Autowired
 	public PatronServiceImpl(FalconPatronRepository falconPatronRepository,
 			FalconUserRepository falconUserRepository,
 			FalconUserRoleRepository falconUserRoleRepository,
 			FalconAppointmentPatronRepository falconAppointmentPatronRepository,
 			PasswordEncoder passwordEncoder) {
 		this.falconPatronRepository = falconPatronRepository;
 		this.falconUserRepository = falconUserRepository;
 		this.falconUserRoleRepository = falconUserRoleRepository;
 		this.falconAppointmentPatronRepository = falconAppointmentPatronRepository;
 		this.passwordEncoder = passwordEncoder;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<User> listRegisteredPatrons(FalconUser admin) {
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserByAdmin(admin);
 		List<User> patrons = new ArrayList<>();
 		User user = null;
 		for(FalconPatron falconPatron : falconPatrons){
 			FalconUser falconUser = falconPatron.getFalconUserByPatron();
 			user = new User();
 			user.setName(falconUser.getName() + " (" + falconUser.getNric() + ")");
 			user.setUsername(falconUser.getUsername());
 			patrons.add(user);
 		}
 		return patrons;
 	}
 
	
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void savePatron(FalconPatron patron) {
 		FalconUser user = patron.getFalconUserByPatron();
 		FalconUser admin = patron.getFalconUserByAdmin();
 		String[] names = StringUtils.split(patron.getFalconUserByPatron().getName(), " ");
 		boolean newUser = false;
 		if(StringUtils.isBlank(user.getUsername())){
 			user.setUsername(user.getEmail());
 			user.setValid(true);
 			newUser = true; 
 		}
 		if(newUser){
 			if(StringUtils.isBlank(user.getPassword())){
 				logger.info("password: " + names[0] + " salt:" + user.getUsername());
 				user.setPassword(passwordEncoder.encodePassword(names[0], user.getUsername()));
 			}
 			patron.setFalconUserByPatron(user);
 			FalconRole falconRole = new FalconRole();
 			falconRole.setRoleName("ROLE_USER");
 			FalconUserRole falconUserRole = new FalconUserRole();
 			falconUserRole.setFalconUser(user);
 			falconUserRole.setFalconRole(falconRole);
 			/**
 			 * check if user already exist
 			 */
 			FalconUser registeredUser = falconUserRepository.findOne(user.getUsername());
 			if(registeredUser == null){
 				/**
 				 * user does not exist. save user and user_role. We do not validete duplicate hp, nric etc already validated in view
 				 */
 				falconUserRepository.save(user);
 				falconUserRoleRepository.save(falconUserRole);
 			}else{
 				/**
 				 * update user to use latest info. comment this if we dont want to update user details
 				 */
 				registeredUser.setNric(user.getNric());
 				registeredUser.setPhone(user.getPhone());
 				registeredUser.setName(user.getName());
 				registeredUser.setSendEmail(user.getSendEmail());
 				registeredUser.setSendSms(user.getSendSms());
 				registeredUser.setValid(true);
 				falconUserRepository.save(registeredUser);
 			}
 		}else{
 			FalconUser updateUser = falconUserRepository.findOne(user.getUsername());
 			if(user.getValid() == null){
 				user.setValid(false);
 			}
 			if(user.getValid() == false){
 				/**
 				 * user trying to delete
 				 */
 				Set<FalconPatron> patrons = updateUser.getFalconPatronsForPatron();
 				List<FalconPatron> toDeletePatrons = new ArrayList<>();
 				List<FalconAppointmentPatron> toDeleteAppointments = new ArrayList<>();
 				for(FalconPatron registeredPatron : patrons){
 					if(registeredPatron.getFalconUserByAdmin().getUsername().equals(admin.getUsername())){
 						toDeletePatrons.add(registeredPatron);
 					}
 					List<FalconAppointmentPatron> registeredAppointment = falconAppointmentPatronRepository.findByFalconPatron(registeredPatron);
 					toDeleteAppointments.addAll(registeredAppointment);
 				}
 				if(CollectionUtils.size(toDeletePatrons) < 2){
 					updateUser.setValid(user.getValid());
 				}
 				falconAppointmentPatronRepository.delete(toDeleteAppointments);
 				falconPatronRepository.delete(toDeletePatrons);
 			}
 			updateUser.setName(user.getName());
 			updateUser.setNric(user.getNric());
 			updateUser.setEmail(user.getEmail());
 			updateUser.setSendEmail(user.getSendEmail());
 			updateUser.setSendSms(user.getSendSms());
 			falconUserRepository.save(updateUser);
 		}
 		List<FalconPatron> patrons = falconPatronRepository.findByFalconUserByAdminAndFalconUserByPatron(patron.getFalconUserByAdmin(), patron.getFalconUserByPatron());
		if(CollectionUtils.isEmpty(patrons) && patron.getFalconUserByAdmin() != null && StringUtils.isNotBlank(patron.getFalconUserByAdmin().getUsername())){
 			falconPatronRepository.save(patron);
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public Set<User> listAvailablePatrons(FalconUser admin, Date start, Date end) {
 		List<User> allPatron = listRegisteredPatrons(admin);
 		Set<User> availablePatron = new HashSet<>();
 		Set<FalconPatron> busyPatrons = falconPatronRepository.findPatronsDateRange(admin, start, end);
 		for(User user: allPatron){
 			boolean found = false;
 			for(FalconPatron patron : busyPatrons){
 				if(StringUtils.equals(user.getUsername(), patron.getFalconUserByPatron().getUsername())){
 					found = true;
 					break;
 				}
 			}
 			if(!found){
 				availablePatron.add(user);
 			}
 		}
 		return availablePatron;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public FalconPatron findPatron(String patron, String admin) {
 		FalconUser falconPatron = new FalconUser();
 		falconPatron.setUsername(patron);
 		FalconUser falconAdmin = new FalconUser();
 		falconAdmin.setUsername(admin);
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserByAdminAndFalconUserByPatron(falconAdmin, falconPatron);
 		FalconPatron uniqeFalconPatron = DataAccessUtils.uniqueResult(falconPatrons);
 		return uniqeFalconPatron;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconPatron> listPatronByAdminNameLike(FalconUser admin, String name) {
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserNameLike(admin, name);
 		List<FalconPatron> validPatrons = new ArrayList<>();
 		for(FalconPatron falconPatron : falconPatrons){
 			if(StringUtils.equals(falconPatron.getFalconUserByAdmin().getUsername(), admin.getUsername())){
 				validPatrons.add(falconPatron);
 			}
 		}
 		return validPatrons;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconPatron> listPatronByAdminNricLike(FalconUser admin, String nric) {
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserNricLike(admin, nric);
 		List<FalconPatron> validPatrons = new ArrayList<>();
 		for(FalconPatron falconPatron : falconPatrons){
 			if(StringUtils.equals(falconPatron.getFalconUserByAdmin().getUsername(), admin.getUsername())){
 				validPatrons.add(falconPatron);
 			}
 		}
 		return validPatrons;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconPatron> listPatronByAdminEmailLike(FalconUser admin, String email) {
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserEmailLike(admin, email);
 		List<FalconPatron> validPatrons = new ArrayList<>();
 		for(FalconPatron falconPatron : falconPatrons){
 			if(StringUtils.equals(falconPatron.getFalconUserByAdmin().getUsername(), admin.getUsername())){
 				validPatrons.add(falconPatron);
 			}
 		}
 		return validPatrons;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconPatron> listPatronByAdminMobileLike(FalconUser admin,
 			String mobile) {
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserHpTelLike(admin, mobile);
 		List<FalconPatron> validPatrons = new ArrayList<>();
 		for(FalconPatron falconPatron : falconPatrons){
 			if(StringUtils.equals(falconPatron.getFalconUserByAdmin().getUsername(), admin.getUsername())){
 				validPatrons.add(falconPatron);
 			}
 		}
 		return validPatrons;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconPatron> listPatronByAdminPatronLike(FalconUser admin, FalconUser patron) {
 		List<FalconPatron> patrons = falconPatronRepository.findByFalconUserPatronLike(admin, patron);
 		List<FalconPatron> matchingPatrons = new ArrayList<>();
 		for(FalconPatron falconPatron : patrons){
 			falconPatron.getFalconUserByPatron().getName();
 			if(StringUtils.equals(falconPatron.getFalconUserByAdmin().getUsername(), admin.getUsername())){
 				matchingPatrons.add(falconPatron);
 			}
 		}
 		if(StringUtils.isNotEmpty(admin.getUsername())){
 			patrons = matchingPatrons;
 		}
 		return patrons;
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void deletePatron(FalconPatron patron) {
 		List<FalconPatron> patrons = listPatronByAdminPatronLike(patron.getFalconUserByAdmin(), patron.getFalconUserByPatron());
 		FalconPatron uniquePatron = null;
 		for(FalconPatron record : patrons){
 			if(StringUtils.equals(record.getFalconUserByAdmin().getUsername(), patron.getFalconUserByAdmin().getUsername())){
 				uniquePatron = record;
 				break;
 			}
 		}
 		if(uniquePatron != null){
 			falconPatronRepository.delete(uniquePatron);
 			if(CollectionUtils.size(patrons) < 2){
 				falconUserRepository.delete(uniquePatron.getFalconUserByPatron());
 			}
 		}
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public FalconPatron findPatron(String patron, boolean isAdmin) {
 		FalconUser falconPatron = new FalconUser();
 		falconPatron.setUsername(patron);
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserByPatron(falconPatron);
 		FalconPatron uniqeFalconPatron = null;
 		try {
 			uniqeFalconPatron = DataAccessUtils.uniqueResult(falconPatrons);
 		} catch (IncorrectResultSizeDataAccessException e) {
 			/**
 			 * user is registered with multiple or no admin
 			 */
 			if(CollectionUtils.isNotEmpty(falconPatrons)){
 				uniqeFalconPatron = falconPatrons.get(0);
 			}
 		}
 		FalconUser falconUser = uniqeFalconPatron.getFalconUserByPatron();
 		FalconUser falconAdmin = uniqeFalconPatron.getFalconUserByAdmin();
 		logger.info("name: " + falconUser.getName());
 		logger.info("admin: " + falconAdmin.getName());
 		FalconPatron theFalconPatron = new FalconPatron();
 		theFalconPatron.setFalconUserByPatron(falconUser);
 		theFalconPatron.setFalconUserByAdmin(falconAdmin);
 		theFalconPatron.setId(uniqeFalconPatron.getId());
 		return theFalconPatron;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconUser> listUserByCriteria(FalconUser user) {
 		List<FalconUser> users = falconUserRepository.findByCriteria(user);
 		for(FalconUser falconUser : users){
 			Set<FalconPatron> registeredPatrons = falconUser.getFalconPatronsForAdmin();
 			for(FalconPatron patron : registeredPatrons){
 				patron.getFalconUserByAdmin().getUsername();
 			}
 		}
 		for(FalconUser falconUser : users){
 			Set<FalconPatron> registeredPatrons = falconUser.getFalconPatronsForPatron();
 			for(FalconPatron patron : registeredPatrons){
 				patron.getFalconUserByAdmin().getUsername();
 			}
 		}
 		return users;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public List<FalconPatron> listAllPatronsAdmin(FalconUser patron) {
 		List<FalconPatron> falconPatrons = falconPatronRepository.findByFalconUserByPatron(patron);
 		for(FalconPatron registeredAdmins : falconPatrons){
 			registeredAdmins.getFalconUserByAdmin().getName();
 			registeredAdmins.getFalconUserByPatron().getName();
 			if(CollectionUtils.isNotEmpty(registeredAdmins.getFalconUserByAdmin().getFalconStaffs())){
 				for(FalconStaff staff : registeredAdmins.getFalconUserByAdmin().getFalconStaffs()){
 					staff.getName();
 					staff.getId();
 					staff.setFalconAppointments(null);
 					staff.setFalconUser(null);
 				}
 			}
 			if(CollectionUtils.isNotEmpty(registeredAdmins.getFalconUserByAdmin().getFalconLocations())){
 				for(FalconLocation location : registeredAdmins.getFalconUserByAdmin().getFalconLocations()){
 					location.getName();
 					location.getId();
 					location.setFalconAppointments(null);
 					location.setFalconUser(null);
 				}
 			}
 			if(CollectionUtils.isNotEmpty(registeredAdmins.getFalconUserByAdmin().getFalconServices())){
 				for(FalconService service : registeredAdmins.getFalconUserByAdmin().getFalconServices()){
 					service.getName();
 					service.getId();
 					service.setFalconAppointments(null);
 					service.setFalconUser(null);
 				}
 			}
 		}
 		return falconPatrons;
 	}
 	
 	
 	
 
 }
