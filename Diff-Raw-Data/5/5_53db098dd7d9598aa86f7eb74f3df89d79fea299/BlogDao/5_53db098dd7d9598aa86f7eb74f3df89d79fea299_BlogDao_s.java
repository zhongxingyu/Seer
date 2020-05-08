 package no.kantega.blog.dao;
 
 import no.kantega.blog.model.Blog;
 import no.kantega.blog.model.BlogPost;
 import no.kantega.blog.model.BlogPostComment;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.List;
 
 /**
  * Database abstraction layer.
  */
 public class BlogDao {
 
     private final JdbcTemplate template;
 
     /**
      * Create a new Data Access Object.
      * 
      * @param dataSource the connection to the database to talk with 
      */
     public BlogDao(DataSource dataSource) {
         this.template = new JdbcTemplate(dataSource);
     }
 
     /**
      * Return all blogs.
      * 
      * @return list of all blogs ordered by name 
      */
     public List<Blog> getAllBlogs() {
         return getBlogsWhere("");
     }
 
     private List<Blog> getBlogsWhere(String wherePart, Object... params) {
         return template.query("select * from blog " + wherePart +" order by blogname", new RowMapper<Blog>() {
             @Override
             public Blog mapRow(ResultSet resultSet, int i) throws SQLException {
                 return getBlogFromResultSet(resultSet);
             }
         }, params);
     }
 
     private Blog getBlogFromResultSet(ResultSet resultSet) throws SQLException {
         Blog blog = new Blog();
         blog.setId(resultSet.getLong("blogid"));
         blog.setName(resultSet.getString("blogname"));
         blog.setColor(resultSet.getString("color"));
         return blog;
     }
 
     /**
      * Saves a blog post to the database.
      * 
      * @param blog the blog post to save 
      */
     public void saveOrUpdate(Blog blog) {
         if (blog.isNew()) {
             Connection con;
             try {
                 con = template.getDataSource().getConnection();
                 PreparedStatement statement = con.prepareStatement("INSERT INTO blog (blogname, color) VALUES (?, ?)");
                 statement.setString(1, blog.getName());
                 statement.setString(2, blog.getColor());
                 statement.executeUpdate();
                 con.close();
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         } else {
             template.update("update blog set blogname=?, color=? where blogid=?", blog.getName(), blog.getColor(), blog.getId());
         }
 
     }
 
     /**
      * Return a blog given an unique name.
      * 
      * @param blogName The name of the blog
      * @return Blog with the given name
      * @throws IllegalArgumentException If no blog with the given name can be found
      */
     public Blog getBlogByName(String blogName) {
         List<Blog> blogs = getBlogsWhere(" where blogname=?", blogName);
         if(blogs.isEmpty()) {
             throw new IllegalArgumentException("Blog not found " + blogName);
         }
         return blogs.iterator().next();
     }
 
     /**
      * Delete a blog given its name.
      * 
      * @param blogName The name of the blog to delete
      */
     public void deleteBlogByName(String blogName) {
         Blog blog = getBlogByName(blogName);
         template.update("DELETE FROM blog WHERE blogname=?", blog.getName());
     }
 
     /**
      * Saves a blog post.
      * 
      * @param post the blog post to save to the database 
      */
     public void saveOrUpdate(BlogPost post) {
         if (post.isNew()) {
             template.update("INSERT INTO blogpost (blogid, posttitle, postcontent, publishdate) VALUES (?, ?, ?, ?)",
                     post.getBlog().getId(),
                     post.getTitle(),
                     post.getContent(),
                     new Timestamp(System.currentTimeMillis()));
         } else {
             template.update("update blog set posttitle=?, postcontent=? where blogpostid=?",
                     post.getTitle(),
                     post.getBlogPostId(),
                     post.getBlogPostId());
         }
     }
 
     /**
      * Return all blog posts for a given blog.
      * 
      * @param blog The blog to read all blog post for
      * @return List of blog posts in this blog
      */
     public List<BlogPost> getBlogPosts(Blog blog) {
         return getBlogPosts("where blogpost.blogid=?", blog.getId());
     }
 
     private List<BlogPost> getBlogPosts(String whereClaus, Object... parameters) {
         boolean fast = false;
         List<BlogPost> result;
         if (fast) {
             result = getBlogPostsEfficient(whereClaus, parameters);
         } else {
             result = getBlogPostsInefficient(whereClaus, parameters);
         }
         return result;
     }
 
     /**
      * Efficient way to return blog posts.
      */
     private List<BlogPost> getBlogPostsEfficient(String whereClaus, Object[] parameters) {
         String sql = "select blog.blogid, blog.blogname, blog.color, blogpost.blogpostid, blogpost.posttitle, blogpost.postcontent, blogpost.publishdate, " +
                " (select count(*) from blogpostcomment where blogpostcomment.blogpostcommentid=blogpost.blogpostid) as commentcount " +
                 " from blogpost left join blog on blog.blogid=blogpost.blogid " + whereClaus + " order by  blogpost.blogpostid desc";
 
         return template.query(sql, new RowMapper<BlogPost>() {
             @Override
             public BlogPost mapRow(ResultSet rs, int i) throws SQLException {
                 Blog blog = getBlogFromResultSet(rs);
                 BlogPost post = new BlogPost(blog);
                 post.setBlogPostId(rs.getInt("blogpostid"));
                 post.setTitle(rs.getString("posttitle"));
                 post.setContent(rs.getString("postcontent"));
                 post.setPublishDate(rs.getTimestamp("publishdate"));
                 post.setCommentCount(rs.getInt("commentcount"));
                 return post;
             }
         }, parameters);
     }
 
     /**
      * Inefficient way to return blog posts.
      */
     private List<BlogPost> getBlogPostsInefficient(String whereClaus, Object[] parameters) {
         String sql = "select blogpost.blogid, blogpost.blogpostid, blogpost.posttitle, blogpost.postcontent, blogpost.publishdate, " +
                " (select count(*) from blogpostcomment where blogpostcomment.blogpostcommentid=blogpost.blogpostid) as commentcount " +
                 " from blogpost " + whereClaus + " order by  blogpost.blogpostid desc";
 
         return template.query(sql, new RowMapper<BlogPost>() {
             @Override
             public BlogPost mapRow(ResultSet rs, int i) throws SQLException {
                 Blog blog = getBlogsWhere("where blogid=?", rs.getInt("blogid")).iterator().next();
                 BlogPost post = new BlogPost(blog);
                 post.setBlogPostId(rs.getInt("blogpostid"));
                 post.setTitle(rs.getString("posttitle"));
                 post.setContent(rs.getString("postcontent"));
                 post.setPublishDate(rs.getTimestamp("publishdate"));
                 post.setCommentCount(rs.getInt("commentcount"));
                 return post;
             }
         }, parameters);
     }
     
     /**
      * Return a blog post given an id of the blog post.
      * 
      * @param blogPostId The id of the blog post
      * @return The blog post with this ID
      */
     public BlogPost getBlogPost(long blogPostId) {
         return getBlogPosts("where blogpost.blogpostid=?", blogPostId).iterator().next();
     }
 
     /**
      * Return a blog post given the blog and the name of the post.
      * 
      * @param blog The blog this post belongs to
      * @param postName The name of the blog post
      * @return The blog post if found
      */
     public BlogPost getBlogPost(Blog blog, String postName) {
         return getBlogPosts("where blogpost.blogid=? and blogpost.posttitle=?", blog.getId(), postName).iterator().next();
     }
 
     /**
      * Saves a blog comment.
      * 
      * @param comment blog comment to save to the database
      */
     public void saveOrUpdate(BlogPostComment comment) {
         if(comment.isNew()) {
             template.update("INSERT INTO blogpostcomment (blogpostid, commentauthor, commentcontent, commentpublishdate) VALUES (?, ?, ?, ?)",
                     comment.getBlogPost().getBlogPostId(),
                     comment.getAuthor(),
                     comment.getContent(),
                     new Timestamp(System.currentTimeMillis()));
         } else {
             template.update("update blogpostcomment set commentauthor=?, commentcontent=?,commentpublishdate=? where blogpostcommentid=?",
                     comment.getAuthor(),
                     comment.getContent(),
                     comment.getPublishDate().toDate(),
                     comment.getBlogPostCommentId());
         }
     }
 
     /**
      * Return a list of comments for a given blog post.
      * 
      * @param post The blog post to read comments from
      * @return List of comments for this blog post.
      */
     public List<BlogPostComment> getComments(final BlogPost post) {
         String sql = "select blogpostcommentid, commentauthor, commentcontent, commentpublishdate from blogpostcomment where blogpostid=?";
         return template.query(sql, new RowMapper<BlogPostComment>() {
             @Override
             public BlogPostComment mapRow(ResultSet rs, int i) throws SQLException {
                 BlogPostComment comment = new BlogPostComment(post);
                 comment.setAuthor(rs.getString("commentauthor"));
                 comment.setContent(rs.getString("commentcontent"));
                 comment.setPublishDate(rs.getTimestamp("commentpublishdate"));
                 return comment;
             }
         }, post.getBlogPostId());
     }
 }
