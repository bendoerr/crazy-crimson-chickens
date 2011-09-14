import me.bendoerr.groovy.statemachine

class Assertions {
  void assertValidKeys(Map m, List validKeys) {
    List invalidKeys = m.keys - validKeys
    if (!invalidKeys.isEmpty()) throw new IllegalArgumentException("Invalid key(s): ${invalidKeys.join(', ')}")
  }
}
