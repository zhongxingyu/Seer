 package algoritmosdeinferencia;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Modela un algoritmo de inducción de tipo deductivo dada una lista de reglas
  * y una lista de hechos de inicio. Extiende a <code>AlgoritmoDeInferencia</code>
  * quien proporciona un comportamiento básico.
  * @author Charlie Corner
  * @see AlgoritmoDeInferencia
  */
 public class Deduccion extends AlgoritmoDeInferencia {
 
     /**
      * La lista de reglas que han sido disparadas y que se usarán para la explicación
      * de la ejecución del algoritmo.
      * @see Regla
      */
     private List<Regla> reglasDisparadas;
 
     /**
      * Constructor por defecto hecho explícito y privado que no inicializa ningún
      * campo. Se debe utilizar el otro constructor para instanciar objetos de
      * esta clase.
      * @see Deduccion#Deduccion(java.util.List, java.util.List) 
      */
     private Deduccion() {
     }
 
     /**
      * Constructor que inicializa todos los campos de este objeto necesarios para
     * la correcta ejecución del algoritmo. Además inicializa el campo de resultado para
     * mostrar un mensaje de error por si no se ha ejecutado el algoritmo.
      * @param conjuntoDeReglas la lista de objetos <code>Regla</code> que modelan
      *                          las reglas de ejecución de este algoritmo
      * @param hechosDeInicio    la lista con los hechos de inicio de este algoritmo
      * @see Regla
      */
     public Deduccion(List<Regla> conjuntoDeReglas, List<String> hechosDeInicio) {
         this.conjuntoDeReglas = conjuntoDeReglas;
         this.hechosDeInicio = hechosDeInicio;
         this.reglasDisparadas = new ArrayList<Regla>();
         this.hechosInferidos = new ArrayList<String>();
         this.hechosPreguntados = new ArrayList<String>();
        this.resultado = "No se ha corrido el algoritmo\n";
     }
 
     @Override
     public String correrAlgoritmo() {
 
         for (Regla r : conjuntoDeReglas) {
             boolean seCompletaLaRegla = true;
 
             // La bandera de cumpliemento de regla cambiara si y sólo si el elemento
             // definitivamente no está en ninguna de las tres listas
             for (String s : r.getCausantes()) {
 
                 if (false == isElementoEnListas(s)) {
 
                     if (true == preguntarAlUsuarioSiEsta(s)) {
 
                         if (true != agregarAHechosPreguntados(s)) {
                             System.err.println("Hubo un problema al agregar "
                                     + r.getIndiceDeRegla() 
                                     + " a la lista de hechos preguntados");
                         }
                     } else {
                         seCompletaLaRegla = false;
                     }
                 }
             }
 
             // Si se cumple la regla y el elemento producido no está ya en alguna de las listas
             if (seCompletaLaRegla) {
                 reglasDisparadas.add(r);
 
                 if (!isElementoEnListas(r.getProducidos())) {
 
                     if (true != agregarAHechosInferidos(r.getProducidos())) {
                         System.err.println("Hubo un problema al agregar "
                                 + r.getIndiceDeRegla()
                                 + " a la lista de hechos preguntados");
                     }
                 }
             }
         }
         this.resultado = crearTextoRespuesta();
         return resultado;
     }
 
     @Override
     protected String crearTextoRespuesta() {
         StringBuilder sb = new StringBuilder("***Resultados del algoritmo de deducción***\n\n");
         sb = sb.append("Explicación:\n");
 
         for (Regla r : reglasDisparadas) {
             sb = sb.append("Se disparó la regla ")
                     .append(r.getIndiceDeRegla())
                     .append(" y se agregó: ")
                     .append(r.getProducidos());
             sb = sb.append('\n');
         }
         sb = sb.append('\n');
         sb = sb.append("Hechos de inicio: \n");
 
         for (String s : hechosDeInicio) {
             sb = sb.append(s).append('\n');
         }
 
         sb = sb.append('\n');
         sb = sb.append("Hechos preguntados: \n");
 
         for (String s : hechosPreguntados) {
             sb = sb.append(s).append('\n');
         }
 
         sb = sb.append('\n');
         sb = sb.append("Hechos inferidos: \n");
 
         for (String s : hechosInferidos) {
             sb = sb.append(s).append('\n');
         }
 
         return sb.toString();
     }
 }
