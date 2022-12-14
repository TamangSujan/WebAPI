package com.ayata.urldatabase.repository;

import com.ayata.urldatabase.model.database.Users;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChwRepository extends MongoRepository<Users, String> {
    @Aggregation(pipeline = {
            "{$match: {'phone': {$regex: //}}}",
            "{$skip: ?1}",
            "{$limit: ?0}"})
    public List<Users> getLimitPageUsers(int perPage, int currentPage);

    @Aggregation(pipeline = "{$match: {'phone': ?0}}")
    public Optional<Users> getByPhone(String phone);

    @Aggregation(pipeline = "{$match: {'chw_id': ?0}}")
    public Optional<Users> getByChwId(Integer chwId);
    @Aggregation(pipeline = "{$count: 'phone'}")
    public Integer totalUser();
}
