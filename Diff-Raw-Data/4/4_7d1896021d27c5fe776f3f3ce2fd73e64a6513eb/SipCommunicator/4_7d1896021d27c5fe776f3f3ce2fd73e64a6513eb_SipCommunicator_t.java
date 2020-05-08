 /* ====================================================================
  * The Apache Software License, Version 1.1
  *
  * Copyright (c) 2000 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Apache" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org/>.
  *
  * Portions of this software are based upon public domain software
  * originally written at the National Center for Supercomputing Applications,
  * University of Illinois, Urbana-Champaign.
  *
  * Copyright 2007 Sun Microsystems, Inc.
  */
 package com.sun.mc.softphone;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import java.lang.reflect.*;
 import java.net.*;
 import java.awt.*;
 import com.sun.mc.softphone.common.*;
 import com.sun.mc.softphone.gui.*;
 import com.sun.mc.softphone.gui.event.*;
 import com.sun.mc.softphone.media.*;
 import com.sun.mc.softphone.sip.*;
 import com.sun.mc.softphone.sip.event.*;
 import com.sun.mc.softphone.sip.security.*;
 import java.io.IOException;
 
 import java.text.ParseException;
 
 import com.sun.voip.Logger;
 
 import com.sun.stun.NetworkAddressManager;
 import com.sun.stun.StunServer;
 
 import java.awt.event.ActionEvent;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.AbstractAction;
 
 /**
  * @todo support authentification
  * @todo use a resource file
  * @todo add the subscribe request management
  * @todo add overall doc
  * @todo add javadoc comments
  * @todo gen javadoc documentation
  * <p>Title: SIP COMMUNICATOR</p>
  * <p>Description:JAIN-SIP Audio/Video phone application</p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
  * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
  * <p>Louis Pasteur University - Strasbourg - France</p>
  * @author Emil Ivov (http://www.emcho.com)
  *
  */
 public class SipCommunicator extends Thread implements 
 	UserActionListener, CommunicationsListener, CallListener, SecurityAuthority,
 	PCPanelListener {
 
     private static Console console = Console.getConsole(SipCommunicator.class);
     public static final String CONFIG_MODE = "ConfigMode";
     public static final String PHONE_MODE = "PhoneMode";
     public String mode = PHONE_MODE;
     private GuiManagerUI guiManager = null;
     private MediaManager mediaManager = null;
     private SipManager sipManager = null;
     private Window tracesViewerWindow = null;
     private Process rmiRegistryProcess = null;
 
     // a map from call ids to media managers
     private Map mediaManagers;
     
     private Integer unregistrationLock = new Integer(0);
 
     private boolean autoLogin = false;
     private boolean showGUI = true;
     private boolean autoAnswer = false;
     private int autoAnswerDelay = 200;
     private boolean openAudio = true;
     private boolean loadGen = false;
     private boolean fromMC = false;
     
     private String userName = null;
     private String authenticationUserName;
     private String encryptionKey;
     private String encryptionAlgorithm;
     private String meetingCode;
     private boolean mute;
     private int nCalls;
     private String passCode;
     private String phoneNumber;
 
     private String treatment;
     private int dutyCycle;
 
     private static SipCommunicator sipCommunicator;
 
     private String registrarAddress;
 
     private ParameterControl parameterControl;
 
     private Utils utils;
 
     private static Object sipCommunicatorLock = new Object();
 
     /*
      * Meeting central starts the communicator and adds MC as
      * as listener so that the comunicator can tell MC the 
      * sip address.
      *
      * When used with MC, there is no visible UI.
      */
     public static SipCommunicator getInstance() {
 	return sipCommunicator;
     }
 
     public SipCommunicator(String[] args) throws ParseException {
 	sipCommunicator = this;
 
         String arguments = "";
 
 	for (int i = 0; i < args.length; i++) {
 	    arguments += args[i] + " ";
 	}
 
 	Logger.println("Arguments:  " + arguments);
 
         try {
             console.logEntry();
 
             if (args.length == 1 && args[0].length() > 0 &&
                     !args[0].startsWith("-"))
             {
                 // work with older mc versions
                 setUserName(args[0]);
                 
                 // called from mc
                 setFromMC(true);
                 setShowGUI(false);
                 setLoadGen(false);
                 setAutoAnswer(true);
             } else {
                 parseArgs(args);
             }
             
 	    synchronized (sipCommunicatorLock) {
 	        initialize();
 	    }
 	} catch (ParseException e) {
 	    usage();
 	    throw e;
         } finally {
             console.logExit();
         }
     }
  
     public void initialize() {
 	Runtime.getRuntime().addShutdownHook(new ShutdownThread());
 
 	String[] config = new String[1];
 
         try {
             console.logEntry();
 
 	    if (userName != null && userName.length() == 0) {
 		userName = null;
 	    }
 
 	    Logger.println("Sip Communicator built on " + Version.getVersion());
 
             String s = System.getProperty("java.version");
 
 	    Logger.println("Running java version " + s);
 
 	    if (s.indexOf("1.5.") < 0 && s.indexOf("1.6") < 0) {
 	        Logger.println("WARNING:  You are running java version " + s 
 		    + " from " + System.getProperty("java.home"));
 	        Logger.println(
 		    "SipCommunicator will run much better with java 1.5 or later!\n");
 	    }
 
             Logger.println("OS Name = " + System.getProperty("os.name")
                 + ", OS Arch = " + System.getProperty("os.arch")
                 + ", OS Version = " + System.getProperty("os.version"));
 
             try {
                 mediaManager = MediaManagerFactory.getInstance(false);
 		mediaManager.initialize(encryptionKey, encryptionAlgorithm,
 		    !openAudio());
             } catch (IOException e) {
                 Logger.println("Unable to start audio:  " + e.getMessage());
             }
 
 	    /*
 	     * When started by Meeting Central, there is no GUI.
 	     */  
             String gui = System.getProperty("com.sun.mc.softphone.GUI_MANAGER");
             if (gui == null) {
 		gui = "com.sun.mc.softphone.gui.NewGuiManager";
 	    }
 
             try {
                 Class guiClass = Class.forName(gui);
                 Class[] params = new Class[] {Boolean.class};
                 Constructor constructor = guiClass.getConstructor(params);
                 if (constructor != null) {
                     Object[] args = new Object[] { new Boolean(showGUI()) };
                     guiManager = (GuiManagerUI)(constructor.newInstance(args));
                 } else {
                     Logger.println("constructor not found for: "+gui);
                     System.exit(1);
                 }
             } catch (Exception e) {
                 Logger.println("Error loading '"+gui+"': "+e.getMessage());
 	        e.printStackTrace();
                 System.exit(1);
             }
 
 	    utils = new Utils(userName, loadGen);
 
             sipManager = new SipManager(this, mediaManager, registrarAddress);
             
 	    mediaManager.setSipCommunicator(this);
 	    mediaManager.setSipManager(sipManager);
 
             guiManager.addUserActionListener(this);
             
             if (guiManager instanceof NewGuiManager) {
                 ((NewGuiManager)guiManager).addConfigMenuItem(new AbstractAction("Test audio") {
                     public void actionPerformed(ActionEvent evt) {
                         guiManager.showLineTest(mediaManager);
                     }
                 });
 
                 ((NewGuiManager)guiManager).addConfigMenuItem(new AbstractAction("Network Interface Config") {
                     public void actionPerformed(ActionEvent evt) {
                         guiManager.showNetworkInterfaceConfig();
                     }
                 });
             }
 
 	    if (fromMC()) {
 		/*
 		 * Meeting Central pings us every 10 seconds.
 		 * If we don't hear from MC, assume it terminated
 		 * and we should quit as well.
 		 */
 		Logger.println("Starting thread to watch for MC heartbeat.");
 		start();
 
 		/*
 		 * Start a thread to check for timeout
 		 */
 		new TimeoutThread();
 	    }
 
 	    if (phoneNumber != null) {
 	        guiManager.setPhoneNumber(phoneNumber);
 	    }
 
             guiManager.showPhoneFrame(showGUI());
             launch();
 
 	    Utils.setPreference("com.sun.mc.softphone.LAST_CONFERENCE", "");
         } finally {
             console.logExit();
         }
     }
  
     public void placeCall(String callerName, String callee,
 	    String conferenceId) throws IOException {
 
 	dial(conferenceId, callerName, callee);
     }
 
     /**
      * Play a treatment to the call.
      * @param audioFile the file to play
      * @param repeats the number of times to repeat the treatment, or -1
      * to repeat until it is manually stopped.
      * @throws IOException if there is an error playing the treatment
      */
     public void playTreatment(String audioFile, int repeats) throws IOException {
 	if (mediaManager == null) {
             return;
         }
         
         mediaManager.playTreatment(audioFile, repeats);
     }
 
     public void pauseTreatment(boolean pause) {
 	if (mediaManager == null) {
             return;
         }
         
 	mediaManager.pauseTreatment(pause);
     }
 
     public void stopTreatment() {
 	if (mediaManager == null) {
             return;
         }
         
         mediaManager.stopTreatment();
     }   
 	
     public void mute(boolean isMuted) {
 	if (mediaManager == null) {
             return;
         }
 
         mediaManager.mute(isMuted);
     }
 
     public void locationChanged(Point p) {
 	parameterControl.setString("x is " + p.getX()
 	    + " y is " + p.getY());
     }
 
     public void sendCommand(String command) {
 	processCommand(command);
     }
 
     public String getSoftphoneAddress() {
 	return sipManager.getPublicAddress();
     }
 
     public boolean isConnected() {
 	return callInProgressInterlocutor != null;
     }
 
     /*
      * When started by Meeting Central, we watch for keep alive messages.
      */
     private int PING_TIMEOUT = 60;
 
     private int ping_timeout_count = 0;
 
     private Integer timeoutLock = new Integer(0);
 
     class TimeoutThread extends Thread {
 	public TimeoutThread() {
 	    start();
 	}
 
 	public void run() {
 	    while (true) {
 	        try {
 		    Thread.sleep(5000);
 	        } catch (InterruptedException e) {
 		}
 
 		synchronized (timeoutLock) {
 		    ping_timeout_count += 5;
 		}
 
                 if (ping_timeout_count < PING_TIMEOUT) {
                     continue;
                 }
 
                 Logger.println("Sip Communicator terminating.  "
                     + "Unable to read heart beat from MC");
 
                 shutDown();
                 break;
 	    }
 	}
     }
 
     /*
      * Meeting Central pings us every 10 seconds.
      * If we don't hear from MC, assume it terminated
      * and we should quit as well.
      */
     boolean firstTime = true;
 
     public void run() {
 	BufferedReader bufferedReader = new
 	    BufferedReader(new InputStreamReader(System.in));
 	
 	/*
 	 * Not sure why we need this but without this,
 	 * the softphone hangs.
 	 */
 	try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
         }
 
 	while (true) {
 	    try {
                 String command = bufferedReader.readLine();
 		
 		if (command == null) {
 		    Logger.println("Sip Communicator terminating.  "
 			+ "End of input stream");
 
 		    System.exit(1);
 		}
 		
 		processCommand(command);
 	    } catch (IOException e) {
 		Logger.println("Sip Communicator terminating.  "
 		    + "Unable to read heart beat from MC");
 		    
 		shutDown();
 		break;
 	    }
 	}
     }
 
     private int previousLogLevel;
 
     public void processCommand(String command) {
 	if (command.indexOf("logLevel=") >= 0) {
             String tokens[] = command.split("=");
 
             try {
                 Utils.setPreference("com.sun.mc.softphone.media.LOG_LEVEL",
                     tokens[1]);
             } catch (NumberFormatException e) {
                 Logger.println("Invalid log level:  " + tokens[1]);
             }
             return;
         }
 
 	if (command.indexOf("isConnected") >= 0) {
 	    if (callInProgressInterlocutor != null) {
 		Logger.println("Softphone is connected");
 	    } else {
 		Logger.println("Softphone is disconnected");
 	    }
 	    return;
 	}
 
 	if (command.indexOf("ping") >= 0) {
 	    if (firstTime) {
 		firstTime = false;
 		Logger.println("softphone got ping from MC:  " + command);
 	    }
 
 	    synchronized (timeoutLock) {
 		ping_timeout_count = 0;
 	    }
 	    return;
 	}
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Got command '" + command + "'");
 	}
 
 	if (command.indexOf("Shutdown") >= 0) {
 	    Logger.println("Received Shutdown command...");
 	    shutDown();
 	    return;
 	}
 
 	if (command.indexOf("Show") == 0) {
 	    if (guiManager != null) {
 	        guiManager.showPhoneFrame(true);
 		Logger.println("Softphone is visible");
 	    }
 	    return;
 	}
 
 	if (command.indexOf("Hide") == 0) {
 	    if (guiManager != null) {
 	        guiManager.showPhoneFrame(false);
 		Logger.println("Softphone is hidden");
 	    }
 	    return;
 	}
  
 	if (command.indexOf("Show Config") >= 0) {
 	    if (guiManager != null) {
 		guiManager.showConfigFrame();
 	    }
 	    return;
 	}
                     
 	if (command.indexOf("ReRegister=") >= 0) {
 	    Logger.println(command);
 
 	    command = command.substring(11);
 
 	    String[] tokens = command.split(":");
 
 	    if (tokens.length < 2) {
 		Logger.println("Missing parameters for Register:  " + command);
 		return;
 	    }
 
 	    try {
 		sipManager.reRegister(tokens[0], tokens[1]);
 	    } catch (CommunicationsException e) {
 		Logger.println("Unable to restart sipManager:  " 
 		    + e.getMessage());
 	    }
 
 	    return;
 	}
 
         if (command.indexOf("linetest") >= 0) {
             if (guiManager != null) {
                 guiManager.showLineTest(mediaManager);
                 Logger.println("Showing line test dialog");
 		return;
 	    }
         }
 
 	command = command.replaceAll("[\n]", ""); 
 
 	if (command.indexOf("PlaceCall=") >= 0) {
 	    command = command.substring(10);
 
 	    String[] tokens = command.split(",");
 
 	    if (tokens.length != 3) {
 		Logger.println("Usage is:  "
 		    + "PlaceCall=conferenceId:<conferenceId>,"
 		    + "userName=<userName>,"
 		    + "callee=<callee>");
 		    return;
 	    }
 
 	    String[] cId = tokens[0].split(":");
 			
 	    if (cId.length != 2) {
 	        Logger.println("Missing conference Id");
 		return;
 	    }
 
 	    if (cId[0].equalsIgnoreCase("conferenceId") == false) {
 		Logger.println("Unknown parameter:  " + cId[0]);
 		return;
 	    }
 
 	    String conferenceId = cId[1];
 
             String[] uName = tokens[1].split(":");
 
             if (uName.length != 2) {
                 Logger.println("Missing userName");
                 return;
             }
 
             if (uName[0].equalsIgnoreCase("userName") == false) {
                 Logger.println("Unknown parameter:  " + cId[0]);
                 return;
             }
 
             String userName = uName[1];
 
             String[] number = tokens[2].split(":");
 
             if (number.length != 2) {
                 Logger.println("Missing callee number");
                 return;
             }
 
             if (number[0].equalsIgnoreCase("callee") == false) {
                 Logger.println("Unknown parameter:  " + number[0]);
                 return;
             }
 
             String callee = number[1];
 
 	    System.out.println(
 		"Dialing Conference id " + conferenceId
 		    + " userName " + userName
 		    + " callee " + callee);
 
 		//dial(conferenceId, userName, callee);
 	    dial(null, userName, callee);
 	    return;
         }
 
 	if (command.indexOf("sampleRate=") >= 0) {
 	    String tokens[] = command.split("=");
 
 	    if (tokens[1].equals("8000") || tokens[1].equals("16000") ||
 		tokens[1].equals("32000") || tokens[1].equals("32000") ||
 		tokens[1].equals("44100") || tokens[1].equals("48000")) {
 
 		Utils.setPreference(
 		    "com.sun.mc.softphone.media.SAMPLE_RATE", tokens[1]);
 	    } else {
 		Logger.println("Invalid sample rate:  " + command);
 	    }
 
 	    return;
 	}
 
 	if (command.indexOf("channels=") >= 0) {
 	    String tokens[] = command.split("=");
 
 	    if (tokens[1].equals("1") || tokens[1].equals("2")) {
                 Utils.setPreference("com.sun.mc.softphone.media.CHANNELS",
 		   tokens[1]);
 	    } else {
 		Logger.println("Invalid number of channels:  " + command);
 	    }
 
 	    return;
 	}
 
 	if (command.indexOf("encoding=") >= 0) {
 	    String tokens[] = command.split("=");
 
 	    if (tokens[1].equals("PCM") || tokens[1].equals("PCMU") ||
 		    tokens[1].equals("SPEEX")) {
 
         	Utils.setPreference("com.sun.mc.softphone.media.ENCODING",
 		    tokens[1]);
 	    } else {
 		Logger.println("Encoding must be PCMU, PCM, or SPEEX:  " 
 		    + command);
 	    }
 
 	    return;
 	}
 
         if (command.indexOf("transmitSampleRate=") >= 0) {
             String tokens[] = command.split("=");
 
             if (tokens[1].equals("8000") || tokens[1].equals("16000") ||
                     tokens[1].equals("32000") || tokens[1].equals("32000") ||
                     tokens[1].equals("44100") || tokens[1].equals("48000")) {
 
                 Utils.setPreference(
                     "com.sun.mc.softphone.media.TRANSMIT_SAMPLE_RATE", tokens[1]);
             } else {
                 Logger.println("Invalid transmit sample rate:  " + command);
             }
 
             return;
         }
 
         if (command.indexOf("transmitChannels=") >= 0) {
             String tokens[] = command.split("=");
 
             if (tokens[1].equals("1") || tokens[1].equals("2")) {
                 Utils.setPreference("com.sun.mc.softphone.media.TRANSMIT_CHANNELS",
                     tokens[1]);
             } else {
                 Logger.println("Invalid number of transmit channels:  " + command);
             }
 
             return;
         }
 
 	if (command.indexOf("transmitEncoding=") >= 0) {
 	    String tokens[] = command.split("=");
 
 	    if (tokens[1].equals("PCM") || tokens[1].equals("PCMU") ||
 		    tokens[1].equals("SPEEX")) {
 
         	Utils.setPreference("com.sun.mc.softphone.media.TRANSMIT_ENCODING",
 		    tokens[1]);
 	    } else {
 		Logger.println("Transmit encoding must be PCMU, PCM, or SPEEX:  " 
 		    + command);
 	    }
 
 	    return;
 	}
 
         //** 1.5 only!
 
         if (command.indexOf("stack") >= 0) {
 	    previousLogLevel = Logger.logLevel;
 
 	    Logger.logLevel = 8;
 	    Utils.setPreference("com.sun.mc.softphone.media.LOG_LEVEL", "8");
 
 	    if (callInProgressInterlocutor != null) {
 		Logger.println("Softphone is connected");
 	    } else {
 		Logger.println("Softphone is disconnected");
 	    }
 
 	    if (mediaManager != null) {
 		try {
 		    mediaManager.startRecording("Recording.au", "Au",
 			false, null);
 		} catch (IOException e) {
 		    Logger.println("Unable to record:  " + e.getMessage());
 		}
 
 	        Timer timer = new Timer();
 
 	        timer.schedule(new TimerTask() {
                     public void run() {
 		        Logger.logLevel = previousLogLevel;
 
 		        Utils.setPreference("com.sun.mc.softphone.media.LOG_LEVEL", 
 			    String.valueOf(previousLogLevel));
 
 		        if (mediaManager != null) {
 			    mediaManager.stopRecording(false);
 		        }
 		}}, 10000);
 	    }
 
             Logger.println("Stack trace: ");
             Map st = Thread.getAllStackTraces();
             for (Iterator i = st.entrySet().iterator(); i.hasNext();) {
                 Map.Entry me = (Map.Entry) i.next();
                 Thread t = (Thread) me.getKey();
                 Logger.println("");
                 Logger.println("Thread " + t.getName());
                     
                 StackTraceElement[] ste = (StackTraceElement[]) me.getValue();
                 for (int c = 0; c < ste.length; c++) {
                     StringBuffer outBuf = new StringBuffer("    ");
                     outBuf.append(ste[c].getClassName() + ".");
                     outBuf.append(ste[c].getMethodName() + "(");
                     outBuf.append(ste[c].getFileName() + ":");
                     outBuf.append(ste[c].getLineNumber() + ")");
                     
                     Logger.println(outBuf.toString());
                 }
             }
                 
             return;
         }
             
 	if (mediaManager != null) {
 	    if (command.indexOf("microphoneVolume=") >= 0) {
 		try {
 		    float volume = Float.parseFloat(command.substring(17));
 		    mediaManager.setMicrophoneVolume(volume);
 		} catch (NumberFormatException e) {
 		    Logger.println("Invalid volume specified "
 			+ command + " " + e.getMessage());
 		}
 		return;
 	    } 
 
 	    if (command.indexOf("speakerVolume=") >= 0) {
                 try {
                     float volume = Float.parseFloat(command.substring(14));
                     mediaManager.setSpeakerVolume(volume);
                 } catch (NumberFormatException e) {
                     Logger.println("Invalid volume specified "
                         + command + " " + e.getMessage());
                 }
 		return;
             } 
 
 	    if (command.indexOf("Mute") == 0 || 
 		    command.indexOf("Unmute") == 0) {
 
                 boolean mute = (command.indexOf("Mute") == 0);        
 
 		if (mute) {
 		    Logger.println("Softphone Muted");
 		} else {
 		    Logger.println("Softphone Unmuted");
 		}
 
                 if (mediaManager.isMuted() != mute) {
                     mediaManager.mute(mute);
                         
                     if (guiManager != null) {
                         guiManager.muted(mediaManager.isMuted());
                     }
                 }
                 return;
             }
 	}
 
 	Logger.println("Unrecognized command:  " + command);
     }
     
     public void launch() {
         try {
             console.logEntry();
             mode = PHONE_MODE;
 
 	    if (Utils.getPreference("com.sun.mc.stun.STUN_SERVER") ==
 		    null) {
 
 		String registrarAddress = sipManager.getRegistrarAddress();
 
 		if (registrarAddress != null) {
 	            System.setProperty(
 			"com.sun.mc.stun.STUN_SERVER", registrarAddress);
 		    
 		    int registrarPort = sipManager.getRegistrarPort();
 
                     System.setProperty(
 			"com.sun.mc.stun.STUN_SERVER_PORT",
                     	String.valueOf(registrarPort));
 		}
 	    }
 
             //InetAddress localhost = NetworkAddressManager.getPrivateLocalHost();
 
             //String stackAddress = localhost.getHostAddress();
             //Add the host address to the properties that will pass the stack
 
 	    //if (Logger.logLevel >= Logger.LOG_MOREINFO) {
             //    Logger.println("Softphone setting stack address to " 
 	    //	    + stackAddress);
 	    //}
 
             //Utils.setProperty("javax.sip.IP_ADDRESS", stackAddress);
 
 	    //Logger.println("Setting SIP Stack address to " + stackAddress);
 
 	    /*
 	     * The listening point specifies the address rather than the property
 	     */
             System.clearProperty("javax.sip.IP_ADDRESS");
 
             initDebugTool();
 
             sipManager.addCommunicationsListener(this);
             sipManager.setSecurityAuthority(this);
 
             try {
                 sipManager.start();	// put in a separate thread
 
                 if (sipManager.isStarted()) {
                     console.trace(
                         "sipManager appears to be successfully started");
 
                     guiManager.setCommunicationActionsEnabled(true);
                 }
             } catch (CommunicationsException exc) {
                 console.showException(
                     "An exception occurred while initializing communication stack!\n"
                     + "You won't be able to send or receive calls",
                     exc);
                 return;
             }
             try {
                 String publicAddress = sipManager.startRegisterProcess();
 		Utils.setPreference("com.sun.mc.softphone.sip.PUBLIC_ADDRESS", publicAddress);
             } catch (CommunicationsException exc) {
                 console.error(
                     "An exception occurred while trying to register, exc");
                 console.showException(
                     "Failed to register!\n"
                     + exc.getMessage() + "\n"
                     + "This is a warning only. The phone would still function",
                     exc);
             }
 	//} catch (IOException e) {
 	//    Logger.println("Unable to determine local host!");
         } finally {
             console.logExit();
         }
     }
 
     public static void main(String[] args) {
         // load the toolkit?
         java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
             
 	try {
             SipCommunicator sipCommunicator = new SipCommunicator(args);
 	} catch (ParseException e) {
 	    usage();
 	    System.exit(1);
 	}
     }
 
     private static void usage() {
 	Logger.println("java SipCommunicator ");
 	Logger.println("                     [-a <encryption algorithm>]");
         Logger.println("                     [-answer > automatically answer incoming calls]");
 	Logger.println("                     [-k <encryption key>]");
         Logger.println("                     [-loadGen > run as load generator]");
         Logger.println("                     [-mc > launched from Meeting Central]");
         Logger.println("                     [-meetingCode <meetingCode> > meetingCode used with loadGen]");
         Logger.println("                     [-mute > mute call when connected, used with loadGen");
         Logger.println("                     [-nCalls > <number of calls to place for load generator> ]");
 	Logger.println("                     [-gui > display the gui]");
 	Logger.println("                     [-l > local IP Address");
 	Logger.println("                     [-nogui > don't display the gui]");
 	Logger.println("                     [-autoLogin > don't display login dialog]");
         Logger.println("                     [-passCode > <passCode> > passCode used with loadGen]");
         Logger.println("                     [-phoneNumber <phoneNumber for load generator>]");
         //Logger.println("                     [-playTreatment <treatment>[:<duty cycle %>]]");
         Logger.println("                     [-r <registrar> > specify the registrar address");
         Logger.println("                     [-sampleRate <sampleRate> > specify the sampleRate");
         Logger.println("                     [-channels <audio channels> > specify # of channels]");
         Logger.println("                     [-encoding PCM | PCMU | SPEEX > specify encoding");
         Logger.println("                     [-silent > don't open the mic/speaker]");
         Logger.println("                     [-stun <server:port> > specify the stun server address");
         Logger.println("                     [-t <registrar timeout> specify the registrar timeout seconds");
         Logger.println("                     [-transmitChannels <channels> specify the xmit channels");
         Logger.println("                     [-transmitSampleRate <sampleRate> specify the xmit rate");
         Logger.println("                     [-transmitEncoding PCM | PCMU | SPEEX > specify transmit encoding");
 	Logger.println("                     [-u <user name>]");
     }
 
     private void parseArgs(String[] args) throws ParseException {
 	for (int i = 0; i < args.length; i++) {
 	    //Logger.println("arg " + i + " " + args[i]);
 
 	    if (args[i].equalsIgnoreCase("-u") && i < (args.length - 1)) {
 		userName = args[++i];
 	    } else if (args[i].equalsIgnoreCase("-k") && i < (args.length - 1)) {
 	        encryptionKey = args[++i];
 	    } else if (args[i].equalsIgnoreCase("-a") && i < (args.length - 1)) {
 		encryptionAlgorithm = args[++i];
 	    } else if (args[i].equalsIgnoreCase("-loadGen")) {
                 setLoadGen(true);
                 setOpenAudio(false);
                 setShowGUI(false);
                 setFromMC(false);
                 setAutoAnswer(true);
             } else if (args[i].equalsIgnoreCase("-autoLogin")) {
 		setAutoLogin(true);
             } else if (args[i].equalsIgnoreCase("-gui")) {
 		setShowGUI(true);
             } else if (args[i].equalsIgnoreCase("-l") && i < args.length - 1) {
 		i++;
 		Logger.println("Setting local IP Address to " + args[i]);
 
 		Utils.setPreference("com.sun.mc.stun.LOCAL_IP_ADDRESS",
 		    args[i]);
 		System.setProperty("com.sun.mc.stun.LOCAL_IP_ADDRESS",
 		    args[i]);
             } else if (args[i].equalsIgnoreCase("-nogui")) {
                 setShowGUI(false);
             } else if (args[i].equalsIgnoreCase("-silent")) {
                 setOpenAudio(false);
 	    } else if (args[i].equalsIgnoreCase("-mc")) {
                 setFromMC(true);
                 setShowGUI(false);
                 setLoadGen(false);
                 setAutoAnswer(true);
 		setAutoLogin(true);
             } else if (args[i].equalsIgnoreCase("-answer")) {
                 setAutoAnswer(true);
             } else if (args[i].equalsIgnoreCase("-meetingCode") && i < args.length - 1) {
 		setMeetingCode(args[++i]);
             } else if (args[i].equalsIgnoreCase("-nCalls") && i < args.length - 1) {
 		try {
 		    int nCalls = Integer.parseInt(args[++i]);
 
 		    if (nCalls > 0) {
 		        setNCalls(nCalls);
 		    }
 
 		    System.setProperty("com.sun.mc.softphone.gui.Lines.MAX_LINES", args[i]);
 		    Utils.setPreference("com.sun.mc.softphone.gui.Lines.MAX_LINES", args[i]);
 		} catch (NumberFormatException e) {
 		}
             } else if (args[i].equalsIgnoreCase("-passCode") && i < args.length - 1) {
 		setPassCode(args[++i]);
             } else if (args[i].equalsIgnoreCase("-phoneNumber") && i < args.length - 1) {
 		setPhoneNumber(args[++i]);
             } else if (args[i].equalsIgnoreCase("-playTreatment") && i < args.length - 1) {
 		// XXX Not done yet!
 
 		i++;
 
 		String treatment = args[i];
 
 		int dutyCycle = 100;
 
 		int ix;
 
 		if ((ix = treatment.indexOf(":")) > 0) {
 		    treatment = treatment.substring(0, ix);
 
 		    try {
 			dutyCycle = Integer.parseInt(args[i].substring(ix + 1));	
 		    } catch (NumberFormatException e) {
 			Logger.println("Invalid duty cycle " 
 			    + args[i].substring(ix + 1) + " using 100%");
 		    }
 		}
 
 		setTreatment(treatment);
 		setDutyCycle(dutyCycle);
             } else if (args[i].equalsIgnoreCase("-mute")) {
 		setMute();
             } else if (args[i].equalsIgnoreCase("-r") && 
 		    i < (args.length - 1)) {
 
 		registrarAddress = args[++i];
 
 		String tokens[] = registrarAddress.split(":");
 
 		if (tokens.length < 2) {
 		    throw new ParseException("Invalid registrar:  " + args[i], 0);
 		}
 
 		String s = tokens[0];
 
 		boolean sipStun = false;
 
 		int ix = s.indexOf(";sip-stun");
 
 		if (ix > 0) {
 		    sipStun = true;
 		    s = s.substring(0, ix);
 		}
 
 		InetAddress ia;
 
 		try {
 		    ia = InetAddress.getByName(s);
 		} catch (UnknownHostException e) {
 		    Logger.println("Unknown host: " + s);
 		    throw new ParseException("Invalid registrar:  " 
 			+ e.getMessage(), 0);
 		}
 
 		String registrar = ia.getHostAddress();
 
 		if (sipStun == true) {
 		    registrar += ";sip-stun";
 		}
 
 		Utils.setPreference("com.sun.mc.softphone.sip.REGISTRAR_ADDRESS", "");
 	
 		Utils.setPreference("com.sun.mc.softphone.sip.REGISTRAR_UDP_PORT", "");
 
 		System.setProperty("com.sun.mc.softphone.sip.REGISTRAR_ADDRESS",
 		    registrar);
 	
 		System.setProperty("com.sun.mc.softphone.sip.REGISTRAR_UDP_PORT",
 		    tokens[1]);
             } else if (args[i].equalsIgnoreCase("-stun") && 
 		    i < (args.length - 1)) {
 
 		String tokens[] = args[++i].split(":");
 	
                 InetAddress ia;
 
                 try {
                     ia = InetAddress.getByName(tokens[0]);
                 } catch (UnknownHostException e) {
                     Logger.println("Unknown host: " + tokens[0]);
                     throw new ParseException("Invalid stun server:  "
                         + e.getMessage(), 0);
                 }
 
 		String stunServer = ia.getHostAddress();
 
 		String stunPort = String.valueOf(StunServer.STUN_SERVER_PORT);
 
 		if (tokens.length >= 2) {
 		    stunPort = tokens[1];
 		}
 
 		//Utils.setPreference("com.sun.mc.stun.STUN_SERVER",
                 //    stunServer);
 
                 //Utils.setPreference("com.sun.mc.stun.STUN_SERVER_PORT",
                 //    stunPort);
 
 		System.setProperty("com.sun.mc.stun.STUN_SERVER",
                     stunServer);
 
                 System.setProperty("com.sun.mc.stun.STUN_SERVER_PORT",
                     stunPort);
             } else if (args[i].equalsIgnoreCase("-t") && 
 		    i < (args.length - 1)) {
 
 		try {
 		    int registrarTimeout = Integer.parseInt(args[++i]);
 		    
 		    System.setProperty(
 			"com.sun.mc.softphone.sip.WAIT_UNREGISTGRATION_FOR",
 			String.valueOf(registrarTimeout));
 		} catch (NumberFormatException e) {
 		    throw new ParseException("Invalid registrar timeout:  "
 			+ e.getMessage(), 0);
 		}
             } else if (args[i].equalsIgnoreCase("-sampleRate") && i < (args.length - 1)) {
                 Utils.setPreference("com.sun.mc.softphone.media.SAMPLE_RATE", args[++i]);
             } else if (args[i].equalsIgnoreCase("-channels") && i < (args.length - 1)) {
                 Utils.setPreference("com.sun.mc.softphone.media.CHANNELS",
                     args[++i]);
 
             } else if (args[i].equalsIgnoreCase("-encoding") && i < (args.length - 1)) {
                 Utils.setPreference("com.sun.mc.softphone.media.ENCODING",
                     args[++i]);
             } else if (args[i].equalsIgnoreCase("-transmitSampleRate") && i < (args.length - 1)) {
                 Utils.setPreference("com.sun.mc.softphone.media.TRANSMIT_SAMPLE_RATE", args[++i]);
             } else if (args[i].equalsIgnoreCase("-transmitChannels") && i < (args.length - 1)) {
                 Utils.setPreference("com.sun.mc.softphone.media.TRANSMIT_CHANNELS", args[++i]);
             } else if (args[i].equalsIgnoreCase("-encoding") && i < (args.length - 1)) {
                 Utils.setPreference("com.sun.mc.softphone.media.TRANSMIT_ENCODING",
                     args[++i]);
             } else {
 	        throw new ParseException("Invalid arguments", 0);
 	    }
 	}
     }
 
 //========================= USER ACTION LISTENER =========================
 
     public void handleAnswerRequest(UserCallControlEvent evt) {
         try {
             console.logEntry();
             Interlocutor interlocutor =
                 (Interlocutor) evt.getAssociatedInterlocutor();
             if (!interlocutor.getCallState().equals(Call.ALERTING)) {
                 return;
             }
 	    answerCall(interlocutor);
 	} finally {
             console.logExit();
         }
     }
 
     private synchronized void answerCall(Interlocutor interlocutor) {
 	try {
 	    console.logEntry();
 
             String sdpData = null;
 
             try {
                 sdpData = mediaManager.generateSdp(true);
             } catch (IOException ex) {
                 console.showError("Failed to Generate an SDP description "
 		    + ex.getMessage());
                 try {
                     sipManager.sendServerInternalError(interlocutor.getID());
                 } catch (CommunicationsException ex1) {
                     console.error(ex1.getMessage(), ex1);
                 }
                 return;
             }
             try {
                 sipManager.answerCall(interlocutor.getID(), sdpData);
             } catch (CommunicationsException exc) {
                 console.showException("Could not answer call!\nError was: "
                                       + exc.getMessage(),
                                       exc);
             }
         } finally {
             console.logExit();
         }
     }
 
     public void handleDialRequest(UserCallInitiationEvent evt) {
         try {
             console.trace(
                 "Entering handleDialRequest(UserCallInitiationEvent)");
 
             String callee = (String) evt.getSource();
 	    dial(null, null, callee);
         } finally {
             console.logExit();
         }
     }
 
     private void dial(String conferenceId, String userName, String callee) {
 	try {
             String sdpData = null;
 
             try {
                 sdpData = mediaManager.generateSdp(callee);
 
 		if (userName != null) {
 		    sdpData += "a=userName:" + userName + "\r\n";
 		}
 
 		if (conferenceId != null) {
 		    sdpData += "a=conferenceId:" + conferenceId + "\r\n";
 		} else {
 		    conferenceId = 
 			Utils.getPreference("com.sun.mc.softphone.LAST_CONFERENCE");
 
 		    if (conferenceId != null) {
 		        sdpData += "a=conferenceId:" + conferenceId + "\r\n";
 		    }
 		}
             } catch (IOException ex) {
                 console.showError("Failed to Generate an SDP description "
 		    + ex.getMessage());
                 return;
             }
             try {
 	        synchronized (mediaManager) {
                     Call call = sipManager.establishCall(this, callee, sdpData);
                     Interlocutor interlocutor = new Interlocutor();
 		    call.setInterlocutor(interlocutor);
                     interlocutor.setCall(call);
                     guiManager.addInterlocutor(interlocutor);
 		}
             } catch (CommunicationsException exc) {
                 console.showException("Could not establish call!\nError was: "
                                       + exc.getMessage(),
                                       exc);
             }
         } finally {
             console.logExit();
         }
     }
 
     public synchronized void handleHangupRequest(UserCallControlEvent evt) {
         try {
             sipManager.endCall(evt.getAssociatedInterlocutor().getID());
 	    callInProgressInterlocutor = null;
         } catch (CommunicationsException exc) {
             console.showException("Could not properly terminate call!\n"
                                   + "(This is not a fatal error)",
                                   exc
                                   );
         }
     }
 
     public void handleDtmfRequest(UserDtmfEvent evt) {
         try {
             console.logEntry();
 
             if (evt.isStartDtmf()) {
 		//Logger.println("Event to start dtmf...");
                 mediaManager.startDtmf(evt.getDtmfKey());
             } else {
 		//Logger.println("Event to stop dtmf...");
                 mediaManager.stopDtmf();
             }
         } finally {
             console.logExit();
         }
     }
 
     public void handleMuteRequest(UserCallControlEvent evt) {
         try {
             console.logEntry();
             mediaManager.mute(!mediaManager.isMuted());
             guiManager.muted(mediaManager.isMuted());
             
             // send a message so the SipStarter knows we've muted
             String status = (mediaManager.isMuted()?"Muted":"Unmuted");
             Logger.println("Softphone " + status);
         } finally {
             console.logExit();
         }
     }
 
     /**
      * Tries to launch the NIST traces viewer.
      * Changes made by M.Ranganathan to match new logging system.
      */
     public void handleDebugToolLaunch() {
         try {
             console.logEntry();
             if (Utils.getPreference("com.sun.mc.softphone.sip.SERVER_LOG") == null) {
                 console.showMsg(
                     "Debug not supported!",
                     "Messages are not logged. Specify SERVER_LOG property");
                 return;
             }
 
             if (tracesViewerWindow != null) {
                 tracesViewerWindow.pack();
                 tracesViewerWindow.setVisible(true);
             }
             Class tracesViewerClass;
             Constructor tracesViewerConstructor;
             try {
                 tracesViewerClass = Class.forName(
                     "tools.tracesviewer.TracesViewer");
                 tracesViewerConstructor =
                     tracesViewerClass.getConstructor(new Class[] {
                     String.class,
                     String.class,
                     String.class,
                     String.class,
                     String.class,
                     String.class
                 });
                 tracesViewerWindow = (Window) tracesViewerConstructor.
                     newInstance(
                     new Object[] {
                     "SipCommunicator Traces",
                     Utils.getPreference("com.sun.mc.softphone.sip.SERVER_LOG"),
                     "com/sun/mc/softphone/common/resource/back.gif",
                     "com/sun/mc/softphone/common/resource/faces.jpg",
                     "com/sun/mc/softphone/common/resource/comp.gif",
                     "com/sun/mc/softphone/common/resource/nistBanner.jpg"
                 }
                     );
                 //Do not Center as it goes on top of the phone and there's no point in doing it.
 //            int x = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()
 //            				- tracesViewerWindow.getWidth())/2;
 //			int y = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()
 //            				- tracesViewerWindow.getHeight())/2;
 //            tracesViewerWindow.setLocation(x, y);
             } catch (Throwable ex) {
                 console.showException(
                     "The following exception occurred while trying "
                     + "to launch the TracesViewerDebugTool\n" + ex.getMessage(),
                     ex);
                 return;
             }
             tracesViewerWindow.pack();
             tracesViewerWindow.setVisible(true);
         }
         finally {
             console.logExit();
         }
     }
 
     private boolean shuttingDown;
 
     class ShutdownThread extends Thread {
 
 	public void run() {
 	    Logger.println("ShutdownThread shutting down");
 	    shuttingDown = true;
 	    shutDown();
 	}
 
     }
 
     public void handleExitRequest() {
 	if (!showGUI() && callInProgressInterlocutor != null) {
     	    guiManager.showPhoneFrame(false);
     	    Logger.println("Softphone is hidden");
 	    return;
 	}
 
 	Logger.println("Got exit request");
         shutDown();
     }
 
     public void endCalls() {
         //close all sip calls
         try {
 	    if (mediaManager != null) {
 	        mediaManager.stopPlayingAllFiles();
 	    }
 
 	    if (sipManager != null) {
                 sipManager.endAllCalls();
 	    }
         } catch (CommunicationsException exc) {
             console.showException(
                 "Could not properly terminate all calls!\n", exc);
         } catch (Throwable exc){
             console.error("Failed to properly end active calls", exc);
         }
     }
 
     private boolean shutdown;
 
     public void shutDown() {
 	if (shutdown) {
 	    return;
 	}
 
 	shutdown = true;
 
 	Logger.println("Shutting down the SIP Communicator");
 
 	Utils.setPreference("com.sun.mc.softphone.sip.PUBLIC_ADDRESS", "");
 
         try {
             console.logEntry();
             //close all media streams
             //close capure devices
             try {
                 mediaManager.stop();
             } catch (Exception exc) {
                 console.showException(
                 "Could not properly close media streams!\n", exc);
             } catch (Throwable exc) {
                 console.error("Failed to properly close media streams", exc);
             }
 
 	    endCalls();
 
             //unregister
             try {
                 //sipManager.unregister();
		if (sipManager != null) {
		    sipManager.stop();
		}
             } catch (CommunicationsException ex) {
                 console.showException("Could not unregister!", ex);
             } catch(Throwable exc) {
                 console.error("Failed to properly unregister", exc);
             }
         } catch(Exception ex) {
             console.error("Failed to properly shut down.", ex);
             if (rmiRegistryProcess != null) {
                 rmiRegistryProcess.destroy();
             }
         } finally {
             console.logExit();
 
 	    Logger.close();
 
 	    if (shuttingDown) {
 		return;  // we were called from the shutdown thread
 	    }
 
             System.exit(0);
         }
     }
 
 //======================= COMMUNICATIONS LISTENER ==============================
 
     private Interlocutor callInProgressInterlocutor;
     private Interlocutor newCallInterlocutor;
 
     public synchronized void callReceived(CallEvent evt) {
 	mediaManager.stopPlayingAllFiles();
 
         try {
             console.logEntry();
             Call call = evt.getSourceCall();
             Interlocutor interlocutor = new Interlocutor();
 	    call.setInterlocutor(interlocutor);
             interlocutor.setCall(call);
 
             guiManager.addInterlocutor(interlocutor);
 
 	    /*
 	     * Add ourself for call state changes.
 	     */
             call.addStateChangeListener(this);
         } finally {
             console.logExit();
         }
     }
 
     public void messageReceived(MessageEvent evt) {
         try {
             console.logEntry();
             String fromAddress = evt.getFromAddress();
             String fromName = evt.getFromName();
             String messageBody = evt.getBody();
             console.showDetailedMsg(
                 "Incoming MESSAGE",
                 "You received a MESSAGE\n"
                 + "From:    " + fromName + "\n"
                 + "Address: " + fromAddress + "\n"
                 + "Message: " + messageBody + "\n");
         } finally {
             console.logExit();
         }
     }
 
     public void callRejectedLocally(CallRejectedEvent evt) {
         try {
             console.logEntry();
             String reason = evt.getReason();
             String detailedReason = evt.getDetailedReason();
             console.showDetailedMsg(
                 "An incoming call was rejected!\n"
                 + evt.getReason(),
                 evt.getDetailedReason());
 	    Logger.println(
                 "An incoming call was rejected!\n"
                 + evt.getReason() + " " + 
                 evt.getDetailedReason());
 	    mediaManager.stopPlayingAllFiles();
         } finally {
             console.logExit();
         }
     }
 
     public void callRejectedRemotely(CallRejectedEvent evt) {
         try {
             console.trace(
                 "Entering callRejectedRemotely(CallRejectedEvent evt)");
             String reason = evt.getReason();
             String detailedReason = evt.getDetailedReason();
             //console.showDetailedMsg(
             //    "The remote party rejected your call!\n"
             //    + evt.getReason(),
             //    evt.getDetailedReason());
 	    Logger.println(evt.getReason());
 
 	    mediaManager.stopPlayingAllFiles();
 
 	    if (evt.getReason().indexOf("NOT FOUND") >= 0) {
 		Logger.println("The phone number is not valid.");
 		Logger.println("If this is an outside number, "
 		    + "you must prepend 9-1 or 9011");
 
 		Console.showErrorUI("Can't Place Call",
 		    "Invalid phone number",
 		    "If this is an outside number, "
 		        + "you must prepend 9-1 or 9011");
 	    }
         } finally {
             console.logExit();
         }
     }
 
     public void registered(RegistrationEvent evt) {
         try {
             console.logEntry();
 
             guiManager.setGlobalStatus(GuiManager.REGISTERED,
                                        evt.getReason());
         } finally {
             console.logExit();
         }
     }
 
     public void registering(RegistrationEvent evt) {
         try {
             console.logEntry();
 
             guiManager.setGlobalStatus(GuiManager.REGISTERING,
                                        evt.getReason());
         } finally {
             console.logExit();
         }
     }
 
     public void unregistered(RegistrationEvent evt) {
         try {
             console.logEntry();
 
             guiManager.setGlobalStatus(GuiManager.NOT_REGISTERED,
                                        evt.getReason());
 
             //we could now exit
             synchronized(unregistrationLock)
             {
                 unregistrationLock.notifyAll();;
             }
 
         } finally {
             console.logExit();
         }
     }
 
     public void unregistering(RegistrationEvent evt) {
         try {
             console.logEntry();
 
             guiManager.setGlobalStatus(GuiManager.NOT_REGISTERED,
                                        evt.getReason());
 
             int waitUnreg = 3000;	// 3 seconds
 
             try {
                 //we get here through a _synchronous_ call from shutdown so let's try
                 //and wait for unregistrations confirmation in case the registrar has requested authorization
                 //before conriming unregistration
                 synchronized(unregistrationLock) {
 		    unregistrationLock.wait(waitUnreg);
 		}
             } catch (InterruptedException ex) {
                 console.error("Failed to wait for sip-communicator to unregister", ex);
             } catch (NumberFormatException ex) {
                 console.error("Value specified for time interval to wait for unregistration was not valid.", ex);
             }
         } finally {
             console.logExit();
         }
     }
 
 
     public void receivedUnknownMessage(UnknownMessageEvent evt) {
         try {
             console.logEntry();
 
 	    if (mediaManager.isStarted()) {
 		console.error("Unexpected message ignored: " + evt.getMessageName());
 	    }
 
             //console.showDetailedMsg(
             //    "Unknown Communications Message",
             //    "SipCommunicator's SipManager didn't know how to handle the message " +
             //    evt.getMessageName() + "\n"
             //    + "in the current context!\n"
             //    + "(See Details) ",
             //    evt.getMessage()
             //    );
         } finally {
             console.logExit();
         }
     }
 
     public void communicationsErrorOccurred(CommunicationsErrorEvent evt) {
         try {
             console.trace(
                 "Entering communicationsErrorOccurred(CommunicationsErrorEvent evt)");
 
 	
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
                 console.showException(
                     "SipManager encountered the following error\n"
                     + evt.getCause().getMessage() + "\n",
                     evt.getCause());
 	    } else {
                 console.showError(
                     "SipManager encountered the following error\n"
                     + evt.getCause().getMessage() + "\n" + evt.getCause());
 	    }
         } finally {
             console.logExit();
         }
     }
 
 //======================= CALL LISTENER ==============================
     public synchronized void callStateChanged(CallStateEvent evt) {
         try {
             console.logEntry();
             Call call = evt.getSourceCall();
 
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("SipCommunicator New state is " 
 		    + evt.getNewState());
 	    }
 
             if (evt.getNewState() == Call.ALERTING) {
 		int autoAnswerDelay = Utils.getIntPreference(
 		    "com.sun.mc.softphone.AUTOANSWER_DELAY");
 
 		if (autoAnswerDelay > 0) {
 		    autoAnswer = true;
 		}
 
 	        if (autoAnswer) {
 		    if (callInProgressInterlocutor != null) {
                         /*
 			 * There is already a call in progress.
 			 * End that call and wait until it's disconnected
 			 * to answer the new call.
 			 */
 		        try {
 			    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 			        Logger.println("Ending previous call...");
 			    }
 			    newCallInterlocutor = call.getInterlocutor();
 			    int callId = callInProgressInterlocutor.getID();
                             callInProgressInterlocutor = null;
                             sipManager.endCall(callId);
 			    return;
 			} catch (CommunicationsException e) {
         	        }
 		    }
 
 		    Interlocutor interlocutor = call.getInterlocutor();
 
 		    if (autoAnswerDelay > 0) {
 			new AutoAnswerHandler(interlocutor, autoAnswerDelay);
 			return;
 		    }
 
 		    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		        Logger.println("Auto answering now");
 		    }
 
 		    answerCall(interlocutor);
 		    callInProgressInterlocutor = interlocutor;
 	        }
             } else if (evt.getNewState() == Call.CONNECTED) {
 	        if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		    Logger.println("Connected...");
 		}
 
 	        if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		    Logger.println("Setting remote sdp...");
 		}
 
                 try {
                     //mediaManager.setRemoteSdpData(call.getRemoteSdpDescription());
 
 		    synchronized (mediaManager) {
                         mediaManager.start();
 		    }
                 } catch (IOException ex) {
                     console.showError(
                         "The following exception occurred while trying to open media connection:\n"
                         + ex.getMessage());
 
 		    if (Logger.logLevel >= Logger.LOG_INFO) {
                         ex.printStackTrace();
 		    }
 		}
 //You better not send an error response. User would terminate call if they wish so.
 //                try {
 //                    sipManager.sendServerInternalError(call.getID());
 //                } catch (CommunicationsException ex1) {
 //                    //Ignore
 //                    console.println("Failed to send an error response. " + ex1.getMessage());
 //                }
 //		  shutDown();
             } else if (evt.getNewState() == Call.DISCONNECTED) {
 		Logger.println("Disconnected");
 
                 // restart the global media manager
                 try {
                     mediaManager.restart();
                 } catch (IOException e) {
                     console.error("Failed to stop mediaManager " + e.getMessage());
                 }
 
 	        callInProgressInterlocutor = null;
 
 		if (shuttingDown == false) {
 		    /*
 		     * If we do this while shutting down, we could wait forever.
 		     */
                     guiManager.muted(false);
 		}
 
 		if (newCallInterlocutor != null) {
 		    /*
 		     * Previous call is disconnected.  Answer new call.
 		     */
 		    Interlocutor interlocutor = newCallInterlocutor;
 		    newCallInterlocutor = null;
 
 		    if (autoAnswerDelay > 0) {
 			new AutoAnswerHandler(interlocutor, autoAnswerDelay);
 			return;
 		    }
 
 		    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		        Logger.println("Auto answering now");
 		    }
 		    answerCall(interlocutor);
 		    callInProgressInterlocutor = interlocutor;
 		}
             }
         } finally {
             console.logExit();
         }
 
         if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("SipCommunicator done with status");
 	}
     }
 //========================== Security Authority ==============================
     /**
      * Implements obtainCredentials from SecurityAuthority.
      * @param realm the realm that credentials are needed for
      * @return the credentials for the specified realm or null if no credentials
      * could be obtained
      */
     private UserCredentials credentials;
 
     public UserCredentials obtainCredentials(String realm, UserCredentials defaultValues) {
         try{
             console.logEntry();
 
 	    if (this.credentials != null) {
 		return credentials;
 	    }
 
             UserCredentials credentials = new UserCredentials();
 
 	    if (!showGUI() || autoLogin) {
 		/*
                  * We're running as part of Meeting Central.
                  * Use default credentials
                  */
                 credentials.setUserName(defaultValues.getUserName());
                 credentials.setAuthenticationUserName(defaultValues.getAuthenticationUserName());
                 credentials.setPassword(null);
 	    } else {
                 guiManager.requestAuthentication(realm,
                                                  defaultValues.getUserName(),
                                                  defaultValues.getAuthenticationUserName(),
                                                  defaultValues.getPassword());
 
 		if (guiManager.getUserName() != null) {
 		    authenticationUserName = guiManager.getAuthenticationUserName();
 		    userName = guiManager.getUserName();
 
                     credentials.setUserName(guiManager.getUserName());
                     credentials.setAuthenticationUserName(guiManager.getAuthenticationUserName());
                     credentials.setPassword(guiManager.getAuthenticationPassword());
 		} else {
                     String authenticationUser = utils.getAuthenticationUserName();
 
                     String pass = Utils.getProperty("com.sun.mc.softphone.sip.PASSWORD");
                     char[] password = null;
 
                     if (pass == null || pass.length() == 0) {
                         password = null;
                     } else {
                         password = pass.toCharArray();
             	    }
 
 		    String userName = utils.getUserName();
 
 		    Utils.setProperty("user.name", userName);
                     credentials.setUserName(userName);
                     credentials.setAuthenticationUserName(authenticationUser);
                     credentials.setPassword(password);
 		}
 	    }
 
 	    this.credentials = credentials;
             return credentials;
         } finally {
             console.logExit();
         }
     }
 
     /**
      * Changes made by M.Ranganathan to match new logging system.
      */
     private void initDebugTool() {
         try {
             console.logEntry();
             // If the trace level is already set then just bail out.
             if (Utils.getProperty
                 ("gov.nist.javax.sip.TRACE_LEVEL") != null) {
                 return;
             }
             // Location where the server log is collected.
             if (Utils.getProperty
                 ("gov.nist.javax.sip.SERVER_LOG") == null) {
                 Utils.setProperty("gov.nist.javax.sip.SERVER_LOG",
                                   "./log/serverlog.txt");
             }
             // 16 or above logs the messages only.
             if (Utils.getProperty
                 ("gov.nist.javax.sip.TRACE_LEVEL") == null) {
                 Utils.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
             }
         } finally {
             console.logExit();
         }
     }
 
     public void handlePropertiesSaveRequest() {
 	//Logger.println("handlePropertiesSaveRequest...");
 
         try {
             console.logExit();
 	    //XXXJP
             console.showMsg(
                 "Notice",
                 "Changes will take effect next time you start SipCommunicator.");
         } finally {
             console.logExit(); ;
         }
     }
 
     /*
      * This is called when a user changes the media settings
      */
     public void mediaChanged() {
 	if (mediaManager == null) {
 	    return;
 	}
 
         String encoding =
 	    Utils.getPreference("com.sun.mc.softphone.media.ENCODING");
 
         int sampleRate = 
 	    Utils.getIntPreference("com.sun.mc.softphone.media.SAMPLE_RATE");
 
         int channels =
 	    Utils.getIntPreference("com.sun.mc.softphone.media.CHANNELS");
 
 	mediaManager.mediaChanged(encoding, sampleRate, channels, true);
     }
     
     public void setRemoteSdpData(String sdp) {
 	mediaManager.setRemoteSdpData(sdp);
     }
 
     public void setAutoLogin(boolean autoLogin) {
 	this.autoLogin = autoLogin;
     }
 
     public boolean autoLogin() {
 	return autoLogin;
     }
 
     public void setShowGUI(boolean showGUI) {
         this.showGUI = showGUI;
     }
     
     public boolean showGUI() {
 	return showGUI;
     }
     
     public void setUserName(String userName) {
 	this.userName = userName;
     }
 
     public String getUserName() {
 	return utils.getUserName();
     }
 
     public String getAuthenticationUserName() {
 	return utils.getAuthenticationUserName();
     }
 
     public void setOpenAudio(boolean openAudio) {
         this.openAudio = openAudio;
     }
     
     public boolean openAudio() {
         return openAudio;
     }
 
     public void setAutoAnswer(boolean autoAnswer) {
         this.autoAnswer = autoAnswer;
     }
     
     public boolean autoAnswer() {
         return autoAnswer;
     }
     
     public void setLoadGen(boolean loadGen) {
         this.loadGen = loadGen;
     }
     
     public void setMeetingCode(String meetingCode) {
 	this.meetingCode = meetingCode;
     }
 
     public void setMute() {
 	this.mute = true;
     }
 
     public void setNCalls(int nCalls) {
 	this.nCalls = nCalls;
     }
 
     public void setPassCode(String passCode) {
 	this.passCode = passCode;
     }
 
     public void setPhoneNumber(String phoneNumber) {
 	this.phoneNumber = phoneNumber;
     }
 
     public void setTreatment(String treatment) {
 	this.treatment = treatment;
     }
 
     public void setDutyCycle(int dutyCycle) {
 	this.dutyCycle = dutyCycle;
     }
 
     public boolean loadGen() {
         return loadGen;
     }
 
     public void setFromMC(boolean fromMC) {
         this.fromMC = fromMC;
     }
     
     public boolean fromMC() {
         return fromMC;
     }
     
     class AutoAnswerHandler extends Thread {
 
 	Interlocutor interlocutor;
 	int autoAnswerDelay;
 
 	public AutoAnswerHandler(Interlocutor interlocutor, 
 		int autoAnswerDelay) {
 
 	    this.interlocutor = interlocutor;
 	    this.autoAnswerDelay = autoAnswerDelay;
 
 	    start();
 	}
 
 	public void run() {
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Auto answering in " + autoAnswerDelay + "ms");
 	    }
 
 	    try {
 		Thread.sleep(autoAnswerDelay);
 	    } catch (InterruptedException e) {
 	    }
 
 	    answerCall(interlocutor);
 	    callInProgressInterlocutor = interlocutor;
 	}
     }
 
 }
