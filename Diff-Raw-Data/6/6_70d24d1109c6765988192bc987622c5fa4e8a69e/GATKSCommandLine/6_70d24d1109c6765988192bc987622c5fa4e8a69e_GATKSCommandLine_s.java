 package gatks;
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.Modifier;
 import javassist.NotFoundException;
 import javassist.bytecode.AnnotationsAttribute;
 import javassist.bytecode.ConstPool;
 import javassist.bytecode.SignatureAttribute;
 import javassist.bytecode.annotation.Annotation;
 import javassist.bytecode.annotation.MemberValue;
 import javassist.bytecode.annotation.StringMemberValue;
 import net.sf.picard.PicardException;
 import net.sf.samtools.SAMException;
 
 import org.apache.commons.lang.StringUtils;
 import org.broad.tribble.TribbleException;
 import org.broadinstitute.sting.commandline.CommandLineProgram;
 import org.broadinstitute.sting.commandline.Input;
 import org.broadinstitute.sting.commandline.Output;
 import org.broadinstitute.sting.commandline.RodBinding;
 import org.broadinstitute.sting.gatk.CommandLineGATK;
 import org.broadinstitute.sting.gatk.walkers.Walker;
 import org.broadinstitute.sting.utils.codecs.vcf.VCFWriter;
 import org.broadinstitute.sting.utils.exceptions.UserException;
 import org.broadinstitute.sting.utils.variantcontext.VariantContext;
 
 
 @SuppressWarnings("rawtypes")
 public class GATKSCommandLine extends CommandLineGATK {
     
     GATKSWalker originalWalker;
     
     CtClass annotatedWalker;
     
 	ClassPool pool = ClassPool.getDefault();
 	
     Map<String, List<Closure>> injections = GATK.getDefaultInjectionMap();
     
     boolean hasRegion = false;
     
     boolean hasISR = false;
 
 	private Map<String, Object> config;
     
     public GATKSCommandLine(Map<String, Object> config, GATKSWalker w) {
         super();
         this.originalWalker = w;
         this.config = config;
     }
 
     public void start(String[] argv) {
         try {
         	annotatedWalker = pool.makeClass("GATKSWalker"+System.currentTimeMillis());
         	annotatedWalker.setSuperclass(pool.get(originalWalker.getClass().getName()));
             
             List<String> args = new ArrayList<String>();
             for(String s : argv) 
             	args.add(s);
             
             args.add("-T");
             args.add(originalWalker.getClass().getName());
             
             if(config.containsKey("bam")) {
             	Object bamOrBams = config.get("bam");
             	addBamArgs(args, bamOrBams);
             }
             
             if(config.containsKey("vcf")) {
             	addVCFArg(args, config.get("vcf"));
             }
             
             if(config.containsKey("out")) {
             	addOutputArg(args, config.get("out"));
             }
             
             if(config.containsKey("bed")) {
             	args.add("-L");
             	args.add((String) config.get("bed"));
             	
             	// When the original input arguments contained a region we don't want to 
             	// UNION the region with our VCF file, because the user is expecting it to 
             	// constrain the walked region.  Therefore as long as they didn't add 
             	// an instruction for how to combine the region themselves, 
             	// we add an interval-set-rule of INTERSECTION ourself
             	if(hasRegion && !hasISR) {
 	            	args.add("-isr");
 	            	args.add("INTERSECTION");
             	}
             }
              
             System.out.println("Running with arguments " + StringUtils.join(args, " "));
             
             GATKSWalker realizedWalker = (GATKSWalker) annotatedWalker.toClass().newInstance();
             realizedWalker.setMapBody(originalWalker.getMapBody());
             
             for (String name : this.injections.keySet()) {
             	for(Closure c : this.injections.get(name)) {
 					realizedWalker.inject(name, c);
             	}
 			}
             
 	        this.engine = new GATKSEngine((Walker)realizedWalker);
             
             start(this, args.toArray(new String[args.size()]));
             
             if(CommandLineProgram.result != 0)
 	            System.exit(CommandLineProgram.result); 
         } 
         catch (UserException e) {
             exitSystemWithUserError(e);
         }
         catch (TribbleException e) {
             exitSystemWithUserError(e);
         }
         catch(PicardException e) {
             exitSystemWithError(e);
         }
         catch (SAMException e) {
             exitSystemWithSamError(e);
         }
         catch (Throwable t) {
             exitSystemWithError(t);
         } 
     }
 
 	private void addVCFArg(List<String> args, Object object) {
 		List<String> vcfs = new ArrayList<String>();
 		if(object instanceof String) {
 			vcfs.addAll(Arrays.asList(((String)object).split(",")));
 		}
 		else
 		if(object instanceof List) {
 			vcfs.add((String)object);
 		}
 		
 		int count = 1;
 		for(String vcf : vcfs) {
 			args.add("--vcf" + count);
 			args.add(vcf);
 			try {
 				ConstPool constPool = annotatedWalker.getClassFile().getConstPool();
 				AnnotationsAttribute att = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
 				Annotation ann = new Annotation(Input.class.getName(), constPool);
 				
 				MemberValue fullName = new StringMemberValue("vcf"+count, constPool);
 				ann.addMemberValue("fullName", fullName);
 				att.addAnnotation(ann);
 				
 				RodBinding<VariantContext> dummy = new RodBinding<VariantContext>(VariantContext.class, "vcf", vcf, "VCF", null);
 				CtClass rodClass = pool.get(dummy.getClass().getName());
 				
 				// If there are multiple vcfs, we number each one - vcf1, vcf2, ...
 				// if there is only one, we just call it "vcf"
 				CtField field = new CtField(rodClass, vcfs.size()>1 ? "vcf"+count : "vcf", annotatedWalker);
 				field.setModifiers(Modifier.PUBLIC);
 				field.getFieldInfo().addAttribute(att);
 				
 				SignatureAttribute sig = new SignatureAttribute(constPool, "Lorg/broadinstitute/sting/commandline/RodBinding<Lorg/broadinstitute/sting/utils/variantcontext/VariantContext;>;;");
 				field.getFieldInfo().addAttribute(sig);
 				
 				annotatedWalker.addField(field);
 				
				VCFHeaderInitializer vcfInit = new VCFHeaderInitializer(this);
				this.injections.get("initializers").add(vcfInit);
 			} 
 			catch (CannotCompileException e) {
 				throw new RuntimeException(e);
 				
 			} 
 			catch (NotFoundException e) {
 				throw new RuntimeException(e);
 			}
 			++count;
 		}
 	}
 
 	private void addOutputArg(List<String> args, Object object) {
 		if(!(object instanceof String)) 
 			throw new IllegalArgumentException("Only a single output file can be specified");
 			
 		String value = (String) object;
 		args.add("-o");
 		if(value.endsWith(".vcf")) {
 			args.add(value);
 			try {
 				ConstPool constPool = annotatedWalker.getClassFile().getConstPool();
 				AnnotationsAttribute att = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
 				Annotation ann = new Annotation(Output.class.getName(), constPool);
 				att.addAnnotation(ann);
 
 				CtClass writerClass = pool.get(VCFWriter.class.getName());
 				CtField field = new CtField(writerClass, "out", annotatedWalker);
 				field.setModifiers(Modifier.PUBLIC);
 				field.getFieldInfo().addAttribute(att);
 				annotatedWalker.addField(field);
 			}
 			catch (NotFoundException e) {
 				throw new RuntimeException(e);
 			}
 			catch (CannotCompileException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		else
 			throw new IllegalArgumentException("Only VCF files are supported as output files");
 	}
 
 	private void addBamArgs(List<String> args, Object bamOrBams) {
 		List<String> bams = null;
 		if(bamOrBams instanceof List) {
 			bams = (List<String>) bamOrBams;
 		}
 		else
 		if(bamOrBams instanceof String) {
 			bams = Arrays.asList(((String) bamOrBams).split(","));
 		}
 		else 
 			throw new IllegalArgumentException("Unsupported type " + bamOrBams.getClass().getName() + " for bam file configuration parameter");
 		
 		for(String b : bams) {
 			args.add("-I");
 			args.add(b);
 		}
 	}
 	
 	public Map<String, List<Closure>> getInjections() {
 		return injections;
 	}
 
 	public boolean isHasISR() {
 		return hasISR;
 	}
 
 	public void setHasISR(boolean hasISR) {
 		this.hasISR = hasISR;
 	}
 
 	public boolean isHasRegion() {
 		return hasRegion;
 	}
 
 	public void setHasRegion(boolean hasRegion) {
 		this.hasRegion = hasRegion;
 	}
 }
