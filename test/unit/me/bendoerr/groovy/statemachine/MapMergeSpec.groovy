package me.bendoerr.groovy.statemachine

import spock.lang.*

class MapMergeSpec extends Specification {
  
  def "Maps can be merged"() {
    given:
      Map.mixin MapMerge
    when:
      Map m1 = [a:"A", b:"B"]
      Map m2 = [c:"C", d:"D"]
    then:
      [a:"A", b:"B", c:"C", d:"D"] == m1.merge(m2)
  }

  def "Original maps are not affected by merge"() {
    given:
      Map.mixin MapMerge
    when:
      Map m1 = [a:"A"]
      Map m2 = [z:"Z"]
      m1.merge m2
    then:
      ([a:"A"] == m1) && ([z:"Z"] == m2)
  }

}
