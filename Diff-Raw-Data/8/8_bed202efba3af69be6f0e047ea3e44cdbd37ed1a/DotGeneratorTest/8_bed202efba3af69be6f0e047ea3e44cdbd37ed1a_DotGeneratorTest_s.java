 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.neuron;
 
 import com.google.common.collect.Lists;
 import de.weltraumschaf.commons.IO;
 import de.weltraumschaf.neuron.cmd.SampleTree;
 import de.weltraumschaf.neuron.event.Observer;
 import de.weltraumschaf.neuron.node.Node;
 import de.weltraumschaf.neuron.shell.Environment;
 import de.weltraumschaf.neuron.shell.Token;
 import java.util.List;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class DotGeneratorTest {
 
     private final Environment env = new Environment(mock(Observer.class));
     private final DotGenerator sut = new DotGenerator(env);
 
     @Before
     public void reset() {
         env.reset();
     }
 
     @Test
     public void digraphWithNoNodes() {
         assertThat(sut.toString(), is(String.format(
                   "digraph neuron {%n"
                 + "}")));
     }
 
     public void digraphWithOneNodes() {
         final Node n1 = env.add();
         assertThat(sut.toString(), is(String.format(
                   "digraph neuron {%n"
                 + "  %d;%n"
                 + "}", n1.getId())));
     }
 
     @Test
     public void digraphWithTwoUnconnectedNodes() {
         final Node n1 = env.add();
         final Node n2 = env.add();
         assertThat(n1.equals(n2), is(false));
         assertThat(sut.toString(), is(String.format(
                   "digraph neuron {%n"
                 + "  %d;%n"
                 + "  %d;%n"
                 + "}", n1.getId(), n2.getId())));
     }
 
     @Test
     public void digraphWithTwoConnectedNodes() {
         final Node n1 = env.add();
         final Node n2 = env.add();
         n1.connect(n2);
         assertThat(sut.toString(), is(String.format(
                   "digraph neuron {%n"
                 + "  %d -> %d;%n"
                 + "}", n1.getId(), n2.getId())));
     }
 
     @Test
     public void digraphWithThreeNodesInOneLine() {
         final Node n1 = env.add();
         final Node n2 = env.add();
         final Node n3 = env.add();
         n1.connect(n2);
         n2.connect(n3);
         assertThat(sut.toString(), is(String.format(
                   "digraph neuron {%n"
                 + "  %d -> %d;%n"
                 + "  %d -> %d;%n"
                 + "}", n1.getId(), n2.getId(), n2.getId(), n3.getId())));
     }
 
     @Test
    public void digraphWithMultipleNode(){
         final List<Token> args = Lists.newArrayList();
         args.add(Token.newNumberToken(5));
         args.add(Token.newNumberToken(2));
         final SampleTree cmd = new SampleTree(env, mock(IO.class), args);
         cmd.execute();
         assertThat(sut.toString(), is(String.format("digraph neuron {%n"
                 + "  0 -> 16;%n"
                 + "  0 -> 1;%n"
                 + "  1 -> 2;%n"
                 + "  1 -> 9;%n"
                 + "  2 -> 3;%n"
                 + "  2 -> 6;%n"
                 + "  3 -> 4;%n"
                 + "  3 -> 5;%n"
                 + "  6 -> 7;%n"
                 + "  6 -> 8;%n"
                 + "  9 -> 10;%n"
                 + "  9 -> 13;%n"
                 + "  10 -> 11;%n"
                 + "  10 -> 12;%n"
                 + "  13 -> 14;%n"
                 + "  13 -> 15;%n"
                 + "  17 -> 18;%n"
                 + "  17 -> 21;%n"
                 + "  16 -> 17;%n"
                 + "  16 -> 24;%n"
                 + "  18 -> 19;%n"
                 + "  18 -> 20;%n"
                 + "  21 -> 23;%n"
                 + "  21 -> 22;%n"
                 + "  25 -> 27;%n"
                 + "  25 -> 26;%n"
                 + "  24 -> 25;%n"
                 + "  24 -> 28;%n"
                 + "  28 -> 29;%n"
                 + "  28 -> 30;%n"
                 + "}")));
     }
 }
