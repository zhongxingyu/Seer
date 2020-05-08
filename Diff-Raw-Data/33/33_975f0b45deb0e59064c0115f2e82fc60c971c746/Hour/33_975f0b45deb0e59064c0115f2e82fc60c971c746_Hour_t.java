 package velo;
 
 /**
  * Classe permettant de simplifier le traitement
  * des heures
  * 
  * Grace à cette classe, la classe "Bike" sera beaucoup simplifiée
  */
 class Hour {
     
     /**
      * L'heure (un nombre entier)
      * @var int hour
      */
     private int hour;
     
     /**
      * Les minutes (un nombre entier)
      * @var int minute
      */
     private int minute;
     
     /**
      * Méthode statique qui permet d'instancier
      * une "Hour" à l'aide d'une chaîne de caractères
      * 
      * @param String hour Chaîne de caractères sous la forme "8h15"
      */
     public static Hour createHourFromString(String hour)
     {
         // On coupe la chaîne de caractères en deux dans un tableau
         // de chaînes de caractères.
         // On utilise la méthode split dont la doc est disponible ici:
         // http://docs.oracle.com/javase/7/docs/api/java/lang/String.html#split%28java.lang.String%29
         String[] hourSplitted = hour.split("h");
         
         // La méthode static Integer.parseInt() permet de transformer les
         // chaînes de caractères en nombres
         return new Hour(
             Integer.parseInt(hourSplitted[0]),
             Integer.parseInt(hourSplitted[1])
         );
     }
     
     /**
      * Le constructeur de la classe
      * il initialise les variables contenues
      * 
      * @param int hour Un nombre représentant l'heure
      * @param int minute Un nombre représentant les minutes
      */
     public Hour(int hour, int minute)
     {
         this.hour   = hour;
         this.minute = minute;
     }
     
     /**
      * Calcule la différence entre l'heure courante et l'heure passée
      * en paramètres
      * 
      * @param Hour otherHour
      * @return int toujours positif représentant la différence entre les deux heures en minutes
      */
     public int diff(Hour otherHour)
     {
         // On calcule la diff directement en minutes, c'est plus simple
         int currentMinutes = this.hour * 60 + this.minute,
            otherMinutes   = otherHour.hour * 60 + otherHour.minute;
        
        //System.out.println("Heure1 : " + this.toString() + "; autre heure: " + otherHour.toString());
         
         return Math.abs(currentMinutes - otherMinutes);
     }
     
     /**
      * Retourne "vrai" si l'heure précède l'heure donnée en paramètres
      * 
      * @param Hour otherHour
      * @return boolean
      */
     public boolean before(Hour otherHour)
     {
         // Une condition retourne toujours "true" ou "false"
         // il est donc inutile de faire un if
         return
             this.hour < otherHour.hour ||
             (this.hour == otherHour.hour && this.minute < otherHour.minute)
         ;
     }
     
     
     /**
      * Retourne "vrai" si l'heure suit l'heure donnée en paramètres
      * 
      * @param Hour otherHour
      * @return boolean
      */
     public boolean after(Hour otherHour)
     {
         return
             this.hour > otherHour.hour ||
             (this.hour == otherHour.hour && this.minute > otherHour.minute)
         ;
     }
    
    /**
     * Méthode classique en java dont hérite tous les objets (de la classe "Object")
     * qui permet d'écrire un objet sous forme de chaîne
     * cf doc http://docs.oracle.com/javase/6/docs/api/java/lang/Object.html#toString()
     * 
     * Une meilleure méthode serait de vérifier qu'il y a bien deux nombres et d'ajouter
     * le zéro au début des nombres (là ça affiche "6h2" pour "6h02", détail.
     * 
     * @return String
     */
    public String toString()
    {
        return this.hour + "h" + this.minute;
    }
 }
