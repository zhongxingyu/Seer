 package com.unrc.app;
 
 import com.unrc.app.controllers.*;
 import com.unrc.app.models.*;
 import com.unrc.app.db.DB;
 import com.unrc.app.searchEngine.*;
 
 import org.javalite.activejdbc.Base;
 
 import com.unrc.app.html.HTML;
 import java.util.LinkedList;
 import javax.crypto.Cipher;
 
 import static spark.Spark.*;
 import spark.*;
 
 public class Inmo {
 
     public static void main(String[] args) {
         /**
          * open browser in windows
          */
         try {
             Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + "http://localhost:4567/index");
         } catch (Exception e) {
         }
         /**
          * open broser in ubuntu
          */
         try {
             Runtime.getRuntime().exec("firefox http://localhost:4567/index");
         } catch (Exception e) {
         }
 
 //        Base.open(DB.driver, DB.url, DB.user, DB.password);
 //        System.out.println(BuildingSearch.searchAll(null, null, null, 250000, "Lucas", -1, -1));
 
 
 
         /**
          * route for list information of a owner identified by id number
          */
         get(new Route("/owners/:id") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = OwnerController.get(new Integer(request.params(":id"))).toString();;
                 Base.close();
                 return ret;
             }//http://localhost:4567/owners/1
         });
 
         /**
          * route for show all owners
          */
         get(new Route("/ownersList") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.show(OwnerController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/ownersList
         });
 
         /**
          * route for show all buildings
          */
         get(new Route("/buildingsList") {
             @Override
             public Object handle(Request request, Response response) {
 
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.show(BuildingController.listAll());
                 Base.close();
                 return ret;
             }// http://localhost:4567/ownersList
         });
 
 
         /**
          * list for show all cities
          */
         get(new Route("/cityList") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.show(CityController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/cityList
         });
 
         /**
          * list for show all realStates
          */
         get(new Route("/realStateList") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.show(RealEstateController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/cityList
         });
 
         /**
          * list for show all districts
          */
         get(new Route("/districtList") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.show(DistrictController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/cityList
         });
 
 
         /*
          * ---------------------------------------------------------------------
          */
 
         /**
          * route for index page
          */
         get(new Route("/index") {
             @Override
             public Object handle(Request request, Response response) {
                 return HTML.index();
             }//para ver el hello world en el buscador pongan http://localhost:4567/index
         });
 
         /*
          * -------------------------------
          */
 
         /**
          * route for form of new owner
          */
         get(new Route("/saveOwner") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret;
                 ret = HTML.saveOwner(CityController.list(), DistrictController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/saveOwner
         });
 
         /**
          *
          */
         post(new Route("/saveOwner") {
             @Override
             public Object handle(Request request, Response response) {
                 String building_type_id = request.queryParams("name"),
                         owner_id = request.queryParams("street"),
                         number = request.queryParams("number"),
                         district_id = request.queryParams("district_id"),
                         city_id = request.queryParams("city_id");
 
                 String[] tags = {
                     "name",
                     "street",
                     "number",
                     "district_id",
                     "city_id"
                 };
 
                 String[] values = {
                     building_type_id,
                     owner_id,
                     number,
                     district_id,
                     city_id};
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 try {
                     OwnerController.insert(tags, values);
                 } catch (Exception e) {
                 }
                 Base.close();
                 return "Duenio agregado correctamente";
             }// http://localhost:4567/saveOwner
         });
 
         /*
          * -------------------------------
          */
 
         /**
          * route for form of new city
          */
         get(new Route("/saveCity") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.saveCity();
                 Base.close();
                 return ret;
             }// http://localhost:4567/saveCity
         });
 
         /**
          *
          */
         post(new Route("/saveCity") {
             @Override
             public Object handle(Request request, Response response) {
                 String nombre = request.queryParams("name");
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 try {
                     CityController.insert(nombre);
                 } catch (Exception e) {
                 }
 
                 Base.close();
                 return "Ciudad agregada correctamente";
             }// http://localhost:4567/saveCity
         });
 
         /**
          * route for form of new district
          */
         get(new Route("/saveDistrict") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.saveDistrict(CityController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/saveCity
         });
 
         /**
          *
          */
         post(new Route("/saveDistrict") {
             @Override
             public Object handle(Request request, Response response) {
                 String name = request.queryParams("name"),
                         city_id = request.queryParams("city_id");
 
                 String[] tags = {
                     "name",
                     "city_id"
                 };
 
                 String[] values = {
                     name,
                     city_id
                 };
 
 
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 try {
                     DistrictController.insert(tags, values);
                 } catch (Exception e) {
                 }
 
                 Base.close();
                 return "Distrito agregado correctamente";
             }// http://localhost:4567/saveCity
         });
 
 
         /*
          * -------------------------------
          */
 
         /**
          * search form
          */
         get(new Route("/search") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = "Sin resultados", op = request.queryParams("search");
                 if (op == null) {
                     ret = HTML.search(CityController.list(), DistrictController.list(), OwnerController.list(), BuildingTypeController.listAll());
                 } else {
                     String city_id = request.queryParams("city_id"),
                             district_id = request.queryParams("district_id"),
                             building_type_id = request.queryParams("building_type_id"),
                             owner_id = request.queryParams("owner_id"),
                             maxPrice = request.queryParams("maxPrice"),
                             sale = request.queryParams("sale"),
                             rental = request.queryParams("rental");
 //                    System.out.println(city_id + " " + district_id + " " + building_type_id + " " + owner_id + " " + maxPrice + " " + sale + " " + rental);
                     try {
                         ret = HTML.show(BuildingSearch.searchAll(city_id, district_id, building_type_id, maxPrice, owner_id, sale, rental));
                     } catch (Exception e) {
                     }
 
                 }
                 Base.close();
                 return ret;
             }// http://localhost:4567/search
         });
 
         /*
          * -------------------------------
          */
 
         /**
          * route for form of new building
          */
         get(new Route("/saveBuilding") {
             @Override
             public Object handle(Request request, Response response) {
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.saveBulding(CityController.list(), DistrictController.list(), OwnerController.list(), BuildingTypeController.listAll());
                 Base.close();
                 return ret;
             }// http://localhost:4567/saveBuilding
         });
 
         /**
          *
          */
         post(new Route("/saveBuilding") {
             @Override
             public Object handle(Request request, Response response) {
                 String building_type_id = request.queryParams("building_type_id"),
                         owner_id = request.queryParams("owner_id"),
                         street = request.queryParams("street"),
                         number = request.queryParams("number"),
                         district_id = request.queryParams("district_id"),
                         city_id = request.queryParams("city_id"),
                         description = request.queryParams("description"),
                         price = request.queryParams("price"),
                         sale = request.queryParams("sale"),
                         rental = request.queryParams("rental");
 
                 String[] tags = {
                     "building_type_id",
                     "owner_id",
                     "street",
                     "number",
                     "district_id",
                     "city_id",
                     "description",
                     "price",
                     "sale",
                     "rental"};
 
                 String[] values = {
                     building_type_id,
                     owner_id,
                     street,
                     number,
                     district_id,
                     city_id,
                     description,
                     price,
                    (sale.compareTo("1") == 0) ? "1" : "0",
                    (rental.compareTo("1") == 0) ? "1" : "0",};
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 try {
                     BuildingController.insert(tags, values);
                 } catch (Exception e) {
                 }
                 Base.close();
                 return "Inmueble agregado correctamente";
             }// http://localhost:4567/saveBuilding
         });
 
         /*
          * -------------------------------
          */
 
         /**
          * route for form of new realState
          */
         get(new Route("/saveRealState") {
             @Override
             public Object handle(Request request, Response response) {
 
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 String ret = HTML.saveRealState(CityController.list(), DistrictController.list(), OwnerController.list());
                 Base.close();
                 return ret;
             }// http://localhost:4567/saveRealState
         });
 
         /**
          * 
          */
         post(new Route("/saveRealState") {
             @Override
             public Object handle(Request request, Response response) {
 
                 String name = request.queryParams("name"),
                         street = request.queryParams("street"),
                         number = request.queryParams("number"),
                         district_id = request.queryParams("district_id"),
                         city_id = request.queryParams("city_id"),
                         phone = request.queryParams("phone"),
                         email = request.queryParams("email"),
                         web = request.queryParams("web"),
                         owner = request.queryParams("owners");
 
 
                 String[] tags = {
                     "name",
                     "street",
                     "number",
                     "district_id",
                     "city_id",
                     "phone",
                     "email",
                     "web"};
 
                 String[] values = {
                     name,
                     (street.compareTo("") != 0) ? street : null,
                     (number.compareTo("") != 0) ? number : null,
                     (district_id.compareTo("") != 0) ? district_id : null,
                     (city_id.compareTo("") != 0) ? city_id : null,
                     (phone.compareTo("") != 0) ? phone : null,
                     (email.compareTo("") != 0) ? email : null,
                     (web.compareTo("") != 0) ? web : null,};
 
                 Base.open(DB.driver, DB.url, DB.user, DB.password);
                 try {
                     RealEstateController.insert(tags, values);
                     RealEstateController.assignOwner(RealEstateController.get(name), OwnerController.get(new Integer(owner)));
                 } catch (Exception e) {
                 }
                 Base.close();
                 return "Inmobiliaria agregada correctamente";
             }// http://localhost:4567/saveRealState
         });
     }
 }
