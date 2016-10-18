package com.devil.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public final class XmlW3cUtil {
	public static String toPrettyXml(Document doc) {
		try (StringWriter sw = new StringWriter()) {
			writeXml(doc, sw);
			return sw.toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void writeXml(Document doc, Writer writer) {
		try {
			StreamResult strResult = new StreamResult(writer);
			TransformerFactory tfac = TransformerFactory.newInstance();
			javax.xml.transform.Transformer t = tfac.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
			// text
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			t.transform(new DOMSource(doc.getDocumentElement()), strResult);
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}
	}
}
