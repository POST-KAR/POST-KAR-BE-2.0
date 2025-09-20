package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.explorer.ExplorerCategoryResponse;
import com.postkar.project3dmodel.service.ExplorerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExplorerController.class)
public class ExplorerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExplorerService explorerService;

    @Test
    @WithMockUser
    public void testGetCategories() throws Exception {
        // Mock data
        ExplorerCategoryResponse category1 = new ExplorerCategoryResponse();
        category1.setId("1");
        category1.setName("Paisa Bolta Hai");
        category1.setDescription("Discover AR stories in currency");
        category1.setItemCount(7);
        category1.setStatus("available");

        ExplorerCategoryResponse category2 = new ExplorerCategoryResponse();
        category2.setId("2");
        category2.setName("Famous Paintings");
        category2.setDescription("Art comes alive");
        category2.setItemCount(0);
        category2.setStatus("coming_soon");

        List<ExplorerCategoryResponse> categories = Arrays.asList(category1, category2);

        when(explorerService.getAllCategories()).thenReturn(categories);

        // Test the endpoint
        mockMvc.perform(get("/api/v1/explorer/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Paisa Bolta Hai"))
                .andExpect(jsonPath("$[0].itemCount").value(7))
                .andExpect(jsonPath("$[0].status").value("available"))
                .andExpect(jsonPath("$[1].name").value("Famous Paintings"))
                .andExpect(jsonPath("$[1].status").value("coming_soon"));
    }

    @Test
    @WithMockUser
    public void testSearchEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/explorer/search")
                .param("q", "currency")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
