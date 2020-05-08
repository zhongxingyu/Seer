 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 import java.util.ArrayList;
 
 /**
  *
  * @author Daniel
  */
 
 //Forzar una aula a no poderse usar un dia/hora
 
 class RestriccioAula extends Restriccio{
 
     private Aula aula;
     private Integer hora;
     private String dia;
 
     //Constructora por defecto:
     public RestriccioAula() {
         super (5); //ID PROVISIONAL
     }
 
     /**
      * @pre: Si hora es null => Tot el dia.
      *
      * @param aula Es l'aula que no podra ser usada.
      * @param hora l'hora en que l'aula que no podra ser usada.
      * @param dia el dia en que l'aula no podra ser usada.
      */
     public RestriccioAula(Aula aula, Integer hora, String dia) {
         super(5);
         this.aula = aula;
         this.hora = hora;
         this.dia = dia;
     }
 
     public RestriccioAula(Aula aula, String dia) { //UNA AULA NO PODRA ESTAR UNA DIA ENTERO.
         super(1003);
         this.aula = aula;
         this.hora = null;
         this.dia = dia;
     }
 
     //Getters y Setters
     public Aula getAula() {
         return aula;
     }
 
     public void setAula(Aula aula) {
         this.aula = aula;
     }
 
     public int getHora() {
         return hora;
     }
 
     public void setHora(int hora) {
         this.hora = hora;
     }
 
     public String getDia() {
         return dia;
     }
 
     public void setDia(String dia) {
         this.dia = dia;
     }
 
     @Override
     public boolean CompleixRes() {return false;}
 
     /**
      *
      * @param cjtRestriccioAula Conjunt al que afeigir la noestra restriccio y en el que es poden produir incoherencies.
      * @param CjtRestGrupoAula Conjunto que puede producir incoherencias con nuestra restriccion.
      * @param CjtRestGrupoSesion Conjunto que puede producir incoherencias con nuestra restriccion.
      * @return true si es pot afeigir al conjunt de restriccions del tipus o false si no es pot perque provoca alguna incoherencia.
      */
 
     public boolean esPotAfegir(CjtRestriccioAula cjtRestAula, CjtRestGrupoAula cjtRestGrupoAula, CjtRestGrupSessio cjtRestGrupSesio) {
         boolean es_pot = true; //Solo sera false cuando se produzca alguna incoherencia!
         //Si un grupo esta obligado a estar en una aula que es la que esta prohibida usarse un dia/hora... Y ese grupo debe hacer clase
         //En esa aula en las horas en que esta prohibida ==> SE PRODUCE INCOHERENCIA!
         
         ArrayList<RestGrupoAula> r;
         r = cjtRestGrupoAula.getRestGrupoAula();
         for (RestGrupoAula r1: r) { //Para cada restriccion Grupo-Aula
             if (r1.getAula().equals(this.aula.getNom())) { //Si a un assig+grupo le fuerzan ir a una aula
                 Integer grupo = r1.getGrup(); //Grupo forzado.
                 String ass = r1.getAssignatura(); //Assignatura forzada.
                 ArrayList<RestGrupSessio> rgs;
                 rgs = cjtRestGrupSesio.getRestriccionsGrupSesio();
                 for (RestGrupSessio r2 : rgs) { //Para cada restriccion que diga que un assig+grupo que debia ir a un dia/hora.
                     if (r2.getGrup() == grupo && r2.getAssignatura().equals(ass)) { //Si se trata del assig+grupo que debia ir a nuestra aula de la restriccion.
                         if (r2.getHora() == this.hora) es_pot = false;
                     }
                 }
             }
         }
         return es_pot;
     }
 
    public boolean compleixRes(Clausula c, ClausulaNom cn) {
        Aula aulaa = cn.getAula();
        int horaa = cn.getHora();
        String diaa = cn.getDia();
        int dur = c.getDuracio();
        boolean compleix = true;
        if (this.aula.equals(aulaa)) { //Si hablan de la aula que esta restringida
            if (this.hora != null) { //Si este aula no puede usarse un dia a una hora concreta...
                if (this.dia.equals(diaa)) {
                    if (this.hora >= horaa && this.hora < horaa+dur)
                        compleix = false; //Si se va a usar la hora que esta prohibida.
                }
            } else { //Si este aula no puede usarse un dia entero...
                if (this.dia.equals(diaa)) compleix = false;
            }
        }
        return compleix;
     }
 }
