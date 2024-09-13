package io.codefresh.gradleexample;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.codefresh.gradleexample.config.OffsetBasedPageRequest;
import io.codefresh.gradleexample.controller.TenderController;
import io.codefresh.gradleexample.entity.Tender;
import io.codefresh.gradleexample.entity.TenderServiceType;
import io.codefresh.gradleexample.service.TenderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GradleExampleApplicationTest {

	@Test
	public void contextLoads() {
		assertEquals("Expected correct message","Hello World","Hello "+"World");
	}

	private MockMvc mockMvc;

	@Mock
	private TenderService tenderService;

	@InjectMocks
	private TenderController tenderController;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(tenderController).build();
	}

	@Test
	public void testGetTendersSuccess() throws Exception {
		// Mocking the service response
		Tender tender1 = new Tender();
		Tender tender2 = new Tender();
		tender1.setServiceType(TenderServiceType.Delivery);
		tender2.setServiceType(TenderServiceType.Construction);

		Pageable pageable = new OffsetBasedPageRequest(0, 5);
		when(tenderService.findAll(Arrays.asList(TenderServiceType.Construction, TenderServiceType.Delivery), pageable))
				.thenReturn(Arrays.asList(tender1, tender2));

		// Perform GET request
		mockMvc.perform(get("/tenders")
						.param("service_type", "Construction", "Delivery")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$[0].serviceType").value("Delivery"))
				.andExpect(jsonPath("$[1].serviceType").value("Construction"))
				.andDo(print());
	}
}
