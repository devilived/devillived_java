package com.devil;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.devil.des.DESCoder;
import com.devil.utils.Base64Coder;
import com.devil.utils.HttpUtil;
import com.devil.utils.HttpUtil.HttpResponse;

public class Config {
	public static void main(String[] args){
		try {
			enc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static String CONF_URL = "bsrIGBPKq/pS3To/roH9mkdpj1o0Mmb7NIZfpvtwmeP1mSgSFkJHxI6j5VSh757uQPVsQjv2w1NR8Rl5xWsktA==";
	public static String SERVER_URL;
	public static final String CS = "UTF-8";
	public static void init(){
		try {
			DESCoder origDes = new DESCoder("12345678".getBytes(CS));
			CONF_URL = new String(origDes.decrypt(CONF_URL),CS);
//			HttpResponse resp = HttpUtil.get(CONF_URL, null, null);
//			String http = resp.getContent();
			String http="z1PfjQ4nVbK4Zwmii0TvXw==(2,10)\n/GrexLtEny+jm6npRs0FjKsyIVnOGNYD";
			BufferedReader br = new BufferedReader(new StringReader(http));
			String firstLine = br.readLine();
			System.out.println("firstLine all:"+firstLine);
			
			int flagStartIdx = firstLine.indexOf('(');
			int[] startAndEnd = getStartAndEnd(firstLine.substring(flagStartIdx));
			System.out.println("start and end:"+startAndEnd[0]+","+startAndEnd[1]);
			firstLine=firstLine.substring(0,flagStartIdx);
			byte[] keyContent = origDes.decrypt(firstLine);
			System.out.println("firstLine all:"+new String(keyContent,CS)+"("+startAndEnd[0]+","+startAndEnd[1]+")");
			byte[] codeKey = Arrays.copyOfRange(keyContent, startAndEnd[0], startAndEnd[1]);
			System.out.println("firstLine:"+firstLine);
			System.out.println("truekey:"+new String(codeKey,CS));
			
			DESCoder propCoder = new DESCoder(codeKey);
			String propContent = new String(propCoder.decrypt(br.readLine()),CS);
			Properties prop = new Properties();
			StringReader sr = new StringReader(propContent);
			prop.load(sr);
			SERVER_URL=prop.getProperty("server_url", "");
			System.out.println("content:"+propContent);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void enc() throws Exception{
		String url="https://raw.github.com/configs/configs/master/guessyourheart";
		String keySeed="guessyourheart";
		int[] keyIdx={2,10};
		Map<String,String> propMap = new HashMap<String,String>();
		propMap.put("key1", "value1");
		propMap.put("key2", "value2");
		
		DESCoder origDes = new DESCoder("12345678".getBytes(CS));
		String cypherUrl = origDes.encrypt(url.getBytes(CS));
		System.out.println("cypher server url is: "+cypherUrl);
		
		
		String keybase64 = origDes.encrypt(keySeed.getBytes(CS));
		String cypherKey=keybase64+"("+keyIdx[0]+","+keyIdx[1]+")";
		String trueKeyStr=keySeed.substring(keyIdx[0],keyIdx[1]);
		byte[] trueKeyByte = trueKeyStr.getBytes(CS);
		System.out.println("truekey:"+trueKeyStr);
		System.out.println("firstline:"+cypherKey);
		
		StringBuilder sb =new StringBuilder();
		for(Entry<String,String> entry:propMap.entrySet()){
			sb.append(entry.getKey()+"="+entry.getValue()+"\n");
		}
		sb.deleteCharAt(sb.length()-1);
		DESCoder contentDes = new DESCoder(trueKeyByte);
		String cypherContent = contentDes.encrypt(sb.toString().getBytes(CS));
		
		
		System.out.println("\ncontent:\n"+sb.toString());
		System.out.println("cyphercontent:"+cypherContent);
		
		System.out.println("\n\nfinal content is:\n"+cypherKey+"\n"+cypherContent);
	}
	private static int[] getStartAndEnd(String src) {
		String s = src.substring(1, src.length() - 1);
		String[] part = s.split(",");
		int[] rtn = { Integer.valueOf(part[0]), Integer.valueOf(part[1]) };
		return rtn;
	}
}
