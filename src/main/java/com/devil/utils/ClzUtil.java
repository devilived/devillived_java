package com.devil.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClzUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ClzUtil.class);
	public static void invoke(Object obj, String methodName, Object... args) {
		Class<?> clz;
		if (obj instanceof Class) {
			clz = (Class<?>) obj;
		} else {
			clz = obj.getClass();
		}

		if (args != null && args.length > 0) {
			Method[] mthds = clz.getDeclaredMethods();
			for (Method mthd : mthds) {
				if (mthd.getName().equals(methodName)) {
					mthd.setAccessible(true);
					try {
						mthd.invoke(obj, args);
					} catch (IllegalArgumentException e) {
						continue;
					} catch (Throwable e) {
						throw new IllegalStateException("invoke method error", e);
					} finally {
						mthd.setAccessible(false);
					}
				}
			}
		}
	}
	
	public static Method getMethod(Class<?> clz,String name){
		 Method[] methods = clz.getDeclaredMethods();
		 for(Method mthd:methods){
			 if(mthd.getName().equals(name)){
				 //因为无论getMethods还是getDeclaredMethods,对于父类和本类中的方法,只要返回值不同,都会作为独立的对象返回。
				 //但是getMthod只会返回最接近本类的方法，为了防止父类中的抽象方法干扰，这里需要根据参数重新过滤一下
				 Class<?>[] parameterTypes = mthd.getParameterTypes();
				 try {
					return clz.getMethod(name, parameterTypes);
				} catch (NoSuchMethodException | SecurityException e) {
					log.error("因为重名，理论上不会走到这里!!");
				}
			 }
		 }
		 return null;
	}

	public static Object getField(Object obj, String fieldName) {
		Class<?> clz;
		if (obj instanceof Class) {
			clz = (Class<?>) obj;
		} else {
			clz = obj.getClass();
		}
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
		} finally {
			f.setAccessible(false);
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
		} finally {
			f.setAccessible(false);
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
