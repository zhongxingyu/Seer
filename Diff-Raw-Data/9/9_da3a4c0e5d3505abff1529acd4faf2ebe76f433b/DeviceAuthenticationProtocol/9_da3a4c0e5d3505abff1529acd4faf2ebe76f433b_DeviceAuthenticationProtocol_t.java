 package tangible.protocols;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import managers.DeviceFinderAccess;
 import tangible.devices.TangibleDevice;
 import tangible.enums.Capacity;
 import tangible.gateway.TangibleGateway;
 import tangible.utils.CallBack;
 import tangible.utils.JsonProtocolHelper;
 import tangible.utils.exceptions.WrongProtocolException;
 import tangible.utils.exceptions.WrongProtocolJsonSyntaxException;
 import tangible.utils.exceptions.WrongProtocolVersionException;
 
 /**
  *
  * @author leo
  */
 public class DeviceAuthenticationProtocol extends AbsJsonTCPProtocol {
 
     private String _api_protocol_version;
     private TangibleGateway gateway;
 
     @Override
     protected void handleDisconnection() {
         if (gateway != null) {
             gateway.handleDisconnection();
         } else {
             Logger.getLogger(DeviceAuthenticationProtocol.class.getName()).log(Level.INFO, "There is a problem with the socket that occured during the authentication... ");
         }
     }
 
     public static abstract class DeviceFoundCallBack
             implements CallBack<TangibleDevice, Boolean> {
     }
 
     public DeviceAuthenticationProtocol(Socket s, int timeout,
             String api_protocol_version) throws IOException {
         super(s);
         s.setSoTimeout(timeout);
         _api_protocol_version = api_protocol_version;
     }
 
     public void authenticateDevices(final DeviceFoundCallBack cb)
             throws WrongProtocolException, IOException {
         try {
             authenticateDevicesV3(cb);
         } catch (WrongProtocolException ex) {
             this.sendJSON(ex);
             throw ex;
         }
     }
 
     public void finalizeAuthentication() {
         finalizeAuthenticationV2();
     }
 
     private void finalizeAuthenticationV2() {
         JsonObject obj = new JsonObject();
         obj.add("success", new JsonPrimitive(true));
         obj.add("msg", new JsonPrimitive("OK_Authentication_successful"));
         this.sendJsonCtrlMsg(obj);
     }
 
     private void authenticateDevicesV3(DeviceFoundCallBack cb)
             throws WrongProtocolException, IOException {
         JsonElement elem = this.readJSON();
 //    Logger.getLogger(DeviceAuthenticationProtocol.class.getName()).log(
 //        Level.INFO, "parsed a Json element: {0}", elem.toString());
 
         //check that we received a msg:
         JsonObject jsonMsg = JsonProtocolHelper.assertObject(elem);
 
         //check that this is actually a msg of type ctrl
         String flow = JsonProtocolHelper.assertStringInObject(jsonMsg, "flow");
         if (!flow.equals("ctrl")) {
             throw new WrongProtocolJsonSyntaxException("The middleware expected a "
                     + "control message");
         }
 
         //proceed the authentication of the devices including the
         //  sifteo communicator and the cubes for instance
         JsonObject content = JsonProtocolHelper.assertObjectInObject(jsonMsg, "msg");
         String type = JsonProtocolHelper.assertStringInObject(content, "type");
 
         //check the API version
         String version = JsonProtocolHelper.assertStringInObject(content, "protocolVersion");
         if (!version.equals(_api_protocol_version)) {
             throw new WrongProtocolVersionException(version, _api_protocol_version);
         }
 
         //now let's talk!
         //we have the type, 
         //we need the reportable events (but we'll keep that for later in a more evolved version)
         //we need the list of devId concerned by the authentication: 
         JsonArray devId_json =
                 JsonProtocolHelper.assertArrayInObject(content, "devices");
         String[] devIds =
                 JsonProtocolHelper.assertArrayOfOneKind(devId_json, String.class);
         JsonArray capacities_json =
               JsonProtocolHelper.assertArrayInObject(content, "capacities");
         String[] capacities_str =
                 JsonProtocolHelper.assertArrayOfOneKind(capacities_json, String.class);
         Capacity[] capacities = new Capacity[capacities_str.length];
         for (int i = 0; i < capacities_str.length; i++) {
             capacities[i] = Capacity.valueOf(capacities_str[i]);
         }
         int width = -1, height = -1;
         boolean screenSize_available = false;
         try {
             JsonArray screenSize_json = JsonProtocolHelper.assertArrayInObject(content, "screenSize");
             width = JsonProtocolHelper.assertInt(screenSize_json.get(0));
             height = JsonProtocolHelper.assertInt(screenSize_json.get(1));
             screenSize_available = true;
         } catch (WrongProtocolJsonSyntaxException e) {
             //no screen size for this protocol...
         }
         //now we have what we need to create a tangible gateway and it's protocol... 
         gateway = new TangibleGateway();
         TangibleGatewayProtocol gw_protocol = new TangibleGatewayProtocol(_sock, type, _api_protocol_version, capacities, gateway);
         if (screenSize_available) {
             gw_protocol.setScreenSize(height, width);
         }
         gateway.attachCommunication(gw_protocol);
         for (String devId : devIds) {
             TangibleDevice dev = new TangibleDevice(gateway, devId);
             gateway.add(dev);
             TangibleDeviceProtocol dev_talk = new TangibleDeviceProtocol(dev);
             dev.attachDeviceProtocol(dev_talk);
             cb.callback(dev);
         }
         gw_protocol.startReading();
         System.out.println("Authentication successful!\n\twe know have " + DeviceFinderAccess.getInstance().getDevices().size() + " devices connected");
         finalizeAuthentication();
     }
 }
