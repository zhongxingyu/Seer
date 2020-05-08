 package cn.sunjiachao.s7blog.modules.comment.controller;
 
 import cn.sunjiachao.s7blog.modules.comment.service.ICommentService;
 import cn.sunjiachao.s7common.model.Comment;
 import cn.sunjiachao.s7common.model.dto.JsonResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.Date;
 import java.util.List;
 
 @Controller
 public class CommentController {
 
     @Autowired
     private ICommentService commentService;
 
     @Autowired
     private JsonResponse result;
 
     @RequestMapping(method = RequestMethod.POST, value = "/comment/post")
     @ResponseBody
     public JsonResponse postComment(Comment comment) {
         comment.setCreateTime(new Date());
         comment.setGuestName("test");
        commentService.createNewComment(comment);
        result.setMessage("发表留言成功！");
         return result;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/comment/listByBlog")
     @ResponseBody
     public List<Comment> listCommentsByBlog(int blogId) {
         List<Comment> comments = commentService.getCommentsByBlog(blogId);
         return comments;
     }
 
 }
