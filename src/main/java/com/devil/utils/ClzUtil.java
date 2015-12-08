package com.devil.utils;

import java.lang.reflect.Field;

public class ClzUtil {

	public static Object getField(Object obj, String fieldName) {

		Class<?> clz;
		if (obj instanceof Class) {
			clz = (Class<?>) obj;
		} else {
			clz = obj.getClass();
		}
		obj.getClass();
		Field f = getField(clz, fieldName);
		f.setAccessible(true);
		try {
			if (clz == obj) {
				return f.get(null);
			} else {
				return f.get(obj);
			}
		} catch (Throwable e) {
			throw new IllegalStateException("set field error", e);
		}
	}

	public static void setField(Object obj, String fieldName, Object value) {
		Class<?> clz = obj.getClass();
		Field f = getField(clz, fieldName);
		f.setAccessible(true);
		try {
			f.set(obj, value);
		} catch (Exception e) {
			throw new IllegalStateException("set field error", e);
		}
	}

	private static Field getField(Class<?> clz, String fieldName) {
		while (true) {
			try {
				return clz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// VLog.w("test", "no field:" + clz + "/" + fName +
				// ", try super");
				clz = clz.getSuperclass();
			}
		}
	}
}
