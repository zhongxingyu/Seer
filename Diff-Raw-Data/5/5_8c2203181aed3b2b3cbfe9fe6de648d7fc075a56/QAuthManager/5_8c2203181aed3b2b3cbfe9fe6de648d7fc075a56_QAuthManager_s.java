 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.qauth;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.Invite;
 import com.dmdirc.config.ConfigBinder;
 import com.dmdirc.config.ConfigBinding;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.events.ClientPrefsOpenedEvent;
 import com.dmdirc.events.QueryMessageEvent;
 import com.dmdirc.events.ServerConnectedEvent;
 import com.dmdirc.events.ServerInviteReceivedEvent;
 import com.dmdirc.events.ServerNoticeEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.User;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.util.validators.NotEmptyValidator;
 
 import java.util.Optional;
 
 import javax.inject.Inject;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Provides Q AUth support in DMDirc.
  */
 public class QAuthManager {
 
     private final String domain;
     private final PluginInfo pluginInfo;
     private final DMDircMBassador eventBus;
     private final ConfigBinder configBinder;
     private String username;
     private String password;
     private boolean whois;
     private boolean autoInvite;
     private boolean acceptInvites;
 
     @Inject
     public QAuthManager(
             @PluginDomain(QAuthPlugin.class) final String domain,
             @PluginDomain(QAuthPlugin.class) final PluginInfo pluginInfo,
             @GlobalConfig final AggregateConfigProvider config,
             final DMDircMBassador eventBus) {
         this.domain = domain;
         this.pluginInfo = pluginInfo;
         this.eventBus = eventBus;
         configBinder = config.getBinder().withDefaultDomain(domain);
     }
 
     public void load() {
         configBinder.bind(this, QAuthManager.class);
         eventBus.subscribe(this);
     }
 
     public void unload() {
         configBinder.unbind(this);
         eventBus.unsubscribe(this);
     }
 
     private boolean isValidConnection(final Connection connection) {
        return "Quakenet".equals(connection.getNetwork());
     }
 
     private boolean isValidUser(final User user) {
         // TODO: Check hostname?
        return "Q".equals(user.getNickname());
     }
 
     private void acceptInvite(final Invite invite) {
         invite.accept();
     }
 
     private void requestInvites(final Connection connection) {
         connection.sendMessage("Q@Cserve.quakenet.org", "invite");
     }
 
     private void auth(final Connection connection) {
         connection.sendMessage("Q@Cserve.quakenet.org", "auth " + username + ' ' + password);
     }
 
     @Handler
     void handleConnect(final ServerConnectedEvent event) {
         if (!isValidConnection(event.getConnection())) {
             return;
         }
         // TODO: whois
         auth(event.getConnection());
     }
 
     @Handler
     void handleInvite(final ServerInviteReceivedEvent event) {
         if (!acceptInvites) {
             return;
         }
         if (isValidConnection(event.getConnection()) && isValidUser(event.getUser())) {
             acceptInvite(event.getInvite());
         }
     }
 
     @Handler
     void handleNotices(final ServerNoticeEvent event) {
         handleCommunication(Optional.of(event.getConnection()), event.getUser(), event.getMessage());
     }
 
     @Handler
     void handleMessages(final QueryMessageEvent event) {
         handleCommunication(event.getQuery().getConnection(), event.getUser(), event.getMessage());
     }
 
     private void handleCommunication(final Optional<Connection> connection, final User user,
             final String message) {
         connection.ifPresent(c -> {
             if (isValidConnection(c) && isValidUser(user) &&
                     ("You are now logged in as " + username + '.').equalsIgnoreCase(message)) {
                 if (autoInvite) {
                     requestInvites(c);
                 }
             }
         });
     }
 
     @ConfigBinding(key = "username")
     void handleUsername(final String value) {
         username = value;
     }
 
     @ConfigBinding(key = "password")
     void handlePassword(final String value) {
         password = value;
     }
 
     @ConfigBinding(key = "whois")
     void handleWhois(final boolean value) {
         whois = value;
     }
 
     @ConfigBinding(key = "autoinvite")
     void handleAutoInvite(final boolean value) {
         autoInvite = value;
     }
 
     @ConfigBinding(key = "acceptinvites")
     void handleAcceptInvites(final boolean value) {
         acceptInvites = value;
     }
 
     @Handler
     void showConfig(final ClientPrefsOpenedEvent event) {
         final PluginPreferencesCategory category = new PluginPreferencesCategory(pluginInfo,
                 "Q Auth", "Q Authentication settings");
         category.addSetting(new PreferencesSetting(PreferencesType.TEXT, new NotEmptyValidator(),
                 domain, "username", "Username", "Your Q username",
                 event.getModel().getConfigManager(), event.getModel().getIdentity()));
         category.addSetting(new PreferencesSetting(PreferencesType.TEXT, new NotEmptyValidator(),
                 domain, "password", "Password", "Your Q password",
                 event.getModel().getConfigManager(), event.getModel().getIdentity()));
         category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 domain, "whois", "Whois before auth",
                 "Should we send a whois before authing and only auth if required?",
                 event.getModel().getConfigManager(), event.getModel().getIdentity()));
         category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 domain, "autoinvite", "Auto Invite", "Should Q autoinvite you to channels?",
                 event.getModel().getConfigManager(), event.getModel().getIdentity()));
         category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 domain, "acceptinvites", "Auto Accept Invites",
                 "Should the client automatically accept invites from Q?",
                 event.getModel().getConfigManager(), event.getModel().getIdentity()));
         event.getModel().getCategory("Plugins").addSubCategory(category);
     }
 }
