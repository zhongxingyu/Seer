 package is.idega.idegaweb.pheidippides.business;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.RegistrationAnswerHolder;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.BankReference;
 import is.idega.idegaweb.pheidippides.data.Company;
 import is.idega.idegaweb.pheidippides.data.Distance;
 import is.idega.idegaweb.pheidippides.data.Event;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.Race;
 import is.idega.idegaweb.pheidippides.data.RacePrice;
 import is.idega.idegaweb.pheidippides.data.RaceShirtSize;
 import is.idega.idegaweb.pheidippides.data.Registration;
 import is.idega.idegaweb.pheidippides.data.RegistrationHeader;
 import is.idega.idegaweb.pheidippides.data.ShirtSize;
 import is.idega.idegaweb.pheidippides.data.Team;
 import is.idega.idegaweb.pheidippides.util.PheidippidesUtil;
 
 import java.io.FileInputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.rmi.RemoteException;
 import java.security.MessageDigest;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ejb.CreateException;
 import javax.ejb.EJBException;
 import javax.ejb.FinderException;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.idega.builder.bean.AdvancedProperty;
 import com.idega.business.IBOLookup;
 import com.idega.business.IBOLookupException;
 import com.idega.business.IBORuntimeException;
 import com.idega.core.accesscontrol.business.LoginDBHandler;
 import com.idega.core.accesscontrol.data.LoginTable;
 import com.idega.core.contact.data.Email;
 import com.idega.core.contact.data.Phone;
 import com.idega.core.file.business.ICFileSystemFactory;
 import com.idega.core.file.data.ICFile;
 import com.idega.core.location.data.Address;
 import com.idega.core.location.data.AddressHome;
 import com.idega.core.location.data.Country;
 import com.idega.core.location.data.CountryHome;
 import com.idega.core.location.data.PostalCode;
 import com.idega.core.location.data.PostalCodeHome;
 import com.idega.core.messaging.MessagingSettings;
 import com.idega.data.IDOAddRelationshipException;
 import com.idega.data.IDOLookup;
 import com.idega.idegaweb.IWMainApplication;
 import com.idega.idegaweb.IWMainApplicationSettings;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.user.business.UserBusiness;
 import com.idega.user.data.Gender;
 import com.idega.user.data.GenderHome;
 import com.idega.user.data.Group;
 import com.idega.user.data.User;
 import com.idega.util.Age;
 import com.idega.util.IWTimestamp;
 import com.idega.util.LocaleUtil;
 import com.idega.util.text.Name;
 
 @Scope("singleton")
 @Service("pheidippidesService")
 public class PheidippidesService {
 
 	private static final String RAFRAEN_UNDIRSKRIFT = "RafraenUndirskrift";
 	private static final String SLOD_NOTANDI_HAETTIR_VID = "SlodNotandiHaettirVid";
 	private static final String SLOD_TOKST_AD_GJALDFAERA_SERVER_SIDE = "SlodTokstAdGjaldfaeraServerSide";
 	private static final String SLOD_TOKST_AD_GJALDFAERA_TEXTI = "SlodTokstAdGjaldfaeraTexti";
 	private static final String SLOD_TOKST_AD_GJALDFAERA = "SlodTokstAdGjaldfaera";
 	private static final String TILVISUNARNUMER = "Tilvisunarnumer";
 	private static final String KAUPANDA_UPPLYSINGAR = "KaupandaUpplysingar";
 	private static final String VALITOR_RETURN_URL_CANCEL = "VALITOR_RETURN_URL_CANCEL";
 	private static final String VALITOR_RETURN_URL_SERVER_SIDE = "VALITOR_RETURN_URL_SERVER_SIDE";
 	private static final String VALITOR_RETURN_URL_TEXT = "VALITOR_RETURN_URL_TEXT";
 	private static final String VALITOR_RETURN_URL = "VALITOR_RETURN_URL";
 	private static final String VALITOR_SECURITY_NUMBER_EUR = "VALITOR_SECURITY_NUMBER_EUR";
 	private static final String VALITOR_SHOP_ID_EUR = "VALITOR_SHOP_ID_EUR";
 	private static final String VALITOR_SECURITY_NUMBER = "VALITOR_SECURITY_NUMBER";
 	private static final String VALITOR_SHOP_ID = "VALITOR_SHOP_ID";
 	private static final String ADEINSHEIMILD = "Adeinsheimild";
 	private static final String GJALDMIDILL = "Gjaldmidill";
 	private static final String LANG = "Lang";
 	private static final String VEFVERSLUN_ID = "VefverslunID";
 	private static final String VALITOR_URL = "VALITOR_URL";
 	private static final String VARA = "Vara_";
 	private static final String VARA_LYSING = "_Lysing";
 	private static final String VARA_FJOLDI = "_Fjoldi";
 	private static final String VARA_VERD = "_Verd";
 	private static final String VARA_AFSLATTUR = "_Afslattur";
 
 	private static String DEFAULT_SMTP_MAILSERVER = "mail.idega.is";
 	private static String DEFAULT_MESSAGEBOX_FROM_ADDRESS = "marathon@marathon.is";
 	private static String DEFAULT_CC_ADDRESS = "marathon@marathon.is";
 
 	@Autowired
 	private PheidippidesDao dao;
 
 	public List<Race> getRaces(Long eventPK, int year) {
 		return dao.getRaces(dao.getEvent(eventPK), year);
 	}
 
 	public List<Race> getOpenRaces(Long eventPK, int year) {
 		return getOpenRaces(eventPK, year, true);
 	}
 
 	public List<Race> getOpenRaces(Long eventPK, int year,
 			boolean showRelayRaces) {
 		List<Race> races = getRaces(eventPK, year);
 		List<Race> openRaces = new ArrayList<Race>();
 
 		IWTimestamp stamp = new IWTimestamp();
 		for (Race race : races) {
 			if (stamp.isBetween(
 					new IWTimestamp(race.getOpenRegistrationDate()),
 					new IWTimestamp(race.getCloseRegistrationDate()))) {
 				if (showRelayRaces) {
 					openRaces.add(race);
 				} else {
 					if (race.getNumberOfRelayLegs() < 2) {
 						openRaces.add(race);
 					}
 				}
 			}
 		}
 
 		return openRaces;
 	}
 
 	public List<Race> getAvailableRaces(Long eventPK, int year,
 			Participant participant) {
 		List<Race> races = getOpenRaces(eventPK, year);
 		List<Race> availableRaces = new ArrayList<Race>();
 
 		Date dateOfBirth = participant.getDateOfBirth();
 		if (dateOfBirth != null) {
 			Age age = new Age(dateOfBirth);
 			for (Race race : races) {
 				if (race.getMinimumAge() <= age.getYears()
 						&& race.getMaximumAge() >= age.getYears()) {
 					boolean addRace = true;
 					if (participant.getUuid() != null
 							&& dao.getNumberOfRegistrations(
 									participant.getUuid(), race,
 									RegistrationStatus.OK) > 0) {
 						addRace = false;
 					}
 
 					if (addRace) {
 						availableRaces.add(race);
 					}
 				}
 			}
 		}
 
 		return availableRaces;
 	}
 
 	public List<Race> getAvailableRaces(Long eventPK, int year, Date dateOfBirth) {
 		List<Race> races = getOpenRaces(eventPK, year);
 		List<Race> availableRaces = new ArrayList<Race>();
 
 		if (dateOfBirth != null) {
 			Age age = new Age(dateOfBirth);
 			for (Race race : races) {
 				if (race.getMinimumAge() <= age.getYears()
 						&& race.getMaximumAge() >= age.getYears()) {
 					availableRaces.add(race);
 				}
 			}
 		}
 
 		return availableRaces;
 	}
 
 	public boolean hasAvailableRaces(String personalID, Long eventPK, int year) {
 		Participant participant = getParticipant(personalID);
 
 		boolean hasRaces = false;
 		if (participant != null) {
 
 			List<Race> races = getAvailableRaces(eventPK, year,
 					participant.getDateOfBirth());
 			for (Race race : races) {
 				if (dao.getNumberOfRegistrations(participant.getUuid(), race,
 						RegistrationStatus.OK) == 0) {
 					hasRaces = true;
 				}
 			}
 		}
 
 		return hasRaces;
 	}
 
 	public List<RaceShirtSize> getShirts(Long racePK) {
 		if (racePK != null) {
 			return dao.getRaceShirtSizes(dao.getRace(racePK));
 		}
 		return new ArrayList<RaceShirtSize>();
 	}
 
 	public Participant getParticipant(Registration registration) {
 		Participant p = null;
 
 		try {
 			User user = getUserBusiness().getUserByUniqueId(
 					registration.getUserUUID());
 			p = getParticipant(user);
 			p.setNationality(registration.getNationality());
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (FinderException e) {
 		}
 
 		return p;
 	}
 
 	public Participant getParticipant(String personalID) {
 		Participant p = null;
 
 		try {
 			User user = getUserBusiness().getUser(personalID);
 			p = getParticipant(user);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (FinderException e) {
 		}
 
 		return p;
 	}
 
 	public Map<Registration, Participant> getParticantMap(
 			List<Registration> registrations) {
 		Map<Registration, Participant> participants = new HashMap<Registration, Participant>();
 
 		for (Registration registration : registrations) {
 			Participant participant = getParticipant(registration);
 			if (participant != null) {
 				participants.put(registration, participant);
 			}
 		}
 
 		return participants;
 	}
 
 	public Map<String, Participant> getRegistratorMap(
 			List<RegistrationHeader> headers) {
 		Map<String, Participant> participants = new HashMap<String, Participant>();
 
 		for (RegistrationHeader header : headers) {
 			try {
 				User user = null;
 
 				if (header.getRegistrantUUID() != null) {
 					user = getUserBusiness().getUserByUniqueId(
 							header.getRegistrantUUID());
 				} else {
 					List<Registration> registrations = dao
 							.getRegistrations(header);
 					if (registrations != null && !registrations.isEmpty()) {
 						user = getUserBusiness().getUserByUniqueId(
 								registrations.iterator().next().getUserUUID());
 					}
 				}
 
 				if (user != null) {
 					Participant participant = getParticipant(user);
 					if (participant != null) {
 						participants.put(header.getUuid(), participant);
 					}
 				}
 			} catch (RemoteException re) {
 				throw new IBORuntimeException(re);
 			} catch (FinderException fe) {
 				// No user found...
 			}
 		}
 
 		return participants;
 	}
 
 	public Map<String, Participant> getCompanyParticipantMap(
 			List<Company> companies) {
 		Map<String, Participant> participants = new HashMap<String, Participant>();
 
 		for (Company company : companies) {
 			try {
 				User user = null;
 
 				if (company.getUserUUID() != null) {
 					user = getUserBusiness().getUserByUniqueId(
 							company.getUserUUID());
 				}
 
 				if (user != null) {
 					Participant participant = getParticipant(user);
 					if (participant != null) {
 						participants.put(company.getUuid(), participant);
 					}
 				}
 			} catch (RemoteException re) {
 				throw new IBORuntimeException(re);
 			} catch (FinderException fe) {
 				// No user found...
 			}
 		}
 
 		return participants;
 	}
 
 	public Map<RegistrationHeader, BankReference> getBankReferencesMap(
 			List<RegistrationHeader> headers) {
 		Map<RegistrationHeader, BankReference> references = new HashMap<RegistrationHeader, BankReference>();
 
 		for (RegistrationHeader header : headers) {
 			BankReference reference = dao.findBankReference(header);
 			if (reference != null) {
 				references.put(header, reference);
 			}
 		}
 
 		return references;
 	}
 
 	public boolean isValidPersonalID(String personalID) {
 		if (personalID != null && personalID.length() == 10) {
 			return getParticipant(personalID) != null;
 		}
 		return true;
 	}
 
 	public Participant getParticipant(User user) {
 		Participant p = new Participant();
 		p.setFirstName(user.getFirstName());
 		p.setMiddleName(user.getMiddleName());
 		p.setLastName(user.getLastName());
 		p.setFullName(user.getName());
 		p.setPersonalId(user.getPersonalID());
 		p.setDateOfBirth(user.getDateOfBirth());
 		p.setUuid(user.getUniqueId());
 		if (user.getGender() != null) {
 			p.setGender(user.getGender().getName());
 		}
 		p.setForeigner(p.getPersonalId() == null);
 
 		try {
 			Address address = user.getUsersMainAddress();
 			if (address != null) {
 				p.setAddress(address.getStreetAddress());
 				p.setPostalAddress(address.getPostalAddress());
 				if (address.getPostalCode() != null) {
 					p.setPostalCode(address.getPostalCode().getPostalCode());
 				}
 				p.setCity(address.getCity());
 
 				Country country = address.getCountry();
 				if (country != null) {
 					p.setCountry(country.getPrimaryKey().toString());
 				}
 			}
 		} catch (EJBException e) {
 		} catch (RemoteException e) {
 		}
 
 		try {
 			Phone homePhone = user.getUsersHomePhone();
 			if (homePhone != null) {
 				p.setPhoneHome(homePhone.getNumber());
 			}
 		} catch (EJBException e) {
 		} catch (RemoteException e) {
 		}
 
 		try {
 			Phone mobilePhone = user.getUsersMobilePhone();
 			if (mobilePhone != null) {
 				p.setPhoneMobile(mobilePhone.getNumber());
 			}
 		} catch (EJBException e) {
 		} catch (RemoteException e) {
 		}
 
 		try {
 			Email email = user.getUsersEmail();
 			if (email != null) {
 				p.setEmail(email.getEmailAddress());
 			}
 		} catch (EJBException e) {
 		} catch (RemoteException e) {
 		}
 
 		if (user.getSystemImageID() != -1) {
 			try {
 				String URI = ICFileSystemFactory.getFileSystem(
 						IWMainApplication.getDefaultIWApplicationContext())
 						.getFileURI(user.getSystemImageID());
 				p.setImageURL(URI);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			LoginTable loginTable = LoginDBHandler.getUserLogin(user);
 			if (loginTable != null) {
 				p.setLogin(loginTable.getUserLogin());
 			}
 		} catch (Exception e) {
 		}
 
 		return p;
 	}
 
 	public Map<CompanyImportStatus, List<Participant>> importCompanyExcelFile(
 			FileInputStream input, Event event, int year) {
 		Map<CompanyImportStatus, List<Participant>> map = new HashMap<CompanyImportStatus, List<Participant>>();
 
 		try {
 			HSSFWorkbook wb = new HSSFWorkbook(input);
 			HSSFSheet sheet = wb.getSheetAt(0);
 
 			NumberFormat format = NumberFormat.getNumberInstance();
 			format.setGroupingUsed(false);
 			format.setMinimumIntegerDigits(10);
 
 			List<Participant> ok = new ArrayList<Participant>();
 			List<Participant> missing = new ArrayList<Participant>();
 			List<Participant> errorInPID = new ArrayList<Participant>();
 			List<Participant> errorAlreadyReg = new ArrayList<Participant>();
 
 			for (int a = sheet.getFirstRowNum() + 1; a <= sheet.getLastRowNum(); a++) {
 				boolean rowHasError = false;
 				boolean errorInPersonalID = false;
 				boolean errorAlreadyRegistered = false;
 
 				HSSFRow row = sheet.getRow(a);
 
 				User user = null;
 				Participant participant = new Participant();
 
 				int column = 0;
 				String personalID = getCellValue(row.getCell(column++));
 				String uniqueID = getCellValue(row.getCell(column++));
 				String name = getCellValue(row.getCell(column++));
 				String dateOfBirth = getCellValue(row.getCell(column++));
 				String address = getCellValue(row.getCell(column++));
 				String city = getCellValue(row.getCell(column++));
 				String postalCode = getCellValue(row.getCell(column++));
 				String country = getCellValue(row.getCell(column++));
 				String gender = getCellValue(row.getCell(column++));
 				String email = getCellValue(row.getCell(column++));
 				String phone = getCellValue(row.getCell(column++));
 				String mobile = getCellValue(row.getCell(column++));
 				String nationality = getCellValue(row.getCell(column));
 
 				// Hmmmm, is this correct?
 				if (personalID == null && uniqueID == null
 						&& (name == null || dateOfBirth == null)) {
 					continue;
 				}
 
 				if (personalID != null) {
 					try {
 						personalID = format.format(format.parse(personalID
 								.replaceAll("-", "")));
 					} catch (ParseException e1) {
 						rowHasError = true;
 						errorInPersonalID = true;
 					}
 				}
 
 				if (!rowHasError) {
 					if (personalID != null || uniqueID != null) {
 						try {
 							if (uniqueID != null) {
 								user = getUserBusiness().getUserByUniqueId(
 										uniqueID);
 							} else if (personalID != null) {
 								user = getUserBusiness().getUser(personalID);
 							}
 						} catch (Exception e) {
 							rowHasError = true;
 							errorInPersonalID = true;
 						}
 					}
 
 					if (user == null) {
 						Date dob = null;
 						if (name == null || "".equals(name.trim())) {
 							rowHasError = true;
 						}
 
 						if (dateOfBirth == null
 								|| "".equals(dateOfBirth.trim())) {
 							rowHasError = true;
 						} else {
 							try {
 								DateFormat dateFormat = new SimpleDateFormat(
 										"dd.MM.yyyy");
 								dob = dateFormat.parse(dateOfBirth);
 							} catch (Exception e) {
 								e.printStackTrace();
 								rowHasError = true;
 							}
 						}
 
 						if (address == null || "".equals(address.trim())) {
 							rowHasError = true;
 						}
 
 						if (city == null || "".equals(city.trim())) {
 							rowHasError = true;
 						}
 
 						if (postalCode == null || "".equals(postalCode.trim())) {
 							rowHasError = true;
 						}
 
 						if (country == null || "".equals(country.trim())) {
 							rowHasError = true;
 						}
 
 						if (gender == null || "".equals(gender.trim())) {
 							rowHasError = true;
 						}
 
 						if (email == null || "".equals(email.trim())) {
 							rowHasError = true;
 						}
 
 						if (nationality == null
 								|| "".equals(nationality.trim())) {
 							rowHasError = true;
 						}
 
 						if (!rowHasError) {
 							participant.setFullName(name);
 							participant.setDateOfBirth(dob);
 							participant.setAddress(address);
 							participant.setCity(city);
 							participant.setPostalCode(postalCode);
 							participant.setCountry(country);
 							participant.setGender(gender);
 							participant.setEmail(email);
 							participant.setPhoneHome(phone);
 							participant.setPhoneMobile(mobile);
 							participant.setNationality(nationality);
 						}
 					} else {
 						if (email == null || "".equals(email.trim())) {
 							rowHasError = true;
 						}
 
 						if (isRegistered(user, event, year)) {
 							rowHasError = true;
 							errorAlreadyRegistered = true;
 						}
 
 						participant = getParticipant(user);
 						participant.setEmail(email);
 						participant.setPhoneHome(phone);
 						participant.setPhoneMobile(mobile);
 					}
 				}
 
 				if (rowHasError) {
 					if (errorInPersonalID) {
 						errorInPID.add(createErrorParticipant(personalID,
 								uniqueID, name, dateOfBirth, address, city,
 								postalCode, country, gender, email, phone,
 								mobile, nationality));
 					} else if (errorAlreadyRegistered) {
 						errorAlreadyReg.add(participant);
 					} else {
 						missing.add(createErrorParticipant(personalID,
 								uniqueID, name, dateOfBirth, address, city,
 								postalCode, country, gender, email, phone,
 								mobile, nationality));
 					}
 				} else {
 					ok.add(participant);
 				}
 			}
 
 			if (!errorInPID.isEmpty()) {
 				map.put(CompanyImportStatus.ERROR_IN_PERSONAL_ID, errorInPID);
 			}
 
 			if (!errorAlreadyReg.isEmpty()) {
 				map.put(CompanyImportStatus.ERROR_ALREADY_REGISTERED,
 						errorAlreadyReg);
 			}
 
 			if (!missing.isEmpty()) {
 				map.put(CompanyImportStatus.MISSING_REQUIRED_FIELD, missing);
 			}
 
 			map.put(CompanyImportStatus.OK, ok);
 
 			return map;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public boolean isRegistered(User user, Event event, int year) {
 		List<Registration> regs = dao.getRegistrationForUser(event, year,
 				user.getUniqueId());
 
 		if (regs != null && !regs.isEmpty()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	private String getCellValue(HSSFCell cell) {
 		if (cell == null) {
 			return null;
 		}
 
 		String value = null;
 		if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
 			value = cell.getStringCellValue();
 		} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
 			if (DateUtil.isCellDateFormatted(cell)) {
 				IWTimestamp stamp = new IWTimestamp(cell.getDateCellValue());
 				value = stamp.getDateString("dd.MM.yyyy");
 			} else {
 				value = String.valueOf(new Double(cell.getNumericCellValue())
 						.longValue());
 			}
 		} else {
 			value = cell.getStringCellValue();
 		}
 
 		return value;
 	}
 
 	private Participant createErrorParticipant(String personalID,
 			String uniqueID, String name, String dateOfBirth, String address,
 			String city, String postalCode, String country, String gender,
 			String email, String phone, String mobile, String nationality) {
 		Participant errorParticipant = new Participant();
 
 		errorParticipant.setPersonalId(personalID);
 		errorParticipant.setUuid(uniqueID);
 		errorParticipant.setFullName(name);
 		try {
 			if (dateOfBirth != null) {
 				DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 				errorParticipant.setDateOfBirth(dateFormat.parse(dateOfBirth));
 			}
 		} catch (Exception e) {
 		}
 		errorParticipant.setAddress(address);
 		errorParticipant.setCity(city);
 		errorParticipant.setPostalCode(postalCode);
 		errorParticipant.setCountry(country);
 		errorParticipant.setGender(gender);
 		errorParticipant.setEmail(email);
 		errorParticipant.setPhoneHome(phone);
 		errorParticipant.setPhoneMobile(mobile);
 		errorParticipant.setNationality(nationality);
 
 		return errorParticipant;
 	}
 
 	public RegistrationAnswerHolder storeRegistration(
 			List<ParticipantHolder> holders, boolean doPayment,
 			String registrantUUID, boolean createUsers, Locale locale,
 			String paymentGroup, boolean isBankTransfer) {
 
 		RegistrationAnswerHolder holder = new RegistrationAnswerHolder();
 
 		String valitorURL = IWMainApplication
 				.getDefaultIWApplicationContext()
 				.getApplicationSettings()
 				.getProperty(VALITOR_URL,
 						"https://testvefverslun.valitor.is/default.aspx");
 		String valitorShopID = IWMainApplication
 				.getDefaultIWApplicationContext().getApplicationSettings()
 				.getProperty(VALITOR_SHOP_ID, "1");
 		String valitorSecurityNumber = IWMainApplication
 				.getDefaultIWApplicationContext().getApplicationSettings()
 				.getProperty(VALITOR_SECURITY_NUMBER, "12345");
 		String valitorShopIDEUR = IWMainApplication
 				.getDefaultIWApplicationContext().getApplicationSettings()
 				.getProperty(VALITOR_SHOP_ID_EUR, "1");
 		String valitorSecurityNumberEUR = IWMainApplication
 				.getDefaultIWApplicationContext().getApplicationSettings()
 				.getProperty(VALITOR_SECURITY_NUMBER_EUR, "12345");
 
 		String valitorReturnURL = IWMainApplication
 				.getDefaultIWApplicationContext()
 				.getApplicationSettings()
 				.getProperty(VALITOR_RETURN_URL,
 						"http://skraning.marathon.is/pages/valitor");
 		String valitorReturnURLText = IWMainApplication
 				.getDefaultIWApplicationContext().getApplicationSettings()
 				.getProperty(VALITOR_RETURN_URL_TEXT, "Halda afram");
 		String valitorReturnURLServerSide = IWMainApplication
 				.getDefaultIWApplicationContext()
 				.getApplicationSettings()
 				.getProperty(VALITOR_RETURN_URL_SERVER_SIDE,
 						"http://skraning.marathon.is/pages/valitor");
 		String valitorReturnURLCancel = IWMainApplication
 				.getDefaultIWApplicationContext()
 				.getApplicationSettings()
 				.getProperty(VALITOR_RETURN_URL_CANCEL,
 						"http://skraning.marathon.is/pages/valitor");
 
 		StringBuilder securityString = null;
 		if (createUsers) {
 			securityString = new StringBuilder(valitorSecurityNumberEUR);
 		} else {
 			securityString = new StringBuilder(valitorSecurityNumber);
 		}
 
 		StringBuilder url = new StringBuilder(valitorURL);
 		url.append("?");
 		url.append(VEFVERSLUN_ID);
 		url.append("=");
 		if (createUsers) {
 			url.append(valitorShopIDEUR);
 		} else {
 			url.append(valitorShopID);
 		}
 		url.append("&");
 		url.append(LANG);
 		url.append("=");
 		if (Locale.ENGLISH.equals(locale)) {
 			url.append("en");
 		} else {
 			url.append("is");
 		}
 		String currency = "ISK";
 		if (createUsers) {
 			currency = "EUR";
 		}
 		url.append("&");
 		url.append(GJALDMIDILL);
 		url.append("=");
 		url.append(currency);
 		url.append("&");
 		url.append(ADEINSHEIMILD);
 		url.append("=");
 		url.append("0");
 		securityString.append("0");
 
 		if (holders != null && !holders.isEmpty()) {
 			RegistrationHeader header = null;
 			if (doPayment) {
 				header = dao.storeRegistrationHeader(null,
 						RegistrationHeaderStatus.WaitingForPayment,
 						registrantUUID, paymentGroup, locale.toString(),
 						createUsers ? Currency.EUR : Currency.ISK, null, null,
 						null, null, null, null, null, null, null, null);
 			} else {
 				header = dao.storeRegistrationHeader(null,
 						RegistrationHeaderStatus.RegisteredWithoutPayment,
 						registrantUUID, paymentGroup, locale.toString(),
 						createUsers ? Currency.EUR : Currency.ISK, null, null,
 						null, null, null, null, null, null, null, null);
 			}
 			holder.setHeader(header);
 
 			valitorReturnURLServerSide += "?uniqueID=" + header.getUuid();
 			valitorReturnURLCancel += "?uniqueID=" + header.getUuid();
 
 			if (isBankTransfer) {
 				BankReference reference = dao.storeBankReference(header);
 				holder.setBankReference(reference);
 			}
 
 			int amount = 0;
 
 			int counter = 1;
 			for (ParticipantHolder participantHolder : holders) {
 				User user = null;
 				Participant participant = participantHolder.getParticipant();
 				if (createUsers) {
 					if (participant.getUuid() != null) {
 						try {
 							user = getUserBusiness().getUserByUniqueId(
 									participant.getUuid());
 						} catch (RemoteException e) {
 						} catch (FinderException e) {
 						}
 					}
 
 					try {
 						if (user == null) {
 							Gender gender = null;
 							if (participant.getGender().equals(
 									getGenderHome().getMaleGender().getName())) {
 								gender = getGenderHome().getMaleGender();
 							} else {
 								gender = getGenderHome().getFemaleGender();
 							}
 							user = saveUser(
 									new Name(participant.getFullName()),
 									new IWTimestamp(participant
 											.getDateOfBirth()),
 									gender,
 									participant.getAddress(),
 									participant.getPostalCode(),
 									participant.getCity(),
 									getCountryHome().findByPrimaryKey(
 											new Integer(participant
 													.getCountry())));
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 						user = null; // Something got fucked up
 					}
 				} else {
 					try {
 						user = getUserBusiness().getUserByUniqueId(
 								participant.getUuid());
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (user != null) {
 					if (participant.getPhoneMobile() != null
 							&& !"".equals(participant.getPhoneMobile())) {
 						try {
 							getUserBusiness().updateUserMobilePhone(user,
 									participant.getPhoneMobile());
 						} catch (Exception e) {
 						}
 					}
 
 					if (participant.getPhoneHome() != null
 							&& !"".equals(participant.getPhoneHome())) {
 						try {
 							getUserBusiness().updateUserHomePhone(user,
 									participant.getPhoneHome());
 						} catch (Exception e) {
 						}
 					}
 
 					if (participant.getEmail() != null
 							&& !"".equals(participant.getEmail())) {
 						try {
 							getUserBusiness().updateUserMail(user,
 									participant.getEmail());
 						} catch (Exception e) {
 						}
 					}
 
 					List<Participant> relayPartners = participantHolder
 							.getRelayPartners();
 					Team team = participantHolder.getTeam();
 					if (relayPartners != null && !relayPartners.isEmpty()) {
 						team = dao.storeTeam(team.getId(), team.getName(),
 								team.isRelayTeam());
 					}
 
 					dao.storeRegistration(null, header,
 							RegistrationStatus.Unconfirmed,
 							participantHolder.getRace(),
 							participantHolder.getShirtSize(), team,
 							participantHolder.getLeg(),
 							participantHolder.getAmount(),
 							participantHolder.getCharity(),
 							participant.getNationality(), user.getUniqueId(),
 							participantHolder.getDiscount(),
 							participantHolder.isHasDoneMarathonBefore(),
 							participantHolder.isHasDoneLVBefore(),
 							participantHolder.getBestMarathonTime(),
 							participantHolder.getBestUltraMarathonTime());
 
 					amount += participantHolder.getAmount()
 							- participantHolder.getDiscount();
 
 					securityString.append("1");
 					securityString.append(participantHolder.getAmount());
 					securityString.append(participantHolder.getDiscount());
 
 					url.append("&");
 					url.append(VARA);
 					url.append(counter);
 					url.append(VARA_LYSING);
 					url.append("=");
 					try {
 						url.append(URLEncoder.encode(
 								participantHolder.getValitorDescription(),
 								"UTF-8"));
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 					url.append("&");
 					url.append(VARA);
 					url.append(counter);
 					url.append(VARA_FJOLDI);
 					url.append("=");
 					url.append("1");
 					url.append("&");
 					url.append(VARA);
 					url.append(counter);
 					url.append(VARA_VERD);
 					url.append("=");
 					url.append(participantHolder.getAmount());
 					url.append("&");
 					url.append(VARA);
 					url.append(counter++);
 					url.append(VARA_AFSLATTUR);
 					url.append("=");
 					url.append(participantHolder.getDiscount());
 
 					if (relayPartners != null && !relayPartners.isEmpty()) {
 						for (Participant participant2 : relayPartners) {
 							user = null;
 
 							if (createUsers) {
 								if (participant2.getPersonalId() != null) {
 									try {
 										user = getUserBusiness().getUser(
 												participant2.getPersonalId());
 									} catch (RemoteException e) {
 									} catch (FinderException e) {
 									}
 								}
 
 								try {
 									if (user == null) {
 										user = saveUser(
 												new Name(participant2
 														.getFullName()),
 												new IWTimestamp(participant2
 														.getDateOfBirth()),
 												null, null, null, null, null);
 									}
 								} catch (Exception e) {
 									e.printStackTrace();
 									user = null; // Something got fucked up
 								}
 							} else {
 								try {
 									user = getUserBusiness().getUser(
 											participant2.getPersonalId());
 								} catch (Exception e) {
 									e.printStackTrace();
 								}
 							}
 
 							if (user != null) {
 								if (participant2.getEmail() != null
 										&& !"".equals(participant2.getEmail())) {
 									try {
 										getUserBusiness().updateUserMail(user,
 												participant2.getEmail());
 									} catch (Exception e) {
 									}
 								}
 
 								dao.storeRegistration(null, header,
 										RegistrationStatus.RelayPartner,
 										participantHolder.getRace(),
 										participant2.getShirtSize(), team,
 										participant2.getRelayLeg(), 0, null,
 										participant.getNationality(),
 										user.getUniqueId(), 0, false, false,
 										null, null);
 							}
 						}
 					}
 				}
 			}
 
 			if (createUsers) {
 				securityString.append(valitorShopIDEUR);
 			} else {
 				securityString.append(valitorShopID);
 			}
 			securityString.append(header.getUuid());
 			securityString.append(valitorReturnURL);
 			securityString.append(valitorReturnURLServerSide);
 			securityString.append(currency);
 
 			url.append("&");
 			url.append(KAUPANDA_UPPLYSINGAR);
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append("NafnSkylda");
 			url.append("=");
 			url.append("1");
 			if (!createUsers) {
 				url.append("&");
 				url.append("KennitalaSkylda");
 				url.append("=");
 				url.append("1");
 			} else {
 				url.append("&");
 				url.append("FelaKennitala");
 				url.append("=");
 				url.append("1");
 			}
 			url.append("&");
 			url.append("FelaHeimilisfang");
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append("FelaPostnumer");
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append("FelaStadur");
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append("FelaLand");
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append("FelaNetfang");
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append("FelaAthugasemdir");
 			url.append("=");
 			url.append("1");
 			url.append("&");
 			url.append(TILVISUNARNUMER);
 			url.append("=");
 			url.append(header.getUuid());
 			url.append("&");
 			url.append(SLOD_TOKST_AD_GJALDFAERA);
 			url.append("=");
 			url.append(valitorReturnURL);
 			url.append("&");
 			url.append(SLOD_TOKST_AD_GJALDFAERA_TEXTI);
 			url.append("=");
 			url.append(valitorReturnURLText);
 			url.append("&");
 			url.append(SLOD_TOKST_AD_GJALDFAERA_SERVER_SIDE);
 			url.append("=");
 			url.append(valitorReturnURLServerSide);
 			url.append("&");
 			url.append(SLOD_NOTANDI_HAETTIR_VID);
 			url.append("=");
 			url.append(valitorReturnURLCancel);
 			url.append("&");
 			url.append(RAFRAEN_UNDIRSKRIFT);
 			url.append("=");
 			url.append(createValitorSecurityString(securityString.toString()));
 
 			holder.setAmount(amount);
 			holder.setValitorURL(url.toString());
 		}
 
 		return holder;
 	}
 
 	public static void main(String args[]) {
 		PheidippidesService service = new PheidippidesService();
 		String uuid = service
 				.createValitorSecurityString("12345011999018536035056191428820http://www.mbl.ishttp://www.visir.isISK");
 		uuid = service
 				.createValitorSecurityString("2ef8ec654c0215000110000207456http://www.minsida.is/takkfyrirhttp://www.minsida.is/sale.aspx?c=82 82&ref=232ISK");
 
 		StringBuilder builder = new StringBuilder(
 				"https://testvefverslun.valitor.is/default.aspx");
 		builder.append("?VefverslunID=1").append("&Lang=is")
 				.append("&Gjaldmidill=ISK").append("&Adeinsheimild=0")
 				.append("&Vara_1_Lysing=Palli");
 		builder.append("&Vara_1_Fjoldi=1").append("&Vara_1_Verd=1999")
 				.append("&Vara_1_Afslattur=0").append("&KaupandaUpplysingar=0")
 				.append("&Tilvisunarnumer=8536035056191428820");
 		builder.append("&SlodTokstAdGjaldfaera=http://www.mbl.is")
 				.append("&SlodTokstAdGjaldfaeraTexti=Eureka")
 				.append("&SlodTokstAdGjaldfaeraServerSide=http://www.visir.is");
 		builder.append("&SlodNotandiHaettirVid=http://www.bleikt.is")
 				.append("&RafraenUndirskrift=").append(uuid);
 
 		System.out.println("url = " + builder.toString());
 	}
 
 	public User saveUser(Name fullName, IWTimestamp dateOfBirth, Gender gender,
 			String address, String postal, String city, Country country) {
 		User user = null;
 		try {
 			user = getUserBusiness().createUser(fullName.getFirstName(),
 					fullName.getMiddleName(), fullName.getLastName(), null,
 					gender, dateOfBirth);
 			user.store();
 
 			if (address != null && !address.equals("")) {
 				Address a = getAddressHome().create();
 				a.setStreetName(address);
 				a.setCity(city);
 				a.setCountry(country);
 				a.setAddressType(getAddressHome().getAddressType1());
 				a.store();
 
 				Integer countryID = (Integer) country.getPrimaryKey();
 				PostalCode p = null;
 				try {
 					p = getPostalCodeHome().findByPostalCodeAndCountryId(
 							postal, countryID.intValue());
 				} catch (FinderException fe) {
 					p = getPostalCodeHome().create();
 					p.setCountry(country);
 					p.setPostalCode(postal);
 					p.setName(city);
 					p.store();
 				}
 				if (p != null) {
 					a.setPostalCode(p);
 				}
 				a.store();
 				try {
 					user.addAddress(a);
 				} catch (IDOAddRelationshipException idoEx) {
 				}
 			}
 			user.store();
 		} catch (RemoteException rme) {
 		} catch (CreateException cre) {
 		}
 		return user;
 	}
 
 	public User updateUser(String uuid, String fullName,
 			java.sql.Date dateOfBirth, String address, String postalCode,
 			String city, Integer countryPK, String gender, String email,
 			String phone, String mobile, ICFile image) {
 		try {
 			Gender userGender = null;
 			if (gender != null
 					&& gender.equals(getGenderHome().getMaleGender().getName())) {
 				userGender = getGenderHome().getMaleGender();
 			} else if (gender != null) {
 				userGender = getGenderHome().getFemaleGender();
 			}
 
 			User user = getUserBusiness().getUserByUniqueId(uuid);
 			if (fullName != null) {
 				user.setFullName(fullName);
 			}
 			if (dateOfBirth != null) {
 				user.setDateOfBirth(dateOfBirth);
 			}
 			if (userGender != null) {
 				user.setGender((Integer) userGender.getPrimaryKey());
 			}
 			if (image != null) {
 				user.setSystemImageID((Integer) image.getPrimaryKey());
 			}
 			user.store();
 
 			if (postalCode != null && countryPK != null) {
 				Country country = getCountryHome().findByPrimaryKey(countryPK);
 				PostalCode postal = getUserBusiness().getAddressBusiness()
 						.getPostalCodeAndCreateIfDoesNotExist(postalCode, city);
 				getUserBusiness().updateUsersMainAddressOrCreateIfDoesNotExist(
 						user, address, postal, country, city, null, null, null);
 			}
 
 			getUserBusiness().updateUserMail(user, email);
 			getUserBusiness().updateUserHomePhone(user, phone);
 			getUserBusiness().updateUserMobilePhone(user, mobile);
 
 			return user;
 		} catch (RemoteException re) {
 			re.printStackTrace();
 		} catch (FinderException fe) {
 			fe.printStackTrace();
 		} catch (CreateException ce) {
 			ce.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public void updateRegistrationStatus() {
 
 	}
 
 	public void calculatePrices(ParticipantHolder current,
 			List<ParticipantHolder> holder, boolean isRegistrationWithPersonalID) {
 		int childCount = 0;
 		if (holder != null && !holder.isEmpty()) {
 			for (ParticipantHolder participantHolder : holder) {
 				Race race = participantHolder.getRace();
 				RacePrice price = dao.getCurrentRacePrice(race,
 						isRegistrationWithPersonalID ? Currency.ISK
 								: Currency.EUR);
 				Participant participant = participantHolder.getParticipant();
 
 				Age age = new Age(participant.getDateOfBirth());
 				if (age.getYears() <= 16) {
 					if (price.getPriceKids() > 0) {
 						participantHolder.setAmount(price.getPriceKids());
 					} else {
 						participantHolder.setAmount(price.getPrice());
 					}
 				} else {
 					participantHolder.setAmount(price.getPrice());
 				}
 
 				if (race.isFamilyDiscount()) {
 					if (age.getYears() <= 16) {
 						childCount++;
 					}
 
 					if (childCount > 1 && price.getFamilyDiscount() > 0) {
 						participantHolder.setDiscount(participantHolder
 								.getAmount() - price.getFamilyDiscount());
 					}
 				}
 			}
 		}
 
 		if (current != null) {
 			Race race = current.getRace();
 			RacePrice price = dao.getCurrentRacePrice(race,
 					isRegistrationWithPersonalID ? Currency.ISK : Currency.EUR);
 			Participant participant = current.getParticipant();
 
 			Age age = new Age(participant.getDateOfBirth());
 			if (age.getYears() <= 16) {
 				if (price.getPriceKids() > 0) {
 					current.setAmount(price.getPriceKids());
 				} else {
 					current.setAmount(price.getPrice());
 				}
 			} else {
 				current.setAmount(price.getPrice());
 			}
 
 			if (race.isFamilyDiscount()) {
 				if (age.getYears() <= 16) {
 					childCount++;
 				}
 
 				if (childCount > 1 && price.getFamilyDiscount() > 0) {
 					current.setDiscount(current.getAmount()
 							- price.getFamilyDiscount());
 				}
 			}
 		}
 
 		/*
 		 * current.setAmount(100); if (holder != null) { for (ParticipantHolder
 		 * participantHolder : holder) { participantHolder.setAmount(100);
 		 * participantHolder.setDiscount(100); } }
 		 */
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<AdvancedProperty> getCountries() {
 		List<AdvancedProperty> properties = new ArrayList<AdvancedProperty>();
 
 		try {
 			Collection<Country> countries = getCountryHome().findAll();
 			for (Country country : countries) {
 				properties.add(new AdvancedProperty(country.getPrimaryKey()
 						.toString(), country.getName()));
 			}
 		} catch (FinderException fe) {
 			fe.printStackTrace();
 		}
 
 		return properties;
 	}
 
 	private AddressHome getAddressHome() {
 		try {
 			return (AddressHome) IDOLookup.getHome(Address.class);
 		} catch (RemoteException rme) {
 			throw new RuntimeException(rme.getMessage());
 		}
 	}
 
 	private PostalCodeHome getPostalCodeHome() {
 		try {
 			return (PostalCodeHome) IDOLookup.getHome(PostalCode.class);
 		} catch (RemoteException rme) {
 			throw new RuntimeException(rme.getMessage());
 		}
 	}
 
 	private CountryHome getCountryHome() {
 		try {
 			return (CountryHome) IDOLookup.getHome(Country.class);
 		} catch (RemoteException rme) {
 			throw new RuntimeException(rme.getMessage());
 		}
 	}
 
 	private GenderHome getGenderHome() {
 		try {
 			return (GenderHome) IDOLookup.getHome(Gender.class);
 		} catch (RemoteException rme) {
 			throw new RuntimeException(rme.getMessage());
 		}
 	}
 
 	public UserBusiness getUserBusiness() {
 		try {
 			return (UserBusiness) IBOLookup.getServiceInstance(
 					IWMainApplication.getDefaultIWApplicationContext(),
 					UserBusiness.class);
 		} catch (IBOLookupException ile) {
 			throw new IBORuntimeException(ile);
 		}
 	}
 
 	public String createValitorSecurityString(String seed) {
 		try {
 			byte[] bytestOfMessage = seed.getBytes("UTF-8");
 			MessageDigest algorithm = MessageDigest.getInstance("MD5");
 			algorithm.reset();
 			algorithm.update(bytestOfMessage);
 			byte messageDigest[] = algorithm.digest();
 
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageDigest.length; i++) {
 				String tmp = Integer.toHexString(0xFF & messageDigest[i]);
 				if (tmp.length() < 2) {
 					tmp = "0" + tmp;
 				}
 				hexString.append(tmp);
 			}
 
 			return hexString.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public List<AdvancedProperty> getLocalizedRaces(Long eventPK, int year,
 			String language, boolean addEmptyValue) {
 		List<AdvancedProperty> properties = new ArrayList<AdvancedProperty>();
 		if (addEmptyValue) {
 			IWResourceBundle iwrb = IWMainApplication
 					.getDefaultIWMainApplication()
 					.getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 					.getResourceBundle(LocaleUtil.getLocale(language));
 
 			properties.add(new AdvancedProperty("", iwrb.getLocalizedString(
 					"all_races", "All races")));
 		}
 
 		List<Race> races = getRaces(eventPK, year);
 		for (Race race : races) {
 			properties.add(getLocalizedRaceName(race, language));
 		}
 
 		return properties;
 	}
 
 	public AdvancedProperty getLocalizedRaceName(Race race, String language) {
 		IWResourceBundle iwrb = IWMainApplication.getDefaultIWMainApplication()
 				.getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 				.getResourceBundle(LocaleUtil.getLocale(language));
 
 		Event event = race.getEvent();
 		Distance distance = race.getDistance();
 
 		return new AdvancedProperty(String.valueOf(race.getId()),
 				PheidippidesUtil.escapeXML(iwrb.getLocalizedString(
 						event.getLocalizedKey()
 								+ "."
 								+ distance.getLocalizedKey()
 								+ (race.getNumberOfRelayLegs() > 1 ? ".relay"
 										: ""), distance.getName())));
 	}
 
 	public List<AdvancedProperty> getLocalizedShirts(Long racePK,
 			String language) {
 		List<AdvancedProperty> properties = new ArrayList<AdvancedProperty>();
 
 		IWResourceBundle iwrb = IWMainApplication.getDefaultIWMainApplication()
 				.getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 				.getResourceBundle(LocaleUtil.getLocale(language));
 
 		properties.add(new AdvancedProperty("", iwrb.getLocalizedString(
 				"select_shirt_size", "Select shirt size")));
 
 		List<RaceShirtSize> shirts = getShirts(racePK);
 		for (RaceShirtSize shirt : shirts) {
 			properties.add(getLocalizedShirtName(shirt, language));
 		}
 
 		return properties;
 	}
 
 	public AdvancedProperty getLocalizedShirtName(RaceShirtSize raceShirt,
 			String language) {
 		ShirtSize size = raceShirt.getSize();
 		Event event = raceShirt.getRace().getEvent();
 
 		return getLocalizedShirtName(event, size, language);
 	}
 
 	public AdvancedProperty getLocalizedShirtName(Event event, ShirtSize size,
 			String language) {
 		IWResourceBundle iwrb = IWMainApplication.getDefaultIWMainApplication()
 				.getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 				.getResourceBundle(LocaleUtil.getLocale(language));
 
 		return new AdvancedProperty(String.valueOf(size.getId()),
 				PheidippidesUtil.escapeXML(iwrb.getLocalizedString(
 						event.getLocalizedKey() + "." + size.getLocalizedKey(),
 						size.getSize().toString() + " - "
 								+ size.getGender().toString())));
 	}
 
 	public RegistrationHeader markRegistrationAsPaymentCancelled(String uniqueID) {
 		RegistrationHeader header = dao.getRegistrationHeader(uniqueID);
 
 		return markRegistrationAsPaymentCancelled(header);
 	}
 
 	public RegistrationHeader markRegistrationAsPaymentCancelled(
 			RegistrationHeader header) {
 		List<Registration> registrations = dao.getRegistrations(header);
 		header = dao.storeRegistrationHeader(header.getId(),
 				RegistrationHeaderStatus.UserDidntFinishPayment, null, null,
 				null, null, null, null, null, null, null, null, null, null,
 				null, null);
 		for (Registration registration : registrations) {
 			if (registration.getStatus().equals(RegistrationStatus.Unconfirmed)) {
 				dao.storeRegistration(registration.getId(), header,
 						RegistrationStatus.Cancelled, null, null, null, null,
 						0, null, null, null, 0,
 						registration.isHasDoneMarathonBefore(),
 						registration.isHasDoneLVBefore(), null, null);
 			}
 		}
 
 		return header;
 	}
 
 	public RegistrationHeader markRegistrationAsPaid(String uniqueID,
 			boolean manualPayment, boolean withoutPayment,
 			String securityString, String cardType, String cardNumber,
 			String paymentDate, String authorizationNumber,
 			String transactionNumber, String referenceNumber, String comment,
 			String saleId) {
 		RegistrationHeader header = dao.getRegistrationHeader(uniqueID);
 
 		return markRegistrationAsPaid(header, manualPayment, withoutPayment,
 				securityString, cardType, cardNumber, paymentDate,
 				authorizationNumber, transactionNumber, referenceNumber,
 				comment, saleId);
 	}
 
 	public RegistrationHeader markRegistrationAsPaid(RegistrationHeader header,
 			boolean manualPayment, boolean withoutPayment,
 			String securityString, String cardType, String cardNumber,
 			String paymentDate, String authorizationNumber,
 			String transactionNumber, String referenceNumber, String comment,
 			String saleId) {
 		List<Registration> registrations = dao.getRegistrations(header);
 		dao.storeRegistrationHeader(
 				header.getId(),
 				withoutPayment ? RegistrationHeaderStatus.RegisteredWithoutPayment
 						: (manualPayment ? RegistrationHeaderStatus.ManualPayment
 								: RegistrationHeaderStatus.Paid), null, null,
 				null, null, securityString, cardType, cardNumber, paymentDate,
 				authorizationNumber, transactionNumber, referenceNumber,
 				comment, saleId, null);
 		for (Registration registration : registrations) {
 			if (registration.getStatus().equals(RegistrationStatus.Unconfirmed)) {
 				registration = dao.storeRegistration(registration.getId(),
 						header, RegistrationStatus.OK, null, null, null, null,
 						0, null, null, null, 0,
 						registration.isHasDoneMarathonBefore(),
 						registration.isHasDoneLVBefore(), null, null);
 				try {
 					User user = getUserBusiness().getUserByUniqueId(
 							registration.getUserUUID());
 					String userNameString = "";
 					String passwordString = "";
 					if (getUserBusiness().hasUserLogin(user)) {
 						try {
 							LoginTable login = LoginDBHandler
 									.getUserLogin(user);
 							userNameString = login.getUserLogin();
 							passwordString = LoginDBHandler
 									.getGeneratedPasswordForUser();
 							LoginDBHandler
 									.changePassword(login, passwordString);
 						} catch (Exception e) {
 							System.out
 									.println("Error re-generating password for user: "
 											+ user.getName());
 							e.printStackTrace();
 						}
 					} else {
 						try {
 							LoginTable login = getUserBusiness()
 									.generateUserLogin(user);
 							userNameString = login.getUserLogin();
 							passwordString = login.getUnencryptedUserPassword();
 						} catch (Exception e) {
 							System.out
 									.println("Error creating login for user: "
 											+ user.getName());
 							e.printStackTrace();
 						}
 					}
 
 					addUserToRootRunnersGroup(user);
 
 					Email email = getUserBusiness().getUserMail(user);
 					Locale locale = LocaleUtil.getLocale(header.getLocale());
 					IWResourceBundle iwrb = IWMainApplication
 							.getDefaultIWMainApplication()
 							.getBundle(
 									PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 							.getResourceBundle(locale);
 					Object[] args = {
 							user.getName(),
 							user.getPersonalID() != null ? user.getPersonalID()
 									: "",
 							new IWTimestamp(user.getDateOfBirth())
 									.getDateString("dd.MM.yyyy"),
 							getLocalizedShirtName(
 									registration.getRace().getEvent(),
 									registration.getShirtSize(),
 									header.getLocale()).getValue(),
 							getLocalizedRaceName(registration.getRace(),
 									header.getLocale()).getValue(),
 							userNameString, passwordString };
 					String subject = PheidippidesUtil.escapeXML(iwrb
 							.getLocalizedString(registration.getRace()
 									.getEvent().getLocalizedKey()
 									+ "."
 									+ "registration_received_subject_mail",
 									"Your registration has been received."));
 					String body = MessageFormat.format(StringEscapeUtils
 							.unescapeHtml(iwrb.getLocalizedString(registration
 									.getRace().getEvent().getLocalizedKey()
 									+ "." + "registration_received_body_mail",
 									"Your registration has been received.")),
 							args);
 
 					body = body.replaceAll("<p>", "")
 							.replaceAll("<strong>", "")
 							.replaceAll("</strong>", "");
 					body = body.replaceAll("</p>", "\r\n");
 					body = body.replaceAll("<br />", "\r\n");
 
 					sendMessage(email.getEmailAddress(), subject, body);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				} catch (FinderException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return header;
 	}
 
 	public void sendPaymentTransferEmail(ParticipantHolder holder,
 			RegistrationAnswerHolder answer, Locale locale) {
 		try {
 			User user = getUserBusiness().getUserByUniqueId(
 					holder.getParticipant().getUuid());
 			Email email = getUserBusiness().getUserMail(user);
 
 			IWResourceBundle iwrb = IWMainApplication
 					.getDefaultIWMainApplication()
 					.getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 					.getResourceBundle(locale);
 			Object[] args = { String.valueOf(answer.getAmount()),
 					answer.getBankReference().getReferenceNumber() };
 			String subject = PheidippidesUtil.escapeXML(iwrb
 					.getLocalizedString(holder.getRace().getEvent()
 							.getLocalizedKey()
 							+ "." + "receipt_subject",
 							"Your registration has been received."));
 			String body = MessageFormat.format(StringEscapeUtils
 					.unescapeHtml(iwrb.getLocalizedString(holder.getRace()
 							.getEvent().getLocalizedKey()
 							+ "." + "receipt_body",
 							"Your registration has been received.")), args);
 
 			body = body.replaceAll("<p>", "").replaceAll("<strong>", "")
 					.replaceAll("</strong>", "");
 			body = body.replaceAll("</p>", "\r\n");
 			body = body.replaceAll("<br />", "\r\n");
 
 			sendMessage(email.getEmailAddress(), subject, body);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (FinderException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void generateNewPassword(Participant participant, Locale locale) {
 		try {
 			User user = getUserBusiness().getUserByUniqueId(
 					participant.getUuid());
 
 			String password = LoginDBHandler.getGeneratedPasswordForUser();
 			LoginTable loginTable = LoginDBHandler.getUserLogin(((Integer) user
 					.getPrimaryKey()).intValue());
 			LoginDBHandler.changePassword(loginTable, password);
 
 			IWResourceBundle iwrb = IWMainApplication
 					.getDefaultIWMainApplication()
 					.getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 					.getResourceBundle(locale);
 
 			String subject = iwrb.getLocalizedString("new_password.subject",
 					"A new password for your account");
 
 			Object[] arguments = { loginTable.getUserLogin(), password };
 			String body = MessageFormat
 					.format(iwrb
 							.getLocalizedString(
 									"new_password.body",
 									"A new password has been created for your account:\n\nLogin: {0}\nPassword:{1}\n\nBest regards,\nReykjavik Marathon"),
 							arguments);
 
 			sendMessage(participant.getEmail(), subject, body);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void addUserToRootRunnersGroup(User user) throws RemoteException {
 		try {
 			Group runners = getRootRunnersGroup();
 			if (!getUserBusiness().isMemberOfGroup(
 					((Integer) runners.getPrimaryKey()).intValue(), user)) {
 				runners.addGroup(user, IWTimestamp.getTimestampRightNow());
 				if (user.getPrimaryGroup() == null) {
 					user.setPrimaryGroup(runners);
 					user.store();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Group getRootRunnersGroup() throws CreateException,
 			FinderException, RemoteException {
 		return getGroupCreateIfNecessaryStoreAsApplicationBinding(
 				"root.runners.group", "Runners",
 				"The root group for all runners in Pheidippides");
 	}
 
 	private Group getGroupCreateIfNecessaryStoreAsApplicationBinding(
 			String parameter, String createName, String createDescription)
 			throws RemoteException, FinderException, CreateException {
 		IWMainApplicationSettings settings = IWMainApplication
 				.getDefaultIWMainApplication().getSettings();
 		String groupId = settings.getProperty(parameter);
 
 		Group group = null;
 		if (groupId != null) {
 			group = getUserBusiness().getGroupBusiness().getGroupByGroupID(
 					new Integer(groupId));
 		} else {
 			System.err.println("Trying to store " + createName + " group");
 			group = getUserBusiness().getGroupBusiness().createGroup(
 					createName, createDescription);
 
 			groupId = group.getPrimaryKey().toString();
 			settings.setProperty(parameter, groupId);
 		}
 
 		return group;
 	}
 
 	private void sendMessage(String email, String subject, String body) {
 		String mailServer = DEFAULT_SMTP_MAILSERVER;
 		String fromAddress = DEFAULT_MESSAGEBOX_FROM_ADDRESS;
 		String cc = DEFAULT_CC_ADDRESS;
 		try {
 			MessagingSettings messagingSetting = IWMainApplication
 					.getDefaultIWMainApplication().getMessagingSettings();
 			mailServer = messagingSetting.getSMTPMailServer();
 			fromAddress = messagingSetting.getFromMailAddress();
 			cc = IWMainApplication.getDefaultIWMainApplication().getSettings()
 					.getProperty("messagebox_cc_receiver_address", "");
 		} catch (Exception e) {
 			System.err
 					.println("MessageBusinessBean: Error getting mail property from bundle");
 			e.printStackTrace();
 		}
 
 		try {
 			com.idega.util.SendMail.send(fromAddress, email.trim(), cc, "",
 					mailServer, subject, body);
 		} catch (javax.mail.MessagingException me) {
 			System.err
 					.println("MessagingException when sending mail to address: "
 							+ email + " Message was: " + me.getMessage());
 		} catch (Exception e) {
 			System.err.println("Exception when sending mail to address: "
 					+ email + " Message was: " + e.getMessage());
 		}
 	}
 
 	public RegistrationHeader getRegistrationHeader(String uniqueID) {
 		return dao.getRegistrationHeader(uniqueID);
 	}
 
 	public List<Participant> searchForParticipants(SearchParameter parameter) {
 		Set<Participant> returnSet = new HashSet<Participant>();
 		
 		boolean doneOneParameter = false;
 		
 		if (parameter.getPersonalId() != null) {
 			try {
 				User user = getUserBusiness()
 						.getUser(parameter.getPersonalId());
 				returnSet.add(getParticipant(user));
 				doneOneParameter = true;
 			} catch (RemoteException e) {
 			} catch (FinderException e) {
 			}
 		}
 
 		if (parameter.getDateOfBirth() != null) {
 			Name name = new Name();
 			if (parameter.getFirstName() != null
 					|| parameter.getMiddleName() != null
 					|| parameter.getLastName() != null) {
 				name = new Name(parameter.getFirstName(),
 						parameter.getMiddleName(), parameter.getLastName());
 			}
 
 			if (parameter.getFullName() != null) {
 				name = new Name(parameter.getFullName());
 			}
 
 			try {
 				Collection<User> col = getUserBusiness()
 						.getUserHome()
 						.findByDateOfBirth(
 								new IWTimestamp(parameter.getDateOfBirth())
 										.getDate());
 				
 				if (col != null && !col.isEmpty()) {
 					Set<Participant> tmp = new HashSet<Participant>();
 					Iterator<User> it = col.iterator();
 					while (it.hasNext()) {
 						tmp.add(getParticipant(it.next()));
 					}
 
 					if (returnSet.isEmpty()) {
 						if (!doneOneParameter) {
 							returnSet.addAll(tmp);
 						}
 					} else {
 						if (parameter.isMustFulfillAllParameters()) {
 							returnSet.retainAll(tmp);
 						} else {
 							returnSet.addAll(tmp);
 						}
 					}
 				}
 				
 				doneOneParameter = true;
 			} catch (RemoteException e) {
 			} catch (FinderException e) {
 			}
 		}
 
 		if (parameter.getFirstName() != null
 				|| parameter.getMiddleName() != null
 				|| parameter.getLastName() != null) {
 			try {
 				Collection<User> col = getUserBusiness().getUserHome()
 						.findByNames(parameter.getFirstName(),
 								parameter.getMiddleName(),
 								parameter.getLastName());
 				if (col != null && !col.isEmpty()) {
 					Set<Participant> tmp = new HashSet<Participant>();
 					Iterator<User> it = col.iterator();
 					while (it.hasNext()) {
 						tmp.add(getParticipant(it.next()));
 					}
 
 					if (returnSet.isEmpty()) {
 						if (!doneOneParameter) {
 							returnSet.addAll(tmp);
 						}
 					} else {
 						if (parameter.isMustFulfillAllParameters()) {
 							returnSet.retainAll(tmp);
 						} else {
 							returnSet.addAll(tmp);
 						}
 					}
 				}
 				
 				doneOneParameter = true;
 			} catch (RemoteException e) {
 			} catch (FinderException e) {
 			}
 		}
 
 		if (parameter.getFullName() != null) {
 			Name name = new Name(parameter.getFullName());
 			try {
 				Collection<User> col = getUserBusiness().getUserHome()
 						.findByNames(name.getFirstName(), name.getMiddleName(),
 								name.getLastName());
 				if (col != null && !col.isEmpty()) {
 					Set<Participant> tmp = new HashSet<Participant>();
 					Iterator<User> it = col.iterator();
 					while (it.hasNext()) {
 						tmp.add(getParticipant(it.next()));
 					}
 
 					if (returnSet.isEmpty()) {
 						if (!doneOneParameter) {
 							returnSet.addAll(tmp);
 						}
 					} else {
 						if (parameter.isMustFulfillAllParameters()) {
 							returnSet.retainAll(tmp);
 						} else {
 							returnSet.addAll(tmp);
 						}
 					}
 				}
 				
 				doneOneParameter = true;
 			} catch (RemoteException e) {
 			} catch (FinderException e) {
 			}
 		}
 
 		if (parameter.getEmail() != null) {
 			try {
 				Collection<User> users = getUserBusiness().getUserHome().findUsersByEmail(parameter.getEmail());
 				if (users != null && !users.isEmpty()) {
 					Set<Participant> tmp = new HashSet<Participant>();
 					Iterator<User> it = users.iterator();
 					while (it.hasNext()) {
 						tmp.add(getParticipant(it.next()));
 					}
 
 					if (returnSet.isEmpty()) {
 						if (!doneOneParameter) {
 							returnSet.addAll(tmp);
 						}
 					} else {
 						if (parameter.isMustFulfillAllParameters()) {
 							returnSet.retainAll(tmp);
 						} else {
 							returnSet.addAll(tmp);
 						}
 					}
 				}
 				
 				doneOneParameter = true;
 			} catch (RemoteException e) {
 			} catch (FinderException e) {
 			}
 		}
 
		// @TODO Add search address..

 		List<Participant> ret = new ArrayList<Participant>();
 		for (Participant participant : returnSet) {
 			ret.add(participant);
 		}
 
 		return ret;
 	}
 
 	public Registration cancelRegistration(Registration registration) {
 		registration = dao.storeRegistration(registration.getId(), null,
 				RegistrationStatus.Cancelled, null, null, null, null, 0, null,
 				null, null, 0, registration.isHasDoneMarathonBefore(),
 				registration.isHasDoneLVBefore(), null, null);
 
 		RegistrationHeader header = registration.getHeader();
 		boolean cancelHeader = true;
 
 		List<Registration> registrations = dao.getRegistrations(header);
 		for (Registration registration2 : registrations) {
 			if (registration2.getStatus() == RegistrationStatus.Unconfirmed
 					|| registration2.getStatus() == RegistrationStatus.OK) { // Should
 																				// RelayPartner
 																				// also
 																				// stop
 																				// us
 																				// from
 																				// cancelling
 																				// the
 																				// header?
 				cancelHeader = false;
 				break;
 			}
 		}
 
 		if (cancelHeader) {
 			dao.storeRegistrationHeader(header.getId(),
 					RegistrationHeaderStatus.Cancelled, null, null, null, null,
 					null, null, null, null, null, null, null, null, null,
 					header.getCompany());
 		}
 
 		return registration;
 	}
 
 	public Registration deregister(Registration registration) {
 		registration = dao.storeRegistration(registration.getId(), null,
 				RegistrationStatus.Deregistered, null, null, null, null, 0,
 				null, null, null, 0, registration.isHasDoneMarathonBefore(),
 				registration.isHasDoneLVBefore(), null, null);
 
 		RegistrationHeader header = registration.getHeader();
 		boolean cancelHeader = true;
 
 		List<Registration> registrations = dao.getRegistrations(header);
 		for (Registration registration2 : registrations) {
 			if (registration2.getStatus() == RegistrationStatus.Unconfirmed
 					|| registration2.getStatus() == RegistrationStatus.OK) { // Should
 																				// RelayPartner
 																				// also
 																				// stop
 																				// us
 																				// from
 																				// cancelling
 																				// the
 																				// header?
 				cancelHeader = false;
 				break;
 			}
 		}
 
 		if (cancelHeader) {
 			dao.storeRegistrationHeader(header.getId(),
 					RegistrationHeaderStatus.Cancelled, null, null, null, null,
 					null, null, null, null, null, null, null, null, null,
 					header.getCompany());
 		}
 
 		return registration;
 	}
 
 	public List<Registration> getRelayPartners(Registration registration) {
 		return dao.getRegistrations(registration.getTeam(),
 				RegistrationStatus.RelayPartner);
 	}
 
 	public List<Registration> getOtherTeamMembers(Registration registration) {
 		List<Registration> registrations = dao.getRegistrations(
 				registration.getTeam(), RegistrationStatus.OK);
 		registrations.remove(registration);
 
 		return registrations;
 	}
 	
 	public void updateRelayTeam(Registration registration, String relayLeg, String teamName, List<Participant> relayPartners) {
 		dao.updateRegistrationStatus(registration.getId(), relayLeg, registration.getStatus());
 		
 		List<Registration> relayPartnerRegistrations = getRelayPartners(registration);
 		List<Participant> participants = new ArrayList<Participant>();
 		
 		for (Registration relayRegistration : relayPartnerRegistrations) {
 			Participant participant = getParticipant(relayRegistration);
 			if (relayPartners.contains(participant)) {
 				participants.add(participant);
 				Participant relayPartner = null;
 				for (Participant participant2 : relayPartners) {
 					if (participant2.equals(participant)) {
 						relayPartner = participant2;
 					}
 				}
 				
 				try {
 					User user = getUserBusiness().getUserByUniqueId(participant.getUuid());
 					if (participant.getPersonalId() == null || participant.getPersonalId().length() == 0) {
 						user.setFullName(relayPartner.getFullName());
 					}
 					getUserBusiness().updateUserMail(user, relayPartner.getEmail());
 					dao.updateRegistrationStatus(relayRegistration.getId(), relayPartner.getRelayLeg(), RegistrationStatus.RelayPartner);
 				}
 				catch (RemoteException re) {
 					throw new IBORuntimeException(re);
 				}
 				catch (FinderException fe) {
 					fe.printStackTrace();
 				}
 				catch (CreateException ce) {
 					ce.printStackTrace();
 				}
 			}
 			else {
 				dao.updateRegistrationStatus(relayRegistration.getId(), null, RegistrationStatus.Deregistered);
 			}
 		}
 		
 		relayPartners.removeAll(participants);
 		
 		try {
 			for (Participant participant : relayPartners) {
 				User user = null;
 				if (participant.getPersonalId() != null && participant.getPersonalId().length() > 0) {
 					try {
 						user = getUserBusiness().getUser(participant.getPersonalId());
 					}
 					catch (RemoteException re) {
 						throw new IBORuntimeException(re);
 					}
 					catch (FinderException fe) {
 						fe.printStackTrace();
 					}
 				}
 				else {
 					SearchParameter parameter = new SearchParameter();
 					parameter.setFullName(participant.getFullName());
 					parameter.setDateOfBirth(participant.getDateOfBirth());
 					
 					List<Participant> searchResults = searchForParticipants(parameter);
 					if (searchResults != null && !searchResults.isEmpty()) {
 						try {
 							user = getUserBusiness().getUserByUniqueId(searchResults.iterator().next().getUuid());
 						}
 						catch (FinderException fe) {
 							fe.printStackTrace();
 						}
 					}
 					else {
 						user = saveUser(new Name(participant.getFullName()), new IWTimestamp(participant.getDateOfBirth()), null, null, null, null, null);					
 					}
 				}
 	
 				if (user != null) {
 					getUserBusiness().updateUserMail(user, participant.getEmail());
 					dao.storeRegistration(null, registration.getHeader(), RegistrationStatus.RelayPartner, registration.getRace(), participant.getShirtSize(), registration.getTeam(), participant.getRelayLeg(), 0, null, participant.getNationality(), user.getUniqueId(), 0, false, false, null, null);
 				}
 			}
 		}
 		catch (RemoteException re) {
 			throw new IBORuntimeException(re);
 		}
 		catch (CreateException ce) {
 			ce.printStackTrace();
 		}
 	}
 
 	public void storeCompanyRegistration(List<ParticipantHolder> holders,
 			Company company, String registrantUUID, Locale locale) {
 
 		if (holders != null && !holders.isEmpty()) {
 			RegistrationHeader header = dao.storeRegistrationHeader(null,
 					RegistrationHeaderStatus.RegisteredWithoutPayment,
 					registrantUUID, company.getName(), locale.toString(),
 					Currency.ISK, null, null, null, null, null, null, null,
 					null, null, company);
 
 			for (ParticipantHolder participantHolder : holders) {
 				try {
 					User user = null;
 					Participant participant = participantHolder
 							.getParticipant();
 					if (participant.getUuid() != null) {
 						try {
 							user = getUserBusiness().getUserByUniqueId(
 									participant.getUuid());
 						} catch (RemoteException e) {
 						} catch (FinderException e) {
 						}
 					}
 
 					try {
 						if (user == null) {
 							Gender gender = null;
 							if (participant.getGender().equals("Male")) {
 								gender = getGenderHome().getMaleGender();
 							} else {
 								gender = getGenderHome().getFemaleGender();
 							}
 							user = saveUser(
 									new Name(participant.getFullName()),
 									new IWTimestamp(participant
 											.getDateOfBirth()),
 									gender,
 									participant.getAddress(),
 									participant.getPostalCode(),
 									participant.getCity(),
 									getCountryHome().findByCountryName(participant
 													.getCountry()));
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 						user = null; // Something got fucked up
 					}
 
 					if (user != null) {
 						if (participant.getPhoneMobile() != null
 								&& !"".equals(participant.getPhoneMobile())) {
 							try {
 								getUserBusiness().updateUserMobilePhone(user,
 										participant.getPhoneMobile());
 							} catch (Exception e) {
 							}
 						}
 
 						if (participant.getPhoneHome() != null
 								&& !"".equals(participant.getPhoneHome())) {
 							try {
 								getUserBusiness().updateUserHomePhone(user,
 										participant.getPhoneHome());
 							} catch (Exception e) {
 							}
 						}
 
 						if (participant.getEmail() != null
 								&& !"".equals(participant.getEmail())) {
 							try {
 								getUserBusiness().updateUserMail(user,
 										participant.getEmail());
 							} catch (Exception e) {
 							}
 						}
 
 						Country country = null;
 						try {
 							country = getCountryHome().findByCountryName(participant.getNationality());
 						} catch(Exception e) {
 							country = getCountryHome().findByIsoAbbreviation(LocaleUtil.getIcelandicLocale().getCountry());
 						}
 						
 						Registration registration = dao.storeRegistration(null,
 								header, RegistrationStatus.OK,
 								participantHolder.getRace(),
 								participantHolder.getShirtSize(), null,
 								null,
 								0,
 								null,
 								country.getPrimaryKey().toString(),
 								user.getUniqueId(),
 								0,
 								false,
 								false,
 								null,
 								null);
 
 						String userNameString = "";
 						String passwordString = "";
 						if (getUserBusiness().hasUserLogin(user)) {
 							try {
 								LoginTable login = LoginDBHandler
 										.getUserLogin(user);
 								userNameString = login.getUserLogin();
 								passwordString = LoginDBHandler
 										.getGeneratedPasswordForUser();
 								LoginDBHandler.changePassword(login,
 										passwordString);
 							} catch (Exception e) {
 								System.out
 										.println("Error re-generating password for user: "
 												+ user.getName());
 								e.printStackTrace();
 							}
 						} else {
 							try {
 								LoginTable login = getUserBusiness()
 										.generateUserLogin(user);
 								userNameString = login.getUserLogin();
 								passwordString = login
 										.getUnencryptedUserPassword();
 							} catch (Exception e) {
 								System.out
 										.println("Error creating login for user: "
 												+ user.getName());
 								e.printStackTrace();
 							}
 						}
 
 						addUserToRootRunnersGroup(user);
 
 						Email email = getUserBusiness().getUserMail(user);
 						IWResourceBundle iwrb = IWMainApplication
 								.getDefaultIWMainApplication()
 								.getBundle(
 										PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 								.getResourceBundle(locale);
 						Object[] args = {
 								user.getName(),
 								user.getPersonalID() != null ? user
 										.getPersonalID() : "",
 								new IWTimestamp(user.getDateOfBirth())
 										.getDateString("dd.MM.yyyy"),
 								getLocalizedShirtName(
 										registration.getRace().getEvent(),
 										registration.getShirtSize(),
 										header.getLocale()).getValue(),
 								getLocalizedRaceName(registration.getRace(),
 										header.getLocale()).getValue(),
 								userNameString, passwordString };
 						String subject = PheidippidesUtil
 								.escapeXML(iwrb.getLocalizedString(registration
 										.getRace().getEvent().getLocalizedKey()
 										+ "."
 										+ "registration_received_subject_mail",
 										"Your registration has been received."));
 						String body = MessageFormat
 								.format(StringEscapeUtils
 										.unescapeHtml(iwrb
 												.getLocalizedString(
 														registration
 																.getRace()
 																.getEvent()
 																.getLocalizedKey()
 																+ "."
 																+ "registration_received_body_mail",
 														"Your registration has been received.")),
 										args);
 
 						body = body.replaceAll("<p>", "")
 								.replaceAll("<strong>", "")
 								.replaceAll("</strong>", "");
 						body = body.replaceAll("</p>", "\r\n");
 						body = body.replaceAll("<br />", "\r\n");
 
 						sendMessage(email.getEmailAddress(), subject, body);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 }
