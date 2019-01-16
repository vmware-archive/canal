package io.pivotal.kanal.json

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class PipelineTemplateModelFromJsonTest  {

    @Test
    fun `JSON pipeline template to model`() {
        val pipeline = JsonAdapterFactory().pipelineTemplateAdapter().fromJson(BasicPipelineTemplate.json)
        Assertions.assertThat(pipeline).isEqualTo(BasicPipelineTemplate.model)
    }

}
