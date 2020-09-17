package com.andrecarvalho.projeto.services;

import org.springframework.mail.SimpleMailMessage;
import com.andrecarvalho.projeto.domain.Pedido;

public interface EmailService {
	
	void sendOrderConfirmationEmail(Pedido obj);
	
	void sendEmail(SimpleMailMessage msg);
	
}