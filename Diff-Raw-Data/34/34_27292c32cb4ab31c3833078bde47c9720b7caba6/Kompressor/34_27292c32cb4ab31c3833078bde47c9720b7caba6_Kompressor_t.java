 package eu.gemstonewebdesign.java.kompressor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Stack;
 
 public class Kompressor extends Thread
 {
 	// Huffman kompressed data
 	private static final String extension_data = ".hkd";
 	
 	// Huffman kompressed metadata
 	private static final String extension_tree = ".hkm";
 	
 	protected static final int read_byte_buffer_size = 8;
 	
 	private KompressorAction action = KompressorAction.KOMPRESS;
 	
 	private long cmd_no;
 	
 	private String filename;
 	
 	private KompressorMode mode = KompressorMode.FILE;
 	
 	private String target;
 	
 	public Kompressor()
 	{
 	}
 	
 	public void assignCmdNo(long cmd_no)
 	{
 		this.cmd_no = cmd_no;
 	}
 	
 	private String cmdIdentifier()
 	{
 		return "[" + this.cmd_no + " '" + this.filename + "']";
 	}
 	
 	private Map<Byte, Boolean[]> computeTable(Node tree)
 	{
 		Map<Byte, Boolean[]> table = new HashMap<Byte, Boolean[]>();
 		
 		Stack<Boolean> path = new Stack<Boolean>();
 		
 		if (tree != null)
 		{
 			this.computeTableStep(tree, path, table);
 		}
 		
 		return table;
 	}
 	
 	private void computeTableStep(Node node, Stack<Boolean> path,
 			Map<Byte, Boolean[]> table)
 	{
 		if (node.isLeaf())
 		{
 			// leaf node - add path
 			Boolean[] current_path = new Boolean[path.size()];
 			
 			// bug fix suggested for the algorithm!
 			if (node.isRoot())
 			{
 				current_path = new Boolean[] { Boolean.TRUE };
 				table.put(node.getData(), current_path);
 			}
 			else
 			{
 				table.put(node.getData(), path.toArray(current_path));
 			}
 		}
 		else
 		{
 			// not a leaf node - proceed with children - both are not null!
 			
 			// <<
 			path.push(Boolean.FALSE); // left child
 			this.computeTableStep(node.getLeft(), path, table);
 			path.pop();
 			
 			// >>
 			path.push(Boolean.TRUE); // right child
 			this.computeTableStep(node.getRight(), path, table);
 			path.pop();
 		}
 	}
 	
 	private void dekompressFile(String file, String to)
 	{
 		File file_to_dekompress = new File(file + Kompressor.extension_data);
 		FileInputStream file_to_dekompress_reader = null;
 		
 		File file_to_dekompress_tree = new File(file + Kompressor.extension_tree);
 		ObjectInputStream file_to_dekompress_tree_reader = null;
 		
 		File dekompressed_file = new File(to);
 		FileOutputStream dekompressed_file_writer = null;
 		
 		try
 		{
 			System.out.println(this.cmdIdentifier() + "sys: Checking files");
 			
 			// test input file
 			if (file_to_dekompress.isFile())
 			{
 				if (file_to_dekompress.canRead())
 				{
 					file_to_dekompress_reader = new FileInputStream(
 						file_to_dekompress);
 				}
 				else
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Unable to read input file: " + file);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "err: Invalid input file: " + file);
 				return;
 			}
 
 			// test input file - metadata
 			if (file_to_dekompress_tree.isFile())
 			{
 				if (file_to_dekompress_tree.canRead())
 				{
 					file_to_dekompress_tree_reader = new ObjectInputStream(new FileInputStream(
 							file_to_dekompress_tree));
 				}
 				else
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Unable to read input file: " + file);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "err: Invalid input file: " + file);
 				return;
 			}
 			
 
 			// test output file
 			if ( ! dekompressed_file.exists())
 			{
 				if ( ! dekompressed_file.createNewFile())
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Could not create output file: " + to);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "sys: Overwriting an existing output file: " + to);
 				
 				// not fatal error - will overwrite
 			}
 			
 			if (dekompressed_file.isFile())
 			{
 				if (dekompressed_file.canWrite())
 				{
 					dekompressed_file_writer = new FileOutputStream(
 							dekompressed_file);
 				}
 				else
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Unable to write to output file: " + to);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "err: Invalid output file: " + to);
 				return;
 			}
 			
 			System.out.println(this.cmdIdentifier() + "sys: Dekompressing");
 			
 			// start decompressing
 			Metadata metadata = (Metadata) file_to_dekompress_tree_reader.readObject(); 
 			Node tree = metadata.tree();
 			try 
 			{
 				file_to_dekompress_tree_reader.close();
 			} catch (IOException e) { /* nezajímavá */ }
			System.out.println(this.cmdIdentifier()+"sys: dekompressing "+metadata.dekompressedByteCount()+
					" bytes long file from "+metadata.kompressedBitCount()+" bits");
 			
 			Reader reader = new Reader(file_to_dekompress_reader);
 			
 			Node current_node = tree;
 			long bit_counter = 0;
 			long byte_counter = 0;
 			
 			// pokud je kořenem stromu list, pak je jediný v celém stromu
 			if (current_node.isLeaf())
 			{
 				for (long i = 0; i < metadata.dekompressedByteCount(); i++)
 				{
 					dekompressed_file_writer.write(current_node.getData());
 				}
 				
 				// pro kontrolu konzistence dat
 				@SuppressWarnings("unused")
 				Boolean current_bit = null;
 				while ((current_bit = reader.read()) != null) 
 				{
 					bit_counter++;
 					byte_counter++;
 				}
 			}
 			else
 			{
 				assert(current_node.getLeft() != null);
 				assert(current_node.getRight() != null);
 				
 				Boolean current_bit = null;
 				
 				while (
 						((current_bit = reader.read()) != null)
 						&& (bit_counter < metadata.kompressedBitCount())
 						&& (byte_counter < metadata.dekompressedByteCount())
 				) {
 					if (current_bit)
 					{
 						// >>
 						current_node = current_node.getRight();
 					}
 					else 
 					{
 						// <<
 						current_node = current_node.getLeft();
 					}
 					
 					bit_counter++;
 					if (current_node.isLeaf())
 					{
 						byte_counter++;
 						dekompressed_file_writer.write(current_node.getData());
						// System.out.println("new byte: "+current_node.getData());
 						current_node = tree; // reset
 					}
 				}
 			}
 			
 			// TODO: output info, check consistency
 			// TODO: write docs
 		}
 		catch (FileNotFoundException e)
 		{
 			System.err.println(this.cmdIdentifier()
 					+ "err: Input file not found");
 		}
 		catch (IOException e)
 		{
 			System.err.println(this.cmdIdentifier()
 					+ "err: I/O error in kompressing: " + e.getMessage());
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			// skvělý příklad toho, kdy je catch víceméně zbytečný, protože aby
 			// tenhle kód byl třeba, musel by někdo smazat kus aplikace, takže by stejně nefungovala
 			e.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				if (file_to_dekompress_reader != null)
 				{
 					file_to_dekompress_reader.close();
 				}
 				if (dekompressed_file_writer != null)
 				{
 					dekompressed_file_writer.close();
 				}
 			}
 			catch (IOException e)
 			{
 				// not interesting
 			}
 		}
 	}
 	
 	private void kompressFile(String file, String to)
 	{
 		File file_to_kompress = new File(file);
 		FileInputStream file_to_kompress_reader = null;
 		
 		File kompressed_file = new File(to + Kompressor.extension_data);
 		FileOutputStream kompressed_file_writer = null;
 		
 		File kompressed_file_tree = new File(to + Kompressor.extension_tree);
 		ObjectOutputStream kompressed_file_tree_writer = null;
 		
 		try
 		{
 			System.out.println(this.cmdIdentifier() + "sys: Checking files");
 			
 			// test input file
 			if (file_to_kompress.isFile())
 			{
 				if (file_to_kompress.canRead())
 				{
 					file_to_kompress_reader = new FileInputStream(
 							file_to_kompress);
 				}
 				else
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Unable to read input file: " + file);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "err: Invalid input file: " + file);
 				return;
 			}
 			
 			// test output file
 			if (!kompressed_file.exists())
 			{
 				if (!kompressed_file.createNewFile())
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Could not create output file: " + to
 							+ Kompressor.extension_data);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "sys: Overwriting an existing output file: " + to
 						+ Kompressor.extension_data);
 				
 				// not fatal error - will overwrite
 			}
 			
 			if (kompressed_file.isFile())
 			{
 				if (kompressed_file.canWrite())
 				{
 					kompressed_file_writer = new FileOutputStream(
 							kompressed_file);
 				}
 				else
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Unable to write to output file: " + to
 							+ Kompressor.extension_data);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "err: Invalid output file: " + to
 						+ Kompressor.extension_data);
 				return;
 			}
 			
 			// test output file - metadata
 			if ( ! kompressed_file_tree.exists())
 			{
 				if ( ! kompressed_file_tree.createNewFile())
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Could not create output file: " + to
 							+ Kompressor.extension_tree);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "sys: Overwriting an existing output file: " + to
 						+ Kompressor.extension_tree);
 				
 				// not fatal error - will overwrite
 			}
 			
 			if (kompressed_file_tree.isFile())
 			{
 				if (kompressed_file_tree.canWrite())
 				{
 					kompressed_file_tree_writer = new ObjectOutputStream(
 							new FileOutputStream(kompressed_file_tree));
 				}
 				else
 				{
 					System.err.println(this.cmdIdentifier()
 							+ "err: Unable to write to output file: " + to
 							+ Kompressor.extension_tree);
 					return;
 				}
 			}
 			else
 			{
 				System.err.println(this.cmdIdentifier()
 						+ "err: Invalid output file: " + to
 						+ Kompressor.extension_tree);
 				return;
 			}
 			
 			System.out.println(this.cmdIdentifier()
 					+ "sys: Analyzing input file");
 			
 			// analyze input file now
 			Map<Byte, Long> stats = new HashMap<Byte, Long>();
 			
 			byte[] in = new byte[Kompressor.read_byte_buffer_size];
 			int bytes_read = 0;
 			
 			assert (file_to_kompress != null);
 			
 			while ((bytes_read = file_to_kompress_reader.read(in)) != -1)
 			{
 				for (int i = 0; i < bytes_read; i++)
 				{
 					// increment byte counter for the read byte
 					Long current_counter = (Long) stats.get((Byte) in[i]);
 					if (current_counter == null)
 					{
 						current_counter = new Long(0);
 					}
 					current_counter++;
 					stats.put((Byte) in[i], current_counter);
 				}
 			}
 			
 			PriorityQueue<Node> queue = new PriorityQueue<Node>();
 			
 			boolean passed_overflow = false;
 			for (byte byte_id = Byte.MIN_VALUE; byte_id <= Byte.MAX_VALUE; byte_id += 1)
 			{
 				if (passed_overflow)
 					break;
 				
 				Long final_counter = (Long) stats.get((Byte) byte_id);
 				if (final_counter != null)
 				{
 					// System.out.println("Adding node for byte " + byte_id +
 					// " with weight " + final_counter);
 					
 					Node node = new Node(byte_id, final_counter);
 					
 					queue.add(node);
 				}
 				// else - skip non-existent bytes
 				
 				if (byte_id == Byte.MAX_VALUE)
 				{
 					// additional constraint, adding 1 to Byte.MAX_VALUE overflows to
 					// Byte.MIN_VALUE
 					passed_overflow = true; // prevent infinite loop
 				}
 			}
 			
 			while (queue.size() > 1)
 			{
 				assert (queue.size() >= 2);
 				
 				Node node_1 = queue.poll();
 				Node node_2 = queue.poll();
 				
 				// System.out.println("Polled for node 1: " + node_1.getData() + ", " +
 				// node_1.getWeight());
 				// System.out.println("Polled for node 2: " + node_2.getData() + ", " +
 				// node_2.getWeight());
 				
 				Node parent = new Node(node_1, node_2);
 				
 				// replace the two nodes with the new parenting one
 				queue.add(parent);
 			}
 			
 			// tree is all build up, so now only retrieve the root:
 			Node tree = queue.poll();
 			
 			// System.out.println("Polled for root: " + tree.getData() + ", " +
 			// tree.getWeight());
 			
 			assert (queue.size() == 0);
 			queue = null; // explicitly free resources
 			
 			System.out.println(this.cmdIdentifier()
 					+ "sys: Kompressing input file");
 			
 			file_to_kompress_reader.close();
 			file_to_kompress_reader = new FileInputStream(file_to_kompress);
 			
 			Map<Byte, Boolean[]> table = this.computeTable(tree);
 			
 			// kompress the file now
 			Writer output = new Writer(kompressed_file_writer);
 			
 			long file_bytes = 0;
 			
 			while ((bytes_read = file_to_kompress_reader.read(in)) != -1)
 			{
 				file_bytes += bytes_read;
 				
 				for (int i = 0; i < bytes_read; i++)
 				{
 					Boolean[] current_path = table.get(in[i]);
 					for (Boolean bit : current_path)
 					{
 						output.write(bit);
 					}
 				}
 			}
 			output.flush();
 			
 			Metadata metadata = new Metadata(tree, output.bitCount(),
 					file_bytes);
 			
 			// file stats
 			long o_bit_count = output.bitCount();
 			long o_byte_count = (long) Math.ceil((output.bitCount() / 8));
 			double o_ratio;
 			if (file_bytes == 0)
 			{
 				o_ratio = 1;
 			}
 			else
 			{
 				o_ratio = (double) ((double) o_byte_count / (double) file_bytes);
 			}
 			System.out.print(this.cmdIdentifier() + "sys: Input file bytes: "
 					+ file_bytes + ", output file bits: " + o_bit_count + " ("
 					+ o_byte_count + " bytes)" + ", compression ratio: ");
 			System.out.format("%,.3f (%,.2f%%)%n", o_ratio, (o_ratio * 100));
 			
 			assert (kompressed_file_tree != null);
 			
 			kompressed_file_tree_writer.writeObject(metadata);
 			
 			System.out.println(this.cmdIdentifier()
 					+ "sys: Kompressing input file finished");
 		}
 		catch (FileNotFoundException e)
 		{
 			System.err.println(this.cmdIdentifier()
 					+ "err: Input file not found");
 		}
 		catch (IOException e)
 		{
 			System.err.println(this.cmdIdentifier()
 					+ "err: I/O error in kompressing: " + e.getMessage());
 			e.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				if (file_to_kompress_reader != null)
 				{
 					file_to_kompress_reader.close();
 				}
 				if (kompressed_file_writer != null)
 				{
 					kompressed_file_writer.close();
 				}
 			}
 			catch (IOException e)
 			{
 				// not interesting
 			}
 		}
 	}
 	
 	public void run()
 	{
 		if (this.mode == KompressorMode.FILE)
 		{
 			if (this.action == KompressorAction.KOMPRESS)
 			{
 				this.kompressFile(this.filename, this.target);
 			}
 			else if (this.action == KompressorAction.DEKOMPRESS)
 			{
 				this.dekompressFile(this.filename, this.target);
 			}
 		}
 	}
 	
 	public void setAction(KompressorAction action, String filename)
 	{
 		synchronized (this)
 		{
 			this.action = action;
 			this.filename = filename;
 		}
 	}
 	
 	public void setMode(KompressorMode mode)
 	{
 		synchronized (this)
 		{
 			this.mode = mode;
 		}
 	}
 	
 	public void setTarget(String target)
 	{
 		synchronized (this)
 		{
 			this.target = target;
 		}
 	}
 }
