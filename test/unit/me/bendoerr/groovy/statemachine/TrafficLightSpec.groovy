package me.bendoerr.groovy.statemachine

import spock.lang.*

class TrafficLightSpec extends Specification {
  def trafficLight

  def "Traffic Light should have three states."() {
    setup:
      StateMachine.globalMixin()
      trafficLight = new TrafficLight()

    expect:
      trafficLight.getStates() == ['stop', 'proceed', 'caution']
  }

}


class TrafficLight {
  def name

  static stateMachine = Machine.define {
    inital 'stop'
    event 'cycle' transistion {
      from 'stop' to 'proceed'
      from 'proceed' to 'caution'
      from 'caution' to 'stop'
    }
  }
}
