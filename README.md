# Request mapping adaptation of the [Origins](https://github.com/MoodMinds/origins)' `Emittable` and `Traversable` when used in [Spring](https://spring.io)'s `@RestController`

Adaptation of the [Origins](https://github.com/MoodMinds/origins)' `Emittable` and `Traversable`
in Spring's `RequestMappingHandlerAdapter` for use as return type in @RestController in Servlet non-reactive context.

## Usage

Include the provided `TraverseSupportMappingHandlerAdaptation` in your Spring config and be able to use `Emittable` and `Traversable`
as return value in methods marked with `@RequestMapping` annotation:

```java
import org.moodminds.lang.Emittable;
import org.moodminds.lang.Traversable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.moodminds.lang.Emittable.emittable;
import static org.moodminds.lang.Traversable.traversable;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
public class StreamController {

    @GetMapping(path = "/emission", produces = TEXT_EVENT_STREAM_VALUE)
    public Emittable<String, Exception> emission() {
        return emittable(traversable());
    }

    @GetMapping(path = "/traverse", produces = TEXT_EVENT_STREAM_VALUE)
    public Traversable<String, Exception> traverse() {
        return traversable();
    }
}
```

The returning `Emittable` or `Traversable` will be processed with the Spring's `ResponseBodyEmitter` under the hood.

## Maven configuration

Artifacts can be found on [Maven Central](https://search.maven.org/) after publication.

```xml
<dependency>
    <groupId>org.moodminds</groupId>
    <artifactId>origins-spring-web</artifactId>
    <version>${version}</version>
</dependency>
```

## Building from Source

You may need to build from source to use **Origins Spring Web** (until it is in Maven Central) with Maven and JDK 9 at least.

## License
This project is going to be released under version 2.0 of the [Apache License][l].

[l]: https://www.apache.org/licenses/LICENSE-2.0