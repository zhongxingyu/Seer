 package com.natepaulus.dailyemail.web.service.impl;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.mail.internet.MimeMessage;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
 import com.luckycatlabs.sunrisesunset.dto.Location;
 import com.natepaulus.dailyemail.repository.DeliveryScheduleRepository;
 import com.natepaulus.dailyemail.repository.RssFeedsRepository;
 import com.natepaulus.dailyemail.repository.RssNewsLinksRepository;
 import com.natepaulus.dailyemail.repository.UserRepository;
 import com.natepaulus.dailyemail.repository.entity.DeliverySchedule;
 import com.natepaulus.dailyemail.repository.entity.RssFeeds;
 import com.natepaulus.dailyemail.repository.entity.RssNewsLinks;
 import com.natepaulus.dailyemail.repository.entity.User;
 import com.natepaulus.dailyemail.repository.entity.UserRssFeeds;
 import com.natepaulus.dailyemail.web.domain.EmailData;
 import com.natepaulus.dailyemail.web.domain.NewsFeed;
 import com.natepaulus.dailyemail.web.domain.NewsStory;
 import com.natepaulus.dailyemail.web.service.interfaces.EmailService;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 
 /**
  * The Class EmailServiceImpl checks every minute to see if any users have
  * requested their email at that time. If it finds any users it then builds and
  * sends them an email with their requested data.
  */
 @Service
 public class EmailServiceImpl implements EmailService {
 
 	/** The logger. */
 	final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
 
 	/** The delivery schedule repository. */
 	@Resource
 	DeliveryScheduleRepository deliveryScheduleRepository;
 
 	/** The Rss Feeds Repository */
 	@Resource
 	RssFeedsRepository rssFeedsRepository;
 
 	/** The User Rss Feeds Repository */
 	@Resource
 	RssNewsLinksRepository rssNewsLinksRepository;
 	
 	/** The user repository. */
 	@Resource
 	UserRepository userRepository;
 
 	/** The java mail sender for sending email. */
 	@Autowired
 	private JavaMailSender sender;
 
 	/** The velocity engine. */
 	@Autowired
 	private VelocityEngine velocityEngine;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.natepaulus.dailyemail.web.service.interfaces.EmailService#
 	 * retrieveUserListForEmails()
 	 */
 	@Scheduled(cron = "0 0/1 * * * ?")
 	public void retrieveUserListForEmails() {
 		//logger.info("Retrieving List of users to email");
 		int dayOfWeek = -1;
 		DateTime currentTime = new DateTime();
 
 		List<DeliverySchedule> allDeliverySchedules = deliveryScheduleRepository
 				.findAll();
 		List<User> users = new ArrayList<User>();
 
 		for (DeliverySchedule d : allDeliverySchedules) {
 			// get current time in user's local date & time
 			DateTime currentLocalTime = currentTime.withSecondOfMinute(0)
 					.withMillisOfSecond(0)					
 					.withZone(DateTimeZone.forID(d.getTz()));
 
 			int currentDayOfWeek = currentLocalTime.getDayOfWeek();
 
 			if (currentDayOfWeek == 7 || currentDayOfWeek == 6) {
 				dayOfWeek = 1;
 			} else {
 				dayOfWeek = 0;
 			}
 
 			LocalTime userSetTime = d.getTime(); // user's set time
 			
 			// convert local time to today's time & date and user's local time &
 			// date
 			DateTime userSetTimeDateTime = userSetTime.toDateTimeToday()					
 					.withZone(DateTimeZone.UTC);
 			DateTime userLocalSetTime = userSetTimeDateTime
 					.withZone(DateTimeZone.forID(d.getTz()));
 			
 			/*logger.info("before IF userLocalSetTime: " + userLocalSetTime.toString());
 			logger.info("before IF currentLocalTime: " + currentLocalTime.toString());*/
 			
 			// check if current time equals user set time for today's date
 			if (userLocalSetTime.equals(currentLocalTime)) {
 				logger.info("User's local set time is equal to current local time.");
 				// if the delivery day (weekend or weekday) equals the delivery
 				// day in the schedule add user to list
 				if (d.getDeliveryDay() == dayOfWeek) {
 					users.add(d.getUser());
 					logger.info("Added user: " + d.getUser().getEmail());
 				}
 			}
 		}
 
 		Iterator<User> userIterator = users.iterator();
 		while (userIterator.hasNext()) {
 			User user = userIterator.next();
 			sendEmail(user);
 		}
 
 	}
 
 	@Scheduled(cron = "0 0/30 * * * ?")
 	public void updateRssFeedLinks() {
 		List<RssFeeds> rssFeeds = rssFeedsRepository.findByDisabled(false);
 		logger.info("Processing rss feeds");
 		for (RssFeeds rssFeed : rssFeeds) {
 
 			Set<RssNewsLinks> rssLinks = new HashSet<RssNewsLinks>();
 
 			try {
 
 				URLConnection connection = new URL(rssFeed.getUrl())
 						.openConnection();
 				connection
 						.setRequestProperty(
 								"User-Agent",
 								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
 				connection.connect();
 				InputStream is = connection.getInputStream();
 				InputSource source = new InputSource(is);
 				SyndFeedInput input = new SyndFeedInput();
 				SyndFeed feed = input.build(source);
 
 				@SuppressWarnings("rawtypes")
 				Iterator iFeed = feed.getEntries().iterator();
 
 				while (iFeed.hasNext()) {
 					RssNewsLinks link = new RssNewsLinks();
 					link.setFeedId(rssFeed.getId());
 					link.setRssFeed(rssFeed);
 					SyndEntry entry = (SyndEntry) iFeed.next();
 					link.setTitle(entry.getTitle());
 					link.setLink(entry.getLink());
 					link.setDescription(entry.getDescription().getValue()
 							.replaceAll("\\<.*?>", ""));
 
 					Date publicationDate = entry.getPublishedDate();
 					if (publicationDate == null) { // feed doesn't have
 													// published date
 						publicationDate = new Date();
 					}
 					DateTime publishedDate = new DateTime(publicationDate);
 					link.setPubDate(publishedDate);
 
 					rssLinks.add(link);
 
 				}
 				Set<RssNewsLinks> currentLinksInFeed = rssFeed
 						.getRssNewsLinks();
 				currentLinksInFeed.addAll(rssLinks);
 				rssFeed.setRssNewsLinks(currentLinksInFeed);
 				rssFeedsRepository.save(rssFeed);
 				logger.info("Saved " + rssFeed.getId());
 
 			} catch (Exception ex) {
 				int rssFeedConnectFailures = rssFeed.getConnectFailures();
 				rssFeedConnectFailures += 1;
 				rssFeed.setConnectFailures(rssFeedConnectFailures);
 				if (rssFeed.getConnectFailures() >= 3) {
 					rssFeed.setDisabled(true);
 					logger.info("The following feed ID was just disabled for too many failed connect attempts: "
 							+ rssFeed.getId());
 					sendErrorEmail("The following feed ID was just disabled for too many failed connect attempts: "
 							+ rssFeed.getId());
 				}
 				logger.error(
 						"There was a parsing issue for: " + rssFeed.getId(), ex);
 			}
 
 		}
 
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.natepaulus.dailyemail.web.service.interfaces.EmailService#generateQuickView(java.lang.String)
 	 */
 	public EmailData generateQuickView(String code){
 		
 		User user = userRepository.findByUrlCode(code);
 		
 		EmailData data = new EmailData();
 		data = getWeatherConditions(data, user);
 		data = getNewsStoriesForEmail(data, user);
 		
 		return data;
 	}
 
 	private void sendErrorEmail(final String errorMessage) {
 
 		MimeMessagePreparator preparator = new MimeMessagePreparator() {
 
 			@Override
 			public void prepare(MimeMessage mimeMessage) throws Exception {
 				MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
 						"UTF-8");
 				message.setTo("nate@natepaulus.com");
 				message.setFrom("dailyemail@natepaulus.com");
 				message.setSubject("Error - Daily News & Weather");
 
 				message.setText(errorMessage, true);
 			}
 		};
 		this.sender.send(preparator);
 		logger.info("Error Message sent!");
 	}
 
 	/**
 	 * Send email to the user with the data they have requested.
 	 * 
 	 * @param user
 	 *            the user
 	 */
 	private void sendEmail(User user) {
 
 		EmailData emailData = new EmailData();
 		emailData.setToAddress(user.getEmail());
 		emailData.setToName(user.getFirstName() + " " + user.getLastName());
 
 		emailData = getWeatherConditions(emailData, user);
 		emailData = getNewsStoriesForEmail(emailData, user);
 		final EmailData data = emailData;
 
 		MimeMessagePreparator preparator = new MimeMessagePreparator() {
 
 			@Override
 			public void prepare(MimeMessage mimeMessage) throws Exception {
 				MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
 						"UTF-8");
 				message.setTo(data.getToAddress());
 				message.setFrom("dailyemail@natepaulus.com");
 				message.setSubject("Daily News & Weather");
 				Map<String, Object> model = new HashMap<String, Object>();
 				model.put("data", data);
 				@SuppressWarnings("deprecation")
 				String messageText = VelocityEngineUtils
 						.mergeTemplateIntoString(velocityEngine, "email.vm",
 								model);
 				message.setText(messageText, true);
 			}
 		};
 
 		this.sender.send(preparator);
 		logger.info("Message sent to: " + data.getToAddress());
 	}
 
 	/**
 	 * Gets the weather conditions from the National Weather Service
 	 * experimental forecast feed.
 	 * 
 	 * @param data
 	 *            the EmailData object to add the weather conditions to
 	 * @param user
 	 *            the user
 	 * @return the weather conditions
 	 */
 	private EmailData getWeatherConditions(EmailData data, User user) {
 
 		try {
 
 			URL curCond_ForecastURL = new URL(
 					"http://forecast.weather.gov/MapClick.php?lat="
 							+ user.getWeather().getLatitude() + "&lon="
 							+ user.getWeather().getLongitude()
 							+ "&unit=0&lg=english&FcstType=dwml");
 			InputStream inCC_Fx = curCond_ForecastURL.openStream();
 			DocumentBuilderFactory factoryCurCond_Fx = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder builderCCFX = factoryCurCond_Fx
 					.newDocumentBuilder();
 			org.w3c.dom.Document docCFX = builderCCFX.parse(inCC_Fx);
 
 			XPathFactory xPathfactoryCCFX = XPathFactory.newInstance();
 			XPath xpathCCFX = xPathfactoryCCFX.newXPath();
 			XPathExpression exprCCFX = null;
 
 			if (user.getWeather().getDeliver_pref() == 1
 					|| user.getWeather().getDeliver_pref() == 3) {
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/time-layout/start-valid-time[@period-name = 'current']/text()");
 				String temp = (String) exprCCFX.evaluate(docCFX,
 						XPathConstants.STRING);
 
 				DateTimeFormatter fmt = DateTimeFormat
 						.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 				DateTime xmlTime = fmt.withOffsetParsed().parseDateTime(temp);
 
 				DateTime localTime = xmlTime;
 
 				logger.info("Localtime: " + localTime.toString());
 				DateTimeFormatter outFmt = DateTimeFormat
 						.forPattern("MMM dd',' yyyy h:mm a");
 				logger.info("Printing Local Time with printer: "
 						+ outFmt.print(xmlTime));
 				data.getWxCurCond().setLatestObDateTime(outFmt.print(xmlTime));
 				data.getWxCurCond().setCityState(
 						user.getWeather().getLocation_name());
 
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/location/area-description/text()");
 				data.getWxCurCond().setWeatherStation(
 						(String) exprCCFX.evaluate(docCFX,
 								XPathConstants.STRING));
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/wind-speed[@type = 'sustained']/value/text()");
 				double mphWindSpeed = 0;
 				boolean isNAWindSpeed = false;
 				try {
 					mphWindSpeed = (Double) exprCCFX.evaluate(docCFX,
 							XPathConstants.NUMBER);
 					mphWindSpeed = 1.15155 * mphWindSpeed;
 				} catch (NumberFormatException e) {
 					isNAWindSpeed = true;
 				}
 				if (isNAWindSpeed) {
 					data.getWxCurCond().setWindSpeed("NA");
 				} else {
 					int wSpeed = (int) Math.round(mphWindSpeed);
 					data.getWxCurCond().setWindSpeed(Integer.toString(wSpeed));
 				}
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/wind-speed[@type = 'gust']/value/text()");
 				double mphWindGust = 0;
 				boolean isNAWindGust = false;
 				try {
 					mphWindGust = (Double) exprCCFX.evaluate(docCFX,
 							XPathConstants.NUMBER);
 					mphWindGust = 1.15155 * mphWindSpeed;
 				} catch (NumberFormatException e) {
 					isNAWindGust = true;
 				}
 				if (isNAWindGust) {
 					data.getWxCurCond().setWindGust("NA");
 				} else {
 					int wGust = (int) Math.round(mphWindGust);
 					data.getWxCurCond().setWindGust(Integer.toString(wGust));
 				}
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/humidity[@type = 'relative']/value/text()");
 				data.getWxCurCond().setHumidity(
 						(String) exprCCFX.evaluate(docCFX,
 								XPathConstants.STRING));
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/temperature[@type = 'apparent']/value/text()");
 				data.getWxCurCond().setCurrentTemp(
 						(String) exprCCFX.evaluate(docCFX,
 								XPathConstants.STRING));
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/weather[@time-layout = 'k-p1h-n1-1']/weather-conditions/@weather-summary");
 				data.getWxCurCond().setCurWx(
 						(String) exprCCFX.evaluate(docCFX,
 								XPathConstants.STRING));
 				DateTimeZone dtz = xmlTime.getZone();
 				logger.info("TimeZone: " + dtz.toString());
 				Location location = new Location(user.getWeather()
 						.getLatitude(), user.getWeather().getLongitude());
 				SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
 						location, dtz.toString());
 				Calendar officialSunrise = calculator
 						.getOfficialSunriseCalendarForDate(Calendar
 								.getInstance());
 				Calendar officialSunset = calculator
 						.getOfficialSunsetCalendarForDate(Calendar
 								.getInstance());
 				DateTimeFormatter dtfSunriseset = DateTimeFormat
 						.forPattern("h:mm a");
 				DateTime dtSunrise = new DateTime(officialSunrise);
 				DateTime dtSunset = new DateTime(officialSunset);
 				DateTime dtSunriseLocal = dtSunrise.withZone(DateTimeZone
 						.forID(dtz.toString()));
 				DateTime dtSunsetLocal = dtSunset.withZone(DateTimeZone
 						.forID(dtz.toString()));
 
 				data.getWxCurCond().setSunRise(
 						dtfSunriseset.print(dtSunriseLocal));
 				data.getWxCurCond().setSunSet(
 						dtfSunriseset.print(dtSunsetLocal));
 
 			}
 			if (user.getWeather().getDeliver_pref() == 2
 					|| user.getWeather().getDeliver_pref() == 3) {
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type='forecast']/time-layout[layout-key/text()=/dwml/data[@type='forecast']/parameters[@applicable-location='point1']/wordedForecast/@time-layout]/start-valid-time");
 				NodeList days = (NodeList) exprCCFX.evaluate(docCFX,
 						XPathConstants.NODESET);
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type='forecast']/parameters[@applicable-location='point1']/wordedForecast/text");
 				NodeList forecastText = (NodeList) exprCCFX.evaluate(docCFX,
 						XPathConstants.NODESET);
 
 				for (int i = 0; i < 3; i++) {
 					Element e = (Element) days.item(i);
 					Element e2 = (Element) forecastText.item(i);
 					data.getWeatherForecast()
 							.getPeriodForecast()
 							.put(e.getAttribute("period-name"),
 									e2.getTextContent());
 
 				}
 			}
 			inCC_Fx.close();
 		} catch (Exception ex) {
 			logger.error("Get Weather Data had an issue.", ex);
 		}
 
 		return data;
 
 	}
 
 	/**
 	 * Gets the news stories for email from the users defined RSS feeds.
 	 * 
 	 * @param data
 	 *            the EmailData object to attach the news story information to
 	 * @param user
 	 *            the user
 	 * @return the news stories for email
 	 */
 	private EmailData getNewsStoriesForEmail(EmailData data, User user) {
 
 		Set<UserRssFeeds> userRssFeeds = user.getUserRssFeeds();
 		Pageable topFiveArticlesByDate = new PageRequest(0, 5);
 
 		for (UserRssFeeds userRssFeed : userRssFeeds) {
 			if (userRssFeed.getDeliver() == 1) {
 				NewsFeed newsFeed = new NewsFeed();
 				newsFeed.setFeedTitle(userRssFeed.getFeedName());
 				List<RssNewsLinks> articles = rssNewsLinksRepository
 						.findByFeedIdOrderByPubDateDesc(
 								userRssFeed.getFeedId(), topFiveArticlesByDate);
 				for (RssNewsLinks r : articles) {
 					NewsStory newsStory = new NewsStory(r.getTitle(),
 							r.getLink(), r.getDescription());
 					newsFeed.getNewsStories().add(newsStory);
 				}
 
 				data.getNewsFeeds().add(newsFeed);
 			}
 		}
 
 		return data;
 	}
 
 }
