package com.cloud.api.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.cloudstack.api.response.HostResponse;
import org.junit.Test;

public class CommandTest {

    public static final String NAME = "testApi";
    public static final String DESC = "Test Description";
    public static final String VERSION = "4.1.2";
    public static final String PARAMNAME1 = "testParamName1";
    public static final String PARAMNAME2 = "testParamName1";
    public static final String RESPONSEARG1 = "testResponseName1";
    public static final String RESPONSEARG2 = "testResponseName2";

    @Test
    public void ReadAttributesTest() {
        Command command = new Command(NAME, TestApiCmd.class);

        assertEquals(NAME, command.getName());
        assertEquals(DESC, command.getDescription());
        assertFalse(command.isAsync());
        assertFalse(command.isList());
        assertEquals(VERSION, command.getSinceVersion());
    }

    @Test
    public void ReadRequestParamsTest() {
        Command command = new Command(NAME, TestApiCmd.class);

        List<Argument> args = command.getRequest();
        assertEquals(2, args.size());

        Argument p1 = args.get(0);
        Argument p2 = args.get(1);

        assertEquals(PARAMNAME1, p1.getName());
        assertEquals(PARAMNAME2, p2.getName());

        assertEquals("string", p1.getDataType());
        assertEquals("long", p2.getDataType());

        assertTrue("p1 should be required", p1.isRequired());
        assertFalse("p2 should not be required", p2.isRequired());

        assertEquals("Param Description 1", p1.getDescription());
        assertEquals("Param Description 2", p2.getDescription());
    }

    @Test
    public void ReadResponseTest() {
        Command command = new Command(NAME, TestApiCmd.class);

        assertEquals(TestApiCmd.TestApiResponse.class.getName(), command.getResponseType());

        List<Argument> responseArgs = command.getResponse();
        assertEquals(2, responseArgs.size());

        Argument r1 = responseArgs.get(0);
        Argument r2 = responseArgs.get(1);

        assertEquals(RESPONSEARG1, r1.getName());
        assertEquals(RESPONSEARG2, r2.getName());

        assertEquals("string", r1.getDataType());
        assertEquals("long", r2.getDataType());

        assertEquals("Response Arg 1", r1.getDescription());
        assertEquals("Response Arg 2", r2.getDescription());
    }

    @Test
    public void ReadListResponseTest() {
        Command command = new Command("testListVm", TestListHostsCmd.class);

        assertTrue(command.isList());
        assertEquals("Host", command.getListContainerName());
        assertEquals(HostResponse.class.getName(), command.getResponseType());

        List<Argument> responseArgs = command.getResponse();
        Optional<Argument> type = responseArgs.stream().filter(a -> a.getName().equals("type")).findFirst();
        Optional<Argument> hostha = responseArgs.stream().filter(a -> a.getName().equals("hostha")).findFirst();

        // These two are complex parameters which are not further defined anywhere
        assertTrue("type was not present on response", type.isPresent());
        assertEquals("type", type.get().getDataType());

        assertTrue("hostha was not present on response", hostha.isPresent());
        assertEquals("hostharesponse", hostha.get().getDataType());
    }
}