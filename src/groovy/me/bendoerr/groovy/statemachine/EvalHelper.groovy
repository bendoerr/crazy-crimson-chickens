package me.bendoerr.groovy.statemachine

class EvalHelper {

  Object evaluateMethod(Object o, Object[] args, Closure c) {
    Object origDelegate = c.delegate
    Integer origResolveStrat = c.resolveStrategy

    c.delegate = o
    c.resolveStrategy = Closure.DELEGATE_ONLY

    Object result = c(args)

    c.delegate = origDelegate
    c.resolveStrategy = origResolveStrat

    return result
  }
}
