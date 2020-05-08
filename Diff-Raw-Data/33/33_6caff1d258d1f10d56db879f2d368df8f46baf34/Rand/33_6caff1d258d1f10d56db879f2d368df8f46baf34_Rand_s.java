 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.evaluator.functions.global;
 
 import java.math.BigDecimal;
 
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.evaluator.nodes.NumberHistoneNode;
 
 /**
  * Returns value chosen pseudorandomly. <br/>
  */
 public class Rand extends GlobalFunction {
     public Rand(NodeFactory nodeFactory) {
         super(nodeFactory);
     }
 
     @Override
     public String getName() {
         return "rand";
     }
 
     @Override
    public Node execute(Node... args) throws GlobalFunctionExecutionException {
		NumberHistoneNode start = args[0].getAsNumber();
		NumberHistoneNode end = args[1].getAsNumber();
		BigDecimal minimum = start.getValue();
		BigDecimal maximum = end.getValue();
 		double multiply = Math.random();
		BigDecimal result = minimum.add(maximum.multiply(BigDecimal.valueOf(multiply))); 
 		return getNodeFactory().number(result);
    }
 }
