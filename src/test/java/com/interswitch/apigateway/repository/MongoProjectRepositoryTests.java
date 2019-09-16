package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.GrantType;
import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoProjectRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoProjectRepository mongoProjectRepository;

    @Test
    public void testCreate() {
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        project.setType(Project.Type.web);
        project.setDescription("testProjectDescription");
        project.setLogoUrl("");
        project.setClients(Map.of(Env.TEST, "testClient", Env.LIVE, "liveClient"));
        mongoProjectRepository.save(project).block();
        StepVerifier.create(mongoProjectRepository.findById(project.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(project.getName());
            assertThat(p.getDescription()).isEqualTo(project.getDescription());
            assertThat(p.getType()).isEqualTo(project.getType());
            assertThat(p.getAuthorizedGrantTypes()).isEqualTo(project.getAuthorizedGrantTypes());
            assertThat(p.getLogoUrl()).isEqualTo(project.getLogoUrl());
            assertThat(p.getClients()).isEqualTo(project.getClients());
            assertThat(p.getRegisteredRedirectUris()).isEqualTo(project.getRegisteredRedirectUris());
        }).expectComplete().verify();
    }

    @Test
    public void testFindById() {
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        project.setType(Project.Type.web);
        project.setDescription("testProjectDescription");
        project.setLogoUrl("");
        project.setClients(Map.of(Env.TEST, "testClient", Env.LIVE, "liveClient"));
        Project savedProject = mongoProjectRepository.save(project).block();
        StepVerifier.create(mongoProjectRepository.findById(project.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(project.getName()).isEqualTo(savedProject.getName());
            assertThat(p.getDescription()).isEqualTo(project.getDescription()).isEqualTo(savedProject.getDescription());
            assertThat(p.getType()).isEqualTo(project.getType()).isEqualTo(savedProject.getType());
            assertThat(p.getAuthorizedGrantTypes()).isEqualTo(project.getAuthorizedGrantTypes()).isEqualTo(savedProject.getAuthorizedGrantTypes());
            assertThat(p.getLogoUrl()).isEqualTo(project.getLogoUrl()).isEqualTo(savedProject.getLogoUrl());
            assertThat(p.getClients()).isEqualTo(project.getClients()).isEqualTo(savedProject.getClients());
            assertThat(p.getRegisteredRedirectUris()).isEqualTo(project.getRegisteredRedirectUris()).isEqualTo(savedProject.getRegisteredRedirectUris());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        Project p1 = new Project();
        p1.setId("testProjectOne");
        p1.setName("testProjectName");
        p1.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        p1.setType(Project.Type.web);
        p1.setDescription("testProjectDescription");
        p1.setLogoUrl("");
        p1.setClients(Map.of(Env.TEST, "testClient", Env.LIVE, "liveClient"));
        Project p2 = new Project();
        p2.setId("testProjectOne");
        p2.setName("testProjectName");
        p2.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        p2.setType(Project.Type.web);
        p2.setDescription("testProjectDescription");
        p2.setLogoUrl("");
        p2.setClients(Map.of(Env.TEST, "testClient", Env.LIVE, "liveClient"));
        mongoProjectRepository.save(p1).block();
        mongoProjectRepository.save(p2).block();
        StepVerifier.create(mongoProjectRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete() {
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        project.setType(Project.Type.web);
        project.setDescription("testProjectDescription");
        project.setLogoUrl("");
        project.setClients(Map.of(Env.TEST, "testClient", Env.LIVE, "liveClient"));
        Project savedProject = mongoProjectRepository.save(project).block();
        mongoProjectRepository.deleteById(savedProject.getId()).block();
        StepVerifier.create(mongoProjectRepository.findById(savedProject.getId())).expectComplete().verify();
    }

    @Test
    public void testAssignProduct() {
        Project project = new Project();
        project.setId("testProjectOne");
        Project savedProject = mongoProjectRepository.save(project).block();
        Resource resource = new Resource();
        resource.setId("test_resource_one");
        resource.setName("test_resource_one");
        savedProject.addResource(resource);
        mongoProjectRepository.save(savedProject).block();
        StepVerifier.create(mongoProjectRepository.findById(project.getId())).assertNext(c -> {
            assertThat(c.getResources()).hasSize(1);
        }).expectComplete().verify();
    }
}
