package com.marlowefinch.ops;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The stand-up line: the four KPIs plus the worst carrier and busiest ticket category. Names are null for an empty range. */
public record Summary(
        LocalDate from,
        LocalDate to,
        Double onTimeRate,
        long openTickets,
        BigDecimal revenue,
        long orders,
        String worstCarrier,
        String busiestTicketCategory) {
}
