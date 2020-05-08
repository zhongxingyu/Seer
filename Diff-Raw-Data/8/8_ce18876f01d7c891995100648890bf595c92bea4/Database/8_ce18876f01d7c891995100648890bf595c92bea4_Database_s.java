 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2009 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.storage;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import org.wwscc.dialogs.BaseDialog.DialogFinisher;
 import org.wwscc.dialogs.DatabaseDialog;
 import org.wwscc.util.MT;
 import org.wwscc.util.Messenger;
 import org.wwscc.util.MultiInputDialog;
 import org.wwscc.util.Prefs;
 
 /**
  *
  * @author bwilson
  */
 public class Database
 {
 	private static Logger log = Logger.getLogger("org.wwscc.storage.Database");
 	public static DataInterface d;
 	public static File file = null;
 
 	static
 	{
 		d = new FakeDatabase();
 	}
 
 	public static void open()
 	{
 		DatabaseDialog dd = new DatabaseDialog(
 				Prefs.getSeriesFile(Prefs.getInstallRoot()+"/series/default.db"),
 				Prefs.getSeriesURL(""),
 				Prefs.useSeriesURL());
 
 		dd.doDialog("Open Series", new DialogFinisher<Object>() {
 			@Override
 			public void dialogFinished(Object object) {
 				try
 				{
 					if (object instanceof File)
 						openDatabaseFile((File)object);
 					else if (object instanceof String)
 						openDatabaseNetwork((String)object);
 				}
 				catch (IOException ioe)
 				{
 					log.severe("Failed to open database: " + ioe);
 				}
 			}
 		});
 	}
 
 	public static void openDefault()
 	{
 		try
 		{
 			if (Prefs.useSeriesURL())
 				openDatabaseNetwork(Prefs.getSeriesURL(""));
 			else if (new File(Prefs.getSeriesFile("")).exists())
 				openDatabaseFile(new File(Prefs.getSeriesFile("")));
 			else
 				openFakeFile();
 		}
 		catch (IOException ioe)
 		{
 			open();
 		}
 	}
 
 	public static void openFakeFile()
 	{
 		d = new FakeDatabase();
 		file = null;
 		Messenger.sendEvent(MT.DATABASE_CHANGED, null);
 		Prefs.setUseSeriesURL(false);
 	}
 	
 	public static void openDatabaseFile(File f) throws IOException
 	{
 		d = new SqliteDatabase(f);
 		file = f;
 		Messenger.sendEvent(MT.DATABASE_CHANGED, null);
 		Prefs.setSeriesFile(f.getAbsolutePath());
 		Prefs.setUseSeriesURL(false);
 	}
 
 	public static void openDatabaseNetwork(String spec) throws IOException
 	{
 		String sp[] = spec.split("/");
 		if (sp.length != 2)
 			throw new IOException("Invalid network spec: " + spec);
 		d = new WebDataSource(sp[0], sp[1]);
 		file = null;
 		Messenger.sendEvent(MT.DATABASE_CHANGED, null);
 		Prefs.setSeriesURL(spec);
 		Prefs.setUseSeriesURL(true);
 	}
 
 	public static void closeDatabase()
 	{
 		d.close();
 		d = null;
 		file = null;
 	}
 
 	static class CancelException extends IOException {}
 
 	private static String getHost() throws IOException
 	{
 		MultiInputDialog dia = new MultiInputDialog("Scorekeeper Server");
 		String host = Prefs.getHomeServer();
 		dia.addString("Hostname", host);
 		if (dia.runDialog())
 		{
 			host = dia.getResponse("Hostname");
 			Prefs.setHomeServer(host);
 			return host;
 		}
 
 		throw new CancelException();
 	}
 
 	public static File download(boolean lockServerSide)
 	{
 		try
 		{
 			RemoteHTTPConnection conn = new RemoteHTTPConnection(getHost());
 			List<String> available[] = conn.getAvailableForCheckout();
 			String dbname = (String)JOptionPane.showInputDialog(null,
 						lockServerSide ? "Select the file to check out" : "Select the file to download",
 						lockServerSide ? "Checkout Database" : "Download Database",
 						JOptionPane.PLAIN_MESSAGE,
 						null, 
 						lockServerSide ? available[0].toArray() : available[2].toArray(),
 						null);
 			if (dbname == null)
 				return null;
 			File out = new File(Prefs.getInstallRoot()+"/series/"+dbname+".db");
 			if (out.exists())
 			{
 				if (!lockServerSide)
 				{
 					if ((file != null) && file.equals(out))
 						d.close();
 					if (!out.delete())
 						throw new IOException("Unable to delete old version already on disk ("+out+")");
 				}
 				else
 					throw new Exception("Can't download file, already exists on local system ("+out+")");
 			}
 
 			conn.downloadDatabase(out, lockServerSide);
 			openDatabaseFile(out);
 			d.clearChanges();
 			return out;
 		}
 		catch (CancelException ex)
 		{
 			return null;
 		}
 		catch (Exception ex)
 		{
			log.severe("Download error: " + ex);
 			return null;
 		}
 	}
 
 	public static void upload()
 	{
 		try
 		{
 			if (file == null)
 				throw new Exception("Current database is not a local file, can't upload");
 
 			RemoteHTTPConnection conn = new RemoteHTTPConnection(getHost());
 			File save = file;
 			closeDatabase();
 			openFakeFile();
 			conn.uploadDatabase(save);
 
 			String newname = save.getName() + "-" + (System.currentTimeMillis()/1000);
 			File back = new File(Prefs.getInstallRoot() + "/backup/" + newname).getCanonicalFile();
 			if (!save.renameTo(back))
 				log.warning("backup/rename failed, file will remain in place for now");
 
 			Messenger.sendEvent(MT.COURSE_CHANGED, null);
 			Messenger.sendEvent(MT.RUNGROUP_CHANGED, null);
 			Prefs.setSeriesFile("");
 		}
 		catch (CancelException ex)
 		{
 			return;
 		}
 		catch (Exception ex)
 		{
			log.severe("Upload error: " + ex);
			ex.printStackTrace();
 		}
 	}
 }
 
