package formulAI.project.bank.banqueCredit.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsExposeTousLesGroupesDEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/clients']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/clients/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/simuler']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/soumettre']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/annuler']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/analyser']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/accepter']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/refuser']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/demandes/{id}/historique']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/dashboard']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }

    @Test
    void swaggerUiEstAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}

