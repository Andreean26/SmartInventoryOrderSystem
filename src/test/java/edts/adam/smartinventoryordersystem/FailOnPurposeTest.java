package edts.adam.smartinventoryordersystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FailOnPurposeTest {

    @Test
    @DisplayName("TEST SENGAJA GAGAL - buktikan CI/CD berhenti jika test fail")
    void thisTestMustFail() {
        assertEquals(1, 2, "SENGAJA GAGAL: membuktikan CI/CD pipeline berhenti jika unit test failed");
    }
}

