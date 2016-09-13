package com.harlanhaskins.Waft;

import java.util.Optional;

/**
 * Represents the internal result type of a single test
 */
enum TestResultType {
    PASS, FAIL, XFAIL
}

/**
 * Represents a single result for a test case. It holds an optional message,
 * an optional test frame, and a result type.
 */
class Result {
    Optional<String> message;
    Optional<TestFrame> frame;
    TestResultType resultType;

    /**
     * Creates a new result with the specified parameters.
     * @param message A nullable message.
     * @param frame A nullable test frame.
     * @param resultType A result type.
     */
    Result(String message, TestFrame frame, TestResultType resultType) {
        this.message = Optional.ofNullable(message);
        this.resultType = resultType;
        this.frame = Optional.ofNullable(frame);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(resultType.toString());
        message.ifPresent((String message) -> {
            builder.append(": ");
            builder.append(message);
            builder.append(" ");
        });
        frame.ifPresent((TestFrame frame) -> {
            builder.append("(");
            builder.append(frame.file);
            builder.append(", line ");
            builder.append(frame.line);
            builder.append(")");
        });
        return builder.toString();
    }
}
