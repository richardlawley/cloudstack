// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.solidfire.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.cloudstack.utils.security.SSLUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;

import com.cloud.utils.exception.CloudRuntimeException;

@SuppressWarnings("deprecation")
public class SolidFireConnection {
    private final String _managementVip;
    private final int _managementPort = 443;
    private final String _clusterAdminUsername;
    private final String _clusterAdminPassword;

    public SolidFireConnection(String managementVip, String clusterAdminUsername, String clusterAdminPassword) {
        _managementVip = managementVip;
        _clusterAdminUsername = clusterAdminUsername;
        _clusterAdminPassword = clusterAdminPassword;
    }

    public String getClusterName() {
        final Gson gson = new GsonBuilder().create();

        ClusterInfoToGet clusterInfoToGet = new ClusterInfoToGet();

        String strClusterInfoToGetJson = gson.toJson(clusterInfoToGet);

        String strClusterInfoToGetResultJson = executeJsonRpc(strClusterInfoToGetJson);

        ClusterInfoGetResult clusterInfoGetResult = gson.fromJson(strClusterInfoToGetResultJson, ClusterInfoGetResult.class);

        verifyResult(clusterInfoGetResult.result, strClusterInfoToGetResultJson, gson);

        return clusterInfoGetResult.result.clusterInfo.name;
    }

    public long createVirtualNetwork(String name, String tag, String startIp, int size, String netmask, String svip) {
        final Gson gson = new GsonBuilder().create();

        VirtualNetworkToAdd virtualNetworkToAdd = new VirtualNetworkToAdd(name, tag, startIp, size, netmask, svip);

        String strVirtualNetworkToAddJson = gson.toJson(virtualNetworkToAdd);

        String strVirtualNetworkToAddResultJson = executeJsonRpc(strVirtualNetworkToAddJson);

        VirtualNetworkAddResult virtualNetworkAddResult = gson.fromJson(strVirtualNetworkToAddResultJson, VirtualNetworkAddResult.class);

        verifyResult(virtualNetworkAddResult.result, strVirtualNetworkToAddResultJson, gson);

        return virtualNetworkAddResult.result.virtualNetworkID;
    }

    public void modifyVirtualNetwork(long id, String name, String tag, String startIp, int size, String netmask, String svip) {
        final Gson gson = new GsonBuilder().create();

        VirtualNetworkToModify virtualNetworkToModify = new VirtualNetworkToModify(id, name, tag, startIp, size, netmask, svip);

        String strVirtualNetworkToModifyJson = gson.toJson(virtualNetworkToModify);

        String strVirtualNetworkToModifyResultJson = executeJsonRpc(strVirtualNetworkToModifyJson);

        VirtualNetworkModifyResult virtualNetworkModifyResult = gson.fromJson(strVirtualNetworkToModifyResultJson, VirtualNetworkModifyResult.class);

        verifyResult(virtualNetworkModifyResult.result, strVirtualNetworkToModifyResultJson, gson);
    }

    private String executeJsonRpc(String strJsonToExecute) {
        DefaultHttpClient httpClient = null;
        StringBuilder sb = new StringBuilder();

        try {
            StringEntity input = new StringEntity(strJsonToExecute);

            input.setContentType("application/json");

            httpClient = getHttpClient(_managementPort);

            URI uri = new URI("https://" + _managementVip + ":" + _managementPort + "/json-rpc/7.0");
            AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_SCHEME);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(_clusterAdminUsername, _clusterAdminPassword);

            httpClient.getCredentialsProvider().setCredentials(authScope, credentials);

            HttpPost postRequest = new HttpPost(uri);

            postRequest.setEntity(input);

            HttpResponse response = httpClient.execute(postRequest);

            if (!isSuccess(response.getStatusLine().getStatusCode())) {
                throw new CloudRuntimeException("Failed on JSON-RPC API call. HTTP error code = " + response.getStatusLine().getStatusCode());
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));) {
                String strOutput;
                while ((strOutput = br.readLine()) != null) {
                    sb.append(strOutput);
                }
            }catch (IOException ex) {
                throw new CloudRuntimeException(ex.getMessage());
            }
        } catch (UnsupportedEncodingException ex) {
            throw new CloudRuntimeException(ex.getMessage());
        } catch (ClientProtocolException ex) {
            throw new CloudRuntimeException(ex.getMessage());
        } catch (IOException ex) {
            throw new CloudRuntimeException(ex.getMessage());
        } catch (URISyntaxException ex) {
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.getConnectionManager().shutdown();
                } catch (Exception t) {
                }
            }
        }

        return sb.toString();
    }

    private static DefaultHttpClient getHttpClient(int iPort) {
        DefaultHttpClient client = null;

        try {
            SSLContext sslContext = SSLUtils.getSSLContext();
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] {tm}, new SecureRandom());

            SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry registry = new SchemeRegistry();

            registry.register(new Scheme("https", iPort, socketFactory));

            BasicClientConnectionManager mgr = new BasicClientConnectionManager(registry);
            client = new DefaultHttpClient();

            return new DefaultHttpClient(mgr, client.getParams());
        } catch (NoSuchAlgorithmException ex) {
            throw new CloudRuntimeException(ex.getMessage());
        } catch (KeyManagementException ex) {
            throw new CloudRuntimeException(ex.getMessage());
        }
        finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private static boolean isSuccess(int iCode) {
        return iCode >= 200 && iCode < 300;
    }

    @SuppressWarnings("unused")
    private static final class ClusterInfoToGet {
        private final String method = "GetClusterInfo";
    }

    @SuppressWarnings("unused")
    private static final class VirtualNetworkToAdd {
        private final String method = "AddVirtualNetwork";
        private final VirtualNetworkToAddParams params;

        public VirtualNetworkToAdd(String name, String tag, String startIp, int size, String netmask, String svip) {
            params = new VirtualNetworkToAddParams(name, tag, startIp, size, netmask, svip);
        }

        private static final class VirtualNetworkToAddParams {
            private final String name;
            private final String virtualNetworkTag;
            private final AddressBlock[] addressBlocks = new AddressBlock[1];
            private final String netmask;
            private final String svip;

            public VirtualNetworkToAddParams(String name, String tag, String startIp, int size, String netmask, String svip) {
                this.name = name;
                this.virtualNetworkTag = tag;

                this.addressBlocks[0] = new AddressBlock(startIp, size);

                this.netmask = netmask;
                this.svip = svip;
            }

            private static final class AddressBlock {
                private final String start;
                private final int size;

                public AddressBlock(String start, int size) {
                    this.start = start;
                    this.size = size;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class VirtualNetworkToModify {
        private final String method = "ModifyVirtualNetwork";
        private final VirtualNetworkToModifyParams params;

        public VirtualNetworkToModify(long id, String name, String tag, String startIp, int size, String netmask, String svip) {
            params = new VirtualNetworkToModifyParams(id, name, tag, startIp, size, netmask, svip);
        }

        private static final class VirtualNetworkToModifyParams {
            private final long virtualNetworkID;
            private final String name;
            private final String virtualNetworkTag;
            private final AddressBlock[] addressBlocks = new AddressBlock[1];
            private final String netmask;
            private final String svip;

            public VirtualNetworkToModifyParams(long id, String name, String tag, String startIp, int size, String netmask, String svip) {
                this.virtualNetworkID = id;
                this.name = name;
                this.virtualNetworkTag = tag;

                this.addressBlocks[0] = new AddressBlock(startIp, size);

                this.netmask = netmask;
                this.svip = svip;
            }

            private static final class AddressBlock {
                private final String start;
                private final int size;

                public AddressBlock(String start, int size) {
                    this.start = start;
                    this.size = size;
                }
            }
        }
    }

    private static final class ClusterInfoGetResult
    {
        private Result result;

        private static final class Result
        {
            private ClusterInfo clusterInfo;

            private static final class ClusterInfo
            {
                private String name;
            }
        }
    }

    private static final class VirtualNetworkAddResult
    {
        private Result result;

        private static final class Result
        {
            private long virtualNetworkID;
        }
    }

    private static final class VirtualNetworkModifyResult
    {
        private Result result;

        private static final class Result
        {
        }
    }

    private static final class JsonError
    {
        private Error error;

        private static final class Error {
            private String message;
        }
    }

    private static void verifyResult(Object result, String strJson, Gson gson) throws IllegalStateException {
        if (result != null) {
            return;
        }

        JsonError jsonError = gson.fromJson(strJson, JsonError.class);

        if (jsonError != null) {
            throw new IllegalStateException(jsonError.error.message);
        }

        throw new IllegalStateException("Problem with the following JSON: " + strJson);
    }
}
