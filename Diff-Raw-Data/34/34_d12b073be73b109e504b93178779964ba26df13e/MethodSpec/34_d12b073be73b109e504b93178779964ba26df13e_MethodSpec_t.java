 import static org.junit.Assert.*;
 
import java.math.BigDecimal;
 import java.util.Map;
 
import javax.sound.midi.Receiver;

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
  public void soporta_variables_locales() throws Throwable {
    assertEquals(BigDecimal.valueOf(5L), new Metodos().metodo_con_variables_locales());
   }
 
   @Test
  public void soporta_when() throws Throwable {
     Metodos receptor = new Metodos();
    assertEquals("ok", receptor.metodo_con_when_verdadero());
    assertEquals("error", receptor.metodo_con_when_falso());
  }

  @Test
  public void soporta_when_con_multiples_condiciones() throws Throwable {
    assertEquals("ok", new Metodos().metodo_con_when_multiple());
  }

  @Test
  public void soporta_when_sin_default() throws Exception {
    fail();
  }

  @Test
  public void soporta_when_que_compila_a_sentencia() throws Throwable {
    assertEquals(BigDecimal.valueOf(5), new Metodos().metodo_con_sentencia_when());
   }
 }
