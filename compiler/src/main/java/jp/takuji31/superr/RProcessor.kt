package jp.takuji31.superr

import android.content.Context
import com.squareup.kotlinpoet.*
import org.apache.commons.io.FileUtils
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
@SupportedOptions("kapt.kotlin.generated")
class RProcessor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val generatedDir = processingEnv.options["kapt.kotlin.generated"] ?: throw IllegalStateException("SuperR needs kapt support.")
        val pattern = "(.+/build/generated/source/)".toRegex()
        val buildVariant = File(generatedDir).name
        val sourceDir = pattern.find(generatedDir)?.groupValues?.get(0) ?: throw IllegalStateException("SuperR needs gradle build environment")

        val buildConfigDir = FileUtils.getFile(sourceDir, "buildConfig", buildVariant)
        val buildConfigFile = FileUtils.listFiles(buildConfigDir, arrayOf("java"), true).firstOrNull { it.name == "BuildConfig.java" }
        if (buildConfigFile == null) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "BuildConfig.java not found in ${buildConfigDir.absolutePath}")
            return true
        }
        val packageName = buildConfigFile.absolutePath
                .replace(buildConfigDir.absolutePath + File.separator, "")
                .replace(File.separator + "BuildConfig.java", "")
                .replace(File.separator, ".")


        val rClass = roundEnv.rootElements.firstOrNull { (it as? TypeElement)?.qualifiedName?.toString() == packageName + ".R" } ?: return true

        val rKtClassName = ClassName.bestGuess("jp.takuji31.rkt.BaseR")
        val rKtClass = TypeSpec.classBuilder(ClassName.bestGuess("$packageName.RKt"))
            .superclass(rKtClassName)

        rClass.enclosedElements.firstOrNull { element -> element is TypeElement && element.kind == ElementKind.CLASS && element.simpleName.toString() == "drawable" }?.let { drawableClass ->
            val superDrawableClassName = ClassName(
                    packageName = packageName,
                    simpleName = "SuperR",
                    simpleNames = "Drawable"
            )
            val superDrawableClass = TypeSpec.classBuilder(className = superDrawableClassName)
                    .superclass(rKtClassName.nestedClass("Image"))
                    .primaryConstructor(
                            FunSpec.constructorBuilder()
                                    .addParameters(
                                            listOf(
                                                    ParameterSpec.builder("context", Context::class).build(),
                                                    ParameterSpec.builder("id", Int::class).build()
                                            )
                                    )
                                    .build()
                    )
                    .addSuperclassConstructorParameter("context, id")
                    .build()
            val drawableProperty = PropertySpec.builder("drawable", superDrawableClassName, KModifier.PUBLIC)
            drawableProperty.initializer(CodeBlock.of("%T(context = context)", superDrawableClassName))
            rKtClass.addProperty(drawableProperty.build())
            rKtClass.addType(superDrawableClass)
            drawableClass.enclosedElements.forEach { drawableId ->

            }
        }


        KotlinFile.builder(packageName = packageName, fileName = "RKt")
                .addType(rKtClass.build())
                .build()
                .writeTo(FileUtils.getFile(generatedDir, "RKt.kt"))
        return true
    }
}
