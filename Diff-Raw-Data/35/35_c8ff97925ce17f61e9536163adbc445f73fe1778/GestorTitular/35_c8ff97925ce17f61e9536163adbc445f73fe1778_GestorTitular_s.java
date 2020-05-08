 package ar.edu.utn.frsf.licenciator.logica;
 
 import java.util.Calendar;
 
 import ar.edu.utn.frsf.licenciator.dao.DaoTitular;
 import ar.edu.utn.frsf.licenciator.entidades.ClaseLicencia;
 import ar.edu.utn.frsf.licenciator.entidades.TipoDoc;
 import ar.edu.utn.frsf.licenciator.entidades.TipoSanguineo;
 import ar.edu.utn.frsf.licenciator.entidades.Titular;
 import ar.edu.utn.frsf.licenciator.entidades.Usuario;
 
 public class GestorTitular {
 	
 	private GestorTitular() {
 		super();
 	}
 	
 	public static Titular createTitular( Usuario creador, TipoDoc tipoDocumento, long numeroDocumento, String nombreTitular, String apellidoTitular, Calendar fechaNacimiento, String direccion, String localidad, ClaseLicencia claseLicencia, TipoSanguineo grupoSanguineo, boolean donante ) throws TitularExistenteExeption {		
 		if( DaoTitular.read( tipoDocumento, numeroDocumento ) != null ) {
 			throw( new TitularExistenteExeption() );
 		}
 		
 		Titular titular = new Titular( tipoDocumento, numeroDocumento, nombreTitular, apellidoTitular, fechaNacimiento, direccion, localidad, claseLicencia, grupoSanguineo, donante );
 		
 		DaoTitular.create(titular );
 		GestorAuditoria.reportarAltaTitular(creador, titular);
 		return titular;
 	}
}
