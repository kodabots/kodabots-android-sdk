-optimizationpasses 3
-dontusemixedcaseclassnames
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-repackageclasses ''
-allowaccessmodification
-dontnote

-keep class ai.koda.mobile.sdk.core.* { *; }
-keep class ai.koda.mobile.sdk.core.** { *; }
-keep class ai.koda.mobile.sdk.core.*$* { *; }
-keep class kotlin.reflect.** { *; }
-keep class org.jetbrains.** { *; }

-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

-dontnote kotlinx.serialization.AnnotationsKt
-dontwarn kotlin.reflect.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ai.koda.mobile.sdk.core.**$$serializer { *; }
-keepclassmembers class ai.koda.mobile.sdk.core.** {
    *** Companion;
    *** Default;
}
-keepclasseswithmembers class ai.koda.mobile.sdk.core.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn org.slf4j.impl.StaticLoggerBinder

-dontwarn groovy.lang.Closure
-dontwarn org.gradle.api.Action
-dontwarn org.gradle.api.DomainObjectSet
-dontwarn org.gradle.api.JavaVersion
-dontwarn org.gradle.api.Named
-dontwarn org.gradle.api.NamedDomainObjectCollection
-dontwarn org.gradle.api.NamedDomainObjectContainer
-dontwarn org.gradle.api.NamedDomainObjectProvider
-dontwarn org.gradle.api.NamedDomainObjectSet
-dontwarn org.gradle.api.Plugin
-dontwarn org.gradle.api.Project
-dontwarn org.gradle.api.Task
-dontwarn org.gradle.api.artifacts.Dependency
-dontwarn org.gradle.api.artifacts.ExternalModuleDependency
-dontwarn org.gradle.api.artifacts.ProjectDependency
-dontwarn org.gradle.api.artifacts.dsl.DependencyHandler
-dontwarn org.gradle.api.attributes.Attribute
-dontwarn org.gradle.api.attributes.AttributeCompatibilityRule
-dontwarn org.gradle.api.attributes.AttributeContainer
-dontwarn org.gradle.api.attributes.AttributeDisambiguationRule
-dontwarn org.gradle.api.attributes.AttributeMatchingStrategy
-dontwarn org.gradle.api.attributes.AttributesSchema
-dontwarn org.gradle.api.attributes.CompatibilityCheckDetails
-dontwarn org.gradle.api.attributes.CompatibilityRuleChain
-dontwarn org.gradle.api.attributes.DisambiguationRuleChain
-dontwarn org.gradle.api.attributes.HasAttributes
-dontwarn org.gradle.api.attributes.MultipleCandidatesDetails
-dontwarn org.gradle.api.component.AdhocComponentWithVariants
-dontwarn org.gradle.api.component.SoftwareComponent
-dontwarn org.gradle.api.file.ConfigurableFileCollection
-dontwarn org.gradle.api.file.DirectoryProperty
-dontwarn org.gradle.api.file.DuplicatesStrategy
-dontwarn org.gradle.api.file.FileCollection
-dontwarn org.gradle.api.file.SourceDirectorySet
-dontwarn org.gradle.api.logging.Logger
-dontwarn org.gradle.api.plugins.ExtensionAware
-dontwarn org.gradle.api.provider.ListProperty
-dontwarn org.gradle.api.provider.Property
-dontwarn org.gradle.api.provider.Provider
-dontwarn org.gradle.api.provider.ProviderFactory
-dontwarn org.gradle.api.provider.SetProperty
-dontwarn org.gradle.api.publish.maven.MavenPublication
-dontwarn org.gradle.api.tasks.Classpath
-dontwarn org.gradle.api.tasks.IgnoreEmptyDirectories
-dontwarn org.gradle.api.tasks.Input
-dontwarn org.gradle.api.tasks.InputDirectory
-dontwarn org.gradle.api.tasks.InputFiles
-dontwarn org.gradle.api.tasks.Internal
-dontwarn org.gradle.api.tasks.LocalState
-dontwarn org.gradle.api.tasks.Nested
-dontwarn org.gradle.api.tasks.Optional
-dontwarn org.gradle.api.tasks.OutputDirectory
-dontwarn org.gradle.api.tasks.PathSensitive
-dontwarn org.gradle.api.tasks.PathSensitivity
-dontwarn org.gradle.api.tasks.SkipWhenEmpty
-dontwarn org.gradle.api.tasks.TaskProvider
-dontwarn org.gradle.api.tasks.util.PatternFilterable
-dontwarn org.gradle.work.Incremental
-dontwarn org.gradle.work.NormalizeLineEndings
-dontwarn org.jetbrains.kotlin.library.BaseKotlinLibrary
-dontwarn org.jetbrains.kotlin.library.BaseWriter
-dontwarn org.jetbrains.kotlin.library.IrKotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.IrLibrary
-dontwarn org.jetbrains.kotlin.library.IrWriter
-dontwarn org.jetbrains.kotlin.library.KotlinAbiVersion
-dontwarn org.jetbrains.kotlin.library.KotlinLibrary
-dontwarn org.jetbrains.kotlin.library.KotlinLibraryKt
-dontwarn org.jetbrains.kotlin.library.KotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.KotlinLibraryProperResolverWithAttributes
-dontwarn org.jetbrains.kotlin.library.KotlinLibraryVersioning
-dontwarn org.jetbrains.kotlin.library.MetadataKotlinLibraryLayout
-dontwarn org.jetbrains.kotlin.library.MetadataLibrary
-dontwarn org.jetbrains.kotlin.library.MetadataWriter
-dontwarn org.jetbrains.kotlin.library.SearchPathResolver
-dontwarn org.jetbrains.kotlin.library.SerializedIrModule
-dontwarn org.jetbrains.kotlin.library.SerializedMetadata
-dontwarn org.jetbrains.kotlin.library.UnresolvedLibrary
-dontwarn org.jetbrains.kotlin.library.impl.BaseKotlinLibraryImpl
-dontwarn org.jetbrains.kotlin.library.impl.BaseLibraryAccess
-dontwarn org.jetbrains.kotlin.library.impl.BaseWriterImpl
-dontwarn org.jetbrains.kotlin.library.impl.BuiltInsPlatform
-dontwarn org.jetbrains.kotlin.library.impl.FromZipBaseLibraryImpl
-dontwarn org.jetbrains.kotlin.library.impl.IrLibraryAccess
-dontwarn org.jetbrains.kotlin.library.impl.IrLibraryImpl
-dontwarn org.jetbrains.kotlin.library.impl.IrWriterImpl
-dontwarn org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutForWriter
-dontwarn org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutImpl
-dontwarn org.jetbrains.kotlin.library.impl.MetadataLibraryAccess
-dontwarn org.jetbrains.kotlin.library.impl.MetadataLibraryImpl
-dontwarn org.jetbrains.kotlin.library.impl.MetadataWriterImpl