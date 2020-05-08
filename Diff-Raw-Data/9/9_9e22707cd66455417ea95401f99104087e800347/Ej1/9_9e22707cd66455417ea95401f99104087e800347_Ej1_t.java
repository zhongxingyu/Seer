 package Ej1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.PriorityQueue;
 import java.util.StringTokenizer;
 
 public class Ej1 {
 
 	public static Integer buscarPrecio(Mapa mapa){
 		
 		Comparator<Tramo> comparator = new TramoComparator();
 		PriorityQueue<Tramo> heap = new PriorityQueue<Tramo>(11, comparator);
 		
 		mapa.origen.estado = true;
 		agregarVecinos(heap, mapa.origen.vecinos);
 		Tramo actual;
 		while (mapa.destino.estado == false){
 			actual = heap.poll();
 			actual.hasta.estado = true;
 			actual.hasta.peso =  Math.min(actual.peso, actual.desde.peso);
 			agregarVecinos(heap, actual.hasta.vecinos);
 		}
 
 		return mapa.destino.peso;
 	}
 	
 	public static void agregarVecinos(PriorityQueue<Tramo> heap, List<Tramo> vecinos){
 		ListIterator<Tramo> it = vecinos.listIterator();
 		while (it.hasNext()){
			Tramo t = it.next();
			if (t.hasta.estado == true){
				continue;
			}else{
				heap.add(t);
			}
 		}
 		
 	}
 	
 	public static void resolverFile(String file) throws IOException{
 		/*
 		 * Esta funcion toma el path del archivo de entrada, procesa los datos y llama
 		 * a la funcion que resuelve el problema. Luego escribe los resultados en 
 		 * un archivo de salida en la carpeta root del proyecto
 		 */
 
 			BufferedReader reader = new BufferedReader(new FileReader(file)); // Creo el buffer a llenar
 			String linea; // Creo el string a utilizar (donde se van a guardar las cosas del buffer)
 			BufferedWriter os = new BufferedWriter( new FileWriter( "Tp2Ej1.out" ) );
 
 			StringTokenizer nombre;
 			int res;
 
 			// Leo el archivo
 			while ((linea = reader.readLine()) != null){ // mientras que hayan mas lineas que leer
 				String p, q, c1, c2, n;
 				c1 = "c1";
 				c2 = "c2";
 				n = "0";
 				
 				// leo la primera linea
 				nombre = new StringTokenizer( linea, " ");
 				p = nombre.nextToken();			// Paso el primer tokenizer, el p
 				q = nombre.nextToken();			// Paso el segundo tokenizer, el q
 				Mapa mapa  = new Mapa(p,q);
 				
 				// leo la segunda linea
 				linea = reader.readLine();
 				StringTokenizer tupla = new StringTokenizer(linea, ";"); // Este token separa pares de amigos
 				while (tupla.hasMoreTokens()){
 					nombre = new StringTokenizer(tupla.nextToken(), " ");
 					c1 = nombre.nextToken();
 					c2 = nombre.nextToken();
 					n = nombre.nextToken();
 					int temp = Integer.parseInt(n);
 					Integer peso = temp;
 					mapa.agregar(c1,c2,peso);
 				}
 
 				res = buscarPrecio(mapa);
 				
 				Integer res2 = res;
 				os.append(res2.toString());
 				os.append("\n");
 			}
 			reader.close();
 			os.close();
 	}
 	
 
 }
