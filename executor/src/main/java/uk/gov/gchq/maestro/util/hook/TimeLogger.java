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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.maestro.util.Request;

/**
 * A {@code TimeLogger} is a simple {@link Hook} that sends logs of the
 * time and name of an executed operation to a {@link Logger}.
 */
public class TimeLogger implements Hook {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogger.class);

    /**
     * Logs the time and name of the operation.
     *
     * @param request Request containing the Operation and Context
     */
    @Override
    public void preExecute(final Request request) {
        LOGGER.info("[{}] Started: {}", new java.util.Date(), request.getOperation().getClass());
    }

    @Override
    public <T> T postExecute(final T result, final Request request) {
        LOGGER.info("[{}] Finished: {}", new java.util.Date(), request.getOperation().getClass());
        return null;
    }

    @Override
    public <T> T onFailure(final T result, final Request request, final Exception e) {
        LOGGER.warn("[{}] Failed to Execute: {}/n{}", new java.util.Date(), request.getOperation().getClass(), e);
        return null;
    }
}
