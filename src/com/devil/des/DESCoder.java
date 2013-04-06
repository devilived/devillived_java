package com.devil.des;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.devil.utils.Base64Coder;

/* 
 * @Author xingqisheng
 * 对字符串提供Des加密和解密
 * 对Xml文件内容进行加密和解密（对属性无效）
 */

public class DESCoder {
	private static final String ALGORITHM = "DES";
	private static final String ALGORITHM_TYPE = "DES/CBC/PKCS5Padding";
	private byte[] key = null;
	// private byte[] paramvector = { 1, 2, 3, 4, 5, 6, 7, 8 };
	private byte[] paramvector = { 1, 2, 3, 4, 5, 6, 7, 8 };

	public DESCoder(byte[] key) {
		this.key = key;
		paramvector = key;
	}

	public DESCoder() {
		this.key = this.getPrivateKey();
		paramvector = key;
	}

	public static void main(String[] args) {
		String src = "1234567";
		System.out.println("原文是:" + src);
		DESCoder coder = new DESCoder();
		String cypher = coder.encrypt(src.getBytes());
		System.out.println("加密后:" + new String(cypher));
		byte[] plain = coder.decrypt(cypher);
		System.out.println("解密后:" + new String(plain));

		// System.out.println("======测试xml文件加密=======");
		// String testElName="username";
		// coder.encryptXml("C:\\Documents and Settings\\Administrator\\v3\\conf\\v3-taobao-config.xml");
		// System.out.println("xml原文是:"+XmlUtil.getDocument("d:\\xx.xml").getRootElement().elementText(testElName));
		// Document doc = coder.decryptXml("d:\\xx_new.xml");
		// System.out.println("xml解密后:"+doc.getRootElement().elementText(testElName));
	}

	public String encrypt(byte[] src) {
		return Base64Coder.encode(this.desEncrypt(src)).replace("\n", "");
	}

	public byte[] decrypt(String src) {
		return this.desDecrypt(Base64Coder.decode(src));
	}

	// 作为一个工具使用
	public void encryptXml(String path) {
		Document doc = XmlUtil.getDocument(path);
		if (doc != null) {
			XmlUtil.encryptElement(doc.getRootElement(), this);
			int dot = path.lastIndexOf(".");
			if (dot != -1) {
				path = path.substring(0, dot) + "_new" + path.substring(dot);
			} else {
				path = path + "_new";
			}
			XmlUtil.writeDocument(doc, path);
		}
	}

	public Document decryptXml(String path) {
		Document doc = XmlUtil.getDocument(path);
		XmlUtil.decryptElement(doc.getRootElement(), this);
		return doc;
	}

	/********** public functions ends ******************/
	private byte[] getPrivateKey() {
		byte[] result = null;
		try {
			byte[] bArr = { 100, 101, 118, 105, 108, 105, 118, 101, 100 };
			SecureRandom secureRandom = new SecureRandom(bArr);
			KeyGenerator kg = null;
			kg = KeyGenerator.getInstance(ALGORITHM);
			kg.init(secureRandom);

			SecretKey secretKey = kg.generateKey();
			result = secretKey.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * 因为不同的加密算法密钥的位数是不同的，因此通过spec进行初步整理，进行取舍，得到符合规范的key。
	 */
	private Key toKey(byte[] key) throws Exception {
		// DESKeySpec spec = new DESKeySpec(key);
		// SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
		// SecretKeyFactory keyFactory =
		// SecretKeyFactory.getInstance(ALGORITHM);
		// SecretKey secretKey = keyFactory.generateSecret(spec);
		// return secretKey;

		SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
		return keySpec;
	}

	private byte[] desDecrypt(byte[] cipherText) {
		byte[] key = this.key;
		byte[] plainText = null;
		try {
			Key k = this.toKey(key);
			IvParameterSpec zeroIv = new IvParameterSpec(paramvector);
			Cipher cipher = Cipher.getInstance(ALGORITHM_TYPE);
			cipher.init(Cipher.DECRYPT_MODE, k, zeroIv);
			plainText = cipher.doFinal(cipherText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return plainText;
	}

	private byte[] desEncrypt(byte[] plainText) {
		byte[] key = this.key;
		byte[] cipherText = null;
		try {
			Key k = this.toKey(key);
			IvParameterSpec zeroIv = new IvParameterSpec(paramvector);
			Cipher cipher = Cipher.getInstance(ALGORITHM_TYPE);
			cipher.init(Cipher.ENCRYPT_MODE, k, zeroIv);
			cipherText = cipher.doFinal(plainText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}
}

class XmlUtil {
	protected static Document getDocument(String path) {
		File f = new File(path);
		Document doc = null;
		if (f.exists()) {
			SAXReader reader = new SAXReader();
			try {
				doc = reader.read(new InputStreamReader(new FileInputStream(f),
						"UTF-8"));
			} catch (Exception e) {
				System.out.println("读入Xml文件" + path + "有误");
				e.printStackTrace();
			}
		}
		return doc;
	}

	protected static void writeDocument(Document doc, String destPah) {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(
					destPah), "UTF-8"), format);
			writer.write(doc);
		} catch (Exception e) {
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					System.out.println("关闭xml也会出问题!");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static void encryptElement(Element e, DESCoder coder) {
		String text = e.getTextTrim();
		if (text != null && text.length() > 0) {
			if (e.attribute("encrypt") != null
					&& "true"
							.equalsIgnoreCase(e.attribute("encrypt").getText())) {
				try {
					text = coder.encrypt(text.getBytes("UTF-8"));
					e.setText(text);
				} catch (UnsupportedEncodingException e1) {
					System.out.println("如果utf8都不支持，还叫java吗？");
					return;
				}
			}
		} else {
			List<Element> elList = e.elements();
			if (elList != null && elList.size() != 0) {
				for (Element el : elList)
					encryptElement(el, coder);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static void decryptElement(Element e, DESCoder coder) {
		String text = e.getTextTrim();
		if (text != null && text.length() > 0) {
			if (e.attribute("encrypt") != null
					&& "true"
							.equalsIgnoreCase(e.attribute("encrypt").getText())) {
				try {
					text = new String(coder.decrypt(text), "UTF-8");
					e.setText(text);
				} catch (UnsupportedEncodingException e1) {
					System.out.println("如果utf8都不支持，还叫java吗？");
					return;
				}
			}
		} else {
			List<Element> elList = e.elements();
			if (elList != null && elList.size() != 0) {
				for (Element el : elList)
					decryptElement(el, coder);
			}
		}
	}
}
