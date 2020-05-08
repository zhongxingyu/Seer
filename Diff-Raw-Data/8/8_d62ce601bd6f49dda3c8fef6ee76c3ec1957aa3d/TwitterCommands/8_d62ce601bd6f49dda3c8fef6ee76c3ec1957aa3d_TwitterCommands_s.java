 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CommandKit
  *
  * CommandKit is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CommandKit is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.commandkit.twitter;
 
 import com.alta189.cyborg.api.command.CommandContext;
 import com.alta189.cyborg.api.command.CommandResult;
 import com.alta189.cyborg.api.command.CommandSource;
 import com.alta189.cyborg.api.command.ReturnType;
 import com.alta189.cyborg.api.command.annotation.Command;
 import com.alta189.cyborg.api.command.annotation.Usage;
 import com.alta189.cyborg.api.util.StringUtils;
 import com.alta189.cyborg.perms.CyborgUser;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.PeriodFormatter;
 import org.joda.time.format.PeriodFormatterBuilder;
 import org.pircbotx.Colors;
 import org.pircbotx.UserSnapshot;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import twitter4j.conf.ConfigurationBuilder;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static com.alta189.cyborg.api.command.CommandResultUtil.get;
 import static com.alta189.cyborg.commandkit.CommandKit.getDatabase;
 import static com.alta189.cyborg.perms.PermissionManager.getUser;
 
 public class TwitterCommands {
 	private static final PeriodFormatter timeFormatter = new PeriodFormatterBuilder()
 			.appendYears().appendSuffix(" years").appendSeparator(", ")
 			.appendMonths().appendSuffix(" months").appendSeparator(", ")
 			.appendWeeks().appendSuffix(" weeks").appendSeparator(", ")
 			.appendDays().appendSuffix(" days").appendSeparator(", ")
 			.appendHours().appendSuffix(" hours").appendSeparator(", ")
 			.appendMinutes().appendSuffix(" minutes").appendSeparator(", ")
 			.appendSeconds().appendSuffix(" seconds")
 			.toFormatter();
 	private static final String lineBreak = System.getProperty("line.separator");
 	private final Map<UserSnapshot, RequestToken> tokenMap = new HashMap<UserSnapshot, RequestToken>();
 	private final ConfigurationBuilder defaultConfigBuilder = new ConfigurationBuilder();
 	private final TwitterFactory defaultTwitterFactory;
 	private final String consumerKey;
 	private final String consumerSecret;
 	private final Twitter twitter;
 
 	public TwitterCommands(String consumerKey, String consumerSecret) {
 		this.consumerKey = consumerKey;
 		this.consumerSecret = consumerSecret;
 		defaultConfigBuilder.setDebugEnabled(true)
 				.setOAuthConsumerKey(consumerKey)
 				.setOAuthConsumerSecret(consumerSecret);
 		defaultTwitterFactory = new TwitterFactory(defaultConfigBuilder.build());
 		twitter = defaultTwitterFactory.getInstance();
 	}
 
 	@Command(name = "twitter", desc = "displays the last tweet of a twitter user", aliases = {"twit"})
 	@Usage(".twitter <user>")
 	public CommandResult twitter(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(ReturnType.NOTICE, "Correct usage is .twitter <user>", source, context);
 		}
 
 		try {
 			List<Status> statusList = twitter.getUserTimeline(context.getArgs()[0]);
 			if (statusList == null || statusList.size() < 1) {
 				return get(ReturnType.MESSAGE, "User has no tweets!", source, context);
 			}
 			Status status = statusList.get(0);
 			status.getUser();
 			StringBuilder builder = new StringBuilder();
 			builder.append(status.getUser().getScreenName()).append(Colors.BLUE).append(": ").append(Colors.NORMAL).append(status.getText()).append(" (").append(timeFormatter.print(new Period(new DateTime(status.getCreatedAt()), new DateTime()))).append(")");
 			return get(ReturnType.MESSAGE, builder.toString().replace(lineBreak, " "), source, context);
 		} catch (TwitterException e) {
 			if (e.getStatusCode() == 404) {
 				return get(ReturnType.MESSAGE, "User not found!", source, context);
 			} else if (e.getStatusCode() == 401) {
 				return get(ReturnType.MESSAGE, "Access denied by Twitter!", source, context);
 			} else {
 				e.printStackTrace();
 				return get(ReturnType.MESSAGE, "There was an internal error!", source, context);
 			}
 		}
 	}
 
 	@Command(name = "twituser", desc = "Add your twitter account so you can tweet from IRC")
 	@Usage(".twituser <twitter username>")
 	public CommandResult twituser(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return get(ReturnType.MESSAGE, "Muse be done from IRC.", source, context);
 		}
 
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(ReturnType.MESSAGE, "Correct usage is .twituser <twitter username>", source, context);
 		}
 
 		String username = context.getArgs()[0];
 		CyborgUser permsAccount = getUser(source.getUser());
 
		if (getUser(source.getUser()) != null) {
 			return get(ReturnType.NOTICE, "You have not registered with me! You need to register to add a twitter account! Type .register for help!", source, context);
 		}
 
 		TwitterUser twitterUser = getDatabase().select(TwitterUser.class).where().equal("permUser", permsAccount.getName()).execute().findOne();
 		if (twitterUser != null) {
 			return get(ReturnType.NOTICE, "You already have an twitter user associated with your account!", source, context);
 		}
 
 		UserSnapshot userSnapshot = source.getUser().generateSnapshot();
 
 		if (tokenMap.get(userSnapshot) != null) {
 			return get(ReturnType.NOTICE, "You already have twitter OAuth URL! Get your pin and type .twitpin <pin>", source, context);
 		}
 
 		Twitter twitter = defaultTwitterFactory.getInstance();
 		try {
 			RequestToken token = twitter.getOAuthRequestToken();
 			tokenMap.put(userSnapshot, token);
 			StringBuilder body = new StringBuilder();
 			body.append("Here is your OAuth Auth URL: ")
 					.append(token.getAuthorizationURL())
 					.append(lineBreak)
 					.append("Go to it. Sign in to twitter and Authorize this Bot. After granting access, it will give you a pin.")
 					.append(lineBreak)
 					.append("Execute this command to finish the registration: .twitpin <pin>");
 			return get(ReturnType.NOTICE, body.toString(), source, context);
 		} catch (TwitterException e) {
 			return get(ReturnType.MESSAGE, "Internal Twitter Exception httpcode:" + e.getStatusCode(), source, context);
 		}
 	}
 
 	@Command(name = "twitpin", desc = "Second stage of adding your twitter account")
 	@Usage(".twitpin <pin>")
 	public CommandResult twitpin(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return get(ReturnType.MESSAGE, "Muse be done from IRC.", source, context);
 		}
 
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(ReturnType.MESSAGE, "Correct usage is .twitpin <pin>", source, context);
 		}
 
 		String pin = context.getArgs()[0];
 		CyborgUser permsAccount = getUser(source.getUser());
 
		if (getUser(source.getUser()) != null) {
 			return get(ReturnType.NOTICE, "You have not registered with me! You need to register to add a twitter account! Type .register for help!", source, context);
 		}
 
 		TwitterUser twitterUser = getDatabase().select(TwitterUser.class).where().equal("permUser", permsAccount.getName()).execute().findOne();
 		if (twitterUser != null) {
 			return get(ReturnType.NOTICE, "You already have an twitter user associated with your account!", source, context);
 		}
 
 		UserSnapshot userSnapshot = source.getUser().generateSnapshot();
 		RequestToken requestToken = tokenMap.get(userSnapshot);
 		if (requestToken == null) {
 			return get(ReturnType.NOTICE, "You already have twitter OAuth URL! Get your pin and type .twituser <user>", source, context);
 		}
 
 		Twitter twitter = defaultTwitterFactory.getInstance();
 
 		try {
 			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, pin);
 			twitterUser = new TwitterUser();
 			twitterUser.setAccessTokenObject(accessToken);
 			twitterUser.setPermUser(permsAccount.getName());
 			getDatabase().save(TwitterUser.class, twitterUser);
 			return get(ReturnType.NOTICE, "Twitter account created! You can now use the tweet command!", source, context);
 		} catch (TwitterException e) {
 			return get(ReturnType.MESSAGE, "Internal Twitter Exception http code:" + e.getStatusCode(), source, context);
 		}
 	}
 
 	@Command(name = "tweet", desc = "Updates your twitter status")
 	@Usage(".tweet <status>...")
 	public CommandResult tweet(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return get(ReturnType.MESSAGE, "Muse be done from IRC.", source, context);
 		}
 
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(ReturnType.MESSAGE, "Correct usage is .tweet <status>", source, context);
 		}
 
 		CyborgUser permsAccount = getUser(source.getUser());
 
		if (getUser(source.getUser()) != null) {
 			return get(ReturnType.NOTICE, "You have not registered with me! You need to register to use a twitter account! Type .register for help!", source, context);
 		}
 
 		TwitterUser twitterUser = getDatabase().select(TwitterUser.class).where().equal("permUser", permsAccount.getName()).execute().findOne();
 		if (twitterUser == null) {
 			return get(ReturnType.NOTICE, "You don't have a twitter user associated with your account! Try .twituser <twitter user>", source, context);
 		}
 
 		String status = StringUtils.toString(context.getArgs());
 
 		Twitter twitter = defaultTwitterFactory.getInstance();
 		twitter.setOAuthAccessToken(twitterUser.getAccessTokenObject());
 
 		try {
 			twitter.updateStatus(status);
 			return get(ReturnType.MESSAGE, "Updated status!", source, context);
 		} catch (TwitterException e) {
 			e.printStackTrace();
 			return get(ReturnType.MESSAGE, "Internal Twitter Exception http code:" + e.getStatusCode(), source, context);
 		}
 	}
 }
