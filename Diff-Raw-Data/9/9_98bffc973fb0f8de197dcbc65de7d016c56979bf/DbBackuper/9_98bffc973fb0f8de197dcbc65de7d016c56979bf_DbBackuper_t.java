 package v6.apps.clients.backup.db;
 
 //import v6.Debug;
 //import static Debug.*;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import lombok.SneakyThrows;
 
 class DbBackuper implements Runnable {
 
 	final static String OK_MARK = "-- OK";
 
 	private final static Pattern TABLE_PATTERN = Pattern
 			.compile("^[a-zA-Z0-9\\-\\_]+$");
 
 	private final String urlBase;
 
 	private String pwd;
 
 	String filePrefix;
 
 	int errs = 0;
 
 	boolean backupTableDefinition = true, backupTableData = true;
 
 	/*private UpdateStrategy<URLConnection> connectionModifer = new UpdateStrategy<URLConnection>() {
 		@Override */
 	@SneakyThrows(UnsupportedEncodingException.class)
 	public void modify(URLConnection c) throws IOException{
 		c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		PrintWriter out = new PrintWriter(c.getOutputStream());
 		out.print("password=" + URLEncoder.encode(pwd, "utf-8"));
 		out.close();
 	}
 	//};
 
 	public DbBackuper(String url, String pwd) {
 		urlBase = url;
 		this.pwd = pwd;
 	}
 
 	public DbBackuper setBackupTableDefinition(boolean v) {
 		backupTableDefinition = v;
 		return this;
 	}
 
 	public DbBackuper setBackupTableData(boolean v) {
 		backupTableData = v;
 		return this;
 	}
 
 	// Modirer<URL> urlModifer = new Modifer()
 
 	public void run() {
 		{
 			Calendar cal = Calendar.getInstance();
 			ByteArrayOutputStream res = new ByteArrayOutputStream(4 + 1 + 2 + 1
 					+ 2 + 1 + 1 + 1 + 2 + 1 + 2 + 1 + 2 + 1);
 			PrintWriter wr = new PrintWriter(res, true);
 			wr.format("%04d-%02d-%02d--%02d-%02d-%02d", cal.get(Calendar.YEAR),
 					cal.get(Calendar.MONTH) + 1,
 					cal.get(Calendar.DAY_OF_MONTH), cal
 							.get(Calendar.HOUR_OF_DAY), cal
 							.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
 			new File(res.toString()).mkdir();
 			filePrefix = res + System.getProperty("file.separator");
 		}
 		List<Thread> thrs = new LinkedList<Thread>();
 		try {
 			HttpURLConnection conn = (HttpURLConnection) new URL(urlBase
 					+ "?action=tables").openConnection();
 			conn.setDoInput(true);
 			conn.setDoOutput(true);
 			conn.setRequestMethod("POST");
 			modify(conn);
 			BufferedReader in = new BufferedReader(new InputStreamReader(conn
 					.getInputStream()));
 			String table, lastline = "";
 			while ((table = in.readLine()) != null) {
 				lastline = table;
 				if (table.equals("") || table.equals(OK_MARK)) {
 					continue;
 				}
 				if (!TABLE_PATTERN.matcher(table).matches()) {
 					System.out.println("(!) nazev tabulky '" + table
 							+ "' neodpovida formatu.");
 					errs++;
 					continue;
 				}
 				Thread t = new Thread(new TableBackuper(this, urlBase, table));
 				t.start();
 				thrs.add(t);
 			}
 			if (!lastline.equals(OK_MARK)) {
 				System.out
 						.println("(!) vypis tabulek nebyl ziskan uspesne! Zprava:"
 								+ lastline);
 				errs++;
 			}
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		// Thread t;
 		try {
 			for (Iterator<Thread> it = thrs.iterator(); it.hasNext();) {
 				it.next().join();
 			}
 			if (errs == 0) {
 				System.out.println("(++) Zalohovani problehlo vporadku.");
 			} else {
 				System.out.println("(!!) Pocet chyb pri zalohovani: " + errs);
 			}
 		} catch (InterruptedException e) {
			System.out.println("(!!) Interrupted");
 		}
 	}
 
 }
