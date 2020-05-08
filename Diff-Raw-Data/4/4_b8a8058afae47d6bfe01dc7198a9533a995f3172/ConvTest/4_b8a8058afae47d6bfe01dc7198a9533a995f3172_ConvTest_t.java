 package jp.dev7.enchant.doga.converter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.List;
 
 import jp.dev7.enchant.doga.converter.data.EnchantMesh;
 import jp.dev7.enchant.doga.parser.SufFileParser;
 import jp.dev7.enchant.doga.parser.data.Suf;
 import jp.dev7.enchant.doga.util.Utils;
 import junit.framework.TestCase;
 import net.arnx.jsonic.JSON;
 
 public class ConvTest extends TestCase {
 
     public void testConv() throws Exception {
         Suf suf = new SufFileParser().parseSufAtr(new File(
                 "src/test/resources/twitter.SUF"));
 
         final SufConverter converter = new SufConverter();
 
         final List<EnchantMesh> meshs = converter.convert(suf);
 
         OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                 "target/twitter.json"), "UTF-8");
         JSON.encode(meshs, out);
         out.flush();
         out.close();
     }
 
     public void testConvUv() throws Exception {
        File file = Utils.dogaPartsFile("mecha/sfnozzle/noz06.suf", null);
         System.out.println(file.getAbsolutePath());
         Suf suf = new SufFileParser().parseSufAtr(file);
 
         SufConverter converter = new SufConverter();
         converter.loadGenieAtr();
         List<EnchantMesh> result = converter.convert(suf);
 
         for (EnchantMesh mesh : result) {
             System.out.println(mesh.texCoords);
         }
     }
 }
