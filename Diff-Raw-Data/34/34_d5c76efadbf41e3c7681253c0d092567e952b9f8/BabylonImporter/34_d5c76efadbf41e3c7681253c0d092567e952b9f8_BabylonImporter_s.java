 // AtlantisEngine.java - Copyright (C) Yannick Comte.
 // This file is subject to the terms and conditions defined in
 // file 'LICENSE', which is part of this source code package.
 package atlantis.engine.graphics3d.importer.babylonjs;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import atlantis.engine.graphics3d.Face3;
 import atlantis.engine.graphics3d.Material;
 import atlantis.engine.graphics3d.Mesh;
 import atlantis.engine.graphics3d.Vertex;
 import atlantis.framework.Vector2;
 import atlantis.framework.Vector3;
 
 /**
  * Importer for a babylon scene.
  * @author Yannick
  */
 public class BabylonImporter {
 	public static Mesh[] loadBabyonScene(String filename) {
 		Mesh[] mesh = null;
 		BufferedReader reader = null;
 		StringBuilder jsonString = new StringBuilder();
 
 		try {
 			reader = new BufferedReader(new FileReader(filename));
 			String line;
 			while((line = reader.readLine()) != null) {
 				jsonString.append(line);
 			}
 			reader.close();
 			
			return getMeshes(createBabylonScene(jsonString.toString()));
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     
         return mesh;
     }
 	
	public static BabylonScene createBabylonScene(String jsonString) {
 		BabylonScene scene = new BabylonScene();
 		
 		JSONObject json = new JSONObject(jsonString);
 		JSONArray materials = json.getJSONArray("materials");
 		JSONArray meshes = json.getJSONArray("meshes");
 		
 		int countMaterials = materials.length();
 		int countMeshes = meshes.length();
 		
 		scene.materials = new BabylonMaterial[countMaterials];
 		
 		for (int i = 0; i < countMaterials; i++) {
 			scene.materials[i] = new BabylonMaterial();
 			JSONObject bMaterial = materials.getJSONObject(i);
 			scene.materials[i].name = bMaterial.getString("name");
 			scene.materials[i].id = bMaterial.getString("id");
 			scene.materials[i].ambient = getFloat3Array(bMaterial, "ambient");
 			scene.materials[i].diffuse = getFloat3Array(bMaterial, "diffuse");
 			scene.materials[i].specular = getFloat3Array(bMaterial, "specular");
 			scene.materials[i].specularPower = (float)bMaterial.getDouble("specularPower");
 			scene.materials[i].emissive = getFloat3Array(bMaterial, "emissive");
 			scene.materials[i].alpha = (float)bMaterial.getDouble("alpha");
 			scene.materials[i].backFaceCulling = bMaterial.getBoolean("backFaceCulling");
 			
 			JSONObject dTexture = bMaterial.getJSONObject("diffuseTexture");
 			if (dTexture != null) {
 				scene.materials[i].diffuseTexture = new BabylonTexture();
				scene.materials[i].diffuseTexture.name = dTexture.getString("name");
 				scene.materials[i].diffuseTexture.level = (float)dTexture.getDouble("level");
 				scene.materials[i].diffuseTexture.hasAlpha = (float)dTexture.getDouble("hasAlpha");
 				scene.materials[i].diffuseTexture.coordinatesMode = dTexture.getInt("coordinatesMode");
 				scene.materials[i].diffuseTexture.uOffset = (float)dTexture.getDouble("uOffset");
 				scene.materials[i].diffuseTexture.vOffset = (float)dTexture.getDouble("vOffset");
 				scene.materials[i].diffuseTexture.uScale = (float)dTexture.getDouble("uScale");
 				scene.materials[i].diffuseTexture.vScale = (float)dTexture.getDouble("vScale");
 				scene.materials[i].diffuseTexture.uAng = (float)dTexture.getDouble("uAng");
 				scene.materials[i].diffuseTexture.vAng = (float)dTexture.getDouble("vAng");
 				scene.materials[i].diffuseTexture.wAng = (float)dTexture.getDouble("wAng");
 				scene.materials[i].diffuseTexture.wrapU = dTexture.getBoolean("wrapU");
 				scene.materials[i].diffuseTexture.wrapV = dTexture.getBoolean("wrapV");
 				scene.materials[i].diffuseTexture.coordinatesIndex = dTexture.getInt("coordinatesIndex");
 			}
 		}
 		
 		scene.meshes = new BabylonMesh[countMeshes];
 		
 		for (int i = 0; i < countMeshes; i++) {
 			scene.meshes[i] = new BabylonMesh();
 			
 			JSONObject jsonMesh = meshes.getJSONObject(i);
 			scene.meshes[i].name = jsonMesh.getString("name");
 			scene.meshes[i].id = jsonMesh.getString("id");
 			try {
 				scene.meshes[i].materialId = jsonMesh.getString("materialId");
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 			scene.meshes[i].position = getFloat3Array(jsonMesh, "position");
 			scene.meshes[i].rotation = getFloat3Array(jsonMesh, "rotation");
 			scene.meshes[i].scaling = getFloat3Array(jsonMesh, "scaling");
 			
 			scene.meshes[i].isVisible = jsonMesh.getBoolean("isVisible");
 			scene.meshes[i].isEnabled = jsonMesh.getBoolean("isEnabled");
 			scene.meshes[i].checkCollisions = jsonMesh.getBoolean("checkCollisions");
 			scene.meshes[i].billboardMode = jsonMesh.getInt("billboardMode");
 			scene.meshes[i].uvCount = jsonMesh.getInt("uvCount");
 
 			scene.meshes[i].vertices = getFloatNArray(jsonMesh, "vertices");
 			scene.meshes[i].indices = getFloatNArray(jsonMesh, "indices");
 		}
 		
 		return scene;
 	}
 	
 	public static float[] getFloat3Array(JSONObject json, String key) {
 		float[] float3 = new float[3];
 
 		JSONArray jsonFloat3 = json.getJSONArray(key);
 		float3[0] = (float)jsonFloat3.getDouble(0);
 		float3[1] = (float)jsonFloat3.getDouble(1);
 		float3[2] = (float)jsonFloat3.getDouble(2);
 		
 		return float3;
 	}
 	
 	public static float[] getFloatNArray(JSONObject json, String key) {
 		JSONArray jsonFloatN = json.getJSONArray(key);
 		int countElem = jsonFloatN.length();
 		float[] floatN = new float[countElem];
 		
 		for (int i = 0; i < countElem; i++) {
 			floatN[i] = (float)jsonFloatN.getDouble(i);
 		}
 		
 		return floatN;
 	}
 	
 	private static Material getMaterialById(BabylonMaterial[] materials, String id) {
 		int i = 0;
 		int size = materials.length;
 		BabylonMaterial bMaterial = null;
 		
 		while(i < size && bMaterial == null) {
 			bMaterial = (materials[i].id.equals(id)) ? materials[i] : bMaterial;
 			i++;
 		}
 		
 		Material material = new Material();
 		
 		if (bMaterial != null) {
 			material = new Material();
 			material.setId(bMaterial.id);
 			material.setName(bMaterial.name);
 			material.setAlpha(bMaterial.alpha);
 			material.setAmbientColor(new Color(bMaterial.ambient[0], bMaterial.ambient[1], bMaterial.ambient[2]));
			material.setDiffuseTextureName(bMaterial.name);
 			material.setDiffuseColor(new Color(bMaterial.diffuse[0], bMaterial.diffuse[1], bMaterial.diffuse[2]));
 			material.setSpecularColor(new Color(bMaterial.specular[0], bMaterial.specular[1], bMaterial.specular[2]));
 			material.setSpecularPower(bMaterial.specularPower);
 			material.setEmissiveColor(new Color(bMaterial.emissive[0], bMaterial.emissive[1], bMaterial.emissive[2]));
 			material.setBackFaceCulling(bMaterial.backFaceCulling);
 		}
 		
 		return material;
 	}
 	
 	public static Mesh[] getMeshes(BabylonScene scene) {
 		Mesh[] meshesCollection = new Mesh[scene.meshes.length];
 
         for (int i = 0, l = scene.meshes.length; i < l; i++) {
             float[] verticesArray = scene.meshes[i].vertices;
             float[] indicesArray = scene.meshes[i].indices;
             int uvCount = scene.meshes[i].uvCount;
             int verticesStep = 1;
 
             verticesStep = uvCount == 0 ? 6 : verticesStep;
             verticesStep = uvCount == 1 ? 8 : verticesStep;
             verticesStep = uvCount == 2 ? 10 : verticesStep;
 
             int verticesCount = verticesArray.length / verticesStep;
             int facesCount = indicesArray.length / 3;
 
             Mesh mesh = new Mesh(scene.meshes[i].name, verticesCount, facesCount);
 
             for (int index = 0; index < verticesCount; index++) {
                 float x = verticesArray[index * verticesStep];
                 float y = verticesArray[index * verticesStep + 1];
                 float z = verticesArray[index * verticesStep + 2];
                 
                 float nx = verticesArray[index * verticesStep + 3];
                 float ny = verticesArray[index * verticesStep + 4];
                 float nz = verticesArray[index * verticesStep + 5];
                 
                 Vector2 textureCoordinate = Vector2.Zero();
                 
                 if (uvCount > 0) {
                 	float u = verticesArray[index * verticesStep + 6];
                     float v = verticesArray[index * verticesStep + 7];
                     textureCoordinate.x = u;
                     textureCoordinate.y = v;
                 }
                 
                 Vertex vertex = new Vertex(new Vector3(x, y, z), new Vector3(nx, ny, nz), Vector3.Zero(), textureCoordinate);
                 mesh.setVertex(index, vertex);
             }
 
             for (int index = 0; index < facesCount; index++) {
                 int a = (int)indicesArray[index * 3];
                 int b = (int)indicesArray[index * 3 + 1];
                 int c = (int)indicesArray[index * 3 + 2];
                 mesh.setFace(index, new Face3(a, b, c));
             }
             
             if (uvCount > 0 && scene.meshes[i].materialId != "") {
             	mesh.setMaterial(getMaterialById(scene.materials, scene.meshes[i].materialId));
             }
 
             mesh.setPosition(new Vector3(scene.meshes[i].position[0], scene.meshes[i].position[1], scene.meshes[i].position[2]));
             mesh.setRotation(new Vector3(scene.meshes[i].rotation[0], scene.meshes[i].rotation[1], scene.meshes[i].rotation[2]));
             mesh.setScale(new Vector3(scene.meshes[i].scaling[0], scene.meshes[i].scaling[1], scene.meshes[i].scaling[2]));
             meshesCollection[i] = mesh;
         }
 
         return meshesCollection;
 	}
 }
