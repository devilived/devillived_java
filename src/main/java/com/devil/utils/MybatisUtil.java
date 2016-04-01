package com.devil.utils;

public class MybatisUtil {
	public static void main(String[] args) {
		String sql = "INSERT INTO v_user(id,`name`,nick,email,phone,sex,birth,status,create_date) VALUES(?,?,?,?,?,?,?,?,now())";
		String params = "2562551(Long), null, null, null, 12345678901(String), 0(Integer), 1996-04-06(Date), NORMAL(String)";
		System.out.println(buildSql(sql, params));
	}

	public static String buildSql(String statement, String args) {
		String[] paramarr = args.split(",");

		for (int i = 0; i < paramarr.length; i++) {
			String rawparam = paramarr[i].trim();
			if ("null".equals(rawparam)) {
				paramarr[i] = rawparam;
			} else {
				String value = rawparam.substring(0, rawparam.indexOf("("));
				String type = rawparam.substring(rawparam.indexOf("(") + 1, rawparam.length() - 1);
				if (notNeedQuot(type)) {
					paramarr[i] = value;
				} else {
					paramarr[i] = " '" + value + "' ";
				}
			}
		}
		return String.format(statement.replace("?", "%s"), paramarr);
	}

	private static boolean notNeedQuot(String type) {
		return "Double".equalsIgnoreCase(type) || "Integer".equalsIgnoreCase(type) || "Long".equalsIgnoreCase(type);
	}
}
