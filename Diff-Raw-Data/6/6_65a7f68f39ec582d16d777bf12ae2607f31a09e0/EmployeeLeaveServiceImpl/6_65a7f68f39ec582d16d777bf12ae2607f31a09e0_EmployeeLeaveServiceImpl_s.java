 package com.madrone.lms.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.madrone.lms.constants.LMSConstants;
 import com.madrone.lms.dao.EmployeeDao;
 import com.madrone.lms.dao.EmployeeLeaveDao;
 import com.madrone.lms.entity.Employee;
 import com.madrone.lms.entity.EmployeeLeave;
 import com.madrone.lms.entity.Leave;
 import com.madrone.lms.form.LeaveDetailsGrid;
 import com.madrone.lms.form.LeaveForm;
 import com.madrone.lms.service.EmployeeLeaveService;
 import com.madrone.lms.utils.DateUtils;
 
 @Service("employeeLeaveService")
 @Transactional(readOnly = true)
 public class EmployeeLeaveServiceImpl implements EmployeeLeaveService {
 
 	@Autowired
 	EmployeeLeaveDao empLeaveDao;
 
 	@Autowired
 	EmployeeDao empDao;
 
 	@Override
 	public EmployeeLeave findById(long id) {
 		return empLeaveDao.findById(id);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void deleteEmployeeLeave(long id) {
 		EmployeeLeave el = findById(id);
 		empLeaveDao.delete(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void saveEmployeeLeave(LeaveForm applyLeaveForm) {
 		EmployeeLeave el = setBeanValuesForSave(applyLeaveForm);
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_PENDING);
 
 		empLeaveDao.saveOrUpdate(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void cancelEmployeeLeave(LeaveForm applyLeaveForm) {
 		EmployeeLeave el = setBeanValuesForSave(applyLeaveForm);
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_CANCEL);
 		empLeaveDao.update(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void approveEmployeeLeave(LeaveForm approveLeaveForm) {
 		EmployeeLeave el = setBeanValuesForSave(approveLeaveForm);
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_APPROVE);
 		empLeaveDao.update(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void rejectEmployeeLeave(LeaveForm approveForm) {
 		EmployeeLeave el = setBeanValuesForSave(approveForm);
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_REJECT);
 		empLeaveDao.update(el);
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getLeaveList(String userName) {
 		Employee emp = empDao.findByEmailAddress(userName);
 		List<EmployeeLeave> leaveList = empLeaveDao.getLeaveList(emp);
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 		if (leaveList != null) {
 			returnList = setBeanValuesForGrid(leaveList);
 		}
 		return returnList;
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getLeaveListOfTeam(String userName,
 			String filter) {
 		Employee leadEmployee = empDao.findByEmailAddress(userName);
 		List<Employee> teamList = empDao.findTeamList(leadEmployee);
 		List<LeaveDetailsGrid> teamLeaveList = new ArrayList<LeaveDetailsGrid>();
 		List<EmployeeLeave> leaveList = new ArrayList<EmployeeLeave>();
 
 		for (Employee emp : teamList) {
 			if ("ALL".equals(filter)) {
 				leaveList = empLeaveDao.getPendingLeaveList(emp);
 			} else if ("A".equals(filter)) {
 				leaveList = empLeaveDao.getApprovalLeaveList(emp);
 			} else if ("R".equals(filter)) {
 				leaveList = empLeaveDao.getRejectionLeaveList(emp);
 			} else if ("C".equals(filter)) {
 				leaveList = empLeaveDao.getCancellationLeaveList(emp);
 			}
 
 			for (EmployeeLeave el : leaveList) {
 				LeaveDetailsGrid bean = new LeaveDetailsGrid();
 				bean.setId(String.valueOf(el.getId()));
 				bean.setEmpId(el.getEmployee().getId());
 				bean.setEmpName(el.getEmployee().getFirstName());
 				bean.setFromDateSession(el.getFromDateSession());
				bean.setFromDate(DateUtils.convertCalendarToString(el
						.getFromDate()));
				bean.setToDate(DateUtils.convertCalendarToString(el
						.getFromDate()));
 				bean.setToDateSession(el.getToDateSession());
 				bean.setLeaveType(el.getLeave().getId());
 				bean.setNoOfDays(el.getNoOfDays());
 				bean.setStatus(el.getLeaveStatus());
 				teamLeaveList.add(bean);
 			}
 		}
 
 		return teamLeaveList;
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getPendingLeaveList(String userName) {
 		Employee emp = empDao.findByEmailAddress(userName);
 		List<EmployeeLeave> leaveList = empLeaveDao.getPendingLeaveList(emp);
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 		if (leaveList != null) {
 			returnList = setBeanValuesForGrid(leaveList);
 		}
 		return returnList;
 	}
 
 	private EmployeeLeave setBeanValuesForSave(LeaveForm leaveForm) {
 
 		EmployeeLeave el = new EmployeeLeave();
 		Employee e = empDao.findById(leaveForm.getEmpId());
 		el.setEmployee(e);
 
 		Leave l = new Leave();
 		l.setId(leaveForm.getLeaveType());
 		el.setLeave(l);
 
 		el.setId(leaveForm.getId());
 
 		el.setFromDate(DateUtils.convertStringToCalendar(leaveForm
 				.getFromDate()));
 		el.setToDate(DateUtils.convertStringToCalendar(leaveForm.getToDate()));
 		el.setFromDateSession(leaveForm.getFromDateSession());
 		el.setToDateSession(leaveForm.getToDateSession());
 		el.setNoOfDays(leaveForm.getNoOfDays());
 		el.setCancellationComments(leaveForm.getReason());
 		el.setEmergencyPhoneNumber(leaveForm.getEmergencyPhone());
 		return el;
 
 	}
 
 	private List<LeaveDetailsGrid> setBeanValuesForGrid(
 			List<EmployeeLeave> leaveList) {
 
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 
 		for (EmployeeLeave el : leaveList) {
 			LeaveDetailsGrid bean = new LeaveDetailsGrid();
 			bean.setId(String.valueOf(el.getId()));
 			bean.setEmpId(el.getEmployee().getId());
 			bean.setEmpName(el.getEmployee().getFirstName());
 			bean.setFromDateSession(el.getFromDateSession());
 			bean.setFromDate(DateUtils.convertCalendarToString(el.getFromDate()));
 			bean.setToDate(DateUtils.convertCalendarToString(el.getToDate()));
 			bean.setToDateSession(el.getToDateSession());
 			bean.setReason(el.getReasonForLeave());
 			bean.setLeaveType(el.getLeave().getId());
 			bean.setNoOfDays(el.getNoOfDays());
 			bean.setStatus(el.getLeaveStatus());
 			returnList.add(bean);
 		}
 		return returnList;
 
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getPendingAndApprovalLeaveList(String userName) {
 		Employee emp = empDao.findByEmailAddress(userName);
 		List<EmployeeLeave> leaveList = empLeaveDao.getPendingAndApprovalList(emp);
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 		if (leaveList != null) {
 			returnList = setBeanValuesForGrid(leaveList);
 		}
 		return returnList;
 	}
 
 }
