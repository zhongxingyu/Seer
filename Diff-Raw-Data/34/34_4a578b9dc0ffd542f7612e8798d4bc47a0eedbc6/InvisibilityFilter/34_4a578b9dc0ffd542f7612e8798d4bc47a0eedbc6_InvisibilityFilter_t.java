 package org.toj.dnd.irctoolkit.filter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.toj.dnd.irctoolkit.engine.ToolkitEngine;
 import org.toj.dnd.irctoolkit.map.MapGrid;
 import org.toj.dnd.irctoolkit.map.MapGridCell;
 import org.toj.dnd.irctoolkit.map.MapModel;
import org.toj.dnd.irctoolkit.token.Color;
 
 public class InvisibilityFilter extends MapFilter {
 
     private static final long serialVersionUID = -2921323894136545503L;
 
     private transient MapGridCell[][] originalMap;
     private List<String> invObjs;
    private List<MapModel> models;
 
     public InvisibilityFilter(List<String> invObjs) {
         super();
         this.invObjs = invObjs;
     }
 
     @Override
     public MapGridCell[][] doApplyFilter(MapGridCell[][] map) {
         this.originalMap = map;
         MapGridCell[][] grid = new MapGridCell[originalMap.length][originalMap[0].length];
         System.arraycopy(originalMap, 0, grid, 0, originalMap.length);
         for (int j = 0; j < grid.length; j++) {
             for (int i = 0; i < grid[j].length; i++) {
                 if (grid[j][i].getObjRef() != null
                         && resolveModels().contains(
                                 grid[j][i].getObjRef().getModel())) {
                    try {
                        grid[j][i] = (MapGridCell) grid[j][i].clone();
                    } catch (CloneNotSupportedException e) {
                        // not gonna happen
                    }
                    grid[j][i].setCh(MapGrid.WHITESPACE.getCh());
                 }
             }
         }
         return grid;
     }
 
     private List<MapModel> resolveModels() {
        models = new ArrayList<MapModel>(invObjs.size());
         for (String id : invObjs) {
             MapModel model = ToolkitEngine.getEngine().getContext()
                     .getModelList().findModelById(id);
             if (model != null) {
                 models.add(model);
             }
         }
         return models;
     }
 
     @Override
     public String getType() {
         return "˾";
     }
 
     @Override
     public String getParams() {
         StringBuilder sb = new StringBuilder();
         for (String o : this.invObjs) {
             if (o != invObjs.get(0)) {
                 sb.append(",");
             }
             sb.append(o);
         }
         return sb.toString();
     }
 }
