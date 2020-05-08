 package cr.ac.una.daw.reshotel.data;
 
 import java.util.Map;
 
 import cr.ac.una.daw.reshotel.data.Constantes.UbicacionColumns;
 
 public class Ubicacion extends TableGateway<Ubicacion> {
 
 	private String pais;
 	private String ciudad;
 
 	public Ubicacion() {
 		super();
 	}
 
 	public Ubicacion(String pais, String ciudad) {
 		super();
 		this.pais = pais;
 		this.ciudad = ciudad;
 	}
 
 	public String getPais() {
 		return pais;
 	}
 
 	public void setPais(String pais) {
 		this.pais = pais;
 	}
 
 	public String getCiudad() {
 		return ciudad;
 	}
 
 	public void setCiudad(String ciudad) {
 		this.ciudad = ciudad;
 	}
 
 	@Override
 	public String getTableName() {
 		return Constantes.UBICACION_TABLE_NAME;
 	}
 
 	@Override
 	public String getInsertStatement() {
		return "INSERT INTO " + getTableName() + " VALUES(?,?)";
 	}
 
 	@Override
 	public String getUpdateStatement() {
 		return "UPDATE " + getTableName() + "SET " + UbicacionColumns.PAIS
 				+ " = ?" + ", " + UbicacionColumns.PAIS + " = ?" + " WHERE "
 				+ UbicacionColumns.ID + " = ?";
 	}
 
 	@Override
 	public Ubicacion fromMap(Map<String, Object> values) {
 		Ubicacion result = new Ubicacion(
 				(String) values.get(UbicacionColumns.PAIS),
 				(String) values.get(UbicacionColumns.CIUDAD));
 		return result;
 	}
	
 	@Override
 	public void load() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Object[] getUpdateArgs() {
 		return new Object[] { getPais(), getCiudad(), getId() };
 	}
 
 }
