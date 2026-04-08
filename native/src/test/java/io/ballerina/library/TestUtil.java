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

import org.testng.annotations.BeforeSuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test utilities for the library tool.
 *
 * @since 0.1.0
 */
public class TestUtil {

    static Path testResources;

    @BeforeSuite
    public void beforeSuite() {
        testResources = Paths.get("src/test/resources/");
    }

    protected static String getOutput(Path outputPath, String fileName) throws IOException {
        return Files.readString(outputPath.resolve("unix").resolve(fileName));
    }

    static String readOutput(ByteArrayOutputStream console) throws IOException {
        String output = console.toString();
        console.close();
        PrintStream out = System.out;
        out.println(output);
        return output;
    }
}
