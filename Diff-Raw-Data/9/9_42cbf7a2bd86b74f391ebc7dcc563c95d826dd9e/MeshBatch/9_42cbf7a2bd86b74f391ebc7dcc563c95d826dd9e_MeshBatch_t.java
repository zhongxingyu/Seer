 package com.unknownloner.lonelib.graphics;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import com.unknownloner.lonelib.graphics.shaders.ShaderProgram;
 import com.unknownloner.lonelib.math.Mat2;
 import com.unknownloner.lonelib.math.Mat3;
 import com.unknownloner.lonelib.math.Mat4;
 import com.unknownloner.lonelib.math.Vec2;
 import com.unknownloner.lonelib.math.Vec3;
 import com.unknownloner.lonelib.math.Vec4;
 
 public class MeshBatch {
 
     private Set<Mesh> displayList = new LinkedHashSet<Mesh>();
     private String modelLocation;
     private Vec3 scale;
     private final ShaderProgram program;
 
     public MeshBatch(ShaderProgram program, String modelLocation, Vec3 scale) {
         this.program = program;
         this.modelLocation = modelLocation;
         this.scale = scale;
     }
 
     public void render() {
         program.assign();
 
         // render each mesh that's active
         for (Mesh mesh : displayList) {
             program.setUniform(modelLocation, Mat4.IDENTITY.translate(mesh.getPosition()).scale(scale));
            program.assignUniforms();
             mesh.render();
         }
     }
 
     /**
      * Adds a mesh to the display set.
      * @param mesh
      */
     public void addMesh(Mesh mesh) {
         displayList.add(mesh);
     }
 
     /**
      * Removes a mesh from the display list
      * @param mesh
      */
     public void removeMesh(Mesh mesh) {
         displayList.remove(mesh);
     }
     
     /**
      * Adds an array of meshes to the display set.
      * @param meshes
      */
     public void addMeshes(Mesh[] meshes) {
     	displayList.addAll(Arrays.asList(meshes)); /* Arrays.asList is O(1) */
     }
     
     /**
      * Removes an array of meshses to the display set.
      * @param meshses
      */
     public void removeMeshes(Mesh[] meshes) {
     	displayList.removeAll(Arrays.asList(meshes));
     }
     
     /**
      * Adds a collection of meshes to the display set.
      * @param meshes
      */
     public void addMeshses(Collection<Mesh> meshes) {
     	displayList.addAll(meshes);
     }
     
     /**
      * Removes a collection of meshes to the display set.
      * @param meshes
      */
     public void removeMeshses(Collection<Mesh> meshes) {
     	displayList.retainAll(meshes);
     }
 
     /**
      * Get the shader program this batch uses
      * @return program
      */
     public ShaderProgram getProgram() {
         return program;
     }
 
 
     public void setUniform(String varname, boolean value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, int value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, float value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, Vec2 value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, Vec3 value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, Vec4 value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, Mat4 value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, Mat3 value) {
         program.setUniform(varname, value);
     }
 
     public void setUniform(String varname, Mat2 value) {
         program.setUniform(varname, value);
     }
 
 }
