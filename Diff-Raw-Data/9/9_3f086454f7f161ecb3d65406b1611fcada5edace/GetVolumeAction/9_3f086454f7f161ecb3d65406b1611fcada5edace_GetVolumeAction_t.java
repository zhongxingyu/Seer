 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.mira.adastra3.server.network.actions.device;
 
 import be.mira.adastra3.server.exceptions.NetworkException;
 import org.teleal.cling.model.action.ActionArgumentValue;
 import org.teleal.cling.model.action.ActionInvocation;
 import org.teleal.cling.model.meta.Service;
 import org.teleal.cling.model.types.Datatype;
 import org.teleal.cling.model.types.UnsignedIntegerOneByte;
 
 /**
  *
  * @author tim
  */
 public class GetVolumeAction extends ActionInvocation {
     public GetVolumeAction(final Service iService) throws NetworkException {
         super(iService.getAction("GetVolume"));
     }
     
    public final Integer getVolume() throws NetworkException {    
         ActionArgumentValue tVolume = getOutput("oVolumeValue");
         if (tVolume == null) {
             throw new NetworkException("state variable not accessible");
         } else if (tVolume.getDatatype().getBuiltin() != Datatype.Builtin.UI1) {
             throw new NetworkException("invalid return type by Media.GetVolume (not an UI1)");
         }
         return (int) (long) ((UnsignedIntegerOneByte) tVolume.getValue()).getValue();
     }
 }
