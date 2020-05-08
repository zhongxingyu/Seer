 package motorRedNeuronal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**La clase Red Neuronal crea e interconecta todos los elementos de la red, la hace calcular y entrenarse, y guarda y carga su estado.*/
 public class RedNeuronal
 {
 	/**Una lista de listas de {@link INeurona}s. Cada lista representa una capa.
 	 * La primera lista es de {@link ElementoEntrada} y la ltima de {@link ElementoSalida}.*/
 	private List <List<INeurona>> capas = new ArrayList<List<INeurona>>();
 	
 	public RedNeuronal(int [] desCapas, float fCorreccion, float momento, float pesoMin, float pesoMax, IFuncionCalculo funcion)
 	{
 		//1. Comprobar que al menos haya dos elementos en el vector de descripcin de capas
 		if (desCapas.length <2)
 			throw new IllegalArgumentException("El array descriptor de la red neuronal tiene menos de dos elementos.");
 		else if(desCapas[0]<1)
 			throw new IllegalArgumentException("Tiene que haber al menos un dato de entrada.");
 		else if (desCapas[1]<1)
 			throw new IllegalArgumentException("Tiene que haber al menos un dato de salida.");
 		
 		//2. Crear y llenar la lista de elementos de entrada
 		List <INeurona> listaEntradas = new ArrayList<INeurona>();
 		for(int i = 0; i< desCapas[0];i++)
 		{
 			listaEntradas.add(new ElementoEntrada());
 		}
 		capas.add(listaEntradas);
 		
 		//3. Ir creando las capas
 		//Por cada capa:
 		for (int i = 1; i< desCapas.length; i++)
 		{
 			//Ir creando una capa nueva (lista)
 			List <INeurona> listaPerceptrones = new ArrayList<INeurona>();
 			for(int n = 1; n<=desCapas[i];n++)
 			{
 				//Llenarla de perceptrones
 				INeurona p = new Perceptron(fCorreccion, momento, pesoMin, pesoMax, funcion);
 				listaPerceptrones.add(p);
 				
 				//Y asociarlas a la capa anterior:
 				Iterator <INeurona> it  = capas.get(capas.size()-1).iterator();
 				while(it.hasNext())
 				{
 					INeurona na = it.next();
 					p.addElementoEntrante(na);
 					na.addElementoSaliente(p);
 				}
 
 			}
 		//Aadir la lista de perceptrones a la lista de capas.
 		capas.add(listaPerceptrones);
 		}
 		
 		//Aadir la lista de Elementos de Salida; Un elemento por cada neurona.
 		List <INeurona> listaSalidas = new ArrayList<INeurona>();
 		Iterator <INeurona> it  = capas.get(capas.size()-1).iterator();
 		while(it.hasNext())
 		{
 			INeurona na = it.next();
 			
 			INeurona s = new ElementoSalida();
 			listaSalidas.add(s);
 			
 			na.addElementoSaliente(s);
 			s.addElementoEntrante(na);
 		}
 		capas.add(listaSalidas);
 		
 	}
 
 	/**La funcin calcular recive un array de float y le pasa cada elemento a cada una de las entradas de la red. Devuelve el resultado.*/
 	public float [] calcular(float[] in)
 	{
 		// Comprobar que el parmetro tiene tantos elementos como la entrada a la red.
 		if(in.length != capas.get(0).size())
 			throw new IllegalArgumentException("El tamao de la entrada es diferente al de los datos pasados.");
 		
 		float [] out = new float[capas.get(capas.size()-1).size()];
 		
 		//Recorrer las entradas pasndole el dato correspondiente
 		List <INeurona> lEntrada = capas.get(0);
 		
 		for (int i = 0; i< in.length; i++)
 			lEntrada.get(i).calcular(in[i], null);
 		
 		//Recorrer las salidas recogiendo el dato correspondiente
 		List <INeurona> lSalida = capas.get(capas.size()-1);
 		
 		for (int i = 0; i< out.length; i++)
 			out[i]=((ElementoSalida)lSalida.get(i)).ultimaSalida;
 		
 		return out;
 	}
 	
 	/**Entrena la red haciendo calculando una entrada y aplicando retroprogramacion con su resultado*/
 	public void entrenar(float[]in, float[]t)
 	{
 		//1. Comprobar el tamao del vector de salida
 		if(t.length != capas.get(capas.size()-1).size())
 			throw new IllegalArgumentException("El tamao del resultado es diferente al del tamao de salida de la red.");
 	
 		//2.Calcular el resultado de la red
 		float [] res = calcular(in);
 		
		//3. Llamar la retroprogramacion en todas las salidas//Calcular delta de neurona de salida: res*(1-res)*(t-res)
 		List <INeurona> lSalida = capas.get(capas.size()-1);
 
 			
 		for (int i = 0; i< t.length; i++)
			lSalida.get(i).retropropagar(res[i]*(1-res[i])*(t[i]-res[i]), null);
 	}
 	
 	public String toString()
 	{
 		//Imprimir las entradas
 		String sal = "Entradas: "+capas.get(0).size()+"\n";
 		
 		//Imprimir las capas
 		for(int i = 1; i < capas.size()-1;i++)
 		{
 			sal+="\nCapa nmero "+i+":\n";
 					
 			Iterator <INeurona> it = capas.get(i).iterator();
 			
 			while(it.hasNext())
 			{
 				INeurona n = it.next();
 				sal+=n.toString()+"\t";
 			}
 			sal+="\n";
 		}
 		
 		//Imprimir las salidas
 		sal+="\nSalidas:\n[";
 		Iterator <INeurona> it = capas.get(capas.size()-1).iterator();
 		
 		while (it.hasNext())
 		{
 			ElementoSalida e = (ElementoSalida)it.next();
 			sal+=e.ultimaSalida+" ";
 		}
 		sal+="]\n";
 		
 		return sal;
 	}
 	
 	public String guardarPesosString()
 	{
 		String sal = "";
 		
 		//Recorrer con un for para que el cdigo quede ms claro -No hay que recorrer ni la ltima ni la primera capa,
 		//que son de elementos auxiliares.
 		for (int i = 1; i < capas.size()-1; i++)
 		{
 			//Recorrer cada capa
 			Iterator <INeurona> itCapa = capas.get(i).iterator();
 			while (itCapa.hasNext())
 			{
 				Perceptron p = (Perceptron)itCapa.next();
 				sal += p.getPesosString()+"@"; //* entre neuronas
 			}
 			
 			sal += "\n"; //Salto de lnea entre capas
 		}
 		
 		return sal;
 	}
 	
 	public void cargarPesos(String s)
 	{
 		//Separar en capas
 		String [] capasS = s.split("\n", 0);
 		
 		//Comprobar que en la red hay las mismas capas que en el parmetro
 		if(capas.size() != capasS.length +2)
 			throw new IllegalArgumentException("En el String hay descritas un nmero diferente de capas que en la red neuronal");
 		
 		for (int i = 0 ;i<capasS.length; i++)
 			{
 				//Separar en perceptrones
 				String []perceptronesS = capasS[i].split("@",0);
 				
 				//Comprobar que en la capa hay los mismos perceptrones que en el String
 				
 				if(capas.get(i+1).size() != perceptronesS.length)
 					throw new IllegalArgumentException("En el String hay descritas un nmero diferente de capas que en la red neuronal. La red neuronal ha podido quedar corrupta.");
 			
 				for (int n = 0; n< perceptronesS.length; n++)
 				{
 					((Perceptron)capas.get(i+1).get(n)).setPesos(perceptronesS[n]);
 				}
 			
 			}
 	}
 }
