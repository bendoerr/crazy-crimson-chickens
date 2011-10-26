package me.bendoerr.groovy.statemachine

import groovy.util.logging.Commons
import groovy.transform.AutoClone

/**
 * A state defines a value that an attribute can be after being transitioned 0
 * or more times. States can represent any type of value, though the most
 * common and default is type is String.
 * <p>
 * In addition to defining the machine's value, a state can also define a
 * behavioral context for an object when that object is in the state. See
 * {@link Machine.state} for more information about how state driven 
 * behavior can be utilized.
 */
@Commons
@AutoClone
@Mixin(Assertions)
class State {
  private Machine machine
  private String name
  private String qualifiedName
  private Object humanName
  private Object value
  private Map<String, Closure> methods

  /**
   * The machine on which this state is defined
   */
  Machine machine

  /**
   * The unique identifier for the state used in event and callback definitions
   */
  String getName() { return name }

  /**
   * The fully-qualified identifier for the state, scoped by the machine's
   * namespace.
   */
  String getQualifiedName() { return qualifiedName }

  /**
   * The human-readable name for this state. Can be a Closure that expects
   * this state and this state's machine's owner class.
   */
  void setHumanName(Object name) { humanName = name }

  /**
   * If a closure calls it to transform into the human readable format.
   */
  String getHumanName(Class clazz = machine.ownerClass) {
    humanName instanceof Closure ? humanName(this, class) : humanName
  }

  /**
   * The value that is written to a machine's attribute when an object
   * transitions into this state
   */
  void setValue(Object value) { this.value = value }

  /**
   * If this states value should be cached after being evaluated
   */
  Boolean cache

  /**
   * If this state is the initial state for new objects
   */
  Boolean initial

  /**
   * A closure for determining if a given value matches this state
   */
  Closure matcher

  /**
   * Tracks all of the methods that have been defined for the machine's owner
   * class when objects are in this state
   */
  Map<String, Closure> getMethods() { return methods }

  /**
   * Create a new state within the given context of a machine.
   * <p>
   * Configuration options:
   * <ul>
   *  <li><tt>initial</tt> - If this state is the beginning state for the
   *      machine. Default is false.</li>
   *  <li><tt>value</tt> - The value to store when an object transistions
   *      to this state. Default is the name (String).</li>
   *  <li><tt>cache</tt> - If a dynamic value via a closure is being used then
   *      setting this to true will cache the evaluated result.</li>
   *  <li><tt>if</tt> - Determins if a value matches this state. By default the
   *      configured value is matched.</li>
   *  <li><tt>humanName</tt> - The human readable version of this state's name.
   */
  State(Machine machine, String name, Map options) {
    assertValidKeys(options, 'initial', 'value', 'cache', 'if', 'humanName')

    this.machine = machine
    this.name = name
    qualifiedName = machine.namespace && name ? "${machine.namespace}_$name" : name
    humanName = options.humanName ?: (name ?: 'null')
    value = options.value ?: name
    cache = options.cache
    matcher = options.if
    methods = [:]
    initial = options.initial

    if(name) {
      def conflictingMachines = machine.ownerClass.stateMachines.findAll {name, otherMachine->
        otherMachine != machine && otherMachines.states.get('qualifiedName', qualifiedName)
      }

      // Output a warning if another machine has a conflicting qualified name
      // for a different attribute
      def conflict = conflictingMachines.find {name, otherMachine->
        otherMachine.attribute != machine.attribute
      }
      if(conflict) {
        Machine cMachine = conflict.value
        log.warn "State $qualifiedName for $machine.name is already defined in $cMachine.name"
      } else if (!conflictingMachines) {
        // Only bother adding predicates when another machine for the same name
        // already hasn't done so
        addPredicate()
      }
    }
  }

  /**
   * Determins if there are any states that can be transistioned to from this
   * state. If there are none, then this state is considered <i>final</i>.
   * Any objects in a final state will remain so forever given the current
   * machines definition.
   */
  Boolean isFinal() {
    machine.events.any {event->
      events.branches.any {branch->
        branch.stateRequirements.any {requirement->
          requirement.matches(name) && !requirement.to.matches(name, from: name)
        }
      }
    }
  }

  /**
   * Generates a human readable description of this state's name / value.
   * <p>
   * For example:
   * <p>
   * new State(machine, 'parked').description                        == "parked"
   * new State(machine, 'parked', value: 'parked').description       == "parked"
   * new State(machine, 'parked', value: null).description           == "parked (null)"
   * new State(machine, 'parked', value: 1).description              == "parked (1)"
   * new State(machine, 'parked', value: { new Date() }).description == "parked (*)"
   */
  String getDescription() {
    String desc = name ?: name.inspect()
    if(name.toString() != value.toString()) {
      desc += " (${value instanceof Closure ? '*' : value.inspect()})"
    }
    return desc
  }

  /**
   * The value that represents this state. If the value is a closure this will
   * optionally evaulate that closure. Otherwise, the static value is returned.
   * <p>
   * For example:
   * <p>
   * new State(machine, 'parked', value: 1).value                        == 1
   * new State(machine, 'parked', value: { new Date() }.value            == Sat Oct 08 21:43:20 EDT 2011
   * new State(machine, 'parked', value: { new Date() }.getValue(false)  == test_script$_closure1@7a9224
   */
  Object getValue(Boolean eval = true) {
    if(value instanceof Closure && eval) {
      if(cache) {
        value = value.call()
        machine.states.update(this)
        return value
      }
      return value
    }
    return value
  }

  /**
   * Determins whether the state matches the given value. If no matcher is
   * configured, then this will check if the values are equal. Otherwise the
   * matcher will dertermine the result.
   * <p>
   * For example:
   * <p>
   * // Without a matcher
   * def state = new State(machine, 'parked', value: 1)
   * state.matches(1) == true
   * state.matches(2) == false
   *
   * // With a matcher
   * def state = new State(machine, 'parked', value: 1, if: {value-> value != null })
   * state.matches(null) == false
   * state.matches('a') == true
   */
  Boolean matches(Object otherValue) {
    matcher ? matcher.call(otherValue) : otherValue == value
  }

  /**
   *
   */
  StateContext context(Closure block) {
    // TODO
  }

  Object call(object, method, methodMissing = null, Object[] args, Closure c) {
      // TODO
  }

  String inspect() {
      this.toString()
  }

  String toString() {
      Map attributes = [name: name, value: value, initial: initial, context: methods.keys]
      "$<${this.class} ${attributes.collect {k,v-> "${k}=${v.inspect()}"}.join(' ')}>"
  }

  private void add
}

