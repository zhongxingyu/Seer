 package ro.danix.first.model.repository.user.mongo;
 
 import static org.hamcrest.Matchers.*;
 import static ro.danix.first.CoreMatchers.*;
 import static org.junit.Assert.*;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import ro.danix.first.model.config.FactoriesConfig;
 import ro.danix.first.model.config.MongoConfig;
 import ro.danix.first.model.domain.Baby;
 import ro.danix.first.model.domain.factory.BabyFactory;
 import ro.danix.first.model.domain.user.BabyParent;
 import ro.danix.first.model.domain.user.factory.BabyParentFactory;
 import ro.danix.first.model.repository.BabyRepository;
 import ro.danix.first.model.repository.user.BabyParentRepository;
 import ro.danix.test.SlowRunningTests;
 
 /**
  *
  * @author danix
  */
 @Category(SlowRunningTests.class)
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = {MongoConfig.class, FactoriesConfig.class})
 @ActiveProfiles(profiles = "factories")
 public class BabyRepositoryImplIntegrationTest {
 
     @Autowired
     private Mongo mongo;
     
     @Autowired
     private BabyRepository babyRepository;
 
     @Autowired
     private BabyParentFactory babyParentFactory;
     
     @Autowired
     private BabyFactory babyFactory;
     
     @Autowired
     private BabyParentRepository babyParentRepository;
 
     @Before
     public void setUp() {
         DB database = mongo.getDB(MongoConfig.DATABASE_NAME);
         DBCollection babyParents = database.getCollection("babyParent");
         babyParents.remove(new BasicDBObject());
         DBCollection babies = database.getCollection("baby");
         babies.remove(new BasicDBObject());
     }
 
     @Test
     public void saveTest() {
         BabyParent babyParent = createBabyParent();
         
         Baby baby = babyFactory.build();
         baby.setBabyParent(babyParent);
         
         // when
         babyRepository.save(baby);
         
         // then
         List<Baby> babies = babyRepository.findByBabyParent(babyParent);
         assertThat(babies, is(notNullValue()));
         assertThat(babies.size(), is(1));
         
         Baby savedBaby = babies.get(0);
         assertThat(savedBaby, is(notNullValue()));
         assertThat(savedBaby, is(named(BabyFactory.NAME)));
     }
 
     @Test
     public void findByBabyParentTest() {
         // given
         BabyParent babyParent = createBabyParent();
         Baby baby = babyFactory.build();
         baby.setBabyParent(babyParent);
         babyRepository.save(baby);
         // when
         List<Baby> babies = babyRepository.findByBabyParent(babyParent);
         assertThat(babies, is(notNullValue()));
         assertThat(babies.size(), is(1));
     }
 
     private BabyParent createBabyParent() {
        // given
         BabyParent babyParent = babyParentFactory.build();
         babyParent = babyParentRepository.save(babyParent);
         return babyParent;
     }
 }
