 package com.abjon.example.services;
 
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 
 import com.abjon.example.data.ArticleBean;
 import com.abjon.example.model.Article;
 
 @Path("/articles")
 public class ArticlesResource {
 
 	  @Context
 	    private UriInfo context;
 	 
 	    ArticleBean ab;
 	    
 	    /**
 	     * Creates a new instance 
 	     */
 	    public ArticlesResource() {
 	         try {
 	            ab = (ArticleBean) new InitialContext().lookup("java:module/ArticleBean");
 	        } catch (NamingException ex) {
 	            Logger.getLogger(ArticlesResource.class.getName()).log(Level.SEVERE, null, ex);
 	        }
 	        
 	    }
 	  
 
 	    /**
 	     * Retrieves representation of an instance of com.abjon.research.ArticlesResource
 	     * @return an instance of java.lang.String
 	     */ 
 	    @GET
 	    @Produces({MediaType.APPLICATION_JSON})
 	    public List<Article> list() {
 	        System.err.println("CALLED LIST");
 	        return (List<Article>)ab.findAll();
 	    }
 	    
 	    @GET
 	    @Path("/{articleid}")
 	    @Produces({MediaType.APPLICATION_JSON})
 	    @Consumes({MediaType.APPLICATION_JSON})
 	    public Article read(@PathParam("articleid") String sarticleid)
 	    {
 	        Long uid = new Long(Long.parseLong(sarticleid));
 	        
 	        
 	        return ab.find(uid);
 	        
 	    } 
 	    
 	    
 	    @POST
 	    @Produces({MediaType.APPLICATION_JSON})
 	    @Consumes({MediaType.APPLICATION_JSON})
 	    public Article create(Article a) 
 	    {
	        System.err.append("CALLED CREATE!");
 	        if(a==null) 
 	        {
 	            a = new Article();
 	            a.setArticle("Ny");
 	            
 	        }
 	        ab.create(a);
 	        
 	        return a;
 	        
 	    }
 
 	    @PUT
 	    @Path("/{articleid}")
 	    @Produces({MediaType.APPLICATION_JSON})
 	    @Consumes({MediaType.APPLICATION_JSON})
 	    public Article update(@PathParam("articleid") String sarticleid,Article article) 
 	    {
	        System.err.append("CALLED UPDATE!");
 	        Long articleid = Long.parseLong(sarticleid);
 	        Article a = null;
 	        a = ab.find(articleid);
 
 	        a.setArticle(article.getArticle());
 	        a.setLatitude(article.getLatitude());
 	        a.setLongitude(article.getLongitude());
 
 	        ab.edit(a);
 
 	        return a;
 	        
 	    }
 	    
 	    @DELETE 
 	    @Path("/{articleid}")
 	    @Produces({MediaType.APPLICATION_JSON})
 	    @Consumes({MediaType.APPLICATION_JSON})
 	    public String destroy(@PathParam("articleid") String sarticleid) 
 	    {
 	        Long articleid = Long.parseLong(sarticleid);
 	        Article a = null;
 	        a = ab.find(articleid);
 	        ab.remove(a);
 	        
 	        return "Success";
 	    }
 
 }
