package io.ballerina.tool.library;

import com.google.gson.GsonBuilder;
import io.ballerina.flowmodelgenerator.core.copilot.CopilotLibraryManager;
import io.ballerina.flowmodelgenerator.core.copilot.model.Library;
import io.ballerina.flowmodelgenerator.core.copilot.model.ModelToJsonConverter;

import java.util.List;

/**
 * Service layer for library search — wraps CopilotLibraryManager.
 */
public class LibrarySearchService {

    private final CopilotLibraryManager manager;

    public LibrarySearchService() {
        this.manager = new CopilotLibraryManager();
    }

    public String search(String[] keywords) {
        List<Library> libraries = manager.getLibrariesBySearch(keywords);
        return new GsonBuilder().setPrettyPrinting().create()
                .toJson(ModelToJsonConverter.librariesToJson(libraries));
    }
}
