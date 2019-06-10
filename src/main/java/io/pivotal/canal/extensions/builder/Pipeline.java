package io.pivotal.canal.extensions.builder;

import com.squareup.moshi.JsonAdapter;
import io.pivotal.canal.json.JsonAdapterFactory;
import io.pivotal.canal.model.*;
import lombok.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class Pipeline<A> {
    private final String name;

    protected final StageGraph<A> stageGraph;
    protected final A artifacts;

    public Pipeline(String name, StageGraph<A> stageGraph) {
        this(name, stageGraph, null);
    }

    public Pipeline(String name, StageGraph<A> stageGraph, A artifacts) {
        this.name = name;
        this.stageGraph = stageGraph;
        this.artifacts = artifacts;
    }

    public Triggers triggers() {
        return new Triggers();
    }

    public String toJson() {
        final JsonAdapter<PipelineModel> adapter = new JsonAdapterFactory().jsonAdapterBuilder().build().adapter(PipelineModel.class);

        List<Artifacts.ArtifactReference> artifactReferences = (artifacts == null) ? emptyList() : Arrays
          .stream(artifacts.getClass().getDeclaredMethods())
          .filter(method -> method.getReturnType().equals(Artifacts.ExpectedArtifact.class))
          .map(expectedArtifactsMethod -> {
              Artifacts.ExpectedArtifact expectedArtifact;
              try {
                  expectedArtifact = (Artifacts.ExpectedArtifact) expectedArtifactsMethod.invoke(artifacts);
              } catch (Exception e) {
                  throw new IllegalStateException(e);
              }
              return expectedArtifact.getArtifactReference().toBuilder()
                .displayName(expectedArtifactsMethod.getName().replaceFirst("get", "")).build();
          }).collect(Collectors.toList());


        final PipelineModel model = new PipelineModel(
                name,
                "",
                emptyList(),
                emptyList(),
                triggers().getTriggers(),
                stageGraph.getStages(artifacts),
                artifactReferences,
                false,
                true
        );
        return adapter.toJson(model);
    }

}
