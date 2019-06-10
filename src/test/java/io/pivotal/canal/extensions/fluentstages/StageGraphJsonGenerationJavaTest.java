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

import io.pivotal.canal.extensions.builder.*;
import io.pivotal.canal.json.StageGraphJson;
import io.pivotal.canal.model.*;
import io.pivotal.canal.model.cloudfoundry.*;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class StageGraphJsonGenerationJavaTest {

    public interface TestArtifacts {
        Artifacts.ExpectedArtifact getAppJar();
    }

    public static class IncrementingIdArtifacts extends Artifacts {
        private int id = 0;
        protected String generateId() {
            return Integer.toString(id++);
        }
    }

    public static class TestArtifactsImpl extends IncrementingIdArtifacts implements TestArtifacts {
        @Getter public ExpectedArtifact appJar = artifactReference(
          artifact.maven(a -> a
            .artifactAccount("spring-artifactory-maven")
            .reference("io.pivotal.spinnaker:multifoundationmetrics:.*"))
        ).defaultArtifact(
          artifact.maven(a -> a
            .artifactAccount("spring-artifactory-maven")
            .reference("io.pivotal.spinnaker:multifoundationmetrics:latest.release"))
        );
    }

    @Test
    void stageGraphConstruction() {
        StageGraph stageGraph = new StageGraph<TestArtifacts>() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            protected StageGrapher stages() {
                return super.stages().then(stage.wait(Duration.ofMinutes(1)))
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
                    .artifact(artifacts.getAppJar())
                    .manifest(new ArtifactManifest("montclair", ".*"))
                  )
                  .then(stage.wait("1+1").name("cool off"))
                  .then(cf.rollback("cluster1")
                    .name("Rollback")
                    .regions(singletonList("dev > dev")));
            }
        };

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph, new TestArtifactsImpl());
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithDefaults() {
        StageGraph stageGraph = new StageGraph<TestArtifacts>() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                    super.stages().then(stage.wait(Duration.ofMinutes(1)))
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
                        .artifact(artifacts.getAppJar())
                        .manifest(new ArtifactManifest("montclair", ".*"))
                      )
                      .then(stage.wait("1+1").name("cool off"))
                      .then(cf.rollback("cluster1").name("Rollback"))
                );
            }
        };

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph, new TestArtifactsImpl());
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanIn());
    }

    @Test
    void stagesWithNestedDefaults() {
        StageGraph stageGraph = new StageGraph<TestArtifacts>() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                    super.stages().then(stage.wait(Duration.ofMinutes(1)))
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
                        .artifact(artifacts.getAppJar())
                        .manifest(new ArtifactManifest("montclair", ".*"))
                      )
                      .then(stage.wait("1+1").name("cool off"))
                      .then(cf.rollback("cluster1").name("Rollback"))
                );
            }
        };

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph, new TestArtifactsImpl());
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithNestedDefaults());
    }

    @Test
    void stagesWithMultiLevelNestedDefaults() {
        StageGraph stageGraph = new StageGraph<TestArtifacts>() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                      super.stages().then(stage.wait(Duration.ofMinutes(1)))
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
                                .artifact(artifacts.getAppJar())
                                .manifest(new ArtifactManifest("montclair", ".*"))
                              )
                          )
                        .then(stage.wait("1+1").name("cool off"))
                        .then(cf.rollback("cluster1").name("Rollback"))
                        )
                  );
            }
        };

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph, new TestArtifactsImpl());
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithMultiLevelNestedDefaults());
    }

    @Test
    void stagesWithTrigger() {
        StageGraph stageGraph = new StageGraph<TestArtifacts>() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            public StageGrapher stages() {
                return defaults
                  .account("montclair")
                  .region("dev > dev")
                  .application("app1").forStages(() ->
                    super.stages().then(stage.wait(Duration.ofMinutes(1)))
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
                        .artifact(artifacts.getAppJar())
                        .manifest(new ArtifactManifest("montclair", ".*"))
                      )
                      .then(stage.wait("1+1").name("cool off"))
                      .then(cf.rollback("cluster1").name("Rollback"))
                  );
            }
        };

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph, new TestArtifactsImpl()) {
            @Override
            public Triggers triggers() {
                return super.triggers()
                  .artifactory(t -> t
                    .artifactorySearchName("spring-artifactory")
                    .artifact(artifacts.getAppJar())
                  );
            }
        };
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getBasicStagesWithFanOutAndFanInWithTrigger());
    }

    @Test
    void nestedStageGraph() {
        StageGraph stageGraph = new StageGraph() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            public StageGrapher stages() {
                return defaults.region("dev > dev")
                  .forStages(() -> super.stages().then(stage.wait(Duration.ofMinutes(1)))
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

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph);
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getNestedStageGraphs());
    }

    @Test
    void stagesDslWithGeneratedFanOutAndFanIn() {
        StageGraph stageGraph = new StageGraph() {
            final CloudFoundryStageCatalog cf = new CloudFoundryStageCatalog("creds1", defaults);

            @Override
            public StageGrapher stages() {
                return defaults.region("dev > dev").forStages(() ->
                  super.stages().then(stage.checkPreconditions().preconditions(new ExpressionPrecondition(true)))
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

        Pipeline pipeline = new Pipeline<TestArtifacts>("test", stageGraph);
        assertThatJson(pipeline.toJson()).isEqualTo(StageGraphJson.getFanOutToMultipleDeployThenDestroys());
    }

}
