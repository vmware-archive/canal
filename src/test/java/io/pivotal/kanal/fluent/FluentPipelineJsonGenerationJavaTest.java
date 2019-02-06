package io.pivotal.kanal.fluent;

import com.squareup.moshi.JsonAdapter;
import io.pivotal.kanal.builders.StageGraphBuilder;
import io.pivotal.kanal.json.JsonAdapterFactory;
import io.pivotal.kanal.model.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class FluentPipelineJsonGenerationJavaTest {

    @Test
    void fluentStagesDslWithFanOutAndFanIn() {
        StageGraphBuilder stages = new StageGraphBuilder() {{
            with(new CheckPreconditionsStage(
                    "Check Preconditions",
                    Arrays.asList(new ExpressionPrecondition(true))
            ));
            andThen(new WaitStage(
                    420,
                    "woah",
                    "Server Group Timeout"
            ));
            parallel(
                    range(1, 4).mapToObj( it -> new StageGraphBuilder() {{
                            with(new DestroyServiceStage(
                                    "Destroy Service " + it + " Before",
                                    "cloudfoundry",
                                    "creds1",
                                    "dev > dev",
                                    "serviceName" + it,
                                    new ExpressionCondition("exp1")
                            ));
                            andThen(new DeployServiceStage(
                                    "Deploy Service " + it,
                                    "cloudfoundry",
                                    "deploy comment",
                                    "creds1",
                                    "serviceParam" + it,
                                    "dev > dev",
                                    "serviceType" + it,
                                    "serviceName" + it,
                                    "servicePlan" + it,
                                    new ExpressionCondition("exp2"),
                                    "serviceTags" + it
                            ));
                        }}
                    ).collect(Collectors.toList())
            );
            andThen(new ManualJudgmentStage(
                    "Thumbs Up?",
                    "Give a thumbs up if you like it."
            ));
        }};

        JsonAdapter<StageGraph> adapter =
                new JsonAdapterFactory().jsonAdapterBuilder().build().adapter(StageGraph.class);
        String json = adapter.toJson(stages.getStageGraph());
        assertThatJson(json).isEqualTo(FluentPipelineJsonGenerationTest.getJson());
    }

}
