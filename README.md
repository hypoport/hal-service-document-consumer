hal-service-document-consumer
=============================

A library to consume HAL based service documents of restful services.

This is comfortable thin wrapper around [theory in practice halbuilder](http://www.theoryinpractice.net/post/94282847622/halbuilder-4-x-released) extended with [Handy-URI-Templates](https://github.com/damnhandy/Handy-URI-Templates).

Usage
-----

### Maven

Add dependency to Maven pom.xml

```xml
<dependency>
  <groupId>org.hypoport</groupId>
  <artifactId>hal-service-document-consumer</artifactId>
  <version>2015-05-04T15-08-09</version>
</dependency>
```

### Example

Given this example service document in [HAL](http://stateless.co/hal_specification.html) formatted:

```json
{
  "_links" : {
    "self" : {
      "href" : "http://foo.io/"
    },
    "http://www.foo.io/rel/persons" : {
      "href" : "http://foo.io/persons/"
    },
    "http://www.foo.io/rel/petsearch" : {
      "href" : "https://www.foo.io/pets{?food,keeping}"
    }
  }
}
```

With spring based injections, you can retrieve the href of links of this service document as follows:

```java
@Inject
@Named("exampleServiceDocument")
ServiceDocument exampleDocument;

public void consume() {

  // simple URI retrieval
  URI self = exampleDocument.getLink("self").getHref();

  // template expansion
  Link link = exampleDocument.getLink("http://www.foo.io/rel/petsearch");
  URI petsearch = link.getTemplate()
      .set("food", "meet")
      .set("keeping", "kennel")
      .expand();

}

```

Java based Spring bean configuration:

```java
public class ExampleSpringConfiguration {

  @Bean
  @Named("exampleServiceDocument")
  public ServiceDocument exampleServiceDocument() {
    return new ServiceDocumentImpl("http://foo.io/");
  }
}
```

Optional, you can provide a proxy configuration and HTTTP header fields, e.g. cookies for authentication.

Development Notes
-----------------

Please put you deployment credentials into ``~/.gradle/gradle.properties``.

```
nexusUsername=...
nexusPassword=...
nexusUrl=...
```


Contributors
-----------

- [Martin W. Kirst](https://github.com/nitram509)
- [Timmo Freudl-Gierke](https://github.com/timmo)

License
-----
     Copyright 2014 HYPOPORT AG

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

