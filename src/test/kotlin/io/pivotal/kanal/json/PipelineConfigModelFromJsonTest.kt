package io.pivotal.kanal.json

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class PipelineConfigModelFromJsonTest  {

    @Test
    fun `JSON pipeline template to model`() {
        val pipeline = JsonAdapterFactory().pipelineConfigAdapter().fromJson(PipelineConfig.json)
        Assertions.assertThat(pipeline).isEqualTo(PipelineConfig.model)
    }

}
