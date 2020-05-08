 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.domain;
 
 import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
 
 /**
  *
  * @author tonykova
  */
 @Entity
 public class Asunto implements Serializable{
     @Column(name = "KOKO")
     private int koko;
     @Column(name = "POSTINUMERO")
     private String postinumero;
     @Column(name = "KAUPUNKI")
     private String kaupunki;
     @Id
     private String osoite;
     @Column(name = "VUOSI")
     private String vuosi;
     @Column(name = "KUNTO")
     private String kunto;
     @ManyToOne
     private Henkilo henkilo;
     public Henkilo getHenkilo() {
         return henkilo;
     }
 
     public void setHenkilo(Henkilo henkilo) {
         this.henkilo = henkilo;
     }
     
     public String getKaupunki() {
         return kaupunki;
     }
 
     public int getKoko() {
         return koko;
     }
 
     public String getKunto() {
         return kunto;
     }
 
     public String getOsoite() {
         return osoite;
     }
 
     public String getPostinumero() {
         return postinumero;
     }
 
     public String getVuosi() {
         return vuosi;
     }
 
     public void setKaupunki(String kaupunki) {
         this.kaupunki = kaupunki;
     }
 
     public void setKoko(int koko) {
         this.koko = koko;
     }
 
     public void setKunto(String kunto) {
         this.kunto = kunto;
     }
 
     public void setOsoite(String osoite) {
         this.osoite = osoite;
     }
 
     public void setPostinumero(String postinumero) {
         this.postinumero = postinumero;
     }
 
     public void setVuosi(String vuosi) {
         this.vuosi = vuosi;
     }
     
     
 }
