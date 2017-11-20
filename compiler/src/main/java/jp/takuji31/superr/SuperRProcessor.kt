package jp.takuji31.superr

import org.apache.commons.io.FileUtils
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
@SupportedOptions("kapt.kotlin.generated")
class SuperRProcessor() : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val generatedDir = processingEnv.options["kapt.kotlin.generated"] ?: throw IllegalStateException("SuperR needs kapt support.")
        val pattern = "(.+/build/generated/source/)".toRegex()
        val buildVariant = File(generatedDir).name
        val sourceDir = pattern.find(generatedDir)?.groupValues?.get(0) ?: throw IllegalStateException("SuperR needs gradle build environment")

        val buildConfigDir = FileUtils.getFile(sourceDir, "buildConfig", buildVariant)
        val buildConfigFile = FileUtils.listFiles(buildConfigDir, arrayOf("java"), true).firstOrNull { it.name == "BuildConfig.java" }
        if (buildConfigFile == null) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "BuildConfig.java not found in ${buildConfigDir.absolutePath}")
            return false
        }
        val packageName = buildConfigFile.absolutePath
                .replace(buildConfigDir.absolutePath + File.separator, "")
                .replace(File.separator + "BuildConfig.java", "")
                .replace(File.separator, ".")


        val rClass = roundEnv.rootElements.firstOrNull { (it as? TypeElement)?.qualifiedName?.toString() == packageName + ".R" } ?: return false

        return true
    }
}
