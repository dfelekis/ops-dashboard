package com.marlowefinch.ops;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** GET /api/summary through MockMvc (TODO-233). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class SummaryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void summaryDefaultsToTheLast30DaysAndReturnsKpisPlusTheTwoNames() throws Exception {
        mvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-08-22"))
                .andExpect(jsonPath("$.to").value("2026-09-21"))
                .andExpect(jsonPath("$.onTimeRate").value(0.937))
                .andExpect(jsonPath("$.openTickets").value(114))
                .andExpect(jsonPath("$.revenue").value(360095.5))
                .andExpect(jsonPath("$.orders").value(624))
                .andExpect(jsonPath("$.worstCarrier").value("Kessler Logistics"))
                .andExpect(jsonPath("$.busiestTicketCategory").value("Delivery delay"));
    }

    @Test
    void summaryForAnEmptyRangeHasNullNamesAndZeroCounts() throws Exception {
        mvc.perform(get("/api/summary").param("from", "2020-01-01").param("to", "2020-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2020-01-01"))
                .andExpect(jsonPath("$.to").value("2020-01-31"))
                .andExpect(jsonPath("$.onTimeRate").value((Object) null))
                .andExpect(jsonPath("$.openTickets").value(0))
                .andExpect(jsonPath("$.orders").value(0))
                .andExpect(jsonPath("$.worstCarrier").value((Object) null))
                .andExpect(jsonPath("$.busiestTicketCategory").value((Object) null));
    }

    @Test
    void summaryAcceptsAnExplicitRangeLikeKpis() throws Exception {
        mvc.perform(get("/api/summary").param("from", "2026-07-01").param("to", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").value(679))
                .andExpect(jsonPath("$.revenue").value(480209.5));
    }

    @Test
    void summaryRejectsBadDatesWith400LikeTheOtherEndpoints() throws Exception {
        mvc.perform(get("/api/summary").param("from", "next-tuesday"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("from must be an ISO date (YYYY-MM-DD)"));
        mvc.perform(get("/api/summary").param("from", "2026-09-21").param("to", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("from must be on or before to"));
    }
}
