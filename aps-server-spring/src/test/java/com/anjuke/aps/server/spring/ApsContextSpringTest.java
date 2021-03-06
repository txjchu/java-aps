package com.anjuke.aps.server.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.anjuke.aps.ApsContext;
import com.anjuke.aps.ModuleVersion;
import com.anjuke.aps.SimpleRequest;
import com.anjuke.aps.SimpleResponse;
import com.anjuke.aps.server.context.ApsContextContainer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class ApsContextSpringTest {

    @Test
    public void test() throws Exception {
        String userdir = System.getProperty("user.dir");

        assertTrue(userdir.endsWith("/aps-server-spring"));

        String parentPath = userdir + "/../aps-test-support/parent-lib/target";

        String childPath = userdir + "/../aps-test-support/child-app";

        ApsContextContainer container = new ApsContextContainer();
        container.setContextLibPath(parentPath);
        container.setAppPathRoot(childPath);
        ApsContext context=new ApsContext();
        container.init(context);
        Set<String> requestMethods = container.getRequestMethods();
        System.out.println(requestMethods);
        assertEquals(Sets.newHashSet(new ModuleVersion("testChildSupport", "beta")),
                container.getModules());


        assertEquals(14, requestMethods.size());
        assertTrue(requestMethods
                .contains(":testChildSupport:childBeanInject.echo"));
        assertTrue(requestMethods
                .contains(":testChildSupport:ingore"));
        assertTrue(requestMethods
                .contains(":testChildSupport:childBeanInject.parentBean"));
        assertTrue(requestMethods
                .contains(":testChildSupport:childBeanXmlConf.echo"));
        assertTrue(requestMethods
                .contains(":testChildSupport:childBeanXmlConf.parentBean"));

        SimpleRequest request = new SimpleRequest();
        SimpleResponse response = new SimpleResponse();
        String msg = "test echo message";
        request.setRequestParams(Arrays.<Object> asList(msg));

        request.setRequestMethod("testChildSupport.childBeanInject.echo");
        container.handle(request, response);
        assertEquals(msg, response.getResult());

        request.setRequestMethod(":testChildSupport:ingore");
        container.handle(request, response);
        assertEquals(msg, response.getResult());

        request.setRequestMethod(":testChildSupport:childBeanXmlConf.echo");
        container.handle(request, response);
        assertEquals(msg, response.getResult());

        request.setRequestParams(Collections.emptyList());

        request.setRequestMethod(":testChildSupport:childBeanInject.parentBean");
        container.handle(request, response);
        assertEquals(ImmutableMap.of("name", "parent"), response.getResult());

        request.setRequestMethod(":testChildSupport:childBeanXmlConf.parentBean");
        container.handle(request, response);
        assertEquals(ImmutableMap.of("name", "parent"), response.getResult());


        request.setRequestMethod(":testChildSupport:childBeanInject.throwException");
        container.handle(request, response);
        assertEquals(9999,response.getStatus());
        assertEquals(Integer.MAX_VALUE,response.getResult());
        System.out.println(response.getErrorMessage().getClass());
        assertEquals("[TestExceptionHandler]",response.getErrorMessage());


        container.destroy(context);

    }
}
