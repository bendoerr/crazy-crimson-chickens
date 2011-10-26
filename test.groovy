
class Module {

    Map mixinMethods = [:]

    void addMethod(String name, Object value) {
        mixinMethods.put(name, value)
    }

    void mix(Object instance) {
        instance.metaClass.invokeMethod << {name, args->
            if(getMixinMethods().containsKey(name)) {
                return getMixinMethods().get(name).call(args)
            }
            def methods = owner.getMetaClass().respondsTo(owner, name, args ?: null)
            if(methods) {
                return args ? owner."$name"(args) : owner."$name"()
            }
            throw new MissingMethodException(name, delegate.class, args)
        }
    }
}

class TestMod extends Module {
    String hello = "world"
    String foobar() { "foobar" }
}

def mod = new TestMod()
mod.addMethod('test') { "abc" }

def o = new Object()
mod.mix(o)

assert "abc" == o.test()
assert "foobar" == o.foobar()
try {
    o.nomethod()
    assert false
} catch (MissingMethodException e) {
    // yay
}

assert "world" == o.hello


