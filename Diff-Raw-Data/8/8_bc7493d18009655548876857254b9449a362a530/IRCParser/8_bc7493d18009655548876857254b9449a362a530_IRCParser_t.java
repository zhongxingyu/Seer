 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package dmdirc.parser;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 public class IRCParser implements Runnable {
 
 	public static final int ndInfo = 1;
 //	public static final int ndSomething = 2;
 
 	public class MyInfo {
 		public String sNickname = "IRCParser";
 		public String sAltNickname = "IRC-Parser"; // Alternative nickname, if this fails, we start prepending _ to sNickname
 		public String sRealname = "Java Test IRCParser";
 		public String sUsername = "IRCParser";
 	}
 
 	private Socket socket = null;
 	private PrintWriter out = null;
 	private BufferedReader in = null;
 	
 	public MyInfo me = new MyInfo(); // This is what the user wants, nickname here is *not* fact.
 
 	private String sThinkNickname; // This is what we want the nickname to be.
 	private String sConfirmedNickname; // This is what the nickname actually is.
 	private boolean TriedAlt = false;
 
 	private boolean Got001 = false;
 	private boolean Got005 = false;
 	private boolean HasBegan = false;
 	private boolean IsFirst = true;
 
 	// Events
 	public interface IDebugInfo { public void onDebug(IRCParser tParser, int nLevel, String sData); }
 	public interface IMOTDEnd { public void onMOTDEnd(IRCParser tParser); }
 	public interface IDataIn { public void onDataIn(IRCParser tParser, String sData); }
 	public interface IDataOut { public void onDataOut(IRCParser tParser, String sData, boolean FromParser); }
 	public interface INickInUse { public void onNickInUse(IRCParser tParser); }
 	class AllEvents {
 		ArrayList<IDebugInfo> DebugInfo = new ArrayList<IDebugInfo>();
 		ArrayList<IMOTDEnd> EndOfMOTD = new ArrayList<IMOTDEnd>();
 		ArrayList<IDataIn> DataIn = new ArrayList<IDataIn>();
 		ArrayList<IDataOut> DataOut = new ArrayList<IDataOut>();
 		ArrayList<INickInUse> NickInUse = new ArrayList<INickInUse>();
 	}
 	public AllEvents cb = new AllEvents();
 
 	private void AddCallback (Object eMethod, ArrayList CallbackList) {
 		for (int i = 0; i < CallbackList.size(); i++) {
 			if (eMethod.equals(CallbackList.get(i))) { return; }
 		}
 		CallbackList.add(eMethod);
 	}
 	public void DelCallback (Object eMethod, ArrayList CallbackList) {
 		for (int i = 0; i < CallbackList.size(); i++) {
 			if (eMethod.equals(CallbackList.get(i))) { CallbackList.remove(i); break; }
 		}
 	}
 
 	public void AddDebugInfo(Object eMethod) { AddCallback(eMethod, cb.DebugInfo); }
 	public void DelDebugInfo(Object eMethod) { DelCallback(eMethod, cb.DebugInfo); }
 	private boolean CallDebugInfo(int level, String data) { 
 		boolean bResult = false;
 		for (int i = 0; i < cb.DebugInfo.size(); i++) {
 			cb.DebugInfo.get(i).onDebug(this, level, data);
 			bResult = true;
 		}
 		return bResult;
 	}
 
 	public void AddMOTDEnd(Object eMethod) { AddCallback(eMethod, cb.EndOfMOTD); }
 	public void DelMOTDEnd(Object eMethod) { DelCallback(eMethod, cb.EndOfMOTD); }
 	private boolean CallMOTDEnd() { 
 		boolean bResult = false;
 		for (int i = 0; i < cb.EndOfMOTD.size(); i++) {
 			cb.EndOfMOTD.get(i).onMOTDEnd(this);
 			bResult = true;
 		}
 		return bResult;
 	}
 
 	public void AddDataIn(Object eMethod) { AddCallback((IDataIn)eMethod, cb.DataIn); }
 	public void DelDataIn(Object eMethod) { DelCallback((IDataIn)eMethod, cb.DataIn); }
 	private boolean CallDataIn(String data) { 
 		boolean bResult = false;
 		for (int i = 0; i < cb.DataIn.size(); i++) {
 			cb.DataIn.get(i).onDataIn(this, data);
 			bResult = true;
 		}
 		return bResult;
 	}
 
 	public void AddDataOut(Object eMethod) { AddCallback((IDataOut)eMethod, cb.DataOut); }
 	public void DelDataOut(Object eMethod) { DelCallback((IDataOut)eMethod, cb.DataOut); }
 	private boolean CallDataOut(String data, boolean FromParser) { 
 		boolean bResult = false;
 		for (int i = 0; i < cb.DataOut.size(); i++) {
 			cb.DataOut.get(i).onDataOut(this, data, FromParser);
 			bResult = true;
 		}
 		return bResult;
 	}
 
 	public void AddNickInUse(Object eMethod) { AddCallback(eMethod, cb.NickInUse); }
 	public void DelNickInUse(Object eMethod) { DelCallback(eMethod, cb.NickInUse); }
 	private boolean CallNickInUse() { 
 		boolean bResult = false;
 		for (int i = 0; i < cb.NickInUse.size(); i++) {
 			cb.NickInUse.get(i).onNickInUse(this);
 			bResult = true;
 		}
 		return bResult;
 	}
 
 	// Constructor.
 	public IRCParser () { }
 
 
 	private void ResetState() {
 			TriedAlt = false;
 			Got001 = false;
 			Got005 = false;
 	};
 
 	public void connect(String sHost) throws Exception {
 		try {
 			ResetState();
 			connect(sHost,6667);
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	public void connect(String sHost, int nPort) throws Exception {
 		if (HasBegan) { return; }
 		try {
 			socket = new Socket(sHost,nPort);
 			out = new PrintWriter(socket.getOutputStream(), true);
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		} catch (UnknownHostException e) {
 			throw new Exception("Socket Exception");
 		} catch (Exception e) {
 			throw new Exception("General Exception");
 		}
 	}
 
 	public void run() {
 		if (HasBegan) { return; } else { HasBegan = true; }
 		// :HACK: While true loops really really suck.
 		while(true) {
 			String line = "";
 			try {
 				line = in.readLine(); // Blocking :/
 				if (IsFirst) {
 					SetNickname(me.sNickname);
 					SendString("USER "+me.sUsername.toLowerCase()+" * * :"+me.sRealname);
 					IsFirst = false;
 				}
 				ProcessLine(line);
 			} catch (IOException e) {
 				System.out.println("Socket read failed");
 				System.exit(-1);
 			}
 		}
 	}
 
 	protected void finalize(){
 		try {
 			socket.close();
 		} catch (IOException e) {
 			System.out.println("Could not close socket");
 			System.exit(-1);
 		}
 	}
 
 	private String GetParam(String line) {
 		String[] params = null;
 		params = line.split(" :",2);
 		return params[params.length-1];
 	}
 
 	private String[] IRCTokenise(String line) {
 		String[] params = null;
 		String[] tokens = null;
 		params = line.split(" :",2);
 		tokens = params[0].split(" ");
 	
 		String[] temp = new String[tokens.length+1];
 		System.arraycopy(tokens, 0, temp, 0, tokens.length);
 		tokens = temp;
 		if (params.length == 2) { tokens[tokens.length-1] = params[1]; }
 		return tokens;
 	}
 
 	public void SendLine(String line) { CallDataOut(line,false); out.printf("%s\r\n",line);} // This should do some checks on stuff, public event!
 	
 	// Our Method
 	private void SendString(String line) {
 		CallDataOut(line,true);
 		out.printf("%s\r\n",line);
 	}
 
 	private void ProcessLine(String line) {
 		String[] token = IRCTokenise(line);
 		String sParam = token[token.length-1];
 
 		int nParam;
 		CallDataIn(line);
 
 		try {nParam = Integer.parseInt(token[1]);} catch (Exception e) { nParam = -1;}
 
 		if (token[0].equals("PING") || token[1].equals("PING")) { SendString("PONG :"+sParam); }
 		else {
 			if (token[0].substring(0,1).equals(":")) {
 				// Post Connect
 				switch (nParam) {
 					case -1:
 						ProcessStringParam(sParam,token);
 						break;
 					case 1: // 001
 						break;
 					case 422: // No MOTD
 					case 376: // End of MOTD
 						ProcessEndOfMOTD(sParam,token);
 						break;
 					case 433: // Nick In Use
 						ProcessNickInUse(sParam,token);
 						break;
 					default: // Unknown
 						break;
 				}
 			} else {
 				// Pre Connect
 			}
 		}
 	}
 
 	private void ProcessStringParam(String sParam,String token[]) {
 		// Process a line where the parameter is a string (IE PRIVMSG, NOTICE etc - Not including PING!)
 	}
 
 	private void ProcessEndOfMOTD(String sParam,String token[]) { CallMOTDEnd(); }
 
 	private void ProcessNickInUse(String sParam,String token[]) {
 		if (!CallNickInUse()) {
 			// Manually handle nick in use.
 			CallDebugInfo(ndInfo,"No Nick in use Handler.");
 			if (!Got001) {
 				CallDebugInfo(ndInfo,"Using inbuilt handler");
 				// If this is before 001 we will try and get a nickname, else we will leave the nick as-is
 				if (!TriedAlt) { SetNickname(me.sAltNickname); TriedAlt = true; }
 				else {
 					if (sThinkNickname.equalsIgnoreCase(me.sAltNickname)) { sThinkNickname = me.sNickname; }
 					SetNickname('_'+sThinkNickname);	
 				}
 			}
 		}
 	}
 
 
 	public void JoinChannel(String sChannelName) {
 		SendString("JOIN "+sChannelName);
 	}
 
 	public void SetNickname(String sNewNickName) {
 		sThinkNickname = sNewNickName;
 		SendString("NICK "+sNewNickName);
 	}
 
 	public void Quit(String sReason) { SendString("QUIT :"+sReason); }
 	// Quit and force Disconnection
 	public void Disconnect(String sReason) {
 		Quit(sReason);
 		try { socket.close(); } catch (Exception e) { /* Meh */ };
 	}	
 }
