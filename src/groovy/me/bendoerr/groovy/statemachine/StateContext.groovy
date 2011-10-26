package me.bendoerr.groovy.statemachine

@Mixin(Assertions)
class StateContext {
  private Machine machine
  private State state
  private Closure condition

  Machine getMachine() { machine}

  State getState() { state }

  StateContext(State state) {
    this.state = state
    machine = state.machine
    condition = {o->
      o.class.stateMachine(machine.name).states.matches(o, state.name)
    }
  }

  void transition(Map options) {
    assertValidKeys(options, 'to', 'on', 'if', 'unless')
    if(!options.to && !options.on) {
      throw new IllegalStateException("Must specify 'to' state and 'on' event.")
    }

    use(MapMerge) {
      machine.transistion(options.merge(from: state.name))
    }
  }

  Object methodMissing(String name, Object[] args) {
    Map options = [:]
    if(args.last() instanceof Map) {
      options = args.last()
    } else {
      args = ((args as List) << options) as Object[]
    }

    def ifCondition = options.remove('if')
    def unlessCondition = options.remove('unless')

    // TODO
  }
}
