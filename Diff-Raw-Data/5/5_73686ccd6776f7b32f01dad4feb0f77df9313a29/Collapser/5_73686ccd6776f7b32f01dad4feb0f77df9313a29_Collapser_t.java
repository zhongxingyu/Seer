 package eclipsedsm.model;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public final class Collapser {
 
 	private Collapser() {
 		// no code
 	}
 
 	public static void collapse(List<RowElement> verticals, List<ColumnElement> horizontals) {
 		while (commonParent(verticals)) {
 			collapse(verticals, RowElement.class);
 		}
 		removeOnechildRoots(verticals);
 		removeOnechildPaths(verticals);
 
 		while (commonParent(horizontals)) {
 			collapse(horizontals, ColumnElement.class);
 		}
 		removeOnechildRoots(horizontals);
 		removeOnechildPaths(horizontals);
 	}
 
 	private static <T extends Element<T>> void removeOnechildPaths(List<T> elements) {
 		if (elements == null) {
 			return;
 		}
 		for (T element : elements) {
 			while (element.getChildren() != null && element.getChildren().size() == 1
 					&& element.getChildren().get(0).getChildren() != null) {
 				List<T> realChildren = element.getChildren().get(0).getChildren();
 				element.getChildren().clear();
 				element.getChildren().addAll(0, realChildren);
 				for (T realChild : realChildren) {
 					realChild.setParent(element);
 				}
 			}
 			removeOnechildPaths(element.getChildren());
 		}
 	}
 
 	private static <T extends Element<T>> void removeOnechildRoots(List<T> elements) {
 		if (elements == null) {
 			return;
 		}
 		while (elements.size() == 1 && elements.get(0).getChildren() != null) {
 			List<T> children = elements.get(0).getChildren();
 			elements.clear();
 			elements.addAll(children);
 			for (T child : children) {
 				child.setParent(null);
 			}
 		}
 		for (int i = 0; i < elements.size(); i++) {
 			while (elements.get(i).getChildren() != null && elements.get(i).getChildren().size() == 1) {
 				elements.set(i, elements.get(i).getChildren().get(0));
 				elements.get(i).setParent(null);
 			}
 
 		}
 	}
 
 	private static <T extends Element<T>> void collapse(List<T> elements, Class<T> elementClass) {
 		Map<String, String[]> splittedNames = new HashMap<String, String[]>();
 		Map<String, T> elementsMap = new HashMap<String, T>();
 		int maxsize = 0;
 		Map<String, List<T>> groupsWithMaxSize = new HashMap<String, List<T>>();
 
 		for (T element : elements) {
 			String[] splitName = element.getName().split("\\.");
 			String name = element.getName();
 			if (splitName.length > maxsize) {
 				maxsize = splitName.length;
 				groupsWithMaxSize.clear();
 			}
 			if (splitName.length == maxsize) {
 				insertIntoGroups(groupsWithMaxSize, parentPackageName(splitName), element);
 			}
 			splittedNames.put(name, splitName);
 			elementsMap.put(element.getName(), element);
 		}
 		for (Entry<String, List<T>> entry : groupsWithMaxSize.entrySet()) {
 			int insertingIndex = insertingIndex(entry.getKey(), elements);
 			T newParent = null;
 			try {
				@SuppressWarnings("unchecked")
				Constructor<T> constructor = (Constructor<T>) elementClass.getConstructors()[0];
				newParent = constructor.newInstance(entry.getKey());
 			} catch (Exception e) {
 				throw new IllegalStateException("Unsupproted construcor was met");
 			}
 			newParent.setChildren(entry.getValue());
 			for (T child : entry.getValue()) {
 				child.setParent(newParent);
 			}
 			elements.add(insertingIndex, newParent);
 			elements.removeAll(entry.getValue());
 		}
 	}
 
 	private static <T extends Element<T>> boolean commonParent(List<T> elements) {
 		Set<String> parentNames = new HashSet<String>();
 		for (T element : elements) {
 			parentNames.add(element.getName().split("\\.")[0]);
 		}
 		return elements.size() != parentNames.size();
 	}
 
 	private static <T extends Element<T>> int insertingIndex(String parentPackageName, List<T> elements) {
 		for (int i = 0; i < elements.size(); i++) {
 			T element = elements.get(i);
 			if (parentPackageName.equals(parentPackageName(element.getName().split("\\.")))) {
 				return i;
 			}
 		}
 		throw new IllegalArgumentException("No parent package name " + parentPackageName + "in list" + elements);
 	}
 
 	private static String parentPackageName(String[] splitName) {
 		StringBuilder result = new StringBuilder();
 		for (int i = 0; i < (splitName.length - 1); i++) {
 			result.append(splitName[i]);
 			if (i != splitName.length - 2) {
 				result.append(".");
 			}
 		}
 		return result.toString();
 	}
 
 	private static <T extends Element<T>> void insertIntoGroups(Map<String, List<T>> groups, String name, T element) {
 		List<T> group = groups.get(name);
 		if (group == null) {
 			group = new ArrayList<T>();
 			groups.put(name, group);
 		}
 		group.add(element);
 
 	}
 
 }
