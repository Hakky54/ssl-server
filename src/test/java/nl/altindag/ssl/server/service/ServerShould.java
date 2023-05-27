/*
 * Copyright 2023 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.ssl.server.service;

import nl.altindag.ssl.SSLFactory;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hakan Altindag
 */
public class ServerShould {

    @Test
    public void start() throws IOException {
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

        Response response = executeRequest("https://localhost:8443/api/hello", sslFactoryForClient.getSslSocketFactory());

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Hello World!");

        server.stop();
    }

    private Response executeRequest(String url, SSLSocketFactory sslSocketFactory) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setSSLSocketFactory(sslSocketFactory);
        connection.setRequestMethod("GET");

        int statusCode = connection.getResponseCode();
        String body = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        connection.disconnect();
        return new Response(statusCode, body);
    }


    private static final class Response {
        private final int statusCode;
        private final String body;

        Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }

}
