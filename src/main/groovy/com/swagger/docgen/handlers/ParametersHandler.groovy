package com.swagger.docgen.handlers

import com.wordnik.swagger.annotations.ApiParam

import java.lang.annotation.Annotation

class ParametersHandler {

    def handleParameters(Annotation[][] annotations) {
        annotations.findAll {validParameter(it)}
                   .collect {handleParameters(it)}
    }

    def validParameter(Annotation[] annotations) {
        annotations.find{it.annotationType() == ApiParam.class} != null
    }

    def handleParameters(Annotation[] annotations)  {
        def result = [:]
        ApiParam apiParam = annotations.find{it.annotationType() == ApiParam.class}
        if (apiParam != null) {
            if (apiParam.value()) result.description = apiParam.value()
            if (apiParam.allowableValues()) result.allowableValues = apiParam.allowableValues()
            result.name = apiParam.name()
            result.required = apiParam.required()
            result.allowMultiple = apiParam.allowMultiple()
        }
        result
    }


}
