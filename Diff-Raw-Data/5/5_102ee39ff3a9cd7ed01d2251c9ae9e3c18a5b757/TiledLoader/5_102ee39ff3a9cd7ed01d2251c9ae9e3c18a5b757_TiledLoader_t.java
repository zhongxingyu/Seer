 /*
  * Copyright (C) 2013 Lasse Dissing Hansen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 
 package volpes.ldk.client;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import volpes.ldk.utils.ImageOptimizer;
 import volpes.ldk.utils.Parsers;
 import volpes.ldk.utils.VFS;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @auther Lasse Dissing Hansen
  */
 public class TiledLoader implements ResourceLoader{
 
     private Map<String,TiledMap> maps = new HashMap<String,TiledMap>();
     private int numberOfLoadedObjects = 0;
 
     @Override
     public void initialize() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void shutdown() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Object get(String id) {
         return maps.get(id);
     }
 
     @Override
     public void load(Element xmlElement) {
         String id = xmlElement.getAttribute("id");
         String path = xmlElement.getTextContent();
        if (path == null || path.length() == 0) {
             System.err.println("Image resource [" + id + "] has invalid path");
            return;
        }
 
         InputStream is;
 
         try {
             is = VFS.getFile(path);
         }  catch (IOException e) {
             System.err.println("Unable to open file " + id + " at " + path);
             return;
         }
         Document doc = Parsers.parseXML(is);
         //Get map info
         Element infoElement = (Element)doc.getFirstChild();
         int width = Integer.parseInt(infoElement.getAttribute("width"));
         int height = Integer.parseInt(infoElement.getAttribute("height"));
         int tilewidth = Integer.parseInt(infoElement.getAttribute("tilewidth"));
         int tileheight = Integer.parseInt(infoElement.getAttribute("tileheight"));
 
         //Check if map is supported
         String orientation = infoElement.getAttribute("orientation");
         if (!orientation.equalsIgnoreCase("orthogonal")) {
             System.err.println("The tiled map " + id + " is not orthogonal and is therefore not supported");
             return;
         }
         float version = Float.parseFloat(infoElement.getAttribute("version"));
         if (version != 1.0) {
             System.out.println("This Tiled map does not of version 1.0 and might contain unsupported features");
         }
 
         //Load tileSets
         NodeList tileSetNodes = doc.getElementsByTagName("tileset");
         List<TileSet> tileSets = parseTileSets(tileSetNodes,new File(path).toPath().toAbsolutePath().getParent());
 
         //Load layers
         NodeList layerNodes = doc.getElementsByTagName("layer");
         List<Layer> layers = parseLayers(layerNodes);
         TiledMap map = new TiledMap(width,height,tilewidth,tileheight,layers,tileSets);
         maps.put(id,map);
         numberOfLoadedObjects++;
     }
 
     private BufferedImage loadTileset(String path, Path tilePath) {
         Path pathObj = tilePath.resolve(new File(path).toPath());
         try {
             InputStream is = VFS.getFile(pathObj.toFile().getPath());
             BufferedImage set = ImageIO.read(is);
             return ImageOptimizer.optimize(set);
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private List<TileSet> parseTileSets(NodeList tileSetNodes, Path relativTo) {
         List<TileSet> tileSets = new ArrayList<TileSet>();
         for (int i = 0; i < tileSetNodes.getLength(); i++) {
             Node tileSetNode = tileSetNodes.item(i);
             Element tileSetElm = (Element)tileSetNode;
             Element imageNode = (Element)tileSetNode.getChildNodes().item(1);
             String imgPath = imageNode.getAttribute("source");
             //imgPath = imgPath.substring(2, imgPath.length());
             String name = imageNode.hasAttribute("name") ? imageNode.getAttribute("name") : "";
             int tileWidth = Integer.parseInt(tileSetElm.getAttribute("tilewidth"));
             int tileHeight = Integer.parseInt(tileSetElm.getAttribute("tileheight"));
             int spacing = tileSetElm.hasAttribute("spacing") ? Integer.parseInt(tileSetElm.getAttribute("spacing")) : 0;
             int margin = tileSetElm.hasAttribute("margin") ? Integer.parseInt(tileSetElm.getAttribute("margin")) : 0;
             TileSet set = new TileSet(name,loadTileset(imgPath,relativTo),tileWidth,tileHeight,spacing,margin);
             tileSets.add(set);
         }
         return tileSets;
     }
 
     private List<Layer> parseLayers(NodeList layerNodes) {
         List<Layer> layers = new ArrayList<Layer>();
         for (int i = 0; i < layerNodes.getLength();i++) {
             Node layerNode = layerNodes.item(i);
             NodeList tileNodes = layerNode.getChildNodes().item(1).getChildNodes(); //Jumps over <data> tag
             layers.add(loadLayer(tileNodes));
         }
         return layers;
     }
 
     private Layer loadLayer(NodeList tileNodes) throws LDKException{
         List<Integer> tiles = new ArrayList<Integer>();
         for (int index = 0; index < tileNodes.getLength(); index++) {
 
             Node tileNode = tileNodes.item(index);
 
             if (tileNode.getNodeType() == Node.ELEMENT_NODE) {
                 Element element = (Element)tileNode;
                 if (element.getTagName().equalsIgnoreCase("tile")) {
                     tiles.add(Integer.parseInt(element.getAttribute("gid")));
                 }
             }
         }
         return new Layer(tiles);
     }
 
     @Override
     public String getLoaderID() {
         return "tiledmap";  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public int getNumberOfLoadedObjects() {
         return numberOfLoadedObjects;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
