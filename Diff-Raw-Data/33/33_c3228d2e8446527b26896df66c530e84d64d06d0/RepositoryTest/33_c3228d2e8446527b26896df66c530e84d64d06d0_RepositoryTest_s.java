 package de.flower.rmt.repository;
 
import com.mysema.query.types.Predicate;
 import com.mysema.query.types.expr.BooleanExpression;
import de.flower.rmt.model.QTeam;
 import de.flower.rmt.model.QUser;
 import de.flower.rmt.model.Team;
 import de.flower.rmt.model.User;
 import de.flower.rmt.test.AbstractIntegrationTests;
 import org.hibernate.LazyInitializationException;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 import static org.testng.Assert.*;
 
 /**
  * @author flowerrrr
  */
 public class RepositoryTest extends AbstractIntegrationTests {
 
     @Test(expectedExceptions = {LazyInitializationException.class})
     public void testManyToOneLazyFetching() {
         User user = userRepo.findOne(1L);
         // this should trigger a LIE.
         user.getClub().getName();
     }
 
     @Test
     public void testEagerFetching() {
         BooleanExpression nr1 = QUser.user.id.eq(1L);
         User user = userRepo.findAll(nr1, QUser.user.club).get(0);
         log.info(user.getClub().getName());
     }
 
     @Test
     public void testFindAllFiltersClubs() {
         List<Team> list = teamRepo.findAll();
         log.info(list.toString());
         assertEquals(list.size(), 3);
     }
 
    @Test
    public void testQueryDsl() {

        BooleanExpression hasClub = QTeam.team.club.eq(securityService.getUser().getClub());
        List<Team> list = teamRepo.findAll(hasClub);
        assertEquals(list.size(), 3);

        list = teamRepo.findAll((Predicate) null);
        assertEquals(list.size(), 4);
    }
 }
