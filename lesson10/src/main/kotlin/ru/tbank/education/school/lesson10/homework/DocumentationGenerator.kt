package ru.tbank.education.school.lesson10.homework

import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object DocumentationGenerator {
    fun generateDoc(obj: Any): String {
        var doc: String
        var props = ""
        var methods = ""
        val ref = obj::class

        if (ref.hasAnnotation<InternalApi>()) {
            return "Документация скрыта (InternalApi)."
        }
        if (!ref.hasAnnotation<DocClass>()) {
            return "Нет документации для класса."
        }

        val params = ref.findAnnotation<DocClass>()!!
        doc = "=== Документация: ${ref.simpleName} ===\n" +
                "Описание: ${params.description}\n"

        if (params.author.isNotEmpty()) {
            doc += "Автор: ${params.author}\n"
        }
        if (params.version.isNotEmpty()) {
            doc += "Версия: ${params.version}\n"
        }
        val hiddenPropertyNames = mutableSetOf<String>()
        ref.memberProperties.forEach { property ->
            if (property.hasAnnotation<InternalApi>()) {
                hiddenPropertyNames.add(property.name)
            } else {
                props += "- ${property.name}\n"
                if (property.hasAnnotation<DocProperty>()) {
                    val values = property.findAnnotation<DocProperty>()!!
                    props += "  Описание: ${values.description}\n"
                    if (values.example.isNotEmpty()) {
                        props += "  Пример: ${values.example}\n"
                    }
                }
                props += "\n"
            }
        }
        ref.declaredMemberFunctions.forEach { method ->
            if (method.hasAnnotation<InternalApi>()) {
                return@forEach
            }
            var isBad = false
            if (ref.isData) {
                isBad = when (method.name) {
                    "equals" -> method.parameters.size == 2 &&
                            method.parameters[1].type.classifier == Any::class &&
                            method.returnType.classifier == Boolean::class
                    "hashCode" -> method.parameters.size == 1 &&
                            method.returnType.classifier == Int::class
                    "toString" -> method.parameters.size == 1 &&
                            method.returnType.classifier == String::class
                    "copy" -> {
                        val constructorParamCount = ref.primaryConstructor?.parameters?.size ?: 0
                        method.parameters.size == constructorParamCount + 1
                    }
                    else -> method.name.startsWith("component") &&
                            method.parameters.size == 1 &&
                            method.returnType.classifier != Unit::class
                }
            }

            if (!isBad) {


                if (method.parameters.any { param ->
                        param.name != null && param.name in hiddenPropertyNames
                    }) {
                    return@forEach
                }

                methods += "- ${method.name}(${method.parameters.joinToString(", ") { param ->
                    "${param.name}: ${param.type.toString().split('.').last()}"}}\n"
                if (method.hasAnnotation<DocMethod>()) {
                    val values = method.findAnnotation<DocMethod>()!!
                    if (values.description.isNotEmpty()) {
                        methods += "  Описание: ${values.description}\n"
                    }
                    if (method.parameters.isNotEmpty()) {
                        methods += "  Параметры:\n"
                        method.parameters.forEach { param ->
                            methods += "    - ${param.name}: "
                            if (param.hasAnnotation<DocParam>()) {
                                methods += param.findAnnotation<DocParam>()!!.description + "\n"
                            } else {
                                methods += "Нет описания\n"
                            }
                        }
                    }
                    methods += "  Возвращает: ${values.returns}\n"
                } else {
                    if (method.parameters.isNotEmpty()) {
                        methods += "  Параметры:\n"
                        method.parameters.forEach { param ->
                            methods += "    - ${param.name}: Нет описания\n"
                        }
                    }
                    methods += "  Возвращает: Нет описания\n"
                }
                methods += "\n"
            }
        }
        return doc + (if (props.isNotEmpty()) "\n--- Свойства ---\n$props" else "") + (if (methods.isNotEmpty()) "\n--- Методы ---\n$methods" else "")
    }
}