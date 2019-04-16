/*
 * Copyright 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.canal.extensions.fluentstages;

import io.pivotal.canal.extensions.builder.Defaults;
import io.pivotal.canal.extensions.builder.Pipeline;
import io.pivotal.canal.json.StageGraphJson;
import io.pivotal.canal.model.*;
import io.pivotal.canal.model.cloudfoundry.CloudFoundryCloudProvider;
import io.pivotal.canal.model.cloudfoundry.ManifestSourceDirect;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class StageGraphJsonGenerationJavaTest {

    @Test
    void stagesWithDefaults() {
        Pipeline pipeline = new Pipeline("test",
                new Defaults()
                        .cloudProvider(new CloudFoundryCloudProvider("creds1"))
                        .region("dev > dev")
        ) {
            @Override
            public StageGraph stages() {
                return stageGraph().then(
                        stage().wait(Duration.ofMinutes(1))
                ).then(
                        stage("mongo").deployService(),
                        stage("rabbit").deployService(),
                        stage("mysql").deployService()
                ).then(
                        stage("deploy to dev").deploy()
                ).then(
                        stage("cool off").wait("1+1")
                ).then(
                        stage().rollback("metricsdemo")
                ).graph();
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void nestedStageGraph() {
        Pipeline pipeline = new Pipeline("test",
                new Defaults()
                        .cloudProvider(new CloudFoundryCloudProvider("creds1"))
                        .region("dev > dev")
        ) {
            @Override
            public StageGraph stages() {
                return stageGraph().then(
                        stage().wait(Duration.ofMinutes(1))
                ).then(
                        stageGraph(
                                stage().destroyService("service1")
                        ).then(
                                stage("deploy service 1").deployService()
                        ),
                        stageGraph(
                                stage().destroyService("service2")
                        ).then(
                                stage("destroy service 2").deployService()
                        ),
                        stage("cool off").wait(Duration.ofMinutes(1))
                ).then(
                        stage().manualJudgment()
                                .instructions("Approve?")
                ).graph();
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getNestedStageGraphs());
    }

    @Test
    void stagesDslWithGeneratedFanOutAndFanIn() {
        CloudFoundryCloudProvider cfProvider = new CloudFoundryCloudProvider("creds1");
        Pipeline pipeline = new Pipeline("test",
                new Defaults()
                        .cloudProvider(cfProvider)
                        .region("dev > dev")
        ) {
            @Override
            public StageGraph stages() {
                return stageGraph().then(
                        stage("Check Preconditions").checkPreconditions(new ExpressionPrecondition(true))
                ).then(
                        stage().wait("420")
                ).then(
                        range(1, 4).mapToObj(it -> stageGraph(
                                    stage(stageConfig()
                                                    .name("Destroy Service " + it + " Before")
                                                    .stageEnabled(new ExpressionCondition("exp1"))
                                    ).destroyService("serviceName1")
                                ).then(
                                    stage(stageConfig()
                                                    .name("Deploy Service " + it)
                                                    .comments("deploy comment")
                                                    .stageEnabled(new ExpressionCondition("exp2"))
                                    ).deployService()
                                            .provider(cfProvider.manifest(
                                                    new ManifestSourceDirect(
                                                        "serviceType" + it,
                                                        "serviceName" + it,
                                                        "servicePlan" + it,
                                                        Arrays.asList("serviceTags" + it),
                                                        "serviceParam" + it
                                                )
                                            ))
                                )
                        ).collect(Collectors.toList())
                ).then(
                        stage().manualJudgment().instructions("Give a thumbs up if you like it.")
                ).graph();
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getFanOutToMultipleDeployThenDestroys());
    }

}
