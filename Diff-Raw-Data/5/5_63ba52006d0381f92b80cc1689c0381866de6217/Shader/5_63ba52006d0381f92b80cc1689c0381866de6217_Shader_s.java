 package com.razh.tiling;
 
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
 public class Shader {
 	public static int MAX_POINT_LIGHTS = 0;
 
 	public static ShaderProgram createLambertShaderProgram() {
 		String vertex =
 			"#define MAX_POINT_LIGHTS " + MAX_POINT_LIGHTS + "\n" +
 			"uniform vec3 diffuse;\n" +
 			"uniform vec3 ambient;\n" +
 			"uniform vec3 emissive;\n" +
 			"uniform vec3 ambientLightColor;\n" +
 			"#if MAX_POINT_LIGHTS > 0\n" +
 			"  uniform vec3 pointLightColor[MAX_POINT_LIGHTS];\n" +
 			"  uniform vec3 pointLightPosition[MAX_POINT_LIGHTS];\n" +
 			"  uniform float pointLightDistance[MAX_POINT_LIGHTS];\n" +
 			"#endif\n" +
 			"uniform mat4 projectionMatrix;\n" +
 			"uniform mat4 viewMatrix;\n" +
 			"uniform mat4 modelMatrix;\n" +
 			"uniform mat3 normalMatrix;\n" +
 			"attribute vec3 a_position;\n" +
 			"attribute vec3 a_normal;\n" +
 			"varying vec3 v_lightFront;\n" +
 			"\n" +
 			"void main()\n" +
 			"{\n" +
 			"  vec4 mvPosition = viewMatrix * modelMatrix * vec4(a_position, 1.0);\n" +
 			"  vec3 transformedNormal = normalize(normalMatrix * a_normal);\n" +
 			"  v_lightFront = vec3(0.0);\n" +
 			"  #if MAX_POINT_LIGHTS > 0\n" +
 			"    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {\n" +
 			"      vec4 lightPosition = viewMatrix * vec4(pointLightPosition[i], 1.0);\n" +
 			"      vec3 lightVector = lightPosition.xyz - mvPosition.xyz;\n" +
 			"      float lightDistance = 1.0;\n" +
 			"      if (pointLightDistance[i] > 0.0) {\n" +
 			"        lightDistance = 1.0 - min((length(lightVector) / pointLightDistance[i]), 1.0);\n" +
 			"      }\n" +
 			"      lightVector = normalize(lightVector);\n" +
 			"      float dotProduct = dot(transformedNormal, lightVector);\n" +
 			"      vec3 pointLightWeighting = vec3(max(dotProduct, 0.0));\n" +
 			"      v_lightFront += pointLightColor[i] * pointLightWeighting * lightDistance;\n" +
 			"    }\n" +
 			"  #endif\n" +
 			"  v_lightFront =  v_lightFront * diffuse + ambient * ambientLightColor + emissive;;\n" +
 			"  gl_Position = projectionMatrix * mvPosition;\n" +
 			"}";
 
 		String fragment =
 			"#ifdef GL_ES\n" +
 			"precision mediump float;\n" +
 			"#endif\n" +
 			"varying vec3 v_lightFront;\n" +
 			"\n" +
 			"void main()\n" +
 			"{\n" +
 			"  gl_FragColor = vec4(1.0);\n" +
 			"  gl_FragColor.xyz *= v_lightFront;\n" +
 			"}";
 		System.out.println(vertex);
 		System.out.println(fragment);
 
 		ShaderProgram shaderProgram = new ShaderProgram(vertex, fragment);
 	 	System.out.println("Compiled: " + shaderProgram.isCompiled() + "----------");
 		return shaderProgram;
 	}
 
 	public static ShaderProgram createColorLambertShaderProgram() {
 		String vertex =
 			"#define MAX_POINT_LIGHTS " + MAX_POINT_LIGHTS + "\n" +
 			"uniform vec3 diffuse;\n" +
 			"uniform vec3 ambient;\n" +
 			"uniform vec3 emissive;\n" +
 			"uniform vec3 ambientLightColor;\n" +
 			"#if MAX_POINT_LIGHTS > 0\n" +
 			"  uniform vec3 pointLightColor[MAX_POINT_LIGHTS];\n" +
 			"  uniform vec3 pointLightPosition[MAX_POINT_LIGHTS];\n" +
 			"  uniform float pointLightDistance[MAX_POINT_LIGHTS];\n" +
 			"#endif\n" +
 			"uniform mat4 projectionMatrix;\n" +
 			"uniform mat4 viewMatrix;\n" +
 			"uniform mat4 modelMatrix;\n" +
 			"uniform mat3 normalMatrix;\n" +
 			"attribute vec3 a_position;\n" +
 			"attribute vec3 a_normal;\n" +
 			"attribute vec3 a_color;\n" +
 			"varying vec3 v_lightFront;\n" +
 			"\n" +
 			"void main()\n" +
 			"{\n" +
 			"  vec4 mvPosition = viewMatrix * modelMatrix * vec4(a_position, 1.0);\n" +
 			"  vec3 transformedNormal = normalize(normalMatrix * a_normal);\n" +
			"  v_lightFront = vec3(0.0);\n" +
 			"  #if MAX_POINT_LIGHTS > 0\n" +
 			"    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {\n" +
 			"      vec4 lightPosition = viewMatrix * vec4(pointLightPosition[i], 1.0);\n" +
 			"      vec3 lightVector = lightPosition.xyz - mvPosition.xyz;\n" +
 			"      float lightDistance = 1.0;\n" +
 			"      if (pointLightDistance[i] > 0.0) {\n" +
 			"        lightDistance = 1.0 - min((length(lightVector) / pointLightDistance[i]), 1.0);\n" +
 			"      }\n" +
 			"      lightVector = normalize(lightVector);\n" +
 			"      float dotProduct = dot(transformedNormal, lightVector);\n" +
 			"      vec3 pointLightWeighting = vec3(max(dotProduct, 0.0));\n" +
 			"      v_lightFront += pointLightColor[i] * pointLightWeighting * lightDistance;\n" +
 			"    }\n" +
 			"  #endif\n" +
			"  v_lightFront = v_lightFront * a_color + ambient * ambientLightColor + emissive + diffuse - diffuse;\n" +
 			"  gl_Position = projectionMatrix * mvPosition;\n" +
 			"}";
 
 		String fragment =
 			"#ifdef GL_ES\n" +
 			"precision mediump float;\n" +
 			"#endif\n" +
 			"varying vec3 v_lightFront;\n" +
 			"\n" +
 			"void main()\n" +
 			"{\n" +
 			"  gl_FragColor = vec4(1.0);\n" +
 			"  gl_FragColor.xyz *= v_lightFront;\n" +
 			"}";
 
 		System.out.println(vertex);
 		System.out.println(fragment);
 
 		ShaderProgram shaderProgram = new ShaderProgram(vertex, fragment);
 	 	System.out.println("Compiled: " + shaderProgram.isCompiled() + "----------");
 		return shaderProgram;
 	}
 
 	/**
 	 * Phong per-pixel lighting.
 	 */
 	public static ShaderProgram createPhongShaderProgram() {
 		String vertex =
 				"uniform mat4 projectionMatrix;\n" +
 				"uniform mat4 viewMatrix;\n" +
 				"uniform mat4 modelMatrix;\n" +
 				"uniform mat3 normalMatrix;\n" +
 				"attribute vec3 a_position;\n" +
 				"attribute vec3 a_normal;\n" +
 				"varying vec3 v_viewPosition;\n" +
 				"varying vec3 v_normal;\n" +
 				"\n" +
 				"void main()\n" +
 				"{\n" +
 				"  vec4 mvPosition = viewMatrix * modelMatrix * vec4(a_position, 1.0);\n" +
 				"  v_viewPosition = -mvPosition.xyz;\n" +
 				"  v_normal = normalize(normalMatrix * a_normal);\n" +
 				"  gl_Position = projectionMatrix * mvPosition;\n" +
 				"}";
 
 			String fragment =
 				"#define MAX_POINT_LIGHTS " + MAX_POINT_LIGHTS + "\n" +
 				"#ifdef GL_ES\n" +
 				"precision mediump float;\n" +
 				"#endif\n" +
 				"uniform mat4 viewMatrix;\n" +
 				"uniform vec3 diffuse;\n" +
 				"uniform vec3 ambient;\n" +
 				"uniform vec3 emissive;\n" +
 				"uniform vec3 specular;\n" +
 				"uniform float shininess;\n" +
 				"uniform vec3 ambientLightColor;\n" +
 				"#if MAX_POINT_LIGHTS > 0\n" +
 				"  uniform vec3 pointLightColor[MAX_POINT_LIGHTS];\n" +
 				"  uniform vec3 pointLightPosition[MAX_POINT_LIGHTS];\n" +
 				"  uniform float pointLightDistance[MAX_POINT_LIGHTS];\n" +
 				"#endif\n" +
 				"varying vec3 v_viewPosition;\n" +
 				"varying vec3 v_normal;\n" +
 				"\n" +
 				"void main()\n" +
 				"{\n" +
 				"  gl_FragColor = vec4(1.0);\n" +
 				"  float specularStrength = 1.0;\n" +
 				"  vec3 viewPosition = normalize(v_viewPosition);\n" +
 				"  vec3 pointDiffuse = vec3(0.0);\n" +
 				"  vec3 pointSpecular = vec3(0.0);\n" +
 				"  for (int i = 0; i < MAX_POINT_LIGHTS; i++) {\n" +
 				"    vec4 lightPosition = viewMatrix * vec4(pointLightPosition[i], 1.0);\n" +
 				"    vec3 lightVector = lightPosition.xyz + v_viewPosition;\n" +
 				"    float lightDistance = 1.0;\n" +
 				"    if (pointLightDistance[i] > 0.0) {\n" +
 				"      lightDistance = 1.0 - min((length(lightVector) / pointLightDistance[i]), 1.0);\n" +
 				"    }\n" +
 				"    lightVector = normalize(lightVector);\n" +
 				"    float dotProduct = dot(v_normal, lightVector);\n" +
 				"    float pointDiffuseWeight = max(dotProduct, 0.0);\n" +
 				"    pointDiffuse += diffuse * pointLightColor[i] * pointDiffuseWeight * lightDistance;\n" +
 				"    vec3 pointHalfVector = normalize(lightVector + viewPosition);\n" +
 				"    float pointDotNormalHalf = max(dot(v_normal, pointHalfVector), 0.0);\n" +
 				"    float pointSpecularWeight = specularStrength * max(pow(pointDotNormalHalf, shininess), 0.0);\n" +
 				"    pointSpecular += specular * pointLightColor[i] * pointSpecularWeight * pointDiffuseWeight * lightDistance;\n" +
 				"  }\n" +
 				"  gl_FragColor.xyz = gl_FragColor.xyz * (emissive + pointDiffuse + ambientLightColor * ambient) + pointSpecular;\n" +
 				"}";
 			System.out.println(vertex);
 			System.out.println(fragment);
 
 			ShaderProgram shaderProgram = new ShaderProgram(vertex, fragment);
 		 	System.out.println("Compiled: " + shaderProgram.isCompiled() + "----------");
 			return shaderProgram;
 	}
 
 	public static ShaderProgram createColorPhongShaderProgram() {
 		String vertex =
 				"uniform mat4 projectionMatrix;\n" +
 				"uniform mat4 viewMatrix;\n" +
 				"uniform mat4 modelMatrix;\n" +
 				"uniform mat3 normalMatrix;\n" +
 				"attribute vec3 a_position;\n" +
 				"attribute vec3 a_normal;\n" +
 				"attribute vec3 a_color;\n" +
 				"varying vec3 v_viewPosition;\n" +
 				"varying vec3 v_normal;\n" +
 				"varying vec3 v_color;\n" +
 				"\n" +
 				"void main()\n" +
 				"{\n" +
 				"  vec4 mvPosition = viewMatrix * modelMatrix * vec4(a_position, 1.0);\n" +
 				"  v_viewPosition = -mvPosition.xyz;\n" +
 				"  v_normal = normalize(normalMatrix * a_normal);\n" +
 				"  v_color = a_color;\n" +
 				"  gl_Position = projectionMatrix * mvPosition;\n" +
 				"}";
 
 			String fragment =
 				"#define MAX_POINT_LIGHTS " + MAX_POINT_LIGHTS + "\n" +
 				"#ifdef GL_ES\n" +
 				"precision mediump float;\n" +
 				"#endif\n" +
 				"uniform mat4 viewMatrix;\n" +
 				"uniform vec3 diffuse;\n" +
 				"uniform vec3 ambient;\n" +
 				"uniform vec3 emissive;\n" +
 				"uniform vec3 specular;\n" +
 				"uniform float shininess;\n" +
 				"uniform vec3 ambientLightColor;\n" +
 				"#if MAX_POINT_LIGHTS > 0\n" +
 				"  uniform vec3 pointLightColor[MAX_POINT_LIGHTS];\n" +
 				"  uniform vec3 pointLightPosition[MAX_POINT_LIGHTS];\n" +
 				"  uniform float pointLightDistance[MAX_POINT_LIGHTS];\n" +
 				"#endif\n" +
 				"varying vec3 v_viewPosition;\n" +
 				"varying vec3 v_normal;\n" +
 				"varying vec3 v_color;\n" +
 				"\n" +
 				"void main()\n" +
 				"{\n" +
 				"  vec3 finalDiffuse = v_color + diffuse - diffuse;\n" +
 				"  gl_FragColor = vec4(1.0);\n" +
 				"  float specularStrength = 1.0;\n" +
 				"  vec3 viewPosition = normalize(v_viewPosition);\n" +
 				"  vec3 pointDiffuse = vec3(0.0);\n" +
 				"  vec3 pointSpecular = vec3(0.0);\n" +
 				"  for (int i = 0; i < MAX_POINT_LIGHTS; i++) {\n" +
 				"    vec4 lightPosition = viewMatrix * vec4(pointLightPosition[i], 1.0);\n" +
 				"    vec3 lightVector = lightPosition.xyz + v_viewPosition;\n" +
 				"    float lightDistance = 1.0;\n" +
 				"    if (pointLightDistance[i] > 0.0) {\n" +
 				"      lightDistance = 1.0 - min((length(lightVector) / pointLightDistance[i]), 1.0);\n" +
 				"    }\n" +
 				"    lightVector = normalize(lightVector);\n" +
 				"    float dotProduct = dot(v_normal, lightVector);\n" +
 				"    float pointDiffuseWeight = max(dotProduct, 0.0);\n" +
 				"    pointDiffuse += finalDiffuse  * pointLightColor[i] * pointDiffuseWeight * lightDistance;\n" +
 				"    vec3 pointHalfVector = normalize(lightVector + viewPosition);\n" +
 				"    float pointDotNormalHalf = max(dot(v_normal, pointHalfVector), 0.0);\n" +
 				"    float pointSpecularWeight = specularStrength * max(pow(pointDotNormalHalf, shininess), 0.0);\n" +
 				"    pointSpecular += specular * pointLightColor[i] * pointSpecularWeight * pointDiffuseWeight * lightDistance;\n" +
 				"  }\n" +
 				"  gl_FragColor.xyz = gl_FragColor.xyz * (emissive + pointDiffuse + ambientLightColor * ambient) + pointSpecular;\n" +
 				"}";
 			System.out.println(vertex);
 			System.out.println(fragment);
 
 			ShaderProgram shaderProgram = new ShaderProgram(vertex, fragment);
 		 	System.out.println("Compiled: " + shaderProgram.isCompiled() + "----------");
 			return shaderProgram;
 	}
 
 	public static ShaderProgram createPointLightShaderProgram() {
 		String vertex =
 			"uniform mat4 modelViewProjectionMatrix;\n" +
 			"uniform vec3 translate;\n" +
 			"uniform vec3 scale;\n" +
 			"attribute vec3 a_position;\n" +
 			"\n" +
 			"void main()\n" +
 			"{\n" +
 			"  gl_PointSize = 5.0;\n" +
 			"  gl_Position = modelViewProjectionMatrix * vec4(scale * a_position + translate, 1.0);\n" +
 			"}";
 
 		String fragment =
 			"#ifdef GL_ES\n" +
 			"precision mediump float;\n" +
 			"#endif\n" +
 			"uniform vec4 color;\n" +
 			"\n" +
 			"void main()\n" +
 			"{\n" +
 			"  gl_FragColor = color;\n" +
 			"}";
 
 		ShaderProgram shaderProgram = new ShaderProgram(vertex, fragment);
 		System.out.println("PLCompiled: " + shaderProgram.isCompiled() + "----------");
 		return shaderProgram;
 	}
 }
