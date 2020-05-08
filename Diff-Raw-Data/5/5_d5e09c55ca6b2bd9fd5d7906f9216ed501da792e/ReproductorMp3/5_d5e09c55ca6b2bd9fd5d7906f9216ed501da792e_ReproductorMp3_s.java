 package GestorSonido;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import javax.sound.sampled.*;
 
 public class ReproductorMp3 extends Thread {
 
     private static SourceDataLine line;
     private static AudioInputStream din = null;
     private static boolean sigue;
     private static String archivo = "Sonidos/BSO.mp3";
 
     public ReproductorMp3(String nombre) {
         super(nombre);
     }
 
     @Override
     public void run() {
         sigue = true;
         URL url = getClass().getClassLoader().getResource(archivo);
         ReproductorMp3.reproduceMusica(url.getPath());
 
         // imprimir nombre del subproceso
         //System.err.println( getName() + " termino su inactividad" );
 
     }
 
     public static void pararMusica() {
         sigue = false;
         line.stop();
         line.close();
         try {
             din.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void cambiaMusica(String url) {
         ReproductorMp3.pararMusica();
         ReproductorMp3.archivo = url;
     }
 
     public static void reproduceMusica(String nombreFichero) {
        
             try {
                 File file = new File(nombreFichero);
                 AudioInputStream in = AudioSystem.getAudioInputStream(file);
                 AudioFormat baseFormat = in.getFormat();
                 AudioFormat decodedFormat = new AudioFormat(
                         AudioFormat.Encoding.PCM_SIGNED,
                         baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                         baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                         false);
                 din = AudioSystem.getAudioInputStream(decodedFormat, in);
                 DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                 line = (SourceDataLine) AudioSystem.getLine(info);
                 if (line != null) {
                     line.open(decodedFormat);
                     byte[] data = new byte[4096];
                     // Start
                     line.start();
                     int nBytesRead;
                     while ((nBytesRead = din.read(data, 0, data.length)) != -1 && sigue) {
                         line.write(data, 0, nBytesRead);
                     }
                     // Stop
                     line.drain();
                     line.stop();
                     line.close();
                     din.close();
 
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 if (din != null) {
                     try {
                         din.close();
                     } catch (IOException e) {
                     }
                 }
            
         }
     }
 }
