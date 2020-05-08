 ﻿package main;
 
 import java.awt.Dialog.ModalityType;
 import java.io.IOException;
 import java.util.Collection;
 
 import logging.Logger;
 import main.config.Config;
 import model.CurrentDate;
import model.InRent;
 import model.Warning;
 import model.data.DataBase;
 import model.data.exceptions.DataException;
 import GUI.MainWindow;
 import GUI.dialogs.CreatedWarningsDialog;
 import GUI.dialogs.SetCurrentDateDialog;
 
 /**
  * @author java
  * 
  */
 public class Programm
 {
 
 	private static MainWindow mainWindow;
 	private static boolean resetWarnings = false;
 	
 	public static void start()
 	{
 		Logger.get().write("Programm gestartet!");
 		Logger.get().write("CurrentDate wird gesetzt");
 
 		if(Config.get().getSetting(Config.Settings.SETDATEONSTARTUP).equals("true"))
 		{
 			try
 			{
 				SetCurrentDateDialog dialog = new SetCurrentDateDialog(null, true);
 				dialog.setVisible(true);
 				dialog.setModalityType(ModalityType.APPLICATION_MODAL);
 				
 				// falls gewollt, merken, dass mahnungen 
 				// zurückgesetzt werden sollen...
 				resetWarnings = dialog.resetWarnings;
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				Logger.get().write("Fehler beim Setzen von CurrentDate!");
 			}
 		}
 		
 		Logger.get().write("Aktuelles Currentdate ist: " + CurrentDate.get());
 
 		Logger.get().write("XML-Daten werden geladen.");
 
 		try
 		{
 			DataBase.loadData();
 			
 			if(resetWarnings)
 			{
 				for(model.InRent ir : model.InRent.findAll())
 				{
 					ir.setWarned(false);
 				}
 			}
 			
 			Logger.get().write("Daten erfolgreich geladen.");
 		}
 		catch (DataException e1)
 		{
 			Logger.get().write("Es gab einen Fehler beim Laden!");
 		}
 
 		mainWindow = new MainWindow();
 		mainWindow.getMainFrame().setVisible(true);
 	}
 
 	public static void shutdown()
 	{
 		mainWindow.getMainFrame().dispose();
 
 		try
 		{
 			// mahnungen in dateien schreiben
 			Collection<Warning> createdWarnings = Warning.createPendingWarnings();
 			
 			// falls welche erstellt, anzeigen
 			if(createdWarnings.size() > 0)
 			{
 				CreatedWarningsDialog dialog = new CreatedWarningsDialog(null, createdWarnings);
 				dialog.setVisible(true);
 			}
 			
 			DataBase.saveData();
 			Config.saveAll();
 		}
 		catch (Exception e1)
 		{
 			e1.printStackTrace();
 		}
 		
 		Logger.get().write("Programm wird beendet!");
 
 		try
 		{
 			Logger.get().close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 		// TODO: abspeichern von daten, andere sachen aufräumen etc.
 	}
 }
