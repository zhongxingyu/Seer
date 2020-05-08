 package se.cygni.web.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import se.cygni.web.model.Illustrator;
 import se.cygni.web.model.Title;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 @Controller
 @RequestMapping("api/title")
 public class TitleController {
 
     private JdbcTemplate jdbcTemplate;
 
     private static String SQL_ALL_TITLES =
         "SELECT DISTINCT Titlar.TitelID, Titlar.TitelSV, Titlar.TitelENG, Titlar.Chrono FROM Titlar, Böcker WHERE Böcker.TitelID = Titlar.TitelID order by TitelSV asc";
 
     private static String SQL_ALL_TITLES_CHRONO =
             "SELECT DISTINCT Titlar.TitelID, Titlar.TitelSV, Titlar.TitelENG, Titlar.Chrono FROM Titlar, Böcker WHERE Böcker.TitelID = Titlar.TitelID AND Titlar.Chrono > 0" +
                    " order by Titlar.Chrono asc";
 
 
     @Autowired
     public TitleController(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
 
         // Warmup (database isn't populated until the first request)
         listTitles();
     }
 
     @RequestMapping("list")
     @ResponseBody
     public List<Title> listTitles() {
 
         return this.jdbcTemplate.query(SQL_ALL_TITLES, new TitleMapper());
     }
 
     @RequestMapping("list/chrono")
     @ResponseBody
     public List<Title> listTitlesChrono() {
 
         return this.jdbcTemplate.query(SQL_ALL_TITLES_CHRONO, new TitleMapper());
     }
 
     @RequestMapping("/id/{id}")
     @ResponseBody
     public Title byId(@PathVariable int id) {
 
         return this.jdbcTemplate.queryForObject("SELECT * FROM Titlar WHERE TitelID = ?", new Object[] {id}, new TitleMapper());
     }
 
     public final class TitleMapper implements RowMapper<Title> {
 
         @Override
         public Title mapRow(ResultSet rs, int i) throws SQLException {
             return new Title(
                     rs.getInt("TitelID"),
                     rs.getString("TitelSV"),
                     rs.getString("TitelENG"),
                     rs.getInt("Chrono")
                     );
         }
     }
 }
