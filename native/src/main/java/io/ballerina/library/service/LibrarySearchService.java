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

package io.ballerina.library.service;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.flowmodelgenerator.core.copilot.CopilotLibraryManager;
import io.ballerina.flowmodelgenerator.core.copilot.model.Library;

import java.util.List;

/**
 * Service layer for library search operations.
 *
 * @since 0.1.0
 */
public class LibrarySearchService {

    private final CopilotLibraryManager manager;

    public LibrarySearchService() {
        this.manager = new CopilotLibraryManager();
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
