/*
 *  Copyright 2016-2024 Qameta Software Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure;

import io.qameta.allure.allure1.Allure1Plugin;
import io.qameta.allure.allure2.Allure2Plugin;
import io.qameta.allure.category.CategoriesPlugin;
import io.qameta.allure.category.CategoriesTrendPlugin;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.context.ReportInfoContext;
import io.qameta.allure.core.AttachmentsPlugin;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.MarkdownDescriptionsPlugin;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.core.TestsResultsPlugin;
import io.qameta.allure.duration.DurationPlugin;
import io.qameta.allure.duration.DurationTrendPlugin;
import io.qameta.allure.environment.Allure1EnvironmentPlugin;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.ga.GaPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.history.HistoryTrendPlugin;
import io.qameta.allure.idea.IdeaLinksPlugin;
import io.qameta.allure.influxdb.InfluxDbExportPlugin;
import io.qameta.allure.launch.LaunchPlugin;
import io.qameta.allure.mail.MailPlugin;
import io.qameta.allure.owner.OwnerPlugin;
import io.qameta.allure.prometheus.PrometheusExportPlugin;
import io.qameta.allure.retry.RetryPlugin;
import io.qameta.allure.retry.RetryTrendPlugin;
import io.qameta.allure.severity.SeverityPlugin;
import io.qameta.allure.status.StatusChartPlugin;
import io.qameta.allure.suites.SuitesPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.tags.TagsPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Builder for {@link Configuration}.
 *
 * @see Configuration
 * @since 2.0
 */
@SuppressWarnings({
        "PMD.ExcessiveImports",
        "ClassDataAbstractionCoupling",
        "ClassFanOutComplexity"
})
public class ConfigurationBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationBuilder.class);

    private static final String ALLURE_VERSION_TXT_PATH = "/allure-version.txt";

    private final List<Extension> extensions = new ArrayList<>();

    private final List<Plugin> plugins = new ArrayList<>();

    private String reportName;

    public ConfigurationBuilder useDefault() {
        final String allureVersion = getVersionFromFile()
                .orElse(getVersionFromManifest().orElse("Undefined"));

        fromExtensions(Arrays.asList(
                new ReportInfoContext(allureVersion),
                new JacksonContext(),
                new MarkdownContext(),
                new FreemarkerContext(),
                new RandomUidContext(),
                new MarkdownDescriptionsPlugin(),
                new TagsPlugin(),
                new RetryPlugin(),
                new RetryTrendPlugin(),
                new SeverityPlugin(),
                new OwnerPlugin(),
                new IdeaLinksPlugin(),
                new HistoryPlugin(),
                new HistoryTrendPlugin(),
                new CategoriesPlugin(),
                new CategoriesTrendPlugin(),
                new DurationPlugin(),
                new DurationTrendPlugin(),
                new StatusChartPlugin(),
                new TimelinePlugin(),
                new SuitesPlugin(),
                new TestsResultsPlugin(),
                new AttachmentsPlugin(),
                new MailPlugin(),
                new InfluxDbExportPlugin(),
                new PrometheusExportPlugin(),
                new SummaryPlugin(),
                new ExecutorPlugin(),
                new LaunchPlugin(),
                new Allure1Plugin(),
                new Allure1EnvironmentPlugin(),
                new Allure2Plugin(),
                new GaPlugin()
        ));
        return this;
    }

    public ConfigurationBuilder fromExtensions(final List<Extension> extensions) {
        this.extensions.addAll(extensions);
        return this;
    }

    public ConfigurationBuilder fromPlugins(final List<Plugin> plugins) {
        this.plugins.addAll(plugins);
        plugins.stream()
                .map(Plugin::getExtensions)
                .forEach(this::fromExtensions);
        return this;
    }

    public ConfigurationBuilder withReportName(final String reportName) {
        this.reportName = reportName;
        return this;
    }

    public Configuration build() {
        return new DefaultConfiguration(
                this.reportName,
                Collections.unmodifiableList(extensions),
                Collections.unmodifiableList(plugins)
        );
    }

    private static Optional<String> getVersionFromFile() {
        try {
            return Optional.of(IOUtils.resourceToString(ALLURE_VERSION_TXT_PATH, StandardCharsets.UTF_8))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .filter(v -> !"#project.version#".equals(v));
        } catch (IOException e) {
            LOGGER.debug("Could not read {} resource", ALLURE_VERSION_TXT_PATH, e);
            return Optional.empty();
        }
    }

    private static Optional<String> getVersionFromManifest() {
        return Optional.of(ConfigurationBuilder.class)
                .map(Class::getPackage)
                .map(Package::getImplementationVersion);
    }
}
