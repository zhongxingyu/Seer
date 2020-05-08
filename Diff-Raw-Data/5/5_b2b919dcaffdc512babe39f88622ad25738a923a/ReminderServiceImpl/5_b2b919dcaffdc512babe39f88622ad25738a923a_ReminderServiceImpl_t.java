 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service.impl;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Customizable;
 import com.orangeleap.tangerine.domain.Schedulable;
 import com.orangeleap.tangerine.domain.ScheduledItem;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.service.ReminderService;
 import com.orangeleap.tangerine.service.ScheduledItemService;
 import com.orangeleap.tangerine.service.communication.EmailService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 
 import edu.emory.mathcs.backport.java.util.Collections;
 
 @Service("reminderService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class ReminderServiceImpl extends AbstractTangerineService implements ReminderService {
 	
 
     protected final Log logger = OLLogger.getLog(getClass());
     
     public final static String REMINDER = "reminder";
 
     @Resource(name = "scheduledItemService")
     private ScheduledItemService scheduledItemService;
     
     @Resource(name = "constituentService")
     private ConstituentService constituentService;
     
     @Resource(name = "recurringGiftService")
     private RecurringGiftService recurringGiftService;
     
     @Resource(name = "pledgeService")
     private PledgeService pledgeService;
     
     @Resource(name = "emailSendingService")
     private EmailService emailService;
     
 	@Override
 	public List<ScheduledItem> listReminders(Schedulable schedulable, Date scheduledPaymentDate) {
 		ScheduledItem scheduledPayment = locateScheduledItemByDate(schedulable, scheduledPaymentDate);
 		return scheduledItemService.readSchedule(scheduledPayment);
 	}
 	
 	@Override
 	public void addReminder(Schedulable schedulable, Date scheduledPaymentDate, Date reminderDate) {
 		ScheduledItem scheduledPayment = locateScheduledItemByDate(schedulable, scheduledPaymentDate);
 		ScheduledItem reminder = getReminder(scheduledPayment, reminderDate);
 		scheduledItemService.maintainScheduledItem(reminder);
 	}
 
 	@Override
 	public void deleteReminder(Schedulable schedulable, Date scheduledPaymentDate, Date reminderDate) {
 		ScheduledItem scheduledPayment = locateScheduledItemByDate(schedulable, scheduledPaymentDate);
 		ScheduledItem reminder = locateScheduledItemByDate(scheduledPayment, reminderDate);
 		scheduledItemService.deleteScheduledItem(reminder);
 	}
 	
 	@Override
 	public void deleteReminders(Schedulable schedulable) {
 		List<ScheduledItem> scheduledPayments = scheduledItemService.readSchedule(schedulable);
 		for (ScheduledItem scheduledPayment: scheduledPayments) {
 			scheduledItemService.deleteSchedule(scheduledPayment);
 		}
 	}
 
 	// Locate by date. Should not have two for same date for scheduled payments and reminders.
 	private ScheduledItem locateScheduledItemByDate(Schedulable schedulable, Date date) {
 		List<ScheduledItem> list = scheduledItemService.readSchedule(schedulable);
 		for (ScheduledItem item : list) {
 			if (item.getActualScheduledDate().equals(date)) return item;
 		}
 		throw new RuntimeException("No scheduled item exists for "+date);
 	}
 
 	@Override
 	public void generateDefaultReminders(Schedulable schedulable) {
 		ReminderInfo ri = new ReminderInfo(schedulable);
 	    if (ri.isGenerateReminders()) generateDefaultReminders(schedulable, ri.getInitialReminder(), ri.getMaximumReminders(), ri.getReminderIntervalDays());
 	}
 
 	@Override
 	public void generateDefaultReminders(Schedulable schedulable, Date scheduledPaymentDate) {
 		ReminderInfo ri = new ReminderInfo(schedulable);
 	    if (ri.isGenerateReminders()) generateDefaultReminders(schedulable, scheduledPaymentDate, ri.getInitialReminder(), ri.getMaximumReminders(), ri.getReminderIntervalDays());
 	}
 
 	private void generateDefaultReminders(Schedulable schedulable, int initialReminderDays, int maximumReminders, int reminderIntervalDays) {
 		List<ScheduledItem> scheduledPayments = scheduledItemService.readSchedule(schedulable);
 		for (ScheduledItem scheduledPayment : scheduledPayments) {
 			generateDefaultReminders(scheduledPayment, initialReminderDays, maximumReminders, reminderIntervalDays);
 		}
 	}
 	
 	private void generateDefaultReminders(Schedulable schedulable, Date scheduledPaymentDate, int initialReminderDays, int maximumReminders, int reminderIntervalDays) {
 		ScheduledItem scheduledPayment = locateScheduledItemByDate(schedulable, scheduledPaymentDate);
 		generateDefaultReminders(scheduledPayment, initialReminderDays, maximumReminders, reminderIntervalDays);
 	}
 	
     private final static Date PAST_DATE = new Date(0);
 	
 	private void generateDefaultReminders(ScheduledItem scheduledPayment, int initialReminderDays, int maximumReminders, int reminderIntervalDays) {
 
 		if (maximumReminders > 100) maximumReminders = 0;
 
 		List<ScheduledItem> existingReminders = scheduledItemService.readSchedule(scheduledPayment);
 		
     	// Get last reminder date used
     	Date afterdate = PAST_DATE;
     	for (ScheduledItem item : existingReminders) {
     		if (item.getActualScheduledDate() != null && item.getActualScheduledDate().after(afterdate)) afterdate = item.getActualScheduledDate();
     		if (item.getOriginalScheduledDate() != null && item.getOriginalScheduledDate().after(afterdate)) afterdate = item.getOriginalScheduledDate();
     	}
 		
 		List<Date> datelist = getDateList(scheduledPayment, initialReminderDays, maximumReminders, reminderIntervalDays);
 		
 		for (Date reminderDate : datelist) {
 			if (reminderDate.after(afterdate)) {
 				ScheduledItem reminder = getReminder(scheduledPayment, reminderDate);
 				scheduledItemService.maintainScheduledItem(reminder);
 			}
 		}
 	}
 	
 	private List<Date> getDateList(ScheduledItem scheduledItem, int initialReminderDays, int maximumReminders, int reminderIntervalDays) {
 		
 		List<Date> datelist = new ArrayList<Date>();
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(scheduledItem.getActualScheduledDate());
 		Date paymentDate = cal.getTime();
 		cal.add(Calendar.DATE, -initialReminderDays);
 		for (int i = 0; i < maximumReminders; i++) {
 			datelist.add(cal.getTime());
 			cal.add(Calendar.DATE, reminderIntervalDays);
 			if (!cal.getTime().before(paymentDate)) break;
 		}
 		return datelist;
 	}
 
 	private ScheduledItem getReminder(ScheduledItem scheduledPayment, Date reminderDate) {
 		ScheduledItem reminder = scheduledItemService.getDefaultScheduledItem(scheduledPayment, reminderDate);
 		reminder.setScheduledItemType(REMINDER);
 		return reminder;
 	}
 	
 	@Override
 	public List<ScheduledItem> getRemindersToProcess(Date processingDate) {
 		
 		List<ScheduledItem> list = scheduledItemService.getAllItemsReadyToProcess("scheduleditem", REMINDER, processingDate);
 		removeDuplicatesAndCompletedPayments(list);
 		return list;
 		
 	}
 	
 	// Delete reminders if corresponding scheduled payment is completed, or more than one reminder for the same schedulePayment is ready to be sent.
 	private void removeDuplicatesAndCompletedPayments(List<ScheduledItem> list) {
 		
 		Collections.reverse(list); // Save only the last dated reminder instead of the first one in the case of duplicates.
		Long lastid = null;
 		Iterator<ScheduledItem> it = list.iterator(); 
 		while (it.hasNext()) {
 			ScheduledItem item = it.next();
 			ScheduledItem scheduledPayment = scheduledItemService.readScheduledItemById(item.getSourceEntityId());
 			if (scheduledPayment == null || scheduledPayment.isCompleted() || scheduledPayment.getId().equals(lastid)) {
 				scheduledItemService.deleteScheduledItem(item);
 				it.remove();
 			}
			lastid = scheduledPayment == null ? null : scheduledPayment.getId();
 		}
 		Collections.reverse(list);
 		
 	}
 
 	@Override
 	public Schedulable getParent(ScheduledItem item) {
 		String parentType = item.getSourceEntity();
 		if (parentType.equals("scheduleditem")) return scheduledItemService.readScheduledItemById(item.getSourceEntityId());
 		if (parentType.equals("recurringgift")) return recurringGiftService.readRecurringGiftById(item.getSourceEntityId());
 		if (parentType.equals("pledge")) return pledgeService.readPledgeById(item.getSourceEntityId());
 		return null;
 	}
 	
 	// Parses custom fields on RecurringGift to determine if using reminders
 	public static final class ReminderInfo {
     	
 		private int initialReminder = 0;
     	private int maximumReminders = 0;
     	private int reminderInterval = 0;
     	private boolean valid = true;
 
     	public ReminderInfo(Customizable entity) {
 			try {
 	    		String sinitialReminder = entity.getCustomFieldValue(StringConstants.INITIAL_REMINDER);
 	    		if (sinitialReminder != null) {
 	    			setInitialReminder(Integer.valueOf(sinitialReminder));
 	    		}
 	    		String smaximumReminders = entity.getCustomFieldValue(StringConstants.MAXIMUM_REMINDERS);
 	    		if (smaximumReminders != null) {
 	    			setMaximumReminders(Integer.valueOf(smaximumReminders));
 	    		}
 	    		String sreminderIntervalDays = entity.getCustomFieldValue(StringConstants.REMINDER_INTERVAL);
 	    		if (sreminderIntervalDays != null) {
 	    			setReminderIntervalDays(Integer.valueOf(sreminderIntervalDays));
 	    		}
 	    	} catch (Exception e) {
 	    		// Should already be validated as numeric
 	    		valid = false;
 	    	}
 		}
     	
     	public boolean isGenerateReminders() {
     		return 
     		valid 
     		&& initialReminder != 0 
     		&& maximumReminders > 0 
     		&& ( reminderInterval > 0 || (reminderInterval == 0 && maximumReminders == 1));
     	}
 
 		public void setInitialReminder(int initialReminder) {
 			this.initialReminder = initialReminder;
 		}
 
 		public int getInitialReminder() {
 			return initialReminder;
 		}
 
 		public void setMaximumReminders(int maximumReminders) {
 			this.maximumReminders = maximumReminders;
 		}
 
 		public int getMaximumReminders() {
 			return maximumReminders;
 		}
 
 		public void setReminderIntervalDays(int reminderIntervalDays) {
 			this.reminderInterval = reminderIntervalDays;
 		}
 
 		public int getReminderIntervalDays() {
 			return reminderInterval;
 		}
 		
 	}
 	
 	private static final BigDecimal DEFAULT_MIN_REMINDER_AMOUNT = new BigDecimal(1);
 	
 	@Override
 	public void processReminder(ScheduledItem reminder) {
 		
 		ScheduledItem scheduledPayment = (ScheduledItem)getParent(reminder);
 		
 		BigDecimal minAmount = DEFAULT_MIN_REMINDER_AMOUNT; // TODO add site or default option
 		if (scheduledPayment == null || scheduledPayment.isCompleted() || scheduledPayment.getScheduledItemAmount().compareTo(minAmount) < 0) {
 			scheduledItemService.completeItem(reminder, null, "Skipped");
 			return;
 		}
 			
     	Schedulable schedulable = getParent(scheduledPayment);
     	
     	String status = "";
     	try {
         	if (schedulable instanceof RecurringGift) {
         		status = processRecurringGiftReminder((RecurringGift)schedulable, scheduledPayment);
         	} else if (schedulable instanceof Pledge) {
         		status = processPledgeReminder((Pledge)schedulable, scheduledPayment);
         	} else {
         		throw new RuntimeException("Unknown schedulable type.");
         	}
         	scheduledItemService.completeItem(reminder, null, status);
     	} catch (Exception e) {
     		logger.error("Error processing reminder " + reminder.getId(), e);
     		status = "Error";
     	}
     	
 	}
 	
 	private void addScheduledPaymentDates(ScheduledItem scheduledPayment, Map<String, String> map) {
     	Date scheduledPaymentDate = scheduledPayment.getActualScheduledDate();
 		SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
 		map.put("ScheduledPaymentDate", sdf1.format(scheduledPaymentDate));
 		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
 		map.put("ScheduledPaymentDateDDMMYYYY", sdf2.format(scheduledPaymentDate));
 	}
 	
 	private String processRecurringGiftReminder(RecurringGift recurringGift, ScheduledItem scheduledPayment) {
 
 		Map<String, String> map = new HashMap<String, String>();
 
 		addScheduledPaymentDates(scheduledPayment, map);
 
 		map.put("GiftAmount", scheduledPayment.getScheduledItemAmount().toString());
     	
 		String subject = "Thank you for your commitment!";
 		String template = "recurringGiftReminder";
 		
 		Constituent constituent = constituentService.readConstituentById(recurringGift.getConstituentId());
 		constituent.setSite(siteService.readSite(constituent.getSite().getName()));
 		emailService.sendMail(constituent, null, recurringGift, null, map, subject, template);
 		
 		return "Complete";
 		
 	}
 
 	private String processPledgeReminder(Pledge pledge, ScheduledItem scheduledPayment) {
 
 		Map<String, String> map = new HashMap<String, String>();
 
 		addScheduledPaymentDates(scheduledPayment, map);
 
 		map.put("GiftAmount", scheduledPayment.getScheduledItemAmount().toString());
     	
 		String subject = "Thank you for your pledge!";
 		String template = "pledgeReminder";
 		
 		Constituent constituent = constituentService.readConstituentById(pledge.getConstituentId());
 		constituent.setSite(siteService.readSite(constituent.getSite().getName()));
 		emailService.sendMail(constituent, null, null, pledge, map, subject, template);
 		
 		return "Complete";
 		
 	}
 
 }
