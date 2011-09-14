package me.bendoerr.groovy.statemachine.examples

import me.bendoerr.groovy.statemachine

@Mixin(StateMachine)
class TrafficLight {
  def stateMachine = {
    initial "stop"

    event "cycle" {"
      transition "stop": "proceed",
                 "proceed": "caution",
                 "caution": "stop"
    }
  }
}
