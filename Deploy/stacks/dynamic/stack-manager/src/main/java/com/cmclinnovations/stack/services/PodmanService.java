package com.cmclinnovations.stack.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cmclinnovations.stack.clients.core.StackClient;
import com.cmclinnovations.stack.clients.docker.PodmanClient;
import com.cmclinnovations.stack.services.config.ServiceConfig;
import com.cmclinnovations.swagger.podman.ApiException;
import com.cmclinnovations.swagger.podman.api.ContainersApi;
import com.cmclinnovations.swagger.podman.api.PodsApi;
import com.cmclinnovations.swagger.podman.api.SecretsApi;
import com.cmclinnovations.swagger.podman.model.BindOptions;
import com.cmclinnovations.swagger.podman.model.ContainerCreateResponse;
import com.cmclinnovations.swagger.podman.model.IDResponse;
import com.cmclinnovations.swagger.podman.model.ListContainer;
import com.cmclinnovations.swagger.podman.model.ListPodsReport;
import com.cmclinnovations.swagger.podman.model.NamedVolume;
import com.cmclinnovations.swagger.podman.model.Namespace;
import com.cmclinnovations.swagger.podman.model.PerNetworkOptions;
import com.cmclinnovations.swagger.podman.model.PodSpecGenerator;
import com.cmclinnovations.swagger.podman.model.PortMapping;
import com.cmclinnovations.swagger.podman.model.Secret;
import com.cmclinnovations.swagger.podman.model.SecretInfoReport;
import com.cmclinnovations.swagger.podman.model.SpecGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.command.ListPodsCmd;
import com.github.dockerjava.api.command.RemovePodCmd;
import com.github.dockerjava.api.model.BindPropagation;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.ContainerSpecConfig;
import com.github.dockerjava.api.model.ContainerSpecFile;
import com.github.dockerjava.api.model.ContainerSpecSecret;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.NetworkAttachmentConfig;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfigProtocol;
import com.github.dockerjava.api.model.ServiceSpec;

public class PodmanService extends DockerService {

    public static final String TYPE = "podman";

    public PodmanService(String stackName, ServiceManager serviceManager, ServiceConfig config) {
        super(stackName, serviceManager, config);

        addStackSecrets();
    }

    @Override
    public PodmanClient initClient(URI dockerUri) {
        return new PodmanClient(dockerUri);
    }

    @Override
    public PodmanClient getClient() {
        return (PodmanClient) super.getClient();
    }

    @Override
    protected void initialise(String stackName) {

        // addStackConfigs();

        createNetwork(stackName);
    }

    @Override
    public void addStackSecrets() {
        SecretsApi secretsApi = new SecretsApi(getClient().getPodmanClient());
        try {
            String stackName = StackClient.getStackName();
            List<SecretInfoReport> existingStackSecrets = secretsApi
                    .secretListLibpod(URLEncoder.encode("{\"name\":[\"^" + stackName + "_\"]}"));

            for (File secretFile : Path.of("/run/secrets").toFile()
                    .listFiles(file -> file.isFile() && !file.getName().startsWith(".git"))) {
                try (Stream<String> lines = Files.lines(secretFile.toPath())) {
                    String data = lines.collect(Collectors.joining("\n"));
                    String secretName = secretFile.getName();

                    String fullSecretName = StackClient.prependStackName(secretName);
                    Optional<SecretInfoReport> currentSecret = existingStackSecrets.stream()
                            .filter(secret -> secret.getSpec().getName().equals(fullSecretName))
                            .findFirst();
                    if (currentSecret.isEmpty()) {
                        getClient().addSecret(secretName, data);
                    } else {
                        existingStackSecrets.remove(currentSecret.get());
                    }
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to load secret file '" + secretFile.getAbsolutePath() + "'.",
                            ex);
                }
            }

            for (SecretInfoReport oldSecret : existingStackSecrets) {
                secretsApi.secretDeleteLibpod(oldSecret.getID(), null);
            }
        } catch (ApiException ex) {
            throw new RuntimeException("Failed to update secrets.", ex);
        }
    }

    private String getPodName(String containerName) {
        return containerName + "_pod";
    }

    @Override
    protected void addStackConfigs() {

        try {
            Files.walkFileTree(Path.of("/inputs/config"), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        if (Files.isReadable(file) && !file.getFileName().toString().startsWith(".git")) {
                            String configName = file.getFileName().toString();

                            try (Stream<String> lines = Files.lines(file)) {
                                String data = lines.collect(Collectors.joining("\n"));
                                dockerClient.addSecret(configName, data);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    } catch (IOException ex) {
                        throw new IOException("Failed to load config file '" + file + "'.", ex);
                    }
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configs.", ex);
        }
    }

    @Override
    protected Optional<Container> configureContainerWrapper(ContainerService service) {
        Optional<Container> container;
        removePod(service);

        container = startPod(service);
        return container;
    }

    private Optional<ListPodsReport> getPod(ContainerService service) {
        try (ListPodsCmd listPodsCmd = getClient().getInternalClient().listPodsCmd()) {
            return listPodsCmd.withNameFilter(List.of(getPodName(service.getContainerName())))
                    .exec().stream().findAny();
        }
    }

    private void removePod(ContainerService service) {
        Optional<ListPodsReport> pod = getPod(service);

        if (pod.isPresent()) {
            try (RemovePodCmd removePodCmd = getClient().getInternalClient()
                    .removePodCmd(pod.get().getId())) {
                removePodCmd.exec();
            }
        }
    }

    private Optional<Container> startPod(ContainerService service) {

        ServiceSpec serviceSpec = configureServiceSpec(service);

        String containerName = serviceSpec.getName();
        ContainerSpec containerSpec = serviceSpec.getTaskTemplate().getContainerSpec();
        PodSpecGenerator podSpecGenerator = new PodSpecGenerator()
                .name(getPodName(containerName))
                .hostname(containerName);
        EndpointSpec endpointSpec = serviceSpec.getEndpointSpec();
        if (null != endpointSpec) {
            List<PortConfig> ports = endpointSpec.getPorts();
            if (null != ports) {
                List<PortMapping> portMappings = ports.stream()
                        .map(port -> {
                            PortConfigProtocol protocol = port.getProtocol();
                            return new PortMapping()
                                    .containerPort(port.getTargetPort())
                                    .hostPort(port.getPublishedPort())
                                    .protocol(null == protocol ? null : protocol.name());
                        })
                        .collect(Collectors.toList());
                podSpecGenerator.portmappings(portMappings);
            }
        }
        List<NetworkAttachmentConfig> networks = serviceSpec.getTaskTemplate().getNetworks();
        if (null != networks) {
            podSpecGenerator.setNetns(new Namespace().nsmode("bridge"));
            podSpecGenerator.setNetworks(
                    networks.stream().collect(
                            Collectors.toMap(NetworkAttachmentConfig::getTarget,
                                    network -> {
                                        PerNetworkOptions perNetworkOptions = new PerNetworkOptions();
                                        perNetworkOptions.setAliases(List.of(containerName));
                                        return perNetworkOptions;
                                    })));
        }

        try {
            PodsApi podsApi = new PodsApi(getClient().getPodmanClient());
            IDResponse podIDResponse = podsApi.podCreateLibpod(podSpecGenerator);

            SpecGenerator containerSpecGenerator = new SpecGenerator();

            containerSpecGenerator.setName(containerName);
            containerSpecGenerator.setPod(podIDResponse.getId());
            containerSpecGenerator.setImage(containerSpec.getImage());
            containerSpecGenerator.setEnv(service.getConfig().getEnvironment());
            containerSpecGenerator.setEntrypoint(containerSpec.getCommand());
        List<ContainerSpecSecret> secrets = containerSpec.getSecrets();
        if (null != secrets) {
                containerSpecGenerator.setSecrets(secrets.stream()
                    .map(dockerSecret -> {
                            Secret secret = new Secret().source(dockerSecret.getSecretName());
                        ContainerSpecFile file = dockerSecret.getFile();
                            if (null != file) {
                                secret.target(file.getName())
                                .GID(Integer.parseInt(file.getGid()))
                                .UID(Integer.parseInt(file.getUid()))
                                .mode(file.getMode().intValue());
                            }
                            return secret;
                    })
                    .collect(Collectors.toList()));
        }
        List<ContainerSpecConfig> configs = containerSpec.getConfigs();
        if (null != configs) {
            configs.forEach(dockerConfig -> {
                    Secret config = new Secret().source(dockerConfig.getConfigName());
                ContainerSpecFile file = dockerConfig.getFile();
                if (null != file) {
                    Long mode = file.getMode();
                        config.target(file.getName())
                            .target("/" + dockerConfig.getConfigName())
                                .mode(mode == null ? null : Math.toIntExact(mode));
                }
                    containerSpecGenerator.addSecretsItem(config);
            });
        }
        List<com.github.dockerjava.api.model.Mount> dockerMounts = containerSpec.getMounts();
        if (null != dockerMounts) {
                /*
                 * TODO: This is roughly how this should be done but there is an issue with the
                 * Podman Swagger spec as described here
                 * https://github.com/containers/podman/issues/13717
                 * and here https://github.com/containers/podman/issues/13092
                 * Implementing the required changes to the Swagger spec can be done later if
                 * required.
                 */
                /*
                 * containerSpecGenerator.setMounts(dockerMounts.stream()
                 * .map(dockerMount -> new Mount()
                 * .source(dockerMount.getSource())
                 * .target(dockerMount.getTarget())
                 * .type(dockerMount.getType().name().toLowerCase())
                 * .readOnly(dockerMount.getReadOnly())
                 * .bindOptions(convertBindOptions(dockerMount.getBindOptions())))
                 * .collect(Collectors.toList()));
                 */
                // This is the temporary workaround but it only works for named volumes
                containerSpecGenerator.setVolumes(dockerMounts.stream()
                        .filter(dockerMount -> dockerMount.getType() == MountType.VOLUME)
                        .map(dockerMount -> new NamedVolume()
                                .name(dockerMount.getSource())
                                .dest(dockerMount.getTarget()))
                    .collect(Collectors.toList()));
        }
            containerSpecGenerator.setLabels(containerSpec.getLabels());

        try {
                ContainerCreateResponse containerCreateResponse = new ContainersApi(getClient().getPodmanClient())
                        .containerCreateLibpod(containerSpecGenerator);

            return getContainerIfCreated(service.getContainerName());
        } catch (ApiException ex) {
            throw new RuntimeException("Failed to create Podman Container '" + containerName + "''.", ex);
            }
        } catch (ApiException ex) {
            throw new RuntimeException("Failed to create Podman Pod '" + containerName + "''.", ex);
        }
    }

    private BindOptions convertBindOptions(com.github.dockerjava.api.model.BindOptions dockerBindOptions) {
        try {
            if (null == dockerBindOptions) {
                return null;
            } else {
                BindPropagation propagation = dockerBindOptions.getPropagation();
                BindOptions podmanBindOptions = new BindOptions();
                if (null != propagation) {
                    podmanBindOptions.propagation(new ObjectMapper().writeValueAsString(propagation));
                }
                return podmanBindOptions;
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Optional<Container> getContainerIfCreated(String containerName) {

        Optional<ListContainer> container;
        do {
            try {
                container = new ContainersApi(getClient().getPodmanClient())
                        .containerListLibpod(true, 1, null, null, null, null,
                                URLEncoder.encode("{\"name\":[\"" + containerName + "\"],\"pod\":[\""
                                        + getPodName(containerName) + "\"]}"))
                        .stream().findFirst();
            } catch (ApiException ex) {
                throw new RuntimeException("Failed to retrieve state of Container '" + containerName + "'.", ex);
            }

        } while (container.isEmpty());

        String containerId = container.get().getId();
        return getContainerFromID(containerId);
    }

}
