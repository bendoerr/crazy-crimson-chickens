package me.bendoerr.groovy.statemachine

@Mixin(Assertions)
class Machine {
  static defaultMessages
  static ignoreMethodConflicts

  static findOrCreate(ownerClass, args, dsl) {}  
  static draw(classNames, options)

  Machine(Class ownerClass, List args, Closure dsl) {
    Map options = args.last instanceof Map ? args.pop() : [:]
    assertValidKeys(options, 'attribute', 'initial', 'initialize', 'action', 'plural', 'namespace', 'integration', 'messages', 'useTransaction')
    
    Class integration

    if(options.integration) {
      integration = Integrations.findByName(options.integration)
    } else {
      integration = Integrations.match(ownerClass)
    }

    if(integration) {
      metaClass.mixin integration
      options = (integration.DEFAULTS ?: []).putAll options // Merge
    }

    // Add machine-wide defaults
    options = [useTransactions: 'true', initialize: true].putAll options // Merge

    // Set machine configuration
    name = args.first ?: 'state'
    attribute = options.attribute ?: name
    events = new EventCollection()
    states = new StateCollection()
    callbacks = [before: [], after: [], failure: []] // TODO Values are closures or lists?
    namespace = options.namespace
    messages = options.messages ?: messages
  }

  String name
  String attribute
  EventCollection events
  StateCollection states
  Map callbacks
  String namespace
}
