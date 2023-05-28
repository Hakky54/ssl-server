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

import io.undertow.Undertow;
import io.undertow.util.Headers;
import nl.altindag.ssl.SSLFactory;
import org.xnio.Options;
import org.xnio.SslClientAuthMode;

import javax.net.ssl.SSLParameters;

/**
 * @author Hakan Altindag
 */
public class UndertowServer implements Server {

    private final Undertow server;

    public UndertowServer(SSLFactory sslFactory, int port, String responseBody) {
        SslClientAuthMode clientAuthMode = getClientAuthMode(sslFactory.getSslParameters());

        server = Undertow.builder()
                .addHttpsListener(port, "localhost", sslFactory.getSslContext())
                .setSocketOption(Options.SSL_CLIENT_AUTH_MODE, clientAuthMode)
                .setHandler(exchange -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send(responseBody);
                }).build();
        server.start();
    }

    static SslClientAuthMode getClientAuthMode(SSLParameters sslParameters) {
        if (sslParameters.getNeedClientAuth()) {
            return SslClientAuthMode.REQUIRED;
        } else if (sslParameters.getWantClientAuth()) {
            return SslClientAuthMode.REQUESTED;
        } else {
            return SslClientAuthMode.NOT_REQUESTED;
        }
    }

    @Override
    public void stop() {
        server.stop();
    }

}
