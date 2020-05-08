 package thesmith.eventhorizon.service;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import thesmith.eventhorizon.model.Account;
 
 import com.google.appengine.repackaged.com.google.common.collect.Lists;
 
 /**
  * Defines the interface to Account objects
  * 
  * @author bens
  */
 public interface AccountService {
   /** All available domains */
   public static enum DOMAIN {
     twitter("twitter.com", Pattern.compile("^http:\\/\\/twitter.com\\/(.+?)\\/?$"), "http://twitter.com",
         "http://twitter.com/%s",
         "{ago}, <a href='{userUrl}' rel='me'>I</a> <a href='{titleUrl}'>tweeted</a> '{title}'."), lastfm("last.fm",
         Pattern.compile("^http:\\/\\/www.last.fm\\/user\\/(.+?)\\/?$"), "http://last.fm", "http://last.fm/user/%s",
         "As far as <a href='{domainUrl}'>last.fm</a> knows, the last thing "
             + "<a href='{userUrl}' rel='me'>I</a> listened to was "
             + "<a href='{titleUrl}'>{title}</a>, and that was {ago}."), flickr("flickr.com", Pattern
         .compile("^http:\\/\\/www.flickr.com\\/photos\\/(.+?)\\/$"), "http://flickr.com",
         "http://flickr.com/people/%s", "<a href='{userUrl}' rel='me'>I</a> took a <a href='{titleUrl}'>photo</a> "
             + "{ago} called '{title}' and uploaded it to <a href='{domainUrl}'>flickr</a>."), birth(null, null, null,
         null, "I was born {ago} in <a href='{titleUrl}'>{title}</a>."), lives(null, null, null, null,
         "I now live in <a href='{titleUrl}'>{title}</a> which I moved to {ago}."), wordr("wordr.org", Pattern
         .compile("^http:\\/\\/wordr.org\\/users\\/(.+?)\\/?$"), "http://wordr.org", "http://wordr.org/users/%s",
         "And, {ago}, <a href='{userUrl}'>my</a> last <a href='{domainUrl}'>word</a> "
             + "was <a href='{titleUrl}'>{title}</a>."), github("github.com", Pattern
         .compile("^http:\\/\\/github.com\\/(.+?)\\/?$"), "http://github", "http://github.com/%s",
         "{ago}, <a href='{userUrl}' rel='me'>I</a> " + "pushed to {title}."), tumblr("tumblr.com", Pattern
         .compile("^http:\\/\\/(.+?).tumblr.com\\/?$"), "http://tumblr.com", "http://%s.tumblr.com",
         "{ago}, <a href='{userUrl}' rel='me'>I</a> <a href='{titleUrl}'>posted</a> '{title}'.");
 
     private final String domainSearch;
     private final Pattern domainMatcher;
     private final String domainUrl;
     private final String userUrl;
     private final String defaultTemplate;
 
     private DOMAIN(String domainSearch, Pattern domainMatcher, String domainUrl, String userUrl, String defaultTemplate) {
       this.domainSearch = domainSearch;
       this.domainMatcher = domainMatcher;
       this.domainUrl = domainUrl;
       this.userUrl = userUrl;
       this.defaultTemplate = defaultTemplate;
     }
 
     public String getDomainSearch() {
       return domainSearch;
     }
 
     public Pattern getDomainMatcher() {
       return domainMatcher;
     }
 
     public String getDomainUrl() {
       return domainUrl;
     }
 
     public String getUserUrl() {
       return userUrl;
     }
 
     public String getDefaultTemplate() {
       return defaultTemplate;
     }
   }
 
   /** Domains that are freestyle */
  public static final List<String> FREESTYLE_DOMAINS = Lists.newArrayList(DOMAIN.birth.toString(), DOMAIN.lives
       .toString());
 
   /**
    * Create an account
    * 
    * @param account
    */
   public void create(Account account);
 
   /**
    * Delete an account
    * 
    * @param account
    */
   public void delete(String personId, String domain);
 
   /**
    * Update an account
    * 
    * @param account
    */
   public void update(Account account);
 
   /**
    * Find an account
    * 
    * @param personId
    * @param domain
    * @return
    */
   public Account find(String personId, String domain);
 
   /**
    * Retrieve all of a person's accounts
    * 
    * @param personId
    * @return
    */
   public List<Account> list(String personId);
 
   /**
    * Retrieve all of a person's accounts and empty ones for accounts they don't have
    * 
    * @param personId
    * @return
    */
   public List<Account> listAll(String personId);
 
   /**
    * Retrieve a limited list of accounts that need processing
    * 
    * @param limit
    * @return
    */
   public List<Account> toProcess(int limit);
 
   /**
    * Retrieve a distinct list of domains that a person has registered with
    * 
    * @param personId
    * @return
    */
   public List<String> domains(String personId);
 
   /**
    * Create a new account object
    * 
    * @param personId
    * @param domain
    * @return
    */
   public Account account(String personId, String domain);
 }
