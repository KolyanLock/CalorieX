package org.nikolait.assignment.caloriex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public abstract class UnitTests {

    @Test
    void defaultUnitTest() {
        assertTrue(true, "This is the base class for running all unit tests");
    }

}
