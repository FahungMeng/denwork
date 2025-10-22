package idv.fhm.demo.controller;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import idv.fhm.demo.util.AccessDataObject;
import idv.fhm.demo.util.Constants;
import idv.fhm.demo.util.JwtUtil;
import idv.fhm.demo.util.MailUtil;
import idv.fhm.demo.util.RSAUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	AccessDataObject ado = new AccessDataObject();

	@GetMapping("/checkAccount")
	public Map<String, Object> checkAccount(String account) {
		Map<String, Object> connection = new HashMap<String, Object>();
		connection.put("email", account);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> qryResult = (List<Map<String, Object>>) ado.executeQuery(Constants.CHECK_ACCOUNT_SQL,
				connection);
		Map<String, Object> result = new HashMap<String, Object>();
		if (qryResult.size() == 0) {
			result.put("msg", "Can't find this account!");
			result.put("status", "false");
		} else if (Boolean.valueOf("false").equals(qryResult.get(0).get("ISACTIVATE"))) {
			result.put("msg", "this account isn't activate!");
			result.put("status", "false");
		} else {
			result.put("msg", "this account creat on " + qryResult.get(0).get("CREATEDT"));
			result.put("status", "true");
		}
		return result;

	}

	@Operation(summary = "建立帳號", description = "以 JSON 格式傳入帳號資料", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = {
			@ExampleObject(name = "帳號範例", summary = "建立帳號 JSON 範例", value = "{\n"
					+ "  \"email\": \"user@example.com\",\n" + "  \"password\": \"123456\"}") })))
	@PostMapping("/register")
	public Map<String, Object> register(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> data) {
		Map<String, Object> result = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
//		data.put("password", rsa.encode(data.get("password")!=null?"":String.valueof));
		List<Map<String, Object>> precheck = (List<Map<String, Object>>) ado.executeQuery(Constants.CHECK_ACCOUNT_SQL,
				data);
		if (precheck.size() > 0) {
			result.put("msg", "This account is already registered!");
			result.put("status", "false");
			return result;
		}
		if (data.get("password") == null || "".equals(data.get("password"))) {
			result.put("msg", "The password can't be empty!");
			result.put("status", "false");
			return result;

		}
		try {
			RSAUtil rsa = new RSAUtil();
			data.put("password", rsa.encode((String) data.get("password")));
			boolean createResult = ado.executeUpdate(Constants.INSERT_ACCOUNT_SQL, Arrays.asList(new Map[] { data }));

			if (createResult) {
				MailUtil.sendMail(String.valueOf(data.get("email")), "http://59.102.142.210/activate?activateStr="
						+ URLEncoder.encode(rsa.encode(String.valueOf(data.get("email"))), "UTF-8"));
				result.put("msg", "The account \'" + data.get("email") + "\' has been created!");
				result.put("status", true);
				return result;
			} else {
				result.put("msg", "create account \'" + data.get("email") + "\' fail! Please contact admin.");
				result.put("status", false);
				return result;

			}

		} catch (NoSuchAlgorithmException e) {
			result.put("msg", "The password error can't create this account");
			result.put("status", false);
		} catch (Exception e) {
			result.put("msg", "The password error can't create this account");
			result.put("status", false);
		}

		return result;

	}

	@PostMapping("/login")
	public Map<String, Object> Login(String account, String password) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (password == null || password.length() == 0) {
			result.put("msg", "error! Please check your account or password!");
			result.put("status", false);

		}
		Map<String, Object> connection = new HashMap<String, Object>();
		connection.put("email", account);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> qryResult = (List<Map<String, Object>>) ado.executeQuery(Constants.CHECK_ACCOUNT_SQL,
				connection);

		if (qryResult.size() == 0) {
			result.put("msg", "Can't find this account!");
			result.put("status", "false");
		} else if (Boolean.valueOf("false").equals(qryResult.get(0).get("ISACTIVATE"))) {
			result.put("msg", "this account isn't activate!");
			result.put("status", "false");
		} else {
			try {
				RSAUtil rsa = new RSAUtil();
				Map<String, Object> loginRecord = new HashMap<String, Object>();
				loginRecord.put("email", account);

				if (password.equals(rsa.decode((String) qryResult.get(0).get("PASSWORD")))) {
					ado.executeUpdate(Constants.INSERT_LOGIN_HIST_SQL,Arrays.asList(new Map[] { loginRecord }));

					result.put("msg", "Login success!");
					result.put("token", JwtUtil.generateToken(account));
					result.put("status", true);
				}
			} catch (NoSuchAlgorithmException e) {
				result.put("msg", "The password error can't create this account");
				result.put("status", false);
			} catch (Exception e) {
				result.put("msg", "The password error can't create this account");
				result.put("status", false);
			}

		}
		return result;

	}

	@GetMapping("/checkPassword")
	public boolean passwordCheck(String passwordd, String confirmPassword) {
		return false;
	}

	@GetMapping("/activate")
	public Object activateAccount(String activateStr) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			RSAUtil rsa = new RSAUtil();
			Map<String, Object> condition = new HashMap<String, Object>();
			condition.put("email", rsa.decode(activateStr));
			boolean flag = ado.executeUpdate(Constants.ACTIVATE_ACCOUNT_SQL, Arrays.asList(new Map[] { condition }));
			if (flag) {
				result.put("msg", "your account has been activated!");
				result.put("status", true);

			} else {
				result.put("msg", "Can't activate you email. Cause the activate url is not valid!");
				result.put("status", false);
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			result.put("msg", "Can't activate you email. Cause the activate url is not valid!");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", "Can't activate you email. Cause the activate url is not valid!");
		}

		return result;
	}

	@PostMapping("/lastRecord")
	public Object lastRecord(@AuthenticationPrincipal User user) {
		Map< String, Object> condition=new HashMap<String, Object>();
		condition.put("email", user.getUsername());
		List<Map<String,Object>> loginHistory=(List<Map<String, Object>>) ado.executeQuery(Constants.QUERY_LOGIN_HIST_SQL,condition );
		
		Map<String,Object> result=new HashMap<String, Object>();

		if(loginHistory.size() >0) {
			result.put("loginuser", loginHistory.get(0).get("EMAIL"));
			result.put("LastLogin", loginHistory.get(0).get("LOGIN_DATE"));
			result.put("status", true);
		}else {
			result.put("LastLogin", "Can't get last login time");
			result.put("status", false);
			
		}
		return result;
	}
}
