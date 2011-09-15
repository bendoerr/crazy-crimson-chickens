package me.bendoerr.groovy.statemachine

import groovy.lang.MissingPropertyException

class Assertions {
  class MissingStateMachineDslException extends MissingPropertyException {
    MissingStateMachineDslException(Object o) {
      message = "An object of class ${o.class} was configured to use StateMachine but had no StateMachine property."
    }
  }

  static StateMachineDslPresent(Object o) {
    if(!o.properties.contains('stateMachine')) throw new MissingStateMachineDslException(o)
  }
}
