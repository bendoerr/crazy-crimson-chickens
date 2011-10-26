package me.bendoerr.groovy.statemachine

import groovy.lang.MissingPropertyException

class Assertions {

  static assertValidKeys(Map options, Object[] keys) {
    Set invalidKeys = options.keySet() - (keys as Set)
    if(invalidKeys) throw new IllegalArgumentException("Invalid key(s): ${invalidKeys.join(', ')}")
  }
}
