package idv.fhm.demo.sec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers("/login", "/register", "/activate", "/swagger-ui/**",
										"/v3/api-docs/**", "/swagger-ui.html", "/webjars/**")
								.permitAll().anyRequest().authenticated())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();

//		http.csrf(csrf -> csrf.disable()) // 關掉 CSRF 保護（若是 REST API）
//				.authorizeHttpRequests(auth -> auth
//						// 👇 這裡列出你想跳過驗證的網址
//						.requestMatchers("/login", "/register", "/activate", "/swagger-ui/**", "/v3/api-docs/**",
//								"/swagger-ui.html", "/webjars/**")
//						.permitAll()
//						// 👇 其他路徑才需要驗證
//						.anyRequest().authenticated()).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//				.formLogin(form -> form.disable()) // 不要用預設登入頁面
//				.httpBasic(basic -> basic.disable()); // 不要彈出 basic auth 視窗

//		return http.build();
	}
}