 package com.commsen.guicelet.test.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TestContext {
 
 	private static List<String> roots = new ArrayList<String>();
 	@SuppressWarnings("rawtypes")
 	private static List<Class> guicelets = new ArrayList<Class>();
 
 	public static List<String> getRoots() {
 		return roots;
 	}
 
 	public static void setRoots(List<String> roots) {
 		TestContext.roots = roots;
 	}
 
 	public static void addRoot(String root) {
 		TestContext.roots.add(root);
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static List<Class> getGuicelets() {
 		return guicelets;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static void setGuicelets(List<Class> guicelets) {
 		TestContext.guicelets = guicelets;
 	}
 	
	@SuppressWarnings("rawtypes")
 	public static void addGuicelet(Class guicelet) {
 		TestContext.guicelets.add(guicelet);
 	}
 
 }
