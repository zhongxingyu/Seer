 package com.galaev.tsp.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * Class {@code Matrix} represents
  * a matrix of costs of transitions
  * between the nodes of the graph.
  * The matrix consists of cells.
  * Note: matrix is square and
  * symmetric about the main diagonal.
  *
  * @author Anton Galaev
  * @see com.galaev.tsp.model.Cell
  */
 public class Matrix
         implements Comparable<Matrix>, Iterable<Cell>, Serializable {
 
     /* Total current cost of transitions*/
     private int cost;
 
     /* Current size of the matrix*/
     private int size;
 
     /* Index of the current node */
     private int current;
 
     /* Matrix itself */
     private List<Cell> matrix;
 
     /* List of performed transitions */
     private List<Transition> transitions;
 
     /* Remained ways, i.e. indices of unvisited nodes */
     private List<Integer> remaining;
 
     /**
      * Getter for total cost.
      *
      * @return total current cost of transitions
      */
     public int getCost() {
         return cost;
     }
 
     /**
      * Setter for total current cost of transitions.
      *
      * @param cost current cost
      */
     public void setCost(int cost) {
         this.cost = cost;
     }
 
     /**
      * Getter for size of the matrix.
      *
      * @return size of the matrix
      */
     public int getSize() {
         return size;
     }
 
     /**
      * Setter for size of the matrix.
      *
      * @param size size of the matrix
      */
     public void setSize(int size) {
         this.size = size;
     }
 
     /**
      * Returns the length of the list, that
      * contains the matrix, i.e. returns
      * result of cells in the matrix.
      *
      * @return length of matrix list
      */
     public int getListLength() {
         return matrix.size();
     }
 
     /**
      * Returns the cell for its index in the list.
      *
      * @param index cell position in the list
      * @return cell at position {@code index}
      * @throws IndexOutOfBoundsException
      */
     public Cell getCell(int index)
             throws IndexOutOfBoundsException {
         return matrix.get(index);
     }
 
     /**
      * Returns the cell, that represents
      * transition from the node with {@code from} index
      * to the node with {@code to} index.
      *
      * @param from start node index in the matrix
      * @param to end node index in the matrix
      * @return cell with matrix indices
      *         {@code from} and {@code to}.
      */
     public Cell findCell(int from, int to) {
         for (Cell cell : matrix) { // find the cell
             if (cell.getFrom() == from &&
                 cell.getTo() == to) { // if indices match
                 return cell; // the cell was found
             }
         }
         return null; // nothing was found
     }
 
     /**
      * Blocks the cell, that represents
      * transition from the node with {@code from} index
      * to the node with {@code to} index.
      * In branch & bound algorithm positive infinity
      * is in fact recorded to the cell.
      * Thus, the cell is not available to transit through.
      *
      * @param from start node index in the matrix
      * @param to end node index in the matrix
      */
     public void blockCell(int from, int to) {
         Cell cell = findCell(from, to);
         if (cell == null) return;
         int index = matrix.indexOf(cell);
         matrix.set(index, new Cell(cell, -1));
     }
 
     /**
      * Decreases on {@code sub} the value of the cell
      * which index is {@code index} in the matrix list.
      * Note: the actual cell is not changed. In fact, it is
      * replaced with the new cell with diminished value.
      *
      * @param index index of the cell in the list
      * @param sub value to be subtracted from the cell value
      */
     public void decreaseCellValue(int index, int sub) {
         Cell cell = getCell(index); // find cell
         matrix.set(index, new Cell(cell, cell.getValue() - sub)); // replace
     }
 
     /**
      * Removes the cell with index {@code index}
      * from the matrix list completely.
      *
      * @param index index of the cell in the list
      */
     public void removeCell(int index) {
         matrix.remove(index);
     }
 
     /**
      * Finds index of the next node to go to from the current node.
      * Returns {@code -1}, if there are no available
      * nodes to perform transition.
      *
      * @return index of the next node in the route
      */
     public int findNextNode() {
         for (int i : remaining) {
             if (findCell(getCurrent(), i).getValue() == 0) { // if we can go there
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Getter for index of the current node.
      *
      * @return index of the last visited node.
      */
     public int getCurrent() {
         return current;
     }
 
     /**
      * Setter for index of the current node.
      *
      * @param current index of the last visited node.
      */
     public void setCurrent(int current) {
         this.current = current;
         remaining.remove(new Integer(current)); // cannot go there again
     }
 
     /**
      * Getter for transitions list.
      * Returns current list of performed transitions.
      *
      * @return list of performed transitions
      */
     public List<Transition> getTransitions() {
         return transitions;
     }
 
     /**
      * Adds new transition to the list of performed transitions.
      *
      * @param from start node index of the transition
      * @param to end node index of the transition
      */
     public void addTransition(int from, int to) {
         transitions.add(new Transition(from, to));
     }
 
     /**
      * Public copy constructor.
      * Creates a shallow copy of given matrix.
      *
      * @param copy matrix to copy
      */
     public Matrix(Matrix copy) {
         size = copy.size;
         cost = copy.cost;
         current = copy.current;
         matrix = new ArrayList<>();
         transitions = new ArrayList<>();
         remaining = new ArrayList<>();
         matrix.addAll(copy.matrix);
         transitions.addAll(copy.transitions);
         remaining.addAll(copy.remaining);
     }
 
     /**
      * Public Matrix constructor.
      * Creates a matrix, that corresponds to
      * a Cell list, given as a parameter.
      *
      * @param cells List of Cells
      */
     public Matrix(List<Cell> cells) {
         size = cells.get(cells.size() - 1).getFrom() + 1;
         matrix = cells;
         transitions = new ArrayList<>();
         remaining = new ArrayList<>();
         for (int i = 0; i < size; ++ i) {
             remaining.add(i);
         }
     }
 
     /**
      * Public Matrix constructor.
      * Creates a matrix, that corresponds to
      * two-dimensional integer array, given as a parameter.
      *
      * @param cells 2-dim int array
      */
     public Matrix(int[][] cells) {
         size = cells[0].length;
         matrix = new ArrayList<>(size * size);
         for (int i = 0; i < size; ++ i) {
             for (int j = 0; j < size; ++ j) {
                 Cell cell = new Cell(cells[i][j], i, j);
                 matrix.add(cell);
             }
         }
         transitions = new ArrayList<>();
         remaining = new ArrayList<>();
         for (int i = 0; i < size; ++ i) {
             remaining.add(i);
         }
     }
 
     /**
      * Public Matrix constructor.
      * Creates a matrix, that corresponds to
      * text description of matrix, provided by Scanner,
      * that is given as a parameter.
      *
      * @param inp input scanner
      */
     public Matrix(Scanner inp) {
         String line = inp.nextLine();
         String[] values = line.split(" ");
         size = values.length;
         matrix = new ArrayList<>();
         transitions = new ArrayList<>();
         remaining = new ArrayList<>();
         for (int i = 0; i < size; ++ i) {
             remaining.add(i);
         }
         for (int i = 0; i < size; ++ i) {
             for (int j = 0; j < size; ++ j) {
                matrix.add(new Cell(Integer.parseInt(values[j]), i, j));
             }
             if (inp.hasNextLine()) {
                 line = inp.nextLine();
                 values = line.split(" ");
             }
         }
     }
 
     /**
      * Returns a string representation of the object.
      * That is the matrix as text, divided with spaces.
      *
      * @return string, contatining the matrix
      */
     public String toString() {
         String str  = "";
         for (int i = 0; i < size; ++ i) {
             for (int j = 0; j < size; ++ j) {
                 str += matrix.get(size * i +j).getValue() + " ";
             }
             str += "\n";
         }
         return str;
     }
 
     /**
      * Compares this matrix to another.
      *
      * @param o another matrix
      * @return if this matrix has greater cost returns
      *         {@code true}, otherwise returns {@code false}
      */
     @Override
     public int compareTo(Matrix o) {
         return this.cost - o.cost;
     }
 
     /**
      * Returns an iterator for the matrix.
      *
      * @return matrix iterator
      */
     @Override
     public Iterator<Cell> iterator() {
         return new MatrixIterator();
     }
 
     /**
      * Inner iterator class.
      * Performs iteration over the matrix.
      */
     private class MatrixIterator
             implements Iterator<Cell> {
 
         /* Current index */
         private int index;
 
         /**
          * Package-private constructor.
          */
         MatrixIterator() {
             index = 0;
         }
 
         /**
          * Checks the next element existence.
          *
          * @return if there is a next element or it is the end
          */
         @Override
         public boolean hasNext() {
             return index < getListLength();
         }
 
         /**
          * Returns the next element.
          *
          * @return next element
          */
         @Override
         public Cell next() {
             if (hasNext()) {
                 return matrix.get(index++);
             } else {
                 throw new NoSuchElementException("MatrixIterator.next()");
             }

         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 }
