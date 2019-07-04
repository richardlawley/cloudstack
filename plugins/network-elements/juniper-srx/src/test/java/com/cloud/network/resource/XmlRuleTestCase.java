package com.cloud.network.resource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import com.cloud.utils.exception.ExecutionException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public abstract class XmlRuleTestCase extends TestCase {
    protected Document getDocument(String xml) throws ExecutionException {
        StringReader srcNatRuleReader = new StringReader(xml);
        InputSource srcNatRuleSource = new InputSource(srcNatRuleReader);
        Document doc = null;

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(srcNatRuleSource);
        } catch (Exception e) {
            throw new ExecutionException(e.getMessage());
        }

        if (doc == null) {
            throw new ExecutionException("Failed to parse xml " + xml);
        } else {
            return doc;
        }
    }
}