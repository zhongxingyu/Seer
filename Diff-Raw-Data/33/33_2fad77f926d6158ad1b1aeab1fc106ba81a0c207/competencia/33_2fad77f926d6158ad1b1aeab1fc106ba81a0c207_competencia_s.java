 //Pedro Andres Rodriguez Leon 625234
 
 class competencia
 {
 	public static void main(String[] args){
	
 		//Ingresar cantidad de competidores
 		System.out.println("ingrese la cantidad de competidores: ");
 		int n=Integer.parseInt(System.console().readLine());
		
 		//valores donde se almacenaran lo  mejores tiempos
 		int uno=0;
 		int dos=0;
 		double aux=999999999;
		
		
 		//inicialización de vectores
 		String[] nombre = new String[n];
 		double[] tiempo = new double[n];
		
 		//introducir nombres
 		for(int i=0;i<n;i++){
 			System.out.println("ingrese el nombre del competidor "+(i+1)+(": "));
 			nombre[i]=System.console().readLine();
 		}
 
 		//introducir tiempos
 		for(int i=0;i<n;i++){
 			System.out.println("ingrese el tiempo del competidor: "+(i+1)+", "+nombre[i]);
 			tiempo[i]=Double.parseDouble(System.console().readLine());
 		}
			
	
 		//busqueda de los dos mejores tiempos}
 
 		for(int i=0;i<n;i++){
 			if(tiempo[i]<aux){
 			aux=tiempo[i];
 			dos=uno;
 			uno=i;
			}
 		}
		
 		//resultados
 		System.out.println("El mejor competidor fue: "+nombre[uno]+" con un tiempo de: "+tiempo[uno]);
 		System.out.println("El segundo mejor competidor fue: "+nombre[dos]+" con un tiempo de: "+tiempo[dos]);
 	}
}
