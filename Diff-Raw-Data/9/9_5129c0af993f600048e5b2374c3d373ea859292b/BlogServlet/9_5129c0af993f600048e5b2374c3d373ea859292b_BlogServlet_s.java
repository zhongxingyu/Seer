 package no.kantega.blog.servlets;
 
 import no.kantega.blog.dao.BlogDao;
 import no.kantega.blog.model.Blog;
 import no.kantega.blog.model.BlogPost;
 import no.kantega.blog.model.BlogPostComment;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 import static no.kantega.blog.services.Services.getService;
 
 
 /**
  *
  */
 @WebServlet(urlPatterns = "/blog/*")
 public class BlogServlet extends HttpServlet {
 
     private BlogDao dao;
     private volatile String content;
 
     @Override
     public void init(ServletConfig servletConfig) throws ServletException {
         this.dao = getService(BlogDao.class, servletConfig.getServletContext());
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         if(isBlogPostRequest(req)) {
             BlogPost post = getBlogPost(req);
             req.setAttribute("post", post);
             req.setAttribute("comments", dao.getComments(post));
 
             req.getRequestDispatcher("/WEB-INF/jsp/post.jsp").forward(req, resp);
         } else {
             Blog blog = getBlog(req);
             req.setAttribute("blog", blog);
 
             req.setAttribute("posts", dao.getBlogPosts(blog));
 
             req.getRequestDispatcher("/WEB-INF/jsp/blog.jsp").forward(req, resp);
         }
 
     }
 
     private boolean isBlogPostRequest(HttpServletRequest req) {
         return req.getRequestURI().indexOf('/', "/blog/".length()) != -1;
     }
 
     private Blog getBlog(HttpServletRequest req) throws UnsupportedEncodingException {
         String requestURI = req.getRequestURI().substring("/blog/".length());
 
         String blogName = URLDecoder.decode(requestURI, "utf-8");
 
 
         return dao.getBlogByName(blogName);
     }
 
     private BlogPost getBlogPost(HttpServletRequest req) throws UnsupportedEncodingException {
         String requestURI = req.getRequestURI().substring("/blog/".length());
 
         String blogName = URLDecoder.decode(requestURI.substring(0, requestURI.indexOf('/')), "utf-8");
         String postName = URLDecoder.decode(requestURI.substring(requestURI.indexOf('/')+1), "utf-8");
 
         return dao.getBlogPost(dao.getBlogByName(blogName), postName);
 
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         if(isComment(req)) {
             postBlogComment(req, resp);
         } else {
            content = req.getParameter("content");
             postBlogEntry(req, resp, getBlog(req));
         }
 
     }
 
     private boolean isComment(HttpServletRequest req) {
         return req.getParameter("commentAuthor") != null;
     }
 
     private void postBlogComment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         BlogPost post = getBlogPost(req);
         String author = req.getParameter("commentAuthor");
        String content = req.getParameter("content");

 
         BlogPostComment comment = new BlogPostComment(post);
 
         comment.setAuthor(author);
         comment.setContent(content);
 
         dao.saveOrUpdate(comment);
 
         resp.sendRedirect(post.getLinkId());
 
 
 
     }
 
     private void postBlogEntry(HttpServletRequest req, HttpServletResponse resp, Blog blog) throws IOException {
         String title = req.getParameter("title");
 
         BlogPost post = new BlogPost(blog);
 
         post.setTitle(title);
 
         post.setContent(content);
 
         dao.saveOrUpdate(post);
 
         resp.sendRedirect(blog.getLinkId());
     }
 }
