 package q.web.people;
 
 import java.sql.SQLException;
 
 import q.biz.exception.PeopleAlreadyExistException;
 import q.biz.exception.RequestParameterInvalidException;
 import q.dao.AuthcodeDao;
 import q.dao.PeopleDao;
 import q.domain.Gender;
 import q.domain.People;
 import q.util.StringKit;
 import q.web.Resource;
 import q.web.ResourceContext;
 
 /**
  * @author Zhehao
  * @author alin
  * @date Feb 14, 2011
 *
  */
 
 public class AddPeople extends Resource {
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	private AuthcodeDao authcodeDao;
 
 	public void setAuthcodeDao(AuthcodeDao authcodeDao) {
 		this.authcodeDao = authcodeDao;
 	}
 
 	@Override
 	public void execute(ResourceContext context) throws SQLException {
 		People people = new People();
 		people.setEmail(context.getString("email"));
 		people.setPassword(context.getString("password"));
 		people.setUsername(context.getString("username"));
		people.setRealName(context.getString("realName"));
 		people.setGender(Gender.convertValue(context.getInt("gender", 0)));
 		people.setLoginToken("xxxx");// FIXME wanglin
 		peopleDao.addPeople(people);
 		context.setModel("people", people);
 	}
 
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		if (!PeopleValidator.validateUsername(context.getString("username"))) {
 			throw new RequestParameterInvalidException("username:用户名不能为空。");
 		}
 		if (!PeopleValidator.validateRealName(context.getString("realName"))) {
 			throw new RequestParameterInvalidException("realName:昵称不能为空。");
 		}
 		if (!PeopleValidator.validatePassword(context.getString("password"))) {
 			throw new RequestParameterInvalidException("password:密码由少于6位,或者有包含有数字,字母,下划线以外的字符组成。");
 		}
 		if (!context.getString("confirmPassword").equals(context.getString("password"))) {
 			throw new RequestParameterInvalidException("confirmPassword:两次输入的密码不同。");
 		}
 		String email = context.getString("email");
 		if (!PeopleValidator.validateEmail(email)) {
 			throw new RequestParameterInvalidException("email:请输入正确的邮箱地址。");
 		}
 		People result = this.peopleDao.getPeopleByEmail(email);
 		if (result != null) {
 			throw new PeopleAlreadyExistException("email:该邮箱地址已经被使用。");
 		}
 		long authcodeId = context.getIdLong("authcodeId");
 		String authcodeValue = authcodeDao.getValueById(authcodeId);
 		if(StringKit.isEmpty(authcodeValue) || !authcodeValue.equals(context.getString("authcode"))) {
 			throw new RequestParameterInvalidException("authcode:验证码不对,请重新输入。");
 		}
 		authcodeDao.updateValueById(authcodeId);
 	}
 
 }
