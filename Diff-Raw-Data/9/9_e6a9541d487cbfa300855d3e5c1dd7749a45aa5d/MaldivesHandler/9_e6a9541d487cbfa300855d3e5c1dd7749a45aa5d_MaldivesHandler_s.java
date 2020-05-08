 package com.cqlybest.weixin.smart;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.cqlybest.common.Constant;
 import com.cqlybest.common.bean.Image;
 import com.cqlybest.common.bean.maldives.MaldivesSeaIsland;
 import com.cqlybest.common.service.ImageService;
 import com.cqlybest.common.service.MaldivesService;
 import com.cqlybest.common.service.OptionService;
 import com.cqlybest.weixin.bean.RequestMessage;
 import com.cqlybest.weixin.bean.ResponseMessage;
 import com.cqlybest.weixin.bean.ResponseNewsMessage;
 import com.cqlybest.weixin.bean.ResponseNewsMessage.Article;
 
 @Component
 public class MaldivesHandler implements Handler {
 
   @Autowired
   private MaldivesService maldivesService;
   @Autowired
   private ImageService imageService;
   @Autowired
   private OptionService optionService;
 
   @Override
   public boolean support(RequestMessage request) {
     String content = request.getContent();
     return content != null && "text".equals(request.getMsgType())
         && content.matches("(.*马尔代夫.*)|(.*馬爾代夫.*)|(.*马代.*)|(.*馬代.*)|(.*maldives.*)|(md)");
   }
 
   @Override
   public ResponseMessage handle(RequestMessage request) {
    String siteUrl = optionService.getOptions().get(Constant.OPTION_WWW_URL);
     ResponseNewsMessage response = new ResponseNewsMessage();
     response.setFromUserName(request.getToUserName());
     response.setToUserName(request.getFromUserName());
     response.setCreateTime(System.currentTimeMillis());
 
     List<Article> articles = new ArrayList<>();
     List<MaldivesSeaIsland> islands = maldivesService.list(1, 5);
     for (MaldivesSeaIsland island : islands) {
       List<Image> images =
           imageService.getImages(Constant.IMAGE_MALDIVES_HOTEL_PICTURE, island.getId());
       if (!images.isEmpty()) {
         Article article = new Article();
         article.setTitle(island.getZhName() + "|" + island.getEnName());
         article.setDescription(island.getAd());
         if (siteUrl != null) {
           article.setPicUrl(siteUrl + "/image/" + images.get(0).getId() + "."
               + images.get(0).getImageType());
           article.setUrl(siteUrl + "/maldives/" + island.getId() + ".html");
         }
         articles.add(article);
       }
     }
     response.setArticles(articles);
     response.setCount(articles.size());
     return response.getCount() == 0 ? null : response;
   }
 
   @Override
   public int priority() {
     return 0;
   }
 
 }
