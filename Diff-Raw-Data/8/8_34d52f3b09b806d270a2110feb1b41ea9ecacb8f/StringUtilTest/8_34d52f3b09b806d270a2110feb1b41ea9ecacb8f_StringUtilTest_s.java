 package org.jailsframework.util;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: May 9, 2010
  *          Time: 1:56:14 AM
  */
 public class StringUtilTest {
 
     @Test
     public void shouldCamelizeGivenString() {
         Assert.assertEquals("Employee", new StringUtil("employee").camelize());
         Assert.assertEquals("EmpLoyee", new StringUtil("empLoyee").camelize());
        Assert.assertEquals("Eemployee", new StringUtil("eemployee").camelize());
         Assert.assertEquals("Employee", new StringUtil("Employee").camelize());
         Assert.assertEquals("EmployeeRecordFile", new StringUtil("employee_record_file").camelize());
     }
 
     @Test
     public void shouldTabelizeGivenString() {
         Assert.assertEquals("employees", new StringUtil("Employee").tabelize());
         Assert.assertEquals("employee_records", new StringUtil("EmployeeRecord").tabelize());
         Assert.assertEquals("employee_record_files", new StringUtil("EmployeeRecordFile").tabelize());
     }
 }
