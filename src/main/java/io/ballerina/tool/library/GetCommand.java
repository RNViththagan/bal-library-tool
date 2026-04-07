package io.ballerina.tool.library;

import picocli.CommandLine;

import java.util.List;

/**
 * `bal library get <lib-names...>`
 * Returns full library details: clients, functions, typeDefs.
 * Library names follow the format: org/package (e.g. ballerina/http, ballerinax/github)
 */
@CommandLine.Command(
        name = "get",
        description = "Get full details of one or more Ballerina libraries"
)
public class GetCommand implements Runnable {

    @CommandLine.Parameters(
            arity = "1..*",
            description = "Library names in org/package format (e.g. ballerina/http ballerinax/github)"
    )
    private List<String> libNames;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true, usageHelp = true)
    private boolean helpFlag;

    @Override
    public void run() {
        LibraryGetService service = new LibraryGetService();
        System.out.println(service.get(libNames.toArray(new String[0])));
    }
}
