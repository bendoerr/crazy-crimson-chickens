package me.bendoerr.groovy.statemachine

import groovy.util.logging.Commons

/**
 * A state machine is a model of behavior composed of states, events, and
 * transistions. This helper class adds support for defining this type of
 * functionality on any Groovy class.
 */
@Commons
class StateMachine {
  /**
   * Creates a new state machine with the given name. The default name, if not
   * given is 'state'.
   * <p>
   * <b>Configuration Options</b>
   * <ul>
   *   <li> <tt>attribute</tt> - TODO The name of the attribute to store the state value in
   *        By default this is the same as the name of the machine.</li>
   *   <li> <tt>inital</tt> - TODO The inital value of the attribute. This can be a static
   *        value or closure to be evaluated at runtime. The closure is passed the
   *        model instance. Default is null.</li>
   *   <li> <tt>initialize</tt> - TODO Whether to automatically initialize the attribute by
   *        wrapping the constructor on the owner class. Default is true.</li>
   *   <li> <tt>action</tt> - TODO The instance method to invoke when an object transitions.
   *        Default is null unless otherwise specified by the implementation.</li>
   *   <li> <tt>integration</tt> - TODO The name of the integration to use for adding libaray
   *        specific behavior to the machine. Built-in integrations: TODO</li>
   * </ul>
   *
   * <p>This also expects a closure which will be used to configure the actual
   * states, events and transistions for the state machine.</p>
   * <hr />
   * <p><b>Examples</b></p>
   * 
   * <p><b>With defaults and no configuration:</b>
   * <pre><code>  class Vehicle {
   *    static machine = StateMachine.stateMachine {
   *      event 'park' {
   *        // transitions
   *      }
   *    }
   *  }</code></pre></p>
   * 
   * <p>The above example will define a state machine named 'state' that will store
   * that value in the 'state' attribute. Every vehicle will start without an
   * inital state.</p>
   *
   * <p><b>With custom name / attribute:</b>
   * <pre><code>  class Vehicle {
   *    static machine = StateMachine.stateMachine 'status', [attribute: 'statusValue'], {
   *      // stuff
   *    }
   *  }</code></pre></p>
   *
   * <p><b>With static inital state:</b>
   * <pre><code>  class Vehicle {
   *    static machine = StateMachine.stateMachine [inital: 'parked'], {
   *      // dsl
   *    }
   *  }</code></pre></p>
   *
   * <p><b>With dynamic inital state:</b>
   * <pre><code>  class Vehicle {
   *    static machine = StateMachine.stateMachine [inital: { it.speed == 0 ? 'parked' : 'idling'}], {
   *      // you know
   *    }
   *  }</code></pre></p>
   */
  Machine stateMachine(Object[] args, Closure dsl) {
    Class ownerClass = dsl.owner instanceof Class ? dsl.owner : dsl.owner.class
    Machine.findOrCreate(ownerClass, args, dsl)
  }

  Machine stateMachine(Closure dsl) {
    stateMachine([] as Object[], dsl)
  }

  /**
   * TODO
   */
  static globalMixin() {
      log.debug "Mixing StateMachine into GroovyObject and Class."
      GroovyObject.metaClass.mixin StateMachine
      Class.metaClass.mixin StateMachine
  }

}
