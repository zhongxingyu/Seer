 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.nantes1900.utils;
 
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.extended.Ground;
 import fr.nantes1900.models.extended.Roof;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.models.extended.Wall;
 import fr.nantes1900.models.islets.steps.Writable;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.citygml4j.builder.jaxb.JAXBBuilder;
 import org.citygml4j.factory.CityGMLFactory;
 import org.citygml4j.factory.GMLFactory;
 import org.citygml4j.factory.geometry.DimensionMismatchException;
 import org.citygml4j.factory.geometry.GMLGeometryFactory;
 import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
 import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
 import org.citygml4j.model.citygml.building.Building;
 import org.citygml4j.model.citygml.core.CityModel;
 import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
 import org.citygml4j.model.gml.geometry.primitives.Polygon;
 import org.citygml4j.model.gml.geometry.primitives.Solid;
 import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
 import org.citygml4j.model.module.citygml.CityGMLVersion;
 import org.citygml4j.util.gmlid.DefaultGMLIdManager;
 import org.citygml4j.util.gmlid.GMLIdManager;
 import org.citygml4j.xml.io.CityGMLOutputFactory;
 import org.citygml4j.xml.io.reader.CityGMLReadException;
 import org.citygml4j.xml.io.writer.CityGMLWriteException;
 
 /**
  *
  * @author JunkieLand
  */
 public class CityGMLWriter extends AbstractWriter {
 
     /**
      * The type of an element we can work on
      */
     public static final int ITEM_TYPE_ROOF = 1;
     /**
      * The type of an element we can work on
      */
     public static final int ITEM_TYPE_WALL = 2;
     /**
      * The type of an element we can work on
      */
     public static final int ITEM_TYPE_GROUND = 3;
     /**
      * The city gml factory.
      */
     private final CityGMLFactory citygml = new CityGMLFactory();
     /**
      * The gml factory.
      */
     private final GMLFactory gml = new GMLFactory();
     /**
      * The city model.
      */
     private final CityModel cityModel = this.citygml.createCityModel();
     /**
      * The gml ID manager.
      */
     private final GMLIdManager gmlIdManager = DefaultGMLIdManager.getInstance();
     /**
      * The geometry factory.
      */
     private final GMLGeometryFactory geom = new GMLGeometryFactory();
     /**
      * The JAXB building.
      */
     private JAXBBuilder builder;
     /**
      * The name of the file to write in.
      */
     private final String fileName;
 
     /**
      * Constructor
      *
      * @param fileName Name of the file to write
      */
     public CityGMLWriter(String fileName, Writable writable) {
         this.fileName = fileName;
         this.writable = writable;
     }
 
     /**
      * Adds a building to the CityGMLFactory.
      *
      * @param buildingToAdd the building to write
      */
     public final void addBuilding(
             final fr.nantes1900.models.extended.Building buildingToAdd) {
 
         final Building building = this.citygml.createBuilding();
 
         // LOD2 solid
         final List<SurfaceProperty> surfaceMember = new ArrayList<SurfaceProperty>();
 
         // Initialisation
        this.initAddItem(null, null);
 
         // Thematic boundary surfaces
         final List<BoundarySurfaceProperty> boundedBy =
                 new ArrayList<BoundarySurfaceProperty>();
 
         try {
             for (Wall wall : buildingToAdd.getbStep6().getWalls()) {
                 this.surfaceToCityGML(wall, surfaceMember, boundedBy, ITEM_TYPE_WALL);
             }
             for (Roof roof : buildingToAdd.getbStep6().getRoofs()) {
                 this.surfaceToCityGML(roof, surfaceMember, boundedBy, ITEM_TYPE_ROOF);
             }
         } catch (final DimensionMismatchException e) {
             e.printStackTrace();
         }
 
         // Finalization
         this.finalizeItemAdd(building, boundedBy);
     }
 
     /**
      * Adds a ground to the CityGMLFactory.
      *
      * @param ground the ground to add
      */
     public final void addGround(final Ground ground) {
         final Building building = this.citygml.createBuilding();
 
         // LOD2 solid
         final List<SurfaceProperty> surfaceMember = new ArrayList<SurfaceProperty>();
 
         // Initialisation
        this.initAddItem(null, null);
 
         // Thematic boundary surfaces
         final List<BoundarySurfaceProperty> boundedBy =
                 new ArrayList<BoundarySurfaceProperty>();
 
         try {
             this.surfaceToCityGML(ground, surfaceMember, boundedBy, ITEM_TYPE_GROUND);
         } catch (final DimensionMismatchException e) {
             e.printStackTrace();
         }
 
         // Finalization
         this.finalizeItemAdd(building, boundedBy);
     }
 
     /**
      * Initialize instanciations in addBuilding, addGround, addSpecialBuilding
      * methods
      *
      * @param building The CityGML building
      * @param surfaceMember List of SurfaceProperty
      */
     private void initAddItem(Building building, List<SurfaceProperty> surfaceMember) {
         // Creates the surface object.
         final CompositeSurface compositeSurface =
                 this.gml.createCompositeSurface();
         compositeSurface.setSurfaceMember(surfaceMember);
         final Solid solid = this.gml.createSolid();
         solid.setExterior(this.gml.createSurfaceProperty(compositeSurface));
 
         building.setLod2Solid(this.gml.createSolidProperty(solid));
     }
     
     /**
      * Take a Surface and turn it to CityGML
      *
      * @param surface The surface we want to turn to CityGML
      * @param surfaceMember List of SurfaceProperty
      * @param boundedBy List of BoundarySurfaceProperty
      * @param itemType Choice between Roof, Wall and Ground
      *
      * @throws DimensionMismatchException
      */
     private void surfaceToCityGML(
             Surface surface,
             List<SurfaceProperty> surfaceMember,
             List<BoundarySurfaceProperty> boundedBy,
             int itemType) throws DimensionMismatchException {
         
         if (surface.getPolygon() != null) {
             this.pointsAsCoordinatesToCityGML(surface.getPolygon(), surfaceMember, boundedBy, itemType);
         } else {
             for (Triangle item : (List<Triangle>) surface.getMesh()) {
                 this.pointsAsCoordinatesToCityGML(item, surfaceMember, boundedBy, itemType);
             }
         }
     }
     
     /**
      * Take an item implementing IPointsAsCoordinates, and turn it to
      * CityGML
      *
      * @param item The item we want to turn to CityGML
      * @param surfaceMember List of SurfaceProperty
      * @param boundedBy List of BoundarySurfaceProperty
      * @param itemType Choice between Roof, Wall and Ground
      *
      * @throws DimensionMismatchException
      */
     private void pointsAsCoordinatesToCityGML(
             IPointsAsCoordinates item,
             List<SurfaceProperty> surfaceMember,
             List<BoundarySurfaceProperty> boundedBy,
             int itemType) throws DimensionMismatchException {
         
         // Creates the geometry as a suite of coordinates.
         final Polygon geometry =
                 this.geom.createLinearPolygon(item.getPointsAsCoordinates(), 3);
 
         // Adds an ID.
         geometry.setId(this.gmlIdManager.generateGmlId());
         surfaceMember.add(this.gml.createSurfaceProperty('#' + geometry.getId()));
 
         // Creates a surface.
         AbstractBoundarySurface boundarySurface = null;
         switch (itemType) {
             case ITEM_TYPE_ROOF:
                 boundarySurface = this.citygml.createRoofSurface();
                 break;
             case ITEM_TYPE_WALL:
                 boundarySurface = this.citygml.createWallSurface();
                 break;
             case ITEM_TYPE_GROUND:
                 boundarySurface = this.citygml.createGroundSurface();
                 break;
         }
 
         // Adds the polygon as a surface.
         boundarySurface.setLod2MultiSurface(this.gml.createMultiSurfaceProperty(this.gml.createMultiSurface(geometry)));
 
         boundedBy.add(this.citygml.createBoundarySurfaceProperty(boundarySurface));
     }
 
     /**
      * Finalize addBuilding, addGround, addSpecialBuilding
      *
      * @param building The CityGML building
      * @param boundedBy List of BoundarySurfaceProperty
      */
     private void finalizeItemAdd(Building building, List<BoundarySurfaceProperty> boundedBy) {
         building.setBoundedBySurface(boundedBy);
 
         this.cityModel.setBoundedBy(building.calcBoundedBy(false));
         this.cityModel.addCityObjectMember(this.citygml.createCityObjectMember(building));
     }
 
     /**
      * Make the CityGML XML
      */
     public void makeFileFromWritable() {
         for (fr.nantes1900.models.extended.Building building : this.writable.getBuildings()) {
             this.addBuilding(building);
         }
         
         this.addGround(this.writable.getGrounds());
     }
 
     /**
      * Writes the CityGML file with the CityGMLFactory.
      */
     @Override
     public final void write() {
 
         CityGMLOutputFactory out;
         try {
             out =
                     this.builder.createCityGMLOutputFactory(CityGMLVersion.v1_0_0);
             final org.citygml4j.xml.io.writer.CityGMLWriter writer =
                     out.createCityGMLWriter(new File(this.fileName));
 
             writer.setPrefixes(CityGMLVersion.v1_0_0);
             writer.setSchemaLocations(CityGMLVersion.v1_0_0);
             writer.setIndentString("  ");
             writer.write(this.cityModel);
             writer.close();
         } catch (final CityGMLReadException e) {
             e.printStackTrace();
         } catch (final CityGMLWriteException e) {
             e.printStackTrace();
         }
 
     }
 }
