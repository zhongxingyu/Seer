 /*
  * Copyright (C) 2012 Felix Bechstein
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
 package de.ub0r.android.websms.connector.messagedj;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import de.ub0r.android.websms.connector.common.BasicConnector;
 import de.ub0r.android.websms.connector.common.BasicSMSLengthCalculator;
 import de.ub0r.android.websms.connector.common.ConnectorCommand;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 import de.ub0r.android.websms.connector.common.Log;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 
 /**
  * AsyncTask to manage IO to message.dj API.
  * 
  * @author flx
  */
 public final class ConnectorMessagedj extends BasicConnector {
 	/** Tag for output. */
 	static final String TAG = "message.dj";
 
 	/** Internal balance. */
 	private String balance;
 
 	/** message.dj gateway URL. */
 	static final String URL = "http://www.message.dj/gateway.php";
 
 	@Override
 	public ConnectorSpec initSpec(final Context context) {
 		String name = context.getString(R.string.connector_name);
 		ConnectorSpec c = new ConnectorSpec(name);
 		c.setAuthor(// .
 		context.getString(R.string.connector_author));
 		c.setBalance(null);
 		c.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
 				| ConnectorSpec.CAPABILITIES_SEND
 				| ConnectorSpec.CAPABILITIES_PREFS);
 		c.addSubConnector("0", name, SubConnectorSpec.FEATURE_MULTIRECIPIENTS);
 		return c;
 	}
 
 	@Override
 	public ConnectorSpec updateSpec(final Context context,
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
 		int maxlength = 0;
 		try {
 			maxlength = Integer.parseInt(p.getString(
 					Preferences.PREFS_SMSLENGTH, "0"));
 		} catch (NumberFormatException e) {
 			Log.w(TAG, "not a valid max sms length", e);
 		}
 		Log.d(TAG, "current maxlength: " + maxlength);
 		if (maxlength <= 0) {
 			connectorSpec.setLimitLength(1000);
 			connectorSpec.setSMSLengthCalculator(null);
 		} else {
 			connectorSpec.setLimitLength(maxlength);
 			connectorSpec.setSMSLengthCalculator(new BasicSMSLengthCalculator(
 					new int[] { maxlength }));
 		}
 		return connectorSpec;
 	}
 
 	/**
 	 * Check return code from message.dj.
 	 * 
 	 * @param context
 	 *            {@link Context}
 	 * @param ret
 	 *            return code
 	 * @return true if no error code
 	 */
 	private static boolean checkReturnCode(final Context context, final int ret) {
 		Log.d(TAG, "ret=" + ret);
 		switch (ret) {
 		case 100:
 			return true;
 		case 120:
 			throw new WebSMSException(context, R.string.connector_error_120);
 		case 130:
 			throw new WebSMSException(context, R.string.connector_error_130);
 		case 131:
 			throw new WebSMSException(context, R.string.connector_error_131);
 		case 132:
 			throw new WebSMSException(context, R.string.connector_error_132);
 		case 133:
 			throw new WebSMSException(context, R.string.connector_error_133);
 		case 134:
 			throw new WebSMSException(context, R.string.connector_error_134);
 		case 135:
 			throw new WebSMSException(context, R.string.connector_error_135);
 		case 136:
 			throw new WebSMSException(context, R.string.connector_error_136);
 		case 137:
 			throw new WebSMSException(context, R.string.connector_error_137);
 		case 140:
 			throw new WebSMSException(context, R.string.connector_error_140);
 		case 174:
 			throw new WebSMSException(context, R.string.connector_error_174);
 		case 175:
 			throw new WebSMSException(context, R.string.connector_error_175);
 		case 180:
 			throw new WebSMSException(context, R.string.connector_error_180);
 		case 404:
 			throw new WebSMSException(context, R.string.connector_error_404);
 		default:
 			throw new WebSMSException(context, R.string.error, " code: " + ret);
 		}
 	}
 
 	@Override
 	protected String getParamUsername() {
 		return "aid";
 	}
 
 	@Override
 	protected String getParamPassword() {
 		return "appkey";
 	}
 
 	@Override
 	protected String getParamRecipients() {
 		return "to";
 	}
 
 	@Override
 	protected String getParamSender() {
 		return null;
 	}
 
 	@Override
 	protected String getParamText() {
 		return "text";
 	}
 
 	@Override
 	protected String getUsername(final Context context,
 			final ConnectorCommand command, final ConnectorSpec cs) {
 		return "1725";
 	}
 
 	@Override
 	protected String getPassword(final Context context,
 			final ConnectorCommand command, final ConnectorSpec cs) {
 		return PreferenceManager.getDefaultSharedPreferences(context)
 				.getString(Preferences.PREFS_PASSWORD, "");
 	}
 
 	@Override
 	protected String getRecipients(final ConnectorCommand command) {
 		return Utils.joinRecipientsNumbers(
 				Utils.national2international(command.getDefPrefix(),
 						command.getRecipients()), ";", true).replaceAll("\\+",
 				"00");
 	}
 
 	@Override
 	protected String getSender(final Context context,
 			final ConnectorCommand command, final ConnectorSpec cs) {
 		return null;
 	}
 
 	@Override
 	protected String getUrlBalance(final ArrayList<BasicNameValuePair> d) {
 		d.add(new BasicNameValuePair("request", "credits"));
 		return URL;
 	}
 
 	@Override
 	protected String getUrlSend(final ArrayList<BasicNameValuePair> d) {
 		return URL;
 	}
 
 	@Override
 	protected boolean usePost(final ConnectorCommand command) {
 		return false;
 	}
 
 	@Override
 	protected String getParamSubconnector() {
 		return "type";
 	}
 
 	@Override
 	protected void parseResponse(final Context context,
 			final ConnectorCommand command, final ConnectorSpec cs,
 			final String htmlText) {
 		if (command.getType() == ConnectorCommand.TYPE_UPDATE) {
 			if (TextUtils.isEmpty(htmlText)) {
 				throw new WebSMSException(context, R.string.connector_error_140);
 			}
 			if (htmlText.contains(".")) {
 				cs.setBalance(this.balance
 						+ htmlText.trim().replaceAll("0*$", "")
 								.replaceAll("\\.$", "") + "\u20AC");
 			} else {
				try {
					checkReturnCode(context, Integer.parseInt(htmlText.trim()));
				} catch (NumberFormatException e) {
					throw new WebSMSException(context, R.string.error, " code: " + htmlText.trim());
				}
 			}
 		} else if (command.getType() == ConnectorCommand.TYPE_SEND) {
 			if (TextUtils.isEmpty(htmlText)) {
 				throw new WebSMSException(context, R.string.error);
 			}
 			int resp = -1;
 			try {
 				resp = Integer.parseInt(htmlText.trim());
 			} catch (NumberFormatException e) {
 				throw new WebSMSException(context, R.string.error);
 			}
 			checkReturnCode(context, resp);
 		} else {
 			throw new IllegalArgumentException("unknown ConnectorCommand: "
 					+ command.getType());
 		}
 	}
 
 	@Override
 	protected void doUpdate(final Context context, final Intent intent)
 			throws IOException {
 		URL u = new URL(URL + "?appkey="
 				+ this.getPassword(context, null, null) + "&"
 				+ this.getParamUsername() + "="
 				+ this.getUsername(context, null, null) + "&request=free");
 		HttpURLConnection con = (HttpURLConnection) u.openConnection();
 		String res = Utils.stream2str(con.getInputStream());
 		if (!TextUtils.isEmpty(res)) {
 			this.balance = res + " + ";
 		} else {
 			this.balance = "";
 		}
 		super.doUpdate(context, intent);
 	}
 }
