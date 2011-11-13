package com.devil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class SpringAutomaticConfig {
	private String javaDir;
	private Element root;
	//该list存储配置文件要配置的类的类型，目前包括dao，service和action，可在外部扩充
	private List<IBeanBuilder> builderList=new ArrayList<IBeanBuilder>();
	/**********************************************/
	//该方法根据set函数的关键字(dao,service,action等)，分析c的个set函数，注入bean，该方法供外部人员使用
	public static void injectBean(Class<?> c, Element bean,String... setFuncKeys){
		Method[] mArr = c.getMethods();
		Arrays.sort(mArr, new Comparator<Method>() {//对所有方法排序，防止每次生成都乱序
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		for (Method m:mArr) {
			String mName=m.getName();
			boolean valid=false;
			for(String key:setFuncKeys){
				String head = "set\\w+";
				String end="\\w?";
				if(mName.matches(head+key+end)){//相当于setXXDaoX的形式
					valid=true;
					break;
				}
			}
			
			if (valid==true) {
				String name = Tool.lowFirstChar(mName.replaceFirst("set", ""));
				
				Class<?>[] ptArr = m.getParameterTypes();
				String type = ptArr[0].getSimpleName();
				String ref = Tool.lowFirstChar(type);

				Element property = bean.addElement("property");
				property.addAttribute("name", name);
				property.addAttribute("ref", ref);
			}
		}
	}
	/********************一下是供外部函数控制参数的函数***********************/
	public void addBeanBuilder(IBeanBuilder builder){
		this.builderList.add(builder);
	}
	public void setRoot(Element root){
		this.root=root;
	};
	public void setJavaDir(String javaDir) {
		this.javaDir = javaDir;
	}
	//该函数产生真正的输出
	public void createXml(String path){
		File file=new File(path);
		if(file.exists()){
			file.delete();
		}
		
		try {
			String[] strArr = Tool.listDir(javaDir);
			Class<?>[] classArr=new Class<?> [strArr.length];
			for(int i=0;i<classArr.length;i++){
				Class<?> cls=Class.forName(strArr[i]);
				classArr[i]=cls;
			}
			this.build(classArr);
			
			Document doc=DocumentHelper.createDocument();
			
			OutputFormat format = OutputFormat.createCompactFormat();
			format.setEncoding("UTF-8");
			format.setIndent("　 "); //缩进2个空格后换行,空格数自己设 
			XMLWriter writer=new XMLWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"),format);
			writer.write(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	private void build(Class<?>[] clsArr){
		if(this.builderList.size()>0){
			Arrays.sort(clsArr, new Comparator<Class<?>>() {//对数组进行排序，防止每次生成的bean都改变很大，对不利
				public int compare(Class<?> o1, Class<?> o2) {
					return o1.getSimpleName().compareTo(o2.getSimpleName());
				}
				});
			
			for(IBeanBuilder builder:builderList){
				builder.buildAutoBean(clsArr,root);
			}
		}
	}

	public static void main(String args[]) {
		final String autoDaoPkg="com.litb.v3.center.database.auto.dao";
		final String autoServicePkg="";
		final String autoActionPkg=null;
		IBeanBuilder daoBuilder=new IBeanBuilder(){
					public Element buildAutoBean(Class<?>[] clsArr, Element root) {
						Element bean = root.addElement("bean");
						String daoRegx=(autoDaoPkg+".\\w+Dao\\w?").replace(".", "\\.");
						for (Class<?> cls:clsArr) {
							String cFullName=cls.getCanonicalName();
							String cName=cFullName.substring(cFullName.lastIndexOf(".")+1);
							if (cFullName.matches(daoRegx)&&!cName.equals("BaseDao")) {
								if (checkIgnore(cls)) {
									System.out.println("ignore dao " + cls.getCanonicalName());
								}else{
									String id = getClassId(cName);
									bean.addAttribute("id", id);
									bean.addAttribute("class", cFullName);
									bean.addAttribute("parent", getClassId(cls.getSuperclass().getSimpleName()));
								}
							}
						}
						return bean;
					}};
					
		IBeanBuilder serviceBuilder=new IBeanBuilder(){
							public Element buildAutoBean(Class<?>[] clsArr,Element root) {
								Element bean = root.addElement("bean");
								String daoRegx=(autoServicePkg+".\\w+Service\\w?").replace(".", "\\.");;
								for (Class<?> cls:clsArr) {
									String cFullName=cls.getCanonicalName();
									String cName=cFullName.substring(cFullName.lastIndexOf(".")+1);
									if (cFullName.matches(daoRegx)) {
										if (checkIgnore(cls)) {
											System.out.println("ignore dao " + cls.getCanonicalName());
										}else{
											String id = getClassId(cName);
											bean.addAttribute("id", id);
											bean.addAttribute("class", cFullName);
											//一下方法是可变参数
											SpringAutomaticConfig.injectBean(cls, bean,"Dao","Service");
										}
									}
								}
								return bean;
			}};
			
			IBeanBuilder actionBuilder=new IBeanBuilder(){
								public Element buildAutoBean(Class<?>[] clsArr, Element root) {
									Element bean = root.addElement("bean");
									String daoRegx=(autoActionPkg+".\\w+Action\\w?").replace(".", "\\.");;
									for (Class<?> cls:clsArr) {
										String cFullName=cls.getCanonicalName();
										String cName=cFullName.substring(cFullName.lastIndexOf(".")+1);
										if (cFullName.matches(daoRegx)) {
											if (checkIgnore(cls)) {
												System.out.println("ignore dao " + cls.getCanonicalName());
											}else{
												String id = getClassId(cName);
												bean.addAttribute("id", id);
												bean.addAttribute("class", cFullName);
												//一下方法是可变参数
												SpringAutomaticConfig.injectBean(cls, bean,"Dao","Service");
											}
										}
									}
									return bean;
				}};
				
		
		String userDir = System.getProperty("user.dir");
		String tplPath = userDir + "/src/main/resources/spring-config/app-ctx-automatic.xml.template";
		String destPath = "D:/xx.xml";
		
		
		SpringAutomaticConfig config=new SpringAutomaticConfig();
		config.addBeanBuilder(actionBuilder);
		config.addBeanBuilder(serviceBuilder);
		config.addBeanBuilder(daoBuilder);
		config.setJavaDir(userDir+"/src/main/java");
		System.out.println(userDir);
		
		Element root=Tool.getXmlRoot(tplPath);
		config.setRoot(root);
		config.createXml(destPath);
		
		
	}
	private static String getClassName(String className) {
	int pos = className.lastIndexOf(".");
	if (pos != -1) {
		className = className.substring(pos + 1);
	}
	return className;
}

	private static String getClassId(String className) {
		className = getClassName(className);
		if(className.startsWith("I")){
			className=className.substring(1);
		}
		return Tool.lowFirstChar(className);
	}
	
	private static boolean checkIgnore(Class<?> c) {
		if (Modifier.isAbstract(c.getModifiers()))
			return true;
		return  false;//c.isAnnotationPresent(SpringManual.class);
	}
}

interface IBeanBuilder{
	public Element buildAutoBean(Class<?>[] clsArr,Element root);
}


class Tool {
	public static String[] listDir(String path) throws IOException{
		File dir = new File(path);
		if (!dir.isDirectory())
			throw new RuntimeException("dir[" + path + "]not exist");
		String[] s = dir.list();
		List<String> l = new ArrayList<String>();
		for (int i = 0; i < s.length; i++) {
			if (!s[i].equals(".svn")) {
				File file = new File(path, s[i]);
				if (file.isDirectory()) {
					String[] files = listDir(file.getCanonicalPath());
					for (String tmp : files)
						l.add(tmp);
				} else {
					l.add(file.getCanonicalPath());
				}
			}
		}
		return l.toArray(new String[0]);
	}

	public static String lowFirstChar(String src) {
		if (src.length() > 0) {
			src = src.substring(0, 1).toLowerCase() + src.substring(1);
		}
		return src;
	}
	
	public static String getFullPath(String rawPath){
		return System.getProperty("user.dir")+File.separator+rawPath;
	}
	
	public static Element getXmlRoot(String path){
		Element root=null;
		File file=new File(path);
		if(file.exists()){
			try {
				SAXReader reader = new SAXReader();
				Document doc = reader.read(file);
				root = doc.getRootElement();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return root;
	}
}

