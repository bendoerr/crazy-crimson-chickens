package me.bendoerr.groovy.statemachine

class Extensions {
  static Boolean classIsExtended = true
  static MachineCollection stateMachines = new MachineCollection()

  /**
   * Gets the current list of state machines defined for the class. This static
   * variable trys to act like an inheritable variable. The variable is
   * avialable to each subclass, each having a copy of its superclass's 
   * values.
   */
  static MachineCollection getStateMachines() {
    def superClassStateMachines = [:] // TODO figure out a way to reflect up and get the class if there is one.
    return stateMachines << superClassStateMachines
  }

  /**
   * Runs one or more events in parallel. All events will run though the
   * following steps:
   * <ul>
   *   <li>Before callbacks</li>
   *   <li>Persist state</li>
   *   <li>Invoke action</li>
   *   <li>After callbacks</li>
   * </ul>
   *
   * <p>For example, if two events (for state machines A and B) are run in
   * parallel, the order in which the steps are run is:
   * <ul>
   *   <li>A - Before transistion callbacks</li>
   *   <li>B - Before transistion callbacks</li>
   *   <li>A - Persist new state</li>
   *   <li>B - Persist new state</li>
   *   <li>A - Invoke action</li>
   *   <li>B - Incoke action (only if different that A's action)</li>
   *   <li>A - After transistion callbacks</li>
   *   <li>B - After transistion callbacks</li>
   * </ul></p>
   *
   * <p><i>Note</i> that multiple events on the same state machine cannot be run
   * in parallel. This this is attempted an IllegalArgumentException will be
   * thrown.</p>
   *
   * <p><b>Halting Callbacks</b></p>
   *
   * <p>When running multiple events in parallel special consideration should be
   * given with regard to how halting within callbacks affects the flow.</p>
   *
   * <p>For <i>before</i> callbacks, <tt>halt</tt> error that is thrown will
   * immediately cancel the perform for all transitions. As a result it is
   * possible for one event's transition to affect the continuation of
   * another.</p>
   *
   * <p>On the other hand any <tt>halt</tt> errot that is thrown from an
   * <i>after</i> callback will only affect that event's transition. Other
   * transitions will continue to run their own callbacks.
   *
   * <p><b>Examples</b></p>
   * <pre><code>
   *  class Vehicle {
   *    static driveMachine = stateMachine [inital: 'parked'], {
   *      event('ignite') {
   *        transition 'parked' to 'idling'
   *      }
   *      event('park') {
   *        transition 'idling' to 'parked'
   *      }
   *    }
   *    static alarmMachine = stateMachine 'alarm_state', [namespace:'alarm', inital:'on'], {
   *      event('enable') {
   *        transistion 'all' to 'on'
   *      }
   *      event('disable') {
   *        transistion 'all' to 'off'
   *      }
   *    }
   *  }
   *
   *  def vehicle = new Vehicle()                   // > Vehicle@234b3a
   *  vehicle.state                                 // > 'parked'
   *  vehicle.alarmState                            // > 'on'
   *
   *  vehicle.fireEvents('ignite', 'disableAlarm')  // > true
   *  vehicle.state                                 // > 'idling'
   *  vehicle.alarmState                            // > 'off'
   *
   *  // If any event fails the entire event chain fails.
   *  vehicle.fireEvents('ignite', 'enableAlarm')   // > false
   *  vehicle.state                                 // > 'idling'
   *  vehicle.alarmState                            // > 'off'
   * </code></pre>
   */
  Boolean fireEvents(List events) {
    stateMachines.fireEvents(this.class, events)
  }

  /**
   * Run one or more events in parallel. If any event fails to run, then a
   * InvalidTransition exception will be thrown.
   * <p>
   * See {@link Extensions.fireEvents}
   */
  Boolean fireEvents_(List events) {
    fireEvents(events << true) || throw new InvalidParallelTransistion(this, events)
  }

  void initializeStateMachines(Map options = [:], Closure c) {
    stateMachines.initializeStates(this, options, c)
  }
}
