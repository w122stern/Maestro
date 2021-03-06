/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.maestro.util.hook;

import uk.gov.gchq.maestro.util.Request;

public class HookPath implements Hook {
    private static final String ERROR_MSG =
            "This " + HookPath.class.getSimpleName() + " hook should not " +
                    "be executed";
    private String path;

    @Override
    public void preExecute(final Request request) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public <T> T postExecute(final T result, final Request request) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public <T> T onFailure(final T result, final Request request,
                           final Exception e) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
