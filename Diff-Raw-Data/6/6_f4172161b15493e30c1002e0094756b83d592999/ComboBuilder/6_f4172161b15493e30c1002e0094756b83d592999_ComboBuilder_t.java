 package net.sf.anathema.charms.character.combo;
 
 import net.disy.commons.core.predicate.IPredicate;
 import net.sf.anathema.charms.character.combo.predicate.IsComboBasic;
 import net.sf.anathema.charms.character.combo.predicate.IsExtraAction;
 import net.sf.anathema.charms.character.combo.predicate.IsNonReflexive;
 import net.sf.anathema.charms.character.combo.predicate.IsSimple;
 import net.sf.anathema.charms.data.CharmDto;
 import net.sf.anathema.charms.data.ICharmDataMap;
 import net.sf.anathema.charms.tree.ICharmId;
 
 public class ComboBuilder {
 
   private final Combo combo;
   private final ICharmDataMap charmDataMap;
 
   public ComboBuilder(Combo combo, ICharmDataMap charmDataMap) {
     this.combo = combo;
     this.charmDataMap = charmDataMap;
   }
 
   public boolean isValid(ICharmId charmId) {
     ComboCharm charm = createComboCharm(charmId);
     if (!charm.hasComboKeyword()) {
       return false;
     }
     if (combinesComboBasicWithNonReflexiveCharms(charm)) {
       return false;
     }
     if (combinesTwoExtraActionOrTwoSimpleCharms(charm)) {
       return false;
     }
     return true;
   }
   
   private boolean combinesComboBasicWithNonReflexiveCharms(ComboCharm charm) {
     boolean containsNonReflexive = contains(new IsNonReflexive());
    boolean comboBasicCharmPresent = charm.isComboBasic() || contains(new IsComboBasic());
    if (containsNonReflexive && comboBasicCharmPresent) {
       return !charm.isReflexive();
     }
     return false;
   }
   
   private boolean combinesTwoExtraActionOrTwoSimpleCharms(ComboCharm charm) {
     boolean containsExtraAction = contains(new IsExtraAction());
     boolean containsSimple = contains(new IsSimple());
     if (containsExtraAction || containsSimple) {
       boolean twoExtraActionCharms = containsExtraAction && charm.isExtraAction();
       boolean twoSimpleCharms = containsSimple && charm.isSimple();
       return twoExtraActionCharms ||  twoSimpleCharms;
     }
     return false;
   }
   
   private boolean contains(IPredicate<ComboCharm> predicate) {
     for (ICharmId charmId : combo.charms) {
       ComboCharm comboCharm = createComboCharm(charmId);
       if (predicate.evaluate(comboCharm)) {
         return true;
       }
     }
     return false;
   }
 
   private ComboCharm createComboCharm(ICharmId charmId) {
     CharmDto data = charmDataMap.getData(charmId);
     return new ComboCharm(data);
   }
 }
