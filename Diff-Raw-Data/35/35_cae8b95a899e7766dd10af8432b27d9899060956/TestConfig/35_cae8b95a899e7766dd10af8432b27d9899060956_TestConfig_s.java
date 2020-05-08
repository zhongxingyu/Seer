 package clear4j;
 
 
 /**
  * User: alexb
  * Date: 24/05/12
  * Time: 16:18
  */
 public enum TestConfig {
    TEST_FILE_PATH("/tmp/test.txt"),
     TEST_FILE_CONTENT("This is\na sample\nfile content.");
 
     private String value;
     private TestConfig(String value) {
         this.value = value;	
     }
 
     public String getValue() {
         return value;
     }
 }
