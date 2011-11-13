package com.devil.SpringAutoConfig;

import java.io.File;
import java.io.FileFilter;
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
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.devil.utils.FileUtil;
import com.devil.utils.StrUtil;
import com.devil.utils.XmlUtil;
/*
 *本类与SpringAutomaticConfig的功能相同，提供spring配置文件的自动生成。
 *生成的文件和原类生成的文件排序后的内容完全相同。(关于Xml排序的程序未做上传)
 *该类能够根据spring中bean的class属性的className和bean的字 元素的name属性进行增序排列，
 *如果是相同的bean，则每次运行的xml文档内容都是相同的，方便用svn进行比较.
 *同时也提供对于非标准目录下类的支持，只需增加IbeanBuilder即可。
 *使用的时候请注意一下Dao,Service和Action等的命名规范，参见每个IbeanBuilder实现的正则表达式
 *对于原有的非规范的命名程序已经做了特殊处理，
 *对于新增加的非规范的命名程序，暂不支持，
 *如果需要支持新增加非规范命名有两种方法：1，可修改命名，使之符合规范，2增加IbeanBuilder接口，使程序能够处理
 */
public class SpringAutomaticConfig {
	private String javaDir;
	private Document doc;
	private Element root;
	//该list存储配置文件要配置的类的类型，目前包括dao，service和action，可在外部扩充
	private List<IBeanBuilder> builderList=new ArrayList<IBeanBuilder>();
	/**********************************************/
	//该方法根据set函数的关键字(dao,service,action等)，分析c的个set函数，注入bean，该方法供IBeanBuild使用使用
	//最后这个可变参数参数代表这个类中的许多函数中需要注入的那些函数所包含的关键字
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
				String head = "set\\w*";
				String end="\\w?";
				if(mName.matches(head+key+end)){//相当于setXXDaoX的形式
					valid=true;
					break;
				}
			}
			
			if (valid==true) {
				String name = StrUtil.lowFirstChar(mName.replaceFirst("set", ""));
				
				Class<?>[] ptArr = m.getParameterTypes();
				String type = ptArr[0].getSimpleName();
				if(ptArr[0].isInterface()){
					type=type.substring(1);
				}
				String ref = getClassId(type);

				Element property = bean.addElement("property");
				property.addAttribute("name", name);
				property.addAttribute("ref", ref);
			}
		}
	}
	//根据className获取class的id，可供外部和内部类使用
	public static String getClassId(String className) {
		int pos = className.lastIndexOf(".");
		if (pos != -1) {
			className = className.substring(pos + 1);
		}
		if(className.equalsIgnoreCase("BaseDao")){
			className="dao";//历史原因，特殊照顾
		}
		return StrUtil.lowFirstChar(className);
	}
	
	/****************** static methods end,  construct method begin*************************************/
	public SpringAutomaticConfig(){}//默认参数不使用默认的buildList
	public SpringAutomaticConfig(String autoDaoPkg,String autoServicePkg,String autoActionPkg){
		this.builderList.add(new DaoBuilder(autoDaoPkg));
		this.builderList.add(new ServiceBuilder(autoServicePkg));
		this.builderList.add(new ActionBuilder(autoActionPkg));
	}
	/********************以下是供外部函数设定程序参数使用***********************/
	public void addBeanBuilder(IBeanBuilder builder){
		this.builderList.add(builder);
	}
	public void setDocument(Document doc){
		this.doc=doc;
		this.root=doc.getRootElement();
	};
	public void setJavaDir(String javaDir) {
		this.javaDir = javaDir;
	}
	public void activeDefaultBuilder(String builderType,String... pkgs){
		if(pkgs!=null){
			if("dao".equalsIgnoreCase(builderType)){
				for(String str:pkgs){
					this.builderList.add(new DaoBuilder(str));
				}
			}else if("service".equalsIgnoreCase(builderType)){
				for(String str:pkgs){
					this.builderList.add(new ServiceBuilder(str));
				}
			}else if("action".equalsIgnoreCase(builderType)){
				for(String str:pkgs){
					this.builderList.add(new ActionBuilder(str));
				}
			}
		}
	}
	//该函数产生真正的输出
	public void createXml(String path){
		if(path==null){
			System.out.print("没有输出xml文件路径");
			return;
		}
		if(this.builderList.size()==0){
			System.out.println("请至少初始化一个builder");
			return;
		}
		if(root==null){
			System.out.println("请初始化xml的根元素");
			return;
		}
		
		File file=new File(path);
		if(file.exists()){
			file.delete();
		}
		
		try {
			FileFilter filter=new FileFilter() {
				@Override
				public boolean accept(File file) {
					if(file.getName().endsWith(".java")){
						return true;
					}
					return false;
				}
			};
			String[] strArr = FileUtil.listDirFiles(javaDir, filter);
			String fulJavaDir=Tool.getFullPath(this.javaDir);
			for(int i=0;i<strArr.length;i++){
				String tmp=strArr[i].replace(fulJavaDir+File.separator, "");
				tmp=tmp.replace(".java", "");
				tmp=tmp.replace(File.separator, ".");
				strArr[i]=tmp;
				System.out.println(tmp);
			}
			this.build(strArr);
			
			OutputFormat format = OutputFormat.createPrettyPrint();  
			XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"), format);
			writer.write(doc);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/************* private default class, provide the default implements, there are 3 class,including "Dao","Service","Action"*****************/	
	private class DaoBuilder implements IBeanBuilder{
		private String daoPkg;
		public DaoBuilder(String daoPkg){
			this.daoPkg=daoPkg;
			
		}
		public Element buildAutoBean(String fulName, Element root) throws ClassNotFoundException {
			Element bean = null;
			String daoRegx=(this.daoPkg+".\\S+Dao\\w?").replace(".", "\\.");
			String cName=fulName.substring(fulName.lastIndexOf(".")+1);
			if (fulName.matches(daoRegx)&&!cName.equals("BaseDao")) {
				Class<?> cls=Class.forName(fulName);
				if (checkIgnore(cls)) {
					System.out.println("ignore dao " + cls.getCanonicalName());
				}else{
					String id = getClassId(cName);
					bean = root.addElement("bean");
					bean.addAttribute("id", id);
					bean.addAttribute("class", fulName);
					bean.addAttribute("parent", getClassId(cls.getSuperclass().getSimpleName()));
				}
			}
			return bean;
		}
	}
	private class ServiceBuilder implements IBeanBuilder{
		private String servicePkg;
		public ServiceBuilder(String servicePkg){
			this.servicePkg=servicePkg;
			
		}
		public Element buildAutoBean(String fulName, Element root) throws ClassNotFoundException {
			Element bean = null;
			String daoRegx=(this.servicePkg+".\\S+Service\\w?").replace(".", "\\.");;
			String cName=fulName.substring(fulName.lastIndexOf(".")+1);
			if (fulName.matches(daoRegx)) {
				Class<?> cls=Class.forName(fulName);
				if (checkIgnore(cls)) {
					System.out.println("ignore Service " + cls.getCanonicalName());
				}else{
					String id = getClassId(cName);
					bean = root.addElement("bean");
					bean.addAttribute("id", id);
					bean.addAttribute("class", fulName);
					//一下方法是可变参数
					SpringAutomaticConfig.injectBean(cls, bean,"Dao","Service");
				}
			}
			return bean;
		}
	}
	private class ActionBuilder implements IBeanBuilder{
		private String actionPkg;
		public ActionBuilder(String actionPkg){
			this.actionPkg=actionPkg;
		}
		public Element buildAutoBean(String fulName, Element root) throws ClassNotFoundException {
			Element bean=null;
			String daoRegx=(this.actionPkg+".\\S+Action\\w?").replace(".", "\\.");;
			String cName=fulName.substring(fulName.lastIndexOf(".")+1);
			if (fulName.matches(daoRegx)) {
				Class<?> cls=Class.forName(fulName);
				if (checkIgnore(cls)) {
					System.out.println("ignore Action " + cls.getCanonicalName());
				}else{
					String id = getClassId(cName);
					bean = root.addElement("bean");
					bean.addAttribute("id", id);
					bean.addAttribute("class", fulName);
					bean.addAttribute("scope", "prototype");
					//一下方法是可变参数
					SpringAutomaticConfig.injectBean(cls, bean,"Dao","Service");
				}
			}
			return bean;
		}
	}
/************* All the methods used by out end, private methods begin ******************************/	
	private void build(String[] fulNameArr){
		if(this.builderList.size()>0){
			Arrays.sort(fulNameArr,new Comparator<String>(){//根据类名排序，
					public int compare(String o1, String o2) {
						String c1=o1.substring(o1.lastIndexOf(".")+1);
						String c2=o2.substring(o2.lastIndexOf(".")+1);
						return c1.compareTo(c2);
					}}
			);
			
			for(IBeanBuilder builder:builderList){
				root.addComment("======one begin======");
				for(int i=0;i<fulNameArr.length;i++){
					String fulName = fulNameArr[i];
					if(fulName!=null){
						try{
							builder.buildAutoBean(fulName, root);
							fulName=null;
						}catch(ClassNotFoundException e){
							System.out.println("找不到类"+fulName);
						}
					}
				}
				root.addComment("======one end======");
			}
		}
	}

	private static boolean checkIgnore(Class<?> c) {
		if (Modifier.isAbstract(c.getModifiers()))
			return true;
		return  false;
	}
		
/*************** all the class end, MAIN method begn******************/
	public static void main(String args[]) {
		final String autoDaoPkg="com.litb.v3.center.database.manual.dao";
		final String autoServicePkg="com.litb.v3.center.service";
		final String autoActionPkg="com.litb.v3.center.webaction";
		//以下使用默认的builder，默认实现dao,service和action的build，可以使用默认的构造函数，设定自定义builder
		SpringAutomaticConfig config=new SpringAutomaticConfig(autoDaoPkg,autoServicePkg,autoActionPkg);
		
		String tplPath = System.getProperty("user.dir") + "/src/main/resources/spring-config/app-ctx-automatic.xml.template";
		String destPath = System.getProperty("user.dir")+ "/src/main/resources/spring-config/app-ctx-automatic.xml";
		
		config.setJavaDir("src/main/java");
		
		Document doc=XmlUtil.getXmlDocument(tplPath);
		config.setDocument(doc);
		
		config.addBeanBuilder(new IBeanBuilder() {//历史原因特殊照顾
			public Element buildAutoBean(String fulName, Element root) throws ClassNotFoundException {
					Element bean=null;
					String loginRegx=("com.litb.v3.center.webaction.(\\S*.)?(Do)?Log(in|out)(Portal)?").replace(".", "\\.");;
					String cName=fulName.substring(fulName.lastIndexOf(".")+1);
					if (fulName.matches(loginRegx)) {
						Class<?> cls=Class.forName(fulName);
						if (checkIgnore(cls)) {
							System.out.println("ignore login " + cls.getCanonicalName());
						}else{
							String id = getClassId(cName);
							bean = root.addElement("bean");
							bean.addAttribute("id", id);
							bean.addAttribute("class", fulName);
							bean.addAttribute("scope", "prototype");
							//以下方法是可变参数，可酌情修改，默认只注入包含Dao和Service
							SpringAutomaticConfig.injectBean(cls, bean,"Dao","Service");
						}
					}
					return bean;
			}
		});
		
		config.createXml(destPath);
	}
}
