 package com.mimascota.mimascota;
 
 
 public class Constantes {
	static String IPSERVER = "192.168.1.34";
 	static int viewerWidth = 256;
 	static int viewerHeight = 179;
 	static boolean debug = false; //sirve para saltear protecciones de llenado de formulario, foto y ubicacion
 	static boolean saltearLogin = false;
 	public static void setIpServer(String string) {
 		IPSERVER = string;		
 	}
 }
