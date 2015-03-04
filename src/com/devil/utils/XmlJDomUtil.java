package com.devil.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public final class XmlJDomUtil {
	public static Document getXmlDocFromFile(File file) {
		Document doc = null;
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			try {
				doc = reader.read(file);
			} catch (DocumentException e) {
				throw new IllegalStateException(e);
			}
		}
		return doc;
	}

	public static Document getXmlDocFromXml(String xml) {
		Document doc = null;
		SAXReader reader = new SAXReader();
		try {
			doc = reader.read(new StringReader(xml));
		} catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
		return doc;
	}

	public static void writeXmlToFile(Document doc, String path) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"), format);
			writer.write(doc);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Document createDocument() {
		return DocumentHelper.createDocument();
	}

}
