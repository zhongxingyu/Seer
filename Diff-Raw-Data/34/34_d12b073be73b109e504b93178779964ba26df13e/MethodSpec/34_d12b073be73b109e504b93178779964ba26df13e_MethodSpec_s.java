 import static org.junit.Assert.*;
 
 import java.util.Map;
 
 import org.junit.Test;
 
 public class MethodSpec {
 
   @Test
   public void soporta_metodos_sin_parametros() throws Exception {
     Metodos.class.getMethod("metodo_sin_parametros");
   }
 
   @Test
   public void soporta_metodos_con_parametros() throws Exception {
     Metodos.class.getMethod("metodo_con_1_parametro", Map.class);
     Metodos.class.getMethod("metodo_con_2_parametros", Map.class);
   }
 
   @Test
   public void soporta_self_y_devolucion_explicita() throws Throwable {
     Metodos receptor = new Metodos();
     assertSame(receptor, receptor.metodo_que_devuelve_self_explicitamente());
   }
 
   @Test
   public void soporta_self_y_devolucion_implicita() throws Throwable {
     Metodos receptor = new Metodos();
     assertSame(receptor, receptor.metodo_que_devuelve_self_implicitamente());
   }
 
   @Test
  public void soporta_variables_locales() throws Exception {
    fail();
   }
 
   @Test
  public void soporta_ifs() throws Throwable {
     Metodos receptor = new Metodos();
    assertEquals("ok", receptor.metodo_con_if_verdadero());
    assertEquals("error", receptor.metodo_con_if_falso());
   }
  
 }
