 package spaceshooters.save;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import me.matterross.moneta.stream.MonetaInputStream;
 import me.matterross.moneta.stream.MonetaOutputStream;
 import me.matterross.moneta.tag.ByteArrayTag;
 import me.matterross.moneta.tag.IntTag;
 import me.matterross.moneta.tag.Tag;
 import me.matterross.moneta.tag.TagBuilder;
 import me.matterross.moneta.tag.TagCompound;
 import spaceshooters.main.Spaceshooters;
 
 public final class SaveData {
 	
 	// USE CAMEL CASE LIKE IN JAVA!!!
 	
 	public static final String SIGNATURE = "S2SF";
 	public static final int VERSION = 1;
 	
 	private static SaveData instance;
 	private static boolean inited = false;
 	
 	public static synchronized void init() {
 		if (!inited) {
 			instance = new SaveData();
 			inited = true;
 		}
 	}
 	
 	public static SaveData getInstance() {
 		return instance == null ? instance = new SaveData() : instance;
 	}
 	
 	private File file;
 	private TagCompound data;
 	
 	private SaveData() {
 		try {
 			file = new File(Spaceshooters.getPath() + "saves/saveNew.ssf");
 			
 			if (!file.exists()) {
 				file.getParentFile().mkdirs();
 				file.createNewFile();
 				this.createEmptySaveFile();
 			}
 			
 			MonetaInputStream in = new MonetaInputStream(new FileInputStream(file));
 			Tag mainTag = in.readTag();
 			in.close();
			if (!(mainTag instanceof TagCompound) || !mainTag.getName().equals("data")) {
 				if (mainTag.getName().equals("Data") && ((TagCompound) mainTag).getTag("auth") == null) {
					System.out.println("Old save version... Running converter!");
 					SaveConverter converter = new SaveConverter(file.getAbsolutePath());
 					mainTag = converter.convert();
				} else {
					throw new IOException("Save is corrupted!");
				}
 			}
 			
 			data = (TagCompound) mainTag;
 			
 			System.out.println(data);
 			
			if (!Arrays.equals((byte[]) ((TagCompound) data.getTag("auth")).getTag("signature").getValue(), SIGNATURE.getBytes())) {
				throw new IOException("Save signature doesn't match!");
			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void createEmptySaveFile() throws IOException {
 		TagBuilder builder = new TagBuilder();
 		TagCompound auth = new TagCompound("auth");
 		auth.addTag(new ByteArrayTag("signature", SIGNATURE.getBytes()));
 		auth.addTag(new IntTag("version", VERSION));
 		builder.appendTagCompound(auth);
 		MonetaOutputStream os = new MonetaOutputStream(new FileOutputStream(file));
 		os.writeTag(builder.toTagCompound("data"));
 		os.close();
 	}
 	
 	public TagCompound getData() {
 		return data;
 	}
 	
 	public void save() {
 		try {
 			MonetaOutputStream os = new MonetaOutputStream(new FileOutputStream(file));
 			os.writeTag(data);
 			os.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
 
 final class SaveConverter {
 	
 	private final File toConvert;
 	
 	SaveConverter(String filePath) {
 		toConvert = new File(filePath);
 	}
 	
 	public TagCompound convert() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		MonetaInputStream in = new MonetaInputStream(new FileInputStream(toConvert));
 		TagCompound main = (TagCompound) in.readTag();
 		TagCompound converted = this.convertCompound(main);
 		TagCompound auth = new TagCompound("auth");
 		auth.addTag(new ByteArrayTag("signature", SaveData.SIGNATURE.getBytes()));
 		auth.addTag(new IntTag("version", SaveData.VERSION));
 		converted.removeTag("signature");
 		converted.addTag(auth);
 		in.close();
 		toConvert.delete();
 		MonetaOutputStream os = new MonetaOutputStream(new FileOutputStream(toConvert));
 		os.writeTag(converted);
 		os.close();
 		return converted;
 	}
 	
 	private Tag convertTag(Tag tag) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		String convertedName = tag.getName().substring(0, 1).toLowerCase() + tag.getName().substring(1);
 		Constructor<?> c = tag.getClass().getConstructors()[0];
 		return (Tag) c.newInstance(convertedName, tag.getValue());
 	}
 	
 	private TagCompound convertCompound(TagCompound tag) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		HashMap<String, Tag> tags = new HashMap<>();
 		for (Tag t : tag.getChildTags()) {
 			if (!(t instanceof TagCompound)) {
 				Tag con = this.convertTag(t);
 				tags.put(con.getName(), con);
 			} else {
 				TagCompound com = this.convertCompound((TagCompound) t);
 				tags.put(com.getName(), com);
 			}
 		}
 		return new TagCompound(tag.getName().substring(0, 1).toLowerCase() + tag.getName().substring(1), tags);
 	}
 }
