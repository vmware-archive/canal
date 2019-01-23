package io.pivotal.kanal.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.pivotal.kanal.model.*
import io.pivotal.kanal.model.cloudfoundry.*

class JsonAdapterFactory {
    @JvmOverloads fun jsonAdapterBuilder(builder: Moshi.Builder = Moshi.Builder()): Moshi.Builder {
        builder
                .add(StageGraphAdapter())
                .add(OrcaStageAdapter())
                .add(PipelineAdapter())
                .add(ScoreThresholdsAdapter())
                .add(ExpressionConditionAdapter())
                .add(ExpressionPreconditionAdapter())
                .add(PipelineConfigAdapter())
                .add(VariableAdapter())
                .add(jsonNumberAdapter)
                .add(PolymorphicJsonAdapterFactory.of(Trigger::class.java, "type")
                        .withSubtype(JenkinsTrigger::class.java, "jenkins")
                        .withSubtype(GitTrigger::class.java, "git")
                        .withSubtype(PubSubTrigger::class.java, "pubsub")
                )
                .add(PolymorphicJsonAdapterFactory.of(Condition::class.java, "type")
                        .withSubtype(ExpressionCondition::class.java, "expression")
                )
                .add(PolymorphicJsonAdapterFactory.of(Precondition::class.java, "type")
                        .withSubtype(ExpressionPrecondition::class.java, "expression")
                )
                .add(PolymorphicJsonAdapterFactory.of(Notification::class.java, "type")
                        .withSubtype(EmailNotification::class.java, "email")
                )
                .add(PolymorphicJsonAdapterFactory.of(Cluster::class.java, "cloudProvider")
                        .withSubtype(CloudFoundryCluster::class.java, "cloudfoundry")
                )
                .add(PolymorphicJsonAdapterFactory.of(Artifact::class.java, "type")
                        .withSubtype(TriggerArtifact::class.java, "trigger")
                        .withSubtype(ReferencedArtifact::class.java, "artifact")
                )
                .add(PolymorphicJsonAdapterFactory.of(Manifest::class.java, "type")
                        .withSubtype(DirectManifest::class.java, "direct")
                        .withSubtype(ArtifactManifest::class.java, "artifact")
                )
                .add(PolymorphicJsonAdapterFactory.of(Stage::class.java, "type")
                        .withSubtype(DestroyServerGroupStage::class.java, "destroyServerGroup")
                        .withSubtype(DeployServiceStage::class.java, "deployService")
                        .withSubtype(DestroyServiceStage::class.java, "destroyService")
                        .withSubtype(WaitStage::class.java, "wait")
                        .withSubtype(ManualJudgmentStage::class.java, "manualJudgment")
                        .withSubtype(WebhookStage::class.java, "webhook")
                        .withSubtype(CanaryStage::class.java, "kayentaCanary")
                        .withSubtype(DeployStage::class.java, "deploy")
                        .withSubtype(CheckPreconditionsStage::class.java, "checkPreconditions")
                        .withSubtype(JenkinsStage::class.java, "jenkins")
                )
                .add(PolymorphicJsonAdapterFactory.of(VariableType::class.java, "type")
                        .withSubtype(IntegerType::class.java, "int")
                        .withSubtype(StringType::class.java, "string")
                        .withSubtype(FloatType::class.java, "float")
                        .withSubtype(BooleanType::class.java, "boolean")
                        .withSubtype(ListType::class.java, "list")
                        .withSubtype(ObjectType::class.java, "object")
                )
                .add(PolymorphicJsonAdapterFactory.of(Inject::class.java, "type")
                        .withSubtype(Inject.Before::class.java, "before")
                        .withSubtype(Inject.After::class.java, "after")
                        .withSubtype(Inject.First::class.java, "first")
                        .withSubtype(Inject.Last::class.java, "last")
                )
                .add(KotlinJsonAdapterFactory())
        return builder
    }

    inline fun <reified T> createAdapter(): JsonAdapter<T> =
            jsonAdapterBuilder(Moshi.Builder()).build().adapter(T::class.java)

}