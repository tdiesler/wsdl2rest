/*
 * #%L
 * Wildfly Camel :: Testsuite
 * %%
 * Copyright (C) 2013 - 2014 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.jboss.fuse.wsdl2rest.test.entesb14876;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.example.TestRequest;
import org.example.TestRequestResponse;
import org.jboss.fuse.wsdl2rest.EndpointInfo;
import org.jboss.fuse.wsdl2rest.impl.Wsdl2Rest;
import org.jboss.fuse.wsdl2rest.util.SpringCamelContextFactory;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


public class ENTESB14876DocLitTest {

    @Test
    public void testJavaClient() throws Exception {
        
    	Path resources = Paths.get("src/test/resources/entesb14876");
        File wsdlFile = resources.resolve("hello.wsdl").toFile();
        Assert.assertTrue(wsdlFile.exists());
        
        Path outpath = Paths.get("target/generated-wsd2rest");
        Wsdl2Rest wsdl2Rest = new Wsdl2Rest(wsdlFile.toURI().toURL(), outpath);
        wsdl2Rest.setCamelContext(Paths.get("camel/entesb14876-camel-context.xml"));

//        List<EndpointInfo> clazzDefs = wsdl2Rest.process();
//        Assert.assertEquals(1, clazzDefs.size());
//        EndpointInfo clazzDef = clazzDefs.get(0);
//        Assert.assertEquals("org.example", clazzDef.getPackageName());
//        Assert.assertEquals("Hello", clazzDef.getClassName());
        
        File camelContextFile = resources.resolve("entesb14876-camel-context.xml").toFile();
        Assert.assertTrue(camelContextFile.exists());
        
        URL resourceUrl = camelContextFile.toURI().toURL();
        CamelContext camelctx = SpringCamelContextFactory.createSingleCamelContext(resourceUrl, null);
        camelctx.start();
        try {
            Assert.assertEquals(ServiceStatus.Started, camelctx.getStatus());

            Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class);
            
            TestRequest tstReq = new TestRequest();
            tstReq.setVal1("foo");
            tstReq.setVal2("bar");
            
            Response res = client.target("http://localhost:8081/jaxrs/testrequest").request()
            	.build(HttpMethod.GET, Entity.json(tstReq)).invoke();
            
            Assert.assertEquals(200, res.getStatus());
            String tstRes = res.readEntity(String.class);
            //TestRequest tstRes = res.readEntity(TestRequest.class);
            System.out.println(tstRes);

        } finally {
            camelctx.stop();
        }
    }
}
