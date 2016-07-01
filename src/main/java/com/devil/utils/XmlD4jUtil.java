package com.devil.utils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public final class XmlD4jUtil {
	public static Document getXmlDoc(String xml) {
		try {
			return DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Document getXmlDoc(Reader reader) {
		Document doc = null;
		SAXReader saxReader = new SAXReader();
		try {
			doc = saxReader.read(reader);
		} catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
		return doc;
	}

	public static Document getXmlDoc(URL url) {
		Document doc = null;
		SAXReader saxReader = new SAXReader();
		try {
			doc = saxReader.read(url);
		} catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
		return doc;
	}

	public static Document getXmlDoc(File file) {
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

	public static String toPrettyXml(Document doc) {
		try (StringWriter sw = new StringWriter()) {
			writeXml(doc, sw, true);
			return sw.toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void writeXml(Document doc, Writer writer, boolean prerry) {
		XMLWriter xmlWriter = null;
		try {
			OutputFormat format = prerry ? OutputFormat.createPrettyPrint() : OutputFormat.createCompactFormat();
			xmlWriter = new XMLWriter(writer, format);
			xmlWriter.write(doc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Document createDocument() {
		return DocumentHelper.createDocument();
	}

	public static Document createUTF8Document() {
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("UTF-8");
		return doc;
	}

}
