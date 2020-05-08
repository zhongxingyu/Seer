 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.description.setup;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import ussr.description.Description;
 import ussr.description.geometry.VectorDescription;
 
 
 /**
  * The description of a world in which a number of robot modules can be placed for
  * the simulation.  Defines the number of modules, the size of the underlying plane,
  * and the position of each obstacle (note: obstacles currently have a fixed geometry)
  * 
  * @author ups
  *
  */
 public class WorldDescription extends Description {
 
     /**
      * Default camera positions
      * @author Modular Robots @ MMMI
      *
      */
     public static enum CameraPosition {
         DEFAULT, FAROUT;
     }
 
     /**
      * A physics-engine independent description of a texture that can be loaded from a file
      * 
      * @author Modular Robots @ MMMI
      */
     public interface TextureDescription {
         public String getFileName();
         public VectorDescription getScale(int size);
     }
 
     public static final TextureDescription GRASS_TEXTURE = new TextureDescription() {
         public String getFileName() { return "resources/myGrass2.jpg"; }
         public VectorDescription getScale(int size) { return new VectorDescription(100f,100f,0f); }
     };
 
     public static final TextureDescription GRID_TEXTURE = new TextureDescription() {
         public String getFileName() { return "resources/grid2.jpg"; }
         public VectorDescription getScale(int size) { return new VectorDescription(50f*size,50f*size,0f); }
     };
 
     public static final TextureDescription MARS_TEXTURE = new TextureDescription() {
         public String getFileName() { return "resources/marsTexture.jpg"; }
         public VectorDescription getScale(int size) { return new VectorDescription(100f,100f,0f); }
     };
 
     /**
      * Number of modules in the simulation
      */
     private int numberOfModules = 0;
 
     /**
      * The size of one edge of the underlying plane
      */
     private int planeSize = 0;
 
     /**
      * The texture of the plane
      */
     private TextureDescription planeTexture = GRASS_TEXTURE;
 
     /**
      * Whether to use a plane or a generated texture
      */
     private boolean flatWorld = true;
 
     /**
      * The position of each module, if zero automatic placement is assumed
      */
     private List<ModulePosition> modules = Collections.emptyList(); 
 
     /**
      * The position of each obstacle (and implicigtly the number of obstacles)
      */
     private List<VectorDescription> smallObstacles = Collections.emptyList();
 
     private List<ModuleConnection> connections = Collections.emptyList();
 
     private List<BoxDescription> bigObstacles = Collections.emptyList();
 
     /**
      * The initial position of the camera
      */
     private CameraPosition cameraPosition = CameraPosition.DEFAULT;
 
     /**
      * Does the background/overhead have clouds etc.?
      */
     private boolean hasBackgroundScenery = true;
 
     /**
      * Light or heavy obstacles?
      */
    private boolean heavyObstacles = true;
     
     /**
      * Get the number of modules initially placed in the simulation
      * @return number of initial modules
      */
     public int getNumberOfModules() {
         if(modules.size()>0)
             return modules.size();
         else
             return numberOfModules;
     }
 
     /**
      * Get the size (e.g., length of one of the edges) of the underlying plane placed in the simulation 
      * @return the underlying plane size
      */
     public int getPlaneSize() {
         return planeSize;
     }
 
     /**
      * Get the positions of obstacles initially placed in the world
      * @return the obstacle positions
      */
     public List<VectorDescription> getObstacles() {
         return smallObstacles;
     }
 
     /**
      * Set the number of modules initially placed in the simulation
      * @param nModules number of initial modules
      */
     public void setNumberOfModules(int nModules) {
         this.numberOfModules = nModules;
     }
 
     /**
      * Set the size (e.g., length of one of the edges) of the underlying plane placed in the simulation 
      * @param size the underlying plane size
      */
     public void setPlaneSize(int size) {
         this.planeSize = size;
     }
 
     /**
      * Set the positions of obstacles initially placed in the world
      * @param descriptions the obstacle positions
      */
     public void setObstacles(VectorDescription[] descriptions) {
         this.smallObstacles = Arrays.asList(descriptions);  
     }
 
     /**
      * @return the modules
      */
     public List<ModulePosition> getModulePositions() {
         return modules;
     }
     public void setModulePositions(List<ModulePosition> pos) {
         this.modules = pos;
     }
 
     /**
      * @param modules the modules to set
      */
     public void setModulePositions(ModulePosition[] modules) {
         this.modules = Arrays.asList(modules);
     }
 
     public void setModuleConnections(ModuleConnection[] connections) {
         this.connections = Arrays.asList(connections);
     }
     public void setModuleConnections(List<ModuleConnection> connections) {
         this.connections = connections;
     }
 
     public List<ModuleConnection> getConnections() {
         return connections;
     }
     public void setCameraPosition(CameraPosition position) {
         cameraPosition = position;
     }
 
     public CameraPosition getCameraPosition() {
         return cameraPosition;
     }
 
     /**
      * @return the bigObstacles
      */
     public List<BoxDescription> getBigObstacles() {
         return bigObstacles;
     }
 
     /**
      * @param bigObstacles the bigObstacles to set
      */
     public void setBigObstacles(BoxDescription[] bigObstacles) {
         this.bigObstacles = Arrays.asList(bigObstacles);
     }
 
     /**
      * @return the planeTexture
      */
     public TextureDescription getPlaneTexture() {
         return planeTexture;
     }
 
     /**
      * @param planeTexture the planeTexture to set
      */
     public void setPlaneTexture(TextureDescription planeTexture) {
         this.planeTexture = planeTexture;
     }
 
     public boolean theWorldIsFlat() {
         return flatWorld;
     }
 
     public void setFlatWorld(boolean flatWorld) {
         this.flatWorld = flatWorld;
     }
 
     public boolean hasBackgroundScenery() {
         return this.hasBackgroundScenery;
     }
 
     public void setHasBackgroundScenery(boolean hasClouds) {
         this.hasBackgroundScenery = hasClouds;
     }
 
     /**
      * @return the heavyObstacles
      */
     public boolean hasHeavyObstacles() {
         return heavyObstacles;
     }
 
     /**
      * @param heavyObstacles the heavyObstacles to set
      */
     public void setHeavyObstacles(boolean heavyObstacles) {
         this.heavyObstacles = heavyObstacles;
     }
 
     /**
      * Generate a random placement for module #i (since the number of modules
      * exceeded the number of supplied module position)
      * @param i the number of the module to place randomly
      * @return a <tt>ModulePosition</tt> indicating the placement of the module
      * @throws <tt>java.lang.Error</tt> unless the method has been overridden with a simulation-specific
      * implementation in a subclass
      */
     public ModulePosition placeModule(int i) {
         throw new Error("Random module placement not specified for simulation");
     }
 }
