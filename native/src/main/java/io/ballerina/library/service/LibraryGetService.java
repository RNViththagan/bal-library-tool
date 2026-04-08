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
import io.ballerina.flowmodelgenerator.core.copilot.CopilotLibraryManager;
import io.ballerina.flowmodelgenerator.core.copilot.model.Library;
import io.ballerina.flowmodelgenerator.core.copilot.model.ModelToJsonConverter;

import java.util.List;

/**
 * Service layer for library get operations.
 *
 * @since 0.1.0
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
