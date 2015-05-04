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

import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;

public class ServiceDocumentImpl implements ServiceDocument {

  private static final Charset UTF8 = Charset.forName("UTF-8");

  final URL url;
  final Proxy proxy;
  final RequestProperty[] requestProperties;

  private final Map<String, Link> simpleStupidLinkCache = new HashMap<>(3);

  public ServiceDocumentImpl(URL url) {
    this(url, (Proxy) null);
  }

  public ServiceDocumentImpl(URL url, RequestProperty... requestProperties) {
    this(url, null, requestProperties);
  }

  public ServiceDocumentImpl(URL url, Proxy proxy, RequestProperty... requestProperties) {
    this.url = url;
    this.proxy = proxy;
    this.requestProperties = requestProperties;
  }

  @Override
  public Link getLink(String relation) {
    return getLink(relation, false);
  }

  @Override
  public Link getLink(String relation, boolean useCachedServiceDocument) {
    if (useCachedServiceDocument && simpleStupidLinkCache.containsKey(relation)) {
      return simpleStupidLinkCache.get(relation);
    }
    com.theoryinpractise.halbuilder.api.Link internalLink = getLinkNoCaching(relation);
    Link link = new Link(internalLink.getRel(), internalLink.getHref(), internalLink.hasTemplate());
    if (useCachedServiceDocument) {
      simpleStupidLinkCache.put(relation, link);
    }
    return link;
  }

  private com.theoryinpractise.halbuilder.api.Link getLinkNoCaching(String relation) {
    InputStream inputStream = downloadServiceDocument(url, proxy, requestProperties);
    ReadableRepresentation representation = getRepresentation(inputStream);
    return representation.getLinkByRel(relation);
  }

  InputStream downloadServiceDocument(URL url, Proxy proxy, RequestProperty[] requestProperties) {
    try {
      URLConnection urlConnection = url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
      urlConnection.addRequestProperty("Accept", "application/hal+json,application/json");
      if (requestProperties != null) {
        for (RequestProperty rp : requestProperties) {
          urlConnection.addRequestProperty(rp.key, rp.value);
        }
      }
      return urlConnection.getInputStream();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ReadableRepresentation getRepresentation(InputStream inputStream) {
    RepresentationFactory representationFactory = new JsonRepresentationFactory();
    try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(inputStream), UTF8)) {
      return representationFactory.readRepresentation(HAL_JSON, reader);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
