package com.harlanhaskins.Waft;

import java.lang.reflect.*;
import java.util.*;

/**
 * Represents an abstract test case. Tests are specified by methods beginning with the word 'test'.
 * Running the tests in a test case will invoke each of the methods, which should call into the expectation APIs.
 * A 'Expectation' is an invariant that's being tested by a given test. A test method may declare as many expectations
 * as they want by calling into the 'expect' family of functions, and if an expectation is not met, it results in a
 * failure recorded within that test, as well as a line number and file to make it easier to find the specific
 * expectation that was broken.
 */
public abstract class TestCase {
    private boolean expectedFail = false;
    private boolean verbose = false;
    private HashMap<String, ArrayList<Result>> results = new HashMap<>();

    /**
     * Runs before each test. Override this method to perform setup before each test, if
     * necessary.
     */
    protected void setUp() {}

    /**
     * Runs after each test. Override this method to undo any changes you made during the test.
     */
    protected void tearDown() {}

    /**
     * Runs all the tests declared in this class. All methods prefixed with 'test' are run in the order
     * they are declared. Do not rely on this order. If a test method throws an unhandled exception, it's
     * recorded as a test failure.
     */
    public final void runTests() {
        runTests(false);
    }

    /**
     * Runs all the tests declared in this class. All methods prefixed with 'test' are run in the order
     * they are declared. Do not rely on this order. If a test method throws an unhandled exception, it's
     * recorded as a test failure.
     */
    public final void runTests(boolean verbose) {
        Method methods[] = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("test"))
                continue;
            String testName = method.getName().substring(4);
            setUp();
            try {
                method.invoke(this);
            } catch (InvocationTargetException e) {
                Result result = resultForException((Exception)e.getCause());
                addResult(testName, result);
            } catch (Exception e) {
                Result result = resultForException(e);
                addResult(testName, result);
            } finally {
                tearDown();
            }
        }
        printResults(verbose);
    }

    /**
     * Converts an exception into a FAIL test result.
     * @param e The exception to convert
     * @return A failing test result.
     */
    private Result resultForException(Exception e) {
        TestFrame frame = getTestFrame();
        String message = "Unhandled " + e.getClass().getName();
        if (e.getMessage() != null) {
            message +=  ": " + e.getMessage();
        }
        return new Result(message, frame, getFailResult());
    }

    /**
     * Prints the results of running all the tests to stdout.
     * Will print specific results for expectations that fail.
     */
    private void printResults() {
        printResults(false);
    }

    /**
     * Prints the results of running all the tests to stdout.
     * Will print specific results for expectations that fail,
     * or all results if passed `true`.
     */
    private void printResults(boolean verbose) {
        System.out.println(getClass().getName() + " Results:");
        for (String test : results.keySet()) {
            System.out.println("  " + test + ":");
            ArrayList<Result> testResults = results.get(test);
            ArrayList<Result> failures = new ArrayList<>();
            ArrayList<Result> passes = new ArrayList<>();
            ArrayList<Result> expectedFailures = new ArrayList<>();
            for (Result result : testResults) {
                switch (result.resultType) {
                    case FAIL:
                        failures.add(result);
                        break;
                    case PASS:
                        passes.add(result);
                        break;
                    case XFAIL:
                        expectedFailures.add(result);
                        break;
                }
            }
            System.out.printf("    %d pass%s, %d failure%s, %d expected failure%s\n",
                    passes.size(), passes.size() == 1 ? "" : "es",
                    failures.size(), failures.size() == 1 ? "" : "s",
                    expectedFailures.size(), expectedFailures.size() == 1 ? "" : "s");
            if (verbose) {
                for (Result result : testResults) {
                    System.out.println("    " + result);
                }
            } else {
                for (Result failure : failures) {
                    System.out.println("    " + failure);
                }
            }
        }
    }

    /**
     * Looks through the call stack for the test method being invoked.
     * Returns the file, name, and line of that method.
     * @param elements The stack trace to look through
     * @return
     */
    private TestFrame getTestFrame(StackTraceElement elements[]) {
        for (StackTraceElement element : elements) {
            if (!element.getMethodName().startsWith("test"))
                continue;
            String name = element.getMethodName().substring(4);
            int line = element.getLineNumber();
            String file = element.getFileName();
            return new TestFrame(file, name, line);
        }
        return null;
    }

    /**
     * Gets a test frame from the current stack trace.
     * @return A TestFrame, or null if it couldn't find a test frame.
     */
    private TestFrame getTestFrame() {
        return getTestFrame(Thread.currentThread().getStackTrace());
    }

    /**
     * @return FAIL or XFAIL depending if we're expecting a failure.
     */
    private TestResultType getFailResult() {
        return expectedFail ? TestResultType.XFAIL : TestResultType.FAIL;
    }

    /**
     * @return TestResultType.PASS
     */
    private TestResultType getSuccessResult() {
        return TestResultType.PASS;
    }

    /**
     * Expect that the given Runnable will produce a fail result.
     * @param r A Runnable that represents test cases.
     */
    protected void expectFailure(Runnable r) {
        expectedFail = true;
        r.run();
        expectedFail = false;
    }

    /**
     * Adds a failing test result if the condition is false.
     * @param condition
     */
    protected void expect(boolean condition) {
        expect(condition, "expect");
    }

    /**
     * Adds a failing test result if the condition is false, using
     * the specified message.
     * @param condition The condition to check
     * @param message The message to append to the result
     */
    protected void expect(boolean condition, String message) {
        TestFrame frame = getTestFrame();
        Result testResult;
        if (!condition) {
            testResult = new  Result(message, frame, getFailResult());
        } else {
            testResult = new Result(message, frame, getSuccessResult());
        }
        addResult(frame.methodName, testResult);
    }

    /**
     * Adds a result for a given test to the results map.
     * @param test The test name to add
     * @param result The result to add
     */
    private void addResult(String test, Result result) {
        ArrayList<Result> _results = results.get(test);
        if (_results == null) {
            _results = new ArrayList<>();
            results.put(test, _results);
        }
        _results.add(result);
    }

    /**
     * Adds a failure result to the results table.
     * @param message The message to assign
     */
    protected void fail(String message) {
        TestFrame frame = getTestFrame();
        addResult(frame.methodName, new Result(message, frame, getFailResult()));
    }

    /**
     * Expects that two arguments will compare equal
     * @param a The first item to compare
     * @param b The second item to compare
     */
    protected <T> void expectEqual(T a, T b) {
        expectEqual(a, b, "expect " + a.toString() + " == " + b.toString());
    }

    /**
     * Expects that two arguments will compare equal
     * @param a The first item to compare
     * @param b The second item to compare
     * @param message The message to display for a failure
     */
    protected <T> void expectEqual(T a, T b, String message) {
        expect(a.equals(b), message);
    }

    /**
     * Expects that two arguments will compare not equal
     * @param a The first item to compare
     * @param b The second item to compare
     */
    protected <T> void expectNotEqual(T a, T b) {
        expectNotEqual(a, b, "expect " + a.toString() + " != " + b.toString());
    }

    /**
     * Expects that two arguments will compare not equal
     * @param a The first item to compare
     * @param b The second item to compare
     * @param message The message to display for a failure
     */
    protected <T> void expectNotEqual(T a, T b, String message) {
        expect(!a.equals(b), message);
    }

    /**
     * Expects that the first argument will compare less than the second.
     * @param a The first item to compare
     * @param b The second item to compare
     */
    protected <T extends Comparable<? super T>> void expectLessThan(T a, T b) {
        expectLessThan(a, b, "expect " + a.toString() + " < " + b.toString());
    }

    /**
     * Expects that the first argument will compare less than the second.
     * @param a The first item to compare
     * @param b The second item to compare
     * @param message The message to display for a failure
     */
    protected <T extends Comparable<? super T>> void expectLessThan(T a, T b, String message) {
        expect(a.compareTo(b) < 0, message);
    }

    /**
     * Expects that the first argument will compare less than or equal to the second.
     * @param a The first item to compare
     * @param b The second item to compare
     */
    protected <T extends Comparable<? super T>> void expectLessThanOrEqual(T a, T b) {
        expectLessThanOrEqual(a, b, "expect " + a.toString() + " <= " + b.toString());
    }

    /**
     * Expects that the first argument will compare less than or equal to the second.
     * @param a The first item to compare
     * @param b The second item to compare
     * @param message The message to display for a failure
     */
    protected <T extends Comparable<? super T>> void expectLessThanOrEqual(T a, T b, String message) {
        expect(a.compareTo(b) <= 0, message);
    }

    /**
     * Expects that the first argument will compare greater than the second.
     * @param a The first item to compare
     * @param b The second item to compare
     */
    protected <T extends Comparable<? super T>> void expectGreaterThan(T a, T b) {
        expectGreaterThan(a, b, "expect " + a.toString() + " > " + b.toString());
    }

    /**
     * Expects that the first argument will compare greater than the second.
     * @param a The first item to compare
     * @param b The second item to compare
     * @param message The message to display for a failure
     */
    protected <T extends Comparable<? super T>> void expectGreaterThan(T a, T b, String message) {
        expect(a.compareTo(b) > 0, message);
    }

    /**
     * Expects that the first argument will compare greater than or equal to the second.
     * @param a The first item to compare
     * @param b The second item to compare
     */
    protected <T extends Comparable<? super T>> void expectGreaterThanOrEqual(T a, T b) {
        expectGreaterThanOrEqual(a, b, "expect " + a.toString() + " >= " + b.toString());
    }

    /**
     * Expects that the first argument will compare greater than or equal to the second.
     * @param a The first item to compare
     * @param b The second item to compare
     * @param message The message to display for a failure
     */
    protected <T extends Comparable<? super T>> void expectGreaterThanOrEqual(T a, T b, String message) {
        expect(a.compareTo(b) >= 0, message);
    }

    /**
     * Expects that the provided runnable will throw that exception
     * @param clazz The exception class that the Runnable should throw
     * @param r The runnable to run
     */
    protected void expectThrows(Class clazz, Runnable r) {
        expectThrows(clazz, r, "expect throws " + clazz.getName());
    }

    /**
     * Expects that the provided runnable will throw that exception
     * @param clazz The exception class that the Runnable should throw
     * @param r The message to display for a failure
     * @param message The message to display for a failure
     */
    protected void expectThrows(Class clazz, Runnable r, String message) {
        try {
            r.run();
            fail(message);
        } catch (Exception e) {
            expect(e.getClass() == clazz, message);
        }
    }
}
