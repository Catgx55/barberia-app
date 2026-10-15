package barberia_api.barberia_api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ApiExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void mapsMissingResourcesToNotFound() throws Exception {
        mvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cita con id 99 no existe."));
    }

    @Test
    void mapsBusinessConflictsToConflict() throws Exception {
        mvc.perform(get("/test-errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @RestController
    static class ErrorTestController {

        @GetMapping("/test-errors/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Cita", 99L);
        }

        @GetMapping("/test-errors/conflict")
        void conflict() {
            throw new ConflictException("Horario ocupado.");
        }
    }
}
