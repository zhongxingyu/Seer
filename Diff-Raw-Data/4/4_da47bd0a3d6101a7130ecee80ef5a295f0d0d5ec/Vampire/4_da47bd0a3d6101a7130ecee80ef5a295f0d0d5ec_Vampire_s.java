 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package VampireWarGame;
 
 /**
  *
  * @author drako
  * 
  */
 public class Vampire extends Fichas{
    protected int lifePoints;
    protected int damagePoints;
    protected int cantMovientos;
     
     public Vampire (String nombre, String col){
         super(nombre,col);
     }
     
     @Override
     public void setCantLP(int lp){
         lifePoints = lp;
     }
     
     @Override
     public void setCantMovimientos(int cant){
         lifePoints = cant;
     }
     
     //FUNCIÓN PARA OBETNER LA DISTANCIA A QUE ESTA DE LA OTRA CASILLA
     //PARA ASI HACER LA FUNCION DE ATAQUE
     public int getDistancia(int distancia){
         return 0;
     }
     
     /**
      * 
      * @param codFichaContraria
      * @Param Lista de codigos de fichas:
      *        1 Hombre Lobo
      *        2 Vampiro
      *        3 Muerte
      *        4 Zombie
      * @return el daño total que le hace a la ficha contraria, este valo
      *         se lo restamos a los LP's de la ficha atacada
      */
     @Override
     public int setDamagePoints(int codFichaContraria){
         switch (codFichaContraria){
             case 1:
                 return -3;
             case 2:
                 return -2;
             case 3:
                 return -1;
             case 4:
                 return -2;
             default: return 0;     
         } 
     }
 }
