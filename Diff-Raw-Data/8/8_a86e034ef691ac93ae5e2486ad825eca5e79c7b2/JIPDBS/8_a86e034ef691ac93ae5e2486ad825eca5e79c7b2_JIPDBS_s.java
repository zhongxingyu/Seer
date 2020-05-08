 package jipdbs;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.Message.RecipientType;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import jipdbs.data.Alias;
 import jipdbs.data.Player;
 import jipdbs.data.Server;
 import net.tanesha.recaptcha.ReCaptcha;
 import net.tanesha.recaptcha.ReCaptchaFactory;
 import net.tanesha.recaptcha.ReCaptchaImpl;
 import net.tanesha.recaptcha.ReCaptchaResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Email;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class JIPDBS extends JIPDBSCore {
 
 	private static final String FROM_ADDR = "contact@ipdburt.appspotmail.com";
 	private static final int NGRAMS_LIMIT = 40;
 	private static final int NGRAMS_OFFSET = 0;
 	private static final int MIN_NGRAM_QUERY = 2;
 	private static final int MAX_NGRAM_QUERY = 8;
 	private static final int MAX_SINGLE_QUERY = 32;
 	private static final int MAX_ALIAS_QUERY = 20; // reject querys with no
 													// exact match
 
 	private static final Logger log = Logger.getLogger(JIPDBS.class.getName());
 
 	private final String recaptchaPublicKey;
 	private final String recaptchaPrivateKey;
 
 	public JIPDBS(Properties props) {
 		recaptchaPublicKey = props.getProperty("recaptcha.public.key", "");
 		recaptchaPrivateKey = props.getProperty("recaptcha.private.key", "");
 	}
 
 	public String getNewRecaptchaCode() {
 		ReCaptcha c = ReCaptchaFactory.newReCaptcha(recaptchaPublicKey,
 				recaptchaPrivateKey, false);
 		return c.createRecaptchaHtml(null, null);
 	}
 
 	public boolean isRecaptchaValid(String remoteAddr, String challenge,
 			String uresponse) {
 		ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
 
 		reCaptcha.setPrivateKey(recaptchaPrivateKey);
 
 		ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr,
 				challenge, uresponse);
 
 		return reCaptchaResponse.isValid();
 	}
 
 	public void addServer(String name, String admin, String uid, String ip) {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Server server = new Server();
 		server.setAdmin(new Email(admin));
 		server.setCreated(new Date());
 		// server.setUpdated(new Date()); // skip this. it should be updated
 		// when the server actually start sending data.
 		server.setUid(uid);
 		server.setName(name);
 		server.setOnlinePlayers(0);
 		server.setAddress(ip);
 		serverDAO.save(service, server);
 	}
 
 	public Server getServer(String encodedKey) throws EntityNotFoundException {
 		return getServer(KeyFactory.stringToKey(encodedKey));
 	}
 
 	public Server getServer(Key key) throws EntityNotFoundException {
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 		return serverDAO.get(service, key);
 	}
 
 	public void saveServer(String key, String name, String admin, String ip) {
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 		try {
 			Server server = getServer(key);
 			server.setName(name);
 			server.setAdmin(new Email(admin));
 			server.setAddress(ip);
 			serverDAO.save(service, server);
 		} catch (EntityNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public List<Server> getServers(int offset, int limit, int[] count) {
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 			return serverDAO.findAll(service, offset, limit, count);
 		} catch (Exception e) {
 			log.severe("Unable to fetch servers:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	public List<SearchResult> rootQuery(int offset, int limit, int[] count) {
 
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 
 			List<Player> players = playerDAO.findLatest(service, offset, limit,
 					count);
 
 			List<SearchResult> results = new ArrayList<SearchResult>();
 
 			for (Player player : players) {
 
 				Alias alias = aliasDAO.getLastUsedAlias(service,
 						player.getKey());
 				Server server = serverDAO.get(service, player.getServer());
 
 				// Whoops! inconsistent data.
 				if (alias == null || server == null)
 					continue;
 
 				SearchResult result = new SearchResult();
 				result.setId(player.getKey().getId());
 				result.setKey(KeyFactory.keyToString(player.getKey()));
 				result.setIp(alias.getMaskedIp());
				result.setLatest(player.getUpdated());
 				result.setPlaying(false);
 				// result.setPlaying(player.getUpdated().equals(server.getUpdated()));
 				result.setName(alias.getNickname());
 				result.setServer(server);
 				result.setBanInfo(player.getBanInfo());
 				results.add(result);
 			}
 			return results;
 		} catch (Exception e) {
 			log.severe("Unable to fetch root query players:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	public List<SearchResult> bannedQuery(int offset, int limit, int[] count) {
 
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 
 			List<Player> players = playerDAO.findBanned(service, offset, limit,
 					count);
 
 			List<SearchResult> results = new ArrayList<SearchResult>();
 
 			for (Player player : players) {
 
 				Alias alias = aliasDAO.getLastUsedAlias(service,
 						player.getKey());
 				Server server = serverDAO.get(service, player.getServer());
 
 				// Whoops! inconsistent data.
 				if (alias == null || server == null)
 					continue;
 
 				SearchResult result = new SearchResult();
 				result.setId(player.getKey().getId());
 				result.setKey(KeyFactory.keyToString(player.getKey()));
 				result.setIp(alias.getMaskedIp());
				result.setLatest(player.getUpdated());
 				result.setPlaying(false);
 				// result.setPlaying(player.getUpdated().equals(server.getUpdated()));
 				result.setName(alias.getNickname());
 				result.setServer(server);
 				result.setBanInfo(player.getBanInfo());
 				results.add(result);
 			}
 			return results;
 		} catch (Exception e) {
 			log.severe("Unable to fetch banned query players:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	public List<SearchResult> aliasSearch(String query, int offset, int limit,
 			int[] count, boolean[] exactMatch) {
 
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 
 			List<Alias> aliasses = new ArrayList<Alias>();
 
 			if (query.length() <= MAX_SINGLE_QUERY) {
 				aliasses = aliasDAO.findByNickname(service, query, offset,
 						limit, count);
 			}
 
 			exactMatch[0] = true;
 
 			// No exact match, try ngrams.
 			if (aliasses.size() == 0 && query.length() >= MIN_NGRAM_QUERY
 					&& query.length() <= MAX_ALIAS_QUERY) {
 				aliasses = aliasDAO.findByNGrams(
 						service,
 						query.length() <= MAX_NGRAM_QUERY ? query : query
 								.substring(0, MAX_NGRAM_QUERY), NGRAMS_OFFSET,
 						NGRAMS_LIMIT, count);
 				exactMatch[0] = false;
 			}
 
 			return marshall(aliasses);
 		} catch (Exception e) {
 			e.printStackTrace();
 			log.severe("Unable to fetch players:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	public List<SearchResult> ipSearch(String query, int offset, int limit,
 			int[] count) {
 
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 
 			return marshall(aliasDAO.findByIP(service, query, offset, limit,
 					count));
 		} catch (Exception e) {
 			e.printStackTrace();
 			log.severe("Unable to fetch players:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	public List<SearchResult> byServerSearch(String query, int offset,
 			int limit, int[] count) {
 
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 
 			return marshall(aliasDAO.findByServer(service, query, offset,
 					limit, count));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			log.severe("Unable to fetch players:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	private List<SearchResult> marshall(List<Alias> aliasses)
 			throws EntityNotFoundException {
 
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 
 		List<SearchResult> results = new ArrayList<SearchResult>();
 
 		for (Alias alias : aliasses) {
 
 			Player player = playerDAO.get(service, alias.getPlayer());
 			Server server = serverDAO.get(service, player.getServer());
 
 			// Whoops! inconsistent data.
 			if (alias == null || server == null)
 				continue;
 
 			SearchResult result = new SearchResult();
 			result.setId(player.getKey().getId());
 			result.setKey(KeyFactory.keyToString(player.getKey()));
 			result.setIp(alias.getMaskedIp());
			result.setLatest(player.getUpdated());
 			result.setPlaying(false);
 			// result.setPlaying(player.getUpdated().equals(server.getUpdated()));
 			result.setName(alias.getNickname());
 			result.setServer(server);
 			result.setBanInfo(player.getBanInfo());
 			results.add(result);
 		}
 
 		return results;
 	}
 
 	public Player getPlayer(String player) throws EntityNotFoundException {
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 		return playerDAO.get(service, KeyFactory.stringToKey(player));
 	}
 
 	public Alias getLastAlias(String player) {
 		DatastoreService service = DatastoreServiceFactory
 				.getDatastoreService();
 		return aliasDAO.getLastUsedAlias(service,
 				KeyFactory.stringToKey(player));
 	}
 
 	public List<AliasResult> alias(String encodedKey, int offset, int limit,
 			int[] count) {
 
 		try {
 			DatastoreService service = DatastoreServiceFactory
 					.getDatastoreService();
 
 			List<AliasResult> result = new ArrayList<AliasResult>();
 
 			Player player = playerDAO.get(service,
 					KeyFactory.stringToKey(encodedKey));
 
 			if (player != null) {
 				List<Alias> aliasses = aliasDAO.findByPlayer(service,
 						player.getKey(), offset, limit, count);
 
 				for (Alias alias : aliasses) {
 
 					AliasResult item = new AliasResult();
 					item.setCount(alias.getCount());
 					item.setIp(alias.getMaskedIp());
 					item.setNickname(alias.getNickname());
 					item.setUpdated(alias.getUpdated());
 					result.add(item);
 				}
 			}
 
 			return result;
 		} catch (Exception e) {
 			log.severe("Unable to fetch aliasses:" + e.getMessage());
 			count[0] = 0;
 			return Collections.emptyList();
 		}
 	}
 
 	public void sendAdminMail(String realId, String from, String body) {
 		try {
 
 			StringBuilder builder = new StringBuilder();
 			builder.append("Responder a: ");
 			builder.append(from);
 			builder.append("\r\n");
 			if (realId != null) {
 				builder.append("Identificado como: ");
 				builder.append(realId);
 				builder.append("\r\n");
 			}
 			builder.append("\r\n");
 			builder.append("------------- MENSAJE -------------\r\n\r\n");
 			builder.append(body);
 
 			Session session = Session
 					.getDefaultInstance(new Properties(), null);
 
 			Address[] replyTo = new InternetAddress[1];
 			replyTo[0] = new InternetAddress(from);
 
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress(FROM_ADDR));
 			msg.setReplyTo(replyTo);
 			msg.addRecipient(RecipientType.TO, new InternetAddress("admins"));
 			msg.setSubject("Mensaje enviado desde IPDB");
 			msg.setText(builder.toString());
 			Transport.send(msg);
 
 		} catch (AddressException e) {
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 		} catch (MessagingException e) {
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 		}
 	}
 }
