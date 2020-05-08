 package org.motechproject.care.reporting.processors;
 
 import org.motechproject.care.reporting.domain.dimension.Flw;
 import org.motechproject.care.reporting.domain.dimension.FlwGroup;
 import org.motechproject.care.reporting.domain.dimension.LocationDimension;
 import org.motechproject.care.reporting.mapper.ProviderSyncMapper;
 import org.motechproject.care.reporting.parser.GroupParser;
 import org.motechproject.care.reporting.parser.ProviderParser;
 import org.motechproject.care.reporting.service.MapperService;
 import org.motechproject.care.reporting.service.Service;
 import org.motechproject.commcare.provider.sync.response.Group;
 import org.motechproject.commcare.provider.sync.response.Provider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.*;
 
 @Component
 public class ProviderSyncProcessor {
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
 
     private GroupParser groupParser;
     private Service service;
     private ProviderParser providerParser;
     private ProviderSyncMapper genericMapper;
 
     @Autowired
     public ProviderSyncProcessor(Service service, MapperService mapperService, ProviderSyncMapper providerSyncMapper) {
         this(mapperService.getGroupInfoParser(), mapperService.getProviderInfoParser(), service, providerSyncMapper);
     }
 
     public ProviderSyncProcessor(GroupParser groupParser, ProviderParser providerParser, Service service, ProviderSyncMapper providerSyncMapper) {
         this.groupParser = groupParser;
         this.service = service;
         this.providerParser = providerParser;
         this.genericMapper = providerSyncMapper;
     }
 
     public void processGroupSync(List<Group> groups) {
         List<FlwGroup> flwGroups = new ArrayList<>();
         for (Group group : groups) {
             String id = group.getId();
             logger.info(String.format("Creating/Updating group with id: %s", id));
             try {
                 FlwGroup flwGroup = processGroup(group);
                 flwGroups.add(flwGroup);
             } catch (Exception e) {
                logger.info(String.format("Error occurred while processing group with id: %s", id));
             }
         }
         service.saveOrUpdateAllByExternalPrimaryKey(FlwGroup.class, flwGroups);
     }
 
     private FlwGroup processGroup(Group group) {
         Map<String, Object> parsedGroup = groupParser.parse(group);
         return genericMapper.map(FlwGroup.class, parsedGroup);
     }
 
     public void processProviderSync(List<Provider> providers) {
         List<Flw> flws = new ArrayList<>();
         Map<String, FlwGroup> flwGroups = new HashMap<>();
         for (Provider provider : providers) {
             try {
                 Flw flw = processProvider(flwGroups, provider);
                 flws.add(flw);
             } catch (Exception e) {
                logger.info(String.format("Error occurred while processing provider with id: %s", provider.getId()));
             }
         }
         service.saveOrUpdateAllByExternalPrimaryKey(Flw.class, flws);
     }
 
     private Flw processProvider(Map<String, FlwGroup> flwGroups, Provider provider) {
         logger.info(String.format("Creating/Updating provider with id: %s", provider.getId()));
         Map<String, Object> parsedProvider = providerParser.parse(provider);
         Flw flw = genericMapper.map(Flw.class, parsedProvider);
         flw.setFlwGroups(new HashSet<>(getAssociatedFlwGroups(provider.getGroups(), flwGroups)));
         flw.setLocationDimension(getLocationDimension(parsedProvider));
         return flw;
     }
 
     private LocationDimension getLocationDimension(Map<String, Object> parsedProvider) {
         return service.getLocation((String) parsedProvider.get("state"), (String) parsedProvider.get("district"),(String)  parsedProvider.get("block"));
     }
 
     private List<FlwGroup> getAssociatedFlwGroups(List<String> groups, Map<String, FlwGroup> existingFlwGroups) {
         ArrayList<FlwGroup> flwGroups = new ArrayList<>();
         if (groups == null)
             return flwGroups;
         for (String groupId : groups) {
             FlwGroup group;
             if (existingFlwGroups.containsKey(groupId)) {
                 group = existingFlwGroups.get(groupId);
             } else {
                 group = service.getOrCreateGroup(groupId);
                 existingFlwGroups.put(groupId, group);
             }
             flwGroups.add(group);
         }
         return flwGroups;
     }
 }
