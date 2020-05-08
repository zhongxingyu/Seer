 package com.kingx.dungeons.entity.graphics;
 
 import java.util.HashMap;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 
 public class Shader {
 
     private enum ShaderType {
         vsh, fsh;
     }
 
     private static class ShaderStructure {
         private String vertex;
         private String fragment;
         private ShaderProgram program;
 
         public void setVertex(String vertex) {
             this.vertex = vertex;
             compile();
         }
 
        
         public void setFragment(String fragment) {
             this.fragment = fragment;
             compile();
         }
         
         private void compile() {
             if(vertex != null && fragment != null){
                 program = new ShaderProgram(vertex, fragment);
                 if (!program.isCompiled()) throw new GdxRuntimeException("Couldn't compile flat shader: " + program.getLog());
             }
         }
 
 
         public String getVertex() {
             return vertex;
         }
 
         public String getFragment() {
             return fragment;
         }
         
         public ShaderProgram getShader() {
             return program;
         }
 
     }
 
     private static final String SHADER_TYPE_DELIMITER = "_";
     private static final HashMap<String, ShaderStructure> map = new HashMap<String, ShaderStructure>();
     static {
         FileHandle dirHandle;
 
         // Eclipse side-effect of linking assets, assets are copied in bin folder
         if (Gdx.app.getType() == ApplicationType.Android) {
             dirHandle = Gdx.files.internal("data/shaders");
         } else {
             // ApplicationType.Desktop ..
             dirHandle = Gdx.files.internal("./bin/data/shaders");
         }
 
         FileHandle[] list = dirHandle.list();
 
         for (FileHandle f : list) {
 
             String ext = f.extension();
             String name = f.nameWithoutExtension();
             
             ShaderStructure ss;
             if (!map.containsKey(name)) {
                 ss = new ShaderStructure();
             } else {
                 ss = map.get(name);
             }
 
             switch (ShaderType.valueOf(ext)) {
                 case vsh:
                     ss.setVertex(f.readString());
                     break;
                 case fsh:
                     ss.setFragment(f.readString());
                     break;
             }
 
             if (!map.containsKey(name)) {
                 map.put(name, ss);
             }
 
         }
     }
 
     public static String getVertex(String name) {
         return getStructure(name).getVertex();
     }
 
     public static String getFragment(String name) {
        return map.get(name).getFragment();
     }
     
     public static ShaderProgram getShader(String name) {
        return map.get(name).getShader();
     }
 
     private static ShaderStructure getStructure(String name) {
         ShaderStructure value = map.get(name);
         if(value == null){
             throw new IllegalArgumentException("Specified Shader does not exists: "+name);
         }
         return value;
     }
 
 }
