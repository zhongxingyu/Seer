 package com.game.rania.net;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.HashMap;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.game.rania.Config;
 import com.game.rania.controller.CommandController;
 import com.game.rania.controller.command.AddPlanetCommand;
 import com.game.rania.controller.command.AddUserCommand;
 import com.game.rania.controller.command.ChatNewMessageCommand;
 import com.game.rania.controller.command.RemoveUserCommand;
 import com.game.rania.controller.command.SetTargetCommand;
 import com.game.rania.controller.command.SwitchScreenCommand;
 import com.game.rania.model.Domain;
 import com.game.rania.model.Nebula;
 import com.game.rania.model.Player;
 import com.game.rania.model.Target;
 import com.game.rania.model.User;
 import com.game.rania.model.Location;
 import com.game.rania.model.Planet;
 import com.game.rania.model.items.Item;
 import com.game.rania.screen.MainMenu;
 import com.game.rania.userdata.Command;
 import com.game.rania.userdata.Client;
 import com.game.rania.userdata.IOStream;
 import com.game.rania.utils.Condition;
 
 public class NetController {
 
 	private Receiver receiver = null;
 	private CommandController cController = null;
 	private Client mClient = null;
 	private int ProtocolVersion = 2;
 	
 	public NetController(CommandController commandController){
 		cController = commandController;
 	}
 	
 	public void dispose(){
 		if (receiver != null)
 			receiver.stopThread();
 	}
 
 	public void sendTouchPoint(int x, int y, int currentX, int currentY)
 	{
 		byte[] data = new byte[16];
 		byte[] xArr = intToByteArray(x);
 		byte[] yArr = intToByteArray(y);
 		byte[] userxArr = intToByteArray(currentX);
 		byte[] useryArr = intToByteArray(currentY);
 		System.arraycopy(xArr, 0, data, 0, 4);
 		System.arraycopy(yArr, 0, data, 4, 4);
 		System.arraycopy(userxArr, 0, data, 8, 4);
 		System.arraycopy(useryArr, 0, data, 12, 4);
 		try
 		{
 			mClient.stream.sendCommand(Command.touchPlayer, data);
 		}
 		catch (Exception ex)
 		{
 
 		}
 	}
 	
 	public void sendTarget(Target target)
 	{
 		byte[] data = new byte[8];
 		byte[] targetTypeArr = intToByteArray(target.type);
 		byte[] targetArr = intToByteArray(target.id);
 		System.arraycopy(targetTypeArr, 0, data, 0, 4);
 		System.arraycopy(targetArr, 0, data, 4, 4);
 		try
 		{
 			mClient.stream.sendCommand(Command.setTarget, data);
 		}
 		catch (Exception ex)
 		{
 
 		}
 	}
 	
 	public int getServerTime(){
 		return mClient.serverTime;
 	}
 
 	public boolean login(String Login, String Password)
 	{
 		mClient = new Client();
 		mClient.login = Login;
 		mClient.socket = null;
 		mClient.isLogin = false;
 		try
 		{
 			mClient.socket = new Socket(InetAddress.getByName(Config.serverAddress), Config.serverPort);
 			if (mClient.socket.isConnected())
 			{
 				mClient.stream = new IOStream(mClient.socket.getInputStream(), mClient.socket.getOutputStream());
 				byte[] LoginArr = Login.getBytes("UTF-16LE");
 				byte[] LoginLenArr = intToByteArray(LoginArr.length);
 				byte[] ProtocolVersionArr = intToByteArray(ProtocolVersion);
 				byte[] data = new byte[LoginArr.length+LoginLenArr.length+ProtocolVersionArr.length];
 				System.arraycopy(ProtocolVersionArr, 0, data, 0, 4);
 				System.arraycopy(LoginLenArr, 0, data, 4, 4);
 				System.arraycopy(LoginArr, 0, data, 8, LoginArr.length);
 				mClient.stream.sendCommand(Command.login, data);
 				mClient.stream.sendCommand(Command.password, Password.getBytes("UTF-16LE"));
 				Command answer = mClient.stream.readCommand();
 				if (answer.idCommand == Command.login)
 				{
 					mClient.isLogin = true;
 					mClient.serverTime = GetIntValue(answer.data, new AddressCommand());
 					receiver = new Receiver(mClient, this);
 					receiver.start();
 					return true;
 				}
 				
 				if (answer.idCommand == Command.faillogin)
 					mClient.isLogin = false;
 				if (answer.idCommand == Command.failversion)
 					mClient.isLogin = false;
 			}
 		}
 		catch (Exception ex)
 		{
 			return false;
 		}
 		return false;
 	}
 	
 	public void disconnect()
 	{
 		try
 		{
 			if (mClient != null && mClient.socket.isConnected() && mClient.isLogin) {
 				mClient.stream.sendCommand(Command.disconnect);
 				receiver.stopThread();
 				mClient.socket.shutdownInput();
 				mClient.socket.shutdownOutput();
 				mClient.socket.close();
 			}
 		}
 		catch (Exception ex)
 		{
 			
 		}
 	}
 	public void sendChatMessage(String Message, int channel)
 	{
 		String toPilot = "";
 		if (Message.isEmpty())
 			return;
 
 		try {
			byte[] ChannelArr = intToByteArray(channel);
 			byte[] MessageArr = Message.getBytes("UTF-16LE");
 			byte[] MessageLenArr = intToByteArray(MessageArr.length);
 			byte[] toPilotArr = toPilot.getBytes("UTF-16LE");
 			byte[] toPilotLenArr = intToByteArray(toPilotArr.length);
 			byte[] data = new byte[ChannelArr.length+MessageArr.length+MessageLenArr.length+toPilotArr.length+toPilotLenArr.length];
 			System.arraycopy(ChannelArr, 0, data, 0, 4);
 			System.arraycopy(MessageLenArr, 0, data, 4, 4);
 			System.arraycopy(MessageArr, 0, data, 8, MessageArr.length);
 			System.arraycopy(toPilotLenArr, 0, data, 8+MessageArr.length, 4);
 			System.arraycopy(toPilotArr, 0, data, 8+MessageArr.length+4, toPilotArr.length);
 			mClient.stream.sendCommand(Command.message, data);
 		
 		} catch (Exception ex) {
 		}
 	}
 	
 	public void clientRelogin()
 	{
 		//mClient.relogin
 	}
 	
 	public HashMap<Integer, User> getNearUsers()
 	{
 		HashMap<Integer, User> UsersMap = new HashMap<Integer, User>();
 		try
 		{
 			mClient.stream.sendCommand(Command.users);
 			Command command = waitCommand(Command.users);
 			AddressCommand ArrPtr = new AddressCommand();
 			int UsersCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<UsersCount; i++)
 			{
 				int UserId = GetIntValue(command.data, ArrPtr);
 				int ShipNameLen = GetIntValue(command.data, ArrPtr);
 				String ShipName = GetStringValue(command.data, ArrPtr, ShipNameLen);
 				int UserX = GetIntValue(command.data, ArrPtr);
 				int UserY = GetIntValue(command.data, ArrPtr);
 				int UserTargetX = GetIntValue(command.data, ArrPtr);
 				int UserTargetY = GetIntValue(command.data, ArrPtr);
 				int UserDomain = GetIntValue(command.data, ArrPtr);
 				User userShip = new User(UserId, UserX, UserY, ShipName, "", UserDomain);
 				userShip.setPositionTarget(UserTargetX, UserTargetY);
 				UsersMap.put(userShip.id, userShip);
 			}
 		}
 		catch (Exception ex)
 		{
 
 		}
 		return UsersMap;
 	}
 
 	public HashMap<Integer, Planet> getPlanets(int idLocation, boolean wait)
 	{
 		HashMap<Integer, Planet> planets = new HashMap<Integer, Planet>();
 		try
 		{
 			mClient.stream.sendCommand(Command.planets, intToByteArray(idLocation));
 			if (!wait)
 				return null;
 			
 			Command command = waitCommand(Command.planets);
 			AddressCommand ArrPtr = new AddressCommand(4);
 			int PlanetsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<PlanetsCount; i++)
 			{
 				int PlanetId = GetIntValue(command.data, ArrPtr);
 				int PlanetNameLen = GetIntValue(command.data, ArrPtr);
 				String PlanetName = GetStringValue(command.data, ArrPtr, PlanetNameLen);
 				int PlanetType = GetIntValue(command.data, ArrPtr);
 				int PlanetSpeed = GetIntValue(command.data, ArrPtr);
 				int PlanetOrbit = GetIntValue(command.data, ArrPtr);
 				int PlanetRadius = GetIntValue(command.data, ArrPtr);
 				Color color = GetColorValue(command.data, ArrPtr);
 				Color atmColor = GetColorValue(command.data, ArrPtr);
 				int PlanetDomain = GetIntValue(command.data, ArrPtr);
 				int PlanetAtmosphere_speedX = GetIntValue(command.data, ArrPtr);
 				int PlanetAtmosphere_speedY = GetIntValue(command.data, ArrPtr);
 				Planet planet = new Planet(PlanetId, PlanetName, PlanetType, PlanetRadius, PlanetSpeed, PlanetOrbit, idLocation, PlanetDomain, PlanetAtmosphere_speedX, PlanetAtmosphere_speedY);
 				planet.color  = color;
 				planet.atmColor  = atmColor;
 				planets.put(PlanetId, planet);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return planets;
 	}
 	
 	public HashMap<Integer, Location> getAllLocations()
 	{
 		HashMap<Integer, Location> locations = new HashMap<Integer, Location>();
 		try
 		{
 			mClient.stream.sendCommand(Command.locations);
 			Command command = waitCommand(Command.locations);
 			AddressCommand ArrPtr = new AddressCommand();
 			int LocationsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<LocationsCount; i++)
 			{
 				Location Loc   = new Location();
 				Loc.id = GetIntValue(command.data, ArrPtr);
 				int StarNameLen = GetIntValue(command.data, ArrPtr);
 				Loc.starName = GetStringValue(command.data, ArrPtr, StarNameLen);
 				Loc.starType = GetIntValue(command.data, ArrPtr);
 				Loc.x = GetIntValue(command.data, ArrPtr);
 				Loc.y = GetIntValue(command.data, ArrPtr);
 				Loc.starRadius = GetIntValue(command.data, ArrPtr);
 				Loc.domain = GetIntValue(command.data, ArrPtr);
 				locations.put(Loc.id, Loc);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return locations;
 	}
 	
 	public HashMap<Integer, Nebula> getAllNebulas()
 	{
 		HashMap<Integer, Nebula> nebulas = new HashMap<Integer, Nebula>();
 		try
 		{
 			mClient.stream.sendCommand(Command.nebulas);
 			Command command = waitCommand(Command.nebulas);
 			AddressCommand ArrPtr = new AddressCommand();
 			int NebulasCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0;i<NebulasCount;i++)
 			{
 				int NebId = GetIntValue(command.data, ArrPtr);
 				int NebType = GetIntValue(command.data, ArrPtr);
 				int NebX = GetIntValue(command.data, ArrPtr);
 				int NebY = GetIntValue(command.data, ArrPtr);
 				int NebScale = GetIntValue(command.data, ArrPtr);
 				int NebAngle = GetIntValue(command.data, ArrPtr);
 				Nebula Neb = new Nebula(NebId, NebType, NebX, NebY, NebAngle, NebScale);
 				nebulas.put(Neb.id, Neb);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return nebulas;
 	}
 	
 	public HashMap<Integer, Item> getAllItems()
 	{
 		HashMap<Integer, Item> items = new HashMap<Integer, Item>();
 		try
 		{
 			mClient.stream.sendCommand(Command.nebulas);
 			Command command = waitCommand(Command.nebulas);
 			AddressCommand ArrPtr = new AddressCommand();
 			int ItemsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0;i<ItemsCount;i++)
 			{
 				Item item = new Item();
 				item.id = GetIntValue(command.data, ArrPtr);
 				item.type = GetIntValue(command.data, ArrPtr);
 				int ItemNameLen = GetIntValue(command.data, ArrPtr);
 				item.description = GetStringValue(command.data, ArrPtr, ItemNameLen);
 				item.power = GetIntValue(command.data, ArrPtr);
 				item.weight = GetIntValue(command.data, ArrPtr);
 				item.vendor = GetIntValue(command.data, ArrPtr);
 				item.region_id = GetIntValue(command.data, ArrPtr);
 				items.put(item.id, item);
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return items;
 	}
 
 	public HashMap<Integer, Domain> getAllDomains()
 	{
 		HashMap<Integer, Domain> domains = new HashMap<Integer, Domain>();
 		try
 		{
 			mClient.stream.sendCommand(Command.domains);
 			Command command = waitCommand(Command.domains);
 			AddressCommand ArrPtr = new AddressCommand();
 			int DomainsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0;i<DomainsCount;i++)
 			{
 				Domain domain = new Domain();
 				domain.id = GetIntValue(command.data, ArrPtr);
 				domain.color = GetColorValue(command.data, ArrPtr);
 				int DomainNameLen = GetIntValue(command.data, ArrPtr);
 				domain.DomainName = GetStringValue(command.data, ArrPtr, DomainNameLen);
 				domains.put(domain.id, domain);
				Gdx.app.log("Domain", domain.DomainName +": "+ "R:"+String.format("%s",domain.color.r)+"  G:"+String.format("%s",domain.color.g)+"  B:"+String.format("%s",domain.color.b)+"  A:"+String.format("%s",domain.color.a));
 			}
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return domains;
 	}
 	
 	public Player getPlayerData()
 	{
 		try
 		{
 			mClient.stream.sendCommand(Command.player);
 
 			Command command = waitCommand(Command.player);
 
 			AddressCommand ArrPtr = new AddressCommand();
 			int UserId = GetIntValue(command.data, ArrPtr);			
 			int UserX = GetIntValue(command.data, ArrPtr);
 			int UserY = GetIntValue(command.data, ArrPtr);
 			int UserDomain = GetIntValue(command.data, ArrPtr);
 			int UserInPlanet = GetIntValue(command.data, ArrPtr);
 			int PnameLen = GetIntValue(command.data, ArrPtr);
 			String PName = GetStringValue(command.data, ArrPtr, PnameLen);			
 			int SnameLen = GetIntValue(command.data, ArrPtr);			
 			String SName = GetStringValue(command.data, ArrPtr, SnameLen);			
 			Player player = new Player(UserId, UserX, UserY, PName, SName, UserDomain, UserInPlanet);
 			return player;
 		}
 		catch (Exception ex)
 		{
 			clientRelogin();
 		}
 		return null;
 	}
 
 	public static int byteArrayToInt(byte[] b)
 	{
 		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
 	}
 
 	public static byte[] intToByteArray(int a)
 	{
 		return new byte[] { (byte)((a >> 24) & 0xFF), (byte)((a >> 16) & 0xFF), (byte)((a >> 8) & 0xFF), (byte)(a & 0xFF) };
 	}
 	
 	//commands 
 	private Condition cWaitCommand = new Condition(), cCopyCommand = new Condition();
 	private volatile int idWaitCommand = Command.none;
 	private volatile Command currentCommand = null;
 	
 	public Command waitCommand(int idCommand) throws InterruptedException{
 		idWaitCommand = idCommand;
 		
 		cWaitCommand.signalWait();
 		Command command = currentCommand;
 		cCopyCommand.signal();
 		return command;
 	}
 
 	public void processingCommand(Command command) throws InterruptedException, UnsupportedEncodingException {
 		if (idWaitCommand != Command.none && idWaitCommand == command.idCommand)
 		{			
 			idWaitCommand = Command.none;
 			currentCommand = command;
 			cWaitCommand.signal();
 			cCopyCommand.signalWait();
 			return;
 		}
 		
 		switch (command.idCommand) {
 		case Command.addUser:
 		{
 			AddressCommand ArrPtr = new AddressCommand();
 			int UserId = GetIntValue(command.data, ArrPtr);
 			int ShipNameLen = GetIntValue(command.data, ArrPtr);
 			String ShipName = GetStringValue(command.data, ArrPtr, ShipNameLen);
 			int UserX = GetIntValue(command.data, ArrPtr);
 			int UserY = GetIntValue(command.data, ArrPtr);
			int TargetX = GetIntValue(command.data, ArrPtr);
			int TargetY = GetIntValue(command.data, ArrPtr);
 			int UserDomain = GetIntValue(command.data, ArrPtr);
 			User user = new User(UserId, UserX, UserY, ShipName, "", UserDomain);
 			cController.addCommand(new AddUserCommand(user));
 			break;
 		}
 		case Command.touchUser:
 		{
 			AddressCommand ArrPtr = new AddressCommand();
 			int UserId = GetIntValue(command.data, ArrPtr);
 			int UserTouchX = GetIntValue(command.data, ArrPtr);
 			int UserTouchY = GetIntValue(command.data, ArrPtr);
 			cController.addCommand(new SetTargetCommand(UserId, UserTouchX, UserTouchY));
 			break;
 		}
 		case Command.removeUser:
 		{
 			int UserId = GetIntValue(command.data, new AddressCommand());
 			cController.addCommand(new RemoveUserCommand(UserId));
 			break;
 		}
 		case Command.disconnect:
 		{
 			try
 			{
 				receiver.stopThread();
 				mClient.socket.shutdownInput();
 				mClient.socket.shutdownOutput();
 				mClient.socket.close();
 				cController.addCommand(new SwitchScreenCommand(new MainMenu()));
 			}
 			catch (Exception ex)
 			{
 			}
 		}
 		case Command.message:
 		{
 			AddressCommand ArrPtr = new AddressCommand();
 			int 	channel 	= GetIntValue(command.data, ArrPtr);
 			int 	messageLen 	= GetIntValue(command.data, ArrPtr);
 			String 	message 	= GetStringValue(command.data, ArrPtr, messageLen);
 			int 	nameLen 	= GetIntValue(command.data, ArrPtr);
 			String 	userName 	= GetStringValue(command.data, ArrPtr, nameLen);
 			int 	toPilotLen 	= GetIntValue(command.data, ArrPtr);
 			String 	toPilot 	= GetStringValue(command.data, ArrPtr, toPilotLen);
 			cController.addCommand(new ChatNewMessageCommand(userName, channel, message, toPilot));
 			break;
 		}
 		case Command.planets:
 		{
 			AddressCommand ArrPtr = new AddressCommand(0);
 			int locID = GetIntValue(command.data, ArrPtr);
 			int PlanetsCount = GetIntValue(command.data, ArrPtr);
 			for (int i=0; i<PlanetsCount; i++)
 			{
 				int PlanetId      = GetIntValue(command.data, ArrPtr);
 				int PlanetNameLen = GetIntValue(command.data, ArrPtr);
 				String PlanetName = GetStringValue(command.data, ArrPtr, PlanetNameLen);
 				int PlanetType 	  = GetIntValue(command.data, ArrPtr);
 				int PlanetSpeed   = GetIntValue(command.data, ArrPtr);
 				int PlanetOrbit   = GetIntValue(command.data, ArrPtr);
 				int PlanetRadius  = GetIntValue(command.data, ArrPtr);
 				Color PlanetColor = GetColorValue(command.data, ArrPtr);
 				Color AtmColor = GetColorValue(command.data, ArrPtr);
 				int PlanetDomain = GetIntValue(command.data, ArrPtr);
 				int PlanetAtmosphere_speedX = GetIntValue(command.data, ArrPtr);
 				int PlanetAtmosphere_speedY = GetIntValue(command.data, ArrPtr);
 				Planet planet = new Planet(PlanetId, PlanetName, PlanetType, PlanetRadius, PlanetSpeed, PlanetOrbit, locID, PlanetDomain, PlanetAtmosphere_speedX, PlanetAtmosphere_speedY);
 				planet.color = PlanetColor;
 				planet.atmColor = AtmColor;
 				cController.addCommand(new AddPlanetCommand(planet));
 			}
 			break;
 		}
 
 		default:
 			break;
 		}
 	}
 
 	private int GetIntValue(byte[] data, AddressCommand AC)
 	{
 		int Res=0;
 		byte[] Arr = new byte[4];
 		System.arraycopy(data, AC.address, Arr, 0, 4);
 		AC.delta(4);
 		Res = byteArrayToInt(Arr);
 		return Res;
 	}
 
 	private String GetStringValue(byte[] data, AddressCommand AC, int SL)
 	{
 		String Res = "";
 		byte[] Arr = new byte[SL];
 		System.arraycopy(data, AC.address, Arr, 0, SL);
 		AC.delta(SL);
 		try {
 			Res = new String(Arr, "UTF-16LE");
 		} catch (UnsupportedEncodingException e) {
 
 		}
 		return Res;
 	}
 	
 	 private Color GetColorValue(byte[] data, AddressCommand AC)
 	 {
 		 char[] ColorArr = new char[4];
 			for (int j=0;j<4;j++)
 			{
 				ColorArr[j]=(char)data[AC.address];
 				AC.delta(1);
 			}
 		Color Res =	new Color(ColorArr[0] / 255.0f, ColorArr[1] / 255.0f, ColorArr[2] / 255.0f, ColorArr[3] / 255.0f);
 		return Res;
 	 }
 	
 	class AddressCommand {
 		public AddressCommand() {
 			this.address = 0;
 		}
 		public AddressCommand(int start){
 			this.address = start;
 		}
 		
 		public int address;
 		public void delta(int delta){
 			address += delta;
 		}
 	}
 }
