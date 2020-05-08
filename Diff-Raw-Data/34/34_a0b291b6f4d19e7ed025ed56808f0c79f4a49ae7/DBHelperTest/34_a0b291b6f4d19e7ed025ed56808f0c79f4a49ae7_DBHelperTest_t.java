 package LinkFuture.Core.DBHelper;
 
 import LinkFuture.Core.DBHelper.Model.FieldInfo;
 import LinkFuture.Core.DBHelper.Model.SPInfo;
 import LinkFuture.Core.Debugger;
 import TestModel.CityInfo;
 import TestModel.CountryInfo;
 import org.junit.Test;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class DBHelperTest {
     @Test
     public void testInsert() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
         DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/hibuImage","root","MSNbbh2gd");
         helper.addParameter("$ProviderID",1);
         helper.addParameter("$MerchantListID",1);
         helper.addParameter("$ProviderProductID",1);
         helper.executeSP("PSP_FindNextJob");
     }
     @Test
     public void testfindClassFieldInfo() throws Exception {
         HashMap<String,FieldInfo> results= DBHelper.findClassFieldInfo(CityInfo.class);
         assertEquals("testfindClassFieldInfo", results.size(),3);
     }
 
     @Test
     public void testExecuteSPWithOneResult() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             helper.addParameter("$ID",10);
             helper.addParameter("$Code","ABW");
             helper.addOutParameter("$test");
             ArrayList<CityInfo> cityList = helper.executeSP(SPName, CityInfo.class);
             assertEquals("One class execute test",cityList.size(),9);
             assertTrue("countryCodeTTTTTT",cityList.get(0).countryCodeTTTTTT!=null);
             assertTrue("Extend.district",cityList.get(0).Extend.district!=null);
             assertEquals("One class execute test,output",helper.getOutputParameterList().get("$test"),(long)9999);
         }
     }
     @Test
     public void testExecuteSPToXml() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             helper.addParameter("$ID",10);
             helper.addParameter("$Code","ABW");
             helper.addOutParameter("$test");
             String result = helper.executeSPToXml(SPName);
             Debugger.traceln(result);
             assertTrue("testExecuteSPToXml",result.length()>0);
         }
     }
     @Test
     public void testExecuteSP() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             helper.addParameter("$ID",10);
             helper.addParameter("$Code","ABW");
             helper.addOutParameter("$test");
             ArrayList<ArrayList<?>> result = helper.executeSP(SPName, CityInfo.class,CountryInfo.class);
             ArrayList<CityInfo> cityList = (ArrayList<CityInfo>)result.get(0);
             ArrayList<CountryInfo> countryList = (ArrayList<CountryInfo>)result.get(1);
 //            Debugger.traceln(Utility.convertToXml(cityList));
 //            Debugger.traceln(Utility.convertToXml(countryList));
 //            Debugger.traceln(Utility.convertToXml(helper.getOutputParameterList()));
             assertEquals("One class execute test",cityList.size(),9);
             assertEquals("One class execute test",countryList.size(),1);
             assertEquals("One class execute test,output",helper.getOutputParameterList().get("$test"),(long)9999);
         }
     }
     @Test
     public void testFindSpInfo() throws Exception {
        try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world?useUnicode=true&characterEncoding=UTF-8","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             SPInfo meta = helper.findSPInfo(SPName);
             assertEquals("jdbc:mysql://localhost:3306/world",meta.dbName);
             assertEquals(SPName,meta.spName);
             assertEquals("$id",meta.parameterList.get(0).parameterName);
         }
     }
 
     @Test
     public void testSPUpdate() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Update";
//            http://info.michael-simons.eu/2013/01/21/java-mysql-and-multi-byte-utf-8-support/
//            ALTER TABLE city charset=utf8mb4, MODIFY COLUMN name CHAR(35) CHARACTER SET utf8mb4 , don't need alter database!!
//            config my.ini file
//            C:\ProgramData\MySQL\MySQL Server 5.6\my.ini
//            character-set-server = utf8mb4
//            collation-server = utf8mb4_unicode_ci
//            character-set-client-handshake = f
//            if calling SP, set character SET utf8mb4 at parameter
//            CREATE DEFINER=`root`@`localhost` PROCEDURE `PSP_City_Update`(
//                    $city varchar(2000) character SET utf8mb4
//            )
//            BEGIN
//            update city SET name = $city where ID=1;
//            END

            helper.addParameter("$City","Kabul2  č,ć,đ 11 \ud83d\udca9 1 2 3👽💔");
             helper.executeSP(SPName);
            //helper.executeSQL("update city SET name = 'Kabul2  č,ć,đ 11 \ud83d\udca9 1' where ID=1;");

         }
     }
 
 }
