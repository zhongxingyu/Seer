 /**
  * 
  */
 package com.vroozi.categorytree.repository;
 
 import java.util.List;
 
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.repository.GraphRepository;
 import org.springframework.data.neo4j.repository.RelationshipOperationsRepository;
 import org.springframework.data.repository.query.Param;
 
 import com.vroozi.categorytree.model.Category;
 
 /**
  * @author Mamoon Habib
  *
  */
 public interface CategoryRepository extends GraphRepository<Category>, RelationshipOperationsRepository<Category>{
 	Category findByCategoryId(String categoryId);
 	Category findByCompanyCategoryCode(String companyCategoryCode);
 	
 	@Query( "START cvgroup=node:ContentViewGroup(token={token}) " +
            " MATCH (cvgroup)-->(cview)-->(category) " +
            " where cvgroup.active = true and cview.active = true " +
             " return category")
 //	+" order by rating desc, cnt desc" +
 //            " limit 10" )
 	List<Category> getCategories(@Param("token") String cvGroupToken );
 }
