package com.devil.utils;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class ImgUtil {
	public static BufferedImage getGrayPicture(BufferedImage originalImage) {
		int imageWidth = originalImage.getWidth();
		int imageHeight = originalImage.getHeight();

		BufferedImage grayPicture = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
		ColorConvertOp cco = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		cco.filter(originalImage, grayPicture);
		return grayPicture;
	}

	public static BufferedImage cutsquareBuffImg(File src) throws IOException {
		String suffix = FileUtil.getSuffix(src.getName());
		/*
		 * 返回包含所有当前已注册 ImageReader 的 Iterator，这些 ImageReader 声称能够解码指定格式。
		 * 参数：formatName - 包含非正式格式名称 .（例如 "jpeg" 或 "tiff"）等 。
		 */
		FileImageInputStream fis = new FileImageInputStream(src);
		Iterator<ImageReader> iterator = ImageIO.getImageReaders(fis);// ImageIO.getImageReadersBySuffix(suffix);
		ImageReader reader = iterator.next();

		ImageInputStream iis = null;

		try {
			InputStream in = new FileInputStream(src);
			iis = ImageIO.createImageInputStream(in);

			/*
			 * iis:读取源.true:只向前搜索 .将它标记为 ‘只向前搜索’。 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许
			 * reader 避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
			 */
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			int len = Math.min(reader.getWidth(0), reader.getHeight(0));
			Rectangle rect = new Rectangle(0, 0, len, len);
			param.setSourceRegion(rect);
			return reader.read(0, param);
		} finally {
			// CommUtil.close(iis);
		}
	}

	public static BufferedImage cutBuffImg(File src, int x, int y, int w, int h) throws IOException {
		String suffix = FileUtil.getSuffix(src.getName());
		/*
		 * 返回包含所有当前已注册 ImageReader 的 Iterator，这些 ImageReader 声称能够解码指定格式。
		 * 参数：formatName - 包含非正式格式名称 .（例如 "jpeg" 或 "tiff"）等 。
		 */
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(suffix);
		ImageReader reader = iterator.next();

		ImageInputStream iis = null;

		try {
			InputStream in = new FileInputStream(src);
			iis = ImageIO.createImageInputStream(in);

			/*
			 * iis:读取源.true:只向前搜索 .将它标记为 ‘只向前搜索’。 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许
			 * reader 避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
			 */
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(x, y, w, h);
			param.setSourceRegion(rect);
			return reader.read(0, param);
		} finally {
			CommUtil.close(iis);
		}
	}

	public static BufferedImage scaleBuffImg(BufferedImage buffImg, int w, int h) {
		double sx = (double) w / buffImg.getWidth();
		double sy = (double) h / buffImg.getHeight();

		AffineTransform transform = new AffineTransform();
		transform.setToScale(sx, sy);
		AffineTransformOp ato = new AffineTransformOp(transform, null);
		BufferedImage newBuffImg = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		ato.filter(buffImg, newBuffImg);
		return newBuffImg;
	}
}
