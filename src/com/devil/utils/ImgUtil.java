package com.devil.utils;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImgUtil {
	public static void createPreviewImage(String srcFile, String destFile, int width) {
		try {
			File fi = new File(srcFile); // src
			File fo = new File(destFile); // dest
			BufferedImage bis = ImageIO.read(fi);

			int w = bis.getWidth();
			int h = bis.getHeight();
			double scale = (double) w / h;
			int nw = width; // final int IMAGE_SIZE = 120;
			int nh = (nw * h) / w;
			if (nh > width) {
				nh = width;
				nw = (nh * w) / h;
			}
			double sx = (double) nw / w;
			double sy = (double) nh / h;
			AffineTransform transform = new AffineTransform();
			transform.setToScale(sx, sy);
			AffineTransformOp ato = new AffineTransformOp(transform, null);
			BufferedImage bid = new BufferedImage(nw, nh, BufferedImage.TYPE_3BYTE_BGR);
			ato.filter(bis, bid);
			ImageIO.write(bid, " jpeg ", fo);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(" Failed in create preview image. Error:  " + e.getMessage());
		}
	}
}
