 package com.github.dansmithy.sanjuan.dao.mongo;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 public class MongoDetails {
 
 	private static final Pattern URI_PATTERN = Pattern.compile("mongodb://(.*):(.*?)@(.*?):(.*?)/(.*)$"); 
 	
 	private String username;
 	private String password;
 	private String host;
 	private int port;
 	private String database;
 
 	public MongoDetails(String username, String password, String host, int port, String database) {
 		this.username = username;
 		this.password = password;
 		this.host = host;
 		this.port = port;
 		this.database = database;
 	}
 	
 	public MongoDetails() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	protected static MongoDetails configureFromUri(MongoDetails details, String uri) {
 		Matcher matcher = URI_PATTERN.matcher(uri);
 		System.out.println(matcher.groupCount());
 		if (matcher.matches() && matcher.groupCount() == 5) {
			String username = matcher.group(1).length() == 0 ? null : matcher.group(1);
			String password = matcher.group(2).length() == 0 ? null : matcher.group(2);
			return details.withUsername(username).withPassword(password).withHost(matcher.group(3)).withPort(Integer.valueOf(matcher.group(4))).withDatabase(matcher.group(5));
 		}
 		throw new RuntimeException(String.format("Unable to get details from URI [%s]", uri));
 	}
 
 	public static MongoDetails createFromUri(String uri) {
 		return configureFromUri(new MongoDetails(), uri);
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public MongoDetails withUsername(String username) {
 		this.username = username;
 		return this;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public MongoDetails withPassword(String password) {
 		this.password = password;
 		return this;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public MongoDetails withHost(String host) {
 		this.host = host;
 		return this;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public MongoDetails withPort(int port) {
 		this.port = port;
 		return this;
 	}
 
 	public String getDatabase() {
 		return database;
 	}
 
 	public MongoDetails withDatabase(String database) {
 		this.database = database;
 		return this;
 	}
 	
 	
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return EqualsBuilder.reflectionEquals(obj, this);
 	}
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this,
 				ToStringStyle.SHORT_PREFIX_STYLE);
 	}
 	
 	
 
 }
