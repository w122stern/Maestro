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

package uk.gov.gchq.maestro.operation.handler.job;


import uk.gov.gchq.maestro.Context;
import uk.gov.gchq.maestro.Executor;
import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.commonutil.iterable.CloseableIterable;
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.handler.OutputOperationHandler;
import uk.gov.gchq.maestro.operation.impl.export.resultcache.GetResultCacheExport;
import uk.gov.gchq.maestro.operation.impl.job.GetJobResults;

/**
 * A {@code GetJobResultsHandler} handles {@link GetJobResults} operations by querying
 * the configured store's job tracker for the required job results.
 */
public class GetJobResultsHandler implements OutputOperationHandler<GetJobResults, CloseableIterable<?>> {
    @Override
    public CloseableIterable<?> doOperation(final GetJobResults operation,
                                            final Context context,
                                            final Executor executor) throws OperationException {
        if (!executor.isSupported(GetResultCacheExport.class)) {
            throw new OperationException("Getting job results is not supported as the " + GetResultCacheExport.class.getSimpleName() + " operation has not been configured for this Maestro instance.");
        }

        // Delegates the operation to the GetResultCacheExport operation handler.
        return executor.execute(new OperationChain<>(new GetResultCacheExport.Builder()
                .jobId(operation.getJobId())
                .key(operation.getKeyOrDefault())
                .build()), context);
    }
}
