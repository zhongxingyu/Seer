 package casarural;
 
 import java.sql.*;
 import java.util.*;
 //import java.rmi.*;
 //import java.sql.Date.*;
 
 public final class GestorBD {
 
 	public static final String FUENTE_DATOS = "jdbc:mysql://localhost/casarural";
 	private static final String user = "root";
 	private static final String pass = "";
 //	public static final String FUENTE_DATOS = "jdbc:mysql://db4free.net/iso2012";
 //	private static final String user = "iso2012";
 //	private static final String pass = "grupo1";
 
 	private static GestorBD elGestorBD = new GestorBD();
 	private Connection c;
 	private Statement s;
 
 	/**
 	 * Constructor
 	 */
 	private GestorBD() {
 		try {
 			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
 			c = DriverManager.getConnection(FUENTE_DATOS, user, pass);
 			c.setAutoCommit(true);
 			s = c.createStatement();
 		} catch (Exception ex) {
 			System.out.println("Error cargando la Fuente de Datos: " + FUENTE_DATOS + "\n\t" + ex.toString());
 		}
 	}
 
 	/**
 	 * Devuelve una instancia del gestor de la BD
 	 *
 	 * @param Ninguno
 	 * @return GestorBD
 	 */
 	public static GestorBD getInstance() {
 		return elGestorBD;
 	}
 
 	/**
 	 * Selecciona reservas entre las fechas que se le indican
 	 *
 	 * @param Dia de inicio, dia de fin y numero de casas
 	 * @return Vector de objetos Oferta
 	 */
 	public List<Oferta> seleccionarReservas(java.sql.Date diaIni, java.sql.Date diaFin, int numCasa) throws NoSePuedeReservarException {
 		List<Oferta> vectorReservas = new ArrayList<Oferta>();
 		try {
 			String consulta = "SELECT NumOferta, DiaIni, DiaFin, Precio FROM Oferta WHERE NumReserva IS NULL AND NumCasa = "
 					+ numCasa
 //					+ " AND DiaIni >= \""
 					+ " AND DiaIni <= \"" // CAMBIADO
 					+ diaIni
 //					+ "\" AND DiaFin <= \"" + diaFin + "\"";
 					+ "\" AND DiaFin >= \"" + diaFin + "\""; // CAMBIADO
 			ResultSet rs = s.executeQuery(consulta);
 			while (rs.next()) {
 				String numeroOferta = rs.getString("NumOferta");
 				java.sql.Date diaFinal = rs.getDate("DiaFin");
 				java.sql.Date diaInicio = rs.getDate("DiaIni");
 				float precio = rs.getFloat("Precio");
 				Oferta res = new Oferta();
 				res.setNumOferta(numeroOferta);
 				res.setDiaIni(diaInicio);
 				res.setDiaFin(diaFinal);
 				res.setPrecio(precio);
 				vectorReservas.add(res);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (vectorReservas.isEmpty())
 			throw new NoSePuedeReservarException("La casa no se encuentra disponible estos dias");
 		return vectorReservas;
 	}
 
 	/**
 	 * Dado un numero de casa selecciona al propietario de esa casa
 	 *
 	 * @param Numero de casa
 	 * @return El propietario
 	 */
 	public Propietario seleccionarPropietario(int numCasa) {
 		Propietario p = new Propietario();
 		try {
 			String consulta = "SELECT P.NumCuentaCorriente FROM Propietario AS P INNER JOIN CasaRural AS C ON C.Propietario=P.CuentaSistema WHERE C.NumCasa = " + numCasa;
 			ResultSet rs = s.executeQuery(consulta);
 			if (rs.next()) {
 				String str = new String(rs.getString("NumCuentaCorriente"));
 				p.setNumCuentaCorriente(str);
 			}
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 		}
 		return p;
 	}
 
 	/**
 	 * Devuelve las casas dado un numero de cuenta de propietario
 	 *
 	 * @param Numero de cuenta de propietario
 	 * @return Vector de objetos de la clase Casa
 	 */
 	public List<Casa> seleccionarCasas(String cuentaSistema) {
 		List<Casa> vectorCasas = new ArrayList<Casa>();
 		try { 
 			String consulta = "SELECT NumCasa FROM CasaRural WHERE Propietario = \""
 					+ cuentaSistema + "\"";
 			ResultSet rs = s.executeQuery(consulta);
 			while (rs.next()) {
 				int nc = rs.getInt("NumCasa");
 				Casa ca = new Casa();
 				ca.setNumCasa(nc);
 				vectorCasas.add(ca);
 			}
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 		}
 		return vectorCasas;
 	}
 
 	/**
 	 * Devuelve las todas las casas
 	 *
 	 * @param Ninguno
 	 * @return Vector de objetos de la clase Casa
 	 */
 	public List<Casa> seleccionarCasas() {
 		List<Casa> vectorCasas = new ArrayList<Casa>();
 		try {
 			String consulta = "SELECT NumCasa FROM CasaRural";
 			ResultSet rs = s.executeQuery(consulta);
 			while (rs.next()) {
 				int nc = rs.getInt("NumCasa");
 				Casa ca = new Casa();
 				ca.setNumCasa(nc);
 				vectorCasas.add(ca);
 			}
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 		}
 		return vectorCasas;
 	}
 
 	/**
 	 * Devuelve el resultado de las ofertas que interesan, segun las
 	 * restricciones de fechas, precio y demas, que opcionalmente pueda haber
 	 * introducido.
 	 *
 	 * Para la realizacion de la consulta, se ha decidido generarla a medida que
 	 * se descubren los parametros introducidos por el usuario. De esta manera,
 	 * puede capturarse cualquiera de las posibilidades introducidas por el
 	 * usuario, de una vez en una sola consulta.
 	 *
 	 * @param numDormitorios, minimo numero de dormitorios.
 	 * @param numBaños, minimo numero de baños.
 	 * @param diaIni, fecha de inicio de para la busqueda de ofertas.
 	 * @param diaIni, fecha de finalizacion de para la busqueda de ofertas.
 	 * @param precio, precio maximo de las ofertas a priori.
 	 * @param numMinDias, numero minimo de dias para la estancia.
 	 * @param orden, ordenados por precio o por numero de dias.
 	 * @return Vector, un vector con las ofertas interesantes para el usuario.
 	 */
 	public List<Oferta> seleccionarCasasDorWC(int numDormitorios, int numBanos, java.sql.Date diaIni, java.sql.Date diaFin, float precio, int numMinDias, boolean orden) {
 
 		String banos = "";
 		String dormi = "";
 		String dias = "";
 		String sOrden = "";
 		String precioMax = "";
 
 		if (numDormitorios != 0) dormi = " AND NumDormitorios >= " + numDormitorios;
 		if (numBanos != 0) banos = " AND NumBaños >= " + numBanos;
 		if (precio != 0) precioMax = " AND Precio <= " + precio;
 		if (numMinDias != 0) dias = " AND (TO_DAYS(DiaFin) - TO_DAYS(DiaIni)) >= " + numMinDias;
 
 		if (orden) sOrden = " ORDER BY Precio ASC";
 		else sOrden = " ORDER BY numDias DESC";
 
 		String consulta = "SELECT *, (TO_DAYS(DiaFin) - TO_DAYS(DiaIni)) AS numDias "
 				+ "FROM CasaRural AS C INNER JOIN Oferta AS O ON C.NumCasa = O.NumCasa "
 				+ "WHERE NumReserva IS NULL " + banos + dormi + precioMax + dias
 				+ " AND DiaIni >= \"" + diaIni + "\" AND Diafin <= \"" + diaFin + "\" " ;
 		List<Oferta> ofertas = new ArrayList<Oferta>();
 
 		try {
 			consulta = consulta + sOrden;
 //			System.out.println(consulta);
 			ResultSet rs = s.executeQuery(consulta );
 			// Se crea la oferta resultado, y se pasa al vector de ofertas.
 			while (rs.next()) {
 				Oferta of = new Oferta();
 				of.setPrecio(rs.getFloat("precio"));
 				of.setDiaFin(rs.getDate("diafin"));
 				of.setDiaIni(rs.getDate("diaini"));
 				of.setNumCasa(rs.getInt("NumCasa"));
 				Integer i = new Integer(rs.getInt("Numoferta"));
 				of.setNumOferta(i.toString());
 				ofertas.add(of);
 			}
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 		}
 
 		return ofertas;
 	}
 
 	/**
 	 * Selecciona las ofertas entre las fechas dadas para un numero de casa
 	 *
 	 * @param Diade inicio, dia de fin y numero de casa
 	 * @return Vector de objetos de la clase Oferta
 	 */
 	public List<Oferta> seleccionarOfertas(java.sql.Date diaIni, java.sql.Date diaFin, int numCasa) {
 		List<Oferta> vectorOfertas = new ArrayList<Oferta>();
 		try {
 			String consulta = "SELECT NumOferta, DiaIni, DiaFin, Precio, NumReserva "
 					+ "FROM Oferta "
 					+ "WHERE DiaFin >= \"" + diaFin
 					+ "\" AND DiaIni <= \"" + diaIni
 					+ "\" AND DiaFin >= \"" + diaIni
 					+ "\" AND DiaIni <= \"" + diaFin
 					+ "\" AND NumCasa = " + numCasa;
 //			System.out.println(consulta);
 			ResultSet rs = s.executeQuery(consulta);
 			while (rs.next()) {
 				String nOferta = rs.getString("NumOferta");
 				java.sql.Date dIni = rs.getDate("DiaIni");
 				java.sql.Date dFin = rs.getDate("DiaFin");
 				float pr = rs.getFloat("Precio");
 				String nRes = rs.getString("NumReserva");
 				Oferta of = new Oferta();
 				of.setDiaFin(dFin);
 				of.setDiaIni(dIni);
 				of.setNumOferta(nOferta);
 				of.setNumReserva(nRes);
 				of.setPrecio(pr);
 				vectorOfertas.add(of);
 			}
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 		}
 
 		return vectorOfertas;
 	}
 
 	/**
 	 * Realiza la reserva en forma de transaccion
 	 *
 	 * @param Vector de Strings numeros de oferta, numero de reserva, telefono y
 	 * precio
 	 * @return Ninguno
 	 */
 	public void transaccionDeReserva(List<String> reservasTotales, String numReserva, String numTfnoReserva, float precioTotal) {
 		try { // CAMBIADO
 			java.sql.Date diadehoy = new java.sql.Date(System.currentTimeMillis());
 			s.executeQuery("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;");
 			String consulta1 = "INSERT INTO Reserva (NumReserva, Pagado, Fecha, NumTfno, PrecioTotal) "
 					+ "VALUES (" + numReserva + ", FALSE, \"" + diadehoy + "\", \"" + numTfnoReserva + "\", " + precioTotal + ")";
 			s.executeUpdate(consulta1);
 			for (String nOferta: reservasTotales) {
 				String consulta = "UPDATE Oferta SET NumReserva = \""
 						+ numReserva + "\" WHERE NumOferta ="
 						+ Integer.valueOf(nOferta).intValue();
 				s.executeUpdate(consulta);
 			}
 			s.executeQuery("COMMIT;");
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 		}
 	}
 
 	/**
 	 * Realiza una trasaccion de ofertas
 	 *
 	 * @param Vector de Strings numeros de oferta, dia de inicio, dia de fin,
 	 * numero de la primera oferta, numero de la ultima oferta,precio
 	 * y numero de casa
 	 * @return Ninguno
 	 */
 	public void transaccionDeOfertas(List<String> todasLasOfertasIncluidas,
 			java.sql.Date diaIni, java.sql.Date diaFin, String numOfePrimera,
 			String numOfeUltima, float precio, int numCasa) {
 		try {
 			// Connection
 			Connection auxC = DriverManager.getConnection(FUENTE_DATOS, user, pass);
 			auxC.setAutoCommit(false);
 			Statement auxS = auxC.createStatement();
 			String consulta;
 			if (!numOfePrimera.equals(numOfeUltima)) {
 				if (!numOfePrimera.equals("cero")) {
 					String consulta1 = "SELECT Precio, DiaIni, DiaFin FROM Oferta WHERE NumOferta = "
 							+ Integer.valueOf(numOfePrimera).intValue();
 					ResultSet rs1 = auxS.executeQuery(consulta1);
 					rs1.next();
 					float prPrim = rs1.getFloat("Precio");
 					java.sql.Date dIniPrim = rs1.getDate("DiaIni");
 					java.sql.Date dFinPrim = rs1.getDate("DiaFin");
 					int numeroDiasPrim = (int) ((dFinPrim.getTime() - dIniPrim
 							.getTime()) / (1000 * 3600 * 24)) + 1;
 					float precioPorDiaPrim = prPrim / (int) numeroDiasPrim;
 					int numDiasAntes = (int) ((diaIni.getTime() - dIniPrim
 							.getTime()) / (1000 * 3600 * 24));
 					int nuevoPrecioPrim = (int) (numDiasAntes * precioPorDiaPrim);
 					java.sql.Date diaIniNuevo = new java.sql.Date(diaIni
 							.getTime() - 1000 * 3600 * 24);
 					if (diaIniNuevo.getTime() >= dIniPrim.getTime()) {
 						String consultaA1 = "UPDATE Oferta SET DiaFin = \""
 								+ diaIniNuevo + "\", Precio = "
 								+ nuevoPrecioPrim + " WHERE NumOferta ="
 								+ Integer.valueOf(numOfePrimera).intValue();
 						auxS.executeUpdate(consultaA1);
 					} else {
 						String consultaA3 = "DELETE FROM Oferta WHERE NumOferta = "
 								+ Integer.valueOf(numOfePrimera).intValue();
 						auxS.executeUpdate(consultaA3);
 					}
 				}
 				if (!numOfeUltima.equals("cero")) {
 					String consulta2 = "SELECT Precio, DiaIni, DiaFin FROM Oferta WHERE NumOferta = "
 							+ Integer.valueOf(numOfeUltima).intValue();
 					ResultSet rs2 = auxS.executeQuery(consulta2);
 					rs2.next();
 					float prUlti = rs2.getFloat("Precio");
 					java.sql.Date dIniUlti = rs2.getDate("DiaIni");
 					java.sql.Date dFinUlti = rs2.getDate("DiaFin");
 					int numeroDiasUlti = (int) ((dFinUlti.getTime() - dIniUlti
 							.getTime()) / (1000 * 3600 * 24)) + 1;
 					float precioPorDiaUlti = prUlti / (int) numeroDiasUlti;
 					int numDiasDespues = (int) ((dFinUlti.getTime() - diaFin
 							.getTime()) / (1000 * 3600 * 24));
 					int nuevoPrecioUlti = (int) (numDiasDespues * precioPorDiaUlti);
 					java.sql.Date diaFinNuevo = new java.sql.Date(diaFin
 							.getTime() + 1000 * 3600 * 24);
 					if (dFinUlti.getTime() >= diaFinNuevo.getTime()) {
 						String consultaA2 = "UPDATE Oferta SET DiaIni = \""
 								+ diaFinNuevo + "\", Precio = "
 								+ nuevoPrecioUlti + " WHERE NumOferta = "
 								+ Integer.valueOf(numOfeUltima).intValue();
 						auxS.executeUpdate(consultaA2);
 					} else {
 						String consultaA4 = "DELETE FROM Oferta WHERE NumOferta = "
 								+ Integer.valueOf(numOfeUltima).intValue();
 						auxS.executeUpdate(consultaA4);
 					}
 				}
 			} else if (!numOfePrimera.equals("cero")) {
 				String consulta1 = "SELECT Precio, DiaIni, DiaFin FROM Oferta WHERE NumOferta = "
 						+ Integer.valueOf(numOfePrimera).intValue();
 				ResultSet rs1 = auxS.executeQuery(consulta1);
 				rs1.next();
 				float prPrim = rs1.getFloat("Precio");
 				java.sql.Date dIniPrim = rs1.getDate("DiaIni");
 				java.sql.Date dFinPrim = rs1.getDate("DiaFin");
 				int numeroDiasPrim = (int) ((dFinPrim.getTime() - dIniPrim
 						.getTime()) / (1000 * 3600 * 24)) + 1;
 				float precioPorDiaPrim = prPrim / (int) numeroDiasPrim;
 				int numDiasAntes = (int) ((diaIni.getTime() - dIniPrim
 						.getTime()) / (1000 * 3600 * 24));
 				int numDiasDespues = (int) ((dFinPrim.getTime() - diaFin
 						.getTime()) / (1000 * 3600 * 24));
 				int nuevoPrecioPrim = (int) (numDiasAntes * precioPorDiaPrim);
 				int nuevoPrecioUlti = (int) (numDiasDespues * precioPorDiaPrim);
 				java.sql.Date diaIniNuevo = new java.sql.Date(
 						diaIni.getTime() - 1000 * 3600 * 24);
 				java.sql.Date diaFinNuevo = new java.sql.Date(
 						diaFin.getTime() + 1000 * 3600 * 24);
 				if (diaIniNuevo.getTime() >= dIniPrim.getTime()) {
 					String consultaA5 = "UPDATE Oferta SET DiaFin = \""
 							+ diaIniNuevo + "\", Precio = " + nuevoPrecioPrim
 							+ " WHERE NumOferta = "
 							+ Integer.valueOf(numOfePrimera).intValue();
 					auxS.executeUpdate(consultaA5);
 				} else {
 					String consultaA6 = "DELETE FROM Oferta WHERE NumOferta = "
 							+ Integer.valueOf(numOfePrimera).intValue();
 					auxS.executeUpdate(consultaA6);
 				}
 				if (dFinPrim.getTime() >= diaFinNuevo.getTime()) {
 					String consulta2 = "INSERT INTO Oferta (DiaIni, DiaFin, Precio, NumCasa) "
 							+ "VALUES(\""
 							+ diaFinNuevo
 							+ "\", \""
 							+ dFinPrim
 							+ "\", " + nuevoPrecioUlti + ", " + numCasa + ")";
 					auxS.executeUpdate(consulta2);
 				}
 			}
 			for (String nOferta: todasLasOfertasIncluidas) {
 				consulta = "DELETE FROM Oferta WHERE NumOferta = "
 						+ Integer.valueOf(nOferta).intValue();
 				auxS.executeUpdate(consulta);
 			}
 			String consultaI1 = "INSERT INTO Oferta (DiaIni, DiaFin, Precio, NumCasa) "
 					+ "VALUES(\""
 					+ diaIni
 					+ "\", \""
 					+ diaFin
 					+ "\", "
 					+ precio
 					+ ", " + numCasa + ")";
 			auxS.executeUpdate(consultaI1);
 			auxC.commit();
 			auxC.close();
 		} catch (Exception ex) {
 			System.out.println(ex.toString());
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Metodo que obtiene el numero de habitaciones y de baños dado el id de la casa
 	 *
 	 * @param numCasa, id de la casa a consultar
 	 * @return vector que contiene por un lado, el numero de habitaciones, y por el otro
 	 * el numero de baños de dicha casa
 	 */
 	public List<String> numHabitacionesnumBanos(int numCasa) {
 		List<String> v = new ArrayList<String>();
 		try {
 			String consulta = "SELECT numDormitorios, numBaños FROM CasaRural "
 					+ "WHERE numCasa = " + numCasa;
 
 			ResultSet rs = s.executeQuery(consulta);
 
 			if (rs.next()) {
 				int dormitorios = rs.getInt(1);
 				int banos = rs.getInt(2);
 				v.add(String.valueOf(dormitorios));
 				v.add(String.valueOf(banos));
 			}
 
 		} catch (Exception ex) {
 			System.out.println(ex.toString()); // error al contar habitaciones y baños
 		}
 		return v;
 	}
 
 	/**
 	 * Metodo que obtiene el numero de camas de una casa. Las camas dobles cuentan por dos.
 	 * El metodo devolvera un unico numero con el total de camas de dicha casa.
 	 *
 	 * @param numCasa, identificador de la casa
 	 * @return el numero de camas de la casa
 	 */
 	public int camas(int numCasa) {
 		int numcamas = 0;
 		try {
 			String consulta = "SELECT SUM(NumCamasSencillas)+SUM(NumCamasDobles)*2 AS tamano "
 					+ "FROM Dormitorio WHERE NumCasa = " + numCasa + " GROUP BY NumCasa";
 
 			ResultSet rs = s.executeQuery(consulta);
 
 			if (rs.next())
 				numcamas = rs.getInt(1);
 
 		} catch (Exception ex) {
 			System.out.println(ex.toString()); // error en camas
 		}
 		return numcamas;
 	}
 	
 	/**
 	 * 
 	 * Metodo que devuelve en un Vector las reservas, ordenadas por el numero de reserva, cuya fecha
 	 * de entrada estan entre las fechas de inicio y fin indicadas en los parametros correspondiente
 	 * 
 	 * @param diaIni, fecha de inicio de la busqueda
 	 * @param diaFin, fecha de fin de la busqueda
 	 * @return Vector con las reservas cuya fecha de entrada esta entre las indicadas
 	 */
 	public Vector<Reserva> seleccionarReservas (java.sql.Date diaIni, java.sql.Date diaFin) {
 		Vector<Reserva> reservas = new Vector<Reserva>();
 			try {
 				ResultSet rs = s.executeQuery("SELECT DISTINCT Oferta.NumReserva, PrecioTotal, NumCasa "
 						+ "FROM Oferta INNER JOIN Reserva ON Oferta.numReserva = Reserva.numReserva "
 						+ "WHERE Oferta.NumReserva IS NOT NULL AND DiaIni <= \"" + diaFin + "\" AND DiaIni >= \"" + diaIni + "\" AND DiaFin >= \"" + diaIni + "\" AND DiaFin <= \"" + diaFin + "\"");				
 				Reserva res;
 				String numR;
 				int casa;
 				float precio;
 				while(rs.next()){
 					numR = rs.getString("NumReserva");
 					casa = rs.getInt("NumCasa");
 					precio = rs.getFloat("PrecioTotal");
 					res = new Reserva(numR,casa,precio);
 					reservas.add(res);
 				}
 			} catch (SQLException e) {
 				System.out.println(e.toString());
 				e.printStackTrace();
 			}
 		return reservas;
 	}
 
 	/**
 	 * Devuelve las ofertas correspondientes a una reserva
 	 * 
 	 * @param numReserva
 	 * @return Vector con las ofertas correspondientes a una reserva
 	 */
 	public Vector<Oferta> seleccionarOfertas (String numReserva) {
 		Vector<Oferta> ofertas = new Vector<Oferta>();
 		Oferta oferta;
 		String numO, numR;
 		java.sql.Date diaIni,diaFin;
 		float precio;
 		try {
 			ResultSet rs = s.executeQuery("SELECT NumOferta, NumReserva, DiaIni, DiaFin, Precio FROM Oferta "
 					+ "WHERE NumReserva = " + numReserva + " ORDER BY DiaIni ASC");
 			while(rs.next()){
 				numO = rs.getString("NumOferta");
 				numR = rs.getString("NumReserva");
 				diaIni = rs.getDate("DiaIni");
 				diaFin = rs.getDate("DiaFin");
 				precio = rs.getFloat("Precio");
 				oferta = new Oferta();
 				oferta.setDiaFin(diaFin);
 				oferta.setDiaIni(diaIni);
 				oferta.setNumOferta(numO);
 				oferta.setNumReserva(numR);
 				oferta.setPrecio(precio);
 				ofertas.add(oferta);
 			}
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 			e.printStackTrace();
 		}
 		return ofertas;
 	}
 
 	/**
 	 * Dado un numero de reserva lo elimina de la base de datos y deja disponibles todas las ofertas correspondientes
 	 * Todo dentro de una transaccion.
 	 * 
 	 * @param numR, numero de la reserva
 	 * @param ofertas, vector de ofertas asociadas a la reserva
 	 */
 	public void anularReserva(String numR, Vector<Oferta> ofertas, Vector<Oferta> ofAnular) throws SQLException{
 
 		try {
 			s.executeQuery("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;");
 			for(int i=0;i<ofertas.size();i++){
 				s.executeUpdate("UPDATE Oferta SET NumReserva = NULL "
 						+ "WHERE NumOferta = " + Integer.parseInt(ofertas.elementAt(i).getNumOferta()));
 			}
 			for(int i=0;i<ofAnular.size();i++){
 				s.executeUpdate("UPDATE Oferta SET NumReserva= -1 "
 						+ "WHERE NumOferta = "+Integer.parseInt(ofAnular.elementAt(i).getNumOferta()));
 			}
 			s.executeUpdate("DELETE FROM Reserva WHERE NumReserva = " + numR);
 			s.executeQuery("COMMIT;");
 		}
 		catch(SQLException e){
 			s.executeQuery("ROLLBACK;");
 			System.out.println(e.toString());
 			e.printStackTrace();
 			throw new SQLException();
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * Obtiene la lista de todas las casas existentes
 	 * 
 	 * @return la lista de todas las casas existentes
 	 */
 	public List<Casa> obtenerCasas() throws SQLException {
 		List<Casa> casas = new ArrayList<Casa>();
 		String consulta = "SELECT NumCasa, Descripcion, Poblacion, Propietario FROM CasaRural";
 		ResultSet rs = s.executeQuery(consulta);
 		while (rs.next()) {
 			Casa ca = new Casa();
 			ca.setNumCasa(rs.getInt("NumCasa"));
 			ca.setDescripcion(rs.getString("Descripcion"));
 			ca.setPoblacion(rs.getString("Poblacion"));
 			ca.setPropietario(rs.getString("Propietario"));
 			casas.add(ca);
 		}
 		return casas;
 	}
 	
 	
 	/**
 	 * Inserta un nuevo recorrido
 	 * 
 	 * @param casas lista de casas por las que pasa el nuevo recorrido
 	 * @return el identificador asignado al nuevo recorrido, o -1 en caso de no haberlo insertado
 	 */
 	int insertarRecorrido(List<Casa> casas) throws SQLException {
 		int recorrido = -1;
 		if (casas.isEmpty()) return recorrido; // si la lista viene vacía, no se inserta nada
 		try {
 			s.executeUpdate("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
 			
 			ResultSet rs = s.executeQuery("SELECT MAX(NumRecorrido) AS Ultimo FROM Recorrido");
 			if (rs.next()) recorrido = 1+rs.getInt("Ultimo");
 			if (recorrido == -1) throw new SQLException("No se sabe cual es el último recorrido insertado.");
 			
 			s.executeUpdate("INSERT INTO Recorrido (NumRecorrido) VALUES (" + recorrido + ")");
 			for(int i=0; i<casas.size(); i++)
 				s.executeUpdate("INSERT INTO CasaRural_Recorrido (Recorrido_NumRecorrido, CasaRural_NumCasa) VALUES (" + recorrido + ", " + casas.get(i).getNumCasa() + ")");
 			s.executeUpdate("COMMIT");
 		} catch(SQLException e){
 			s.executeUpdate("ROLLBACK");
 			System.out.println(e.toString());
 			throw new SQLException();
 		}
 		return recorrido;
 	}
 	
 	
 	/**
 	 * Obtiene los todos los servicios disponibles que cumplan que
 	 * son en la fecha y pasan por la casa indicados
 	 * 
 	 * @param codCasa código de la casa por la que tiene que pasar el servicio
 	 * @param fecha fecha en la que contratar el servicio
 	 * @return la lista de servicios que cumplen dichas condiciones
 	 */
 	public List<Servicio> obtenerServicios(int codCasa, java.sql.Date fecha) throws SQLException {
 		List<Servicio> servicios = new ArrayList<Servicio>();
 		String consulta = "SELECT S.NumServicio, DATE(S.Fecha) AS Fecha, TIME(S.Fecha) AS Hora, S.NumRecogida, S.NumPlazas, S.Precio, S.NumPlazasReservadas, S.NumRecorrido, R.Tipo, R.Direccion, R.Nombre "
 				+ "FROM (Servicio AS S INNER JOIN CasaRural_Recorrido AS C ON S.NumRecorrido = C.Recorrido_NumRecorrido) INNER JOIN Recogida as R ON S.NumRecogida = R.NumRecogida "
 				+ "WHERE C.CasaRural_NumCasa = " + codCasa + " AND DATE(S.Fecha) = \"" + fecha + "\"";
 		ResultSet rs = s.executeQuery(consulta);
 		while (rs.next()) {
 			Servicio serv = new Servicio();
 			serv.setNumServicio(rs.getInt("NumServicio"));
 			serv.setFecha(rs.getDate("Fecha"));
 			serv.setHora(rs.getTime("Hora"));
 			serv.setNumRecogida(rs.getInt("NumRecogida"));
 			serv.setNumPlazas(rs.getInt("NumPlazas"));
 			serv.setPrecio(rs.getFloat("Precio"));
 			serv.setNumPlazasReservadas(rs.getInt("NumPlazasReservadas"));
 			serv.setNumRecorrido(rs.getInt("NumRecorrido"));
 			serv.setTipoRecogida(rs.getString("Tipo"));
 			serv.setDireccionRecogida(rs.getString("Direccion"));
 			serv.setNombreRecogida(rs.getString("Nombre"));
 			servicios.add(serv);
 		}
 		return servicios;
 	}
 	
 	
 	/**
 	 * Realiza una reserva (incluyendo servicio de recogida) en forma de transacción
 	 *
 	 * @param reservasTotales lista de ofertas que se han reservado
 	 * @param numReserva número de referencia que se va a asignar a la nueva reserva
 	 * @param numTfnoReserva número de teléfono asociado a la reserva
 	 * @param precioTotal precio total de la reserva
 	 * @param idServicio número identificador del servicio de recogida a contratar
 	 * @param numPlazas número de plazas a reservar en el servicio de recogida
 	 * @return void
 	 * @throws SQLException
 	 */
	public void transaccionDeReserva(List<Reserva> reservasTotales, int numReserva, String numTfnoReserva, float precioTotal, int idServicio, int numPlazas) throws SQLException {
 		try {
 			java.sql.Date diadehoy = new java.sql.Date(System.currentTimeMillis());
 			s.executeUpdate("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
 			String consulta1 = "INSERT INTO Reserva (NumReserva, Pagado, Fecha, NumTfno, PrecioTotal, NumServicio) "
 					+ "VALUES (" + numReserva + ", FALSE, \"" + diadehoy + "\", \"" + numTfnoReserva + "\", " + precioTotal + ", " + idServicio + ")";
 			s.executeUpdate(consulta1);
 			s.executeUpdate("UPDATE Servicio SET NumPlazasreservadas = NumPlazasreservadas+" + numPlazas + " WHERE NumServicio = " + idServicio);
			for (Reserva nOferta: reservasTotales) {
 				String consulta = "UPDATE Oferta SET NumReserva = \""
 						+ numReserva + "\" WHERE NumOferta = "
						+ Integer.valueOf(nOferta.getNumReserva()).intValue();
 				s.executeUpdate(consulta);
 			}
 			s.executeUpdate("COMMIT");
 		} catch(SQLException e){
 			s.executeUpdate("ROLLBACK");
 			System.out.println(e.toString());
 			throw new SQLException();
 		}
 	}
 /*
 	public List<Servicio> getServicios() {
 		List<Servicio> servicios = new ArrayList<Servicio>();
 		
 		try {
 			ResultSet rs = s.executeQuery("SELECT NumServicio, Fecha, NumRecogida, NumPlazas, "
 					+" Precio, NumPlazasReservadas, NumRecorrido FROM Recorrido");
 			
 			while (rs.next()) {
 				Servicio servicio = new Servicio();
 				
 				servicio.setNumRecorrido(rs.getInt("NumRecorrido"));
 				servicio.setFecha(rs.getDate("Fecha"));
 				servicio.setNumRecogida(rs.getInt("NumRecogida"));
 				servicio.setNumPlazas(rs.getInt("NumPlazas"));
 				servicio.setPrecio(rs.getFloat("Precio"));
 				servicio.setNumPlazasReservadas(rs.getInt("NumPlazasReservadas"));
 				servicio.setNumRecorrido(rs.getInt("NumRecorrido"));
 				
 				servicios.add(servicio);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return servicios;
 	}
 */
 	
 	/**
 	 * Obtiene los identificadores de todos los recorridos almacenados
 	 *
 	 * @return lista con los identificadores de todos los recorridos disponibles
 	 */
 	public List<Recorrido> getRecorridos() {
 		List<Recorrido> recorridos = new ArrayList<Recorrido>();
 		
 		try {
 			ResultSet rs = s.executeQuery("SELECT NumRecorrido FROM Recorrido");
 			
 			while (rs.next()) {
 				Recorrido recorrido = new Recorrido();
 				
 				recorrido.setNumRecorrido(rs.getInt("NumRecorrido"));
 				
 				recorridos.add(recorrido);
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return recorridos;
 	}
 
 	/**
 	 * Obtiene todos los lugares de recogida almacenados
 	 *
 	 * @return lista con todos los lugares de recogida almacenados
 	 */
 	public List<Recogida> getRecogidas() {
 		List<Recogida> recogidas = new ArrayList<Recogida>();
 		
 		try {
 			ResultSet rs = s.executeQuery("SELECT NumRecogida, Tipo, Direccion, Nombre FROM Recogida");
 			
 			while (rs.next()) {
 				Recogida recogida = new Recogida();
 				
 				recogida.setNumRecogida(rs.getInt("NumRecogida"));
 				recogida.setTipo(rs.getString("Tipo"));
 				recogida.setDireccion(rs.getString("Direccion"));
 				recogida.setNombre(rs.getString("Nombre"));
 				
 				recogidas.add(recogida);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return recogidas;
 	}
 
 	/**
 	 * Crea un nuevo servicio
 	 *
 	 * @param servicio nuevo servicio a insertar
 	 * @return true si se ha insertado el servicio, false en otro caso
 	 * @throws SQLException
 	 */
 	public boolean crearServicio(Servicio servicio) throws SQLException {
 		PreparedStatement prstd = c.prepareStatement("INSERT INTO Servicio (Fecha, NumRecogida, NumPlazas, "
 				+ "Precio, NumPlazasReservadas, NumRecorrido) VALUES (\"" + servicio.getFecha() + " " + servicio.getHora() + "\", ?, ?, ?, ?, ?)");
 		
 		//prstd.setString(1, "\"" + servicio.getFecha() + " " + servicio.getHora() + "\"");
 		prstd.setInt(1, servicio.getNumRecogida());
 		prstd.setInt(2, servicio.getNumPlazas());
 		prstd.setFloat(3, servicio.getPrecio());
 		prstd.setInt(4, 0);
 		prstd.setInt(5, servicio.getNumRecorrido());
 		
 		int result = prstd.executeUpdate();
 		
 		return result == 1;
 	}
 }
