 package no.uis.service.fsimport;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.AbstractCollection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import no.uis.service.fsimport.StudInfoImport.StudinfoType;
 import no.uis.service.studinfo.data.FsSemester;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized2;
 import org.junit.runners.Parameterized2.Parameters;
 import org.junit.runners.Parameterized2.TestName;
 
 @RunWith (Parameterized2.class)
 public class ValidatorTest {
 
   private int year;
   private FsSemester semester;
   private StudinfoType infoType;
   private String lang;
 
   public ValidatorTest(int year, FsSemester semester, StudinfoType infoType, String lang) {
     this.year = year;
     this.semester = semester;
     this.infoType = infoType;
     this.lang = lang;
   }
   
   @Parameters
   public static List<Object[]> configParams() {
     final String sYear = System.getProperty("studinfo.year");
     final String sInfoType = System.getProperty("studinfo.type");
     final String sLang = System.getProperty("studinfo.lang");
    final String sSemester = System.getProperty("studinfo.semester");
 
    if (sYear == null || sSemester == null) {
       return Collections.emptyList();
     }
    FsSemester semester = FsSemester.stringToUisSemester(sSemester);
     
     int year = Integer.parseInt(sYear);
     
     StudinfoType[] infoTypes = (sInfoType != null ? new StudinfoType[] {StudinfoType.valueOf(sInfoType)} : StudinfoType.values());
     String[] langs = (sLang != null ? sLang.split(",") : new String[] {"B", "E", "N"});
     
     List<Object[]> params = new ArrayList<Object[]>();
     for (StudinfoType type : infoTypes) {
       for (String lang : langs) {
         params.add(new Object[] {year, semester, type, lang});
       }
     }
     return params;
   }
 
   @TestName
   public static String getTestName(String method, Object[] params) {
     
     StringBuilder sb = new StringBuilder();
     sb.append(method);
     sb.append('(');
     for (int i = 0; i < params.length; i++) {
       if (i > 0) {
         sb.append(", ");
       }
       sb.append(params[i]);
     }
     sb.append(')');
     return sb.toString();
   }
   
   /**
    * The test is disabled by default. It can be enabled by setting the system properties {@code studinfo.year} and {@code studinfo.semester}. The year
    * is set with the system property {@code studinfo-validate.year}. The semester is set with the system property {@code studinfo-validate.semester}.
    */
   @Test
   public void validate() throws Exception {
     StudinfoValidator validator = new StudinfoValidator();
     List<String> messages = validator.fetchAndValidate(year, semester, lang, infoType);
     assertThat(new ListWrapper(messages), is(emptyCollection()));
   }
   
   private static class IsEmptyCollection extends BaseMatcher<Collection<?>> {
 
     @Override
     public boolean matches(Object item) {
       return (item != null && ((Collection<?>)item).isEmpty());
     }
 
     @Override
     public void describeTo(Description description) {
       description.appendText("empty");
     }
   }
 
   private static Matcher<Collection<?>> emptyCollection() {
     return new IsEmptyCollection();
   }
 
   /**
   * This class overrides the {@link AbstractCollection#toString()} method so that the matcher puts every message on a line.
   */
   @SuppressWarnings("serial")
   private static class ListWrapper extends ArrayList<String> {
     
     public ListWrapper(List<String> wrappee) {
       super(wrappee);
     }
     
     @Override
     public String toString() {
       if (isEmpty()) {
         return "[]";
       }
 
       StringBuilder sb = new StringBuilder();
       sb.append("[\n");
       for (String elem : this) {
         sb.append(elem);
         sb.append("\n,\n");
       }
       sb.append(']');
       return sb.toString();
     }
     
   }
 }
