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

import io.ballerina.library.cli.LibraryTool;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static io.ballerina.library.TestUtil.getOutput;
import static io.ballerina.library.TestUtil.readOutput;
import static io.ballerina.library.TestUtil.testResources;

/**
 * Test cases for the library tool CLI command.
 *
 * @since 0.1.0
 */
public class LibraryToolTest {

    private ByteArrayOutputStream console;
    private PrintStream printStream;

    @BeforeMethod
    public void setUp() {
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @Test
    public void testHelp() throws IOException {
        LibraryTool cmd = new LibraryTool(printStream);
        new CommandLine(cmd).parseArgs("--help");
        cmd.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "help.txt");
        Assert.assertTrue(buildLog.contains(expected), "Help text mismatched");
    }

    @Test
    public void testSearchNoArgs() throws IOException {
        LibraryTool cmd = new LibraryTool(printStream);
        new CommandLine(cmd).parseArgs("search");
        cmd.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "search-no-args.txt");
        Assert.assertTrue(buildLog.contains(expected), "Search no-args error mismatched");
    }

    @Test
    public void testGetNoArgs() throws IOException {
        LibraryTool cmd = new LibraryTool(printStream);
        new CommandLine(cmd).parseArgs("get");
        cmd.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "get-no-args.txt");
        Assert.assertTrue(buildLog.contains(expected), "Get no-args error mismatched");
    }

    @Test
    public void testUnknownSubcommand() throws IOException {
        LibraryTool cmd = new LibraryTool(printStream);
        new CommandLine(cmd).parseArgs("foo");
        cmd.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "unknown-subcommand.txt");
        Assert.assertTrue(buildLog.contains(expected), "Unknown subcommand error mismatched");
    }
}
