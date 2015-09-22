package com.devil.decLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DecLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DecLog dl=new DecLog();
		Calendar c1=Calendar.getInstance();
		c1.set(Calendar.HOUR_OF_DAY, 6);
		c1.set(Calendar.MINUTE, 34);
		c1.set(Calendar.SECOND, 4);
		
		Calendar c2=Calendar.getInstance();
		c2.set(Calendar.HOUR_OF_DAY, 6);
		c2.set(Calendar.MINUTE, 34);
		c2.set(Calendar.SECOND, 52);
		dl.analyse(c1.getTime(),c2.getTime());
	}

	public void analyse(Date beginDate,Date endDate){
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BufferedReader br=null;
		BufferedWriter bw=null;
		try{
			File f=new File("C:/Documents and Settings/Administrator/桌面/xx/v3center_part.log");
			if(f.exists()){
				f.delete();
			}
			br=new BufferedReader(new FileReader("C:/Documents and Settings/Administrator/桌面/xx/v3center"));
			bw=new BufferedWriter(new FileWriter(f));
			boolean isValid=false;
			String tmp=null;
			while((tmp=br.readLine())!=null){
				String str=null;
				String dateS=null;
				if(tmp.length()>=8){
					dateS=tmp.substring(0,8);
				}
				try{
					
					Date d=df.parse(df.format(new Date()).substring(0,10)+" "+dateS);
					if(d.after(endDate)){
						break;
					}
					if(d.after(beginDate)){
						if(isValid==false){
							isValid=true;
						}
						str=tmp+"\n";
					}
				}catch (Exception e) {
					if(isValid==true){
						str = tmp+"\n";
					}
				}
				///////////////////
//				if(str!=null){
//					boolean isError=false;
//					if(str.contains("ERROR")){
//						isError=true;
//					}
//					if(isError==true){
//						bw.write(str);
//						if(str.startsWith("\\d(2):\\d(2):\\d(2)")){
//							isError=false;
//						}
//					}
//				}
				
				////////////////////
				if(str!=null){
					bw.write(str);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
