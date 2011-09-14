def foo = {Object[] l->
  l.inject 0 {s,i-> s += i }
}

String.metaClass.getSize = { delegate.size() }

foo '123'.size, '12'.size
