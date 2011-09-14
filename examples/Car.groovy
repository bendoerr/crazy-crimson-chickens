package me.bendoerr.groovy.statemachine.examples

import me.bendoerr.groovy.statemachine

@Mixin(StateMachine)
class Car {
  
  stateMachine inital: 'parked' { // stateMachine([inital: 'parked'], {...}
    event 'park' {
      transition 'idling', 'firstGear' to 'parked' // transition(['idling', 'firstGear']).to('parked')
    }

    event 'ignite' {
      transition 'stalled': same, 'parked': 'idling' // transition(['stalled':same, 'parked': 'idling'])
      transition 'stalled' to same, 'parked' to 'idling' 


}
