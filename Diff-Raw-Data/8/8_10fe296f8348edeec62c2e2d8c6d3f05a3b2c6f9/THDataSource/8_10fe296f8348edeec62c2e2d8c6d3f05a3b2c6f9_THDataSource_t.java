 package prefwork.rating.datasource;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import prefwork.core.Utils;
 import prefwork.rating.Rating;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.SparseInstance;
 
 public class THDataSource extends ContentDataSource{
 
 	protected boolean userIdSent = false;
 	protected boolean hasUserId = false;
 	protected boolean hasObjectId = false;
 
 	protected boolean allowNulls = false;
 	double coef = 1,add = 0;
 
 	protected List<Integer> ignoreList;
 	
 	String file;
 	
 	protected Rating processOneLine(String line, int i){
 		String[] rec = line.split(";");
 		if(rec.length<2)
 			return null;
 		Rating r = new Rating(instances);
 
 		double[] vals = new double[instances.numAttributes()];
 		
 		int j = 0;
 		//UserId
 		if(hasUserId){
 			r.setUserId(Utils.objectToInteger(rec[0]));
 			j++;
 		}
 		else
 			r.setUserId(currentUser);
 			
 		//ObjectId
 		if(hasObjectId && hasUserId){
 			r.setObjectId(Utils.objectToInteger(rec[1]));
 			j++;
 		}
 		else
 			r.setObjectId(i);
 
 		int index = 0;
 		for (; j < rec.length; j++) {
 			if(ignoreList.contains(j+2)){
 				continue;
 			}
 			if(instances.attribute(index).isRelationValued()){
 				String val = rec[j];
				Instances value = instances.attribute(index).relation();
 				val = val.substring(1,val.length()-1);
 				String [] values = val.split(",");
 				for (int k = 0; k < values.length; k++) {
 					Instance inst = new weka.core.SparseInstance(1);
					inst.setDataset(value);
					inst.setValue(0, value.attribute(0).addStringValue(values[k]));
 					value.add(inst);
 				}
				vals[index] = instances.attribute(index).addRelation(value);
 										
 			}
 			else if("?".equals(rec[j]) ){
 					if(!allowNulls)
 						return null;
 					vals[index] = weka.core.Utils.missingValue();
 				}
 			else if(instances.attribute(index).isNumeric()){
 				try {
 					vals[index] =  Utils.objectToDouble(rec[j]);
 				} catch (Exception e) {
 					e.printStackTrace();
 					//vals[index] =  rec[j];
 				}
 			}
 			else{
 				try {
 					Utils.addStringValue(rec[j], vals, instances.attribute(index));
 				} catch (Exception e) {
 					e.printStackTrace();
 					//r.getRecord().setValue(index, rec[j]);
 				}
 				
 			}
 			index++;
 		}
 		r.setRecord(new SparseInstance(instances.numAttributes(), vals));
 		return r;
 	}
 	public void loadClasses(Rating[] userRecords){
 		List<Double> classes = Utils.getList(5);
 		for (int j = 0; j < userRecords.length; j++) {
 			Rating rec = userRecords[j];
 			double r= Utils.objectToDouble(rec.getRating());
 			if(!classes.contains((r+add)*coef+1)){
 				classes.add((r+add)*coef+1);					
 			}
 			//Setting new rating
 			rec.setRating((r+add)*coef+1);
 		}
 		this.classes = new double[classes.size()];
 		for (int j = 0; j < this.classes.length; j++) {
 			this.classes[j]=classes.get(j);
 		}
 	}
 	protected void getData() {
 		try {
 			BufferedReader in = new BufferedReader(
 					new FileReader(file + ".dat"));
 			String line;
 			int i = 0;
 			double max = Double.MIN_VALUE,min = Double.MAX_VALUE;
 			List<Rating> userRecords = Utils.getList();
 			while ((line = in.readLine()) != null && i < maxLoad) {
 				Rating r = processOneLine(line, i);
 				if(r == null)
 					continue;
 				double rating = r.getRating();
 				if(rating>max)
 					max = rating;
 				if(rating<min)
 					min = rating;
 				userRecords.add(r);
 				i++;
 			}
 			in.close();
 			add = -min ;
 			coef = 4/(max-min);
 			this.userRecords = new Rating[1][userRecords.size()];
 			for (int j = 0; j < this.userRecords[0].length; j++) {
 				this.userRecords[0][j]=userRecords.get(j);
 			}
 			shuffleInstances();			
 			loadClasses(userRecords.toArray(new Rating[0]));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	protected void getDefs() {
 		try {
 
 			ignoreList = Utils.getList();
 			BufferedReader in = new BufferedReader(
 					new FileReader(file + ".def"));
 			String line;
 			line = in.readLine();
 			line = in.readLine();
 			int i = 2;
 			ArrayList<Attribute> attrs = new ArrayList<Attribute>();
 			/*
 			Attribute attr = new Attribute("userId", 0);
 			attrs.add(attr);
 			attr = new Attribute("objectId", 1);
 			attrs.add(attr);*/
 			Attribute attr = null;
 			int attrCount = 0;
 			while ((line = in.readLine()) != null) {
 				String[] attrProp = line.split(";");
 				if ("I".equals(attrProp[0])) {
 					ignoreList.add(i);
 					i++;
 					continue;
 				}
 				if(attrProp[1].equals("userId")){
 					hasUserId = true;
 					continue;
 				}
 				if(attrProp[1].equals("objectId")){
 					hasObjectId = true;
 					continue;
 				}
 				if ("O".equals(attrProp[0])) {
 					attr = new Attribute(attrProp[1], attrCount);
 				} else if ("N".equals(attrProp[0])) {
 					attr = new Attribute(attrProp[1],  (ArrayList<String>)null, attrCount);
 				}else if ("L".equals(attrProp[0])) {
 					ArrayList<Attribute> list = new ArrayList<Attribute>();
 					list.add(new Attribute("list",new ArrayList<String>()));
 					attr = new Attribute(attrProp[1],  new Instances("list"+attrCount, list,10), attrCount);
 				} else if ("R".equals(attrProp[0])) {
 					attr = new Attribute(attrProp[1], attrCount);
 					classIndex = attrCount;
 				}
 				attrs.add(attr);
 				i++;
 				attrCount++;
 			}
 
 			in.close();
 			//Switch so that rating is on position 0
 			//attrs.add(0,attrs.get(classIndex));
 			//attrs.remove(classIndex+1);
 			//attributes.setClassIndex(0);
 			instances = new Instances("list"+attrCount, attrs,10);
 			instances.setClassIndex(classIndex);
 			/*attrs.toArray(attributes);
 			for (int j = 0; j < attributes.length; j++) {
 				attributes[j].setIndex(j);
 			}*/
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	public String getName() {
 		return "TH"+file.substring(file.lastIndexOf('\\')+1);
 	}
 
 	public void configDataSource(XMLConfiguration config, String section, String dataSourceName) {
 		Configuration dsConf = config.configurationAt(section);
 		file = Utils.getFromConfIfNotNull(dsConf, ".datasources."+dataSourceName+".file", file);
 		size = Utils.getIntFromConfIfNotNull(dsConf, "size", size);
 		if(dsConf.containsKey(".datasources."+dataSourceName+".file")){
 			getDefs();
 			getData();
 		}
 	}
 
 	public String getFile() {
 		return file;
 	}
 
 	public void setFile(String file) {
 		this.file = file;
 	}
 
 	public void setFixedUserId(Integer value) {
 		;
 	}
 
 	public Integer userId() {
 		if (userIdSent)
 			return null;
 		userIdSent = true;
 		return currentUser;
 	}
 
 	public void restartUserId() {
 		userIdSent = false;
 	}
 
 
 	public void setInstances(Instances attributes) {
 		this.instances = attributes;
 		this.names = null;
 	}
 	public void setClasses(double[] classes) {
 		this.classes = classes;
 	}
 
 	public int getTargetAttribute() {
 		return 2;
 	}
 	
 }
