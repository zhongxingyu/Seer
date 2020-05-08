 package com.washingtonpost.videocomments.web;
 
 import com.washingtonpost.videocomments.model.VideoComment;
 import com.washingtonpost.videocomments.service.VideoCommentsService;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.UUID;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.io.IOUtils;
 import org.red5.logging.Red5LoggerFactory;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
 
 @Controller
 public class VideoController {
 
     private Logger log = Red5LoggerFactory.getLogger(VideoController.class);
 
     @Autowired
     private VideoCommentsService videoCommentsService;
 
     @RequestMapping(value = "/create", method = RequestMethod.GET)
     public void create(HttpServletResponse response) throws IOException {
         UUID id = videoCommentsService.createNewComment();
         response.getWriter().write(id.toString());
     }
 
     @RequestMapping(value = "/complete", method = RequestMethod.GET)
     public void complete(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
         UUID uuid = UUID.fromString(id);
         videoCommentsService.complete(uuid);
         response.getWriter().write("OK");
     }
 
 
     @RequestMapping(value = "/video", method = RequestMethod.GET)
     public void video(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
         UUID uuid = UUID.fromString(id);
         checkComment(uuid);
         response.setContentType("video/x-flv");
         File file = new File(videoCommentsService.getPath() + File.separator + uuid.toString() + ".flv");
         if (!file.exists()) {
             throw new RuntimeException("Not found");
         }
         InputStream stream = null;
         try {
             stream = new FileInputStream(file);
             IOUtils.copy(stream, response.getOutputStream());
         } finally {
             IOUtils.closeQuietly(stream);
         }
     }
 
     private void checkComment(UUID id) {
         VideoComment comment = videoCommentsService.loadComment(id);
         if (!comment.isComplete()) {
             throw new IllegalArgumentException("Not created yet");
         }
     }
 
     @RequestMapping(value = "/thumbnail", method = RequestMethod.GET)
     public void thumbnail(@RequestParam("id") String id, HttpServletResponse response) throws IOException {
         UUID uuid = UUID.fromString(id);
         checkComment(uuid);
         response.setContentType("image/jpeg");
         File file = new File(videoCommentsService.getPath() + File.separator + uuid.toString() + ".jpg");
         if (!file.exists()) {
             throw new RuntimeException("Not found");
         }
         InputStream stream = null;
         try {
             stream = new FileInputStream(file);
             IOUtils.copy(stream, response.getOutputStream());
         } finally {
             IOUtils.closeQuietly(stream);
         }
     }
 
 
     @RequestMapping(value = "/thumbnail", method = RequestMethod.POST)
     public void thumbnail(@RequestParam("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
         UUID uuid = UUID.fromString(id);
         VideoComment comment = videoCommentsService.loadComment(uuid);
         if (comment.isComplete()) {
             throw new IllegalArgumentException("Is complete");
         }
         File file = new File(videoCommentsService.getPath(), id + ".jpg");
         if (file.exists()) {
             file.delete();
         }
         if (!file.createNewFile()) {
             throw new IllegalArgumentException("Can't create file " + file.getAbsolutePath());
         }
         FileOutputStream fileOutputStream = null;
         try {
             fileOutputStream = new FileOutputStream(file);
             IOUtils.copy(request.getInputStream(), fileOutputStream);
             videoCommentsService.addThumbnail(uuid);
         } finally {
             IOUtils.closeQuietly(fileOutputStream);
         }
         response.getWriter().write("OK");
     }
 
     @RequestMapping(value = "/video", method = RequestMethod.POST)
     public void video(@RequestParam("id") String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
         UUID uuid = UUID.fromString(id);
         VideoComment comment = videoCommentsService.loadComment(uuid);
         if (comment.isComplete()) {
             throw new IllegalArgumentException("Is complete");
         }
         OutputStream fileOutputStream = null;
         try {
             MultipartFile multipartFile = ((DefaultMultipartHttpServletRequest) request).getFile("Filedata");
             String originalName = multipartFile.getOriginalFilename();
             String format = getFormat(originalName);
            comment.setFormat(format);
             File file = new File(videoCommentsService.getPath(), id + "." + format);
             fileOutputStream = new FileOutputStream(file);
             log.debug("Upload video file: " + originalName);
 
             final InputStream filedata = multipartFile.getInputStream();
             IOUtils.copy(filedata, fileOutputStream);
             videoCommentsService.addVideo(uuid, format);
         } finally {
             IOUtils.closeQuietly(fileOutputStream);
         }
        response.getWriter().write(id + "." + comment.getFormat());
     }
 
     private String getFormat(String originalName) {
         originalName = originalName.toLowerCase();
         if (originalName.endsWith(".mp4")) {
             return "mp4";
         } else if (originalName.endsWith(".flv")) {
             return "flv";
         } else if (originalName.endsWith(".3gp")) {
             return "3gp";
         } else if (originalName.endsWith(".webm")) {
             return "webm";
         } else {
             throw new RuntimeException("Unknown video format: " + originalName);
         }
     }
 
 //    @ExceptionHandler(Exception.class)
 //    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 //    public void serviceExceptionHandler(Exception e, HttpServletResponse response) throws IOException {
 //        logger.error("Error", e);
 //    }
 
     public void setVideoCommentsService(VideoCommentsService videoCommentsService) {
         this.videoCommentsService = videoCommentsService;
     }
 }
