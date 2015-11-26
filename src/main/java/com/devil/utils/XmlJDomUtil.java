package com.devil.utils;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public final class XmlJDomUtil {
	public static Document getXmlDocFromString(String xml) {
		return getXmlDocFromReader(new StringReader(xml));
	}

	public static Document getXmlDocFromReader(Reader reader) {
		Document doc = null;
		SAXReader saxReader = new SAXReader();
		try {
			doc = saxReader.read(reader);
		} catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
		return doc;
	}

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

	public static void writeXml(Document doc, OutputStream out, boolean prerry) {
		XMLWriter writer = null;
		try {
			OutputFormat format = prerry ? OutputFormat.createPrettyPrint() : OutputFormat.createCompactFormat();
			writer = new XMLWriter(new OutputStreamWriter(out, "UTF-8"), format);
			writer.write(doc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommUtil.close(writer);
		}
	}

	public static Document createDocument() {
		return DocumentHelper.createDocument();
	}

}
