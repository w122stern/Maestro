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
package uk.gov.gchq.maestro.hook;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.maestro.util.Request;

/**
 * A {@code ChainTrackingHook} is a {@link Hook} that sends logs of the
 * time and name of an executed operation to a {@link Logger}.
 */
@JsonPropertyOrder(alphabetic = true)
public abstract class ChainTrackingHook implements Hook {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainTrackingHook.class);

    public ChainTrackingHook() {
    }

    /**
     * Instantiates an {@link ExecutionTrackingInfo} Object containing
     * various fields extracted from a request.
     *
     * @param request Request containing the Operation and Context
     */
    @Override
    public void preExecute(final Request request) {
        ExecutionTrackingInfo preInfo = new ExecutionTrackingInfo(request);
        track(preInfo);
    }

    @Override
    public <T> T postExecute(final T result, final Request request) {
        ExecutionTrackingInfo postInfo = new ExecutionTrackingInfo(result, request);
        track(postInfo);
        return result;
    }

    @Override
    public <T> T onFailure(final T result, final Request request, final Exception e) {
        LOGGER.warn("[{}] Failed to Execute: {}  - {}", new java.util.Date(), request.getOperation().getClass().getSimpleName(), e.getMessage());
        return null;
    }

    public abstract void track(ExecutionTrackingInfo info);
}
