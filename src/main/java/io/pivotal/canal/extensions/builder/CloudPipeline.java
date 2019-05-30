package io.pivotal.canal.extensions.builder;

public class CloudPipeline<T extends CloudStageCatalog> extends Pipeline {

    protected final T cloud;

    protected final DefaultsForStages defaults;

    public CloudPipeline(String name, T cloudStageCatalog) {
        super(name);
        this.cloud = cloudStageCatalog;
        this.defaults = new DefaultsForStages(cloudStageCatalog);
    }

}
