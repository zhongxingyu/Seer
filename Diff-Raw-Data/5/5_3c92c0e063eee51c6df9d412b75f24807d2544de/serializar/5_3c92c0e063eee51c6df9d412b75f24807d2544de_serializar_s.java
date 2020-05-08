 /**Esta clase implementa métodos para convertir objetos Nodo a un string que
  * pueda ser escrito en un archivo y el proceso inverso*/
 import java.util.*;
 
 class Serializar{
 
 	/**Cadena que representa la versión imprimible de los objetos Nodo*/
 	private String cadena;
 
 	/**Los nodos obtenidos del string dado en el constructor*/
 	private Vector<Nodo> nodos;
 
 	/**Constructor a partir de un String
 	 * @param cadena Cadena que contiene los objetos a ser interpretados*/
 	Serializar(String cadena)
 	{
 		this.cadena=cadena;
 		limpiar();
 		this.nodos=new Vector();
 		this.des_serializar();
 	}
 
 	/**Constructor a partir de un vector de nodos
 	 * @param nodos Un vector con los nodos a ser pasados a un string*/
 	Serializar(Vector<Nodo> nodos)
 	{
 		this.nodos=nodos;
 		this.cadena=new String();
 		this.serializar();
 	}
 
 
 	/**@return Devuelve una cadena representando los objetos del vector nodo
 	 * dado en el constructor*/
 	public String Serializacion()
 	{
 		return cadena;
 	}
 
 	/**@return Devuelve un vector con los nodos que se lograron obtener de
 	 * un string dado en el constructor*/
 	public Vector<Nodo> Nodos()
 	{
 		return this.nodos;
 	}
 
 
 	/**Convierte los nodos en un string para poder escribir en un archivo
 	 * Sigue el siguiente formato:
 	 * nombre_medicamento = NOMBRE_MEDICAMENTO (Opcional)
 	 * nombre_compuesto = NOMBRE_COMPUESTO 
 	 * sintomas = sintoma1,sintoma2,sintoma3,etc
 	 *
 	 * Acepta comentarios con #*/
 	private void serializar()
 	{
 		int i, j;
 		String c;
 		Nodo nodo;
 
 		//convertimos cada elemento en un string y lo vamos agregando al
 		//string c
 		for(i=0;i<this.nodos.size();i++)
 		{
 			c="";
 			nodo=this.nodos.elementAt(i);
 			
 			if(nodo.getNombreMedicamento()!=null)
 			{
 				c+="nombre_medicamento = "+nodo.getNombreMedicamento()+"\n";
 			}
 
 			if(nodo.getNombreCompuesto()!=null)
 			{
 				c+="nombre_compuesto = "+nodo.getNombreCompuesto();
 			}
 			
 			//solo si hay síntomas los mostramos en el string
 			if(nodo.getSintomas().size()>0)
 			{
 				c+="\nsintomas = ";
 				for(j=0;j<nodo.getSintomas().size();j++)
 				{
 					//Agregamos cada síntoma al string c
 					//separados por comas
 					c+=nodo.getSintomas().elementAt(j)+",";
 				}
 			}
 
 
 			//esto es obligación: debemos separar cada bloque por un
 			//salto de linea. El primer salto de linea no se cuenta
 			//ya que es el que va al final de la linea de
 			//nombre_compuesto en el caso de que no hayan síntomas o
 			//al final de síntomas en el caso de que si hayan.
 			//El segundo salto de linea separa cada bloque de
 			//variables entre si.
 			c+="\n\n";
 
 			this.cadena+=c;
 		}
 	}
 
 	/**A partir de un string crea los nodos correspondientes y los guarda el
 	 * atributo nodos*/
 	private void des_serializar()
 	{
 		//cada bloque es un conjunto de variables que juntas crean un
 		//nodo. Esta separado por 2 saltos de linea
 		String []bloques=this.cadena.split("\n\n");
 
 		//cada linea de un bloque, osea, conjunto de variables
 		String []lineas;
 
 		//aquí vamos guardando los nodos que vamos creando a partir de
 		//las variables leídas del string
 		this.nodos=new Vector();
 		Nodo nodo;
 
 		int i, j;
 
 		for(i=0;i<bloques.length;i++)
 		{
 			//separamos las lineas, cada linea termina con un fin de
 			//linea, osea, por un "\ņ"
 			lineas=bloques[i].split("\n");
 			nodo=crear(lineas);
 			if(nodo!=null)
 			{
 				this.nodos.add(nodo);
 			}
 		}
 	}
 
 	
 	/**Crea un nodo a partir de las lineas dadas
 	 * @param lineas Un array de String de las lineas a ser interpretadas
 	 * para ser convertidas en un objeto nodo*/
 	private Nodo crear(String []lineas)
 	{
 		Nodo nodo=new Nodo();
 		String []asignacion;
 		String variable, valor;
 		int i;
 		for(i=0;i<lineas.length;i++)
 		{
 			if(lineas[i]!="")
 			{
 				//separamos las lineas por el símbolo "="
 				//cada linea contiene los datos de esta forma:
 				//VARIABLE = VALOR
 				//Al separar con split VARIABLE y VALOR quedan guardadas
 				//en un array (asignación) en el cual el primer elemento
 				//es la VARIABLE y el segundo el VALOR de la variable
 				asignacion=lineas[i].split("=");
 
 				if(asignacion.length==2) //esta linea es valida
 				{
 
 					//eliminamos los espacios en blanco al final y
 					//al principio de la cadena si es que existen
 					variable=asignacion[0].trim();
 					valor=asignacion[1].trim();
 
 
 					if(variable.equalsIgnoreCase("nombre_medicamento"))
 					{
 						//encontramos una variable
 						//nombre_medicamento, la agregamos al
 						//nodo
						nodo.setNombreMedicamento(asignacion[1]);
 
 					}else if(variable.equalsIgnoreCase("nombre_compuesto")){
 
 						//encontramos una variable
 						//nombre_compuesto, la agregamos al
 						//nodo
						nodo.setNombreCompuesto(asignacion[1]);
 
 					}else if(variable.equalsIgnoreCase("sintomas")){
 
 						//llamamos a crear_sintomas que se va a
 						//encargar de agregar los síntomas al
 						//nodo dado
 						crear_sintomas(valor, nodo);
 
 					}else{
 						//no valido
 						return null;
 					}
 				}else{
 					return null;
 				}
 			}
 		}
 
 		return nodo;
 	}
 
 	/**Lee los síntomas desde el string valor en donde están separados por
 	 * comas y las agrega al nodo dado
 	 * @param valor String que contiene a los síntomas separados por comas:
 	 * vómitos,mareos,etc
 	 * @param nodo Nodo al cual se va a agregar los síntomas*/
 	private void crear_sintomas(String valor, Nodo nodo)
 	{
 		int i;
 
 		//los valores de los síntomas están separados por una coma
 		String []valores=valor.split(",");
 
 		for(i=0;i<valores.length;i++)
 		{
 			if(valores[i]!=null)
 			{
 				//encontramos un síntoma valido, le sacamos los
 				//espacios a ambos lados y lo agregamos a nodo
 				nodo.AgregarSintoma(valores[i].trim());
 			}
 		}
 	}
 
 	/**Limpia la entrada para que se pueda interpretar correctamente*/
 	private void limpiar()
 	{
 		//buscamos cosas que no deberian existir en el string
 		//y las reemplazamos
 
 		int i;
 		int saltos_linea=0;
 		String nueva="";
 
 		//si hay 3 o mas saltos de linea seguidos los sacamos
 		for(i=0;i<this.cadena.length();i++)
 		{
 			if(this.cadena.charAt(i)=='\n')
 			{
 				saltos_linea++;
 			}else{
 				saltos_linea=0;
 			}
 
 			if(saltos_linea<=2)
 			{
 				nueva+=cadena.charAt(i);
 			}
 		}
 
 		this.cadena=nueva;
 
 	}
 }
