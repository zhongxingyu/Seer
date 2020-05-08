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
 package de.ub0r.android.websms.connector.betamax;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpResponse;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import de.ub0r.android.websms.connector.common.Connector;
 import de.ub0r.android.websms.connector.common.ConnectorCommand;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 
 /**
  * AsyncTask to manage IO to betamax API.
  * 
  * @author flx
  */
 public class ConnectorBetamax extends Connector {
 	
 	/** Tag for debug output. */
 	private static final String TAG = "WebSMS.betamax";
 	/** SmsBug Gateway URL. */
 	private static final String URL_SEND = "/myaccount/sendsms.php";
 	/** SmsBug Gateway URL. */
 	private static final String URL_BALANCE = "/myaccount/getbalance.php";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public final ConnectorSpec initSpec(final Context context) {
 		final String name = context.getString(R.string.connector_betamax_name);
 		ConnectorSpec c = new ConnectorSpec(name);
 		c.setAuthor(context.getString(R.string.connector_betamax_author));
 		c.setBalance(null);
 		c.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
 				| ConnectorSpec.CAPABILITIES_SEND
 				| ConnectorSpec.CAPABILITIES_PREFS);
 		c.addSubConnector(TAG, name, SubConnectorSpec.FEATURE_MULTIRECIPIENTS);
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
 		if (p.getBoolean(Preferences.PREFS_ENABLED, false)) {
 			if (p.getString(Preferences.PREFS_PASSWORD, "").length() > 0) {
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
 	 * Check return code from smsbug.com.
 	 * 
 	 * @param context
 	 *            {@link Context}
 	 * @param ret
 	 *            return code
 	 * @return true if no error code
 	 * @throws WebSMSException
 	 *             WebSMSException
 	 */
 	private boolean checkReturnCode(final Context context, final int ret)
 			throws WebSMSException {
 		Log.d(TAG, "ret=" + ret);
 		if (ret < 200) {
 			return true;
 		} else if (ret < 300) {
 			throw new WebSMSException(context, R.string.error_input);
 		} else {
 			if (ret == 401) {
 				throw new WebSMSException(context, R.string.error_pw);
 			}
 			throw new WebSMSException(context, R.string.error_server, // .
 					" " + ret);
 		}
 	}
 
 	/**
 	 * Send data.
 	 * 
 	 * @param context
 	 *            {@link Context}
 	 * @param command
 	 *            {@link ConnectorCommand}
 	 * @throws WebSMSException
 	 *             WebSMSException
 	 */
 	private void sendData(final Context context, final ConnectorCommand command)
 			throws WebSMSException {
 		// do IO
 		try { // get Connection
 			final String text = command.getText();
 			final boolean checkOnly = (text == null || text.length() == 0);
 			final StringBuilder url = new StringBuilder();
 			final ConnectorSpec cs = this.getSpec(context);
 			final SharedPreferences p = PreferenceManager
 					.getDefaultSharedPreferences(context);
 
 			url.append("https://");
 			url.append(p.getString(Preferences.PREFS_DOMAIN, "www.rynga.com"));
 
 			if (checkOnly) {
 				url.append(URL_BALANCE);
 			} else {
 				url.append(URL_SEND);
 			}
 			url.append("?from=");
 
 			url.append(URLEncoder.encode(Utils.getSender(context, command.getDefSender()))
 					.replace("+", ""));
 			url.append("&username=");
 			url.append(URLEncoder.encode(p.getString(Preferences.PREFS_USER, "")));
 			url.append("&password=");
 			url.append(URLEncoder.encode(p.getString(Preferences.PREFS_PASSWORD, "")));
 
 			if (!checkOnly) {
 				url.append("&text=");
 				url.append(URLEncoder.encode(text));
 				url.append("&to=");
 				url.append(URLEncoder.encode(Utils.national2international(command.getDefPrefix(),
 						Utils.getRecipientsNumber(command.getRecipients()[0]))
 						.substring(1)));
 
 			}
 			
 			// send data
 			HttpResponse response = Utils.getHttpClient(url.toString(), null,
 					null, null, null);
 			int resp = response.getStatusLine().getStatusCode();
 			if (resp != HttpURLConnection.HTTP_OK) {
 				this.checkReturnCode(context, resp);
 				throw new WebSMSException(context, R.string.error_http, " "
 						+ resp);
 			}
 			
 			
 			if (checkOnly) {
 				InputStream htmlStream = response.getEntity().getContent();
 				String htmlText = Utils.stream2str(htmlStream).trim();
 				String[] lines = htmlText.split("\n");
 				htmlText = null;
 				for (String s : lines) {
 					cs.setBalance(s.replace("| &#8364;", "\u20AC"));
 				}
 		    
 			} else {
 				// Parse XML response looking for resultstring value
 				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			    Document doc = dBuilder.parse(response.getEntity().getContent());
 			    doc.getDocumentElement().normalize();
 			    Integer nValue = Integer.parseInt(doc.getElementsByTagName("result").item(0).getChildNodes().item(0).getNodeValue());
 			    String nValueString = doc.getElementsByTagName("resultstring").item(0).getChildNodes().item(0).getNodeValue();
 
 				// Use WebSMSException for failure messages
 				if (nValue < 1) {
 					Log.d(TAG, "failed to send message via Betamax vendor, response following:");
 					Log.d(TAG, nValueString);
 					throw new WebSMSException(context, R.string.error_sending);
 				}
 			}
 		} catch (IOException e) {
 			Log.e(TAG, null, e);
 			throw new WebSMSException(e.getMessage());
 		} catch (ParserConfigurationException e) {
 			Log.e(TAG, null, e);
 		} catch (SAXException e) {
 			Log.e(TAG, null, e);
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doUpdate(final Context context, final Intent intent)
 			throws WebSMSException {
 		this.sendData(context, new ConnectorCommand(intent));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doSend(final Context context, final Intent intent)
 			throws WebSMSException {
 		this.sendData(context, new ConnectorCommand(intent));
 	}		 
 }
