import addatude.serialization.Validation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ValidationTest {
    static class InvalidStrings implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of("nam\u0001"));
            list.add(Arguments.of("\u0001this is bad"));
            list.add(Arguments.of("Sri\u0001ng"));
            list.add(Arguments.of("Sri\nng"));
            list.add(Arguments.of("Sring\n"));
            list.add(Arguments.of((Object) null));
            list.add(Arguments.of(Stream.generate(() -> "a").limit(100000).collect(Collectors.joining())));
            return list.stream();
        }
    }

    static class ValidStrings implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of(""));
            list.add(Arguments.of("test"));
            list.add(Arguments.of("this is a longer name"));
            list.add(Arguments.of(Stream.generate(() -> "a").limit(99999).collect(Collectors.joining())));
            return list.stream();
        }
    }

    @DisplayName("invalidString tests")
    @Nested
    class invalidStringTests{
        @DisplayName("Valid Strings")
        @ParameterizedTest(name = "str = {0}")
        @ArgumentsSource(ValidStrings.class)
        public void validStrings(String str){
            Assertions.assertFalse(Validation.isInvalidString(str));
        }

        @DisplayName("Invalid Strings")
        @ParameterizedTest(name = "str = {0}")
        @ArgumentsSource(InvalidStrings.class)
        public void invalidStrings(String str){
            Assertions.assertTrue(Validation.isInvalidString(str));
        }
    }

    @DisplayName("invalidNumbers tests")
    @Nested
    class invalidNumbersTests{
        @DisplayName("Valid Numbers")
        @ParameterizedTest(name = "num = {0}")
        @ValueSource(longs = {0, 1, 123, 99999})
        public void validStrings(long num){
            Assertions.assertFalse(Validation.isInvalidNumber(num));
        }

        @DisplayName("Invalid Numbers")
        @ParameterizedTest(name = "num = {0}")
        @ValueSource(longs = {-1, -10000, 100000})
        public void invalidStrings(long num){
            Assertions.assertTrue(Validation.isInvalidNumber(num));
        }
    }

    @DisplayName("invalidLongitude tests")
    @Nested
    class invalidLongitudeTests{
        @DisplayName("Valid Longitude")
        @ParameterizedTest(name = "lng = {0}")
        @ValueSource(strings = {"1.1", "180.0", "-180.0", "0.0", "-0.1"})
        public void validLongitude(String str){
            Assertions.assertFalse(Validation.isInvalidLongitude(str));
        }

        @DisplayName("Invalid Longitude")
        @ParameterizedTest(name = "lng = {0}")
        @ValueSource(strings = {"bad", "180.01", "-180.1", "0", "75", "1.x"})
        public void invalidLongitude(String str){
            Assertions.assertTrue(Validation.isInvalidLongitude(str));
        }
    }

    @DisplayName("invalidLatitude tests")
    @Nested
    class invalidLatitudeTests{
        @DisplayName("Valid Latitude")
        @ParameterizedTest(name = "lat = {0}")
        @ValueSource(strings = {"1.1", "90.0", "-90.0", "0.0", "-0.1"})
        public void validLatitude(String str){
            Assertions.assertFalse(Validation.isInvalidLatitude(str));
        }

        @DisplayName("Invalid Latitude")
        @ParameterizedTest(name = "lat = {0}")
        @ValueSource(strings = {"bad", "90.01", "-90.1", "0", "75", "1.x"})
        public void invalidLatitude(String str){
            Assertions.assertTrue(Validation.isInvalidLatitude(str));
        }
    }
}
