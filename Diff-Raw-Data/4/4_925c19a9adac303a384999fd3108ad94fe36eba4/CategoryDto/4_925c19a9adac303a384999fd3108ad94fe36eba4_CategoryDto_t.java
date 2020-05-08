 package net.cyklotron.cms.modules.rest.category;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.lang3.Validate;
 
 import net.cyklotron.cms.category.CategoryResource;
 
 public class CategoryDto
 {
     String id;
 
     String name;
 
     String path;
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String id)
     {
         this.id = id;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getPath()
     {
         return path;
     }
 
     public void setPath(String path)
     {
         this.path = path;
     }
 
     public static Collection<CategoryDto> create(Collection<CategoryResource> categories)
     {
         Validate.notNull(categories);
         Collection<CategoryDto> categoriesDtos = new ArrayList<>(categories.size());
         for(CategoryResource category : categories)
         {
             categoriesDtos.add(create(category));
         }
         return categoriesDtos;
     }
 
     private static CategoryDto create(CategoryResource category)
     {
         CategoryDto categoryDto = new CategoryDto();
         categoryDto.name = category.getName();
         categoryDto.id = category.getIdString();
         categoryDto.path = category.getPath();
         return categoryDto;
     }
 
 }
