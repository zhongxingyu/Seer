 package ar.edu.utn.frsf.licenciator.logica;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import ar.edu.utn.frsf.licenciator.dao.DaoClaseLicencia;
 import ar.edu.utn.frsf.licenciator.dao.DaoLicencia;
 import ar.edu.utn.frsf.licenciator.dao.DaoTipoDoc;
import ar.edu.utn.frsf.licenciator.dao.DaoTitular;
 import ar.edu.utn.frsf.licenciator.entidades.ClaseLicencia;
 import ar.edu.utn.frsf.licenciator.entidades.Licencia;
 import ar.edu.utn.frsf.licenciator.entidades.TipoDoc;
 import ar.edu.utn.frsf.licenciator.entidades.Titular;
 import ar.edu.utn.frsf.licenciator.entidades.Usuario;
 
 public class GestorLicencias {
 
 	/* Constante */
 	final static Calendar FECHA_ACTUAL = Calendar.getInstance();
 
 	private GestorLicencias() {
 		super();
 	}
 
 	public static Licencia emitirLicencia( Titular titular, ClaseLicencia clase, String obs ) {
 		Calendar emision; 
 		Calendar venc;
 
 		String nro;
 
 		long nroDoc = titular.getNroDoc();
 
 		/* Se genera el nmero de licencia */
 		nro = "3" + Long.toString( nroDoc ) + clase.getTipo();
 
 		/* Fecha de emision: es la fecha actual */
 		emision = new GregorianCalendar();
 		emision.set( Calendar.MINUTE, 0 );
 		emision.set( Calendar.SECOND, 0 );
 		emision.set( Calendar.MILLISECOND, 0 );
 
 		/* Se calcula la fecha de vigencia */
 		venc = calcularVigencia( titular.getFechaNac(), titular.getLicencias().isEmpty() );
 
 		/* Se crea la licencia a partir de los datos calculados */
 		Licencia licencia = new Licencia( titular, clase, nro, emision, venc, obs );
 
 		/* Se verifica la licencia, si todo est ok se retorna la misma,
 		 * si no, retornamos null.
 		 */
 		if( verificarLicencia(licencia) )
 			return licencia;
 		else 
 			return null;
 	}
 
	public static Titular buscarTitular( String tip, long nro ) {		
		TipoDoc tipo = DaoTipoDoc.read( tip );
		return DaoTitular.read( tipo, nro );
	}

 	public static void guardarLicencia(  Usuario usuario, Licencia licencia ) {
 		GestorAuditoria.reportarEmitirLicencia(usuario, licencia);
 		DaoLicencia.create( licencia );
 	}
 
 	/** Verifica un objeto licencia antes de persistirlo en la base de datos segun clase y edad */
	public static Boolean verificarLicencia( Licencia licencia ) {
 
 		/* ******************************************************************
 		 * 1- Si no cumple con la edad minima para esa clase, retorno false
 		 * ******************************************************************/
 		int edad = calcularEdad( licencia.getTitular().getFechaNac() );		
 		if( edad < licencia.getClaseLicencia().getEdadMinima() ) 
 		{
 			return false;
 		}
 		
 		/* ******************************************************************
 		 * 2- Verificamos que no tenga una licencia de la misma clase que este
 		 *    vigente
 		 * ******************************************************************/
 		List<Licencia> licencias = licencia.getTitular().getLicencias();
 		Iterator<Licencia> it = licencias.iterator();
 		String clase = licencia.getClaseLicencia().getTipo();
 
 		Calendar fechaActual = Calendar.getInstance();
 
 		while(it.hasNext())
 		{
 			Licencia licenciaExistente = (Licencia) it.next();
 			String claseExistente = licenciaExistente.getClaseLicencia().getTipo(); 
 			Calendar fechaVencimiento = licenciaExistente.getFechaVencimiento();
 			if(claseExistente.equals(clase) && fechaVencimiento.after(fechaActual))
 			{
 				return false;
 			}
 		}
 		
 		/* ******************************************************************
 		 * 3- Verificaciones para conductores profesionales
 		 * ******************************************************************/
 		if( clase.equals( "C" ) || clase.equals( "D" ) || clase.equals( "E" ) ) {
 
 			/* Si tiene mas de 65, no puede obtenerla por primera vez */
 			if( edad >= 65 ) {
 				it = licencias.iterator();
 
 				Boolean puede = false;
 				/* Se recorre el conjunto de licencias del titular buscando una
 				 * licencia profesional ya existente
 				 */
 				while( it.hasNext() && !puede ) {
 					Licencia lic = (Licencia)it.next();
 					String claseExistente = lic.getClaseLicencia().getTipo();
 					if( claseExistente.equals( "C" ) || 
 						claseExistente.equals( "D" ) ||
 						claseExistente.equals( "E" ) )
 					{
 						puede = true;
 					}
 				}
 
 				if( !puede )
 					return false;
 			} else {
 	
 				/* Y recorremos para ver si previamente tenia una B */
 				it = licencias.iterator();
 	
 				while( it.hasNext() ) {
 					Licencia lic = ( Licencia )it.next();
 	
 					if( lic.getClaseLicencia().getTipo().equals( "B" ) ) 
 					{  
 						/* La obtuvo minimo un ao antes? */
 						int anyos = calcularEdad( lic.getFechaEmision() );
 	
 						if( anyos >= 1 )
 							return true;
 					}
 				}
 	
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public static Calendar calcularVigencia( Calendar fechaNacimientoCalendar, boolean primeraVez  ) {		
 		int edad = calcularEdad( fechaNacimientoCalendar );
 		int anio = FECHA_ACTUAL.get( Calendar.YEAR );
 
 		/* Variable para calcular el ajuste por los 6 meses antes o despues del cumpleaos */
 		int ajuste = 0;
 
 		Calendar fechaCumpleanosCalendar = Calendar.getInstance();
 
 		fechaCumpleanosCalendar.set( anio, ( fechaNacimientoCalendar.get( Calendar.MONTH ) - 1 ), fechaNacimientoCalendar.get( Calendar.DATE ) );
 
 		/* Pregunto si ya paso o no la fecha de cumpleaos */
 		if( FECHA_ACTUAL.get( Calendar.MONTH ) > fechaNacimientoCalendar.get( Calendar.MONTH ) ) {
 			/* Si ya paso, pregunto si fue hace 6 meses */
 			if( ( FECHA_ACTUAL.get( Calendar.MONTH ) - fechaNacimientoCalendar.get( Calendar.MONTH ) ) <= 6 ) {
 				/* Si fue hace 6 meses se descuentan los meses perdidos */
 				ajuste = 0;
 			}
 			else {
 				/* Si faltan 6 meses se bonifican los meses perdidos */
 				ajuste = 1;
 			}
 		} else {
 			/* Pregunto si faltan 6 meses para el cumpleaos */
 			if( fechaNacimientoCalendar.get( Calendar.MONTH ) - ( FECHA_ACTUAL.get( Calendar.MONTH ) ) <= 6 ) {
 				/* Si faltan 6 meses se bonifica */
 				ajuste = 0;
 			}
 			else {
 				/* Si fue hace 6 meses se descuenta */
 				ajuste = 1;
 			}
 		}
 
 		if( edad < 21 ) {
 			if( primeraVez ) {		
 				anio += 1;
 			} else {
 				anio += 3;	
 			}
 		} else if( edad < 46 ) {
 			anio += 5;
 		} else if( edad < 60 ) {
 			anio += 4;
 		} else if( edad < 70) {
 			anio += 3;
 		} else {
 			anio += 1;
 		}
 
 		Calendar fechaVigenciaCalendar = Calendar.getInstance();
 
 		fechaVigenciaCalendar.set( ( anio + ajuste ), fechaNacimientoCalendar.get( Calendar.MONTH ), fechaNacimientoCalendar.get( Calendar.DATE ) );
 		fechaVigenciaCalendar.set( Calendar.MINUTE, 0 );
 		fechaVigenciaCalendar.set( Calendar.SECOND, 0 );
 		fechaVigenciaCalendar.set( Calendar.MILLISECOND, 0 );
 
 		return fechaVigenciaCalendar;
 	}
 
 	/* Devuelve una lista de string de todos los tipos de documentos existentes
 	 * en la BD.
 	 */
 	public static List<TipoDoc> obtenerTiposDocumento() {
 
 		return DaoTipoDoc.readAll();
 	}
 	
 	/* Calcula la edad en aos de alguien que nacio en la fecha */
 	private static int calcularEdad( Calendar fechaNacimientoCalendar ) {	
 		/* Se restan la fecha actual y la fecha de nacimiento */
 		int anio = FECHA_ACTUAL.get( Calendar.YEAR ) - fechaNacimientoCalendar.get( Calendar.YEAR );
 		int mes = FECHA_ACTUAL.get( Calendar.MONTH ) - fechaNacimientoCalendar.get( Calendar.MONTH );
 		int dia = FECHA_ACTUAL.get( Calendar.DATE ) - fechaNacimientoCalendar.get( Calendar.DATE );
 
 		/* Se ajusta el ao dependiendo el mes y el dia */
 		if( mes < 0 || ( mes == 0 && dia < 0 ) )
 			anio--;
 
 		return anio;
 	}
 
 	public static List<ClaseLicencia> obtenerTiposDeLicencia() {
 		return DaoClaseLicencia.readAll();
 	}
 }
