 package test;
 import org.junit.*;
 
 import exception.InvalidLinkException;
 
 import tempo.IntervaloDeTempo;
 import tempo.UnidadeDeTempo;
 
 import blog.LinhaDoTempo;
 public class TestaLinhaDoTempo {
 
 	private LinhaDoTempo ldt;
 	@Before
 	public void setUP(){
 		ldt = new LinhaDoTempo();
 	}
 
 	@Test 
 	public void testaTempoMedioEntrePostagens(){
 		IntervaloDeTempo intervalo;
 		Assert.assertNull(ldt.getTempoMedioEntrePostagens());
 		
 		ldt.posta("http://google.com",21,9,2012,23,45);
 		ldt.posta("http://google.com",21,9,2012,23,50);
 		ldt.posta("http://google.com",21,9,2012,23,55);
		
 		intervalo = ldt.getTempoMedioEntrePostagens();
 		Assert.assertEquals(intervalo.getValor(), 5f, 0.001);
 		Assert.assertEquals(UnidadeDeTempo.Minutos,intervalo.getUnidade());
 		
 		ldt.posta("http://google.com",22,9,2012,23,45);
 		intervalo = ldt.getTempoMedioEntrePostagens();
 		Assert.assertEquals(intervalo.getValor(), 8f, 0.001);
 		Assert.assertEquals(UnidadeDeTempo.Horas,intervalo.getUnidade());
 		
 		ldt.posta("http://google.com",29,9,2012,23,45);
 		intervalo = ldt.getTempoMedioEntrePostagens();
 		Assert.assertEquals(intervalo.getValor(), 2f, 0.001);
 		Assert.assertEquals(UnidadeDeTempo.Dias,intervalo.getUnidade());
 	}
 	
 }
