package com.bloodlink.controller;

import com.bloodlink.dto.request.CreateDonorRequest;
import com.bloodlink.dto.response.DonorResponse;
import com.bloodlink.service.DonorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DonorController.class)
class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DonorService donorService;

    @Test
    @DisplayName("POST /api/donors should return 201 when valid")
    void testRegisterDonor_Success() throws Exception {
        CreateDonorRequest request = new CreateDonorRequest("Arun Kumar", "arun@example.com", "9876543210",
                1L, "Coimbatore", LocalDate.now().minusDays(100));

        DonorResponse response = new DonorResponse(1L, "Arun Kumar", "arun@example.com", "9876543210",
                1L, "A+", "Coimbatore", LocalDate.now().minusDays(100), true, true, false, 0,
                LocalDate.now().minusDays(10), LocalDateTime.now(), LocalDateTime.now());

        when(donorService.registerDonor(any(CreateDonorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Arun Kumar")))
                .andExpect(jsonPath("$.bloodGroup", is("A+")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    @DisplayName("POST /api/donors should return 400 when validation fails")
    void testRegisterDonor_ValidationFailure() throws Exception {
        CreateDonorRequest request = new CreateDonorRequest("", "invalid-email", "123",
                null, "", null);

        mockMvc.perform(post("/api/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.bloodGroupId").exists())
                .andExpect(jsonPath("$.errors.city").exists());
    }

    @Test
    @DisplayName("GET /api/donors/search should return matching donors")
    void testSearchDonors() throws Exception {
        DonorResponse response = new DonorResponse(1L, "Demo Student", "demo@example.com", "9876543212",
                1L, "O+", "Coimbatore", null, true, true, false, 0,
                LocalDate.now(), LocalDateTime.now(), LocalDateTime.now());

        when(donorService.searchDonors("O+", "Coimbatore")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/donors/search")
                        .param("bloodGroup", "O+")
                        .param("city", "Coimbatore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Demo Student")))
                .andExpect(jsonPath("$[0].bloodGroup", is("O+")))
                .andExpect(jsonPath("$[0].city", is("Coimbatore")));
    }

    @Test
    @DisplayName("GET /api/donors/count-by-blood-group should return map of counts")
    void testGetDonorCountsByBloodGroup() throws Exception {
        Map<String, Long> countMap = Map.of(
                "A+", 12L,
                "A-", 4L,
                "B+", 15L,
                "B-", 3L,
                "AB+", 7L,
                "AB-", 2L,
                "O+", 20L,
                "O-", 5L
        );

        when(donorService.getDonorCountsByBloodGroup()).thenReturn(countMap);

        mockMvc.perform(get("/api/donors/count-by-blood-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['A+']", is(12)))
                .andExpect(jsonPath("$.['O+']", is(20)));
    }
}
