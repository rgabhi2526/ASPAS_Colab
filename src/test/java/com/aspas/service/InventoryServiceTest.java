package com.aspas.service;

import com.aspas.exception.PartNotFoundException;
import com.aspas.model.dto.SparePartRequestDTO;
import com.aspas.model.entity.SparePart;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.jpa.StorageRackRepository;
import com.aspas.repository.jpa.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// This annotation tells JUnit to enable Mockito!
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    // 1. @Mock creates a "fake" version of your repositories
    @Mock
    private SparePartRepository sparePartRepository;
    
    @Mock
    private StorageRackRepository storageRackRepository;

    @Mock
    private VendorRepository vendorRepository;

    // 2. @InjectMocks creates your Service and injects the fake repos into it
    @InjectMocks
    private InventoryService inventoryService;

    // ---------------------------------------------------------
    // Testing: getPartByNumber()
    // ---------------------------------------------------------
    @Test
    void testGetPartByNumber_Found() {
        // GIVEN: We train the fake database to return a specific part
        SparePart fakePart = new SparePart();
        fakePart.setPartNumber("SP-100");
        fakePart.setPartName("Engine Valve");

        when(sparePartRepository.findByPartNumber("SP-100"))
            .thenReturn(Optional.of(fakePart));

        // WHEN: We call the actual service method
        SparePart result = inventoryService.getPartByNumber("SP-100");

        // THEN: We verify the service handled it correctly
        assertNotNull(result);
        assertEquals("Engine Valve", result.getPartName(), "Should return the correct part name");
    }

    @Test
    void testGetPartByNumber_NotFound_ThrowsException() {
        // GIVEN: We train the fake database to return absolutely nothing
        when(sparePartRepository.findByPartNumber("SP-999"))
            .thenReturn(Optional.empty());

        // WHEN & THEN: We verify that the custom exception is thrown
        assertThrows(PartNotFoundException.class, () -> {
            inventoryService.getPartByNumber("SP-999");
        }, "Should throw PartNotFoundException when part doesn't exist");
    }

    // ---------------------------------------------------------
    // Testing: addPart() Validations
    // ---------------------------------------------------------
    @Test
    void testAddPart_DuplicatePartNumber_ThrowsException() {
        // GIVEN: We train the fake DB to claim this part number is already taken
        when(sparePartRepository.existsByPartNumber("DUPE-123")).thenReturn(true);

        SparePartRequestDTO newPart = new SparePartRequestDTO();
        newPart.setPartNumber("DUPE-123");

        // WHEN & THEN: Verify the service blocks the creation
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.addPart(newPart);
        });

        // Verify the error message is correct
        assertTrue(exception.getMessage().contains("already exists"), "Error message should mention it already exists");
    }
}