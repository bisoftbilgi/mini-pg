package com.bisoft.minipg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bisoft.minipg.helper.SymmetricEncryptionUtil;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@Data
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final SymmetricEncryptionUtil symmetricEncryptionUtil;

    @Value("${minipg.username:postgres}")
    private String username;

    @Value("${minipg.password:postgres}")
    private String password;

    @Value("${bfm.user-crypted:false}")
    public boolean isEncrypted;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        if(isEncrypted){
            // password = symmetricEncryptionUtil.decrypt(password).replace("=","");
            password = symmetricEncryptionUtil.decrypt(password);
        }
        auth.inMemoryAuthentication()
                .withUser(username)
                .password(passwordEncoder().encode(password))
                .roles("USER").authorities("ROLE_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
