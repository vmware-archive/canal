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
import io.pivotal.canal.extensions.builder.StageGrapher;
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
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                        .then(stage.deployService(it -> it.name("mongo")),
                                stage.deployService(it -> it.name("rabbit")),
                                stage.deployService(it -> it.name("mysql")))
                        .then(stage.deploy(it -> it.name("mysql")))
                        .then(stage.wait("1+1", it -> it.name("cool off")))
                        .then(stage.rollback("metricsdemo"));
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
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                        .then(
                                stage.destroyService("service1")
                                        .then(stage.deployService(it -> it.name("deploy service 1"))),
                                stage.destroyService("service2")
                                        .then(stage.deployService(it -> it.name("deploy service 2"))),
                                stage.wait("60", it -> it.name("cool off"))
                        )
                        .then(stage.manualJudgment(it -> it.instructions("Approve?")));
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
            public StageGrapher stages() {
                return stage.checkPreconditions(it -> it.preconditions(new ExpressionPrecondition(true)))
                        .then(stage.wait("420"))
                        .then(
                                range(1, 4).mapToObj(i ->
                                        stage.destroyService("serviceName1", it -> it
                                                .name("Destroy Service " + i + " Before")
                                                .stageEnabled(new ExpressionCondition("exp1")))
                                                .then(stage.deployService(it -> it
                                                        .name("Deploy Service " + i)
                                                        .comments("deploy comment")
                                                        .stageEnabled(new ExpressionCondition("exp2"))
                                                        .provider(cfProvider.manifest(
                                                                new ManifestSourceDirect(
                                                                        "serviceType" + i,
                                                                        "serviceName" + i,
                                                                        "servicePlan" + i,
                                                                        Arrays.asList("serviceTags" + i),
                                                                        "serviceParam" + i
                                                                )
                                                        ))
                                                ))
                                ).collect(Collectors.toList())
                        ).then(stage.manualJudgment(it -> it.instructions("Give a thumbs up if you like it.")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getFanOutToMultipleDeployThenDestroys());
    }

}
