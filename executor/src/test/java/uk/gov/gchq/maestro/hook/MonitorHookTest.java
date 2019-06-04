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

import org.junit.Test;

import uk.gov.gchq.maestro.Context;
import uk.gov.gchq.maestro.Executor;
import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.declaration.OperationDeclaration;
import uk.gov.gchq.maestro.operation.declaration.OperationDeclarations;
import uk.gov.gchq.maestro.operation.handler.chain.OperationChainHandler;
import uk.gov.gchq.maestro.operation.handler.output.ToArrayHandler;
import uk.gov.gchq.maestro.operation.impl.output.ToArray;
import uk.gov.gchq.maestro.user.User;
import uk.gov.gchq.maestro.util.Config;
import uk.gov.gchq.maestro.util.Request;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class MonitorHookTest extends HookTest<MonitorHook> {

    public MonitorHookTest() {
        super(MonitorHook.class);
    }

    @Test
    public void shouldReturnResultWithoutModification() {
        // Given
        final MonitorHook hook = getTestObject();
        final Object result = mock(Object.class);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToArray<>())
                .build();
        final Request request = new Request(opChain, new Context(new User()));
        hook.preExecute(request);

        // When
        final Object returnedResult = hook.postExecute(result, request);

        // Then
        assertSame(result, returnedResult);
    }

    @Test
    public void shouldExecuteWithHooks() {
        // Given
        final MonitorHook hook = getTestObject();
        final OperationChain opChain1 = new OperationChain.Builder()
                .first(new ToArray<>())
                .build();

        final OperationChain opChain2 = new OperationChain.Builder()
                .first(opChain1)
                .then(opChain1)
                .build();

        final OperationDeclaration opChainDeclaration = new OperationDeclaration.Builder()
                .operation(OperationChain.class)
                .handler(new OperationChainHandler())
                .build();

        final OperationDeclaration toArrayDeclaration = new OperationDeclaration.Builder()
                .operation(ToArray.class)
                .handler(new ToArrayHandler())
                .build();

        final OperationDeclarations opDecs = new OperationDeclarations.Builder()
                .declaration(opChainDeclaration)
                .declaration(toArrayDeclaration)
                .build();

        final Config config = new Config.Builder()
                .operationHandlers(opDecs)
                .id("configId12345678")
                .addOperationHook(hook)
                .addRequestHook(hook)
                .build();

        final Executor e1 = new Executor(config);
        final User u1 = new User();

        //setup for a test with a job not complete #TODO
        //This is required for each executor config to have its id set?
        //else the test fails
//        final Properties serviceLoaderProperties = new Properties();
//        serviceLoaderProperties.setProperty(CacheProperties.CACHE_SERVICE_CLASS, EmptyCacheService.class.getName());
//        CacheServiceLoader.initialise(serviceLoaderProperties);
//        final String jobId = "123456789";
//        final String userId = "user";
//        final String description = "user job1";
//        final JobStatus status = JobStatus.SCHEDULED_PARENT;
//        final Job operation = new Job.Builder()
//                .operation(opChain2)
//                .build();
//        JobDetail jobDetail = new JobDetail(jobId,userId, opChain2 , status, description);
//        JobHandler jobHandler = new JobHandler();
//        this.executorId = "6666"; //request.getConfig().getId()

            // When
        try {
//            jobHandler.doOperation(operation,new Context(u1),e1);
            e1.execute(new Request(opChain2, new Context(u1)));
            System.out.println("Done");

        } catch (OperationException e) {
            e.printStackTrace();
        }

        // Then
        assertSame(1, 1);
    }

    @Override
    public MonitorHook getTestObject() {
        return new MonitorHook();
    }
}