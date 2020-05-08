 package kkckkc.jsourcepad.model.bundle;
 
 import com.google.common.collect.Maps;
 import kkckkc.jsourcepad.action.bundle.BundleAction;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.util.action.ActionGroup;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.utils.Pair;
 
 import javax.swing.*;
 import java.util.List;
 import java.util.Map;
 
 public class BundleMenuProvider {
 
     private static Map<String, Action> itemActions = Maps.newHashMap();
     private static Map<String, ActionGroup> actionGroups = Maps.newHashMap();
 
     public static Action getActionForItem(String uuid) {
         return itemActions.get(uuid);
     }
 
     public static void registerActionForItem(String uuid, Action action) {
         itemActions.put(uuid, action);
     }
 
 
     public static synchronized ActionGroup getBundleActionGroup() {
         if (actionGroups.containsKey(null)) return actionGroups.get(null);
 
         BundleManager bm = Application.get().getBundleManager();
         List<Bundle> bundles = bm.getBundles();
 
         ActionGroup ag = new ActionGroup();
 		actionGroups.put(null, ag);
         buildMenu(ag, bundles);
 
         Application.get().topic(BundleListener.class).subscribe(DispatchStrategy.ASYNC, new BundleListener() {
             @Override
             public void bundleAdded(Bundle bundle) {
                 synchronized (actionGroups) {
                     ActionGroup root = actionGroups.get(null);
 
                     ActionGroup newActionGroup = new ActionGroup(bundle.getName());
                     actionGroups.put(bundle.getName(), newActionGroup);
                     createMenu(newActionGroup, bundle.getMenu());
 
                     root.add(newActionGroup);
                     root.updateDerivedComponents();
                 }
             }
 
             @Override
             public void bundleRemoved(Bundle bundle) {
                 synchronized (actionGroups) {
                     ActionGroup bundleActionGroup = actionGroups.get(bundle.getName());
                     if (bundleActionGroup == null) return;
 
                     ActionGroup root = actionGroups.get(null);
                     root.remove(bundleActionGroup);
                     root.updateDerivedComponents();
 
                     actionGroups.remove(bundle.getName());
                 }
             }
 
             @Override
             public void bundleUpdated(Bundle bundle) {
                 ActionGroup bundleActionGroup = actionGroups.get(bundle.getName());
                 if (bundleActionGroup == null) return;
 
                 bundleActionGroup.clear();
                 createMenu(bundleActionGroup, bundle.getMenu());
                 bundleActionGroup.updateDerivedComponents();
             }
 
             @Override
             public void languagesUpdated() {
             }
         });
 
 		return ag;
     }
 
 	private static void buildMenu(ActionGroup ag, List<Bundle> list) {
 	    for (Bundle b : list) {
            if (b.getMenu().isEmpty()) continue;
            
             ActionGroup bm = new ActionGroup(b.getName());
             actionGroups.put(b.getName(), bm);
             ag.add(bm);
             
             createMenu(bm, b.getMenu());
 	    }
     }
 
     private static void createMenu(ActionGroup ag, List<Object> items) {
         for (Object o : items) {
             if (o == null) {
                 ag.add(null);
             } else if (o instanceof BundleItemSupplier) {
                 ag.add(new BundleAction((BundleItemSupplier) o));
             } else {
                 Pair<String, List<Object>> pair = (Pair<String, List<Object>>) o;
                 ActionGroup sub = new ActionGroup(pair.getFirst());
                 createMenu(sub, pair.getSecond());
                 ag.add(sub);
             }
         }
     }
 
 }
