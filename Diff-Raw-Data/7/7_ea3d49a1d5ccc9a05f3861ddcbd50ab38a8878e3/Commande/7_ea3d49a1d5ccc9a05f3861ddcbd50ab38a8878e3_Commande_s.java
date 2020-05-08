 package fr.ybonnel.codestory.path.jajascript;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 public class Commande {
 
     @JsonProperty("VOL")
     private String nomVol;
     @JsonProperty("DEPART")
     private int heureDepart;
     @JsonProperty("DUREE")
     private int tempsVol;
     @JsonProperty("PRIX")
     private int prix;
 
     public Commande() {
     }
 
     public Commande(String nomVol, int heureDepart, int tempsVol, int prix) {
         this.nomVol = nomVol;
         this.heureDepart = heureDepart;
         this.tempsVol = tempsVol;
         this.prix = prix;
     }
 
     public boolean compatibleWith(Commande other) {
        return (other.getHeureFin() <= this.heureDepart
                || other.heureDepart >= getHeureFin());
     }
 
     private int getHeureFin() {
        return this.heureDepart + this.tempsVol;
     }
 
     public String getNomVol() {
         return nomVol;
     }
 
     public int getHeureDepart() {
         return heureDepart;
     }
 
     public int getTempsVol() {
         return tempsVol;
     }
 
     public int getPrix() {
         return prix;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Commande commande = (Commande) o;
 
         if (heureDepart != commande.heureDepart) return false;
         if (prix != commande.prix) return false;
         if (tempsVol != commande.tempsVol) return false;
         if (nomVol != null ? !nomVol.equals(commande.nomVol) : commande.nomVol != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = nomVol != null ? nomVol.hashCode() : 0;
         result = 31 * result + heureDepart;
         result = 31 * result + tempsVol;
         result = 31 * result + prix;
         return result;
     }
 
     @Override
     public String toString() {
         return "Commande{" +
                 "nomVol='" + nomVol + '\'' +
                 ", heureDepart=" + heureDepart +
                 ", tempsVol=" + tempsVol +
                 ", prix=" + prix +
                 '}';
     }
 }
