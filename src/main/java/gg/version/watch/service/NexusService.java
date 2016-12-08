package gg.version.watch.service;

import gg.version.watch.model.Dependency;
import gg.version.watch.model.nexus.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gokcen Guner
 * 06.12.2016
 */
@Service
public class NexusService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NexusService.class);

  @Value("#{'${gg.version.watch.nexus.urls}'.split(',')}")
  private List<String> nexusUrls;

  private RestTemplate restTemplate;

  public NexusService() {
    this.restTemplate = new RestTemplateBuilder()
        .additionalMessageConverters(new Jaxb2RootElementHttpMessageConverter())
        .setConnectTimeout(3000)
        .setReadTimeout(5000)
        .build();
  }

  public Dependency retrieveDependency(String groupId, String artifactId) {
    String path = constructPath(groupId, artifactId);
    return getLatestDependency(path);
  }

  private String constructPath(String groupId, String artifactId) {
    String groupPath = StringUtils.replace(groupId, ".", "/");
    return String.format("%s/%s/maven-metadata.xml", groupPath, artifactId);
  }

  private Dependency getLatestDependency(String path) {
    List<Dependency> dependencies = new ArrayList<>();
    for (String nexusUrl : nexusUrls) {
      ResponseEntity<Metadata> metadataFromNexus = getMetadataFromNexus(nexusUrl, path);
      if (metadataFromNexus == null) {
        continue;
      }
      Metadata metadata = metadataFromNexus.getBody();
      if (!StringUtils.hasText(metadata.getGroupId()) || !StringUtils.hasText(metadata.getArtifactId())) {
        LOGGER.warn("Invalid Nexus response from {} for {}", nexusUrl, path);
        continue;
      }
      Dependency dependency = new Dependency(metadata);
      dependencies.add(dependency);
    }
    if (dependencies.isEmpty()) {
      LOGGER.error("Error for path {}", path);
      return null;
    }
    Collections.sort(dependencies);
    return dependencies.get(0);
  }

  private ResponseEntity<Metadata> getMetadataFromNexus(String nexusUrl, String artifactPath) {
    ResponseEntity<Metadata> metadataResponseEntity;
    String url = nexusUrl.endsWith("/") ? nexusUrl + artifactPath : nexusUrl + "/" + artifactPath;
    try {
      metadataResponseEntity = restTemplate.getForEntity(url, Metadata.class);
    } catch (HttpClientErrorException e) {
//      HttpStatus statusCode = e.getStatusCode();
//      LOGGER.warn("{} - {}", statusCode, url);
      return null;
    } catch (RestClientException e) {
//      LOGGER.warn("Error! Nexus: {}. Message:{}", url, e.getLocalizedMessage());
      return null;
    }
    HttpStatus statusCode = metadataResponseEntity.getStatusCode();
    if (!HttpStatus.OK.equals(statusCode)) {
      LOGGER.error("Code:{} Can't get metadata for {}.", statusCode, artifactPath);
      return null;
    }
    return metadataResponseEntity;
  }
}
