package com.harlanhaskins.Waft;

/**
 * A class that records the origin (file/line/method) for a given test.
 */
class TestFrame {
    String file;
    String methodName;
    int line;
    TestFrame(String file, String methodName, int line) {
        this.file = file;
        this.methodName = methodName;
        this.line = line;
    }
}
