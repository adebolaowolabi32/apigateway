package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.*;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
import com.interswitch.apigateway.repository.MongoUserRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.interswitch.apigateway.util.FilterUtil.isInterswitchEmail;

@Service
public class ProjectService {
    private MongoProjectRepository mongoProjectRepository;
    private MongoProductRepository mongoProductRepository;
    private MongoResourceRepository mongoResourceRepository;
    private MongoUserRepository mongoUserRepository;
    private PassportService passportService;

    public ProjectService(MongoProjectRepository mongoProjectRepository, MongoProductRepository mongoProductRepository, MongoResourceRepository mongoResourceRepository, MongoUserRepository mongoUserRepository, PassportService passportService) {
        this.mongoProjectRepository = mongoProjectRepository;
        this.mongoProductRepository = mongoProductRepository;
        this.mongoResourceRepository = mongoResourceRepository;
        this.mongoUserRepository = mongoUserRepository;
        this.passportService = passportService;
    }

    private static ProjectData from(Project project) {
        ProjectData projectData = new ProjectData();
        projectData.setId(project.getId());
        projectData.setName(project.getName());
        projectData.setOwner(project.getOwner());
        projectData.setType(project.getType());
        projectData.setClients(project.getClients());
        projectData.setResources(project.getResources());
        return projectData;
    }

    private static ProjectData from(ProjectData projectData, PassportClient passportClient) {
        projectData.setName(passportClient.getClientName());
        projectData.setDescription(passportClient.getDescription());
        projectData.setLogoUrl(passportClient.getLogoUrl());
        projectData.setRegisteredRedirectUris(passportClient.getRegisteredRedirectUri());
        projectData.setAuthorizedGrantTypes(passportClient.getAuthorizedGrantTypes().stream().map(GrantType::valueOf).collect(Collectors.toSet()));
        return projectData;
    }

    private static Project from(ProjectData projectData) {
        Project project = new Project();
        project.setId(projectData.getId());
        project.setName(projectData.getName());
        project.setOwner(projectData.getOwner());
        project.setType(projectData.getType());
        project.setClients(projectData.getClients());
        project.setResources(projectData.getResources());
        return project;
    }

    private static Flux<ProductRequest> parseResources(Flux<Resource> resources) {
        if (resources != null) {
            List<ProductRequest> requested = new ArrayList<>();
            return resources.flatMap(resource -> {
                Product product = resource.getProduct();
                ProductRequest requestedProduct = new ProductRequest();
                ResourceRequest requestedResource = new ResourceRequest();
                requestedResource.setId(resource.getId());
                requestedResource.setName(resource.getName());
                requestedProduct.setName(product.getName());
                requestedProduct.setDescription(product.getDescription());
                AtomicBoolean exists = new AtomicBoolean(false);
                for (var request : requested) {
                    if (request.getName().equalsIgnoreCase(product.getName())) {
                        Set<ResourceRequest> listOfRequestedResources = request.getResources();
                        listOfRequestedResources.add(requestedResource);
                        request.setResources(listOfRequestedResources);
                        exists.set(true);
                        break;
                    }
                }
                if (!exists.get()) {
                    Set<ResourceRequest> listOfRequestedResources = new LinkedHashSet<>();
                    listOfRequestedResources.add(requestedResource);
                    requestedProduct.setResources(listOfRequestedResources);
                    requested.add(requestedProduct);
                }
                return Mono.empty();
            }).thenMany(Flux.fromIterable(requested));
        }
        return Flux.empty();
    }

    private static Set<String> getApi_resources(Map<String, Object> additionalInformation) {
        Set<String> listOfResources = new LinkedHashSet<>();
        var api_resourcesObj = additionalInformation.get("api_resources");
        if (api_resourcesObj instanceof ArrayList)
            listOfResources = new LinkedHashSet<>((ArrayList) api_resourcesObj);
        return listOfResources;
    }

    public Flux<Resource> getResourcesForUser(String projectOwner) {
        if (isInterswitchEmail(projectOwner))
            return mongoResourceRepository.findAll();
        return mongoResourceRepository.findAll().filter(resource -> resource.getProduct().getCategory().equals(Product.Category.PUBLIC));
    }

    private static PassportClient buildPassportClient(PassportClient passportClient, ProjectData project) {
        passportClient.setClientName(project.getName());
        passportClient.setDescription(project.getDescription());
        passportClient.setClientOwner(project.getOwner());
        passportClient.setScope(Collections.singleton("profile"));
        passportClient.setAuthorizedGrantTypes(project.getAuthorizedGrantTypes().stream().map(Enum::name).collect(Collectors.toSet()));
        passportClient.setRegisteredRedirectUri(project.getRegisteredRedirectUris());
        passportClient.setLogoUrl(project.getLogoUrl());
        int accessTokenValiditySeconds;
        int refreshTokenValiditySeconds;
        if (project.getType().equals(Project.Type.web)) {
            accessTokenValiditySeconds = 1800;
            refreshTokenValiditySeconds = 3600;
        } else {
            accessTokenValiditySeconds = 3600;
            refreshTokenValiditySeconds = 7200;
        }
        passportClient.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        passportClient.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
        return passportClient;
    }

    private static PassportClient setResourceIds(PassportClient passportClient, Set<String> audiences) {
        Set<String> resourceIds = new HashSet<>();
        resourceIds.addAll(passportClient.getResourceIds());
        resourceIds.addAll(audiences);
        passportClient.setResourceIds(resourceIds);
        return passportClient;
    }

    private Mono<PassportClient> loadAudiencesIntoClient(PassportClient passportClient, Env env) {
        if (env.toString().equalsIgnoreCase(Env.TEST.toString())) {
            Set<String> audiences = new LinkedHashSet<>(Set.of("api-gateway", "passport"));
            return mongoProductRepository.findAll().flatMap(product -> {
                audiences.addAll(product.getAudiences());
                return Mono.empty();
            }).then(Mono.defer(() -> Mono.just(setResourceIds(passportClient, audiences))));
        }
        return Mono.just(passportClient);
    }

    public Flux<ProjectData> getAllProjects(String projectOwner) {
        Map<String, ProjectData> projects = new LinkedHashMap<>();
        return mongoProjectRepository.findByOwner(projectOwner)
                .flatMap(project -> {
                    ProjectData projectData = from(project);
                    projects.put(projectData.getName(), projectData);
                    return Mono.empty();
                }).thenMany(passportService.getPassportClients(projectOwner, Env.TEST).flatMap(passportClient -> {
                    ProjectData projectData = projects.get(passportClient.getClientName());
                    if (projectData != null)
                        return Mono.just(from(projectData, passportClient));
                    return Mono.empty();
                }));
    }

    public Mono<ProjectData> getProject(String projectOwner, String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        String clientId = project.getClientId(Env.TEST);
                        if (clientId.isEmpty()) return Mono.empty();
                        return passportService.getPassportClient(clientId, Env.TEST)
                                .flatMap(passportClient -> Mono.just(from(from(project), passportClient)));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view this project because you are not its owner"));
                });
    }

    public Mono<ProjectData> createProject(String projectOwner, ProjectData projectData) {
        String name = projectData.getName().trim().toLowerCase();
        return mongoProjectRepository.existsByName(name)
                .flatMap(exists -> {
                    if (exists)
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Project already exists"));
                    projectData.setName(name);
                    projectData.setOwner(projectOwner);
                    projectData.setClients(new LinkedHashMap<>());
                    projectData.setResources(new LinkedHashSet<>());
                    Project project = from(projectData);
                    PassportClient passportClient = buildPassportClient(new PassportClient(), projectData);
                    passportClient.setAdditionalInformation(Map.of("env", Env.TEST));
                    return loadAudiencesIntoClient(passportClient, Env.TEST).flatMap(updatedClient ->
                            passportService.createPassportClient(updatedClient, Env.TEST)
                                    .flatMap(createdClient -> {
                                        project.setClientId(createdClient.getClientId(), Env.TEST);
                                        return mongoProjectRepository.save(project)
                                                .flatMap(p -> {
                                                    projectData.setId(p.getId());
                                                    return Mono.just(projectData);
                                                });
                                    }));
                });
    }

    public Mono<ProjectData> updateProject(String projectOwner, ProjectData projectData) {
        return mongoProjectRepository.findById(projectData.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        String name = projectData.getName().trim().toLowerCase();
                        projectData.setName(name);
                        projectData.setOwner(project.getOwner());
                        projectData.setClients(project.getClients());
                        projectData.setResources(project.getResources());
                        return Flux.fromArray(Env.values()).flatMap(env -> {
                            String clientId = project.getClientId(env);
                            if (!clientId.isEmpty()) {
                                return passportService.getPassportClient(clientId, env)
                                        .flatMap(retrievedClient -> {
                                            PassportClient passportClient = buildPassportClient(retrievedClient, projectData);
                                            return loadAudiencesIntoClient(passportClient, env)
                                                    .flatMap(updatedPassportClient ->
                                                            passportService.updatePassportClient(updatedPassportClient, env));
                                        });
                            }
                            return Mono.empty();
                        }).then(mongoProjectRepository.save(from(projectData))).then(Mono.just(projectData));
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not modify this project because you are not its owner"));
                    }
                });
    }

    public Mono<Client> getClientCredentials(String projectOwner, String projectId, Env env) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        Client client = new Client();
                        client.setClientId("");
                        client.setClientSecret("");
                        String clientId = project.getClientId(env);
                        if (!clientId.isEmpty()) {
                            return passportService.getPassportClient(clientId, env)
                                    .flatMap(passportClient -> {
                                        client.setClientId(passportClient.getClientId());
                                        client.setClientSecret(passportClient.getClientSecret());
                                        return Mono.just(client);
                                    });
                        }
                        return Mono.just(client);
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view credentials for this project because you are not its owner"));
                });
    }

    public Mono<Void> refreshAudiences(String projectOwner, String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        String clientId = project.getClientId(Env.TEST);
                        if (!clientId.isEmpty()) {
                            return passportService.getPassportClient(clientId, Env.TEST)
                                    .flatMap(passportClient ->
                                            loadAudiencesIntoClient(passportClient, Env.TEST)
                                                    .flatMap(updatedPassportClient ->
                                                            passportService.updatePassportClient(updatedPassportClient, Env.TEST)));
                        }
                        return Mono.empty();
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not refresh credentials for this project because you are not its owner"));
                });
    }

    public Flux<Project> getPendingProjects() {
        return mongoProjectRepository.findAll()
                .filter(project -> !project.getResources().isEmpty());
    }

    public Flux<ProductRequest> getAvailableResources(String projectOwner, String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMapMany(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        Set<Resource> availableResources = new LinkedHashSet<>();
                        Set<Resource> requestedResources = project.getResources();
                        Set<Resource> resourcesToBeExcluded = new LinkedHashSet<>(requestedResources);
                        return getApprovedResources(project).flatMap(resource -> {
                            resourcesToBeExcluded.add(resource);
                            return Flux.empty();
                        }).thenMany(getResourcesForUser(projectOwner).flatMap(resource -> {
                            if (!resourcesToBeExcluded.contains(resource)) availableResources.add(resource);
                            return Flux.empty();
                        })).thenMany(parseResources(Flux.fromIterable(availableResources)));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You cannot view these resources because you are not the owner of this project"));
                });
    }

    public Flux<ProductRequest> getRequestedResources(String projectOwner, String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMapMany(project -> {
                    if (projectOwner.equals(project.getOwner()))
                        return parseResources(Flux.fromIterable(project.getResources()));
                    return mongoUserRepository.findByUsername(projectOwner)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to view these resources")))
                            .flatMapMany(user -> {
                                if (user.getRole().equals(User.Role.ADMIN))
                                    return parseResources(Flux.fromIterable(project.getResources()));
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to view these resources"));
                            });
                });
    }

    public Mono<Void> saveRequestedResources(String projectOwner, String projectId, Map<String, LinkedHashSet<String>> request) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        Set<String> resources = request.get("resources");
                        if (!resources.isEmpty()) {
                            return Flux.fromIterable(resources).flatMap(r ->
                                    mongoResourceRepository.findById(r)
                                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more of the requested resources do not exist")))
                                            .flatMap(resource -> {
                                                if (!isInterswitchEmail(projectOwner))
                                                    if (resource.getProduct().getCategory().equals(Product.Category.RESTRICTED))
                                                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have permission to request for this resource"));
                                                project.addResource(resource);
                                                return Mono.empty();
                                            })).then(Mono.defer(() -> {
                                String clientId = project.getClientId(Env.TEST);
                                if (!clientId.isEmpty()) {
                                    return passportService.getPassportClient(clientId, Env.TEST)
                                            .flatMap(passportClient -> loadAudiencesIntoClient(passportClient, Env.TEST)
                                                    .flatMap(updatedPassportClient ->
                                                            passportService.updatePassportClient(updatedPassportClient, Env.TEST)
                                                                    .then(mongoProjectRepository.save(project))
                                                                    .then(Mono.empty())));
                                }
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Project does not have a test client on Passport"));
                            }));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources list cannot be null or empty"));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not request for these resources because you are not the owner of this project"));
                });
    }

    public Mono<Void> requestProjectGoLive(String projectOwner, String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        String liveClientId = project.getClientId(Env.LIVE);
                        if (liveClientId == null || liveClientId.isEmpty()) {
                            String testClientId = project.getClientId(Env.TEST);
                            if (!testClientId.isEmpty()) {
                                return passportService.getPassportClient(testClientId, Env.TEST)
                                        .flatMap(passportClient -> {
                                            passportClient.setClientId(null);
                                            passportClient.setClientSecret(null);
                                            passportClient.setResourceIds(Set.of("api-gateway", "passport"));
                                            passportClient.setAdditionalInformation(Map.of("env", Env.LIVE));
                                            return passportService.createPassportClient(passportClient, Env.LIVE)
                                                    .flatMap(createdClient -> {
                                                        project.setClientId(createdClient.getClientId(), Env.LIVE);
                                                        return mongoProjectRepository.save(project)
                                                                .then(Mono.empty());
                                                    });
                                        });
                            }
                            return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Project does not have a test client on Passport"));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "This project is already live"));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You cannot go live with this project because you are not its owner"));

                });
    }

    public Flux<ProductRequest> getApprovedResources(String projectOwner, String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMapMany(project -> {
                    if (projectOwner.equals(project.getOwner())) {
                        return parseResources(getApprovedResources(project));
                    }
                    return mongoUserRepository.findByUsername(projectOwner)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to view these resources")))
                            .flatMapMany(user -> {
                                if (user.getRole().equals(User.Role.ADMIN))
                                    return parseResources(getApprovedResources(project));
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to view these resources"));
                            });
                });
    }

    public Mono<Void> saveApprovedResources(String projectId, Map<String, LinkedHashSet<String>> request) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    Set<String> resources = request.get("resources");
                    if (!resources.isEmpty()) {
                        Set<String> api_resources = new LinkedHashSet<>();
                        Set<String> audiences = new LinkedHashSet<>();
                        return Flux.fromIterable(resources).flatMap(r -> {
                            var resourceOptional = project.getResource(r);
                            return Mono.fromRunnable(() -> resourceOptional.ifPresentOrElse(resource -> {
                                String resourceString = resource.getId() + "-" + resource.getMethod() + resource.getPath();
                                api_resources.add(resourceString);
                                audiences.addAll(resource.getProduct().getAudiences());
                                project.removeResource(resource);
                            }, () -> {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more of these resources has not been requested for");
                            })).then(Mono.defer(() -> {
                                Resource resource = resourceOptional.get();
                                Product product = resource.getProduct();
                                product.addProject(project);
                                return mongoProductRepository.save(product);
                            }));
                        }).then(Mono.defer(() -> {
                            String clientId = project.getClientId(Env.LIVE);
                            if (!clientId.isEmpty()) {
                                return passportService.getPassportClient(clientId, Env.LIVE)
                                        .flatMap(passportClient -> {
                                            setResourceIds(passportClient, audiences);
                                            Map<String, Object> additionalInformation = passportClient.getAdditionalInformation();
                                            Set<String> listOfResources = getApi_resources(additionalInformation);
                                            listOfResources.addAll(api_resources);
                                            additionalInformation.put("api_resources", listOfResources);
                                            passportClient.setAdditionalInformation(additionalInformation);
                                            return passportService.updatePassportClient(passportClient, Env.LIVE)
                                                    .then(mongoProjectRepository.save(project))
                                                    .then(Mono.empty());
                                        });
                            }
                            return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Project does not have a live client on Passport"));
                        }));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources list cannot be empty"));
                });
    }

    public Mono<Void> declineRequestedResources(String projectId, Map<String, LinkedHashSet<String>> request) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    Set<String> resources = request.get("resources");
                    Set<String> api_resources = new HashSet<>();
                    if (!resources.isEmpty()) {
                        return Flux.fromIterable(resources).flatMap(r ->
                                mongoResourceRepository.findById(r)
                                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more of these resources has not been requested for")))
                                        .flatMap(resource -> {
                                            if (project.getResources().contains(resource))
                                                project.removeResource(resource);
                                            else {
                                                api_resources.add(resource.getId() + "-" + resource.getMethod() + resource.getPath());
                                            }
                                            return Mono.empty();
                                        })).thenMany(Flux.defer(() -> {
                                    if (api_resources.isEmpty()) return Mono.empty();
                                    String clientId = project.getClientId(Env.LIVE);
                                    if (!clientId.isEmpty()) {
                                        return passportService.getPassportClient(clientId, Env.LIVE).flatMap(passportClient -> {
                                            Map<String, Object> additionalInformation = passportClient.getAdditionalInformation();
                                            Set<String> approvedResources = getApi_resources(additionalInformation);
                                            approvedResources.removeAll(api_resources);
                                            additionalInformation.put("api_resources", approvedResources);
                                            passportClient.setAdditionalInformation(additionalInformation);
                                            return passportService.updatePassportClient(passportClient, Env.LIVE);
                                        });
                                    }
                                    return Mono.empty();
                        })).then(mongoProjectRepository.save(project)).then(Mono.empty());

                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources list cannot be empty"));
                });
    }

    private Flux<Resource> getApprovedResources(Project project) {
        String clientId = project.getClientId(Env.LIVE);
        if (!clientId.isEmpty()) {
            return passportService.getPassportClient(clientId, Env.LIVE).flatMapMany(passportClient -> {
                Map<String, Object> additionalInformation = passportClient.getAdditionalInformation();
                Set<String> resources = new LinkedHashSet<>();
                Set<String> api_resources = getApi_resources(additionalInformation);
                api_resources.forEach(api_resource -> {
                    String resourceId = api_resource.substring(0, api_resource.indexOf("-"));
                    if (!resourceId.isEmpty()) resources.add(resourceId);
                });
                return mongoResourceRepository.findAllById(resources);
            });
        }
        return Flux.empty();
    }
}
