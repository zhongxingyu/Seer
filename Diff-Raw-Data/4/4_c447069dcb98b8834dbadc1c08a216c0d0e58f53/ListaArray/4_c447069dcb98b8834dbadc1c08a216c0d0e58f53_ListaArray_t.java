 package estructurasDeDatos;
 
 import gestionDeSentencias.Fichero;
 import java.io.IOException;
 
 /**
  * Estructura de vector:
  * Representacin como array esttico. Se redimensiona en caso de necesitar ms espacio.
  * Puede insertar mantiendo un orden ascendente de sus elementos.
  * @param <T> - Parmetro genrico que implemente la interfaz Comparable<T>
  */
 public class ListaArray<T extends Comparable<T>> implements Comparable<ListaArray<T>> {
 	
 	private final static int defaultSize = 100;
 	private final static int resizeFactor = 2;
 	
 	private Object[] datos;
 	private int capacidad;
 	private int longitud;
 
 	
 	public ListaArray() {
 		this(defaultSize);
 	}
 	
 	public ListaArray(int maxSize) {
 		capacidad = maxSize;
 		datos = new Object[capacidad];
 		longitud = 0;
 	}
 
 	
 	public boolean isEmpty() {
 		return (longitud == 0);
 	}
 
 	public int size() {
 		return longitud;
 	}
 
 	public T first() {
 		return accesoDatos(0);
 	}
 
 	public T last() {
 		return accesoDatos(longitud - 1);
 	}
 
 	public T elementAt(int posicion) {
 		if (posicion < 0 || posicion >= longitud)
 			return null;
 		return accesoDatos(posicion);
 	}
 	
 	public T elementMatch(T elemento) {
 		for( int i = 0; i < longitud; ++i )
 			if( accesoDatos(i).equals(elemento) )
 				return accesoDatos(i);
 		return null;
 	}
 
 	public void insertFirst(final T elemento) {
 		throw new UnsupportedOperationException("Sin implementar");
 	}
 	
 	public void insertLast(T elemento) {
 		if( longitud == capacidad )
 			expandir();
 		datos[longitud] = elemento;
 		longitud++;
 	}
 	
 	public void insertOrdered(T elemento) {
 		if( longitud == capacidad )
 			expandir();
 		int posicion = busquedaBinaria(elemento);
		for( int i = longitud; i > posicion; --i )
 			datos[i] = datos[i-1];
 		datos[posicion] = elemento;
		longitud++;
 	}
 
 	public T removeLast() {
 		if (longitud == 0)
 			return null;
 		return accesoDatos(--longitud);
 	}
 	
 	public Object[] toArray() {
 		return datos;
 	}
 
 	public void printToFile(String filename) {
 		try {
 			Fichero.abrir(filename, true);
 			for (int i = 0; i < longitud; ++i)
 				Fichero.escribirSentencia(accesoDatos(i).toString());
 			Fichero.cerrar();
 		} catch (IOException e) {
 			System.out.println("Error: Imposible acceder al fichero especificado.");
 		}
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	private T accesoDatos(int index) {
 		return (T) datos[index];
 	}
 	
 	private void expandir() {
 		capacidad *= resizeFactor;
 		Object[] nuevosDatos = new Object[capacidad];
 		for (int i = 0; i < longitud; ++i)
 			nuevosDatos[i] = datos[i];
 		datos = nuevosDatos;
 	}
 	
 	private int busquedaBinaria(T elemento) {
 		int menor = 0, mayor = longitud - 1, mitad, comp;
 		while( menor <= mayor ) {
 			mitad = (menor+mayor)/2;
 			comp = accesoDatos(mitad).compareTo(elemento);
 			if( comp == 0 ) {
 				return mitad + 1;
 			} else if( comp < 0 ) {
 				menor = mitad + 1;
 			} else {
 				mayor = mitad - 1;
 			}
 		}
 		return menor;
 	}
 
 	@Override
 	public int compareTo(ListaArray<T> o) {
 		return (longitud - o.longitud);
 	}
 	
 }
