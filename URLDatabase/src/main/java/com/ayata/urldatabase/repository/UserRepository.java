package com.ayata.urldatabase.repository;

import com.ayata.urldatabase.model.database.Users;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<Users, String> {
    @Aggregation(pipeline = "{$match : {'phone': ?0}}")
    public Users findByPhone(String phone);

    @Aggregation(pipeline = "{$match : {'chw_id': ?0}}")
    public Users findByChwId(Integer chw_id);

    @Aggregation(pipeline = "{$count: 'phone'}")
    public int totalUsers();

    @Aggregation(pipeline = {"{$match: {'phone': {$ne : ''}}}",
                             "{$sort: {'chw_id': -1}}",
                             "{$limit: 1}"})
    public Users lastUser();
}
