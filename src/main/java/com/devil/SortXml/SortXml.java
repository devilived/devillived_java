package com.devil.SortXml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
public class SortXml {
	@SuppressWarnings("unchecked")
	public Document sort(Document doc){
		Element root=doc.getRootElement();
		
		Document newDoc=DocumentHelper.createDocument();
		Element newRoot=root.createCopy();
		newRoot.clearContent();
		newDoc.add(newRoot);
		
		List<Element> elList=root.elements();
		List<Element> rstList=new ArrayList<Element>();
		for(Element el:elList){
			rstList.add(this.rebuildByContent(el, "name"));
		}
		this.sortByAttributeClassName(rstList);
		for(Element el:rstList){
			System.out.println("ading..."+el.attributeValue("class"));
			newRoot.add(el);
		}
		return newDoc;
	}
	
	private List<Element> sortByAttributeClassName(List<Element> srcList){
		Collections.sort(srcList,new Comparator<Element>() {
			public int compare(Element e1, Element e2) {
				String attr1=e1.attributeValue("class");
				String attr2=e2.attributeValue("class");
				String cn1=attr1.substring(attr1.lastIndexOf(".")+1);
				String cn2=attr2.substring(attr2.lastIndexOf(".")+1);
				if(cn1.contains("Dao")&&!cn2.contains("Dao")||
						cn2.contains("Action")&&!cn1.contains("Action")){
					return -1;
				}
				if(cn1.contains("Action")&&!cn2.contains("Action")
						||cn2.contains("Dao")&&!cn1.contains("Dao")){
					return 1;
				}
				return cn1.compareToIgnoreCase(cn2);
			}});
		return srcList;
	}
	private List<Element> sortByAttr(final List<Element> srcList,final String attr){
		List<Element> rstList=new ArrayList<Element>();
		if(srcList!=null&&srcList.size()!=0){
			for(Element el:srcList){
				Element newEl=el.createCopy();
				rstList.add(newEl);
			}
			Collections.sort(rstList,new Comparator<Element>() {
				public int compare(Element e1, Element e2) {
					String n1=e1.attributeValue(attr);
					String n2=e2.attributeValue(attr);
					if(n1==null){
						return 1;
					}
					if(n2==null){
						return -1;
					}
					return n1.compareToIgnoreCase(n2);
				}});
		}
		return rstList;
	}
	@SuppressWarnings("unchecked")
	private Element rebuildByContent(final Element src,final String attr){
		Element rst=src.createCopy();//DocumentHelper.createElement(el.getQName());
		rst.clearContent();
		List<Element> childList=src.elements();
		if(childList!=null&&childList.size()!=0){
			List<Element> newChildList=this.sortByAttr(childList, attr);
			for(Element newChild:newChildList){
				rst.add(newChild);
			}
		}
		return rst;
	}
	
	
	public static void main(String[] args) throws IOException {
		String src="d:\\app-ctx-automatic.xml";
		String dest="d:\\sb.xml";
		
		SortXml sx=new SortXml();
		Document doc=null;
		File file=new File(src);
		if(file.exists()){
			SAXReader reader = new SAXReader();
			try {
				doc = reader.read(file);
			} catch (DocumentException e) {
				error("读取xml"+src+"失败");
				return;
			}
		}
		Document newDoc=sx.sort(doc);
		OutputFormat format = OutputFormat.createPrettyPrint();  
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(dest),"UTF-8"), format);
		writer.write(newDoc);
		writer.close();
	}
	public static void error(String src){
		System.out.println("======================================================");
		System.out.println(src);
		System.out.println("======================================================");
	};
}