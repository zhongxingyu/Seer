 package org.jahia.modules.wurfl;
 
 import net.sourceforge.wurfl.core.Device;
 import net.sourceforge.wurfl.core.WURFLManager;
 import net.sourceforge.wurfl.core.resource.ModelDevice;
 import net.sourceforge.wurfl.core.resource.WURFLModel;
 import org.jahia.services.channels.ChannelProvider;
 import org.jahia.services.channels.ChannelService;
 import org.springframework.beans.factory.InitializingBean;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class WURFLProvider implements ChannelProvider, InitializingBean {
     private ChannelService channelService;
     private WURFLModel wurflModel;
     private WURFLManager wurflManager;
     private int priority;
 
     public void setWurflModel(WURFLModel wurflModel) {
         this.wurflModel = wurflModel;
     }
 
     public void setWurflManager(WURFLManager wurflManager) {
         this.wurflManager = wurflManager;
     }
 
     public int getPriority() {
         return priority;
     }
 
     public void setPriority(int priority) {
         this.priority = priority;
     }
 
     public ChannelService getChannelService() {
         return channelService;
     }
 
     public void setChannelService(ChannelService channelService) {
         this.channelService = channelService;
     }
 
     public Map<String, String> getChannelCapabilities(String identifier) {
         Map<String,String> results = new HashMap<String, String>();
         ModelDevice deviceById = wurflModel.getDeviceById(identifier);
         if (!deviceById.getFallBack().equals("root")) {
             results.putAll(channelService.getChannel(deviceById.getFallBack()).getCapabilities());
         }
 
         results.putAll(deviceById.getCapabilities());
 
         return results;
     }
 
     public List<String> getAllChannels() {
         return new ArrayList<String>();
     }
 
 //    @Override
 //    public Channel getChannel(String identifier) {
 //        Channel channel = super.getChannel(identifier);
 //        if (channel == null) {
 //            channel = wurflChannels.get(identifier);
 //        }
 //        if (channel == null) {
 //            channel = new Channel(identifier,identifier);
 //            ModelDevice deviceById = wurflModel.getDeviceById(channel.getIdentifier());
 //            if (deviceById != null) {
 //                List<ModelDevice> hierarchy = wurflModel.getDeviceHierarchy(deviceById);
 //                for (ModelDevice device : hierarchy) {
 //                    channel.getCapabilities().putAll(device.getCapabilities());
 //                }
 //            }
 //            wurflChannels.put(identifier, channel);
 //        }
 //        return channel;
 //    }
 //
     public String resolveChannel(HttpServletRequest request) {
         Device deviceForRequest = wurflManager.getDeviceForRequest(request);
         String id = deviceForRequest.getId();
         return id;
     }
 
     public void afterPropertiesSet() throws Exception {
         channelService.addProvider(this);
     }
 
     public String getFallBack(String identifier) {
         ModelDevice deviceById = wurflModel.getDeviceById(identifier);
         return deviceById.getFallBack();
     }
 
     public boolean isVisible(String identifier) {
         //
         return true;
     }
 }
