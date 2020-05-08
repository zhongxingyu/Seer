 package graph;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mmaxy
  */
 public class Graph {
 
     private int layers;
     private Node[] vertex;
     private Map<Integer, String> dict;
 
     {
         dict = new HashMap<Integer, String>();
         dict.put(1, "public");
         dict.put(2, "private");
     }
 
     /**
      * Конструктор
      * @param size размер графа
      * @throws IllegalArgumentException
      */
     public Graph(int size) throws IllegalArgumentException {
         if (size <= 0) throw new IllegalArgumentException("Количество вершин в графе должно быть строго больше 0");
         this.vertex = new Node[size];
         for (int i = 0; i < size; i++) vertex[i] = new Node();
     }
 
     /**
      * Новый размер графа
      * @param newSize новый размер
      * @throws IllegalArgumentException
      */
     public void setGraphSize(int newSize) throws IllegalArgumentException {
         if (newSize <= 0) throw new IllegalArgumentException("Количество вершин в графе должно быть строго больше 0");
         vertex = new Node[newSize];
     }
 
     /**
      * Строим граф из матрицы
      * @param adjacencyMatrix собственно, матрица(КВАДРАТНАЯ)
      * @throws IllegalArgumentException
      */
     public void setGraphFromMatrix(int[][] adjacencyMatrix) throws IllegalArgumentException {
         if (adjacencyMatrix == null) throw new IllegalArgumentException();
         if (adjacencyMatrix.length != adjacencyMatrix[0].length)
             throw new IllegalArgumentException("Матрица должна быть квадратная");
         if (adjacencyMatrix.length != this.vertex.length)
             throw new IllegalArgumentException("Матрица должна соответствовать количеству вершин");
 
         for (int i = 0; i < adjacencyMatrix.length; i++) {
             for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                 if (adjacencyMatrix[i][j] == 0) continue;
                 if (adjacencyMatrix[i][j] > 2)
                     throw new IllegalArgumentException("В матрице можно использовать только 0 1 и 2");
                 this.vertex[i].addUsing(j);
                 this.vertex[j].addUsedBy(i);
                 this.vertex[i].setAccessModifier(adjacencyMatrix[i][j]);
             }
         }
     }
 
     /**
      * Строим граф из листа
      * @param list лист, из которого строить граф
      * @throws IllegalArgumentException
      */
     public void setGraphFromList(int[][] list) throws IllegalArgumentException {
         if (list == null) throw new IllegalArgumentException();
         if (list[0].length != 3)
             throw new IllegalArgumentException("список должен состоять из трех элементов. Neither more, nor less");
         for (int i = 0; i < list.length; i++) {
             if (list[i][2] == 0) continue;
             this.vertex[list[i][0]].addUsing(list[i][1]);
             this.vertex[list[i][1]].addUsedBy(list[i][0]);
             this.vertex[i].setAccessModifier(list[i][2]);
         }
     }
 
     /**
      * Читаем из файла
      * @param fr FileReader, из которого читать
      * @return массив прочитанного(может быть как квадратным, так и 2*n)
      * @throws Exception
      */
     public static int[][] readFromFile(FileReader fr) throws Exception {
         List<int[]> res = new ArrayList<int[]>();
         BufferedReader br = new BufferedReader(fr);
         String[] splited;
         try {
             String line = br.readLine();
             if (line.equals("List")) {
                 line = br.readLine();
                 while (line != null) {
                     splited = line.split("\\s+");
                     if (splited.length < 3) throw new Exception("Неправильное форматирование файла");
                     res.add(new int[]{Integer.parseInt(splited[0]), Integer.parseInt(splited[1]), Integer.parseInt(splited[2])});
                     line = br.readLine();
                 }
             } else if (line.equals("Matrix")) {
                 //Строка - i(откуда)
                 //Столбец - j(куда)
                 line = br.readLine();
                 splited = line.split("\\s+");
                 int numberOfLines = 1;
                 int[] tmp = new int[splited.length];
                 for (int i = 0; i < splited.length; i++) {
                     tmp[i] = Integer.parseInt(splited[i]);
                 }
                 res.add(tmp);
                 line = br.readLine();
                 while (line != null) {
                     splited = line.split("\\s+");
                     if (splited.length != tmp.length || numberOfLines > tmp.length)
                         throw new Exception("Неправильное форматирование файла, матрица не квадратная");
                     tmp = new int[splited.length];
                     for (int i = 0; i < splited.length; i++) {
                         tmp[i] = Integer.parseInt(splited[i]);
                     }
                     res.add(tmp);
                     numberOfLines++;
                     line = br.readLine();
                 }
             } else {
                 throw new Exception("Неправильное форматирование файла");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             br.close();
         }
 
         return res.toArray(new int[res.size()][]);
     }
 
     /**
      * Строит матрицу смежности
      * @return матрицу смежности
      */
     public int[][] buildAdjacencyMatrix() {
         int[][] res = new int[this.vertex.length][];
         for (int i = 0; i < this.vertex.length; i ++) {
             res[i] = new int[this.vertex.length];
         }
 
         for (int i = 0; i < this.vertex.length; i++) {
             List<Integer> connections = this.vertex[i].getUsing();
             for (Integer connection : connections) {
                 res[i][connection] = this.vertex[connection].getAccessModifier();
             }
         }
 
         return res;
     }
 
     /**
      * Вернуть массив вершин
      * @return массив вершин
      */
     public Node[] getVertexes() {
         return this.vertex;
     }
 
     /**
      * Вернуть массив вершин
      * @return массив вершин
      */
     public void setVertexes(Node[] newVertex) {
         this.vertex = newVertex;
     }
 
     /**
      * Удаляет связь от вершины к вершине
      * @param from от какой вершины
      * @param to до какой вершины
      */
     public void removeReference(int from, int to) {
         int i;
         List<Integer> using = vertex[from].getUsing();
         for (i = 0; i < using.size(); i++) {
             if (using.get(i) == to)
                 break;
         }
         this.vertex[from].getUsing().remove(i);
 
         List<Integer> usedBy = vertex[to].getUsedBy();
         for (i = 0; i < usedBy.size(); i++) {
             if (usedBy.get(i) == from)
                 break;
         }
         this.vertex[to].getUsedBy().remove(i);
     }
 
     /**
      * Находит все слои
      */
     public void findLayers() {
         List<Node> vert = new ArrayList<Node>();
 
         Collections.addAll(vert, this.vertex);
 
         findL(vert);
         int i = 1;
         while (searchNextLayer(i)) i++;
         this.layers = i;
     }
 
     public int getLayers() {
         return this.layers;
     }
 
     /**
      * Поиск следующего слоя. Убирает вершины, слой которых определен, из оставшихся строит новый граф и находит слой
      * @param currentLayer сколько уже слоев проставили
      * @return истина, если были вершины для распределения. Ложь, если не было.
      */
     private boolean searchNextLayer(int currentLayer) {
         List<Node> graph = new ArrayList<Node>();
         Map<Integer, Integer> mapGraphToVer = new HashMap<Integer, Integer>();
         Map<Integer, Integer> mapVerToGraph = new HashMap<Integer, Integer>();
 
         for (int i = 0; i < vertex.length; i++) {
             if (vertex[i].getLayer() == 0 && vertex[i].getAccessModifier() == 1) {
                 graph.add(new Node());
                 mapGraphToVer.put(graph.size() - 1, i);
                 mapVerToGraph.put(i, graph.size() - 1);
             }
         }
         if (graph.size() == 0) return false;
         for (int i = 0; i < graph.size(); i ++) {
             graph.get(i).setAccessModifier(vertex[mapGraphToVer.get(i)].getAccessModifier());
             List<Integer> usedBy = vertex[mapGraphToVer.get(i)].getUsedBy();
             for (Integer u : usedBy) {
                 if (mapVerToGraph.containsKey(u)) {
                     graph.get(i).addUsedBy(mapVerToGraph.get(u));
                 }
             }
             List<Integer> using = vertex[mapGraphToVer.get(i)].getUsing();
             for (Integer u : using) {
                 if (mapVerToGraph.containsKey(u)) {
                     graph.get(i).addUsing(mapVerToGraph.get(u));
                 }
             }
         }
         findL(graph);
 
         for (int i = 0; i < graph.size(); i++) {
             if (graph.get(i).getLayer() != 0) {
                 vertex[mapGraphToVer.get(i)].setLayer(i+1);
             }
         }
         return true;
     }
 
     /**
      * Ищет нижний слой
      * @param graph граф, в котором ищет
      */
     private void findL(List<Node> graph) {
         for (Node aVertex : graph) {
             if (aVertex.getSizeOfUsing() == 0 && aVertex.getAccessModifier() != 2) {
                 aVertex.setLayer(1);
             }
         }
         int[] crossHandled = findCrossHandledModules(graph);
         for (int aCrossHandled : crossHandled) {
             graph.get(aCrossHandled).setLayer(1);
         }
         List<Integer> loop = findFreeLoop(graph);
         for (int aLoop : loop) {
             graph.get(aLoop).setLayer(1);
         }
     }
 
     /**
      * Ищет модли, использующие друг-друга на этом уровне.
      * @param graph граф, в котором ищет
      * @return список вершин
      */
     private int[] findCrossHandledModules(List<Node> graph) {
         List<Integer> res = new ArrayList<Integer>();
         List<Integer> alreadyChecked = new ArrayList<Integer>();
         for (int i = 0; i < graph.size(); i++) {
             if (!alreadyChecked.contains(i)) {
                 List<Integer> tmp = new ArrayList<Integer>();
                 if (check(graph, tmp, i)) {
                     for (Integer aTmp : tmp)
                     if (!res.contains(aTmp))
                         res.add(aTmp);
                 }
                 alreadyChecked.add(i);
                 for (Integer aTmp : tmp)
                     if (!alreadyChecked.contains(aTmp))
                         alreadyChecked.add(aTmp);
             }
         }
         int[] resArray = new int[res.size()];
         for (int i = 0; i < res.size(); i++) {
             resArray[i] = res.get(i);
         }
         return resArray;
     }
 
     /**
      * Проверка на кросс связность
      * @param graph граф, в котором проверять
      * @param alreadyChecked проверяли ли уже
      * @param v вершина, в которой стоим
      * @return все ли хорошо
      */
     private boolean check(List<Node> graph, List<Integer> alreadyChecked, int v) {
         boolean res = true;
         List<Integer> using = graph.get(v).getUsing();
         if (alreadyChecked.contains(v)) {
             return true;
         }
         alreadyChecked.add(v);
         for (int j = 0; j < using.size(); j++) {
             if (graph.get(using.get(j)).getUsing().contains(v)) {
                 res = res && check(graph, alreadyChecked, j);
             } else {
                 res = false;
             }
         }
         return res;
     }
 
     /***
      * Найди свободный цикл, как в примере CDE, то есть множество вершин, которые используют друг друга и
      * модули, не имеющие зависимостей никаких, кроме базового слоя.
      * @param graph граф, в котором надо искать
      * @return массив индексов таких точек
      */
     private List<Integer> findFreeLoop(List<Node> graph) {
         List<Integer> res = new ArrayList<Integer>();
        //TODO здесь нужно искать свободный цикл, то есть такой, который использует либо себя, либо еще модули с нижнего уровня
         List<Integer> allLoops = findAllLoops();
         for (Node node : graph) {
             boolean innerUsedBy = true;
             boolean innerUsing = true;
             List<Integer> usedByIndexes = node.getUsedBy();
             List<Integer> usingIndexes = node.getUsing();
             for (Integer i : usedByIndexes) {
                 if (!(graph.contains(vertex[i]) && allLoops.contains(i))) {
                     innerUsedBy = false;
                 }
             }
             for (Integer i : usingIndexes) {
                 if (!(graph.contains(vertex[i]) && allLoops.contains(i))) {
                     innerUsing = false;
                 }
             }
             if (innerUsedBy && innerUsing) {
                res.add(indexOfPoint(node));
             }
         }
         return res;
     }
 
     public List<Integer> findAllLoops() {
         List<Integer> result = new ArrayList<Integer>();
         Map<Node, Integer> markMap = new HashMap<Node, Integer>();
         for (Node p : vertex) {
             markMap.put(p, 0);
         }
 
         markMap = depthFirstSearch(vertex[0], markMap);
         for (Node p : markMap.keySet()) {
             if (markMap.get(p) == 1)
                 result.add(indexOfPoint(p));
         }
         return result;
     }
 
     private int indexOfPoint(Node node) {
         for (int i = 0; i < vertex.length; i++) {
             if (vertex[i] == node) {
                 return i;
             }
         }
         return -1;
     }
 
     private Map<Node, Integer> depthFirstSearch(Node currentNode, Map<Node, Integer> markMap) {
         List<Integer> neighbourIndexes = currentNode.getUsing();
         List<Node> neighbours = new ArrayList<Node>();
         for (Integer i : neighbourIndexes) {
             neighbours.add(vertex[i]);
         }
         if (neighbours.isEmpty()) {
             markMap.put(currentNode, 2);
             return markMap;
         }
         markMap.put(currentNode, 1);
         boolean containsGray = false;
         boolean containsBlack = false;
         List<Node> notMarked = new ArrayList<Node>();
         for (Node neighbour : neighbours) {
             if (!markMap.containsKey(neighbour)) {
                 continue;
             }
             switch (markMap.get(neighbour)) {
                 case 0:
                     notMarked.add(neighbour);
                     break;
                 case 1:
                     containsGray = true;
                     break;
                 case 2:
                     containsBlack = true;
                     break;
                 default: break;
             }
         }
             if (notMarked.isEmpty()) {
                 if (containsGray) {
                     markMap.put(currentNode, 1);
                     return markMap;
                 }
                 if (containsBlack) {
                     markMap.put(currentNode, 2);
                     return markMap;
                 }
             }
             for (Node p : notMarked) {
                 markMap = depthFirstSearch(p, markMap);
             }
             switch (analyzeNeighbours(neighbours, markMap)) {
                 case 1:
                     markMap.put(currentNode, 1);
                     break;
                 case 2:
                     markMap.put(currentNode, 2);
                     break;
                 case 0:
                     default:
                         break;
             }
         return markMap;
     }
 
     private int analyzeNeighbours(List<Node> neighbours, Map<Node, Integer> markMap) {
         boolean containsGray = false;
         boolean containsBlack = false;
         boolean containsEmpty = false;
         for (Node neighbour : neighbours) {
             if (!markMap.containsKey(neighbour)) {
                 continue;
             }
             switch (markMap.get(neighbour)) {
                 case 0:
                     containsEmpty = true;
                     break;
                 case 1:
                     containsGray = true;
                     break;
                 case 2:
                     containsBlack = true;
                     break;
                 default: break;
             }
         }
         if (containsEmpty)
             return 0;
         if (containsGray)
             return 1;
         if (containsBlack)
             return 2;
         return -1;
     }
 
     public List<Connection>  findAllConflictBindings() {
         List<Connection> result = new ArrayList<Connection>();
         for (Node node : vertex) {
             List<Integer> usingIndexes = node.getUsing();
             for (Integer i : usingIndexes) {
                 if (vertex[i].getLayer() > node.layer) {
                     result.add(new Connection(node, vertex[i]));
                 }
             }
         }
         return result;
     }
 
     /**
      * Класс модуля
      */
     public class Node {
 
         private int accessModifier;
         private int layer;
         private List<Integer> using;
         private List<Integer> usedBy;
 
         public Node() {
             this.using = new ArrayList<Integer>();
             this.usedBy = new ArrayList<Integer>();
         }
 
         public int getAccessModifier() {
             return accessModifier;
         }
 
         public void setAccessModifier(int accessModifier) {
             this.accessModifier = accessModifier;
         }
 
         public String getStringAccessModifier() {
             return dict.get(this.accessModifier);
         }
 
         public int getLayer() {
             return layer;
         }
 
         public void setLayer(int layer) {
             this.layer = layer;
         }
 
         public List<Integer> getUsing() {
             return using;
         }
 
         public void setUsing(List<Integer> using) {
             this.using = using;
         }
 
         public void addUsing(int newPoint) {
             this.using.add(newPoint);
         }
 
         public int getSizeOfUsing() {
             return this.using.size();
         }
 
         public List<Integer> getUsedBy() {
             return usedBy;
         }
 
         public void setUsedBy(List<Integer> usedBy) {
             this.usedBy = usedBy;
         }
 
         public void addUsedBy(int newPoint) {
             this.usedBy.add(newPoint);
         }
 
         public int getSizeOfUsedBy() {
             return this.usedBy.size();
         }
     }
 
 }
