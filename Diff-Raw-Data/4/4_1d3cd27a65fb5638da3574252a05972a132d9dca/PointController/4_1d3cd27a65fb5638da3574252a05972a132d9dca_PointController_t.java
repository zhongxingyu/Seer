 package controllers;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import models.Account;
 import models.Point;
 import models.Target;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.*;
 import play.mvc.*;
 
 public class PointController extends Controller {
 	public static Result newPoint() {
 		Form<Point> newPoint = form(Point.class).bindFromRequest();
 		if (!newPoint.hasErrors()) {
 			Point pointPO = newPoint.get();
 			Target tempTarget = Target.find.byId(pointPO.target.id);
 			if (tempTarget != null) {
 				pointPO.target = tempTarget;
 			} else {
 				String id = newPoint.field("target.id").value();
 				return badRequest(newpoint.render("该对象不存在！",
 						Target.find.byId(Long.parseLong(id))));
 
 			}
 			pointPO.save();
 			return ok(newpoint.render("创建成功", tempTarget));
 		} else {
 			String id = newPoint.field("target.id").value();
 
 			return badRequest(newpoint.render("输入的格式不正确！",
 					Target.find.byId(Long.parseLong(id))));
 		}
 
 	}
 
 	// @BodyParser.Of(play.mvc.BodyParser.Json.class)
 	public static Result getPointsByTarget(Long id) {
 		Target target = Target.find.byId(id);
 		if (target != null) {
 			for (Point point : target.points) {
 				point.target = null;
 			}
 			JsonNode pointsJson = Json.toJson(target.points);
 			ObjectNode result = Json.newObject();
 			target.points = null;
 			target.account = null;
 			result.put("target", Json.toJson(target));
 			result.put("points", pointsJson);
 			return ok(result);
 		} else {
 			return badRequest();
 		}
 	}
 
 	public static Result deletePoint(Long id) {
		Point point = Point.find.byId(id);
		point.target = null;
		point.delete();
 		return ok();
 	}
 
 	public static Result updatePoint() {
 		Form<Point> updatePoint = form(Point.class).bindFromRequest();
 		if (!updatePoint.hasErrors()) {
 			Point point = updatePoint.get();
 			point.update();
 			point = Point.find.byId(point.id);
 			String targetName = point.target.targetName;
 			return ok(updatepoint.render("更新成功", updatePoint.fill(point)));
 		} else {
 			Point point = Point.find.byId(Long.parseLong(updatePoint
 					.field("id").value()));
 			String targetName = point.target.targetName;
 			return badRequest(updatepoint.render("数据格式有误",
 					updatePoint.fill(point)));
 		}
 	}
 }
