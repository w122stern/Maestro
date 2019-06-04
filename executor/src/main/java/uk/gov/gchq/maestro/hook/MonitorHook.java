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

import java.util.Date;

/**
 * A {@code MonitorHook} is a {@link Hook} that sends logs of the
 * time and name of an executed operation to a {@link Logger}.
 */
@JsonPropertyOrder(alphabetic = true)
public class MonitorHook implements Hook {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorHook.class);

    public MonitorHook() {
//        ExecutionTrackingInfo jobInfo = new ExecutionTrackingInfo(request);
        System.out.println("Monitor Hook Created. ------");
    }

    /**
     * Instantiates an {@link ExecutionTrackingInfo} Object containing
     * various fields extracted from a request.
     * {@link ExecutionTrackingInfo} is referenced in the
     * {@link uk.gov.gchq.maestro.Context} so the start and end time may be linked.
     *
     * @param request Request containing the Operation and Context
     */
    @Override
    public void preExecute(final Request request) {
        ExecutionTrackingInfo preInfo = new ExecutionTrackingInfo(request);
        request.getContext().setExecutionTrackingInfo(preInfo);
//        LOGGER.info(preInfo.toString());
        System.out.println(preInfo.toString());
//        reportInfoToStore(preInfo)
    }

    @Override
    public <T> T postExecute(final T result, final Request request) {
        ExecutionTrackingInfo postInfo = request.getContext().getExecutionTrackingInfo();
        postInfo.setTimeEnd(new Date());
//        LOGGER.info(postInfo.toString());
        System.out.println(postInfo.toString());
//        reportInfoToStore(postInfo);
        return result;
    }

    @Override
    public <T> T onFailure(final T result, final Request request, final Exception e) {
        LOGGER.warn("[{}] Failed to Execute: {}  - {}", new java.util.Date(), request.getOperation().getClass().getSimpleName(), e.getMessage());
        System.out.println(request.getContext().getOriginalOperation());
        return null;
    }
}
