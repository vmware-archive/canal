package io.pivotal.kanal.extensions.fluentstages;

import com.squareup.moshi.JsonAdapter;
import io.pivotal.kanal.builders.StageGraphBuilder;
import io.pivotal.kanal.json.JsonAdapterFactory;
import io.pivotal.kanal.model.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.pivotal.kanal.model.cloudfoundry.CloudFoundryCloudProvider;
import io.pivotal.kanal.model.cloudfoundry.ManifestSourceDirect;
import org.junit.jupiter.api.Test;

import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class FluentPipelineJsonGenerationJavaTest {

    @Test
    void fluentStagesDslWithFanOutAndFanIn() {
        StageGraphBuilder stages = new StageGraphBuilder() {{
            with(new CheckPreconditions(
                    Arrays.asList(new ExpressionPrecondition(true))
                    ),
                    new BaseStage("Check Preconditions")
            );
            andThen(new Wait(420));
            parallel(
                    range(1, 4).mapToObj( it -> new StageGraphBuilder() {{
                            with(
                                    new DestroyService(
                                            new CloudFoundryCloudProvider("creds1"),
                                            "dev > dev",
                                            "serviceName" + it
                                    ),
                                    new BaseStage("Destroy Service " + it + " Before",
                                            null,
                                            new ExpressionCondition("exp1")
                                    )
                            );
                            andThen(
                                    new DeployService(
                                            new CloudFoundryCloudProvider(
                                                    "creds1",
                                                    new ManifestSourceDirect(
                                                            "serviceType" + it,
                                                            "serviceName" + it,
                                                            "servicePlan" + it,
                                                            Arrays.asList("serviceTags" + it),
                                                            "serviceParam" + it
                                                    )),
                                            "dev > dev"
                                    ),
                                    new BaseStage("Deploy Service " + it,
                                            "deploy comment",
                                            new ExpressionCondition("exp2")
                                    )
                            );
                        }}
                    ).collect(Collectors.toList())
            );
            andThen(new ManualJudgment(
                    "Give a thumbs up if you like it."
            ));
        }};

        JsonAdapter<StageGraph> adapter =
                new JsonAdapterFactory().jsonAdapterBuilder().build().adapter(StageGraph.class);
        String json = adapter.toJson(stages.getStageGraph());
        assertThatJson(json).isEqualTo(FluentPipelineJsonGenerationTest.getJson());
    }

}
