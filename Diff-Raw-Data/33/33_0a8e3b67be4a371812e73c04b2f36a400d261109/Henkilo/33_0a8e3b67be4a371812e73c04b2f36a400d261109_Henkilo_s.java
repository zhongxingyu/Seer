 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.domain;
 
 import java.io.Serializable;
 import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
 
 /**
  *
  * @author tonykova
  */
 @Entity
 public class Henkilo implements Serializable {
 
     @Id
     private String nimi;
     @Column(name = "PUHELINNUMERO")
     private String puhelinnumero;
     @Column(name = "SAHKOPOSTIOSOITE")
     private String sahkopostiosoite;
    @OneToMany
     private List<Asunto> kohteet;
 
     public void setKohteet(List<Asunto> kohteet) {
         this.kohteet = kohteet;
     }
     public List<Asunto> getKohteet() {
         return kohteet;
     }
     public void setNimi(String nimi) {
         this.nimi = nimi;
     }
 
     public void setPuhelinnumero(String puhelinnumero) {
         this.puhelinnumero = puhelinnumero;
     }
 
     public void setSahkopostiosoite(String sahkopostiosoite) {
         this.sahkopostiosoite = sahkopostiosoite;
     }
 
     public String getNimi() {
         return nimi;
     }
 
     public String getPuhelinnumero() {
         return puhelinnumero;
     }
 
     public String getSahkopostiosoite() {
         return sahkopostiosoite;
     }
     public void lisaaKohde(Asunto asunto) {
         this.kohteet.add(asunto);
     }
 }
