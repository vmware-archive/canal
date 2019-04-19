package io.pivotal.canal.extensions.builder;

import com.squareup.moshi.JsonAdapter;
import io.pivotal.canal.json.JsonAdapterFactory;
import io.pivotal.canal.model.*;

import static java.util.Collections.emptyList;

public class Pipeline {
    private final String name;
    private final PipelineDefaults defaults;

    protected final StageCatalog stage;

    public Pipeline(String name, Defaults defaults) {
        this.name = name;
        this.defaults = defaults.getDelegate();
        stage = new StageCatalog(this.defaults, new StageGrapher());
    }

    public StageGrapher stages() {
        return new StageGrapher();
    }

    public String toJson() {
        final JsonAdapter<PipelineModel> adapter = new JsonAdapterFactory().jsonAdapterBuilder().build().adapter(PipelineModel.class);
        final PipelineModel model = new PipelineModel(
                name,
                "",
                emptyList(),
                emptyList(),
                emptyList(),
                stages().graph(),
                emptyList(),
                false,
                true
        );
        return adapter.toJson(model);
    }
}
