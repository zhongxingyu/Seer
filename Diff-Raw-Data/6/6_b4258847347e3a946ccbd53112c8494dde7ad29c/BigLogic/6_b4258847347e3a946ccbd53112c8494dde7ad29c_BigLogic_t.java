 package controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.ConfigLoader;
 import model.Server;
 import view.Icon;
 import view.TDI;
 import view.TDIDialog;
 import view.Wallpaper;
 
 /**
  * Implements Runnable interface. Is Master, is big.
  */
 public class BigLogic implements Runnable {
 
 	public ArrayList<Icon> icons;
 	public ArrayList<TDI> tdis;
 	private Server server;
 	/**
 	 * counter for scaling
 	 */
 	private int scaleCount=0;
 	private int scaleCount2=0;
 	/**
 	 * times(1 = 100ms) to wait for scaling
 	 */
 	private int waitTime=5; 
 
 	/**
 	 * The wallpaper
 	 */
 	private Wallpaper wallpaper;
 	/**
 	 * The commands that have to be executed.
 	 */
 	public ArrayList<TDI> commands;
 
 	/**
 	 * Lädt Dialog und Desktop configuration
 	 *
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		BigLogic bl = new BigLogic();
 	}
 
 	/**
 	 * The run method that is overridden
 	 */
 	public void run() {
 
 		while (true) {
 			if (commands.size() > 0) {
 				if (tdis.contains(commands.get(0))) {
 					TDI tdi = tdis.get(tdis.indexOf(commands.get(0)));
 					TDI command = commands.get(0);
 					float[] movParam = new float[3];
 					TDI windFocused=null;
 					TDI taskFocused=null;
 					if (tdi.getPosition() != command.getPosition()) { //CASE POSITION
 						if (tdi.getPosition()[0] != command.getPosition()[0] || tdi.getPosition()[1] != command.getPosition()[1]) // if x or y axis changed
 						{
 							//SZENARIO A DM
 							if(tdi.getState()=="desktop") // is in Desktop Mode == kein TDI in Taskbar == kein Fenster offen
 							{
 								if(tdi.getLocked() == false)
 								{
 									if(PosInTaskbar(command.getPosition()[0],command.getPosition()[1]) == false)//ist die neue Position in Taskbar?
 									{	
 										// TDI für Icons zuständig
 										// neu berechenen der zugehörigen Icons
 									}
 									else // neue pos in taskbar => setzen des Tdi in taskbar mode öffen von program
 									{
 										tdi.setState("taskbar");
 										ProgramHandler.openProgram(tdi.getIcons().get(0)); //open active icon
 										//Vibrate(); //500ms
 										//setLedRed();
 										tdi.setPosition(0, 0, 0); // linke Ecke der Taskbar
 										for(int x=2,y=10; x < tdis.size() ;x++) // set evry other tdi in the middle of the desk
 										{
 											tdis.get(tdis.indexOf(commands.get(x))).setPosition(0, 0, 0+y); // irgendwo am Rand des Tisches (+y damit sie nicht aufeinander fahren)
 											tdis.get(tdis.indexOf(commands.get(x))).setState("default"); //
 											//setLedGreen();
 
 											//set one tdi in window mode - position = middle of the desk
 											tdis.get(tdis.indexOf(commands.get(1))).setState("window");
 											tdis.get(tdis.indexOf(commands.get(1))).setPosition(0, 0, 0);
 										}
 									}
 								}
 							}
 							if(tdi.getState()=="taskbar")
 							{
 								if(ProgramHandler.getRunningPrograms().size()>0) //if programs are open (at least 1)
 								{
 									if(PosInTaskbar(command.getPosition()[0],command.getPosition()[1])) // neue pos immer noch in taskbar
 									{
 										tdi.setPosition(command.getPosition()[0], command.getPosition()[1], tdi.getPosition()[2]);
 									}
 									else // neue pos außerhalb der taskbar
 									{
 										for(TDI t: tdis) // find out if any other TDI has focused a window
 										{
 											if(t.getState()=="window")
 												windFocused=t;
 										}
 										if(windFocused != null)
 										{
 											if(StartScaleMode(tdi,windFocused))
 											{
 												scaleCount2=scaleCount2+1;
 												if(scaleCount2==waitTime)
 												{
 													scaleCount2=0;
 													// skaliermodus TDI repräsentiert untere linke Ecke des Fensters
 													taskFocused.setPosition(0, 0, 0);
 													tdi.setIsScale(true);
 													windFocused.setIsScale(true);
 												    float width=windFocused.getPosition()[0]-tdi.getPosition()[0];
 												    float height=windFocused.getPosition()[1]-tdi.getPosition()[1];
 													ProgramHandler.resizeProgram(width, height);
 												}
 												else 
 												{
 													tdi.setPosition(tdis.indexOf(commands.get(0).getPosition()[0]), tdis.indexOf(commands.get(0).getPosition()[1]), tdis.indexOf(commands.get(0).getPosition()[2]));
 												}
 											}
 										}
 										else
 										{
 											tdi.setPosition(command.getPosition()[0], command.getPosition()[1], tdi.getPosition()[2]);
 										}
 									}
 
 								}
 								else
 								{
 									for(int x=0; x < tdis.size() ;x++) // set evry tdi in desktop mode cause no programs are open
 									{
 										tdis.get(tdis.indexOf(commands.get(x))).setState("desktop"); //?
 										tdis.get(tdis.indexOf(commands.get(x))).setPosition(1, 1, 1);// wohin??
 									}
 								}
 							}
 							if(tdi.getState()=="window")
 							{
 								for(TDI t: tdis) // find out taskbar tdi
 								{
 									if(t.getState()=="taskbar")
 										taskFocused=t;
 									
 								}
 								// taskbar TDI in nähe des aktuellen
 								if(StartScaleMode(taskFocused, tdi))
 								{
 									scaleCount=scaleCount+1;
 										if(scaleCount==waitTime)
 										{
 											scaleCount=0;
 											// skaliermodus TDI repräsentiert obere linke Ecke des Fensters
 											tdi.setPosition(0, 0, 0);
 											// TODO window TDI repräsentiert obere rechte Ecke des Fensters
 											taskFocused.setPosition(0, 0, 0);
 											tdi.setIsScale(true);
 											taskFocused.setIsScale(true);
 										    float width=taskFocused.getPosition()[0]-tdi.getPosition()[0];
 										    float height=taskFocused.getPosition()[1]-tdi.getPosition()[1];
 											ProgramHandler.resizeProgram(width, height);
 										}
 										else 
 										{
 											tdi.setPosition(tdis.indexOf(commands.get(0).getPosition()[0]), tdis.indexOf(commands.get(0).getPosition()[1]), tdis.indexOf(commands.get(0).getPosition()[2]));
 										}
 								}
 								else
 								{
 									tdi.setPosition(command.getPosition()[0], command.getPosition()[1], tdi.getPosition()[2]);
 									//tdi.getIcons().get(0).setPosition(); new position of program window
 								}
 							}
 							if(tdi.getState()=="inapp")
 							{
 
 							}
 						}
 					}	
 					//neigen 
 					//TODO change rotation  => 1 wert drehen 2 werte neigen 
 					//welcher der Werte ist neigen (jeweils positiv verändert / negativ verändert) = 4 werte
 					if (tdi.getRotation() != command.getRotation()) {
 						if(tdi.getState()=="desktop")
 						{
 							//nach rechts neigen
 							if (tdi.getRotation()[1] != command.getRotation()[1])
 								tdi.toggleLock();
 							//tdi.toggleGreenLED();
 						}
 						if(tdi.getState()=="window")
 						{
 							//nach oben
 							if (tdi.getRotation()[0] != command.getRotation()[0])
 							{
 								ProgramHandler.toggleMaximization();
 								if(ProgramHandler.getNonMinimized()==0)
 								{
 									tdi.setState("desktop");
 									tdi.setPosition(1, 1, 1);
 									splitIcons();
 								}
 							}
 							//nach links neigen
 							if(tdi.getRotation()[3]!=command.getRotation()[3])
 							{
 								ProgramHandler.closeProgram();
 								tdi.getIcons().remove(0);
 							}
 							// TDI nach unten neigen
 							if (tdi.getRotation()[2] != command.getRotation()[2]) {
 								ProgramHandler.minimize(); //TODO When still focused, move TDI when not for icons
 								if(ProgramHandler.isDesktopMode())
 								{
 									tdi.setState("desktop");
 									tdi.setPosition(1, 1, 1);
 									splitIcons();
 								}
 								else
 								{
 									tdi.setPosition(1, 1, 1);
 									//TODO GO to location of window
 								};	
 							}
 						}
 						if(tdi.getState()=="taskbar")
 						{
 
 							/* TDI nach oben neigen
 							Alle Fenster werden wiederhergestellt */
 							//nach oben
 							if (tdi.getRotation()[0] != command.getRotation()[0])
 							{
 								ProgramHandler.restoreAllPrograms();
 								if(ProgramHandler.isDesktopMode())
 								{
 									if(tdis.get(1).getState()!="taskbar")
 									{
 										tdis.get(1).setState("window");
 										tdis.get(1).setPosition(1, 1, 1); // to maximised window
 									}
 									else
 									{
 										tdis.get(2).setState("window");
 										tdis.get(1).setPosition(1, 1, 1); // to maximised window
 									}
 								}
 								else
 								{
 									for(TDI t:tdis)
 									{
 										if(t.getState()=="window")
 										{
 											t.setPosition(1, 1, 1); // to maximised window
 										}
 									}
 								}
 							}
 							//nach links/rechts neigen
 							if(tdi.getRotation()[3]!=command.getRotation()[3])
 							{
 								ProgramHandler.closeAllPrograms();
 								for(TDI t:tdis)
 								{
 									t.setState("desktop");
 								}
 							}
 							// TDI nach unten neigen
 							if (tdi.getRotation()[2] != command.getRotation()[2]) {
 								ProgramHandler.minimizeAllPrograms();
 								for(TDI t:tdis)
 								{
 									if(t.getState()=="window")
 									{
 										t.setState("desktop");
 										tdi.setPosition(1, 1, 1);
 										splitIcons();
 									}
 
 								}
 							}
 
 						}
 
 					}
 
 					// drehen
 					if(tdi.getRotation() != command.getRotation())
 					{
 						if(tdi.getState()=="taskbar")
 						{
 							//nicht im Skaliermodus
 							if(tdi.getIsScale()==false)
 							{
 								//nach links Das vorherige Fenster in der Taskleiste wird fokussiert
 								if(tdi.getRotation()[0] > command.getRotation()[0])
 								{
 									tdi.setRotation(command.getRotation());
 									tdi.getIcons().set(0, tdi.getIcons().get(tdi.getIcons().size()));
 								}
 								//nach rechts Das nächste Fenster in der Taskleiste wird fokussiert
 								if(tdi.getRotation()[0] < command.getRotation()[0])
 								{
 									tdi.setRotation(command.getRotation());
 									tdi.getIcons().set(0, tdi.getIcons().get(1));
 								}
 							}
 						}
 						if(tdi.getState()=="desktop")
 						{
 							//nach links Das vorherige Icon, wofür das TDI zuständig ist wird ausgewählt
 							if(tdi.getRotation()[0] > command.getRotation()[0])
 							{
 								tdi.setRotation(command.getRotation());
 								tdi.getIcons().set(0, tdi.getIcons().get(tdi.getIcons().size()));
 							}
 							//nach rechts Das nächste Icon, wofür das TDI zuständig ist wird ausgewählt
 							if(tdi.getRotation()[0] < command.getRotation()[0])
 							{
 								tdi.setRotation(command.getRotation());
 								tdi.getIcons().set(0, tdi.getIcons().get(1));
 							}
 						}
 						if(tdi.getState()=="inapp")
 						{
							//nach rechts neigen
							if(tdi.getRotation()[0] > command.getRotation()[0])
							{
								tdi.setState("window");
							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 *
 	 * @param command
 	 */
 	public void addCommand(TDI command) {
 		commands.add(command);
 	}
 	/**
 	 * checks if givenPos is in taskbar
 	 * @return
 	 */
 	private boolean PosInTaskbar(float x, float y)
 	{
 		return true;
 	}
 	/**
 	 * checks if a taskbar TDI and a window TDI are near enough to start the scale mode
 	 */
 	private boolean StartScaleMode(TDI t1, TDI t2)
 	{
 		int range = 5;
 		if(t1.getPosition()[0] <= t2.getPosition()[0]+range && t1.getPosition()[0] >= t2.getPosition()[0]-range ||
 				t1.getPosition()[1] <= t2.getPosition()[1]+range && t1.getPosition()[1] >= t2.getPosition()[1]-range)
 			return true;
 		if(t2.getPosition()[0] <= t1.getPosition()[0]+range && t2.getPosition()[0] >= t1.getPosition()[0]-range ||
 				t2.getPosition()[1] <= t1.getPosition()[1]+range && t2.getPosition()[1] >= t1.getPosition()[1]-range)
 			return true;
 		else
 			return false;
 	}
 
 	public BigLogic() {
 		ConfigLoader cl = new ConfigLoader();
 //		TDIDialog td = new TDIDialog(cl.getPlugins());
 		icons = cl.loadIcons();
 		Collections.sort(icons);
 //		wallpaper.setBackground(cl.loadWallpaper());
 //		wallpaper.setResolution(cl.loadScreensize());
 		server = new Server();
 		tdis = server.fullPose();
 		splitIcons();
 		Timer mo = new Timer();
 		mo.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				ArrayList<TDI> tdis = server.fullPose();
 				for (TDI t : tdis) {
 					commands.add(t);
 				}
 			}
 		}, 0, 500);
 	}
 	
 	public void splitIcons() {
 		float f = icons.size() / tdis.size();
 		if (icons.size() % tdis.size() == 0) {
 			for (TDI t : tdis) {
 				int f1 = (int) f;
 				int fromIndex = 0;
 				t.setIcons(icons.subList(fromIndex, fromIndex += f1));
 			}
 		}
 	}
 }
 
