 package gov.nih.nci.evs.browser.utils;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import org.LexGrid.LexBIG.DataModel.Collections.NCIChangeEventList;
 import org.LexGrid.LexBIG.DataModel.NCIHistory.NCIChangeEvent;
 import org.LexGrid.LexBIG.DataModel.NCIHistory.types.ChangeType;
 import org.LexGrid.LexBIG.Exceptions.LBException;
 import org.LexGrid.LexBIG.History.HistoryService;
 import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
 import org.LexGrid.LexBIG.Utility.Constructors;
 import org.LexGrid.concepts.Concept;
 
 public class HistoryUtils {
     private static final String CODING_SCHEME = "NCI Thesaurus";
     private static DateFormat _dataFormatter = new SimpleDateFormat("yyyy-MM-dd");
     
     public static Vector<String> getTableHeader() {
         Vector<String> v = new Vector<String>();
         v.add("Edit Actions");
         v.add("Date");
         v.add("Reference Concept");
         return v;
     }
     
     public static Vector<String> getEditActions(String codingSchemeName, 
             String vers, String ltag, String code) throws LBException {
         LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
         HistoryService hs = lbSvc.getHistoryService(CODING_SCHEME);
         
         NCIChangeEventList list = hs.getEditActionList(Constructors
             .createConceptReference(code, null), null, null);
        return getEditActions(codingSchemeName, vers, ltag, list);
     }
     
     private static Vector<String> getEditActions(String codingSchemeName, 
            String vers, String ltag, NCIChangeEventList list) {
         Enumeration<NCIChangeEvent> enumeration = list.enumerateEntry();
         int i = 0;
         Vector<String> v = new Vector<String>();
         while (enumeration.hasMoreElements()) {
             NCIChangeEvent event = enumeration.nextElement();
             ChangeType type = event.getEditaction();
             Date date = event.getEditDate();
            String code = event.getReferencecode();
            String info = "none";
            if (code != null && code.length() > 0 && 
                    ! code.equalsIgnoreCase("null")) {
                 Concept c = DataUtils.getConceptByCode(
                    codingSchemeName, vers, ltag, code);
                 String name = c.getEntityDescription().getContent();
                info = name + " (Code " + code + ")";
             }
             
            String data = type + "|" + _dataFormatter.format(date) + "|" + info;
            System.out.println(++i + ") " + data);
            v.add(data);
         }
         return v;
     }
 }
