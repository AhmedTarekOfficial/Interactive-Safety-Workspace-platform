package com.saftyhub.project1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.saftyhub.project1.model.*;
import com.saftyhub.project1.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RulesRepository rulesRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private CoursesInformationRepository coursesInformationRepository;
    
    @Autowired
    private CoursesCategoryRepository coursesCategoryRepository;
    
    @Autowired
    private CourseModulesRepository courseModulesRepository;

    @Autowired
    private ModuleVideosRepository moduleVideosRepository;
    
    // ========== USER MANAGEMENT ==========
    
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<Users> getManagers() {
        return userRepository.findAll().stream()
            .filter(u -> u.getRule() != null && "Manager".equalsIgnoreCase(u.getRule().getName()))
            .toList();
    }
    
    public List<Users> getEmployees() {
        return userRepository.findAll().stream()
            .filter(u -> u.getRule() != null && "Worker".equalsIgnoreCase(u.getRule().getName()))
            .toList();
    }
    
    public Optional<Users> getUserById(int userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public boolean createUser(String username, String phoneNumber, int jobId, int ruleId,
                              String email, String password) {
        try {
            Optional<job_information> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                System.out.println("Error: Job ID " + jobId + " not found");
                return false;
            }
            Optional<Rules> ruleOpt = rulesRepository.findById(ruleId);
            if (ruleOpt.isEmpty()) {
                System.out.println("Error: Rule ID " + ruleId + " not found");
                return false;
            }

            Users user = new Users();
            user.setUsername(username);
            user.setPhoneNumber(phoneNumber);
            user.setJoinDate(LocalDate.now());
            user.setJob(jobOpt.get());
            user.setRule(ruleOpt.get());
            user = userRepository.save(user);

            if (user.getUserId() == null) {
                throw new RuntimeException("Failed to generate user ID");
            }

            Account_information account = new Account_information();
            account.setAccountId(user.getUserId());
            account.setAccountEmail(email);
            account.setAccountPassword(password);
            accountRepository.save(account);
            
            System.out.println("Successfully created user: " + username + " with ID: " + user.getUserId());
            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + username);
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean deleteUser(int userId) {
        try {
            accountRepository.deleteByAccountIdNative(userId);
            Optional<Users> user = userRepository.findById(userId);
            if (user.isPresent()) {
                userRepository.delete(user.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateUser(int userId, String username, String phoneNumber, int jobId, 
                             int ruleId, String email, String password) {
        try {
            Optional<Users> userOpt = userRepository.findById(userId);
            Optional<Account_information> accountOpt = accountRepository.findById(userId);
            
            if (userOpt.isEmpty() || accountOpt.isEmpty()) {
                return false;
            }
            
            Users user = userOpt.get();
            Account_information account = accountOpt.get();
            
            user.setUsername(username);
            user.setPhoneNumber(phoneNumber);
            
            if (jobId > 0) {
                Optional<job_information> job = jobRepository.findById(jobId);
                job.ifPresent(user::setJob);
            }
            
            if (ruleId > 0) {
                Optional<Rules> rule = rulesRepository.findById(ruleId);
                rule.ifPresent(user::setRule);
            }
            
            if (email != null && !email.isEmpty()) {
                account.setAccountEmail(email);
            }
            
            if (password != null && !password.isEmpty()) {
                account.setAccountPassword(password);
            }
            
            userRepository.save(user);
            accountRepository.save(account);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== RULES MANAGEMENT ==========
    
    public List<Rules> getAllRules() {
        return rulesRepository.findAll();
    }
    
    public Optional<Rules> getRuleById(int ruleId) {
        return rulesRepository.findById(ruleId);
    }
    
    @Transactional
public boolean createRule(String name) {
    Rules rule = new Rules();
    rule.setName(name);
    rulesRepository.save(rule);
    return true;
}
    
    @Transactional
    public boolean updateRule(int ruleId, String name) {
        try {
            Optional<Rules> ruleOpt = rulesRepository.findById(ruleId);
            if (ruleOpt.isEmpty()) {
                return false;
            }
            Rules rule = ruleOpt.get();
            rule.setName(name);
            rulesRepository.save(rule);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Add this method to get users without rules

public List<Users> getUsersWithoutRules() {
    return userRepository.findByRuleIsNull();
}


// Add this method to assign a rule to a user
@Transactional
public boolean assignRuleToUser(int userId, int ruleId) {
    try {
        Optional<Users> userOpt = userRepository.findById(userId);
        Optional<Rules> ruleOpt = rulesRepository.findById(ruleId);
        
        if (userOpt.isEmpty() || ruleOpt.isEmpty()) {
            return false;
        }
        
        Users user = userOpt.get();
        user.setRule(ruleOpt.get());
        userRepository.save(user);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
    
    @Transactional
public boolean deleteRule(int ruleId) {
    try {
        // First, unassign all users from this rule
        List<Users> users = userRepository.findByRoleId(ruleId);
        users.forEach(user -> user.setRule(null));
        userRepository.saveAll(users);
        
        // Then delete the rule if it exists
        if (rulesRepository.existsById(ruleId)) {
            rulesRepository.deleteById(ruleId);
            return true;
        }
        return false;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}



    
    // ========== DEPARTMENT MANAGEMENT ==========
    
    public List<departments> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    public Optional<departments> getDepartmentById(int depId) {
        return departmentRepository.findById(depId);
    }
    
    @Transactional
    public boolean createDepartment(String depTitle, int departmentCapacity) {
        try {
            departments dept = new departments();
            dept.setDep_title(depTitle);
            dept.setDepartment_capacity(departmentCapacity);
            departmentRepository.save(dept);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateDepartment(int depId, String depTitle, int departmentCapacity) {
        try {
            Optional<departments> deptOpt = departmentRepository.findById(depId);
            if (deptOpt.isEmpty()) {
                return false;
            }
            departments dept = deptOpt.get();
            dept.setDep_title(depTitle);
            dept.setDepartment_capacity(departmentCapacity);
            departmentRepository.save(dept);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
   @Transactional
public boolean deleteDepartment(int depId) {
    try {
        // First, find all jobs in this department
        List<job_information> jobs = jobRepository.findByDepartmentId(depId);
        
        // // Option A: Delete all jobs in this department first
        // jobRepository.deleteAll(jobs);
        
        // OR Option B: Reassign jobs to a default department
        departments defaultDept = departmentRepository.findById(depId).orElse(null);
        jobs.forEach(job -> job.setDep(defaultDept));
        jobRepository.saveAll(jobs);
        
        // Now delete the department
        departmentRepository.deleteById(depId);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to delete department", e);
    }
}
    
    // ========== JOB MANAGEMENT ==========
    
    public List<job_information> getAllJobs() {
        return jobRepository.findAll();
    }
    
    public Optional<job_information> getJobById(int jobId) {
        return jobRepository.findById(jobId);
    }
    
    @Transactional
    public boolean createJob(String jobTitle, int departmentId) {
        try {
            Optional<departments> dept = departmentRepository.findById(departmentId);
            if (dept.isEmpty()) {
                return false;
            }
            job_information job = new job_information();
            job.setJob_title(jobTitle);
            job.setDep(dept.get());
            jobRepository.save(job);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateJob(int jobId, String jobTitle, int departmentId) {
        try {
            Optional<job_information> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isEmpty()) {
                return false;
            }
            Optional<departments> dept = departmentRepository.findById(departmentId);
            if (dept.isEmpty()) {
                return false;
            }
            job_information job = jobOpt.get();
            job.setJob_title(jobTitle);
            job.setDep(dept.get());
            jobRepository.save(job);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean deleteJob(int jobId) {
        try {
            Optional<job_information> job = jobRepository.findById(jobId);
            if (job.isPresent()) {
                jobRepository.delete(job.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== COURSE INFORMATION MANAGEMENT ==========
    
    public List<courses_information> getAllCourses() {
        return coursesInformationRepository.findAll();
    }
    
    public Optional<courses_information> getCourseById(int courseId) {
        return coursesInformationRepository.findById(courseId);
    }
    
    @Transactional
    public boolean createCourse(String courseTitle, courses_information.DifficultyStatus difficultyStatus, 
                               String courseDescription) {
        try {
            courses_information course = new courses_information();
            course.setCourseTitle(courseTitle);
            course.setDifficultyStatus(difficultyStatus);
            course.setCourseDescription(courseDescription);
            coursesInformationRepository.save(course);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateCourse(int courseId, String courseTitle, 
                              courses_information.DifficultyStatus difficultyStatus, 
                              String courseDescription) {
        try {
            Optional<courses_information> courseOpt = coursesInformationRepository.findById(courseId);
            if (courseOpt.isEmpty()) {
                return false;
            }
            courses_information course = courseOpt.get();
            course.setCourseTitle(courseTitle);
            course.setDifficultyStatus(difficultyStatus);
            course.setCourseDescription(courseDescription);
            coursesInformationRepository.save(course);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean deleteCourse(int courseId) {
        try {
            Optional<courses_information> course = coursesInformationRepository.findById(courseId);
            if (course.isPresent()) {
                coursesInformationRepository.delete(course.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== COURSE CATEGORY MANAGEMENT ==========
    
    public List<courses_category> getAllCourseCategories() {
        return coursesCategoryRepository.findAll();
    }
    
    public Optional<courses_category> getCourseCategoryById(int categoryId) {
        return coursesCategoryRepository.findById(categoryId);
    }
    
    @Transactional
    public boolean createCourseCategory(courses_category.CategoryMajor categoryMajor) {
        try {
            courses_category category = new courses_category();
            category.setCategoryMajor(categoryMajor);
            coursesCategoryRepository.save(category);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateCourseCategory(int categoryId, courses_category.CategoryMajor categoryMajor) {
        try {
            Optional<courses_category> categoryOpt = coursesCategoryRepository.findById(categoryId);
            if (categoryOpt.isEmpty()) {
                return false;
            }
            courses_category category = categoryOpt.get();
            category.setCategoryMajor(categoryMajor);
            coursesCategoryRepository.save(category);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean deleteCourseCategory(int categoryId) {
        try {
            Optional<courses_category> category = coursesCategoryRepository.findById(categoryId);
            if (category.isPresent()) {
                coursesCategoryRepository.delete(category.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== COURSE MODULES MANAGEMENT ==========
    
    public List<course_modules> getAllCourseModules() {
        return courseModulesRepository.findAll();
    }
    
    public Optional<course_modules> getCourseModuleById(int moduleId) {
        return courseModulesRepository.findById(moduleId);
    }
    
    @Transactional
    public boolean createCourseModule(String moduleName, int courseId) {
        try {
            Optional<courses_information> course = coursesInformationRepository.findById(courseId);
            if (course.isEmpty()) {
                return false;
            }
            course_modules module = new course_modules();
            module.setModuleName(moduleName);
            module.setCourse(course.get());
            courseModulesRepository.save(module);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateCourseModule(int moduleId, String moduleName, int courseId) {
        try {
            Optional<course_modules> moduleOpt = courseModulesRepository.findById(moduleId);
            if (moduleOpt.isEmpty()) {
                return false;
            }
            Optional<courses_information> course = coursesInformationRepository.findById(courseId);
            if (course.isEmpty()) {
                return false;
            }
            course_modules module = moduleOpt.get();
            module.setModuleName(moduleName);
            module.setCourse(course.get());
            courseModulesRepository.save(module);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean deleteCourseModule(int moduleId) {
        try {
            Optional<course_modules> module = courseModulesRepository.findById(moduleId);
            if (module.isPresent()) {
                courseModulesRepository.delete(module.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== MODULE VIDEOS MANAGEMENT ==========
    
    public List<ModuleVideos> getAllModuleVideos() {
        return moduleVideosRepository.findAll();
    }
    
    public Optional<ModuleVideos> getModuleVideoById(int videoId) {
        return moduleVideosRepository.findById(videoId);
    }
    
    @Transactional
    public boolean createModuleVideo(String videoPath, int moduleId) {
        try {
            Optional<course_modules> moduleOpt = courseModulesRepository.findById(moduleId);
            if (moduleOpt.isEmpty()) {
                return false;
            }
            ModuleVideos video = new ModuleVideos();
            video.setVideoPath(videoPath);
            video.setModule(moduleOpt.get());
            moduleVideosRepository.save(video);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean updateModuleVideo(int videoId, String videoPath, int moduleId) {
        try {
            Optional<ModuleVideos> videoOpt = moduleVideosRepository.findById(videoId);
            if (videoOpt.isEmpty()) {
                return false;
            }
            Optional<course_modules> moduleOpt = courseModulesRepository.findById(moduleId);
            if (moduleOpt.isEmpty()) {
                return false;
            }
            ModuleVideos video = videoOpt.get();
            video.setVideoPath(videoPath);
            video.setModule(moduleOpt.get());
            moduleVideosRepository.save(video);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Transactional
    public boolean deleteModuleVideo(int videoId) {
        try {
            Optional<ModuleVideos> video = moduleVideosRepository.findById(videoId);
            if (video.isPresent()) {
                moduleVideosRepository.delete(video.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

