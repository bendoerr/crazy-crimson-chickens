package me.bendoerr.groovy.statemachine

class StateMachine {
  

  def ownerClass
  def states
  def initalState

  static StateMachine define(Closure dsl) {
    // TODO Find or create... create only impl now

    Class ownerClass = dsl.owner instanceof Class ? dsl.owner : dsl.owner.class
    return new StateMachine(ownerClass, dsl)
  }

  StateMachine(Class ownerClass, Closure dsl) {
    this.ownerClass = ownerClass
    states = []

    new StateMachineDslSupport(this, dsl).evaluate()
    enhanceClass()
  }

  void enhanceClass() {
    println "Adding getStates to ${ownerClass}"
    ownerClass.metaClass.getStates = {-> states }
  }

  class StateMachineDslSupport {
    StateMachine stateMachine
    Closure dsl

    StateMachineDslSupport(StateMachine sm, Closure dsl) {
      this.stateMachine = sm
      this.dsl = dsl
    }

    void evaluate() {
      dsl.delegate = this
      dsl()
    }

    void inital(String initalState) {
      addState(initalState)
      stateMachine.initalState = initalState
    }

    StateMachineDslSupport event(String event) {
      this
    }

    void transistion(Closure dsl) {
      dsl.delegate = this
      dsl()
    }

    StateMachineDslSupport from(String fromState) {
      addState fromState
      this
    }

    void to(String toState) {
      addState toState
      this
    }


    protected void addState(String state) {
      if(!stateMachine.states.contains(state)) {
        stateMachine.states.add state
      }
    }
  }


}
