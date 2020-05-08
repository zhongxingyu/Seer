 package com.mavenlab.jetset.controller;
 
 
 import java.io.ByteArrayOutputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
import java.util.Map;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 
 import jxl.Workbook;
 import jxl.WorkbookSettings;
 import jxl.write.DateFormats;
 import jxl.write.DateTime;
 import jxl.write.Label;
 import jxl.write.Number;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 
 import org.jboss.logging.Logger;
 import org.jboss.seam.solder.logging.Category;
 
 import com.mavenlab.jetset.model.Entry;
 import com.mavenlab.jetset.model.SMSEntry;
 import com.mavenlab.jetset.model.WebEntry;
import com.mavenlab.jetset.rest.MOReceiver;
 import com.mavenlab.jetset.util.Pagination;
 
 @Stateless
 public class EntryController {
 	
 	public static final int WEEK1 = 20110911;
 	public static final int WEEK2 = 20110918;
 	public static final int WEEK3 = 20110925;
 	public static final int WEEK4 = 20111002;
 	public static final int WEEK5 = 20111009;
 	public static final int WEEK6 = 20111016;
 	public static final int WEEK7 = 20111023;
 	public static final int WEEK8 = 20111030;
 	public static final int WEEK9 = 20111106;
 	public static final int WEEK10 = 20111113;
 	public static final int WEEK11 = 20111120;
 	public static final int WEEK12 = 20111130;
 	
 	private final static String DATE_PATTERN = "dd MMMM yyyy HH:mm:ss";
 
 	@Inject
 	private EntityManager em;
 	
 	@Inject
 	@Category("jetset.EntryController")
 	private Logger log;
 	
 	public Entry getEntryFetchAll(int id) {
 		try {
 			return (Entry) em.createNamedQuery("jetset.query.Entry.findIdFetchAll").setParameter("id", id).getSingleResult();
 		} catch(NoResultException e) {
 		}
 
 		return null;
 	}
 	
 	public boolean isDuplicate(Entry entry, boolean web) {
 		
 		String dt = new SimpleDateFormat("yyyyMMdd").format(new Date());
 		int dtNow = Integer.parseInt(dt);
 		
 		Calendar calStart = Calendar.getInstance();
 		calStart.setTime(new Date());
 
 		Calendar calEnd = Calendar.getInstance();
 		calEnd.setTime(new Date());
 		
 		if(dtNow <= WEEK1) {
 			calStart.set(2011, Calendar.SEPTEMBER, 1, 0, 0, 0);
 			calEnd.set(2011, Calendar.SEPTEMBER, 11, 23, 59, 59);
 		} else if(dtNow <= WEEK2) {
 			calStart.set(2011, Calendar.SEPTEMBER, 12, 0, 0, 0);
 			calEnd.set(2011, Calendar.SEPTEMBER, 18, 23, 59, 59);
 		} else if(dtNow <= WEEK3) {
 			calStart.set(2011, Calendar.SEPTEMBER, 19, 0, 0, 0);
 			calEnd.set(2011, Calendar.SEPTEMBER, 25, 23, 59, 59);
 		} else if(dtNow <= WEEK4) {
 			calStart.set(2011, Calendar.SEPTEMBER, 26, 0, 0, 0);
 			calEnd.set(2011, Calendar.OCTOBER, 02, 23, 59, 59);
 		} else if(dtNow <= WEEK5) {
 			calStart.set(2011, Calendar.OCTOBER, 3, 0, 0, 0);
 			calEnd.set(2011, Calendar.OCTOBER, 9, 23, 59, 59);
 		} else if(dtNow <= WEEK6) {
 			calStart.set(2011, Calendar.OCTOBER, 10, 0, 0, 0);
 			calEnd.set(2011, Calendar.OCTOBER, 16, 23, 59, 59);
 		} else if(dtNow <= WEEK7) {
 			calStart.set(2011, Calendar.OCTOBER, 17, 0, 0, 0);
 			calEnd.set(2011, Calendar.OCTOBER, 23, 23, 59, 59);
 		} else if(dtNow <= WEEK8) {
 			calStart.set(2011, Calendar.OCTOBER, 24, 0, 0, 0);
 			calEnd.set(2011, Calendar.OCTOBER, 30, 23, 59, 59);
 		} else if(dtNow <= WEEK9) {
 			calStart.set(2011, Calendar.OCTOBER, 31, 0, 0, 0);
 			calEnd.set(2011, Calendar.NOVEMBER, 6, 23, 59, 59);
 		} else if(dtNow <= WEEK10) {
 			calStart.set(2011, Calendar.NOVEMBER, 7, 0, 0, 0);
 			calEnd.set(2011, Calendar.NOVEMBER, 13, 23, 59, 59);
 		} else if(dtNow <= WEEK11) {
 			calStart.set(2011, Calendar.NOVEMBER, 14, 0, 0, 0);
 			calEnd.set(2011, Calendar.NOVEMBER, 20, 23, 59, 59);
 		} else if(dtNow <= WEEK12) {
 			calStart.set(2011, Calendar.NOVEMBER, 21, 0, 0, 0);
 			calEnd.set(2011, Calendar.NOVEMBER, 30, 23, 59, 59);
 		}
 		
 		long count = (Long) em.createNamedQuery("jetset.query.Entry.duplicateCheckRange").
 								setParameter("receipt", entry.getReceipt()).
 								setParameter("stationId", entry.getStation().getId()).
 								setParameter("start", calStart.getTime()).
 								setParameter("end", calEnd.getTime()).
 								getSingleResult();
 
 		return (!web && count > 1) || (web && count > 0);
 	}
 
 	/**
 	 * count all
 	 * 
 	 * @return
 	 */
 	public long countAll() {
 		return (Long) em.createNamedQuery("jetset.query.Entry.countAll").getSingleResult();
 	}
 	
 	/**
 	 * count all by type
 	 * 
 	 * @param type
 	 * @return {@link Long}
 	 */
 	public long countAllByType(String type) {
 		String query = "jetset.query.Entry.countAll";
 		if(type.equals("SMS")) {
 			query = "jetset.query.SMSEntry.countAll";
 		} else if(type.equals("Web")) {
 			query = "jetset.query.WebEntry.countAll";
 		}
 		return (Long) em.createNamedQuery(query).getSingleResult();
 	}
 	
 	/**
 	 * fetch all with offset and limit
 	 * 
 	 * @param offset
 	 * @param limit
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchAll(int offset, int limit) {
 		return em.createNamedQuery("jetset.query.Entry.findFetchAll").
 				setFirstResult(offset).
 				setMaxResults(limit).
 				getResultList();
 	}
 	
 	/**
 	 * fetch all with offset and limit
 	 * 
 	 * @param type
 	 * @param offset
 	 * @param limit
 	 * @return {@link List}
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchAllByType(String type, int offset, int limit) {
 		String query = "jetset.query.Entry.findFetchAll";
 		if(type.equals("SMS")) {
 			query = "jetset.query.SMSEntry.findFetchAll";
 		} else if(type.equals("Web")) {
 			query = "jetset.query.WebEntry.findFetchAll";
 		}
 		return em.createNamedQuery(query).
 				setFirstResult(offset).
 				setMaxResults(limit).
 				getResultList();
 	}
 
 	/**
 	 * count by msisdn
 	 * 
 	 * @param msisdn
 	 * @return
 	 */
 	public long countByMsisdn(String msisdn) {
 		return (Long) em.createNamedQuery("jetset.query.Entry.countByMsisdn").setParameter("msisdn", msisdn).getSingleResult();
 	}
 	
 	/**
 	 * count by type msisdn
 	 * 
 	 * @param msisdn
 	 * @return {@link Long}
 	 */
 	public long countByTypeMsisdn(String type, String msisdn) {
 		String query = "jetset.query.Entry.countByMsisdn";
 		if(type.equals("SMS")) {
 			query = "jetset.query.SMSEntry.countByMsisdn";
 		} else if(type.equals("Web")) {
 			query = "jetset.query.WebEntry.countByMsisdn";
 		}
 		return (Long) em.createNamedQuery(query).setParameter("msisdn", msisdn).getSingleResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchByMsisdn(String msisdn, int offset, int limit) {
 		return em.createNamedQuery("jetset.query.Entry.findFetchByMsisdn").
 				setParameter("msisdn", msisdn).
 				setFirstResult(offset).
 				setMaxResults(limit).
 				getResultList();
 	}
 	
 	/**
 	 * Fetch by type msisdn
 	 * @param type
 	 * @param msisdn
 	 * @param offset
 	 * @param limit
 	 * @return {@link List}
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchByTypeMsisdn(String type, String msisdn, int offset, int limit) {
 		String query = "jetset.query.Entry.findFetchByMsisdn";
 		if(type.equals("SMS")) {
 			query = "jetset.query.SMSEntry.findFetchByMsisdn";
 		} else if(type.equals("Web")) {
 			query = "jetset.query.WebEntry.findFetchByMsisdn";
 		}
 		
 		return em.createNamedQuery(query).
 				setParameter("msisdn", msisdn).
 				setFirstResult(offset).
 				setMaxResults(limit).
 				getResultList();
 	}
 	
 	/**
 	 * count by date
 	 * 
 	 * @param startDate
 	 * @param endDate
 	 * @return
 	 */
 	public long countByDate(Date startDate, Date endDate) {
 		if(startDate != null && endDate != null) {
 			return (Long) em.createNamedQuery("jetset.query.Entry.countByDateRange").
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		} else if(startDate != null) {
 			return (Long) em.createNamedQuery("jetset.query.Entry.countByStartDate").
 					setParameter("startDate", startDate).
 					getSingleResult();
 		} else if(endDate != null) {
 			return (Long) em.createNamedQuery("jetset.query.Entry.countByEndDate").
 					setParameter("endDate", endDate).
 					getSingleResult();
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * count by type date
 	 * 
 	 * @param type
 	 * @param startDate
 	 * @param endDate
 	 * @return {@link Long}
 	 */
 	public long countByTypeDate(String type, Date startDate, Date endDate) {
 		if(startDate != null && endDate != null) {
 			String query = "jetset.query.Entry.countByDateRange";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.countByDateRange";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.countByDateRange";
 			}
 			return (Long) em.createNamedQuery(query).
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		} else if(startDate != null) {
 			String query = "jetset.query.Entry.countByStartDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.countByStartDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.countByStartDate";
 			}
 			return (Long) em.createNamedQuery(query).
 					setParameter("startDate", startDate).
 					getSingleResult();
 		} else if(endDate != null) {
 			String query = "jetset.query.Entry.countByEndDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.countByEndDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.countByEndDate";
 			}
 			return (Long) em.createNamedQuery(query).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		}
 		
 		return 0;
 	}
 
 	/**
 	 * fetch by date
 	 * 
 	 * @param startDate
 	 * @param endDate
 	 * @param offset
 	 * @param limit
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchByDate(Date startDate, Date endDate, int offset, int limit) {
 		if(startDate != null && endDate != null) {
 			return em.createNamedQuery("jetset.query.Entry.findFetchByDateRange").
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(startDate != null) {
 			return em.createNamedQuery("jetset.query.Entry.findFetchByStartDate").
 					setParameter("startDate", startDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(endDate != null) {
 			return em.createNamedQuery("jetset.query.Entry.findFetchByEndDate").
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * fetch by date
 	 * 
 	 * @param type
 	 * @param startDate
 	 * @param endDate
 	 * @param offset
 	 * @param limit
 	 * @return {@link List}
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchByTypeDate(String type, Date startDate, Date endDate, int offset, int limit) {
 		if(startDate != null && endDate != null) {
 			String query = "jetset.query.Entry.findFetchByDateRange";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.findFetchByDateRange";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.findFetchByDateRange";
 			}
 			return em.createNamedQuery(query).
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(startDate != null) {
 			String query = "jetset.query.Entry.findFetchByStartDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.findFetchByStartDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.findFetchByStartDate";
 			}
 			
 			return em.createNamedQuery(query).
 					setParameter("startDate", startDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(endDate != null) {
 			String query = "jetset.query.Entry.findFetchByEndDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.findFetchByEndDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.findFetchByEndDate";
 			}
 			
 			return em.createNamedQuery(query).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * count by msisdn date
 	 * 
 	 * @param msisdn
 	 * @param startDate
 	 * @param endDate
 	 * @return
 	 */
 	public long countByMsisdnDate(String msisdn, Date startDate, Date endDate) {
 		if(startDate != null && endDate != null) {
 			return (Long) em.createNamedQuery("jetset.query.Entry.countByMsisdnDateRange").
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		} else if(startDate != null) {
 			return (Long) em.createNamedQuery("jetset.query.Entry.countByMsisdnStartDate").
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					getSingleResult();
 		} else if(endDate != null) {
 			return (Long) em.createNamedQuery("jetset.query.Entry.countByMsisdnEndDate").
 					setParameter("msisdn", msisdn).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * count by type msisdn date
 	 * 
 	 * @param type
 	 * @param msisdn
 	 * @param startDate
 	 * @param endDate
 	 * @return {@link Long}
 	 */
 	public long countByTypeMsisdnDate(String type, String msisdn, Date startDate, Date endDate) {
 		if(startDate != null && endDate != null) {
 			String query = "jetset.query.Entry.countByMsisdnDateRange";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.countByMsisdnDateRange";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.countByMsisdnDateRange";
 			}
 			
 			return (Long) em.createNamedQuery(query).
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		} else if(startDate != null) {
 			String query = "jetset.query.Entry.countByMsisdnStartDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.countByMsisdnStartDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.countByMsisdnStartDate";
 			}
 			
 			return (Long) em.createNamedQuery(query).
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					getSingleResult();
 		} else if(endDate != null) {
 			String query = "jetset.query.Entry.countByMsisdnEndDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.countByMsisdnEndDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.countByMsisdnEndDate";
 			}
 			
 			return (Long) em.createNamedQuery(query).
 					setParameter("msisdn", msisdn).
 					setParameter("endDate", endDate).
 					getSingleResult();
 		}
 		
 		return 0;
 	}
 	
 	/**
 	 * fetch by msisdn and date
 	 * @param msisdn
 	 * @param startDate
 	 * @param endDate
 	 * @param offset
 	 * @param limit
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchByMsisdnDate(String msisdn, Date startDate, Date endDate, int offset, int limit) {
 		if(startDate != null && endDate != null) {
 			return em.createNamedQuery("jetset.query.Entry.findFetchByMsisdnDateRange").
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(startDate != null) {
 			return em.createNamedQuery("jetset.query.Entry.findFetchByMsisdnStartDate").
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(endDate != null) {
 			return em.createNamedQuery("jetset.query.Entry.findFetchByMsisdnEndDate").
 					setParameter("msisdn", msisdn).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * fetch by msisdn and date
 	 * @param msisdn
 	 * @param startDate
 	 * @param endDate
 	 * @param offset
 	 * @param limit
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Entry> fetchByTypeMsisdnDate(String type, String msisdn, Date startDate, Date endDate, int offset, int limit) {
 		if(startDate != null && endDate != null) {
 			String query = "jetset.query.Entry.findFetchByMsisdnDateRange";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.findFetchByMsisdnDateRange";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.findFetchByMsisdnDateRange";
 			}
 			
 			return em.createNamedQuery(query).
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(startDate != null) {
 			String query = "jetset.query.Entry.findFetchByMsisdnStartDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.findFetchByMsisdnStartDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.findFetchByMsisdnStartDate";
 			}
 			
 			return em.createNamedQuery(query).
 					setParameter("msisdn", msisdn).
 					setParameter("startDate", startDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		} else if(endDate != null) {
 			String query = "jetset.query.Entry.findFetchByMsisdnEndDate";
 			if(type.equals("SMS")) {
 				query = "jetset.query.SMSEntry.findFetchByMsisdnEndDate";
 			} else if(type.equals("Web")) {
 				query = "jetset.query.WebEntry.findFetchByMsisdnEndDate";
 			}
 			
 			return em.createNamedQuery(query).
 					setParameter("msisdn", msisdn).
 					setParameter("endDate", endDate).
 					setFirstResult(offset).
 					setMaxResults(limit).
 					getResultList();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Download entries
 	 * @param type
 	 * @param msisdn
 	 * @param start
 	 * @param end
 	 * @return {@link Byte}
 	 */
 	public byte[] downloadEntries(String type, String msisdn, String start, String end) throws Exception {
 		try {
 			Date startDate = null;
 			Date endDate = null;
 			
 			SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
 			
 			if(msisdn != null) {
 				msisdn = msisdn.trim();
 				if(msisdn.length() > 0 && msisdn.length() < 3) {
 					throw new Exception("Please enter at least 3 numbers");
 				} else if(msisdn.length() == 0) {
 					msisdn = null;
 				}
 			}
 			
 			if(start != null && start.trim().length() > 0) {
 				try {
 					start += " 00:00:00";
 					startDate = sdf.parse(start);
 				} catch (ParseException e) {
 					throw new Exception("Invalid Start Date Format");
 				}
 			}
 
 			if(end != null && end.trim().length() > 0) {
 				try {
 					end += " 23:59:59";
 					endDate = sdf.parse(end);
 				} catch (ParseException e) {
 					throw new Exception("Invalid End Date Format");
 				}
 			}
 
 			Pagination pagination = new Pagination();
 			
 			List<Entry> entries = null;
 			List<SMSEntry> smsEntries = new ArrayList<SMSEntry>();
 			List<WebEntry> webEntries = new ArrayList<WebEntry>();
 			
 			if(msisdn != null && (startDate != null || endDate != null)) {
 				log.info("QUERY FOR ENTRIES BY MSISDN DATE: " + startDate + " - " + endDate + " - " + msisdn);
 				pagination.setTotal(countByTypeMsisdnDate(type, msisdn, startDate, endDate));
 				pagination.setLimit(Long.valueOf(pagination.getTotal()).intValue());
 				entries = fetchByTypeMsisdnDate(type, msisdn, startDate, endDate, pagination.getOffset(), pagination.getLimit());
 			} else if(msisdn == null && (startDate != null || endDate != null)) {
 				log.info("QUERY FOR ENTRIES BY DATE: " + startDate + " - " + endDate);
 				pagination.setTotal(countByTypeDate(type, startDate, endDate));
 				pagination.setLimit(Long.valueOf(pagination.getTotal()).intValue());
 				entries = fetchByTypeDate(type, startDate, endDate, pagination.getOffset(), pagination.getLimit());
 			} else if(msisdn != null && startDate == null && endDate == null) {
 				log.info("QUERY FOR ENTRIES BY MSISDN: " + msisdn);
 				
 				pagination.setTotal(countByTypeMsisdn(type, msisdn));
 				pagination.setLimit(Long.valueOf(pagination.getTotal()).intValue());
 				entries = fetchByTypeMsisdn(type, msisdn, pagination.getOffset(), pagination.getLimit());
 			} else {
 				log.info("QUERY FOR ALL ENTRIES");
 
 				pagination.setTotal(countAllByType(type));
 				pagination.setLimit(Long.valueOf(pagination.getTotal()).intValue());
 				entries = fetchAllByType(type, pagination.getOffset(), pagination.getLimit());
 			}
 			
 			if(entries != null && entries.size() > 0) { 
 				
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				WorkbookSettings ws = new WorkbookSettings();
 				ws.setLocale(new Locale("en", "EN"));
 
 				WritableWorkbook workbook = Workbook.createWorkbook(baos, ws);
 				WritableSheet sheet = workbook.createSheet("SMS & WEB", 0);
 				WritableCellFormat cf = new WritableCellFormat();
 
 				sheet.addCell(new Label(0, 0, "ID", cf));
 				sheet.addCell(new Label(1, 0, "Channel", cf));
 				sheet.addCell(new Label(2, 0, "Mobile", cf));
 				sheet.addCell(new Label(3, 0, "NRIC/Passport", cf));
 				sheet.addCell(new Label(4, 0, "Station", cf));
 				sheet.addCell(new Label(5, 0, "Receipt", cf));
 				sheet.addCell(new Label(6, 0, "Prize", cf));
 				sheet.addCell(new Label(7, 0, "Date", cf));
 				sheet.addCell(new Label(8, 0, "Status", cf));
 				
 				WritableSheet sheetPrize = workbook.createSheet("PRIZE", 3);
 				sheetPrize.addCell(new Label(0, 0, "ID", cf));
 				sheetPrize.addCell(new Label(1, 0, "Channel", cf));
 				sheetPrize.addCell(new Label(2, 0, "Mobile", cf));
 				sheetPrize.addCell(new Label(3, 0, "NRIC/Passport", cf));
 				sheetPrize.addCell(new Label(4, 0, "Station", cf));
 				sheetPrize.addCell(new Label(5, 0, "Receipt", cf));
 				sheetPrize.addCell(new Label(6, 0, "Prize", cf));
 				sheetPrize.addCell(new Label(7, 0, "Date", cf));
 				sheetPrize.addCell(new Label(8, 0, "Chance", cf));
 				
				Map<String,String> map = new HashMap<String, String>();
 				int row = 1;
 				int rowPrize = 1;
 				for(Entry entry : entries) {
 					sheet.addCell(new Number(0, row, entry.getId(), cf));
 					sheet.addCell(new Label(1, row, (entry instanceof SMSEntry ? "SMS" : "WEB"), cf));
 					sheet.addCell(new Label(2, row, entry.getMsisdn(), cf));
 					sheet.addCell(new Label(3, row, entry.getNric(), cf));
 					sheet.addCell(new Label(4, row, (entry.getStation() != null ? entry.getStation().getName() + " (" + entry.getStation().getId()+ ")" : ""),cf));
 					sheet.addCell(new Label(5, row, entry.getReceipt(), cf));
 					sheet.addCell(new Label(6, row, (entry.getPrize() != null ? entry.getPrize().getName() : ""), cf));
 					sheet.addCell(new Label(7, row, new SimpleDateFormat(DATE_PATTERN).format(entry.getCreatedAt()), cf));
 					sheet.addCell(new Label(8, row, entry.getStatus(), cf));
 					
 					row++;
 					if(entry instanceof SMSEntry) {
 						smsEntries.add((SMSEntry) entry);
 					} else if(entry instanceof WebEntry) {
 						webEntries.add((WebEntry) entry);
 					}
 					
					if(!entry.getStatus().equals("duplicate") && entry.getReceipt().toUpperCase().matches(MOReceiver.PATTERN_RECEIPT) && entry.getStation()!= null) {
						String key = entry.getStation().getId() + "|" + entry.getReceipt();
						if(!map.containsKey(key)) {
							sheetPrize.addCell(new Number(0, rowPrize, entry.getId(), cf));
							sheetPrize.addCell(new Label(1, rowPrize, (entry instanceof SMSEntry ? "SMS" : "WEB"), cf));
							sheetPrize.addCell(new Label(2, rowPrize, entry.getMsisdn(), cf));
							sheetPrize.addCell(new Label(3, rowPrize, entry.getNric(), cf));
							sheetPrize.addCell(new Label(4, rowPrize, (entry.getStation() != null ? entry.getStation().getName() + " (" + entry.getStation().getId()+ ")" : ""),cf));
							sheetPrize.addCell(new Label(5, rowPrize, entry.getReceipt(), cf));
							sheetPrize.addCell(new Label(6, rowPrize, (entry.getPrize() != null ? entry.getPrize().getName() : ""), cf));
							sheetPrize.addCell(new Label(7, rowPrize, new SimpleDateFormat(DATE_PATTERN).format(entry.getCreatedAt()), cf));
							sheetPrize.addCell(new Number(8, rowPrize, entry.getChance(), cf));
 						
							rowPrize++;
							map.put(key, key);
						}
 					}
 				}
 				
 				WritableSheet smsSheet = workbook.createSheet("SMS", 1);
 				smsSheet.addCell(new Label(0, 0, "ID", cf));
 				smsSheet.addCell(new Label(1, 0, "Mobile", cf));
 				smsSheet.addCell(new Label(2, 0, "NRIC/Passport", cf));
 				smsSheet.addCell(new Label(3, 0, "Station", cf));
 				smsSheet.addCell(new Label(4, 0, "Receipt", cf));
 				smsSheet.addCell(new Label(5, 0, "Prize", cf));
 				smsSheet.addCell(new Label(6, 0, "Text", cf));
 				smsSheet.addCell(new Label(7, 0, "Reply Text", cf));
 				smsSheet.addCell(new Label(8, 0, "Reply Status", cf));
 				smsSheet.addCell(new Label(9, 0, "Date", cf));
 				smsSheet.addCell(new Label(10, 0, "Status", cf));
 				
 				row = 1;
 				for(SMSEntry sms : smsEntries) {
 					smsSheet.addCell(new Number(0, row, sms.getId(), cf));
 					smsSheet.addCell(new Label(1, row, sms.getMsisdn(), cf));
 					smsSheet.addCell(new Label(2, row, sms.getNric(), cf));
 					smsSheet.addCell(new Label(3, row, (sms.getStation() != null ? sms.getStation().getName() + " (" + sms.getStation().getId()+ ")" : ""),cf));
 					smsSheet.addCell(new Label(4, row, sms.getReceipt(), cf));
 					smsSheet.addCell(new Label(5, row, (sms.getPrize() != null ? sms.getPrize().getName() : ""), cf));
 					smsSheet.addCell(new Label(6, row, (sms.getMoLog() != null ? sms.getMoLog().getMessage() : ""), cf));
 					smsSheet.addCell(new Label(7, row, (sms.getMtLog() != null ? sms.getMtLog().getMessage() : ""), cf));
 					smsSheet.addCell(new Label(8, row, (sms.getMtLog() != null ? (sms.getMtLog().getStatusId().equals("501") ? "Success" : "Failed") : ""), cf));
 					smsSheet.addCell(new Label(9, row, new SimpleDateFormat(DATE_PATTERN).format(sms.getCreatedAt()), cf));
 					smsSheet.addCell(new Label(10, row, sms.getStatus(), cf));
 					
 					row++;
 				}
 				
 				WritableSheet webSheet = workbook.createSheet("WEB", 2);
 				webSheet.addCell(new Label(0, 0, "ID", cf));
 				webSheet.addCell(new Label(1, 0, "Mobile", cf));
 				webSheet.addCell(new Label(2, 0, "NRIC/Passport", cf));
 				webSheet.addCell(new Label(3, 0, "Name", cf));
 				webSheet.addCell(new Label(4, 0, "Email", cf));
 				webSheet.addCell(new Label(5, 0, "Payment Mode", cf));
 				webSheet.addCell(new Label(6, 0, "Credit Card", cf));
 				webSheet.addCell(new Label(7, 0, "Station", cf));
 				webSheet.addCell(new Label(8, 0, "Fuel Grade", cf));
 				webSheet.addCell(new Label(9, 0, "Receipt", cf));
 				webSheet.addCell(new Label(10, 0, "Prize", cf));
 				webSheet.addCell(new Label(11, 0, "Member", cf));
 				webSheet.addCell(new Label(12, 0, "Subscribe", cf));
 				webSheet.addCell(new Label(13, 0, "Date", cf));
 				webSheet.addCell(new Label(14, 0, "Status", cf));
 				row = 1;
 				for(WebEntry web : webEntries) {
 					webSheet.addCell(new Number(0, row, web.getId(), cf));
 					webSheet.addCell(new Label(1, row, web.getMsisdn(), cf));
 					webSheet.addCell(new Label(2, row, web.getNric(), cf));
 					webSheet.addCell(new Label(3, row, web.getName(), cf));
 					webSheet.addCell(new Label(4, row, web.getEmail(), cf));
 					webSheet.addCell(new Label(5, row, web.getPaymentMode(), cf));
 					webSheet.addCell(new Label(6, row, web.getCcType(), cf));
 					webSheet.addCell(new Label(7, row, (web.getStation() != null ? web.getStation().getName() + " (" + web.getStation().getId()+ ")" : ""),cf));
 					webSheet.addCell(new Label(8, row, web.getFuelGrade(), cf));
 					webSheet.addCell(new Label(9, row, web.getReceipt(), cf));
 					webSheet.addCell(new Label(10, row, (web.getPrize() != null ? web.getPrize().getName() : ""), cf));
 					webSheet.addCell(new Label(11, row, web.isMember() ? "Yes" : "No", cf));
 					webSheet.addCell(new Label(12, row, web.isSubscribe() ? "Yes" : "No", cf));
 					webSheet.addCell(new Label(13, row, new SimpleDateFormat(DATE_PATTERN).format(web.getCreatedAt()), cf));
 					webSheet.addCell(new Label(14, row, web.getStatus(), cf));
 					
 					row++;
 				}
 				
 				workbook.write();
 				workbook.close();
 
 				baos.flush();
 				byte[] result = baos.toByteArray();
 				baos.close();
 
 				return result;
 			} else {
 				throw new Exception("Entries not found.");
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 }
