 package de.flower.rmt.repository.factory;
 
 import de.flower.rmt.repository.IUserRepo;
 import de.flower.rmt.repository.impl.BaseRepository;
 import de.flower.rmt.repository.impl.UserRepo;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.support.JpaEntityInformation;
 import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
 import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
 import org.springframework.data.repository.core.RepositoryMetadata;
 import org.springframework.util.Assert;
 
 import javax.persistence.EntityManager;
 import java.io.Serializable;
 
 /**
  * @author flowerrrr
  */
 public class RepositoryFactoryBean<T extends JpaRepository<S, ID>, S, ID extends Serializable> extends JpaRepositoryFactoryBean<T, S, ID> {
 
     @Override
     protected RepositoryFactory createRepositoryFactory(
             EntityManager entityManager) {
         return new RepositoryFactory(entityManager);
     }
 
     private static class RepositoryFactory extends JpaRepositoryFactory {
 
         public RepositoryFactory(EntityManager entityManager) {
             super(entityManager);
         }
 
         @Override
        protected <T, ID extends Serializable> Object getTargetRepository(
                RepositoryMetadata metadata, EntityManager entityManager) {

             JpaEntityInformation<?, Serializable> entityInformation =
                     getEntityInformation(metadata.getDomainClass());
             Assert.notNull(entityInformation);
             if (metadata.getRepositoryInterface().equals(IUserRepo.class)) {
                 return new UserRepo(entityInformation, entityManager);
             } else {
                 return new BaseRepository(entityInformation, entityManager);
             }
         }
 
         @Override
         public Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
             if (metadata.getRepositoryInterface().equals(IUserRepo.class)) {
                 return UserRepo.class;
             } else {
                 return BaseRepository.class;
             }
         }
     }
 }
