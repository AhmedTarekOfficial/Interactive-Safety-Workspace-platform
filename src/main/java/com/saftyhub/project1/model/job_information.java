package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "job_information")
public class job_information {

    @Id
    @Column(name = "job_id")
    private int job_id;

    @Column(name = "job_title", nullable = false)
    private String job_title;

    // nullable=true - some jobs may not have a department
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dep_id", nullable = true)
    public departments dep;

    public int getJob_id()              { return job_id; }
    public void setJob_id(int id)       { this.job_id = id; }

    public String getJob_title()        { return job_title; }
    public void setJob_title(String t)  { this.job_title = t; }

    public departments getDep()         { return dep; }
    public void setDep(departments d)   { this.dep = d; }
}
