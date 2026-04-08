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

package io.ballerina.library.cli;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.library.service.LibraryGetService;
import io.ballerina.library.service.LibrarySearchService;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Entry point for the {@code bal library} CLI tool.
 *
 * @since 0.1.0
 */
@CommandLine.Command(name = "library")
public class LibraryTool implements BLauncherCmd {

    @CommandLine.Parameters(arity = "0..*")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    private final PrintStream outStream;

    public LibraryTool() {
        this.outStream = System.out;
    }

    @Override
    public String getName() {
        return "library";
    }

    @Override
    public void execute() {
        if (helpFlag || argList == null || argList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            printLongDesc(sb);
            outStream.println(sb);
            return;
        }

        String subcommand = argList.get(0);
        String[] args = argList.subList(1, argList.size()).toArray(new String[0]);

        switch (subcommand) {
            case "search" -> {
                if (args.length == 0) {
                    outStream.println("error: at least one keyword is required\n" +
                            "Usage: bal library search <keywords...>");
                    return;
                }
                outStream.println(new LibrarySearchService().search(args));
            }
            case "get" -> {
                if (args.length == 0) {
                    outStream.println("error: at least one library name is required\n" +
                            "Usage: bal library get <lib-names...>");
                    return;
                }
                outStream.println(new LibraryGetService().get(args));
            }
            default -> {
                outStream.println("error: unknown subcommand '" + subcommand + "'");
                StringBuilder sb = new StringBuilder();
                printLongDesc(sb);
                outStream.println(sb);
            }
        }
    }

    @Override
    public void printLongDesc(StringBuilder sb) {
        sb.append("Search and retrieve Ballerina library information.\n\n");
        sb.append("Usage:\n");
        sb.append("  bal library search <keywords...>   Search libraries by keywords\n");
        sb.append("  bal library get <lib-names...>     Get full library details\n\n");
        sb.append("Examples:\n");
        sb.append("  bal library search http client\n");
        sb.append("  bal library get ballerina/http\n");
        sb.append("  bal library get ballerina/http ballerinax/github\n");
    }

    @Override
    public void printUsage(StringBuilder sb) {
        sb.append("  bal library <search|get> [args]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
