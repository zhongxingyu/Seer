 package com.danharper.cwk.view;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.ejb.SessionContext;
 import javax.enterprise.context.Conversation;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.inject.Inject;
 
 import com.danharper.cwk.service.AbstractService;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public abstract class AbstractBean<T> implements Serializable
 {
 
     protected Class<T> entityClass;
     protected T entity;
     protected T example;
 
     public AbstractBean(Class<T> entityClass)
     {
         this.entityClass = entityClass;
 
         try
         {
             this.example = entityClass.newInstance();
             this.add = entityClass.newInstance();
         }
         catch (InstantiationException ex)
         {
             Logger.getLogger(AbstractBean.class.getName()).log(Level.SEVERE, null, ex);
         }
         catch (IllegalAccessException ex)
         {
             Logger.getLogger(AbstractBean.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     protected static final long serialVersionUID = 1L;
     @Inject
     protected Conversation conversation;
     @Resource
     protected SessionContext sessionContext;
     private Long id;
 
     protected abstract AbstractService<T> getService();
 
     public Long getId()
     {
         return this.id;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     public T getExample()
     {
         return this.example;
     }
 
     public void setExample(T example)
     {
         this.example = example;
     }
 
     public String create()
     {
 
         this.conversation.begin();
         return "create?faces-redirect=true";
     }
 
     public void retrieve()
     {
 
         if (FacesContext.getCurrentInstance().isPostback())
         {
             return;
         }
 
         if (this.conversation.isTransient())
         {
             this.conversation.begin();
         }
 
         if (this.id == null)
         {
             this.entity = this.example;
         }
         else
         {
             this.entity = findById(getId());
         }
     }
 
     public T findById(Long id)
     {
         return getService().find(id);
     }
 
     public String update()
     {
         this.conversation.end();
 
         try
         {
             if (this.id == null)
             {
                 getService().create(this.entity);
                 return "search?faces-redirect=true";
             }
             else
             {
                 getService().update(this.entity);
                 return "view?faces-redirect=true&id=" + getId();
             }
         }
         catch (Exception e)
         {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
             return null;
         }
     }
 
     public String delete()
     {
         this.conversation.end();
 
         try
         {
             getService().remove(findById(getId()));
             return "search?faces-redirect=true";
         }
         catch (Exception e)
         {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
             return null;
         }
     }
 
     /*
      * Support searching Idea entities with pagination
      */
     protected int page;
     protected long count;
     protected List<T> pageItems;
 
     public int getPage()
     {
         return this.page;
     }
 
     public void setPage(int page)
     {
         this.page = page;
     }
 
     public int getPageSize()
     {
         return 4;
     }
 
     public void search()
     {
         this.page = 0;
     }
 
     public void paginate()
     {
        this.count = getService().count();
        this.pageItems = getService().findRange(this.page, getPageSize());
     }
 
     public List<T> getPageItems()
     {
         return this.pageItems;
     }
 
     public long getCount()
     {
         return this.count;
     }
 
     /*
      * For correctly listing idea entities (e.g. HtmlSelectOneMenu)
      */
     public List<T> getAll()
     {
         return getService().findAll();
     }
 
     public abstract Converter getConverter();
 
     /*
      * Bi-directional, one-to-many tables
      */
     protected T add;
 
     public T getAdd()
     {
         return this.add;
     }
 
     public T getAdded()
     {
         T added = this.add;
         
         try
         {
             this.add = entityClass.newInstance();
         }
         catch (InstantiationException ex)
         {
             Logger.getLogger(AbstractBean.class.getName()).log(Level.SEVERE, null, ex);
         }
         catch (IllegalAccessException ex)
         {
             Logger.getLogger(AbstractBean.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         return added;
     }
 
 }
