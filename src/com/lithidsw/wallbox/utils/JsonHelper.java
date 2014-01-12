/*
 * Copyright 2013 Jeremie Long
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lithidsw.wallbox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class JsonHelper {

    public String getJsonUrl(String url) {
        return getNetFile(url);
    }

    private String getNetFile(String url) {
        DefaultHttpClient defaultClient = new DefaultHttpClient();
        HttpGet httpGetRequest = new HttpGet(url);
        HttpResponse httpResponse;
        try {
            httpResponse = defaultClient.execute(httpGetRequest);
        } catch (IllegalStateException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (ClientProtocolException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(httpResponse
                    .getEntity().getContent(), "UTF-8"));
        } catch (NullPointerException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        } catch (IllegalStateException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        String json;
        StringBuilder total = new StringBuilder();
        try {
            while ((json = reader.readLine()) != null) {
                if (json.equals("<!doctype html>")) {
                    return null;
                }
                total.append(json + "\n");
            }
        } catch (NullPointerException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return total.toString();
    }
}
