 package net.holyc.openflow.handler;
 
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 
 import com.google.gson.Gson;
 
 import org.openflow.protocol.OFPort;
 import org.openflow.protocol.OFPacketIn;
 import org.openflow.protocol.OFPacketOut;
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.OFMatch;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionOutput;
 import org.openflow.util.HexString;
 import org.openflow.util.U16;
 
 import net.holyc.HolyCIntent;
 import net.holyc.dispatcher.OFPacketInEvent;
 import net.holyc.dispatcher.OFReplyEvent;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 /** A L2 flow-based learning switch
  *
  * @author ykk
  */
 
 public class FlowSwitch
     extends BroadcastReceiver
 {
     /** Log name
      */
     private String TAG = "HOLYC.FlowSwitch";
     /** Reference to GSON
      */
     Gson gson = new Gson();
     /** MAC address and port association
      */
    public static HashMap<String, Short> hostPort = new HashMap<String, Short>();
     
     @Override    
 	public void onReceive(Context context, Intent intent) 
     {
 	if(intent.getAction().equals(HolyCIntent.OFPacketIn_Intent.action)){
 	    OFPacketInEvent opi = 
 		gson.fromJson(intent.getStringExtra(HolyCIntent.OFPacketIn_Intent.str_key),
 			      OFPacketInEvent.class);
 	    OFPacketIn opie = opi.getOFPacketIn();
 	    
 	    //Record port and entry
 	    OFMatch ofm = new OFMatch();
 	    ofm.loadFromPacket(opie.getPacketData(),
 			       opie.getInPort());
	    hostPort.put(HexString.toHexString(ofm.getDataLayerSource()), new Short(ofm.getInputPort()));
 
 	    //Find outport if any
 	    Short out;
 	    if (HexString.toHexString(ofm.getDataLayerDestination()).equals("ff:ff:ff:ff:ff:ff"))
 		out = new Short(OFPort.OFPP_FLOOD.getValue());
 	    else
 		out = hostPort.get(HexString.toHexString(ofm.getDataLayerDestination()));
 	    OFActionOutput oao = new OFActionOutput();	    
 	    oao.setMaxLength((short) 0);     
 	    List<OFAction> actions = new ArrayList<OFAction>();
 	    ByteBuffer bb;
 	    Intent poutIntent = new Intent(HolyCIntent.BroadcastOFReply.action);
 	    poutIntent.setPackage(context.getPackageName());
 	    if (out != null)
 	    {
 		//Form flow_mod
 		oao.setPort(out.shortValue());
 		OFFlowMod offm = new OFFlowMod();
 		actions.add(oao);
 		offm.setActions(actions);
 		offm.setMatch(ofm);
                 offm.setOutPort((short) OFPort.OFPP_NONE.getValue());                              
 		offm.setBufferId(opie.getBufferId());
 		offm.setCommand(OFFlowMod.OFPFC_ADD);
 		offm.setIdleTimeout((short) 5);
 		offm.setHardTimeout((short) 0);
 		offm.setPriority((short) 32768);
 		offm.setFlags((short) 1); //Send flow removed
 		offm.setCookie(0);                                                                 
 		offm.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
 
 		bb = ByteBuffer.allocate(offm.getLength());
 		offm.writeTo(bb);
 	    }
 	    else
 	    {
 		//Flood packet
 		oao.setPort(OFPort.OFPP_FLOOD.getValue()); //Flood port
 		OFPacketOut opo = new OFPacketOut();
 		actions.add(oao);
 	    	opo.setActions(actions);
 		opo.setInPort(opie.getInPort());
 		opo.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
 		opo.setBufferId(opie.getBufferId());
 		int length = OFPacketOut.MINIMUM_LENGTH + opo.getActionsLengthU();
 		if (opie.getBufferId()==-1)
 		    opo.setPacketData(opie.getPacketData());
 			length += opie.getPacketData().length;		
 		opo.setLengthU(length);
 		bb = ByteBuffer.allocate(opo.getLength());
 		opo.writeTo(bb);
 	    }
 	    OFReplyEvent ofpoe = new OFReplyEvent(opi.getSocketChannelNumber(),
 						  bb.array());
 	    poutIntent.putExtra(HolyCIntent.BroadcastOFReply.str_key,
 				gson.toJson(ofpoe, OFReplyEvent.class));
 	    context.sendBroadcast(poutIntent);
 	    
 	}
     }    
 }
