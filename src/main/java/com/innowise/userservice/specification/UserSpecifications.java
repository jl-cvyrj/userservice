package com.innowise.userservice.specification;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("name"), name);
        };
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("surname"), surname);
        };
    }
}
