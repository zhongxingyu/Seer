 package org.siraya.rent.user.service;
 import org.siraya.rent.mobile.service.IMobileGatewayService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.user.dao.IDeviceDao;
 import org.siraya.rent.utils.IApplicationConfig;
 import org.siraya.rent.pojo.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 
 import junit.framework.Assert;
 import org.siraya.rent.user.dao.IUserDAO;
 import org.siraya.rent.utils.RentException;
 import org.siraya.rent.utils.RentException.RentErrorCode;
 @Service("mobileAuthService")
 public class MobileAuthService implements IMobileAuthService {
     @Autowired
     private IMobileGatewayService mobileGatewayService;
     @Autowired
     private IApplicationConfig applicationConfig;
     @Autowired
     private IDeviceDao deviceDao;    
     @Autowired
     private IUserDAO userDao;
     private static Logger logger = LoggerFactory.getLogger(MobileAuthService.class);
 	
     
     @Transactional(value = "rentTxManager", propagation = Propagation.SUPPORTS, readOnly = false, rollbackFor = java.lang.Throwable.class)
     public void sendAuthMessage(String deviceId,String userId)throws Exception{
     	logger.debug("device id is "+deviceId+" user id is "+userId);
 		Device device = deviceDao.getDeviceByDeviceIdAndUserId(deviceId,userId);
 		if (device == null) {
 			throw new RentException(RentErrorCode.ErrorNotFound, "device id not exist");
 
 		}
 		device.setUser(userDao.getUserByUserId(device.getUserId()));
 		this.sendAuthMessage(device);
     }
     /**
 	 * 1. check device status to prevent duplicate auth.
 	 * 2. set auth code by random from 0 - 9999
 	 * 3. send message.
 	 */
     void sendAuthMessage(Device device) throws Exception{
     	//
 		// check status and limit
 		//
 		Assert.assertNotNull(device.getToken());
 		
 		if (device.getStatus() != DeviceStatus.Init.getStatus()
 				&& device.getStatus() != DeviceStatus.Authing.getStatus()) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate, "device status isn't init or authing");
 		}
 		//
 		// check retry count
 		//
 		int retryLimit = (Integer)applicationConfig.get("general").get("auth_retry_limit");
 		int currentRetry = device.getAuthRetry();
 		logger.info("current retry count is "+currentRetry);
 		if (retryLimit < currentRetry ) {
 			//
 			// retry too many times, change to suspend status.
 			//
 			logger.debug("update to suspend status");
 			int ret = deviceDao.updateStatusAndRetryCount(device.getId(),
 					device.getUserId(), DeviceStatus.Suspend.getStatus(),
 					DeviceStatus.Authing.getStatus(), device.getModified());
 			if (ret != 1) {
 				throw new RentException(RentErrorCode.ErrorStatusViolate,
 						"update device status count is not 1");
 			}
 			throw new RentException(RentErrorCode.ErrorExceedLimit,
 					"auth retry too many time try");
 		}
 		
 		User user = device.getUser();
 		Assert.assertNotNull("user can't be null",user);
 		
 		//
 		// prepare message.
 		//
 		String phone=user.getMobilePhone();
 	    ResourceBundle resource = ResourceBundle.getBundle("user",user.getLocale());
 		String message = MessageFormat.format(resource.getString("mobile_auth_message"),
 				device.getId(), device.getToken());
 		logger.debug("message is "+message);
 		//
 		// update status
 		//
 		device.setModified(0);
 		int ret =deviceDao.updateStatusAndRetryCount(device.getId(), device.getUserId(),
 				DeviceStatus.Authing.getStatus(),
 				DeviceStatus.Init.getStatus(), device.getModified());
 		if (ret != 1) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate,
 					"update device status count is not 1");
 		}
 
 		//
 		// send message through gateway.
 		//
 		mobileGatewayService.sendSMS(phone, message);
 		device.setAuthRetry(device.getAuthRetry()+1);
 		device.setStatus(DeviceStatus.Authing.getStatus());
 	}
 
 	/**
 	 * set user dao for testing
 	 * @param deviceDao
 	 */
 	public void setDeviceDao(IDeviceDao deviceDao) {
 		this.deviceDao = deviceDao;
 	}
 
 	public void setUserDao(IUserDAO userDao){
 		this.userDao = userDao;
 	}
 	/**
 	 * 
 	 */
 	public void verifyAuthCode(String deviceId, String userId,String authCode) throws Exception{
 		//
 		// check status.
 		//
 		Device device = deviceDao.getDeviceByDeviceIdAndUserId(deviceId,userId);
 		if (device.getUser() == null) {
 			device.setUser(userDao.getUserByUserId(device.getUserId()));			
 		}
 		_verifyAuthCode(device,authCode);
 	}
 
 	private void _verifyAuthCode(Device device,String  authCode){
 		device.setModified(0); // reset modified to get current time.
 		if (device.getStatus() != DeviceStatus.Authing.getStatus()) {
 			throw new RentException(RentErrorCode.ErrorStatusViolate,
 					"device auth status not match current status is "+device.getStatus());
 		}
 		//
 		// if device modified + timeout  > current time then throw timeout exception.
 		// check verify timeout
 		//
     	long time=java.util.Calendar.getInstance().getTimeInMillis()/1000;
 		int timeout = (Integer)applicationConfig.get("general").get("auth_verify_timeout");
     	if (time > device.getModified() + timeout ) {	
 			throw new RentException(RentErrorCode.ErrorAuthExpired,
 					"verify mobile auth code has been timeout");
     	}
     	
 		//
 		// check retry count
 		//
 		int retryLimit = (Integer)applicationConfig.get("general").get("auth_retry_limit");
 		if(logger.isDebugEnabled()) {
 			logger.debug("retry limit is "+retryLimit+" current auth retry is "+device.getAuthRetry());
 		}
 		if (device.getAuthRetry() > retryLimit) {
 			throw new RentException(RentErrorCode.ErrorExceedLimit,
 					"mobile verify auth retry too many time try:"
 							+ device.getAuthRetry() + " > " + retryLimit);
 		}
 
 		//
 		// check auth code
 		//
 		String token = device.getToken();
 		if (!authCode.equals(token)) {
 			//
 			// if fail update retry status
 			//
 			logger.debug("token not match, token is "+token);
 			int ret =deviceDao.updateStatusAndRetryCount(device.getId(), device.getUserId(),
 					DeviceStatus.Authing.getStatus(),
 					DeviceStatus.Init.getStatus(), device.getModified());
 			if (ret != 1) {
 				throw new RentException(RentErrorCode.ErrorStatusViolate,
 						"update device status count is not 1");
 			}
 			throw new RentException(RentErrorCode.ErrorAuthFail,
 					"device id "+device.getId()+" enter auth code not match");
 
 		}
 		
 		//
 		// update device and user in database.
 		//
 		logger.debug("update device status to authed");
 		deviceDao.updateStatusAndRetryCount(device.getId(),device.getUserId(),
 				DeviceStatus.Authed.getStatus(),
 				DeviceStatus.Authing.getStatus(),
 				device.getModified());
 		User user = device.getUser();
 		if (user.getStatus() == UserStatus.Init.getStatus()){
 			logger.debug("update user status to authed");
 			user.setModified((long)0);
 			userDao.updateUserStatus(user.getId(), UserStatus.Authed.getStatus()
 					,  UserStatus.Init.getStatus(), user.getModified());
 		}
 		
 	}
 	
 	/**
 	 * 
 	 */
 	public void verifyAuthCodeByMobilePhone(String mobildPhone,String authCode){
 		User user = userDao.getUserByMobilePhone(mobildPhone);
 		if (user == null) {
 			throw new RentException(RentErrorCode.ErrorNotFound,"use not found");
 		}
 		Device device = deviceDao.getDeviceByUserIdAndStatusAuthing(user.getId());
 		_verifyAuthCode(device,authCode);
 	}
 }
