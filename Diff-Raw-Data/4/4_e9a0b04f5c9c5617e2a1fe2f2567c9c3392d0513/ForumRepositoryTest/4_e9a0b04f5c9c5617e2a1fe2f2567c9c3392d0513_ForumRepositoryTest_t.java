 package np.springmvc101.repository;
 
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.ExpectedDatabase;
 import np.springmvc101.domain.Forum;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * User: Nightpig
  * Date: 2013/7/9
  * Time: 下午 8:49
  */
 public class ForumRepositoryTest extends BaseRepositoryTestCase {
 
     @Autowired
     private ForumRepository repository;
 
     @DatabaseSetup("ForumRepositoryTest.xml")
     @ExpectedDatabase("ForumRepositoryTest-delete.xml")
     @Test
     public void delete() {
         repository.delete(1L);
     }
 
     @DatabaseSetup("ForumRepositoryTest.xml")
     @Test
     public void findAll() {
         List<Forum> forums = repository.findAll(new PageRequest(0, 5)).getContent();
         assertEquals(2, forums.size());
     }
 
     @DatabaseSetup("ForumRepositoryTest.xml")
     @Test
     public void findOne() {
         Forum forum = repository.findOne(1L);
         assertEquals(Long.valueOf(1L), forum.getId());
         assertEquals("init", forum.getDescription());
         assertEquals("init", forum.getTitle());
     }
 
     @DatabaseSetup("ForumRepositoryTest.xml")
     @ExpectedDatabase("ForumRepositoryTest-save1.xml")
     @Test
     public void save1() {
         Forum forum = new Forum();
         forum.setDescription("test");
         forum.setTitle("test");
         repository.save(forum);
     }
 
     @DatabaseSetup("ForumRepositoryTest.xml")
     @ExpectedDatabase("ForumRepositoryTest-save2.xml")
     @Test
     public void save2() {
         Forum forum = new Forum();
         forum.setId(2L);
        forum.setDescription("中文測試");
        forum.setTitle("中文測試");
         repository.save(forum);
     }
 
 }
