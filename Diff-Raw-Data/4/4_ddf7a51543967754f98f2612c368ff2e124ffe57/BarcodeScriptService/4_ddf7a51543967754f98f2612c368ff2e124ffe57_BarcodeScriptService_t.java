 package com.celements.photo;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 import org.krysalis.barcode4j.BarcodeException;
 import org.krysalis.barcode4j.BarcodeGenerator;
 import org.krysalis.barcode4j.BarcodeUtil;
 import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
 import org.xml.sax.SAXException;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.script.service.ScriptService;
 
 @Component("barcode")
 public class BarcodeScriptService implements ScriptService {
   public void generate(String number, OutputStream out) {
     BitmapCanvasProvider provider = new BitmapCanvasProvider(
         out, "image/png", 300, BufferedImage.TYPE_BYTE_GRAY, true, 0);
     DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
     String xmlConf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><barcode><ean8>" +
      "<module-width>0.4mm</module-width></ean8></barcode>";
     InputStream in = new ByteArrayInputStream(xmlConf.getBytes());
     try {
       Configuration cfg = builder.build(in);
      in.close();
       BarcodeGenerator gen = BarcodeUtil.getInstance().createBarcodeGenerator(cfg);
       gen.generateBarcode(provider, number);
       provider.finish();
     } catch (ConfigurationException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (SAXException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (BarcodeException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 }
