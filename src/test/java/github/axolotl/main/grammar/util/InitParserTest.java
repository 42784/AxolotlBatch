package github.axolotl.main.grammar.util;

import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class InitParserTest {

    @org.junit.jupiter.api.Test
    void parseStringAppend() {
        AtomicReference<String> atomicReference = new AtomicReference<>();

        atomicReference.set("(a+b+(c+d))");
        InitParser.parseStringAppend(atomicReference);System.out.println("atomicReference = " +atomicReference.get());
        atomicReference.set("=a+b");
        InitParser.parseStringAppend(atomicReference);System.out.println("atomicReference = " +atomicReference.get());
        atomicReference.set("a+b;");
        InitParser.parseStringAppend(atomicReference);System.out.println("atomicReference = " +atomicReference.get());
        atomicReference.set("$Str0+1");
        InitParser.parseStringAppend(atomicReference);System.out.println("atomicReference = " +atomicReference.get());
        atomicReference.set("{#name+$Str0}");
        InitParser.parseStringAppend(atomicReference);System.out.println("atomicReference = " +atomicReference.get());
        atomicReference.set("{$Str0+#name}");
        InitParser.parseStringAppend(atomicReference);System.out.println("atomicReference = " +atomicReference.get());
    }
}