package com.devil.utils;

public class MybatisUtil {
	public static void main(String[] args) {
		String sql = "SELECT uid,lon,lat,`time`, 6378.138*1000*2*asin(sqrt(pow(sin( (?-lat)*pi()/180/2),2)+cos(?*pi()/180)*cos(lat*pi()/180)* pow(sin((?-lon)*pi()/180/2),2))) AS distance FROM v_location_nearby AS a WHERE a.loc_private=0 AND a.uid != ? ORDER BY distance ASC limit ?,? ";
		String params = "28.68062(Double), 28.68062(Double), 112.900653(Double), 2498024(Long), 0(Integer), 20(Integer)";
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
