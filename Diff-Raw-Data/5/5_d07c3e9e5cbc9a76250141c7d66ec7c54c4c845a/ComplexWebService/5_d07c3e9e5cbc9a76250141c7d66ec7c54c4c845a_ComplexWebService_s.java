 package com.daemonspoint.webservice;
 
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 
 import java.io.File;
 import java.util.Map;
 import java.util.Date;
 
 @WebService(name = "complexWebService", targetNamespace = "http:/complex.de/complex-web-service/")
 public interface ComplexWebService {
    @WebMethod
    public String echo(
             @WebParam(name = "msg", mode = WebParam.Mode.IN) String msg
    );
 
    @WebMethod
    public int manyPrimitiveParameters(
             @WebParam(name = "p0", mode = WebParam.Mode.IN) int p0,
             @WebParam(name = "p1", mode = WebParam.Mode.IN) int p1,
             @WebParam(name = "p2", mode = WebParam.Mode.IN) int p2,
             @WebParam(name = "p3", mode = WebParam.Mode.IN) int p3,
             @WebParam(name = "p4", mode = WebParam.Mode.IN) int p4,
             @WebParam(name = "p5", mode = WebParam.Mode.IN) int p5,
             @WebParam(name = "p6", mode = WebParam.Mode.IN) int p6,
             @WebParam(name = "p7", mode = WebParam.Mode.IN) int p7,
             @WebParam(name = "p8", mode = WebParam.Mode.IN) int p8,
             @WebParam(name = "p9", mode = WebParam.Mode.IN) int p9,
             @WebParam(name = "p10", mode = WebParam.Mode.IN) boolean p10,
             @WebParam(name = "p11", mode = WebParam.Mode.IN) boolean p11,
             @WebParam(name = "p12", mode = WebParam.Mode.IN) boolean p12,
             @WebParam(name = "p13", mode = WebParam.Mode.IN) boolean p13,
             @WebParam(name = "p14", mode = WebParam.Mode.IN) boolean p14,
             @WebParam(name = "p15", mode = WebParam.Mode.IN) boolean p15,
             @WebParam(name = "p16", mode = WebParam.Mode.IN) boolean p16,
             @WebParam(name = "p17", mode = WebParam.Mode.IN) boolean p17,
             @WebParam(name = "p18", mode = WebParam.Mode.IN) boolean p18,
             @WebParam(name = "p19", mode = WebParam.Mode.IN) boolean p19,
             @WebParam(name = "p20", mode = WebParam.Mode.IN) long p20,
             @WebParam(name = "p21", mode = WebParam.Mode.IN) long p21,
             @WebParam(name = "p22", mode = WebParam.Mode.IN) long p22,
             @WebParam(name = "p23", mode = WebParam.Mode.IN) long p23,
             @WebParam(name = "p24", mode = WebParam.Mode.IN) long p24,
             @WebParam(name = "p25", mode = WebParam.Mode.IN) long p25,
             @WebParam(name = "p26", mode = WebParam.Mode.IN) long p26,
             @WebParam(name = "p27", mode = WebParam.Mode.IN) long p27,
             @WebParam(name = "p28", mode = WebParam.Mode.IN) long p28,
             @WebParam(name = "p29", mode = WebParam.Mode.IN) long p29
    );
 
    @WebMethod
    public Date manyObjectParameters(
             @WebParam(name = "p0", mode = WebParam.Mode.IN) Date p0,
             @WebParam(name = "p1", mode = WebParam.Mode.IN) Date p1,
             @WebParam(name = "p2", mode = WebParam.Mode.IN) Date p2,
             @WebParam(name = "p3", mode = WebParam.Mode.IN) Date p3,
             @WebParam(name = "p4", mode = WebParam.Mode.IN) Date p4,
             @WebParam(name = "p5", mode = WebParam.Mode.IN) Date p5,
             @WebParam(name = "p6", mode = WebParam.Mode.IN) Date p6,
             @WebParam(name = "p7", mode = WebParam.Mode.IN) Date p7,
             @WebParam(name = "p8", mode = WebParam.Mode.IN) Date p8,
             @WebParam(name = "p9", mode = WebParam.Mode.IN) Date p9,
             @WebParam(name = "p10", mode = WebParam.Mode.IN) File p10,
             @WebParam(name = "p11", mode = WebParam.Mode.IN) File p11,
             @WebParam(name = "p12", mode = WebParam.Mode.IN) File p12,
             @WebParam(name = "p13", mode = WebParam.Mode.IN) File p13,
             @WebParam(name = "p14", mode = WebParam.Mode.IN) File p14,
             @WebParam(name = "p15", mode = WebParam.Mode.IN) File p15,
             @WebParam(name = "p16", mode = WebParam.Mode.IN) File p16,
             @WebParam(name = "p17", mode = WebParam.Mode.IN) File p17,
             @WebParam(name = "p18", mode = WebParam.Mode.IN) File p18,
             @WebParam(name = "p19", mode = WebParam.Mode.IN) File p19,
             @WebParam(name = "p20", mode = WebParam.Mode.IN) Map<String,String> p20,
             @WebParam(name = "p21", mode = WebParam.Mode.IN) Map<String,String> p21,
             @WebParam(name = "p22", mode = WebParam.Mode.IN) Map<String,String> p22,
             @WebParam(name = "p23", mode = WebParam.Mode.IN) Map<String,String> p23,
             @WebParam(name = "p24", mode = WebParam.Mode.IN) Map<String,String> p24,
             @WebParam(name = "p25", mode = WebParam.Mode.IN) Map<String,String> p25,
             @WebParam(name = "p26", mode = WebParam.Mode.IN) Map<String,String> p26,
             @WebParam(name = "p27", mode = WebParam.Mode.IN) Map<String,String> p27,
             @WebParam(name = "p28", mode = WebParam.Mode.IN) Map<String,String> p28,
             @WebParam(name = "p29", mode = WebParam.Mode.IN) Map<String,String> p29
    );
 
    @WebMethod
    public Out customObjects(
             @WebParam(name = "p0", mode = WebParam.Mode.IN) In i
    );
 }
