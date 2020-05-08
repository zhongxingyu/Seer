 package org.hogel;
 
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.is;
 
 public class JsonDataTest {
     @Test
     public void readJson() throws IOException {
        JsonData data = JsonData.load("src/test/resources/web/data/hoge.json").get();
         assertThat((String) data.getData().get("name"), is("hogelog"));
     }
 
     @Test
     public void importData() throws IOException {
        JsonData data = JsonData.load("src/test/resources/web/data/hoge.json").get();
         assertThat(Number.class.cast(data.getData().get("id")).intValue(), is(10));
     }
 
 }
