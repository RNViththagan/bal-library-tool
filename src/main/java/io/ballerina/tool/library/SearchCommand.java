package io.ballerina.tool.library;

import picocli.CommandLine;

import java.util.List;

/**
 * `bal library search <keywords...>`
 * Searches libraries by keywords using the SQLite FTS5 index.
 */
@CommandLine.Command(
        name = "search",
        description = "Search Ballerina libraries by keywords"
)
public class SearchCommand implements Runnable {

    @CommandLine.Parameters(
            arity = "1..*",
            description = "Keywords to search for (e.g. http client, kafka, github)"
    )
    private List<String> keywords;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true, usageHelp = true)
    private boolean helpFlag;

    @Override
    public void run() {
        LibrarySearchService service = new LibrarySearchService();
        System.out.println(service.search(keywords.toArray(new String[0])));
    }
}
