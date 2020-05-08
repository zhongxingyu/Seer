 import ngmud.CLog;
 import ngmud.ngMUDException;
 import ngmud.network.packets.*;
 import ngmud.network.*;
 import ngmud.util.CConfig;
 import ngmud.util.CPair;
 import java.net.InetSocketAddress;
 import java.util.LinkedList;
 
 import chattest.Server;
 
 public class Test {
 	public static void main(String[] args) throws ngMUDException { // Test-main-Func
 		CLog.Init("",true,CLog.LOG_LEVEL.CUSTOM,(short)5);
 		CLog.Info("Log initialized successfull :D");
 		CLog.Custom("Custom-Test Level 1", 1);
 		CLog.Custom("Custom-Text Level 6", 6);
 		CLog.Custom("Custom-Test Level 5", 5);
 		CLog.Debug("Debug-Test");
 		CLog.Warning("Warnung-Test");
 		CLog.Error("Error-Test");
 		CLog.Force("Force-Test");
 		CLog.CustomForce("Custom-Force Test");
 		
 		//CPacket.InitPackets("Test.ini");
 		CConfig Conf=new CConfig();
 		Conf.Init("Packets.ini",false);
 		CPair<LinkedList<String>,LinkedList<String>> Pair=Conf.GetKeysAndValuesSeperate();
 		CPacket.InitPackets(Pair.GetFirst(),Pair.GetSecond());
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY+1);
 		
 		Server Serv=new Server();
 		Serv.Run();
 		
 		CSocket Socket=new CSocket();
 		CLog.Warning(Socket.Init(new InetSocketAddress("127.0.0.1",3724), 3724, null, 0, 10, false) ? "Con" : "NoCon");
 		CPacket Pack=new CPacket(Socket);
 		while(true)
 		{
 			if(Pack.GetSock().DataAvailable()>0)
 			{
 				CLog.Warning("There is data!");
 			}
 			if(Pack.Recv()==CPacket.RECIEVED.ALL)
 			{
 				CLog.Error(((Pack_Chat)Pack.GetData()).From+": "+CPacketHelper.BytesToString(((Pack_Chat)Pack.GetData()).Content));
 			}
 			try {
 				Thread.sleep(2000);
 			} catch(Exception e) {}
 			Pack_Chat SendPackData=new Pack_Chat();
 			SendPackData.From=1;
 			SendPackData.Content=CPacketHelper.StringToBytes("FHHFFH");
 			CPacket SendPack=new CPacket(Pack.GetSock());
 			SendPack.SetType(Pack_Chat.PACK_NUM);
 			SendPack.SetSubType((short)Pack_Chat.SUB_TYPE.SAY.ordinal());
 			SendPack.SetData(SendPackData);
 			CLog.Warning(SendPack.Send() ? "Sending Data" : "Failure Sending Data");
 		}
 		
 		//CLog.UnInit();
 	}
 
 }
