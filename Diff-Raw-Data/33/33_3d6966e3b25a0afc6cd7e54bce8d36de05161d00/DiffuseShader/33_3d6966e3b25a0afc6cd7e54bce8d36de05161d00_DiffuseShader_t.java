 package plia.framework.scene.group.shading;
 
 final class DiffuseShader extends Shader
 {
 	
 	private DiffuseShader()
 	{
 		
 	}
 	
 	@Override
 	void loadShader()
 	{
 		instance.programs[0] = new ShaderProgram(getDiffuseSrc01());
 		instance.programs[1] = new ShaderProgram(getDiffuseSrc02());
 		instance.programs[2] = new ShaderProgram(getDiffuseSrc03());
 		instance.programs[3] = new ShaderProgram(getDiffuseSrc04());
 		instance.programs[5] = new ShaderProgram(getDiffuseSrc06());
 	}
 	
 	private static DiffuseShader instance = new DiffuseShader();
 	static DiffuseShader getInstance()
 	{
 		return instance;
 	}
 	
 	private static final String matrixUniform = 
 			"uniform mat4 modelViewMatrix;" +
 			"uniform mat4 projectionMatrix;" +
 			"uniform mat3 normalMatrix;";
 	
 	private static final String lightAttribute = 
 			"const int MAX_LIGHTS = 8;" +
 			"uniform vec4 lightPosition[MAX_LIGHTS];" +
 			"uniform vec4 lightColor[MAX_LIGHTS];" +
 			"uniform float lightRange[MAX_LIGHTS];" +
 			"uniform float lightIntensity[MAX_LIGHTS];" +
 			"uniform float lightCount;";
 	
 	private static final String vertexAndNormalAttribute = 
 			"attribute vec4 vertex;" +
 			"attribute vec3 normal;";
 	
 	private static final String vertexNormalAndUVAttribute = 
 			 vertexAndNormalAttribute +
 			"attribute vec2 uv;";
 
 	private static final String boneAttributeAndMatrixPaletteUniform = 
 			"const int MATRICES_SIZE = 96;" +
 			"uniform mat4 matrixPalette[MATRICES_SIZE];" +
 			"attribute vec4 boneIndices;" +
 			"attribute vec4 boneWeights;" +
 			"attribute float boneCount;";
 
 	private static final String iDifVarying = 
 			"varying lowp vec4 Idif;";
 	
 	private static final String uvVarying = 
 			"varying vec2 uvCoord;";
 	
 	private static final String initialIDif = 
 			"	Idif = vec4(0.0);";
 	
 	private static final String vertexSkinningLoop = 
 			"	vec4 skinnedPosition = vec4(0.0);" +
 			"	vec3 skinnedNormal = vec3(0.0);" +
 			"	" +
 			"	int bCount = int(boneCount);" +
 			"	for(int b = 0; b < bCount; ++b)" +
 			"	{" +
 			"		float wt = boneWeights[b];" +
 			"		if(wt > 0.0)" +
 			"		{" +
 			"			int indx = int(boneIndices[b]);" +
 			"			mat4 matrix = matrixPalette[indx];" +
 			"			skinnedPosition += wt * vec4(vec3(matrix * vertex), vertex.w);" +
 			"			skinnedNormal += wt * vec3(mat3(matrix) * normal);" +
 			"		}" +
 			"	}";
 	
 	private static String initialVN_WithoutSkinning = 
 			"vec4 V = modelViewMatrix * vertex;" +
 			"vec3 N = normalMatrix * normal;";
 
 	private static String initialVN_WithSkinning = 
 			"vec4 V = modelViewMatrix * skinnedPosition;" +
 			"vec3 N = normalMatrix * skinnedNormal;";
 	
 	private static String lightLoop = 
 			"	int count = int(lightCount);" +
 			"	for(int i = 0; i<count; ++i)" +
 			"	{" +
 			"		vec4 lightDir = lightPosition[i];" +
 			"		vec3 L = normalize( lightDir.xyz - (V.xyz * lightDir.w) );" +
 			"		float lambertTerm = dot(N, L);" +
 			"		if(lambertTerm > 0.0)" +
 			"		{" +
 			"			if(lightDir.w > 0.0)" +
 			"			{" +
 			"				float dist = length(L);" +
 			"				float att = 1.0 - (dist / lightRange[i]);" +
 			"				lambertTerm *= att;" +
 			"			}" +
 			"			Idif += lightColor[i] * lambertTerm * lightIntensity[i];" +
 			"		}" +
 			"	}";
 	
 	private static final String initialUVCoordVarying = 
 			"	uvCoord = uv;";
 
 	//
 	private static final String diffuseMap = 
 			"uniform sampler2D diffuseMap;";
 	
 	private static final String normalMap = 
 			"uniform sampler2D normalMap;";
 	
 	private static final String heightMap = 
 			"uniform sampler2D heightMap;";
 	
 //	private static final String displacementAttribute = 
 //			"uniform float size;" +
 //			"uniform float height;";
 	
 	private static String fsWithTexture = 
 			"precision mediump float;" +
 			"" +
 			diffuseMap +
 			uvVarying +
 			iDifVarying +
 			"" +
 			"void main()" +
 			"{" +
 			"	vec4 texColor = texture2D(diffuseMap, uvCoord);" +
 			"	texColor.r *= Idif.r;" +
 			"	texColor.g *= Idif.g;" +
 			"	texColor.b *= Idif.b;" +
 			"	texColor.a *= Idif.a;" +
 			"	gl_FragColor = texColor;" +
 			"}";
 
 	private static String fsWithOutTexture = 
 			"precision mediump float;" +
 			"" +
 			"uniform vec4 color;" +
 			iDifVarying +
 			"" +
 			"void main()" +
 			"{" +
 			"	gl_FragColor = color * Idif; " +
 			"}";
 	
 	///////////////////////////////
 	private static String[] getDiffuseSrc01()
 	{
 		// Mesh Without Texture
 		String vs =
 				matrixUniform +
 				lightAttribute +
 				vertexAndNormalAttribute +
 				iDifVarying +
 				"" +
 				"void main()" +
 				"{" +
 					initialIDif +
 					initialVN_WithoutSkinning +
 					lightLoop +
 				"" +
 				"	gl_Position = projectionMatrix * V;" +
 				"}";
 
 		return new String[] {vs, fsWithOutTexture};
 	}
 	
 	private static String[] getDiffuseSrc02()
 	{
 		// Skinned-Mesh Without Texture
 		String vs =
 				matrixUniform +
 				lightAttribute +
 				vertexAndNormalAttribute +
 				boneAttributeAndMatrixPaletteUniform +
 				iDifVarying +
 				"" +
 				"void main()" +
 				"{" +
 					initialIDif +
 					vertexSkinningLoop +
 					initialVN_WithSkinning +
 					lightLoop +
 				"" +
 				"	gl_Position = projectionMatrix * V;" +
 				"}";
 
 		return new String[] {vs, fsWithOutTexture};
 	}
 	
 	private static String[] getDiffuseSrc03()
 	{
 		// Mesh With Texture
 		String vs =
 				matrixUniform +
 				lightAttribute +
 				vertexNormalAndUVAttribute +
 				iDifVarying +
 				uvVarying +
 				"" +
 				"void main()" +
 				"{" +
 					initialIDif +
 					initialVN_WithoutSkinning +
 					lightLoop +
 					initialUVCoordVarying +
 				"	gl_Position = projectionMatrix * V;" +
 				"}";
 
 		return new String[] {vs, fsWithTexture};
 	}
 	
 	private static String[] getDiffuseSrc04()
 	{
 		// Skinned-Mesh With Texture
 		String vs =
 				matrixUniform +
 				lightAttribute +
 				vertexNormalAndUVAttribute +
 				boneAttributeAndMatrixPaletteUniform +
 				iDifVarying +
 				uvVarying +
 				"" +
 				"void main()" +
 				"{" +
 					initialIDif +
 					vertexSkinningLoop +
 					initialVN_WithSkinning +
 					lightLoop +
 					initialUVCoordVarying +
 				"	gl_Position = projectionMatrix * V;" +
 				"}";
 
 		return new String[] {vs, fsWithTexture};
 	}
 	
 	private static String[] getDiffuseSrc06()
 	{
 		// Terrain With Texture
 		String vs = 
 				matrixUniform + 
 				lightAttribute + 
 				iDifVarying +
 				uvVarying +
 				"" +
 				"uniform vec3 terrainData;" +
 				"attribute vec2 vertex;" +
 				"" +
 				heightMap +
 				normalMap +
 				"" +
 				"void main()" +
 				"{" +
 				"	float segSize = terrainData.z / terrainData.y;" +
 
 				"	float u = min(0.99, max(0.01, vertex.x / terrainData.y));" +
 				"	float v = min(0.99, max(0.01, vertex.y / terrainData.y));" +
 				"" +
 				"	vec2 uv = vec2(u, v);" +
 				"" +
 				"	vec4 displace = texture2D(heightMap, uv);" +
 				"	float height = displace.x * terrainData.x;" +
 				"" +
 				"	vec4 position = vec4(vertex.x*segSize, vertex.y*segSize, height, 1.0);" +
 				"	vec3 normal = (texture2D(normalMap, vec2(uv.x, 1.0-uv.y)).xzy - 0.5) * 2.0;" +
 				"" +
 				"	vec4 V = modelViewMatrix * position;" +
 				"	vec3 N = normalMatrix * normal;" +
 				"	" +
 					initialIDif +
 					lightLoop +
 				"" +
					initialUVCoordVarying +
 				"	gl_Position = projectionMatrix * V;" +
 				"}";
 		
 //		String fs = "";
 		
 		return new String[] { vs, fsWithTexture };
 	}
 }
