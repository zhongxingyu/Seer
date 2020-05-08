 /**
  *    Copyright 2013 MegaFon
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
 package ru.histone.optimizer;
 
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import org.junit.Test;
 import ru.histone.HistoneException;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class FragmentsConcatinationOptimizerTest extends AbstractOptimizersTest {
 
     @Test
     public void test_join() throws IOException, HistoneException {
         ArrayNode input = (ArrayNode) getJackson().readTree("[\"AAA\",\"BBB\"]");
         ArrayNode expeced = (ArrayNode) getJackson().readTree("[\"AAABBB\"]");
 
         ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         assertEquals(expeced.toString(), ast.toString());
     }
 
     @Test
     public void test_singleElement() throws IOException, HistoneException {
         ArrayNode input = (ArrayNode) getJackson().readTree("[[\"AAA\"]]");
         ArrayNode expected = (ArrayNode) getJackson().readTree("[\"AAA\"]");
 
         ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         assertEquals(expected.toString(), ast.toString());
     }
 
     @Test
     public void test_complex() throws IOException, HistoneException {
         ArrayNode input = (ArrayNode) getJackson().readTree("[[\"AAA\",\"BBB\",[\"C\",[\"C\",\"C\"]]]]");
         ArrayNode expected = (ArrayNode) getJackson().readTree("[\"AAABBBCCC\"]");
 
         ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         assertEquals(expected.toString(), ast.toString());
     }
 
     @Test
     public void test_keep_selectors() throws IOException, HistoneException {
         ArrayNode input = (ArrayNode) getJackson().readTree("[[\"AAA\",\"BBB\",[105, [\"x\", \"y\", \"z\"]]]]");
         ArrayNode expected = (ArrayNode) getJackson().readTree("[\"AAABBB\",[105, [\"x\", \"y\", \"z\"]]]");
 
         ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         assertEquals(expected.toString(), ast.toString());
     }
 
     @Test
     public void test_if_statements() throws IOException, HistoneException {
         ArrayNode input = (ArrayNode) getJackson().readTree("[[1000,[[[16],[\"asdfadsf\",\"zzz\"]]]]]");
        ArrayNode expected = (ArrayNode) getJackson().readTree("[1000,[[[16],[\"asdfadsfzzz\"]]]]");
 
         ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         assertEquals(expected.toString(), ast.toString());
     }
 
     @Test
     public void test_compatabilityCheck() throws IOException, HistoneException {
         String inputTpl = "a {{for item in itemsX}} b {{else}} c {{/for}} d";
         ArrayNode input = getHistone().parseTemplateToAST(inputTpl);
        ArrayNode expected = (ArrayNode) getJackson().readTree("[]");
 
         ArrayNode ast = getHistone().optimizeAST(input, OptimizationTypes.FRAGMENT_CONCATENATION, OptimizationTypes.ELIMINATE_SINGLE_NODE);
 
         System.out.println(input.get(1).toString());
         assertEquals(expected.toString(), ast.toString());
     }
 
 
 }
