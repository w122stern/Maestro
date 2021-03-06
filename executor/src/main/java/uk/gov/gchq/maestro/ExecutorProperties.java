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

package uk.gov.gchq.maestro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.koryphe.util.ReflectionUtil;
import uk.gov.gchq.maestro.commonutil.DebugUtil;
import uk.gov.gchq.maestro.commonutil.StreamUtil;
import uk.gov.gchq.maestro.commonutil.ToStringBuilder;
import uk.gov.gchq.maestro.commonutil.serialisation.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.maestro.commonutil.serialisation.jsonserialisation.JSONSerialiserModules;
import uk.gov.gchq.maestro.operation.declaration.OperationDeclarations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * A {@code ExecutorProperties} contains specific configuration information
 * for the executor, such as database
 * connection strings. It wraps {@link Properties} and lazy loads the all properties from a file when first used.
 * <p>
 * All ExecutorProperties classes must be JSON serialisable.
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "executorPropertiesClassName")
public class ExecutorProperties implements Cloneable {
    public static final String OPERATION_DECLARATIONS = "maestro.executor.operation.declarations";

    public static final String JOB_TRACKER_ENABLED = "maestro.executor.job.tracker.enabled";

    public static final String EXECUTOR_SERVICE_THREAD_COUNT = "maestro.executor.job.executor.threads";
    public static final String EXECUTOR_SERVICE_THREAD_COUNT_DEFAULT = "50";

    public static final String JSON_SERIALISER_CLASS = JSONSerialiser.JSON_SERIALISER_CLASS_KEY;
    public static final String JSON_SERIALISER_MODULES = JSONSerialiser.JSON_SERIALISER_MODULES;
    public static final String STRICT_JSON = JSONSerialiser.STRICT_JSON;

    public static final String ADMIN_AUTH = "maestro.executor.admin.auth";

    /**
     * CSV of extra packages to be included in the reflection scanning.
     */
    public static final String REFLECTION_PACKAGES = "maestro.executor.reflection.packages";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorProperties.class);

    private Properties props = new Properties();

    // Required for loading by reflection.
    public ExecutorProperties() {
    }

    public ExecutorProperties(final Path propFileLocation) {
        if (null != propFileLocation) {
            try (final InputStream accIs = Files.newInputStream(propFileLocation, StandardOpenOption.READ)) {
                props.load(accIs);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ExecutorProperties(final Properties props) {
        setProperties(props);
    }

    public static ExecutorProperties loadExecutorProperties(final String pathStr) {
        final ExecutorProperties executorProperties;
        final Path path = Paths.get(pathStr);
        try {
            if (path.toFile().exists()) {
                executorProperties = loadExecutorProperties(Files.newInputStream(path));
            } else {
                executorProperties = loadExecutorProperties(StreamUtil.openStream(ExecutorProperties.class, pathStr));
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load executor properties " +
                    "file : " + e.getMessage(), e);
        }

        return executorProperties;
    }

    public static ExecutorProperties loadExecutorProperties(final Path executorPropertiesPath) {
        try {
            return loadExecutorProperties(null != executorPropertiesPath ? Files.newInputStream(executorPropertiesPath) : null);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load executor properties file : " + e.getMessage(), e);
        }
    }

    public static ExecutorProperties loadExecutorProperties(final InputStream executorPropertiesStream) {
        if (null == executorPropertiesStream) {
            return new ExecutorProperties();
        }
        final Properties props = new Properties();
        try {
            props.load(executorPropertiesStream);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load executor properties file : " + e.getMessage(), e);
        } finally {
            try {
                executorPropertiesStream.close();
            } catch (final IOException e) {
                LOGGER.error("Failed to close executor properties stream: {}", e.getMessage(), e);
            }
        }
        return loadExecutorProperties(props);
    }

    public static ExecutorProperties loadExecutorProperties(final Properties props) {
        final ExecutorProperties executorProperties;
        try {
            executorProperties =
                    Class.forName(ExecutorProperties.class.getName()).asSubclass(ExecutorProperties.class).newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create executor properties file : " + e.getMessage(), e);
        }

        executorProperties.setProperties(props);
        return executorProperties;
    }

    /**
     * @param key the property key
     * @return a property properties file with the given key.
     */
    public String get(final String key) {
        return props.getProperty(key);
    }

    public boolean containsKey(final Object key) {
        return props.containsKey(key);
    }

    /**
     * Get a parameter from the schema file, or the default value.
     *
     * @param key          the property key
     * @param defaultValue the default value to use if the property doesn't
     *                     exist
     * @return a property properties file with the given key or the default
     * value if the property doesn't exist
     */
    public String get(final String key, final String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /**
     * Set a parameter from the schema file.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(final String key, final String value) {
        if (null == value) {
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
    }

    public void merge(final ExecutorProperties properties) {
        props.putAll(properties.getProperties());
    }

    /**
     * Returns the operation definitions from the file specified in the
     * properties.
     * This is an optional feature, so if the property does not exist then this
     * function
     * will return an empty object.
     *
     * @return The Operation Definitions to load dynamically
     */
    @JsonIgnore
    public OperationDeclarations getOperationDeclarations() {
        OperationDeclarations declarations = null;

        final String declarationsPaths = get(ExecutorProperties.OPERATION_DECLARATIONS);
        if (null != declarationsPaths) {
            declarations = OperationDeclarations.fromPaths(declarationsPaths);
        }

        if (null == declarations) {
            declarations = new OperationDeclarations.Builder().build();
        }

        return declarations;
    }

    public Boolean getJobTrackerEnabled() {
        return Boolean.valueOf(get(JOB_TRACKER_ENABLED, "false"));
    }

    public void setJobTrackerEnabled(final Boolean jobTrackerEnabled) {
        set(JOB_TRACKER_ENABLED, jobTrackerEnabled.toString());
    }

    public String getOperationDeclarationPaths() {
        return get(OPERATION_DECLARATIONS);
    }

    public void setOperationDeclarationPaths(final String paths) {
        set(OPERATION_DECLARATIONS, paths);
    }

    public String getReflectionPackages() {
        return get(REFLECTION_PACKAGES);
    }

    public void setReflectionPackages(final String packages) {
        set(REFLECTION_PACKAGES, packages);
        ReflectionUtil.addReflectionPackages(packages);
    }

    public Integer getJobExecutorThreadCount() {
        return Integer.parseInt(get(EXECUTOR_SERVICE_THREAD_COUNT, EXECUTOR_SERVICE_THREAD_COUNT_DEFAULT));
    }

    public void addOperationDeclarationPaths(final String... newPaths) {
        final String newPathsCsv = StringUtils.join(newPaths, ",");
        String combinedPaths = getOperationDeclarationPaths();
        if (null == combinedPaths) {
            combinedPaths = newPathsCsv;
        } else {
            combinedPaths = combinedPaths + "," + newPathsCsv;
        }
        setOperationDeclarationPaths(combinedPaths);
    }

    public String getJsonSerialiserClass() {
        return get(JSON_SERIALISER_CLASS);
    }

    @JsonIgnore
    public void setJsonSerialiserClass(final Class<? extends JSONSerialiser> jsonSerialiserClass) {
        setJsonSerialiserClass(jsonSerialiserClass.getName());
    }

    public void setJsonSerialiserClass(final String jsonSerialiserClass) {
        set(JSON_SERIALISER_CLASS, jsonSerialiserClass);
    }

    public String getJsonSerialiserModules() {
        return get(JSON_SERIALISER_MODULES, "");
    }

    @JsonIgnore
    public void setJsonSerialiserModules(final Set<Class<? extends JSONSerialiserModules>> modules) {
        final Set<String> moduleNames = new HashSet<>(modules.size());
        for (final Class module : modules) {
            moduleNames.add(module.getName());
        }
        setJsonSerialiserModules(StringUtils.join(moduleNames, ","));
    }

    public void setJsonSerialiserModules(final String modules) {
        set(JSON_SERIALISER_MODULES, modules);
    }

    public Boolean getStrictJson() {
        final String strictJson = get(STRICT_JSON);
        return null == strictJson ? null : Boolean.parseBoolean(strictJson);
    }

    public void setStrictJson(final Boolean strictJson) {
        set(STRICT_JSON, null == strictJson ? null : Boolean.toString(strictJson));
    }

    public String getAdminAuth() {
        return get(ADMIN_AUTH, "");
    }

    public void setAdminAuth(final String adminAuth) {
        set(ADMIN_AUTH, adminAuth);
    }

    public Properties getProperties() {
        return props;
    }

    public void setProperties(final Properties properties) {
        if (null == properties) {
            this.props = new Properties();
        } else {
            this.props = properties;
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @SuppressFBWarnings(value = "CN_IDIOM_NO_SUPER_CALL", justification = "Only inherits from Object")
    @Override
    public ExecutorProperties clone() {
        return ExecutorProperties.loadExecutorProperties((Properties) getProperties().clone());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final ExecutorProperties properties = (ExecutorProperties) obj;

        return new EqualsBuilder()
                .append(props, properties.props)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 7)
                .append(props)
                .toHashCode();
    }

    @Override
    public String toString() {
        if (DebugUtil.checkDebugMode()) {
            return new ToStringBuilder(this)
                    .append("properties", getProperties())
                    .toString();
        }

        // If we are not in debug mode then don't return the property values in case we leak sensitive properties.
        return super.toString();
    }
}
