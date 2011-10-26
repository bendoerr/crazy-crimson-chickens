package me.bendoerr.groovy.statemachine

/**
 * An error occured during a state machine invocation
 */
class StateMachineException extends RuntimeException {
  private Object object

  /**
   * The object that failed
   */
  Object getObject() {
    object
  }

  StateMachineException(Object o, String message) {
    super(message)
    object = o
  }
}

/**
 * An invalid transistion was attempted
 */
class InvalidTransistionException extends StateMachineException {
  private Machine machine
  private def from
  private def event
  private def fromState

  InvalidTransistionException(Object o, Machine m, def e) {
    machine = m
    fromState = machine.states.match(o)
    from = machine.read(o, 'state')
    event = machine.events.fetch(e)
    
    super(o, "Cannot transistion to ${machine.name} via '${event}' from ${fromName}")
  }

  /**
   * The machine attempting to be transistioned
   */
  Machine getMachine() { machine }

  /**
   * The current state value for the machine
   */
  String getFrom() { from }

  /**
   * The event that triggered the failure
   */
  String getEvent() { event.name }

  /**
   * The fully qualified name of the event that triggered the failure
   */
  String getQualifiedEvent() { event.qualifiedName }

  /**
   * The name for the current state
   */
  String getFromName() { fromState.name }

  /**
   * The fully qualified name of the current state
   */
  String getQualifiedFromName() { fromState.qualifiedName }
}

/**
 * A set of transistion failled to run in parallel
 */
class InvalidParallelTransistion extends StateMachineException {
  private List events

  InvalidParallelTransistion(Object o, List e) {
    events = e

    super(o, "Cannot run events in parallel ${events.join(', ')}")
  }

  List getEvents() { events }
}


class MissingStateMachineDslException extends MissingPropertyException {
  MissingStateMachineDslException(Object o) {
    message = "An object of class ${o.class} was configured to use StateMachine but had no StateMachine property."
  }
}
