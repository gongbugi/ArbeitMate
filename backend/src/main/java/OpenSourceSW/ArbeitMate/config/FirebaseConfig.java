package OpenSourceSW.ArbeitMate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("!test") // test 실행시 예외
public class FirebaseConfig {

    @Bean
    public FirebaseApp initFirebaseAuth() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        // FileInputStream serviceAccount = new FileInputStream("src/main/resources/arbeitmate-firebase-adminsdk.json");

        InputStream serviceAccount = new ClassPathResource("arbeitmate-firebase-adminsdk.json").getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
