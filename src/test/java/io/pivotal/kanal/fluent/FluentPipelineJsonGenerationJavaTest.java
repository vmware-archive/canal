package io.pivotal.kanal.fluent;

import io.pivotal.kanal.json.FanOutMultistagePipeline;
import io.pivotal.kanal.json.JsonAdapterFactory;
import io.pivotal.kanal.model.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class FluentPipelineJsonGenerationJavaTest {

    @Test
    void fluentStagesDslWithFanOutAndFanIn() {
        Stages stages = Stages.Factory.first(new CheckPreconditionsStage(
                "Check Preconditions",
                emptyList()
        )).andThen(new WaitStage(
                "Server Group Timeout",
                420,
                "woah"
        )).fanOut(
                range(1, 4).mapToObj( it -> Stages.Factory.first(new DestroyServiceStage(
                        "Destroy Service " + it + " Before",
                        "cloudfoundry",
                        "creds1",
                        "dev > dev",
                        "serviceName" + it,
                        new ExpressionCondition("exp1")
                )).andThen(new DeployServiceStage(
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
                ))).collect(Collectors.toList())
        ).fanIn(new ManualJudgmentStage(
                "Thumbs Up?",
                "Give a thumbs up if you like it."
        ));

        String json = new JsonAdapterFactory().stageGraphAdapter().toJson(stages.getStageGraph());
        assertThatJson(json).isEqualTo(FanOutMultistagePipeline.getJson());
    }

}
