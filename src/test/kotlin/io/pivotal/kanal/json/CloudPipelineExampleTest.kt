package io.pivotal.kanal.json

import io.pivotal.kanal.extensions.*
import io.pivotal.kanal.extensions.fluentstages.addStage
import io.pivotal.kanal.extensions.fluentstages.andThen
import io.pivotal.kanal.extensions.fluentstages.parallel
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
  "keepWaitingPipelines" : false,
  "limitConcurrent" : true,
  "description":"",
  "expectedArtifacts": [],
  "notifications": [],
  "parameterConfig": [],
  "stages" : [ {
    "name" : "Prepare test environment",
    "refId" : "jenkins1",
    "requisiteStageRefIds" : [ ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-test-prepare"
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
    "name" : "Run tests on test",
    "refId" : "jenkins3",
    "requisiteStageRefIds" : [ "deploy2" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-test-env-test"
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
    "stageEnabled" : {
      "expression" : "$\\{trigger.properties['LATEST_PROD_VERSION']}",
      "type" : "expression"
    }
  }, {
    "instructions" : "Wait for stage env",
    "refId" : "manualJudgment6",
    "requisiteStageRefIds" : [ "jenkins5" ],
    "type" : "manualJudgment",
    "judgmentInputs" : []
  }, {
    "name" : "Prepare stage environment",
    "refId" : "jenkins7",
    "requisiteStageRefIds" : [ "manualJudgment6" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-stage-prepare"
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
    "instructions" : "Prepare for end to end tests",
    "refId" : "manualJudgment9",
    "requisiteStageRefIds" : [ "deploy8" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ]
  }, {
    "name" : "End to end tests on stage",
    "refId" : "jenkins10",
    "requisiteStageRefIds" : [ "manualJudgment9" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-stage-env-test"
  }, {
    "instructions" : "Approve production",
    "refId" : "manualJudgment11",
    "requisiteStageRefIds" : [ "jenkins10" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ]
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
    "name" : "Push prod tag",
    "refId" : "jenkins13",
    "requisiteStageRefIds" : [ "deploy12" ],
    "type" : "jenkins",
    "waitForCompletion" : true,
    "parameters" : {
      "PIPELINE_VERSION" : "$\\{trigger.properties['PIPELINE_VERSION']}"
    },
    "master" : "Spinnaker-Jenkins",
    "job" : "spinnaker-github-webhook-pipeline-prod-tag-repo"
  }, {
    "instructions" : "Approve rollback",
    "refId" : "manualJudgment14",
    "requisiteStageRefIds" : [ "deploy12" ],
    "type" : "manualJudgment",
    "judgmentInputs" : [ ]
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
    "refId" : "deploy15",
    "requisiteStageRefIds" : [ "manualJudgment14" ],
    "type" : "deploy"
  }, {
    "failPipeline" : true,
    "name" : "Remove prod tag",
    "refId" : "jenkins16",
    "requisiteStageRefIds" : [ "deploy15" ],
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
  } ]
}
    """.trimIndent()

    val projectName = "spinnaker-github-webhook-pipeline"
    val master = "Spinnaker-Jenkins"

    fun clusterFor(region: String, artifact: String, route: String) : CloudFoundryCluster {
        return CloudFoundryCluster(
                "githubwebhook",
                "calabasasaccount",
                "scpipelines > ${region}",
                DeploymentStrategy.Highlander,
                TriggerArtifact(
                        "jenkins",
                        artifact
                ),
                Capacity(1),
                DirectManifest(
                        listOf("github-rabbitmq", "github-eureka"),
                        listOf(route)
                )
        )
    }

    fun jenkinsStageFor(jobName: String,
                        parameters: Map<String, String> = mapOf("PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}")) : Jenkins {
        return Jenkins(
                "$projectName-$jobName",
                master,
                parameters
        )
    }

    fun jenkinsTestJobName(env: String, testName: String) : String {
        return "$env-env-$testName"
    }

    val model = Pipeline().with {
        limitConcurrent = true
        triggers = listOf(
                JenkinsTrigger(
                        "$projectName-build",
                        master,
                        "trigger.properties"
                )
        )
        stages = StageGraph().addStage(
                jenkinsStageFor("test-prepare"),
                BaseStage("Prepare test environment")
        ).andThen(
                Deploy(
                        clusterFor("sc-pipelines-test-github-webhook",
                                "^github-webhook.*VERSION.jar${'$'}",
                                "sc-pipelines-test-github-webhook.test.foo.com")
                ),
                BaseStage("Deploy to test")
        ).andThen(
                jenkinsStageFor(jenkinsTestJobName("test", "test")),
                BaseStage("Run tests on test")
        ).andThen(
                Deploy(
                        clusterFor("sc-pipelines-test-github-webhook",
                                "^github-webhook.*VERSION-latestprodversion.jar${'$'}",
                                "sc-pipelines-test-github-webhook.test.foo.com")
                ),
                BaseStage("Deploy to test latest prod version",
                        stageEnabled = ExpressionCondition("$\\{trigger.properties['LATEST_PROD_VERSION']}")
                )
        ).andThen(
                jenkinsStageFor(
                        jenkinsTestJobName("test", "rollback-test"),
                        mapOf(
                                "PIPELINE_VERSION" to "$\\{trigger.properties['PIPELINE_VERSION']}",
                                "PASSED_LATEST_PROD_TAG" to "$\\{trigger.properties['PASSED_LATEST_PROD_TAG']}"
                        )
                ),
                BaseStage("Run rollback tests on test",
                        stageEnabled = ExpressionCondition("$\\{trigger.properties['LATEST_PROD_VERSION']}"))
        ).andThen(
                ManualJudgment("Wait for stage env")
        ).andThen(
                jenkinsStageFor("stage-prepare"),
                BaseStage("Prepare stage environment")
        ).andThen(
                Deploy(
                        clusterFor("sc-pipelines-stage",
                                "^github-webhook.*VERSION.jar${'$'}",
                                "github-webhook-sc-pipelines-stage.stage.foo.com")
                ),
                BaseStage("Deploy to stage")
        ).andThen(
                ManualJudgment("Prepare for end to end tests")
        ).andThen(
                jenkinsStageFor(jenkinsTestJobName("stage", "test")),
                BaseStage("End to end tests on stage")
        ).andThen(
                ManualJudgment("Approve production")
        ).andThen(
                Deploy(
                        clusterFor("sc-pipelines-prod",
                                "^github-webhook.*VERSION.jar${'$'}",
                                "github-webhook.prod.foo.com")
                ),
                BaseStage("Deploy to prod")
        ).parallel(
                StageGraph().addStage(
                        jenkinsStageFor("prod-tag-repo"),
                        BaseStage("Push prod tag")
                ),
                StageGraph().addStage(ManualJudgment("Approve rollback"))
                        .andThen(
                                Deploy(
                                        clusterFor("sc-pipelines-prod",
                                                "^github-webhook.*VERSION-latestprodversion.jar${'$'}",
                                                "github-webhook.prod.foo.com")
                                ),
                                BaseStage("Rollback")
                        ).andThen(
                                jenkinsStageFor(jenkinsTestJobName("prod", "remove-tag")),
                                BaseStage("Remove prod tag",
                                        failPipeline = true,
                                        continuePipeline = false
                                )
                        )
        )
    }

    @Test
    fun `Generate Cloud Pipeline Example JSON`() {
        val pipelineJson = JsonAdapterFactory().createAdapter<Pipeline>().toJson(model)
        JsonAssertions.assertThatJson(pipelineJson).isEqualTo(json)
    }

    @Test
    fun `Generate Model from Cloud Pipeline Example JSON`() {
        val pipeline = JsonAdapterFactory().createAdapter<Pipeline>().fromJson(json)
        Assertions.assertThat(pipeline).isEqualTo(model)
    }

}