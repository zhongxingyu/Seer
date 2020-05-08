 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.domain;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 /**
  *
  * @author tonykova
  */
 public class Lomake {
     @Pattern(regexp="^[a-zA-Z0-9äöüÄÖÜ ]*$", message="Nimi ei saa sisältää erikoismerkkejä")
     private String nimi;
     @Pattern(regexp="[0-9]{10}", message="Puhelinnumerossa pitää olla 10 numeroa")
     private String puhelinnumero;
     @Pattern(regexp=".+@.+\\.[a-z]+", message="Sähköpostiosoitteen tulee olla oikean muotoinen")
     private String sahkopostiosoite;
     private int koko;
     @Pattern(regexp="[0-9]{5}", message="Postinumeron pitää koostua viidestä numerosta")
     private String postinumero;
     @Pattern(regexp="^[a-zA-Z0-9äöüÄÖÜ]*$", message="Kaupungin nimen ei tule sisältää erikoismerkkejä")
     private String kaupunki;
    @Pattern(regexp="[a-zA-Z0-9 ÄäÖöÅå]*+", message="Osoitteen tulla olla muotoa: Osoite katunumero (rappu) (asunto)")
     private String osoite;
     @Pattern(regexp="[0-9]{4}", message="Vuosiluvun kuuluu olla neljänumeroinen")
     private String vuosi;
     private String kunto;
     
     public String getKaupunki() {
         return kaupunki;
     }
 
     public int getKoko() {
         return koko;
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
 
     public void setNimi(String nimi) {
         this.nimi = nimi;
     }
 
     public void setOsoite(String osoite) {
         this.osoite = osoite;
     }
 
     public void setPostinumero(String postinumero) {
         this.postinumero = postinumero;
     }
 
     public void setPuhelinnumero(String puhelinnumero) {
         this.puhelinnumero = puhelinnumero;
     }
 
     public void setSahkopostiosoite(String sahkopostiosoite) {
         this.sahkopostiosoite = sahkopostiosoite;
     }
 
     public void setVuosi(String vuosi) {
         this.vuosi = vuosi;
     }
 
     public String getKunto() {
         return kunto;
     }
 
     public String getNimi() {
         return nimi;
     }
 
     public String getOsoite() {
         return osoite;
     }
 
     public String getPostinumero() {
         return postinumero;
     }
 
     public String getPuhelinnumero() {
         return puhelinnumero;
     }
 
     public String getSahkopostiosoite() {
         return sahkopostiosoite;
     }
 
     public String getVuosi() {
         return vuosi;
     }
     public Henkilo teeHenkilo() {
         Henkilo henkilo = new Henkilo();
         henkilo.setNimi(this.nimi);
         henkilo.setPuhelinnumero(puhelinnumero);
         henkilo.setSahkopostiosoite(sahkopostiosoite);
         return henkilo;
     }
     public Asunto teeAsunto() {
         Asunto asunto = new Asunto();
         asunto.setKaupunki(kaupunki);
         asunto.setKoko(koko);
         asunto.setKunto(kunto);
         asunto.setOsoite(osoite);
         asunto.setPostinumero(postinumero);
         asunto.setVuosi(vuosi);
         return asunto;
     }
 }
