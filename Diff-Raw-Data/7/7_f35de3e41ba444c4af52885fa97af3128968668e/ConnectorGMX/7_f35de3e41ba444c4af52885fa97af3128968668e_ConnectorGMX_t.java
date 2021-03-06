 package de.ub0r.android.andGMXsms;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 
 /**
  * AsyncTask to manage IO to GMX API.
  * 
  * @author flx
  */
 public class ConnectorGMX extends AsyncTask<String, Boolean, Boolean> {
 	/** Target host. */
 	private static final String TARGET_HOST = "app0.wr-gmbh.de";
 	// private static final String TARGET_HOST = "app5.wr-gmbh.de";
 	/** Target path on host. */
 	private static final String TARGET_PATH = "/WRServer/WRServer.dll/WR";
 	/** Target mime encoding. */
 	private static final String TARGET_ENCODING = "wr-cs";
 	/** Target mime type. */
 	private static final String TARGET_CONTENT = "text/plain";
 	/** HTTP Useragent. */
 	private static final String TARGET_AGENT = "Mozilla/3.0 (compatible)";
 	/** Target version of protocol. */
 	private static final String TARGET_PROTOVERSION = "1.13.03";
 
 	/** SMS DB: address. */
 	public static final String ADDRESS = "address";
 	/** SMS DB: person. */
 	// private static final String PERSON = "person";
 	/** SMS DB: date. */
 	// private static final String DATE = "date";
 	/** SMS DB: read. */
 	public static final String READ = "read";
 	/** SMS DB: status. */
 	// private static final String STATUS = "status";
 	/** SMS DB: type. */
 	public static final String TYPE = "type";
 	/** SMS DB: body. */
 	public static final String BODY = "body";
 	/** SMS DB: type - sent. */
 	public static final int MESSAGE_TYPE_SENT = 2;
 
 	/** ID of text in array. */
 	public static final int ID_TEXT = 0;
 	/** ID of recipient in array. */
 	public static final int ID_TO = 1;
 
 	/** ID of mail in array. */
 	public static final int ID_MAIL = 0;
 	/** ID of password in array. */
 	public static final int ID_PW = 1;
 
 	/** Number of IDs in array for sms send. */
 	public static final int IDS_SEND = 3;
 	/** Number of IDs in array for bootstrap. */
 	public static final int IDS_BOOTSTR = 2;
 
 	/** Result: ok. */
 	private static final int RSLT_OK = 0;
 
 	/** Result: wrong customerid/password. */
 	private static final int RSLT_WRONG_CUSTOMER = 11;
 
 	/** Result: wrong mail/password. */
 	private static final int RSLT_WRONG_MAIL = 25;
 
 	/** recipient. */
 	private String[] to;
 	/** recipients list. */
 	private String tos = "";
 	/** text. */
 	private String text;
 
 	/** mail. */
 	private String mail;
 	/** password. */
 	private String pw;
 
 	/** Connector is bootstrapping. */
 	public static boolean inBootstrap = false;
 
 	/**
 	 * Write key,value to StringBuilder.
 	 * 
 	 * @param buffer
 	 *            buffer
 	 * @param key
 	 *            key
 	 * @param value
 	 *            value
 	 */
 	private static void writePair(final StringBuilder buffer, final String key,
 			final String value) {
 		buffer.append(key);
 		buffer.append('=');
 		buffer.append(value.replace("\\", "\\\\").replace(">", "\\>").replace(
 				"<", "\\<"));
 		buffer.append("\\p");
 	}
 
 	/**
 	 * Create default data hashtable.
 	 * 
 	 * @param packetName
 	 *            packetName
 	 * @param packetVersion
 	 *            packetVersion
 	 * @param addCustomer
 	 *            add customer id/password
 	 * @return Hashtable filled with customer_id and password.
 	 */
 	private static StringBuilder openBuffer(final String packetName,
 			final String packetVersion, final boolean addCustomer) {
 		StringBuilder ret = new StringBuilder();
 		ret.append("<WR TYPE=\"RQST\" NAME=\"");
 		ret.append(packetName);
 		ret.append("\" VER=\"");
 		ret.append(packetVersion);
 		ret.append("\" PROGVER=\"");
 		ret.append(TARGET_PROTOVERSION);
 		ret.append("\">");
 		if (addCustomer) {
 			writePair(ret, "customer_id", AndGMXsms.prefsUser);
 			writePair(ret, "password", AndGMXsms.prefsPasswordGMX);
 		}
 		return ret;
 	}
 
 	/**
 	 * Close Buffer.
 	 * 
 	 * @param buffer
 	 *            buffer
 	 * @return buffer
 	 */
 	private static StringBuilder closeBuffer(final StringBuilder buffer) {
 		buffer.append("</WR>");
 		return buffer;
 	}
 
 	/**
 	 * Parse returned packet. Search for name=(.*)\n and return (.*)
 	 * 
 	 * @param packet
 	 *            packet
 	 * @param name
 	 *            parma's name
 	 * @return param's value
 	 */
 	private String getParam(final String packet, final String name) {
 		int i = packet.indexOf(name + '=');
 		if (i < 0) {
 			return null;
 		}
 		int j = packet.indexOf("\n", i);
 		if (j < 0) {
 			return packet.substring(i + name.length() + 1);
 		} else {
 			return packet.substring(i + name.length() + 1, j);
 		}
 	}
 
 	/**
 	 * Send data.
 	 * 
 	 * @param packetData
 	 *            packetData
 	 * @return successful?
 	 */
 	private boolean sendData(final StringBuilder packetData) {
 		try {
 			// get Connection
 			HttpURLConnection c = (HttpURLConnection) (new URL("http://"
 					+ TARGET_HOST + TARGET_PATH)).openConnection();
 			// set prefs
 			c.setRequestProperty("User-Agent", TARGET_AGENT);
 			c.setRequestProperty("Content-Encoding", TARGET_ENCODING);
 			c.setRequestProperty("Content-Type", TARGET_CONTENT);
 			c.setRequestMethod("POST");
 			c.setDoOutput(true);
 			// push post data
 			OutputStream os = c.getOutputStream();
 			os.write(packetData.toString().getBytes("ISO-8859-1"));
 			os.close();
 			os = null;
 
 			// send data
 			int resp = c.getResponseCode();
 			if (resp != HttpURLConnection.HTTP_OK) {
 				AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, AndGMXsms.me
						.getResources().getString(R.string.log_error_http)
						+ " " + resp);
 			}
 			// read received data
 			int bufsize = c.getHeaderFieldInt("Content-Length", -1);
 			if (bufsize > 0) {
 				String resultString = AndGMXsms.stream2String(c
 						.getInputStream());
 				if (resultString.startsWith("The truth")) {
 					// wrong data sent!
 
 					AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, AndGMXsms.me
 							.getResources()
 							.getString(R.string.log_error_server)
							+ " " + resultString);
 					return false;
 				}
 
 				// strip packet
 				int resultIndex = resultString.indexOf("rslt=");
 				String outp = resultString.substring(resultIndex).replace(
 						"\\p", "\n");
 				outp = outp.replace("</WR>", "");
 
 				// get result code
 				String resultValue = this.getParam(outp, "rslt");
 				int rslt;
 				try {
 					rslt = Integer.parseInt(resultValue);
 				} catch (Exception e) {
 					AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, e.toString());
 					return false;
 				}
 				switch (rslt) {
 				case RSLT_OK: // ok
 					// fetch additional info
 					String p = this.getParam(outp, "free_rem_month");
 					if (p != null) {
 						AndGMXsms.smsGMXfree = Integer.parseInt(p);
 						p = this.getParam(outp, "free_max_month");
 						if (p != null) {
 							AndGMXsms.smsGMXlimit = Integer.parseInt(p);
 						}
 						AndGMXsms
 								.sendMessage(AndGMXsms.MESSAGE_FREECOUNT, null);
 					}
 					p = this.getParam(outp, "customer_id");
 					if (p != null) {
 						AndGMXsms.prefsUser = p;
 						if (this.pw != null) {
 							AndGMXsms.prefsPasswordGMX = this.pw;
 						}
 						if (this.mail != null) {
 							AndGMXsms.prefsMail = this.mail;
 						}
 						AndGMXsms.me.savePreferences();
 						inBootstrap = false;
 						AndGMXsms.sendMessage(AndGMXsms.MESSAGE_PREFSREADY,
 								null);
 					}
 					return true;
 				case RSLT_WRONG_CUSTOMER: // wrong user/pw
 					AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, AndGMXsms.me
 							.getResources().getString(R.string.log_error_pw));
 					return false;
 				case RSLT_WRONG_MAIL: // wrong mail/pw
 					inBootstrap = false;
 					AndGMXsms.prefsPasswordGMX = "";
 					AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, AndGMXsms.me
 							.getResources().getString(R.string.log_error_mail));
 					AndGMXsms.sendMessage(AndGMXsms.MESSAGE_PREFSREADY, null);
 					return false;
 				default:
 					AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, outp + "#"
 							+ rslt);
 					return false;
 				}
 			} else {
 				AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, AndGMXsms.me
 						.getResources().getString(
 								R.string.log_http_header_missing));
 				return false;
 			}
 		} catch (IOException e) {
 			AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, e.toString());
 			return false;
 		}
 	}
 
 	/**
 	 * Get free sms count.
 	 * 
 	 * @return ok?
 	 */
 	private boolean getFree() {
 		return this.sendData(closeBuffer(openBuffer("GET_SMS_CREDITS", "1.00",
 				true)));
 	}
 
 	/**
 	 * Send sms.
 	 * 
 	 * @return ok?
 	 */
 	private boolean send() {
 		AndGMXsms.sendMessage(AndGMXsms.MESSAGE_DISPLAY_ADS, null);
 		StringBuilder packetData = openBuffer("SEND_SMS", "1.01", true);
 		// fill buffer
 		writePair(packetData, "sms_text", this.text);
 		StringBuilder recipients = new StringBuilder();
 		// table: <id>, <name>, <number>
 		int j = 0;
 		for (int i = 1; i < this.to.length; i++) {
 			if (this.to[i] != null && this.to[i].length() > 1) {
 				recipients.append(++j);
 				recipients.append("\\;null\\;");
 				recipients.append(this.to[i]);
 				recipients.append("\\;");
 				if (j > 1) {
 					this.tos += ", ";
 				}
 				this.tos += this.to[i];
 			}
 		}
 		this.publishProgress((Boolean) null);
 		recipients.append("</TBL>");
 		String recipientsString = "<TBL ROWS=\"" + j + "\" COLS=\"3\">"
 				+ "receiver_id\\;receiver_name\\;receiver_number\\;"
 				+ recipients.toString();
 		recipients = null;
 		writePair(packetData, "receivers", recipientsString);
 		writePair(packetData, "send_option", "sms");
 		writePair(packetData, "sms_sender", AndGMXsms.prefsSender);
 		// if date!='': data['send_date'] = date
 		// push data
 		if (!this.sendData(closeBuffer(packetData))) {
 			// failed!
 			AndGMXsms.sendMessage(AndGMXsms.MESSAGE_LOG, AndGMXsms.me
 					.getResources().getString(R.string.log_error));
 			return false;
 		} else {
 			// result: ok
 			AndGMXsms.sendMessage(AndGMXsms.MESSAGE_RESET, null);
 			AndGMXsms.saveMessage(this.to, this.text);
 			return true;
 		}
 	}
 
 	/**
 	 * Bootstrap: Get preferences.
 	 * 
 	 * @return ok?
 	 */
 	private boolean bootstrap() {
 		inBootstrap = true;
 		StringBuilder packetData = openBuffer("GET_CUSTOMER", "1.10", false);
 		writePair(packetData, "email_address", this.mail);
 		writePair(packetData, "password", this.pw);
 		writePair(packetData, "gmx", "1");
 		return this.sendData(closeBuffer(packetData));
 	}
 
 	/**
 	 * Run IO in background.
 	 * 
 	 * @param textTo
 	 *            (text,recipient)
 	 * @return ok?
 	 */
 	@Override
 	protected final Boolean doInBackground(final String... textTo) {
 		boolean ret = false;
 		if (textTo == null || textTo[0] == null) {
 			this.publishProgress((Boolean) null);
 			ret = this.getFree();
 		} else if (textTo.length == IDS_BOOTSTR) {
 			this.mail = textTo[ID_MAIL];
 			this.pw = textTo[ID_PW];
 			this.publishProgress((Boolean) null);
 			ret = this.bootstrap();
 		} else {
 			this.text = textTo[ID_TEXT];
 			this.to = textTo;
 			this.publishProgress((Boolean) null);
 			ret = this.send();
 		}
 		return new Boolean(ret);
 	}
 
 	/**
 	 * Update progress. Only ran once on startup to display progress dialog.
 	 * 
 	 * @param progress
 	 *            finished?
 	 */
 	@Override
 	protected final void onProgressUpdate(final Boolean... progress) {
 		if (AndGMXsms.dialog != null) {
 			try {
 				AndGMXsms.dialog.dismiss();
 			} catch (Exception e) {
 				// do nothing
 			}
 		}
 		if (this.to == null) {
 			if (this.mail == null) {
 				AndGMXsms.dialogString = AndGMXsms.me.getResources().getString(
 						R.string.log_update);
 				AndGMXsms.dialog = ProgressDialog.show(AndGMXsms.me, null,
 						AndGMXsms.dialogString, true);
 			} else {
 				AndGMXsms.dialogString = AndGMXsms.me.getResources().getString(
 						R.string.bootstrap_);
 				AndGMXsms.dialog = ProgressDialog.show(AndGMXsms.me, null,
 						AndGMXsms.dialogString, true);
 			}
 		} else {
 			AndGMXsms.dialogString = AndGMXsms.me.getResources().getString(
 					R.string.log_sending);
 			if (this.tos != null && this.tos.length() > 0) {
 				AndGMXsms.dialogString += " (" + this.tos + ")";
 			}
 			AndGMXsms.dialog = ProgressDialog.show(AndGMXsms.me, null,
 					AndGMXsms.dialogString, true);
 		}
 	}
 
 	/**
 	 * Push data back to GUI. Close progress dialog.
 	 * 
 	 * @param result
 	 *            result
 	 */
 	@Override
 	protected final void onPostExecute(final Boolean result) {
 		AndGMXsms.dialogString = null;
 		if (AndGMXsms.dialog != null) {
 			try {
 				AndGMXsms.dialog.dismiss();
 				AndGMXsms.dialog = null;
 			} catch (Exception e) {
 				System.gc();
 			}
 		}
 	}
 }
