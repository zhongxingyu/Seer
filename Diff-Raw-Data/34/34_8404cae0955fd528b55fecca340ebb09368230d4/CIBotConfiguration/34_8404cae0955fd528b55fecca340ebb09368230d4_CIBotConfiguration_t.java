 package com.cibot.config;
 
 import com.cibot.cimodel.BuildStatus;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.apache.commons.lang.StringUtils;
 
 import javax.xml.bind.annotation.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Uefix
  */
 @XmlRootElement(name = "cibotConfig")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class CIBotConfiguration {
 
 
     private static Pattern JOB_BY_FEED_URL_PATTERN = Pattern.compile("^.*/(.*)/rssAll$");
 
 
     @XmlElement
     private Thumbi thumbi = new Thumbi();
 
     @XmlElement
     private FeedReader feedReader = new FeedReader();
 
 
 
     public Login getLogin(Feed feed) {
         Preconditions.checkArgument(feed != null);
         Preconditions.checkArgument(feed.hasLogin());
 
         return feedReader.getLogins().get(feed.getLogin());
     }
 
 
     public Thumbi getThumbi() {
         return thumbi;
     }
 
 
     public FeedReader getFeedReader() {
         return feedReader;
     }
 
 
     @XmlRootElement
     @XmlAccessorType(XmlAccessType.FIELD)
     public static class Thumbi {
 
         @XmlElement
         private String connectionUri;
 
         @XmlElement
         private long timeout;
 
         @XmlAttribute
         private boolean enabled = false;
 
         public String getConnectionUri() {
             return connectionUri;
         }
 
         public void setConnectionUri(String connectionUri) {
             this.connectionUri = connectionUri;
         }
 
 
         public long getTimeout() {
             return timeout;
         }
 
         public void setTimeout(long timeout) {
             this.timeout = timeout;
         }
 
 
         public boolean isEnabled() {
             return enabled;
         }
 
         public void setEnabled(boolean enabled) {
             this.enabled = enabled;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Thumbi thumbi = (Thumbi) o;
 
             if (enabled != thumbi.enabled) return false;
             if (timeout != thumbi.timeout) return false;
             if (connectionUri != null ? !connectionUri.equals(thumbi.connectionUri) : thumbi.connectionUri != null)
                 return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = connectionUri != null ? connectionUri.hashCode() : 0;
             result = 31 * result + (int) (timeout ^ (timeout >>> 32));
             result = 31 * result + (enabled ? 1 : 0);
             return result;
         }
 
 
         @Override
         public String toString() {
             return "Thumbi{" +
                     "connectionUri='" + connectionUri + '\'' +
                     ", timeout=" + timeout +
                     ", enabled=" + enabled +
                     '}';
         }
     }
 
 
     @XmlRootElement
     @XmlAccessorType(XmlAccessType.FIELD)
     public static class Login {
 
         @XmlAttribute
         private String id;
 
         @XmlAttribute
         private String user;
 
         @XmlAttribute
         private String password;
 
 
         public Login() {
         }
 
         public Login(String id, String user, String password) {
             this.id = id;
             this.user = user;
             this.password = password;
         }
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getUser() {
             return user;
         }
 
         public void setUser(String user) {
             this.user = user;
         }
 
         public String getPassword() {
             return password;
         }
 
         public void setPassword(String password) {
             this.password = password;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Login login = (Login) o;
 
             if (id != null ? !id.equals(login.id) : login.id != null) return false;
             if (password != null ? !password.equals(login.password) : login.password != null) return false;
             if (user != null ? !user.equals(login.user) : login.user != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = id != null ? id.hashCode() : 0;
             result = 31 * result + (user != null ? user.hashCode() : 0);
             result = 31 * result + (password != null ? password.hashCode() : 0);
             return result;
         }
 
         @Override
         public String toString() {
             return "Login{" +
                     "id='" + id + '\'' +
                     ", user='" + user + '\'' +
                     ", password='" + password + '\'' +
                     '}';
         }
     }
 
 
     @XmlRootElement
     @XmlAccessorType(XmlAccessType.FIELD)
     public static class Feed {
 
         @XmlAttribute
         private String login;
 
         @XmlAttribute
         private String jobName;
 
         @XmlValue
         private URL url;
 
         public Feed() {
         }
 
         public Feed(URL url, String login) {
             this.url = url;
             this.login = login;
         }
 
         public void setJobName(String jobName) {
             this.jobName = jobName;
         }
 
         public String getJobName() {
             if (jobName == null) {
                 return parseJobName();
             }
             return jobName;
         }
 
         private String parseJobName() {
             if (url != null) {
                 try {
                     Matcher matcher = JOB_BY_FEED_URL_PATTERN.matcher(url.toExternalForm());
                     if (matcher.matches()) {
                         return matcher.group(1);
                     }
                 } catch (Exception e) {
                     // ignore
                 }
             }
             return null;
         }
 
 
         public String getLogin() {
             return login;
         }
 
         public void setLogin(String login) {
             this.login = login;
         }
 
         public boolean hasLogin() {
             return StringUtils.isNotBlank(login);
         }
 
 
         public URL getUrl() {
             return url;
         }
 
         public void setUrl(URL url) {
             this.url = url;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Feed feed = (Feed) o;
 
             if (jobName != null ? !jobName.equals(feed.jobName) : feed.jobName != null) return false;
             if (login != null ? !login.equals(feed.login) : feed.login != null) return false;
             if (url != null ? !url.equals(feed.url) : feed.url != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = login != null ? login.hashCode() : 0;
             result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
             result = 31 * result + (url != null ? url.hashCode() : 0);
             return result;
         }
 
         @Override
         public String toString() {
             return "Feed{" +
                     "login='" + login + '\'' +
                     ", jobName='" + jobName + '\'' +
                     ", url=" + url +
                     '}';
         }
     }
 
 
     @XmlRootElement
     public static class FeedReader {
 
         private Map<String,Login> logins = Maps.newHashMap();
 
         private List<Feed> feeds = Lists.newArrayList();
 
         /**
                  * http://www.mail-archive.com/cxf-user@incubator.apache.org/msg04723.html
                  * http://www.java-forum.org/xml-co/116454-jaxb-hashmap.html
                  */
         private ArrayListMultimap<BuildStatus,String> statusMappings = ArrayListMultimap.create();
 
 
         public Map<String,Login> getLogins() {
             return logins;
         }
 
         @XmlElementWrapper(name = "logins")
         @XmlElement(name = "login")
         public List<Login> getLoginsList() {
             return Lists.newArrayList(logins.values());
         }
 
         public void setLoginsList(List<Login> loginsList) {
             logins.clear();
             for (Login login : loginsList) {
                 logins.put(login.getId(), login);
             }
         }
 
 
         @XmlElementWrapper(name = "feeds")
         @XmlElement(name = "feed")
         public List<Feed> getFeeds() {
             return feeds;
         }
 
         public void setFeeds(List<Feed> feeds) {
             this.feeds = feeds;
         }
 
         public ArrayListMultimap<BuildStatus, String> getStatusMappings() {
             return statusMappings;
         }
 
 
         public BuildStatus mapBuildStatus(String buildStatusString) {
             Preconditions.checkArgument(StringUtils.isNotEmpty(buildStatusString),
                     "Given build status string must not be empty");
 
             buildStatusString = buildStatusString.toLowerCase();
 
             for (BuildStatus status : statusMappings.keySet()) {
                 if (statusMappings.asMap().get(status).contains(buildStatusString)) {
                     return status;
                 }
             }
             return null;
         }
 
 
         @XmlElement(name = "statusMapping")
         private StatusMapping[] getStatusMappingArray() {
             List<StatusMapping> mappingsList = Lists.newArrayList();
             for (Map.Entry<BuildStatus, Collection<String>> entry : statusMappings.asMap().entrySet()) {
                 mappingsList.add(StatusMapping.forMapEntry(entry));
             }
             return mappingsList.toArray(new StatusMapping[mappingsList.size()]);
         }
 
         private void setStatusMappingArray(StatusMapping[] statusMappings) {
             for (StatusMapping statusMapping : statusMappings) {
                 this.statusMappings.putAll(
                         statusMapping.getBuildStatus(),
                         statusMapping.getTexts());
             }
         }
 
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             FeedReader that = (FeedReader) o;
 
             if (feeds != null ? !feeds.equals(that.feeds) : that.feeds != null) return false;
             if (logins != null ? !logins.equals(that.logins) : that.logins != null) return false;
             if (statusMappings != null ? !statusMappings.equals(that.statusMappings) : that.statusMappings != null)
                 return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = logins != null ? logins.hashCode() : 0;
             result = 31 * result + (feeds != null ? feeds.hashCode() : 0);
             result = 31 * result + (statusMappings != null ? statusMappings.hashCode() : 0);
             return result;
         }
 
         @Override
         public String toString() {
             return "FeedReader{" +
                     "logins=" + logins +
                     ", feeds=" + feeds +
                     ", statusMappings=" + statusMappings +
                     '}';
         }
     }
 
 
 
     public static class StatusMapping {
 
 
         private BuildStatus buildStatus;
 
         private ArrayList<String> texts = Lists.newArrayList();
 
 
         public static StatusMapping forMapEntry(Map.Entry<BuildStatus, Collection<String>> entry) {
             StatusMapping result = new StatusMapping();
             result.setBuildStatus(entry.getKey());
             result.getTexts().addAll(entry.getValue());
             return result;
         }
 
 
         @XmlAttribute(name = "status")
         public BuildStatus getBuildStatus() {
             return buildStatus;
         }
 
         public void setBuildStatus(BuildStatus buildStatus) {
             this.buildStatus = buildStatus;
         }
 
         @XmlElement(name = "text")
         public ArrayList<String> getTexts() {
             return texts;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             StatusMapping that = (StatusMapping) o;
 
             if (buildStatus != null ? !buildStatus.equals(that.buildStatus) : that.buildStatus != null) return false;
             if (texts != null ? !texts.equals(that.texts) : that.texts != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = buildStatus != null ? buildStatus.hashCode() : 0;
             result = 31 * result + (texts != null ? texts.hashCode() : 0);
             return result;
         }
     }
 
 
     @XmlRootElement
     @XmlAccessorType(XmlAccessType.FIELD)
     public static class LabelProperties {
 
 
         @XmlAttribute
         private int width = 250;
 
         @XmlAttribute
         private int height = 30;
 
         @XmlAttribute
         private int fontSize = 14;
 
 
         public int getWidth() {
             return width;
         }
 
         public void setWidth(int width) {
             this.width = width;
         }
 
         public int getHeight() {
             return height;
         }
 
         public void setHeight(int height) {
             this.height = height;
         }
 
         public int getFontSize() {
             return fontSize;
         }
 
         public void setFontSize(int fontSize) {
             this.fontSize = fontSize;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             LabelProperties that = (LabelProperties) o;
 
             if (fontSize != that.fontSize) return false;
             if (height != that.height) return false;
             if (width != that.width) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = width;
             result = 31 * result + height;
             result = 31 * result + fontSize;
             return result;
         }
     }
 
/*
     @XmlRootElement
     @XmlAccessorType(XmlAccessType.FIELD)
     public static class GUI {
         @XmlElement
         private LabelProperties labelProperties = new LabelProperties();
 
 
         public LabelProperties getLabelProperties() {
             return labelProperties;
         }
     }
 */
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         CIBotConfiguration that = (CIBotConfiguration) o;
 
         if (feedReader != null ? !feedReader.equals(that.feedReader) : that.feedReader != null) return false;
//        if (labelProperties != null ? !labelProperties.equals(that.labelProperties) : that.labelProperties != null)
//            return false;
         if (thumbi != null ? !thumbi.equals(that.thumbi) : that.thumbi != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = thumbi != null ? thumbi.hashCode() : 0;
         result = 31 * result + (feedReader != null ? feedReader.hashCode() : 0);
//        result = 31 * result + (labelProperties != null ? labelProperties.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "CIBotConfiguration{" +
                 "thumbi=" + thumbi +
                 ", feedReader=" + feedReader +
//                ", labelProperties=" + labelProperties +
                 '}';
     }
}
