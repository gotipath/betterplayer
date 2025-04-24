/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jhomlala.better_player;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallbackException;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException;
import com.google.android.exoplayer2.upstream.StatsDataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link MediaDrmCallback} that makes requests using {@link DataSource} instances.
 *
 * @deprecated com.google.android.exoplayer2 is deprecated. Please migrate to androidx.media3 (which
 * contains the same ExoPlayer code). See <a
 * href="https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide">the
 * migration guide</a> for more details, including a script to help with the migration.
 */
@Deprecated
public final class HttpMediaDrmCallbackCustom implements MediaDrmCallback {

    private static final int MAX_MANUAL_REDIRECTS = 5;

    private final DataSource.Factory dataSourceFactory;
    @Nullable
    private final String defaultLicenseUrl;
    private final boolean forceDefaultLicenseUrl;
    private final Map<String, String> keyRequestProperties;

    /**
     * Constructs an instance.
     *
     * @param defaultLicenseUrl The default license URL. Used for key requests that do not specify
     *                          their own license URL. May be {@code null} if it's known that all key requests will specify
     *                          their own URLs.
     * @param dataSourceFactory A factory from which to obtain {@link DataSource} instances. This will
     *                          usually be an HTTP-based {@link DataSource}.
     */
    public HttpMediaDrmCallbackCustom(@Nullable String defaultLicenseUrl, DataSource.Factory dataSourceFactory) {
        this(defaultLicenseUrl, /* forceDefaultLicenseUrl= */ false, dataSourceFactory);
    }

    /**
     * Constructs an instance.
     *
     * @param defaultLicenseUrl      The default license URL. Used for key requests that do not specify
     *                               their own license URL, or for all key requests if {@code forceDefaultLicenseUrl} is set to
     *                               true. May be {@code null} if {@code forceDefaultLicenseUrl} is {@code false} and if it's
     *                               known that all key requests will specify their own URLs.
     * @param forceDefaultLicenseUrl Whether to force use of {@code defaultLicenseUrl} for key
     *                               requests that include their own license URL.
     * @param dataSourceFactory      A factory from which to obtain {@link DataSource} instances. This will
     *                               * usually be an HTTP-based {@link DataSource}.
     */
    public HttpMediaDrmCallbackCustom(@Nullable String defaultLicenseUrl, boolean forceDefaultLicenseUrl, DataSource.Factory dataSourceFactory) {
        Assertions.checkArgument(!(forceDefaultLicenseUrl && TextUtils.isEmpty(defaultLicenseUrl)));
        this.dataSourceFactory = dataSourceFactory;
        this.defaultLicenseUrl = defaultLicenseUrl;
        this.forceDefaultLicenseUrl = forceDefaultLicenseUrl;
        this.keyRequestProperties = new HashMap<>();
    }

    /**
     * Sets a header for key requests made by the callback.
     *
     * @param name  The name of the header field.
     * @param value The value of the field.
     */
    public void setKeyRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        synchronized (keyRequestProperties) {
            keyRequestProperties.put(name, value);
        }
    }

    /**
     * Clears a header for key requests made by the callback.
     *
     * @param name The name of the header field.
     */
    public void clearKeyRequestProperty(String name) {
        Assertions.checkNotNull(name);
        synchronized (keyRequestProperties) {
            keyRequestProperties.remove(name);
        }
    }

    /**
     * Clears all headers for key requests made by the callback.
     */
    public void clearAllKeyRequestProperties() {
        synchronized (keyRequestProperties) {
            keyRequestProperties.clear();
        }
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws MediaDrmCallbackException {
        return new byte[0];
//        String url = request.getDefaultUrl() + "&signedRequest=" + Util.fromUtf8Bytes(request.getData());
//        return executePost(dataSourceFactory, url,
//                /* httpBody= */ null,
//                /* requestProperties= */ Collections.emptyMap());
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws MediaDrmCallbackException {
        String url = request.getLicenseServerUrl();
        if (forceDefaultLicenseUrl || TextUtils.isEmpty(url)) {
            url = defaultLicenseUrl;
        }
        if (TextUtils.isEmpty(url)) {
            throw new MediaDrmCallbackException(new DataSpec.Builder().setUri(Uri.EMPTY).build(), Uri.EMPTY,
                    /* responseHeaders= */ ImmutableMap.of(),
                    /* bytesLoaded= */ 0,
                    /* cause= */ new IllegalStateException("No license URL"));
        }
        Map<String, String> requestProperties = new HashMap<>();
        // Add standard request properties for supported schemes.
        String contentType = C.PLAYREADY_UUID.equals(uuid) ? "text/xml" : (C.CLEARKEY_UUID.equals(uuid) ? "application/json" : "application/octet-stream");
        requestProperties.put("Content-Type", contentType);
        if (C.PLAYREADY_UUID.equals(uuid)) {
            requestProperties.put("SOAPAction", "http://schemas.microsoft.com/DRM/2007/03/protocols/AcquireLicense");
        }
        // Add additional request properties.
        synchronized (keyRequestProperties) {
            requestProperties.putAll(keyRequestProperties);
        }
//        Log.d("executeKeyRequest Data", Arrays.toString(request.getData()));
//        Log.d("executeKeyRequest Type", String.valueOf(request.getRequestType()));
//        Log.d("executeKeyRequest Url", url);
//        Log.d("executeKeyRequest Body", requestProperties.toString());
        return executePostCustom(dataSourceFactory, url, request.getData(), requestProperties);
    }

    private static byte[] executePost(DataSource.Factory dataSourceFactory, String url, @Nullable byte[] httpBody, Map<String, String> requestProperties) throws MediaDrmCallbackException {
        StatsDataSource dataSource = new StatsDataSource(dataSourceFactory.createDataSource());
        int manualRedirectCount = 0;
        DataSpec dataSpec = new DataSpec.Builder().setUri(url).setHttpRequestHeaders(requestProperties).setHttpMethod(DataSpec.HTTP_METHOD_POST)
                .setHttpBody(httpBody).setFlags(DataSpec.FLAG_ALLOW_GZIP).build();
        DataSpec originalDataSpec = dataSpec;
        try {
            while (true) {
                DataSourceInputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
                try {
                    return Util.toByteArray(inputStream);
                } catch (InvalidResponseCodeException e) {
                    @Nullable String redirectUrl = getRedirectUrl(e, manualRedirectCount);
                    if (redirectUrl == null) {
                        throw e;
                    }
                    manualRedirectCount++;
                    dataSpec = dataSpec.buildUpon().setUri(redirectUrl).build();
                } finally {
                    Util.closeQuietly(inputStream);
                }
            }
        } catch (Exception e) {
            throw new MediaDrmCallbackException(originalDataSpec, Assertions.checkNotNull(dataSource.getLastOpenedUri()), dataSource.getResponseHeaders(), dataSource.getBytesRead(),
                    /* cause= */ e);
        }
    }

    private byte[] executePostCustom(DataSource.Factory dataSourceFactory, String url, byte[] data, Map<String, String> requestProperties) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);

            JSONObject json = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray();
                int bitmask = 0x000000FF;
                for (byte aData : data) {
                    jsonArray.put(bitmask & (int) aData);
                }

                json.put("token", requestProperties.get("token"));
                json.put("drm_info", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                data = json.toString().getBytes(StandardCharsets.UTF_8);
            } else {
                data = json.toString().getBytes();
            }

            if (data != null) {
                try (OutputStream out = urlConnection.getOutputStream()) {
                    out.write(data);
                }
            }

            int responseCode = urlConnection.getResponseCode();
            if (responseCode < 400) {
                // Read and return the response body.
                try (InputStream inputStream = urlConnection.getInputStream()) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] scratch = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(scratch)) != -1) {
                        byteArrayOutputStream.write(scratch, 0, bytesRead);
                    }
                    Log.v("RNdrm: execute done ", Arrays.toString(byteArrayOutputStream.toByteArray()));
                    return byteArrayOutputStream.toByteArray();
                }
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Nullable
    private static String getRedirectUrl(InvalidResponseCodeException exception, int manualRedirectCount) {
        // For POST requests, the underlying network stack will not normally follow 307 or 308
        // redirects automatically. Do so manually here.
        boolean manuallyRedirect = (exception.responseCode == 307 || exception.responseCode == 308) && manualRedirectCount < MAX_MANUAL_REDIRECTS;
        if (!manuallyRedirect) {
            return null;
        }
        Map<String, List<String>> headerFields = exception.headerFields;
        if (headerFields != null) {
            @Nullable List<String> locationHeaders = headerFields.get("Location");
            if (locationHeaders != null && !locationHeaders.isEmpty()) {
                return locationHeaders.get(0);
            }
        }
        return null;
    }
}