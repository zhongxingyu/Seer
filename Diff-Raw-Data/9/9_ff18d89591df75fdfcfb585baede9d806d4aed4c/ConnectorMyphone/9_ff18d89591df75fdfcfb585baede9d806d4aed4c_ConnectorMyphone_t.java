 /*
  * Copyright (C) 2010 Felix Bechstein
  * 
  * This file is part of WebSMS.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; If not, see <http://www.gnu.org/licenses/>.
  */
 package org.herrlado.websms.connector.myphone;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import de.ub0r.android.websms.connector.common.Connector;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 
 /**
  * Receives commands coming as broadcast from WebSMS.
  * 
  * @author lado
  */
 public class ConnectorMyphone extends Connector {
 	/** Tag for debug output. */
 	private static final String TAG = "WebSMS.myphone.ge";
 
 	/** Login URL, to send Login (POST). */
 	private static final String LOGIN_URL = "https://myaccount.myphone.ge";
 
 	/** Send SMS URL(POST) / Free SMS Count URL(GET). */
 	private static final String SMS_URL = "https://myaccount.myphone.ge/ajax/sms/send.php";
 
 	/** Get Balance - > {"balance":"16.28","livesupporturl":"livesupport_on"} **/
 	private static final String BALANCE_URL = "https://myaccount.myphone.ge/ajax/account/getbalance.php";
 
 	/** Encoding to use. */
 	private static final String ENCODING = "UTF-8";
 
 	/** HTTP Header User-Agent. */
 	private static final String FAKE_USER_AGENT = "Mozilla/5.0 (Windows; U;"
 			+ " Windows NT 5.1; de; rv:1.9.0.9) Gecko/2009040821"
 			+ " Firefox/3.0.9 (.NET CLR 3.5.30729)";
 
 	/** This String will be matched if the user is logged in. */
 	private static final String MATCH_LOGIN_SUCCESS = "control_panel_logout";
 
 	/**
 	 * Pattern to extract free sms count from sms page. Looks like.
 	 */
 	private static final Pattern BALANCE_MATCH_PATTERN = Pattern.compile(
 			"<span id=\"balance\">(.+?)</span>", Pattern.DOTALL);
 
 	private static final String PAGE_ENCODING = "UTF-8";
 	// 16.03.2010 07:10:00
 	private static final SimpleDateFormat format = new SimpleDateFormat(
 			"dd.MM.yyyy HH:mm:ss");
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final ConnectorSpec initSpec(final Context context) {
 		final String name = context.getString(R.string.myphone_name);
 		final ConnectorSpec c = new ConnectorSpec(TAG, name);
 		c.setAuthor(// .
 				context.getString(R.string.myphone_author));
 		c.setBalance(null);
 		c.setPrefsTitle(context.getString(R.string.preferences));
 
 		c.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
 				| ConnectorSpec.CAPABILITIES_SEND
 				| ConnectorSpec.CAPABILITIES_PREFS);
 		c.addSubConnector(TAG, c.getName(),
 				SubConnectorSpec.FEATURE_CUSTOMSENDER);
 		// | SubConnectorSpec.FEATURE_SENDLATER);//TODO fix me
 		// | SubConnectorSpec.FEATURE_SENDLATER_QUARTERS);
 		return c;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final ConnectorSpec updateSpec(final Context context,
 			final ConnectorSpec connectorSpec) {
 		final SharedPreferences p = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		if (p.getBoolean(Preferences.ENABLED, false)) {
 			if (p.getString(Preferences.PASSWORD, "").length() > 0) {
 				connectorSpec.setReady();
 			} else {
 				connectorSpec.setStatus(ConnectorSpec.STATUS_ENABLED);
 			}
 		} else {
 			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
 		}
 		return connectorSpec;
 	}
 
 	/**
 	 * b This post data is needed for log in.
 	 * 
 	 * @param username
 	 *            username
 	 * @param password
 	 *            password
 	 * @return array of params
 	 * @throws UnsupportedEncodingException
 	 *             if the url can not be encoded
 	 */
 	private static String getLoginPost(final String username,
 			final String password) throws UnsupportedEncodingException {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("user=");
 		sb.append(URLEncoder.encode(username, ENCODING));
 		sb.append("&pass=");
 		sb.append(URLEncoder.encode(password, ENCODING));
 		return sb.toString();
 	}
 
 	/**
 	 * These post data is needed for sending a sms.
 	 * 
 	 * @param ctx
 	 *            {@link ConnectorContext}
 	 * @return array of params
 	 * @throws Exception
 	 *             if an error occures.
 	 */
 	private String getSmsPost(final ConnectorContext ctx) throws Exception {
 		final SharedPreferences p = ctx.getPreferences();
 		final StringBuilder sb = new StringBuilder();
 		final String[] to = ctx.getCommand().getRecipients();
		String delimiter = ",";
		for (int i = 0; i < to.length; ++i) {
			if (i == to.length - 1) {
				delimiter = "";
			}
			sb.append(Utils.getRecipientsNumber(to[i])).append(delimiter);

 		}
 		// data: {
 		// rcpt: this.target,
 		// sender: sender,
 		// sendernum: $pick(sendernum, ''),
 		// message: text,
 		// schedule: (this.options.schedule || ''),
 		// parentid: (parentid || ''),
 		// parenttype: (parenttype || ''),
 		// eventtype: (eventtype || ''),
 		// send: true
 		// },
 
 		final StringBuilder sb1 = new StringBuilder();
 		sb1.append("rcpt=");
 		// 
 		sb1.append(URLEncoder.encode(sb.toString(), PAGE_ENCODING));
 
 		String sender = Utils.getSender(ctx.getContext(), ctx.getCommand()
 				.getDefSender());
 		sender = URLEncoder.encode(sender, PAGE_ENCODING);
 		sb1.append("&sender=");
 		sb1.append("&sendernum=").append(sender);
 		sb1.append("&message=");
 		sb1
 				.append(URLEncoder.encode(ctx.getCommand().getText(),
 						PAGE_ENCODING));
 		final long sendLater = ctx.getCommand().getSendLater();
 		sb1.append("&schedule=");
 		if (sendLater > 0) {
 			final String late = format.format(new Date(sender));
 			sb1.append(URLDecoder.decode(late, PAGE_ENCODING));
 		}
 		sb1.append("&parentid=");
 		sb1.append("&parenttype=");
 		sb1.append("&eventtype=");
 		sb1.append("&send=true");
 		final String post = sb1.toString();
 		Log.d(TAG, "request: " + post);
 		return post;
 	}
 
 	/**
 	 * Login to arcor.
 	 * 
 	 * @param ctx
 	 *            {@link ConnectorContext}
 	 * @return true if successfullu logged in, false otherwise.
 	 * @throws WebSMSException
 	 *             if any Exception occures.
 	 */
 	private boolean login(final ConnectorContext ctx) throws WebSMSException {
 		try {
 
 			final SharedPreferences p = ctx.getPreferences();
 			final HttpPost request = createPOST(LOGIN_URL, getLoginPost(p
 					.getString(Preferences.USERNAME, ""), p.getString(
 					Preferences.PASSWORD, "")));
 			final HttpResponse response = ctx.getClient().execute(request);
 			final String cutContent = Utils.stream2str(response.getEntity()
 					.getContent());
 			if (cutContent.indexOf(MATCH_LOGIN_SUCCESS) == -1) {
 				throw new WebSMSException(ctx.getContext(), R.string.error_pw);
 			}
 
 			notifyFreeCount(ctx, cutContent);
 
 		} catch (final Exception e) {
 			throw new WebSMSException(e.getMessage());
 		}
 		return true;
 	}
 
 	/**
 	 * Create and Prepare a Post Request. Set also an User-Agent
 	 * 
 	 * @param url
 	 *            http post url
 	 * @param urlencodedparams
 	 *            key=value pairs as url encoded string
 	 * @return HttpPost
 	 * @throws Exception
 	 *             if an error occures
 	 */
 	private static HttpPost createPOST(final String url,
 			final String urlencodedparams) throws Exception {
 		final HttpPost post = new HttpPost(url);
 		post.setHeader("User-Agent", FAKE_USER_AGENT);
 		post.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,
 				URLEncodedUtils.CONTENT_TYPE));
 		post.setEntity(new StringEntity(urlencodedparams));
 		return post;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doUpdate(final Context context, final Intent intent)
 			throws WebSMSException {
 		final ConnectorContext ctx = ConnectorContext.create(context, intent);
 		this.login(ctx);
 	}
 
 	/**
 	 * Sends an sms via HTTP POST.
 	 * 
 	 * @param ctx
 	 *            {@link ConnectorContext}
 	 * @return successfull?
 	 * @throws WebSMSException
 	 *             on an error
 	 */
 	private boolean sendSms(final ConnectorContext ctx) throws WebSMSException {
 		try {
 			final HttpResponse response = ctx.getClient().execute(
 					createPOST(SMS_URL, this.getSmsPost(ctx)));
 			final boolean sent = this.afterSmsSent(ctx, response);
 			if (sent) {
 				notifyBalance(ctx, ctx.getClient());
 			}
 			return sent;
 		} catch (final Exception ex) {
 			throw new WebSMSException(ex.getMessage());
 		}
 	}
 
 	private void notifyBalance(final ConnectorContext ctx,
 			final DefaultHttpClient client) {
 		try {
 			final HttpResponse respone = client
 					.execute(new HttpGet(BALANCE_URL));
 			if (respone.getStatusLine().getStatusCode() != 200) {
 				Log.w(TAG, "notifyBalance: " + respone.getStatusLine());
 				return;
 			}
 			final JSONObject jo = new JSONObject(Utils.stream2str(respone
 					.getEntity().getContent()));
 			final String balance = jo.getString("balance");
 			this.getSpec(ctx.getContext()).setBalance(balance);
 
 		} catch (final Exception ex) {
 			Log.w(TAG, "notifyBalance: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * Handles content after sms sending.
 	 * 
 	 * @param ctx
 	 *            {@link ConnectorContext}
 	 * @param response
 	 *            HTTP Response
 	 * @return true if arcor returns success
 	 * @throws Exception
 	 *             if an Error occures
 	 */
 	private boolean afterSmsSent(final ConnectorContext ctx,
 			final HttpResponse response) throws Exception {
 
 		final String body = Utils.stream2str(response.getEntity().getContent());
 
 		Log.d(TAG, "response: " + body);
 
 		if (body == null || body.length() == 0) {
 			throw new WebSMSException("response.empty");// TODO
 		}
 
 		JSONObject jo = null;
 		try {
 			jo = new JSONObject(body);
 		} catch (final JSONException e) {
 			throw new WebSMSException("respone.nojson");
 		}
 
 		if (jo.has("result")) {
 			jo = jo.getJSONObject("result");
 		}
 		final int faultCode = jo.getInt("faultCode");
 
 		final String faultString = jo.getString("faultString");
 
 		if (faultCode != 200) {
 			throw new WebSMSException(faultString);
 		}
 		return true;
 	}
 
 	/**
 	 * Push SMS Free Count to WebSMS.
 	 * 
 	 * @param ctx
 	 *            {@link ConnectorContext}
 	 * @param content
 	 *            conten to investigate.
 	 */
 	private void notifyFreeCount(final ConnectorContext ctx,
 			final String content) {
 		final Matcher m = BALANCE_MATCH_PATTERN.matcher(content);
 		String term = null;
 		if (m.find()) {
 			term = m.group(1) + "l";
 		} else {
 			Log.w(TAG, content);
 			term = "?";
 		}
 		this.getSpec(ctx.getContext()).setBalance(term);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doSend(final Context context, final Intent intent)
 			throws WebSMSException {
 		final ConnectorContext ctx = ConnectorContext.create(context, intent);
 		if (this.login(ctx)) {
 			this.sendSms(ctx);
 		}
 
 	}
 }
