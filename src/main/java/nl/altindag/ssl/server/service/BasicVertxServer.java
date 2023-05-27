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

import io.vertx.core.Vertx;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.web.Router;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.server.exception.ServerException;

import javax.net.ssl.SSLParameters;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Hakan Altindag
 */
public class BasicVertxServer implements Server {

    private final Vertx vertx;
    private final HttpServer httpServer;
    private final AtomicBoolean serverStarted = new AtomicBoolean(false);

    public BasicVertxServer(SSLFactory sslFactory, int port, String responseBody) {
        vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        router.route().path("/api/hello").handler(context -> context.response()
                .putHeader("Content-Type", "text/plain")
                .setStatusCode(200)
                .send(responseBody)
        );

        ClientAuth clientAuth = getClientAuth(sslFactory.getSslParameters());
        HttpServerOptions serverOptions = new HttpServerOptions()
                .setSsl(true)
                .setClientAuth(clientAuth);

        sslFactory.getKeyManager()
                .map(KeyCertOptions::wrap)
                .ifPresent(serverOptions::setKeyCertOptions);

        sslFactory.getTrustManager()
                .map(TrustOptions::wrap)
                .ifPresent(serverOptions::setTrustOptions);

        httpServer = vertx.createHttpServer(serverOptions)
                .requestHandler(router);

        httpServer.listen(port, result -> serverStarted.set(true));

        waitTillServerHasBeenStarted();
    }

    static ClientAuth getClientAuth(SSLParameters sslParameters) {
        if (sslParameters.getNeedClientAuth()) {
            return ClientAuth.REQUIRED;
        } else if (sslParameters.getWantClientAuth()) {
            return ClientAuth.REQUEST;
        } else {
            return ClientAuth.NONE;
        }
    }

    private void waitTillServerHasBeenStarted() {
        try {
            while (serverStarted.get()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }

            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(e);
        }
    }

    @Override
    public void stop() {
        httpServer.close();
        vertx.deploymentIDs().forEach(vertx::undeploy);
        vertx.close();
    }

}
