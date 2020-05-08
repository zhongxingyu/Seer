 package it.ivncr.erp.service;
 
 import it.ivncr.erp.model.personale.Addetto;
 import it.ivncr.erp.model.personale.Reparto;
 import it.ivncr.erp.service.addetto.AddettoService;
 import it.ivncr.erp.service.lut.LUTService;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class AddettoServiceTest extends BaseServiceTest {
 
 	@Test
 	public void testListAddettiAndServizi() {
 
 		LUTService ls = ServiceFactory.createService("LUT");
 		Reparto reparto = ls.retrieveItemByDescrizione("Reparto", "Sede Centrale");
 
 		GregorianCalendar gc = new GregorianCalendar(2012, 10, 2); // 2012-11-02
 		Date dataMattinale = gc.getTime();
 
 		AddettoService as = ServiceFactory.createService("Addetto");
 		long start = System.currentTimeMillis();
		List<Object[]> list = as.listAddettiAndServizi(reparto.getId(), dataMattinale);
 		long end = System.currentTimeMillis();
 
 		Assert.assertNotNull(list);
 
		for(Object[] o : list) {
			Addetto addetto = (Addetto)o[0];
 			System.out.println(String.format("%s %s: %d", addetto.getCognome(), addetto.getNome(), addetto.getServizi().size()));
 		}
 
 		System.out.println("Elapsed time: " + (end -start));
 	}
 }
