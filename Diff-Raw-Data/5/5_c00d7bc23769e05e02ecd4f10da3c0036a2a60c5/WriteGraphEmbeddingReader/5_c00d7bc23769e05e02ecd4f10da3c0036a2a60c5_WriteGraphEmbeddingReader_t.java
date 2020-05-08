 package nvcleemp.quadviewer.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import nvcleemp.quadviewer.data.Quadrangulation;
 
 /**
  *
  * @author nvcleemp
  */
 public class WriteGraphEmbeddingReader {
 
     private WriteGraphEmbeddingReader() {
         //do not instantiate
     }
     
     public static double[][] readEmbedding(Quadrangulation q, Reader r) throws IOException{
         BufferedReader br = new BufferedReader(r);
         String lastRead = br.readLine();
         if(">>writegraph2d<<".equals(lastRead)) lastRead = br.readLine(); //skip header
         
         double embedding[][] = new double[q.getOrder()][2];
         for (int i = 0; i < q.getOrder(); i++) {
             if("0".equals(lastRead)) throw new IOException("Reached end of graph to soon.");
             
            String parts[] = lastRead.trim().split("\\s+");
             embedding[i][0] = Double.parseDouble(parts[1]);
             embedding[i][1] = Double.parseDouble(parts[2]);
             lastRead = br.readLine();
         }
         
        if(!"0".equals(lastRead.trim()))
             throw new IOException("We're finished, but apparently the graph isn't.");
         
         return embedding;
     }
 }
