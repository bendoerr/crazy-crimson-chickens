package me.bendoerr.groovy.statemachine

class Module {

    Map mixinMethods = [:]

    void addMethod(String name, Object value) {
        mixinMethods.put name, value
    }

    void mix(Object instance) {
        instance.metaClass.invokeMethod << {name, args->

    }
}
