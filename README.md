# Waft ![Build Status](https://travis-ci.org/harlanhaskins/Waft.svg?branch=master)
A dead-simple testing framework for Java.

Waft tests are expressed as test methods that expose various expectations
about the code they're testing.
For example, if you want to test a shopping cart, you would check

```java
class ShoppingCartTests {
  void testPutItem() {
    ShoppingCart cart = new ShoppingCart();
    cart.add(new Gallon<Milk>());
    expectEqual(cart.getItemCount(), 1, "cart has one item after insertion");
    expectNotEqual(cart.getItemCount(), 1, "cart does not have one item after insertion");
  }
}
```

`new ShoppingCartTests().runTests()` would print:

```
com.mypackage.ShoppingCartTests Results:
  PutItem:
    1 pass, 1 failure, 0 expected failures
    FAIL: expect 1 != 1 (ShoppingCartTests.java, line 6)
```

There are lots of expectations to choose from, and you can build
complex expectations by calling into the existing mechanism.

## Built-in expectations

- `expect`
- `expectEqual`
- `expectNotEqual`
- `expectLessThan`
- `expectLessThanOrEqual`
- `expectGreaterThan`
- `expectGreaterThanOrEqual`
- `expectThrows`
- `expectFailure`

## Author
Harlan Haskins ([@harlanhaskins](https://github.com/harlanhaskins))

## License
This project is released under the MIT license, a copy of which is included
in this repository.
