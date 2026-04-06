package org.example.producer.repository;

import org.example.producer.model.Watermark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatermarkRepository extends JpaRepository<Watermark, String> {}
