package com.saftyhub.project1;

import com.saftyhub.project1.model.Account_information;
import com.saftyhub.project1.model.Rules;
import com.saftyhub.project1.model.Users;
import com.saftyhub.project1.repository.AccountRepository;
import com.saftyhub.project1.repository.RulesRepository;
import com.saftyhub.project1.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RulesRepository rulesRepository;
    private final DataSource dataSource;

    public DataInitializer(
            UserRepository userRepository,
            AccountRepository accountRepository,
            RulesRepository rulesRepository,
            DataSource dataSource) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.rulesRepository = rulesRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        // Print the real DB name the app is connected to (helps debug safety_workspace vs safety_workspace_v2).
        try (Connection c = DataSourceUtils.getConnection(dataSource)) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    System.out.println("[DB] Connected to schema: " + rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("[DB] Could not detect schema name: " + e.getMessage());
        }

        // Create roles if they don't exist
        Rules managerRole = createRoleIfNotExist("Manager", "Manager Role");
        Rules adminRole = createRoleIfNotExist("Admin", "Admin Role");
        Rules userRole = createRoleIfNotExist("User", "User Role");

        // Create users with updated names
        createIfNotExist("Ahmed_Tarek456@outlook.com", "Abdullah adel", "1234", "Male", managerRole);
        createIfNotExist("Ahmedyay532@gmail.com", "veroxs", "1234", "Male", userRole);
        createIfNotExist("admin@safetyhub.com", "boda_veroxs", "1234", "Male", adminRole);
    }

    private Rules createRoleIfNotExist(String name, String desc) {
        return rulesRepository.findByName(name).orElseGet(() -> {
            Rules r = new Rules();
            r.setName(name);
            r.setDescription(desc);
            r.setCreatedAt(LocalDateTime.now());
            return rulesRepository.save(r);
        });
    }

    private void createIfNotExist(String email, String username, String password, String gender, Rules role) {
        if (accountRepository.findByAccountEmail(email).isEmpty()) {
            Users user = new Users();
            user.setUsername(username);
            user.setGender(gender);
            user.setJoinDate(LocalDate.now());
            user.setRule(role);
            user = userRepository.save(user);

            Account_information account = new Account_information();
            account.setAccountId(user.getUserId());
            account.setAccountEmail(email);
            account.setAccountPassword(password);
            accountRepository.save(account);
            
            System.out.println("Created " + role.getName() + " user: " + username + " (" + email + ")");
        } else {
            // Update name if already exists (optional, but good for user request)
            accountRepository.findByAccountEmail(email).ifPresent(acc -> {
                userRepository.findById(acc.getAccountId()).ifPresent(u -> {
                    u.setUsername(username);
                    userRepository.save(u);
                });
            });
        }
    }
}
