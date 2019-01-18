package io.pivotal.kanal.json

import io.pivotal.kanal.fluent.Stages
import io.pivotal.kanal.model.*
import org.intellij.lang.annotations.Language

object BasicPipelineTemplate {

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
                    Variable(
                            "waitTime",
                            "The time a wait stage shall pauseth",
                            IntegerType(42)
                    )
            ),
            Pipeline(
                    stageGraph = Stages.of(WaitStage(
                            "$\\{ templateVariables.waitTime }",
                            name = "My Wait Stage"
                    )).stageGraph
            )
    )
}

