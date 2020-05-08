 /** @class ArbolSintomas :
 	Esta clase implementa un ?rbol binario de b?squeda (ABB) ordenado respecto a los sintomas.
 	Los siguientes parametros representan los atributos de la clase:
 	@param raiz : Snodo raiz del Arbol ABB
 */
 
 import java.util.Vector;
 
 class ArbolSintomas{
 	private Snodo raiz;
 	
 	/**Incializa el ArbolSintomas con una raiz nula*/
 	public ArbolSintomas(){
 		raiz = new Snodo();
 	}
 	
 	/**Inicializa el ArbolSintomas con una lista de snodos
 		@param snodos : Los nodos a agregar recursivamente
 	*/
 	public ArbolSintomas(Vector<Snodo> snodos){
 		raiz=new Snodo();
 		this.agregarSnodo(snodos);
 	}
 	
 	/**Agrega un nodo al arbol, si no existen Snodos o mas bien, si el nodo raiz no tiene datos asignados
 	entonces el Snodo a agregar ser? la raiz.
 	Despues de agregar el Snodola funcion AVL verificara que el arbol tenga una estructura AVL,
 	de no ser asi AVL intentara arreglarlo
 	@param snodo : Snodo que se desea agregar al Arbol ABB
 	*/
 	public void agregarSnodo(Snodo snodo){	
 		if(this.raiz.getSintoma()=="none"){
 			this.raiz.igualar(snodo);
 		}else{
 			this.raiz.igualar(this.agregarSintoma(this.raiz,snodo));
 		}
                 
 		this.raiz.igualar(AVLAS.AVLArbolSintomas(this.raiz));
 	}
 	
 	/**Agrega una serie de snodos de forma recursiva
 		@param snodos : lista de nodos a agregar
 	*/
 	public void agregarSnodo(Vector<Snodo> snodos){		
 		for(int i=0;i<snodos.size();i++)
 		{
 			this.agregarSnodo(snodos.elementAt(i));
 		}		
 		
 	}
 	
 	/**Agrega el snodo al lugar correspondiente segun un orden alfabetico dado por el nombre del sintoma,
 	Si ambos sintomas son iguales se reemplaza el actual por el nuevo
 	@param raiz : nodo actual en el que se evalua a que lado quedara el Snodo
 	@param snodo : Snodo que se desea agregar
 	*/
 	private Snodo agregarSintoma(Snodo raiz, Snodo snodo){
 		int comparacion=raiz.getSintoma().compareToIgnoreCase(snodo.getSintoma());
 		
 		if(comparacion<0){
 			if(raiz.getSnodoIzq()==null){
 				raiz.setSnodoIzq(snodo);
 			}else{
 				raiz.setSnodoIzq(agregarSintoma(raiz.getSnodoIzq(),snodo));
 			}
 		}else if(comparacion>0){
 			if(raiz.getSnodoDer()==null){
 				raiz.setSnodoDer(snodo);
 			}else{
 				raiz.setSnodoDer(agregarSintoma(raiz.getSnodoDer(),snodo));
 			}
 		}else{
 			snodo.reemplazar(raiz);		
 			raiz.limpiar();
 			return snodo;
 		}
 		
 		return raiz;
 	}
 	
 	/**Permite crear una lista (vector) con todos los snodos del arbol
 		@return Devuelve un vector de todos los snodos que se encuentran en este arbol
 	*/
 	public Vector<Snodo> VSnodos()
 	{
 		Vector<Snodo> vector=new Vector();
 		//debemos recorrer ambas partes del arbol ya que un nodo puede
 		//no tener un compuesto o puede no tener un medicamento.

		//debemos agregar el nodo raiz tambien al vector
		vector.add(this.raiz);
 		this.VSnodo(vector, this.raiz.getSnodoIzq());
 		this.VSnodo(vector, this.raiz.getSnodoDer());
 		return vector;
 	}
 
 	/**Agrega todos los snodos del árbol al vector dado como argumento
 	   @param vector : El vector que contendrá a los snodos del árbol
 	   @param raiz : La raíz inicial de un árbol que contenga snodos
 	*/
 	private void VSnodo(Vector<Snodo> vector, Snodo raiz)
 	{
 		if(raiz==null)
 		{
 			return;
 		}else{
 			if(!vector.contains(raiz))
 			{
 				vector.add(raiz);
 			}
 			this.VSnodo(vector, raiz.getSnodoIzq());
 			this.VSnodo(vector, raiz.getSnodoDer());
 		}
 	}
 
 	/**Elmina el nodo del arbol si no se encuentra lanza la excepcion ArbolSintomasNoEncontrado	   
 		@param snodo : El Snodo a eliminar del arbol*/
 	public void eliminar(Snodo snodo)
 	{
 		eliminar(this.raiz, snodo);
 	}
 	
 	/**Elimina el nodo dado del arbol
 	   @param raiz : La raiz del arbol del cual se va a eliminar el nodo
 	   @param snodo : El Snodo que se quiere eliminar del arbol
 	*/
 	private void eliminar(Snodo raiz, Snodo snodo)
 	{
 		Snodo subarbol_der = new Snodo();
                 Snodo subarbol_izq = new Snodo();
 		if(raiz!=null)
 		{
 			if(raiz.getSnodoIzq()==snodo)
 			{
 				subarbol_der.igualar(raiz.getSnodoIzq().getSnodoDer());
 				subarbol_izq.igualar(raiz.getSnodoIzq().getSnodoIzq());
 				raiz.setSnodoIzq(null);
 				//debemos volver a agregar los elementos al arbol
 				Vector<Snodo> Snodos=new Vector();
 
 				this.VSnodo(Snodos, subarbol_der);
 				this.agregarSnodo(Snodos);
 
 				Snodos=new Vector();
 
 				this.VSnodo(Snodos, subarbol_izq);
 				this.agregarSnodo(Snodos);
 
 			}else{
 				this.eliminar(raiz.getSnodoIzq(), snodo);
 			}
 
 			if(raiz.getSnodoDer()==snodo)
 			{
 				subarbol_der.igualar(raiz.getSnodoDer().getSnodoDer());
 				subarbol_izq.igualar(raiz.getSnodoDer().getSnodoIzq());
 				raiz.setSnodoDer(null);
 				//debemos volver a agregar los elementos al arbol
 				Vector<Snodo> Snodos=new Vector();
 
 				this.VSnodo(Snodos, subarbol_der);
 				this.agregarSnodo(Snodos);
 
 				Snodos=new Vector();
 
 				this.VSnodo(Snodos, subarbol_izq);
 				this.agregarSnodo(Snodos);
 			}else{
 				this.eliminar(raiz.getSnodoDer(), snodo);
 			}
 		}
 	}	
 
 	/**Busca un sintoma según la cadena dada, retornando asi el nodo encontrado, si no se encuentra se enviara un exception
 	  @param cadena : El string a buscar en el árbol ABB. Se ignoran las mayúsculas
 	  @return El nodo encontrado*/
 	public Snodo BuscarSintoma(String cadena)throws ArbolSintomasNoEncontrado{
 		if(this.raiz==null)
 		{
 			throw new ArbolSintomasNoEncontrado("No hay ningún nodo en la base de datos");
 		}else{
 			try{			
 				return this.buscarSintoma(this.raiz.getSnodoDer(), cadena);
 			}catch(ArbolSintomasNoEncontrado e){
 				System.out.println("No se encontró "+cadena+" en la base de datos");
                                 return this.raiz;
 			}
 		}
 	}
 
 	/**Busca la cadena asumiendo que es un compuesto en el árbol binario dado, si no se encuentra entonces se lanza una excepción
 		ArbolSintomasNoEncontrado
 	   @param raiz Raíz del árbol binario en donde buscar la cadena.
 	   @param cadena Un string a buscar en el árbol binario se ignoran las mayúsculas
 	*/
 	private Snodo buscarSintoma(Snodo raiz, String cadena)throws ArbolSintomasNoEncontrado
 	{
 		if(raiz==null)
 		{
 			//No encontramos el compuesto dado
 			throw new ArbolSintomasNoEncontrado("No se encontró "+cadena+" en la base de datos");
 		}else{
 			int compuesto=raiz.getSintoma().compareToIgnoreCase(cadena);
 
 			if(compuesto==0)
 			{
 				//encontramos el nodo 
 				return raiz;
 			}else{
 				//No hemos encontrado el Snodo, debemos ver por que
 				//camino nos vamos. El camino izquierdo es para los
 				//valores negativos y el derecho para los positivos
 
 				if(compuesto<0)
 				{
 					return buscarSintoma(raiz.getSnodoIzq(), cadena);
 				}else{
 					return buscarSintoma(raiz.getSnodoDer(), cadena);
 				}
 			}
 		}
 	}
 }
 
 /**@class ArbolSintomasNoEncontrado : 
 	Clase de excepción para lanzar en caso de no encontrar un Snodo
 */
 class ArbolSintomasNoEncontrado extends Exception{
 
 	ArbolSintomasNoEncontrado(String mensaje)
 	{
 		super(mensaje);
 	}
 
 	ArbolSintomasNoEncontrado()
 	{
 		super();
 	}
}
