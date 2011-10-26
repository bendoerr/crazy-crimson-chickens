package me.bendoerr.groovy.statemachine

import spock.lang.*

class HelperModuleSpec extends Specification {

  def "Test"() {
    given:
      Object o = new Object()
      o.metaClass.mixin MixMe

    expect:
      Object == o.a()
  }
}

class MixMe {
    static String YAY = "toast"

    def a() {
      return this.class
    }

    def foo_() {
    }
}
