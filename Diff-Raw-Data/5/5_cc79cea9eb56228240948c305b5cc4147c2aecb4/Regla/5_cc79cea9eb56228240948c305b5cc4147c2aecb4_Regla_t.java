 package algoritmosdeinferencia;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author Charlie Corner
  */
 public class Regla {
 
     private static int cuentaIndiceReglas = 1;
     private int indiceDeRegla;
     private List<String> causantes;
     private String producidos;
 
     private Regla() {
     }
 
     public Regla(int indiceDeRegla, List<String> causantes, String producidos) {
         this.indiceDeRegla = indiceDeRegla;
         this.causantes = causantes;
         this.producidos = producidos;
     }
 
     public static Regla fromLineaSinParsear(String lineaDeReglas) {
         List<String> listaCausantes = new ArrayList<String>();
         String producto;
         String reglas[] = lineaDeReglas.split("=");
        String causas[] = reglas[0].replaceAll("[()]", "").split("\\^");
 
         listaCausantes.addAll(Arrays.asList(causas));
         producto = reglas[1];
 
         return new Regla(cuentaIndiceReglas++, listaCausantes, producto);
     }
 
     public static List<Regla> listaFromLineasSinParsear(String listaSinParsear[]) {
         List<Regla> listasDeReglas = new ArrayList<Regla>();
         
         for (String linea : listaSinParsear) {
             List<String> listaCausantes = new ArrayList<String>();
             String producto;
             String reglas[] = linea.split("=");
            String causas[] = reglas[0].replaceAll("[()]", "").split("\\^");
 
             listaCausantes.addAll(Arrays.asList(causas));
             producto = reglas[1];
 
             listasDeReglas.add(new Regla(cuentaIndiceReglas++, listaCausantes, producto));
         }
         return listasDeReglas;
     }
 
     public static void setCuentaIndiceReglas(int cuentaIndiceReglas) {
         Regla.cuentaIndiceReglas = cuentaIndiceReglas;
     }
 
     public static int getCuentaIndiceReglas() {
         return cuentaIndiceReglas;
     }
 
     public void setCausantes(List<String> causantes) {
         this.causantes = causantes;
     }
 
     public void setProducidos(String producidos) {
         this.producidos = producidos;
     }
 
     public List<String> getCausantes() {
         return causantes;
     }
 
     public String getProducidos() {
         return producidos;
     }
 
     public int getIndiceDeRegla() {
         return indiceDeRegla;
     }
 
     public void setIndiceDeRegla(int indiceDeRegla) {
         this.indiceDeRegla = indiceDeRegla;
     }
 }
