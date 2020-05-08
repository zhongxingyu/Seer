 
 /**
  *
  * @author victor
  */
 public class Error_21 extends Exception{
     
     String coderror = "Error 21 (";
     String mensaje1 = "' no esta definido";
     String lexema;
     int fila,columna;
     
     public Error_21(String tipo, String nombre, int args, int _fila, int _columna){
         lexema= tipo + "." + nombre+"(";
         if(args > 0){
            lexema += "double";
         }
         for(int i = 1; i < args;i++){
            lexema += ",double";
         }
         lexema += ")";
         fila = _fila;
         columna = _columna;
     }
     
     public void setFilaColumna(int _fila, int _columna){
 		fila = _fila;
 		columna = _columna;
 	}
     
     @Override
     public String toString(){
         return coderror + fila + "," + (columna+1) + "): el metodo '" + lexema + mensaje1;
     }
     
     
 }
