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

import io.pivotal.canal.extensions.builder.CloudPipeline;
import io.pivotal.canal.extensions.builder.Defaults;
import io.pivotal.canal.extensions.builder.StageGrapher;
import io.pivotal.canal.json.StageGraphJson;
import io.pivotal.canal.model.*;
import io.pivotal.canal.model.cloudfoundry.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class StageGraphJsonGenerationJavaTest {

    @Test
    void stageGraphConstruction() {
        CloudPipeline<CloudFoundryStageCatalog> pipeline = new CloudPipeline<CloudFoundryStageCatalog>("test",
          new CloudFoundryStageCatalog("creds1")
        ) {

            @Override
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                  .then(
                    cloud.deployService(it -> it
                      .name("Deploy Mongo")
                      .region("dev > dev")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}"))
                    ),
                    cloud.deployService(it -> it
                      .name("Deploy Rabbit")
                      .region("dev > dev")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}"))
                    ),
                    cloud.deployService(it -> it
                      .name("Deploy MySQL")
                      .region("dev > dev")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                    )
                  )
                  .then(cloud.deploy(it -> it.
                    .name("Deploy to Dev")
                    .account("montclair")
                    .region("dev > dev")
                    .application("app1")
                    .artifact(new TriggerArtifact("montclair", ".*"))
                    .manifest(new ArtifactManifest("montclair", ".*"))
                  ))
                  .then(stage.wait("1+1", it -> it.name("cool off")))
                  .then(cloud.rollback("cluster1",it -> it
                    .name("Rollback")
                    .regions(singletonList("dev > dev"))));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithDefaults() {
        CloudPipeline pipeline = new CloudPipeline<CloudFoundryStageCatalog>("test",
          new CloudFoundryStageCatalog("creds1").withDefaults(new Defaults()
              .account("montclair")
              .region("dev > dev")
              .application("app1"))
        ) {

            @Override
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                  .then(
                    cloud.deployService(it -> it
                      .name("Deploy Mongo")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}"))
                    ),
                    cloud.deployService(it -> it
                      .name("Deploy Rabbit")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}"))
                    ),
                    cloud.deployService(it -> it
                      .name("Deploy MySQL")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                    )
                  )
                  .then(cloud.deploy(it -> it
                      .name("Deploy to Dev")
                      .artifact(new TriggerArtifact("montclair", ".*"))
                      .manifest(new ArtifactManifest("montclair", ".*"))
                  ))
                  .then(stage.wait("1+1", it -> it.name("cool off")))
                  .then(cloud.rollback("cluster1", it -> it.name("Rollback")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithNestedDefaults() {
        CloudPipeline pipeline = new CloudPipeline<CloudFoundryStageCatalog>("test",
          new CloudFoundryStageCatalog("creds1").withDefaults(new Defaults()
            .account("montclair")
            .region("dev > dev")
            .application("app1"))
        ) {

            @Override
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                  .then(
                    defaults.region("dev1 > dev").forStages(() ->
                        cloud.deployService(it -> it
                          .name("Deploy Mongo")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}"))
                        )
                    ),
                    defaults.region("dev2 > dev").forStages(() ->
                        cloud.deployService(it -> it
                          .name("Deploy Rabbit")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}"))
                        )
                    ),
                    defaults.region("dev3 > dev").forStages(() ->
                        cloud.deployService(it -> it
                          .name("Deploy MySQL")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                        )
                    )
                  )
                  .then(cloud.deploy(it -> it
                    .name("Deploy to Dev")
                    .artifact(new TriggerArtifact("montclair", ".*"))
                    .manifest(new ArtifactManifest("montclair", ".*"))
                  ))
                  .then(stage.wait("1+1", it -> it.name("cool off")))
                  .then(cloud.rollback("cluster1", it -> it.name("Rollback")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithNestedDefaults());
    }

    @Test
    void stagesWithMultiLevelNestedDefaults() {
        CloudPipeline pipeline = new CloudPipeline<CloudFoundryStageCatalog>("test",
          new CloudFoundryStageCatalog("creds1").withDefaults(new Defaults()
            .account("montclair")
            .region("dev > dev")
            .application("app1"))
        ) {

            @Override
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                  .then(
                    defaults.account("acc1").region("dev0 > dev").forStages(() -> parallel(
                      defaults.region("dev1 > dev").forStages(() ->
                        cloud.deployService(it -> it
                          .name("Deploy Mongo")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}"))
                        )
                      ),
                      cloud.deployService(it -> it
                        .name("Deploy Rabbit")
                        .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}"))
                      ),
                      cloud.deployService(it -> it
                        .name("Deploy MySQL")
                        .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                      ))
                      .then(cloud.deploy(it -> it
                        .name("Deploy to Dev")
                        .artifact(new TriggerArtifact("montclair", ".*"))
                        .manifest(new ArtifactManifest("montclair", ".*"))
                      ))
                    )
                  )
                  .then(stage.wait("1+1", it -> it.name("cool off")))
                  .then(cloud.rollback("cluster1", it -> it.name("Rollback")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithNestedDefaults());
    }

    @Test
    void nestedStageGraph() {
        CloudPipeline pipeline = new CloudPipeline<CloudFoundryStageCatalog>("test",
          new CloudFoundryStageCatalog("creds1")
        ) {
            @Override
            public StageGrapher stages() {
                return stage.wait(Duration.ofMinutes(1))
                  .then(
                    cloud.destroyService("service1")
                      .then(cloud.deployService(it -> it.name("deploy service 1"))),
                    cloud.destroyService("service2")
                      .then(cloud.deployService(it -> it.name("deploy service 2"))),
                    stage.wait("60", it -> it.name("cool off"))
                  )
                  .then(stage.manualJudgment(it -> it.instructions("Approve?")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getNestedStageGraphs());
    }

    @Test
    void stagesDslWithGeneratedFanOutAndFanIn() {
        CloudPipeline pipeline = new CloudPipeline<CloudFoundryStageCatalog>("test",
          new CloudFoundryStageCatalog("creds1")
        ) {
            @Override
            public StageGrapher stages() {
                return stage.checkPreconditions(it -> it.preconditions(new ExpressionPrecondition(true)))
                  .then(stage.wait("420"))
                  .then(
                    range(1, 4).mapToObj(i ->
                      cloud.destroyService("serviceName1", it -> it
                      .name("Destroy Service " + i + " Before")
                      .stageEnabled(new ExpressionCondition("exp1")))
                      .then(cloud.deployService(it -> it
                          .name("Deploy Service " + i)
                          .comments("deploy comment")
                          .stageEnabled(new ExpressionCondition("exp2"))
                          .manifest(
                            new ManifestSourceDirect(
                              "serviceType" + i,
                              "serviceName" + i,
                              "servicePlan" + i,
                              Arrays.asList("serviceTags" + i),
                              "serviceParam" + i
                            )
                          )
                        )
                      )
                    ).collect(Collectors.toList())
                  ).then(stage.manualJudgment(it -> it.instructions("Give a thumbs up if you like it.")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getFanOutToMultipleDeployThenDestroys());
    }

}
