 package org.jsonman;
 
 /*
  * Copyright 2013 Takao Nakaguchi.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import java.io.InputStream;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import net.arnx.jsonic.JSON;
 
 import org.apache.commons.lang3.tuple.Pair;
 import org.jsonman.node.MapNode;
 import org.jsonman.node.StringNode;
 import org.junit.Test;
 
 public class NodeFilteringTest {
 	@Test
 	public void test() throws Exception{
 		Node src = null;
 		try(InputStream is = NodeFilteringTest.class.getResourceAsStream("NodeFilteringTest_1.json")){
 			src = NodeFactory.create(JSON.decode(is));
 		}
 
 		// /attributes[name=class]
 		final Node target = src.createEmpty();
 		src.visit(new ArrayExpandingVisitor() {
 			@Override
 			public void accept(MapNode node) {
 				Node an = node.getChild("attributes");
 				if(an == null) return;
 				an.visit(new ArrayExpandingVisitor(){
 					@Override
 					public void accept(final MapNode node) {
 						Node nn = node.getChild("name");
 						if(nn == null) return;
 						nn.visit(new ArrayExpandingVisitor(){
 							@Override
 							public void accept(StringNode vnode) {
 								if(!"class".equals(vnode.getValue())) return;
 								copyTo(target, node);
 							}
 						});
 					}
 				});
 			}
 			void copyTo(Node target, Node node){
 				Deque<Pair<Node, Object>> paths = new LinkedList<Pair<Node, Object>>();
 				Node parent = node;
 				while(parent != null){
 					paths.add(Pair.of(parent, parent.getChildId()));
 					parent = parent.getParent();
 				}
 				Iterator<Pair<Node, Object>> it = paths.descendingIterator();
 				while(true){
 					Pair<Node, Object> ref = it.next();
 					Node n = null;
 					if(it.hasNext()){
 						n = ref.getLeft().createEmpty();
 					} else{
 						n = ref.getLeft();
 					}
 					if(target.isArray()){
 						target.appendChild(n);
 					} else if(target.isMap()){
 						target.appendChild(ref.getRight().toString(), n);
 					} else{
 						break;
 					}
 					if(it.hasNext()){
 						target = n;
 					} else{
 						break;
 					}
 				}
 			}
 		});
		System.out.println(JSON.encode(target.getValue()));
 	}
 }
