 /*
  * Copyright 2013 Webster Smalley
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.webstersmalley.countdown.numbers;
 
 public class CalculationNode implements Node {
     private boolean integerResult;
     private int value;
     private Node nodeA;
     private Node nodeB;
     private Operator operator;
 
     public CalculationNode(Node nodeA, Node nodeB, Operator operator) {
        if (nodeA.getValue() < nodeB.getValue()) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
        } else if (nodeA.getValue() > nodeB.getValue()) {
            this.nodeA = nodeB;
            this.nodeB = nodeA;
        } else {
            if (nodeA.getComplexity() < nodeB.getComplexity()) {
                 this.nodeA = nodeA;
                 this.nodeB = nodeB;
            } else {
                 this.nodeA = nodeB;
                 this.nodeB = nodeA;
             }
         }
 
         this.operator = operator;
         calculate();
     }
 
     public String toString() {
         return "(" + nodeA + " " + operator + " " + nodeB + ")";
     }
 
     private void calculate() {
         double a = (double) nodeA.getValue();
         double b = (double) nodeB.getValue();
         double value = operator.eval(a, b);
         integerResult = (Math.abs(value - Math.round(value)) < 0.0000001);
         if (integerResult) {
             this.value = (int) value;
         }
     }
 
     public int getValue() {
         return value;
     }
 
     @Override
     public boolean equals(Object oNode) {
         if (this == oNode) {
             return true;
         }
         if (oNode instanceof CalculationNode) {
             CalculationNode cNode = (CalculationNode) oNode;
             return this.nodeA.equals(cNode.nodeA) && this.nodeB.equals(cNode.nodeB) && this.operator.equals(cNode.operator) ||
                     this.nodeA.equals(cNode.nodeB) && this.nodeB.equals(cNode.nodeA) && (this.operator == Operator.PLUS || this.operator == Operator.TIMES);
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return nodeA.hashCode() + 31 * nodeB.hashCode() + 217 * operator.hashCode();
     }
 
     public boolean givesIntegerResult() {
         return integerResult;
     }
 
     public int getComplexity() {
         return 1 + nodeA.getComplexity() + nodeB.getComplexity();
     }
 }
