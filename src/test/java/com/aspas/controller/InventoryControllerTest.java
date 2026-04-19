package com.aspas.controller;

import com.aspas.model.dto.SparePartRequestDTO;
import com.aspas.model.entity.SparePart;
import com.aspas.service.InventoryService;
import com.aspas.service.JITService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext; 
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest tells Spring to ONLY load the web layer
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    // THIS IS THE FIX FOR OPTION 1
    // It gives Spring a fake database mapping so the JPA Auditing doesn't crash the web test
    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    private MockMvc mockMvc; 

    @MockBean
    private InventoryService inventoryService; 

    @MockBean
    private JITService jitService; 

    @Autowired
    private ObjectMapper objectMapper; 

    // ---------------------------------------------------------
    // Testing: GET /api/parts
    // ---------------------------------------------------------
    @Test
    void testGetAllParts_Returns200OK() throws Exception {
        // GIVEN: We train our fake service to return one part
        SparePart fakePart = new SparePart();
        fakePart.setPartNumber("SP-001");
        fakePart.setPartName("Brake Pad");
        
        when(inventoryService.getAllParts()).thenReturn(Arrays.asList(fakePart));

        // WHEN & THEN: We send a GET request and check the JSON response
        mockMvc.perform(get("/api/parts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Checks for HTTP 200
                .andExpect(jsonPath("$[0].partNumber").value("SP-001"))
                .andExpect(jsonPath("$[0].partName").value("Brake Pad"));
    }

    // ---------------------------------------------------------
    // Testing: POST /api/parts
    // ---------------------------------------------------------
    @Test
    void testAddPart_Returns201Created() throws Exception {
        // GIVEN: We create a fake incoming JSON request
        SparePartRequestDTO newPartRequest = new SparePartRequestDTO();
        newPartRequest.setPartNumber("SP-002");
        newPartRequest.setPartName("Oil Filter");
        newPartRequest.setUnitPrice(15.99);

        // We create what the service SHOULD return
        SparePart savedPart = new SparePart();
        savedPart.setPartNumber("SP-002");
        savedPart.setPartName("Oil Filter");

        when(inventoryService.addPart(any(SparePartRequestDTO.class))).thenReturn(savedPart);

        // WHEN & THEN: We send a POST request and verify it succeeds
        mockMvc.perform(post("/api/parts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPartRequest))) // Translates DTO to JSON
                .andExpect(status().isCreated()) // Checks for HTTP 201
                .andExpect(jsonPath("$.partNumber").value("SP-002"));
    }
}