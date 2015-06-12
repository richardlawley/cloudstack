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

        JsonError jsonError = gson.fromJson(strVirtualNetworkToModifyResultJson, JsonError.class);

        if (jsonError.error != null) {
            throw new IllegalStateException(jsonError.error.message);
        }
    }

    public void deleteVirtualNetwork(long id) {
        final Gson gson = new GsonBuilder().create();

        VirtualNetworkToDelete virtualNetworkToDelete = new VirtualNetworkToDelete(id);

        String strVolumeToDeleteJson = gson.toJson(virtualNetworkToDelete);

        String strVirtualNetworkToDeleteResultJson = executeJsonRpc(strVolumeToDeleteJson);

        JsonError jsonError = gson.fromJson(strVirtualNetworkToDeleteResultJson, JsonError.class);

        if (jsonError.error != null) {
            throw new IllegalStateException(jsonError.error.message);
        }
    }

    public long createVolume(String name, long accountId, long totalSize, long minIops, long maxIops, long burstIops) {
        final Gson gson = new GsonBuilder().create();

        VolumeToCreate volumeToCreate = new VolumeToCreate(name, accountId, totalSize, true, minIops, maxIops, burstIops);

        String strVolumeToCreateJson = gson.toJson(volumeToCreate);

        String strVolumeCreateResultJson = executeJsonRpc(strVolumeToCreateJson);

        VolumeCreateResult volumeCreateResult = gson.fromJson(strVolumeCreateResultJson, VolumeCreateResult.class);

        verifyResult(volumeCreateResult.result, strVolumeCreateResultJson, gson);

        return volumeCreateResult.result.volumeID;
    }

    public void modifyVolume(long volumeId, long size, long minIops, long maxIops, long burstIops)
    {
        final Gson gson = new GsonBuilder().create();

        VolumeToModify volumeToModify = new VolumeToModify(volumeId, size, minIops, maxIops, burstIops);

        String strVolumeToModifyJson = gson.toJson(volumeToModify);

        String strVolumeModifyResultJson = executeJsonRpc(strVolumeToModifyJson);

        JsonError jsonError = gson.fromJson(strVolumeModifyResultJson, JsonError.class);

        if (jsonError.error != null) {
            throw new IllegalStateException(jsonError.error.message);
        }
    }

    public void deleteVolume(long id)
    {
        final Gson gson = new GsonBuilder().create();

        VolumeToDelete volumeToDelete = new VolumeToDelete(id);

        String strVolumeToDeleteJson = gson.toJson(volumeToDelete);

        String strVolumeToDeleteResultJson = executeJsonRpc(strVolumeToDeleteJson);

        JsonError jsonError = gson.fromJson(strVolumeToDeleteResultJson, JsonError.class);

        if (jsonError.error != null) {
            throw new IllegalStateException(jsonError.error.message);
        }
    }

    public long createSolidFireAccount(String strAccountName)
    {
        final Gson gson = new GsonBuilder().create();

        AccountToAdd accountToAdd = new AccountToAdd(strAccountName);

        String strAccountAddJson = gson.toJson(accountToAdd);

        String strAccountAddResultJson = executeJsonRpc(strAccountAddJson);

        AccountAddResult accountAddResult = gson.fromJson(strAccountAddResultJson, AccountAddResult.class);

        verifyResult(accountAddResult.result, strAccountAddResultJson, gson);

        return accountAddResult.result.accountID;
    }

    public SolidFireAccount getSolidFireAccount(String sfAccountName) {
        try {
            return getSolidFireAccountByName(sfAccountName);
        } catch (Exception ex) {
            return null;
        }
    }

    public SolidFireAccount getSolidFireAccountById(long lSfAccountId)
    {
        final Gson gson = new GsonBuilder().create();

        AccountToGetById accountToGetById = new AccountToGetById(lSfAccountId);

        String strAccountToGetByIdJson = gson.toJson(accountToGetById);

        String strAccountGetByIdResultJson = executeJsonRpc(strAccountToGetByIdJson);

        AccountGetResult accountGetByIdResult = gson.fromJson(strAccountGetByIdResultJson, AccountGetResult.class);

        verifyResult(accountGetByIdResult.result, strAccountGetByIdResultJson, gson);

        String strSfAccountName = accountGetByIdResult.result.account.username;
        String strSfAccountInitiatorSecret = accountGetByIdResult.result.account.initiatorSecret;
        String strSfAccountTargetSecret = accountGetByIdResult.result.account.targetSecret;

        return new SolidFireAccount(lSfAccountId, strSfAccountName, strSfAccountInitiatorSecret, strSfAccountTargetSecret);
    }

    public SolidFireAccount getSolidFireAccountByName(String strSfAccountName)
    {
        final Gson gson = new GsonBuilder().create();

        AccountToGetByName accountToGetByName = new AccountToGetByName(strSfAccountName);

        String strAccountToGetByNameJson = gson.toJson(accountToGetByName);

        String strAccountGetByNameResultJson = executeJsonRpc(strAccountToGetByNameJson);

        AccountGetResult accountGetByNameResult = gson.fromJson(strAccountGetByNameResultJson, AccountGetResult.class);

        verifyResult(accountGetByNameResult.result, strAccountGetByNameResultJson, gson);

        long lSfAccountId = accountGetByNameResult.result.account.accountID;
        String strSfAccountInitiatorSecret = accountGetByNameResult.result.account.initiatorSecret;
        String strSfAccountTargetSecret = accountGetByNameResult.result.account.targetSecret;

        return new SolidFireAccount(lSfAccountId, strSfAccountName, strSfAccountInitiatorSecret, strSfAccountTargetSecret);
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

    @SuppressWarnings("unused")
    private static final class VirtualNetworkToDelete
    {
        private final String method = "RemoveVirtualNetwork";
        private final VirtualNetworkToDeleteParams params;

        private VirtualNetworkToDelete(long id) {
            params = new VirtualNetworkToDeleteParams(id);
        }

        private static final class VirtualNetworkToDeleteParams {
            private long virtualNetworkID;

            private VirtualNetworkToDeleteParams(long id) {
                virtualNetworkID = id;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class VolumeToCreate {
        private final String method = "CreateVolume";
        private final VolumeToCreateParams params;

        private VolumeToCreate(final String strVolumeName, final long lAccountId, final long lTotalSize, final boolean bEnable512e,
                final long lMinIOPS, final long lMaxIOPS, final long lBurstIOPS) {
            params = new VolumeToCreateParams(strVolumeName, lAccountId, lTotalSize, bEnable512e, lMinIOPS, lMaxIOPS, lBurstIOPS);
        }

        private static final class VolumeToCreateParams {
            private final String name;
            private final long accountID;
            private final long totalSize;
            private final boolean enable512e;
            private final VolumeToCreateParamsQoS qos;

            private VolumeToCreateParams(final String strVolumeName, final long lAccountId, final long lTotalSize, final boolean bEnable512e,
                    final long lMinIOPS, final long lMaxIOPS, final long lBurstIOPS) {
                name = strVolumeName;
                accountID = lAccountId;
                totalSize = lTotalSize;
                enable512e = bEnable512e;

                qos = new VolumeToCreateParamsQoS(lMinIOPS, lMaxIOPS, lBurstIOPS);
            }

            private static final class VolumeToCreateParamsQoS {
                private final long minIOPS;
                private final long maxIOPS;
                private final long burstIOPS;

                private VolumeToCreateParamsQoS(final long lMinIOPS, final long lMaxIOPS, final long lBurstIOPS) {
                    minIOPS = lMinIOPS;
                    maxIOPS = lMaxIOPS;
                    burstIOPS = lBurstIOPS;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class VolumeToModify
    {
        private final String method = "ModifyVolume";
        private final VolumeToModifyParams params;

        private VolumeToModify(long id, long totalSize, long minIOPS, long maxIOPS, long burstIOPS)
        {
            params = new VolumeToModifyParams(id, totalSize, minIOPS, maxIOPS, burstIOPS);
        }

        private static final class VolumeToModifyParams
        {
            private final long volumeID;
            private final long totalSize;
            private final VolumeToModifyParamsQoS qos;

            private VolumeToModifyParams(long id, long totalSize, long minIOPS, long maxIOPS, long burstIOPS)
            {
                this.volumeID = id;
                this.totalSize = totalSize;

                this.qos = new VolumeToModifyParamsQoS(minIOPS, maxIOPS, burstIOPS);
            }
        }

        private static final class VolumeToModifyParamsQoS {
            private final long minIOPS;
            private final long maxIOPS;
            private final long burstIOPS;

            private VolumeToModifyParamsQoS(long minIOPS, long maxIOPS, long burstIOPS) {
                this.minIOPS = minIOPS;
                this.maxIOPS = maxIOPS;
                this.burstIOPS = burstIOPS;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class VolumeToDelete
    {
        private final String method = "DeleteVolume";
        private final VolumeToDeleteParams params;

        private VolumeToDelete(final long lVolumeId) {
            params = new VolumeToDeleteParams(lVolumeId);
        }

        private static final class VolumeToDeleteParams {
            private long volumeID;

            private VolumeToDeleteParams(final long lVolumeId) {
                volumeID = lVolumeId;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class AccountToAdd
    {
        private final String method = "AddAccount";
        private final AccountToAddParams params;

        private AccountToAdd(final String strAccountName)
        {
            params = new AccountToAddParams(strAccountName);
        }

        private static final class AccountToAddParams
        {
            private final String username;

            private AccountToAddParams(final String strAccountName)
            {
                username = strAccountName;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class AccountToGetById
    {
        private final String method = "GetAccountByID";
        private final AccountToGetByIdParams params;

        private AccountToGetById(final long lAccountId)
        {
            params = new AccountToGetByIdParams(lAccountId);
        }

        private static final class AccountToGetByIdParams
        {
            private final long accountID;

            private AccountToGetByIdParams(final long lAccountId)
            {
                accountID = lAccountId;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class AccountToGetByName
    {
        private final String method = "GetAccountByName";
        private final AccountToGetByNameParams params;

        private AccountToGetByName(final String strUsername)
        {
            params = new AccountToGetByNameParams(strUsername);
        }

        private static final class AccountToGetByNameParams
        {
            private final String username;

            private AccountToGetByNameParams(final String strUsername)
            {
                username = strUsername;
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

    private static final class VolumeCreateResult {
        private Result result;

        private static final class Result {
            private long volumeID;
        }
    }

    private static final class AccountAddResult {
        private Result result;

        private static final class Result {
            private long accountID;
        }
    }

    private static final class AccountGetResult {
        private Result result;

        private static final class Result {
            private Account account;

            private static final class Account {
                private long accountID;
                private String username;
                private String initiatorSecret;
                private String targetSecret;
            }
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

    public static class SolidFireAccount
    {
        private final long _id;
        private final String _name;
        private final String _initiatorSecret;
        private final String _targetSecret;

        public SolidFireAccount(long id, String name, String initiatorSecret, String targetSecret) {
            _id = id;
            _name = name;
            _initiatorSecret = initiatorSecret;
            _targetSecret = targetSecret;
        }

        public long getId() {
            return _id;
        }

        public String getName() {
            return _name;
        }

        public String getInitiatorSecret() {
            return _initiatorSecret;
        }

        public String getTargetSecret() {
            return _targetSecret;
        }

        @Override
        public int hashCode() {
            return (_id + _name).hashCode();
        }

        @Override
        public String toString() {
            return _name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!obj.getClass().equals(SolidFireAccount.class)) {
                return false;
            }

            SolidFireAccount sfa = (SolidFireAccount)obj;

            if (_id == sfa._id && _name.equals(sfa._name) &&
                _initiatorSecret.equals(sfa._initiatorSecret) &&
                _targetSecret.equals(sfa._targetSecret)) {
                return true;
            }

            return false;
        }
    }
}
