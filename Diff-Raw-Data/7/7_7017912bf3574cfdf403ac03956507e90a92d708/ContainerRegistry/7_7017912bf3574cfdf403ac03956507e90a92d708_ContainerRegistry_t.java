 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.common;
 
 import monnef.core.MonnefCorePlugin;
 import monnef.core.block.ContainerMonnefCore;
 import monnef.core.client.GuiContainerMonnefCore;
 import monnef.core.external.eu.infomas.annotation.AnnotationDetector;
 import monnef.core.utils.ReflectionTools;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.tileentity.TileEntity;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Constructor;
 import java.text.DecimalFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import static monnef.core.MonnefCorePlugin.Log;
 
 public class ContainerRegistry {
     public static final String CANNOT_INSTANTIATE_CONTAINER = "Cannot instantiate container.";
     private static HashMap<Class<? extends TileEntity>, MachineItem> db = new HashMap<Class<? extends TileEntity>, MachineItem>();
     private static boolean filledFromAnnotationsServer = false;
     private static boolean filledFromAnnotationsClient = false;
     private static DecimalFormat decimalFormatter = new DecimalFormat("#.##");
 
     @SuppressWarnings("unchecked")
     public static void fillRegistrationsFromAnnotations(boolean clientSide) {
         if (!clientSide) {
             if (filledFromAnnotationsServer)
                 throw new RuntimeException("Server registry are already filled from annotations.");
             filledFromAnnotationsServer = true;
         } else {
             if (filledFromAnnotationsClient)
                 throw new RuntimeException("Client registry are already filled from annotations.");
             filledFromAnnotationsClient = true;
         }
 
         final HashSet<Class<?>> result = new HashSet<Class<?>>();
         final AnnotationDetector.TypeReporter reporter = new AnnotationDetector.TypeReporter() {
 
             @SuppressWarnings("unchecked")
             @Override
             public Class<? extends Annotation>[] annotations() {
                 return new Class[]{ContainerTag.class};
             }
 
             @Override
             public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                 Log.printFinest(String.format("Discovered ContainerTag annotation on class: %s", className));
                 try {
                     result.add(this.getClass().getClassLoader().loadClass(className));
                 } catch (ClassNotFoundException e) {
                     Log.printWarning("Container registry: unable to load \"" + className + "\"!");
                 }
             }
         };
         final AnnotationDetector cf = new AnnotationDetector(reporter);
         long timeStart = System.currentTimeMillis();
         try {
             cf.detect();
             long timeStop = System.currentTimeMillis();
             Log.printFine(String.format("%s-side annotation class-path scanning for ContainerTag took %ss", clientSide ? "Client" : "Server", decimalFormatter.format((timeStop - timeStart) / 1000f)));
 
             timeStart = System.currentTimeMillis();
             File file = new File(MonnefCorePlugin.getMcPath() + "/mods/");
             if (file.isDirectory()) {
                 for (File f : file.listFiles()) {
                     Log.printFinest(String.format("Scanning file: %s", f.getName()));
                    try {
                        cf.detect(f);
                    } catch (IOException e) {
                        Log.printWarning(String.format("Cannot process file: '%s'", f.getName()));
                        e.printStackTrace();
                    }
                 }
             }
             timeStop = System.currentTimeMillis();
             Log.printFine(String.format("%s-side annotation mods directory scanning for ContainerTag took %ss", clientSide ? "Client" : "Server", decimalFormatter.format((timeStop - timeStart) / 1000f)));
         } catch (IOException e) {
             Log.printSevere("Encountered IO error:" + e.getMessage());
             e.printStackTrace();
         }
 
         for (Class<?> c : result) {
             ContainerTag tag = c.getAnnotation(ContainerTag.class);
             if (TileEntity.class.isAssignableFrom(c)) {
                 Class<TileEntity> tec = (Class<TileEntity>) c;
                 if (clientSide) {
                     if (!"".equals(tag.guiClassName())) {
                         try {
                             Class<? extends GuiContainerMonnefCore> clazz = (Class<? extends GuiContainerMonnefCore>) ContainerRegistry.class.getClassLoader().loadClass(tag.guiClassName());
                             registerOnClientInternal(tec, clazz);
                         } catch (ClassNotFoundException e) {
                             Log.printWarning("Client-side registration failed, class \"" + tag.guiClassName() + "\" cannot be loaded. TE class: " + tec.getName());
                         }
                     }
                 } else {
                     if (!"".equals(tag.containerClassName())) {
                         try {
                             Class<? extends ContainerMonnefCore> clazz = (Class<? extends ContainerMonnefCore>) ContainerRegistry.class.getClassLoader().loadClass(tag.containerClassName());
                             registerInternal(tec, clazz);
                         } catch (ClassNotFoundException e) {
                             Log.printWarning("Registration failed, class \"" + tag.containerClassName() + "\" cannot be loaded. TE class: " + tec.getName());
                         }
                     }
                 }
             } else {
                 Log.printWarning(String.format("Class %s is annotated with ContainerTag but is not TileEntity, mistake?", c.getName()));
             }
         }
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.TYPE)
     @Inherited
     public @interface ContainerTag {
         int slotsCount();
 
         int outputSlotsCount() default 1;
 
         int inputSlotsCount() default -1;
 
         String guiClassName() default "";
 
         String containerClassName() default "";
     }
 
     public static class ContainerDescriptor {
         private final int slotsCount;
         private final int outputSlotsCount;
         private final int inputSlotsCount;
 
         public ContainerDescriptor(ContainerTag tag) {
             slotsCount = tag.slotsCount();
             outputSlotsCount = tag.outputSlotsCount();
             inputSlotsCount = tag.inputSlotsCount() == -1 ? slotsCount - outputSlotsCount : tag.inputSlotsCount();
         }
 
         public int getSlotsCount() {
             return slotsCount;
         }
 
         public int getOutputSlotsCount() {
             return outputSlotsCount;
         }
 
         public int getInputSlotsCount() {
             return inputSlotsCount;
         }
 
         public int getStartIndexOfOutput() {
             return getSlotsCount() - getOutputSlotsCount();
         }
     }
 
     public static class MachineItem {
         public final Class<? extends TileEntity> tileClass;
         public final Class<? extends ContainerMonnefCore> containerClass;
         private Class<?> guiClass; // GuiContainerBasicProcessingMachine
 
         public final ContainerDescriptor containerPrototype;
         private Constructor<?> guiConstructor; // GuiContainerBasicProcessingMachine
         public final Constructor<? extends ContainerMonnefCore> containerConstructor;
 
         private MachineItem(Class<? extends TileEntity> tileClass, Class<? extends ContainerMonnefCore> containerClass, ContainerDescriptor containerPrototype) {
             this.tileClass = tileClass;
             this.containerClass = containerClass;
             this.containerPrototype = containerPrototype;
 
             if (containerClass.getConstructors().length != 1) {
                 throw new RuntimeException("Multiple constructors of container class.");
             }
             Constructor<?> constructor = containerClass.getConstructors()[0];
             if (constructor.getParameterTypes().length != 2) {
                 throw new RuntimeException("Wrong count of parameters of container constructor.");
             }
             if (!InventoryPlayer.class.isAssignableFrom(constructor.getParameterTypes()[0]) ||
                     !TileEntity.class.isAssignableFrom(constructor.getParameterTypes()[1])) {
                 throw new RuntimeException("Incorrect type of parameters of container constructor.");
             }
             //this.containerConstructor = containerClass.getConstructor(InventoryPlayer.class, TileEntity.class);
             this.containerConstructor = (Constructor<? extends ContainerMonnefCore>) constructor;
         }
 
         private void setGuiConstructor(Constructor<?> guiConstructor) {
             if (this.guiConstructor != null) throw new RuntimeException("GUI constructor already set");
             this.guiConstructor = guiConstructor;
         }
 
         public void setGuiClass(Class<?> guiClass) {
             this.guiClass = guiClass;
             if (guiClass != null) {
                 if (guiClass.getConstructors().length != 1) {
                     throw new RuntimeException("Multiple constructors of GUI class.");
                 }
                 Constructor<?> constructor = guiClass.getConstructors()[0];
                 if (constructor.getParameterTypes().length != 3) {
                     throw new RuntimeException("Wrong count of parameters of GUI constructor.");
                 }
                 if (!InventoryPlayer.class.isAssignableFrom(constructor.getParameterTypes()[0]) ||
                         !TileEntity.class.isAssignableFrom(constructor.getParameterTypes()[1]) ||
                         !ContainerMonnefCore.class.isAssignableFrom(constructor.getParameterTypes()[2])) {
                     throw new RuntimeException("Incorrect type of parameters of GUI constructor.");
                 }
                 setGuiConstructor(constructor);
             } else setGuiConstructor(null);
         }
 
         public Constructor<?> getGuiConstructor() {
             return guiConstructor;
         }
 
         public Class<?> getGuiClass() {
             return guiClass;
         }
     }
 
     @SuppressWarnings("deprecation")
     private static void registerInternal(Class<? extends TileEntity> clazz, Class<? extends ContainerMonnefCore> container) {
         register(clazz, container);
     }
 
     @SuppressWarnings("deprecation")
     private static void registerOnClientInternal(Class<? extends TileEntity> clazz, Class<?> gui) {
         registerOnClient(clazz, gui);
     }
 
     @Deprecated
     public static void register(Class<? extends TileEntity> clazz, Class<? extends ContainerMonnefCore> container) {
         if (db.containsKey(clazz)) {
             throw new RuntimeException("containerPrototype already contains this class, cannot re-register");
         }
         ContainerTag tag = ReflectionTools.findAnnotation(ContainerTag.class, clazz);
         if (tag == null) {
             throw new RuntimeException(ContainerTag.class.getSimpleName() + " not found on " + clazz.getSimpleName());
         }
 
         db.put(clazz, new MachineItem(clazz, container, new ContainerDescriptor(tag)));
     }
 
     @Deprecated
     public static void registerOnClient(Class<? extends TileEntity> clazz, Class<?> gui) {
         MachineItem item = db.get(clazz);
         if (item == null) {
             throw new RuntimeException(String.format("Registering GUI container with unknown TE. TE: %s, GUI: %s", clazz.getName(), gui.getName()));
         }
         if (!GuiContainerMonnefCore.class.isAssignableFrom(gui)) {
             throw new RuntimeException("Class doesn't inherit from proper ancestor!");
         }
         item.setGuiClass(gui);
     }
 
     public static void assertAllItemsHasGuiClass() {
         for (Map.Entry<Class<? extends TileEntity>, MachineItem> item : db.entrySet()) {
             if (item.getValue().getGuiClass() == null) {
                 throw new RuntimeException("TE " + item.getKey().getSimpleName() + " is missing GUI mapping.");
             }
         }
     }
 
     public static ContainerDescriptor getContainerPrototype(Class<? extends TileEntity> clazz) {
         if (!db.containsKey(clazz)) {
             throw new RuntimeException("Query for not registered TE named " + clazz.getSimpleName());
         }
         return db.get(clazz).containerPrototype;
     }
 
     public static Collection<Class<? extends TileEntity>> getTileClasses() {
         return db.keySet();
     }
 
     public static ContainerMonnefCore createContainer(TileEntity tile, InventoryPlayer inventory) {
         try {
             return db.get(tile.getClass()).containerConstructor.newInstance(inventory, tile);
         } catch (Throwable e) {
             throw new RuntimeException("Cannot create new container for tile class: " + tile.getClass().getSimpleName(), e);
         }
     }
 
     public static MachineItem getItem(Class tileClass) {
         return db.get(tileClass);
     }
 
     public static boolean containsRegistration(TileEntity tile) {
         if (tile == null) return false;
         return db.containsKey(tile.getClass());
     }
 
     public static Object createGui(TileEntity tile, InventoryPlayer inventory) {
         try {
             return ContainerRegistry.getItem(tile.getClass()).getGuiConstructor().newInstance(inventory, tile, ContainerRegistry.createContainer(tile, inventory));
         } catch (Throwable e) {
             throw new RuntimeException("Cannot create new GUI for container for tile class: " + tile.getClass().getSimpleName(), e);
         }
     }
 }
