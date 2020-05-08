 package com.lumpundform.utilerias;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.XmlReader;
 import com.badlogic.gdx.utils.XmlReader.Element;
 import com.lumpundform.escenario.DatosEscenario;
 
 public class D {
 	private static String nombreArchivo = "data/datos.xml";
 	private static Element datos;
 
 	private static void init() {
 		XmlReader reader = new XmlReader();
 		datos = reader.parse(Gdx.files.internal(nombreArchivo).readString());
 	}
 
 	public static SpriteSheet ss(String nombre, String tipo) {
		// TODO: Hacer que lea únicamente el archivo XML si no loa ha leído
		// antes para el mismo personaje.
		Element spriteSheetGrupo = getSpriteGrupo(nombre);
		Element spriteSheet = getSpriteSheetTipo(spriteSheetGrupo, tipo);
 
		String ruta = spriteSheet.getChildByName("ruta").getText();
		int columnas = Integer.parseInt(spriteSheet.getChildByName("columnas").getText());
		int columnasOffset = Integer.parseInt(spriteSheet.getChildByName("columnasOffset").getText());
		int renglones = Integer.parseInt(spriteSheet.getChildByName("renglones").getText());
		int renglonesOffset = Integer.parseInt(spriteSheet.getChildByName("renglonesOffset").getText());
 
		return new SpriteSheet(ruta, columnas, columnasOffset, renglones, renglonesOffset);
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
