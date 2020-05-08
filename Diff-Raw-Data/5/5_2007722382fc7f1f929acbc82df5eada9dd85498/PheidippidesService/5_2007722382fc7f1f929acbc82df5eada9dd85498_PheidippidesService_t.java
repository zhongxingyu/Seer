 package is.idega.idegaweb.pheidippides.business;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.RegistrationAnswerHolder;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.BankReference;
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
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.rmi.RemoteException;
 import java.security.MessageDigest;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.ejb.CreateException;
 import javax.ejb.EJBException;
 import javax.ejb.FinderException;
 
 import org.apache.commons.lang.StringEscapeUtils;
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
 		List<Race> races = getRaces(eventPK, year);
 		List<Race> openRaces = new ArrayList<Race>();
 
 		IWTimestamp stamp = new IWTimestamp();
 		for (Race race : races) {
 			if (stamp.isBetween(
 					new IWTimestamp(race.getOpenRegistrationDate()),
 					new IWTimestamp(race.getCloseRegistrationDate()))) {
 				openRaces.add(race);
 			}
 		}
 
 		return openRaces;
 	}
 
 	public List<Race> getAvailableRaces(Long eventPK, int year, Date dateOfBirth) {
 		List<Race> races = getOpenRaces(eventPK, year);
 		List<Race> availableRaces = new ArrayList<Race>();
 
 		Age age = new Age(dateOfBirth);
 		for (Race race : races) {
 			if (race.getMinimumAge() <= age.getYears()
 					&& race.getMaximumAge() >= age.getYears()) {
 				availableRaces.add(race);
 			}
 		}
 
 		return availableRaces;
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
 
 	public boolean isValidPersonalID(String personalID) {
 		if (personalID != null && personalID.length() == 10) {
 			return getParticipant(personalID) != null;
 		}
 		return true;
 	}
 
 	private Participant getParticipant(User user) {
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
 
 		try {
 			Address address = user.getUsersMainAddress();
 			if (address != null) {
 				p.setAddress(address.getStreetAddress());
 				p.setPostalAddress(address.getPostalAddress());
 				p.setPostalCode(address.getPostalCode().getPostalCode());
 				p.setCity(address.getCity());
 				p.setCountry(address.getCountry().getName());
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
 
 		return p;
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
 						null, null, null, null, null, null, null);
 			} else {
 				header = dao.storeRegistrationHeader(null,
 						RegistrationHeaderStatus.RegisteredWithoutPayment,
 						registrantUUID, paymentGroup, locale.toString(),
 						createUsers ? Currency.EUR : Currency.ISK, null, null,
 						null, null, null, null, null, null, null);
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
 							Name fullName = new Name(
 									participant.getFirstName(),
 									participant.getMiddleName(),
 									participant.getLastName());
 							user = saveUser(
 									fullName,
 									new IWTimestamp(participant
 											.getDateOfBirth()),
 									gender,
 									participant.getAddress(),
 									participant.getPostalCode(),
 									participant.getCity(),
 									getCountryHome().findByPrimaryKey(
 											new Integer(participant.getCountry())));
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
 						team = dao.storeTeam(team.getId(), team.getName(), team.isRelayTeam());
 					}
 
 					dao.storeRegistration(null, header,
 							RegistrationStatus.Unconfirmed,
 							participantHolder.getRace(),
 							participantHolder.getShirtSize(), team,
 							participantHolder.getLeg(),
 							participantHolder.getAmount(),
 							participantHolder.getCharity(),
 							participant.getNationality(),
 							participant.getUuid(),
 							participantHolder.getDiscount());
 
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
 										user = getUserBusiness()
 												.getUser(
 														participant2.getPersonalId());
 									} catch (RemoteException e) {
 									} catch (FinderException e) {
 									}
 								}
 
 								try {
 									if (user == null) {
 										user = saveUser(
 												new Name(participant2.getFullName()),
 												new IWTimestamp(participant2
 														.getDateOfBirth()),
 												null,
 												null,
 												null,
 												null,
 												null);
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
 										participantHolder.getShirtSize(), team,
 										participantHolder.getLeg(),
 										participantHolder.getAmount(),
 										participantHolder.getCharity(),
 										participant.getNationality(),
 										participant.getUuid(),
 										participantHolder.getDiscount());
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
 			String language) {
 		List<AdvancedProperty> properties = new ArrayList<AdvancedProperty>();
 
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
 				null);
 		for (Registration registration : registrations) {
 			if (registration.getStatus().equals(RegistrationStatus.Unconfirmed)) {
 				dao.storeRegistration(registration.getId(), header,
 						RegistrationStatus.Cancelled, null, null, null, null,
 						0, null, null, null, 0);
 			}
 		}
 
 		return header;
 	}
 
 	public RegistrationHeader markRegistrationAsPaid(String uniqueID,
 			boolean manualPayment, String securityString, String cardType,
 			String cardNumber, String paymentDate, String authorizationNumber,
 			String transactionNumber, String referenceNumber, String comment,
 			String saleId) {
 		RegistrationHeader header = dao.getRegistrationHeader(uniqueID);
 
 		return markRegistrationAsPaid(header, manualPayment, securityString,
 				cardType, cardNumber, paymentDate, authorizationNumber,
 				transactionNumber, referenceNumber, comment, saleId);
 	}
 
 	public RegistrationHeader markRegistrationAsPaid(RegistrationHeader header,
 			boolean manualPayment, String securityString, String cardType,
 			String cardNumber, String paymentDate, String authorizationNumber,
 			String transactionNumber, String referenceNumber, String comment,
 			String saleId) {
 		List<Registration> registrations = dao.getRegistrations(header);
 		dao.storeRegistrationHeader(header.getId(),
 				manualPayment ? RegistrationHeaderStatus.ManualPayment
 						: RegistrationHeaderStatus.Paid, null, null, null,
 				null, securityString, cardType, cardNumber, paymentDate,
 				authorizationNumber, transactionNumber, referenceNumber,
 				comment, saleId);
 		for (Registration registration : registrations) {
 			if (registration.getStatus().equals(RegistrationStatus.Unconfirmed)) {
 				registration = dao.storeRegistration(registration.getId(),
 						header, RegistrationStatus.OK, null, null, null, null,
 						0, null, null, null, 0);
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
 					String subject = PheidippidesUtil.escapeXML(iwrb.getLocalizedString(registration
 							.getRace().getEvent().getLocalizedKey()
 							+ "."
 							+ "registration_received_subject_mail",
 							"Your registration has been received."));
 					String body = MessageFormat.format(StringEscapeUtils.unescapeHtml(iwrb.getLocalizedString(
 							registration.getRace().getEvent().getLocalizedKey() + "."
 									+ "registration_received_body_mail",
 							"Your registration has been received.")), args);
 					
 					body = body.replaceAll("<p>", "").replaceAll("<strong>", "").replaceAll("</strong>", "");
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
 	
 	public void sendPaymentTransferEmail(ParticipantHolder holder, RegistrationAnswerHolder answer, Locale locale) {
 		try {
 			User user = getUserBusiness().getUserByUniqueId(holder.getParticipant().getUuid());
 			Email email = getUserBusiness().getUserMail(user);
 			
 			IWResourceBundle iwrb = IWMainApplication
 					.getDefaultIWMainApplication()
 					.getBundle(
 							PheidippidesConstants.IW_BUNDLE_IDENTIFIER)
 					.getResourceBundle(locale);
 			Object[] args = { String.valueOf(answer.getAmount()), answer.getBankReference().getReferenceNumber() };
 			String subject = PheidippidesUtil.escapeXML(iwrb.getLocalizedString(holder
 					.getRace().getEvent().getLocalizedKey()
 					+ "."
 					+ "receipt_subject",
 					"Your registration has been received."));
 			String body = MessageFormat.format(StringEscapeUtils.unescapeHtml(iwrb.getLocalizedString(
 					holder.getRace().getEvent().getLocalizedKey() + "."
 							+ "receipt_body",
 					"Your registration has been received.")), args);
 			
 			body = body.replaceAll("<p>", "").replaceAll("<strong>", "").replaceAll("</strong>", "");
			body = body.replaceAll("</p>", "\r\n");
 			body = body.replaceAll("<br />", "\r\n");
 			
 			sendMessage(email.getEmailAddress(), subject, body);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (FinderException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void addUserToRootRunnersGroup(User user) throws RemoteException {
 		try {
 			Group runners = getRootRunnersGroup();
 			if (!getUserBusiness().isMemberOfGroup(((Integer) runners.getPrimaryKey()).intValue(), user)) {
 				runners.addGroup(user, IWTimestamp.getTimestampRightNow());
 				if (user.getPrimaryGroup() == null) {
 					user.setPrimaryGroup(runners);
 					user.store();
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Group getRootRunnersGroup() throws CreateException, FinderException, RemoteException {
 		return getGroupCreateIfNecessaryStoreAsApplicationBinding("root.runners.group", "Runners", "The root group for all runners in Pheidippides");
 	}
 
 	private Group getGroupCreateIfNecessaryStoreAsApplicationBinding(String parameter, String createName, String createDescription) throws RemoteException, FinderException, CreateException {
 		IWMainApplicationSettings settings = IWMainApplication.getDefaultIWMainApplication().getSettings();
 		String groupId = settings.getProperty(parameter);
 
 		Group group = null;
 		if (groupId != null) {
 			group = getUserBusiness().getGroupBusiness().getGroupByGroupID(new Integer(groupId));
 		}
 		else {
 			System.err.println("Trying to store " + createName + " group");
 			group = getUserBusiness().getGroupBusiness().createGroup(createName, createDescription);
 
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
 }
