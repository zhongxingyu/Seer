 package com.cqlybest.weixin.smart;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.cqlybest.common.Constant;
 import com.cqlybest.common.bean.Image;
 import com.cqlybest.common.bean.Product;
 import com.cqlybest.common.bean.ProductDetailMaldives;
 import com.cqlybest.common.bean.maldives.MaldivesSeaIsland;
 import com.cqlybest.common.service.ImageService;
 import com.cqlybest.common.service.MaldivesService;
 import com.cqlybest.common.service.OptionService;
 import com.cqlybest.common.service.ProductService;
 import com.cqlybest.weixin.bean.RequestMessage;
 import com.cqlybest.weixin.bean.ResponseMessage;
 import com.cqlybest.weixin.bean.ResponseNewsMessage;
 import com.cqlybest.weixin.bean.ResponseNewsMessage.Article;
 
 @Component
 public class MaldivesIslandHandler implements Handler {
 
   @Autowired
   private MaldivesService maldivesService;
   @Autowired
   private ProductService productService;
   @Autowired
   private ImageService imageService;
   @Autowired
   private OptionService optionService;
 
   private Map<RequestMessage, MaldivesSeaIsland> cached =
       new HashMap<RequestMessage, MaldivesSeaIsland>();
 
   @Override
   public boolean support(RequestMessage request) {
     String content = request.getContent();
     if (StringUtils.isBlank(content)) {
       return false;
     }
 
     content = content.replaceAll("岛", "");
     if (StringUtils.isBlank(content)) {
       return false;
     }
 
     List<MaldivesSeaIsland> islands = maldivesService.searchIslandByKeyword(content, 1, 1);
     if (islands.isEmpty()) {
       return false;
     }
 
     cached.put(request, islands.get(0));
     return true;
   }
 
   @Override
   public ResponseMessage handle(RequestMessage request) {
    String siteUrl = optionService.getOptions().get(Constant.OPTION_MOBILE_URL);
     ResponseNewsMessage response = new ResponseNewsMessage();
     response.setFromUserName(request.getToUserName());
     response.setToUserName(request.getFromUserName());
     response.setCreateTime(System.currentTimeMillis());
 
     List<Article> articles = new ArrayList<>();
     MaldivesSeaIsland island = cached.remove(request); // 海岛
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
 
     for (Product product : productService.getMaldivesProductByIsland(island.getId(), 5)) {
       images = product.getPosters();
       if (!images.isEmpty()) {
         Article article = new Article();
         article.setTitle(getTitle(product));
         article.setDescription(product.getDescription());
         if (siteUrl != null) {
           article.setPicUrl(siteUrl + "/image/" + images.get(0).getId() + "."
               + images.get(0).getImageType());
           article.setUrl(siteUrl + "/product/" + product.getId() + ".html");
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
 
   private String getTitle(Product product) {
     StringBuilder sb = new StringBuilder(product.getName());
     if (product.getDays() != null) {
       sb.append(product.getDays() + "天");
     }
     if (product.getNights() != null) {
       sb.append(product.getNights() + "晚");
     }
     ProductDetailMaldives detail = (ProductDetailMaldives) product.getDetail();
     if (detail != null) {
       if (detail.getRoom1() != null && detail.getRoom1Unit() != null) {
         sb.append(detail.getRoom1() + detail.getRoom1Unit());
       }
       if (detail.getRoom2() != null && detail.getRoom2Unit() != null) {
         sb.append(detail.getRoom2() + detail.getRoom2Unit());
       }
       if (detail.getRoom3() != null && detail.getRoom3Unit() != null) {
         sb.append(detail.getRoom3() + detail.getRoom3Unit());
       }
     }
     return sb.toString();
   }
 
 }
