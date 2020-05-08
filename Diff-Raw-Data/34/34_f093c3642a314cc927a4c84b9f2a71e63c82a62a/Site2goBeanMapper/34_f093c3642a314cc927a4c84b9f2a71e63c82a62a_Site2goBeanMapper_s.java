 package com.site2go.dto.mapper;
 
 import com.site2go.dao.entities.LayoutEntity;
 import com.site2go.dao.entities.PageEntity;
 import com.site2go.dao.entities.SiteEntity;
 import com.site2go.dao.entities.UserEntity;
 import com.site2go.dto.Layout;
 import com.site2go.dto.Page;
 import com.site2go.dto.Site;
 import com.site2go.dto.User;
 import org.dozer.DozerBeanMapper;
 import org.dozer.loader.api.BeanMappingBuilder;
 import org.dozer.loader.api.FieldDefinition;
 
 import static org.dozer.loader.api.FieldsMappingOptions.copyByReference;
 
 public class Site2goBeanMapper extends DozerBeanMapper {
     public Site2goBeanMapper() {
         super();
         this.addMapping(new BeanMappingBuilder() {
             @Override
             protected void configure() {
                 mapping(SiteEntity.class, Site.class)
                    .fields("defaultLayout.name", "defaultLayout")
                    .fields("modifiedDate", "modifiedDate", copyByReference())
                    .fields("createdDate", "createdDate", copyByReference());
 
                 mapping(LayoutEntity.class, Layout.class)
                     .fields("site.domain", "site")
                     .fields("modifiedDate", "modifiedDate", copyByReference())
                     .fields("createdDate", "createdDate", copyByReference());
 
                 mapping(PageEntity.class, Page.class)
                     .fields("layout.name", "layout")
                     .fields("metaTitle", new FieldDefinition("meta").mapKey("title"))
                     .fields("metaDescription", new FieldDefinition("meta").mapKey("description"))
                     .fields("metaKeywords", new FieldDefinition("meta").mapKey("keywords"))
                     .fields("modifiedDate", "modifiedDate", copyByReference())
                     .fields("createdDate", "createdDate", copyByReference());
 
                 mapping(UserEntity.class, User.class)
                     .fields("modifiedDate", "modifiedDate", copyByReference())
                     .fields("createdDate", "createdDate", copyByReference());
             }
         });
     }
 }
