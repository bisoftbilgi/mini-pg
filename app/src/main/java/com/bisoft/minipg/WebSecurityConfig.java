package com.bisoft.minipg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import com.bisoft.minipg.helper.SymmetricEncryptionUtil;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final SymmetricEncryptionUtil symmetricEncryptionUtil;

    @Value("${minipg.username:postgres}")
    private String username;

    @Value("${minipg.password:postgres}")
    private String password;

    @Value("${bfm.user-crypted:false}")
    public boolean isEncrypted;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
		.authorizeHttpRequests(authorize -> authorize
			.anyRequest().authenticated()
		)
		.csrf(csrf -> csrf.disable())
		.formLogin(Customizer.withDefaults())
		.httpBasic(Customizer.withDefaults());
		
	return http.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
	 return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		if(isEncrypted){
            // password = symmetricEncryptionUtil.decrypt(password).replace("=","");
            password = symmetricEncryptionUtil.decrypt(password);
        }
		UserDetails user =
			 User.withUsername(username)
				.password(passwordEncoder().encode(password))
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(user);
	}

	
}