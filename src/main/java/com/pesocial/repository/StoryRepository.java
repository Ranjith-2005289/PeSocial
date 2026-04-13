package com.pesocial.repository;

import com.pesocial.model.Story;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends MongoRepository<Story, String> {
  List<Story> findByAuthorIdOrderByTimestampDesc(String authorId);

  @Query("{ 'authorId': { $in: ?0 }, 'timestamp': { $gt: ?1 } }")
  List<Story> findActiveStoriesByAuthors(List<String> authorIds, LocalDateTime since);

  List<Story> findByAuthorIdAndTimestampGreaterThan(String authorId, LocalDateTime since);
}
