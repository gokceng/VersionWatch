package gg.version.watch.repo;

import gg.version.watch.model.Dependency;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Gokcen Guner
 * 08.12.2016
 */
@Repository
public interface DependencyRepository extends MongoRepository<Dependency, String> {

}
