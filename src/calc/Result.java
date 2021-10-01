package calc;

import java.util.function.Consumer;
import java.util.function.Function;

class Result<T> {
    private T value;
    private String message;

    private Result(T value, String message) {
        this.value = value;
        this.message = message;
    }

    static <T> Result<T> of(T value) {
        return new Result<>(value, null);
    }

    static <T> Result<T> error(String message) {
        return new Result<>(null, message);
    }

    <R> Result<R> map(Function<T, R> mapper) {
        if (value != null) {
            return Result.of(mapper.apply(value));
        }
        return Result.error(message);
    }

    <R> Result<R> andThen(Function<T, Result<R>> mapper) {
        if (value != null) {
            return mapper.apply(value);
        }
        return Result.error(message);
    }

    void ifValue(Consumer<T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    void consume(Consumer<T> actionIfValue, Consumer<String> actionIfError) {
        if (value != null) {
            actionIfValue.accept(value);
        } else {
            actionIfError.accept(message);
        }
    }
}
