 package ru.finam.bustard.codegen;
 
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
 
import java.io.File;
import java.io.FileReader;
 import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
 
 public class FileLinesParser {
 
     public static List<String> retrieveResource(ClassLoader loader, String filePath) throws IOException {
        final List<URL> urls = Lists.newArrayList(Iterators.forEnumeration(
                loader.getResources(filePath)));
        try {
             List<String> result = new ArrayList<String>();
 
             for (URL url : urls) {
                 StringTokenizer tokenizer = new StringTokenizer(readEntireFile(url), "\n\r");
                 while (tokenizer.hasMoreTokens()) {
                     String token = tokenizer.nextToken().trim();
                     if (token.isEmpty()) {
                         continue;
                     }
 
                     result.add(token);
                 }
             }
             return result;
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
        }
     }
 
    private static String readEntireFile(URL fileUrl) throws URISyntaxException, IOException {
         File file = new File(fileUrl.toURI());
         FileReader reader = new FileReader(file);
         StringBuilder contents = new StringBuilder();
         char[] buffer = new char[4096];
         int read = 0;
         do {
             contents.append(buffer, 0, read);
             read = reader.read(buffer);
         } while (read >= 0);
         return contents.toString();
    }
 }
