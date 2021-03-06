/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client;

import com.carrotsearch.randomizedtesting.generators.RandomInts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.lucene.util.LuceneTestCase;

import java.io.IOException;

public class RestClientBuilderTests extends LuceneTestCase {

    public void testBuild() throws IOException {
        try {
            RestClient.builder((HttpHost[])null);
            fail("should have failed");
        } catch(IllegalArgumentException e) {
            assertEquals("no hosts provided", e.getMessage());
        }

        try {
            RestClient.builder();
            fail("should have failed");
        } catch(IllegalArgumentException e) {
            assertEquals("no hosts provided", e.getMessage());
        }

        try {
            RestClient.builder(new HttpHost[]{new HttpHost("localhost", 9200), null}).build();
            fail("should have failed");
        } catch(NullPointerException e) {
            assertEquals("host cannot be null", e.getMessage());
        }

        try {
            RestClient.builder(new HttpHost("localhost", 9200))
                    .setMaxRetryTimeoutMillis(RandomInts.randomIntBetween(random(), Integer.MIN_VALUE, 0));
            fail("should have failed");
        } catch(IllegalArgumentException e) {
            assertEquals("maxRetryTimeoutMillis must be greater than 0", e.getMessage());
        }

        try {
            RestClient.builder(new HttpHost("localhost", 9200)).setDefaultHeaders(null);
            fail("should have failed");
        } catch(NullPointerException e) {
            assertEquals("default headers must not be null", e.getMessage());
        }

        try {
            RestClient.builder(new HttpHost("localhost", 9200)).setDefaultHeaders(new Header[]{null});
            fail("should have failed");
        } catch(NullPointerException e) {
            assertEquals("default header must not be null", e.getMessage());
        }

        try {
            RestClient.builder(new HttpHost("localhost", 9200)).setFailureListener(null);
            fail("should have failed");
        } catch(NullPointerException e) {
            assertEquals("failure listener must not be null", e.getMessage());
        }

        int numNodes = RandomInts.randomIntBetween(random(), 1, 5);
        HttpHost[] hosts = new HttpHost[numNodes];
        for (int i = 0; i < numNodes; i++) {
            hosts[i] = new HttpHost("localhost", 9200 + i);
        }
        RestClient.Builder builder = RestClient.builder(hosts);
        if (random().nextBoolean()) {
            builder.setHttpClient(HttpClientBuilder.create().build());
        }
        if (random().nextBoolean()) {
            int numHeaders = RandomInts.randomIntBetween(random(), 1, 5);
            Header[] headers = new Header[numHeaders];
            for (int i = 0; i < numHeaders; i++) {
                headers[i] = new BasicHeader("header" + i, "value");
            }
            builder.setDefaultHeaders(headers);
        }
        if (random().nextBoolean()) {
            builder.setMaxRetryTimeoutMillis(RandomInts.randomIntBetween(random(), 1, Integer.MAX_VALUE));
        }
        try (RestClient restClient = builder.build()) {
            assertNotNull(restClient);
        }
    }
}
