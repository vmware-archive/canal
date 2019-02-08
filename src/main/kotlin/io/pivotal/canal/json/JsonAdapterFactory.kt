package io.pivotal.canal.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.pivotal.canal.model.*
import io.pivotal.canal.model.cloudfoundry.*

class JsonAdapterFactory {
    @JvmOverloads fun jsonAdapterBuilder(builder: Moshi.Builder = Moshi.Builder(),
                                         useCloudSpecificAdapter: Boolean = true): Moshi.Builder {
        builder
                .add(StageGraphAdapter())
                .add(ExpressionConditionAdapter())
                .add(ExpressionPreconditionAdapter())
                .add(PipelineTemplateInstanceAdapter())

        if (useCloudSpecificAdapter) {
            builder.add(CloudSpecificToJsonAdapter())
        }

        builder
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
                .add(PolymorphicJsonAdapterFactory.of(StageConfig::class.java, "type")
                        .withSubtype(DestroyServerGroup::class.java, "destroyServerGroup")
                        .withSubtype(DeployService::class.java, "deployService")
                        .withSubtype(DestroyService::class.java, "destroyService")
                        .withSubtype(DisableServerGroup::class.java, "disableServerGroup")
                        .withSubtype(EnableServerGroup::class.java, "enableServerGroup")
                        .withSubtype(ResizeServerGroup::class.java, "resizeServerGroup")
                        .withSubtype(Wait::class.java, "wait")
                        .withSubtype(ManualJudgment::class.java, "manualJudgment")
                        .withSubtype(Webhook::class.java, "webhook")
                        .withSubtype(Canary::class.java, "kayentaCanary")
                        .withSubtype(Deploy::class.java, "deploy")
                        .withSubtype(CheckPreconditions::class.java, "checkPreconditions")
                        .withSubtype(Jenkins::class.java, "jenkins")
                )
                .add(PolymorphicJsonAdapterFactory.of(Variable::class.java, "type")
                        .withSubtype(IntegerVariable::class.java, "int")
                        .withSubtype(StringVariable::class.java, "string")
                        .withSubtype(FloatVariable::class.java, "float")
                        .withSubtype(BooleanVariable::class.java, "boolean")
                        .withSubtype(ListVariable::class.java, "list")
                        .withSubtype(ObjectVariable::class.java, "object")
                )
                .add(PolymorphicJsonAdapterFactory.of(Inject::class.java, "type")
                        .withSubtype(Inject.Before::class.java, "before")
                        .withSubtype(Inject.After::class.java, "after")
                        .withSubtype(Inject.First::class.java, "first")
                        .withSubtype(Inject.Last::class.java, "last")
                )
                .add(PolymorphicJsonAdapterFactory.of(ExpectedArtifact::class.java, "type")
                )
                .add(PolymorphicJsonAdapterFactory.of(ManifestSource::class.java, "type")
                        .withSubtype(ManifestSourceArtifact::class.java, "artifact")
                        .withSubtype(ManifestSourceUserProvided::class.java, "userProvided")
                        .withSubtype(ManifestSourceDirect::class.java, "direct")
                )
                .add(PolymorphicJsonAdapterFactory.of(ResizeAction::class.java, "action")
                        .withSubtype(ScaleExactResizeAction::class.java, "scale_exact")
                )
                .add(PolymorphicJsonAdapterFactory.of(CloudProvider::class.java, "cloudProvider")
                        .withSubtype(CloudFoundryCloudProvider::class.java, "cloudfoundry")
                )
                .add(KotlinJsonAdapterFactory())
        return builder
    }

    // without this Adapter that has no CloudSpecificToJsonAdapter, map to stage from Json will loop as we try
    // to pull the flattened cloudprovider out and convert the rest of the map to a stage
    inline fun <reified T>  createNonCloudSpecificAdapter(): JsonAdapter<T> =
            jsonAdapterBuilder(Moshi.Builder(), false)
                    .build().adapter(T::class.java)

    inline fun <reified T> createAdapter(): JsonAdapter<T> =
            jsonAdapterBuilder(Moshi.Builder())
                    .build().adapter(T::class.java)

}
