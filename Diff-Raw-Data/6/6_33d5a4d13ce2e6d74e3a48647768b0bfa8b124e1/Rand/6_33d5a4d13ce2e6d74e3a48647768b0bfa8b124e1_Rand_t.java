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
 		BigDecimal value = getNumber(args, 0);
 		final BigDecimal minimum = value == null ? BigDecimal.valueOf(Integer.MIN_VALUE) : value;
 		value = getNumber(args, 1);
 		final BigDecimal maximum = value == null ? BigDecimal.valueOf(Integer.MAX_VALUE) : value;
        final BigDecimal additional = maximum.subtract(minimum);
        		
 		double multiply = Math.random();
		BigDecimal result = minimum.add(additional.multiply(BigDecimal.valueOf(multiply)));
 		return getNodeFactory().number(result);
 	}
 
 	private BigDecimal getNumber(Node[] args, int i) {
 		if (args.length <= i)
 			return null;
 		BigDecimal value = null;
 		if (args[i].isNumber()) {
 			value = args[i].getAsNumber().getValue();
 		} else if (args[i].isString()) {
 			try {
 				value = new BigDecimal(args[0].getAsString().getValue());
 			} catch (Exception e) {
 				// if wrong format return null
 			}
 		}
 		return value;
 	}
 }
