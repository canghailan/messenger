package cc.whohow.messenger.util;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Text {
    private static final Pattern COMMA = Pattern.compile(",");

    public static boolean isBlank(String string) {
        if (isEmpty(string)) {
            return true;
        }
        return string.trim().isEmpty();
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    public static String trimToEmpty(String string) {
        if (string == null) {
            return "";
        }
        return string.trim();
    }

    public static Set<String> splitAsSet(String csv) {
        if (csv == null || csv.isEmpty()) {
            return Collections.emptySet();
        }
        return COMMA.splitAsStream(csv)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
