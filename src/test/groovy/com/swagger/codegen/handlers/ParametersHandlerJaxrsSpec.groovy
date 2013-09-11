package com.swagger.codegen.handlers

import com.swagger.docgen.handlers.ParametersHandler
import spock.lang.Specification

class ParametersHandlerJaxrsSpec extends Specification {

    class ApiClass1 {
        def noAnnotationsMethod(String x) {}
        def simpleMethod(String x) {}
    }

    def "test method with no annotations"() {
        setup:
            def parametersHandler = new ParametersHandler()
            def parametersAnnotations = ApiClass1.class.getMethods().find{it.name =="noAnnotationsMethod"}.getParameterAnnotations()

        when:
            def result = parametersAnnotations.collect{ parametersHandler.handleParameters(it)}
        then:
            println "Result is $result"
            result != null
    }
}
