 package org.jboss.pressgang.ccms.wrapper.base;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.proxy.RESTEntityProxyFactory;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.wrapper.RESTWrapperFactory;
 
 public abstract class RESTBaseWrapper<T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U, ?, ?>> implements EntityWrapper<T> {
 
     private final RESTWrapperFactory wrapperFactory;
     private final RESTProviderFactory providerFactory;
     private final boolean isRevision;
     private final U proxyEntity;
     private final U entity;
     private final boolean isNewEntity;
 
     protected RESTBaseWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision, boolean isNewEntity) {
         this(providerFactory, entity, isRevision, null, isNewEntity);
     }
 
     protected RESTBaseWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision, final RESTBaseEntityV1<?, ?,
             ?> parent, boolean isNewEntity) {
         this.providerFactory = providerFactory;
         wrapperFactory = providerFactory.getWrapperFactory();
         this.isRevision = isRevision;
         this.entity = entity;
         this.isNewEntity = isNewEntity;
         if (isNewEntity) {
             proxyEntity = entity;
         } else {
             proxyEntity = RESTEntityProxyFactory.createProxy(providerFactory, entity, isRevision, parent);
         }
     }
 
     protected RESTProviderFactory getProviderFactory() {
         return providerFactory;
     }
 
     protected RESTWrapperFactory getWrapperFactory() {
         return wrapperFactory;
     }
 
     public U unwrap() {
         return getEntity();
     }
 
     protected U getProxyEntity() {
         return proxyEntity;
     }
 
     protected U getEntity() {
         return entity;
     }
 
     @Override
     public Integer getId() {
         return getProxyEntity().getId();
     }
 
     @Override
     public void setId(Integer id) {
         getEntity().setId(id);
     }
 
     @Override
     public Integer getRevision() {
         return getProxyEntity().getRevision();
     }
 
     @Override
     public boolean isRevisionEntity() {
         return isRevision;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o instanceof RESTBaseWrapper && getProxyEntity() != null) {
             return o == null ? false : getEntity().equals(((RESTBaseWrapper) o).getEntity());
         } else {
             return super.equals(o);
         }
     }
 
    @Override
    public int hashCode() {
        if (getProxyEntity() != null) {
            return getEntity().hashCode();
        } else {
            return super.hashCode();
        }
    }

     protected boolean isNewEntity() {
         return isNewEntity;
     }
 }
