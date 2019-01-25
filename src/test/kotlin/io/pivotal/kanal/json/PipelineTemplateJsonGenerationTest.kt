package io.pivotal.kanal.json

import io.pivotal.kanal.fluent.Stages
import io.pivotal.kanal.model.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class PipelineTemplateJsonGenerationTest {

    companion object {

        @Language("JSON")
        @JvmStatic
        val json = """
    {
        "schema" : "v2",
        "variables" : [
            {
                "type" : "int",
                "defaultValue" : 42,
                "description" : "The time a wait stage shall pauseth",
                "name" : "waitTime",
                "merge" : false,
                "nullable" : false,
                "remove" : false
            }
        ],
        "id" : "newSpelTemplate",
        "protect" : false,
        "metadata" : {
            "name" : "Variable Wait",
            "description" : "A demonstrative Wait Pipeline.",
            "owner" : "example@example.com",
            "scopes" : ["global"]
        },
        "pipeline": {
            "lastModifiedBy" : "anonymous",
            "updateTs" : "0",
            "parameterConfig" : [],
            "limitConcurrent": false,
            "keepWaitingPipelines": false,
            "description" : "",
            "triggers" : [],
            "notifications" : [],
            "stages" : [
                {
                    "waitTime" : "$\\{ templateVariables.waitTime }",
                    "name": "My Wait Stage",
                    "type" : "wait",
                    "refId" : "wait1",
                    "requisiteStageRefIds": [],
                    "comments": ""
                }
            ],
            "expectedArtifacts": [],
            "appConfig": {}
        }
    }
        """.trimMargin()

        @JvmStatic
        val model = PipelineTemplate(
                "newSpelTemplate",
                Metadata(
                        "Variable Wait",
                        "A demonstrative Wait Pipeline.",
                        "example@example.com"
                ),
                listOf(
                        IntegerVariable(
                                "waitTime",
                                "The time a wait stage shall pauseth",
                                42
                        )
                ),
                Pipeline(
                        stages = Stages.of(WaitStage(
                                "$\\{ templateVariables.waitTime }",
                                name = "My Wait Stage"
                        )).stageGraph
                )
        )
    }

    @Test
    fun `generate pipeline template JSON`() {
        val json = JsonAdapterFactory().createAdapter<PipelineTemplate>().toJson(model)
        JsonAssertions.assertThatJson(json).isEqualTo(json)
    }

    @Test
    fun `JSON pipeline template to model`() {
        val pipeline = JsonAdapterFactory().createAdapter<PipelineTemplate>().fromJson(json)
        Assertions.assertThat(pipeline).isEqualTo(model)
    }

}
