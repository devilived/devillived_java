package com.devil.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	public static final long ADAY = 24 * 60 * 60 * 1000;
	public static final long AWEEK = ADAY * 7;
	private static final DateFormat DF_MONTH=new SimpleDateFormat("yyyyMM");
	private static final DateFormat DF_DAY=new SimpleDateFormat("yyyyMMdd");
	
	public static Date addYear(Date date,int year){
		Calendar cld = Calendar.getInstance();
		cld.setTime(date);
		cld.add(Calendar.YEAR, year);
		return cld.getTime();
	}
	
	public static int deltaDate(Date d1,Date d2){
		Date start1=getDateStart(d1);
		Date start2=getDateStart(d2);
		return (int) (Math.abs(start1.getTime()-start2.getTime())/ADAY)+1;
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
		return DF_DAY.format(d1).equals(DF_DAY.format(d2));
	}

	public static boolean sameMonth(Date d1, Date d2) {
		return DF_MONTH.format(d1).equals(DF_MONTH.format(d2));
	}
	
	public static class SqlDate extends java.sql.Date{
		public SqlDate(long time){
			super(getDateStart(new java.sql.Date(time)).getTime());
		}
		public SqlDate(java.util.Date date){
			super(getDateStart(date).getTime());
		}
		@Override
		public boolean equals(Object obj) {
			return super.equals(obj);
		}

		public int between(SqlDate date) {
			return (int) (Math.abs(this.getTime() - date.getTime()) / ADAY + 1);
		}
	}
}
