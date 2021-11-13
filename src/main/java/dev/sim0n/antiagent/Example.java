package dev.sim0n.antiagent;

import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Example {
    private static final List<String> BAD_INPUT_FLAGS = Arrays.asList(
            "-javaagent",
            "-agentlib"
    );

    private static final byte[] EMPTY_CLASS_BYTES =
            {
                    -54, -2, -70, -66, 0, 0, 0, 49, 0, 5, 1, 0, 34, 115, 117, 110,
                    47, 105, 110, 115, 116, 114, 117, 109, 101, 110, 116, 47, 73,
                    110, 115, 116, 114, 117, 109, 101, 110, 116, 97, 116, 105, 111,
                    110, 73, 109, 112, 108, 7, 0, 1, 1, 0, 16, 106, 97, 118, 97, 47,
                    108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 7, 0, 3, 0, 1,
                    0, 2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0
            };

    public static void main(String[] args) {
        Optional<String> inputFlag = ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(input -> BAD_INPUT_FLAGS.stream().anyMatch(input::contains))
                .findFirst();

        // if there's a bad input flag present in the vm options
        // then InstrumentationImpl will already have been loaded
        if (inputFlag.isPresent()) {
            throw new IllegalArgumentException(String.format("Bad VM option \"%s\"", inputFlag.get()));
        }

        Unsafe unsafe = Example.getUnsafe();

        unsafe.defineClass("sun.instrument.InstrumentationImpl", EMPTY_CLASS_BYTES, 0, EMPTY_CLASS_BYTES.length, null, null);

        // this is for testing purposes to make sure it's actually loaded
        try {
            Class.forName("sun.instrument.InstrumentationImpl");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");

            unsafeField.setAccessible(true);

            return (Unsafe) unsafeField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
