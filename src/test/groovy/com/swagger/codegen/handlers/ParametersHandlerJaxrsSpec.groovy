package com.swagger.codegen.handlers

import com.swagger.docgen.handlers.ParametersHandler
import com.wordnik.swagger.annotations.ApiParam
import spock.lang.Specification

class ParametersHandlerJaxrsSpec extends Specification {

    class ApiClass1 {
        def noAnnotationsMethod(String x) {}
        def simpleMethod(@ApiParam(value = "description", name = "x") String x) {}
        def advancedMethod(@ApiParam(value = "description", name = "x", required = true, allowableValues = "range[1,10]",
                                     allowMultiple = true) String x) {}
    }

    def getParametersAnnotations(def methodName) {
        ApiClass1.class.getMethods().find{it.name == methodName}.getParameterAnnotations()
    }

    def parametersHandler
    def setup() {
        parametersHandler = new ParametersHandler()
    }

    def "test method with no annotations"() {
        setup:
            def methodName = "noAnnotationsMethod"
            def parametersAnnotations = getParametersAnnotations(methodName)

        when:
            def result = parametersHandler.handleParameters(parametersAnnotations)
        then:
            println "Result for method $methodName is $result"
            result != null
    }

    def "test simple method"() {
        setup:
            def methodName = "simpleMethod"
            def parametersAnnotations = getParametersAnnotations(methodName)

        when:
            def result = parametersHandler.handleParameters(parametersAnnotations)
        then:
            println "Result for method $methodName is $result"
            result != null
            result.size() == 1
            result[0].name == "x"
            result[0].description == "description"
            result[0].required == false
            result[0].allowableValues == null
            result[0].allowMultiple == false
    }

    def "test advanced method"() {
        setup:
            def methodName = "advancedMethod"
            def parametersAnnotations = getParametersAnnotations(methodName)
        when:
            def result = parametersHandler.handleParameters(parametersAnnotations)
        then:
            println "Result for method $methodName is $result"
            result != null
            result.size() == 1
            result[0].name == "x"
            result[0].description == "description"
            result[0].required == true
            result[0].allowableValues == "range[1,10]"
            result[0].allowMultiple == true
    }
}
