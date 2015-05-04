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

import com.damnhandy.uri.template.UriTemplate;

import java.net.URI;


public class Link {

  String relation;

  String href;

  boolean hasTemplate;

  public Link(String relation, String href, boolean hasTemplate) {
    this.relation = relation;
    this.href = href;
    this.hasTemplate = hasTemplate;
  }

  public String getRelation() {
    return relation;
  }

  public URI getHref() {

    return URI.create(href);
  }

  public boolean hasTemplate() {
    return hasTemplate;
  }

  public TemplateExpander getTemplate() {
    return new TemplateExpander();
  }

  public class TemplateExpander {

    final UriTemplate internalTemplate;

    public TemplateExpander() {
      if (!hasTemplate()) {
        throw new IllegalStateException("Href of link is not an URI template.");
      }
      internalTemplate = UriTemplate.fromTemplate(href);
    }

    public TemplateExpander set(String key, Object value) {
      internalTemplate.set(key, value);
      return this;
    }

    public URI expand() {
      String href = internalTemplate.expand();
      return URI.create(href);
    }
  }
}
