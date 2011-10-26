package me.bendoerr.groovy.statemachine

/**
 * TODO
 */
@Mixin([Assertions, EvalHelper])
class Machine implements Cloneable {

  static {
    Map.mixin MapMerge
  }

  /**
   * Try to find or create a state machine for the given class.
   *
   * If a machine of a given name already exists in one of the class's
   * superclasses, then a copy of that machine will be created and stored in
   * the new owner class (the original will remain unchanged).
   */
  static Machine findOrCreate(Class ownerClass, Object[] args) {
    List listArgs = args as List

    Closure dsl = listArgs.last() instanceof Closure ? listArgs.pop() : null
    List options = listArgs.last() instanceof Map ? listArgs.pop() : []
    String name = listArgs.first() ?: 'state'

    Machine machine

    // Find an existing machine
    if(ownerClass.respondsTo('stateMachines')) {
      machine = ownerClass.stateMachines[name]
    }

    if(machine) {
      // If this is a subclass and changes are being made create a new copy.
      if(machine.ownerClass != ownerClass && (options.any {it} || dsl)) {
        machine = machine.clone()
        if(options.containsKey('inital')) machine.initalState = options.inital
        machine.ownerClass = ownerClass
      }

      // Evaluate the DSL
      if(dsl) machine.with dsl
    } else {
      // No existing machine, create a new one.
      machine = new Machine(ownerClass, name, options, dsl)
    }

    return machine
  }

  private String name
  private EventCollection events
  private StateCollection states
  private Map callbacks
  private String action
  private String namespace
  private Boolean useTransistions

  /**
   * If false and any of the dynamic methods that would be metaClassed onto
   * the ownerClass would override an existing method, do not override. If set
   * to true, override the method. Default is false.
   */
  Boolean ignoreMethodConflicts = false

  /**
   * The class this machine is defined on.
   */
  Class ownerClass

  /**
   * The name of the machine, used for scoping methods generated for the
   * machine as a whole (not states or events).
   */
  String getName() { name }

  /**
   * The events that trigger transistions. These are sorted, by default,
   * in the order in which they are defined.
   */
  EventCollection getEvents() { events }

  /**
   * A list of all the states known to this state machine. States will be added
   * from the following sources.
   * <ul>
   *  <li> Inital state
   *  <li> State behaviors
   *  <li> Event transitions (to, from, exceptFrom options)
   *  <li> Transition callbacks (to, from, exceptFrom, exceptTo options)
   *  <li> Unreferenced states (using <i>otherStates</i> helper)
   * </ul>
   * These are sorted by default in the order they were referenced.
   */
  StateCollection getStates() { states }

  /**
   * The callbacks to invoke before/after a transistion or on the failure of a
   * transition.
   *
   * <p>Maps before: callbacks, after: callbacks, failure: callbacks.</p>
   */
  Map getCallbacks() { callbacks }

  /**
   * The action (method/closure name) to invoke when an object transitions.
   */
  String getAction() { action }

  /**
   * An identifier that forces all methods (including state predicates and 
   * event methods) to be generated with the value prefixed or suffixed,
   * depending on the context.
   */
  String getNamespace() { namespace }

  /**
   * If the machine will use transactions when firing events.
   */
  Boolean getUseTransactions() { useTransactions }

  Machine(Class ownerClass, Object[] args = new Object[0], Closure dsl = null) {
    List argsList = args as List

    Map options = argsList.last instanceof Map ? argsList.pop() : [:]
    assertValidKeys(options, 'attribute', 'initial', 'initialize', 'action', 'plural', 'namespace', 'integration', 'messages', 'useTransactions')

    // Find an integration that matches this machine's owner class
    integration = options.integration ? Integrations.findByName(options.integration) : Integrations.match(ownerClass)

    if(integration) {
      metaClass.mixin integration
      options = (integration.defaults ?: [:]).merge options
    }

    // Add machine wide defaults
    options = [useTransactions: true, initialize: true].merge options

    // Set machine configuration
    name = argsList.first() ?: 'state'
    attribute = options.attribute ?: name
    events = new EventCollection(this)
    states = new StateCollection(this)
    callbacks = [before: [], after: [], failure: []]
    namespace = options.namespace
    action = options.action
    useTransactions = options.useTransactions
    initalizeState = options.initalize
    setOwnerClass(ownerClass)
    if(!siblingMachines.any()) initalState = options.initalState

    // Merge with sibling machine configurations
    addSiblingMachineConfigs()

    // Define class integration
    defineHelpers()
    defineScopes(options.plural)
    afterInitalize()

    // Evaluate the dsl
    if(dsl) with dsl
  }

  /**
   * Clone this machine so that modifications to it or it's collections
   * (events, states, callbacks) will not affect the original machine.
   */
  Machine clone(Machine orig) {
    def clone = super.clone()
    clone.events = events.clone()
    clone.events.machine = clone
    clone.states = states.clone()
    clone.states.machine = clone
    clone.callbacks = [
        before: callbacks.before.clone(),
        after: callbacks.after.clone(),
        failure: callbacks.failure.clone()]
    return clone
  }

  /**
   * Sets the class which is the owner of this state machine. Any methods
   * generated by states, events, or other parts of the machine will be defined
   * on the given owner class.
   */
  void setOwnerClass(Class clazz) {
    ownerClass = clazz
    if(ownerClass.metaClass.respondsTo('getClassIsExtended')) {
      ownerClass.metaClass.mixin Extensions
      if(initalizeState) {
        defineStateInitalizer()
      }
    }

    ownerClass.stateMachines[name] = this
  }

  void setInitialState(Object newInitialState) {
    initialState = newInitialState
    if(!dynamicInitialState) {
      addStates([initialState])
    }

    states.each {state-> state.initial = (state.name == initialState)}
  }

  State initialState(Object object) {
    if(initialState != null) {
      return states.fetch(dynamicInitialState ? evaluateMethod(object, initialState) : initialState)
    }
  }

  Boolean isDynamicInitialState() {
      return initialState instanceof Closure
  }

  void initializeState(Object object, Map options = [:]) {
      State state = initialState(object)
      if(state && (options.force || shouldInitializeState(object))) {
        value = state.value
        if(options.to) {
          options.to.put(attribute.toString(), value)
        } else {
          write(object, 'state', value)
        }
      }
  }

  String getAttribute(String name = 'state') {
    name == 'state' ? attribute : "${this.name}${name.capitalize()}"
  }

  void defineHelper(scope, method, Object[] args, Closure c) {
    // TODO
  }

  Object state(Object[] args) {
    List argsList = args as List

    Closure stateDsl argsList.last() instanceof Closure ? argsList.pop() : {}
    Map options = argsList.last() instanceof Map ? argsList.pop() : [:]
    assertValidKeys(options, 'value', 'cache', 'if', 'humanName')

    def states = addStates(argsList)
    states.each {state->
      if(options.containsKey('value')) {
        state.value = options.value
        this.states.update(state)
      }

      if(options.containsKey('humanName')) state.humanName = options.humanName
      if(options.containsKey('cache')) state.cache = options.cache
      if(options.containsKey('if')) state.matcher = options.if
      state.context(stateDsl)
    }

    states.length() == 1 ? states.first() : states
  }

  Object otherStates(Object[] args) {
      state(args)
  }

  Object read(Object object, String attribute) {
    attribute = this.attribute(attribute)
    //object.invokeMethod("get${attribute.capitalize()}", null)
    object."${attribute}"
  }

  void write(Object object, String attribute, Object value) {
    attribute = this.attribute(attribute)
    //object.invokeMethod("set${attribute.capitalize()", value)
    object."${attribute}" = value
  }


}
