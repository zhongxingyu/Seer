 package com.br.caelum.bridge;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 public class CompactarArquivo extends PosProcessamento {
 
     @Override
     public byte[] processar(byte[] bytes) throws IOException {
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         ZipOutputStream out = new ZipOutputStream(byteOut);
         out.putNextEntry(new ZipEntry("internal"));
         out.write(bytes);
         out.closeEntry();
         out.close();
        return byteOut.toByteArray();
     }
 
}
