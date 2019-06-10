package io.pivotal.canal.extensions.builder;

import io.pivotal.canal.model.Stages;

public class StageGraph<A> {
    protected final StageCatalog stage;
    protected final DefaultsForStages defaults;
    protected A artifacts;

    public StageGraph() {
        stage = new StageCatalog();
        this.defaults = new DefaultsForStages();
    }

    protected StageGrapher stages() {
        return new StageGrapher();
    }

    Stages getStages(A artifacts) {
        this.artifacts = artifacts;
        return stages().graph();
    }

}
