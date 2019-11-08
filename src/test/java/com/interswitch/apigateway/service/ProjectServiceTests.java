package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.*;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
import com.interswitch.apigateway.repository.MongoUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {ProjectService.class})
public class ProjectServiceTests {

    @Autowired
    private ProjectService projectService;

    @MockBean
    private PassportService passportService;

    @MockBean
    private MongoProjectRepository mongoProjectRepository;

    @MockBean
    private MongoProductRepository mongoProductRepository;

    @MockBean
    private MongoResourceRepository mongoResourceRepository;

    @MockBean
    private MongoUserRepository mongoUserRepository;

    private ArgumentCaptor<PassportClient> passportClientArgumentCaptor;

    private PassportClient passportClient;

    private ProjectData projectData;

    private Project project;

    private Resource resource;

    private Product product, productOne;

    private Set<String> resourceIds;

    private ProductRequest productRequest;

    private Map<String, LinkedHashSet<String>> resources;

    private String projectOwner = "project.owner";

    @BeforeEach
    public void setup() {
        passportClient = new PassportClient();
        passportClient.setClientId("clientId");
        passportClient.setClientName("project.name");
        passportClient.setDescription("test project description");
        passportClient.setScope(Set.of("profile"));
        passportClient.setClientSecret("secret");
        passportClient.setLogoUrl("http://logoUrl");
        passportClient.setResourceIds(new LinkedHashSet<>(Set.of("api-gateway", "passport")));
        passportClient.setAuthorizedGrantTypes(Set.of("authorization_code", "client_credentials"));
        passportClient.setRegisteredRedirectUri(Set.of("http://redirectUrl"));
        passportClient.setClientOwner(projectOwner);
        passportClient.setAutoApproveScopes(Collections.emptySet());
        passportClient.setAuthorities(Collections.emptyList());
        passportClient.setAdditionalInformation(new LinkedHashMap<>(Map.of("api_resources", new ArrayList<>(Collections.singletonList("resourceId-GET/path")))));
        passportClient.setAccessTokenValiditySeconds(1800);
        passportClient.setRefreshTokenValiditySeconds(1209600);

        product = new Product();
        product.setName("product");
        product.setDescription("description");
        product.setAudiences(Set.of("audienceOne", "audienceTwo"));

        productOne = new Product();
        productOne.setName("productOne");
        productOne.setDescription("description");
        productOne.setAudiences(Set.of("audienceThree", "audienceFour"));

        resourceIds = new HashSet<>();
        resourceIds.addAll(product.getAudiences());
        resourceIds.addAll(productOne.getAudiences());

        resource = new Resource();
        resource.setId("resourceId");
        resource.setName("resourceName");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        resource.setProduct(product);

        project = new Project();
        project.setId("testprojectone");
        project.setName("project.name");
        project.setOwner(projectOwner);
        project.setType(Project.Type.web);
        project.addResource(resource);
        project.setClientId("testClientId", Env.TEST);

        projectData = new ProjectData();
        projectData.setId("testprojectone");
        projectData.setName("project.name");
        projectData.setType(Project.Type.web);
        projectData.setDescription("test project description");
        projectData.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code, GrantType.client_credentials));
        projectData.setRegisteredRedirectUris(Set.of("http://redirectUrl"));
        projectData.setLogoUrl("http://logoUrl");
        projectData.setOwner(projectOwner);
        projectData.setClients(Map.of(Env.TEST, "testClientId"));
        projectData.setResources(Set.of(resource));

        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.setId("resourceId");
        resourceRequest.setName("resourceName");

        productRequest = new ProductRequest();
        productRequest.setName("product");
        productRequest.setDescription("description");
        productRequest.setResources(Set.of(resourceRequest));

        resources = Map.of("resources", new LinkedHashSet(Collections.singleton("resourceId")));

        passportClientArgumentCaptor = ArgumentCaptor.forClass(PassportClient.class);
    }

    @Test
    public void testGetAllProjects() {
        when(mongoProjectRepository.findByOwner(project.getOwner())).thenReturn(Flux.fromIterable(Collections.singleton(project)));
        when(passportService.getPassportClients(projectOwner, Env.TEST)).thenReturn(Flux.fromIterable(Collections.singleton(passportClient)));
        StepVerifier.create(projectService.getAllProjects(projectOwner)).assertNext(p -> {
            assertThat(p.getId()).isEqualTo(projectData.getId());
            assertThat(p.getName()).isEqualTo(projectData.getName());
            assertThat(p.getType()).isEqualTo(projectData.getType());
            assertThat(p.getDescription()).isEqualTo(projectData.getDescription());
            assertThat(p.getOwner()).isEqualTo(projectData.getOwner());
            assertThat(p.getLogoUrl()).isEqualTo(projectData.getLogoUrl());
            assertThat(p.getAuthorizedGrantTypes()).isEqualTo(projectData.getAuthorizedGrantTypes());
            assertThat(p.getRegisteredRedirectUris()).isEqualTo(projectData.getRegisteredRedirectUris());
            assertThat(p.getClients()).isEqualTo(projectData.getClients());
            assertThat(p.getResources()).isEqualTo(projectData.getResources());
        }).expectComplete().verify();
    }

    @Test
    public void testGetProject() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(project.getClientId(Env.TEST), Env.TEST)).thenReturn(Mono.just(passportClient));
        StepVerifier.create(projectService.getProject(projectOwner, projectData.getId())).assertNext(p -> {
            assertThat(p.getId()).isEqualTo(projectData.getId());
            assertThat(p.getName()).isEqualTo(projectData.getName());
            assertThat(p.getType()).isEqualTo(projectData.getType());
            assertThat(p.getDescription()).isEqualTo(projectData.getDescription());
            assertThat(p.getOwner()).isEqualTo(projectData.getOwner());
            assertThat(p.getLogoUrl()).isEqualTo(projectData.getLogoUrl());
            assertThat(p.getAuthorizedGrantTypes()).isEqualTo(projectData.getAuthorizedGrantTypes());
            assertThat(p.getRegisteredRedirectUris()).isEqualTo(projectData.getRegisteredRedirectUris());
            assertThat(p.getClients()).isEqualTo(projectData.getClients());
            assertThat(p.getResources()).isEqualTo(projectData.getResources());
        }).expectComplete().verify();
    }

    @Test
    public void testCreateProject() {
        when(mongoProjectRepository.existsByName(project.getName())).thenReturn(Mono.just(false));
        when(passportService.createPassportClient(passportClientArgumentCaptor.capture(), any(Env.class))).thenReturn(Mono.just(passportClient));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(mongoProductRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(product, productOne)));

        StepVerifier.create(projectService.createProject(projectOwner, projectData)).assertNext(p -> {
            assertThat(p.getId()).isEqualTo(projectData.getId());
            assertThat(p.getName()).isEqualTo(projectData.getName());
            assertThat(p.getType()).isEqualTo(projectData.getType());
            assertThat(p.getDescription()).isEqualTo(projectData.getDescription());
            assertThat(p.getOwner()).isEqualTo(projectData.getOwner());
            assertThat(p.getLogoUrl()).isEqualTo(projectData.getLogoUrl());
            assertThat(p.getAuthorizedGrantTypes()).isEqualTo(projectData.getAuthorizedGrantTypes());
            assertThat(p.getRegisteredRedirectUris()).isEqualTo(projectData.getRegisteredRedirectUris());
            assertThat(p.getClients()).isEqualTo(projectData.getClients());
            assertThat(p.getResources()).isEqualTo(projectData.getResources());
            assertThat(passportClientArgumentCaptor.getValue().getResourceIds()).containsAll(resourceIds);
        }).expectComplete().verify();
    }

    @Test
    public void testUpdateProject() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(any(String.class), any(Env.class))).thenReturn(Mono.just(passportClient));
        when(passportService.updatePassportClient(passportClientArgumentCaptor.capture(), any(Env.class))).thenReturn(Mono.empty());
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(mongoProductRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(product, productOne)));

        StepVerifier.create(projectService.updateProject(projectOwner, projectData)).assertNext(p -> {
            assertThat(p.getId()).isEqualTo(projectData.getId());
            assertThat(p.getName()).isEqualTo(projectData.getName());
            assertThat(p.getType()).isEqualTo(projectData.getType());
            assertThat(p.getDescription()).isEqualTo(projectData.getDescription());
            assertThat(p.getOwner()).isEqualTo(projectData.getOwner());
            assertThat(p.getLogoUrl()).isEqualTo(projectData.getLogoUrl());
            assertThat(p.getAuthorizedGrantTypes()).isEqualTo(projectData.getAuthorizedGrantTypes());
            assertThat(p.getRegisteredRedirectUris()).isEqualTo(projectData.getRegisteredRedirectUris());
            assertThat(p.getClients()).isEqualTo(projectData.getClients());
            assertThat(p.getResources()).isEqualTo(projectData.getResources());
            assertThat(passportClientArgumentCaptor.getValue().getResourceIds()).containsAll(resourceIds);
        }).expectComplete().verify();
    }

    @Test
    public void testGetClientCredentials() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(project.getClientId(Env.TEST), Env.TEST)).thenReturn(Mono.just(passportClient));
        StepVerifier.create(projectService.getClientCredentials(projectOwner, project.getId(), Env.TEST)).assertNext(client -> {
            assertThat(client.getClientId()).isEqualTo(passportClient.getClientId());
            assertThat(client.getClientSecret()).isEqualTo(passportClient.getClientSecret());
        }).expectComplete().verify();
    }

    @Test
    public void testRefreshProject() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(any(String.class), any(Env.class))).thenReturn(Mono.just(passportClient));
        when(passportService.updatePassportClient(passportClientArgumentCaptor.capture(), any(Env.class))).thenReturn(Mono.empty());
        when(mongoProductRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(product, productOne)));
        StepVerifier.create(projectService.refreshAudiences(projectOwner, project.getId())).expectComplete().verify();
        assertThat(passportClientArgumentCaptor.getValue().getResourceIds()).containsAll(resourceIds);
    }

    @Test
    public void testGetRequestedResources() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        StepVerifier.create(projectService.getRequestedResources(projectOwner, projectData.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(productRequest.getName());
            assertThat(p.getDescription()).isEqualTo(productRequest.getDescription());
            assertThat(p.getResources()).isEqualTo(productRequest.getResources());
        }).expectComplete().verify();
    }

    @Test
    public void testSaveRequestedResources() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(mongoResourceRepository.findAllById(Set.of(resource.getId()))).thenReturn(Flux.fromIterable(Collections.singleton(resource)));
        when(mongoProductRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(product, productOne)));
        when(passportService.getPassportClient(project.getClientId(Env.TEST), Env.TEST)).thenReturn(Mono.just(passportClient));
        when(passportService.updatePassportClient(passportClient, Env.TEST)).thenReturn(Mono.empty());
        StepVerifier.create(projectService.saveRequestedResources(projectOwner, projectData.getId(), resources)).expectComplete().verify();
    }

    @Test
    public void testRequestProjectGoLive() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(project.getClientId(Env.TEST), Env.TEST)).thenReturn(Mono.just(passportClient));
        when(passportService.createPassportClient(passportClient, Env.LIVE)).thenReturn(Mono.just(passportClient));
        StepVerifier.create(projectService.requestProjectGoLive(projectOwner, projectData.getId())).expectComplete().verify();
    }

    @Test
    public void testGetApprovedResources() {
        project.setClientId("liveClientId", Env.LIVE);
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoResourceRepository.findAllById(Set.of(resource.getId()))).thenReturn(Flux.fromIterable(Collections.singleton(resource)));
        when(passportService.getPassportClient(project.getClientId(Env.LIVE), Env.LIVE)).thenReturn(Mono.just(passportClient));
        StepVerifier.create(projectService.getApprovedResources(projectOwner, projectData.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(productRequest.getName());
            assertThat(p.getDescription()).isEqualTo(productRequest.getDescription());
            assertThat(p.getResources()).isEqualTo(productRequest.getResources());
        }).expectComplete().verify();
    }

    @Test
    public void testSaveApprovedResources() {
        project.setClientId("liveClientId", Env.LIVE);
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(project.getClientId(Env.LIVE), Env.LIVE)).thenReturn(Mono.just(passportClient));
        when(passportService.updatePassportClient(passportClient, Env.LIVE)).thenReturn(Mono.empty());
        StepVerifier.create(projectService.saveApprovedResources(projectData.getId(), resources)).expectComplete().verify();
    }

    @Test
    public void testDeclineRequestedResources() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(mongoResourceRepository.findAllById(Set.of(resource.getId()))).thenReturn(Flux.fromIterable(Collections.singleton(resource)));
        when(passportService.getPassportClient(project.getClientId(Env.LIVE), Env.LIVE)).thenReturn(Mono.just(passportClient));
        when(passportService.updatePassportClient(passportClient, Env.LIVE)).thenReturn(Mono.empty());
        StepVerifier.create(projectService.declineRequestedResources(projectData.getId(), resources)).expectComplete().verify();
    }

    @Test
    public void testGetAvailableResources() {
        Resource newResource = new Resource();
        newResource.setId("resourceTwo");
        newResource.setProduct(product);
        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.setId(newResource.getId());

        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoResourceRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(resource, newResource)));
        when(mongoResourceRepository.findAllById(Set.of(resource.getId()))).thenReturn(Flux.fromIterable(Collections.singleton(resource)));
        when(passportService.getPassportClient(project.getClientId(Env.LIVE), Env.LIVE)).thenReturn(Mono.just(passportClient));
        StepVerifier.create(projectService.getAvailableResources(projectOwner, projectData.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(productRequest.getName());
            assertThat(p.getDescription()).isEqualTo(productRequest.getDescription());
            assertThat(p.getResources()).containsOnly(resourceRequest);
        }).expectComplete().verify();
    }

    @Test
    public void testGetPendingProjects() {
        Project projectTwo = new Project();
        projectTwo.setId("projectTwo");
        projectTwo.setName("projectTwo");
        projectTwo.setOwner(projectOwner);
        projectTwo.setType(Project.Type.mobile);
        when(mongoProjectRepository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(project, projectTwo)));
        StepVerifier.create(projectService.getPendingProjects()).assertNext(p -> {
            assertThat(p.getId()).isEqualTo(project.getId());
            assertThat(p.getName()).isEqualTo(project.getName());
            assertThat(p.getType()).isEqualTo(project.getType());
        }).expectComplete().verify();
    }
}
