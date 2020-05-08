 package amber.data.res;
 
 import amber.al.Audio;
 import amber.data.Workspace;
 import amber.data.io.FileIO;
 import amber.gl.model.obj.WavefrontObject;
 import amber.gui.misc.ErrorHandler;
 import java.io.File;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import static amber.data.res.ResourceListener.*;
 import static amber.data.res.Resource.*;
 import amber.gl.model.obj.Material;
 
 /**
  *
  * @author Tudor
  */
 public abstract class AbstractResourceManager implements IResourceManager {
 
     protected Workspace workspace;
     protected File imgDir, audioDir, modelDir;
     protected HashMap<String, Resource<Tileset>> tilesets = new HashMap<String, Resource<Tileset>>();
     protected HashMap<String, Resource<Audio>> clips = new HashMap<String, Resource<Audio>>();
     protected HashMap<String, Resource<WavefrontObject>> models = new HashMap<String, Resource<WavefrontObject>>();
     protected ArrayList<IResourceListener> listeners = new ArrayList<IResourceListener>();
 
     /**
      * Constructs a new AbstractResourceManager object for the given Workspace
      *
      * @param space the Workspace
      */
     public AbstractResourceManager(Workspace space) {
         workspace = space;
 
         imgDir = new File(workspace.getDataDirectory(), "graphics");
         if (!imgDir.exists()) {
             imgDir.mkdir();
         }
         audioDir = new File(workspace.getDataDirectory(), "audio");
         if (!audioDir.exists()) {
             audioDir.mkdir();
         }
         modelDir = new File(workspace.getDataDirectory(), "models");
         if (!modelDir.exists()) {
             modelDir.mkdir();
         }
     }
 
     public void importTileset(String name, Tileset sheet, File source) {
         Resource<Tileset> resource = new Resource<Tileset>(sheet, name, source, Resource.TILESET);
         tilesets.put(name, resource);
         doCopy(resource, imgDir);
         emitResourcesSafe();
         for (IResourceListener listener : listeners) {
             listener.tilesetImported(resource);
         }
     }
 
     public void removeTileset(String name) {
         Resource<Tileset> removed = tilesets.remove(name);
         doDelete(name, imgDir);
         emitResourcesSafe();
         for (IResourceListener listener : listeners) {
             listener.tilesetRemoved(removed);
         }
     }
 
     public void importAudio(String name, Audio clip, File source) {
         Resource<Audio> resource = new Resource<Audio>(clip, name, source, Resource.AUDIO);
         clips.put(name, resource);
         doCopy(resource, audioDir);
         emitResourcesSafe();
         for (IResourceListener listener : listeners) {
             listener.audioImported(resource);
         }
     }
 
     public void removeAudio(String name) {
         Resource<Audio> removed = clips.remove(name);
         doDelete(name, audioDir);
         emitResourcesSafe();
         for (IResourceListener listener : listeners) {
             listener.audioRemoved(removed);
         }
     }
 
     public void importModel(String name, WavefrontObject model, File source) {
         Resource<WavefrontObject> resource = new Resource<WavefrontObject>(model, name, source, Resource.MODEL);
         models.put(name, resource);
         try {
             File objDir = new File(modelDir, name);
             objDir.mkdirs();
             File to = new File(objDir, name + "." + FileIO.getFileExtension(source));
             if (!to.equals(source)) {
                 FileIO.copy(source, to);
                 File context = source.getParentFile();
                 for (Material m : model.getMaterials().values()) {
                     FileIO.copy(new File(context, m.getTextureName()), new File(objDir, m.getTextureName()));
                 }
                 File mtllib = model.getMaterialsFile();
                 if (mtllib != null) {
                     FileIO.copy(mtllib, new File(objDir, mtllib.getName()));
                 }
             }
         } catch (Exception ex) {
             ErrorHandler.alert(ex);
         }
         emitResourcesSafe();
         for (IResourceListener listener : listeners) {
             listener.modelImported(resource);
         }
     }
 
     public void removeModel(String name) {
         Resource<WavefrontObject> removed = models.remove(name);
         doDelete(name, modelDir);
         emitResourcesSafe();
         for (IResourceListener listener : listeners) {
             listener.modelRemoved(removed);
         }
     }
 
     public Tileset getTileset(String name) {
         Resource<Tileset> res = getTilesetResource(name);
         return res != null ? res.get() : null;
     }
 
     public Audio getAudio(String name) {
         Resource<Audio> res = getAudioResource(name);
         return res != null ? res.get() : null;
     }
 
     public WavefrontObject getModel(String name) {
         Resource<WavefrontObject> res = getModelResource(name);
         return res != null ? res.get() : null;
     }
 
     public Resource<Tileset> getTilesetResource(String name) {
         return tilesets.get(name);
     }
 
     public Resource<Audio> getAudioResource(String name) {
         return clips.get(name);
     }
 
     public Resource<WavefrontObject> getModelResource(String name) {
         return models.get(name);
     }
 
     private void doCopy(Resource res, File dir) {
         try {
             File from = res.getSource();
             File to = new File(dir, res.getName() + "." + FileIO.getFileExtension(res.getSource()));
             if (!from.equals(to)) {
                 FileIO.copy(from, to);
             }
         } catch (Exception ex) {
             ErrorHandler.alert(ex);
         }
     }
 
     private void doDelete(String name, File dir) {
         try {
             FileIO.getFilesByName(name, dir)[0].delete();
         } catch (Exception ex) {
             ErrorHandler.alert(ex);
         }
     }
 
     public Collection<Resource<Tileset>> getTilesets() {
         return tilesets.values();
     }
 
     public Collection<Resource<Audio>> getClips() {
         return clips.values();
     }
 
     public Collection<Resource<WavefrontObject>> getModels() {
         return models.values();
     }
 
     public Collection<IResourceListener> getResourceListeners() {
         return listeners;
     }
 
     public void addResourceListener(IResourceListener listener) {
         listeners.add(listener);
     }
 
     public void removeResourceListener(IResourceListener listener) {
         listeners.remove(listener);
     }
 
     public void registerResourceListener(Object listener) {
         Object parent = null;
         Class owner = listener instanceof Class ? (Class) listener : (parent = listener).getClass();
         for (Method m : owner.getDeclaredMethods()) {
             ResourceListener rl = m.getAnnotation(ResourceListener.class);
             if (rl
                     != null) {
                 if ((Modifier.isStatic(m.getModifiers()) && parent == null) || parent != null) {
                     listeners.add(new ReflectiveResourceListener(m, parent, rl.type(), rl.event()));
                 }
             }
         }
     }
 
     public void unregisterResourceListener(Object listener) {
         Object parent = null;
         Class owner = listener instanceof Class ? (Class) listener : (parent = listener).getClass();
         for (int i = 0; i != listeners.size(); i++) {
             IResourceListener l = listeners.get(i);
             if (l instanceof ReflectiveResourceListener) {
                 ReflectiveResourceListener rrl = (ReflectiveResourceListener) l;
                 Method m = rrl.listener;
                 if (m.getClass() == owner && Modifier.isStatic(m.getModifiers()) || rrl.parent == parent) {
                     listeners.remove(l);
                 }
             }
         }
     }
 
     private final void emitResourcesSafe() {
         try {
             emitResources();
         } catch (Exception ex) {
             ErrorHandler.alert(ex);
         }
     }
 
     static class ReflectiveResourceListener implements IResourceListener {
 
         Method listener;
         Object parent;
         private int type;
         private int event;
 
         public ReflectiveResourceListener(Method listener, Object parent, int type, int event) {
             if (!Modifier.isStatic(listener.getModifiers()) && parent == null) {
                 throw new IllegalArgumentException("listener method " + listener + " is static, but parent is null");
             }
             this.listener = listener;
             this.parent = parent;
             this.type = type;
             this.event = event;
         }
 
         public void tilesetImported(Resource<Tileset> sheet) {
             if ((event & IMPORT) > 0 && (type & TILESET) > 0) {
                 doExec(sheet, event);
             }
         }
 
         public void tilesetRemoved(Resource<Tileset> sheet) {
             if ((event & DELETE) > 0 && (type & TILESET) > 0) {
                 doExec(sheet, event);
             }
         }
 
         public void audioImported(Resource<Audio> clip) {
             if ((event & IMPORT) > 0 && (type & AUDIO) > 0) {
                 doExec(clip, event);
             }
         }
 
         public void audioRemoved(Resource<Audio> clip) {
             if ((event & DELETE) > 0 && (type & AUDIO) > 0) {
                 doExec(clip, event);
             }
         }
 
         public void modelImported(Resource<WavefrontObject> model) {
             if ((event & IMPORT) > 0 && (type & MODEL) > 0) {
                 doExec(model, event);
             }
         }
 
         public void modelRemoved(Resource<WavefrontObject> model) {
             if ((event & DELETE) > 0 && (type & MODEL) > 0) {
                 doExec(model, event);
             }
         }
 
         private void doExec(Resource res, int type) {
             try {
                 if (listener.getParameterTypes().length == 1) {
                     listener.invoke(parent, res);
                 } else if (listener.getParameterTypes().length == 2) {
                     listener.invoke(parent, res, type);
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
 }
