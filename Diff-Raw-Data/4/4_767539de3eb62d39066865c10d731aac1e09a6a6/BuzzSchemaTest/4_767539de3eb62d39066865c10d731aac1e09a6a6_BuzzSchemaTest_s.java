 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.json;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.beequeue.util.Files;
 import org.beequeue.util.ToStringUtil;
 import org.junit.Test;
 
 import com.fasterxml.jackson.annotation.JsonValue;
 import com.fasterxml.jackson.core.type.TypeReference;
 
 public class BuzzSchemaTest {
 	public static class A {
 		public String s;
 		public int i;
 		public Date date;
 		public Boolean getB(){
 			return true;
 		}
 		public void setB(Boolean b){
 		}
 	}
 	public static enum C { X, Y, Z } ;
 
 	public static class D {
 		
 		public static D valueOf(String s){
 			return new D();
 		}
 		
 		@JsonValue
 		public String toString(){
 			return null;
 		}
 	}
 	public static class B {
 		public A aga;
 		public C qqq;
 		public D ddd;
 	}
 	
 	public static class Q {
 		public List<A> as;
 		public C[] cs;
 		public List<String> ss;
 		public float[] fs;
 		public Map<String,Integer> m0;
 		public Map<C,D> m1;
 		public Map<D,A> m2;
 	}
 
 	public static class X<T> {
 		public T t;
 		public X<Q> xq;
 		public String s;
 	}
 	
 	@Test
 	public void mapAttribute() throws IOException {
 		BuzzSchemaBuilder bsb = new BuzzSchemaBuilder();
 		bsb.schema.object = bsb.add(new TypeReference<X<B>>(){});
 		ToStringUtil.out(bsb.schema);
 	}
 	@Test
 	public void test() throws IOException {
 		BuzzSchemaBuilder bsb = new BuzzSchemaBuilder();
 		bsb.schema.object = bsb.add(new TypeReference<X<B>>(){});
 		ToStringUtil.out(bsb.schema);
 		Class<? extends BuzzSchemaTest> c = getClass();
 		String dir = c.getPackage().getName().replaceAll("\\.", "/");
 		String xbPath = dir + "/xb.json";
//		Files.writeAll(new File("test/"+xbPath), ToStringUtil.toString(bsb.schema));
 		String readAll = Files.readAll(new InputStreamReader(c.getResourceAsStream("/"+xbPath)));
 		Assert.assertEquals(readAll, ToStringUtil.toString(bsb.schema));
 		A a = new A();
 		a.date = new Date();
 		A o = ToStringUtil.toObject(ToStringUtil.toString(a),A.class);
 		ToStringUtil.out(o);
 		ToStringUtil.out(a);
 		Assert.assertEquals(
 				ToStringUtil.toString(a),
 				ToStringUtil.toString(o));
 		
 	}
 
 
 }
