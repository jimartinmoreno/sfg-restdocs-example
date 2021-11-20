package guru.springframework.sfgrestdocsexample.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgrestdocsexample.domain.Beer;
import guru.springframework.sfgrestdocsexample.repositories.BeerRepository;
import guru.springframework.sfgrestdocsexample.web.model.BeerDto;
import guru.springframework.sfgrestdocsexample.web.model.BeerStyleEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @AutoConfigureRestDocs Annotation that can be applied to a test class to enable and configure auto-configuration of
 * Spring REST Docs.
 * @ExtendWith(RestDocumentationExtension.class) A JUnit Jupiter Extension used to automatically manage the
 * RestDocumentationContext.
 */
@Slf4j
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(BeerController.class)
@ComponentScan(basePackages = "guru.springframework.sfgrestdocsexample.web.mappers")
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerRepository beerRepository;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
//                .apply(documentationConfiguration(restDocumentation))
//                .build();
    }

    @Test
    void getBeerById() throws Exception {
        //given(beerRepository.findById(any())).willReturn(Optional.of(Beer.builder().build()));
        Beer beer = getValidBeer();
        String beerString = objectMapper.writeValueAsString(beer);
        log.info("beer: " + beer);
        log.info("beerString: " + beerString);
        given(beerRepository.findById(any())).willReturn(Optional.of(beer));

        BeerDto beerDto = getValidBeerDto();
        log.info("getValidBeerDto: " + beerDto);
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/beer/{beerId}", UUID.randomUUID().toString())
                        //.param("param1", "value1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(beerString))
                .andExpect(content().json(beerString))
                .andExpect(jsonPath("$['id']").value(beer.getId().toString()))
                .andExpect(jsonPath("$['beerStyle']").value("ALE"))
                //.andDo(print()) // pinta la solicitud
                .andDo(document("v1/beer-get",
                        pathParameters(
                                parameterWithName("beerId").description("UUID of desired beer to get.")
                        ),
                        responseFields(beerResponseDescriptor)
                        //, requestParameters(
                        //        parameterWithName("param1").description("param1 description")
                        // )

                )).andReturn();
        log.info("getResponse():" + mvcResult.getResponse().getContentAsString());
    }

    @Test
    void saveNewBeer() throws Exception {
        given(beerRepository.save(any())).willReturn(getValidBeer());

        BeerDto beerDto = getValidBeerDto();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        mockMvc.perform(post("/api/v1/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(beerDtoJson))
                .andExpect(status().isCreated())
                .andDo(document("v1/beer-new",
                        requestFields(beerRequestContrainedDescriptor),
                        //requestFields(beerRequestDescriptor),
                        responseFields(beerResponseDescriptor)
                ));
    }

    @Test
    void updateBeerById() throws Exception {

        Beer beer = getValidBeer();
        given(beerRepository.findById(any())).willReturn(Optional.of(beer));
        given(beerRepository.save(any())).willReturn(beer);

        BeerDto beerDto = getValidBeerDto();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        ConstraintDescriptions userConstraints = new ConstraintDescriptions(BeerDto.class);
        List<String> descriptions = userConstraints.descriptionsForProperty("beerName");
        descriptions.forEach(System.out::println);

        mockMvc.perform(put("/api/v1/beer/{beerId}", beer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(beerDtoJson))
                .andExpect(status().isNoContent())
                //.andDo(print())
                .andDo(document("v1/beer-update",
                        pathParameters(
                                parameterWithName("beerId").description("UUID of desired beer to get.")
                        ),
                        requestFields(beerRequestContrainedDescriptor)
                        //requestFields(beerRequestDescriptor)
                ));
    }

    private BeerDto getValidBeerDto() {
        return BeerDto.builder()
                .beerName("Nice Ale")
                .beerStyle(BeerStyleEnum.ALE)
                .price(new BigDecimal("9.99"))
                .upc(123123123123L)
                .minOnHand(2)
                .build();

    }

    private Beer getValidBeer() {
        return Beer.builder()
                .beerName("Nice Ale")
                .beerStyle("ALE")
                .price(new BigDecimal("9.99"))
                .id(UUID.randomUUID())
                .upc(123123123123L)
                .minOnHand(2)
                .quantityToBrew(1)
                .build();
    }

    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }

    ConstrainedFields constrainedFields = new ConstrainedFields(BeerDto.class);
    FieldDescriptor[] beerRequestContrainedDescriptor = new FieldDescriptor[]{
            constrainedFields.withPath("id").ignored(),
            constrainedFields.withPath("version").ignored(),
            constrainedFields.withPath("createdDate").ignored(),
            constrainedFields.withPath("lastModifiedDate").ignored(),
            constrainedFields.withPath("beerName").description("Name of the beer"),
            constrainedFields.withPath("beerStyle").description("Style of Beer"),
            constrainedFields.withPath("upc").description("Beer UPC").attributes(),
            constrainedFields.withPath("price").description("Beer Price"),
            constrainedFields.withPath("minOnHand").description("min On Hand"),
            constrainedFields.withPath("quantityToBrew").ignored()
    };

    FieldDescriptor[] beerRequestDescriptor = new FieldDescriptor[]{
            fieldWithPath("id").ignored(),
            fieldWithPath("version").ignored(),
            fieldWithPath("createdDate").ignored(),
            fieldWithPath("lastModifiedDate").ignored(),
            fieldWithPath("beerName").description("beer Name"),
            fieldWithPath("upc").description("upc"),
            fieldWithPath("price").description("price"),
            fieldWithPath("minOnHand").description("min O nHand"),
            fieldWithPath("beerStyle").description("beerStyle description"),
            fieldWithPath("quantityToBrew").ignored()
    };

    private final FieldDescriptor[] beerResponseDescriptor = new FieldDescriptor[]{
            fieldWithPath("id").description("UUID of beer to get"),
            fieldWithPath("version").description("version"),
            fieldWithPath("createdDate").description("created Date"),
            fieldWithPath("lastModifiedDate").description("last Modified Date"),
            fieldWithPath("beerName").description("beer Name"),
            fieldWithPath("upc").description("upc"),
            fieldWithPath("price").description("price"),
            fieldWithPath("minOnHand").description("min O nHand"),
            fieldWithPath("beerStyle").description("beerStyle description"),
            fieldWithPath("quantityToBrew").description("quantity To Brew")
    };
}