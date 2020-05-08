 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.util.scene;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.se.simplicity.jogl.scene.SimpleJOGLScene;
 import com.se.simplicity.model.ArrayVG;
 import com.se.simplicity.model.ModelConstants;
 import com.se.simplicity.model.VertexGroup;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.Light;
 import com.se.simplicity.scene.Scene;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.scenegraph.SceneGraph;
 import com.se.simplicity.scenegraph.SimpleNode;
 import com.se.simplicity.scenegraph.SimpleSceneGraph;
 import com.se.simplicity.scenegraph.model.ModelNode;
 import com.se.simplicity.util.metadata.rendering.MetaDataCamera;
 import com.se.simplicity.util.metadata.rendering.MetaDataLight;
 import com.se.simplicity.util.metadata.scene.MetaDataScene;
 import com.se.simplicity.util.metadata.scenegraph.MetaDataNode;
 import com.se.simplicity.util.metadata.scenegraph.MetaDataSceneGraph;
 import com.se.simplicity.vector.ArrayBackedObjectf;
 import com.se.simplicity.vector.TransformationMatrixf;
 import com.se.simplicity.vector.TranslationVectorf;
 
 /**
  * <p>
  * Builds <code>Scene</code>s from their serialised source and binary representations.
  * </p>
  * 
  * @author Gary Buyn
  */
 public final class SceneFactory
 {
     /**
      * <p>
      * Creates an instance of a <code>Camera</code> from a serialised source representation.
      * </p>
      * 
      * @param cameraElement The serialised source representation of the <code>Camera</code>.
      * @param sceneGraph The <code>SceneGraph</code> containing the <code>Node</code> the <code>Camera</code> should be linked to (if a link exists).
      * 
      * @return The instance of a <code>Camera</code> created from a serialised source representation.
      */
     private static Camera createCameraFromSource(final Element cameraElement, final SceneGraph sceneGraph)
     {
         // Check that a class was specified.
         if (cameraElement.getAttribute("class").isEmpty())
         {
             throw new IllegalArgumentException("Invalid Camera definition: Does not specify a class.");
         }
 
         MetaDataCamera camera = null;
         try
         {
             camera = new MetaDataCamera((Camera) Class.forName(cameraElement.getAttribute("class")).newInstance());
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("Invalid Camera definition: Specifies an invalid class.", e);
         }
 
         // Add the name to the Camera if one was specified.
         if (!cameraElement.getAttribute("name").isEmpty())
         {
             camera.setAttribute("name", cameraElement.getAttribute("name"));
         }
 
         // Link the Camera to the appropriate Node in the Scene Graph if one was specified.
         if (!cameraElement.getAttribute("node").isEmpty())
         {
             camera.setNode(sceneGraph.getNode(Integer.parseInt(cameraElement.getAttribute("node"))));
         }
 
         return (camera);
     }
 
     /**
      * <p>
      * Creates an instance of a <code>Light</code> from a serialised source representation.
      * </p>
      * 
      * @param lightElement The serialised source representation of the <code>Light</code>.
      * @param sceneGraph The <code>SceneGraph</code> containing the <code>Node</code> the <code>Light</code> should be linked to (if a link exists).
      * 
      * @return The instance of a <code>Light</code> created from a serialised source representation.
      */
     private static Light createLightFromSource(final Element lightElement, final SceneGraph sceneGraph)
     {
         // Check that a class was specified.
         if (lightElement.getAttribute("class").isEmpty())
         {
             throw new IllegalArgumentException("Invliad Light definition: Does not specify a class.");
         }
 
         MetaDataLight light = null;
         try
         {
             light = new MetaDataLight((Light) Class.forName(lightElement.getAttribute("class")).newInstance());
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("Invalid Light definition: Specifies an invalid class.", e);
         }
 
         // Add the name to the Light if one was specified.
         if (!lightElement.getAttribute("name").isEmpty())
         {
             light.setAttribute("name", lightElement.getAttribute("name"));
         }
 
         // Link the Light to the appropriate Node in the Scene Graph if one was specified.
         if (!lightElement.getAttribute("node").isEmpty())
         {
             light.setNode(sceneGraph.getNode(Integer.parseInt(lightElement.getAttribute("node"))));
         }
 
         // Set the colour of the Light if it was specified.
         Element ambientElement = (Element) lightElement.getElementsByTagName("ambient").item(0);
         if (ambientElement != null)
         {
            light.setAmbientLight(getFloatArray(ambientElement.getAttribute("colour")));
         }
 
         Element diffuseElement = (Element) lightElement.getElementsByTagName("diffuse").item(0);
         if (diffuseElement != null)
         {
            light.setDiffuseLight(getFloatArray(diffuseElement.getAttribute("colour")));
         }
 
         Element specularElement = (Element) lightElement.getElementsByTagName("specular").item(0);
         if (specularElement != null)
         {
            light.setSpecularLight(getFloatArray(specularElement.getAttribute("colour")));
         }
 
         return (light);
     }
 
     /**
      * <p>
      * Creates an instance of a <code>Node</code> from a serialised source representation.
      * </p>
      * 
      * @param nodeElement The serialised source representation of the <code>Node</code>.
      * 
      * @return The instance of a <code>Node</code> created from a serialised source representation.
      */
     private static Node createNodeFromSource(final Element nodeElement)
     {
         // Check that a class was specified. If not, use a default implementation.
         MetaDataNode node = null;
         if (nodeElement.getAttribute("class").isEmpty())
         {
             node = new MetaDataNode(new SimpleNode());
         }
         else
         {
             try
             {
                 node = new MetaDataNode((Node) Class.forName(nodeElement.getAttribute("class")).newInstance());
             }
             catch (Exception e)
             {
                 throw new IllegalArgumentException("Invalid Node definition: Specifies an invalid class.", e);
             }
         }
 
         // Add the name to the Node if one was specified.
         if (!nodeElement.getAttribute("name").isEmpty())
         {
             node.setAttribute("name", nodeElement.getAttribute("name"));
         }
         else
         {
             node.addDefaultNameAttribute();
         }
 
         // Add content to the Node if any was specified.
         NodeList contentsElement = nodeElement.getElementsByTagName("content");
         for (int index = 0; index < contentsElement.getLength(); index++)
         {
             Element contentElement = (Element) contentsElement.item(index);
 
             if (contentElement.getAttribute("type").equals("transformation"))
             {
                 performTransformationFromSource(contentElement, node);
             }
             if (contentElement.getAttribute("type").equals("vertexGroup"))
             {
                 node.setVertexGroup(createVertexGroupFromSource(contentElement));
             }
         }
 
         return (node);
     }
 
     /**
      * <p>
      * Creates a serialised source representation from an instance of a <code>Camera</code>.
      * </p>
      * 
      * @param document The document the serialised source representation will be included in.
      * @param camera The <code>Camera</code> to create a serialised source representation from.
      * 
      * @return The serialised source representation created from the <code>Camera</code>.
      */
     private static Element createSourceFromCamera(final Document document, final Camera camera)
     {
         Element cameraElement = document.createElement("camera");
 
         if (camera instanceof MetaDataCamera)
         {
             cameraElement.setAttribute("class", ((MetaDataCamera) camera).getWrappedCamera().getClass().getName());
         }
         else
         {
             cameraElement.setAttribute("class", camera.getClass().getName());
         }
 
         if (camera instanceof MetaDataCamera)
         {
             cameraElement.setAttribute("name", (String) ((MetaDataCamera) camera).getAttribute("name"));
         }
 
         if (camera.getNode() != null)
         {
             cameraElement.setAttribute("node", Integer.toString(camera.getNode().getID()));
         }
 
         return (cameraElement);
     }
 
     /**
      * <p>
      * Creates a serialised source representation from an instance of a <code>Light</code>.
      * </p>
      * 
      * @param document The document the serialised source representation will be included in.
      * @param light The <code>Light</code> to create a serialised source representation from.
      * 
      * @return The serialised source representation created from the <code>Light</code>.
      */
     private static Element createSourceFromLight(final Document document, final Light light)
     {
         Element lightElement = document.createElement("light");
 
         if (light instanceof MetaDataLight)
         {
             lightElement.setAttribute("class", ((MetaDataLight) light).getWrappedLight().getClass().getName());
         }
         else
         {
             lightElement.setAttribute("class", light.getClass().getName());
         }
 
         if (light instanceof MetaDataLight)
         {
             lightElement.setAttribute("name", (String) ((MetaDataLight) light).getAttribute("name"));
         }
 
         if (light.getNode() != null)
         {
             lightElement.setAttribute("node", Integer.toString(light.getNode().getID()));
         }
 
         Element ambientElement = document.createElement("ambient");
         lightElement.appendChild(ambientElement);
         ambientElement.setAttribute("colour", getCommaSeparatedList(light.getAmbientLight()));
 
         Element diffuseElement = document.createElement("diffuse");
         lightElement.appendChild(diffuseElement);
         diffuseElement.setAttribute("colour", getCommaSeparatedList(light.getDiffuseLight()));
 
         Element specularElement = document.createElement("specular");
         lightElement.appendChild(specularElement);
         specularElement.setAttribute("colour", getCommaSeparatedList(light.getSpecularLight()));
 
         return (lightElement);
     }
 
     /**
      * <p>
      * Creates a serialised source representation from an instance of a <code>Node</code>.
      * </p>
      * 
      * @param document The document the serialised source representation will be included in.
      * @param node The <code>Node</code> to create a serialised source representation from.
      * 
      * @return The serialised source representation created from the <code>Node</code>.
      */
     private static Element createSourceFromNode(final Document document, final Node node)
     {
         Element nodeElement = document.createElement("node");
         Node wrappedNode = null;
         if (node instanceof MetaDataNode)
         {
             wrappedNode = ((MetaDataNode) node).getWrappedNode();
         }
 
         if (node instanceof MetaDataNode)
         {
             nodeElement.setAttribute("class", wrappedNode.getClass().getName());
         }
         else
         {
             nodeElement.setAttribute("class", node.getClass().getName());
         }
 
         nodeElement.setAttribute("id", Integer.toString(node.getID()));
 
         if (node instanceof MetaDataNode)
         {
             nodeElement.setAttribute("name", (String) ((MetaDataNode) node).getAttribute("name"));
         }
 
         nodeElement.appendChild(createSourceFromTransformation(document, node.getTransformation()));
 
         if (node instanceof MetaDataNode)
         {
             if (wrappedNode instanceof ModelNode)
             {
                 nodeElement.appendChild(createSourceFromVertexGroup(document, ((ModelNode) wrappedNode).getVertexGroup()));
             }
         }
         else if (node instanceof ModelNode)
         {
             nodeElement.appendChild(createSourceFromVertexGroup(document, ((ModelNode) node).getVertexGroup()));
         }
 
         return (nodeElement);
     }
 
     /**
      * <p>
      * Creates a serialised source representation from a subgraph.
      * </p>
      * 
      * @param document The document the serialised source representation will be included in.
      * @param subgraphRoot The root <code>Node</code> of the subgraph to create a serialised source representation from.
      * 
      * @return The serialised source representation created from the subgraph.
      */
     private static Element createSourceFromSubgraph(final Document document, final Node subgraphRoot)
     {
         Element subgraphElement = createSourceFromNode(document, subgraphRoot);
 
         for (Node node : subgraphRoot.getChildren())
         {
             subgraphElement.appendChild(createSourceFromSubgraph(document, node));
         }
 
         return (subgraphElement);
     }
 
     /**
      * <p>
      * Creates a serialised source representation from an instance of a <code>TransformationMatrixf</code>.
      * </p>
      * 
      * @param document The document the serialised source representation will be included in.
      * @param transformation The <code>TransformationMatrixf</code> to create a serialised source representation from.
      * 
      * @return The serialised source representation created from the <code>TransformationMatrixf</code>.
      */
     private static Element createSourceFromTransformation(final Document document, final TransformationMatrixf transformation)
     {
         Element transformationElement = document.createElement("content");
         transformationElement.setAttribute("type", "transformation");
 
         Element translationElement = document.createElement("translation");
         transformationElement.appendChild(translationElement);
         translationElement.setAttribute("vector", getCommaSeparatedList(((ArrayBackedObjectf) transformation.getTranslation()).getArray()));
 
         float[] rotation = new float[] {transformation.getXAxisRotation(), transformation.getYAxisRotation(), transformation.getZAxisRotation()};
 
         Element rotationElement = document.createElement("rotation");
         transformationElement.appendChild(rotationElement);
         rotationElement.setAttribute("axisAngles", getCommaSeparatedList(rotation));
 
         return (transformationElement);
     }
 
     /**
      * <p>
      * Creates a serialised source representation from an instance of a <code>VertexGroup</code>.
      * </p>
      * 
      * @param document The document the serialised source representation will be included in.
      * @param vertexGroup The <code>VertexGroup</code> to create a serialised source representation from.
      * 
      * @return The serialised source representation created from the <code>VertexGroup</code>.
      */
     private static Element createSourceFromVertexGroup(final Document document, final VertexGroup vertexGroup)
     {
         Element vertexGroupElement = document.createElement("content");
         vertexGroupElement.setAttribute("type", "vertexGroup");
         vertexGroupElement.setAttribute("class", vertexGroup.getClass().getName());
 
         if (vertexGroup instanceof ArrayVG)
         {
             float[] colours = ((ArrayVG) vertexGroup).getColours();
             float[] normals = ((ArrayVG) vertexGroup).getNormals();
             float[] vertices = ((ArrayVG) vertexGroup).getVertices();
             float[] tempArray = new float[3];
 
             for (int vertexIndex = 0; vertexIndex < vertices.length / 3; vertexIndex++)
             {
                 Element vertexElement = document.createElement("vertex");
                 vertexGroupElement.appendChild(vertexElement);
 
                 System.arraycopy(colours, vertexIndex * 3, tempArray, 0, 3);
                 vertexElement.setAttribute("colour", getCommaSeparatedList(tempArray));
                 System.arraycopy(normals, vertexIndex * 3, tempArray, 0, 3);
                 vertexElement.setAttribute("normal", getCommaSeparatedList(tempArray));
                 System.arraycopy(vertices, vertexIndex * 3, tempArray, 0, 3);
                 vertexElement.setAttribute("vertex", getCommaSeparatedList(tempArray));
             }
         }
 
         return (vertexGroupElement);
     }
 
     /**
      * <p>
      * Creates a subgraph of <code>Node</code>s from a serialised source representation.
      * </p>
      * 
      * @param subgraphRootXml The serialised source representation of the root <code>Node</code> of the subgraph.
      * 
      * @return The root <code>Node</code> of the subgraph created from a serialised source representation.
      */
     private static Node createSubgraphFromSource(final Element subgraphRootXml)
     {
         Node node = createNodeFromSource(subgraphRootXml);
 
         // For every child XML node of the subgraph root.
         for (int index = 0; index < subgraphRootXml.getChildNodes().getLength(); index++)
         {
             org.w3c.dom.Node childElement = subgraphRootXml.getChildNodes().item(index);
 
             // If the child is also a Node (i.e. not an XML text node or other XML node).
             if (childElement.getNodeName().equals("node"))
             {
                 node.addChild(createSubgraphFromSource((Element) childElement));
             }
         }
 
         return (node);
     }
 
     /**
      * <p>
      * Creates a <code>XertexGroup</code> from a serialised source representation.
      * </p>
      * 
      * @param vertexGroupElement The serialised source representation of the root <code>XertexGroup</code> of the subgraph.
      * 
      * @return The instance of a <code>XertexGroup</code> created from a serialised source representation.
      */
     private static VertexGroup createVertexGroupFromSource(final Element vertexGroupElement)
     {
         // Check that a class was specified.
         if (vertexGroupElement.getAttribute("class").isEmpty())
         {
             throw new IllegalArgumentException("Invalid Vertex Group definition: Does not specify a class.");
         }
 
         VertexGroup vertexGroup = null;
         try
         {
             vertexGroup = (VertexGroup) Class.forName(vertexGroupElement.getAttribute("class")).newInstance();
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("Invalid Vertex Group definition: Specifies an invalid class.", e);
         }
 
         // Retrieve the vertex data.
         NodeList verticesElement = vertexGroupElement.getElementsByTagName("vertex");
         int vertexCount = verticesElement.getLength();
 
         if (vertexGroup instanceof ArrayVG)
         {
             // Create arrays to contain all vertex data.
             float[] colours = new float[vertexCount * ModelConstants.ITEMS_IN_CNV];
             float[] normals = new float[vertexCount * ModelConstants.ITEMS_IN_CNV];
             float[] vertices = new float[vertexCount * ModelConstants.ITEMS_IN_CNV];
 
             // For every vertex to be added to the Vertex Group.
             for (int index = 0; index < vertexCount; index++)
             {
                 // Retrieve the individual vertex data.
                 Element vertexElement = (Element) verticesElement.item(index);
                 float[] vertexColours = getFloatArray(vertexElement.getAttribute("colour"));
                 float[] vertexNormals = getFloatArray(vertexElement.getAttribute("normal"));
                 float[] vertexVertices = getFloatArray(vertexElement.getAttribute("vertex"));
 
                 // Copy the vertex data to the Vertex Group arrays.
                 System.arraycopy(vertexColours, 0, colours, index * ModelConstants.ITEMS_IN_CNV, vertexColours.length);
                 System.arraycopy(vertexNormals, 0, normals, index * ModelConstants.ITEMS_IN_CNV, vertexNormals.length);
                 System.arraycopy(vertexVertices, 0, vertices, index * ModelConstants.ITEMS_IN_CNV, vertexVertices.length);
             }
 
             ((ArrayVG) vertexGroup).setColours(colours);
             ((ArrayVG) vertexGroup).setNormals(normals);
             ((ArrayVG) vertexGroup).setVertices(vertices);
         }
 
         return (vertexGroup);
     }
 
     /**
      * <p>
      * Retrieves a comma separated list from an array of float data.
      * </p>
      * 
      * @param numbers The float data to retrieve the comma separated list from.
      * 
      * @return A comma separated list from an array of float data.
      */
     private static String getCommaSeparatedList(final float[] numbers)
     {
         String list = "";
 
         for (float number : numbers)
         {
             if (list.isEmpty())
             {
                 list = Float.toString(number);
             }
             else
             {
                 list = list + ", " + Float.toString(number);
             }
         }
 
         return (list);
     }
 
     /**
      * <p>
      * Retrieves an array of float data from a comma separated list.
      * </p>
      * 
      * @param commaSeparatedList The comma separated list to retrieve the float data from.
      * 
      * @return An array of float data from a comma separated list.
      */
     private static float[] getFloatArray(final String commaSeparatedList)
     {
         String[] stringValues = commaSeparatedList.split(",");
         float[] floatValues = new float[stringValues.length];
 
         for (int index = 0; index < stringValues.length; index++)
         {
             floatValues[index] = Float.parseFloat(stringValues[index].trim());
         }
 
         return (floatValues);
     }
 
     /**
      * <p>
      * Loads a list of <code>Camera</code>s from a serialised source representation.
      * </p>
      * 
      * @param camerasElement The serialised source representation of the <code>Camera</code>s.
      * @param sceneGraph The <code>SceneGraph</code> containing the <code>Node</code>s the <code>Camera</code>s should be linked to (if a link
      * exists).
      * 
      * @return A list of <code>Camera</code>s loaded from a serialised source representation.
      */
     private static List<Camera> loadCamerasFromSource(final NodeList camerasElement, final SceneGraph sceneGraph)
     {
         ArrayList<Camera> cameras = new ArrayList<Camera>();
 
         for (int index = 0; index < camerasElement.getLength(); index++)
         {
             cameras.add(createCameraFromSource((Element) camerasElement.item(index), sceneGraph));
         }
 
         return (cameras);
     }
 
     /**
      * <p>
      * Loads a <code>Scene</code> from a serialised binary representation.
      * </p>
      * 
      * @param binary The serialised binary representation.
      * 
      * @return A <code>Scene</code> from a serialised binary representation.
      */
     public static Scene loadFromBinary(final InputStream binary)
     {
         return (null);
     }
 
     /**
      * <p>
      * Loads a <code>Scene</code> from a serialised source representation.
      * </p>
      * 
      * @param source The serialised source representation.
      * 
      * @return A <code>Scene</code> from a serialised source representation.
      */
     public static Scene loadFromSource(final InputStream source)
     {
         MetaDataScene scene = new MetaDataScene(new SimpleJOGLScene());
 
         Document document = null;
         try
         {
             document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("Invalid serialised source representation.", e);
         }
 
         scene.setSceneGraph(loadSceneGraphFromSource((Element) document.getElementsByTagName("sceneGraph").item(0)));
         scene.setCameras(loadCamerasFromSource(document.getElementsByTagName("camera"), scene.getSceneGraph()));
         scene.setLights(loadLightsFromSource(document.getElementsByTagName("light"), scene.getSceneGraph()));
 
         return (scene);
     }
 
     /**
      * <p>
      * Loads a list of <code>Light</code>s from a serialised source representation.
      * </p>
      * 
      * @param lightsElement The serialised source representation of the <code>Light</code>s.
      * @param sceneGraph The <code>SceneGraph</code> containing the <code>Node</code>s the <code>Light</code>s should be linked to (if a link exists).
      * 
      * @return A list of <code>Light</code>s loaded from a serialised source representation.
      */
     private static List<Light> loadLightsFromSource(final NodeList lightsElement, final SceneGraph sceneGraph)
     {
         ArrayList<Light> lights = new ArrayList<Light>();
 
         for (int index = 0; index < lightsElement.getLength(); index++)
         {
             lights.add(createLightFromSource((Element) lightsElement.item(index), sceneGraph));
         }
 
         return (lights);
     }
 
     /**
      * <p>
      * Loads a <code>SceneGraph</code> from a serialised source representation.
      * </p>
      * 
      * @param sceneGraphElement The serialised source representation of the <code>SceneGraph</code>.
      * 
      * @return A <code>SceneGraph</code> loaded from a serialised source representation.
      */
     private static SceneGraph loadSceneGraphFromSource(final Element sceneGraphElement)
     {
         MetaDataSceneGraph sceneGraph = new MetaDataSceneGraph(new SimpleSceneGraph());
         NodeList childrenElement = sceneGraphElement.getChildNodes().item(1).getChildNodes();
 
         for (int index = 0; index < childrenElement.getLength(); index++)
         {
             org.w3c.dom.Node childElement = childrenElement.item(index);
 
             if (childElement.getNodeName().equals("node"))
             {
                 sceneGraph.addSubgraph(createSubgraphFromSource((Element) childElement));
             }
         }
 
         return (sceneGraph);
     }
 
     /**
      * <p>
      * Performs a transformation from a serialised source representation on a <code>Node</code>.
      * </p>
      * 
      * @param contentElement The serialised source representation of the transformation.
      * @param node The <code>Node</code> to perform the transformation on.
      */
     private static void performTransformationFromSource(final Element contentElement, final Node node)
     {
         Element translationElement = (Element) contentElement.getElementsByTagName("translation").item(0);
 
         float[] translationArray = getFloatArray(translationElement.getAttribute("vector"));
 
         TranslationVectorf translation = node.getTransformation().getTranslation();
         translation.setX(translationArray[0]);
         translation.setY(translationArray[1]);
         translation.setZ(translationArray[2]);
         node.getTransformation().translate(translation);
     }
 
     /**
      * <p>
      * Writes a serialised source representation of a <code>Scene</code> to a <code>File</code>.
      * </p>
      * 
      * @param scene The <code>Scene</code> to write a serialised source representation of.
      * @param source The <code>File</code> that will contain serialised source representation.
      */
     public static void writeToSource(final Scene scene, final File source)
     {
         Document document = null;
         try
         {
             document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         }
         catch (Exception e)
         {
             Logger.getLogger(SceneFactory.class).error("Failed to create XML Document", e);
         }
 
         writeToSource(scene, document);
 
         try
         {
             StreamResult result = new StreamResult(source);
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.transform(new DOMSource(document), result);
         }
         catch (TransformerException e)
         {
             Logger.getLogger(SceneFactory.class).error("Failed to transform XML Document", e);
         }
     }
 
     /**
      * <p>
      * Writes a serialised source representation of a <code>Scene</code> to an <code>OutputStream</code>.
      * </p>
      * 
      * @param scene The <code>Scene</code> to write a serialised source representation of.
      * @param source The <code>OutputStream</code> that will contain serialised source representation.
      */
     public static void writeToSource(final Scene scene, final OutputStream source)
     {
         Document document = null;
         try
         {
             document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         }
         catch (Exception e)
         {
             Logger.getLogger(SceneFactory.class).error("Failed to create XML Document", e);
         }
 
         writeToSource(scene, document);
 
         try
         {
             StreamResult result = new StreamResult(new OutputStreamWriter(source));
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.transform(new DOMSource(document), result);
         }
         catch (TransformerException e)
         {
             Logger.getLogger(SceneFactory.class).error("Failed to transform XML Document", e);
         }
     }
 
     /**
      * <p>
      * Writes a serialised source representation of a <code>Scene</code> to a <code>Document</code>.
      * </p>
      * 
      * @param scene The <code>Scene</code> to write a serialised source representation of.
      * @param document The <code>Document</code> that will contain serialised source representation.
      */
     public static void writeToSource(final Scene scene, final Document document)
     {
         Element simplicityElement = document.createElement("simplicity");
         document.appendChild(simplicityElement);
 
         Element sceneElement = document.createElement("scene");
         simplicityElement.appendChild(sceneElement);
 
         for (Camera camera : scene.getCameras())
         {
             sceneElement.appendChild(createSourceFromCamera(document, camera));
         }
 
         for (Light light : scene.getLights())
         {
             sceneElement.appendChild(createSourceFromLight(document, light));
         }
 
         Element sceneGraphElement = document.createElement("sceneGraph");
         sceneElement.appendChild(sceneGraphElement);
         Element internalNodesElement = document.createElement("node");
         sceneGraphElement.appendChild(internalNodesElement);
         internalNodesElement.setAttribute("name", "Internally Managed Node(s)");
 
         for (Node subgraphRoot : scene.getSceneGraph().getSubgraphRoots())
         {
             internalNodesElement.appendChild(createSourceFromSubgraph(document, subgraphRoot));
         }
     }
 
     /**
      * <p>
      * Creates an instance of <code>SceneFactory</code>. Hidden as an instance should never need to be created.
      * </p>
      */
     private SceneFactory()
     {}
 }
