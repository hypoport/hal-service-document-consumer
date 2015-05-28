/**
 *  Copyright 2014 HYPOPORT AG
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

public class ServiceDocumentImplTest {

  ServiceDocumentImpl document;
  HttpServer httpServer;

  @BeforeClass
  public void startHttpServer() throws IOException {
    httpServer = HttpServer.create(new InetSocketAddress(0), 0);
    httpServer.createContext("/example-service-document.hal.json", new HttpHandler() {
      @Override
      public void handle(HttpExchange httpExchange) throws IOException {
        String document = IOUtils.toString(ServiceDocumentImplTest.this.getClass().getResourceAsStream("example-service-document.hal.json"));
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
    assertThat(document.getLink("self")).isNotNull();
    assertThat(document.getLink("http://www.foo.io/rel/persons")).isNotNull();
  }

  @Test
  public void link_contains_href() {
    assertThat(document.getLink("self").getHref()).isEqualTo(URI.create("http://foo.io/"));
    assertThat(document.getLink("http://www.foo.io/rel/persons").getHref()).isEqualTo(URI.create("http://foo.io/persons/"));
  }

  @Test
  public void href_template_recognizion() {
    Link link = document.getLink("http://www.foo.io/rel/carsearch");
    assertThat(link.hasTemplate()).isTrue();
  }

  /**
   * @see @link http://tools.ietf.org/html/rfc6570
   */
  @Test
  public void href_template_expansion_level1() {
    Link link = document.getLink("http://www.foo.io/rel/carsearch");

    URI href = link.getTemplate().set("color", "blue").expand();

    assertThat(href.toString()).isEqualTo("https://www.foo.io/cars?color=blue");
  }

  @Test
  public void href_template_expansion_level1_multiple_params() {
    Link link = document.getLink("http://www.foo.io/rel/petsearch");

    URI href = link.getTemplate()
        .set("food", "meet")
        .set("keeping", "kennel")
        .expand();

    assertThat(href.toString()).isEqualTo("https://www.foo.io/pets?food=meet&keeping=kennel");
  }

  @Test
  public void href_template_expansion_level2() {
    Link link = document.getLink("http://www.foo.io/rel/petsearch");

    URI href = link.getTemplate()
        .set("food", "meet")
        .set("keeping", "dog stable")
        .expand();

    assertThat(href.toString()).isEqualTo("https://www.foo.io/pets?food=meet&keeping=dog%20stable");
  }

  @Test
  public void without_Cache_the_serviceDocument_will_be_downloaded_multiple_times() throws IOException {

    assertThat(document.getLink("http://www.foo.io/rel/persons").getHref()).isEqualTo(URI.create("http://foo.io/persons/"));
    assertThat(document.getLink("http://www.foo.io/rel/persons").getHref()).isEqualTo(URI.create("http://foo.io/persons/"));

    verify(document, times(2)).downloadServiceDocument(any(URL.class), any(Proxy.class), Matchers.<RequestProperty[]>any());
  }

  @Test
  public void with_CacheEnabled_the_serviceDocument_is_downloaded_only_once() throws IOException {

    boolean withCacheEnabled = true;
    assertThat(document.getLink("http://www.foo.io/rel/persons", withCacheEnabled).getHref()).isEqualTo(URI.create("http://foo.io/persons/"));
    assertThat(document.getLink("http://www.foo.io/rel/persons", withCacheEnabled).getHref()).isEqualTo(URI.create("http://foo.io/persons/"));

    verify(document, times(1)).downloadServiceDocument(any(URL.class), any(Proxy.class), Matchers.<RequestProperty[]>any());
  }
}
