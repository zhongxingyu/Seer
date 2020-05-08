 package de.skuzzle.polly.core.moduleloader;
 
 import org.apache.log4j.Logger;
 
 
 public abstract class AbstractProvider implements Provider {
 
     protected Logger logger = Logger.getLogger(AbstractProvider.class.getName());
 
     private ModuleLoader loader;
     private String name;
     private boolean crucial;
     private boolean setup;
     private boolean run;
 
 
 
     public AbstractProvider(String name, ModuleLoader loader, boolean isCrucial) {
         this.name = name;
         this.loader = loader;
         this.crucial = isCrucial;
 
         loader.registerModule(this);
     }
     
     
     
     @Override
     public ModuleLoader getModuleLoder() {
         return this.loader;
     }
 
 
 
     @Override
     public void addState(int state) {
         this.loader.addState(state);
     }
 
     
 
     @Override
     public void provideComponent(Object component) {
         this.loader.provideComponent(component);
     }
 
 
 
     @Override
     public void provideComponentAs(Class<?> type, Object component) {
         this.loader.provideComponentAs(type, component);
     }
 
 
 
     @Override
     public <T> T requireNow(Class<T> component, boolean check) {
         if (check && !this.loader.checkRequires(component, this)) {
             throw new ModuleDependencyException("Module " + this + 
                     " tries to access a non-reported requirement: " + component);
         }
         return this.loader.requireNow(component);
     }
 
 
 
     public void beforeSetup() {}
 
 
 
     @Override
     public final void setupModule() throws SetupException {
         if (this.setup) {
             return;
         }
 
         try {
             this.beforeSetup();
             this.setup();
         } catch (SetupException e) {
             if (this.crucial) {
                 throw e;
             }
             logger.error(
                 "Error while setup of non-crucial module '" + this.getName()
                     + "'", e);
         } finally {
             this.setup = true;
         }
     }
 
 
 
     public abstract void setup() throws SetupException;
 
 
 
     public void beforeRun() {}
 
 
 
     public final void runModule() throws Exception {
         if (this.run) {
             return;
         } else if (!this.setup) {
             throw new ModuleDependencyException("Module " + this + 
                     "' must be set up before running!");
         }
 
         try {
             this.beforeRun();
             this.run();
         } catch (Exception e) {
             if (this.isCrucial()) {
                 throw e;
             }
         } finally {
             this.run = true;
         }
     }
 
 
 
     public void run() throws Exception {
     }
 
 
 
     @Override
     public boolean isCrucial() {
         return this.crucial;
     }
 
 
 
     @Override
     public boolean isSetup() {
         return this.setup;
     }
 
 
 
     @Override
     public boolean isRun() {
         return this.run;
     }
 
 
 
     @Override
     public String getName() {
         return this.name;
     }
 
 
 
     @Override
     public String toString() {
         return this.getName();
     }
     
     
     
     @Override
     public void dispose() {
         this.loader = null;
     }
 }
