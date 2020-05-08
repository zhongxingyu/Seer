 package com.simon.example.layout.skin;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.simon.example.layout.R;
 import com.simon.example.layout.skin.impl.DaySkin;
 import com.simon.example.layout.skin.impl.NightSkin;
 
 import java.util.List;
 import java.util.Stack;
 
 import static com.simon.example.layout.skin.SkinInflatorFactory.ValueInfo;
 
 /**
  * 皮肤服务，替代SkinManager
  * @author yulu02
  */
 public class SkinService {
 
     private static SkinInflatorFactory sSkinInflatorFactory;
 
     public synchronized static SkinInflatorFactory getFactory(Context context) {
         if (sSkinInflatorFactory == null) {
             sSkinInflatorFactory = new SkinInflatorFactory(context);
             sSkinInflatorFactory.addHookSet(new DaySkin());
             sSkinInflatorFactory.addHookSet(new NightSkin());
             Loot.configure(context);
         }
         return sSkinInflatorFactory;
     }
 
     private static String mSkin;
 
     public static String getTheme() {
         return mSkin;
     }
 
     public static void applySkin(Activity activity) {
         mSkin = activity.getSharedPreferences("default", Context.MODE_PRIVATE).getString("skin", DaySkin.NAME);
         Loot.logApply().info("Applying skin [" + mSkin + "] to activity " + activity.getClass().getSimpleName());
         applyViews(activity.findViewById(android.R.id.content));
     }
 
     public static void applySkin(Activity activity, String skin) {
         mSkin = skin;
        activity.getSharedPreferences("default", Context.MODE_PRIVATE).edit().putString("skin", mSkin).apply();
        Loot.logApply().info("Applying skin [" + mSkin + "] to activity " + activity.getClass().getSimpleName());
        applyViews(activity.findViewById(android.R.id.content));
     }
 
     private static void applyViews(View root) {
         if (mSkin == null) return;
 
         Loot.logApply().info("Loop the view tree: " + root);
         Stack<View> stack = new Stack<View>();
         stack.push(root);
 
         while (!stack.isEmpty()) {
             View v = stack.pop();
 
             if (v instanceof ViewGroup) {
                 ViewGroup vg = (ViewGroup) v;
                 final int count = vg.getChildCount();
                 for (int i = 0; i < count; i++) {
                     stack.push(vg.getChildAt(i));
                 }
             } else {
                 @SuppressWarnings("unchecked")
                 List<ValueInfo> list = (List<ValueInfo>) ViewTagger.getTag(v, R.id.skin_hooker);
 
                 if (list == null) {
                     continue;
                 }
 
                 Loot.logApply().info("Apply skin [" + mSkin + "] to view id: " + Integer.toHexString(v.getId()));
                 for (ValueInfo info : list) {
                     if (mSkin.equals(info.skin)) {
                         info.apply.to(v, info.typedValue);
                     }
                 }
             }
         }
     }
 }
