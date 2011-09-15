package me.bendoerr.groovy.statemachine

class Assertions {
  class MissingStateMachineDslException extends MissingPropertyException {
    MissingStateMachineDslException(Object o) {
      message = "An object of class ${o.class} was configured to use StateMachine but had no 'stateMachine' property."
    }
  }

  void assert_stateMachineDsl(Object o) {
    if(!o.stateMachine) throw new MissingStateMachineDslException(o)
  }
}
