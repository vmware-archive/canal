package io.pivotal.canal.extensions.builder;

import lombok.*;

import java.util.UUID;
import java.util.function.Function;

public class Artifacts {
    protected String generateId() {
        return UUID.randomUUID().toString();
    }

    protected Catalog artifact = new Catalog();

    protected ExpectedArtifactWithoutDefault artifactReference(Artifact matchArtifact) {
        return new ExpectedArtifactWithoutDefault(
          new ArtifactReference(generateId(), null, matchArtifact, null, false, true)
        );
    }

    public static class Catalog {

        public MavenFile maven(Function<MavenFile.MavenFileBuilder, MavenFile.MavenFileBuilder> assign) {
            return assign.apply(MavenFile.builder()).build();
        }

        public JenkinsFile jenkins(Function<JenkinsFile.JenkinsFileBuilder, JenkinsFile.JenkinsFileBuilder> assign) {
            return assign.apply(JenkinsFile.builder()).build();
        }

        public GitHubFile gitHubFile(Function<GitHubFile.GitHubFileBuilder, GitHubFile.GitHubFileBuilder> assign) {
            return assign.apply(GitHubFile.builder()).build();
        }

    }

    public interface ExpectedArtifact {
        ArtifactReference getArtifactReference();
    }

    public static class ArtifactRefId {
        final String artifactId;

        public ArtifactRefId(String artifactId) {
            this.artifactId = artifactId;
        }
    }

    @Value
    @Builder(toBuilder = true)
    public static class ArtifactReference {
        public final String id;
        public final String displayName;
        public final Artifact matchArtifact;
        public final Artifact defaultArtifact;
        public final Boolean useDefaultArtifact;
        public Boolean usePriorArtifact;
    }

    @Value
    public static class ExpectedArtifactWithoutDefault implements ExpectedArtifact {
        final ArtifactReference artifactReference;

        public ExpectedArtifactWithoutDefault usePriorArtifact() {
            return new ExpectedArtifactWithoutDefault(artifactReference.toBuilder().usePriorArtifact(true).build());
        }

        public ExpectedArtifactWithDefault defaultArtifact(Artifact defaultArtifact) {
            return new ExpectedArtifactWithDefault(
              artifactReference.toBuilder().defaultArtifact(defaultArtifact).useDefaultArtifact(true).build()
            );
        }
    }

    @Value
    public static class ExpectedArtifactWithDefault implements ExpectedArtifact {
        final ArtifactReference artifactReference;

        public ExpectedArtifactWithDefault usePriorArtifact() {
            return new ExpectedArtifactWithDefault(artifactReference.toBuilder().usePriorArtifact(true).build());
        }
    }

    public interface Artifact {
        String getType();
        String getArtifactAccount();
        String getReference();
    }

    @Value
    @Builder
    public static class MavenFile implements Artifact {
        final String type = "maven/file";
        @NonNull String artifactAccount;
        @NonNull String reference;
    }

    @Value
    @Builder
    public static class JenkinsFile implements Artifact {
        final String type = "jenkins/file";
        @NonNull String artifactAccount;
        @NonNull String reference;
    }

    @Value
    @Builder
    public static class GitHubFile implements Artifact {
        final String type = "github/file";
        @NonNull String artifactAccount;
        @NonNull String reference;
        @NonNull String version;
    }

}
