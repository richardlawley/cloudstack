package com.cloud.network.resource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SrxStaticNatRuleTest extends XmlRuleTestCase {
    private final String _testData = "<rule>" + "<name>192-168-2-197-10-33-224-112</name>" + "<static-nat-rule-match>"
            + "<destination-address>" + "<dst-addr>192.168.2.197/32</dst-addr>" + "</destination-address>"
            + "</static-nat-rule-match>" + "<then>" + "<static-nat>" + "<prefix>"
            + "<addr-prefix>10.33.224.112/32</addr-prefix>" + "</prefix>" + "</static-nat>" + "</then>" + "</rule>";

    public void testParseStaticNatRule() throws Exception {
        Document doc = getDocument(_testData);
        Node node = doc.getDocumentElement();

        SrxStaticNatRule rule = new SrxStaticNatRule(node);

        assertEquals("192-168-2-197-10-33-224-112", rule.getName());
        assertEquals("192.168.2.197", rule.getPublicIp());
        assertEquals("10.33.224.112", rule.getPrivateIp());
    }

    public void testToString() throws Exception {
        Document doc = getDocument(_testData);
        Node node = doc.getDocumentElement();

        SrxStaticNatRule rule = new SrxStaticNatRule(node);

        assertEquals("StaticNat 192.168.2.197 <-> 10.33.224.112", rule.toString());
    }
}