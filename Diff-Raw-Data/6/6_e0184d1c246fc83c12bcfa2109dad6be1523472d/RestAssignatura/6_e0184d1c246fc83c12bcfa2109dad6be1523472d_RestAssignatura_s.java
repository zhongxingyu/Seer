 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 import java.util.ArrayList;
 
 /**
  *
  * @author Joan Pol
  */
 public class RestAssignatura extends Restriccio {
        private Assignatura assignatura;
        private int grup;
        private String dia;
        private int hora;
 
     public RestAssignatura(){
         super(3);
         assignatura = null;
         grup = -1;
         dia = null;
         hora = -1;
     }
     public RestAssignatura(Assignatura assignatura, int grup, String dia, int hora){
         super(3);
         this.assignatura = assignatura;
         this.grup = grup;
         this.dia = dia;
         this.hora = hora;
     }
     public void setAssignatura(Assignatura assignatura){
         this.assignatura = assignatura;
     }
     public void setGrup(int grup){
         this.grup = grup;
     }
     public void setDia(String dia){
         this.dia = dia;
     }
     public void setHora(int hora){
         this.hora = hora;
     }
     public Assignatura getAssignatura(){
         return assignatura;
     }
     public int getGrup(){
         return grup;
     }
     public String getDia(){
         return dia;
     }
     public int getHora(){
         return hora;
     }
     public boolean everyday(){
         if(dia == null) return true;
         else return false;
     }
     public boolean everthour(){
         if(hora == -1) return true;
         else return false;
     }
     
     @Override
     public boolean CompleixRes(){return false;}
     
     
      public boolean esPotAfegir(CjtRestAssignatura cjtResAssig,CjtRestGrupSessio cjtResGS) {
          ArrayList<Restriccio> llista = new ArrayList();
          llista = cjtResAssig.getCjtRes();
          int size = llista.size();
          for(int i = 0; i < size; ++i){
              Restriccio res = llista.get(i);
              RestAssignatura resdw = (RestAssignatura) res;
              if(resdw.getAssignatura().equals(this.assignatura) && resdw.getGrup
                      () == this.grup && resdw.getDia().equals(this.dia) && 
                      resdw.getHora() == this.hora) return false;
          }
          llista = cjtResGS.getCjtRes();
          size = llista.size();
          for(int i = 0; i < size; ++i){
              Restriccio res = llista.get(i);
              RestGrupSessio resdw = (RestGrupSessio) res;
             if(resdw.getAssignatura().equals(this.assignatura) && resdw.getGrup
                     () == this.grup && resdw.getHora() == this.hora) return false;
          }
          return true;
     }
     
     public boolean compleixResHora(Assignatura assignatura, int grup, int hora) {
         boolean comp = true;
         if (this.assignatura.equals(assignatura) && this.grup == grup) {
             if(this.hora == hora) comp = false;
         }
         return comp;
     }
     public boolean compleixResDia(Assignatura assignatura, int grup, String dia){
         boolean comp = true;
         if(this.assignatura == assignatura && this.grup == grup){
            if(this.dia == dia) comp = false;
         }
         return comp;
     }
     
     public boolean CompleixResDiaHora(Assignatura assignatura, int grup, String dia,int hora){
         if(compleixResHora(assignatura, grup, hora) && compleixResDia(assignatura, grup, dia)) return false;
         else return true;
    }
 
     public boolean compleixRes(Clausula c, ClausulaNom cn) {
         String d = cn.getDia();
         Integer h = cn.getHora();
         int g = c.getGrup();
         if(h !=-1 && dia != null){
             if (!CompleixResDiaHora(c.getAssignatura(), g, d, h)) return false;
         }
         else if(dia!=null){
             if (!compleixResDia(c.getAssignatura(), g, d)) return false;
         }
         else if(h != -1){
             if (!compleixResHora(c.getAssignatura(), g, h)) return false;
         }
         return true;
     }
     
 }
