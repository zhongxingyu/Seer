 package br.com.projeto.dao;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import br.com.projeto.dao.UserDao;
 import br.com.projeto.entity.User;
import org.springframework.test.context.transaction.AfterTransaction;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import static junit.framework.Assert.*;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
         "classpath*:applicationContext.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
 public class UserDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
 
     @Autowired
     private UserDao dao;
 
    private Long idToDelete;

     @Autowired
     private DriverManagerDataSource ds;
 
     private void criaJoao(String n) throws SQLException {
         System.out.println("Cria o joao" + n + " para usar nos testes");
         Connection con = ds.getConnection();
         Statement s = con.createStatement();
         s.executeUpdate(" insert into tbUsuario (data_insercao,login,senha,data_atualizacao) values ('2012-02-02','joao" + n + "','456','2012-02-02') ");
         s.close();
         con.close();
     }
 
     private void limpaLixo(Long id) throws SQLException {
         System.out.println("Apaga o joao usado nos testes - " + id);
         Connection con = ds.getConnection();
         Statement s = con.createStatement();
         s.executeUpdate(" delete from tbUsuario where id = " + id);
         s.close();
         con.close();
     }
 
 
     @Test
    @Rollback(true)
     public void testCreate() throws SQLException {
         System.out.println("testCreate Iniciando!");
         User u = new User();
         u.setLogin("juca");
         u.setPassword("123");
         dao.insert(u);
        User u2 = dao.getUser("juca", "123");
        assertNotNull(u2);
         System.out.println("testCreate Concluido!");
     }
 
     @Test
     public void testRetrieve() throws SQLException {
         criaJoao("retrieve");
         User u = dao.getUser("joaoretrieve", "456");
         assertNotNull(u);
         assertEquals(u.getLogin(), "joaoretrieve");
         System.out.println("testRetrieve Concluido!");
         limpaLixo(u.getId());
     }
 
 
     @Test
     public void testUpdate() throws SQLException {
         criaJoao("U");
         User u = dao.getUser("joaoU", "456");
         u.setLogin("joao-upd");
         dao.update(u);
         User u2 = dao.getUser("joao-upd", "456");
         assertNotNull(u2);
         assertEquals(u2.getLogin(), "joao-upd");
        dao.remove(User.class,u.getId());
         System.out.println("testUpdate Concluido!");
     }
 




     @Test
     public void testDelete() throws SQLException {
         criaJoao("D");
         User u = dao.getUser("joaoD", "456");
         dao.remove(User.class, u.getId());
         User u2 = dao.getUser("joaoD", "456");
         assertNull(u2);
         System.out.println("testDelete Concluido!");
     }
 }
