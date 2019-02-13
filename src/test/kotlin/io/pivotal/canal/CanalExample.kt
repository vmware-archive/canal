package io.pivotal.canal

import io.pivotal.canal.evaluation.ExpressionEvaluator
import io.pivotal.canal.extensions.nestedstages.stages
import io.pivotal.canal.extensions.pipeline
import io.pivotal.canal.json.JsonAdapterFactory
import io.pivotal.canal.model.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat

class CanalExample {

    companion object {
        @Language("JSON")
        @JvmStatic
        val json = """
            {
                "name":"Canal Example",
                "description":"",
                "keepWaitingPipelines": false,
                "limitConcurrent": true,
                "parameterConfig": [
                  {
                    "name": "canalName",
                    "required": true
                  }
                ],
                "stages": [
                  {
                    "name": "Check canal name",
                    "preconditions": [
                      {
                        "context": {
                          "expression": "true"
                        },
                        "type": "expression"
                      }
                    ],
                    "refId": "checkPreconditions1",
                    "requisiteStageRefIds": [],
                    "type": "checkPreconditions"
                  },
                  {
                    "name": "Wait for decent weather",
                    "refId": "wait2",
                    "requisiteStageRefIds": [
                      "checkPreconditions1"
                    ],
                    "type": "wait",
                    "waitTime": "1"
                  },
                  {
                    "name": "Wait for reservation #42",
                    "refId": "wait3",
                    "requisiteStageRefIds": [
                      "checkPreconditions1"
                    ],
                    "type": "wait",
                    "waitTime": "2"
                  },
                  {
                    "name": "Wait for lock to open",
                    "refId": "wait4",
                    "requisiteStageRefIds": [
                      "checkPreconditions1"
                    ],
                    "type": "wait",
                    "waitTime": "3"
                  },
                  {
                    "judgmentInputs": [],
                    "name": "Travel through canal",
                    "refId": "manualJudgment5",
                    "requisiteStageRefIds": [
                      "wait2",
                      "wait3",
                      "wait4"
                    ],
                    "type": "manualJudgment"
                  }
                ],
                "notifications":[],
                "expectedArtifacts":[],
                "triggers": [
                  {
                    "enabled": true,
                    "project": "canal-pipelines",
                    "slug": "canal",
                    "source": "github",
                    "type": "git"
                  }
                ]
              }
        """.trimMargin()
    }

    val reservationNumber = "42"
    val pipeline = pipeline("Canal Example") {
        parameters(Parameter("canalName"))
        stages = stages {
            stage(
                    CheckPreconditions(
                            ExpressionPrecondition(
                                    "\${ {'Corinth', 'Panama', 'Suez'}.contains(trigger['parameters']['canalName']) }"
                            )
                    ),
                    name = "Check canal name"
            ) then {
                stage(Wait(1), name = "Wait for decent weather")
                stage(Wait(2), name = "Wait for reservation #${reservationNumber}")
                stage(Wait(3), name = "Wait for lock to open")
            } then {
                stage(
                        ManualJudgment(),
                        name = "Travel through canal"
                )
            }
        }
        triggers(
                GitHubTrigger(
                        org = "canal-pipelines",
                        repo = "canal"
                )
        )
    }

    @Test
    fun `Evaluate Example pipeline and convert it to JSON`() {
        val pipelineExecution = PipelineExecution(
                mapOf(
                        "parameters" to mapOf(
                                "canalName" to "Panama"
                        )
                )
        )
        val evaluator = ExpressionEvaluator(pipelineExecution)

        val evaluatedPipeline = evaluator.evaluate(pipeline)

        assertThat(evaluatedPipeline.stageGraph.stages[0].stageConfig)
                .isEqualTo(CheckPreconditions(ExpressionPrecondition(true)))

        val pipelineJson = JsonAdapterFactory().createAdapter<Pipeline>().toJson(evaluatedPipeline)
        JsonAssertions.assertThatJson(pipelineJson).isEqualTo(json)

    }

}
