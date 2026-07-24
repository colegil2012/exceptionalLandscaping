package com.studio.api.repository;

import com.studio.api.model.Inquiry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InquiryRepository extends MongoRepository<Inquiry, String> {
    List<Inquiry> findByStatusOrderByCreatedAtDesc(Inquiry.Status status);
}
