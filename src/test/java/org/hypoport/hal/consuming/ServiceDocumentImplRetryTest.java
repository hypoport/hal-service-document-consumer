/**
 *  Copyright 2015 HYPOPORT AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hypoport.hal.consuming;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.mockito.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ServiceDocumentImplRetryTest {

  ServiceDocumentImpl document;
  HttpServer httpServer;

  @BeforeClass
  public void startHttpServer() throws IOException {
    httpServer = HttpServer.create(new InetSocketAddress(0), 0);
    httpServer.createContext("/example-service-document.hal.json", new HttpHandler() {

      int requestCounter = 0;

      @Override
      public void handle(HttpExchange httpExchange) throws IOException {
        if (requestCounter<3) {
          requestCounter++;
          throw new IOException("Test");
        }
        String document = IOUtils.toString(ServiceDocumentImplRetryTest.this.getClass().getResourceAsStream("example-service-document.hal.json"));
        httpExchange.sendResponseHeaders(200, document.getBytes("UTF-8").length);
        httpExchange.getResponseHeaders().add("Content-Type", "application/hal+json");
        IOUtils.write(document, httpExchange.getResponseBody(), "UTF-8");
        httpExchange.getResponseBody().close();
      }
    });
    httpServer.setExecutor(null); // creates a default executor
    httpServer.start();
    System.out.println("http server port: " + httpServer.getAddress().getPort());
  }


  @AfterClass(alwaysRun = true)
  public void shutdownHttpServer() {
    if (httpServer!=null) {
      httpServer.stop(0);
    }
  }

  @BeforeMethod
  public void initialize_example_ServiceDocument() throws MalformedURLException {
    document = spy(new ServiceDocumentImpl(new URL("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/example-service-document.hal.json")));
  }

  @Test
  public void link_exists() {
    assertThat(document.getLink("self", true)).isNotNull();
  }

}
