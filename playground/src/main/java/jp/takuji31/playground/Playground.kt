package jp.takuji31.playground

import com.squareup.kotlinpoet.*

object Playground {
    @JvmStatic
    fun main(args: Array<String>) {
        val packageName = "jp.takuji31.playground"
        val userClassName = ClassName(packageName = packageName, simpleName = "User")
        val userClass = TypeSpec
            .classBuilder(userClassName)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(ParameterSpec
                        .builder("id", Int::class)
                        .build()
                    )
                    .addParameter(ParameterSpec
                        .builder("name", String::class)
                        .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("id", Int::class)
                    .initializer("id")
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("name", String::class)
                    .initializer("name")
                    .build()
            )
            .build()

        val kotlinFile = KotlinFile.builder(packageName, "User")
            .addType(userClass)
            .build()
        print(kotlinFile.toString())
    }
}
