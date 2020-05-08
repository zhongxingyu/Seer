 package com.hexcore.cas.control.protocol;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 public abstract class Node
{
 	public abstract void write(OutputStream out) throws IOException;
 }
