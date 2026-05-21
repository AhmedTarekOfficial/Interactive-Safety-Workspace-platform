package com.saftyhub.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saftyhub.project1.model.ModuleVideos;
import java.util.List;

public interface ModuleVideosRepository extends JpaRepository<ModuleVideos, Integer> {
    List<ModuleVideos> findAll();
}

