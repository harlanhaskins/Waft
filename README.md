# Waft
A dead-simple testing framework for Java.

Waft tests are expressed as test methods that expose various expectations
about the code they're testing.
For example, if you want to test a shopping cart, you would check

```java
ShoppingCart cart = new ShoppingCart();
cart.add(new Gallon<Milk>());
expectEqual(cart.getItemCount(), 1, "cart has one item after insertion");
```
