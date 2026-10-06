package com.marlowefinch.ops;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * A closed date range for the query endpoints.
 *
 * Both bounds default to "the last 30 days ending today". {@link #resolve} validates the
 * request parameters (TODO-232); the record itself does not, so callers that build a
 * range directly are not checked.
 */
public record DateRange(LocalDate from, LocalDate to) {

    public static final int DEFAULT_DAYS = 30;
    public static final int MAX_SPAN_DAYS = 366;

    public static DateRange resolve(String from, String to, Clock clock) {
        List<String> errors = new ArrayList<>();
        DateRange range = resolve(from, to, clock, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return range;
    }

    /** Adds one message per problem to {@code errors}; returns null if any were added. */
    static DateRange resolve(String from, String to, Clock clock, List<String> errors) {
        int before = errors.size();
        LocalDate today = LocalDate.now(clock);
        LocalDate start = parse("from", from, today.minusDays(DEFAULT_DAYS), errors);
        LocalDate end = parse("to", to, today, errors);
        if (errors.size() > before) {
            return null;
        }
        if (start.isAfter(end)) {
            errors.add("from must be on or before to");
            return null;
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_SPAN_DAYS) {
            errors.add("date range must not exceed " + MAX_SPAN_DAYS + " days");
            return null;
        }
        return new DateRange(start, end);
    }

    private static LocalDate parse(String name, String value, LocalDate fallback, List<String> errors) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            errors.add(name + " must be an ISO date (YYYY-MM-DD)");
            return null;
        }
    }
}
