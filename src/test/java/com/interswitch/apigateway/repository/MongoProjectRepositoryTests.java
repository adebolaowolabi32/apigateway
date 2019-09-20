package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoProjectRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoProjectRepository mongoProjectRepository;

    @Test
    public void testFindById() {
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setType(Project.Type.web);
        project.setOwner("project.owner");
        project.setClientId("testClient", Env.TEST);
        project.setClientId("liveClient", Env.LIVE);
        mongoProjectRepository.save(project).block();
        StepVerifier.create(mongoProjectRepository.findById(project.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(project.getName());
            assertThat(p.getType()).isEqualTo(project.getType());
            assertThat(p.getClients()).isEqualTo(project.getClients());
        }).expectComplete().verify();
    }

    @Test
    public void testCreate() {
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setType(Project.Type.web);
        project.setOwner("project.owner");
        project.setClientId("testClient", Env.TEST);
        project.setClientId("liveClient", Env.LIVE);
        Project savedProject = mongoProjectRepository.save(project).block();
        StepVerifier.create(mongoProjectRepository.findById(project.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(project.getName()).isEqualTo(savedProject.getName());
            assertThat(p.getType()).isEqualTo(project.getType()).isEqualTo(savedProject.getType());
            assertThat(p.getOwner()).isEqualTo(project.getOwner()).isEqualTo(savedProject.getOwner());
            assertThat(p.getClients()).isEqualTo(project.getClients()).isEqualTo(savedProject.getClients());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        Project p1 = new Project();
        p1.setId("testProjectOne");
        p1.setName("testProjectName");
        p1.setType(Project.Type.web);
        p1.setOwner("project.owner");
        Project p2 = new Project();
        p2.setId("testProjectOne");
        p2.setName("testProjectName");
        p2.setType(Project.Type.web);
        p2.setOwner("project.owner");
        mongoProjectRepository.save(p1).block();
        mongoProjectRepository.save(p2).block();
        StepVerifier.create(mongoProjectRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete() {
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setType(Project.Type.web);
        mongoProjectRepository.save(project).block();
        mongoProjectRepository.deleteById(project.getId()).block();
        StepVerifier.create(mongoProjectRepository.findById(project.getId())).expectComplete().verify();
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
