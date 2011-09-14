package me.bendoerr.groovy.statemachine

import spock.lang.*

class TrafficLight_TransistionClosure {
  def stateMachine = {
    inital 'stop'

    event 'cycle' transistion { // event('cycle').transistion(closure)
      'stop' to 'proceed'
      'proceed' to 'caution'
      'caution' to 'stop'
    }
  }
}

class TrafficLight2_TransitionMap {
  def stateMachine = {
    inital 'stop'

    event 'cycle' transistion 'stop': 'proceed', 'proceed': 'caution': 'caution': 'stop'
  }
}
