package com.devil.utils;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/* 
 * @Author xingqisheng
 * 对字符串提供Des加密和解密
 * 对Xml文件内容进行加密和解密（对属性无效）
 */

public class AES128Coder {
	private static final String KEY_TYPE = "AES";
	private static final String CIPHER_ALGORITHM_ECB  = "AES/ECB/PKCS5Padding";

//	private static final String CIPHER_ALGORITHM_ECB  = "AES";

	private byte[] key = null;

	private static AES128Coder defaultInstance;

	public static AES128Coder getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new AES128Coder();
		}
		return defaultInstance;
	}

	private AES128Coder() {
		this.key = this.getPrivateKey();
	}

	public AES128Coder(byte[] key) {
		if (key.length < 16) {
			byte[] newkey = new byte[16];
			System.arraycopy(key, 0, newkey, 0, key.length);
			key = newkey;
		} else if (key.length > 16) {
			key = Arrays.copyOf(key, 16);
		}
		System.out.println("key="+Arrays.toString(key));
		this.key = key;
	}

	public static void main(String[] args) {
		// String src = "guessyourheart";
		// System.out.println("原文是:" + src);
		// byte[] key = "guessyourheart".getBytes(Charset.forName("UTF-8"));
		String src = "123455";
		System.out.println("原文是:" + src);
		byte[] key = "12345678".getBytes(Charset.forName("UTF-8"));
		AES128Coder coder = new AES128Coder(key);
		byte[] notplain = src.getBytes();
		String cypher = coder.encrypt(notplain);
		System.out.println("加密后:" + cypher);
		byte[] plain = coder.decrypt(cypher);
		System.out.println("解密后:" + new String(plain));

		// System.out.println("======测试xml文件加密=======");
		// String testElName="username";
		// coder.encryptXml("C:\\Documents and
		// Settings\\Administrator\\v3\\conf\\v3-taobao-config.xml");
		// System.out.println("xml原文是:"+XmlUtil.getDocument("d:\\xx.xml").getRootElement().elementText(testElName));
		// Document doc = coder.decryptXml("d:\\xx_new.xml");
		// System.out.println("xml解密后:"+doc.getRootElement().elementText(testElName));
	}

	public String encrypt(byte[] src) {
		byte[] bytes = this.aesEncrypt(src);
		System.out.println("bytes[]=" + Arrays.toString(bytes));
		return CommUtil.encBase64Url(bytes);
	}

	public byte[] decrypt(String src) {
		return this.aesDecrypt(CommUtil.decBase64Url(src));
	}

	/********** public functions ends ******************/
	private byte[] getPrivateKey() {
		byte[] result = null;
		try {
			byte[] bArr = { 100, 101, 118, 105, 108, 105, 118, 101, 100 };
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(bArr);

			// SecureRandom secureRandom = new SecureRandom(bArr);
			KeyGenerator kg = KeyGenerator.getInstance(KEY_TYPE);
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
		return new SecretKeySpec(key, KEY_TYPE);
	}

	private byte[] aesDecrypt(byte[] cipherText) {
		byte[] key = this.key;
		byte[] plainText = null;
		try {
			Key k = this.toKey(key);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
			cipher.init(Cipher.DECRYPT_MODE, k);
			plainText = cipher.doFinal(cipherText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return plainText;
	}

	private byte[] aesEncrypt(byte[] plainText) {
		byte[] key = this.key;
		byte[] cipherText = null;
		try {
			Key k = this.toKey(key);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
			cipher.init(Cipher.ENCRYPT_MODE, k);
			cipherText = cipher.doFinal(plainText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}
}
