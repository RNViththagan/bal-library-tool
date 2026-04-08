/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.library;

import io.ballerina.library.service.LibraryGetService;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for the library get service.
 *
 * @since 0.1.0
 */
public class LibraryGetServiceTest {

    @Test
    public void testGet() {
        LibraryGetService service = new LibraryGetService();
        String result = service.get(new String[]{"ballerina/http"});
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("ballerina/http"), "Get should return ballerina/http");
        Assert.assertTrue(result.contains("typeDefs"), "Get should include typeDefs");
    }
}
