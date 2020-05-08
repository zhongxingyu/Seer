 package helpers;
 
 import java.util.Comparator;
 
 /**
  * a comparator for sorting by rank
  */
 class RankComparator implements Comparator
 {
    public int compare(Object o1, Object o2)
    {
       PlayerInfo info1 = (PlayerInfo) o1;
       PlayerInfo info2 = (PlayerInfo) o2;
       if (info1.getRoundWins() == info2.getRoundWins())
       {
          if (info1.getRoundByes() == info2.getRoundByes())
          {
             if (info1.getIndividualWins() == info2.getIndividualWins())
             {
               return (info1.getIndividualLosses() - info2.getIndividualLosses());
             }
             else
             {
                return (info2.getIndividualWins() - info1.getIndividualWins());
             }
          }
          else
          {
             return (info2.getRoundByes() - info1.getRoundByes());
          }
       }
       else
       {
          return (info2.getRoundWins() - info1.getRoundWins());
       }
    }
 }
