package io.pivotal.canal.extensions.builder;

import com.squareup.moshi.JsonAdapter;
import io.pivotal.canal.json.JsonAdapterFactory;
import io.pivotal.canal.model.*;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class Pipeline {
    private final String name;

    protected final StageCatalog stage;

    public Pipeline(String name) {
        this.name = name;
        stage = new StageCatalog();
    }

    public StageGrapher stages() {
        return new StageGrapher();
    }

    public StageGrapher parallel(StageGrapher... stageGraphers) {
        return parallel(Arrays.asList(stageGraphers));
    }
    public StageGrapher parallel(List<StageGrapher> stageGraphers) {
        return new StageGrapher().union(stageGraphers);
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
