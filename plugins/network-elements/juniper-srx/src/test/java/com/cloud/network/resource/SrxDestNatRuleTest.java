package com.cloud.network.resource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SrxDestNatRuleTest extends XmlRuleTestCase {
    private final String _testData = "<rule>" +
        "<name>destnatrule-12345678p</name>" +
        "<dest-nat-rule-match>" +
        "<destination-address>" +
        "<dst-addr>192.168.2.197</dst-addr>" +
        "</destination-address>" +
        "<destination-port>" +
        "<name>99</name>" +
        "</destination-port>" +
        "</dest-nat-rule-match>" +
        "<then>" +
        "<destination-nat>" +
        "<pool>" +
        "<pool-name>10-5-17-75-199</pool-name>" +
        "</pool>" +
        "</destination-nat>" +
        "</then>" +
        "</rule>";

    public void testParseDestNatRule() throws Exception {
        Document doc = getDocument(_testData);
        Node node = doc.getDocumentElement();

        SrxDestNatRule rule = new SrxDestNatRule(node, "test-ruleset");

        assertEquals("destnatrule-12345678p", rule.getName());
        assertEquals("192.168.2.197", rule.getPublicIp());
        assertEquals("10.5.17.75", rule.getPrivateIp());
        assertEquals(99, rule.getSourcePort());
        assertEquals(199, rule.getDestPort());
        assertEquals("test-ruleset", rule.getRuleSet());
        assertEquals("10-5-17-75-199", rule.getPoolName());
    }

    public void testToString() throws Exception {
        Document doc = getDocument(_testData);
        Node node = doc.getDocumentElement();

        SrxDestNatRule rule = new SrxDestNatRule(node, "test-ruleset");

        assertEquals("DestNat 192.168.2.197:99 -> 10.5.17.75:199", rule.toString());
    }
}