package io.github.detekt

import io.github.detekt.internal.DetektService
import io.github.detekt.internal.info
import io.github.detekt.psi.ABSOLUTE_PATH
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

class DetektComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            ?: MessageCollector.NONE
        val extension = DetektExtension(messageCollector)
        AnalysisHandlerExtension.registerExtension(project, extension)
    }
}

class DetektExtension(private val log: MessageCollector) : AnalysisHandlerExtension {

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        log.info("Starting detekt")
        prepareFiles(files)
        DetektService(log).analyze(files, bindingTrace.bindingContext)
        return null
    }

    private fun prepareFiles(files: Collection<KtFile>) {
        fun KtFile.originalFilePath() = MessageUtil.psiElementToMessageLocation(this)?.path!!
        files.forEach { it.putUserData(ABSOLUTE_PATH, it.originalFilePath()) }
    }
}
