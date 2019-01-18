package io.pivotal.kanal.json

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.junit.jupiter.api.Test

class PipelineConfigJsonGenerationTest {

    @Test
    fun `generate pipeline template json`() {
        val json = JsonAdapterFactory().pipelineConfigAdapter().toJson(PipelineConfig.model)
        JsonAssertions.assertThatJson(json).isEqualTo(PipelineConfig.json)
    }

}
