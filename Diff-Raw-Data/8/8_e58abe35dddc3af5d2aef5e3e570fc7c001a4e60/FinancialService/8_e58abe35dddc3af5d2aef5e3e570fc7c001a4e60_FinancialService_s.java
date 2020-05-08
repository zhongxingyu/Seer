 package com.financial.tools.recorderserver.service;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.financial.tools.recorderserver.business.DeviceManager;
 import com.financial.tools.recorderserver.business.FinancialManager;
 import com.financial.tools.recorderserver.business.UserManager;
 import com.financial.tools.recorderserver.entity.BudgetTrail;
 import com.financial.tools.recorderserver.entity.BudgetTrailType;
 import com.financial.tools.recorderserver.entity.FinancialRecord;
 import com.financial.tools.recorderserver.entity.FinancialRecordStatus;
 import com.financial.tools.recorderserver.exception.ErrorCode;
 import com.financial.tools.recorderserver.exception.FinancialRecorderException;
 import com.financial.tools.recorderserver.payload.AddFinancialRecordUsersRequest;
 import com.financial.tools.recorderserver.payload.CashinRequest;
 import com.financial.tools.recorderserver.payload.FinancialRecordListResponse;
 import com.financial.tools.recorderserver.payload.FinancialRecordRequest;
 import com.financial.tools.recorderserver.payload.FinancialRecordResponse;
 import com.financial.tools.recorderserver.payload.UserBudgetTrailResponse;
 import com.financial.tools.recorderserver.payload.UserFinancialInfoResponse;
 import com.financial.tools.recorderserver.transactionlog.aop.TransactionLog;
 import com.financial.tools.recorderserver.transactionlog.aop.TransactionLogType;
 import com.financial.tools.recorderserver.transactionlog.entry.TransactionLogEntry;
 import com.financial.tools.recorderserver.transactionlog.entry.TransactionLogThreadLocalContext;
 import com.financial.tools.recorderserver.util.CopyUtils;
 import com.financial.tools.recorderserver.util.FinancialRecorderConstants;
 import com.financial.tools.recorderserver.util.NotificationHelper;
 import com.google.common.collect.Lists;
 
 @Path("/finance")
 @Produces(MediaType.APPLICATION_JSON)
 public class FinancialService {
 
 	private static final Logger logger = LoggerFactory.getLogger(FinancialService.class);
 
 	private FinancialManager financialManager;
 
 	private DeviceManager deviceManager;
 
 	private UserManager userManager;
 
 	@POST
 	@Path("/cashin")
 	@Produces(MediaType.TEXT_PLAIN)
 	@TransactionLog(type = TransactionLogType.CASH_IN)
 	public String cashin(CashinRequest request) {
 		TransactionLogEntry entry = TransactionLogThreadLocalContext.getEntry();
 
 		float balance = financialManager.cashin(request.getUserName(), request.getAmount());
 
 		// collect transaction log info.
 		entry.setAmount(request.getAmount()).setUserNameList(Lists.newArrayList(request.getUserName()));
 
 		// send notification.
 		String deviceRegId = deviceManager.getRegisteredDeviceId(request.getUserName());
 		String notificationMessage = String.format("Hi %1$s, cashed in %2$.2f RMB for you.", request.getUserName(),
 				request.getAmount());
 		boolean result = NotificationHelper.sendNotification(deviceRegId, BudgetTrailType.CASH_IN.getValue(),
 				notificationMessage, -1);
 		if (!result) {
 			deviceManager.unRegister(request.getUserName(), deviceRegId);
 		}
 
 		return String.valueOf(balance);
 	}
 
 	@POST
 	@Path("/create")
 	@Produces(MediaType.TEXT_PLAIN)
 	@TransactionLog(type = TransactionLogType.CREATE_FINANCIAL_RECORD)
 	public Response createFinancialRecord(FinancialRecordRequest financialRecordRequest) {
 		TransactionLogEntry entry = TransactionLogThreadLocalContext.getEntry();
 
 		long financialRecordId = financialManager.createFinancialRecord(financialRecordRequest);
 
 		entry.setFinancialRecordId(financialRecordId).setFinancialRecordName(financialRecordRequest.getName())
 				.setFee(financialRecordRequest.getTotalFee()).setUserNameList(financialRecordRequest.getUserNameList());
 
 		return Response.ok(financialRecordId).build();
 	}
 
 	@POST
 	@Path("/addUsers")
 	@Produces(MediaType.APPLICATION_JSON)
 	public FinancialRecordResponse addFinancialRecordUsers(AddFinancialRecordUsersRequest request) {
 		FinancialRecord financialRecord = financialManager.getFinancialRecord(request.getFinancialRecordId());
 		if (financialRecord == null) {
 			throw new FinancialRecorderException(ErrorCode.FINANCIAL_RECORD_NOT_EXIST_ERROR,
 					"No financial record found by id: " + request.getFinancialRecordId());
 		}

		List<String> userNameList = Lists.newArrayList(financialRecord.getUserNames().split(
				FinancialRecorderConstants.USER_NAME_SEPARATE));
 		List<String> newJoinedUserNameList = Lists.newArrayList();
 		for (String userName : request.getUserNameList()) {
 			if (!userNameList.contains(userName)) {
 				newJoinedUserNameList.add(userName);
 				userNameList.add(userName);
 			}
 		}
 
 		if (newJoinedUserNameList.isEmpty()) {
 			logger.info("No new user joined into financial record, id: {}", request.getFinancialRecordId());
 			return CopyUtils.convertFinancialRecord2Response(financialRecord);
 		}
 
 		String userNames = StringUtils.join(userNameList, FinancialRecorderConstants.USER_NAME_SEPARATE);
 		financialRecord.setUserNames(userNames);
 		FinancialRecord updateFinancialRecord = financialManager.updateFinancialRecord(financialRecord);
 		for (String joinedUserName : newJoinedUserNameList) {
 			userManager.addUserRecord(joinedUserName, request.getFinancialRecordId());
 		}
 
 		return CopyUtils.convertFinancialRecord2Response(updateFinancialRecord);
 	}
 
 	@GET
 	@Path("/list/{financialRecordStatus}")
 	public FinancialRecordListResponse listFinancialRecords(
 			@PathParam("financialRecordStatus") int financialRecordStatus) {
 		FinancialRecordStatus status = FinancialRecordStatus.getStatusByValue(financialRecordStatus);
 		if (status == null) {
 			throw new FinancialRecorderException(ErrorCode.FINANCIALRECORD_STATUS_INVALID_ERROR,
 					"Invalid status for financial record.");
 		}
 		return new FinancialRecordListResponse(financialManager.listFinancialRecordsByStatus(status));
 	}
 
 	@GET
 	@Path("/update/{financialRecord}")
 	@Produces(MediaType.TEXT_PLAIN)
 	@TransactionLog(type = TransactionLogType.UPDATE_FINANCIAL_RECORD)
 	public String updateFinance(@PathParam("financialRecord") String financialRecordId) {
 		TransactionLogEntry entry = TransactionLogThreadLocalContext.getEntry();
 
 		long financialRecordIdValue = Long.valueOf(financialRecordId);
 		financialManager.deductFee(financialRecordIdValue);
 
 		entry.setFinancialRecordId(financialRecordIdValue);
 		return "";
 	}
 
 	@GET
 	@Path("/info/{userId}")
 	public UserFinancialInfoResponse getUserFinancialInfo(@PathParam("userId") String userId) {
 		return financialManager.getUserFinancialInfo(Long.valueOf(userId));
 	}
 
 	@GET
 	@Path("/search")
 	public UserBudgetTrailResponse searchUserBudgetTrail(@QueryParam("userName") String userName) {
 		List<BudgetTrail> userBudgetTrailList = financialManager.searchUserBudgetTrail(userName);
 		return new UserBudgetTrailResponse(userBudgetTrailList);
 	}
 
 	@Autowired
 	public void setFinancialManager(FinancialManager financialManager) {
 		this.financialManager = financialManager;
 	}
 
 	@Autowired
 	public void setDeviceManager(DeviceManager deviceManager) {
 		this.deviceManager = deviceManager;
 	}
 
 	@Autowired
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
 
 }
