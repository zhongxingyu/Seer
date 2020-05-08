 package ar.edu.utn.frsf.licenciator.entidades;
 
 import javax.persistence.Id;
 import javax.persistence.Entity;
 
 @Entity
 public class ClaseLicencia {
 
 	@Id
 	private String tipo;
 	private String descripcion;
 	private int edadMinima;
 	
 	/* Nota: Al no estar implementado el "Calcular costo vigencia,
 	 * se toma el mismo costo para toda la clase.
 	 */
 	private int costo;
 	
 	public ClaseLicencia ()
 	{}
 
 	public ClaseLicencia (String tipo, String desc, int edad, int c1)
 	{
 		this.tipo = tipo;
 		descripcion = desc;
 		edadMinima = edad;
 		costo = c1;
 	}
 	
 	public String getTipo()
 	{
 		return tipo;
 	}
 	
 	public String getDescripcion()
 	{
 		return descripcion;
 	}
 	
 	public int getEdadMinima()
 	{
 		return edadMinima;
 	}
 	
 	public int getCosto()
 	{
 		return costo;
 	}
 	
 	/** Utilizado para que en los CheckBoxes aparezca el nombre
 	 * de la licencia
 	 */
 	@Override
 	public String toString() {
 		return tipo;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj.getClass() == ClaseLicencia.class) {
 			ClaseLicencia lic = (ClaseLicencia) obj;
			return (tipo.equals(lic.tipo) &&
					descripcion.equalsIgnoreCase(lic.descripcion) &&
					edadMinima==lic.edadMinima &&
					costo==lic.costo);
 		} else if(obj.getClass() == String.class) {
			return ((String) obj).equals(tipo);
 		}
 		return super.equals(obj);
 	}
 }
