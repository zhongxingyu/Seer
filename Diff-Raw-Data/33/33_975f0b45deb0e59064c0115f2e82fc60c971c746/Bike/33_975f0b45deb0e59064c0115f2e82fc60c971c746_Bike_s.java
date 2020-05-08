 // Similaire aux "namespaces" en PHP
 // Correspond au nom du dossier
 package velo;
 
 // Similaire au "use" en php, dans le cadre des namespaces
 // peut être vu comme un "include"
 import java.util.Scanner;
 
 /**
  * Exemple d'implémentation (simplifiée) de calcul de paiement pour le vélo'v
  */
 class Bike {
     
     private Hour start;
     private Hour end;
     private int  age;
     
     /**
      * Méthode de base de java
      * exécutée automatiquement
      */
     public static void main(String[] args)
     {
         Bike v = new Bike();
         
         System.out.println("Vous devez payer: " + String.valueOf(v.calculate()) + " euros" );
     }
     
     /**
      * Construit la classe vélo
      * en demandant les informations à l'utilisateur
      * 
      * Note: ici, aucune véfication n'est faite par rapport à ce que l'utilisateur
      * entre, ça serait une bonne amélioration que de vérifier les données
      */
     public Bike()
     {
         Scanner sc = new Scanner(System.in);
         
         // Demande de l'heure de départ
         System.out.println("Entrez une heure de départ :");
         this.start = Hour.createHourFromString(sc.nextLine());
 
         // Demande de l'heure d'arrivée
         System.out.println("Entrez une heure d'arrivée :");
         this.end   = Hour.createHourFromString(sc.nextLine());
         
         // Demande de l'âge
         // Note: le programme va planter si l'utilisateur ne rentre pas un nombre
         System.out.println("Entrez votre âge :");
         this.age   = sc.nextInt();
     }
     
     /**
      * Calcule le prix que va payer l'utilisateur
      */
     public float calculate()
     {
         // float représente un nombre à virgule
         float res = 0.0f;
         
         if (this.start.before(new Hour(7,0))) {
             
             // Note: ne pas utiliser 01, cela serait compris comme un nombre "octal"
             // et pas "décimal" (ce qui n'est pas grave de 1 à 7)
             if (this.end.before(new Hour(7,1))) {
                 // diff retourne un entier, pour préciser que le résultat doit
                 // être un nombre à virgule on utilise des nombres à virgules
                 // dans l'opération
                 res = this.start.diff(this.end) / 60.0f * 1.25f;
             } else {
                res += this.start.diff(new Hour(7,0)) / 60.0f * 1.25f;
             }
         }
         
        if (this.end.before(new Hour(17,0))) {
             if (this.start.after(new Hour(7,0))) {
                 res = this.start.diff(this.end) / 60.0f * 1.5f;
             } else {
                 res += this.end.diff(new Hour(7,0)) / 60.0f * 1.5f;
             }
         }
         
         if (this.end.after(new Hour(17,0))) {
             if (this.start.after(new Hour(17,0))) {
                 res = this.start.diff(this.end) / 60.0f * 1.25f;
             } else {
                 res += this.start.diff(new Hour(17,0)) / 60.0f * 1.25f;
             }
         }
         
         // La promotion pour les jeunes:
         if (this.age < 30 || this.age > 75) {
             res = res * 0.8f;
         }
         
         return res;
     }
 }
 
