 package com.chuckanutbay.webapp.common.server;
 
 import static com.chuckanutbay.webapp.common.server.DtoUtils.fromEmployeeDtoFunction;
 import static com.google.common.collect.Lists.newArrayList;
 import static java.util.Collections.sort;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.PeriodType;
 
 import com.chuckanutbay.businessobjects.Employee;
 import com.chuckanutbay.businessobjects.EmployeeWorkInterval;
 import com.chuckanutbay.businessobjects.EmployeeWorkIntervalActivityPercentage;
 import com.chuckanutbay.businessobjects.dao.ActivityDao;
 import com.chuckanutbay.businessobjects.dao.ActivityHibernateDao;
 import com.chuckanutbay.businessobjects.dao.EmployeeDao;
 import com.chuckanutbay.businessobjects.dao.EmployeeHibernateDao;
 import com.chuckanutbay.businessobjects.dao.EmployeeWorkIntervalActivityPercentageDao;
 import com.chuckanutbay.businessobjects.dao.EmployeeWorkIntervalActivityPercentageHibernateDao;
 import com.chuckanutbay.businessobjects.dao.EmployeeWorkIntervalDao;
 import com.chuckanutbay.businessobjects.dao.EmployeeWorkIntervalHibernateDao;
 import com.chuckanutbay.webapp.common.client.TimeClockService;
 import com.chuckanutbay.webapp.common.shared.ActivityDto;
 import com.chuckanutbay.webapp.common.shared.DayReportData;
 import com.chuckanutbay.webapp.common.shared.EmployeeDto;
 import com.chuckanutbay.webapp.common.shared.EmployeeWorkIntervalActivityPercentageDto;
 import com.chuckanutbay.webapp.common.shared.EmployeeWorkIntervalDto;
 import com.chuckanutbay.webapp.common.shared.IntervalDto;
 import com.chuckanutbay.webapp.common.shared.PayPeriodReportData;
 import com.chuckanutbay.webapp.common.shared.WeekReportData;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class TimeClockServiceImpl extends RemoteServiceServlet implements TimeClockService {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Override
 	public EmployeeDto clockIn(Integer barcode) {
 		EmployeeDao employeeDao = new EmployeeHibernateDao();
 		EmployeeWorkIntervalDao intervalDao = new EmployeeWorkIntervalHibernateDao();
 		
 		//Find employee from barcode
 		Employee employee = employeeDao.findEmployeeWithBarcodeNumber(barcode);
 		if (employee == null) {
 			System.out.println("No Employees had the barcode number");
 			return null;
 		}
 		System.out.println("The employee with the barcode number is: " + employee.getFirstName());
 		
 		if (intervalDao.findOpenEmployeeWorkInterval(employee) == null) {
 			//Start a new employee work interval
 			EmployeeWorkInterval newInterval = new EmployeeWorkInterval();
 			newInterval.setEmployee(employee);
 			newInterval.setStartDateTime(new Date());
 			intervalDao.makePersistent(newInterval);
 			
 			System.out.println("The new interval was persisted");
 			
 			//Return a complete EmployeeDto
 			return completeToEmployeeDto(employee);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void clockOut(EmployeeDto employeeDto) {
 		EmployeeWorkIntervalDao intervalDao = new EmployeeWorkIntervalHibernateDao();
 		EmployeeWorkIntervalActivityPercentageDao percentageDao = new EmployeeWorkIntervalActivityPercentageHibernateDao();
 				
 		EmployeeWorkInterval interval = intervalDao.findOpenEmployeeWorkInterval(DtoUtils.fromEmployeeDtoFunction.apply(employeeDto));
 		interval.setEndDateTime(new Date());
 		interval.setComment(employeeDto.getComment());
 		intervalDao.makePersistent(interval);
 		System.out.println("Persisted Interval");
 		
 		List<EmployeeWorkIntervalActivityPercentage> percentages = DtoUtils.transform(employeeDto.getEmployeeWorkIntervalPercentages(), DtoUtils.fromEmployeeWorkIntervalActivityPercentageDtoFunction);
 		for (EmployeeWorkIntervalActivityPercentage percentage : percentages) {
 			percentage.setEmployeeWorkInterval(interval);
 			System.out.println("Percentage: ActivityId(" + percentage.getActivity().getId() + ") IntervalId(" + percentage.getEmployeeWorkInterval().getId() + ") Percentage(" + percentage.getPercentage() + ") of " + percentages.size());
 			percentageDao.makePersistent(percentage);
 		}
 		System.out.println("Persisted Percentages");
 	}
 
 	@Override
 	public SortedSet<EmployeeDto> getClockedInEmployees() {
 		EmployeeWorkIntervalDao dao = new EmployeeWorkIntervalHibernateDao();
 		SortedSet<EmployeeDto> employees = new TreeSet<EmployeeDto>();
 		for (EmployeeWorkInterval interval : dao.findOpenEmployeeWorkIntervals()) {
 			employees.add(completeToEmployeeDto(interval.getEmployee()));
 		}
 		return employees;
 	}
 
 	@Override
 	public void cancelClockIn(Integer barcode) {
 		EmployeeDao employeeDao = new EmployeeHibernateDao();
 		Employee employee = employeeDao.findEmployeeWithBarcodeNumber(barcode);
 		
 		EmployeeWorkIntervalDao intervalDao = new EmployeeWorkIntervalHibernateDao();
 		EmployeeWorkInterval interval = intervalDao.findOpenEmployeeWorkInterval(employee);
 		intervalDao.makeTransient(interval);
 		
 	}
 
 	@Override
 	public SortedSet<ActivityDto> getActivities() {
 		ActivityDao dao = new ActivityHibernateDao();
 		return new TreeSet<ActivityDto>(DtoUtils.transform(dao.findAll(), DtoUtils.toActivityDtoFunction));
 	}
 
 	private static EmployeeDto completeToEmployeeDto(Employee employee) {
 		EmployeeWorkIntervalDao dao = new EmployeeWorkIntervalHibernateDao();
 		
 		//Do an incomplete conversion form Employee to EmployeeDto
 		EmployeeDto employeeDto = DtoUtils.toEmployeeDtoFunction.apply(employee);
 		 
 		calculateMinutesWorkedThisWeek(employeeDto);
 		
 		//Set the percentages to be what they were for the most recently closed interval
 		
 		Set<EmployeeWorkIntervalActivityPercentage> lastEnteredPercentages = dao.findLastEnteredPercentages(employee);
 		List<EmployeeWorkIntervalActivityPercentageDto> percentageDtos;
 		if (lastEnteredPercentages == null) {
 			percentageDtos = new ArrayList<EmployeeWorkIntervalActivityPercentageDto>();
 		} else {
 			percentageDtos = DtoUtils.transform(lastEnteredPercentages, DtoUtils.toEmployeeWorkIntervalActivityPercentageDtoFunction);
 		}
 		employeeDto.setEmployeeWorkIntervalPercentages(percentageDtos);
 		return employeeDto;
 	}
 
 	private static void calculateMinutesWorkedThisWeek(EmployeeDto employeeDto) {
 		EmployeeWorkIntervalDao dao = new EmployeeWorkIntervalHibernateDao();
 		
 		//Find last Sunday at the start of the day
 		DateTime lastSunday = new DateMidnight().minusDays(new DateMidnight().getDayOfWeek()).toDateTime();
 		
 		employeeDto.setMinsWorkedThisWeek(0);
 		List<EmployeeWorkInterval> employeeWorkIntervals = dao.findEmployeeWorkIntervalsBetweenDates(fromEmployeeDtoFunction.apply(employeeDto), lastSunday, new DateTime());
 		for (EmployeeWorkInterval interval : employeeWorkIntervals) {
 			
 			//If the interval isn't closed then use the current time to determine the number of minutes worked. Otherwise use the difference between start and end time.
 			if (interval.getEndDateTime() == null) {
 				Period period = new Period(new DateTime(interval.getStartDateTime()), new DateTime(), PeriodType.minutes());
 				System.out.println("Period length in min: " + period.getMinutes());
 				System.out.println("Minutes worked before update: " + employeeDto.getMinsWorkedThisWeek());
 				employeeDto.setMinsWorkedThisWeek(employeeDto.getMinsWorkedThisWeek() + period.getMinutes());
 				System.out.println("Minutes worked after update: " + employeeDto.getMinsWorkedThisWeek());
 			} else {
 				Period period = new Period(new DateTime(interval.getStartDateTime()), new DateTime(interval.getEndDateTime()), PeriodType.minutes());
 				System.out.println("Time now in mill: " + new DateMidnight().getMillis());
 				System.out.println("StartTime in mill: " + new DateTime(interval.getStartDateTime()).getMillis());
 				System.out.println("EndTime in mill: " + new DateTime(interval.getEndDateTime()).getMillis());
 				System.out.println("Period length in sec: " + period.getSeconds());
 				System.out.println("Minutes worked before update: " + employeeDto.getMinsWorkedThisWeek());
 				employeeDto.setMinsWorkedThisWeek(employeeDto.getMinsWorkedThisWeek() + period.getMinutes());
 				System.out.println("Minutes worked after update: " + employeeDto.getMinsWorkedThisWeek());
 			}
 		}
 	}
 
 	@Override
 	public IntervalDto getLastPayPeriodIntervalFromServer(Date date) {
 		DateTime startOfPayPeriod;
 		DateTime endOfPayPeriod;
 		DateTime dateTime = new DateTime(date);
 		if(dateTime.getDayOfMonth() > 15) {
 			startOfPayPeriod = dateTime.withDayOfMonth(1);
 			endOfPayPeriod = dateTime.withDayOfMonth(15);
 		} else {
 			startOfPayPeriod = dateTime.minusMonths(1);
 			startOfPayPeriod = startOfPayPeriod.withDayOfMonth(16);
 			endOfPayPeriod = dateTime.minusMonths(1);
 			endOfPayPeriod = endOfPayPeriod.dayOfMonth().withMaximumValue();
 		}
 		return new IntervalDto(startOfPayPeriod.toDate(), endOfPayPeriod.toDate());
 	}
 
 	@Override
 	public List<PayPeriodReportData> getPayPeriodReportDataFromDatabase(
 			Date start, Date end, Integer shift) {
 		EmployeeDao employeeDao = new EmployeeHibernateDao();
 		EmployeeWorkIntervalDao intervalDao = new EmployeeWorkIntervalHibernateDao();
 		
 		List<PayPeriodReportData> reportData = newArrayList();
 
 		DateMidnight payPeriodStart = new DateMidnight(start);
 		System.out.println("Pay Period Start: " + payPeriodStart.toString("MMMM hh:mm a"));
 		DateMidnight payPeriodEnd = new DateMidnight(end);
 		System.out.println("Pay Period End: " + payPeriodStart.toString("MMMM hh:mm a"));
 		payPeriodEnd = payPeriodEnd.plusDays(1);
 		DateMidnight sundayBeforePayPeriodStart;
 		
 		if (payPeriodStart.getDayOfWeek() == 7) {
 			sundayBeforePayPeriodStart = payPeriodStart;
 		} else {
 			sundayBeforePayPeriodStart = payPeriodStart.minusDays(payPeriodStart.getDayOfWeek());
 		}
 		System.out.println("Sunday Before Period Start: " + payPeriodStart.toString("MMMM hh:mm a"));
 		
 		List<Employee> employees;
 		if (shift == 0) {
 			employees = employeeDao.findAll();
 		} else {
 			employees = employeeDao.findEmployeesByShift(shift);
 		}
 		for (Employee employee : employees) {
 			if (intervalDao.findEmployeeWorkIntervalsBetweenDates(employee, payPeriodStart.toDateTime(), payPeriodEnd.toDateTime()).size() != 0) {
 				PayPeriodReportData payPeriod = new PayPeriodReportData();
 				payPeriod.setName(employee.getFirstName() + " " + employee.getLastName());
 				payPeriod.setId(employee.getId());
 				payPeriod.setShift(employee.getShift());
 				payPeriod.setDate(new Date());
 				payPeriod.setPayPeriodStart(payPeriodStart.toDate());
 				payPeriod.setPayPeriodEnd(payPeriodEnd.minusDays(1).toDate());
 				
 				WeekReportData week = new WeekReportData();
 				payPeriod.addInterval(week);
 				DayReportData day = new DayReportData();
 				week.addInterval(day);
 				
 				int d = 0;
 				
 				while (d < new Period(sundayBeforePayPeriodStart, payPeriodStart, PeriodType.days()).getDays()) {
 					for (EmployeeWorkInterval interval : findIntervalsHelper(employee, sundayBeforePayPeriodStart, d)) {
 						double intervalHours = getDifference(interval.getStartDateTime(), interval.getEndDateTime());
 						week.addPrePayPeriodHours(intervalHours);
 					}
 					d++;
 				}
 				
 				while (d < new Period(sundayBeforePayPeriodStart, payPeriodEnd, PeriodType.days()).getDays()) {
 					if (d == 7 || d == 14 || d == 21) {
 						week = new WeekReportData();
 						payPeriod.addInterval(week);
 					} 
 					for (EmployeeWorkInterval interval : findIntervalsHelper(employee, sundayBeforePayPeriodStart, d)) {
 						if (interval.getEndDateTime() != null) {
 							double intervalHours = getDifference(interval.getStartDateTime(), interval.getEndDateTime());
 							week.addHours(intervalHours);
 							day.addHours(intervalHours);
 							EmployeeWorkIntervalDto intervalDto = DtoUtils.toEmployeeWorkIntervalDtoFunction.apply(interval);
 							intervalDto.addHours(intervalHours);
 							day.addInterval(intervalDto);
 						}
 					}
 					day = new DayReportData();
 					week.addInterval(day);
 					d++;
 				}
 				for (WeekReportData weekData : payPeriod.getWeekReportData()) {
 					payPeriod.addNormalPayHours(weekData.getHoursNormalPay());
 					payPeriod.addOvertimeHours(weekData.getHoursOvertime());
 				}
 				reportData.add(payPeriod);
 			}
 		}
 		sort(reportData, new Comparator<PayPeriodReportData>() {
 
 			@Override
 			public int compare(PayPeriodReportData person1, PayPeriodReportData person2) {
				String lastName1 = person1.getName().substring(person1.getName().indexOf(" "));
				String lastName2 = person2.getName().substring(person2.getName().indexOf(" "));
 				return lastName1.compareTo(lastName2);
 			}
 			
 		});
 		return reportData;
 	}
 	
 	private List<EmployeeWorkInterval> findIntervalsHelper(Employee employee, DateMidnight sundayBeforePayPeriodStart, int day) {
 		return new EmployeeWorkIntervalHibernateDao().findEmployeeWorkIntervalsBetweenDates(employee, sundayBeforePayPeriodStart.plusDays(day).toDateTime(), sundayBeforePayPeriodStart.plusDays(day + 1).toDateTime());
 	}
 	
 	private double getDifference(Date start, Date end) {
 		Period period = new Period(new DateTime(start), new DateTime(end));
 		return new Double(period.getHours()) + new Double(period.getMinutes())/60;
 	}
 
 	@Override
 	public SortedSet<EmployeeDto> updateMinutesWorkedInCurrentWeek(
 			SortedSet<EmployeeDto> employees) {
 		for (EmployeeDto employeeDto : employees) {
 			calculateMinutesWorkedThisWeek(employeeDto);
 		}
 		return employees;
 	}
 }
