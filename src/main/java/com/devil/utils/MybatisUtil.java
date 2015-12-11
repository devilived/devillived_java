package com.devil.utils;

public class MybatisUtil {
	public static void main(String[] args) {
		String sql = " INSERT INTO v_location(uid,lat,lon,time,loc_private,sex,birth)VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE lat=?, lon=? ";
		String params = "6(Long), 39.927383(Double), 116.577323(Double), 2015-12-10 10:33:14.264(Timestamp), false(Boolean), 0(Integer), 1995-12-14(Date), 39.927383(Double), 116.577323(Double)";
		System.out.println(buildSql(sql, params));
	}

	public static String buildSql(String statement, String args) {
		String[] paramarr = args.split(",");

		for (int i = 0; i < paramarr.length; i++) {
			String rawparam = paramarr[i].trim();
			String value = rawparam.substring(0, rawparam.indexOf("("));
			String type = rawparam.substring(rawparam.indexOf("(") + 1, rawparam.length() - 1);
			if (notNeedQuot(type)) {
				paramarr[i] = value;
			} else {
				paramarr[i] = " '" + value + "' ";
			}
		}
		return String.format(statement.replace("?", "%s"), paramarr);
	}

	private static boolean notNeedQuot(String type) {
		return "Double".equalsIgnoreCase(type) || "Integer".equalsIgnoreCase(type) || "Long".equalsIgnoreCase(type);
	}
}
