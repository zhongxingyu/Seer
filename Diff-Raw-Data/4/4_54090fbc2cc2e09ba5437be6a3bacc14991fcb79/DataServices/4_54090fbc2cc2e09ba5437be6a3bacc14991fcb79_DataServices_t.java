 package com.hcalendar.data;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.hcalendar.HCalendarConstants;
 import com.hcalendar.config.ConfigurationNotInitedException;
 import com.hcalendar.config.ConfigurationUtils;
 import com.hcalendar.data.calculator.Calculator;
 import com.hcalendar.data.calculator.exception.CalculatorException;
 import com.hcalendar.data.dto.WorkInputsDTO;
 import com.hcalendar.data.dto.WorkInputsDTO.Holiday;
 import com.hcalendar.data.dto.WorkInputsDTO.WorkInput;
 import com.hcalendar.data.exception.BusinessException;
 import com.hcalendar.data.orm.IORMClient;
 import com.hcalendar.data.orm.exception.ORMException;
 import com.hcalendar.data.orm.impl.ORMHelper;
 import com.hcalendar.data.utils.DateHelper;
 import com.hcalendar.data.utils.exception.DateException;
 import com.hcalendar.data.xml.userconfiguration.ObjectFactory;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf.FreeDays;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf.FreeDays.FreeDay;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf.WorkingDays;
 import com.hcalendar.data.xml.workedhours.AnualHours;
 import com.hcalendar.data.xml.workedhours.AnualHours.UserInput.Holidays;
 import com.hcalendar.data.xml.workedhours.AnualHours.UserInput.WorkedHours;
 import com.hcalendar.fop.PDFCreator;
 import com.hcalendar.ui.subViews.ExportDataWindow;
 
 public class DataServices {
 
 	@SuppressWarnings("deprecation")
 	private static Map<String, Float> getHoursMonthResume(String fromDateStr, String toDateStr, int year, String profileName, IORMClient orm) throws ORMException, DateException{
 		Map<String, Float> resultMap = new LinkedHashMap<String, Float>();
 		Date fromDate = null;
 		Date toDate = null;
 		Boolean haveToFilter = false;
 		if (!(fromDateStr == null || fromDateStr.equals(""))) {
 			fromDate = DateHelper.parse2Date(fromDateStr);
 			toDate = DateHelper.parse2Date(toDateStr);
 			haveToFilter = true;
 		} else
 			haveToFilter = false;
 		int daysOnMonth;
 		float lastCalculatedHours = 0;
 		for (int i = 0; i < DateHelper.months.length; i++) {
 			if (haveToFilter)
 				// Is month is filter?
 				if (!(fromDate.getMonth() <= i && toDate.getMonth() >= i))
 					continue;
 			if (DateHelper.isLeap(year) && i == Calendar.FEBRUARY)
 				daysOnMonth = DateHelper.daysOnMonth[i] + 1;
 			else
 				daysOnMonth = DateHelper.daysOnMonth[i];
 			float hours = Calculator.calculateHoursUntilDate(orm
 					.getAnualHours(),
 					new Date(year - 1900, i, daysOnMonth), profileName);
 			lastCalculatedHours = i == 0 ? 0 : Calculator
 					.calculateHoursUntilDate(orm.getAnualHours(), new Date(
 							year - 1900, i - 1, daysOnMonth), profileName);
 			resultMap.put(DateHelper.months[i], hours - lastCalculatedHours);
 		}
 		return resultMap;
 	}
 	
 	private static void dayliResumePDF(int year, String profileName,
 			String fromDateStr, String toDateStr, IORMClient orm)
 			throws BusinessException {
 		try {
 			AnualHours anualHours = orm.getAnualHours();
 			Date fromDate = null;
 			Date toDate = null;
 			if (!(fromDateStr == null || fromDateStr.equals(""))) {
 				fromDate = DateHelper.parse2Date(fromDateStr);
 				toDate = DateHelper.parse2Date(toDateStr);
 			}
 			// get worked hours list
 			List<WorkedHours> workedHours = ORMHelper.getUsersWorkedHourList(
 					anualHours, profileName, fromDate, toDate);
 			List<Holidays> holidays = ORMHelper.getUserHolidaysList(anualHours, profileName, fromDate, toDate);
 			List<FreeDay> freeDays = ORMHelper.getCalendarFreeDays(orm.getAnualConfiguration(), profileName, year, fromDate, toDate);
 			
 			// create DTO for xsl file
 			WorkInputsDTO dto = new WorkInputsDTO();
 			dto.setFromFilter(fromDate);
 			dto.setToFilter(toDate);	
 			dto.setProfileName(profileName);
 			dto.setYear(year);
 			for (WorkedHours work : workedHours) {
 				WorkInput subDto = new WorkInput(work);
 				dto.addWorkInput(subDto);
 			}
 			for (Holidays hol : holidays) {
 				Holiday subDto = new Holiday(hol);
 				dto.addHoliday(subDto);
 			}
 			for (FreeDay day : freeDays) {
 				com.hcalendar.data.dto.WorkInputsDTO.FreeDay subDto = new com.hcalendar.data.dto.WorkInputsDTO.FreeDay(day);
 				dto.addFreeDay(subDto);
 			}
 			dto.setMonthHoursResume(getHoursMonthResume(fromDateStr, toDateStr, year, profileName, orm));
 			// Create PDF
 			new PDFCreator().createPDF(dto);
 
 		} catch (Exception e) {
 			throw new BusinessException(e);
 		}
 	}
 
 	private static void monthResumeCSV(int year, String profileName,
 			String fromDateStr, String toDateStr, IORMClient orm)
 			throws BusinessException {
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(ConfigurationUtils.getCSVTempFile());
 			writer.append("Mes");
 			writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 			writer.append("Horas");
 			writer.append(HCalendarConstants.EXPORT_CSV_ROW_SEPARATOR);
 
 			Map<String, Float> monthResume = getHoursMonthResume(fromDateStr, toDateStr, year, profileName, orm);
 			// Write columns
 			for (String monthName :monthResume.keySet()){
 				writer.append(monthName);
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append(String.valueOf(monthResume.get(monthName)));
 				writer.append(HCalendarConstants.EXPORT_CSV_ROW_SEPARATOR);				
 			}
 		} catch (IOException e) {
 			throw new BusinessException(e);
 		} catch (ORMException e) {
 			throw new BusinessException(e);
 		} catch (DateException e) {
 			throw new BusinessException(e);
 		} catch (ConfigurationNotInitedException e) {
 			throw new BusinessException(e);
 		} finally {
 			try {
 				if (writer == null)
 					return;
 				writer.flush();
 				writer.close();
 			} catch (IOException e) {
 				throw new BusinessException(e);
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private static void dayliResumeCSV(int year, String profileName,
 			String fromDateStr, String toDateStr, IORMClient orm)
 			throws BusinessException {
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(ConfigurationUtils.getCSVTempFile());
 
 			Date fromDate = null;
 			Date toDate = null;
 			if (!(fromDateStr == null || fromDateStr.equals(""))) {
 				fromDate = DateHelper.parse2Date(fromDateStr);
 				toDate = DateHelper.parse2Date(toDateStr);
 			}
 			writer.append("Fecha");
 			writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 			writer.append("Dia");
 			writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 			writer.append("Horas");
 			writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 			writer.append("Comentarios");
 			writer.append(HCalendarConstants.EXPORT_CSV_ROW_SEPARATOR);
 
 			// Write columns
 			AnualHours anualHours = orm.getAnualHours();
 			List<WorkedHours> workedHours = ORMHelper.getUsersWorkedHourList(
 					anualHours, profileName, fromDate, toDate);
 			for (WorkedHours days : workedHours) {
 				Date date = DateHelper
 						.xmlGregorianCalendar2Date(days.getDate());
 				writer.append(DateHelper.DATE_FORMAT.format(date));
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append(DateHelper.translateDayOfWeek(date.getDay()));
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append(days.getHours() > 0.0 ? String.valueOf(days
 						.getHours()) : "");
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append(days.getDescription());
 				writer.append(HCalendarConstants.EXPORT_CSV_ROW_SEPARATOR);
 			}
 			// Month resume
 			Map<String, Float> monthResume = getHoursMonthResume(fromDateStr, toDateStr, year, profileName, orm);
 			float totHours = 0;
 			for (String monthName :monthResume.keySet()){
 				totHours = totHours + monthResume.get(monthName);
 				writer.append("Resumen del mes");
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append(monthName);
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append(String
 						.valueOf(monthResume.get(monthName)) + " horas");
 				writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 				writer.append("");
 				writer.append(HCalendarConstants.EXPORT_CSV_ROW_SEPARATOR);		
 			}
 			// Year resume
 			writer.append("Resumen del ao");
 			writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 			writer.append(String.valueOf(year));
 			writer.append(HCalendarConstants.EXPORT_CSV_COLUMN_SEPARATOR);
 			writer.append(totHours + " horas");
 			writer.append(HCalendarConstants.EXPORT_CSV_ROW_SEPARATOR);
 		} catch (IOException e) {
 			throw new BusinessException(e);
 		} catch (ORMException e) {
 			throw new BusinessException(e);
 		} catch (DateException e) {
 			throw new BusinessException(e);
 		} catch (ConfigurationNotInitedException e) {
 			throw new BusinessException(e);
 		} finally {
 			try {
 				if (writer == null)
 					return;
 				writer.flush();
 				writer.close();
 			} catch (IOException e) {
 				throw new BusinessException(e);
 			}
 		}
 	}
 
 	/**
 	 * Calculates the planned hours of a given user and year
 	 * 
 	 * @param orm
 	 *            orm instance
 	 * @param name
 	 *            Username which get the hour input
 	 * @param year
 	 *            year
 	 * @param calendarHours
 	 *            hours of the (spanish:convenio)
 	 * @param listaDiasLaborales
 	 *            Labour days of week
 	 * @param dLibresList
 	 *            Calandar free days of a given year
 	 * @param ovewriteProfile
 	 *            ovewrite file?
 	 * 
 	 * @return AnualHours java bean
 	 * @throws CalculatorException
 	 * */
 	public static UserConfiguration createAnualConfigurationProfile(
 			IORMClient orm, String name, int year, String calendarHours,
 			Map<Integer, String> listaDiasLaborales, List<String> dLibres,
 			boolean ovewriteProfile) throws BusinessException {
 		try {
 			ObjectFactory of = new ObjectFactory();
 			final UserConfiguration anualConfig = orm.getAnualConfiguration();
 			if (ovewriteProfile) {
 				for (User userTemp : anualConfig.getUser())
 					if (userTemp.getName().equals(name)) {
 						anualConfig.getUser().remove(userTemp);
 						break;
 					}
 			}
 			final User user = of.createUserConfigurationUser();
 			user.setName(name);
 			List<YearConf> yearList = user.getYearConf();
 			YearConf yearConf = of.createUserConfigurationUserYearConf();
 			yearConf.setCalendarHours(Float.valueOf(calendarHours));
 			List<WorkingDays> calWorkinDays = yearConf.getWorkingDays();
 			WorkingDays wd;
 			for (Integer day : listaDiasLaborales.keySet()) {
 				wd = of.createUserConfigurationUserYearConfWorkingDays();
 				wd.setWorkingDay(String.valueOf(day));
 				wd.setHours(Float.valueOf(listaDiasLaborales.get(day)));
 				calWorkinDays.add(wd);
 			}
 
 			FreeDays fDays = of.createUserConfigurationUserYearConfFreeDays();
 			List<FreeDay> freeDays = fDays.getFreeDay();
 			for (String day : dLibres) {
 				FreeDay freeDay = of
 						.createUserConfigurationUserYearConfFreeDaysFreeDay();
 				freeDay.setDay(DateHelper.parse2XMLGregorianCalendar(day));
 				freeDay.setComment("Dias libres de configuracion de usuario");
 				freeDays.add(freeDay);
 			}
 			yearConf.setFreeDays(fDays);
 			yearConf.setYear(year);
 			yearList.add(yearConf);
 
 			anualConfig.getUser().add(user);
 			return anualConfig;
 		} catch (DateException e) {
 			throw new BusinessException(e);
 		} catch (ORMException e) {
 			throw new BusinessException(e);
 		}
 	}
 
 	/**
 	 * Export data to CSV or PDF with two options: - By day: resume by day - By month:
 	 * resume by month
 	 * 
 	 * @param orm
 	 *            orm instance
 	 * @param year
 	 *            year
 	 * @param profileName
 	 *            Username which get the hour input
	 * @param callback
	 *            callback interface
 	 * 
 	 * */
 	public static void exportInfo(final IORMClient orm, final int year,
 			final String profileName, final Observer callback) {
 		new ExportDataWindow(new Observer() {
 
 			@Override
 			public void update(Observable o, Object arg) {
 				List<Object> list = ((List<Object>) arg);
 				Integer selectedOption = (Integer) list.get(0);
 				String fromDate = (String) list.get(1);
 				String toDate = (String) list.get(2);
 
 				try {
 					File fileToOpen = null;
 					if (selectedOption
 							.equals(ExportDataWindow.EXPORT_CSV_OPTION_MONTH)) {
 						monthResumeCSV(year, profileName, fromDate, toDate, orm);
 						fileToOpen = ConfigurationUtils.getCSVTempFile();
 					} else if (selectedOption
 							.equals(ExportDataWindow.EXPORT_CSV_OPTION_DAY)) {
 						dayliResumeCSV(year, profileName, fromDate, toDate, orm);
 						fileToOpen = ConfigurationUtils.getCSVTempFile();
 					} else if (selectedOption
 							.equals(ExportDataWindow.EXPORT_PDF_OPTION_DAY)){
 						dayliResumePDF(year, profileName, fromDate, toDate, orm);
 						fileToOpen = ConfigurationUtils.getPDFTempFile();
 					}else if (selectedOption
 							.equals(ExportDataWindow.EXPORT_PDF_OPTION_MONTH)){
 						dayliResumePDF(year, profileName, fromDate, toDate, orm);
 						fileToOpen = ConfigurationUtils.getPDFTempFile();
 					}else
 						callback.update(null, -1);
 
 					// Open with excel
 					if (!Desktop.isDesktopSupported()) {
 						Process p = Runtime.getRuntime().exec(
 								"rundll32 url.dll,FileProtocolHandler "
 										+ fileToOpen.getAbsolutePath());
 						// use alternative (Runtime.exec)
 						callback.update(null, -1);
 					}
 
 					Desktop desktop = Desktop.getDesktop();
 					if (!desktop.isSupported(Desktop.Action.OPEN)) {
 						System.err.println("OPEN not supported");
 						// use alternative (Runtime.exec)
 						callback.update(null, -1);
 					}
 
 					desktop.open(fileToOpen);
 					callback.update(null, 0);
 				} catch (IOException e) {
 					e.printStackTrace();
 					callback.update(null, -1);
 				} catch (ConfigurationNotInitedException e) {
 					e.printStackTrace();
 					callback.update(null, -1);
 				} catch (BusinessException e) {
 					e.printStackTrace();
 					callback.update(null, -1);
 				}
 			}
 		});
 	}
 }
