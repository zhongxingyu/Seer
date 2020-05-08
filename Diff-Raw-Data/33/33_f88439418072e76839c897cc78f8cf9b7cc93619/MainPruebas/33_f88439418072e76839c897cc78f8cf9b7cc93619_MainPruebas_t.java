 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Datos;
 
 import Integracion.ConexionSAT.CSD;
 import Negocios.Cifrado.Cifrado;
 import Negocios.ObtenerFiel.Fiel;
 import Negocios.ObtenerFolios.Folio;
 import java.io.*;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 import java.util.Date;
 import org.jdom.Document;
 
 /**
  *
  * @author Raul Hernandez
  */
 public class MainPruebas {
     public static void main(String[] args) throws FileNotFoundException, IOException, CertificateException, Exception{
         try {
             //CERTIFICADO
            File fCSD=new File("CSD-ISFE.cer");
             InputStream isCSD=new FileInputStream(fCSD);
             byte[] bCSD=new byte[(int)fCSD.length()];
             int offset=0;
             int numRead=0;
             while(offset<bCSD.length && (numRead=isCSD.read(bCSD, offset, bCSD.length-offset))>=0){
                 offset+=numRead;
             }
             String nCSD=Cifrado.obtenerNumeroCertificado(bCSD);
             isCSD.close();
             //FIEL
             File fFIEL=new File("FIEL-ISFE.key");
             InputStream isFIEL=new FileInputStream(fFIEL);
             byte[] bFIEL=new byte[(int)fFIEL.length()];
             offset=0;
             numRead=0;
             while(offset<bFIEL.length && (numRead=isFIEL.read(bFIEL, offset, bFIEL.length-offset))>=0){
                 offset+=numRead;
             }
             String password="a0123456789";
             //PrivateKey key=Cifrado.getLlavePrivada(bFIEL, password);
             //
             Direccion d=new Direccion();
             d.setCalle("ANTONIO ROANOVA VARGAS");
             d.setCodigoPostal("07230");
             d.setColonia("ZONA ESCOLAR");
             d.setEstado("DISTRITO FEDERAL");
             d.setLocalidad("CIUDAD DE MEXICO");
             d.setMunicipio("GUSTAVO A MADERO");
             d.setNoExterior("51");
             d.setNoInterior("51");
             d.setReferencia("ENTRE FRANCISCO VILLA Y CENTRO ESCOLAR");
             //EMISOR-RECEPTOR
             Usuario u=new Usuario();
             u.setApMaterno("DELGADO");
             u.setApPaterno("HERNANDEZ");
             u.setCSD(new CSD(bCSD,nCSD));
             u.setCURP("PRUEBA");
             u.setCorreo("PRUEBA");
             u.setDireccion(d);
             u.setFiel(new Fiel(bFIEL,password));
             u.setNombre("RAUL");
             u.setRFC("PAM660606ER9");
             u.setReferencia("ENTRE FRANCISCO VILLA Y CENTRO ESCOLAR");
             //PAC
             ISFE isfe=new ISFE();
             isfe.setCSD(new CSD(bCSD,nCSD));
             isfe.setFiel(new Fiel(bFIEL,password));
             //CONCEPTOS
             ArrayList<Concepto> conceptos=new ArrayList<Concepto>();
             Concepto c1=new Concepto();
             c1.setCantidad(1.0);
             c1.setDescripcion("VIBRAMICINA 100MG 10");
             c1.setValorUnitario(244.00);
             c1.setUnidad("CAPSULAS");
             c1.setImporte(244.00);
             Concepto c2=new Concepto();
             c2.setCantidad(1.0);
             c2.setDescripcion("CLORUTO 500M");
             c2.setValorUnitario(137.93);
             c2.setUnidad("BOTELLA");
             c2.setImporte(137.93);
             Concepto c3=new Concepto();
             c3.setCantidad(1.0);
             c3.setDescripcion("SEDEPRON 250MG 10");
             c3.setValorUnitario(84.50);
             c3.setUnidad("TABLETAS");
             c3.setImporte(84.50);
             conceptos.add(c1);
             conceptos.add(c2);
             conceptos.add(c3);
             //FACTURA
             Factura f=new Factura();
             f.setCSD(new CSD(bCSD,nCSD));
             f.setConceptos(conceptos);
             f.setEmisor(u);
             f.setExpedidoEn(d);
             f.setFecha(new Date());
             f.setFiel(new Fiel(bFIEL,password));
             f.setFolio(new Folio(111,111,false));
             f.setFormaDePago("UNA SOLA EXIBICION");
             f.setReceptor(u);
             f.setTipoDeComprobante("INGRESO");
             f.generarTotal();
             XML xml=new XML();
             Document dXML=xml.generarXML(f, isfe,"");
             File fXML=XML.generarArchivoXML(dXML, "f.xml");
             File xslt=new File("cadOriginalCFDI_3.xslt");
             System.out.println(xslt);
             //System.out.println(fXML.getAbsolutePath());
             //File fXML=new File("f.xml");
             BufferedReader br=new BufferedReader(new FileReader(fXML));
             String l=br.readLine();
             while(l!=null){
                System.out.println(l);
                 l=br.readLine();
             }
            File fPDF=PDF.generarArchivoPDF(fXML, "", "F.pdf");
             /**EnvioMail em=new EnvioMail();
             em.EnvioMail("raul.hernandez.900519@gmail.com","PRUEBA XML","PRUEBA XML",fXML,f.getEmisor().getRFC()+f.getFolio().getNoFolio()+f.getReceptor().getRFC()+".xml");
             BufferedReader br=new BufferedReader(new FileReader(fXML));
             String l=br.readLine();
             while(l!=null){
                System.out.println(l);
                 l=br.readLine();
             }
             String cad="||3.0|11|2012-04-27T14:00:34|121|2012|INGRESO|UNA SOLA EXIBICION|34.00|10.00|288.00|PAM660606ER9|JUAN|ANTONIO R VARGAS|GUSTAVO A MADERO|DISTRITO FEDERAL|MEXICO|MEXICO|PAM660606ER9|PEDRO PEREZ|ANTONIO R VARGAS|GUSTAVO A MADERO|DISTRITO FEDERAL|MEXICO|1|TABLETAS|30.00|30.00|2|BOTELLA|50.00|100.00||";
             System.out.println(cad);
             System.out.println();
             SAT sat=new SAT();
             System.out.println(sat.ValidarCadenaOriginal(cad));*/
            /**String path="C:\\Users\\Natalia Hernández\\Documents\\Google Drive\\Cer_Sellos\\aaa010101aaa_csd_10.cer";
             File fCSD=new File("CSD-ISFE.cer");
             InputStream isCSD=new FileInputStream(fCSD);
             byte[] bCSD=new byte[(int)fCSD.length()];
             int offset=0;
             int numRead=0;
             while(offset<bCSD.length && (numRead=isCSD.read(bCSD, offset, bCSD.length-offset))>=0){
                 offset+=numRead;
             }
             String nCSD=Cifrado.obtenerNumeroCertificado(bCSD);
             isCSD.close();
            System.out.println(nCSD);*/
         } catch (Exception ex) {
             throw new Exception("",ex);
         }
     }
 }
