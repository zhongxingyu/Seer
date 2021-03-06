 package de.ub0r.android.andGMXsms;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Message;
 
 /**
  * AsyncTask to manage IO to GMX API.
  * 
  * @author flx
  */
 public class Connector extends AsyncTask<String, Boolean, Boolean> {
 	/** Dialog ID. */
 	public static final int DIALOG_IO = 0;
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
 
 	/** Max Buffer size. */
 	private static final int MAX_BUFSIZE = 4096;
 
 	/** SMS DB: address. */
 	private static final String ADDRESS = "address";
 	/** SMS DB: person. */
 	// private static final String PERSON = "person";
 	/** SMS DB: date. */
 	// private static final String DATE = "date";
 	/** SMS DB: read. */
 	private static final String READ = "read";
 	/** SMS DB: status. */
 	// private static final String STATUS = "status";
 	/** SMS DB: type. */
 	private static final String TYPE = "type";
 	/** SMS DB: body. */
 	private static final String BODY = "body";
 	/** SMS DB: type - sent. */
 	private static final int MESSAGE_TYPE_SENT = 2;
 
 	/** ID of text in array. */
 	public static final int ID_TEXT = 0;
 	/** ID of receiver in array. */
 	public static final int ID_TO = 1;
 
 	/** ID of mail in array. */
 	public static final int ID_MAIL = 0;
 	/** ID of password in array. */
 	public static final int ID_PW = 1;
 	/** ID of null in array. */
 	public static final int ID_BOOTSTRAP_NULL = 2;
 
 	/** Result: ok. */
 	private static final int RSLT_OK = 0;
 
 	/** Result: wrong customerid/password. */
 	private static final int RSLT_WRONG_CUSTOMER = 11;
 
 	/** Result: wrong mail/password. */
 	private static final int RSLT_WRONG_MAIL = 25;
 
 	/** receiver. */
 	private String to;
 	/** text. */
 	private String text;
 
 	/** mail. */
 	private String mail;
 	/** password. */
 	private String pw;
 
 	/**
 	 * Write key,value to StringBuffer.
 	 * 
 	 * @param buffer
 	 *            buffer
 	 * @param key
 	 *            key
 	 * @param value
 	 *            value
 	 */
 	private static void writePair(final StringBuffer buffer, final String key,
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
 	private static StringBuffer openBuffer(final String packetName,
 			final String packetVersion, final boolean addCustomer) {
 		StringBuffer ret = new StringBuffer();
 		ret.append("<WR TYPE=\"RQST\" NAME=\"");
 		ret.append(packetName);
 		ret.append("\" VER=\"");
 		ret.append(packetVersion);
 		ret.append("\" PROGVER=\"");
 		ret.append(TARGET_PROTOVERSION);
 		ret.append("\">");
 		if (addCustomer) {
 			writePair(ret, "customer_id", AndGMXsms.prefsUser);
 			writePair(ret, "password", AndGMXsms.prefsPassword);
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
 	private static StringBuffer closeBuffer(final StringBuffer buffer) {
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
 	private boolean sendData(final StringBuffer packetData) {
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
 			os.write(packetData.toString().getBytes());
 			os.close();
 			os = null;
 
 			// send data
 			int resp = c.getResponseCode();
 			if (resp != HttpURLConnection.HTTP_OK) {
 				Message.obtain(
 						AndGMXsms.me.messageHandler,
 						AndGMXsms.MESSAGE_LOG,
 						AndGMXsms.me.getResources().getString(
 								R.string.log_error_http + resp)).sendToTarget();
 			}
 			// read received data
 			int bufsize = c.getHeaderFieldInt("Content-Length", -1);
 			StringBuffer data = null;
 			if (bufsize > 0) {
 				data = new StringBuffer();
 				InputStream is = c.getInputStream();
 				byte[] buf;
 				if (bufsize > MAX_BUFSIZE) {
 					buf = new byte[MAX_BUFSIZE];
 				} else {
 					buf = new byte[bufsize];
 				}
 				int read = is.read(buf, 0, buf.length);
 				int count = read;
 				while (read > 0) {
 					data.append(new String(buf, 0, read, "ASCII"));
 					read = is.read(buf, 0, buf.length);
 					count += read;
 				}
 				buf = null;
 				is.close();
 				is = null;
 				String resultString = data.toString();
 				if (resultString.startsWith("The truth")) {
 					// wrong data sent!
 					Message.obtain(
 							AndGMXsms.me.messageHandler,
 							AndGMXsms.MESSAGE_LOG,
 							AndGMXsms.me.getResources().getString(
 									R.string.log_error_server)
 									+ resultString).sendToTarget();
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
 					Message.obtain(AndGMXsms.me.messageHandler,
 							AndGMXsms.MESSAGE_LOG, e.toString()).sendToTarget();
 					return false;
 				}
 				switch (rslt) {
 				case RSLT_OK: // ok
 					// fetch additional info
 					String p = this.getParam(outp, "free_rem_month");
 					if (p != null) {
 						String freecount = p;
 						p = this.getParam(outp, "free_max_month");
 						if (p != null) {
 							freecount += " / " + p;
 						}
 						Message.obtain(AndGMXsms.me.messageHandler,
 								AndGMXsms.MESSAGE_FREECOUNT, freecount)
 								.sendToTarget();
 					}
 					p = this.getParam(outp, "customer_id");
 					if (p != null) {
 						AndGMXsms.prefsUser = p;
 						AndGMXsms.prefsSender = this.getParam(outp,
 								"cell_phone");
 						Settings.reset();
 						Message.obtain(AndGMXsms.me.messageHandler,
 								AndGMXsms.MESSAGE_SETTINGS).sendToTarget();
 					}
 					return true;
 				case RSLT_WRONG_CUSTOMER: // wrong user/pw
 					Message.obtain(
 							AndGMXsms.me.messageHandler,
 							AndGMXsms.MESSAGE_LOG,
 							AndGMXsms.me.getResources().getString(
									R.string.log_error_pw)).sendToTarget();
 					return false;
 				case RSLT_WRONG_MAIL: // wrong mail/pw
 					Message.obtain(
 							AndGMXsms.me.messageHandler,
 							AndGMXsms.MESSAGE_LOG,
 							AndGMXsms.me.getResources().getString(
 									R.string.log_error_mail)).sendToTarget();
 					return false;
 				default:
 					Message.obtain(AndGMXsms.me.messageHandler,
 							AndGMXsms.MESSAGE_LOG, outp + "#" + rslt)
 							.sendToTarget();
 					return false;
 				}
 			} else {
 				Message.obtain(
 						AndGMXsms.me.messageHandler,
 						AndGMXsms.MESSAGE_LOG,
 						AndGMXsms.me.getResources().getString(
 								R.string.log_http_header_missing))
 						.sendToTarget();
 				return false;
 			}
 		} catch (IOException e) {
 			Message.obtain(AndGMXsms.me.messageHandler, AndGMXsms.MESSAGE_LOG,
 					e.toString()).sendToTarget();
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
 		StringBuffer packetData = openBuffer("SEND_SMS", "1.01", true);
 		// fill buffer
 		writePair(packetData, "sms_text", this.text);
 		// table: <id>, <name>, <number>
 		String receivers = "<TBL ROWS=\"1\" COLS=\"3\">"
 				+ "receiver_id\\;receiver_name\\;receiver_number\\;"
 				+ "1\\;null\\;" + this.to + "\\;" + "</TBL>";
 		writePair(packetData, "receivers", receivers);
 		writePair(packetData, "send_option", "sms");
 		writePair(packetData, "sms_sender", AndGMXsms.prefsSender);
 		// if date!='': data['send_date'] = date
 		// push data
 		if (!this.sendData(closeBuffer(packetData))) {
 			// failed!
 			Message.obtain(AndGMXsms.me.messageHandler, AndGMXsms.MESSAGE_LOG,
 					AndGMXsms.me.getResources().getString(R.string.log_error))
 					.sendToTarget();
 			return false;
 		} else {
 			// result: ok
 			Composer.reset();
 
 			// save sms to content://sms/sent
 			ContentValues values = new ContentValues();
 			values.put(ADDRESS, this.to);
 			// values.put(DATE, "1237080365055");
 			values.put(READ, 1);
 			// values.put(STATUS, -1);
 			values.put(TYPE, MESSAGE_TYPE_SENT);
 			values.put(BODY, this.text);
 			// Uri inserted =
 			AndGMXsms.me.getContentResolver().insert(
 					Uri.parse("content://sms/sent"), values);
 			return true;
 		}
 	}
 
 	/**
 	 * Bootstrap: Get preferences.
 	 * 
 	 * @return ok?
 	 */
 	private boolean bootstrap() {
 		StringBuffer packetData = openBuffer("GET_CUSTOMER", "1.10", false);
 		writePair(packetData, "email_address", this.mail);
 		writePair(packetData, "password", this.pw);
 		writePair(packetData, "gmx", "1");
 		return this.sendData(closeBuffer(packetData));
 	}
 
 	/**
 	 * Run IO in background.
 	 * 
 	 * @param textTo
 	 *            (text,receiver)
 	 * @return ok?
 	 */
 	@Override
 	protected final Boolean doInBackground(final String... textTo) {
 		boolean ret = false;
 		if (textTo == null || textTo[0] == null) {
 			this.publishProgress((Boolean) null);
 			ret = this.getFree();
 		} else if (textTo.length == 2) {
 			this.text = textTo[ID_TEXT];
 			this.to = textTo[ID_TO];
 			this.publishProgress((Boolean) null);
 			ret = this.send();
 		} else {
 			this.mail = textTo[ID_MAIL];
 			this.pw = textTo[ID_PW];
 			this.publishProgress((Boolean) null);
 			ret = this.bootstrap();
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
 					R.string.log_sending)
 					+ " (" + this.to + ")";
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
 				// nothing to do
 			}
 		}
 	}
 }
