package io.ballerina.tool.library.service;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.tool.library.manager.LibraryManager;
import io.ballerina.tool.library.model.Library;

import java.util.List;

/**
 * Service layer for library search — wraps LibraryManager.
 * Returns only name and description — search results never contain clients/functions/typeDefs.
 */
public class LibrarySearchService {

    private final LibraryManager manager;

    public LibrarySearchService() {
        this.manager = new LibraryManager();
    }

    public String search(String[] keywords) {
        List<Library> libraries = manager.getLibrariesBySearch(keywords);
        JsonArray array = new JsonArray();
        for (Library lib : libraries) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", lib.getName());
            obj.addProperty("description", lib.getDescription());
            array.add(obj);
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(array);
    }
}
