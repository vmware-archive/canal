package io.pivotal.kanal.json

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.junit.jupiter.api.Test

class PipelineTemplateJsonGenerationTest {

    @Test
    fun `generate pipeline template json`() {
        val json = JsonAdapterFactory().pipelineTemplateAdapter().toJson(BasicPipelineTemplate.model)
        JsonAssertions.assertThatJson(json).isEqualTo(BasicPipelineTemplate.json)
    }

}
