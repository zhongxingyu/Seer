 package com.cvparse.process;
 
 import static org.junit.Assert.*;
 
import java.io.IOException;

 import org.junit.Test;
 
 public class GCCCommandTest {
 
 	@Test
	public void testCompile() throws IOException {
		GCCCommand c = new GCCCommand();
		String out = c.compile("tst"+System.currentTimeMillis(), "test", code);
		assertNotNull(out);
 	}
 
 	@Test
 	public void testCompileAndTest() {
		//TODO fail("Not yet implemented");
 	}
 
	final static String code = 
			"#include <stdio.h>\n"
			+ "#include <stdlib.h>\n"
			+ "void play() \n"
			+ "{\n"
			+ "  int i;\n"
			+ "  printf(\"welcome to my world of pointers %p\", &i);\n"
			+ " printf(\"%d %d\", sizeof(i), i);\n"
			+ "}\n"
			+ "int main(void)\n"
			+ "{ \n"
			+ "  play();\n"
			+ "  return 0;\n"
			+ "}";
 }
