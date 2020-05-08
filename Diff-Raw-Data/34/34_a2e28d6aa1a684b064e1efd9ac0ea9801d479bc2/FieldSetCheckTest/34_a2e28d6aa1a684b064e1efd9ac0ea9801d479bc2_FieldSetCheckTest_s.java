 package net.sf.clirr.checks;
 
 import net.sf.clirr.event.ApiDifference;
 import net.sf.clirr.event.Severity;
 import net.sf.clirr.framework.ClassChangeCheck;
 
 /**
  * Tests FieldSetCheck.
  *
  * @author lkuehne
  */
 public class FieldSetCheckTest extends AbstractCheckTestCase
 {
     public void testFieldCheck()
     {
         ApiDifference[] expected = new ApiDifference[] {
             new ApiDifference("Field stat7 has been removed in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "stat7"),
             new ApiDifference("Accessibility of field fin2 has been weakened in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "fin2"),
             new ApiDifference("Accessibility of field stat4 has been weakened in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "stat4"),
             new ApiDifference("Added public field priv2 in testlib.MembersChange", Severity.INFO, "testlib.MembersChange", null, "priv2"),
             new ApiDifference("Accessibility of field stat5 has been weakened in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "stat5"),
             new ApiDifference("Field stat2 is now final in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "stat2"),
             new ApiDifference("Accessibility of field stat6 has been weakened in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "stat6"),
            new ApiDifference("Field stat2 is now static in testlib.MembersChange", Severity.ERROR, "testlib.MembersChange", null, "stat2"),
             new ApiDifference("Added public field stat8 in testlib.MembersChange", Severity.INFO, "testlib.MembersChange", null, "stat8"),
            new ApiDifference("Field fin4 is now non-final in testlib.MembersChange", Severity.INFO, "testlib.MembersChange", null, "fin4")
         };
         verify(expected);
     }
 
 
     protected final ClassChangeCheck createCheck(TestDiffListener tdl)
     {
         return new FieldSetCheck(tdl);
     }
 
 }
