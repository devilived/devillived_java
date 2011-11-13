package com.devil.utils;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.Method;

final public class UIUtil {
	public static void positionFrameOnScreen(Window frame,double horizontalPercent, double verticalPercent) {
		Rectangle s = getMaximumWindowBounds();
		Dimension f = frame.getSize();
		int w = Math.max(s.width - f.width, 0);
		int h = Math.max(s.height - f.height, 0);
		int x = (int) (horizontalPercent *  w) + s.x;
		int y = (int) (verticalPercent *  h) + s.y;
		frame.setBounds(x, y, f.width, f.height);
	}

	public static Rectangle getMaximumWindowBounds() {
		try {
			GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Method method = GraphicsEnvironment.class.getMethod("getMaximumWindowBounds");
			return (Rectangle) method.invoke(localGraphicsEnvironment);
		} catch (Exception e) {
			Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
			return new Rectangle(0, 0, s.width, s.height);
		}

	}
}
