 package info.yasskin.droidmuni;
 
 import java.lang.reflect.InvocationTargetException;
 
 import android.os.Build;
 import android.util.Log;
 
 public class Compatibility {
   public static void enableStrictMode() {
     if (Globals.DEVELOPER_MODE) {
       try {
         // StrictMode was added in Gingerbread, but I don't want to build
         // against the Gingerbread APIs, so I set it up with reflection, and
         // catch the exceptions on earlier platforms.
 
         Class<?> StrictMode = Class.forName("android.os.StrictMode");
         Class<?> StrictModeThreadPolicy =
             Class.forName("android.os.StrictMode$ThreadPolicy");
         Class<?> StrictModeThreadPolicyBuilder =
             Class.forName("android.os.StrictMode$ThreadPolicy$Builder");
 
         Object thread_policy_builder =
             StrictModeThreadPolicyBuilder.newInstance();
         thread_policy_builder =
             StrictModeThreadPolicyBuilder.getMethod("detectAll").invoke(
                 thread_policy_builder);
         thread_policy_builder =
             StrictModeThreadPolicyBuilder.getMethod("penaltyLog").invoke(
                 thread_policy_builder);
         thread_policy_builder =
             StrictModeThreadPolicyBuilder.getMethod("penaltyDeath").invoke(
                 thread_policy_builder);
         Object thread_policy =
             StrictModeThreadPolicyBuilder.getMethod("build").invoke(
                 thread_policy_builder);
         StrictMode.getMethod("setThreadPolicy", StrictModeThreadPolicy).invoke(
             null, thread_policy);
 
         Class<?> StrictModeVmPolicy =
             Class.forName("android.os.StrictMode$VmPolicy");
         Class<?> StrictModeVmPolicyBuilder =
             Class.forName("android.os.StrictMode$VmPolicy$Builder");
 
         Object vm_policy_builder = StrictModeVmPolicyBuilder.newInstance();
         vm_policy_builder =
             StrictModeVmPolicyBuilder.getMethod("detectAll").invoke(
                 vm_policy_builder);
         vm_policy_builder =
             StrictModeVmPolicyBuilder.getMethod("penaltyLog").invoke(
                 vm_policy_builder);
//        vm_policy_builder =
//            StrictModeVmPolicyBuilder.getMethod("penaltyDeath").invoke(
//                vm_policy_builder);
         Object vm_policy =
             StrictModeVmPolicyBuilder.getMethod("build").invoke(
                 vm_policy_builder);
         StrictMode.getMethod("setVmPolicy", StrictModeVmPolicy).invoke(null,
             vm_policy);
       } catch (ClassNotFoundException e) {
         strictModeNotAvailable(e);
         return;
       } catch (IllegalAccessException e) {
         strictModeNotAvailable(e);
         return;
       } catch (InstantiationException e) {
         strictModeNotAvailable(e);
         return;
       } catch (IllegalArgumentException e) {
         strictModeNotAvailable(e);
         return;
       } catch (SecurityException e) {
         strictModeNotAvailable(e);
         return;
       } catch (InvocationTargetException e) {
         strictModeNotAvailable(e);
         return;
       } catch (NoSuchMethodException e) {
         strictModeNotAvailable(e);
         return;
       }
     }
   }
 
   private static void strictModeNotAvailable(Exception e) {
     if (Build.VERSION.SDK_INT >= 9) {
       // Gingerbread added StrictMode support.
       throw new RuntimeException(e);
     }
     Log.d("DroidMuni", "Strict mode not available on this platform.");
   }
 }
