package me.bendoerr.groovy.statemachine

import spock.lang.*

@Mixin(Assertions)
class AssertionsSpec extends Specification {
  Map map = [foo: "oof", bar: "rab"]

  def "assertValidKeys throws IllegalArgumentException for invalid keys."() {
    when:
      assertValidKeys(map, keys as Object[])

    then:
      thrown(IllegalArgumentException)

    where:
      keys << [
          ['foo'],
          ['bar'],
          ['oof', 'rab'],
          ['baz', 'foo'],
          ['bar', 'baz']]
  }

  def "assertValidKeys is OK with valid keys"() {
    when:
      assertValidKeys(map, keys as Object[])

    then:
      notThrown(IllegalArgumentException)

    where:
      keys << [
          ['foo', 'bar'],
          ['foo', 'bar', 'baz'],
          ['baz', 'bar', 'foo']]
  }

}
