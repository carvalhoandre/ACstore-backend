package com.andrecarvalho.projeto.services;

import org.springframework.security.core.context.SecurityContextHolder;

import com.andrecarvalho.projeto.security.UserSS;

public class UserService {
	
	public static UserSS authenticated() {
		try {
			return(UserSS) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		} catch (Exception e) {
			return null;
		}
	}
}