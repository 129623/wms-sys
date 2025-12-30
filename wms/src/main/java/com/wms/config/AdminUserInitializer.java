package com.wms.config;

import com.wms.entity.SysUser;
import com.wms.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking admin user...");
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, "admin");
        SysUser user = sysUserService.getOne(wrapper);

        String encodedPwd = passwordEncoder.encode("123456");

        if (user == null) {
            System.out.println("Initializing admin user...");
            user = new SysUser();
            user.setUsername("admin");
            user.setPassword(encodedPwd);
            user.setRealName("Administrator");
            user.setStatus(1);
            sysUserService.save(user);
            System.out.println("Admin user created with password '123456'");
        } else {
            System.out.println("Admin user exists. Resetting password...");
            user.setPassword(encodedPwd);
            user.setStatus(1); // Ensure active
            sysUserService.updateById(user);
            System.out.println("Admin user password reset to '123456'.");
        }
    }
}
