package com.creazione.space_learning;

import com.creazione.space_learning.exception.CommandConflictException;
import com.creazione.space_learning.utils.Init;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Log4j2
@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
@DependsOn("dataSet")
public class SpaceLinguaApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		try {
			SpringApplication.run(SpaceLinguaApplication.class, args);
			Thread.sleep(15000);
			Init init = new Init();
			init.init();
			log.trace("Это TRACE сообщение!!!!!!!");
			log.debug("Это DEBUG сообщение!!!!!");
			log.info("Это INFO сообщение");
			log.warn("Это WARN сообщение");
			log.error("Это ERROR сообщение");
		} catch (CommandConflictException e) {
			// Красиво форматируем вывод ошибки
			System.err.println("\n" + "=".repeat(80));
			System.err.println("⛔ КРИТИЧЕСКАЯ ОШИБКА: КОНФЛИКТ КОМАНД");
			System.err.println("=".repeat(80));
			System.err.println(e.getMessage());
			System.err.println("=".repeat(80));
			System.err.println("ℹ️ Для решения проблемы:");
			System.err.println("1. Найдите указанные классы-дубликаты");
			System.err.println("2. Убедитесь, что команды уникальны");
			System.err.println("3. Перезапустите приложение");
			System.exit(1); // Завершаем приложение с кодом ошибки

		} catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128); // 128 бит
		byte[] key = keyGen.generateKey().getEncoded();
		String base64Key = Base64.getEncoder().encodeToString(key);
		System.out.println("Сгенерированный ключ: " + base64Key);
	}
}