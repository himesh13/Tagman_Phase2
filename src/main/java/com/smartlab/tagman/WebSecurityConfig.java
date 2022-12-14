package com.smartlab.tagman;



import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Qualifier("userDetailsServiceImpl")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource;
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	PortMapperImpl portMapper = new PortMapperImpl();
    	portMapper.setPortMappings(Collections.singletonMap("443","443"));
    	PortResolverImpl portResolver = new PortResolverImpl();
    	portResolver.setPortMapper(portMapper);
    	LoginUrlAuthenticationEntryPoint entryPoint = new LoginUrlAuthenticationEntryPoint(
    			"/login");
    	entryPoint.setPortMapper(portMapper);
    	entryPoint.setPortResolver(portResolver);
    	
        http
        .exceptionHandling()
		.authenticationEntryPoint(entryPoint)
		.and()
        .requiresChannel(chanel -> chanel.anyRequest().requiresSecure())
            .authorizeRequests()
                .antMatchers("/css/**", "/js/**", "/registration").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	//auth.jdbcAuthentication().dataSource(dataSource);
    	
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }
}