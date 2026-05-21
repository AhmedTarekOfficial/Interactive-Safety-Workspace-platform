package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@Table(name = "users")
@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = true)
    private job_information job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_id", nullable = true)
    private Rules rule;

    // nullable=true so existing rows with NULL join_date don't throw on load
    @Column(name = "join_date", nullable = true)
    private LocalDate joinDate;

    @Column(name = "gender")
    private String gender; // "Male" or "Female"

    @Column(name = "profile_picture")
    private String profilePicture; // Filename of uploaded pic

    public Integer getUserId()              { return id; }
    public void setUserId(Integer userId)   { this.id = userId; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getPhoneNumber()          { return phoneNumber; }
    public void setPhoneNumber(String p)    { this.phoneNumber = p; }

    public LocalDate getJoinDate()          { return joinDate; }
    public void setJoinDate(LocalDate d)    { this.joinDate = d; }

    public job_information getJob()         { return job; }
    public void setJob(job_information j)   { this.job = j; }

    public Rules getRule()                  { return rule; }
    public void setRule(Rules r)            { this.rule = r; }

    public String getGender()               { return gender; }
    public void setGender(String g)         { this.gender = g; }

    public String getProfilePicture()       { return profilePicture; }
    public void setProfilePicture(String p) { this.profilePicture = p; }

    @Column(name = "warning_count", nullable = false, columnDefinition = "int default 0")
    private Integer warningCount = 0;
    public Integer getWarningCount()          { return warningCount != null ? warningCount : 0; }
    public void setWarningCount(Integer w)    { this.warningCount = w != null ? w : 0; }
}

