package account.security;

import account.AccountDetailsService;
import account.admin.CustomAccessDeniedHandler;
import account.auditor.EventService;
import account.exception.CustomizedAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final AccountDetailsService accountDetailsService;
    private final EventService eventService;


    @Autowired
    public ApplicationSecurityConfig(PasswordEncoder passwordEncoder, AccountDetailsService accountDetailsService, EventService eventService) {
        this.passwordEncoder = passwordEncoder;
        this.accountDetailsService = accountDetailsService;
        this.eventService = eventService;

    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasAuthority("ROLE_ADMINISTRATOR")
                .antMatchers(HttpMethod.PUT,"/api/admin/user/**").hasAuthority("ROLE_ADMINISTRATOR")
                .antMatchers(HttpMethod.GET,"/api/admin/user/**").hasAuthority("ROLE_ADMINISTRATOR")
                .antMatchers(HttpMethod.PUT, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                .antMatchers(HttpMethod.POST, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                .antMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyAuthority("ROLE_ACCOUNTANT", "ROLE_USER")
                .antMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_ACCOUNTANT", "ROLE_USER")
                .antMatchers(HttpMethod.GET, "/api/security/events/").hasAuthority("ROLE_AUDITOR")
                .antMatchers(HttpMethod.POST, "/api/auth/signup").anonymous()
                .anyRequest().permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                .csrf().disable()
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(accountDetailsService);
        return provider;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(eventService);
    }

    @Bean
    public CustomizedAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new CustomizedAuthenticationEntryPoint();
    }

}
