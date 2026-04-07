package io.ballerina.tool.library;

import com.google.gson.GsonBuilder;
import io.ballerina.flowmodelgenerator.core.copilot.CopilotLibraryManager;
import io.ballerina.flowmodelgenerator.core.copilot.model.Library;
import io.ballerina.flowmodelgenerator.core.copilot.model.ModelToJsonConverter;

import java.util.List;

/**
 * Service layer for library get — wraps CopilotLibraryManager.
 */
public class LibraryGetService {

    private final CopilotLibraryManager manager;

    public LibraryGetService() {
        this.manager = new CopilotLibraryManager();
    }

    public String get(String[] libNames) {
        List<Library> libraries = manager.loadFilteredLibraries(libNames);
        return new GsonBuilder().setPrettyPrinting().create()
                .toJson(ModelToJsonConverter.librariesToJson(libraries));
    }
}
