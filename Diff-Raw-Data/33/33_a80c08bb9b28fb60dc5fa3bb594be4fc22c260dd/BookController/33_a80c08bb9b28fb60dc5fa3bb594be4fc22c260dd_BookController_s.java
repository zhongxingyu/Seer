 package se.cygni.web.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import se.cygni.web.model.Book;
 import se.cygni.web.util.WebUtil;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 
 @Controller
 @RequestMapping("api/book")
 public class BookController {
 
     private JdbcTemplate jdbcTemplate;
 
     private static String SQL_ALL_BOOKS =
             "SELECT " +
                 "Böcker.BokID, " +
                 "Böcker.SV1upplaga, " +
                 "Böcker.DennaUpplaga, " +
                 "Böcker.AntalUpplagor, " +
                 "Böcker.RyggNr, " +
                 "Böcker.Konvelut, " +
                 "Böcker.Handling, " +
                 "Författare.Efternamn as FörfattareEfternamn, " +
                 "Författare.Förnamn as FörfattareFörnamn, " +
                 "Titlar.TitelSV, " +
                 "Titlar.TitelENG, " +
                 "Titlar.ENG1upplaga, " +
                 "Titlar.Chrono, " +
                 "Illustratörer.Efternamn as IllustratörEfternamn, " +
                 "Illustratörer.Förnamn as IllustratörFörnamn, " +
                 "Översättare.Efternamn as ÖversättareEfternamn, " +
                 "Översättare.Förnamn as ÖversättareFörnamn, " +
                 "EngFörlag.ENGFörlag, " +
                 "EngFörlag.ENGkort, " +
                 "SVFörlag.SVFörlag, " +
                 "SVFörlag.SVkort, " +
                 "RevÖversättare.Efternamn as RevÖversättareEfternamn, " +
                 "RevÖversättare.Förnamn as RevÖversättareFörnamn " +
             "FROM " +
                 "Författare Författare, " +
                 "Titlar Titlar, " +
                 "Illustratörer Illustratörer, " +
                 "Böcker Böcker, " +
                 "`Översättare` Översättare, " +
                 "EngFörlag EngFörlag, " +
                 "SVFörlag SVFörlag, " +
                 "`Översättare` RevÖversättare " +
             "WHERE " +
                 "Böcker.TitelID = Titlar.TitelID AND " +
                 "Böcker.FörfattareID = Författare.FörfattareID AND " +
                 "Böcker.SVFörlag = SVFörlag.SVFörlagID AND " +
                 "Böcker.`ÖversättareID` = Översättare.ÖversättareID AND " +
                 "Böcker.RevÖversättareID = RevÖversättare.ÖversättareID AND " +
                 "Böcker.IllustratörID = Illustratörer.IllustratörID AND " +
                 "Titlar.ENGFörlag = EngFörlag.ENGFörlagID ";
 
     @Autowired
     public BookController(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
 
         // Warmup database
         listByPublisher(4);
     }
 
     @RequestMapping("id/{id}")
     @ResponseBody
     public Book byId(@PathVariable int id) {
        return this.jdbcTemplate.queryForObject(createQuery("Böcker.BokID", false), new Object[] {id}, new BookMapper());
     }
 
     @RequestMapping("list")
     @ResponseBody
     public List<Book> listBooks() {
         return this.jdbcTemplate.query(SQL_ALL_BOOKS, new BookMapper());
     }
 
     @RequestMapping("search/title/{term}")
     @ResponseBody
     public List<Book> searchBooks(@PathVariable String term) {
        return this.jdbcTemplate.query(createQuery("Titlar.TitelSV", true), new Object[] {term}, new BookMapper());
     }
 
     @RequestMapping("list/title")
     @ResponseBody
     public List<String> listTitles() {
         return this.jdbcTemplate.queryForList("SELECT DISTINCT Titlar.TitelSV FROM Titlar, Böcker WHERE Böcker.TitelID = Titlar.TitelID order by TitelSV asc", String.class);
     }
 
     @RequestMapping("list/title/id/{id}")
     @ResponseBody
     public List<Book> listByTitle(@PathVariable int id) {
        return this.jdbcTemplate.query(createQuery("Titlar.TitelID", false), new Object[] {id}, new BookMapper());
     }
 
     @RequestMapping("list/publisher/id/{id}")
     @ResponseBody
     public List<Book> listByPublisher(@PathVariable int id) {
        return this.jdbcTemplate.query(createQuery("SVFörlag.SVFörlagID", false), new Object[] {id}, new BookMapper());
     }
 
     @RequestMapping("list/illustrator/id/{id}")
     @ResponseBody
     public List<Book> listByIllustrator(@PathVariable int id) {
        return this.jdbcTemplate.query(createQuery("Böcker.IllustratörID", false), new Object[] {id}, new BookMapper());
     }
 
     @RequestMapping("list/translator/id/{id}")
     @ResponseBody
     public List<Book> listByTranslator(@PathVariable int id) {
        return this.jdbcTemplate.query(createQuery("Böcker.ÖversättareID", false), new Object[] {id}, new BookMapper());
     }
 
     @RequestMapping("list/random/{noof}")
     @ResponseBody
     public List<Book> listRandomNoof(@PathVariable int noof) {
         List<Book> allBooks = listBooks();
         Collections.shuffle(allBooks);
         return allBooks.subList(0, noof);
     }
 
    private String createQuery(String column, boolean isLike) {
         String sql = SQL_ALL_BOOKS;
         if (isLike) {
             sql += "AND " + column + " LIKE CONCAT('%', ?, '%')";
         } else {
             sql += "AND " + column + " = ?";
         }
 
        return sql + " ORDER BY Titlar.TitelSV asc";
     }
 
     public final class BookMapper implements RowMapper<Book> {
 
         @Override
         public Book mapRow(ResultSet rs, int i) throws SQLException {
             return new Book(
                     rs.getInt("BokID"),
                     rs.getString("TitelSV"),
                     rs.getString("TitelEng"),
                     rs.getString("FörfattareFörnamn") + " " + rs.getString("FörfattareEfternamn"),
                     rs.getString("ÖversättareFörnamn") + " " + rs.getString("ÖversättareEfternamn"),
                     rs.getString("IllustratörFörnamn") + " " + rs.getString("IllustratörEfternamn"),
 
                     rs.getString("SVFörlag"),
                     rs.getString("ENGFörlag"),
                     rs.getString("SV1Upplaga"),
                     rs.getString("ENG1upplaga"),
                     rs.getString("DennaUpplaga"),
                     rs.getString("AntalUpplagor"),
                     rs.getString("RyggNr"),
                     rs.getBoolean("Konvelut"),
                     rs.getString("Handling"),
                     WebUtil.getCoverImageUrl(rs.getString("SVFörlag.SVkort"), rs.getString("TitelSV"), rs.getString("DennaUpplaga")), //coverImagePath
                     rs.getInt("Chrono")
                     );
         }
     }
 }
