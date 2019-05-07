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
import io.pivotal.canal.model.cloudfoundry.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class StageGraphJsonGenerationJavaTest {

    @Test
    void stageGraphConstruction() {
        Pipeline pipeline = new Pipeline("test") {
            @Override
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                        .then(stage.deployService(it -> it.name("Deploy Mongo")),
                                stage.deployService(it -> it.name("Deploy Rabbit")),
                                stage.deployService(it -> it.name("Deploy MySQL")))
                        .then(stage.deploy(it -> it
                                .name("Deploy to Dev")
                                .clusters(
                                        new CloudFoundryCluster(
                                                "app1",
                                                "montclair",
                                                "dev > dev",
                                                new TriggerArtifact("montclair", ".*"),
                                                new ArtifactManifest("montclair", ".*")
                                        )
                                )
                        ))
                        .then(stage.wait("1+1", it -> it.name("cool off")))
                        .then(stage.rollback("cluster1", it -> it.name("Rollback")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithDefaults() {
        Pipeline pipeline = new Pipeline("test") {
            @Override
            public StageGrapher stages() {
                return new Defaults()
                  .cloudProvider(new CloudFoundryCloudProvider("creds1"))
                  .account("montclair")
                  .region("dev > dev")
                  .forStages(sc -> sc.wait(Duration.ofMinutes(1))
                        .then(
                          sc.deployService(it -> it.name("Deploy Mongo")),
                          sc.deployService(it -> it.name("Deploy Rabbit")),
                          sc.deployService(it -> it.name("Deploy MySQL"))
                        )
                        .then(
                          sc.deploy(it -> it
                            .name("Deploy to Dev")
                            .clusters(
                              new CloudFoundryCluster(
                                "app1",
                                "montclair",
                                "dev > dev",
                                new TriggerArtifact("montclair", ".*"),
                                new ArtifactManifest("montclair", ".*")
                              )
                            )
                          ))
                        .then(sc.wait("1+1", it -> it.name("cool off")))
                        .then(sc.rollback("cluster1", it -> it.name("Rollback")))
                );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithNestedDefaults() {
        Pipeline pipeline = new Pipeline("test") {
            @Override
            public StageGrapher stages() {
                return new Defaults()
                  .cloudProvider(new CloudFoundryCloudProvider("creds1"))
                  .account("montclair")
                  .region("dev > dev")
                  .forStages((d1, sc) -> sc.wait(Duration.ofMinutes(1))
                    .then(
                      d1.region("dev1 > dev").forStages(sc1 -> sc1.deployService(it -> it.name("Deploy Mongo"))),
                      d1.region("dev2 > dev").forStages(sc1 -> sc1.deployService(it -> it.name("Deploy Rabbit"))),
                      d1.region("dev3 > dev").forStages(sc1 -> sc1.deployService(it -> it.name("Deploy MySQL")))
                    )
                    .then(
                      sc.deploy(it -> it
                        .name("Deploy to Dev")
                        .clusters(
                          new CloudFoundryCluster(
                            "app1",
                            "montclair",
                            "dev > dev",
                            new TriggerArtifact("montclair", ".*"),
                            new ArtifactManifest("montclair", ".*")
                          )
                        )
                      ))
                    .then(sc.wait("1+1", it -> it.name("cool off")))
                    .then(sc.rollback("cluster1", it -> it.name("Rollback")))
                  );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithNestedDefaults());
    }

    @Test
    void nestedStageGraph() {
        Pipeline pipeline = new Pipeline("test") {
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
        Pipeline pipeline = new Pipeline("test") {
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
