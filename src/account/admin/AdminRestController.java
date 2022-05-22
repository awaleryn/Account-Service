package account.admin;

import account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin/")
public class AdminRestController {

    private final AdminService adminService;

    @Autowired
    public AdminRestController(AdminService adminService) {
        this.adminService = adminService;
    }


    @PutMapping("user/role")
    public Map<String,Object> setAccountRole(@RequestBody AdminRequest request) {
        return adminService.setAccountRole(request);
    }

    @GetMapping("user/")
    public List<Account> getUsers() {
        return adminService.getAccounts();
    }

    @DeleteMapping("user/{email}")
    public Map<String, String> deleteUser(@PathVariable String email) {
        adminService.deleteAccount(email);
        return Map.of("user", email,
                        "status", "Deleted successfully!");
    }

    @PutMapping("/user/access")
    public Map<String, String> changeAccess(@RequestBody AdminAccessRequest request) {
        return adminService.changeAccess(request);
    }
}
