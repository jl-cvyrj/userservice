package com.innowise.userservice.specification;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecifications {

    public static Specification<PaymentCard> hasHolder(String holder) {
        return (root, query, cb) -> {
            if (holder == null || holder.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("holder"), holder);
        };
    }
}
