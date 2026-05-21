package com.saftyhub.project1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.saftyhub.project1.model.Users;
import com.saftyhub.project1.model.Account_information;
import com.saftyhub.project1.model.Rules;
import com.saftyhub.project1.model.departments;
import com.saftyhub.project1.model.job_information;
import com.saftyhub.project1.repository.UserRepository;
import com.saftyhub.project1.repository.AccountRepository;
import com.saftyhub.project1.repository.DepartmentRepository;
import com.saftyhub.project1.repository.JobRepository;
import com.saftyhub.project1.repository.RulesRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RulesRepository rulesRepository;

    // Get all users
    public List<Users> getAllUsers() {
        return userRepository.findAllWithJobDepAndRule();
    }
    
    // Get all job titles
    public List<job_information> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<departments> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Rules> getAllRoles() {
        return rulesRepository.findAll();
    }

    public List<job_information> getJobsByDepartmentId(int depId) {
        return jobRepository.findByDepartmentId(depId);
    }
    
    // Delete user and their account
    @Transactional
    public boolean deleteUser(int userId) {
        try {
            // Try to delete account using native query first (runs in separate transaction via REQUIRES_NEW)
            // If it fails, fall back to standard delete
            boolean accountDeleted = false;
            try {
                accountRepository.deleteByAccountIdNative(userId);
                accountDeleted = true;
            } catch (Exception e) {
                // Native query failed, try standard delete
                Optional<Account_information> account = accountRepository.findById(userId);
                if (account.isPresent()) {
                    accountRepository.delete(account.get());
                    accountDeleted = true;
                }
            }
            
            // Delete user
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
    
    // Register new employee
    @Transactional
    public boolean registerEmployee(String username, String phoneNumber, int jobId,
                                   Integer ruleId,
                                   String email, String password) {
        try {
            // Check if email already exists
            Optional<Account_information> existingAccount = accountRepository.findByAccountEmail(email);
            if (existingAccount.isPresent()) {
                System.out.println("Error: Email " + email + " already exists");
                return false;
            }
            
            // Create user first (ID will be auto-generated)
            Users user = new Users();
            user.setUserId(null);  // Explicitly ensure ID is null for auto-generation
            user.setUsername(username);
            user.setPhoneNumber(phoneNumber);
            user.setJoinDate(LocalDate.now());
            
            // Set job
            Optional<job_information> job = jobRepository.findById(jobId);
            if (job.isEmpty()) {
                System.out.println("Error: Job ID " + jobId + " not found");
                return false; // Invalid job ID
            }
            user.setJob(job.get());

            // Set role (optional)
            if (ruleId != null && ruleId > 0) {
                rulesRepository.findById(ruleId).ifPresent(user::setRule);
            }
            
            // Save user to get the auto-generated ID
            user = userRepository.save(user);
            
            // Verify the user was saved and got an ID
            if (user.getUserId() == null) {
                throw new RuntimeException("Failed to generate user ID");
            }
            
            // Create account with the same ID as the user
            Account_information account = new Account_information();
            account.setAccountId(user.getUserId());
            account.setAccountEmail(email);
            account.setAccountPassword(password);
            accountRepository.save(account);
            
            System.out.println("Successfully registered employee: " + username + " with ID: " + user.getUserId());
            return true;
        } catch (Exception e) {
            System.err.println("Error registering employee: " + username);
            e.printStackTrace();
            return false;
        }
    }
    
    // Update user information
    @Transactional
    public boolean updateUser(int userId, String username, String phoneNumber, 
                             Integer jobId,
                             Integer ruleId,
                             String email, String password) {
        try {
            Optional<Users> userOpt = userRepository.findById(userId);
            Optional<Account_information> accountOpt = accountRepository.findById(userId);
            
            if (userOpt.isEmpty() || accountOpt.isEmpty()) {
                return false;
            }
            
            Users user = userOpt.get();
            Account_information account = accountOpt.get();
            
            // Update user information
            user.setUsername(username);
            user.setPhoneNumber(phoneNumber);
            
            // Update job if provided
            if (jobId != null && jobId > 0) {
                jobRepository.findById(jobId).ifPresent(user::setJob);
            }

            // Update role if provided (allow clearing by sending 0)
            if (ruleId != null) {
                if (ruleId <= 0) {
                    user.setRule(null);
                } else {
                    rulesRepository.findById(ruleId).ifPresent(user::setRule);
                }
            }
            
            // Update account information
            if (email != null && !email.isEmpty()) {
                // Check if email is already taken by another user
                Optional<Account_information> existingAccount = accountRepository.findByAccountEmail(email);
                if (existingAccount.isPresent() && existingAccount.get().getAccountId() != userId) {
                    return false; // Email already taken
                }
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
    
    // Get user by ID
    public Optional<Users> getUserById(int userId) {
        return userRepository.findById(userId);
    }
    
    // Get account by user ID
    public Optional<Account_information> getAccountByUserId(int userId) {
        return accountRepository.findById(userId);
    }

    // Rename a department
    @Transactional
    public boolean renameDepartment(int depId, String newName) {
        try {
            Optional<departments> opt = departmentRepository.findById(depId);
            if (opt.isEmpty()) return false;
            departments dept = opt.get();
            dept.setDep_title(newName);
            departmentRepository.save(dept);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // Create a new department
    @Transactional
    public boolean createDepartment(String depTitle, int departmentCapacity) {
        try {
            departments dept = new departments();
            // `departments.dep_id` has no @GeneratedValue, so we must set it explicitly.
            Integer maxId = departmentRepository.findMaxDepId();
            int nextId = (maxId == null ? 0 : maxId) + 1;
            dept.setDep_id(nextId);
            dept.setDep_title(depTitle);
            dept.setDepartment_capacity(departmentCapacity);
            departmentRepository.save(dept);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update department title + capacity
    @Transactional
    public boolean updateDepartment(int depId, String depTitle, int departmentCapacity) {
        try {
            Optional<departments> opt = departmentRepository.findById(depId);
            if (opt.isEmpty()) return false;
            departments dept = opt.get();
            dept.setDep_title(depTitle);
            dept.setDepartment_capacity(departmentCapacity);
            departmentRepository.save(dept);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a department (nullify users' jobs if needed)
    @Transactional
    public boolean deleteDepartment(int depId) {
        try {
            // job.dep is nullable, so we null it first to avoid FK constraint failures
            List<job_information> jobs = jobRepository.findByDepartmentId(depId);
            jobs.forEach(j -> j.setDep(null));
            jobRepository.saveAll(jobs);

            departmentRepository.deleteById(depId);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

}