[![Actions Status](https://github.com/Hakky54/ssl-server/workflows/Build/badge.svg)](https://github.com/Hakky54/ssl-server/actions)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.hakky54%3Assl-server&metric=security_rating)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Assl-server)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.github.hakky54%3Assl-server&metric=coverage)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Assl-server)
[![JDK Compatibility](https://img.shields.io/badge/JDK_Compatibility-8+-blue.svg)](#)
[![Kotlin Compatibility](https://img.shields.io/badge/Kotlin_Compatibility-1.5+-blue.svg)](#)
[![Android API Compatibility](https://img.shields.io/badge/Android_API_Compatibility-24+-blue.svg)](#)
[![Apache2 license](https://img.shields.io/badge/license-Aache2.0-blue.svg)](https://github.com/Hakky54/ssl-server/blob/master/LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.hakky54/ssl-server/badge.svg)](https://mvnrepository.com/artifact/io.github.hakky54/ssl-server)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Assl-server)

# SSL Server 🔐

This is a small Hello World server with SSL/TLS enabled which can be used during integration test to validate your ssl
configuration with Keystores, PEM files or other configurations related to ssl.

Initially I used [BadSSL.com](https://badssl.com/) for one of my
projects, [SSLContext-Kickstart](https://github.com/Hakky54/sslcontext-kickstart), however the certificates would not
get
updated shortly after expiring which resulted into false-positive failing tests. I always needed to temporally disable
those tests till the certificates got updated.
I wanted to have something as easy as possible to just only test my ssl configuration and that is how this project came
into life.

# Install library with:

### Install with [Maven](https://mvnrepository.com/artifact/io.github.hakky54/ssl-server)

```xml

<dependency>
    <groupId>io.github.hakky54</groupId>
    <artifactId>ssl-server</artifactId>
    <version>1.0.2</version>
    <scope>test</scope>
</dependency>
```

### Install with Gradle

```groovy
testImplementation 'io.github.hakky54:ssl-server:1.0.2'
```

### Install with Gradle Kotlin DSL

```kotlin
testImplementation("io.github.hakky54:ssl-server:1.0.2")
```

### Install with Scala SBT

```
libraryDependencies += "io.github.hakky54" % "ssl-server" % "1.0.2" % Test
```

# Usage

Example integration test:

```java
import nl.altindag.ssl.SSLFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerShould {

    @Test
    public void startWithSslEnabledAndRequireMutualAuthentication() throws IOException, InterruptedException {
        SSLFactory sslFactoryForServer = SSLFactory.builder()
                .withIdentityMaterial("keystore/server/identity.jks", "secret".toCharArray())
                .withTrustMaterial("keystore/server/truststore.jks", "secret".toCharArray())
                .withNeedClientAuthentication()
                .build();

        Server server = Server.createDefault(sslFactoryForServer);

        SSLFactory sslFactoryForClient = SSLFactory.builder()
                .withIdentityMaterial("keystore/client/identity.jks", "secret".toCharArray())
                .withTrustMaterial("keystore/client/truststore.jks", "secret".toCharArray())
                .build();

        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslFactoryForClient.getSslContext())
                .sslParameters(sslFactoryForClient.getSslParameters())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://localhost:8443/api/hello"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello World!");

        server.stop();
    }

}
```