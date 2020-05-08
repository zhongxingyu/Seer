 
 public enum ExpCurve {
     SLOW, MEDIUM_SLOW, MEDIUM, FAST, NONE;
     
     public static int expToNextLevel(ExpCurve curve, int currLevel, int totalExp) {
         if (curve == NONE)
             return 0;
         
         int n = currLevel + 1; //next level
        int nextExp = lowestExpForLevel(curve, n);
         
         return nextExp - totalExp;
     }
     
     public static int lowestExpForLevel(ExpCurve curve, int level) {
         int n = level;
         int exp = 0;
         switch(curve) {
         case SLOW:
             exp = 5*n*n*n/4;
            break;
         case MEDIUM_SLOW:
            exp = 6*n*n*n/5 - 15*n*n + 100*n - 140;
            break;
         case MEDIUM:
             exp = n*n*n;
            break;
         case FAST:
             exp = 4*n*n*n/5;
            break;
         default:
             break;
         }
         return exp;
     }
 }
