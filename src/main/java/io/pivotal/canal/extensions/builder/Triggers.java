package io.pivotal.canal.extensions.builder;

import com.squareup.moshi.Json;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.pivotal.canal.extensions.builder.Artifacts.ExpectedArtifact;

public class Triggers {
    @Getter final List<Trigger> triggers;

    Triggers() {
        triggers = Collections.emptyList();
    }

    private Triggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    public Triggers artifactory(Function<Artifactory.ArtifactoryBuilder, Artifactory.ArtifactoryBuilder> assign) {
        val allTriggers = new ArrayList<>(this.triggers);
        allTriggers.add(assign.apply(Artifactory.builder()).build().toBuilder().build());
        return new Triggers(allTriggers);
    }

    public interface Trigger {
        String getType();
        Boolean getEnabled();
        List<String> getExpectedArtifactIds();
    }

    static abstract class ExpectedArtifactGetable {
        public abstract List<ExpectedArtifact> getArtifacts();

        public List<String> calculateExpectedArtifactIds() {
            return Optional.ofNullable(getArtifacts()).orElse(Collections.emptyList())
              .stream().map(a -> a.getArtifactReference().getId()).collect(Collectors.toList());
        }
    }

    @Value
    @Builder(toBuilder = true)
    public static class Artifactory extends ExpectedArtifactGetable implements Trigger {
        final String type = "artifactory";
        @Builder.Default Boolean enabled = true;
        @Singular transient List<ExpectedArtifact> artifacts;
        @Builder.ObtainVia(method = "calculateExpectedArtifactIds")
        final List<String> expectedArtifactIds;

        @NonNull String artifactorySearchName;
    }

    @Value @Builder public static class Jenkins extends ExpectedArtifactGetable implements Trigger {
        final String type = "jenkins";
        @Builder.Default Boolean enabled = true;
        @Singular transient List<ExpectedArtifact> artifacts;
        @Builder.ObtainVia(method = "calculateExpectedArtifactIds")
        final List<String> expectedArtifactIds;

        @NonNull String job;
        @NonNull String master;
        String propertyFile;
    }

    @Value @Builder public static class GitHub extends ExpectedArtifactGetable implements Trigger {
        final String type = "git";
        @Builder.Default Boolean enabled = true;
        @Singular transient List<ExpectedArtifact> artifacts;
        @Builder.ObtainVia(method = "calculateExpectedArtifactIds")
        final List<String> expectedArtifactIds;

        final String source = "github";
        @NonNull @Json(name = "project") String org;
        @NonNull @Json(name = "slug") String repo;
        String branch;
        String secret;
    }

}
