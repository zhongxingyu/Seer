 package com.lumpundform.utilerias;
 
import java.util.HashMap;
import java.util.Map;

 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.XmlReader;
 import com.badlogic.gdx.utils.XmlReader.Element;
 import com.lumpundform.escenario.DatosEscenario;
 
 public class D {
 	private static String nombreArchivo = "data/datos.xml";
 	private static Element datos;
	private static Map<String, SpriteSheet> spriteSheets = new HashMap<String, SpriteSheet>();
 
 	private static void init() {
 		XmlReader reader = new XmlReader();
 		datos = reader.parse(Gdx.files.internal(nombreArchivo).readString());
 	}
 
 	public static SpriteSheet ss(String nombre, String tipo) {
		String key = nombre + "_" + tipo;
		SpriteSheet spriteSheet = spriteSheets.get(key);
		if (spriteSheet == null) {
			Element spriteSheetGrupo = getSpriteGrupo(nombre);
			Element spriteSheetElement = getSpriteSheetTipo(spriteSheetGrupo, tipo);
 
			String ruta = spriteSheetElement.getChildByName("ruta").getText();
			int columnas = Integer.parseInt(spriteSheetElement.getChildByName("columnas").getText());
			int columnasOffset = Integer.parseInt(spriteSheetElement.getChildByName("columnasOffset").getText());
			int renglones = Integer.parseInt(spriteSheetElement.getChildByName("renglones").getText());
			int renglonesOffset = Integer.parseInt(spriteSheetElement.getChildByName("renglonesOffset").getText());
 
			spriteSheet = new SpriteSheet(ruta, columnas, columnasOffset, renglones, renglonesOffset);
			spriteSheets.put(key, spriteSheet);
		}
		return spriteSheet;
 	}
 
 	public static DatosEscenario de(String nombre) {
 		Element escenario = getEscenario(nombre);
 
 		String archivoTmx = escenario.getChildByName("archivo_tmx").getText();
 		String atlas = escenario.getChildByName("atlas").getText();
 		String fondo = escenario.getChildByName("fondo").getText();
 
 		return new DatosEscenario(archivoTmx, atlas, fondo);
 	}
 
 	private static Element getEscenario(String nombre) {
 		Array<Element> escenarios = getDatos().getChildrenByNameRecursively("Escenario");
 		for (Element escenario : escenarios) {
 			if (escenario.getAttribute("nombre").equals(nombre)) {
 				return escenario;
 			}
 		}
 		return null;
 	}
 
 	private static Element getSpriteSheetTipo(Element spriteSheetGrupo, String tipo) {
 		Array<Element> spriteSheets = spriteSheetGrupo.getChildrenByNameRecursively("SpriteSheet");
 		for (Element spriteSheet : spriteSheets) {
 			if (spriteSheet.getAttribute("tipo").equals(tipo)) {
 				return spriteSheet;
 			}
 		}
 		return null;
 	}
 
 	private static Element getSpriteGrupo(String nombre) {
 		Array<Element> spriteSheetGrupos = getDatos().getChildrenByNameRecursively("SpriteSheetGrupo");
 		for (Element grupo : spriteSheetGrupos) {
 			if (grupo.getAttribute("nombre").equals(nombre)) {
 				return grupo;
 			}
 		}
 		return null;
 	}
 
 	private static Element getDatos() {
 		if (datos == null) {
 			init();
 		}
 		return datos;
 	}
 }
