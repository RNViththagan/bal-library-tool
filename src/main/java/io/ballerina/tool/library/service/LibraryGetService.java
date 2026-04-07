package io.ballerina.tool.library.service;

import com.google.gson.GsonBuilder;
import io.ballerina.tool.library.manager.LibraryManager;
import io.ballerina.tool.library.model.Library;
import io.ballerina.tool.library.model.ModelToJsonConverter;

import java.util.List;

/**
 * Service layer for library get — wraps LibraryManager.
 */
public class LibraryGetService {

    private final LibraryManager manager;

    public LibraryGetService() {
        this.manager = new LibraryManager();
    }

    public String get(String[] libNames) {
        List<Library> libraries = manager.loadFilteredLibraries(libNames);
        return new GsonBuilder().setPrettyPrinting().create()
                .toJson(ModelToJsonConverter.librariesToJson(libraries));
    }
}
