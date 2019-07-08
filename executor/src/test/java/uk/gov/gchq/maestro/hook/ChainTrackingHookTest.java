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
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.impl.output.ToArray;
import uk.gov.gchq.maestro.user.User;
import uk.gov.gchq.maestro.util.Config;
import uk.gov.gchq.maestro.util.Request;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class ChainTrackingHookTest extends HookTest<ChainTrackingHook> {

    public ChainTrackingHookTest() {
        super(ChainTrackingHook.class);
    }

    final Config config = new Config.Builder()
            .id("exampleExecutorId1")
            .build();

    @Test
    public void shouldReturnResultWithoutModification() {
        // Given
        final ChainTrackingHook hook = getTestObject();
        final Object result = mock(Object.class);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToArray<>())
                .build();
        final Request request = new Request(opChain, new Context(new User()));
        request.setConfig(config);
        hook.preExecute(request);

        // When
        final Object returnedResult = hook.postExecute(result, request);

        // Then
        assertSame(result, returnedResult);
    }

    @Test
    public void shouldGenerateExecutionTrackingInfo() {
        //Given
        final Request request = new Request(mock(OperationChain.class),new Context(new User()));
        request.setConfig(config);
        final String result = "testResult";

        // When
        ExecutionTrackingInfo preInfo = new ExecutionTrackingInfo(request);
        ExecutionTrackingInfo postInfo = new ExecutionTrackingInfo(result, request);

        //Then
        assertEquals("ExecutionTrackingInfo{operationType='" + preInfo.getOperationType() + "', timeBegin=" + new SimpleDateFormat("MM/dd HH:mm:ss").format(preInfo.getTimeBegin()) + ", jobId='" + preInfo.getJobId() + "', parentJobId='null', executorId='exampleExecutorId1', userId='UNKNOWN'}", preInfo.toString());
        assertEquals("ExecutionTrackingInfo{operationType='" + postInfo.getOperationType() + "', timeEnd=" + new SimpleDateFormat("MM/dd HH:mm:ss").format(postInfo.getTimeEnd()) + ", jobId='" + postInfo.getJobId() + "', parentJobId='null', executorId='exampleExecutorId1', userId='UNKNOWN'}", postInfo.toString());

    }

    @Override
    public ChainTrackingHook getTestObject() {
        return new ChainTrackerFileLogger();
    }

    @Test
    public void shouldExecuteWithHooks() {
        // Given
        final ChainTrackingHook hook = getTestObject();
        final Object result = mock(Object.class);
        final OperationChain opChain1 = new OperationChain.Builder()
                .first(new ToArray<>())
                .build();

        Request request = new Request(opChain1, new Context(new User()));
        request.setConfig(config);

        hook.preExecute(request);
        hook.postExecute(result, request);

        // Then
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("log.txt"));
            String filePreInfo = bufferedReader.readLine();
            String filePostInfo = bufferedReader.readLine();
//            System.out.println(filePreInfo);
//            System.out.println(filePostInfo); #TODO complete test
        } catch (IOException e) {
            e.printStackTrace();

        assertSame(1, 1);
    }
}
}