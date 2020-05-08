 package be.noselus.scraping;
 
 import be.noselus.AbstractDbDependantTest;
 import be.noselus.model.Question;
 import be.noselus.repository.AssemblyRegistry;
 import be.noselus.repository.AssemblyRegistryInDatabase;
 import be.noselus.repository.PoliticianRepository;
 import be.noselus.repository.PoliticianRepositoryInMemory;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 public class QuestionParserTest extends AbstractDbDependantTest {
 
     private QuestionParser parser;
 
     @Before
     public void setup() {
         PoliticianRepository politicianRepository = new PoliticianRepositoryInMemory();
         AssemblyRegistry assemblyRegistry = new AssemblyRegistryInDatabase(AbstractDbDependantTest.dataSource);
         parser = new QuestionParser(politicianRepository, assemblyRegistry);
     }
 
     @Test
     public void openData() throws IOException {
         final InputStream in = getClass().getClassLoader().getResourceAsStream("scraping/PW_36256.html");
         Question qr = parser.parse(in, "classpath:scraping/PW_36256.html", 36256);
 
         Assert.assertEquals("36256", qr.assemblyRef);
         Assert.assertEquals("l'\"Open Data - Open Government\"", qr.title);
         Assert.assertEquals("2010-2011", qr.session);
         Assert.assertEquals(2011, qr.year.intValue());
         Assert.assertEquals("594 (2010-2011) 1", qr.number);
         Assert.assertEquals("2011-08-29", qr.dateAsked.toString());
         Assert.assertEquals(21, qr.askedBy);
         Assert.assertEquals(78, qr.askedTo);
         Assert.assertEquals("2011-10-07", qr.dateAnswered.toString());
         Assert.assertEquals(78, qr.answeredBy);
        Assert.assertEquals(2596, qr.questionText.length());
         Assert.assertEquals(5663, qr.answerText.length());
     }
 
     @Test
     public void eolien() throws IOException {
         final InputStream in = getClass().getClassLoader().getResourceAsStream("scraping/PW_50370.html");
         Question qr = parser.parse(in, "classpath:scraping/PW_50370.html", 50370);
 
         Assert.assertEquals("50370", qr.assemblyRef);
         Assert.assertEquals("le coût élevé de l'éolien", qr.title);
         Assert.assertEquals("2013-2014", qr.session);
         Assert.assertEquals(2013, qr.year.intValue());
         Assert.assertEquals("51 (2013-2014) 1", qr.number);
         Assert.assertEquals("2013-10-04", qr.dateAsked.toString());
         Assert.assertEquals(63, qr.askedBy);
         Assert.assertEquals(75, qr.askedTo);
         Assert.assertEquals("2013-10-25", qr.dateAnswered.toString());
         Assert.assertEquals(75, qr.answeredBy);
        Assert.assertEquals(2044, qr.questionText.length());
         Assert.assertEquals(296, qr.answerText.length());
     }
 
     @Test
     public void andreAntoine() throws IOException {
         Question qr = parser.parse(50054);
 
         Assert.assertEquals("50054", qr.assemblyRef);
         Assert.assertEquals("la baisse de l'emploi dans les P.M.E.", qr.title);
         Assert.assertEquals("2012-2013", qr.session);
         Assert.assertEquals(2013, qr.year.intValue());
         Assert.assertEquals("467 (2012-2013) 1", qr.number);
         Assert.assertEquals("2013-09-13", qr.dateAsked.toString());
         Assert.assertEquals(31, qr.askedBy);
         Assert.assertEquals(76, qr.askedTo);
         Assert.assertEquals(null, qr.dateAnswered);
         Assert.assertEquals(0, qr.answeredBy);
        Assert.assertEquals(2244, qr.questionText.length());
         Assert.assertEquals(null, qr.answerText);
     }
 
     @Test
     public void texts() throws IOException {
         String url = "http://parlement.wallonie.be/content/print_container.php?print=quest_rep_voir.php&id_doc=36256&type=all";
         Document doc = Jsoup.parse(new URL(url).openStream(), "iso-8859-1", url);
 
         List<String> texts = parser.extract(doc, "div#print_container div + div");
 
         Assert.assertEquals(2596, texts.get(0).length());
         Assert.assertEquals(5663, texts.get(2).length());
     }
 
     @Test
     public void parseDate() {
         DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                 .appendDayOfMonth(2)
                 .appendLiteral('/')
                 .appendMonthOfYear(2)
                 .appendLiteral('/')
                 .appendYear(4, 4)
                 .toFormatter();
 
         LocalDate tmp = LocalDate.parse("29/08/2011", dateFormatter);
 
         System.out.println(tmp);
     }
 
     @Test
     public void reponseProvisoire() {
         Assert.assertEquals("02/10/2013", "Réponse du 02/10/2013 ".replaceFirst("Réponse(.)* du ", "").trim());
         Assert.assertEquals("02/10/2013", "Réponse provisoire du 02/10/2013 ".replaceFirst("Réponse(.)* du ", "").trim());
     }
 }
