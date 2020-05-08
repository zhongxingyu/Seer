 
 import java.util.ArrayList;
 
 
 
 /**
  *
  * @author victor
  */
 public class Tipo {
     
     String tipo;
     int dimension;
     Tipo tipobase;
     boolean array;
     
     public Tipo(String _tipo){
         tipo = _tipo;
         dimension = 0;
         tipobase = null;
         array = false;
     }
     
     public Tipo(String _tipo, Tipo _tipobase){
         tipo = _tipo;
         dimension = -1;
         tipobase = _tipobase;
         array = true;
     }
     
     public Tipo(Tipo _anterior){
         tipo = _anterior.tipo;
         dimension = _anterior.dimension;
         tipobase = _anterior.tipobase;
     }
     
     public String getTipo(){
 		return tipo;
 	}
 	
     public Tipo getTipoBase(){
 		return tipobase;
 	}
 	
 	public String getTipoFinal(){
 		if(array){
 			return tipobase.getTipoFinal();
 		} else{
 			return tipo;
 		}
 	}
 	
     public int getDimension(){
 		return dimension;
 	}
 	
     public int getDimensionTotal(){
 		if(array){
 			return dimension*tipobase.getDimensionTotal();
 		}else{
 			return 1;
 		}
 	}
 	
     public void setDimension(int _dim){
		if(_dim > 0){
			dimension = _dim;
		}else{
			// throw Error 8
		}
 	}
     public void setDimension(String _dim){
		setDimension(Integer.parseInt(_dim));
 	}
 	
 	@Override
 	public String toString(){
 		if (array){
 			return tipobase.toStringSoloTipo() + "[]";
 			//return "[] -> " + tipobase.toString();
 		}else{
 			return tipo;
 		}
 		
 	}
 	
 	private String toStringSoloTipo(){
 		if (array){
 			return tipobase.toStringSoloTipo();
 		}else{
 			return tipo;
 		}
 		
 	}
 	
 }
