package io.ballerina.tool.library;

import io.ballerina.cli.BLauncherCmd;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Entry point for the `bal library` CLI tool.
 * Dispatches to search and get subcommands manually.
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
                    outStream.println("error: at least one keyword is required\nUsage: bal library search <keywords...>");
                    return;
                }
                outStream.println(new LibrarySearchService().search(args));
            }
            case "get" -> {
                if (args.length == 0) {
                    outStream.println("error: at least one library name is required\nUsage: bal library get <lib-names...>");
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
        // no-op
    }
}
