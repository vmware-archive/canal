package io.pivotal.kanal.json

import io.pivotal.kanal.fluent.Stages
import io.pivotal.kanal.model.*
import io.pivotal.kanal.model.cloudfoundry.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class CloudPipelineExampleTest  {

    @Language("JSON")
    val json = """
{
  "appConfig" : { },
  "keepWaitingPipelines" : false,
  "limitConcurrent" : true,
  "description":"",
  "expectedArtifacts": [],
  "lastModifiedBy": "anonymous",
  "notifications": [],
  "parameterConfig": [],
  "stages" : [ {
    "failPipeline" : true,
    "name" : "Prepare test environment",
    "refId" : "jenkins1",
    "requisiteStageRefIds" : [ ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-test-prepare",
    "continuePipeline" : false
  }, {
    "clusters" : [ {
      "account" : "calabasasaccount",
      "application" : "githubwebhook",
      "artifact" : {
        "account" : "jenkins",
        "pattern" : "^github-webhook.*VERSION.jar${'$'}",
        "type" : "trigger"
      },
      "capacity" : {
        "desired" : "1",
        "max" : "1",
        "min" : "1"
      },
      "cloudProvider" : "cloudfoundry",
      "detail" : "",
      "manifest" : {
        "diskQuota" : "1024M",
        "env" : [ ],
        "instances" : 1,
        "memory" : "1024M",
        "services" : [ "github-rabbitmq", "github-eureka" ],
        "type" : "direct",
        "routes" : [ "sc-pipelines-test-github-webhook.test.foo.com" ]
      },
      "provider" : "cloudfoundry",
      "region" : "scpipelines > sc-pipelines-test-github-webhook",
      "stack" : "",
      "strategy" : "highlander"
    } ],
    "name" : "Deploy to test",
    "refId" : "deploy2",
    "requisiteStageRefIds" : [ "jenkins1" ],
    "type" : "deploy"
  }, {
    "failPipeline" : true,
    "name" : "Run tests on test",
    "refId" : "jenkins3",
    "requisiteStageRefIds" : [ "deploy2" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-test-env-test",
    "continuePipeline" : false
  }, {
    "clusters" : [ {
      "account" : "calabasasaccount",
      "application" : "githubwebhook",
      "artifact" : {
        "account" : "jenkins",
        "pattern" : "^github-webhook.*VERSION-latestprodversion.jar${'$'}",
        "type" : "trigger"
      },
      "capacity" : {
        "desired" : "1",
        "max" : "1",
        "min" : "1"
      },
      "cloudProvider" : "cloudfoundry",
      "detail" : "",
      "manifest" : {
        "diskQuota" : "1024M",
        "env" : [ ],
        "instances" : 1,
        "memory" : "1024M",
        "services" : [ "github-rabbitmq", "github-eureka" ],
        "type" : "direct",
        "routes" : [ "sc-pipelines-test-github-webhook.test.foo.com" ]
      },
      "provider" : "cloudfoundry",
      "region" : "scpipelines > sc-pipelines-test-github-webhook",
      "stack" : "",
      "strategy" : "highlander"
    } ],
    "name" : "Deploy to test latest prod version",
    "refId" : "deploy4",
    "requisiteStageRefIds" : [ "jenkins3" ],
    "type" : "deploy",
    "stageEnabled" : {
      "expression" : "$\\{trigger.properties['LATEST_PROD_VERSION']}",
      "type" : "expression"
    }
  }, {
    "failPipeline" : true,
    "name" : "Run rollback tests on test",
    "refId" : "jenkins5",
    "requisiteStageRefIds" : [ "deploy4" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}",
      "PASSED_LATEST_PROD_TAG" : "$\\{trigger.properties['PASSED_LATEST_PROD_TAG']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-test-env-rollback-test",
    "continuePipeline" : false,
    "stageEnabled" : {
      "expression" : "$\\{trigger.properties['LATEST_PROD_VERSION']}",
      "type" : "expression"
    }
  }, {
    "failPipeline" : true,
    "name" : "Wait for stage env",
    "refId" : "manualJudgment6",
    "requisiteStageRefIds" : [ "jenkins5" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ],
    "notifications" : [ ]
  }, {
    "failPipeline" : true,
    "name" : "Prepare stage environment",
    "refId" : "jenkins7",
    "requisiteStageRefIds" : [ "manualJudgment6" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-stage-prepare",
    "continuePipeline" : false
  }, {
    "clusters" : [ {
      "account" : "calabasasaccount",
      "application" : "githubwebhook",
      "artifact" : {
        "account" : "jenkins",
        "pattern" : "^github-webhook.*VERSION.jar${'$'}",
        "type" : "trigger"
      },
      "capacity" : {
        "desired" : "1",
        "max" : "1",
        "min" : "1"
      },
      "cloudProvider" : "cloudfoundry",
      "detail" : "",
      "manifest" : {
        "diskQuota" : "1024M",
        "env" : [ ],
        "instances" : 1,
        "memory" : "1024M",
        "services" : [ "github-rabbitmq", "github-eureka" ],
        "type" : "direct",
        "routes" : [ "github-webhook-sc-pipelines-stage.stage.foo.com" ]
      },
      "provider" : "cloudfoundry",
      "region" : "scpipelines > sc-pipelines-stage",
      "stack" : "",
      "strategy" : "highlander"
    } ],
    "name" : "Deploy to stage",
    "refId" : "deploy8",
    "requisiteStageRefIds" : [ "jenkins7" ],
    "type" : "deploy"
  }, {
    "failPipeline" : true,
    "name" : "Prepare for end to end tests",
    "refId" : "manualJudgment9",
    "requisiteStageRefIds" : [ "deploy8" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ],
    "notifications" : [ ]
  }, {
    "failPipeline" : true,
    "name" : "End to end tests on stage",
    "refId" : "jenkins10",
    "requisiteStageRefIds" : [ "manualJudgment9" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-stage-env-test",
    "continuePipeline" : false
  }, {
    "failPipeline" : true,
    "name" : "Approve production",
    "refId" : "manualJudgment11",
    "requisiteStageRefIds" : [ "jenkins10" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ],
    "notifications" : [ ]
  }, {
    "clusters" : [ {
      "account" : "calabasasaccount",
      "application" : "githubwebhook",
      "artifact" : {
        "account" : "jenkins",
        "pattern" : "^github-webhook.*VERSION.jar${'$'}",
        "type" : "trigger"
      },
      "capacity" : {
        "desired" : "1",
        "max" : "1",
        "min" : "1"
      },
      "cloudProvider" : "cloudfoundry",
      "detail" : "",
      "manifest" : {
        "diskQuota" : "1024M",
        "env" : [ ],
        "instances" : 1,
        "memory" : "1024M",
        "services" : [ "github-rabbitmq", "github-eureka" ],
        "type" : "direct",
        "routes" : [ "github-webhook.prod.foo.com" ]
      },
      "provider" : "cloudfoundry",
      "region" : "scpipelines > sc-pipelines-prod",
      "stack" : "",
      "strategy" : "highlander"
    } ],
    "name" : "Deploy to prod",
    "refId" : "deploy12",
    "requisiteStageRefIds" : [ "manualJudgment11" ],
    "type" : "deploy"
  }, {
    "failPipeline" : true,
    "name" : "Push prod tag",
    "refId" : "jenkins1_13",
    "requisiteStageRefIds" : [ "deploy12" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-prod-tag-repo",
    "continuePipeline" : false
  }, {
    "failPipeline" : true,
    "name" : "Approve rollback",
    "refId" : "manualJudgment1_14",
    "requisiteStageRefIds" : [ "deploy12" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ],
    "notifications" : [ ]
  }, {
    "clusters" : [ {
      "account" : "calabasasaccount",
      "application" : "githubwebhook",
      "artifact" : {
        "account" : "jenkins",
        "pattern" : "^github-webhook.*VERSION-latestprodversion.jar${'$'}",
        "type" : "trigger"
      },
      "capacity" : {
        "desired" : "1",
        "max" : "1",
        "min" : "1"
      },
      "cloudProvider" : "cloudfoundry",
      "detail" : "",
      "manifest" : {
        "diskQuota" : "1024M",
        "env" : [ ],
        "instances" : 1,
        "memory" : "1024M",
        "services" : [ "github-rabbitmq", "github-eureka" ],
        "type" : "direct",
        "routes" : [ "github-webhook.prod.foo.com" ]
      },
      "provider" : "cloudfoundry",
      "region" : "scpipelines > sc-pipelines-prod",
      "stack" : "",
      "strategy" : "highlander"
    } ],
    "name" : "Rollback",
    "refId" : "deploy2_15",
    "requisiteStageRefIds" : [ "manualJudgment1_14" ],
    "type" : "deploy"
  }, {
    "failPipeline" : true,
    "name" : "Remove prod tag",
    "refId" : "jenkins3_16",
    "requisiteStageRefIds" : [ "deploy2_15" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-prod-env-remove-tag",
    "continuePipeline" : false
  } ],
  "triggers" : [ {
    "enabled" : true,
    "job" : "spinnaker-github-webhook-pipeline-build",
    "master" : "Spinnaker-Jenkins",
    "type" : "jenkins",
    "propertyFile" : "trigger.properties"
  } ],
  "updateTs": "0"
}
    """.trimIndent()

    val model = Pipeline(
            limitConcurrent = true,
            triggers = listOf(
                    JenkinsTrigger(
                            "spinnaker-github-webhook-pipeline-build",
                            "Spinnaker-Jenkins",
                            "trigger.properties"
                    )
            ),
            stageGraph = Stages.of(
                    JenkinsStage(
                            "Prepare test environment",
                            "spinnaker-github-webhook-pipeline-test-prepare",
                            "Spinnaker-Jenkins",
                            mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")
                    )
            ).andThen(
                    DeployStage(
                            "Deploy to test",
                            CloudFoundryCluster(
                                    "githubwebhook",
                                    "calabasasaccount",
                                    "scpipelines > sc-pipelines-test-github-webhook",
                                    "highlander",
                                    TriggerArtifact(
                                            "jenkins",
                                            "^github-webhook.*VERSION.jar${'$'}"
                                    ),
                                    Capacity(1),
                                    DirectManifest(
                                            listOf("github-rabbitmq", "github-eureka"),
                                            listOf("sc-pipelines-test-github-webhook.test.foo.com")
                                    )
                            )
                    )
            ).andThen(
                    JenkinsStage(
                            "Run tests on test",
                            "spinnaker-github-webhook-pipeline-test-env-test",
                            "Spinnaker-Jenkins",
                            mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")
                    )
            ).andThen(
                    DeployStage(
                            "Deploy to test latest prod version",
                            CloudFoundryCluster(
                                    "githubwebhook",
                                    "calabasasaccount",
                                    "scpipelines > sc-pipelines-test-github-webhook",
                                    "highlander",
                                    TriggerArtifact(
                                            "jenkins",
                                            "^github-webhook.*VERSION-latestprodversion.jar${'$'}"
                                    ),
                                    Capacity(1),
                                    DirectManifest(
                                            listOf("github-rabbitmq", "github-eureka"),
                                            listOf("sc-pipelines-test-github-webhook.test.foo.com")
                                    )
                            ),
                            ExpressionCondition("$\\{trigger.properties['LATEST_PROD_VERSION']}")
                    )
            ).andThen(
                    JenkinsStage(
                            "Run rollback tests on test",
                            "spinnaker-github-webhook-pipeline-test-env-rollback-test",
                            "Spinnaker-Jenkins",
                            mapOf(
                                    "PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}",
                                    "PASSED_LATEST_PROD_TAG" to "$\\{trigger.properties['PASSED_LATEST_PROD_TAG']}"
                            ),
                            ExpressionCondition("$\\{trigger.properties['LATEST_PROD_VERSION']}")
                    )
            ).andThen(
                    ManualJudgmentStage("Wait for stage env")
            ).andThen(
                    JenkinsStage(
                            "Prepare stage environment",
                            "spinnaker-github-webhook-pipeline-stage-prepare",
                            "Spinnaker-Jenkins",
                            mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")
                    )
            ).andThen(
                    DeployStage(
                            "Deploy to stage",
                            CloudFoundryCluster(
                                    "githubwebhook",
                                    "calabasasaccount",
                                    "scpipelines > sc-pipelines-stage",
                                    "highlander",
                                    TriggerArtifact(
                                            "jenkins",
                                            "^github-webhook.*VERSION.jar${'$'}"
                                    ),
                                    Capacity(1),
                                    DirectManifest(
                                            listOf("github-rabbitmq", "github-eureka"),
                                            listOf("github-webhook-sc-pipelines-stage.stage.foo.com")
                                    )
                            )
                    )
            ).andThen(
                    ManualJudgmentStage("Prepare for end to end tests")
            ).andThen(
                    JenkinsStage(
                            "End to end tests on stage",
                            "spinnaker-github-webhook-pipeline-stage-env-test",
                            "Spinnaker-Jenkins",
                            mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")
                    )
            ).andThen(
                    ManualJudgmentStage("Approve production")
            ).andThen(
                    DeployStage(
                            "Deploy to prod",
                            CloudFoundryCluster(
                                    "githubwebhook",
                                    "calabasasaccount",
                                    "scpipelines > sc-pipelines-prod",
                                    "highlander",
                                    TriggerArtifact(
                                            "jenkins",
                                            "^github-webhook.*VERSION.jar${'$'}"
                                    ),
                                    Capacity(1),
                                    DirectManifest(
                                            listOf("github-rabbitmq", "github-eureka"),
                                            listOf("github-webhook.prod.foo.com")
                                    )
                            )
                    )
            ).parallel(
                    Stages.of(JenkinsStage(
                            "Push prod tag",
                            "spinnaker-github-webhook-pipeline-prod-tag-repo",
                            "Spinnaker-Jenkins",
                            mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")
                    )),
                    Stages.of(ManualJudgmentStage("Approve rollback"))
                            .andThen(
                                    DeployStage(
                                            "Rollback",
                                                    CloudFoundryCluster(
                                                    "githubwebhook",
                                                    "calabasasaccount",
                                                    "scpipelines > sc-pipelines-prod",
                                                    "highlander",
                                                    TriggerArtifact(
                                                            "jenkins",
                                                            "^github-webhook.*VERSION-latestprodversion.jar${'$'}"
                                                    ),
                                                    Capacity(1),
                                                    DirectManifest(
                                                            listOf("github-rabbitmq", "github-eureka"),
                                                            listOf("github-webhook.prod.foo.com")
                                                    )
                                            )
                                    )
                            ).andThen(
                                    JenkinsStage(
                                            "Remove prod tag",
                                            "spinnaker-github-webhook-pipeline-prod-env-remove-tag",
                                            "Spinnaker-Jenkins",
                                            mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")
                                    )
                            )
            )
                    .stageGraph
    )

    val pipelineAdapter = JsonAdapterFactory().pipelineAdapter()

    @Test
    fun `Generate Cloud Pipeline Example JSON`() {
        val pipelineJson = pipelineAdapter.toJson(model)
        JsonAssertions.assertThatJson(pipelineJson).isEqualTo(json)
    }

    @Test
    fun `Generate Model from Cloud Pipeline Example JSON`() {
        val pipeline = pipelineAdapter.fromJson(json)
        Assertions.assertThat(pipeline).isEqualTo(model)
    }

}