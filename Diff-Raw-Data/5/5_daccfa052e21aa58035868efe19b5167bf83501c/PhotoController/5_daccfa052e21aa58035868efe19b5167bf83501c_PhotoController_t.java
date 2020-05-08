 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lefoto.controller.media;
 
 import com.lefoto.common.base.BaseController;
 import com.lefoto.common.base.Const;
 import com.lefoto.common.cache.PhotoCache;
 import com.lefoto.common.cache.UserCache;
 import com.lefoto.common.utils.UpYunUtil;
 import com.lefoto.model.content.LeComment;
 import com.lefoto.model.media.LePhoto;
 import com.lefoto.model.media.LePhotoUp;
 import com.lefoto.model.user.LeUser;
 import com.lefoto.service.iface.content.CommentService;
 import com.lefoto.service.iface.media.PhotoService;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author Eric
  */
 @Controller
 @RequestMapping("/photo")
 public class PhotoController extends BaseController {
 
     @Autowired
     PhotoService photoService;
     @Autowired
     CommentService commentService;
 
     @RequestMapping(value = "/detail")
     public ModelAndView show(HttpServletRequest request) {
         ModelAndView mv = new ModelAndView("/photo/detail");
         int photoId = this.getParaIntFromRequest("photoId");
         LeUser user = this.getRequestUser(request);
         if (user != null) {
             mv.addObject("user", user);
         }
         LePhoto photo = photoService.findPhotoById(photoId);
         List<LeComment> comments = commentService.getCommentsAjax(0, photo.getId(), 0, 20);
         mv.addObject("photo", photo);
         mv.addObject("comments", comments);
         return mv;
     }
 
     @RequestMapping(value = "/deletePhotoByAdmin")
     public @ResponseBody
     String deletePhotoByAdmin(HttpServletRequest request) {
         LeUser user = this.getRequestUser(request);
         if (user == null || !user.getEmail().equals("admin@lefoto.me")) {
             return Const.FAILURE;
         }
         int photoId = this.getParaIntFromRequest("photoId");
         int cateId = this.getParaIntFromRequest("cateId");
         try {
             //从数据库和缓存中删除图片
             LePhoto photo = PhotoCache.getPhotoById(photoId, cateId);
             photoService.deletePhoto(photo);
             //从又拍云上删除图片
             UpYunUtil.delete(photo.getUrl());
         } catch (Exception e) {
             return Const.FAILURE;
         }
         return Const.SUCCESS;
     }
 
     @RequestMapping(value = "/deletePhoto")
     public @ResponseBody
     String deletePhoto(HttpServletRequest request) {
         LeUser user = this.getRequestUser(request);
         int photoId = this.getParaIntFromRequest("photoId");
         LePhoto photo = photoService.findPhotoById(photoId);
         if (photo == null) {
             return Const.FAILURE;
         }
         if (photo.getUserId() != user.getId()) {
             return Const.FAILURE;
         }
         photoService.deletePhoto(photo);
         return Const.SUCCESS;
     }
 
     @RequestMapping(value = "/upPhoto")
     public @ResponseBody
     String upPhoto(HttpServletRequest request) {
         LeUser user = this.getRequestUser(request);
         int photoId = this.getParaIntFromRequest("photoId");
             photoService.upPhoto(photoId, user.getId());
         return Const.SUCCESS;
     }
 
     @RequestMapping(value = "/cancelUpPhoto")
     public @ResponseBody
     String cancelUpPhoto(HttpServletRequest request) {
         LeUser user = this.getRequestUser(request);
         int photoId = this.getParaIntFromRequest("photoId");
         photoService.cancelUpPhoto(photoId, user.getId());
         return Const.SUCCESS;
     }
 
     @RequestMapping(value = "/getUpUsers")
     public ModelAndView getUpUsers(HttpServletRequest request){
         ModelAndView mv = new ModelAndView("/index/likeUsers");
         //List result = new ArrayList();
         int photoId = this.getParaIntFromRequest("photoId");
         List<LePhotoUp> ups = PhotoCache.findPhotoUps(photoId);
         
         List<LeUser> users = new ArrayList<LeUser>();
         if(ups == null){
             return mv;
         }
         for (LePhotoUp up : ups) {
             LeUser user = UserCache.getUserById(up.getUserId());
             if(user == null){continue;}
             users.add(user);
         }
         mv.addObject("upUsers",users);
         return mv;
     }
 }
