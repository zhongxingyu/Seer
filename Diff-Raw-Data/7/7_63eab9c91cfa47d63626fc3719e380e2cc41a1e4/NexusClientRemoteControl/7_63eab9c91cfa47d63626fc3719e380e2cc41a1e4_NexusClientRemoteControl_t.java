 package com.nexus.client;
 
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.nexus.EnumPlayerstate;
 import com.nexus.LogLevel;
 import com.nexus.NexusServer;
 import com.nexus.Packet;
 import com.nexus.client.remotecontrol.EnumRemoteColors;
 import com.nexus.client.remotecontrol.RemoteControlButton;
 import com.nexus.client.remotecontrol.RemoteControlLabel;
 import com.nexus.webserver.WebServerStatus;
 
 public class NexusClientRemoteControl extends NexusClient {
 	
 	public NexusClientPlayout ControllingPlayoutClient;
 
 	public RemoteControlButton PlayButton = new RemoteControlButton("", EnumRemoteColors.Purple, "PlayButton", this);
 	public RemoteControlButton PrevButton = new RemoteControlButton("", EnumRemoteColors.Purple, "PrevButton", this);
 	public RemoteControlButton NextButton = new RemoteControlButton("", EnumRemoteColors.Purple, "NextButton", this);
 
 	public RemoteControlLabel PrevNowplayingLabel = new RemoteControlLabel("", EnumRemoteColors.White, "PrevNowplaying", this);
 	public RemoteControlLabel NowplayingLabel = new RemoteControlLabel("", EnumRemoteColors.White, "Nowplaying", this);
 	public RemoteControlLabel NextNowplayingLabel = new RemoteControlLabel("", EnumRemoteColors.White, "NextNowplaying", this);
 
 	public RemoteControlButton Cam1Button = new RemoteControlButton("", EnumRemoteColors.Purple, "Cam1Button", this);
 	public RemoteControlButton Cam2Button = new RemoteControlButton("", EnumRemoteColors.Purple, "Cam2Button", this);
 	public RemoteControlButton Cam3Button = new RemoteControlButton("", EnumRemoteColors.Purple, "Cam3Button", this);
 	public RemoteControlButton Cam4Button = new RemoteControlButton("", EnumRemoteColors.Purple, "Cam4Button", this);
 	public RemoteControlButton Cam5Button = new RemoteControlButton("", EnumRemoteColors.Purple, "Cam5Button", this);
 	
 	public NexusClientRemoteControl(String ClientName, String Token) {
 		super(ClientName, Token);
 	}
 
 	public NexusClientRemoteControl(String ClientName, int ClientID, String Token) {
 		super(ClientName, ClientID, Token);
 	}
 	
 	public int GetClientTypeID() {
 		return 1;
 	}
 
 	public String GetClientTypeName() {
 		return "RemoteControl";
 	}
 	
 	public void OnCurrentNowplayingUpdate(int ClientID){
		if(this.ControllingPlayoutClient == null) return;
 		if(ClientID == this.ControllingPlayoutClient.GetClientID()){
 			NowplayingLabel.SetText(ControllingPlayoutClient.GetCurrentNowplaying().toString());
 		}
 	}
 	
 	public void OnPreviousNowplayingUpdate(int ClientID){
		if(this.ControllingPlayoutClient == null) return;
 		if(ClientID == this.ControllingPlayoutClient.GetClientID()){
 			PrevNowplayingLabel.SetText(ControllingPlayoutClient.GetPreviousNowplaying().toString());
 		}
 	}
 	
 	public void OnUpcomingNowplayingUpdate(int ClientID){
		if(this.ControllingPlayoutClient == null) return;
 		if(ClientID == this.ControllingPlayoutClient.GetClientID()){
 			NextNowplayingLabel.SetText(ControllingPlayoutClient.GetUpcomingNowplaying().toString());
 		}
 	}
 	
 	public void OnPlayerstateChange(int ClientID){
		if(this.ControllingPlayoutClient == null) return;
 		if(ClientID == this.ControllingPlayoutClient.GetClientID()){
 			PlayButton.SetColor(GetButtonColorFromState("PlayButton",this.ControllingPlayoutClient.GetPlayerstate()));
 			PlayButton.SetText(ControllingPlayoutClient.GetPlayerstate() == EnumPlayerstate.PLAYING ? "Pause":"Play");
 		}
 	}
 	
 	private EnumRemoteColors GetButtonColorFromState(String Button, Object State){
 		NexusServer.config.readSettings();
 		Map<String, String> Config = NexusServer.settings;
 		EnumRemoteColors Color = EnumRemoteColors.White;
 		String StateStr = "";
 		try{
 			if(Button == "PlayButton"){
 				StateStr = ((EnumPlayerstate) State).toString();
 				String ColorStr = Config.get("Color" + Button + StateStr);
 				Color = new Gson().fromJson(ColorStr, EnumRemoteColors.class);
 			}
 		}catch(Exception e){}
 		return Color;
 	}
 
 	public void OnDataReceived(Packet Package) throws Exception {
 		super.OnDataReceived(Package);
 		if(Package.IsNotify) return; //fixes a bug
 		switch(Package.Data.split("/")[2].toUpperCase()){
 		case "CONNECT":
 			if(!Package.Internal){
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"Errors\": \"You are not allowed to send this command to another client\"}");
 			}
 			NexusClient client = NexusServer.ClientManager.GetClientByID(Integer.parseInt(Package.Data.split("/")[3]));
 			if(client instanceof NexusClientPlayout){
 				ControllingPlayoutClient = (NexusClientPlayout) client;
 
 				PlayButton.SetColor(EnumRemoteColors.White);
 				PrevButton.SetColor(EnumRemoteColors.White);
 				PrevButton.SetText("Vorige");
 				NextButton.SetColor(EnumRemoteColors.White);
 				NextButton.SetText("Volgende");
 				
 				OnCurrentNowplayingUpdate(client.GetClientID());
 				OnPreviousNowplayingUpdate(client.GetClientID());
 				OnUpcomingNowplayingUpdate(client.GetClientID());
 				OnPlayerstateChange(client.GetClientID());
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"Errors\": \"None\"}");
 				NexusServer.log.write(this.GetClientTypeName() + " client " + this.GetClientID() + " is now controlling " + client.GetClientTypeName() + " client " + client.GetClientID(), LogLevel.Info);
 			}else if(client instanceof NexusClient){
 				Cam1Button.SetColor(EnumRemoteColors.White);
 				Cam2Button.SetColor(EnumRemoteColors.White);
 				Cam3Button.SetColor(EnumRemoteColors.White);
 				Cam4Button.SetColor(EnumRemoteColors.White);
 				Cam5Button.SetColor(EnumRemoteColors.White);
 				Cam1Button.SetText("CAM1");
 				Cam2Button.SetText("CAM2");
 				Cam3Button.SetText("CAM3");
 				Cam4Button.SetText("CAM4");
 				Cam5Button.SetText("CAM5");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"Errors\": \"You cannot control this client\"}");
 			}
 			break;
 		case "PLAY":
 		case "NEXT":
 		case "PREV":
 		case "PAUSE":
 			if(!Package.Internal){
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"Errors\": \"You are not allowed to send this command to another client\"}");
 			}
 			Package.DestinationClient = ControllingPlayoutClient;
 			ControllingPlayoutClient.OnDataReceived(Package);
 			break;
 		}
 	}
 }
