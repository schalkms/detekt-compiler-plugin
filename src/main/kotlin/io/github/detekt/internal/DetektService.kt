package io.github.detekt.internal

import io.github.detekt.tooling.api.DetektProvider
import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.tooling.api.spec.RulesSpec
import io.gitlab.arturbosch.detekt.api.UnstableApi
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

class DetektService(private val log: MessageCollector) {

    @OptIn(UnstableApi::class)
    fun analyze(files: Collection<KtFile>, context: BindingContext) {
        val spec = ProcessingSpec {
            rules {
                maxIssuePolicy = RulesSpec.MaxIssuePolicy.AllowAny // TODO: why does detekt not catch this?
            }
        }
        val detekt = DetektProvider.load().get(spec)
        val result = detekt.run(files, context)
        log.info("${files.size} files analyzed")
        result.container?.let { log.reportFindings(it) }
        result.error?.let { throw it }
    }
}
