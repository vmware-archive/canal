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

import io.pivotal.canal.extensions.builder.Pipeline;
import io.pivotal.canal.extensions.builder.StageGrapher;
import io.pivotal.canal.json.StageGraphJson;
import io.pivotal.canal.model.*;
import io.pivotal.canal.model.cloudfoundry.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static io.pivotal.canal.model.extensions.StageGraphExtensions.*;

class StageGraphJsonGenerationJavaTest {

    @Test
    void stageGraphConstruction() {
        Pipeline pipeline = new Pipeline("test") {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", this);

            @Override
            public StageGrapher stages() {
                return stageGraphFor(stage.wait(Duration.ofMinutes(1)))
                  .then(
                    cf.deployService()
                      .name("Deploy Mongo")
                      .region("dev > dev")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}")),
                    cf.deployService()
                      .name("Deploy Rabbit")
                      .region("dev > dev")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}")),
                    cf.deployService()
                      .name("Deploy MySQL")
                      .region("dev > dev")
                      .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                  )
                  .then(cf.deploy()
                    .name("Deploy to Dev")
                    .account("montclair")
                    .region("dev > dev")
                    .application("app1")
                    .artifact(new TriggerArtifact("montclair", ".*"))
                    .manifest(new ArtifactManifest("montclair", ".*"))
                  )
                  .then(stage.wait("1+1").name("cool off"))
                  .then(cf.rollback("cluster1")
                    .name("Rollback")
                    .regions(singletonList("dev > dev")));
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithDefaults() {
        Pipeline pipeline = new Pipeline("test") {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", this);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                    stage.wait(Duration.ofMinutes(1)).toGraph()
                      .then(
                        cf.deployService()
                          .name("Deploy Mongo")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}")),
                        cf.deployService()
                          .name("Deploy Rabbit")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}")),
                        cf.deployService()
                          .name("Deploy MySQL")
                          .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                      )
                      .then(cf.deploy()
                        .name("Deploy to Dev")
                        .artifact(new TriggerArtifact("montclair", ".*"))
                        .manifest(new ArtifactManifest("montclair", ".*"))
                      )
                      .then(stage.wait("1+1").name("cool off"))
                      .then(cf.rollback("cluster1").name("Rollback"))
                );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithNestedDefaults() {
        Pipeline pipeline = new Pipeline("test") {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", this);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                    stage.wait(Duration.ofMinutes(1)).toGraph()
                      .then(
                        defaults.region("dev1 > dev").forStages(() ->
                          cf.deployService()
                            .name("Deploy Mongo")
                            .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}"))
                        ),
                        defaults.region("dev2 > dev").forStages(() ->
                          cf.deployService()
                            .name("Deploy Rabbit")
                            .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}"))
                        ),
                        defaults.region("dev3 > dev").forStages(() ->
                          cf.deployService()
                            .name("Deploy MySQL")
                            .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                        )
                      )
                      .then(cf.deploy()
                        .name("Deploy to Dev")
                        .artifact(new TriggerArtifact("montclair", ".*"))
                        .manifest(new ArtifactManifest("montclair", ".*"))
                      )
                      .then(stage.wait("1+1").name("cool off"))
                      .then(cf.rollback("cluster1").name("Rollback"))
                );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithNestedDefaults());
    }

    @Test
    void stagesWithMultiLevelNestedDefaults() {
        Pipeline pipeline = new Pipeline("test") {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", this);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                      stage.wait(Duration.ofMinutes(1)).toGraph()
                        .then(
                          defaults.region("dev0 > dev").forStages(() -> new StageGrapher().then(
                            defaults.region("dev1 > dev").forStages(() ->
                              cf.deployService()
                                .name("Deploy Mongo")
                                .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mongo}"))
                            ),
                            cf.deployService()
                              .name("Deploy Rabbit")
                              .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_rabbit}")),
                            cf.deployService()
                              .name("Deploy MySQL")
                              .manifest(new ManifestSourceArtifact("public", "$\\{service_manifest_mysql}"))
                            )
                              .then(cf.deploy().name("Deploy to Dev")
                                .artifact(new TriggerArtifact("montclair", ".*"))
                                .manifest(new ArtifactManifest("montclair", ".*"))
                              )
                          )
                        .then(stage.wait("1+1").name("cool off"))
                        .then(cf.rollback("cluster1").name("Rollback"))
                        )
                  );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithMultiLevelNestedDefaults());
    }

    @Test
    void nestedStageGraph() {
        Pipeline pipeline = new Pipeline("test") {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", this);

            @Override
            public StageGrapher stages() {
                return defaults.region("dev > dev")
                  .forStages(() -> stage.wait(Duration.ofMinutes(1)).toGraph()
                    .then(
                      cf.destroyService("service1").toGraph()
                        .then(stage.wait("1").name("deploy service 1")),
                      cf.destroyService("service2").toGraph()
                        .then(stage.wait("2").name("deploy service 2")),
                      stage.wait("60").name("cool off")
                    )
                    .then(stage.manualJudgment().instructions("Approve?"))
                  );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getNestedStageGraphs());
    }

    @Test
    void stagesDslWithGeneratedFanOutAndFanIn() {
        Pipeline pipeline = new Pipeline("test") {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", this);

            @Override
            public StageGrapher stages() {
                return defaults.region("dev > dev").forStages(() -> stage.checkPreconditions().preconditions(new ExpressionPrecondition(true)).toGraph()
                  .then(stage.wait("420"))
                  .then(
                    range(1, 4).mapToObj(i ->
                      cf.destroyService("serviceName" + i)
                        .name("Destroy Service " + i + " Before")
                        .stageEnabled(new ExpressionCondition("exp1")).toGraph()
                        .then(cf.deployService()
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
                    ).collect(Collectors.toList())
                  ).then(stage.manualJudgment().instructions("Give a thumbs up if you like it."))
                );
            }
        };

        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getFanOutToMultipleDeployThenDestroys());
    }

}
