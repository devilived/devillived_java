package com.devil.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	private static final long ADAY = 24 * 60 * 60 * 1000;

	public static int getDate(Date d) {
		Calendar cld = Calendar.getInstance();
		cld.setTime(d);
		return cld.get(Calendar.DAY_OF_MONTH);
	}

	public static Date getDateStart(Date d) {
		Calendar cld = Calendar.getInstance();
		cld.setTime(d);
		cld.set(Calendar.HOUR_OF_DAY, 0);
		cld.set(Calendar.MINUTE, 0);
		cld.set(Calendar.SECOND, 0);
		cld.set(Calendar.MILLISECOND, 0);
		return cld.getTime();
	}

	public static boolean sameDay(Date d1, Date d2) {
		return d1.getTime() / ADAY == d2.getTime() / ADAY;
	}

	public static boolean sameMonth(Date d1, Date d2) {
		Calendar cld = Calendar.getInstance();
		cld.setTime(d1);
		Calendar cld2 = Calendar.getInstance();
		cld2.setTime(d2);
		boolean yeareq = cld.get(Calendar.YEAR) == cld2.get(Calendar.YEAR);
		boolean montheq = cld.get(Calendar.MONTH) == cld2.get(Calendar.MONTH);
		return montheq && yeareq;
	}
}
