 package com.server.cx.service.cx.impl;
 
 import com.cl.cx.platform.dto.DataItem;
 import com.cl.cx.platform.dto.DataPage;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.server.cx.dao.cx.GraphicInfoDao;
 import com.server.cx.dao.cx.StatusTypeDao;
 import com.server.cx.dao.cx.UserInfoDao;
 import com.server.cx.dao.cx.UserStatusMGraphicDao;
 import com.server.cx.dao.cx.spec.GraphicInfoSpecifications;
 import com.server.cx.entity.cx.GraphicInfo;
 import com.server.cx.entity.cx.StatusType;
 import com.server.cx.entity.cx.UserStatusMGraphic;
 import com.server.cx.exception.CXServerBusinessException;
 import com.server.cx.model.ActionBuilder;
 import com.server.cx.service.cx.StatusTypeService;
 import com.server.cx.service.cx.UserSubscribeTypeService;
 import com.server.cx.service.util.BusinessFunctions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.Map;
 
 @Component
 @Transactional(readOnly = true)
 public class StatusTypeServiceImpl extends UserCheckService implements StatusTypeService {
     private static final Logger LOGGER = LoggerFactory.getLogger(StatusTypeServiceImpl.class);
     
     @Autowired
     private StatusTypeDao statusTypeDao;
     
     @Autowired
     private UserStatusMGraphicDao userStatusMGraphicDao;
     
     @Autowired
     private BusinessFunctions businessFunctions;
     
     @Autowired
     private BasicService basicService;
 
     @Autowired
     private GraphicInfoDao graphicInfoDao;
     
     @Autowired
     private UserSubscribeTypeService userSubscribeTypeService;
     
     @Autowired
     private UserInfoDao userInfoDao;
     
     @Autowired
     private ActionBuilder actionBuilder;
     
     @Override
     public DataPage queryAllStatusTypes(String imsi) {
         checkAndSetUserInfoExists(imsi);
         userSubscribeTypeService.checkSubscribeType(userInfo, "status");
         List<StatusType> statusType = Lists.newArrayList(statusTypeDao.findAll());
         final String baseHref = basicService.generateStatusTypeVisitURL(imsi);
         List<DataItem> statusTypeList = generateStatusTypeList(statusType, imsi);
 
         DataPage dataPage = new DataPage();
         dataPage.setOffset(0);
         dataPage.setLimit(statusTypeList.size());
         dataPage.setTotal(1);
         dataPage.setHref(baseHref);
         dataPage.setItems(statusTypeList);
         dataPage.setActions(actionBuilder.buildCancelSubscribeStatusAction(imsi));
         return dataPage;
     }
 
     @Override
     public GraphicInfo getFirstChild(Long statusType) {
         List<GraphicInfo> graphicInfos = graphicInfoDao.findAll(GraphicInfoSpecifications.statusTypeGraphicInfoExcludedUsed(statusType, null),new Sort(Sort.Direction.DESC,"createdOn"));
         if(graphicInfos != null && graphicInfos.size()>0){
             for(GraphicInfo graphicInfo : graphicInfos){
                 if(graphicInfo.getPrice() == 0.0D){
                     return graphicInfo;
                 }
             }
         }
         throw new CXServerBusinessException("该状态包下没有数据!");
     }
 
     private List<DataItem> generateStatusTypeList(List<StatusType> statusTypes, String imsi) {
         List<UserStatusMGraphic>  userStatusMGraphics = userStatusMGraphicDao.findByUserInfo(userInfo);
         List<StatusType> statusTypeList = Lists.transform(userStatusMGraphics, businessFunctions.userStatusMGraphicTransformToStatusType());
         Map<Long,UserStatusMGraphic> userStatusMGraphicMap =  Maps.uniqueIndex(userStatusMGraphics, new Function<UserStatusMGraphic, Long>() {
             @Override
             public Long apply(@Nullable UserStatusMGraphic input) {
                 if(input == null) return null;
                 StatusType statusType = input.getStatusType();
                 return statusType.getId();
             }
         });
         return Lists.transform(statusTypes, businessFunctions.statusTypeTransformToDataItem(imsi,statusTypeList,userStatusMGraphicMap));
     }
     
    @Transactional(readOnly = false)
     @Override
     public DataPage subscribeAndQueryStatusTypes(String imsi) {
         LOGGER.info("Into subscribeAndQueryStatusTypes imsi = " + imsi);
         checkAndSetUserInfoExists(imsi);
         userSubscribeTypeService.checkUserUnSubscribeType(userInfo, "status");
         userSubscribeTypeService.subscribeType(userInfo, "status");
         return queryAllStatusTypes(imsi);
     }
 
     @Override
     public void cancelSubscribeStatusType(String imsi) {
         userSubscribeTypeService.cancelSubscribeType(imsi, "status");
     }
 
 }
