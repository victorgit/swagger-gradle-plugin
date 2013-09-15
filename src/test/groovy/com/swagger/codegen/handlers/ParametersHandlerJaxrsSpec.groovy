package com.swagger.codegen.handlers

import com.swagger.docgen.handlers.ParametersHandler
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiParam
import com.wordnik.swagger.config.SwaggerConfig
import com.wordnik.swagger.model.ApiListing
import groovy.json.JsonBuilder
import scala.Option
import spock.lang.Specification

import com.wordnik.swagger.jaxrs.*
import com.wordnik.swagger.jaxrs.reader.*

import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

class ParametersHandlerJaxrsSpec extends Specification {

    @Api(value = "ApiClass1", description = "ApiClass1desc", produces = "application/xml")
    //@Produces ([MediaType.APPLICATION_JSON])
    @Produces("application/json")
    class ApiClass1 {
        def noAnnotationsMethod(String x) {}
        @ApiOperation(value = "valueSimpleMethod", notes = "notesSimpleMethod")
        def simpleMethod(@ApiParam(value = "descriptionSimpleValue", name = "xSimpleName") String x) {}
        @ApiOperation(value = "value2", notes = "notes2")
        def advancedMethod(@ApiParam(value = "descriptionAdvancedValue", name = "xAdvancedName", required = true, allowableValues = "range[1,10]",
                                     allowMultiple = true) String x) {}
    }

    def getParametersAnnotations(def methodName) {
        ApiClass1.class.getMethods().find{it.name == methodName}.getParameterAnnotations()
    }

    def parametersHandler
    def setup() {
        parametersHandler = new ParametersHandler()
    }



    def convert(c) {

        def fieldRes = [:]

        def decFieldsMap = c.getClass().getDeclaredFields().collectEntries{[it.getName(), it.getType()]}

        decFieldsMap.each {k,v ->

            def val = c.invokeMethod(k, null)

             if (v.name == "scala.collection.immutable.List") {
                 def size = val.size()
                 if (size > 0) {
                     def listResults = []
                     for (int i = 0; i < size; i++) {
                         def result = val.apply(i)
                         if (result instanceof  String) {
                             listResults.add(result)
                         }
                         else {
                             listResults.add(convert(result))
                         }
                     }
                     fieldRes[k] = listResults
                 }
                 else fieldRes[k] = ""
             }
            else if (v.name == "scala.Option") {
                 if (val.toString()=="None")  fieldRes[k] = "NONE"
                 else fieldRes[k] = val.get()
             }
            else fieldRes[k] = val
        }

        /*c.getClass().getDeclaredFields().each {
            fieldRes[it] = c.invokeMethod(it, null)
        }


        if (c.getClass() == List.class) {

        }
        def decFieldsNames = c.getClass().getDeclaredFields().collect{it.getName()}
        decFieldsNames.collect{ fieldRes[it] = c.invokeMethod(it, null)}      */
        fieldRes
    }


    def "test"() {
        when:

        def decFieldsNames = ApiListing.class.getDeclaredFields().collect{it.getName()}


        SwaggerConfig config = new SwaggerConfig("1.0", "1.3", "basePath", "apiPath", null, null)

        JaxrsApiReader reader = new DefaultJaxrsApiReader()

        Option<ApiListing> result = reader.read("root", ApiClass1.class, config)
        ApiListing apiListing = result.get()

        def fieldRes = [:]
        def fields = decFieldsNames.collect{ fieldRes[it] = apiListing.invokeMethod(it, null)}



        //scala.collection.immutable.List apis =
        def z = apiListing.apis().apply(0)
        def res = [:]
        res.apiVersion = apiListing.apiVersion()
        res.swaggerVersion = apiListing.swaggerVersion()

      //  def apisArray = apis.toArray()


        def r2 = convert(apiListing)
        def json = new JsonBuilder(r2)

        def p = apiListing.getProperties()



        then:
        println json.toPrettyString()

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
            result[0].name == "xSimpleName"
            result[0].description == "descriptionSimpleValue"
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
            result[0].name == "xAdvancedName"
            result[0].description == "descriptionAdvancedValue"
            result[0].required == true
            result[0].allowableValues == "range[1,10]"
            result[0].allowMultiple == true
    }
}
