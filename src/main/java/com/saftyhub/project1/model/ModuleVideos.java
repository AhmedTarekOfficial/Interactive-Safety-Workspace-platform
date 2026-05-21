package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "module_vedioes")
public class ModuleVideos {

    @Id
    @Column(name = "Video_id")
    private Integer videoId;

    @Column(name = "video_path", length = 200)
    private String videoPath;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private course_modules module;

    // Explicit setters (in case Lombok isn't processing)
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setModule(course_modules module) {
        this.module = module;
    }
}

