Feature: Karate operations

  Scenario: karate map operation
    * def fun = function(x){ return x * x }
    * def list = [1, 2, 3]
    * def res = karate.map(list, fun)
    * match res == [1, 4, 9]

  Scenario: convert an array into a different shape
    * def before = [{ foo: 1 }, { foo: 2 }, { foo: 3 }]
    * def fun = function(x){ return { bar: x.foo } }
    * def after = karate.map(before, fun)
    * match after == [{ bar: 1 }, { bar: 2 }, { bar: 3 }]

  Scenario: convert array of primitives into array of objects
    * def list = [ 'Bob', 'Wild', 'Nyan' ]
    * def data = karate.mapWithKey(list, 'name')
    * match data == [{ name: 'Bob' }, { name: 'Wild' }, { name: 'Nyan' }]

  Scenario: karate filter operation
    * def fun = function(x){ return x % 2 == 0 }
    * def list = [1, 2, 3, 4]
    * def res = karate.filter(list, fun)
    * match res == [2, 4]

  Scenario: forEach works even on object key-values, not just arrays
    * def keys = []
    * def vals = []
    * def idxs = []
    * def fun =
    """
    function(x, y, i) {
      karate.appendTo(keys, x);
      karate.appendTo(vals, y);
      karate.appendTo(idxs, i);
    }
    """
    * def map = { a: 2, b: 4, c: 6 }
    * karate.forEach(map, fun)
    * match keys == ['a', 'b', 'c']
    * match vals == [2, 4, 6]
    * match idxs == [0, 1, 2]

  Scenario: filterKeys
    * def schema = { a: '#string', b: '#number', c: '#boolean' }
    * def response = { a: 'x', c: true }
    # very useful for validating a response against a schema "super-set"
    * match response == karate.filterKeys(schema, response)
    * match karate.filterKeys(response, 'b', 'c') == { c: true }
    * match karate.filterKeys(response, ['a', 'b']) == { a: 'x' }

  Scenario: merge
    * def foo = { a: 1 }
    * def bar = karate.merge(foo, { b: 2 })
    * match bar == { a: 1, b: 2 }

  Scenario: append
    * def foo = [{ a: 1 }]
    * def bar = karate.append(foo, { b: 2 })
    * match bar == [{ a: 1 }, { b: 2 }]

  Scenario: sort
    * def foo = [{a: { b: 3 }}, {a: { b: 1 }}, {a: { b: 2 }}]
    * def fun = function(x){ return x.a.b }
    * def bar = karate.sort(foo, fun)
    * match bar == [{a: { b: 1 }}, {a: { b: 2 }}, {a: { b: 3 }}]
    * match bar.reverse() == [{a: { b: 3 }}, {a: { b: 2 }}, {a: { b: 1 }}]