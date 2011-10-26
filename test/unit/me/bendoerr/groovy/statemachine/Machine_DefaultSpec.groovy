package me.bendoerr.groovy.statemachine

import spock.lang.*

class Machine_DefaultSpec extends Specification {
  Class clazz = Object.class
  Machine machine = new Machine(clazz)
  Object object = new Object()

  def "The machine should have an owner class"() {
    assert clazz == machine.ownerClass
  }

  def "The machine should use the default name of 'state'"() {
    assert 'state' == machine.name
  }

  def "The machine should use the default attribute name of 'state'"() {
    assert 'state' == machine.attribute
  }

  def "Custom attributes should be prefixed with the attribute name"() {
    assert 'state_event' == machine.attribute('event')
  }

  def "The machine should have an initial state"() {
    assert null != machine.initialState(object)
  }

  def "The initial state's value should be null"() {
    assert null == machine.initialState(object).value
  }

}
