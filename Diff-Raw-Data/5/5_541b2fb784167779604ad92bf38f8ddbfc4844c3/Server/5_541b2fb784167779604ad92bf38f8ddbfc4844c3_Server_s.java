 package server;
 
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Random;
 
 import database.Configure;
 
 @SuppressWarnings("serial")
 class Server extends UnicastRemoteObject implements Interface {
 	private Main frame;
 
 	public Server(Main frame) throws Exception {
 		this.frame = frame;
 	}
 
 	@Override
 	public void connect() throws Exception {
 		frame.add("A user has connected.", true);
 		frame.add("", true);
 	}
 
 	@Override
 	public String setPic(File pic) throws Exception {
 		Random rand = new Random();
 		File pic1;
 		String s1, s2;
 		try {
 			do {
 				s1 = "";
 				for (int i = 0; i < 27; i++)
 					s1 += (char) (rand.nextInt(26) + 97);
 				s2 = "";
 				for (int i = 0; i < 5; i++)
 					s2 += (char) (rand.nextInt(26) + 97);
 				pic1 = new File(Configure.siteDirectory + "pic/" + s1 + "/"
 						+ s2 + ".jpg");
 			} while (pic1.exists());
 			File path = new File(Configure.siteDirectory + "pic/" + s1 + "/");
 			if (!path.exists())
 				path.mkdirs();
 			FileInputStream inStream = new FileInputStream(pic);
 			FileOutputStream outStream = new FileOutputStream(pic1);
 			FileChannel in = inStream.getChannel();
 			FileChannel out = outStream.getChannel();
 			long size = in.size();
 			in.transferTo(0, size, out);
 			out.close();
 			in.close();
 			outStream.close();
 			inStream.close();
 			frame.add("Successfully added " + pic.getName(), true);
			frame.add(size + " bytes", true);
 			frame.add("", true);
 		} catch (Exception e) {
 			frame.add("Failed adding " + pic.getName(), false);
 			frame.add("", false);
 			throw new Exception();
 		}
 		return s1 + s2;
 	}
 
 	@Override
 	public void deletePic(String name) throws Exception {
 		try {
 			File origin = new File(Configure.siteDirectory + "pic/"
 					+ name.substring(0, name.length() - 5) + "/"
 					+ name.substring(name.length() - 5) + ".jpg");
 			if (origin.exists()) {
 				File move = new File(Configure.siteDirectory + "picdeleted/"
 						+ name.substring(0, name.length() - 5) + "/"
 						+ name.substring(name.length() - 5) + ".jpg");
 				File oldPath = new File(Configure.siteDirectory + "pic/"
 						+ name.substring(0, name.length() - 5) + "/");
 				File newPath = new File(Configure.siteDirectory + "picdeleted/"
 						+ name.substring(0, name.length() - 5) + "/");
 				if (!newPath.exists())
 					newPath.mkdirs();
 				int i = 1;
 				while (move.exists()) {
 					move = new File(Configure.siteDirectory + "picdeleted/"
 							+ name.substring(0, name.length() - 5) + "/"
 							+ name.substring(name.length() - 5) + "(" + i
 							+ ").jpg");
 					i++;
 				}
 				origin.renameTo(move);
 				File[] oldPathFiles = oldPath.listFiles();
 				if (oldPathFiles.length == 0)
 					oldPath.delete();
 				frame.add(
 						"Successfully deleted "
 								+ name.substring(name.length() - 5) + ".jpg",
 						true);
 				frame.add("", true);
 			}
 		} catch (Exception e) {
 			frame.add("Failed deleting " + name.substring(name.length() - 5)
 					+ ".jpg", false);
 			frame.add("", false);
 		}
 	}
 }
