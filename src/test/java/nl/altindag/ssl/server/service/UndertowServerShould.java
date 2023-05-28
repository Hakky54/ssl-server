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

import org.junit.jupiter.api.Test;
import org.xnio.SslClientAuthMode;

import javax.net.ssl.SSLParameters;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hakan Altindag
 */
public class UndertowServerShould {

    @Test
    public void mapNeedClientAuth() {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setNeedClientAuth(true);

        SslClientAuthMode clientAuthMode = UndertowServer.getClientAuthMode(sslParameters);
        assertThat(clientAuthMode).isEqualTo(SslClientAuthMode.REQUIRED);
    }

    @Test
    public void mapWantClientAuth() {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setWantClientAuth(true);

        SslClientAuthMode clientAuthMode = UndertowServer.getClientAuthMode(sslParameters);
        assertThat(clientAuthMode).isEqualTo(SslClientAuthMode.REQUESTED);
    }

    @Test
    public void mapNoneClientAuth() {
        SSLParameters sslParameters = new SSLParameters();
        sslParameters.setWantClientAuth(false);
        sslParameters.setNeedClientAuth(false);

        SslClientAuthMode clientAuthMode = UndertowServer.getClientAuthMode(sslParameters);
        assertThat(clientAuthMode).isEqualTo(SslClientAuthMode.NOT_REQUESTED);
    }

}
