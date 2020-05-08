 package no.kantega.android.utils;
 
 import no.kantega.android.models.Transaction;
 import no.kantega.android.models.TransactionTag;
 import no.kantega.android.models.TransactionType;
 import org.junit.Test;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class GsonUtilTest {
 
     @Test
     public void parseTransactions() {
         final String json = "[{\"accountingDate\":\"2009-04-15 00:00:00\"," +
                 "\"fixedDate\":\"2009-04-15 00:00:00\",\"amountIn\":0.0," +
                 "\"amountOut\":1272.56," +
                 "\"text\":\"456997107150**** 09.04 SEK 1550,00 CLAS OHLSON AB (49)\"," +
                 "\"archiveRef\":\"50001685147\"," +
                 "\"type\":{\"name\":\"Visa\",\"id\":1}," +
                 "\"tags\":[{\"name\":\"Datautstyr\",\"id\":4}],\"id\":7}]";
         final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         final Transaction t = new Transaction();
         try {
             t.accountingDate = sdf.parse("2009-04-15 00:00:00");
             t.fixedDate = sdf.parse("2009-04-15 00:00:00");
             t.amountIn = 0.0;
             t.amountOut = 1272.56;
             t.text = "456997107150**** 09.04 SEK 1550,00 CLAS OHLSON AB (49)";
             t.archiveRef = "50001685147";
             final TransactionType type = new TransactionType();
             type.name = "Visa";
             t.type = type;
             final TransactionTag tag = new TransactionTag();
             tag.name = "Datautstyr";
             t.tags = new ArrayList<TransactionTag>() {{
                 add(tag);
             }};
         } catch (ParseException e) {
             assertTrue(false);
         }
         List<Transaction> expected = new ArrayList<Transaction>() {{
             add(t);
         }};
         List<Transaction> actual = GsonUtil.parseTransactions(json);
        assertEquals(expected, actual);
     }
 }
