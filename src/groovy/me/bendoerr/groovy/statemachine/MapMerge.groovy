package me.bendoerr.groovy.statemachine

class MapMerge {
  static Map merge(Map base, Map top) {
    Map merged = base.clone()
    merged.putAll top
    return merged
  }
}
