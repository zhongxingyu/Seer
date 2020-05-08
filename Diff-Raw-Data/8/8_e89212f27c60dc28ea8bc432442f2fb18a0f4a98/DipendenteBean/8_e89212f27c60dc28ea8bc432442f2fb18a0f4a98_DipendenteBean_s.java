 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import ejb.*;
 import entities.*;
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 import javax.ejb.EJB;
 
 /**
  *
  * @author Balordo
  */
 public class DipendenteBean implements Serializable{
     
     private static final long serialVersionUID = 1L;
     
     private Dipendente dipendente;
     private Cliente cliente;
     private LinkedList<Dipendente> dipList;
     private boolean rem,aggiunta, esistenti=true;
     
     @EJB
     SalarioFacadeLocal salBean;
     
     @EJB
     MansioneFacadeLocal manBean;
     
     @EJB
     DipendenteFacadeLocal dipBean;
     
     @EJB
     ProgettoFacadeLocal progettoBean;
 
     @EJB
     ContrattoFacadeLocal contrattoBean;
 
     public Cliente getCliente() {
         return cliente;
     }
 
     public void setCliente(Cliente cliente) {
         this.cliente = cliente;
     }
 
     public Dipendente getDipendente() {
         return dipendente;
     }
 
     public void setDipendente(Dipendente dipendente) {
         this.dipendente = dipendente;
     }
     
     public boolean isAggiunta() {
         return aggiunta;
     }
 
     public void setAggiunta(boolean aggiunta) {
         this.aggiunta = aggiunta;
     }

     public boolean isEsistenti() {
         return esistenti;
     }
 
     public void setEsistenti(boolean esistenti) {
         this.esistenti = esistenti;
     }
     
     public String dettagli(Integer id){
         dipendente = dipBean.findByID(id);
         return "visualizza";
     }
 
     public LinkedList<Dipendente> getDipList() {
         return dipList;
     }
 
     public void setDipList(LinkedList<Dipendente> dipList) {
         this.dipList = dipList;
     }
        
     public String aggiungi(){        
         aggiunta = true;
         dipendente = new Dipendente();
         dipendente.setMansione(new Mansione());
         dipendente.setSalario(new Salario());
         dipendente.setLogin(new Login());       
         return "aggiungi";
     }
     
     public boolean esisteAmministratore()
     {
         Mansione mansione = manBean.findByMansione("Amministratore");
 
         if(mansione == null){
             manBean.create(new Mansione("Amministratore"));
             mansione = manBean.findByMansione("Amministratore");
         }
 
         if(dipBean.findByMansione(mansione).isEmpty())
             return false;
 
         return true;
     }
 
     public String registraAmministratore(){
         Mansione mansione = manBean.findByMansione("Amministratore");
         dipendente = new Dipendente();
         dipList = new LinkedList<Dipendente>();
         dipList.add(dipendente);
         dipendente.setMansione(mansione);
         dipendente.setSalario(new Salario());
         dipendente.setLogin(new Login());
         return "registra";
     }
 
     public String aggiungiAmministratore(){
        dipendente.getLogin().setDipendente(dipendente);
        dipBean.create(dipendente);
        return "back";
     }
     
     public String clear(){
         esistenti = true;
         aggiunta = false;        
         return "back";
     }
     
     public List<Mansione> mansioni(){
         List<Mansione> lista = manBean.findAll();
         Mansione mansione = manBean.findByMansione("Amministratore");
         lista.remove(mansione);
         return lista;
     }
 
     public String seleziona(boolean esistenti){
         this.esistenti = esistenti;
         return "loop";
     }
 
     public List<Progetto> progetti(){
         return progettoBean.findByIngegnere(dipendente);
     }
 
     public List<Contratto> contratti(){
         return contrattoBean.findByAddetto(dipendente);
     }
     
     public String preRimozione()
     {
         rem=true;
         return "loop";
     }
        
     public String rimuovi(){
       dipBean.remove(dipendente);
       rem=false;
       return "back";
     }
 
     public boolean isRem() {
         return rem;
     }
 
     public void setRem(boolean rem) {
         this.rem = rem;
     }
     
     public String applica()
     {
         String nome = dipendente.getMansione().getMansione();
         Mansione manEsistente = manBean.findByMansione(nome);
         
         if(manEsistente == null)
         {
             manBean.create(new Mansione(nome));
             manEsistente = manBean.findByMansione(nome);
         }
 
         dipendente.setMansione(manEsistente);
 
         if(aggiunta)
         {
             dipendente.getLogin().setDipendente(dipendente);
             dipBean.create(dipendente);
         }
 
         else
             dipBean.edit(dipendente);
 
         esistenti = true;
         aggiunta = false;
                 
         return "visualizza";
     }
 }
