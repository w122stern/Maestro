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

import com.google.common.collect.Sets;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import uk.gov.gchq.maestro.Context;
import uk.gov.gchq.maestro.commonutil.exception.UnauthorisedException;
import uk.gov.gchq.maestro.helper.TestOperationsImpl;
import uk.gov.gchq.maestro.named.operation.AddNamedOperation;
import uk.gov.gchq.maestro.named.operation.ParameterDetail;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.impl.DiscardOutput;
import uk.gov.gchq.maestro.operation.impl.output.ToArray;
import uk.gov.gchq.maestro.operation.impl.output.ToList;
import uk.gov.gchq.maestro.operation.impl.output.ToSet;
import uk.gov.gchq.maestro.operation.impl.output.ToSingletonList;
import uk.gov.gchq.maestro.user.User;
import uk.gov.gchq.maestro.util.Request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


public class OperationAuthoriserTest extends HookTest<OperationAuthoriser> {
    private static final String OP_AUTHS_PATH = "/opAuthoriser.json";
    public static final String USER = "User";

    public OperationAuthoriserTest() {
        super(OperationAuthoriser.class);
    }

    @Test
    public void shouldAcceptOperationChainWhenUserHasAllOpAuths() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToList())
                .then(new ToSingletonList())
                .then(new ToArray())
                .then(new DiscardOutput())
                .then(new TestOperationsImpl(Collections.singletonList(new ToSet())))
                .build();
        final User user = new User.Builder()
                .opAuths("AdminUser", "SuperUser", "ReadUser", "User",
                        "WriteUser")
                .build();

        // When
        hook.preExecute(new Request(opChain, new Context(user)));

        // Then - no exceptions
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAllOpAuthsForNestedOperations() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToList())
                .then(new ToSingletonList())
                .then(new ToArray())
                .then(new DiscardOutput())
                .then(new TestOperationsImpl(Collections.singletonList(new ToSet())))
                .build();
        final User user = new User.Builder()
                .opAuths("SuperUser", "ReadUser", "User")
                .build();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldAcceptOperationChainWhenUserHasAllOpAuthsForAddNamedOperation() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final AddNamedOperation addNamedOperation = new AddNamedOperation.Builder()
                .operationChain("{\"operations\":[{\"class\": \"uk.gov.gchq.maestro.operation.impl.output.ToArray\", " +
                        "\"options\": {\"optionKey\": \"${testParameter}\"}}]}")
                .description("Test Named Operation")
                .name("Test")
                .overwrite(false)
                .parameter("testParameter", new ParameterDetail.Builder()
                        .description("the seed")
                        .defaultValue("seed1")
                        .valueClass(String.class)
                        .required(false)
                        .build())
                .score(2)
                .build();
        final OperationChain opChain = new OperationChain.Builder()
                .first(addNamedOperation)
                .build();
        final User user = new User.Builder()
                .opAuths("SuperUser", "ReadUser", "User")
                .build();

        // When
        hook.preExecute(new Request(opChain, new Context(user)));

        // Then - no exceptions
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAllOpAuthsForAddNamedOperation() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final AddNamedOperation addNamedOperation = new AddNamedOperation.Builder()
                .operationChain("{\"operations\":[{\"class\": \"uk.gov.gchq.maestro.operation.impl.output.ToSet\", " +
                        "\"options\": {\"optionKey\": \"${testParameter}\"}}]}")
                .description("Test Named Operation")
                .name("Test")
                .overwrite(false)
                .parameter("testParameter", new ParameterDetail.Builder()
                        .description("the seed")
                        .defaultValue("seed1")
                        .valueClass(String.class)
                        .required(false)
                        .build())
                .score(2)
                .build();
        final OperationChain opChain = new OperationChain.Builder()
                .first(addNamedOperation)
                .build();
        final User user = new User.Builder()
                .opAuths("SuperUser", "ReadUser", "User")
                .build();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveSuperAuthForAddNamedOperation() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final AddNamedOperation addNamedOperation = new AddNamedOperation.Builder()
                .operationChain("{\"operations\":[{\"class\": \"uk.gov.gchq.maestro.operation.impl.output.ToSet\", " +
                        "\"options\": {\"optionKey\": \"${testParameter}\"}}]}")
                .description("Test Named Operation")
                .name("Test")
                .overwrite(false)
                .parameter("testParameter", new ParameterDetail.Builder()
                        .description("the seed")
                        .defaultValue("seed1")
                        .valueClass(String.class)
                        .required(false)
                        .build())
                .score(2)
                .build();
        final OperationChain opChain = new OperationChain.Builder()
                .first(addNamedOperation)
                .build();
        final User user = new User.Builder()
                .opAuths("ReadUser", "User")
                .build();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveWriteAuthForAddNamedOperation() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final AddNamedOperation addNamedOperation = new AddNamedOperation.Builder()
                .operationChain("{\"operations\":[{\"class\": \"uk.gov.gchq.maestro.operation.impl.output.ToSet\", \"options\": {\"optionKey\": \"${testParameter}\"}}]}")
                .description("Test Named Operation")
                .name("Test")
                .overwrite(false)
                .parameter("testParameter", new ParameterDetail.Builder()
                        .description("the seed")
                        .defaultValue("seed1")
                        .valueClass(String.class)
                        .required(false)
                        .build())
                .score(2)
                .build();
        final OperationChain opChain = new OperationChain.Builder()
                .first(addNamedOperation)
                .build();
        final User user = new User.Builder()
                .opAuths("User")
                .build();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAllOpAuthsForAllOperations() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToSet<>())  // Requires SuperUser
                .build();

        final User user = new User.Builder()
                .opAuths("WriteUser", "ReadUser", "User")
                .build();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAnyOpAuths() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToArray())
                .then(new ToSet())
                .build();

        final User user = new User();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldRejectOperationChainWhenUserDoesntHaveAllowedAuth() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToList<>())
                .then(new ToSet<>())
                .build();

        final User user = new User.Builder()
                .opAuths("unknownAuth")
                .build();

        // When/Then
        try {
            hook.preExecute(new Request(opChain, new Context(user)));
            fail("Exception expected");
        } catch (final UnauthorisedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void shouldReturnAllOpAuths() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);

        // When
        final Set<String> allOpAuths = hook.getAllAuths();

        // Then
        assertThat(allOpAuths,
                IsCollectionContaining.hasItems("User", "ReadUser", "WriteUser", "SuperUser", "AdminUser"));
    }

    @Test
    public void shouldReturnResultWithoutModification() {
        // Given
        final OperationAuthoriser hook = fromJson(OP_AUTHS_PATH);
        final Object result = mock(Object.class);
        final OperationChain opChain = new OperationChain.Builder()
                .first(new ToList<>())
                .build();
        final User user = new User.Builder()
                .opAuths("NoScore")
                .build();

        // When
        final Object returnedResult = hook.postExecute(result,
                new Request(opChain, new Context(user)));

        // Then
        assertSame(result, returnedResult);
    }

    @Test
    public void shouldSetAndGetAuths() {
        // Given
        final OperationAuthoriser hook = new OperationAuthoriser();
        final Map<Class<?>, Set<String>> auths = new HashMap<>();
        auths.put(Operation.class, Sets.newHashSet("auth1"));
        auths.put(ToArray.class, Sets.newHashSet("auth2"));
        auths.put(ToSet.class, Sets.newHashSet("auth3", "auth4"));

        // When
        hook.setAuths(auths);
        final Map<Class<?>, Set<String>> result = hook.getAuths();

        // Then
        assertEquals(auths, result);
        assertEquals(
                Sets.newHashSet("auth1", "auth2", "auth3", "auth4"),
                hook.getAllAuths()
        );
    }

    @Test
    public void shouldSetAndGetAuthsAsStrings() throws ClassNotFoundException {
        // Given
        final OperationAuthoriser hook = new OperationAuthoriser();
        final Map<String, Set<String>> auths = new HashMap<>();
        auths.put(Operation.class.getName(), Sets.newHashSet("auth1"));
        auths.put(ToSet.class.getName(), Sets.newHashSet("auth2"));
        auths.put(ToArray.class.getName(), Sets.newHashSet("auth3", "auth4"));

        // When
        hook.setAuthsFromStrings(auths);
        final Map<String, Set<String>> result = hook.getAuthsAsStrings();

        // Then
        assertEquals(auths, result);
        assertEquals(
                Sets.newHashSet("auth1", "auth2", "auth3", "auth4"),
                hook.getAllAuths()
        );
    }

    @Test
    public void shouldHandleNestedOperationChain() {
    }

    @Override
    protected OperationAuthoriser getTestObject() {
        return fromJson(OP_AUTHS_PATH);
    }
}