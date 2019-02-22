package com.justai.cm.utils;

public class ExceptionUtils {

    public interface Call<T> {
        T call() throws Exception;
    }

    public interface CallVoid {
        void call() throws Exception;
    }

    public static <T> T wrap(Call<T> r) {
        try {
            return r.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void wrap(CallVoid r) {
        try {
            r.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
